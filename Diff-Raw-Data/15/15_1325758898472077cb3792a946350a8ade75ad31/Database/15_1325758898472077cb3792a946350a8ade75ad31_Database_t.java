 package com.medicalmanager.controllers;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import javax.swing.JOptionPane;
 
 import com.medicalmanager.models.Patient;
 import com.medicalmanager.views.PatientView;
 
 /**
  * Functionality for the data store and data manipulation including <ul>
  * <li> Searching </li>
  * <li> Sorting </li>
  * <li> File I/O </li>
  * <li> General Persistence </li>
  *
  * @author Ben Vest
  *
  */
 public class Database {
 	public static int[] allIDs;
 	public static ArrayList<Patient> sortedList = new ArrayList<Patient>();
 	public static FileWriter fw;
 	private static File writeDirectory;
 	private static File writeFile;
 	private static int[] numbers;
 	private static int[] helper;
 	static int number;
   
 	/**
 	 * Getter for where the current database is located
 	 * 
 	 * @return The folder that corresponds to where everything
 	 * 			is being written to.
 	 */
 	public static File getWriteDirectory() {
 		return writeDirectory;
 	}
 	
 	/**
 	 * Setter for the database directory
 	 * 
 	 * @param writeDirectory The full path of the location of the folder
 	 * on the file system where everything will be written to.
 	 */
 	public static void setWriteDirectory(String writeDirectory) {
 		Database.writeDirectory = new File(writeDirectory);
 	}
 	
 	/**
 	 * Getter for the actual write file, that is the file that contains
 	 * the patient records in CSV format.
 	 * 
 	 * @return A File that is being used for current File I/O
 	 */
 	public static File getFile() {
 		return writeFile;
 	}
 	
 	/**
 	 * Sets the absolute file location by combining where the 
 	 * write directory is with the filename
 	 * 
 	 * @param fileName What you want the file to be called
 	 */
 	public static void setFile(String fileName) {
 		Database.writeFile = new File(Database.writeDirectory.getPath() + "/" + fileName);
 	}
 	
 	/**
 	 * Prepares the file for writing by creating the directory or actual file
 	 * if necessary, a newly created file or directory is entirely empty of 
 	 * any data.
 	 */
 	public static void prepareFile(){
 		try{
 			if(Database.getWriteDirectory().mkdir()){
 				System.out.println("Directory Created Name: " + getWriteDirectory());
 			}else{
 				System.out.println("Directory not created - already exists; Name: " + getWriteDirectory());
 			}
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 
 		if (!Database.getFile().exists()) {
 			try {
 				Database.getFile().createNewFile();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}	
 	}
 
 	/**
 	 * Function for writing lines to a file, the function relies on the
 	 * write file that is assigned to Database at the time of the function
 	 * call. Therefore in order to change where something is being written
 	 * you must <b> change the write file </b> furthermore you <b> must </b>
 	 * pass <b> true </b> into append or else the <b> entire file </b> will 
 	 * be rewritten with only the last line called in.
 	 * 
 	 * @param input What is being written to the file
 	 * @param append Determines whether to append or not, append is necessary
 	 * unless you are trying to rewrite the file entirely, such as what happens
 	 * when you edit a patient.
 	 * @throws IOException
 	 */
 	public static void writeToFile(String input, boolean append) throws IOException{
 		try {
 			fw = new FileWriter(Database.getFile(), append);
 			fw.write(input);
 			fw.flush();
 			fw.close();
  
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (fw != null) {
 				fw.close();
 			}
 		}
 	}	
 
 	/**
 	 * Reads the current writeFile and counts the lines
 	 * this method is faster than readLines.
 	 * 
 	 * @return The number of lines in the file.
 	 * @throws IOException
 	 */
 	public static int countLines() throws IOException {
 	    InputStream is = new BufferedInputStream(new FileInputStream(writeFile));
 	    try {
 	        byte[] c = new byte[1024];
 	        int count = 0;
 	        int readChars = 0;
 	        boolean empty = true;
 	        while ((readChars = is.read(c)) != -1) {
 	            empty = false;
 	            for (int i = 0; i < readChars; ++i) {
 	                if (c[i] == '\n')
 	                    ++count;
 	            }
 	        }
 	        return (count == 0 && !empty) ? 1 : count;
 	    } finally {
 	        is.close();
 	    }
 	}
 	
 	/**
 	 * Reads all the patients from the file and instantiates them as 
 	 * Patient Objects while updating the master array list and the
 	 * PatientList in the PatientView. <br>
 	 * Indices of a split CSV patient line of data
 	 * <ol>
 	 * <li> ID </li> <li> first name </li> <li> middle name </li>
 	 * <li> last name </li> <li> date of birth </li> <li> address </li>
 	 * <li> email </li> <li> home phone </li> <li> cell phone </li>
 	 * <li> additional information </li> <li> current medical conditions </li>
 	 * <li> tobaco usage </li> <li> alcohol consumption </li>
 	 * <li> current medications </li> <li> additionalmedical information </li>
 	 * <li> patient analysis </li> <li> Age </li> <li> Height </li>
 	 * <li> Weight </li> <li> Systolic </li> <li> Diastolic </li>
 	 * <li> BMI </li> <li> MAP </li> <li> MAP Analysis </li>
 	 * <li> BMI Analysis </li>
 	 * </ol>
 	 * 
 	 * @throws IOException
 	 */
 	public static void readAllPatientsFromFile() throws IOException{
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(writeFile));
 			String line = null;
 			String[] stringray = new String[24];
 			while ((line = br.readLine()) != null) {
 				stringray = splitPatient(line);
 				if(stringray != null) {
 					Patient readPatient = new Patient()
 						.setID(Integer.parseInt(stringray[0]))
 						.setFirstName(stringray[1])
 						.setMiddleName(stringray[2])
 						.setLastName(stringray[3])
 						.setDOB(stringray[4])
 						.setAddress(stringray[5])
 						.setEmailAddress(stringray[6])
 						.setHomePhone(stringray[7])
 						.setCellPhone(stringray[8])
 						.setAdditionalPatientInformation(stringray[9])
 						.setMedicalConditions(stringray[10])
 						.setTobaccoUsage(stringray[11])
 						.setAlcoholConsumption(stringray[12])
 						.setCurrentMedications(stringray[13])
 						.setAdditionalMedicalInformation(stringray[14])
 						.setPatientAnalysis(stringray[15])
 						.setAge(Double.parseDouble(stringray[16]))
 						.setHeight(Double.parseDouble(stringray[17]))
 						.setWeight(Double.parseDouble(stringray[18]))
 						.setSystolicBloodPressure(Double.parseDouble(stringray[19]))
 						.setDiastolicBloodPressure(Double.parseDouble(stringray[20]))
 						.setBMI(Double.parseDouble(stringray[21]))
 						.setMAP(Double.parseDouble(stringray[22]));
 					
 				
 					PatientView.updateList(readPatient);
 				}
 			}
 			
 			br.close();
 		}catch (Exception e) {
 		    e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Writes every patient in the master ArrayList to
 	 * a file, this is a helper method for when a patient is edited.
 	 * Append is called initially then set to true which allows the file
 	 * to be totally rewritten.
 	 * 
 	 * @throws IOException
 	 */
 	public static void writeAllPatientsToFile() throws IOException{
 		boolean append = false;
 		for(Patient p: PatientView.patientArray){
 			writeToFile(Patient.stringify(p), append);
 			append = true;
 		}
 	}
 	
 	/**
 	 * 
 	 * @author Ben Vest
 	 *
 	 */
 	public static class CompareStringAttribute implements Comparator<Patient> {
 		  boolean direction = false; // false is descending; true is ascending
 		  String type = "FIRST_COMP"; 
 		  
 		  public CompareStringAttribute(){}
 		  
 		  public CompareStringAttribute(boolean direction, String type){
 			  this.direction = direction;
 			  this.type = type;
 		  }
 		
 		  @Override
 		  public int compare(Patient p1, Patient p2) {
 			System.out.println("COMPARINGSTRING");
 		    String rank1 = p1.getFirstName();
 		    String rank2 = p2.getFirstName();
 		    if (rank1 == null && rank2 == null) {
 		      return 0;
 		    }
 		
 		    if (rank1 == null) {
 		      return 1;
 		    }
 		    if (rank2 == null) {
 		      return -1;
 		    }
 		
 		    return rank1.compareTo(rank2);
 		  }    
 	}
 	
 	
 	/**
 	 * @author Ben Vest
 	 */
 	public static class CompareNumberAttribute implements Comparator<Patient> {
 		private boolean direction = false; // descending;
 		private String type = "ID_COMP";
 		private double rank1;
 		private double rank2;
 		
 		public CompareNumberAttribute(){}
 		
 		public CompareNumberAttribute(boolean direction, String type){
 			this.direction = direction;
 			this.type = type;
 		}
 		
 		@Override
 		public int compare(Patient p1, Patient p2) {
 		
 			if(type.equals("ID_COMP")){
 				this.rank1 = p1.getID();
 				this.rank2 = p2.getID();
 			}else if(type.equals("AGE_COMP")){
 				this.rank1 = p1.getAge();
 				this.rank2 = p2.getAge();
 			}else if(type.equals("HEIGHT_COMP")){
 				this.rank1 = p1.getHeight();
 				this.rank2 = p2.getHeight();
 			}else if(type.equals("WEIGHT_COMP")){
 				this.rank1 = p1.getWeight();
 				this.rank2 = p2.getWeight();
 			}else{
 				System.out.println("Unrecognized type: Defaulting to ID");
 				this.rank1 = p1.getID();
 				this.rank2 = p1.getID();
 			}
 			
 			if(direction){
 				  return (rank1 > rank2 ? -1 : (rank1 == rank2 ? 0 : 1));
 			}else{
 				  return (rank1 < rank2 ? -1 : (rank1 == rank2 ? 0 : 1));
 			}
 		}
 	
 	}
 	
 	/**
 	 * Super simple integer bubble sort.
 	 * 
 	 * @param toSort Unsorted array of integers for sorting
 	 */
 	public static int[] bubbleSort(int[] toSort){
 		int n = toSort.length;
 		
 		for(int i = 0; i < n; i++){
 			for(int j = 0; j < (n-i); j++){
 				if(toSort[j-1] > toSort[j]){
 					swap(j, toSort);
 				}
 			}
 		}
 		
 		return toSort;
 	}
 	
 	/**
 	 * Swap for bubble sort. Swaps an element of an array.
 	 * 
 	 * @param index The index of the item that is being swapped
 	 * @param theArray The array that is swapping
 	 */
 	private static void swap(int index, int[] theArray){
 		int replace = theArray[index - 1];
 		theArray[index - 1] = theArray[index];
 		theArray[index] = replace;
 	}
 	
 	/**
 	 * Linear Search of patients
 	 * 
 	 * @param p ArrayList of all the patients
 	 * @param searchFor ID that is being searched for
 	 * @return The patient if one is found, else null
 	 */
 	public static Patient linSearch(ArrayList<Patient> p, int searchFor){
		for( int i = 0; i < p.size(); i++ ){
 			if(p.get(i).getID() == searchFor){
 				return p.get(i);
 			}
 		}
 		
 		return null;
 	}
 	
 	/*
 	 * Note: This is a single item lookup type of search, what I need to do from here is take the
 	 * index that I find from the search and use it to do another binary search of the names.
 	 *  Options are the search meta data
 	 *  0 -> Search Query: Name or ID
 	 *  1 -> minage-maxage
 	 *  2 -> minheight-maxheight
 	 *  3 -> minweight-maxweight
 	 *  4 -> gender
 	 *  5 -> bmiclass
 	 *  If any option is not filled in by the user it will come in as "n/a"
 	 */
 	/**
 	 * 
 	 * @param options
 	 * @return
 	 */
 	public static Patient[] advancedPatientSearch(String[] options){
 		int index;
 
 		ArrayList<Patient> listToSort = PatientView.patientArray;
 		Patient pRay[] = new Patient[listToSort.size()];
 		
 		sortPatients(listToSort, options[0], true, false);
 		
 		Patient toFind = new Patient();
 		
 		try{
 			Integer.parseInt(options[0]); // If this passes then it's a number else it goes to the catch block
 			toFind.setID(Integer.parseInt(options[0]));
 			index = Collections.binarySearch(listToSort, toFind, new CompareNumberAttribute());
 		} catch (NumberFormatException se){
 			toFind.setFirstName(options[0]);
 			index = Collections.binarySearch(listToSort, toFind, new CompareStringAttribute());
 			System.out.println(index);
 			if(index > 0){
 				pRay = checkMultiples(index, listToSort);		
 			}
 		}		
 		return pRay;
 	}
 	
 	/**
 	 * 
 	 * @param index
 	 * @param sortedList
 	 * @return
 	 */
 	public static Patient[] checkMultiples(int index, ArrayList<Patient> sortedList){
 		boolean upTrue = true;
 		boolean downTrue = true;
 		
 		Patient testAgainst = PatientView.patientArray.get(index);
 		ArrayList<Patient> multiples = new ArrayList<Patient>();
 		multiples.add(testAgainst);
 
 		int indexSorted = sortedList.indexOf(testAgainst);
 
 		Patient up;
 		Patient down;
 		
 		int x = 1;
 		while(downTrue || upTrue){
 			if(upTrue){
 				up = checkUp(indexSorted + x, testAgainst.getFirstName(), sortedList);
 				if(up != null){
 					multiples.add(up);
 					x++;
 				}else{
 					upTrue = false; 
 				}
 			}
 			
 			if(downTrue){
 				down = checkDown(indexSorted - x, testAgainst.getFirstName(), sortedList);
 				if(down != null){
 					multiples.add(down);
 					x++;
 				}else{
 					downTrue = false;
 				}
 			}
 		}
 
 		return multiples.toArray(new Patient[multiples.size()]);
 	}
 	
 	/**
 	 * 
 	 * @param upTo
 	 * @param name
 	 * @param list
 	 * @return
 	 */
 	public static Patient checkUp(int upTo, String name, ArrayList<Patient> list){
 		try{
 			if(list.get(upTo).getFirstName().equals(name)){
 				return list.get(upTo);
 			}
 		}catch(IndexOutOfBoundsException e){
 			return null;
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param downTo
 	 * @param name
 	 * @param list
 	 * @return
 	 */
 	public static Patient checkDown(int downTo, String name, ArrayList<Patient> list){
 		try{
 			if(list.get(downTo).getFirstName().equals(name)){
 				return list.get(downTo);
 			}
 		}catch(IndexOutOfBoundsException e){
 			return null;
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param p
 	 * @param searchValue
 	 * @return
 	 */
 	public static int search(ArrayList<Patient> p, int searchValue) {
 			int left = 0;
 			int right = p.size() - 1;
 			return binarySearch(p, searchValue, left, right);
 	}
  
 	/**
 	 * 
	 * 
 	 * @param p
 	 * @param searchValue
 	 * @param left
 	 * @param right
 	 * @return
 	 */
 	private static int binarySearch(ArrayList<Patient> p, int searchValue, int left, int right) {
 		if (right < left) {
 			return -1;
 		}
 		
 		int mid = (left + right) >>> 1;
 		if (searchValue > p.get(mid).getID()) {
 			return binarySearch(p, searchValue, mid + 1, right);
 		} else if (searchValue < p.get(mid).getID()) {
 			return binarySearch(p, searchValue, left, mid - 1);
 		} else {
 			return mid;
 		}               
 	} 
 	
 	/**
 	 * @param direction Ascending or Descending. true is asc, false desc
 	 * @param type Number or String; true is number, false string
 	 * @param pRay The patient array list
 	 * @param option The way the list will be sorted. Options are attribute_COMP, i.e. ID_COMP, FIRST_COMP, LAST_COMP, etc
 	 * @return A sorted array list of type patient
 	 */
 	public static ArrayList<Patient> sortPatients(ArrayList<Patient> pRay, String option, boolean direction, boolean type){
 			try{
 				if(type){
 					Collections.sort(pRay, new CompareNumberAttribute(direction, option));
 					return pRay;
 				}else{
 					Collections.sort(pRay, new CompareStringAttribute(direction, option));
 					return pRay;
 				}
 			} catch (Exception ae){
 				PatientView.showError("Problem Sorting List!", "Sort Error");
 			}
 			return pRay;
 	}
 	
 	/**
 	 * Sort an array of Integers.
 	 * 
 	 * @param values Array of Integers to be sorted
 	 */
 	public static void sort(int[] values) {
 		number = values.length;
 		helper = new int[number];
 		mergesort(0, number - 1);
 	}
 	
 	/**
 	 * Recursively sorts an array by essentially splitting the array up and
 	 * then merging it together.
 	 * 
 	 * @param low The smallest size of the array being sorted
 	 * @param high The max size of the array being sorted
 	 */
 	public static void mergesort(int low, int high) {
 		if (low < high) {
 			int middle = low + (high - low) / 2;
 			mergesort(low, middle);
 			mergesort(middle + 1, high);
 			merge(low, middle, high);
 		}
 	}
 		
 	/**
 	 * Merge portion of the mergesort routine.
 	 * 
 	 * @param low Low number
 	 * @param middle Number in the middle
 	 * @param high High number
 	 */
 	  public static void merge(int low, int middle, int high) {
 		  // Copy both parts into the helper array
 		  for (int i = low; i <= high; i++) {
 			  helper[i] = numbers[i];
 		  }
 	
 		  int i = low;
 		  int j = middle + 1;
 		  int k = low;
 	
 		  // Copy the smallest values from either the left or the right side back
 		  // to the original array
 		  while (i <= middle && j <= high) {
 			  if (helper[i] <= helper[j]) {
 				  numbers[k] = helper[i];
 				  i++;
 			  } else {
 				  numbers[k] = helper[j];
 				  j++;
 			  }
 			  k++;
 		  }
 		  
 		  // Copy the rest of the left side of the array into the target array
 		  while (i <= middle) {
 			  numbers[k] = helper[i];
 			  k++;
 			  i++;
 		  }
 	  }
 	  
 	  /**
 	   * Gets all of the userIDs in a patient ArrayList and puts them into the 
 	   * array allIDs of integers.
 	   * 
 	   * @param p ArrayList of all the patients that IDs need to be extracted from
 	   */
 	  public static void getAllIDs(ArrayList<Patient> p){
 		  allIDs = new int[p.size()];
 
 		  for(int x = 0; x < p.size(); x++){
 			  int anID = p.get(x).getID();
 			  allIDs[x] = anID;
 		  }
 	  }
 	
 	/**
 	 * Changes the patient object according to edits 
 	 * made in the EditPatientDialog by assigning the data passed in  
 	 * it then calls a helper function inside of the PatientView in   
 	 * order to update the PatientList which reflects the edits to    
 	 * the user. pRay is used to find the index of the patient at the 
 	 * time of the edit.    
 	 *                                  
 	 * @param p Patient that is being updated 
 	 * @param data array of data for updating the patient
 	 * @param pRay ArrayList of all the patients in memory. 
 	 */
 	public static void updatePatient(Patient p, String[] data, ArrayList<Patient> pRay){
 		int editIndex = pRay.indexOf(p);
 		p.setFirstName(data[0]);
 		PatientView.updateListAfterPatientEdit(editIndex, p);
 		try {
 			Database.writeAllPatientsToFile();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Takes a string in CSV format i.e. blah,                   
 	 * blah, blah and returns an array of the items split                     
 	 * similarly to the way the split function works in                       
 	 * Java.
 	 *                            
 	 * @param input A csv line passed in to be split
 	 * strings must be be encapsulated by single quotes ''
 	 * @return An Array of Strings
 	 */
 	public static String[] splitPatient(String input){
 		if(input.length() > 0){
 			int startIndex = input.indexOf(",");
 			int finalIndex = input.lastIndexOf(",");
 			int index = startIndex;
 	
 			int start = -1;
 			int count = 2; // accounts for the first and last word
 	
 			String word;
 	
 			// find size of the patients
 			while(index < finalIndex){
 				index = input.indexOf(",", index + 1);
 				count++;
 			}
 			
 			// create an array that is the size of the number of patient fields
 			String[] words = new String[count]; 
 			index = startIndex; //reset backs to the start
 	
 			// actually split the words
 			// return an array of them
 			for(int i = 0; i < count; i++){
 				if(index == finalIndex && count == 2 && i == 1){
 					word = input.substring(start+1, finalIndex);
 				}else if(index < finalIndex){
 					word = input.substring(start+1, index);
 					start = input.indexOf(",", index);
 					index = input.indexOf(",", index+1);
 				}else{
 					word = input.substring(finalIndex + 1);
 				}
 				words[i] = word.trim();
 			}
 			return words;
 		} else {
 			return null;
 		}
 	}
 }
