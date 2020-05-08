 package org.sonar.ide.idea.editor;
 
 import com.intellij.openapi.actionSystem.AnActionEvent;
 import com.intellij.openapi.ui.popup.IconButton;
 import com.intellij.openapi.ui.popup.JBPopup;
 import com.intellij.openapi.ui.popup.JBPopupFactory;
 import com.intellij.openapi.util.IconLoader;
 import org.sonar.wsclient.services.Violation;
 
 import javax.swing.*;
 import java.util.Collection;
 
 /**
  * @author Evgeny Mandrikov
  */
 public class ViolationsTooltipPanel extends JPanel {
   public ViolationsTooltipPanel(Collection<Violation> violations) {
     setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 
     for (Violation violation : violations) {
      // TODO RuleName should be a link to rule description
      StringBuilder sb = new StringBuilder()
          .append("<html>")
          .append("<b>").append(violation.getRuleName()).append("</b>")
          .append(" : ").append(violation.getMessage())
          .append("</html>");
      JLabel label = new JLabel(sb.toString());
       label.setIcon(ViolationIcons.getPriorityIcon(violation.getPriority()));
       add(label);
     }
   }
 
   public static void showTooltipPanel(AnActionEvent event, Collection<Violation> violations) {
     ViolationsTooltipPanel panel = new ViolationsTooltipPanel(violations);
     JBPopup popup = JBPopupFactory.getInstance().createComponentPopupBuilder(panel, panel)
         .setRequestFocus(true)
         .setCancelOnClickOutside(true)
         .setCancelOnOtherWindowOpen(false)
         .setCancelButton(new IconButton("Close", IconLoader.getIcon("/actions/cross.png")))
         .setMovable(false)
         .setTitle("Violations")
         .setResizable(true)
         .setCancelKeyEnabled(true)
         .createPopup();
     popup.showInBestPositionFor(event.getDataContext());
   }
 }
