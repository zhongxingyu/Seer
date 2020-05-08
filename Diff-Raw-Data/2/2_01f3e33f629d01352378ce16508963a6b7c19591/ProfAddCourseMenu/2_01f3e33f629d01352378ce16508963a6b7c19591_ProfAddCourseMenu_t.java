 /**
  * 
  */
package edu.ncsu.csc.csc440.project1.menu;
 
 import java.util.Scanner;
 
 /**
  * @author Allison
  *
  */
 public class ProfAddCourseMenu{
 	
 	private String promptText = "Please enter new course information in this order: <id> <coursename> <startdate> <enddate> <your_prof_id> \n All dates should be in the format mm/dd/yyyy";
 	
 	public boolean run(){
 			String answer = promptUser(promptText);
 			
 			Scanner scan = new Scanner(answer);
 			String cid = scan.next();
 			String cname = scan.next();
 			String start = scan.next();
 			String end = scan.next();
 			String profid = scan.next();
 			
 			// TODO do stuff with the input to prepare it for INSERT:
 			//must convert start & end into oracle TIMESTAMP format
 			
 			
 		return false;
 	}
 	
 	public String promptUser(String prompt) {
         System.out.print(prompt);
 		Scanner scan = new Scanner(System.in);
 		return scan.nextLine();
 	}
 	
 }
