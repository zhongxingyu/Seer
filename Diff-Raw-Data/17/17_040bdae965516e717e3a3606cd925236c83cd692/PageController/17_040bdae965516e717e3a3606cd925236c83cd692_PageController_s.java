 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 import repo.Repository;
 
 public class PageController extends Controller {
 	
 	/**
 	 * Displays the index (Main) page
 	 */
     public static void index() {
         render();
     }
     
     /**
      * Displays the search page
      */
     public static void search(){
     	List<Physician> physicians = Physician.findAll();
     	List<Patient> patients = Patient.findAll();
     	
     	render(physicians, patients);
     }
     
     /**
      * Reders the results page
      * @param exams the exams in the result
      */
     public static void results(List<Exam> exams){
     	render(exams);
     }
     
     /**
      * Searches for all exams by the given physician
     * @param id the physician's ID
      */
    public static void searchByPhysician(Long id){
    	results( Repository.searchByPhysician(id) );
     }
     
     /**
      * Searches for all exams for the given patient
     * @param id the patient's ID
      */
    public static void searchByPatient(Long id){
    	results( Repository.searchByPatient(id) );
     }
 
 }
