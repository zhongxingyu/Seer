 package gutenberg.workers;
 
 import gutenberg.blocs.EntryType;
 import gutenberg.blocs.ManifestType;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStreamReader;
 import java.nio.file.Files;
 import java.nio.file.Path;
 
 public class Locker {
 
 	public Locker(Config config) throws Exception {
 		LOCKER = config.getPath(Resource.locker);
 	}
 
 	public File[] fetch(String[] scanIds) {
 		Path scan = null, thumb = null;
 		File[] scans = new File[scanIds.length * 2];
 		Path locker = new File(LOCKER).toPath();
 		for (int i = 0; i < scanIds.length; i++) {
 			scan = locker.resolve(scanIds[i]);
 			thumb = locker.resolve("thumb-" + scanIds[i]);
 			scans[i] = scan.toFile();
 			scans[i + 1] = thumb.toFile();
 		}
 		return scans;
 	}
 
 	public ManifestType save(File[] scans) throws Exception {
 		
 		ManifestType manifest = new ManifestType();
 		manifest.setRoot(LOCKER);
 		
 		Path locker = new File(LOCKER).toPath();		
 		String scanId = null; 
		boolean flip = false; 
 		String[] tokens = null;
 		
 		for (File scan:scans) {
 			
 			tokens = scan.getName().split("\\.");
 			scanId = tokens[0];
			flip = tokens[1].equals("1") ? true:false;
 			if (!Files.exists(locker.resolve(scanId))) {						
				if (convert(scan.toPath(), locker.resolve(scanId), SCAN_SIZE, flip) != 0 ||
					convert(scan.toPath(), locker.resolve("thumb-" + scanId), THUMB_SIZE, flip) != 0) {
 					throw new Exception("Error running convert utility on "
 							+ scan.getName());
 				}
 
 				EntryType image = new EntryType();
 				image.setId(scanId);
 				manifest.addImage(image);
 			}
 			
 			if (!scan.delete()) {
 				throw new Exception("SaveScan error: could not delete" + scan.getName());
 			}
 			
 		}
 		return manifest;
 	}
 
 	private String LOCKER;
 	
	private int convert(Path src, Path target, String size, boolean flip)
 			throws Exception {
 		ProcessBuilder pb = new ProcessBuilder();
		if (flip) {
			pb.command("convert", src.toString(), "-flip", "-resize", size, target.toString());
 		} else {
 			pb.command("convert", src.toString(), "-resize", size, target.toString());
 		}
 		
 		pb.directory(new File(LOCKER));
 		pb.redirectErrorStream(true);
 
 		Process process = pb.start();
 		BufferedReader messages = new BufferedReader(new InputStreamReader(
 				process.getInputStream()));
 		String line = null;
 
 		while ((line = messages.readLine()) != null) {
 			System.out.println(line);
 		}
 
 		return process.waitFor();
 	}
 
 	private final String SCAN_SIZE = "600x800";
 	private final String THUMB_SIZE = "120x120";
 }
