 package net.sf.anathema.character.description;
 
 import org.eclipse.osgi.util.NLS;
 
 public class Messages extends NLS {
  private static final String BUNDLE_NAME = "net.sf.anathema.character.description.messages"; //$NON-NLS-1$
   public static String CharacterDescriptionEditor_Characterization;
   public static String CharacterDescriptionEditor_Concept;
   public static String CharacterDescriptionEditor_Name;
   public static String CharacterDescriptionEditor_Notes;
   public static String CharacterDescriptionEditor_Periphrasis;
   public static String CharacterDescriptionEditor_PhysicalDescription;
   public static String CharacterDescriptionEditor_Player;
   public static String CharacterDescriptionEditorInput_Description_Message;
   static {
     NLS.initializeMessages(BUNDLE_NAME, Messages.class);
   }
 
   private Messages() {
     // nothing to do
   }
 }
