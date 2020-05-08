 package pcc.ui;
 import java.io.File;
 import java.io.IOException;
 import java.util.Scanner;
 
 import pcc.analysis.ChangeCounterUtils;
 import pcc.io.IOUtils;
 import pcc.vercon.Project;
 import pcc.vercon.ProjectVersion;
 
 
 public class Main{
 	private static Project project;
 	private static String projectName;
 	private static Scanner in;
 	public static void main(String[] args){
 		//init resources
 		in = new Scanner(System.in);
 		//display the commands
 		displayCommands();
 		//ask for input until forced exit
 		while(true)
 			promptMainMenu();
 	}
 	public static void openProject(){
 		System.out.print("Project name: ");
 		projectName = in.nextLine();
 		try {
 			project = IOUtils.openProject(projectName+File.separator+"project.dat");
 		} catch (Throwable t){
 			t.printStackTrace();
 			System.out.println("Error opening project file.");
 		}
 	}
 	private static void saveProject(){
 		if(project==null)
 			return;
 		try {
 			File dir = new File(projectName);
 			if(!dir.exists())
 				IOUtils.createFolder(projectName);
 			IOUtils.saveProject(project, projectName+File.separator+"project.dat");
 		} catch (Throwable t) {
 			t.printStackTrace();
 			System.out.println("Error updating project file.");
 		}
 	}
 	public static void explicitSave(){
 		if(project==null){
 			System.out.println("Please open or create a project first.");
 			return;
 		}
 		saveProject();
 	}
 	public static void promptMainMenu(){
 		//request input
 		String input = in.nextLine();
 		if(input.equals("new"))
 			newProject();
 		else if(input.equals("open"))
 			openProject();
 		else if(input.equals("add"))
 			addFile();
 		else if(input.equals("rm"))
 			removeFile();
 		else if(input.equals("commit"))
 			commitNewVersion();
 		else if(input.equals("ecl"))
 			exportChangeLables();
 		else if(input.equals("files"))
 			displayAllFiles();
 		else if(input.equals("version"))
 			displayCurrentVersion();
 		else if(input.equals("versions"))
 			displayAllVersions();
 		else if(input.equals("data"))
 			displayVersionData();
 		else if(input.equals("change"))
 			displayVersionChanges();
 		else if(input.equals("exit"))
 			System.exit(0);
 		else if(input.equals("help"))
 			displayCommands();
 		else if(input.equals("save"))
 			saveProject();
 		else{
 			System.out.println("Invalid command entered.");
 			displayCommands();
 		}
 	}
 	private static void displayCommands(){
 		System.out.println("The following are the only valid commands:");
 		System.out.println("  new: Create a new project.");
 		System.out.println("  open: Open an existing project.");
 		System.out.println("  add: Add a file to the current project.");
 		System.out.println("  rm: Remove a file from the current project.");
 		System.out.println("  commit: Commit a new version of the projet.");
 		System.out.println("  ecl: Export Change Label Files.");
 		System.out.println("  files: Display all files in the current project.");
 		System.out.println("  version: Display the current project version.");
 		System.out.println("  versions: Display all versions of the current project.");
 		System.out.println("  data: Display data for a selected version.");
 		System.out.println("  change: Display verion changes.");
 		System.out.println("  exit: Close this program.");
 		System.out.println("  help: Display this data.");
 	}
 	public static void newProject(){
 		System.out.print("Project name: ");
 		projectName = in.nextLine();
 		if(!projectName.matches("[a-zA-Z0_9_]+"))
 			System.out.println("Invalid name entered: letters and underscores only.");
 		else{
 			if((new File(projectName)).exists())
 				System.out.println("A folder/file with the given name already exists.");
 			else{
 				project = new Project();
 			}
 		}
 	}
 	public static void addFile(){
 		if(project==null){
 			System.out.println("Please open or create a project first.");
 			return;
 		}
 		System.out.print("File name: ");
 		String name = in.nextLine();
 		project.addFile(name);
 		saveProject();
 	}
 	public static void removeFile(){
 		if(project==null){
 			System.out.println("Please open or create a project first.");
 			return;
 		}
 		System.out.print("File name: ");
 		String name = in.nextLine();
 		project.removeFile(name);
 		saveProject();
 	}
 	public static void commitNewVersion(){
 		if(project==null){
 			System.out.println("Please open or create a project first.");
 			return;
 		}
 		System.out.print("Version number: ");
 		String number = in.nextLine();
 		System.out.print("Author name: ");
 		String author = in.nextLine();
 		System.out.print("Reason for commit: ");
 		String reason = in.nextLine();
 		try {
 			project.commit(number, author, reason);
 		} catch (IOException e) {
 			System.out.println("Error commiting source files.");
 		}
 		saveProject();
 	}
 	public static void exportChangeLables(){
 		if(project==null){
 			System.out.println("Please open or create a project first.");
 			return;
 		}
 		System.out.print("First version: ");
 		String version1 = in.nextLine();
 		System.out.print("Last version: ");
 		String version2 = in.nextLine();
 		System.out.print("Output directory: ");
 		String dir = in.nextLine();
 		ChangeCounterUtils.exportChangeLabels(dir,
 				project.getVersion(version1), project.getVersion(version2));
 	}
 	public static void displayAllFiles(){
 		if(project==null){
 			System.out.println("Please open or create a project first.");
 			return;
 		}
 		for(String fn:project.getFiles())
 			System.out.println(fn);
 	}
 	public static void displayCurrentVersion(){
 		if(project==null){
 			System.out.println("Please open or create a project first.");
 			return;
 		}
 		System.out.println(project.getCurrentVersion());
 	}
 	public static void displayAllVersions(){
 		if(project==null){
 			System.out.println("Please open or create a project first.");
 			return;
 		}
 		for(String v:project.getVersionList())
 			System.out.println(v);
 	}
 	public static void displayVersionData(){
 		if(project==null){
 			System.out.println("Please open or create a project first.");
 			return;
 		}
 		System.out.print("Version: ");
 		String number = in.nextLine();
 		ProjectVersion version = project.getVersion(number);
 		System.out.println(version.getMetaData());
 		System.out.println("Total LLOC: "+ChangeCounterUtils.getLLOC(version));
 	}
 	public static void displayVersionChanges(){
 		if(project==null){
 			System.out.println("Please open or create a project first.");
 			return;
 		}
 		System.out.print("[S]hort or [L]ong report? ");
 		String type = in.nextLine();
 		if(!type.matches("[sSlL]")){
 			System.out.println("Invalid report type.");
 			return;
 		}
 		System.out.print("First version: ");
 		String version1 = in.nextLine();
 		System.out.print("Last version: ");
 		String version2 = in.nextLine();
 		ProjectVersion v1 = project.getVersion(version1);
 		ProjectVersion v2 = project.getVersion(version2);
 		
 		String report = "";
 		if(type.matches("[sS]"))
 			report = ChangeCounterUtils.getLLOCChanges(v1, v2);
 		else
 			report = ChangeCounterUtils.getLineChangesReport(v1, v2);
 		System.out.println(report);
 	}
 }
