 package bioUtils;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.Map;
 
 import bioUtils.Result;
 
 public class CalcThread implements Runnable {
 
 	protected File f = null;
 	protected int sIndex = 0;
 	protected int winSize = 0;
 	protected Map<Integer, Result> resMap = null;
 	protected int lIndex = -1;
 
 	public CalcThread(File val, int startIndex, int lastIndex, int windowSize, Map<Integer, Result> map) {
 		
 		this.f = val;
 		this.sIndex = startIndex;
 		this.winSize = windowSize;
 		this.resMap = map;
 		this.lIndex = lastIndex;
 	}
 
 	public void run() {
 		
 		BufferedReader reader = null;
 		int curIndex = sIndex;
 		
 		try {
 			FileReader input = new FileReader(f);
 			reader = new BufferedReader(input);
 			
 			//Skip the first line
 			reader.readLine();
 			
 			//Get to the start index
 			for(int i = 1; i < sIndex;) {
 				int res = reader.read();
 				if(res > 40) {
 					i++;
 				}
 				if(res == -1) {
 					input.close();
 					return;
 				}
 			}
 			
 			boolean end = false;
 			
 			//Continue to read window sizes until we reach either the end of file or
 			//we hit last index
 			while(!end) {
 				
 				StringBuilder segment = new StringBuilder();
 				int startIndex = curIndex;
 				
 				for(int i  = 0; i < winSize;) {
 					int res = reader.read();
 					if(res > 40) {
 						segment.append((char)res);
 						i++;
 						curIndex++;
 					}
 					if(res == -1 || (curIndex >= lIndex + 1 && lIndex > 0)) {
 						end = true;
 						break;
 					}
 				}
 				
 				Result res = DNAUtil.findGcContentForSegment(segment.toString());
 				if(!res.max.isNaN() && !res.min.isNaN()) {
					if(curIndex <= lIndex + 1 || lIndex == -1) {
 						resMap.put(startIndex, res);
 					}
 				}
 
 			}			
 			
 			reader.close();
 		} catch (Exception e) {
 			return;
 		}
 		
 		return;
 	}
 
 }
