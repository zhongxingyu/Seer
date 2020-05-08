 package jp.ac.osaka_u.ist.sdl.mpanalyzer.db;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.clone.Clone;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.CodeFragment;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Modification;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Modification.ChangeType;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Modification.ModificationType;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.ModificationPattern;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Revision;
 
 public class ReadOnlyDAO extends DAO {
 
 	static private ReadOnlyDAO SINGLETON = null;
 
 	static public ReadOnlyDAO getInstance() throws Exception {
 		if (null == SINGLETON) {
 			SINGLETON = new ReadOnlyDAO();
 		}
 		return SINGLETON;
 	}
 
 	static public void deleteInstance() throws Exception {
 		if (null != SINGLETON) {
 			SINGLETON.modificationStatement.close();
 			SINGLETON.patternStatement.close();
 			SINGLETON = null;
 		}
 	}
 
 	final private PreparedStatement modificationStatement;
 	final private PreparedStatement patternStatement;
	final private PreparedStatement inconsistencyStatement;
 
 	private ReadOnlyDAO() throws Exception {
 
 		super(false, false, false, false, false, false);
 
 		final StringBuilder patternSQL = new StringBuilder();
 		patternSQL
 				.append("select id, beforeHash, afterHash, type, support, confidence");
 		patternSQL
 				.append(" from pattern where ? <= support and ? <= confidence");
 		this.patternStatement = this.connector.prepareStatement(patternSQL
 				.toString());
 
 		final StringBuilder modificationSQL = new StringBuilder();
 		modificationSQL
 				.append("select T.id, T.filepath, T.beforeHash, T.beforeText, ");
 		modificationSQL
 				.append("T.beforeStart, T.beforeEnd, T.afterHash, T.afterText, ");
 		modificationSQL
 				.append("T.afterStart, T.afterEnd, T.revision, T.type, T.date, T.message from ");
 		modificationSQL
 				.append("(select M.id id, M.filepath filepath, M.beforeHash beforeHash, ");
 		modificationSQL
 				.append("(select C2.text from codefragment C2 where C2.id = M.beforeID) beforeText, ");
 		modificationSQL
 				.append("(select C3.start from codefragment C3 where C3.id = M.beforeID) beforeStart, ");
 		modificationSQL
 				.append("(select C4.end from codefragment C4 where C4.id = M.beforeID) beforeEnd, ");
 		modificationSQL.append("M.afterHash afterHash, ");
 		modificationSQL
 				.append("(select C6.text from codefragment C6 where C6.id = M.afterID) afterText, ");
 		modificationSQL
 				.append("(select C7.start from codefragment C7 where C7.id = M.afterID) afterStart, ");
 		modificationSQL
 				.append("(select C8.end from codefragment C8 where C8.id = M.afterID) afterEnd, ");
 		modificationSQL.append("M.revision revision, M.type type, ");
 		modificationSQL
 				.append("(select R1.date from revision R1 where R1.number = M.revision) date, ");
 		modificationSQL
 				.append("(select R2.message from revision R2 where R2.number = M.revision) message ");
 		modificationSQL
 				.append("from modification M) T where T.beforeHash=? and T.afterHash=?");
 		this.modificationStatement = this.connector
 				.prepareStatement(modificationSQL.toString());

		final StringBuilder inconsistencySQL = new StringBuilder();
		inconsistencySQL
				.append("select filepath, start, end, problempattern, problemID, presentCode, proposedCode from problem where problemID in ");
		inconsistencySQL.append("(select problemID from problem ");
		inconsistencySQL
				.append("group by problemID having count(problemID) <= ?)");
		this.inconsistencyStatement = this.connector
				.prepareStatement(inconsistencySQL.toString());
 	}
 
 	public List<Modification> getModifications(final int beforeHash,
 			final int afterHash) throws Exception {
 
 		this.modificationStatement.setInt(1, beforeHash);
 		this.modificationStatement.setInt(2, afterHash);
 		final ResultSet result = this.modificationStatement.executeQuery();
 
 		final List<Modification> modifications = new ArrayList<Modification>();
 		while (result.next()) {
 			final int id = result.getInt(1);
 			final String filepath = result.getString(2);
 			final int beforeID = result.getInt(3);
 			final String beforeText = result.getString(4);
 			final int beforeStart = result.getInt(5);
 			final int beforeEnd = result.getInt(6);
 			final int afterID = result.getInt(7);
 			final String afterText = result.getString(8);
 			final int afterStart = result.getInt(9);
 			final int afterEnd = result.getInt(10);
 			final long number = result.getLong(11);
 			final ModificationType modificationType = beforeText.isEmpty() ? ModificationType.ADD
 					: afterText.isEmpty() ? ModificationType.DELETE
 							: ModificationType.CHANGE;
 			final ChangeType changeType = ChangeType.getType(result.getInt(12));
 			final String date = result.getString(13);
 			final String message = result.getString(14);
 			final Modification modification = new Modification(id, filepath,
 					new CodeFragment(beforeID, beforeText, beforeStart,
 							beforeEnd), new CodeFragment(afterID, afterText,
 							afterStart, afterEnd), new Revision(number, date,
 							message), modificationType, changeType);
 			modifications.add(modification);
 		}
 
 		return modifications;
 	}
 
 	public List<ModificationPattern> getModificationPatterns(
 			final int supportThreshold, final float confidenceThreshold)
 			throws Exception {
 
 		this.patternStatement.setInt(1, supportThreshold);
 		this.patternStatement.setFloat(2, confidenceThreshold);
 		final ResultSet result = this.patternStatement.executeQuery();
 
 		final List<ModificationPattern> patterns = new ArrayList<ModificationPattern>();
 		while (result.next()) {
 			final int id = result.getInt(1);
 			final int beforeHash = result.getInt(2);
 			final int afterHash = result.getInt(3);
 			final ModificationType modificationType = (0 == beforeHash) ? ModificationType.ADD
 					: (0 == afterHash) ? ModificationType.DELETE
 							: ModificationType.CHANGE;
 			final ChangeType changeType = ChangeType.getType(result.getInt(4));
 			final int support = result.getInt(5);
 			final float confidence = result.getFloat(6);
 			final ModificationPattern pattern = new ModificationPattern(id,
 					support, confidence, beforeHash, afterHash,
 					modificationType, changeType);
 			patterns.add(pattern);
 		}
 
 		return patterns;
 	}
 
 	public SortedSet<Revision> getRevisions() throws Exception {
 
 		final Statement revisionStatement = this.connector.createStatement();
 		final ResultSet result = revisionStatement
 				.executeQuery("select number, date, message from revision");
 
 		final SortedSet<Revision> revisions = new TreeSet<Revision>();
 		while (result.next()) {
 			final long number = result.getLong(1);
 			final String date = result.getString(2);
 			final String message = result.getString(3);
 			final Revision revision = new Revision(number, date, message);
 			revisions.add(revision);
 		}
 
 		return revisions;
 	}
 
 	public Map<Integer, List<Clone>> getClones() throws Exception {
 
 		final Statement cloneStatement = this.connector.createStatement();
 		final ResultSet result = cloneStatement
 				.executeQuery("select id, filepath, start, end, revision, setID, changed from clone");
 
 		final Map<Integer, List<Clone>> clones = new HashMap<Integer, List<Clone>>();
 		while (result.next()) {
 			final int id = result.getInt(1);
 			final String path = result.getString(2);
 			final int startLine = result.getInt(3);
 			final int endLine = result.getInt(4);
 			final long revision = result.getLong(5);
 			final int setID = result.getInt(6);
 			final int changed = result.getInt(7);
 
 			final Clone clone = new Clone(path, revision, startLine, endLine,
 					changed);
 			List<Clone> cloneset = clones.get(setID);
 			if (null == cloneset) {
 				cloneset = new ArrayList<Clone>();
 				clones.put(setID, cloneset);
 			}
 			cloneset.add(clone);
 		}
 
 		return clones;
 	}
 
 	@Override
 	public void close() throws Exception {
 		super.close();
 		SINGLETON = null;
 	}
 }
