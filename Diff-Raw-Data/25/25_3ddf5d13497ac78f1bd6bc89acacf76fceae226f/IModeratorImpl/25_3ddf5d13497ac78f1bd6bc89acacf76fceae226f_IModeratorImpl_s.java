 package org.smartsnip.server;
 
 import org.smartsnip.core.Logging;
 import org.smartsnip.core.User;
 import org.smartsnip.shared.IModerator;
 import org.smartsnip.shared.NoAccessException;
 import org.smartsnip.shared.NotFoundException;
 import org.smartsnip.shared.XUser;
 
 public class IModeratorImpl extends GWTSessionServlet implements IModerator {
 
 	/** Serialisation ID */
 	private static final long serialVersionUID = -3154461839994229670L;
 
 	@Override
 	public boolean isModerator() {
 		Session session = getSession();
 		if (!session.isLoggedIn())
 			return false;
 		User user = session.getUser();
 		if (user == null)
 			return false;
 		return user.canModerate();
 	}
 
 	@Override
 	public void setUserState(String username, XUser.UserState state) throws NotFoundException, NoAccessException {
		if (username == null)
 			return;
 
 		Session session = getSession();
 		User user = session.getUser();
 		if (user == null || !session.isLoggedIn())
 			throw new NoAccessException();
 		if (!user.isModerator())
 			throw new NoAccessException();
 
 		User targetUser = User.getUser(username);
 		if (targetUser == null)
 			throw new NotFoundException();
 
		Logging.printInfo("MODERATOR: Setting state of user \"" + targetUser.username + "\" to " + state);
 		targetUser.setState(state);
 	}
 
 }
