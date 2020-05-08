 package kkckkc.jsourcepad.action.text;
 
 import kkckkc.jsourcepad.model.Application;
 import kkckkc.jsourcepad.model.Doc;
 import kkckkc.jsourcepad.model.InsertionPoint;
 import kkckkc.jsourcepad.model.bundle.BundleItemSupplier;
 import kkckkc.jsourcepad.model.bundle.BundleMenuProvider;
 import kkckkc.jsourcepad.model.bundle.snippet.SnippetUtils;
 import kkckkc.jsourcepad.util.action.ActionGroup;
 import kkckkc.jsourcepad.util.action.MenuFactory;
 import kkckkc.syntaxpane.model.Interval;
 import kkckkc.syntaxpane.model.Scope;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.util.Collection;
 
 public final class TabAction extends AbstractAction {
     private final Doc doc;
 
     public TabAction(Doc doc) {
 	    this.doc = doc;
     }
 
     public void actionPerformed(final ActionEvent e) {
     	InsertionPoint ip = doc.getActiveBuffer().getInsertionPoint();

        if (doc.getActiveBuffer().getCurrentLine() == null) {
            insertTab(ip);
            return;
        }

    	String s = doc.getActiveBuffer().getCurrentLine().getText();
     	
     	String token = SnippetUtils.getSnippet(s);
     	
 		Scope scope = ip.getScope();
 		
 		Collection<BundleItemSupplier> items = Application.get().getBundleManager().getItemsForTabTrigger(token, scope);
 		if (items.isEmpty()) {
            insertTab(ip);
        } else {
 			final ActionGroup tempActionGroup = new ActionGroup();
 			
 			for (BundleItemSupplier r : items) {
 				tempActionGroup.add(BundleMenuProvider.getActionForItem(r.getUUID()));
 			}
 			
 			if (tempActionGroup.size() > 1) {
 				
 				EventQueue.invokeLater(new Runnable() {
                     public void run() {
         				JPopupMenu jpm = new MenuFactory().buildPopup(tempActionGroup, null);
         				Point point = MouseInfo.getPointerInfo().getLocation();
         				
         				Point componentPosition = ((Component) e.getSource()).getLocationOnScreen();
         				
         				point.translate(- (int) componentPosition.getX(), - (int) componentPosition.getY());
 
                         jpm.show((Component) e.getSource(), point.x, point.y);
                     }
 				});
 				
 			} else {
 				doc.getActiveBuffer().remove(new Interval(ip.getPosition() - token.length(), ip.getPosition()));
 				tempActionGroup.getItems().get(0).actionPerformed(
 						new ActionEvent(e.getSource(), 1, null));
 				
 			}
 		}
 		
 	}

    private void insertTab(InsertionPoint ip) {
        doc.getActiveBuffer().insertText(ip.getPosition(), doc.getTabManager().createIndent(1), null);
    }
 }
