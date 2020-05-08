 package nl.bneijt.videosaic;
 
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.SynchronousQueue;
 import java.util.concurrent.TimeUnit;
 
 import javax.imageio.ImageIO;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.apache.log4j.Logger;
 
 import com.mongodb.MongoException;
 
 import nl.bneijt.videosaic.FrameLocation;
 import nl.bneijt.videosaic.Frame;
 
 class App {
 	static final Logger LOG = Logger.getLogger(App.class);
 
 	public static void main(String args[]) throws InterruptedException,
 			MongoException, IOException {
 		final int nTilesPerSide = 10;
 
 		Options options = new Options();
 
 		// add t option
 		options.addOption("v", false, "Return version information and exit");
 		CommandLineParser parser = new PosixParser();
 		CommandLine cmd = null;
 		try {
 			cmd = parser.parse(options, args);
 		} catch (ParseException e) {
 			System.out.println("Unable to parse commandline options");
 			System.out.printf("Usage: %s <command> <input file>", args[0]);
 			System.exit(1);
 		}
 		if (cmd.hasOption("v")) {
 			System.out.println("Version... hmm....");
 			System.exit(0);
 		}
 		if (cmd.getArgList().size() < 1) {
 			System.out.println("No enough arguments given");
 			System.out.printf("Usage: %s <sub|super|store> <input file>",
 					args[0]);
 			System.exit(0);
 		}
 
 		ArrayList<String> fileArguments = new ArrayList<String>();
 		fileArguments.addAll(Arrays.asList(cmd.getArgs())); // cmd.getArgList
 		// sucks because it
 		// does not contain
 		// a type
 		System.out.println(fileArguments);
 		String command = fileArguments.remove(0);
 		ArrayList<File> files = new ArrayList<File>();
 
 		for (String arg : fileArguments) // TODO This is just a map, find a nice
 		// mapping system to handle this
 		{
 			files.add(new File(arg));
 		}
 
 		System.out.printf("Command '%s'\n", command);
 		System.out.printf("Files: %s\n", files.toString());
 		IdentStorage identStorage = new MongoDBIdentStorage();
		IdentProducer identifier = new MeanIdentProducer();
 		if (command.equals("super")) {
 			File targetFile = files.get(0);
 			BlockingQueue<Frame> queue = new SynchronousQueue<Frame>();
 			FrameGenerator fg = new FrameGenerator(queue, targetFile);
 			fg.run();
 			Frame f = queue.poll(10, TimeUnit.SECONDS);
 
 			while (f != null) {
 				System.out.println(String.format("Frame number %d: %dx%d", f
 						.frameNumber(), f.getWidth(), f.getHeight()));
 				FrameLocation location = new FrameLocation(targetFile
 						.getAbsolutePath(), f.frameNumber());
 				// Create idents for each of the pieces of the puzzel
 				int w = f.getWidth() / nTilesPerSide;
 				int h = f.getHeight() / nTilesPerSide;
 				ArrayList<String> idents = new ArrayList<String>();
 				for (int xOffset = 0; xOffset < f.getWidth(); xOffset += w)
 					for (int yOffset = 0; yOffset < f.getHeight(); yOffset += h) {
 						String ident = identifier.identify(f.getSubimage(
 								xOffset, yOffset, w, h));
 						idents.add(ident);
 					}
 				identStorage.storeSuperIdent(idents, location);
 				f = queue.poll(2, TimeUnit.SECONDS); // TODO UGLY CODE!!
 			}
 			// Load frame idents and see if they are in the database
 
 		} else if (command.equals("sub")) {
 			LOG.debug("Entering command: " + command);
 			DiskFrameStorage frameStorage = new DiskFrameStorage();
 			// Load frame idents as targets into the database (documents)
 			// Create index of output movie
 			File targetFile = files.get(0);
 			BlockingQueue<Frame> queue = new SynchronousQueue<Frame>();
 			FrameGenerator fg = new FrameGenerator(queue, targetFile);
 			fg.run();
 			Frame f = queue.poll(10, TimeUnit.SECONDS);
 			while (f != null) {
 				FrameLocation location = new FrameLocation(targetFile
 						.getAbsolutePath(), f.frameNumber());
 				String ident = identifier.identify(f);
 
 				System.out.println(String.format("Frame number %d ident %s", f
 						.frameNumber(), ident));
 
 				boolean stored = identStorage.storeSubIdent(ident, location);
 				// Store the thumbnail on disk, frame store is born!
 				if (stored)
 					frameStorage.storeFrame(f.getScaledInstance(
 							320 / nTilesPerSide, 240 / nTilesPerSide,
 							Image.SCALE_SMOOTH), location);
 
 				f = queue.poll(2, TimeUnit.SECONDS);// TODO UGLY CODE!
 			}
 			// Load frame idents and see if they are in the database
 		} else if (command.equals("info")) {
 			// Show general storage information
 			System.out.println(identStorage.information());
 		} else if (command.equals("clear")) {
 			identStorage.clear();
 		} else if (command.equals("collapse")) {
 			// Enumerate trough super frames and choose an output path from the
 			// database
 			// Each super frame should have an collection of sub frames ready in
 			// the document
 			// Target video is a given file
 			DiskFrameStorage frameStorage = new DiskFrameStorage();
 			File targetFile = files.get(0);
 			for (int frameNumber = 0;; ++frameNumber) {
 				FrameLocation location = new FrameLocation(targetFile
 						.getAbsolutePath(), frameNumber);
 				// Find a frame and all it's sub-frames
 				List<FrameLocation> subframes = identStorage
 						.loadSubFrames(location);
 				LOG.debug(String
 						.format("Found %d sub frames", subframes.size()));
 				if (subframes.size() == 0)
 					break;
 				// Collapse all the sub-frames of the current frame into it's
 				// parts.
 				// Use a default of black if the frame is missing
 				BufferedImage outputFrame = new BufferedImage(320, 240,
 						BufferedImage.TYPE_INT_RGB);
 				Graphics2D outputGraphics = outputFrame.createGraphics();
 				for (int i = 0; i < subframes.size(); ++i) {
 					FrameLocation fl = subframes.get(i);
 					LOG.debug(String.format("Loading subimage %d from %s", i,
 							fl.toString()));
 					// Load subframe as buffered image
 					BufferedImage subframe = frameStorage.loadFrame(fl);
 					// Scale to fit
 					int tileWidth = 320 / nTilesPerSide;
 					int tileHeight = 240 / nTilesPerSide;
 					Image scaledSubframe = subframe.getScaledInstance(
 							tileWidth, tileHeight, Image.SCALE_SMOOTH);
 					int x = tileWidth * (i % nTilesPerSide);
 					int y = tileHeight * (i / nTilesPerSide);
 					// Store the subframe in the bufferedImage
 					assert (x < outputFrame.getWidth());
 					assert (y < outputFrame.getHeight());
 					// Paste the subimage
 					LOG
 							.debug(String.format("Putting sub frame at %d,%d",
 									x, y));
 					outputGraphics.drawImage(scaledSubframe, x, y, null);
 
 				}
 				ImageIO.write(outputFrame, "png", new File(String.format(
 						"/tmp/image_%d.png", frameNumber)));
 			}
 
 		} else if (command.equals("dump")) {
 			// /Output the frame idents to stdout
 			LOG.debug("Entering command: " + command);
 			// Load frame idents as targets into the database (documents)
 			// Create index of output movie
 			BlockingQueue<Frame> queue = new SynchronousQueue<Frame>();
 			for (File targetFile : files) {
 				FrameGenerator fg = new FrameGenerator(queue, targetFile);
 				fg.run();
 				Frame f = queue.poll(10, TimeUnit.SECONDS);
 				while (f != null) {
 					System.out.println(identifier.identify(f));
 					f = queue.poll(2, TimeUnit.SECONDS);// TODO UGLY CODE!
 				}
 			}
 		} else {
 			System.out.println("command - " + command);
 		}
 
 	}
 
 }
