 package gamepatcher;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Scanner;
 
 public class Patcher {
 	protected String pingURL;
 	protected void setUserDir(){
 //		sets property "user.dir" to the one specified, or leaves it as default if nothing is specified or an invalid location is specified
 		File file = new File(System.getProperty("user.dir") + File.separatorChar + "gamepatchersettings.txt");
 		if(file.exists()){
 			Scanner scanner = null;
 			try {
 				scanner = new Scanner(file);
 				if(scanner.hasNext())
 					scanner.nextLine();
 				if(scanner.hasNext()){
 					String location = scanner.nextLine();
 					location = location.substring(location.indexOf(':') + 1, location.length());
 					if(new File(location).exists())
 						System.setProperty("user.dir", location);
 				}
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} finally {
 				scanner.close();
 			}
 		}
 		else {
 			try {
 				makeSettings(file);
 				setUserDir();
 			} catch (IOException e) {
 				String attemptPath = file.getAbsolutePath();
 				while (true){
 					if(attemptPath.lastIndexOf(File.separatorChar) == -1)
 						break;
 					File attemptFile = new File(attemptPath.substring(0, attemptPath.lastIndexOf(File.separatorChar) - 1).substring(0, attemptPath.substring(0, attemptPath.lastIndexOf(File.separatorChar) - 1).lastIndexOf(File.separatorChar)));
 					if(attemptFile.exists()){
 						System.setProperty("user.dir", attemptFile.getAbsolutePath());
 						setUserDir();
 						break;
 					}
 					else {
 						try {
 							makeSettings(attemptFile);
 							System.out.println("No write access in desired directory!  Settings made at " + attemptFile.getAbsolutePath());
 							System.out.println("The file will be downloaded to " + attemptFile.getAbsolutePath() + " as well.");
 							System.setProperty("user.dir", attemptFile.getAbsolutePath());
 							setUserDir();
 							break;
 						} catch (IOException e1) {
 							
 						}
 					}
 				}
 			}
 		}
 	}
 	protected void setPingURL(){
 //		sets variable pingURL to the ping URL specified in settings
 		File file = new File(System.getProperty("user.dir") + File.separatorChar + "gamepatchersettings.txt");
 		if(file.exists()){
 			Scanner scanner = null;
 			try {
 				scanner = new Scanner(file);
 				if(scanner.hasNext())
 					for(int i = 0; i < 2; i++)
 						scanner.nextLine();
 				if(scanner.hasNext()){
 					String url = scanner.nextLine();
 					url = url.substring(url.indexOf(':') + 1, url.length());
 					if(new URL(url).getHost() != null)
 						pingURL = url;
 				}
 			} catch (FileNotFoundException e) {
 				ErrorLogger.logError(e);
 			} catch (MalformedURLException e) {
 				ErrorLogger.logError(e);
 			} finally {
 				scanner.close();
 			}
 		}
 		else {
 			try {
 				makeSettings(file);
 				setPingURL();
 			} catch (IOException e) {
 				String attemptPath = file.getAbsolutePath();
 				while (true){
 					if(attemptPath.lastIndexOf(File.separatorChar) == -1)
 						break;
 					File attemptFile = new File(attemptPath.substring(0, attemptPath.lastIndexOf(File.separatorChar) - 1).substring(0, attemptPath.substring(0, attemptPath.lastIndexOf(File.separatorChar) - 1).lastIndexOf(File.separatorChar)));
 					if(attemptFile.exists()){
 						System.setProperty("user.dir", attemptFile.getAbsolutePath());
 						setPingURL();
 						break;
 					}
 					else {
 						try {
 							makeSettings(attemptFile);
 							System.out.println("No write access in desired directory!  Settings made at " + attemptFile.getAbsolutePath());
 							System.out.println("The file will be downloaded to " + attemptFile.getAbsolutePath() + " as well.");
 							System.setProperty("user.dir", attemptFile.getAbsolutePath());
 							setPingURL();
 							break;
 						} catch (IOException e1) {
 							
 						}
 					}
 				}
 			}
 		}
 	}
 	private void makeSettings(File settings) throws IOException {
//		makes the settings file and inputs default values (line 1-"Custom directory path:", line 2-"Custom ping URL:https://google.com/")
 		if(settings.createNewFile()){
 			FileWriter settingsWriter = new FileWriter(settings);
 			BufferedWriter bw = new BufferedWriter(settingsWriter);
 			bw.write("For all settings, write the setting right after the character ':'.  No spaces, please.");
 			bw.newLine();
 			bw.write("Custom directory path (no setting will result in the directory that the launcher is in):");
 			bw.newLine();
			bw.write("Custom ping URL:https://google.com/");
 			bw.close();
 		}
 		else
 			return;
 	}
 }
