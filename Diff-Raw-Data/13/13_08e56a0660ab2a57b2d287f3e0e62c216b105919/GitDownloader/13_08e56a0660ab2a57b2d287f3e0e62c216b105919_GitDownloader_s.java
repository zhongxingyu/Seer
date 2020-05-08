 package styling.view;
 
 public class GitDownloader {
 
 	public static String download(String repository) {
 		
 		String path = "";
 		String projectName = getProjectName(repository);
 		try {
 			
 			String userHome = System.getProperty("user.home");			
			Runtime.getRuntime().exec("mkdir " + userHome + "/workspace/");
			Runtime.getRuntime().exec("git clone " + repository + " " + userHome + "/workspace/" + projectName);
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
 
