 package db;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.LinkedList;
 import java.util.List;
 
 import models.Change;
 import models.Commit;
 import models.User;
 
 
 public class CallGraphDb extends DbConnection
 {
 	public CallGraphDb()
 	{
 		super();
 	}
 	
 	/**
 	 * Gets all of the change objects associated including and before a given commit.
 	 * @param CommitId
 	 * @return
 	 */
 	public List<Change> getAllOwnerChangesBefore(String CommitId)
 	{
 		try 
 		{
 			LinkedList<Change> changes = new LinkedList<Change>();
 			String sql = "SELECT commit_id, file_id, owner_id, char_start, char_end, change_type FROM owners natural join commits where commit_date <= (select commit_date from commits where commit_id=?)" +
 					"and (branch_id is NULL OR branch_id=?) order by commit_date, commit_id, char_start;"; 
 			String[] parms = {CommitId, branchID};
 			ResultSet rs = execPreparedQuery(sql, parms);
 			while(rs.next())
 			{
 				changes.add(new Change(rs.getString("owner_id"), rs.getString("commit_id"), Resources.ChangeType.valueOf(rs.getString("change_type")), rs.getString("file_id"), rs.getInt("char_start"), 
 						rs.getInt("char_end")));
 			}
 			return changes;
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public List<Change> getAllFileOwnerChangesBefore(String FileId, String CommitId)
 	{
 		try 
 		{
 			LinkedList<Change> changes = new LinkedList<Change>();
 			String sql = "SELECT commit_id, file_id, owner_id, char_start, char_end, change_type FROM owners natural join commits where commit_date <= (select commit_date from commits where commit_id=?)" +
 					"and (branch_id is NULL OR branch_id=?) and file_id=? order by commit_date, commit_id, line_start;"; 
 			String[] parms = {CommitId, branchID, FileId};
 			ResultSet rs = execPreparedQuery(sql, parms);
 			while(rs.next())
 			{
 				changes.add(new Change(rs.getString("owner_id"), rs.getString("commit_id"), 
 						Resources.ChangeType.valueOf(rs.getString("change_type")), rs.getString("file_id"),
 						rs.getInt("line_start"), rs.getInt("line_end")));
 			}
 			return changes;
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public List<Change> getAllOwnersForFileAtCommit(String FileId, String CommitId)
 	{
 		try 
 		{
 			LinkedList<Change> changes = new LinkedList<Change>();
 			String sql = "SELECT source_commit_id, file_id, owner_id, char_start, char_end, change_type FROM owners natural join commits where commit_id=?" +
					"and (branch_id is NULL OR branch_id=?) and file_id=? order by commit_date, commit_id, char_start;"; 
 			String[] parms = {CommitId, branchID, FileId};
 			ResultSet rs = execPreparedQuery(sql, parms);
 			if(!rs.next()) {
 				List<Commit> parents = getCommitParents(CommitId);
 				if(!parents.isEmpty()) {
 					return getAllOwnersForFileAtCommit(FileId, parents.get(0).getCommit_id());
 				}
 			}
 			else {
 				do
 				{
 					changes.add(new Change(rs.getString("owner_id"), rs.getString("source_commit_id"), 
 							Resources.ChangeType.valueOf(rs.getString("change_type")), rs.getString("file_id"),
 							rs.getInt("char_start"), rs.getInt("char_end")));
 				} while(rs.next());
 			}
 			return changes;
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public List<Commit> getCommitChildren(String CommitID) {
 		try 
 		{
 			LinkedList<Commit> commits = new LinkedList<Commit>();
 			String sql = "SELECT commit_id, author, author_email, comments, commit_date, branch_id FROM commit_family " +
 					"JOIN Commits ON (commit_family.child = Commits.commit_id) where parent=?" +
 					"and (branch_id is NULL OR branch_id=?) order by commit_date, commit_id;"; 
 			String[] parms = {CommitID, branchID};
 			ResultSet rs = execPreparedQuery(sql, parms);
 			while(rs.next())
 			{
 				commits.add(new Commit(rs.getString(1), rs.getString(2),
 						rs.getString(3), rs.getString(4), rs.getTimestamp(5), rs.getString(6)));
 			}
 			return commits;
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public List<Commit> getCommitParents(String CommitID) {
 		try 
 		{
 			LinkedList<Commit> commits = new LinkedList<Commit>();
 			String sql = "SELECT commit_id, author, author_email, comments, commit_date, branch_id FROM commit_family " +
 					"JOIN Commits ON (commit_family.parent = Commits.commit_id) where child=?" +
 					"and (branch_id is NULL OR branch_id=?) order by commit_date, commit_id;"; 
 			String[] parms = {CommitID, branchID};
 			ResultSet rs = execPreparedQuery(sql, parms);
 			while(rs.next())
 			{
 				commits.add(new Commit(rs.getString(1), rs.getString(2),
 						rs.getString(3), rs.getString(4), rs.getTimestamp(5), rs.getString(6)));
 			}
 			return commits;
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public List<String> getFilesChanged(String oldCommit, String newCommit) {
 		try 
 		{
 			LinkedList<String> files = new LinkedList<String>();
 			String sql = "SELECT file_id FROM file_diffs " +
 					"WHERE old_commit_id=? AND new_commit_id=?"; 
 			String[] parms = {oldCommit, newCommit};
 			ResultSet rs = execPreparedQuery(sql, parms);
 			while(rs.next())
 			{
 				if(!files.contains(rs.getString("file_id")))
 					files.add(rs.getString("file_id"));
 			}
 			return files;
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public boolean parentHasChild(String parent, String child) {
 		try 
 		{
 			String sql = "SELECT parent, child FROM commit_family " +
 					"WHERE parent=? AND child=?"; 
 			String[] parms = {parent, child};
 			ResultSet rs = execPreparedQuery(sql, parms);
 			while(rs.next())
 				return true;
 			
 			return false;
 		}
 		catch(SQLException e) 
 		{
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	/**
 	 * Adds a new network record into the networks table for a pair of commits.
 	 * @param NewCommitId
 	 * @param OldCommitId
 	 */
 	public int addNetworkRecord(String NewCommitId, String OldCommitId)
 	{
 		try {
 			// delete duplicates
 			String sql = "DELETE FROM networks where new_commit_id=? and old_commit_id=?";
 			String[] parms = {NewCommitId, OldCommitId};
 			execPrepared(sql, parms);
 			
 			// add new network
 			sql = "INSERT INTO networks (new_commit_id, old_commit_id, network_id) VALUES (?, ?, default);";
 			execPrepared(sql, parms);
 			
 			// get the id generated;
 			sql = "SELECT network_id from networks where new_commit_id=? and old_commit_id=?;";
 			ResultSet rs = execPreparedQuery(sql, parms);
 			if (!rs.next())
 				return -1;
 			else
 				return rs.getInt(1);
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return -1;
 		}
 	}
 	
 	/**
 	 * Adds a node record into the nodes table for a given User/UserfullName
 	 * @param UserId
 	 * @param UserFullName
 	 */
 	public void addNode(String UserId, int NetworkId)
 	{
 		try {
 			if (nodeExists(UserId))
 				return;
 			String sql = "INSERT INTO nodes (id, label, network_id) VALUES (?, ?, ?);";
 			PreparedStatement s = conn.prepareStatement(sql);
 			s.setString(1, UserId);
 			s.setString(2, UserId);
 			s.setInt(3, NetworkId);
 			s.execute();
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public boolean nodeExists(String UserId)
 	{
 		try {
 			String sql = "SELECT * FROM nodes WHERE id=?;";
 			String[] parms = {UserId};
 			ResultSet rs = execPreparedQuery(sql, parms);
 			return rs.next();
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	public String getUserFullName(String UserId, int NetworkId)
 	{
 		try
 		{
 			String sql = "SELECT author from commits where author_email=?;";
 			String[] parms = {UserId};
 			ResultSet rs = execPreparedQuery(sql, parms);
 			if(!rs.next())
 				return null;
 			else
 				return rs.getString("author");
 		}
 		catch(SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Adds an edge record between two users with the weight given.
 	 * @param UserId
 	 * @param UserSource
 	 * @param UserTarget
 	 * @param weight
 	 */
 	public void addEdge(String UserSource, String UserTarget, float weight, boolean isFuzzy, int NetworkId)
 	{
 		try {
 			String sql = "INSERT INTO edges (source, target, weight, is_fuzzy, network_id) VALUES (?, ?, ?, ?, ?);";
 			PreparedStatement s = conn.prepareStatement(sql);
 			s.setString(1, UserSource);
 			s.setString(2, UserTarget);
 			s.setFloat(3, weight);
 			s.setBoolean(4, isFuzzy);
 			s.setInt(5, NetworkId);
 			s.execute();
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public User getUserFromCommit(String CommitId)
 	{
 		try {
 			User u = new User();
 			String sql = "SELECT author, author_email from commits where commit_id = ?";
 			String[] parms = {CommitId};
 			ResultSet rs = this.execPreparedQuery(sql, parms);
 			if (!rs.next())
 				return null;
 			u.setUserName(rs.getString("author"));
 			u.setUserEmail(rs.getString("author_email"));
 			return u;
 		}
 		catch(SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 }
