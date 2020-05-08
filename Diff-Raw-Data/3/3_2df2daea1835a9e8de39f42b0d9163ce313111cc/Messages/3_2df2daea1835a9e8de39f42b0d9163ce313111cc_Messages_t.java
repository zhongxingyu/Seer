 package net.sf.anathema.character.attributes.view;
 
 import org.eclipse.osgi.util.NLS;
 
 public class Messages extends NLS {
   private static final String BUNDLE_NAME = "net.sf.anathema.character.attributes.view.messages"; //$NON-NLS-1$
   public static String MarkBonusPointsAction_Tooltip;
   static {
     NLS.initializeMessages(BUNDLE_NAME, Messages.class);
   }
 
   private Messages() {
    // nothing to do
   }
 }
