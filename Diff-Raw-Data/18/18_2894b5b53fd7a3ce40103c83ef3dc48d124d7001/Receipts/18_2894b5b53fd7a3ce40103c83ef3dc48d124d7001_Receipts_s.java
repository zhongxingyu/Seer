 package controllers;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import models.Comment;
 import models.Payment;
 import models.Receipt;
 import models.ReceiptOwner;
 import models.Subpot;
 import models.User;
 import play.*;
 import play.data.validation.Required;
 import play.i18n.Messages;
 import play.mvc.*;
 
 /**
  * For CRUD-interface
  * 
  * @author Peksa
  */
 @With(Secure.class) // Require login for controller access
 public class Receipts extends Controller
 {
 	/**
 	 * Show a receipt
 	 * 
 	 * @param id of receipt
 	 */
 	public static void show(Long id)
 	{
 		if (validation.hasErrors())
 			error(Messages.get("controllers.Receipts.show.error"));
 		Receipt receipt = Receipt.findById(id);
 		User user = Security.connectedUser();
 		
 		if (!receipt.members.contains(user))
		{
 			error(Messages.get("controllers.Payments.validate.unauthorized"));
 		}
 
 		render(receipt, user);
 	}
 
 	/**
 	 * Removes the current user from the receipt
 	 * @param id
 	 */
 	public static void removeMe(Long id)
 	{
 		if (validation.hasErrors())
 			error(Messages.get("controllers.Receipts.show.error"));
 		
 		Receipt receipt = Receipt.findById(id);
 		User user = Security.connectedUser();
 		if (!receipt.members.contains(Security.connectedUser())) {
 			error(Messages.get("You are not a member of this receipt"));
 		} else if (receipt.hasPaymentsDone()){
 			error(Messages.get("Payments has been done on this recipt, cannot edit"));
 		} else {
 			removeUser(receipt, user);
 			Application.index(false);
 		}
 		
 	}
 	
 	private static void removeUser(Receipt receipt, User user) {
 		// Remove receipt from all active payments
 		for(Payment payment : receipt.payments) {
 			if(!payment.deprecated && 
 				(payment.payer.equals(user) || payment.receiver.equals(user))) {
 				Payment fixedPayment = new Payment(payment, receipt, false);
 				fixedPayment.save();
 			}
 		}
 		
 		// Remove user from all subrounds
 		for(Subpot pot : receipt.subpots) {
 			pot.members.remove(user);
 		}
		receipt.owners.remove(user);
 		receipt.members.remove(user);
 		receipt.save();
 	}
 
 	public static void delete(Long id)
 	{
 		if (validation.hasErrors())
 			error(Messages.get("controllers.Receipts.show.error"));
 		Receipt receipt = Receipt.findById(id);
 
 		if (!Security.isAuthorized(receipt.creator)) {
 			error(Messages.get("Must be owner"));
 		} else if (receipt.hasPaymentsDone()){
 			error(Messages.get("Payments has been done on this recipt, cannot delete"));
 		} else {
 			Set<User> remove = new HashSet<>(receipt.members);
 			for(User user : remove) removeUser(receipt, user);
 			receipt.flagAsDeleted();
 			receipt.save();
 			Application.index(false);
 		}
 	}
 	
 	public static void edit(Long id)
 	{
 		if (validation.hasErrors())
 			error(Messages.get("controllers.Receipts.show.error"));
 		
 		Receipt receipt = Receipt.findById(id);
 		if (!Security.isAuthorized(receipt.creator)) {
 			error(Messages.get("Must be owner"));
 		} else if (receipt.hasPaymentsDone()){
 			error(Messages.get("Payments has been done on this recipt, cannot edit"));
 		} else {
 			render();
 		}
 	}
 
 	// Sort of ugly with public, but play breaks otherwise
 	public static class SubroundInput
 	{
 		public ArrayList<Long> members;
 		public String description;
 		public double amount;
 		public boolean everyoneExcept;
 		public boolean together;
 
 		public void testPrint()
 		{
 			System.out.println("Subround:");
 			for(Long s : members) System.out.println(s.toString());
 					System.out.println(description);
 					System.out.println(amount);
 		}
 		
 		public String validate(List<Long> members)
 		{
 			// Empty subrounds not counted
 			if(this.members == null) return null;
 			
 			for (Long id : this.members)
 			{
 				if(!members.contains(id))
 				{
 					return Messages.get("controllers.Receipts.add.subroundMemberNotMember");
 				}
 				if(description == null || description.length() == 0) return "Subround lacks description";
 				if(amount <= 1e-8) return "Subround amount requred (and must be positive)";
 			}
 			return null;
 		}
 		
 	}
 
 	public static void add(String title, int tip, List<Long> members, String description, double total, List<SubroundInput> subrounds, String paid)
 	{
 		User creator = Security.connectedUser();
 		boolean creatorIsMember = false;
 		for(Long id : members)
 		{
 			if(id.equals(creator.id)) creatorIsMember = true;
 		}
 		
 		// Hack to make adding of debts where you spent nothing simpler
 		// this must be done in "i paid everything"-mode right now
 		if(paid.equals("creator") && !creatorIsMember)
 		{
 			double subTotal = 0;
 			for (SubroundInput subround : subrounds) subTotal += subround.amount;
 			
 			// Add everything to a subround with just the old members
 			if(subTotal < total)
 			{
 				SubroundInput restRound = new SubroundInput();
 				restRound.members = new ArrayList<Long>();
 				restRound.members.addAll(members);
 				restRound.amount = total - subTotal;
 				restRound.description = "Other";
 				subrounds.add(restRound);
 			}
 			
 			members.add(creator.id);
 			creatorIsMember = true;
 		}
 		
 		// validate
 		String errorStr = null;
 		if(title == null || title.length() == 0) errorStr = "Title requred";
 		else if(members.size() == 0) errorStr = "Members requred";
 		else if(total <= 1e-8) errorStr = "Total requred (and must be positive)";
 		else if(!creatorIsMember) errorStr = "Creator must also be a member";
 		else
 		{
 			double subTotal = 0;
 			for (SubroundInput subround : subrounds)
 			{
 				subTotal += subround.amount;
 				errorStr = subround.validate(members);
 				if(errorStr != null) break; // break on first error
 			}
 			if(subTotal > total) errorStr = "Subround amount grater than total";
 			
 		}
 		if(errorStr != null)
 		{
 			error(errorStr);
 			return;
 		}
 		
 		Set<User> membersSet = new HashSet<User>();
 
 		for (Long id : members) 
 		{
 			User u = User.findById(id);
 			membersSet.add(u);
 		}
 
 		Receipt receipt = new Receipt(title, Security.connectedUser(), description);
 		receipt.tip = tip;
 		receipt.members.addAll(membersSet);
 		
 		if(paid.equals("split"))
 		{
 			double eachShare = total / receipt.members.size();
 			for(User u : receipt.members)
 			{
 				receipt.owners.add(new ReceiptOwner(receipt, u, eachShare));
 			}
 		}
 		else // Creator payed for everything
 		{
 			receipt.owners.add(new ReceiptOwner(receipt, receipt.creator, total));
 		}
 		
 		//receipt.save();
 		if(subrounds != null)
 		{
 			for(SubroundInput input : subrounds)
 			{
 				if(input.members != null)
 				{
 					Subpot subpot = new Subpot(input.amount);
 					subpot.description = input.description;
 					for (Long id : input.members)
 					{
 						User u = User.findById(id);
 						subpot.members.add(u);
 					}
 					// Why the fuck do i have to do both?
 					receipt.subpots.add(subpot);
 					subpot.receipt = receipt;
 				}
 			}
 		}
 		receipt.save();
 		
 		Payment.generatePayments(receipt);
 		Receipts.show(receipt.id);
 	}
 
 	public static void details(Long id) 
 	{
 		Receipt receipt = Receipt.findById(id);
 		render(receipt);
 	}
 
 
 	public static void register()
 	{
 		List<User> members = User.find("order by fullname asc").fetch();
 		User user = Security.connectedUser();
 		render(members, user);
 	}
 }
