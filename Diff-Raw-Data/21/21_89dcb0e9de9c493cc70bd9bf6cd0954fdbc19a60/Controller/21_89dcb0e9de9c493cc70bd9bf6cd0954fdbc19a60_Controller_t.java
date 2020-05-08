 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 
 public class Controller {
 	
 	final ArrayList<short[]> data = new ArrayList<short[]>();;
 	final float[] firstLine = new float[60];
 	
 	final String outFile = "patterns.csv";
	//final String inputDir = "D:/Studium/Semester06_So12/Bachelorarbeit/KinectData";
	final String inputDir = "KinectData";
 	
 	// Genauigkeit der Daten
 	final float faktor = 100f;
 	final String format = "0,00";
 	DecimalFormat dcf = new DecimalFormat(format);
 	
	//TODO to be removed soon
 	int max;
 	
 	
 	public Controller(){
 		
 		initializeData();
 		
 		// Fake-Patterns: HashMap mit Startindex und Anzahl Frames
 		HashMap<Integer,Integer> indices = new HashMap<Integer,Integer>();
 		indices.put(3100, 120);
 		indices.put(6000, 500);
 		
 		printForViewer(indices);
 	}
 	
 	
 	
 	/**
 	 * goes through all files in the directory and
 	 * store the values in data
 	 */
 	private void initializeData(){
 		long start = System.currentTimeMillis();;
 		long end;
 
 		File dir = new File(inputDir);
 		File[] fileList = dir.listFiles();
 		
 		int amountOfFiles = fileList.length;
 		int fileCounter = 0;
 		short[] help = null;
 
 		for (File file : fileList) {
 			fileCounter++;
 			System.out.println(file.getName() + " - reading " + fileCounter + "/"+ amountOfFiles + "...");
 			try {
 				// read file content
 				BufferedReader br = new BufferedReader(new FileReader(file));
 				String strLine;
 				boolean initFirst;
 				
 				while ((strLine = br.readLine()) != null) {
 					initFirst = (help == null) ? true : false;
 					short[][] result = processLine(strLine, help, initFirst); 
 					short[] dataset = result[0];
 					if (!initFirst){
 						data.add(dataset); // add new Array to data						
 					}
 					help = result[1];
 				}
 				
 				System.out.println("...reading complete");
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		
 		end = System.currentTimeMillis();
 		System.out.println("Dauer Einlesen: " + (end-start));
 		System.out.println("Gre von data: " + data.size() + ", Maximaler Wert: " + max);
 	}
 	
 	/**
 	 * For one line of a file parses the content and rounds the values
 	 * @param strLine Line
 	 * @param help help-Array containing the values of the line before
 	 * @param initFirst whether it's the first line of the first file or not
 	 * @return short[] containing the values
 	 * @throws ParseException
 	 */
 	private short[][] processLine(String strLine, short[] help, boolean initFirst) throws ParseException {
 		String[] strValues = strLine.split(";");
 		int di = 0;
 		short newValue = 0;
 		short[] dataset = new short[60];
 		short[] helpNew = new short[60];
 		
 		for(int i = 1; i<strValues.length; i++){
 			
 			if (i % 4 == 0) continue;  // Koordinate W	
 			
 			if(initFirst){
 				firstLine[di] = Math.round(dcf.parse(strValues[i]).floatValue() *faktor)/faktor;
 				helpNew[di] = (short) (dcf.parse(strValues[i]).floatValue() * faktor);
 				//System.out.println("help " + di + ": " + s[i] + " => " + d[di]);
 				
 			}else{
 				newValue = (short) (dcf.parse(strValues[i]).floatValue() * faktor);
 				if (Math.abs(newValue - help[di]) > Short.MAX_VALUE) {
 					System.out.println(">>> value too big: " + (newValue-help[di]));
 					}
 				dataset[di] = (short)(newValue - help[di]);
 				if(dataset[di]>max){
 					max = dataset[di];
 				}
 				helpNew[di] = newValue;
 			}
 			
 			di++;	
 		}
 		
 		short[][] result = {dataset, helpNew};
 		return result;
 	}
 	
 	
 	
 	/**
 	 * Durchlaeuft Vektoren-Array, berechnet jeweils den aktuellen 
 	 * Punktdatensatz und schreibt diesen in eine Datei,
 	 * falls er zu einem gefundenen Pattern gehrt
 	 * @param patternIndices HashMap die die Position als key und Laenge 
 	 * als value der gefundenen Patterns enthaelt
 	 */
 	private void printForViewer(HashMap<Integer,Integer> patternIndices){
 		StringBuffer buff = new StringBuffer();
 		int ms = 0;
 		short[] dataset;
		float[] pointDataset = firstLine.clone();  // beginnend vom ersten Datensatz Punkte... 
 		
 		for (int i = 0; i < data.size(); i++) {
 			dataset = data.get(i);
 			pointDataset = calculateNewPoints(pointDataset, dataset);
 			
 			if(patternIndices.containsKey(i)){
				float[] patternPoints = pointDataset.clone();
				int patternEnd = patternIndices.get(i);
				for (int j = 0; j < patternEnd; j++) {
 					buff.append(ms+";");
 					for (int k = 0; k < patternPoints.length; k++) {
 						buff.append(patternPoints[k] + ";");
 						if((k+1)%3 == 0){
 							buff.append("1;");
 						}
 					}
 					ms += 33;
 					buff.append("\n");
 					
 					//i++;  // i nicht weiter hochzhlen - patterns muessen sich ueberschneiden koennen!
					//dataset = data.get(i);
					dataset = data.get(i+j+1);
 					patternPoints = calculateNewPoints(patternPoints, dataset);
 				}
 				for (int k = 0; k < 30; k++) { // am Ende der Sequenz eine Sekunde lang Nuller
 					buff.append("0;0;0;0;1;0;0;0;1;0;0;0;1;0;0;0;1;0;0;0;1;0;0;0;1;0;0;0;1;0;0;0;1;0;0;0;1;0;0;0;1;");
 					buff.append("0;0;0;1;0;0;0;1;0;0;0;1;0;0;0;1;0;0;0;1;0;0;0;1;0;0;0;1;0;0;0;1;0;0;0;1;0;0;0;1\n");
 				}
 			}
 		}
 		
 		FileWriter writer;
 		try {
 			writer = new FileWriter(outFile, false);
 			writer.write(buff.toString().replace(".", ","));
 			writer.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Calculates the new Joints
 	 * @param pointDataset old Joints
 	 * @param dataset the vectors
 	 */
 	private float[] calculateNewPoints(float[] pointDataset, short[] dataset){
 		for (int j = 0; j < pointDataset.length; j++) {
 			pointDataset[j] = pointDataset[j] + ((float)dataset[j])/faktor;
 		}
 		return pointDataset;
 	}
 
 
 }
