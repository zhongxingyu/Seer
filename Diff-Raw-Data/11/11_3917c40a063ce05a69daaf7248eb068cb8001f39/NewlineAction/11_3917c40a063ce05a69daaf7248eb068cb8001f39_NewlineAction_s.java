 package kkckkc.jsourcepad.action.text;
 
 import kkckkc.jsourcepad.model.Application;
 import kkckkc.jsourcepad.model.Buffer;
 import kkckkc.jsourcepad.model.Doc;
 import kkckkc.jsourcepad.model.InsertionPoint;
 import kkckkc.jsourcepad.util.action.BaseAction;
 import kkckkc.syntaxpane.model.Interval;
 
 import java.awt.event.ActionEvent;
 
 public final class NewlineAction extends BaseAction {
     public void performAction(ActionEvent e) {
         Doc doc = Application.get().getWindowManager().getFocusedWindow().getDocList().getActiveDoc();
 
    	InsertionPoint ip = doc.getActiveBuffer().getInsertionPoint();

         Buffer buffer = doc.getActiveBuffer();
 
         boolean specialHandlingForBraces = false;
 
         if (ip.getPosition() > 0) {
             String charToTheRight = buffer.getText(Interval.createWithLength(ip.getPosition(), 1));
             String charToTheLeft = buffer.getText(Interval.createWithLength(ip.getPosition() - 1, 1));
 
             if ("{".equals(charToTheLeft) && "}".equals(charToTheRight)) {
                 specialHandlingForBraces = true;
             }
         }
 
         if (specialHandlingForBraces) {
             buffer.insertText(ip.getPosition(), "\n\n", null);
             buffer.indent(Interval.createWithLength(ip.getPosition(), 2));
             buffer.setSelection(Interval.createEmpty(buffer.getInsertionPoint().getPosition() - 1));
         } else {
             buffer.insertText(ip.getPosition(), "\n", null);
             buffer.indent(Interval.createEmpty(ip.getPosition() + 1));
         }
     }
 }
