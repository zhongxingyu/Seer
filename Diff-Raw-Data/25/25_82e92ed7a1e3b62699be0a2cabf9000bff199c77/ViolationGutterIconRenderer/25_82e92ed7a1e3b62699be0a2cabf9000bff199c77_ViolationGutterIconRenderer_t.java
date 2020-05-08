 package org.sonar.ide.idea.editor;
 
 import com.intellij.openapi.actionSystem.AnAction;
 import com.intellij.openapi.actionSystem.AnActionEvent;
 import com.intellij.openapi.editor.markup.GutterIconRenderer;
 import org.jetbrains.annotations.NotNull;
 import org.sonar.ide.shared.ViolationUtils;
 import org.sonar.wsclient.services.Violation;
 
 import javax.swing.*;
 import java.awt.*;
 import java.util.List;
 
 /**
  * @author Evgeny Mandrikov
  */
 public class ViolationGutterIconRenderer extends GutterIconRenderer {
   /**
    * Violations sorted by priority in descending order.
    */
   private final List<Violation> violations;
 
   private final String description;
 
   public ViolationGutterIconRenderer(List<Violation> violations) {
     this.violations = ViolationUtils.sortByPriority(violations);
     this.description = getDescription(this.violations);
   }
 
   @NotNull
   @Override
   public Icon getIcon() {
     return ViolationIcons.VIOLATION_ICON;
   }
 
   @Override
   public String getTooltipText() {
     return description;
   }
 
   @Override
   public AnAction getClickAction() {
     return new AnAction() {
       @Override
       public void actionPerformed(AnActionEvent event) {
         ViolationsTooltipPanel.showTooltipPanel(event, violations);
       }
     };
   }
 
   protected static String getDescription(List<Violation> violations) {
     StringBuilder sb = new StringBuilder();
     for (int i = 0; i < violations.size(); i++) {
       Violation violation = violations.get(i);
       if (i != 0) {
         sb.append("<hr/>");
       }
       sb.append("<b>").append(violation.getRuleName()).append("</b>");
       sb.append("<br/>").append(violation.getMessage());
     }
     return sb.toString();
   }
 
  public Color getErrorStripeMarkColor() {
     return ViolationIcons.getPriorityColor(violations.get(0).getPriority());
   }
 }
