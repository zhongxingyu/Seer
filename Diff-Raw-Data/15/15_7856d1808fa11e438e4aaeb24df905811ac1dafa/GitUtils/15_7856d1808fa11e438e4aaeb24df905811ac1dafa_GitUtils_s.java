 package utils;
 
 public final class GitUtils {
 
 	private GitUtils() {
 	}
 
 	public final static String gitBranch(final String projectPath) {
		return FileSystemUtils.runCommand("/usr/bin/git branch", FileSystemUtils.getCanonicalPath(projectPath));
 	}
 
 }
