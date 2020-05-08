 import java.io.File;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 public class SearchConsole {
 	public static void main (String args[]){
 		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
 		String rootFolder = "";
 		String fileName = "";
 		String addFile = "y";
 		try {
 			System.out.println("Set root folder:");
 			rootFolder = input.readLine();
 			File rootDirectory = new File(rootFolder);
 			if (rootDirectory.exists()){
 				if (rootDirectory.isDirectory()){
 //Oleh: Usually it is better to name variables, fields and classes as noun. In this case it would be "fileFinder"
 //Igor: done						
 					FileFinder fileFinder = new FileFinder();
 					System.out.println("Input file name to find:");
 //Oleh: spaces around " = "
 //Igor: Fixed						
 					fileName = input.readLine();
 					fileFinder.searchingFiles.add(fileName);
 					while (!addFile.equals("n"))	{
 //Oleh: another (typo)
 //Igor: Fixed							
 						System.out.println("Do you want input another file(s) to search(y/n)?[y]");
 						addFile = input.readLine();
 				
 						if((addFile.isEmpty()) || (addFile.equals("y"))){
 							System.out.println("Input file name to search:");
							fileName=input.readLine();
 							fileFinder.searchingFiles.add(fileName);
 							addFile = "y";
 						}else {
 //Oleh: Always use curly brackets even if there is only one operator after it. This is a holy rule.
 //Igor: Fixed								
 								if (!addFile.equals("n")){
 //Oleh: Why wrong?
 //Igor: This message appears when user set neither "y" no "n"
 //added additional explanation in mw\essage  										
 									//System.out.println("Wrong choice!!!");
 									System.out.println("Wrong choice!!! Should be \"y\" or \"n\"");
 								}
 							}
 								
 			}
 			
 					fileFinder.findFiles(rootDirectory);
 					System.out.println("---------------FOUND FILES---------------");
 					fileFinder.printResults();
 				}else{
 //Oleh: "else" should go on the same line as closing bracket, i.e. "} else {" or "} else if (...) {"
 //Igor: Fixed
 				
 						System.out.println("Specified path is not folder");
 					}
 			}else { 
 					System.out.println("Root folder does not exist");
 			 }
 			
 			
 		}
 		catch (IOException e){
 //Oleh: bracket should be on the same line
 //Igor:Fixed
 				System.out.println(e.getMessage());
 			
 		}
 			
 	}
 
 }
