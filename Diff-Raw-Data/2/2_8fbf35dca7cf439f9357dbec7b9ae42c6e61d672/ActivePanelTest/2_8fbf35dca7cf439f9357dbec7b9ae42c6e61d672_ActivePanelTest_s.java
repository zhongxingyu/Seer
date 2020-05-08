 /**
  * swing-revival:
  * Swing Revival Toolkit
  *
  * Copyright (c) 2009 by Alistair A. Israel.
  *
  * This software is made available under the terms of the MIT License.
  * See LICENSE.txt.
  *
  * Created Sep 30, 2009
  */
 package swing.revival;
 
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 
 import junit.extensions.abbot.ComponentTestFixture;
 
 import org.junit.Test;
 
 import swing.revival.annotations.Component;
 import swing.revival.annotations.Font;
 
 /**
  *
  * @author Alistair A. Israel
  */
 public final class ActivePanelTest extends ComponentTestFixture {
 
     /**
      *
      * @author Alistair A. Israel
      */
     @SuppressWarnings("serial")
     @Font(name = "Tahoma")
     public static final class MyPanel extends ActivePanel {
 
         private JLabel field1Label;
 
         @Component
         private JTextField field1TextField;
 
     }
 
     /**
      * @throws Exception
      *         on exception
      */
     @Test
     public void testMyPanel() throws Exception {
         final MyPanel myPanel = new MyPanel();
         final JLabel field1Label = myPanel.field1Label;
         assertNotNull("field1Label is null!", field1Label);
         assertEquals("field1Label", field1Label.getName());
         assertEquals("Tahoma", field1Label.getFont().getName());
 
         final JTextField field1TextField = myPanel.field1TextField;
         assertNotNull("field1TextField is null!", field1TextField);
         assertEquals("field1TextField", field1TextField.getName());
        assertEquals("field1TextField tooltip text", field1TextField.getToolTipText());
         assertEquals("Tahoma", field1TextField.getFont().getName());
         assertSame("field1Label is not label for field1TextField!", field1TextField,
                 field1Label.getLabelFor());
         showFrame(myPanel);
     }
 
 }
