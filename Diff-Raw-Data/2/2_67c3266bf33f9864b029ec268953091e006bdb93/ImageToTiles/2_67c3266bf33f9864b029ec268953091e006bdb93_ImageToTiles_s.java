 import java.io.File;
 
 import javax.swing.JProgressBar;
 
 import net.niconomicon.tile.source.app.Ref;
 import net.niconomicon.tile.source.app.tiling.SQliteTileCreatorMultithreaded;
 
 /**
  * 
  */
 
 /**
  * @author niko
  * 
  */
 public class ImageToTiles {
 
 	public static void printOptions() {
 		System.out.println("usage : tests [nThreads] [nIters] [srcimage] [destFile]");
 		System.out.println("[nThread] will be the number of threads used to serialize the tiles. ");
 		System.out.println("          The main thread will open,resize the image and write the tiles to the storage. If <= 1 will be replaced by 1.");
 		System.out.println("[nIters] will be the number of times the image will be open, tiled and serialized.");
 		System.out.println("          if <=1 will be replaced by 1. If >1 the iteration will be added to the destination file name.");
 
 		System.out.println("[srcImage] the image you want to tile.");
 		System.out.println("[srcImage] where the tiles image will be stored.");
 		System.out.println("example usage :");
 		System.out.println("java -Xmx1024m -jar bla.jar Tile 1 1 ~niko/Big.jpg /tmp/test.tdb");
 
 		System.out.println("java -Xmx1024m -jar bla.jar Tile 24 1 ~niko/Big.jpg /tmp/test.tdb");
 
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws Exception {
 		if (args.length < 4) {
 			printOptions();
 		}
 		int nThreads = 1;
 		int nIters = 1;
 		File fopen = null;
 		File fWrite = null;
 		try {
 			nThreads = Integer.parseInt(args[0]);
 			nIters = Integer.parseInt(args[1]);
 		} catch (Exception ex) {
 			printOptions();
 			ex.printStackTrace();
 			System.exit(0);
 		}
 		nIters = Math.max(nIters, 1);
 		nThreads = Math.max(nThreads, 1);
 		try {
 			fopen = new File(args[2]);
 			if (!fopen.exists()) {
 				System.out.println("Could not find this file : [" + args[2] + "]");
 				printOptions();
 				System.exit(0);
 			}
 			if (!fopen.canRead()) {
 				System.out.println("The program doesn't have the rights to read this file : [" + args[2] + "]");
 				printOptions();
 				System.exit(0);
 			}
 		} catch (Exception ex) {
 			printOptions();
 			ex.printStackTrace();
 			System.exit(0);
 		}
 
 		try {
 			fWrite = new File(args[3]);
 			if (fWrite.exists()) {
 				System.out.println("This file : [" + args[3] + "] already exists. Please remove it before running this program.");
 				printOptions();
 				System.exit(0);
 			}
 			if (!fWrite.createNewFile()) {
 				System.out.println("The program could not create this file: [" + args[3] + "] Please ensure the place is writable.");
 				printOptions();
 				System.exit(0);
 			}
 		} catch (Exception ex) {
 			printOptions();
 			ex.printStackTrace();
 			System.exit(0);
 		}
 
 		// default behavior:
 		// manbus : 64 ~> 40 seconds on second iteration.
 		// globcover : 20 ~> 14 seconds on second iteration.
 		// String destDir = "/Users/niko/tileSources/bench/";
 		// String src = "/Users/niko/tileSources/";
 		String title = fopen.getName().substring(0, fopen.getName().lastIndexOf("."));
 
 		SQliteTileCreatorMultithreaded creator = new SQliteTileCreatorMultithreaded();
 		creator.title = title;
 		creator.loadLib();
 		Thread.sleep(3000);
 		long start, stop;
 		for (int i = 0; i < nIters; i++) {
 			System.out.println("Processing " + title);
 			start = System.nanoTime();
 
			creator.calculateTiles(fWrite.getAbsolutePath(), fopen.getAbsolutePath(), 192, "png", null, nThreads);
 			creator.finalizeFile();
 			stop = System.nanoTime();
 			System.out.println("total_time: " + ((double) (stop - start) / 1000000) + " ms");
 		}
 	}
 }
