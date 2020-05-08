 package utils;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStreamReader;
 
 public final class FileSystemUtils {
 
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

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
 
	public static final String runCommand(final String path, final String... command) {
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
			      sb.append(line).append(FileSystemUtils.LINE_SEPARATOR);
 			}
 			return sb.toString();
 		} catch (final Exception exc) {
 			throw new RuntimeException(exc);
 		}
 	}
 
 }
