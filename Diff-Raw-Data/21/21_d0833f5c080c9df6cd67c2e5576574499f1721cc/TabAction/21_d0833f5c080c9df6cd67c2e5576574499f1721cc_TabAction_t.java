 package kkckkc.jsourcepad.action.text;
 
 import kkckkc.jsourcepad.model.Application;
import kkckkc.jsourcepad.model.Buffer;
 import kkckkc.jsourcepad.model.Doc;
 import kkckkc.jsourcepad.model.InsertionPoint;
 import kkckkc.jsourcepad.model.bundle.BundleItemSupplier;
 import kkckkc.jsourcepad.model.bundle.BundleMenuProvider;
 import kkckkc.jsourcepad.model.bundle.snippet.SnippetUtils;
 import kkckkc.jsourcepad.util.action.ActionGroup;
 import kkckkc.jsourcepad.util.action.BaseAction;
 import kkckkc.jsourcepad.util.action.MenuFactory;
 import kkckkc.syntaxpane.model.Interval;
 import kkckkc.syntaxpane.model.Scope;
 
 import javax.swing.*;
import javax.swing.text.BadLocationException;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.util.Collection;
 
 public final class TabAction extends BaseAction {
     public void performAction(final ActionEvent e) {
         final Doc doc = Application.get().getWindowManager().getFocusedWindow().getDocList().getActiveDoc();
 
     	final InsertionPoint ip = doc.getActiveBuffer().getInsertionPoint();
 
         if (doc.getActiveBuffer().getCurrentLine() == null) {
             insertTab(doc, ip);
             return;
         }
 
     	String s = doc.getActiveBuffer().getCurrentLine().getText();
     	
     	final String token = SnippetUtils.getSnippet(s);
     	
 		Scope scope = ip.getScope();
 		
 		Collection<BundleItemSupplier> items = Application.get().getBundleManager().getItemsForTabTrigger(token, scope);
 		if (items.isEmpty()) {
             insertTab(doc, ip);
         } else {
 			final ActionGroup tempActionGroup = new ActionGroup();
 
             Action firstAction = null;
 			for (BundleItemSupplier r : items) {
                 final Action delegate = BundleMenuProvider.getActionForItem(r.getUUID());
                 if (firstAction == null) firstAction = delegate;
 				tempActionGroup.add(new AbstractAction((String) delegate.getValue(Action.NAME)) {
 
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         doc.getActiveBuffer().remove(new Interval(ip.getPosition() - token.length(), ip.getPosition()));
                         delegate.actionPerformed(e);
                     }
                 });
 			}
 			
 			if (tempActionGroup.size() > 1) {
 				
 				EventQueue.invokeLater(new Runnable() {
                     public void run() {
         				JPopupMenu jpm = new MenuFactory().buildPopup(tempActionGroup, null);

                        Buffer buffer = doc.getActiveBuffer();
                        Point point;
                        try {
                            point = buffer.getTextComponent().modelToView(buffer.getInsertionPoint().getPosition()).getLocation();
                            point.translate(10, 0);
                        } catch (BadLocationException e1) {
                            point = MouseInfo.getPointerInfo().getLocation();

                            Point componentPosition = ((Component) e.getSource()).getLocationOnScreen();
                            point.translate(- (int) componentPosition.getX(), - (int) componentPosition.getY());
                        }
 
                         jpm.show((Component) e.getSource(), point.x, point.y);
                     }
 				});
 				
 			} else {
 				doc.getActiveBuffer().remove(new Interval(ip.getPosition() - token.length(), ip.getPosition()));
 				firstAction.actionPerformed(new ActionEvent(e.getSource(), 1, null));
 				
 			}
 		}
 		
 	}
 
     private void insertTab(Doc doc, InsertionPoint ip) {
         doc.getActiveBuffer().insertText(ip.getPosition(), doc.getTabManager().createIndent(1), null);
     }
 }
