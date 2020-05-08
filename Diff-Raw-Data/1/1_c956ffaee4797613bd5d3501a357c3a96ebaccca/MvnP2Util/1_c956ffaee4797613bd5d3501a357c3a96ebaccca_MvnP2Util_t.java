 package org.eclipse.cbi.versiontracker.db.main;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import org.eclipse.cbi.versiontracker.db.db.DBI;
 import org.eclipse.cbi.versiontracker.db.db.MavenP2Col;
 import org.eclipse.cbi.versiontracker.db.db.MySQLDBI;
 import org.eclipse.cbi.versiontracker.db.db.SQLiteDBI;
 import org.eclipse.cbi.versiontracker.db.exception.MvnP2Exception;
 import org.eclipse.cbi.versiontracker.db.exception.DBIException;
 
 /** * A utility for managing the version database.  */
 public class MvnP2Util {
 	private DBI dbi;
 
 	public MvnP2Util() {
 		this(new MySQLDBI());
 	}
 
 	protected MvnP2Util(DBI dbi) {
 		this.dbi = dbi;
 	}
 
 	/**
 	 * Validates a version manifest.
 	 * 
 	 * Assumes that it is valid to omit every value. Only verifies that
 	 * values that do exist are valid for that field.
 	 */
 	protected static void validate(VersionManifest mft) throws InvalidManifestException {
 		// TODO Implement.
 		String commit = mft.getGitCommit();
 		if (commit != null && !Pattern.matches("[a-f0-9]{40}", commit)) {
 			throw new InvalidManifestException("Invalid Git commit: " + commit);
 		}
 
 		// We should make sure that versions are valid, project is not just
 		// whitespace.
 	}
 
 	/**
 	 * Validates a version manifest for an add.
 	 *
 	 * Tests that all the required fields:
 	 *  * commit
 	 *  * branch
 	 *  * repo
 	 * are present, and that at least one of the version fields:
 	 *  * p2 version
 	 *  * Maven version
 	 * is present.
 	 */
 	protected static void validateAdd(VersionManifest mft) throws InvalidManifestException
 	{
 		if (mft.getGitCommit() == null ||
 				mft.getGitBranch() == null ||
 				mft.getGitRepo() == null ||
 				(mft.getP2Version() == null && mft.getMavenVersion() == null))
 			throw new InvalidManifestException("Git commit, branch, repo, and one version are required");
 	}
 
 	/**
 	 * Adds a version manifest to the database.
 	 */
 	public void add(VersionManifest mft)
 		throws InvalidManifestException, DBIException {
 		validate(mft);
 		validateAdd(mft);
 
 		Map<String,String> map = mft.createMap();
 		if (!doUpdate(map)) {
 			dbi.addRecord(mft);
 		}
 	}
 
 	/**
 	 * Updates a version manifest.
 	 *
 	 * @return true if a record was updated, false otherwise
 	 */
 	public boolean update(VersionManifest mft)
 		throws InvalidManifestException, DBIException {
 		validate(mft);
 		validateAdd(mft);
 
 		Map<String, String> map = mft.createMap();
 		return doUpdate(map);
 	}
 
 	/**
 	 * Finds all version manifests matching this manifest.
 	 *
 	 * Finds the manifests whose values match all those in this manifest. Null
 	 * values are equivalent to wildcards.
 	 */
 	public List<VersionManifest> find(VersionManifest mft)
 		throws InvalidManifestException, DBIException {
 		validate(mft);
 
 		return dbi.find(mft);
 	}
 
 	/**
 	 * Parses a version manifest from command-line options.
 	 *
 	 * Expects the options to begin at index 1.
 	 * @param args The command-line arguments.
 	 */
 	protected static VersionManifest createManifest(String[] args) {
 		VersionManifest mft = new VersionManifest();
 
 		int i = 1;
 		// Ensure we've always got at least 2 more arguments available
 		while(i < args.length - 1) {
 			String key = args[i++];
 			String val = args[i++];
 			MavenP2Col col = MavenP2Col.findByStr(key);
 
 			if (col == null) {
 				throw new IllegalArgumentException("Unrecognized option: " + key);
 			}
 
 			switch (col) {
 				case GIT_COMMIT: mft.setGitCommit(val); break;
 				case GIT_BRANCH: mft.setGitBranch(val); break;
 				case GIT_REPO: mft.setGitRepo(val); break;
				case GIT_TAG: mft.setGitTag(val); break;
 				case PROJECT: mft.setProject(val); break;
 				case P2_VERSION: mft.setP2Version(val); break;
 				case MAVEN_VERSION: mft.setMavenVersion(val); break;
 				default: assert false : "Unhandled argument: " + col.toString();
 			}
 		}
 
 		return mft;
 	}
 
 	/**
 	 * Create a map from a manifest.
 	 *
 	 * Temporary - until the DBI is updated to use manifests.
 	 */
 	private static Map<String,String> createMap(VersionManifest mft) {
 		Map<String, String> map = new HashMap<String, String>();
 
 		String commit = mft.getGitCommit();
 		String repo = mft.getGitRepo();
 		String branch = mft.getGitBranch();
 		String project = mft.getProject();
 		String p2Version = mft.getP2Version();
 		String mavenVersion = mft.getMavenVersion();
 
 		if (commit != null) map.put(MavenP2Col.GIT_COMMIT.getColName(), commit);
 		if (branch != null) map.put(MavenP2Col.GIT_BRANCH.getColName(), branch);
 		if (repo != null) map.put(MavenP2Col.GIT_REPO.getColName(), repo);
 		if (project != null) map.put(MavenP2Col.PROJECT.getColName(), project);
 		if (p2Version != null) map.put(MavenP2Col.P2_VERSION.getColName(), p2Version);
 		if (mavenVersion != null) map.put(MavenP2Col.MAVEN_VERSION.getColName(), mavenVersion);
 
 		return map;
 	}
 
 
 	/**
 	 * Attempts to update a record using the map given, searching for matches in
 	 * for the hardcoded list of columns (as seen in filterMap). If any matches found, 
 	 * prompts the user to confirm an update, updating if confirmed and canceling 
 	 * the database call otherwise.
 	 * @param map of db column name and input value
 	 * @return true if a matching record was found to update, false otherwise
 	 */
 	private boolean doUpdate(Map<String, String> map) throws DBIException {
 		//TODO: Change doUpdate to use VersionManifest
 		Map<String, String> mvnMap = filterMap(map, MavenP2Col.MAVEN_VERSION);
 		Map<String, String> p2Map = filterMap(map, MavenP2Col.P2_VERSION);
 		List<VersionManifest> mvnMatch = dbi.findFromMap(mvnMap);
 		List<VersionManifest> p2Match = dbi.findFromMap(p2Map);
 		if (mvnMatch.size() > 0 || p2Match.size() > 0){
 			if (mvnMatch.size() > 0) {
 				for (String key : mvnMap.keySet()) map.remove(key);
 				dbi.updateRecordFromMap(mvnMap, map);
 			} else {
 				for (String key : p2Map.keySet()) map.remove(key);
 				dbi.updateRecordFromMap(p2Map, map);
 			}
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Filter a given Map of database values to only include required values
 	 * (for use in determining whether a given record already exists).
 	 * The required values are: git_repo, git_commit, git_branch, and one of
 	 * p2_version and maven_version.
 	 * @param map Set of command name-value pairs
 	 * @param filter Which of p2_version and maven_version to search for matches
 	 */
 	private static Map<String, String> filterMap(Map<String, String> map, MavenP2Col filter){
 		Map<String, String> result = new HashMap<String, String>();
 		for (String key : map.keySet()){
 			if (key == MavenP2Col.GIT_REPO.getColName() ||
 					key == MavenP2Col.GIT_COMMIT.getColName() || 
 					key == MavenP2Col.GIT_BRANCH.getColName() ||
 					key == filter.getColName()){
 				result.put(key, map.get(key));
 					}
 		}
 		return result;
 	}
 
 	/**
 	 * main method
 	 * @param args
 	 */
 	public static void main(String[] args) throws MvnP2Exception{
 		if (args.length < 1) {
 			String errormsg = "Arguments not found. Must specify one of: " +
 					"add, find, update.";
 			System.err.println(errormsg);
 			throw new MvnP2Exception(errormsg);
 		}
 
 		Command command = Command.findByStr(args[0]);
 		if (command == null) {
 			String errormsg = "Command not found: " + args[0];
 			System.err.println(errormsg);
 			throw new MvnP2Exception(errormsg);
 		}
 
 		MvnP2Util util = new MvnP2Util();
 		VersionManifest mft = createManifest(args);
 
 		try {
 			switch (command) {
 				case ADD:
 					util.add(mft);
 					break;
 				case FIND:
 					List<VersionManifest> mfts = util.find(mft);
 					for (VersionManifest vm : mfts) {
 						System.out.println(vm.toString());
 					}
 					if (mfts.size() == 0) {
 						System.err.println("No matching records found.");
 					}
 					break;
 				case UPDATE:
 					if (util.update(mft)) {
 						System.err.println("Updated record.");
 					} else {
 						System.err.println("No matching record found.");
 					}
 					break;
 				default:
 					assert false : "Unhandled command: " + command.toString();
 			}
 		} catch (InvalidManifestException e) {
 			System.err.println("Invalid manifest: " + e.getMessage());
 		} catch (DBIException e) {
 			System.err.println("Connection error.");
 			e.printStackTrace();
 		}
 
 	}
 
 }
