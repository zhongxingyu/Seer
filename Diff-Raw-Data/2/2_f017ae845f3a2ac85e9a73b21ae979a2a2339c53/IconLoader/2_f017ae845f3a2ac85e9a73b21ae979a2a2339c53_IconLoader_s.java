 package de.leonhardt.sbm.gui.common.resource;
 
 import java.net.URL;
 import java.util.logging.Logger;
 
 import javax.swing.ImageIcon;
 
 import de.leonhardt.sbm.core.model.MessageConsts.Status;
 import de.leonhardt.sbm.core.model.MessageConsts.Type;
 
 //TODO refactor, rewrite
 public class IconLoader extends ResourceLoader implements IconService {
 
	private static String resPath = "/resources/images/uiIcons/%s";
 	
 	
 	public ImageIcon getLoadingAnimation() {
 		return getIcon2("load-anim.gif");
 	}
 	
 	public ImageIcon getMessageTypeIcon(Type mType) {
 		return getIcon2(mType.getIcon());
 	}
 	
 	public ImageIcon getMessageStatusIcon(Status mStatus) {
 		return getIcon2(mStatus.getIcon());
 	}
 	
 	public ImageIcon getIcon(String iconName) {		
 		if (iconName != null && iconName.length() > 0) {
 			String nIconName = normalize(iconName);
 			return getIcon2(nIconName);
 		} else {
 			return new ImageIcon();
 		}
 	}
 	
 	private ImageIcon getIcon2(String normalizedIconName) {
 		URL iconURL = getResourceURL(buildResPath(normalizedIconName));
 		if (iconURL == null) {
 			Logger.getAnonymousLogger().fine("Could not find icon '"+normalizedIconName+"' (URL null)");
 			return new ImageIcon();
 		} else {
 			return new ImageIcon(iconURL, normalizedIconName);
 		}
 	}
 	
 	/**
 	 * Formats the resource path.
 	 * 
 	 * @param iconName
 	 * @return
 	 */
 	private String buildResPath(String iconName) {
 		return String.format(resPath, iconName);
 	}
 	
 }
