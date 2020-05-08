 package fm.flickr.stat;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.log4j.Logger;
 
 import fm.flickr.api.wrapper.service.FlickrService;
 import fm.flickr.api.wrapper.service.param.PhotoItem;
 import fm.flickr.api.wrapper.service.param.PhotoItemsSet;
 import fm.flickr.stat.perform.ActivityStat;
 import fm.flickr.stat.perform.DailyUploadsStat;
 import fm.flickr.stat.perform.GroupStat;
 import fm.flickr.stat.perform.TagStat;
 import fm.flickr.stat.perform.TimeStat;
 import fm.flickr.stat.perform.UserStat;
 import fm.util.Config;
 
 /**
  * This is the main entry point for collecting data. It gets photos from Interestingness at the 
  * specified dates. Then for each photo it optionally collects additinal stats about groups, tags,
  * users, times etc., using classes from package fm.flickr.stat.perform.
  * @author fmichel
 */
 public class CollectData
 {
 	private static Logger logger = Logger.getLogger(CollectData.class.getName());
 
 	private static Configuration config = Config.getConfiguration();
 
 	/** Wrapper for Flickr services */
 	private static FlickrService service = new FlickrService();
 
 	public static void main(String[] args) {
 
 		logger.debug("begin");
 		try {
 			// Convert start and stop dates into GregorianCalendars
 			String startDate = config.getString("fm.flickr.stat.startdate");
 			String[] tokensStart = startDate.split("-");
 			GregorianCalendar calStart = new GregorianCalendar(Integer.valueOf(tokensStart[0]), Integer.valueOf(tokensStart[1]) - 1, Integer.valueOf(tokensStart[2]));
 
 			String stopDate = config.getString("fm.flickr.stat.enddate");
 			String[] tokensEnd = stopDate.split("-");
 			GregorianCalendar calEnd = new GregorianCalendar(Integer.valueOf(tokensEnd[0]), Integer.valueOf(tokensEnd[1]) - 1, Integer.valueOf(tokensEnd[2]));
 
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 
 			//--- Collect data from Interstingness and store it into one file per day and per type of statistics
 			while (calStart.before(calEnd)) {
 
 				// Format date to process as yyyy-mm-dd
 				String date = sdf.format(calStart.getTime());
 				logger.info("Starting collecting data on " + date);
 
 				// Collect data on photos from Interstingness on that date (Flickr will report max 500 every day)
 				collectDataFromInterestingness(date);
 
 				// Collect number of daily uploads to Flickr
 				if (config.getString("fm.flickr.stat.action.uploads").equals("on"))
 					new DailyUploadsStat().collecDailyUploads(date);
 
 				// Increase the date by n days, and proceed with that next date
 				calStart.add(GregorianCalendar.DAY_OF_MONTH, config.getInt("fm.flickr.stat.step_between_measure"));
 
 				// Sleep between each photo... just not to be overloading (may not be necessary...)
 				try {
 					//logger.info("Sleep for 5s");
 					Thread.sleep(10);
 				} catch (InterruptedException e) {
 					logger.warn("Unepected interruption: " + e.toString());
 					e.printStackTrace();
 				}
 			}
 
 			logger.info("end");
 
 		} catch (Exception e) {
 			logger.error("Unexpected error. Exception: " + e.toString());
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * <p>Retrieve the list of photos from Interestingness and run the statistics.
 	 * On the given date, a maximum of 'fm.flickr.stat.maxphotos' photos from Interestingness will be processed.</p>
 	 * <p>The results are saved into files by the classes implementing statistics.</p>
 	 * 
 	 * @param date date of photos from Interestingness, given in format "YYY-MM-DD"
 	 */
 	private static void collectDataFromInterestingness(String date) throws IOException {
 		PhotoItemsSet photos = null;
 
		if (config.getString("fm.flickr.stat.action.group").equals("on") && config.getString("fm.flickr.stat.action.tag").equals("on") && config.getString("fm.flickr.stat.action.time").equals("on") && config.getString("fm.flickr.stat.action.user").equals("on") && config.getString("fm.flickr.stat.action.activity").equals("on")) {
 
 			String photoList = System.getProperty("fm.flickr.stat.photoslist");
 			if (photoList != null) {
 				photos = getInterestingnessPhotosFromFile(photoList);
 			} else {
 				// Get photos from Interstingness on the given date (Flickr will report maximum 500 every day)
 				photos = service.getInterestingnessPhotos(date, config.getInt("fm.flickr.stat.maxphotos"), 1);
 			}
 
 			if (photos != null) {
 				logger.info("######## " + date + ": " + photos.size() + " photos from Interestingness to be processed...");
 
 				if (config.getString("fm.flickr.stat.action.group").equals("on"))
 					new GroupStat().collecAdditionalData(date, photos);
 
 				if (config.getString("fm.flickr.stat.action.tag").equals("on"))
 					new TagStat().collecAdditionalData(date, photos);
 
 				if (config.getString("fm.flickr.stat.action.time").equals("on"))
 					new TimeStat().collecAdditionalData(date, photos);
 
 				if (config.getString("fm.flickr.stat.action.user").equals("on"))
 					new UserStat().collecAdditionalData(date, photos);
 
 				if (config.getString("fm.flickr.stat.action.activity").equals("on"))
 					new ActivityStat().collecAdditionalData(date, photos);
 			}
 		}
 	}
 
 	/**
 	 * This specific function is a way of running the statistics acquisition process not on photos 
 	 * retrieved from Interestingness, but from a simple list of photo identifiers given in a file.
 	 * 
 	 * @param fileName the file were the photos identifiers are listed in the interestingness rank order
 	 * @return the set of photos, or null if an error occurs (IO error or file not found)
 	 */
 	private static PhotoItemsSet getInterestingnessPhotosFromFile(String fileName) {
 
 		ArrayList<PhotoItem> photosList = new ArrayList<PhotoItem>();
 		File file = null;
 		int rank = 1;
 
 		try {
 			file = new File(fileName);
 			if (!file.exists()) {
 				String errMsg = "No file: " + file.getAbsolutePath();
 				logger.warn(errMsg);
 				return null;
 			}
 
 			FileInputStream fis = new FileInputStream(file);
 			BufferedReader buffer = new BufferedReader(new InputStreamReader(fis));
 			logger.info("Loading photo ids from file " + file.getAbsolutePath());
 
 			String photoId = buffer.readLine();
 			while (photoId != null) {
 				// Create a simple PhotItem with only the photo id and the interestingness rank
 				PhotoItem item = new PhotoItem();
 				item.setPhotoId(photoId);
 				item.setInterestingnessRank(rank++);
 				photosList.add(item);
 				photoId = buffer.readLine();
 			}
 			fis.close();
 			return new PhotoItemsSet(photosList, 1, photosList.size());
 
 		} catch (IOException e) {
 			String errMsg = "Error when reading file " + file.getName() + ". Exception: " + e.toString();
 			logger.warn(errMsg);
 			return null;
 		}
 	}
 }
