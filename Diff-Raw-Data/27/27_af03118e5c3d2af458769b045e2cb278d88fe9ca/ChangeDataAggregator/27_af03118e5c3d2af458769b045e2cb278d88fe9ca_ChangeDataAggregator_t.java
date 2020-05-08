 package sdcl.ics.uci.edu.lda.modelAggregator;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import sdcl.ics.uci.edu.lda.modelAggregator.lightweightModel.coreModel.ChangeSet;
 import sdcl.ics.uci.edu.lda.modelAggregator.lightweightModel.coreModel.LightweightTopicModel;
 import sdcl.ics.uci.edu.lda.modelAggregator.lightweightModel.coreModel.ChangeSet.FileChange;
 import sdcl.uci.edu.lda.commitLogReader.CommitLogData;
 import sdcl.uci.edu.lda.commitLogReader.CommitLogData.ClassChange;
 import sdcl.uci.edu.lda.commitLogReader.CommitLogReader;
 
 /**
  * Reads data from a commit log file using the CommitLogReader. Associates all
  * changes with the appropriate model. Each change is stored (After the
  * associateModelToChanges method is called) as a ChangeSet object in the
  * appropriate LighweightTopicModel.
 * 
  * 
  * @author nlopezgi
  * 
  */
 public class ChangeDataAggregator {
 
 	/**
 	 * A map from an index (for a version number, same index as in the other two
 	 * arrays) to a set of CommitLogData that should be associated to a given
 	 * version
 	 */
 	Map<Integer, List<CommitLogData>> versionToLogs;
 	long[] versionNumbers;
 	Date[] versionDates;
 
 	/**
 	 * Loads all change data from the given commit log file and makes it available
 	 * so it can be associated with the lightweight models. Stores all
 	 * CommitLogData objects in a map with the version number index as key.
 	 * 
 	 * @param versionNumbers
 	 *          an array of longs, each representing the date for one version of
 	 *          the code for which a lightweight model is available. Version
 	 *          numbers should be ordered in descending values.
 	 * @param commitLogFile
 	 *          the file to read logs from
 	 * @throws Exception
 	 */
 	public void createChangeData(long[] versionNumbers, String commitLogFile)
 			throws Exception {
 		this.versionNumbers = versionNumbers;
 		versionDates = new Date[versionNumbers.length];
 
 		for (int i = 0; i < versionNumbers.length; i++) {
 			long versionDate = versionNumbers[i] * 1000;
 			versionDates[i] = new Date(versionDate);
 		}
 
 		List<CommitLogData> allLogData = CommitLogReader
 				.readCommitLogFile(commitLogFile);
 
 		System.out.println("number of commits in log file:" + allLogData.size());
 
 		Iterator<CommitLogData> logDataIterator = allLogData.iterator();
 
 		List<CommitLogData> commitsForCurrentVersion = new ArrayList<CommitLogData>();
 		versionToLogs = new HashMap<Integer, List<CommitLogData>>();
 		// go throught the array of versions
 		for (int i = 0; i < versionNumbers.length - 1; i++) {
 			// System.out.println("starting with version " + i + " long id:"
 			// + versionNumbers[i] + ". Date:" + versionDates[i]);
 			boolean nextVersion = false;
 			while (!nextVersion && logDataIterator.hasNext()) {
 				CommitLogData oneLog = logDataIterator.next();
 				Date logDate = oneLog.getDate();
 
 				// if the log date is before the preceding version (the next one in the
 				// array) then continue with the next version in the array
 				if (logDate.before(versionDates[i + 1])) {
 					// put it in the next version
 					nextVersion = true;
 					// store the array for the current version in the map
 					versionToLogs.put(i, commitsForCurrentVersion);
 					// create a new array for the preceding version
 					commitsForCurrentVersion = new ArrayList<CommitLogData>();
 				}
 				// add the commit log to the current version array
 				commitsForCurrentVersion.add(oneLog);
 			}
 		}
 	}
 
 	/**
 	 * Associates all changes that occurred at a time close to version using the
 	 * map of versionToLogs and converting each CommitLogData into a ChangeSet
 	 * object.
 	 * 
 	 * 
 	 * @param model
 	 * @param version
 	 */
 	public void associateModelToChanges(LightweightTopicModel model, long version) {
 		int index = -1;
 		// find the index of this version in the versionNumbers array
 		for (int i = 0; i < versionNumbers.length; i++) {
 			if (versionNumbers[i] == version) {
 				index = i;
 				break;
 			}
 		}
 		if (index != -1) {
 			List<CommitLogData> logsForVersion = versionToLogs.get(index);
 
 			if (logsForVersion != null && logsForVersion.size() > 0) {
 				int changeIndex = 0;
 				ChangeSet[] allChangeSets = new ChangeSet[logsForVersion.size()];
 				for (CommitLogData oneLog : logsForVersion) {
 					ChangeSet oneChangeSet = new ChangeSet();
 					oneChangeSet.id = oneLog.getId();
 					FileChange[] fileChanges = new FileChange[oneLog.getClassChanges()
 							.size()];
 					int i = 0;
 					for (ClassChange change : oneLog.getClassChanges()) {
 						FileChange oneChange = new FileChange();
 						int totalChanged = change.numChanged;
 						oneChange.detla = totalChanged;
 						int totalSigns = change.proportionalAdded
 								+ change.proportionalRemoved;
 						int totalAdded = (change.proportionalAdded * totalChanged)
 								/ totalSigns;
 						int totalRemoved = (change.proportionalRemoved * totalChanged)
 								/ totalSigns;
 						oneChange.add = totalAdded;
 						oneChange.del = totalRemoved;
 						oneChange.id = getIdForClass(model, change.className);
 						fileChanges[i] = oneChange;
 						i++;
						if (oneChange.id == -1) {
							System.err
									.println("could not process one change, there's something funky in your commit logs: File "
											+ change.className
											+ " could not be found in version "
											+ version
											+ "("
											+ new Date(version * 1000)
											+ ")"
											+ ", yet it is referenced in commit log with id:"
											+ oneChangeSet.id + " from " + oneLog.getDate());

						}
 					}
 					oneChangeSet.changes = fileChanges;
 					allChangeSets[changeIndex] = oneChangeSet;
 					changeIndex++;
 				}
 				model.changeSets = allChangeSets;
 			}
 		}
 	}
 
 	public int getIdForClass(LightweightTopicModel model, String className) {
 		for (int i = 0; i < model.classNames.length; i++) {
 			if (model.classNames[i].endsWith(className)) {
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	public class ClassChangeImpact {
 		String className;
 		int totalChangedLines;
 		int totalAdded;
 		int totalRemoved;
 	}
 }
