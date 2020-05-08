 package controllers;
 
 import models.*;
 import play.mvc.Mailer;
 
 public class Mails extends Mailer {
 	
 	public static void masterApproval(User user, String masterData) {
 		setFrom("Agency <agency@cosco-ogs.com>");
 		setSubject("Approval for " + masterData);
 		addRecipient(user.email);
 		send(user);
 	}
 	
 	public static void operationalApproval(User sender, User receiver, Operational ops) {
 		setFrom(sender.email);
 		String subject = "Approval for " + ops.vessel.name + " " + ops.voyage 
 							+ " at Port " + ops.port.name;
 		setSubject(subject);
 		addRecipient(receiver.email);
 		send(receiver, ops);
 	}
 	
 	public static void operationalApproved(User sender, String receiver, Operational ops) {
 		setFrom(sender.email);
 		String subject = "Approved " + ops.vessel.name + " " + ops.voyage 
 			+ " at Port " + ops.port.name;
 		setSubject(subject);
 		addRecipient(receiver);
 		send(receiver);
 	}
 	
 	public static void operationalToOwner(String sender, String receiver, Operational operational) {
 		setFrom(sender);
		String subject = "Estimate Port Disbursement for " + operational.vessel.name + " " 
			+ operational.voyage + " at Port " + operational.port.name;
 		setSubject(subject);
 		addRecipient(receiver);
 		addCc("agency@cosco-ogs.com");
 		send(receiver, operational);
 	}
 	
 	public static void operationalRejected(User sender, User receiver, Operational ops) {
 		setFrom(sender.email);
 		String subject = "Rejected " + ops.vessel.name + " " + ops.voyage 
 			+ " at Port " + ops.port.name;
 		setSubject(subject);
 		addRecipient(receiver.email);
 		send(receiver);
 	}
 
 }
