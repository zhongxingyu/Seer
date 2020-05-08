 package gamepatcher;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.util.Calendar;
 import java.util.Scanner;
 import java.util.TimeZone;
 
 public class Downloader {
 	private String filePath;
 	private String datePath;
 	private String fileSite;
 	private String dateSite;
 	private boolean hasInternet;
 	public Downloader(String fileName, String dateName, String fileSite, String dateSite){
 		filePath = System.getProperty("user.dir") + File.separatorChar + fileName;
 		datePath = System.getProperty("user.dir") + File.separatorChar + dateName;
 		this.fileSite = fileSite;
 		this.dateSite = dateSite;
 		System.out.println("Checking if internet is available...");
 		try {
 			InetAddress gitHub = InetAddress.getByName(new URL("https://github.com/").getHost());
 			if(gitHub.isReachable(5000)){
 				hasInternet = true;
 				System.out.println("Internet connection available");
 			}
 			else
 				hasInternet = false;	
 		} catch (UnknownHostException e) {
 			hasInternet = false;
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	public void checkForUpdate(){
 		if(hasInternet){
 			if(new File(filePath).exists()){
 				if(new File(datePath).exists()){
 					System.out.println("Checking date of update...");
 					try {
 						URL date = new URL(dateSite);
 						ReadableByteChannel rbc = Channels.newChannel(date.openStream());
 						FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir") + File.separatorChar + "tempDate.txt");
 						fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
 						rbc.close();
 						fos.close();
 					} catch (MalformedURLException e) {
 						e.printStackTrace();
 					} catch (FileNotFoundException e) {
 						e.printStackTrace();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 					Calendar timeOfUpdate = fileToCalendar(System.getProperty("user.dir") + File.separatorChar + "tempDate.txt");
 					Calendar timeOnFile = fileToCalendar(datePath);
 					new File(System.getProperty("user.dir") + File.separatorChar + "tempDate.txt").delete();
 					if(timeOnFile.compareTo(timeOfUpdate) > 0)
 						System.out.println("No download necessary");
 					else
 						downloadFiles();
 				}
 				else
 					downloadFiles();
 			}
 			else
 				downloadFiles();
 		}
 		else
 			System.out.println("No internet detected, no update will be downloaded");
 	}
 	private void downloadFiles(){
 		System.out.println("Download necessary");
 		System.out.println("Downloading...");
 		try {
 			URL file = new URL(fileSite);
 			ReadableByteChannel rbc = Channels.newChannel(file.openStream());
 			FileOutputStream fos = new FileOutputStream(filePath);
 			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
 			rbc.close();
 			fos.close();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		System.out.println("Success");
 		System.out.println("Making datestamp...");
 		File dateFile = new File(datePath);
 		try {
 			dateFile.createNewFile();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		if(dateFile.exists()){
 			Calendar calendar = Calendar.getInstance();
			TimeZone gmt = TimeZone.getTimeZone("GMT");
			calendar.setTimeZone(gmt);
 			String[] values = new String[6];
 			values[0] = String.valueOf(calendar.get(Calendar.YEAR));
 			values[1] = String.valueOf(calendar.get(Calendar.MONTH) + 1);
 			values[2] = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
			values[3] = String.valueOf(calendar.get(Calendar.HOUR));
 			values[4] = String.valueOf(calendar.get(Calendar.MINUTE));
 			values[5] = String.valueOf(calendar.get(Calendar.SECOND));
 			try {
 				FileWriter dateWriter = new FileWriter(dateFile);
 				BufferedWriter bw = new BufferedWriter(dateWriter);
 				for(int i = 0; i < values.length; i++){
 					bw.write(values[i]);
 					bw.newLine();
 				}
 				bw.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			System.out.println("Datestamp success");
 		}
 	}
 	private Calendar fileToCalendar(String path){
 		Scanner scanner = null;
 		try {
 			scanner = new Scanner(new File(path));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		int[] values = new int[6];
 		for(int i = 0; i < values.length; i++)
 			values[i] = Integer.parseInt(scanner.nextLine());
 		scanner.close();
 		Calendar calendar = Calendar.getInstance();
 		calendar.set(values[0], values[1], values[2], values[3], values[4], values[5]);
 		return calendar;
 	}
 }
