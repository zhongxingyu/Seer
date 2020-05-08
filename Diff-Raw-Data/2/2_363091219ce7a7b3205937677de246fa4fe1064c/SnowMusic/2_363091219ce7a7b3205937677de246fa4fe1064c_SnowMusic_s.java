 package uk.ac.soton.ecs.wais.fest13.demos;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.sound.midi.MidiUnavailableException;
 import javax.swing.JFrame;
 
 import org.openimaj.image.DisplayUtilities;
 import org.openimaj.image.MBFImage;
 import org.openimaj.image.colour.ColourSpace;
 import org.openimaj.image.colour.RGBColour;
 import org.openimaj.image.typography.hershey.HersheyFont;
 import org.openimaj.math.geometry.shape.Rectangle;
 import org.openimaj.util.data.Context;
 import org.openimaj.util.function.Operation;
 import org.openimaj.util.stream.Stream;
 
 import uk.ac.soton.ecs.jsh2.mediaeval13.placing.evaluation.GeoLocation;
 import uk.ac.soton.ecs.sound.vis.FlickrTagFilter;
 import uk.ac.soton.ecs.sound.vis.FlickrTimePostedWindow;
 import uk.ac.soton.ecs.sound.vis.FlickrTimePredicate;
 import uk.ac.soton.ecs.wais.fest13.FlickrCSVStream;
 import uk.ac.soton.ecs.wais.fest13.FlickrCSVStream.FlickrImageSoundOperation;
 import uk.ac.soton.ecs.wais.fest13.FlickrImageDrawOperation;
 import uk.ac.soton.ecs.wais.fest13.FullScreenDemo;
 import uk.ac.soton.ecs.wais.fest13.GetAll;
 import uk.ac.soton.ecs.wais.fest13.PassThrough;
 import uk.ac.soton.ecs.wais.fest13.SocialComment;
 import uk.ac.soton.ecs.wais.fest13.StaticWorldMap;
 import uk.ac.soton.ecs.wais.fest13.UserInformation;
 import uk.ac.soton.ecs.wais.fest13.sound.SoundTranslator;
 import uk.ac.soton.ecs.wais.fest13.sound.midi.MIDISoundTranslator;
 
 public class SnowMusic {
 	public static void main(String[] args) throws FileNotFoundException, MidiUnavailableException {
 		final MBFImage img = new MBFImage(1080, 580, ColourSpace.RGB);
 		final JFrame wind = DisplayUtilities.displaySimple(img);
 //		final MBFImage img = FullScreenDemo.createImage();
 //		final JFrame wind = FullScreenDemo.display(img, "Snow Music");
 
 		final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
 
 		final String data = "/Users/ss/Development/java/WAISFest13/data-taken.csv";
 		// final String data = "/home/dd/data-takensort.csv";
 		// final String data = "/Users/ss/Development/java/WAISFest13/data-taken.csv";
 //		final String data = "/Users/jamie/Data/data-taken.csv";
 		final FlickrImageHeatmapOperation heatmapOp = new FlickrImageHeatmapOperation(img);
 		final FlickrImageDrawOperation imagePointOp = new FlickrImageDrawOperation(img, RGBColour.YELLOW);
 		final List<SocialComment> comments = new ArrayList<SocialComment>();
 		final SoundTranslator trans = new MIDISoundTranslator();
 		
 		final MBFImage worldmap = StaticWorldMap.getMap(wind.getWidth(), wind.getHeight(),
 				new Float[]{1f, 1f, 1f, 0f},
 				new Float[]{1f, 1f, 1f, 0f},
 				new Float[]{1f, 1f, 1f, 0.2f});
 		
 		new FlickrCSVStream(new File(data))
 				.filter(new FlickrTimePredicate())
 				.transform(new FlickrTimePostedWindow(24 * 60 * 60 * 1000L))
 				.forEach(new Operation<Context>() {
 					@SuppressWarnings("unchecked")
 					@Override
 					public void perform(Context object) {
 						img.multiplyInplace(0.95f);
 						img.drawImage(worldmap, 0, 0);
 						comments.clear();
 
 						((Stream<Context>) object.get("window"))
 								.filter(new FlickrTagFilter("snow"))
 								.filter(new PassThrough<Context>(heatmapOp))
 								.filter(new PassThrough<Context>(imagePointOp))
 								.filter(new PassThrough<Context>(new FlickrImageSoundOperation(comments)))
 								.forEach(new GetAll<Context>());
 
 						UserInformation userInformation = new UserInformation();
 						userInformation = new UserInformation();
 						userInformation.location = new GeoLocation(51.5, 0);
 						trans.translate(comments, userInformation);
 
 //						heatmapOp.windowDrawn(object);
 						imagePointOp.windowDrawn(object);
								HersheyFont.ROMAN_SIMPLEX, 18, RGBColour.WHITE);
						
 						
 						DisplayUtilities.display(img, wind);
 //						FullScreenDemo.update(wind, img);
 
 						try {
 							Thread.sleep(1000L / 30L);
 						} catch (final InterruptedException e) {
 							e.printStackTrace();
 						}
 					}
 				});
 	}
 }
