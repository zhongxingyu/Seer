 package controllers;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 import models.Payment;
 import models.User;
 import models.Receipt;
 import play.*;
 import play.mvc.*;
 import play.i18n.Messages;
 
 /**
  * For CRUD-interface
  * 
  * @author Peksa
  */
 @With(Secure.class) // Require login for controller access
 public class Payments extends CRUD
 {
 	public static void index() {
 		User user = Security.connectedUser();
 		if (user != null)
 		{
 			// Semi-hack to generate payments for initial data
 			List<Receipt> list = Receipt.findAll();
 			for(Receipt r : list)
 			{
 				if(r.payments.size() == 0) Payment.generatePayments(r);
 			}
 			
			List<Payment> settled = Payment.find("deprecated = false AND accepted != null AND (payer = ? OR receiver = ?)", user, user).fetch();
 			
 			// Incomming
 			List<Payment> pending = Payment.find("deprecated = false AND payer = ? AND paid != null AND accepted = null", user).fetch();
 			List<Payment> liabilities = Payment.find("deprecated = false AND payer = ? AND paid = null", user).fetch();
 			
 			// Outgoing
 			List<Payment> accept = Payment.find("deprecated = false AND receiver = ? AND paid != null AND accepted = null", user).fetch();
 			List<Payment> securities = Payment.find("deprecated = false AND receiver = ? AND paid = null", user).fetch();
 			
 			render(liabilities, pending, securities, accept, settled, user);
 		}
 		else reportToSanta();
 	}
 	
 	public static void pay(Long id)
 	{
 		validation.required(id);
 		Payment payment = Payment.findById(id);
 		if(payment == null) reportToSanta();
 		if(!validate(payment.payer.id)) return;
 		if(payment.paid != null) reportToSanta();
 		
 		if(payment.deprecated)
 		{
 			flash.error(Messages.get("error.deprecatedPayment"));
 		}
 		else
 		{
 			payment.paid = new Date();
 			payment.save();
 		}
 		
 		index();
 	}
 
 	/**
 	 * Marks a payment as accepted
 	 * @param userId
 	 * @param id
 	 */
 	public static void accept(Long id) {
 		validation.required(id);
 		Payment payment = Payment.findById(id);
 		if(payment == null) reportToSanta();
 		if(!validate(payment.receiver.id)) return;
 		if(payment.paid == null || payment.accepted != null) reportToSanta();
 
 		payment.accepted = new Date();
 		payment.save();
 
 		index();
 	}
 
 	/**
 	 * Verify that user ID is same as authorized ID and input has no errors
 	 * @param userId Id of user
 	 * @return true iff validation is successful
 	 */
 	static boolean validate(Long userId) {
 		if (validation.hasErrors()) {
 			error(Messages.get("validateFail"));
 			return false;
 		}
 
 		User user = User.findById(userId);
 		if (user == null || !Security.isAuthorized(user)) {
 			reportToSanta();
 			return false;
 		}
 
 		return true;
 	}
 	
 	static void reportToSanta()
 	{
 		error(Messages.get("controllers.Payments.validate.unauthorized"));
 	}
 }
