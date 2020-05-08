 package kkckkc.jsourcepad.ui.statusbar;
 
import javax.swing.JLabel;
import javax.swing.JPopupMenu;

 import kkckkc.jsourcepad.model.Buffer;
 import kkckkc.jsourcepad.model.Doc;
 import kkckkc.jsourcepad.model.DocList;
 import kkckkc.jsourcepad.model.Window;
 import kkckkc.jsourcepad.util.action.MenuFactory;
 import kkckkc.jsourcepad.util.messagebus.DispatchStrategy;
 import kkckkc.jsourcepad.util.ui.PopupUtils;
 import kkckkc.syntaxpane.parse.grammar.Language;
 
 public class LanguageView extends JLabel implements DocList.Listener, Buffer.LanguageListener {
 
 	public LanguageView(Window window) {
 		setText("--");
 		setEnabled(false);
 		
 		window.topic(DocList.Listener.class).subscribe(DispatchStrategy.ASYNC_EVENT, this);
 		window.topic(Buffer.LanguageListener.class).subscribe(DispatchStrategy.ASYNC_EVENT, this);
 		
 		MenuFactory menuFactory = new MenuFactory();
 		JPopupMenu jpm = menuFactory.buildPopup(window.getActionManager().getActionGroup("language-menu"), null);
 		
 		PopupUtils.bind(jpm, this, true);
 	}
 
 	@Override
     public void closed(int index, Doc doc) {
     }
 
 	@Override
     public void created(Doc doc) {
     }
 
 	@Override
     public void selected(int index, Doc doc) {
 		Language l = doc.getActiveBuffer().getLanguage();
 		setEnabled(true);
 		setText(l.getName());
     }
 
 	@Override
     public void languageModified(Buffer buffer) {
 		Language l = buffer.getLanguage();
 		setEnabled(true);
 		setText(l.getName());
     }
 }
