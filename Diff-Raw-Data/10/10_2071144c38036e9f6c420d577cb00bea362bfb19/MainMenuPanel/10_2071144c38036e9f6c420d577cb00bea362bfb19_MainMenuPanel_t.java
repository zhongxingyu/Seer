 package net.alpha01.jwtest.panels;
 
import net.alpha01.jwtest.JWTestSession;
 import net.alpha01.jwtest.component.BookmarkablePageLinkSecure;
import net.alpha01.jwtest.pages.HomePage;
 import net.alpha01.jwtest.pages.profiles.ProfilesPage;
 import net.alpha01.jwtest.pages.project.AddProjectPage;
 import net.alpha01.jwtest.pages.project.ProjectPage;
 import net.alpha01.jwtest.pages.session.SessionsPage;
 import net.alpha01.jwtest.pages.user.UsersPage;
 
 import org.apache.wicket.authroles.authorization.strategies.role.Roles;
 import org.apache.wicket.markup.html.image.ContextImage;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.panel.Panel;
 
 public class MainMenuPanel extends Panel{
 	private static final long serialVersionUID = -4072348589046255061L;
 
 	public MainMenuPanel(String id) {
 		super(id);
		if (JWTestSession.getProject()==null){
			add(new BookmarkablePageLink<String>("projectLnk",HomePage.class).add(new ContextImage("projectImg", "images/folder.png")));	
		}else{
			add(new BookmarkablePageLink<String>("projectLnk",ProjectPage.class).add(new ContextImage("projectImg", "images/folder.png")));
		}
 		add(new BookmarkablePageLinkSecure<String>("addProjectLnk",AddProjectPage.class,Roles.ADMIN).add(new ContextImage("addProjectImg", "images/add_folder.png")));
 		add(new BookmarkablePageLinkSecure<String>("sessionsLnk",SessionsPage.class,Roles.ADMIN,"PROJECT_ADMIN","MANAGER","TESTER").add(new ContextImage("sessionsImg", "images/session.png")));		
 		add(new BookmarkablePageLinkSecure<String>("usersLnk",UsersPage.class,Roles.ADMIN).add(new ContextImage("usersImg", "images/users.png")));
 		add(new BookmarkablePageLinkSecure<String>("profilesLnk",ProfilesPage.class,Roles.ADMIN,"PROJECT_ADMIN").add(new ContextImage("profilesImg", "images/profiles.png")));
 	}
 
 }
