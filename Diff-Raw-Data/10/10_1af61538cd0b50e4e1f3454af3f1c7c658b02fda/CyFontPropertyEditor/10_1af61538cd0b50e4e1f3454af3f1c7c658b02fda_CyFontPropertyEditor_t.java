 package cytoscape.visual.ui.editors.discrete;
 
 import java.awt.Font;
 
 import com.l2fprod.common.beans.editor.FontPropertyEditor;
 import com.l2fprod.common.util.ResourceManager;
 
 import cytoscape.Cytoscape;
 import cytoscape.visual.ui.PopupFontChooser;
 
 public class CyFontPropertyEditor extends FontPropertyEditor {
 	
 	protected void selectFont() {
 		ResourceManager rm = ResourceManager.all(FontPropertyEditor.class);
 	    String title = rm.getString("FontPropertyEditor.title");
 
 	    Font font = (Font) super.getValue();
 	    
	    Font selectedFont = PopupFontChooser.showDialog(Cytoscape.getDesktop(), font);
 
 	    if (selectedFont != null) {
 	      Font oldFont = font;
 	      Font newFont = selectedFont;
 	      
 	      super.setValue(newFont);
 	      firePropertyChange(oldFont, newFont);
 	    }
 	}
 }
