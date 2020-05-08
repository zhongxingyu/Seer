 package devhood.im.sim.ui.presenter;
 
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 
 import devhood.im.sim.ui.util.SmileyFactory;
 import devhood.im.sim.ui.util.UiUtil;
 import devhood.im.sim.ui.view.SmileyView;
 
 @Named
 public class SmileyPanelPresenter {
 
 	@Inject
 	private SmileyView view;
 
 	@Inject
 	private SmileyFactory smileyFactory;
 
 	private MouseListener smileyIconMouseListener;
 
 	private Map<String, String[]> smileyShortcutPathMap = new HashMap<String, String[]>();
 
 	private boolean initialized;
 
 	public void show(JComponent relativeTo) {
 		if (!initialized) {
 			initPanel();
 			view.pack();
 		}
 
 		view.setVisible(true);
 		view.setLocationRelativeTo(relativeTo);
 	}
 
 	public void initPanel() {
 		initialized = true;
 		List<String> sortedKeyList = initSmileys();
 
 		for (String key : sortedKeyList) {
 
 			String smileyCode = key.replace("&gt;", ">").replace("&lt;", "<")
 					.replace("&amp;", "&").replace("&quot;", "\"");
 			ImageIcon img = UiUtil
 					.createImageIcon(
 							"/images/yahoo_smileys/"
 									+ smileyFactory.getSmileys().get(
 											smileyShortcutPathMap.get(key)),
 							smileyCode);
 			JLabel smileyLabel = new JLabel(img);
 			smileyLabel.setToolTipText(smileyCode);
 			smileyLabel.addMouseListener(smileyIconMouseListener);
 
 			view.addSmiley(smileyLabel);
 		}
 
 		view.revalidate();
 
 	}
 
 	public List<String> initSmileys() {
 		Set<String[]> keySet = smileyFactory.getSmileys().keySet();
 		List<String> keyListForSorting = new ArrayList<String>();
 		for (String[] keys : keySet) {
 			smileyShortcutPathMap.put(keys[0], keys);
 			keyListForSorting.add(keys[0]);
 		}
 
 		Collections.sort(keyListForSorting);
 
 		return keyListForSorting;
 	}
 
 	public MouseListener getSmileyIconMouseListener() {
 		return smileyIconMouseListener;
 	}
 
 	public void setSmileyIconMouseListener(MouseListener smileyIconMouseListener) {
 		this.smileyIconMouseListener = smileyIconMouseListener;
 	}
 
 	public void hide() {
 		view.dispose();
 
 	}
 }
