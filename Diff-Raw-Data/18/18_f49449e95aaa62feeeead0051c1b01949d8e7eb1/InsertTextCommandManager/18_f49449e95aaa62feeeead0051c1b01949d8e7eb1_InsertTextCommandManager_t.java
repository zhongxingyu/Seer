 package kkckkc.jsourcepad.ui;
 
 import kkckkc.jsourcepad.command.window.InsertTextCommand;
 import kkckkc.jsourcepad.model.Window;
 import kkckkc.jsourcepad.util.action.BaseAction;
 import kkckkc.jsourcepad.util.messagebus.DispatchStrategy;
 import kkckkc.syntaxpane.ScrollableSourcePane;
 import kkckkc.utils.Pair;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.annotation.PostConstruct;
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.util.Map;
 import java.util.WeakHashMap;
 
 public class InsertTextCommandManager implements BaseAction.ActionPerformedListener {
     private Window window;
     private Map<JEditorPane, Pair<Integer, StringBuilder>> stateMap = new WeakHashMap<JEditorPane, Pair<Integer, StringBuilder>>();
     private DefaultTypedAction defaultTypedAction;
 
     public InsertTextCommandManager() {
     }
 
     @PostConstruct
     public void init() {
         window.topic(BaseAction.ActionPerformedListener.class).subscribe(DispatchStrategy.SYNC, this);
     }
 
     @Autowired
     public void setWindow(Window window) {
         this.window = window;
     }
 
     public DefaultTypedAction getDefaultTypedAction(Action action) {
         synchronized (this) {
             if (defaultTypedAction == null) {
                 defaultTypedAction = new DefaultTypedAction(action);
             }
         }
         return defaultTypedAction;
     }
 
     private Pair<Integer, StringBuilder> complete(JEditorPane editorPane) {
         Pair<Integer, StringBuilder> state = stateMap.get(editorPane);
         if (state != null && state.getSecond().length() > 0) {
             InsertTextCommand insertTextCommand = new InsertTextCommand();
             insertTextCommand.setNoExecute(true);
             insertTextCommand.setText(state.getSecond().toString());
             window.getCommandExecutor().execute(insertTextCommand);
         }
 
         state = new Pair<Integer, StringBuilder>(editorPane.getCaret().getDot(), new StringBuilder());
         stateMap.put(editorPane, state);
         return state;
     }
 
     @Override
     public void actionPerformed(BaseAction baseAction, ActionEvent e) {
        if (window.getDocList().getActiveDoc() == null) return;
         DocPresenter dp = window.getDocList().getActiveDoc().getPresenter(DocPresenter.class);
         complete(((ScrollableSourcePane) dp.getComponent()).getEditorPane());
     }
 
 
     public class DefaultTypedAction extends AbstractAction {
         private Action delegatedTo;
 
         public DefaultTypedAction(Action action) {
             this.delegatedTo = action;
         }
 
         @Override
         public void actionPerformed(ActionEvent e) {
             JEditorPane editorPane = (JEditorPane) e.getSource();
             Pair<Integer, StringBuilder> state = stateMap.get(editorPane);
             if (state == null || editorPane.getCaret().getDot() != state.getFirst()) {
                 state = complete(editorPane);
             }
 
             boolean properKeyTyped = false;
             String content = e.getActionCommand();
             int mod = e.getModifiers();
             if ((content != null) && (content.length() > 0) &&
                 ((mod & ActionEvent.ALT_MASK) == (mod & ActionEvent.CTRL_MASK))) {
                 char c = content.charAt(0);
                 if ((c >= 0x20) && (c != 0x7F)) {
                     state.getSecond().append(content);
                     properKeyTyped = true;
                 }
             }
 
             delegatedTo.actionPerformed(e);
 
             if (! properKeyTyped) {
                 return;
             }
 
             state = new Pair<Integer, StringBuilder>(state.getFirst() + 1, state.getSecond());
             stateMap.put(editorPane, state);
         }
     }
 
 }
