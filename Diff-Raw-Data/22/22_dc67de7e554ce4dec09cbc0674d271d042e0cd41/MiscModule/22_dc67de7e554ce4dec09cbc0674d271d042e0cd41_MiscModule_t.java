 package ho.module.misc;
 
 import java.awt.event.KeyEvent;
 
 import ho.core.model.HOVerwaltung;
 import ho.core.module.DefaultModule;
 
 import javax.swing.JPanel;
 import javax.swing.KeyStroke;
 
 
 public final class MiscModule extends DefaultModule {
 	
 	public MiscModule(){
 		super(true);
 	}
 	
 	@Override
 	public int getModuleId() {
 		return MISC;
 	}
 
 	@Override
 	public String getDescription() {
 		return HOVerwaltung.instance().getLanguageString("Verschiedenes");
 	}
 
 	@Override
 	public JPanel createTabPanel() {
 		return new InformationsPanel();
 	}
 
 	public void addMenus() {
 		// TODO Auto-generated method stub
 
 	}
 
	@Override
 	public KeyStroke getKeyStroke() {
 		return KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0);
 	}
 
 }
