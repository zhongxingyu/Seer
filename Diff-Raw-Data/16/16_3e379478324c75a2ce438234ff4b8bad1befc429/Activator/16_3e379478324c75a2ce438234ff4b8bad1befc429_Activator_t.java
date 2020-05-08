 package net.bioclipse.brunn.ui;
 
 import java.io.IOException;
 import java.net.URL;
 
 import net.bioclipse.brunn.Springcontact;
 import net.bioclipse.brunn.business.audit.IAuditManager;
 import net.bioclipse.brunn.pojos.User;
 import net.bioclipse.usermanager.IUserManagerListener;
 import net.bioclipse.usermanager.UserManagerEvent;
 import net.bioclipse.usermanager.business.IUserManager;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class Activator extends AbstractUIPlugin implements IUserManagerListener {
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "net.bioclipse.brunn.ui";
 
 	// The shared instance
 	private static Activator plugin;
 	
 	//The currently logged in user
 	private User currentUser;
 	
 	/**
 	 * The constructor
 	 */
 	public Activator() {
 		plugin = this;
 		net.bioclipse.usermanager.Activator
 		   .getDefault().getUserManager().addListener(this);
 	}
 
 	private void setLoggedInUser() {
 		System.out.println("Activator.setLoggedInUser()");
 		IUserManager userManager = net.bioclipse.usermanager.Activator.getDefault().getUserManager();
         if ( userManager.getAccountIdsByAccountTypeName( "BrunnAccountType" )
                         .size() > 1 ) {
             throw new IllegalStateException("Found multiple brunn accounts for user. This version of Brunn doesn't support that");
         }
         String accountId = userManager.getAccountIdsByAccountTypeName( "BrunnAccountType" ).get( 0 );
         
 		for ( User user : ( (IAuditManager)Springcontact.getBean("auditManager") ).getAllUsers() ) {

 			if( user.getName().equals( userManager.getProperty( accountId, "Brunn user" )  ) ) {
 				if( user.passwordMatch( userManager.getProperty( accountId, "Brunn password" ) ) ) {
 					currentUser = user;
 					System.out.println(currentUser.getName() + " has logged in");
 					return;
 				}
 			}
 		}
 		throw new IllegalStateException("Did not find user in database or password did not match");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 		plugin = null;
 		super.stop(context);
 	}
 
 	/**
 	 * Returns the shared instance
 	 *
 	 * @return the shared instance
 	 */
 	public static Activator getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * Returns an image descriptor for the image file at the given
 	 * plug-in relative path
 	 *
 	 * @param path the path
 	 * @return the image descriptor
 	 */
 	public static ImageDescriptor getImageDescriptor(String path) {
 		return imageDescriptorFromPlugin(PLUGIN_ID, path);
 	}
 	
 	public static URL getPluginURL() {
 		try {
 	        return FileLocator.toFileURL(Platform.getBundle(PLUGIN_ID).getEntry("/"));
         }
         catch (IOException e) {
 	        // TODO Auto-generated catch block
 	        e.printStackTrace();
         }
         return null;
 	}
 
 	public User getCurrentUser() {
 		return currentUser;
 	}
 
 	public void setCurrentUser(User currentUser) {
 		this.currentUser = currentUser;
 	}
 
     public void receiveUserManagerEvent( UserManagerEvent event ) {
         switch (event) {
             case LOGIN:
               if( net.bioclipse.usermanager.Activator.getDefault()
                      .getUserManager()
                      .isLoggedInWithAccountType("BrunnAccountType") ) {
                 setLoggedInUser();
               }
               /*
                * A bit of a hack to instantiate the platemanager at login
                */
               Springcontact.getBean( "plateManager" );
               break;
             case LOGOUT:
               currentUser = null;
               break;
             case UPDATE:
               Springcontact.CONTEXT = null;
             default:
               break;
         }    
     }
 }
