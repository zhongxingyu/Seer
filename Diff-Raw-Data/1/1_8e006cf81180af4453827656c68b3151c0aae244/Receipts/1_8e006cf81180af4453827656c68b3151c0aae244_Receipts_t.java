 package controllers;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import models.Comment;
 import models.Receipt;
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
 public class Receipts extends CRUD
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
 		User connectedUser = Security.connectedUser();
 		
 		System.out.println("Subpot testing!");
 		for(Subpot p : receipt.subpots)
 		{
 			System.out.println(p.total);
 			for(User u : p.members) System.out.println(u.username);
 		}
 		
 		render(receipt, connectedUser);
 	}
 	
 	
 	public static void delete(Long id)
 	{
 		if (validation.hasErrors())
 			error(Messages.get("controllers.Receipts.show.error"));
 		Receipt receipt = Receipt.findById(id);
 		
 		
 		// Check that the user is owner of receipt.
 		if (Security.isAuthorized(receipt.owner))
 		{
 			receipt.delete();
 		}
 		else
 		{
 			error(Messages.get("error"));
 		}
 	}
 	
 	// SOrt of ugly with public, but play breaks otherwise
 	public class SubroundInput
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
 	}
 	
 	public static void add(String title, int tip, List<Long> members, String description, double total, List<SubroundInput> subrounds)
 	{
 		Set<User> membersSet = new HashSet<User>();
 		
 		for (Long id : members) 
 		{
 			User u = User.findById(id);
 			membersSet.add(u);
 		}
 		
 		Receipt receipt = new Receipt(title, Security.connectedUser(), description, total);
 		receipt.tip = tip;
 		receipt.members.addAll(membersSet);
 		receipt.finished = true;
 		receipt.save();
 		
 		if(subrounds != null)
 		{
 			for(SubroundInput input : subrounds)
 			{
 				if(input.members != null)
 				{
 					input.testPrint();
 					Subpot subpot = new Subpot(input.amount);
 					subpot.description = input.description;
 					for (Long id : input.members)
 					{
 						User u = User.findById(id);
						if(!receipt.members.contains(u)) error(Messages.get("controllers.Receipts.add.subroundMemberNotMember"));
 						subpot.members.add(u);
 					}
 					subpot.receipt = receipt;
 					subpot.save();
 				}
 			}
 		}
 					
 		System.out.println("Subpot testing!");
 		for(Subpot p : receipt.subpots)
 		{
 			System.out.println(p.total);
 			for(User u : p.members) System.out.println(u.username);
 		}
 		//Receipts.details(receipt.id);
 		Application.index();
 	}
 	
 	public static void details(Long id) 
 	{
 		Receipt receipt = Receipt.findById(id);
 		render(receipt);
 	}
 
 
 	public static void register()
 	{
 		List<User> members = User.find("order by fullname asc").fetch();
 		User currentUser = Security.connectedUser();
 		render(members, currentUser);
 	}
 	
 
 }
