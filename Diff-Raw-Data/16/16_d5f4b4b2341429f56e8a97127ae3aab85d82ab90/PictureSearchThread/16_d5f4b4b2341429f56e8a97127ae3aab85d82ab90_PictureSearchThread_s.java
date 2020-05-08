 package filter;
 
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.URL;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 
 import application.FtpManager;
 
 public class PictureSearchThread extends Thread {
 
 	private BufferedImage img;
 	private int precision = 5, category = 0;
 	private PictureSearchContent content;
 
 	public PictureSearchThread(BufferedImage img, 
 			int category, int precision, PictureSearchContent content) {
 		
 		this.img = img;
 		this.precision = precision;
 		this.category = category;
 		this.content = content;
 	}
 
 	public void run() {
 		content.setLoadingBar("saving");
 		content.setLoadingBarState(true);		
 		File outputfile = new File("ImageSearch/SearchImage.jpg");
 		try {
 			ImageIO.write(img, "jpg", outputfile);
 			content.setLoadingBar("uploading");
 			String image_url = FtpManager.uploadFileToFtp(
 					"ftp.asight.bplaced.net", "asight", "DummyPW123",
 					outputfile.getCanonicalPath(), outputfile.getName(), "");
 			content.setLoadingBar("searching");
 			String charset = "UTF-8";
 			String category = Integer.toString(this.category);
 			String precision = Integer.toString(this.precision);
 
 			URL url = new URL(
 					"http://www.revimg.net/jsapi/index_api.php?category="
 							+ category + "&image_url="
 							+ application.Utility.encodeURIComponent(image_url)
 							+ "&precision=" + precision + "&callback=?");
 			ArrayList<String> rslt = generateResults(new InputStreamReader(
 					url.openStream(), charset));
 
 			content.displayResults(rslt.get(0) != "Error" ? rslt.get(0)
 					: "nothing found", rslt.size() > 1 ? rslt.get(1) : "", rslt
 					.size() > 2 ? rslt.get(2) : "");
 
 			content.setLoadingBarState(false);
 			FtpManager.deleteFileFromFtp("ftp.asight.bplaced.net", "asight",
 					"DummyPW123", outputfile.getName(), "");
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public static ArrayList<String> generateResults(Reader results) {
 		ArrayList<String> resultList = new ArrayList<String>();
 		StringBuffer rsltBuffer = null;
 		try {
 
 			BufferedReader bRslt = new BufferedReader(results);
 			String resultString = "";
 			boolean loop = true;
 			String line = null;
 			while (loop) {
 				try {
 					line = bRslt.readLine();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				if (line != null) {
 					resultString = resultString + line;
 				} else {
 					loop = false;
 				}
 			}
 
 			rsltBuffer = new StringBuffer(resultString);
 			int p1 = 0, p2 = 0, t1 = 0, t2 = 0;
 			p1 = rsltBuffer.indexOf("[") + 1;
 			loop = true;
 			String tmp, tmpPercent;
 			while (loop) {
 				p2 = rsltBuffer.indexOf("]", p1);
 				if (p2 >= 0) {
 					tmp = rsltBuffer.substring(p1 + 1, p2 - 1);
 					t1 = tmp.indexOf(",", tmp.indexOf(",") + 1);
 					t2 = tmp.indexOf(",", t1 + 1);
 					tmpPercent = tmp.substring(t1 + 1, t2 - 1) + "%";
 					tmp = tmpPercent + " - " + tmp.substring(t2 + 2);
 					resultList.add(tmp);
 					p1 = p2 + 2;
 				} else {
 					loop = false;
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(rsltBuffer);
 			resultList.clear();
 			resultList.add("Error");
 		}
 		return resultList;
 	}
 }
