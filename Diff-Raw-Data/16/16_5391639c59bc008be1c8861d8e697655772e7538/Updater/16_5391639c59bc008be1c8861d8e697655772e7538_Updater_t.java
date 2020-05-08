 package gamepatcher;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.Calendar;
 import java.util.TimeZone;
 
 import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
 
 public class Updater {
 	private String filePath;
 	private String uploadPath;
 	private String fileName;
 	private String dateName;
 	private boolean canMake = true;
 	public Updater(String fileName, String dateName, String uploadPath){
 		filePath = System.getProperty("user.dir") + File.separatorChar + fileName;
 		this.uploadPath = uploadPath;
 		if(uploadPath.charAt(uploadPath.length() - 1) != File.separatorChar){
 			System.out.println("uploadPath must end in " + File.separatorChar);
 			canMake = false;
 		}
 		this.fileName = fileName;
 		this.dateName = dateName;
 	}
 	public void update(){
 		if(canMake){
 			System.out.println("Deleting obsolete files...");
 			File uploadFile = new File(uploadPath + fileName);
 			File uploadDate = new File(uploadPath + dateName);
 			uploadFile.delete();
 			uploadDate.delete();
 			System.out.println("Updating files...");
 			Path originalFile = Paths.get(filePath);
 			Path newFile = Paths.get(uploadPath + fileName);
 			try {
 				Files.copy(originalFile, newFile, REPLACE_EXISTING);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			System.out.println("File updated.  Timestamping...");
 			File file = new File(uploadPath + dateName);
 			try {
 				file.createNewFile();
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 			if(file.exists()){
 				Calendar calendar = Calendar.getInstance();
 				String[] values = new String[6];
				TimeZone utc = TimeZone.getTimeZone("UTC");
				calendar.setTimeZone(utc);
 				values[0] = String.valueOf(calendar.get(Calendar.YEAR));
 				values[1] = String.valueOf(calendar.get(Calendar.MONTH) + 1);
 				values[2] = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
 				values[3] = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
 				values[4] = String.valueOf(calendar.get(Calendar.MINUTE));
 				values[5] = String.valueOf(calendar.get(Calendar.SECOND));
 				try {
 					FileWriter dateWriter = new FileWriter(file);
 					BufferedWriter bw = new BufferedWriter(dateWriter);
 					for(int i = 0; i < values.length; i++){
 						bw.write(values[i]);
 						bw.newLine();
 					}
 					bw.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				System.out.println("Timestamp complete");
 			}
 			System.out.println("Update complete");
 		}
 	}
 }
