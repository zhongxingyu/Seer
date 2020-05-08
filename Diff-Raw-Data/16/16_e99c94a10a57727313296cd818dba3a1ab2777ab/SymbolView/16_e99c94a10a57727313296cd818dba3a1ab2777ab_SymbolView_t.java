 package kkckkc.jsourcepad.ui.statusbar;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.ButtonGroup;
 import javax.swing.JLabel;
 import javax.swing.JPopupMenu;
 
 import com.google.common.collect.Lists;
 
 import kkckkc.jsourcepad.model.Application;
 import kkckkc.jsourcepad.model.Buffer;
 import kkckkc.jsourcepad.model.SymbolList;
 import kkckkc.jsourcepad.model.Window;
 import kkckkc.jsourcepad.model.bundle.PrefKeys;
 import kkckkc.jsourcepad.util.SedUtils;
 import kkckkc.jsourcepad.util.ui.PopupUtils;
 import kkckkc.syntaxpane.model.Interval;
 import kkckkc.syntaxpane.model.LineManager;
 import kkckkc.syntaxpane.model.Scope;
 import kkckkc.syntaxpane.model.SourceDocument;
 import kkckkc.syntaxpane.model.LineManager.Line;
 import kkckkc.syntaxpane.util.Pair;
 
 public class SymbolView extends JLabel {
 
 	public SymbolView(final Window window) {
 		setText("Function Pop-Up");
 		
 		final boolean allButtons = true;
 		addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent e) {
 				maybeShowPopup(e);
 			}
 
 			public void mouseReleased(MouseEvent e) {
 			}
 
 			private void maybeShowPopup(MouseEvent e) {
 				if (allButtons || e.isPopupTrigger()) {
 					final Buffer buffer = window.getDocList().getActiveDoc().getActiveBuffer();
 					
 					List<Pair<String, Integer>> symbols = new SymbolList(buffer).getSymbols();
 					
					if (symbols == null || symbols.isEmpty()) return;
					
 					JPopupMenu popupMenu = new JPopupMenu();
 
 					for (final Pair<String, Integer> p : symbols) {
 						popupMenu.add(new AbstractAction(p.getFirst()) {
                             public void actionPerformed(ActionEvent e) {
                             	buffer.setSelection(Interval.createEmpty(p.getSecond()));
                             }
 						});
 					}
 
 					PopupUtils.show(popupMenu, e.getPoint(), SymbolView.this);
 				}
 			}
 
 		});
 	}
 }
