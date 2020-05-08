 package org.synyx.minos.skillz.web;
 
 import static org.synyx.minos.skillz.SkillzPermissions.SKILLZ_ADMINISTRATION;
 import static org.synyx.minos.skillz.SkillzPermissions.SKILLZ_USER;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.synyx.minos.core.authentication.AuthenticationService;
 import org.synyx.minos.core.web.menu.AbstractMenuItemProvider;
 import org.synyx.minos.core.web.menu.MenuItem;
import org.synyx.minos.core.web.menu.UrlResolver;
import org.synyx.minos.core.web.menu.UserPlaceholderAwareUrlResolver;
 
 
 /**
  * {@link org.synyx.minos.core.web.menu.MenuItemProvider} for Skillz module.
  * 
  * @author Oliver Gierke - gierke@synyx.de
  */
 public class SkillzMenuItemProvider extends AbstractMenuItemProvider {
 
     @Autowired
     private AuthenticationService authenticationService;
 
     private static final String MENU_SKILLZ = "MENU_SKILLZ";
     private static final String MENU_SKILLZ_PRIVATEPROJECTS = "MENU_SKILLZ_PRIVATEPROJECTS";
     private static final String MENU_SKILLZ_RESUME = "MENU_SKILLZ_RESUME";
     private static final String MENU_SKILLZ_SKILLZ = "MENU_SKILLZ_SKILLZ";
     private static final String MENU_SKILLZ_RESUMES_MANAGE = "MENU_SKILLZ_RESUMES_MANAGE";
 
 
     /*
      * (non-Javadoc)
      * 
      * @see com.synyx.minos.core.web.menu.AbstractMenuItemProvider#initMenuItems()
      */
     @Override
     protected List<MenuItem> initMenuItems() {
 
         MenuItem manageResumes =
                 MenuItem.create(MENU_SKILLZ_RESUMES_MANAGE).withKeyBase("skillz.menu.manageResumes").withPosition(10)
                         .withUrl("/skillz/resumes").withPermission(SKILLZ_ADMINISTRATION).build();
 
         MenuItem skillz =
                 MenuItem.create(MENU_SKILLZ_SKILLZ).withKeyBase("skillz.menu.skillz").withPosition(20).withUrl(
                         "/skillz").withPermission(SKILLZ_ADMINISTRATION).build();
 
         MenuItem resume =
                 MenuItem.create(MENU_SKILLZ_RESUME).withKeyBase("skillz.menu.resume").withPosition(40).withUrl(
                         "/skillz/resume").withPermission(SKILLZ_USER).build();
 
        UrlResolver privateProjectsStrategy =
                new UserPlaceholderAwareUrlResolver(String.format("/skillz/projects/%s",
                        UserPlaceholderAwareUrlResolver.DEFAULT_PLACEHOLDER), authenticationService);
         MenuItem privateProjects =
                 MenuItem.create(MENU_SKILLZ_PRIVATEPROJECTS).withKeyBase("skillz.menu.projects.private").withPosition(
                         50).withUrlResolver(privateProjectsStrategy).withPermission(SKILLZ_USER).build();
 
         MenuItem item =
                 MenuItem.create(MENU_SKILLZ).withKeyBase("skillz.menu").withPosition(20).withUrl("/skillz")
                         .withPermission(SKILLZ_USER).withSubmenues(manageResumes, skillz, resume, privateProjects)
                         .build();
 
         return Arrays.asList(item);
 
     }
 }
