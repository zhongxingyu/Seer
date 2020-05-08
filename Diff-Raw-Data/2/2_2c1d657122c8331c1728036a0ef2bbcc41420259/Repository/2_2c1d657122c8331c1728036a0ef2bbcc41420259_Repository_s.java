 package repo;
 
 import java.util.*;
 import org.apache.commons.lang.time.DateUtils;
 import models.*;
 
 /**
  * Class that contains many general functions for the system.
  */
 public class Repository {
 	
 	/**
 	 * Checks if the given username/password is valid
 	 * @param username the username
 	 * @param password the password
 	 * @return true if the username/password are valid
 	 */
 	public static boolean login(String username, String password){
 		User tmp = User.find("byUsernameAndPassword", username, encodePassword(password)).first();
 		return (tmp != null);
 	}
 	
 	/**
 	 * Searches for exams within the given dates.<br>
 	 * If last is before first, they are swapped and the search is still run.
 	 * @param first the beginning of the date range
 	 * @param last the end of the test range
 	 * @return a list of all the results
 	 */
 	public static List<Exam> searchByDate(Date first, Date last){
 		//Check for null
 		if( first == null || last == null){
 			return new ArrayList<Exam>(0);
 		}
 		
 		//Make sure that last is either the same day or after the first date
 		if(!last.after(first) && DateUtils.isSameDay(first, last)){
 			//Swap them, so the search still works
 			Date temp = first;
 			first = last;
 			last = temp;
 		}
 		
 		List<Exam> exams = Exam.findAll();
 		List<Exam> toRet = new ArrayList<Exam>(exams.size());
 		
 		//Manually compare them all
 		for(Exam cur: exams){
			if(cur.getDate().equals(first) || (cur.getDate().after(first)
 					&& (cur.getDate().before(last) || DateUtils.isSameDay(cur.getDate(), last)))){
 				toRet.add(cur);
 			}
 		}
 		
 		return toRet;
 	}
 	
 	/**
 	 * Gets all the exams for a given patient
 	 * @param patientId the patient's ID
 	 * @return a list of all their exams
 	 */
 	public static List<Exam> searchByPatient(Long patientId){
 		Patient patient = User.findById(patientId);
 		if(patient != null){
 			return patient.getExams();
 		} else {
 			return new ArrayList<Exam>(0);
 		}
 	}
 	
 	/**
 	 * Gets all the exams by a given physician
 	 * @param physicianId the physician's ID
 	 * @return a list of all their exams
 	 */
 	public static List<Exam> searchByPhysician(Long physicianId){
 		Physician physician = Physician.findById(physicianId);
 		if(physician != null){
 			return physician.getExams();
 		} else {
 			return new ArrayList<Exam>(0);
 		}
 	}
 	
 	/**
 	 * Encodes the given string
 	 * @param plaintext the string to encode
 	 * @return the encoded string
 	 */
 	public static String encodePassword(String plaintext){
 		return play.libs.Crypto.encryptAES(plaintext);
 	}
 	
 	/**
 	 * Decodes the given string
 	 * @param encoded the encoded string
 	 * @return the decoded string
 	 */
 	public static String decodePassword(String encoded){
 		return play.libs.Crypto.decryptAES(encoded);
 	}
 	
 }
