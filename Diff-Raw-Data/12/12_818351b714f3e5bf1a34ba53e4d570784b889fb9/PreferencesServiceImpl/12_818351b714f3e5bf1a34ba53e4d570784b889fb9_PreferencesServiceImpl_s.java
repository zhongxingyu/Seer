 package net.cyklotron.cms.preferences.internal;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.store.ValueRequiredException;
 import org.objectledge.parameters.CompoundParameters;
 import org.objectledge.parameters.DefaultParameters;
 import org.objectledge.parameters.Parameters;
 
 import net.cyklotron.cms.preferences.PreferencesResource;
 import net.cyklotron.cms.preferences.PreferencesResourceImpl;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 
 /**
  * An implementation of <code>Preferences Service</code>.
  */
 public class PreferencesServiceImpl
     implements PreferencesService
 {
     // instance variables ////////////////////////////////////////////////////
 
     /** The system-wide preferences. */
     private PreferencesResource systemPrefs;
     
     /** The parent node of user's preferences. */
     private Resource userPrefsRoot;
 
     public PreferencesServiceImpl()
     {
     }
 
     // PreferenceService interface ///////////////////////////////////////////
 
     // preference manipulation ///////////////////////////////////////////////
 
     /**
      * Returns the preferences of a specific navigation node.
      *
      * <p>You could use <code>NavigationNodeResource.getPreferences()</code> with the
      * same effect. This method is here for completness.</p>
      *
      * <p>The returnced Parameters object is backed by the database. Any
      * changes made to it will be persistent.</p>
      *
      * @param node the navigation node.
      * @return the node preferences.
      */
     public Parameters getNodePreferences(NavigationNodeResource node)
     {
         return node.getPreferences();
     }
     
     /**
      * Returns the preferences of a specific user.
      *
      * <p>This is the officialy endorsed method of stetting persistent
      * preferences of an user in Cyklotron system.</p>
      *
      * <p>The returnced Parameters object is backed by the database. Any
      * changes made to it will be persistent.</p>
      *
      * @param subject the user.
      * @return the user's preferences.
      */
     public Parameters getUserPreferences(CoralSession coralSession, Subject subject)
     {
         Resource[] res = coralSession.getStore().
             getResource(getUserPreferencesRoot(coralSession), subject.getName());
         PreferencesResource prefs = null;
         if(res.length != 0)
         {
             prefs = (PreferencesResource)res[0];
         }
         else
         {
             try
             {
                 prefs = PreferencesResourceImpl.
                     createPreferencesResource(coralSession,
                                               subject.getName(),
                                               getUserPreferencesRoot(coralSession),
                                               new DefaultParameters());
             }
             catch(ValueRequiredException e)
             {
                 // won't happen
             }
         }
         return prefs.getPreferences();
     }
     
     /**
      * Returns the system wide default preferences.
      *
      * <p>Returns the system wide preferences. This is not related to
      * configuration settings available from Labeo ParametersService.</p>
      * 
      * <p>The returnced Parameters object is backed by the database. Any
      * changes made to it will be persistent.</p>
      *
      * @return the user's preferences.
      */
     public Parameters getSystemPreferences(CoralSession coralSession)
     {
         return getPreferencesResource(coralSession).getPreferences();
     }
     
     // effective preferences /////////////////////////////////////////////////
 
     /**
      * Returns preferences in effect for a given navigation node and user.
      *
      * <p>The effective preferences are a combination of system preferences,
      * preferences of navigation nodes that form the path between site root
      * and the selected node, and user's preferences. A preference value
      * defined further in the chain replaces any value that might have been
      * defined earlier.</p>
      *
      * <p>The returned configuration is immutable. Any attempt to modify
      * the information will result in an exception being thrown.</p>
      *
      * @param node the navigation node. 
      * @param subject the user.  
      * @return the effective preferences.
      */
     public Parameters getPreferences(CoralSession coralSession, NavigationNodeResource node,
                                         Subject subject)
     {
         List containers = new ArrayList();
        containers.add(getPreferencesResource(coralSession).getPreferences());
         containers.add(node.getPreferences());
         while(node.getParent() != null && 
               node.getParent() instanceof NavigationNodeResource)
         {
             node = (NavigationNodeResource)node.getParent();
            containers.add(0, node.getPreferences());
         }
         containers.add(getUserPreferences(coralSession, subject));
         return new CompoundParameters(containers);
     }
 
     // combining preferences /////////////////////////////////////////////////
 
     /**
      * Returns combined preferences of a node and it's ancestors.
      *
      * <p>The returned configuration is immutable. Any attempt to modify
      * the information will result in an exception being thrown.</p>
      *
      * @param node the navigation node.
      * @return the combined preferences.
      */
     public Parameters getCombinedNodePreferences(CoralSession coralSession, NavigationNodeResource node)
     {
         if(node == null)
         {
             return getPreferencesResource(coralSession).getPreferences();
         }
         List containers = new ArrayList();
         containers.add(node.getPreferences());
         while(node.getParent() != null && 
               node.getParent() instanceof NavigationNodeResource)
         {
             node = (NavigationNodeResource)node.getParent();
            containers.add(0, node.getPreferences());
         }
        containers.add(0, getPreferencesResource(coralSession).getPreferences());
         return new CompoundParameters(containers);
     }
 
     /**
      * Returns a value of a preference as defined by the node and it's ancestors
      * up to the site's root.
      *
      * @param node the node where search for value should start.
      * @param preference the preference name.
      * @return preference value, possibly undefined.
      */
     public String getNodePreferenceValue(NavigationNodeResource node, 
                                             String preference)
     {
         Parameters parameters = node.getPreferences();
         while(!parameters.isDefined(preference) && node.getParent() != null &&
               node.getParent() instanceof NavigationNodeResource)
         {
             node = (NavigationNodeResource)node.getParent();
             parameters = node.getPreferences();
         }
         return parameters.get(preference, null);
     }
 
     /**
      * Returns the most nested node in the chain between the selected node and
      * the site root that defines a value of a preference.
      *
      * @param node the node where search for value should start.
      * @param preference the preference name.
      * @return navigation node, possibly <code>null</code>.
      */
     public NavigationNodeResource getNodePreferenceOrigin(NavigationNodeResource node, 
                                                           String preference)
     {
         Parameters parameters = node.getPreferences();
         while(!parameters.isDefined(preference) && node.getParent() != null &&
               node.getParent() instanceof NavigationNodeResource)
         {
             node = (NavigationNodeResource)node.getParent();
             parameters = node.getPreferences();
         }
         return parameters.isDefined(preference) ? node : null;
     }
 
     public PreferencesResource getPreferencesResource(CoralSession coralSession)
     {
         if(systemPrefs == null)
         {
             Resource[] res = coralSession.getStore().getResourceByPath("/cms/preferences/system");
             if(res.length != 1)
             {
                 throw new Error("failed to find system preferences node");
             }
             systemPrefs = (PreferencesResource)res[0];
         }
         return systemPrefs;
     }
     
     public Resource getUserPreferencesRoot(CoralSession coralSession)
     {
         if(userPrefsRoot == null)
         {
             Resource[] res = coralSession.getStore().getResourceByPath("/cms/preferences/users");
             if(res.length != 1)
             {
                 throw new Error("failed to find user preferences root");
             }
             userPrefsRoot = res[0];
         }
         return userPrefsRoot;
     }
 }
