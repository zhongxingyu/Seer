 package net.sf.anathema.basics.repository.view.internal;
 
 import org.eclipse.osgi.util.NLS;
 
 public class Messages extends NLS {
  private static final String BUNDLE_NAME = "net.sf.anathema.basics.repository.view.messages"; //$NON-NLS-1$
   public static String RepositoryView_InitializeDndErrorMessage;
   public static String RepositoryView_OpenEditorErrorMessage;
   public static String TreeViewRefresher_JobName;
 
   static {
     NLS.initializeMessages(BUNDLE_NAME, Messages.class);
   }
 
   private Messages() {
     // nothing to do
   }
 }
