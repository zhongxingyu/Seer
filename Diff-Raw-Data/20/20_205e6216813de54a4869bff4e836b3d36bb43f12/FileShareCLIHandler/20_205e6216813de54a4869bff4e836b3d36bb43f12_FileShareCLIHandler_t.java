 package org.peerbox.demo.cli;
 
 import java.io.File;
 import java.io.PrintStream;
 
 import org.peerbox.fileshare.FileInfo;
 import org.peerbox.fileshare.FileShareManager;
 import org.peerbox.fileshare.ResponseListener;
 import org.peerbox.fileshare.messages.FileResponse;
 import org.peerbox.fileshare.messages.SharedDirectoryResponse;
 import org.peerbox.friend.Friend;
 import org.peerbox.friend.FriendManager;
 import org.peerbox.network.http.HttpClientListener;
 
 public class FileShareCLIHandler implements CLIHandler{
 	
 	protected FileShareManager fileshare;
 	protected FriendManager friend_manager;
 	public FileShareCLIHandler(FileShareManager fs_manager, FriendManager fr_manager) {
 		this.fileshare = fs_manager;
 		this.friend_manager = fr_manager;
 	}
 	@Override
 	public void handleCommand(final String[] args, final ExtendableCLI cli) {
 		// TODO Auto-generated method stub
 		if(args.length < 2) {
 			help(cli.out());
 			return;
 		}
 		String function = args[1];
 		if(function.equalsIgnoreCase("share")) {
 			if(args.length != 3) {
 				help(cli.out());
 				return;
 			}
 			String folder = args[2];
 			if(fileshare.setSharedFilePath(folder)) {
 				cli.out().println("Sharing folder located at "+folder);
 			} else {
 				cli.out().println("No directory exists at "+folder+".  Please specify another folder to share");
 			}
 		}
 		else if(function.equalsIgnoreCase("downloadTo")) {
 			if(args.length != 3) {
 				help(cli.out());
 				return;
 			}
 			String folder = args[2];
 			if(fileshare.setDownloadFilePath(folder)) {
 				cli.out().println("Download folder located at "+folder);
 			} else {
 				cli.out().println("No directory exists at "+folder+".  Please specify another folder to download files to");
 			}
 		}
 		else if(function.equalsIgnoreCase("browse")) {
 			if(args.length < 3 || args.length > 4) {
 				help(cli.out());
 				return;
 			}
 			final String alias = args[2];
 			Friend friend = friend_manager.getFriend(alias);
			if (friend == null || !friend.isAlive()) {
 				System.out.println("friend is offline");
 				return;
 			}
 			if(friend != null) {
 				String relativePath = "";
 				if(args.length == 4) {
 					relativePath = args[3];
 				}
 				fileshare.getSharedDirectory(relativePath, friend, new ResponseListener<SharedDirectoryResponse>() {
 
 					@Override
 					public void onFailure() {
 						// TODO Auto-generated method stub
 						cli.out().println("Unable to retrieve shared directory from "+alias);
 					}
 
 					@Override
 					public void onResponseReceived(
 							SharedDirectoryResponse response) {
 						if(response.getContents() != null) {
 							FileInfo[] contents = response.getContents();
 							for(int i=0; i<contents.length;i++){
 								String directory = "";
 								if(contents[i].getType().equals("directory")){
 									directory = "/";
 								}
 								cli.out().println(directory+contents[i].getName());
 							}
 						}
 						else{
 							if(args.length == 4) {
 								cli.out().println("The shared directory does not exist");
 							} else {
 								cli.out().println(alias+" is not sharing a directory");
 							}
 						}
 						
 					}
 					
 				});
 			}
 			else {
 				cli.out().println("The friend "+alias+" does not exist");
 			}
 		}
 		else if(function.equalsIgnoreCase("get")) {
 			if(args.length != 4) {
 				help(cli.out());
 				return;
 			}
 			final String alias = args[2];
 			Friend friend = friend_manager.getFriend(alias);
 			if (!friend.isAlive()) {
 				System.out.println("friend is offline");
 				return;
 			}
 			if(friend != null) {
 				final String filePath = args[3];
 				final File file = new File(filePath);
 				fileshare.getFile(file.getParent(), friend, file.getName(), new ResponseListener<FileResponse>() {
 
 					@Override
 					public void onFailure() {
 						cli.out().println("Failed to receive response to file request");
 						
 					}
 
 					@Override
 					public void onResponseReceived(
 							FileResponse response) {
 						//System.out.println("RECEIVED RESPONSE TO FILE REQUEST");
 						//TODO: Pass full download path
 						if(response.getFileLocURI() == null) {
 							cli.out().println("The requested file "+file.getName()+" is not available");
 						} else {
 							fileshare.download(response.getFileLocURI(), file.getName(), new HttpClientListener() {
 
 								@Override
 								public void finished() {
 									// TODO Auto-generated method stub
 									cli.out().println("Successfully downloaded file "+file.getName());
 								}
 
 								@Override
 								public void downloadError() {
 									// TODO Auto-generated method stub
 									cli.out().println("Unable to download reqested file "+file.getName());
 								}
 
 								@Override
 								public void started() {
 									// TODO Auto-generated method stub
 									cli.out().println("Started downloading requested file "+file.getName());
 								}
 
 								@Override
 								public void localFileError() {
 									// TODO Auto-generated method stub
 									cli.out().println("Unable to open file to download to");
 								}
 
 							});
 						}
 						
 					}
 					
 				});
 				
 			}
 			else {
 				cli.out().println("The friend "+alias+" does not exist");
 			}
 		}
		else {
			help(cli.out());
			return;
		}
 		
 	}
 	
 	public void help(PrintStream out) {
 		out.println("Function:\t Command");
 		out.println("Share Directory:\t share [directory]");
 		out.println("Browse Friend's Shared Directory:\t browse [alias] [relative path]");
 		out.println("Download Files To:\t downloadTo [directory]");
 		out.println("Download File:\t get [alias] [file path]");
 	}
 
 }
