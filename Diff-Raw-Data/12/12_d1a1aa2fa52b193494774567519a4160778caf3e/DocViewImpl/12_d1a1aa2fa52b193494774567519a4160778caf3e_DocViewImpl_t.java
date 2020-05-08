 package kkckkc.jsourcepad.ui;
 
 import java.awt.Font;
 
 import javax.swing.text.PlainDocument;
 
 import kkckkc.jsourcepad.model.Application;
 import kkckkc.syntaxpane.ScrollableSourcePane;
 import kkckkc.syntaxpane.style.StyleScheme;
 
 public class DocViewImpl implements DocView {
 
 	private ScrollableSourcePane sourcePane;
 
 	public DocViewImpl() {
 		sourcePane = createScrollableSource();
 	}
 	
 	@Override
     public ScrollableSourcePane getComponent() {
 	    return sourcePane;
     }
 
 	protected ScrollableSourcePane createScrollableSource() {
 	    return new ScrollableSourcePane(Application.get().getLanguageManager());
     }
 
 	@Override
     public void updateTabSize(int tabSize) {
 		sourcePane.getDocument().putProperty(PlainDocument.tabSizeAttribute, tabSize);
     }
 
 	@Override
     public void redraw() {
 		// "Clone" font to force full redraw / recalculation
 		Font f = sourcePane.getFont();
 		f = f.deriveFont(f.getStyle());
 		sourcePane.getEditorPane().setFont(f);
     }
 
 	@Override
     public void setStyleScheme(StyleScheme styleScheme) {
 		sourcePane.setStyleScheme(styleScheme);
     }
 
 }
