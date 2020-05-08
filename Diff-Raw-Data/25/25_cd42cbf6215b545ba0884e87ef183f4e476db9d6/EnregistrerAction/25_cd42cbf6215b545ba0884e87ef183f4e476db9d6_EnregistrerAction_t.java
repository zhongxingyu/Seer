 package core;
 
 import java.awt.Component;
 
 import modele.Image;
 
 public class EnregistrerAction extends AbstractCoreAction {
 	private static final long serialVersionUID = 1L;
 	protected static final String FICHIER_SAVE = "app.frame.menus.file.save";
 	Component parent;
 	
 	public EnregistrerAction(Component comp) {
 		super(ApplicationSupport.getResource(FICHIER_SAVE));
 		parent = comp;
 	}
 	
 	@Override
 	public void executeAction() {
 		Serializer.getInstance().serialize(Image.getInstance().getFilename());
 		
 	}
 }
