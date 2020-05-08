 package objects;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Collection;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Random;
 import java.math.BigDecimal;
 /**
  * Object representing an assignment for a course
  * 
  * @author Jesse W. Milburn
  * @date 01 October, 2013
  */
 public class Assignment {
     private String name;
     private int worth;
     private Map<String, Integer> grades = new HashMap<String, Integer>();
     private ArrayList<Integer> studentGrades = new ArrayList<Integer>();
     public Assignment() {
     	
     }
     /**
      * Creates an Assignment object
      * 
      * @param an	Name of the assignment
      * @param val	Maximum point value of the assignment
      */
     public Assignment(String an, int val) {
         name = an;
         worth = val;
     }
     
     /**
      * Sets a name to the name field
      * 
      * @param an	Sets the name of the assignment
      */
     public void setName(String an) {
     	name = an;    	
     }
     
     /**
      * Sets the maximum point value for the assignment
      * 
      * @param val	Maximum point value of the assignment
      */
     public void setWorth(int val) {
     	worth = val;
     }
     
     /**
      * Fetches the average grade for one assignment for students only
      * does not ascertain the ghost students scores at all
      * 
      * @param students	List of Student objects
      * @return			the average grade for the assignment
      */
     public double getAssignmentAverageGrade(List<Student> students){
     	double studentPoints = getTotalStudentPoints(students);
     	double maximumPoints = getMaximumPoints(students);
     	return ((studentPoints / maximumPoints) * 100);
     }
 
     /**
      * Fetches the average point value for one assignment for students only
      * does not ascertain the ghost students scores at all
      * 
      * @param students	List of Student objects
      * @return			the average grade for the assignment
      */
     public double getAssignmentAveragePoints(List<Student> students){
     	double studentPoints = getTotalStudentPoints(students);
     	return (studentPoints / students.size());
     }
 
     public int getTotalStudentPoints(List<Student> students) {
     	int total = 0;
     	for (int i = 0; i < students.size(); i++) {
     		if (grades.get(students.get(i).getPseudoName()) != null) 
     			total += grades.get(students.get(i).getPseudoName());
     	}
     	return total;
     }
     public int getMaximumPoints(List<Student> students) {
     	return (worth * students.size());
     }
     
     /**
      * Fetches the name field of the assignment
      * 
      * @return		The title of the assignment
      */
     public String getName() {
         return name;
     }
     
     /**
      * Fetches the maximum point value for the assignment
      * 
      * @return		Maximum point value of the assignment
      */
     public int getWorth() {
         return worth;
     }
     
     /**
      * Sets the grade for the student by the students pseudo name
      * 
      * @param pseudoName	The pseudo name of the student
      * @param grade			The score the student is being assigned for the assignment
      * @return				The previous value associated to the key.
      */
     public Integer setGrade(String pseudoName, int grade) {
     	grades.put(pseudoName, grade);
     	if (grades.containsKey(pseudoName)) studentGrades.add(grade);
     	return grades.get(pseudoName);
     }
     
     /**
      * Gets the grade for the requested student
      * 
      * @param pseudoName	The pseudo name of the student
      * @return				Integer value representing the students grade. Returns
      * 						null if the mapping is empty, or if the pseudo name 
      * 						does not exist as a key.
      */
     public Integer getGrade(String pseudoName) {
         if(grades.get(pseudoName) != null)
             return grades.get(pseudoName);
         else
             return 0;
     }
     
     /**
      * Gets a collection of all the values in the grades HashMap
      * Useful for dealing with statistical analysis
      * 
      * @return		Collection of values in the grades HashMap
      */
     public Collection<Integer> getAllGrades() {
     	return grades.values();
     }
     public void setGhostGrades(String[] ghostNames) {
     	int[] ghostGrades = new int[ghostNames.length];
     	assignGhostGrades(ghostGrades);
    	for (int i = 0; i < ghostNames.length; i++) {
    		this.setGrade(ghostNames[i], ghostGrades[i]);
    	}
     }
     private void assignGhostGrades(int[] ghostGrades) {
     	int numberOfGhosts = ghostGrades.length;
     	int numberOfStudents = studentGrades.size();
     	int startIndex = 0;
         int endIndex= 1;
         
         int[] numberOfScoresInRanges = new int[numberOfRanges(worth)];//this needs error handling
         int[][] ranges = defineRanges(worth);
         numberOfScoresInRanges = numberOfScoresInRanges(studentGrades, ranges, numberOfScoresInRanges.length);
         
         double studentsMean = mean(studentGrades);
         Random rng = new Random();
         
         int offset = 0;//tracks the number of scores assigned
         for (int i = 0; i < ranges.length; i++) {//wrap entire loop in a do while or call outside functions
             double percentage = (double)numberOfScoresInRanges[i] / (double)numberOfStudents;
             int amountOfGhosts = (int)Math.round(numberOfGhosts * percentage);
             int randomMultiplier = ranges[i][endIndex] - ranges[i][startIndex];
             
             //inserts scores into the ghostGrades array by percentages of students scoring in ranges
             if (percentage > 0.0 && randomMultiplier == 0) {
                 for (int j = 0; j < amountOfGhosts; j++) {
                     ghostGrades[j] = randomMultiplier;
                 }
             } else if (percentage > 0.0) {
                 for (int j = 0; j < amountOfGhosts; j++) {
                     if (offset + j == ghostGrades.length) break;//if percentages assigned more than allowed break out
                     ghostGrades[j + offset] = rng.nextInt(randomMultiplier + 1) + ranges[i][startIndex];
                 }
             }
             
             offset += amountOfGhosts;
             
             //checks at end of foor loop for end case #2 having one fewer score assigned from percentage mismatch
             if (offset < numberOfGhosts && i == ranges.length - 1) {
                 ghostGrades[numberOfGhosts - 1] = (int)studentsMean;//cheap fix doesn't verify a student
                 //has a grade in the median range
             }
         }
         
         //if means do not match this adjust the grades one by one until means match
         adjustGhostGradesToMean(ghostGrades, studentsMean, worth);
         
         //randomize ghost grades in array so not in order
         shuffleArray(ghostGrades);
     }
     
     private void adjustGhostGradesToMean(int[] ghostGrades, double mean, int worth) {
         Random rng = new Random();
         int precision = 1;
         
         int lowestIncrementableScore = lowestIncrementableScore(ghostGrades);
         double studentsMean = round(mean, precision, BigDecimal.ROUND_HALF_UP);
         double ghostsMean = round(mean(ghostGrades), precision, BigDecimal.ROUND_HALF_UP);
         while (studentsMean != ghostsMean) {
         	int findScoreIndex = rng.nextInt(ghostGrades.length);
             if (studentsMean < ghostsMean 
                 && ghostGrades[findScoreIndex] > studentsMean) {
                 ghostGrades[findScoreIndex]--;
                 ghostsMean = round(mean(ghostGrades), precision, BigDecimal.ROUND_HALF_UP);
             } else if (studentsMean > ghostsMean 
                        && ghostGrades[findScoreIndex] >= lowestIncrementableScore 
                        && ghostGrades[findScoreIndex] < worth) {
                 ghostGrades[findScoreIndex]++;
                 ghostsMean = round(mean(ghostGrades), precision, BigDecimal.ROUND_HALF_UP);
             }
         }
     }
     private int lowestIncrementableScore(int[] ghostGrades) {
         int lowestNonZero = Integer.MAX_VALUE;
         for (int i = 0; i < ghostGrades.length; i++) {
             if (ghostGrades[i] < lowestNonZero && ghostGrades[i] != 0) lowestNonZero = ghostGrades[i];
         }
         return lowestNonZero;
     }
     private double round(double unrounded, int precision, int roundingMode) {
         String number = Double.toString(unrounded);
         BigDecimal bd = new BigDecimal(number);
         BigDecimal rounded = bd.setScale(1, roundingMode);
         return rounded.doubleValue();
     }
     
     
     
     private void shuffleArray(int[] array) {
         Random rng = new Random();
         for (int i = 0; i < array.length; i++) {
             int index = rng.nextInt(i + 1);
             int temp = array[index];
             array[index] = array[i];
             array[i] = temp;
         }
     }
     
     private int[] numberOfScoresInRanges(ArrayList<Integer> studentsGrades, int[][] ranges, int size) {
     	int[] numberOfScoresInRanges = new int[size];
         for (int i = 0; i < studentsGrades.size(); i++) {
             for (int j = 0; j < ranges.length; j++) {
             	if (studentsGrades.get(i) >= ranges[j][0] && studentsGrades.get(i) <= ranges[j][1]) {
                 	numberOfScoresInRanges[j] ++;
                     break;//if added breaks out of inner loop
                 }
             }
         }
         
         return numberOfScoresInRanges;
     }
     
     private int[][] defineRanges(int worth) {
         int numberOfRanges = numberOfRanges(worth);
         int[][] ranges = new int[numberOfRanges][2];
                 
         //set fields for setting ranges algorithm
         int start = 0;//index 0 holds the start of the range
         int end = 1;//index 1 holds the end of the range
         int baseRange = worth / (numberOfRanges - 1);//the first range is 0 - 0
         int numberOfRangesWithMoreThanBase = worth % (numberOfRanges - 1);//first range is 0 - 0
         int startingRangeWithMore = numberOfRanges - numberOfRangesWithMoreThanBase;
         int current = 1;
         for (int i = 1; i < ranges.length; i++) {
             ranges[i][start] = current;
             current += (baseRange - 1);
             ranges[i][end] = current;
             if (i >= startingRangeWithMore) {
                 current ++;
                 ranges[i][end] = current;
             }
             current++;
         }
         
         return ranges;
     }
     
     
     private int numberOfRanges(int worth) {
         int [] RANGES = {-1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10};
         if (worth > 18) return 11;//maximum number of ranges
         else if (worth <= 0) return -1;//or throw an error
         return RANGES[worth];
     }
     
     public double mean(ArrayList<Integer> values) {
         double temp = 0.0;
         for (int i = 0; i < values.size(); i++) {
             temp += values.get(i);
         }
         return temp / values.size();
     }
     public double mean(int[] values) {
         double temp = 0.0;
         for (int i = 0; i < values.length; i++) {
             temp += values[i];
         }
         return temp / values.length;
     }
         
         
     	
     	
     	
     	
 }
     
     
     
     
     
     
     
     
     
     
     
     
 
