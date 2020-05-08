 package FindBugsManager.FindBugs;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import FindBugsManager.Core.Main;
 import FindBugsManager.Core.XMLReader;
 import FindBugsManager.DataSets.BugInstanceSet;
 import FindBugsManager.Git.BlameManager;
 import FindBugsManager.Git.DiffManager;
 import FindBugsManager.Git.EditType;
 
 public class FindBugsManager {
 	private File _file = null;
 
 	private static FindBugsManager instance = new FindBugsManager();
 
 	private XMLReader reader = new XMLReader();
 
 	private ArrayList<BugInstanceSet> infoList = new ArrayList<BugInstanceSet>();
 	private ArrayList<BugInstanceSet> preInfoList = new ArrayList<BugInstanceSet>();
 
 	private ArrayList<BugInstanceSet> editedBugList = new ArrayList<BugInstanceSet>();
 
 	private String _committer = null;
 	private static String antXML = Main.getAntXML();
 
 	private FindBugsManager() {
 
 	}
 
 	public void initBugInfoLists() {
 		infoList = new ArrayList<BugInstanceSet>();
 		preInfoList = new ArrayList<BugInstanceSet>();
 		editedBugList = new ArrayList<BugInstanceSet>();
 	}
 
 	public static int runFindbugs(String selectedComment, String targetPath, File bugDataDirectory) {
 
 		ProcessBuilder pb1 = new ProcessBuilder("cmd.exe", "/C", "ant", "-f", antXML);
 		pb1.directory(new File("../"));
 
 		ProcessBuilder pb2 = new ProcessBuilder("cmd.exe", "/C", "findbugs", "-textui", "-low",
 				"-xml", "-output", selectedComment + ".xml", "-project", targetPath, "-effort:min");
 		pb2.directory(bugDataDirectory);
 
 		try {
 			int eValue = 0;
 			eValue = cmdRun(pb1);
 			if (eValue != 0) {
 				System.out.println("Compile Error!");
 				return -1;
 			}
 			eValue = cmdRun(pb2);
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		} catch (InterruptedException e1) {
 			e1.printStackTrace();
 		}
 		return 0;
 	}
 
 	public void createBugInfoList(File currentFile) {
 		_file = currentFile;
 		if (_file.length() != 0) {
 			infoList = reader.parseFindBugsXML(infoList, _file);
 			System.out.println(infoList.size());
 		}
 	}
 
 	public void createPreBugInfoList(File previousFile) {
 		_file = previousFile;
 		if (_file.length() != 0) {
 			preInfoList = reader.parseFindBugsXML(preInfoList, _file);
 			System.out.println(preInfoList.size());
 			int min = infoList.size();
 			if (infoList.size() > preInfoList.size()) {
 				min = preInfoList.size();
 			}
 			for (int i = 0; i < min; i++) {
 				System.out
 						.print(infoList.get(i).getBugInstance().getBugPattern().getAbbrev() + " ");
 				System.out.println(preInfoList.get(i).getBugInstance().getBugPattern().getAbbrev());
 			}
 		}
 	}
 	public void compareBugInfoLists() {
 		ArrayList<String> preTypeList = new ArrayList<String>();
 		ArrayList<String> typeList = new ArrayList<String>();
 
 		ArrayList<String> editedTypeList = new ArrayList<String>();
 		ArrayList<String> newTypeList = new ArrayList<String>();
 
 		for (BugInstanceSet bugInfo : preInfoList) {
 			bugInfo.setEditType(EditType.NO_CHANGE);
 			preTypeList.add(bugInfo.getBugInstance().getBugPattern().getType());
 		}
 		for (BugInstanceSet bugInfo : infoList) {
 			bugInfo.setEditType(EditType.NO_CHANGE);
 			typeList.add(bugInfo.getBugInstance().getBugPattern().getType());
 		}
 
 		ArrayList<String> compareList = new ArrayList<String>(typeList);
 		for (String preType : preTypeList) {
 			if (compareList.contains(preType)) {
 				for (int i = 0; i < compareList.size(); i++) {
 					if (compareList.get(i).equals(preType)) {
 						compareList.remove(i);
 						break;
 					}
 				}
 			} else {
 				editedTypeList.add(preType);
 			}
 		}
 
 		ArrayList<String> preCompareList = new ArrayList<String>(preTypeList);
 		for (String type : typeList) {
 			if (preCompareList.contains(type)) {
 				for (int i = 0; i < preCompareList.size(); i++) {
 					if (preCompareList.get(i).equals(type)) {
 						preCompareList.remove(i);
 						break;
 					}
 				}
 			}
 		}
 
 		for (String name : editedTypeList) {
 			for (int i = 0; i < preInfoList.size(); i++) {
 				BugInstanceSet preInfo = preInfoList.get(i);
 				if (preInfo.getBugInstance().getBugPattern().getType().equals(name)) {
 					preInfo.setEditType(EditType.EDIT);
 					preInfo.setAmender(_committer);
 					editedBugList.add(preInfo);
 					break;
 				}
 			}
 		}
 
 		for (String name : newTypeList) {
 			for (int i = 0; i < infoList.size(); i++) {
 				BugInstanceSet info = infoList.get(i);
 				String typeName = info.getBugInstance().getBugPattern().getType();
 				if (typeName.equals(name) && info.getEditType().equals(EditType.NO_CHANGE)) {
 					info.setEditType(EditType.NEW);
 					info.setAuthor(_committer);
 					break;
 				}
 			}
 		}
 
 		Collections.sort(editedBugList, new IndexSort());
 		Collections.sort(infoList, new IndexSort());
 	}
 	public void checkEditedBugs(DiffManager diff, BlameManager blame) {
 		editedBugList = new ArrayList<BugInstanceSet>();
 
 		for (BugInstanceSet bugInfo : preInfoList) {
 			EditType type = bugInfo.getEditType();
 			if (type == EditType.EDIT) {
 				editedBugList.add(bugInfo);
 			}
 		}
 
 		if (!editedBugList.isEmpty()) {
 			int editedBugStart = 0;
 			for (BugInstanceSet editedBugInfo : editedBugList) {
 				editedBugStart = editedBugInfo.getStartLine();
 				for (BugInstanceSet info : infoList) {
 					if (info.getEditedStartLine() <= editedBugStart
 							&& editedBugStart <= info.getEditedEndLine()) {
 						if (info.getBugInstance().equals(editedBugInfo.getBugInstance())) {
 							info.setExistFlag(true);
 						}
 					}
 				}
 			}
 
 			int i = 0;
 			for (BugInstanceSet info : editedBugList) {
 				if (info.getExistFlag() == true) {
 					editedBugList.remove(i);
 				}
 				i++;
 			}
 
 			ArrayList<String> author;
 			for (BugInstanceSet bugInfo : editedBugList) {
 				author = blame.getAuthors(bugInfo.getStartLine(), bugInfo.getEndLine());
 				bugInfo.setAmender(author.get(0));
 			}
 		}
 
 		Collections.sort(editedBugList, new IndexSort());
 		Collections.sort(infoList, new IndexSort());
 	}
 
 	public static FindBugsManager getInstance() {
 		return instance;
 	}
 
 	public ArrayList<BugInstanceSet> getBugInfoList() {
 		return infoList;
 	}
 
 	public ArrayList<BugInstanceSet> getPreBugInfoList() {
 		return preInfoList;
 	}
 
 	public ArrayList<BugInstanceSet> getEditedBugList() {
 		return editedBugList;
 	}
 
 	public int getBugCounts() {
 		return infoList.size();
 	}
 
 	public int getPreBugCounts() {
 		return preInfoList.size();
 	}
 
 	public void setCommitter(String committer) {
 		_committer = committer;
 	}
 
 	private static int cmdRun(ProcessBuilder pb) throws IOException, InterruptedException {
 		Process process = pb.start();
 		final InputStream in = process.getInputStream();
 		final InputStream ein = process.getErrorStream();
 
 		Runnable inputStreamThread = new Runnable() {
 			public void run() {
 				try {
 					String line = null;
 					System.out.println("Thread stdRun start");
 					BufferedReader br = new BufferedReader(new InputStreamReader(in));
 					while ((line = br.readLine()) != null) {
 						System.out.println(line);
 					}
 					System.out.println("Thread stdRun end");
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		};
 		Runnable errStreamThread = new Runnable() {
 			public void run() {
 				try {
 					String errLine = null;
 					System.out.println("Thread errRun start");
 					BufferedReader ebr = new BufferedReader(new InputStreamReader(ein));
 					while ((errLine = ebr.readLine()) != null) {
 						System.out.println(errLine);
 					}
 					System.out.println("Thread errRun end");
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		};
 
 		Thread stdRun = new Thread(inputStreamThread);
 		Thread errRun = new Thread(errStreamThread);
 
 		stdRun.start();
 		errRun.start();
 
 		process.waitFor();
 		int exitValue = process.exitValue();
 		process.destroy();
 		return exitValue;
 	}
 
 	public void display() {
 		for (BugInstanceSet info : infoList) {
 			System.out.println(info.getBugInstance().getBugPattern().getType());
 			System.out.println(info.getStartLine() + " ~ " + info.getEndLine());
 			System.out.println(info.getEditType());
 			System.out.println(info.getAmender());
 			System.out.println(info.getAuthor());
 		}
 	}
 
 }
 
 class IndexSort implements Comparator<BugInstanceSet>, Serializable {
 	private static final long serialVersionUID = 1L;
 
 	public int compare(BugInstanceSet set1, BugInstanceSet set2) {
 		int rank1 = set1.getBugInstance().getBugRank();
 		int rank2 = set2.getBugInstance().getBugRank();
 		if (rank1 > rank2) {
 			return 1;
 		} else if (rank1 == rank2) {
 			return 0;
 		} else {
 			return -1;
 		}
 	}
 }
