 package daemon;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 
 import net.sf.json.JSONObject;
 
import antlr.collections.List;

 public class TailBuffer implements Runnable{
 	
 	private String FILEPATH = "";
 	private BufferedReader in = null;
 	private class TailFrame{
 		TailFrame(String line,int frm){
 			this.sb=line;
 			this.frame = frm;
 		}
 		String sb = null;
 		int frame ;
 	}
 	static ArrayList<TailFrame> frames = null;
 	
 	public void setFILEPATH(String fILEPATH) {
 		FILEPATH = fILEPATH;
 	}
 	public static JSONObject getTailOutput(int index){
 		StringBuffer tailString = new StringBuffer();
 		int frame=index;
 		for(int i=frame;i<frames.size();i++){
 			if(frames.get(i)!=null){
 				String line =frames.get(i).sb;
 				tailString.append(line+"\n");
 				frame = i+1;
 			}
 		}
 	    JSONObject jsonObject = new JSONObject();
 	    jsonObject.set("frame", frame);
 	    jsonObject.set("tailOutput", tailString.toString());
 	    System.out.println("JSON " + jsonObject);
 		return jsonObject;
 	}
 	@Override
 	public void run() {
 		frames = new ArrayList<TailFrame>();
 		try {
 			in = new BufferedReader(new InputStreamReader(
 					 Runtime.getRuntime().exec("tail -n 100000 -f " + FILEPATH).getInputStream()));
 			String line = null;
 			int i = 0;
 			while((line = in.readLine())!=null){	
 				TailFrame tf = new TailFrame(line, i++);
 				frames.add(tf);
 				if(line.equals("EOFBLD"))
 					break;
 			}
 			System.out.println("Thread ended");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	public static void main(String []args){
 		TailBuffer tb = new TailBuffer();
 		frames = new ArrayList<TailBuffer.TailFrame>();
 		tb.setFILEPATH("/tmp/tail.txt");
 		Thread t = new Thread(tb);
 		t.start();
 	}
 }
