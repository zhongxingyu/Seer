 package com.bday.view;
 
 import java.util.List;
 
import javax.servlet.http.HttpSession;

 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.springframework.ui.Model;
 
 import com.bday.model.InviteModel;
 import com.bday.model.UserModel;
 import com.bday.utils.Constants;
 
 public class InviteView {
 
 	@SuppressWarnings("unchecked")
 	public static void invite(UserModel user, List<String> emails, Model model) {
 		// step 1, magic incantation
 		Session sess = ViewManager.getCurrentSession();
 		if (!sess.isOpen()) sess = ViewManager.openSession(); // safety check
 		Transaction tr = sess.beginTransaction();
 		
 		// safety check
 		if (emails == null) throw new RuntimeException("emails is not a valid list, need to list of emails");
 		
 		// step 2, for loop that list
 		for (String email : emails)
 		{
 			// get the user model associated with the given email,
 			List users = sess.createQuery("from UserModel as user where user.email=?").setString(0, email).list();
 			UserModel invitee = (UserModel) users.get(0);
 			// add the invitation to the user;
 			InviteModel invite = new InviteModel(user, user.getFirstNotNullFamily(), Constants.INVITE_MESSAGE);
 			invitee.addInvite(invite);
 			
 			sess.save(invite);
 			sess.update(invitee);
 		}
 		
 		// commit the batched request and push nothing important to the model
 		Constants.toJson(emails, model);
 		tr.commit();
 	}
 
	public static void getUserInvites(HttpSession session, Model model) {
		// step 1, magic incantation
		Session sess = ViewManager.getCurrentSession();
		if (!sess.isOpen()) sess = ViewManager.openSession(); // safety check
		Transaction tr = sess.beginTransaction();
		
		// step 2, get the user from the session
		UserModel user = (UserModel) session.getAttribute(Constants.USER);
		
		user = (UserModel) sess.merge(user);
		
		// get all the user invites
		List<InviteModel> invites = user.getInvitations();
		Constants.toJson(invites, model);
		tr.commit();
	}

 }
