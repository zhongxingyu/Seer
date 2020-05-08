 import java.io.File;
 import java.io.IOException;
 
 import tcwi.exception.Exceptions;
 import tcwi.fileHandler.Check;
 import tcwi.xml.Parser;
 
 
 public class Web_DeleteProject {
 	private static final String VERSION = "0.0.0.1";
 	private static final String AUTHORS = "EifX & hulllemann";
 	private static Parser parser;
 	private static Check check = new Check();
 	private static Exceptions exception = new Exceptions();
 	
 	public static void main(String[] args) {
 		if(args.length!=2){
 			System.out.println("Help - web_DeleteProject "+VERSION+" by "+AUTHORS);
 			System.out.println("----------------------------------------------------");
 			System.out.println("\nUsage: web_DeleteProject [PROJECTNAME] [GLOBAL_SETTINGS]");
 			System.out.println("\n[PROJECTNAME]");
 			System.out.println("\n     Project name\n");
 			System.out.println("\n[GLOBAL_SETTINGS]");
 			System.out.println("\n     Absolute Path for the global_settings.xml\n     (include the name of the settings file)\n");
 		}else{
 			
 			//Init the Parser
 			parser = new Parser(args[1]);
 			String[] xpathProjectsPath = {"settings","global","projects","path"};
 			String webIntProjectsPath = "";
 
 			try{
 			webIntProjectsPath = parser.read_setting(xpathProjectsPath);
 			} catch (IOException e) {
 				exception.throwException(1, e, true, "");
 			} catch (Exception e) {
 				String path = "";
 				for(int i=0;i<xpathProjectsPath.length;i++){
 					path += xpathProjectsPath[i]+" ";
 				}
 				exception.throwException(2, e, true, path);
 			}
 			
 			parser = new Parser(args[1]);
 			String[] xpathTreeviewPath = {"settings","website","generic","treeview","path"};
 			String webIntTreeviewPath = "";
 
 			try{
 				webIntTreeviewPath = parser.read_setting(xpathTreeviewPath);
 			} catch (IOException e) {
 				exception.throwException(1, e, true, "");
 			} catch (Exception e) {
 				String path = "";
 				for(int i=0;i<xpathTreeviewPath.length;i++){
 					path += xpathTreeviewPath[i]+" ";
 				}
 				exception.throwException(2, e, true, path);
 			}
 			
 			File f = new File(webIntTreeviewPath + check.folderSeparator() + args[0] + ".js");
 			f.delete();
 			
 			f = new File(webIntProjectsPath + check.folderSeparator() + args[0] + ".project");
 			f.delete();
 			f = new File(webIntProjectsPath + check.folderSeparator() + args[0] + ".project.xml");
 			f.delete();
 			f = new File(webIntProjectsPath + check.folderSeparator() + args[0] + ".lst");
 			f.delete();
			System.out.println("Project "+ args[0] +" successfully deleted!");
 		}
 	}
 
 }
