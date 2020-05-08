 package net.marioosh.picasa.uploader;
 
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.UUID;
 import javax.imageio.ImageIO;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.PosixParser;
 import org.apache.log4j.Logger;
 import org.apache.sanselan.ImageReadException;
 import org.apache.sanselan.Sanselan;
 import org.apache.sanselan.common.IImageMetadata;
 import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
 import com.google.gdata.client.photos.PicasawebService;
 import com.google.gdata.data.PlainTextConstruct;
 import com.google.gdata.data.media.MediaFileSource;
 import com.google.gdata.data.photos.AlbumEntry;
 import com.google.gdata.data.photos.PhotoEntry;
 import com.google.gdata.data.photos.UserFeed;
 
 public class Main {
 
 	Logger log = Logger.getLogger(Main.class);
 	
 	private static String[] args;
 	private static final String API_PREFIX = "https://picasaweb.google.com/data/feed/api/user/";
 	private static final int WIDTH = 1600;
 	SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
 
 	public Main() {
 		try {
 
 			/**
 			 * parse options
 			 */
 			Options options = new Options();
 			options.addOption("h", false, "help");
 			options.addOption("v", false, "be verbose");
			Option user = new Option("u", true, "user [REQUIRED]");
 			user.setRequired(true);
 			options.addOption(user);
			Option pass = new Option("p", true, "password [REQUIRED]");
 			pass.setRequired(true);
 			options.addOption(pass);
 			options.addOption("t", true, "album title");
 			options.addOption("d", true, "album description");
 			options.addOption("l", false, "list albums");
			CommandLine cmd = null;
			try {
				cmd = new PosixParser().parse(options, args);
			} catch (MissingOptionException e) {
				syntax(options);
				return;
			}
 			if (cmd.hasOption("h")) {
				syntax(options);
 				return;
 			}
 			String title = cmd.hasOption("t") && cmd.getOptionValue("t") != null ? cmd.getOptionValue("t") : sdf.format(new Date());
 			String descr = cmd.hasOption("d") && cmd.getOptionValue("d") != null ? cmd.getOptionValue("d") : null;
 
 			/**
 			 * auth
 			 */
			log.info("LOGGING...");
 			PicasawebService myService = new PicasawebService("exampleCo-exampleApp-1");
 			myService.setUserCredentials(cmd.getOptionValue("u"), cmd.getOptionValue("p"));
 
 			URL feedUrl = new URL(API_PREFIX + "default");
 
 			/**
 			 * list albums
 			 */
 			if (cmd.hasOption("l")) {
 				UserFeed myUserFeed = myService.getFeed(feedUrl, UserFeed.class);
 				for (AlbumEntry myAlbum : myUserFeed.getAlbumEntries()) {
 					log.info(myAlbum.getTitle().getPlainText());
 				}
 			}
 
 			/**
 			 * add photos
 			 */
 			if (cmd.getArgs().length > 0) {
 
 				List<String> files = list(cmd.getArgs(), null);
 
 				if (files.size() > 0) {
 
 					/**
 					 * create album
 					 */
 					AlbumEntry myAlbum = new AlbumEntry();
 					myAlbum.setTitle(new PlainTextConstruct(title));
 					if (descr != null) {
 						myAlbum.setDescription(new PlainTextConstruct(descr));
 					}
 					AlbumEntry insertedEntry = myService.insert(feedUrl, myAlbum);
 
 					/**
 					 * add files to album
 					 */
 					for (String ph : files) {
 						File f = new File(ph);
 						try {
 							IImageMetadata meta = Sanselan.getMetadata(f);
 							if (meta instanceof JpegImageMetadata) {
 								
 								/**
 								 * resize
 								 */
 								log.info(ph + " RESIZING...");
 								final JpegImageMetadata jpegMetadata = (JpegImageMetadata) meta;
 								BufferedImage sourceImage = ImageIO.read(f);
 								
 								boolean horiz = true;
 								if(sourceImage.getHeight() > sourceImage.getWidth()) {
 									horiz = false;
 								}
 								
 								Image thumbnail = sourceImage.getScaledInstance(horiz ? WIDTH : -1, horiz ? -1 : WIDTH, Image.SCALE_SMOOTH);
 								BufferedImage bufferedThumbnail = new BufferedImage(thumbnail.getWidth(null), thumbnail.getHeight(null), BufferedImage.TYPE_INT_RGB);
 								bufferedThumbnail.getGraphics().drawImage(thumbnail, 0, 0, null);
 								File output = File.createTempFile(UUID.randomUUID()+"", "");
 								output.deleteOnExit();
 								ImageIO.write(bufferedThumbnail, "jpeg", output);
 								
 								/*
 								ByteArrayOutputStream out = new ByteArrayOutputStream();
 								ImageIO.write(bufferedThumbnail, "jpeg", out);
 								out.close();
 								*/
 								
 								/**
 								 * upload
 								 */
 								log.info(ph + " UPLOADING...");
 								URL albumPostUrl = new URL(insertedEntry.getId().replace("/entry","/feed/api"));// new URL("https://picasaweb.google.com/data/feed/api/user/default/albumid/"+insertedEntry.getId());
 								PhotoEntry myPhoto = new PhotoEntry();
 								myPhoto.setClient("myClientName");
 								MediaFileSource myMedia = new MediaFileSource(output, "image/jpeg");
 								myPhoto.setMediaSource(myMedia);
 								PhotoEntry returnedPhoto = myService.insert(albumPostUrl, myPhoto);								
 								
 							}
 						} catch (ImageReadException r) {
 							log.info(ph + ": NO IMAGE");
 						}
 
 					}
 				}
 			}
 			
 			log.info("DONE.");
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
	private void syntax(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java -jar picasa-uploader.jar [options] <dir1|file1> <dir2|file2>...", options);
	}

 	private List<String> list(String[] paths, List<String> files) {
 		if (files == null) {
 			files = new ArrayList<String>();
 		}
 
 		for (String path : paths) {
 			if (new File(path).isDirectory()) {
 				File d = new File(path);
 				List<String> f = new ArrayList<String>();
 				for (File f1 : d.listFiles()) {
 					f.add(f1.getAbsolutePath());
 				}
 				list(f.toArray(new String[d.listFiles().length]), files);
 			}
 			if (new File(path).isFile()) {
 				files.add(new File(path).getAbsolutePath());
 			}
 		}
 
 		return files;
 	}
 
 	public static void main(String[] args) {
 		Main.args = args;
 		new Main();
 	}
 }
