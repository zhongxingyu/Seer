 package ee.ttu.kinect.util;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 
 import ee.ttu.kinect.model.Body;
 
 public class FileWorker {
 
 	private final Logger logger = Logger.getLogger(this.getClass().getName());
 
 	private BufferedWriter fileWriter;
 
 	private BufferedReader fileReader;
 
 	private List<String> textCache = new ArrayList<String>();
 
 	public void readFile(File file) throws FileNotFoundException {
 		fileReader = new BufferedReader(new FileReader(file));
 		emptyCache();
 		try {
 			String input = fileReader.readLine();
 			logger.info("opening... " + input);
 			while ((input = fileReader.readLine()) != null) {
 				addCachedText(input);
 			}
 			fileReader.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public synchronized void dumpFile() throws IOException {
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd_HH-mm-ss");
 		File file = new File(System.getProperty("user.dir") + "/"
 				+ sdf.format(new Date()) + ".csv");
 		fileWriter = new BufferedWriter(new FileWriter(file, true));
 
 		fileWriter.write("experimentId");
 		fileWriter.newLine();
 		sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
 		fileWriter.write(sdf.format(new Date()));
 		fileWriter.newLine();
 		fileWriter.write(Body.getHeader());
 		fileWriter.newLine();
 
 		fileWriter.write(getCachedText());
 
 		emptyCache();
 		fileWriter.close();
 	}
 
	public void addToSave(String text) throws IOException {
 		addCachedText(text);
 	}
 
	public String readNextLine() throws IOException {
 		String str = null;
 		try {
 			str = textCache.remove(0);
 		} catch(Exception e) {}
 		
 		return str;
 	}
 
	private synchronized String getCachedText() {
 		StringBuffer cachedText = new StringBuffer();
 		for (String cached : textCache) {
 			cachedText = cachedText.append(cached).append("\n");
 		}
 		return cachedText.toString();
 	}
 
 	private void addCachedText(String text) {
 		textCache.add(text);
 	}
 
 	private void emptyCache() {
 		textCache = new ArrayList<String>();
 	}
 
 }
