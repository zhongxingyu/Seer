 package org.fit.cvut.mvi.evaluator;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class FitnessEvaluator {
 
 	private String pathToNetLogo;
 	private String pathToTemplate;
 	private String pathToSetupFile;
 	
 	/**
 	 * NetLogo code for random move. Can be used for moveSheepBody and moveWolfBody arguments.
 	 */
 	public final static String RANDOM_MOVE = "report list random 360 random-float 1";
 	
 	/**
 	 * Determines whether or not the temporary .nlogo files should be deleted after execution.
 	 */
 	public static boolean deleteAfterExecution = true;
 	
 	/**
 	 * Creates new instance of fitness evaluator.
 	 * @param pathToNetLogo Path to the NetLogo.jar in the filesystem.
 	 * @param pathToTemplate Path to the MI-MVI NetLogo template (sablona.nlogo) in the filesystem.
 	 * @param pathToSetupFile Path to the MI-MVI NetLogo experiments setup file (sablona.xml) in the filesystem.
 	 */
 	public FitnessEvaluator(String pathToNetLogo, String pathToTemplate, String pathToSetupFile) {
 		
 		this.pathToNetLogo = pathToNetLogo;
 		this.pathToTemplate = pathToTemplate;
 		this.pathToSetupFile = pathToSetupFile;
 	}
 	
 	/**
 	 * Calculates the fitness of sheep w.r.t. given sheep and wolf control codes.
 	 * @param moveSheepBody Body of the NetLogo move-sheep reporter.
 	 * @param moveWolfBody Body of the NetLogo move-wolf reporter.
 	 * @return Sheep fitness.
 	 */
 	public double evaluateSheepFitness(String moveSheepBody, String moveWolfBody) {
 		
 		try {
 			
 			// Create temporary directory if not exists
 			File tmpDir = new File("tmp");
 			if(!tmpDir.exists()) {
 				tmpDir.mkdir();
 			}
 
 			String line;
 			
 			// Concatenate code provided with the template
 			File template = new File(this.pathToTemplate);
 			BufferedReader templateReader = new BufferedReader(new FileReader(template));
 
 			File nlogoFile = new File("tmp/" + Math.round(1e16*Math.random()) + ".nlogo");
 			FileWriter nlogoWriter = new FileWriter(nlogoFile);
 			
 			nlogoWriter.write("to-report move-sheep\n");
 			nlogoWriter.write(moveSheepBody + "\n");
 			nlogoWriter.write("end\n\n");
 			
 			nlogoWriter.write("to-report move-wolf\n");
 			nlogoWriter.write(moveWolfBody + "\n");
 			nlogoWriter.write("end\n\n");
 			
 			while((line = templateReader.readLine()) != null) {
 				nlogoWriter.write(line + "\n");
 			}
 			
 			templateReader.close();
 			nlogoWriter.close();
 			
 			
 			// Execute the process
			Process process = Runtime.getRuntime().exec("java -Xmx1024m -Dfile.encoding=UTF-8 -cp \""
				+ this.pathToNetLogo + "\" org.nlogo.headless.Main --model \""
				+ nlogoFile.getAbsolutePath() + "\" --setup-file \""
				+ this.pathToSetupFile + "\" --experiment experiment --table -");
 			
 			// Create output reader
 			InputStream output = process.getInputStream();
 			BufferedReader outputReader = new BufferedReader(new InputStreamReader(output));
 			
 			// Variables for averaging fitness
 			double fitnessSum = 0;
 			int fitnessCount = 0;
 			
 			// Regular expression pattern for matching relevant lines
 			Pattern p = Pattern.compile("^(\"\\w+\",){10}\"(\\d+)\",\"(\\d+)\",\"(\\d+)\"$");
 			
 			// Read the lines until the process finishes its output
 			while((line = outputReader.readLine()) != null) {
 				
 				Matcher m = p.matcher(line);
 				if(m.matches()) {
 					
 					int tick = Integer.parseInt(m.group(2));
 					int numSheep = Integer.parseInt(m.group(3));
 					int numWolves = Integer.parseInt(m.group(4));
 					
 					if(numSheep == 0 || numWolves == 0 || tick == 300) {
 						
 						if(numWolves == 0 || numWolves == 0) {
 							fitnessSum += (numWolves == 0 ? 1 : -1) * 100;
 							fitnessSum += 10*(numSheep - numWolves);
 						}
 						else {
 							fitnessSum += numSheep - numWolves;
 						}
 						
 						fitnessCount ++;
 					}
 				}
 			}
 			
 			// Delete the temporary file if set so.
 			if(FitnessEvaluator.deleteAfterExecution) {
 				nlogoFile.delete();
 			}
 			
 			// Return averaged fitness
 			return fitnessSum / fitnessCount;
 		}
 		catch(Exception ex) {
 			
 			ex.printStackTrace();
 			
 			// If anything goes wrong, burn in hell!
 			return Double.NEGATIVE_INFINITY;
 		}
 	}
 	
 	/**
 	 * Calculates the fitness of wolves w.r.t. given sheep and wolf control codes.
 	 * @param moveSheepBody Body of the NetLogo move-sheep reporter.
 	 * @param moveWolfBody Body of the NetLogo move-wolf reporter.
 	 * @return Wolves fitness.
 	 */
 	public double evaluateWolvesFitness(String moveSheepBody, String moveWolfBody) {
 		
 		// Fitness of wolves = inverse fitness of sheep.
 		return -evaluateSheepFitness(moveSheepBody, moveWolfBody);
 	}
 	
 }
