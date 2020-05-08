 package gitHelper;
 
 import gitHelper.utils.*;
 import gitHelper.utils.Constants.Relationship;
 import gitHelper.utils.RunIt.Output;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.eclipse.egit.github.core.Repository;
 import org.eclipse.egit.github.core.RepositoryBranch;
 import org.eclipse.egit.github.core.service.RepositoryService; 
 
 
 public class GitHelper {
 	
 	// TODO: logging
 	private static RepositoryService s_repositoryService;
 	private static String _executablePath;
 	
 	/**
 	 * @return
 	 * @effect: Initialize static members
 	 */
 	private static void Initialize() {
 		_executablePath = RunIt.getExecutable(RunIt
 				.getExecutable(Constants.GIT));
 	}
 
 	
 	/**
 	 * @return
 	 * @effect: Get git exe path
 	 */
 	private static String GetExecutablePath() {
 		if (null == _executablePath)
 			Initialize();
 
 		return _executablePath;
 	}
 
 	
 	/**
 	 * @param String
 	 *            branchName: the name of the host branch to reset the repository to
 	 * @param String
 	 *            pathToLocalRepo: the path to the local repository checkout location
 	 * @return
 	 * @effect: performs a hard reset on the given repository
 	 */
 	public static void ResetBranchSourceRepository
 	(
 	String branchName,
 	String pathToLocalRepo
 	) throws OperationException 
 	{
 		Output output;
 		// Reset
 		
 		String executablePath = GetExecutablePath();
 		String[] myArgs = { "reset", "origin/" + branchName, "--hard" };
 		String command = executablePath + "reset origin/" + branchName + "--hard";
 		
 		try 
 		{
 			output = RunIt.execute (executablePath, myArgs, pathToLocalRepo, false);
 		} 
 		catch (IOException e2) 
 		{
 			throw new OperationException(command, pathToLocalRepo, e2.getMessage());
 		}
 		
 		// TODO: parse output: 
 	}
 	
 	
 	/**
 	 * @param String
 	 *            branchName: the name of the branch to checkout
 	 * @param String
 	 *            pathToLocalRepo: the path to the local repository checkout location
 	 * @return
 	 * @effect: performs a hard reset on the given repository
 	 */
 	public static void CheckoutBranch
 	(
 	String branchName,
 	String pathToLocalRepo
 	) throws OperationException 
 	{
 		Output output;
 		// Reset
 		
 		String executablePath = GetExecutablePath();
 		
 		String[] myArgs = {"checkout", branchName};
 		String command = executablePath + " checkout " + branchName;
 		
 		try 
 		{
 			output = RunIt.execute (executablePath, myArgs, pathToLocalRepo, false);
 		} 
 		catch (IOException e2)
 		{
 			throw new OperationException(command, pathToLocalRepo, e2.getMessage());
 		}
 		
 		if ((!output.getError().isEmpty() || output.getStatus() != 0)  && output.getOutput().indexOf("Switched to ") < 0 && output.getOutput().indexOf("Already on") < 0
 				&& output.getError().indexOf("Switched to ") < 0 && output.getError().indexOf("Already on") < 0)
 			throw new OperationException(command, pathToLocalRepo, output.getError());
 	}
 	
 	
 	/**
 	 * @param String
 	 *            pathToRemoteRepo: the path to the executable
 	 * @param String
 	 *            pathToLocalRepo: the path to the local repo which this method creates
 	 * @param String
 	 *            tempWorkPath: path to a temp directory
 	 * @return
 	 * @effect: performs a pull and update on the pathToLocalRepo repository
 	 */
 	protected static synchronized void UpdateLocalRepositoryBranch
 	(
 	String pathToRemoteRepo, 
 	String pathToLocalRepo, 
 	String branch
 	) throws IOException, OperationException 
 		{
 		assert (null != pathToLocalRepo);
 		assert (null != pathToRemoteRepo);
 		assert (null != branch);
 		
 		String pathExecutable = GetExecutablePath();
 		
 		// _log.info("update local repository");
 
 		String command = pathExecutable + " pull "/*-u " + pathToRemoteRepo*/;
 		List<String> myArgsList = new ArrayList<String>();
 		myArgsList.add("pull");
 		//myArgsList.add("-u");
 		//myArgsList.add(pathToRemoteRepo);
 		// TODO: make sure the correct branch is being pulled
 		/*if (branch != null) {
 			myArgsList.add("-b");
 			myArgsList.add(branch);
 			command += " -b " + branch;
 		}*/
 
 		// String[] myArgs = { "pull", "-u" };
 		Output output = RunIt.execute(pathExecutable, myArgsList.toArray(new String[0]), pathToLocalRepo, false);
 		if (pathExecutable.contains("git")) {
 			/*_log.info("update local repository");
 			_log.info("run command: " + command);
 			_log.info("output: \n" + output.getOutput());
 			if (output.getError().length() > 0) {
 				_log.info("error: " + output.getError()); TODO: Logging
 			}*/
 		}
 
 		// for git
 		if (pathExecutable.contains("git") && (output.getOutput().indexOf("Fast-forward") < 0) && (output.getOutput().indexOf("Already up-to-date.") < 0)) {
 			throw new OperationException(command, pathToLocalRepo, output.toString());
 		}
 
 		/*
 		 * if (pathExecutable.contains("git")) { _log.info("output: \n" + output.getOutput() + "\n error: \n" + output.getError()); }
 		 */
 	}
 
 	/**
 	 * @param String
 	 *            repositoryURL: the URL for the git repository
 	 * @param String
 	 *            pathToLocalRepo: the path to the local repo which this method creates
 	 * @param String
 	 *            tempWorkPath: path to a temp directory
 	 * @return
 	 * @effect: performs a pull and update on the pathToLocalRepo repository
 	 */
 	public static synchronized void updateLocalRepositoryBranchAndCheckCacheError
 	(
 	String repositoryURL,
 	String localRepo,
 	String branch
 	) throws OperationException, IOException 
 	{
 		// TODO Logging 
 		//_log.info("update local repository and check cache error");
 		//_log.info("call update local repository and check cache error");
 		//_log.info("local repository: " + localRepo + ", exist?: " + (new File(localRepo).exists()));
 		boolean needToCreateLocalRepository = true;
 		
 		if (new File(localRepo).exists()) 
 		{
 			needToCreateLocalRepository = false;
 			try {
 				//_log.info("trying to update local repository: " + localRepo);
 				UpdateLocalRepositoryBranch(repositoryURL,  localRepo, branch);
 				//_log.info("successfully finished updating local repository: " + localRepo);
 			} catch (OperationException e) {
 				//_log.info("operation exception in running update local repository");
 				//_log.info("command: " + e.getCommand() + "\n path: " + e.getPath() + "\n output: " + e.getOutput() + "\n");
 				String errorMsg = "Error executing\n" + e.getCommand() + "\nin " + e.getPath() + ".\n" + "Got the unexpected output:\n" + e.getOutput() + "\n";
 				//_log.error(errorMsg);
 
 				//throw new OperationException(errorMsg, e.getPath(), e.getOutput());
 				needToCreateLocalRepository = true;
 			}
 		} 
 		
 		if (needToCreateLocalRepository)
 		{
 			//_log.info("trying to create local repository: " + localRepo);
 			createLocalRepository(repositoryURL, localRepo, branch);
 			//_log.info("finished creating local repository: " + localRepo);
 		}
 	}
 	
 	/**
 	 * @param String
 	 *            pathToRemoteRepo: the full path to the remote repo
 	 * @param String
 	 *            pathToLocalRepo: the path to the local repo which this method creates
 	 * @param String
 	 *            branch: path to a temp directory
 	 * @effect: clones the pathToRemoteRepo repository to pathToLocalRepo
 	 */
 	protected static synchronized void createLocalRepository
 	(
 	String pathToRemoteRepo,
 	String pathToLocalRepo,
 	String branch/*,
 	String remoteCmd*/ // TODO: is this needed?
 	) throws IOException, OperationException 
 	{
 
 		assert (null != pathToRemoteRepo);
 		assert (null != pathToLocalRepo);
 		assert (null != branch);
 
 		// _log.info("create local repository");
 		// String git = prefs.getClientPreferences().getGitPath();
 
 		// Create Directory
 		File directory = new File(pathToLocalRepo); 
 		if (!directory.exists())
 			{
 			boolean created = directory.mkdirs();
 			if (created);
 			}
 		
 		String pathExecutable = GetExecutablePath();
 		
 		String command = pathExecutable + " clone";
 
 		List<String> myArgsList = new ArrayList<String>();
 		myArgsList.add("clone");
 		/*if (remoteCmd != null) {
 			myArgsList.add("--remotecmd");
 			myArgsList.add(remoteCmd);
 			command += " --remotecmd " + remoteCmd;
 		}*/
 		myArgsList.add(pathToRemoteRepo);
 		if (branch != null)
 		{
 			myArgsList.add("-b");
 			myArgsList.add(branch);
 		}
 		
 		myArgsList.add(pathToLocalRepo);
 		
 		command +=  " " + pathToRemoteRepo;
 		if (branch != null)
 			command +=  " -b " + branch;
 		command +=  " " + pathToLocalRepo;
 		
 		Output output = RunIt.execute(pathExecutable, myArgsList.toArray(new String[0]), pathToLocalRepo, true);
 		if (pathExecutable.contains("git")) {
 			/*_log.info("create local repository");
 			_log.info("run command: " + command);
 			_log.info("output: " + output.getOutput());
 			if (output.getError().length() > 0) {
 				_log.info("error: " + output.getError());
 			}*/ // TODO: logging
 		}
 		
 		if ((!output.getError().isEmpty() || output.getStatus()!= 0) && output.getOutput().indexOf("updating to branch") < 0 && output.getOutput().indexOf("done.") < 0) {
 			String errorMsg = "Tried to execute command:\n" + "\"" + pathExecutable + " clone " + pathToRemoteRepo + " " + pathToLocalRepo + "\"\n" + "from \""
 					+ pathToLocalRepo + "\"\n" + "but got the unexpected output:\n" + output.toString();
 			// JOptionPane.showMessageDialog(null, dialogMsg, "git clone failure", JOptionPane.ERROR_MESSAGE);
 			throw new OperationException(errorMsg, pathToLocalRepo, output.toString());
 			// throw new RuntimeException("Could not clone repository " + pathToRemoteRepo + " to " + pathToLocalRepo + "\n" + output);
 		}
 	}
 	
 	/**
 	 * @param String
 	 *            hostBranchSourceCheckoutLocation: the path to the local host repository checkout location
 	 * @param String
 	 *            mergeBranch: the name of the branch to merge
 	 * @param String
 	 *            mergeBranchSourceCheckoutLocation: the path to the local merge repository checkout location
 	 * @return
 	 * @effect: merges the given host and merge branch
 	 */
 	public static String MergeBranches (
 	String hostBranchSourceCheckoutLocation,
 	String mergeBranch,
 	String mergeBranchSourceCheckoutLocation,
 	String oldRelationship
 	) 
 	{
 		assert (null != mergeBranch);
 
 		String executablePath = GetExecutablePath();
 
 		Output output;
 
 		// Add the merge branch as a remote to the source branch repository
 		try {
 			String[] myArgs = { "remote", "add", "-f", mergeBranch,
 					mergeBranchSourceCheckoutLocation };
 			output = RunIt.execute(executablePath, myArgs,
 					hostBranchSourceCheckoutLocation, false);
 		} catch (IOException e2) {
 			return Constants.Relationship.Error.toString()
 					+ " Couldn't checkout host branch: " + e2.getMessage();
 		}
 
 		// TODO: check for history?
 
 		// Perform merge
 		try {
 			String[] myArgs = { "merge", mergeBranch + "/" + mergeBranch };
 			output = RunIt.execute(executablePath, myArgs,
 					hostBranchSourceCheckoutLocation, false);
 		} catch (IOException e2) {
 			return Relationship.Error.toString()
 					+ " Couldn't perform a branch merge: " + e2.getMessage();
 		}
 		// Check for merge status
 		if (output.getOutput().contains("CONFLICT"))
 			return Relationship.MergeConflict.toString();
 
 		return Relationship.MergeClean.toString();
 	}
 	
 	/*public static void main(String[] args)
 	{
 		String userName = JOptionPane.showInputDialog("Please Enter Github username");
 		if (null == userName || userName.isEmpty())
 			return;
 		
 		String repositoryName = JOptionPane.showInputDialog("Please enter Repository Name");
 		
 		if (null == repositoryName || userName.isEmpty()) 
 			return;
 		
 		String dir = "d:\\Src\\MergeTesting\\" ;
 		
 		MergeAllBranchesOfRepostiroy(userName, repositoryName, dir, true);
 	}*/
 	
 	public static void MergeAllBranchesOfRepostiroy
 	(
 	String userName,
 	String repositoryName,
 	String workDirectory,
 	boolean resetMerged
 	) throws OperationException
 	{
 		assert (null != userName && !userName.isEmpty());		
 		assert (null != repositoryName && !repositoryName.isEmpty());
 		
 		Repository mainRepo = GetRepository (userName, null, repositoryName);		
 		if (null == mainRepo)
 			return;
 		
 		HashMap<Repository, List<String>> repositoryToBranch = new HashMap<Repository, List<String>>();
 	
 		GetForksAndBranches (mainRepo, repositoryToBranch);
 			
 		HashMap<String, String> repoAndBranchList = new HashMap<String, String>();
 		
 		// Clone/Update all branches
 		for (Repository currRepo : repositoryToBranch.keySet())
 		{
 			String currRepositoryURL = currRepo.getCloneUrl();
 			String currRepositoryName =  currRepo.getName();
 			
 			for (String currBranchName : repositoryToBranch.get(currRepo))
 			{
 				repoAndBranchList.put (currRepositoryName + File.separatorChar + currBranchName, currRepositoryURL);
 			
 				String currWorkDirectory = getWorkDirectoryForBranch (workDirectory, currRepositoryName, currBranchName);
 
 				try {
 					updateLocalRepositoryBranchAndCheckCacheError 
 						(
 						currRepositoryURL,
 						currWorkDirectory,
 						currBranchName
 					    );
 				} catch (OperationException e1) {
 					//_log.info("BranchMerger::MergeBraches(..) - ERROR: failed to update local repo and check cache error in getBranchesMergeRelationship");
 					return /*Relationship.ERROR + " " + e1.getMessage()*/;
 				} catch (IOException e2) {
 					//_log.info("BranchMerger::MergeBraches(..) - ERROR: failed to update local repo and check cache error in getBranchesMergeRelationship");
 					return /*Relationship.ERROR + " " + e2.getMessage()*/;
 				}
 			}
 		}
 		
 		boolean[][] results = new boolean[repoAndBranchList.size()][repoAndBranchList.size()];
 		
 		int hostIndex = -1;
 		for (String hostRepoAndBranch : repoAndBranchList.keySet())
 		{
 			hostIndex++; 
 			String hostBranchDir	= getWorkDirectoryForBranch (workDirectory, null, hostRepoAndBranch);			
 			String hostBranch 		= hostRepoAndBranch.substring(hostRepoAndBranch.lastIndexOf (File.separatorChar), hostRepoAndBranch.length() - 1);
 			String repositoryURL 	= repoAndBranchList.get(hostRepoAndBranch);
 			
 			int branchIndex = -1;
 			for (String mergeRepoAndBranch : repoAndBranchList.keySet())
 			{
 				branchIndex++;
 				if (hostIndex == branchIndex)
 					{
 					results[hostIndex][branchIndex] = false;
 					continue;
 					}
 				
 				String mergeBranchDir = getWorkDirectoryForBranch (workDirectory, null, mergeRepoAndBranch);						
 				String mergeBranch = mergeRepoAndBranch.substring (mergeRepoAndBranch.lastIndexOf (File.separatorChar), mergeRepoAndBranch.length() - 1);
 				
 				String mergeRelationship = MergeBranches (hostBranchDir, mergeBranch, mergeBranchDir, null);
 			
 				results[hostIndex][branchIndex] = mergeRelationship == Relationship.MergeClean.toString();
 					
 				//if (mergeRelationship != null)
 				//	JOptionPane.showMessageDialog (null, "MergeRelationship between " + hostBranch + " and " + mergeBranch + ": " + mergeRelationship);
 				
 				if (resetMerged)
 					ResetBranchSourceRepository (hostBranch, hostBranchDir);
 			}
 		}
 		
 		/*String[] branchNames = new String [repoAndBranchList.size()];
 		branchNames = repoAndBranchList.keySet().toArray(branchNames);
 	
 		MergeResultsDisplay resultsDisplay = new MergeResultsDisplay (branchNames, results, repoAndBranchList.size());
 		resultsDisplay.toString();
 		*/
 	}
 	
 	private static String getWorkDirectoryForBranch (String workDirectory, String repositoryName, String branchName)
 	{
 		String returnString = workDirectory;
 		if (!workDirectory.endsWith(File.separator))
 			returnString += File.separatorChar;
 		
 		if (null != repositoryName && !repositoryName.isEmpty())
 			returnString += repositoryName + File.separatorChar;
 		
 		if (null != branchName && !branchName.isEmpty())
 			returnString += branchName + File.separatorChar;
 		
 		return returnString;
 	}
 	
 	protected static RepositoryService GetRepositoryService ()
 	{
 		if (null == s_repositoryService)
 			s_repositoryService = new RepositoryService();
 		
 		return s_repositoryService;
 	}
 	
 	public static Repository GetRepository (String user, String pw, String repositoryName)
 	{
 		// Authenticate
 
 		// Get the repository
 		Repository repo = null;
 		try {
 			repo = GetRepositoryService().getRepository (user, repositoryName);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 		
 		return repo;
 	}
 	
 	
 	private static List<String> GetBranches (Repository repo)
 	{
 		assert (null != repo);
 
 		List<String> branchesList = new ArrayList<String>();
 				
 		// Get Branches/forks
 		//int forks = repo.getForks();
 		//if (0 == forks)
 		//	return branchesList;
 		
 		
 		
 		//if (forksList.isEmpty())
 		//	return branchesList;
 
 		List<RepositoryBranch> branches;
 		try{
 			branches = GetRepositoryService().getBranches (repo);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return branchesList;
 		}
 
		branchesList.add(repo.getHtmlUrl());
 		
 		for(RepositoryBranch repoBranch : branches)
 		{
 			if (isBranchValid (repoBranch))
 				branchesList.add (repoBranch.getName());
 		}
 		
 		return branchesList;
 	}
 	
 	public static List<String> GetBranches (String user, String repositoryName)
 	{
 		Repository repo = GetRepository(user, null, repositoryName);
 		
 		if (null == repo)
 			return null;
 		
 		return GetBranches (repo);
 	}
 	
 	private static void GetForksAndBranches(Repository mainRepo, HashMap<Repository, List<String>> repositoryToBranchMap)
 	{
 		List<String> branchList = GetBranches (mainRepo);
 		repositoryToBranchMap.put (mainRepo, branchList);
 		
 		List<Repository> forksList;
 		try {
 			forksList = GetRepositoryService().getForks (mainRepo);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return;
 		}
 		
 		for (Repository fork : forksList)
 		{
 			List<String> forkBranchList = GetBranches (fork);
 			
 			// TODO: Suppress duplicate Branches across forks
 			
 			
 			if (null != branchList)
 				repositoryToBranchMap.put (fork, forkBranchList);
 		}
 	}
 	
 	public static boolean isForkValid (Repository mainRepo, Repository fork)
 	{
 		// TODO: implement
 		return true;
 	}
 	
 	public static boolean isBranchValid (RepositoryBranch repoBranch)
 	{
 		// TODO: implement
 		return true;
 	}
 
 }
