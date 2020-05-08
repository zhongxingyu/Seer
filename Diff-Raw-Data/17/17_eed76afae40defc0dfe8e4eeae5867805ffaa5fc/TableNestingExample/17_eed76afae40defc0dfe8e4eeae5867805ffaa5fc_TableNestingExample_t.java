 // $Id$
 package wingset;
 
 import org.wings.SComboBox;
 import org.wings.SComponent;
import org.wings.SForm;
 import org.wings.SGridLayout;
 import org.wings.SLabel;
import org.wings.SPanel;
 import org.wings.border.SEmptyBorder;
import org.wings.script.JavaScriptListener;
 import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
 
 /**
  * @author <a href="mailto:B.Schmid@eXXcellent.de">Benjamin Schmid</a>
  */
 public class TableNestingExample  extends WingSetPane{
 
     private final SComboBox selectComboBox = new SComboBox();
     private final SForm mainPanel = new SForm(new SGridLayout(1));
     private Color[] colors = new Color[] { Color.red, Color.green, Color.yellow};
 
     protected SComponent createExample() {
        selectComboBox.addScriptListener(JavaScriptListener.JS_ON_CHANGE_SUBMIT_FORM);
         for(int i = 5; i< 30; i++) {
             selectComboBox.addItem(new Integer(i));
         }
         selectComboBox.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 update();
             }
         });
         selectComboBox.setSelectedIndex(0);
         update();
         return mainPanel;
     }
 
     private void update() {
         mainPanel.removeAll();
         mainPanel.add(new SLabel("Below you will see panels with GridLayout nested into panels.\n" +
                 "You will realize that the browsers stop rendering at a specific level of table nesting.\n" +
                 "(in IE try 9 vs. 10; in FireFox 20 vs. 21"));
         mainPanel.add(selectComboBox);
         mainPanel.add(nestPanel(((Integer)selectComboBox.getSelectedItem()).intValue()));
     }
 
     private SPanel nestPanel(int depth) {
         SPanel panel = new SPanel(new SGridLayout());
         panel.add(new SLabel ("Depth "+depth));
         panel.setBackground(colors[depth % colors.length]);
         panel.setBorder(new SEmptyBorder(5,5,5,5));
         if (depth > 1)
             panel.add(nestPanel(depth-1));
         return panel;
     }
 }
