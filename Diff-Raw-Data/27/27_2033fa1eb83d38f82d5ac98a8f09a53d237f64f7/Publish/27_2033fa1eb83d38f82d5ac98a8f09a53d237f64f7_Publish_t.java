 /*
  * Jan 10, 2006
  */
 package com.thinkparity.ophelia.browser.platform.action.container;
 
 import java.util.Iterator;
 import java.util.List;
 
 import com.thinkparity.codebase.model.contact.Contact;
 import com.thinkparity.codebase.model.profile.Profile;
 
 import com.thinkparity.ophelia.browser.application.browser.Browser;
 import com.thinkparity.ophelia.browser.platform.action.AbstractAction;
 import com.thinkparity.ophelia.browser.platform.action.ActionId;
 import com.thinkparity.ophelia.browser.platform.action.Data;
 import com.thinkparity.ophelia.model.profile.ProfileModel;
 import com.thinkparity.ophelia.model.user.TeamMember;
 
 /**
  * Publish a document.  This will send a given document version to
  * every member of the team.
  *
  * @author raykroeker@gmail.com
  * @version 1.1
  */
 public class Publish extends AbstractAction {
 
 	/** @see java.io.Serializable */
 	private static final long serialVersionUID = 1;
 
     /** The browser application. */
     private final Browser browser;
 
 	/** Create Publish. */
 	public Publish(final Browser browser) {
 		super(ActionId.CONTAINER_PUBLISH);
         this.browser = browser;
 	}
 
 	/**
 	 * @see com.thinkparity.ophelia.browser.platform.action.AbstractAction#invoke(com.thinkparity.ophelia.browser.platform.action.Data)
 	 * 
 	 */
 	public void invoke(final Data data) {
 		final Long containerId = (Long) data.get(DataKey.CONTAINER_ID);
 		if (browser.confirm("Publish.Temp")) {
             final Profile profile = ProfileModel.getModel().read();
             final List<Contact> contacts = getContactModel().read();
             final List<TeamMember> teamMembers = getContainerModel().readTeam(containerId);
             Contact contact;
             for (final Iterator<Contact> iContact = contacts.iterator(); iContact.hasNext(); ) {
                 contact = iContact.next();
                 for (final TeamMember teamMember : teamMembers) {
                     if (teamMember.getId().equals(contact.getId())) {
                         iContact.remove();
                     }
                 }
             }
             TeamMember teamMember;
             for (final Iterator<TeamMember> iTeamMember = teamMembers.iterator(); iTeamMember.hasNext(); ) {
                 teamMember = iTeamMember.next();
                 if (teamMember.getId().equals(profile.getId())) {
                     iTeamMember.remove();
                 }
             }
 
 		    getContainerModel().publish(containerId, contacts, teamMembers);
 		    getArtifactModel().applyFlagSeen(containerId);
         }
 	}
 
 	/**
 	 * The key used to set\get the data.
 	 * 
 	 * @see Data
 	 */
 	public enum DataKey { CONTAINER_ID }
 }
