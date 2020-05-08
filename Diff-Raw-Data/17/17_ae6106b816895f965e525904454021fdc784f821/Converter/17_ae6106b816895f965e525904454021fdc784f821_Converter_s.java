 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 public class Converter {
 	private Display display;
 	private File inputFile;
 	private File outputFile;
 	
 	private Map<String, MessageType> msgdat;
 	private ArrayList<float[]> values;
 	private ArrayList<String> axis;
 	
 	private int convertProgress;
 	private int normalizeProgress;
 	private int printProgress;
 	
 	public Converter() {
 		display = new Display(this);
 		display.setVisible(true);
 		initMessageInfo();
 	}
 	
 	public void begin() {
 		this.convertProgress = 0;
 		this.normalizeProgress = 0;
 		this.printProgress = 0;
 		
 		long time = System.currentTimeMillis();
 		this.axis = prepareAxis();
 		
 		if(!prepareFiles()) return;
 		if(!convert()) return;
 		normalize();
 		outputValues();
 		
 		display.showTimeElapsed((System.currentTimeMillis() - time) / 1000.0);
 	}
 	
 	public void initMessageInfo() {
 		msgdat = new TreeMap<String, MessageType>();
 		msgdat.put("0050", new MessageType("0050", 4, new SubMessage[] {new SubMessage("SuPosRL", false, false, 0.012207f, "mm"), new SubMessage("SuPosRR", false, false, 0.0148866f, "mm")}));
 		msgdat.put("0060", new MessageType("0060", 4, new SubMessage[] {new SubMessage("SuPosFR", false, false, 0.012207f, "mm"), new SubMessage("SuPosFL", false, false, 0.012207f, "mm")}));
 		msgdat.put("0070", new MessageType("0070", 8, new SubMessage[] {new SubMessage("YawRate", true, false, 0.005f, "dg/s"), new SubMessage("Reserved", true, false, 1, ""), new SubMessage("LatAccl", true, false, 0.0001274f, "g"), new SubMessage("Unused", true, false, 1, "")}));
 		msgdat.put("0080", new MessageType("0080", 8, new SubMessage[] {new SubMessage("YawAccl", true, false, 0.0125f, "d/s2"), new SubMessage("Reserved", true, false, 1, ""), new SubMessage("LngAccl", true, false, 0.0001274f, "g"), new SubMessage("Unused", true, false, 1, "")}));
 		msgdat.put("0100", new MessageType("0100", 0, new SubMessage[] {}));
 		msgdat.put("0110", new MessageType("0110", 6, new SubMessage[] {new SubMessage("BrkPrs1", false, false, 0.7629395f, "psi"), new SubMessage("BrkPrs0", false, false, 0.7629395f, "psi"), new SubMessage("SterAng", false, false, 0.0878906f, "deg"), new SubMessage("Unused", true, false, 1, "")}));
 		msgdat.put("0200", new MessageType("0200", 8, new SubMessage[] {new SubMessage("RPM", true, true, 1, "rpm"), new SubMessage("ThrtPos", true, true, 0.1f, "%"), new SubMessage("OilPres", true, true, 0.1f, "psi"), new SubMessage("OilTemp", true, true, 0.1f, "C")}));
 		msgdat.put("0201", new MessageType("0201", 8, new SubMessage[] {new SubMessage("EngTemp", true, true, 0.1f, "C"), new SubMessage("Lambda", true, true, 0.001f, "La"), new SubMessage("ManPres", true, true, 0.1f, "kPa"), new SubMessage("BatVolt", true, true, 0.01f, "V")}));
 		msgdat.put("0202", new MessageType("0202", 8, new SubMessage[] {new SubMessage("WlSpdFL", true, true, 0.1f, "MPH"), new SubMessage("WlSpdFR", true, true, 0.1f, "MPH"), new SubMessage("WlSpdRR", true, true, 0.1f, "MPH"), new SubMessage("WlSpdRL", true, true, 0.1f, "MPH")}));
 		msgdat.put("0203", new MessageType("0203", 2, new SubMessage[] {new SubMessage("GrndSpd", true, true, 0.1f, "MPH"), new SubMessage("Unused", true, true, 0.1f, "")}));
 	}
 	
 	private ArrayList<String> prepareAxis() {
 		ArrayList<String> axis = new ArrayList<String>();
 		
 		int c = 0;
 		axis.add("Time [s] ");
 		Iterator<Entry<String, MessageType>> it = msgdat.entrySet().iterator();
 		while(it.hasNext()) {
 			Entry<String, MessageType> message = it.next();
 			SubMessage[] messages = message.getValue().getSubMessages();
 			for(int i=0;i<messages.length;i++) {
 				if(!(messages[i].getTitle().equals("Reserved") || messages[i].getTitle().equals("Unused"))) {
 					c++;
 					message.getValue().getSubMessages()[i].setColumnIndex(c);
 					axis.add(messages[i].getTitle() + " [" + messages[i].getUnits() + "] ");
 				}
 			}
 		}
 		return axis;
 	}
 	
 	private boolean prepareFiles() {
 		if(inputFile == null || !inputFile.canRead() || !inputFile.exists() || inputFile.isDirectory() || !inputFile.isFile()) {
 			display.error("Invalid File Error","Please choose a valid input file.");
 			return false;
 		}
 		
 		if(outputFile == null) {
 			display.error("Invalid File Error", "Please specify an output file.");
 			return false;
 		}
 		return true;
 	}
 	
 	private boolean convert() {		
 		byte[] data = null;
 		try {
 			data = Files.readAllBytes(inputFile.toPath());
 		} catch (IOException e) {
 			display.error("IO Error","Please try again or try a different file.");
 			return false;
 		}
 		
 		this.values = new ArrayList<float[]>();
 			
 		int i=0;
 		while(true) {
 			try {
 				if(i >= data.length) break;
 				
 				int temp = (int)(Math.floor(((i / (float)data.length) * 100.0f)));
				if(temp > this.convertProgress) display.setProgress(0,temp);
 				
 				String msgId = hex(data[i+1]) + hex(data[i]);
 				if(msgdat.containsKey(msgId)) {
 					int len = msgdat.get(msgId).getLength();
 					byte[] msgBytes = new byte[len];
 					byte[] timeBytes = new byte[] {data[i + len + 2], data[i + len + 3], data[i + len + 4], data[i + len + 5]};
 					for(int j=0; j<len;j++) {
 						msgBytes[j] = data[j+i+2];
 					}
 					values.add(msgdat.get(msgId).translateData(msgBytes, timeBytes, this.axis.size()));						
 					i = i + len + 6;
 				} 
 				else {
 					System.out.println("Error: Message Code Not Found - " + msgId);
 					break;
 				}
 			} catch(ArrayIndexOutOfBoundsException e) {
 				break;
 			}
 		}
		display.setProgress(0, 100);
 		return true;
 	}
 	
 	private void normalize() {
 		for(int i=1;i<values.get(0).length;i++) {
 			for(int j=0;j<values.size();j++) {
 				if(values.get(j)[i] != Float.MAX_VALUE) {
 					for(int k=0;k<j;k++) {
 						values.get(k)[i] = values.get(j)[i];
 					}
 					break;
 				}
 			}
 		}			
 		
 		for(int i=1;i<values.get(0).length;i++) {
 			int j=0;
 			float val = values.get(0)[i];
 			while(j < values.size()) {
 				if(values.get(j)[i] == Float.MAX_VALUE) values.get(j)[i] = val;					 
 				else val = values.get(j)[i];
 				j++;
 			}
 			int temp = (int)(Math.floor(((i / (float)values.get(0).length) * 100.0f)));
			if(temp > this.normalizeProgress) display.setProgress(1, temp);
 		}
 				
 		for(int i=0;i<values.get(0).length;i++) {
 			for(int j=0;j<values.size();j++) {
 				if(values.get(j)[i] == Float.MAX_VALUE) {
 					values.get(j)[i] = 0.0f;
 				}
 			}
 		}
		display.setProgress(1, 100);
 	}
 	
 	public void outputValues() {
 		try {
 			FileWriter fw = new FileWriter(outputFile.getAbsoluteFile());
 			BufferedWriter bw = new BufferedWriter(fw, 32768);
 			
 			for(String e: axis) {
 				bw.write(e + " ");
 			}
 			bw.write("\n");
 			
 			for(int i=0;i<values.size();i++) {
 				for(int j=0;j<values.get(i).length;j++) {
 					bw.write(round(values.get(i)[j]) + " ");
 				}
 				
 				int temp = (int)(Math.floor(((i / (float)values.size()) * 100.0f)));
				if(temp > this.printProgress) display.setProgress(2, temp);
 				
 				bw.write("\n");
 			}
			display.setProgress(2, 100);
 			
 			bw.flush();
 			fw.flush();
 			bw.close();
 			fw.close();
 		} catch (IOException e) {
 			display.error("IO Error","Please try again or try a different file.");
 			return;
 		}
 	}
 	
 	public void setInputFile(File file) {
 		this.inputFile = file;
 	}
 	
 	public void setOutputFile(File file) {
 		this.outputFile = file;
 	}
 	
 	public String round(double num) {
 		final int SIG_FIGS = 6;
 	    if(num == 0) {
 	        return "0.0";
 	    }
 
 	    final double d = Math.ceil(Math.log10(num < 0 ? -num: num));
 	    final int power = SIG_FIGS - (int) d;
 
 	    final double magnitude = Math.pow(10, power);
 	    final long shifted = Math.round(num*magnitude);
 	    return "" + shifted/magnitude;
 	}
 	
 	private String hex(byte num) {
 		return String.format("%02x", num);
 	}
 	
 	public void setProgress(int which, int progress) {
 		display.setProgress(which, progress);
 		switch(which) {
 			case 0:
 				this.convertProgress = progress;
 				return;
 			case 1:
 				this.normalizeProgress = progress;
 				return;
 			case 2:
 				this.printProgress = progress;
 				return;
 		}
 	}
 }
