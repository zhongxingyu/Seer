 package gamepatcher;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.UnknownHostException;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.nio.file.AccessDeniedException;
 import java.nio.file.DirectoryStream;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.Calendar;
 import java.util.Scanner;
 import java.util.TimeZone;
 
 import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
 
 public class Downloader extends Patcher {
 	private String filePath;
 	private String datePath;
 	private String fileSite;
 	private String dateSite;
 	private long chunkSize;
 	private float progress;
 	private boolean downloaded = false;
 //	this boolean allows you to deny new updates
 	public Downloader(String fileName, String dateName, String fileSite, String dateSite, long chunkSize){
 		constructor(fileName, dateName, fileSite, dateSite);
 //		sets chunkSize, which is the amount of data downloaded per round
 		this.chunkSize = chunkSize;
 	}
 	public Downloader(String fileName, String dateName, String fileSite, String dateSite){
 		constructor(fileName, dateName, fileSite, dateSite);
 		chunkSize = 1000;
 	}
 	public void constructor(String fileName, String dateName, String fileSite, String dateSite){
 		setUserDir();
 		filePath = System.getProperty("user.dir") + File.separatorChar + fileName;
 		datePath = System.getProperty("user.dir") + File.separatorChar + dateName;
 		this.fileSite = fileSite;
 		this.dateSite = dateSite;
 		scanAndDeleteOldFiles("tempDate", getSuffix(datePath));
 		scanAndDeleteOldFiles("tempFile", getSuffix(filePath));
 	}
 	public boolean isUpdateNecessary(){
 		if(new File(filePath).exists() && new File(datePath).exists()){
 //			the date of update on the website copy will be compared to the local copy to see if an update should happen
 			System.out.println("Checking date of update...");
 			ReadableByteChannel rbc = null;
 			FileOutputStream fos = null;
 			File tempDate = null;
 			try {
 				URL date = new URL(dateSite);
 				URLConnection dateConnection = date.openConnection();
 				long size = dateConnection.getContentLength();
 				rbc = Channels.newChannel(date.openStream());
 				tempDate = File.createTempFile("tempDate", ".txt");
 				tempDate.deleteOnExit();
 				fos = new FileOutputStream(tempDate);
 				fos.getChannel().transferFrom(rbc, 0, size);
 			} catch (MalformedURLException e) {
 				ErrorLogger.logError(e);
 				return false;
 			} catch (FileNotFoundException e) {
 				ErrorLogger.logError(e);
 				return false;
 			} catch (UnknownHostException e) {
 				System.out.println("No internet found");
 				return false;
 			} catch (IOException e) {
 				ErrorLogger.logError(e);
 				return false;
 			} finally {
 				if(rbc != null)
 					try {
 						rbc.close();
 					} catch (IOException e) {
 						ErrorLogger.logError(e);
 						return false;
 					}
 				if(fos != null)
 					try {
 						fos.close();
 					} catch (IOException e) {
 						ErrorLogger.logError(e);
 						return false;
 					}
 			}
 			Calendar timeOfUpdate = fileToCalendar(tempDate.getAbsolutePath());
 			Calendar timeOnFile = fileToCalendar(datePath);
 			if(timeOnFile.compareTo(timeOfUpdate) > 0){
 				System.out.println("No download necessary");
 				return false;
 			}
 			else
 				return true;
 		}
 		else
 			return true;
 	}
 	public void autoUpdate(){
 		if(isUpdateNecessary())
 			downloadFiles();
 	}
 	private Calendar fileToCalendar(String path){
 //		converts a file in the date format used in this program (line 1-year, line 2-month, line 3-day, line 4-hour, line 5-minute, line 6-second)
 		Scanner scanner = null;
 		int[] values = new int[6];
 		try {
 			scanner = new Scanner(new File(path));
 			for(int i = 0; i < values.length; i++)
 				values[i] = Integer.parseInt(scanner.nextLine());
 		} catch (FileNotFoundException e) {
 			ErrorLogger.logError(e);
 			return null;
 		} finally {
 			if(scanner != null)
 				scanner.close();
 		}
 		Calendar calendar = Calendar.getInstance();
 		calendar.set(values[0], values[1] - 1, values[2], values[3], values[4], values[5]);
 		return calendar;
 	}
 	private String getSuffix(String fileName){
 		return fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.'), fileName.length()) : "";
 	}
 	private void scanAndDeleteOldFiles(String name, String suffix){
 //		if the download is aborted, a temporary file will be left behind.  this method deletes all temporary files left behind in the past
 		DirectoryStream<Path> ds = null;
 		try {
 			ds = Files.newDirectoryStream(Paths.get(System.getProperty("java.io.tmpdir")), name + '*' + suffix);
 			for(Path file : ds){
 				if(file.toFile().delete())
 					System.out.println("Old file " + file.toFile().getAbsolutePath() + " deleted successfully.");
 				else
 					System.out.println("Old file " + file.toFile().getAbsolutePath() + " denied being deleted.  That evil file!");
 			}
 		} catch (IOException e) {
 			ErrorLogger.logError(e);
 			return;
 		} finally {
 			try {
 				ds.close();
 			} catch (IOException e) {
 				ErrorLogger.logError(e);
 				return;
 			}
 		}
 	}
 	public void downloadFiles(){
 		System.out.println("Download necessary");
 		System.out.println("Downloading...");
 		ReadableByteChannel rbc = null;
 		FileOutputStream fos = null;
 		try {
 //			creates file in temporary directory to be downloaded to, so that the main file isn't lost if the download is aborted
			File temp = File.createTempFile("tempFile", filePath.substring(filePath.lastIndexOf('.'), filePath.length()));
 //			the file will be deleted upon exit unless the download is aborted mid-way
 			temp.deleteOnExit();
 			URL file = new URL(fileSite);
 			URLConnection urlconnection = file.openConnection();
 			urlconnection.setReadTimeout(5000);
 			long size = urlconnection.getContentLength();
 			rbc = Channels.newChannel(file.openStream());
 			fos = new FileOutputStream(temp);
 			long position = 0;
 			while(position < size){
 				position += fos.getChannel().transferFrom(rbc, position, chunkSize);
 //				sets progress to the nearest tenth of the amount of bytes downloaded out of the total
 				progress = Math.round((float)(100 * (float)position / (float)size) * (float)10) / (float)10;
 				System.out.println(progress + "% done");
 			}
 			File actual = new File(filePath);
 			if(actual.exists())
 				if(!actual.delete())
 					return;
 			Path temporary = temp.toPath();
 			Path desired = actual.toPath();
 			Files.copy(temporary, desired, REPLACE_EXISTING);
 		} catch (AccessDeniedException e) {
 			System.out.println("Access is denied to copy the file to the desired folder.  Please change the path in gamepatchersettings.txt to a folder in which you have write access.");
 			ErrorLogger.logError(e);
 			return;
 		} catch (MalformedURLException e) {
 			ErrorLogger.logError(e);
 			return;
 		} catch (IOException e) {
 			ErrorLogger.logError(e);
 			return;
 		} finally {
 			if(rbc != null)
 				try {
 					rbc.close();
 				} catch (IOException e) {
 					ErrorLogger.logError(e);
 					return;
 				}
 			if(fos != null)
 				try {
 					fos.close();
 				} catch (IOException e) {
 					ErrorLogger.logError(e);
 					return;
 				}
 		}
 		System.out.println("Success");
 		System.out.println("Making datestamp...");
 //		a datestamp of when the update took place will be made
 		File dateFile = new File(datePath);
 		try {
 			dateFile.createNewFile();
 		} catch (IOException e) {
 			ErrorLogger.logError(e);
 			return;
 		}
 		if(dateFile.exists()){
 			Calendar calendar = Calendar.getInstance();
 			TimeZone utc = TimeZone.getTimeZone("UTC");
 //			all time is converted to UTC so that time zones don't screw up updating
 			calendar.setTimeZone(utc);
 			String[] values = new String[6];
 			values[0] = String.valueOf(calendar.get(Calendar.YEAR));
 			values[1] = String.valueOf(calendar.get(Calendar.MONTH) + 1);
 			values[2] = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
 			values[3] = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
 			values[4] = String.valueOf(calendar.get(Calendar.MINUTE));
 			values[5] = String.valueOf(calendar.get(Calendar.SECOND));
 			BufferedWriter bw = null;
 			try {
 				FileWriter dateWriter = new FileWriter(dateFile);
 				bw = new BufferedWriter(dateWriter);
 				for(int i = 0; i < values.length; i++){
 					bw.write(values[i]);
 					bw.newLine();
 				}
 			} catch (IOException e) {
 				ErrorLogger.logError(e);
 				return;
 			} finally {
 				if(bw != null)
 					try {
 						bw.close();
 					} catch (IOException e) {
 						ErrorLogger.logError(e);
 					}
 			}
 			System.out.println("Datestamp success");
 			downloaded = true;
 		}
 	}
 	public float getProgress(){
 		return progress;
 	}
 	public boolean getDownloaded(){
 		return downloaded;
 	}
 }
