 package db;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import callgraphanalyzer.Resources;
 
 public class DbConnection {
 
 	public static Connection conn = null;
 	public ResultSet savedResultSet = null;
 	public boolean isPaging = false;
 	public static DbConnection ref = null;
 	private String branchName = null;
 	private String branchID = null;
 
 	private DbConnection() 
 	{
 		try 
 		{
 			Class.forName("org.postgresql.Driver").newInstance();
 		} 
 		catch (InstantiationException e) 
 		{
 			e.printStackTrace();
 		} 
 		catch (IllegalAccessException e) 
 		{
 			e.printStackTrace();
 		} 
 		catch (ClassNotFoundException e) 
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public static DbConnection getInstance()
 	{
 		if (ref == null)
 			return (ref = new DbConnection());
 		else
 			return ref;
 	}
 
 	public String getBranchID() {
 		return branchID;
 	}
 	
 	public void setBranchID(String branchID) {
 		this.branchID = branchID;
 	}
 
 	public String getBranchName() {
 		return branchName;
 	}
 
 	/**
 	 * Should be called AFTER @see {@link #connect(String)}, as it also does 
 	 * a lookup on the branchID and sets it behind the scenes.
 	 * Also does a lookup in the branches table for 
 	 * @param branchName
 	 */
 	public void setBranchName(String branchName) {
 		this.branchName = branchName;
 		try
 		{
 			String[] params = {branchName};
 			ResultSet rs = ref.execPreparedQuery("SELECT branch_id from branches where branch_name ~ ? LIMIT 1", params);
 			rs.next();
 			setBranchID(rs.getString(1));
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Executes a string of SQL on the current database
 	 * NOTE: this assumes your sql is valid.
 	 * @param sql
 	 * @return true if successful
 	 */
 	public boolean exec(String sql)
 	{
 		try {
 			PreparedStatement s = conn.prepareStatement(sql);
 			s.execute();
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean execPrepared(String sql, String[] params)
 	{
 		try {
 			PreparedStatement s = conn.prepareStatement(sql);
 			for (int i = 1;i <= params.length;i++)
 			{
 				s.setString(i, params[i-1]);
 			}
 			s.execute();
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Executes the given query with escaped values in String[] params in place of
 	 * ? characters in sql.
 	 * @param sql ex. "SELECT * FROM something where my_column=?"
 	 * @param params ex. {"braden's work"}
 	 * @return Query ResultSet on success, null otherwise
 	 */
 	public ResultSet execPreparedQuery(String sql, String[] params)
 	{
 		try {
 			PreparedStatement s = conn.prepareStatement(sql);
 			for (int i = 1;i <= params.length;i++)
 			{
 				s.setString(i, params[i-1]);
 			}
 			return s.executeQuery();
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Connects to the given database.  
 	 * @param dbName
 	 * @return true if successful
 	 */
 	public boolean connect(String dbName)
 	{
 		try {
 			conn = DriverManager.getConnection(Resources.dbUrl + dbName.toLowerCase(), Resources.dbUser, Resources.dbPassword);
 		} 
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Close all connection
 	 */
 	public boolean close()
 	{
 		try {
 			conn.close();
 			return true;
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	/**
 	 * Returns a HashMap<(filePath), (fileContents)>
 	 * @param commitID
 	 * @return
 	 */
 	public Map<String, String> getCommitChangedFiles(String commitID)
 	{
 		Map<String, String> files = new HashMap<String, String>();
 		try {
 			String sql = "SELECT file_id, raw_file FROM files where commit_id=?;";
 			String[] params = {commitID};
 			ResultSet rs = execPreparedQuery(sql, params);
 			while(rs.next())
 			{
 				files.put(rs.getString("file_id"), rs.getString("raw_file"));
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 		return files;
 	}
 	
 	/**
 	 * Returns a List of filepaths
 	 * @param commitID
 	 * @return
 	 */
 	public HashSet<String> getCommitFileStructure(String commitID)
 	{
 		HashSet<String> filepaths = new HashSet<String>();
 		try {
			String sql = "SELECT file_structure FROM commits where commit_id=? and (branch_id=? OR branch_id is NULL);";
 			String[] params = {commitID, this.branchID};
 			ResultSet rs = execPreparedQuery(sql, params);
 			rs.next();
 			String[] structure = (String[])rs.getArray(1).getArray();
 			for (String s: structure)
 			{
 				filepaths.add(s);
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 		return filepaths;
 	}
 	
 	/**
 	 * Gets the 100 commits
 	 * @param commitID
 	 * @return
 	 */
 	public List<CommitsTO> getCommitsBefore(String commitID)
 	{
 		isPaging = true;
 		List<CommitsTO> commitsList = new ArrayList<CommitsTO>();
 		try {
			String sql = "SELECT * FROM commits where (branch_id=? OR branch_id is NULL) and commit_date <= (SELECT commit_date FROM commits WHERE commit_id=? and (branch_id=? OR branch_id is NULL));";
 			String[] params = {this.branchID, commitID, this.branchID};
 			this.savedResultSet = execPreparedQuery(sql, params);
 			CommitsTO commit;
 			for (int i = 0; i < 100;i++)
 			{
 				this.savedResultSet.next();
 				commit = new CommitsTO();
 				commit.setAuthor(this.savedResultSet.getString("author"));
 				commit.setAuthor_email(this.savedResultSet.getString("author_email"));
 				commit.setBranch_id(this.savedResultSet.getString("branch_id"));
 				commit.setComment(this.savedResultSet.getString("comments"));
 				commit.setCommit_date(this.savedResultSet.getDate("commit_date"));
 				commit.setCommit_id(this.savedResultSet.getString("commit_id"));
 				commit.setId(this.savedResultSet.getInt("id"));
 				commit.setChanged_files(new HashSet<String>(Arrays.asList((String[]) this.savedResultSet.getArray("changed_files").getArray())));
 				commit.setFile_structure(new HashSet<String>(Arrays.asList((String[]) this.savedResultSet.getArray("file_structure").getArray())));
 				commitsList.add(commit);
 				if (this.savedResultSet.isLast())
 				{
 					isPaging = false;
 					break;
 				}
 			}
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 		return commitsList;
 	}
 	
 	/**
 	 * To be called after @see {@link #getCommitsBefore(String)}
 	 * gets the next 100 commits and moves the ResultSet
 	 * @return
 	 */
 	public List<CommitsTO> getNextCommitsPage()
 	{
 		isPaging = true;
 		List<CommitsTO> commitsList = new ArrayList<CommitsTO>();
 		try{
 			CommitsTO commit;
 			for (int i = 0; i < 100;i++)
 			{
 				this.savedResultSet.next();
 				commit = new CommitsTO();
 				commit.setAuthor(this.savedResultSet.getString("author"));
 				commit.setAuthor_email(this.savedResultSet.getString("author_email"));
 				commit.setBranch_id(this.savedResultSet.getString("branch_id"));
 				commit.setComment(this.savedResultSet.getString("comments"));
 				commit.setCommit_date(this.savedResultSet.getDate("commit_date"));
 				commit.setCommit_id(this.savedResultSet.getString("commit_id"));
 				commit.setId(this.savedResultSet.getInt("id"));
 				commit.setChanged_files(new HashSet<String>(Arrays.asList((String[]) this.savedResultSet.getArray("changed_files").getArray())));
 				commit.setFile_structure(new HashSet<String>(Arrays.asList((String[]) this.savedResultSet.getArray("file_structure").getArray())));
 				commitsList.add(commit);
 				if (this.savedResultSet.isLast())
 				{
 					isPaging = false;
 					break;
 				}
 			}
 			return commitsList;
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public CommitsTO getCommit(String commitID)
 	{
 		CommitsTO commit = new CommitsTO();
 		try {
 			String[] params = {commitID, this.branchID};
			ResultSet rs = execPreparedQuery("SELECT * from commits where commit_id=? and (branch_id=? OR branch_id is NULL);", params);
 			rs.next();
 			commit.setAuthor(rs.getString("author"));
 			commit.setAuthor_email(rs.getString("author_email"));
 			commit.setBranch_id(rs.getString("branch_id"));
 			commit.setComment(rs.getString("comments"));
 			commit.setCommit_date(rs.getDate("commit_date"));
 			commit.setCommit_id(rs.getString("commit_id"));
 			commit.setId(rs.getInt("id"));
 			commit.setChanged_files(new HashSet<String>(Arrays.asList((String[]) rs.getArray("changed_files").getArray())));
 			commit.setFile_structure(new HashSet<String>(Arrays.asList((String[]) rs.getArray("file_structure").getArray())));
 			return commit;
 		}
 		catch(SQLException e)
 		{
 			e.printStackTrace();
 		}
 		return commit;
 	}
 	public String getRawFile(String fileID, String commitID)
 	{
 		try{
 			String sql = "SELECT raw_file from files where commit_id=? and file_id=?;";
 			String[] params = {commitID, fileID};
 			ResultSet rs = execPreparedQuery(sql, params);
 			rs.next();
 			return rs.getString(1);
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 }
