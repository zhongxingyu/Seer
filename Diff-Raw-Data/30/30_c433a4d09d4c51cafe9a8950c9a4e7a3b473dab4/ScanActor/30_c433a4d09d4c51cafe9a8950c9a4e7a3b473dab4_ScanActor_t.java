 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import akka.actor.ActorRef;
 import akka.actor.UntypedActor;
 
 
 public class ScanActor extends UntypedActor {
 	private String filename;
 	private String pattern;
 	private ActorRef collectionActor;
 	private Pattern patternObj;
 	
 	public ScanActor() {}
 
 	@Override
 	public void onReceive(Object object) throws Exception {
 		if (object.getClass() == Configure.class) {
 			this.filename = ((Configure) object).filename;
 			this.pattern = ((Configure) object).pattern;
 			this.collectionActor = ((Configure) object).collectionActor;
 			this.patternObj = Pattern.compile(this.pattern);
 			
 			if (this.filename != null) {
 				this.scanFile();
 			} else {
 				this.filename = "stdin";
 				this.scanStdIn();
 			}
 		}
 	}
 	
 	private void scanStdIn() {
 		ArrayList<String> lines = new ArrayList<String>();
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 		String line;
 		int lineNum = 0;
 		
		try {
			while ((line = in.readLine()) != null && line.length() != 0) {
				Matcher matcher = patternObj.matcher(line);
				
				if (matcher.find()) {
					lines.add(lineNum + " " + line);
				}
				
				lineNum++;
 			}
 			
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
 		}
 		
 		collectionActor.tell(new Found(this.filename, lines));
 	}
 	
 	private void scanFile() {
 		ArrayList<String> lines = new ArrayList<String>();
 		BufferedReader br;
 		String line;
 		int lineNum = 0;
 		
 		try {
 			br = new BufferedReader(new FileReader(this.filename));
 			
 			while ((line = br.readLine()) != null) {
 				Matcher matcher = patternObj.matcher(line);
 				
 				if (matcher.find()) {
 					lines.add(lineNum + " " + line);
 				}
 				
 				lineNum++;
 			}
 			
 			collectionActor.tell(new Found(this.filename, lines));
 			
 			br.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
