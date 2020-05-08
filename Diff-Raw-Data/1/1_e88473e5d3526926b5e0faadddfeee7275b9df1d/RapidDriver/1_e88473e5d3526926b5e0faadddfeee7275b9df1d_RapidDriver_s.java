 package fNIRs;
 
 import java.io.*;
 import java.util.*;
 
 import com.rapidminer.RapidMiner.ExecutionMode;
 import com.rapidminer.RapidMiner;
 import com.rapidminer.Process;
 import com.rapidminer.operator.Operator;
 import com.rapidminer.operator.OperatorException;
 import com.rapidminer.operator.IOContainer;
 import com.rapidminer.operator.io.ExcelExampleSource;
 import com.rapidminer.tools.XMLException;
 
 
 import com.rapidminer.example.Attribute;
 import com.rapidminer.example.Example;
 import com.rapidminer.example.ExampleSet;
 
 public class RapidDriver {
 	//Workspace Dir
 	Process process;
 	
 	RapidDriver() throws IOException, XMLException{
 		//Initialize Rapid Miner and create process specified in process file 
 		//Running within another program		
 		RapidMiner.setExecutionMode(ExecutionMode.EMBEDDED_WITHOUT_UI);	
 		File myFile = new File("./src/fNirs/process");
 
 		// MUST BE INVOKED BEFORE ANYTHING ELSE !!!
 		RapidMiner.init();
 	    // create the process from the process file
 		this.process = new Process(myFile);
 	}
 
 	private void writeRow(BufferedWriter fwriter, String[] entries){
 		try {
 			for(int i = 0; i < (entries.length - 1); i++){
 				fwriter.write(entries[i]); 
 				fwriter.write(", ");
 			}
 			fwriter.write(entries[entries.length -1]);
 		}	
 		catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	void empty(File output){
 		try {
 			BufferedWriter fwriter;
 			fwriter = new BufferedWriter(new FileWriter(output));
 			fwriter.write("0");
 			fwriter.newLine();	
 			fwriter.write("0");
 			fwriter.newLine();
 		} catch (IOException ie) {
 			// TODO Auto-generated catch block
 			ie.printStackTrace();
 		}
 		
 	}
 	
 	void filter(ArrayList<Integer> conditions, File input, File output){
 		//Filter a Hb/Hbo file with conditions listed in the last col
 		//Result only contains rows with conditions in conditions array
 		BufferedReader freader;
 		BufferedWriter fwriter;
 		try {
 			freader = new BufferedReader(new FileReader(input));
 			fwriter = new BufferedWriter(new FileWriter(output));
 			String line = freader.readLine();
 			//Remove leading and trailing ws
 			line = line.trim();
 			
 			String [] entries = line.split("(,|\\s)+");
 			
 			//Write Col Headers
 			for(int i = 1; i < (entries.length); i++){
 				fwriter.write("Ch" + i);
 				fwriter.write(", ");	
 			}
 			fwriter.write("Label");
 			fwriter.newLine();
 			//Check to see if the condition is in conditions
 			if (conditions.contains(Integer.parseInt(entries[(entries.length)-1]))){
 				//Write First Row
 				writeRow(fwriter, entries);
 				fwriter.newLine();
 			}
 			while ((line = freader.readLine()) != null){
 				//Remove leading and trailing ws
 				line = line.trim();
 				entries = line.split("(,|\\s)+");
 				//Check to see if the condition is in conditions
 				if (conditions.contains(Integer.parseInt(entries[(entries.length)-1]))){
 					writeRow(fwriter, entries);
 					fwriter.newLine();
 				}
 			}
 			
 			fwriter.close();
 			freader.close();
 			
 		} catch (IOException ie) {
 			// TODO Auto-generated catch block
 			ie.printStackTrace();
 		}
 		
 	}
 	
 	void run(File input) throws OperatorException{
 
 		ExampleSet resultSet1 = null;
 		
 		Operator op = this.process.getOperator("Read Excel");
 
 		op.setParameter(ExcelExampleSource.PARAMETER_EXCEL_FILE, input.getAbsolutePath());
 		
 		IOContainer ioResult = this.process.run();
 		
 		if (ioResult.getElementAt(0) instanceof ExampleSet) {
             resultSet1 = (ExampleSet)ioResult.getElementAt(0);
 
             for (Example example : resultSet1) {
                 Iterator<Attribute> allAtts = example.getAttributes().allAttributes();
                 while(allAtts.hasNext()) {
                     Attribute a = allAtts.next();
                             if (a.isNumerical()) {
                                     double value = example.getValue(a);
                                     System.out.println(value);
 
                             } else {
                                     String value = example.getValueAsString(a);
                                     System.out.println(value);
                             }
                      }
             }
                 }
 	}
 
 /*	public static void main(String[] argv) throws Exception {
 		//		String rapidMinerHome = "C:\\Program Files\\Rapid-I\\RapidMiner5\\lib";
 		//System.out.println(System.getProperty("user.dir"));
 		
 		//		String rapidMinerHome = "/fNIRs";
 //		System.setProperty("rapidminer.home", rapidMinerHome);
 		
 	
 		File myFile = new File("./src/fNirs/process");
 		System.out.print(myFile.getCanonicalPath());
 		// MUST BE INVOKED BEFORE ANYTHING ELSE !!!
 		RapidMiner.init();
 
 		
 	    // create the process from the command line argument file
 		Process process = new Process(myFile);
 
 	    // run the process on the input
 	    process.run();
 	}
 */	
 }
