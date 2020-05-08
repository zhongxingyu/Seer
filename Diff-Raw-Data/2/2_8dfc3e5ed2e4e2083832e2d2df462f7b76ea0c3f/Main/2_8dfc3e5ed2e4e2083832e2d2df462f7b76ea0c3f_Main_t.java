 package pojoGenerator;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.ProjectHelper;
 
 public class Main {
 
 	public static void main(String[] args) throws Exception {
 
 		PathHolder.loadConfig();
 		deleteFolders();
 		createParentStructure();
 		checkOutFiles();
 
 		File f = new File(PathHolder.SOURCE_PATH);
 		generatePojoClasses(f);
 
 		File pojoPath = new File(PathHolder.TEMP_POJO);
 		removeUnwantedFilesAndStrings(pojoPath);
 
 		System.out.println("\nCompleted generating pojo's at path " + PathHolder.POJO_TARGET_PATH);
 
 
 	}
 
 	private static String getFileNameWithoutExtension(String name) {
 		int pos = name.lastIndexOf(".");
 		if (pos > 0) {
 		    name = name.substring(0, pos);
 		}
 		return name;
 	}
 	
 	public static final String[] unwantedContainsArray = {
 		"org.apache.commons.lang.builder", "org.codehaus.jackson",
		"javax.annotation", "additionalProperties"};
 
 	public static final String[] unwantedStartsWithArray = { "@" };
 
 	private static void refactorFileAndGenerateDuplicate(File f) throws Exception {
 		String path = PathHolder.POJO_TARGET_PATH + File.separator + f.getPath().substring(f.getPath().indexOf("com" + File.separator));
 
 		File targetFile = new File(path);
 		File parent = targetFile.getParentFile();
 		if(!parent.exists() && !parent.mkdirs()){
 			throw new IllegalStateException("Couldn't create dir: " + parent);
 		}
 
 		FileReader fileReader = new FileReader(f);
 		BufferedReader reader = new BufferedReader(fileReader);
 
 		BufferedWriter writer = new BufferedWriter(new FileWriter(path));
 
 
 		String currentLine;
 
 		boolean isValueFound = false;
 
 		StringBuilder dataHolder = new StringBuilder();
 
 		while ((currentLine = reader.readLine()) != null) {
 			String trimmedLine = currentLine.trim();
 
 			isValueFound = false;
 			for (String value : unwantedStartsWithArray) {
 				if (trimmedLine.startsWith(value)) {
 					isValueFound = true;
 					break;
 				}
 			}
 
 			if (isValueFound) {
 				continue;
 			}
 
 			isValueFound = false;
 			for (String value : unwantedContainsArray) {
 				if (trimmedLine.contains(value)) {
 					isValueFound = true;
 					break;
 				}
 			}
 
 			if (isValueFound) {
 				continue;
 			}
 
 
 
 			if (currentLine.startsWith("public class")) {
 
 				dataHolder.append("import com.google.gson.Gson;\n");
 
 				dataHolder.append("import com.wethejumpingspiders.finance.base.PojoDataPartInterface;\n");
 				dataHolder.append("public class " + getFileNameWithoutExtension(f.getName()) + " implements PojoDataPartInterface {\n");
 				File servlet = f.getParentFile().getParentFile();
 				File base = servlet.getParentFile();
 
 
 				String getServletGroupMethod = "public String getServletGroup() {\n return \"" + base.getName() + "\";\n }";
 				String getServletNameMethod = "public String getServletName() {\n return \"" + servlet.getName() + "\";\n }";
 				String toJSON = "public String toJSON() {\n Gson gson = new Gson(); \n return gson.toJson(this);\n }";
 				dataHolder.append(getServletGroupMethod);
 				dataHolder.append("\n");
 				dataHolder.append(getServletNameMethod);
 				dataHolder.append("\n");
 				dataHolder.append(toJSON);
 
 				continue;
 			}
 
 			dataHolder.append(currentLine);
 			dataHolder.append("\n");
 		}
 
 
 
 		String modifiedData = dataHolder.toString();
 
 		writer.write(modifiedData);
 
 		fileReader.close();
 		writer.flush();
 		writer.close();
 
 	}
 
 	private static void removeUnwantedFilesAndStrings(File f) throws Exception{		
 		for (File file : f.listFiles()) {
 			if (file.isDirectory()) {
 				removeUnwantedFilesAndStrings(file);
 			}
 			else {
 				if (file.getName().endsWith(".java")) {
 					String[] nameOfFilesToBeDeleted = {"Request.java", "Request_.java", "Echo.java", "Response.java", "Response_.java"};					
 					for (int i = 0; i < nameOfFilesToBeDeleted.length; i++) {
 						if (file.getName().equalsIgnoreCase(nameOfFilesToBeDeleted[i])) {
 							deleteDir(file);
 							return;
 						}
 					}
 
 					refactorFileAndGenerateDuplicate(file);
 
 				}
 			}
 		}
 	}
 
 	private static void generatePojoClasses(File f) {		
 		for (File file : f.listFiles()) {
 			if (file.isDirectory()) {
 				generatePojoClasses(file);
 			}
 			else {
 				if (file.getName().endsWith(".json")) {
 					System.out.println(file.getName() + " is a .json file.");
 
 					File servlet = file.getParentFile().getParentFile();
 					File base = servlet.getParentFile();
 
 					String basePackage = PathHolder.PACKAGENAME + "." + base.getName() + "." + servlet.getName();
 
 					if (file.getName().startsWith("request")) {
 						createPojo(file.getAbsolutePath(), PathHolder.TEMP_POJO, basePackage + ".Request");
 					}
 					else if (file.getName().startsWith("response")) {
 						createPojo(file.getAbsolutePath(), PathHolder.TEMP_POJO, basePackage + ".Response");
 					}
 				}
 			}
 		}
 	}
 
 	private static void createPojo(String fromJSONFileNamed, String targetFolder, String packageName) {
 
 		System.out.println("\nTrying to generate POJO from " + fromJSONFileNamed
 				+ " \nto: " + targetFolder + "\nusingPackageName " + packageName);
 
 		File buildFile = new File("build.xml");
 		Project p = new Project();
 		p.setUserProperty("ant.file", buildFile.getAbsolutePath());
 		p.setUserProperty("targetPackage", packageName);
 		p.setUserProperty("targetDirectory", targetFolder);
 		p.setUserProperty("source", fromJSONFileNamed);
 		p.init();
 		ProjectHelper helper = ProjectHelper.getProjectHelper();
 		p.addReference("ant.projectHelper", helper);
 		helper.parse(p, buildFile);
 		p.executeTarget(p.getDefaultTarget());
 	}
 
 	public static void checkOutFiles() throws Exception {
 
 		File f = new File(PathHolder.CHECKOUT_PATH);
 		Process p = Runtime.getRuntime().exec(PathHolder.SVN_CO_CMD, null, f);
 		readFromProcess(p);
 	}
 
 	public static void deleteFolders() {
 		String[] dirCreater = { PathHolder.SCHEMA_FILE, PathHolder.POJO_TARGET_PATH, PathHolder.TEMP_POJO};
 		for (int i = 0; i < dirCreater.length; i++) {
 			deleteDir(new File(dirCreater[i]));
 		}
 	}
 
 	public static void createParentStructure() {
 		String[] path = {
 				PathHolder.CHECKOUT_PATH, PathHolder.POJO_TARGET_PATH, PathHolder.TEMP_POJO};
 		File mainFolder = new File(PathHolder.MAIN_FOLDER);
 		if (mainFolder.exists()) {
 			deleteDir(mainFolder);
 		}
 		mainFolder.mkdir();
 		for (int i = 0; i < path.length; i++) {
 			File file = new File(path[i]);
 
 			file.mkdirs();
 		}
 	}
 
 	public static void deleteDir(File dir) {
 		if (dir.isDirectory()) {
 			File[] parent = dir.listFiles();
 			for (File file : parent) {
 
 				if (file.isDirectory()) {
 					deleteDir(file);
 				}
 				file.delete();
 			}
 		}
 	}
 
 	private static void readFromProcess(Process p) throws IOException {
 		BufferedReader bi = new BufferedReader(new InputStreamReader(
 				p.getInputStream()));
 		String lineString = null;
 		while ((lineString = bi.readLine()) != null) {
 			System.out.println(lineString);
 		}
 		bi.close();
 
 	}
 
 
 }
