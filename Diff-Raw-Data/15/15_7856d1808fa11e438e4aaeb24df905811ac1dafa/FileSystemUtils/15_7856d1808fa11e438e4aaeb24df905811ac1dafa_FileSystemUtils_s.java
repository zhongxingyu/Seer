 package utils;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStreamReader;
 
 public final class FileSystemUtils {
 
 	private FileSystemUtils() {
 	}
 
 	public static final String getCanonicalPath(final String path) {
 		try {
 			final File file = new File(path);
 			return file.getCanonicalPath();
 		} catch (final Exception exc) {
 			throw new RuntimeException(exc);
 		}
 	}
 
	public static final String runCommand(final String command, final String path) {
 		try {
 			final ProcessBuilder pb = new ProcessBuilder(command);
 			pb.redirectErrorStream(true);
 			pb.directory(new File(path));
 			final Process p = pb.start();
 			p.waitFor();
 			BufferedReader is = new BufferedReader(new InputStreamReader(p.getInputStream()));
 			final StringBuilder sb = new StringBuilder();
 			String line;
 			while ((line = is.readLine()) != null) {
			      sb.append(line + "\n");
 			}
 			return sb.toString();
 		} catch (final Exception exc) {
 			throw new RuntimeException(exc);
 		}
 	}
 
 }
