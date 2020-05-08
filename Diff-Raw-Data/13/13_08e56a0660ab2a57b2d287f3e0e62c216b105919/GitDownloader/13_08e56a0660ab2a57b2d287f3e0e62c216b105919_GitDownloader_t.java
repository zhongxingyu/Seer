 package styling.view;
 
 public class GitDownloader {
 
 	public static String download(String repository) {
 		
 		String path = "";
 		String projectName = getProjectName(repository);
 		try {
 			
 			String userHome = System.getProperty("user.home");			
			
			Process mkdirProcess = Runtime.getRuntime().exec("mkdir " + userHome + "/workspace/");
			mkdirProcess.waitFor();
			
			String comandoGit = "git clone " + repository + " " + userHome + "/workspace/" + projectName;
			
			System.out.println("Comando git a ejecutar: " + comandoGit);
			
			Process gitProcess = Runtime.getRuntime().exec(comandoGit);
			gitProcess.waitFor();

 			path = userHome + "/workspace/" + projectName;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return path;
 	}
 
 	private static String getProjectName(String repository) {
 		
 		String[] split = repository.split("/");
 		return split[split.length - 1].replace(".git", "");
 	}
 }
 
