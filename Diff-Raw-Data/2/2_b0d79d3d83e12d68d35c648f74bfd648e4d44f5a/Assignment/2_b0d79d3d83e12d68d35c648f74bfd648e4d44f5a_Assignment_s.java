 package objects;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Collection;
 import java.util.List;
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
     	return ((studentPoints / maximumPoints) * 100); //FIXME get rid of the magic number or format at an upper layer
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
     	return grades.put(pseudoName, grade);
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
         if(grades.get(psuedoName) != null)
             return grades.get(pseudoName);
         else
            return 0.0;
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
 }
