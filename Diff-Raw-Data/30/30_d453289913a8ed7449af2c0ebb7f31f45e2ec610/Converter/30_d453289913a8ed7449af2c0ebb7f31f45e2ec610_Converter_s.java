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
 	//private Display display;
 	private File inputFile;
 	private File outputFile;
 	
 	private Map<String, MessageType> msgdat;
 	private ArrayList<float[]> values;
 	private ArrayList<String> axis;
 	
 	private int convertProgress;
 	private int normalizeProgress;
 	private int printProgress;
 	
 	public Converter() {
 		//display = new Display(this);
 		//display.setVisible(true);
 		initMessageInfo();
 		setInputFile(new File(new File("").getAbsolutePath() + "/sample-data/500.data"));
 		setOutputFile(new File(new File("").getAbsolutePath() + "/sample-data/500.out"));
 	}
 	
 	public void begin() {
 		this.convertProgress = 0;
 		this.normalizeProgress = 0;
 		this.printProgress = 0;
 		
 		long time = System.currentTimeMillis();
 		this.axis = prepareAxis();
 		
 		convert();
 		normalize();
 		outputValues();
 		
 		System.out.println("Time Elapsed " + (System.currentTimeMillis() - time) / 1000.0 + "s");
 	}
 	
 	public void initMessageInfo() {
 		msgdat = new TreeMap<String, MessageType>();
 		msgdat.put("0050", new MessageType("0050", 4, new SubMessage[] {new SubMessage("Suspension Position RL", false, false, 0.012207f, "mm"), new SubMessage("Suspension Position RR", false, false, 0.0148866f, "mm")}));
 		msgdat.put("0060", new MessageType("0060", 4, new SubMessage[] {new SubMessage("Suspension Position FR", false, false, 0.012207f, "mm"), new SubMessage("Suspension Position FL", false, false, 0.012207f, "mm")}));
 		msgdat.put("0070", new MessageType("0070", 8, new SubMessage[] {new SubMessage("Yaw Rate", true, false, 0.005f, "deg/sec"), new SubMessage("Reserved", true, false, 1, ""), new SubMessage("Lateral Acceleration", true, false, 0.0001274f, "g"), new SubMessage("Unused", true, false, 1, "")}));
 		msgdat.put("0080", new MessageType("0080", 8, new SubMessage[] {new SubMessage("Yaw Acceleration", true, false, 0.0125f, "deg/sec^2"), new SubMessage("Reserved", true, false, 1, ""), new SubMessage("Longitudinal Acceleration", true, false, 0.0001274f, "g"), new SubMessage("Unused", true, false, 1, "")}));
 		msgdat.put("0100", new MessageType("0100", 0, new SubMessage[] {}));
 		msgdat.put("0110", new MessageType("0110", 6, new SubMessage[] {new SubMessage("Brake Pressure 1", false, false, 0.7629395f, "psi"), new SubMessage("Brake Pressure 0", false, false, 0.7629395f, "psi"), new SubMessage("Steering Angle", false, false, 0.0878906f, "deg"), new SubMessage("Unused", true, false, 1, "")}));
 		msgdat.put("0200", new MessageType("0200", 8, new SubMessage[] {new SubMessage("RPM", true, true, 1, "rpm"), new SubMessage("Throttle Position", true, true, 0.1f, "%"), new SubMessage("Oil Pressure", true, true, 0.1f, "psi"), new SubMessage("Oil Temperature", true, true, 0.1f, "C")}));
 		msgdat.put("0201", new MessageType("0201", 8, new SubMessage[] {new SubMessage("Engine Temperature", true, true, 0.1f, "C"), new SubMessage("Lambda", true, true, 0.001f, "La"), new SubMessage("Manifold Pressure", true, true, 0.1f, "kPa"), new SubMessage("Battery Voltage", true, true, 0.01f, "V")}));
 		msgdat.put("0202", new MessageType("0202", 8, new SubMessage[] {new SubMessage("Wheel Speed FL", true, true, 0.1f, "MPH"), new SubMessage("Wheel Speed FR", true, true, 0.1f, "MPH"), new SubMessage("Wheel Speed RR", true, true, 0.1f, "MPH"), new SubMessage("Wheel Speed RL", true, true, 0.1f, "MPH")}));
 		msgdat.put("0203", new MessageType("0203", 2, new SubMessage[] {new SubMessage("Ground Speed", true, true, 0.1f, "MPH"), new SubMessage("Unused", true, true, 0.1f, "")}));
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
 	
 	private void convert() {
 		if(inputFile == null || !inputFile.canRead() || !inputFile.exists() || inputFile.isDirectory() || !inputFile.isFile()) {
 			//JOptionPane.showConfirmDialog(progressBar.getParent(), "Please choose a valid input file.", "Invalid File Error", JOptionPane.ERROR_MESSAGE);
 			System.out.println("Invalid File. Please choose another.");
 			return;
 		}
 				
 		byte[] data = null;
 		try {
 			data = Files.readAllBytes(inputFile.toPath());
 		} catch (IOException e) {
 			//JOptionPane.showConfirmDialog(progressBar.getParent(), "Please try again or try a different file.", "IO Error", JOptionPane.ERROR_MESSAGE);
 			System.out.println("IO Error. Please try again or try a different file.");
 			return;
 		}
 		
 		this.values = new ArrayList<float[]>();
 			
 		int i=0;
 		while(true) {
 			try {
 				if(i >= data.length) break;
 				
 				int temp = (int)(Math.floor(((i / (float)data.length) * 100.0f)));
 				if(temp > this.convertProgress) setProgress(0,temp);
 				
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
 		setProgress(0, 100);
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
				
		for(int i=0;i<values.size();i++) {
			for(int m=0;m<values.get(i).length;m++) {
				if(values.get(i)[m] == Float.MAX_VALUE) {
					for(int n=i;n>=0;n--) {
						if(values.get(n)[m] != Float.MAX_VALUE) {
							values.get(i)[m] = values.get(n)[m];
							break;
						}
					}
				}
 			}
			int temp = (int)(Math.floor(((i / (float)values.size()) * 100.0f)));
 			if(temp > this.normalizeProgress) setProgress(1, temp);
 		}
 				
 		for(int i=0;i<values.get(0).length;i++) {
 			for(int j=0;j<values.size();j++) {
 				if(values.get(j)[i] == Float.MAX_VALUE) {
 					values.get(j)[i] = 0.0f;
 				}
 			}
 		}
 		setProgress(1, 100);
 	}
 	
 	public void outputValues() {
 		try {
 			FileWriter fw = new FileWriter(outputFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
 			
 			for(String e: axis) {
 				bw.write(e + " ");
 			}
 			bw.write("\n");
 			
 			for(int i=0;i<values.size();i++) {
 				for(int j=0;j<values.get(i).length;j++) {
 					bw.write(round(values.get(i)[j]) + " ");
 				}
 				
 				int temp = (int)(Math.floor(((i / (float)values.size()) * 100.0f)));
 				if(temp > this.printProgress) setProgress(2, temp);
 				
 				bw.write("\n");
 			}
 			setProgress(2, 100);
 			
 			bw.close();
 			fw.close();
 		} catch (IOException e) {
 			//JOptionPane.showConfirmDialog(progressBar.getParent(), "Please try again or try a different file.", "IO Error", JOptionPane.ERROR_MESSAGE);
 			System.out.println("IO Error. Please try again or try a different file.");
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
 	
 	private void setProgress(int which, int progress) {
 		switch(which) {
 			case 0:
 				this.convertProgress = progress;
 				System.out.println("Conversion Progress: " + progress + "%");
 				return;
 			case 1:
 				this.normalizeProgress = progress;
 				System.out.println("Normalization Progress: " + progress + "%");
 				return;
 			case 2:
 				this.printProgress = progress;
 				System.out.println("Output Progress: " + progress + "%");
 				return;
 		}
 	}
 }
