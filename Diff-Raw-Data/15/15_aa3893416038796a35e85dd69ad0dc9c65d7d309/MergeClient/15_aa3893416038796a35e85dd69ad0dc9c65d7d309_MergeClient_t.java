 import gitHelper.BranchInfo;
 import gitHelper.GitHelper;
 import gitHelper.utils.OperationException;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Merges the the branches of a github repository 
  * Usage:
  * MergeClient userName repository [-HBN=hostBranchName|-allBranches|-allforks] [-MBN=mergeBranchName]
  *		    [-MU=mergeUserName] [-MR=mergeRepository]  [-CIP=controllerIP] [-CP=controllerPort]
  *
  *		    Where [] represents optional arguments
  * 
  * @author AsharGhani
  * 
  */
 public class MergeClient {
 
 	private static String s_usage = "MergeClient userName repository [-HBN=hostBranchName|-allBranches|-allforks] [-MBN=mergeBranchName]"
 			+ "[-MU=mergeUserName] [-MR=mergeRepository]  [-CIP=controllerIP] [-CP=controllerPort] "
 			+ "\nWhere [] represents optional arguments";
 
 	private static String s_localBaseDir = "localRepositories" + File.separatorChar;
 	private static String s_masterDirName = "__masterBranch__";
 
 	
 	static void printParsingError (String invalidArgument)
 	{
 		System.out.println ("Error! Invalid Argument: " + (null != invalidArgument?invalidArgument:"") + "\n\nUsage:\n\n" + s_usage);
 	}
 
 	
 	/**
 	 * @param String[]
 	 *            args: the input arguments to parse
 	 * @param ClientSettings
 	 *            settings: an output parameter to populate with the settings parsed
 	 * @return boolean
 	 * 			  true indicating a successfully parsed arguments list, false otherwise
 	 * @effect Parses the given list of arguments as per the correct usage:
 	 *          MergeClient userName repository [-HBN=hostBranchName|-allBranches|-allforks] [-MBN=mergeBranchName]
 	 *		    [-MU=mergeUserName] [-MR=mergeRepository]  [-CIP=controllerIP] [-CP=controllerPort]
 	 *		    Where [] represents optional arguments
 	 */
 	static boolean parseCommands 
 	(
 	String[] args,
 	ClientSettings settings
 	)
 	{
 		if (args.length < 2)
 			{
 			printParsingError (null);
 			return false;
 			}
 
 		settings.user = args[0];
 		settings.repo = args[1];
 
 		//Parse all the Optional arguments
 		for (int i = 2; i < args.length; i++) 
 		{
 			if (args[i].toLowerCase().startsWith ("-hbn")) 
 			{
 				String[] splits = args[i].split("=");
 				if (splits.length < 2) {
 					printParsingError(args[i]);
 					return false;
 				}
 
 				if (null != settings.branches)
 					return false;
 								
 				BranchInfo hostBranchInfo = new BranchInfo (settings.user, settings.repo, splits[1], null, null);
 				
 				settings.branches = new ArrayList<BranchInfo>() ;
 				settings.branches.add(hostBranchInfo );
 				
 				settings.mergeMode = MergeMode.HostAndMergeBranches;
 			}
 			else if (args[i].toLowerCase().startsWith ("-mbn")) 
 			{
 				String[] splits = args[i].split("=");
 				if (splits.length < 2) {
 					printParsingError(args[i]);
 					return false;
 				}
 
 				if (null == settings.branches || settings.branches.size() != 1)
 					{
 					printParsingError("-hbn argument expected before -mbn");
 					return false;
 					}
 				
 				BranchInfo hostBranchInfo = new BranchInfo (settings.user, settings.repo, splits[1], null, null);
 				settings.branches.add(hostBranchInfo );
 			}
 			else if (args[i].toLowerCase().startsWith ("-allbranches")) 
 			{
 				settings.mergeMode = MergeMode.AllBranches;
 			}
 			else if (args[i].toLowerCase().startsWith ("-allforks")) 
 			{
 				settings.mergeMode = MergeMode.AllForks;
 			}
 			else if (args[i].toLowerCase().startsWith ("-mu")) 
 			{
 				String[] splits = args[i].split("=");
 				if (splits.length != 2) {
 					printParsingError(args[i]);
 					return false;
 				}
 
 				if (null == settings.branches || settings.branches.size() != 2)
 				{
 					printParsingError("-hbn and -mbn argument expected before -mu ");
 					return false;
 				}
 				
 				settings.branches.get(1).user = splits[1];
 			}
 			else if (args[i].toLowerCase().startsWith ("-mr")) 
 			{
 				String[] splits = args[i].split("=");
 				if (splits.length != 2) {
 					printParsingError(args[i]);
 					return false;
 				}
 
 				if (null == settings.branches || settings.branches.size() != 2)
 				{
 					printParsingError("-hbn and -mbn argument expected before -mr ");
 					return false;
 				}
 				
 				settings.branches.get(1).repo = splits[1];
 			}
 			else if (args[i].toLowerCase().startsWith ("-cip")) 
 			{
 				String[] splits = args[i].split("=");
 				if (splits.length != 2) {
 					printParsingError(args[i]);
 					return false;
 				}
 
 				settings.controllerIP = splits[1];
 			}
 			else if (args[i].toLowerCase().startsWith ("-cp")) 
 			{
 				String[] splits = args[i].split("=");
 				if (splits.length != 2) {
 					printParsingError(args[i]);
 					return false;
 				}
 
 				//try
 				//{
 				settings.controllerPort = Integer.parseInt(splits[1]);
 				//}
 				//catch (){}
 			}
 			else
 				printParsingError(args[i]);
 		}
 		
 		if (MergeMode.HostAndMergeBranches == settings.mergeMode)
 		{
 			if (null == settings.branches || settings.branches.size() != 2)
 			{
 				printParsingError("-HBN requires -MBN argument to be supplied too");
 				return false;
 			}
 			else 
 			{
 				BranchInfo mergeBranchInfo = settings.branches.get(1);
 				if (null == mergeBranchInfo.user)
 					mergeBranchInfo.user = settings.user;
 				
 				if (null == mergeBranchInfo.repo)
 					mergeBranchInfo.repo = settings.repo;
 			}
 		}
 
 		return true;
 	}
 
 	
 	static String getRepositoryDirectory (String user, String repository)
 	{
 		File repositoryDir = new File (s_localBaseDir + user + File.separatorChar + repository + File.separatorChar);
 		if (!repositoryDir.exists())
			repositoryDir.mkdirs();
 		
 		String absolutePath = repositoryDir.getAbsolutePath();
 		
 		if (absolutePath.endsWith ("" + File.separatorChar))
 			return absolutePath;
 		
 		return absolutePath + File.separatorChar;
 	}
 	
 	
 	static String getRepositoryURL (String user, String repository)
 	{
 		return "http://github.com/" + user + "/" + repository;
 	}
 	
 	
 	static String getDefaultBranchName (String user, String repository)
 	{
 		//TODO: implement properly
 		return "master";
 	}
 	
 	
 	static String getMasterBranchDir (String hostRepositoryDir)
 	{
 		return hostRepositoryDir + s_masterDirName + File.separatorChar; 
 	}
 	
 	
 	static String getBranchDir (String mergeRepositoryDir, String branchName)
 	{
 		return mergeRepositoryDir + branchName + File.separatorChar;
 	}
 
 	
 	static boolean createBranchDirFromMaster (String masterBranchDir, String branchDir, String branchName)
 	{
 		try 
 		{
 			gitHelper.GitHelper.CheckoutBranch (branchName, masterBranchDir);
 
 			GitHelper.updateLocalRepositoryBranchAndCheckCacheError (masterBranchDir, branchDir, branchName);
 		} 
 		catch (OperationException e) {
 			e.printStackTrace();
 			return false;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 		
 		return true;
 	}
 	
 	
 	static BranchInfo getMasterBranchInfo (List<BranchInfo> masterBranchList, BranchInfo branch)
 	{
 		for (BranchInfo masterBranch : masterBranchList)
 		{
 			if (masterBranch.user == branch.user && masterBranch.repo == branch.repo)
 				return masterBranch;
 		}
 		
 		String masterRepositoryDir = getRepositoryDirectory (branch.user, branch.repo);			
 		String masterBranchDir = getMasterBranchDir (masterRepositoryDir);
 		
 		if (null == masterBranchDir)
 			return null;
 		
 		BranchInfo masterBranchInfo = new BranchInfo (
 													 branch.user,
 													 branch.repo,
 													 getDefaultBranchName(branch.user, branch.repo),
 													 masterBranchDir,
 													 branch.repositoryURL
 													 );
 		
 		// Create or Update the master branch directory
		try 
 		{
 			gitHelper.GitHelper.updateLocalRepositoryBranchAndCheckCacheError (masterBranchInfo.repositoryURL, masterBranchDir, null);
 		} 
 		catch (OperationException e)
 		{
 			e.printStackTrace();
 			return null;
 		} 
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 			return null;
		}
 		
 		masterBranchList.add(masterBranchInfo);
 		
 		return masterBranchInfo;
 	}
 	
 	
 	static boolean prepareBranches (ClientSettings settings)
 	{	
 		if (MergeMode.HostAndMergeBranches == settings.mergeMode)
 		{
 			// settings.branches should already be populated with exactly 2 branches
 			// assume the first to be host and the second to be the merge branch
 
 			if (null == settings.branches)
 				return false;
 
 			// there should be at least 2 branches to perform merge (1st string is the repositoryURL
 			if (settings.branches.size() != 2)
 				return true;
 
 			String baseRepositoryDir = getRepositoryDirectory (settings.user, settings.repo);
 			
 			List<BranchInfo> masterBranchList = new ArrayList<BranchInfo>();
 
 			for (BranchInfo branch : settings.branches)
 			{
 				branch.localDir = getBranchDir (baseRepositoryDir, branch.branchName);
 				branch.repositoryURL = getRepositoryURL (branch.user, branch.repo);
 				
 				BranchInfo masterBranchInfo = getMasterBranchInfo (masterBranchList, branch);
 				
 				if (null == masterBranchInfo)
 					return false;
 				
 				if (!createBranchDirFromMaster (masterBranchInfo.localDir, branch.localDir, branch.branchName))
 					return false;
 				
 				// reset host branch dir to correct branch - this would have been disturbed during branch dir creation
 				try 
 				{
 					GitHelper.CheckoutBranch (masterBranchInfo.branchName, masterBranchInfo.localDir);
 				} 
 				catch (OperationException e)
 				{
 					e.printStackTrace();
 					return false;
 				}
 			}
 			return true;
 		}
 		else if (MergeMode.AllBranches == settings.mergeMode)
 		{
 			List<String> allBranches = GitHelper.GetBranches (settings.user, settings.repo);
 
 			if (null == allBranches)
 				return false;
 
 			// there should be at least 2 branches to perform merge (1st string is the repositoryURL
 			if (allBranches.size() < 3)
 				return true;
 
 			String repositoryURL = allBranches.get(0);
 			
 			String baseRepositoryDir = getRepositoryDirectory (settings.user, settings.repo);
 			
 			settings.branches = new ArrayList<BranchInfo>();
 			
 			List<BranchInfo> masterBranchList = new ArrayList<BranchInfo>();
 
 			for (int i = 1; i < allBranches.size(); i++)				
 			{
 				String branchName = allBranches.get(i);
 				
 				String branchLocalDir = getBranchDir (baseRepositoryDir, branchName);
 				
 				BranchInfo branch = new BranchInfo  (
 													settings.user,
 													settings.repo,
 													branchName,
 													branchLocalDir,
 													repositoryURL
 													);
 				
 				BranchInfo masterBranchInfo = getMasterBranchInfo (masterBranchList, branch);
 				
 				if (!createBranchDirFromMaster (masterBranchInfo.localDir, branchLocalDir, branch.branchName))
 					return false;
 				
 				settings.branches.add(branch);
 			}
 			
 			for (BranchInfo masterBranchInfo : masterBranchList)
 			{
 				// reset host branch dir to correct branch - this would have been disturbed during branch dir creation
 				try 
 				{
 					GitHelper.CheckoutBranch (masterBranchInfo.branchName, masterBranchInfo.localDir);
 				} 
 				catch (OperationException e)
 				{
 					e.printStackTrace();
 					return false;
 				}
 			}
 			return true;
 		}
 		else
 			throw new java.lang.UnsupportedOperationException ("Mergeing All Branches - yet to be implemented");
 	}
 	
 	
 	static void performBranchesMerge (ClientSettings settings)
 	{
 		Socket 		socket = null;
 		PrintWriter socketOut = null;
 		if (settings.controllerIP != null)
 		{
 			try 
 			{
 				socket = new Socket (settings.controllerIP, settings.controllerPort);
 				socketOut= new PrintWriter (socket.getOutputStream());
 				
 			} catch (UnknownHostException e) 
 			{
 				e.printStackTrace();
 				return;
 			} catch (IOException e) 
 			{
 				e.printStackTrace();
 				return;
 			}
 		}
 
 		if (MergeMode.HostAndMergeBranches == settings.mergeMode || MergeMode.AllBranches == settings.mergeMode)
 		{
 			int hostIndex = -1;
 			for (BranchInfo hostBranch : settings.branches)
 			{
 				hostIndex++; 				
 				int branchIndex = -1;
 
 				for (BranchInfo mergeBranch : settings.branches)
 				{
 					branchIndex++;
 					if (hostIndex >= branchIndex)
 						continue;
 
 					String mergeResult = GitHelper.MergeBranches (hostBranch.localDir, mergeBranch.branchName, mergeBranch.localDir, null);
 
 					String messageString = "MergeResult: " + hostBranch.user + "\\" + hostBranch.repo + " " + 
 							hostBranch.branchName + " & " + mergeBranch.branchName + " = " + mergeResult;
 
 					if (null != socketOut)
 						socketOut.print(messageString);
 
 					//	JOptionPane.showMessageDialog (null, "MergeRelationship between " + hostBranch + " and " + mergeBranch + ": " + mergeRelationship);
 
 					System.out.println (messageString);
 
 					try 
 					{
 						GitHelper.ResetBranchSourceRepository (hostBranch.branchName, hostBranch.localDir);
 					} catch (OperationException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 		else
 		{
 			if (null != socket)
 			{
 				try 
 				{
 					socketOut.close();
 					socket.close();
 				} catch (IOException e) 
 				{
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			throw new java.lang.UnsupportedOperationException ("Mergeing All Branches - yet to be implemented");
 		}
 		
 		if (null != socket)
 		{
 			try 
 			{
 				socketOut.close();
 				socket.close();
 			} catch (IOException e) 
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	
 	static void notifyStatusToController (PrintWriter socketOut, String status)
 	{
 		
 		System.out.println("Status: " + status);
 	}
 	
 	public static void main(String[] args) 
 	{
 		// parse commands
 		ClientSettings settings = new ClientSettings();
 
 		if (!parseCommands (args, settings)) {
 			// TODO: notifyController if possible
 			return;
 		}
 		
 		// prepare the host and merge branches
 		if (!prepareBranches (settings))
 			return;
 
 		// perform merge
 		performBranchesMerge (settings);
 		
 		// delete merge branches? I don't think so...
 	}
 }
