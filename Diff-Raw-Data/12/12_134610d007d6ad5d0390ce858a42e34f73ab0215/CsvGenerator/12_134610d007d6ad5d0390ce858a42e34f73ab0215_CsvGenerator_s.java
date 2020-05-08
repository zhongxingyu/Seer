 package at.ac.tuwien.lsdc.csvGenerator;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.math3.random.JDKRandomGenerator;
 import org.apache.commons.math3.random.RandomData;
 import org.apache.commons.math3.random.RandomDataImpl;
 
 import at.ac.tuwien.lsdc.generator.Request;
 import at.ac.tuwien.lsdc.generator.RequestGenerator;
 
 public class CsvGenerator {
 
 	public static void main(String args[]) throws IOException {
 
 		File f;
 		f = new File("input.csv");
 		if (!f.exists()) {
 			f.createNewFile();
 			System.out
 					.println("New file \"input.txt\" has been created to the current directory");
 		}
 		try {
 			
 			List<Integer> slas = new ArrayList<Integer>();
 			
 			
 			BufferedWriter out = new BufferedWriter(new FileWriter("input.csv"));
 			RandomData randomData = new RandomDataImpl();
 			
 
			for(int k =0;k<300;k++){
 			// Runtime
			int value = randomData.nextInt(300, 8000);
 			out.write(value + ";");
 			// Slas
 			for (int i = 0; i < 3; i++){
 				int generatedValue= randomData.nextInt(11, 60);
 				slas.add(generatedValue);
 				out.write(generatedValue + ";");
 			}
 
 			//Set statistical parameters for the normal distribution m�, sigma
 			int cpumu = randomData.nextInt(10, (int)(slas.get(0)*0.7));
 			int cpusigma = randomData.nextInt(1, (int)cpumu/5+1);
 			
 			int memmu = randomData.nextInt(10, (int)(slas.get(1)*0.7));
 			int memsigma = randomData.nextInt(1, (int)memmu/5+1);
 			
 			int stormu = randomData.nextInt(10, (int)(slas.get(2)*0.7));
 			int storsigma = randomData.nextInt(1, (int)stormu/5+1);
 			
 			// Cpu
 			for (int i = 0; i < value; i++) {
 				out.write(new Double(randomData.nextGaussian(cpumu, cpusigma)).intValue()+ ";");
 			}
 			// Memory
 			for (int i = 0; i < value; i++)
 				out.write(new Double(randomData.nextGaussian(memmu, memsigma)).intValue()+ ";");
 			// Storage
 			for (int i = 0; i < value; i++)
 				out.write(new Double(randomData.nextGaussian(stormu, storsigma)).intValue()+ ";");
 			//Start
 			out.write(randomData.nextInt(1, 20000)+";");
 
 			//
 			out.newLine();
 			out.flush();
 			
 			}
 
 		} catch (IOException e) {
 			System.out.println("Excption while writing");
 			
 		}
 		
 		RequestGenerator rg = RequestGenerator.getInstance();
 		List<Request> requests = new ArrayList<Request>();
 		requests = rg.generateRequests();
 		for (Request r: requests){
 			System.out.println(r.toString());
 			System.out.println("");
 		}
 
 	}
 
 }
