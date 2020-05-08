 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 
 public class Application extends Controller {
 
     public static void index() {
         render();
     }
     
     public static void listByRecipientDonarYear(String recipient, String donar, int year) {
     	if (recipient.isEmpty() && donar.isEmpty()) {
     		flash.error("both recipient and donar cannot be empty");
     		index();
 		}
     	List <CandidateContributions> cc = null;
     	if (!recipient.isEmpty() && !donar.isEmpty())
     		cc = CandidateContributions.findByRecipientDonar(recipient, donar, year);
     	else if (!recipient.isEmpty()) {
     		cc = CandidateContributions.findByRecipient(recipient, year);
     	} 
     	else {
    		cc = CandidateContributions.findByDonor(donar, year);
     	}
     	render(cc);
     }
 
 }
