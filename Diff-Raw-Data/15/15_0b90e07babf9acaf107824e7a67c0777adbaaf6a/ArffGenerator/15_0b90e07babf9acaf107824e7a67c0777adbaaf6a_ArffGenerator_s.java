 /**
  * 
  */
 package VisualizeBinary;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import javax.swing.JCheckBox;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import VisualizeBinary.Features.AverageAgeFeature;
 import VisualizeBinary.Features.Feature;
 import VisualizeBinary.Features.TotalAgeFeature;
 import VisualizeBinary.Features.TotalFeature;
 import VisualizeBinary.Matrix.Matrix;
 
 /**
  * This class will allow users to generate an .arff file from fragments
  * @author hartell
  *
  */
 public class ArffGenerator {
 	
 	/**
 	 * Allows the user to select the data sets for the arff file
 	 * 
 	 * @return boolean - if the user is finished selecting sets
 	 */
 	public static boolean selectDataSets(ArrayList<File> fragDirs){
 		boolean done = false;
 		
 		//Create a JFileChooser to allow the user to navigate to the select data set directory
 		JFileChooser chooser = new JFileChooser();
 		chooser.setDialogTitle("Select a dataset");
 		chooser.setCurrentDirectory(new File("H://SVM/"));
 		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 		
 		int option = chooser.showOpenDialog(null);
 		
 		//If the user has added a folder to the train data
 		if(option == JFileChooser.APPROVE_OPTION){
 			//Add the files to the list
 			File dir = chooser.getSelectedFile();
 			fragDirs.add(dir);
 			System.out.println("Added to dataset: " + dir.getAbsolutePath() + " as type = " + (fragDirs.size()) );
 		}
 		else{
 			//Cancel the adding of a data set file
 		}
 		
 		//Ask the user if the want to add more data to the set?
 		int addMore = JOptionPane.showConfirmDialog(null, "Add another data set?", "Add more?", JOptionPane.YES_NO_OPTION);
 		if(addMore == JOptionPane.YES_OPTION){
 			done = false;
 		}
 		else if(addMore == JOptionPane.NO_OPTION){
 			done = true;
 		}
 		else if(addMore == JOptionPane.CLOSED_OPTION){
 			done = true;
 		}
 		
 		return done;
 	}
 	
 	/**
 	 * Creates all the attributes for the arff file
 	 * Example: @ ATTRIBUTE avgBytes numeric
 	 * @param theBoxes
 	 * @return 
 	 */
 	public static ArrayList<String> createAttributes(ArrayList<String> arffFile,  ArrayList<File> fragDirs, ArrayList<JCheckBox> theBoxes, int granularity){
 		
 		//Add in a file about the fileName (key attribute) -- will be removed from within weka
 		arffFile.add("@ATTRIBUTE fName string");
 		
 		//Go through all the Attributes from the checkboxes and add them..
 		for(JCheckBox b : theBoxes){
 			
 			//Get the name w/o spaces...
 			String aName = b.getText();
 			aName = aName.replaceAll("\\s", "");
 			
 			//Add the attribute to the arff file:
 			if(b.isSelected()){
 				int totalNumAttributes = (int) Math.pow(4, granularity);
 				for(int i = 0 ; i < totalNumAttributes; i++){
 					//Add an attribute for each granularity selected
 					arffFile.add("@ATTRIBUTE " + aName + i + " numeric");
 				}
 			}
 		}
 		
 		//Add the number of types (current based off of the number of Directories added.
 		String type = "{";
 		for(int i = 0; i < fragDirs.size(); i++){
 			type = type + (i+1) +",";
 		}
 		type = type.substring(0, type.length()-1);
 		type = type + "}";
 		
 		
 		arffFile.add("@ATTRIBUTE type " + type);
 		arffFile.add("");
 		
 		//Create the @data Section
 		arffFile.add("@DATA");
 		
 		return arffFile;
 	}
 	
 	/**
 	 * Calculates each of the Attributes selected
 	 * @param theBoxes - a list of JCheckBoxes containing the possible attributes.
 	 */
	public static String calcAttributes(File f, ArrayList<JCheckBox> theBoxes){
 		//Get the name of the file (key)
 		String result = new String();
 		
 		//Determine which attributes we need to use
 		for(int i = 0; i < theBoxes.size(); i++){
 			//For all the attributes selected..
 			if(theBoxes.get(i).isSelected()){
 				//Get the name of the Attribute & remove all Whitespace (for ease of equals method)
 				String name = theBoxes.get(i).getText();
 				name = name.replaceAll("\\s", "");				
 				
 				//Get the path of the file
 				Path path = Paths.get(f.getAbsolutePath());
 				
 				//Read in all the bytes of that file
 				byte[] bytes = null;
 				try {
 					bytes = Files.readAllBytes(path);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 	
 				//Determine which features were selected
 				Feature feature = null;
 				if(name.equalsIgnoreCase("TotalGrainsFeature")){
 					//Set the feature
 					feature = new TotalFeature();
 				}
 				//Same as totalFeature but in %
 //				else if (name.equalsIgnoreCase("PercentageFeature")){
 //					//Set the feature
 //					feature = new PercentageFeature();
 //				}
 				else if (name.equalsIgnoreCase("TotalAgeFeature")){
 					//Set the feature
 					feature = new TotalAgeFeature();
 				}
 				else if (name.equalsIgnoreCase("AverageAgeFeature")){
 					//Set the feature
 					feature = new AverageAgeFeature();
 				}
 				else{
 					System.out.println("Error: No Features Selected!");
 				}
 				//ADD MORE ATTRIBUTES HERE!!!!
 				//Create the matrix using the feature
				Matrix m = new Matrix(1, bytes, feature);
 				//Return the results of the feature
 				double[][] matrix = m.getMatrix();
 				
 				//Add the feature's result to the arff results
 				for(int k = 0; k < matrix.length; k++){
 					for(int j = 0; j < matrix[k].length; j++){
 						 result = result + matrix[k][j] + ",";
 						 //System.out.println(matrix[k][j]);
 					}
 				}
 			}
 		}
 		
 		return result;	
 	}
 	
 	/**
 	 * Generates and saves the Arff File to a predetermined location
 	 * @param name - the name of the arff File
 	 */
 	public static void makeArffFile(String name, ArrayList<String> arffFile){
 		try{
 			//Create a Writer, write each line out to a file.
 			System.out.println("Writing Arrf File to disk");
 			FileWriter fStream = new FileWriter("H://SVM/ARFF/" + name + ".arff");
 			BufferedWriter out = new BufferedWriter(fStream);
 			for(String s : arffFile){
 				out.write(s);
 				out.newLine();
 			}
 			out.close();
 			fStream.close();
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 		
 	}
 }
