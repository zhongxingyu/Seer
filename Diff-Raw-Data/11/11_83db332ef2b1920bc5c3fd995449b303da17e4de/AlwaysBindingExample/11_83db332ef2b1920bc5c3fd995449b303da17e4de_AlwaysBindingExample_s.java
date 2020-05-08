 //   Copyright 2011 Palantir Technologies
 //
 //   Licensed under the Apache License, Version 2.0 (the "License");
 //   you may not use this file except in compliance with the License.
 //   You may obtain a copy of the License at
 // 
 //       http://www.apache.org/licenses/LICENSE-2.0
 // 
 //   Unless required by applicable law or agreed to in writing, software
 //   distributed under the License is distributed on an "AS IS" BASIS,
 //   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   
 //   See the License for the specific language governing permissions and
 //   limitations under the License.
 package alwaysBindingDemo;
 
 import java.util.Calendar;
 import java.util.Date;
 
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 import java.lang.reflect.InvocationTargetException;
 
 import javax.swing.BorderFactory;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import com.google.common.base.Strings;
 import com.palantir.ptoss.cinch.core.Bindable;
 import com.palantir.ptoss.cinch.core.Bindings;
 import com.palantir.ptoss.cinch.core.DefaultBindableModel;
 import com.palantir.ptoss.cinch.example.Examples;
 import com.palantir.ptoss.cinch.swing.Action;
 import com.palantir.ptoss.cinch.swing.Bound;
 import com.palantir.ptoss.cinch.swing.EnabledIf;
 import com.palantir.ptoss.cinch.swing.OnChange;
 
 public class AlwaysBindingExample {
 
     public static class IntroModel extends DefaultBindableModel {
     	private String currentColor = "";
     	private ImageIcon currentRealColor = null;
         private int red;
         private int blue;
         private int green;
         private Date myCal = null;
         private String standardTime = "";
 
         public String getStandardTime() {
             return myCal.toString();
         }
 
         public void setStandardTime(String standardTime) {
             this.standardTime = standardTime;
             update();
         }
 
         @SuppressWarnings("deprecation")
 		public String getcurrentColor() {
         	red = (int) (255*((myCal.getHours()-1)/(11.0)));
         	green = (int) (255*((myCal.getMinutes())/59.0));
         	blue = (int) (255*((myCal.getSeconds())/59.0));
         	currentColor = red+" "+green+" "+blue;
             return currentColor;
         }
 
         public void setCurrentColor(String currentColor) {
             this.currentColor = currentColor;
             update();
         }
 
         public Icon getCurrentColorImage() {
        	myCal = Calendar.getInstance().getTime();
         	ImageIcon img = new ImageIcon();
         	BufferedImage bi = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
         	Graphics2D g = bi.createGraphics();
         	Color c = new Color(red,blue, green);
         	
         	g.setColor(c);
         	g.fill(new Rectangle(0,0,bi.getHeight(), bi.getWidth()));
         	img.setImage(bi);
         	g.dispose();
         	currentRealColor = img;
         	return img;
         }
 
 
 
         @Override
         public String toString() {
             return "[currentColor=" + currentColor + "]";
         }
     }
 
     public static class IntroController {
         private IntroModel model;
 
         public IntroController(final IntroModel model) {
             this.model = model;
         }
         
         public void updateTime() {
         	
             model.setStandardTime(model.getStandardTime());
             System.out.println(model.getStandardTime());
         }
         
         public ImageIcon getColorIcon()
         {
         	return model.currentRealColor;
         }
         
     }
 
     private final JPanel panel = new JPanel();
 
     private final Bindings bindings = AlwaysExtensionBindings.alwaysExtensionBindings();
 
     private final IntroModel model = new IntroModel();
 
     @SuppressWarnings("unused")
     @Bindable
     private final IntroController controller = new IntroController(model);
 
     @Bound(to = "currentColor")
     private final JTextField rgbField = new JTextField();
 
     @Bound(to = "standardTime")
    @Always(call = "updateTime", delay = 500)
     private final JTextField standardTimeField = new JTextField();
 
 
    @Bound(to = "currentColorImage")
    private final JLabel messageLabel = new JLabel((Icon)controller.getColorIcon());
 
     public AlwaysBindingExample() {
         initializeInterface();
         bindings.bind(this);
     }
 
     private void initializeInterface() {
         JPanel rgbPanel = new JPanel(new BorderLayout());
         rgbPanel.add(new JLabel("RGB [0-255] based on hour, minute and second"), BorderLayout.NORTH);
         rgbPanel.add(rgbField, BorderLayout.CENTER);
         rgbPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
 
         JPanel standardTimePanel = new JPanel(new BorderLayout());
         standardTimePanel.add(new JLabel("Standard Time"), BorderLayout.NORTH);
         standardTimePanel.add(standardTimeField, BorderLayout.CENTER);
         standardTimePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
         JPanel topPanel = new JPanel(new BorderLayout());
         topPanel.add(rgbPanel, BorderLayout.NORTH);
         topPanel.add(standardTimePanel, BorderLayout.SOUTH);
         
         /*
          * The following two lines are to be uncommented once the JLabel displays it's image 
          * rather than the text representation of its pointer.
          */
         
         //JPanel bottomPanel = new JPanel(new BorderLayout());
         //bottomPanel.add(messageLabel, BorderLayout.CENTER);
 
         panel.setLayout(new BorderLayout());
         panel.add(topPanel, BorderLayout.NORTH);
         
         //this line is related to the previously mentioned JLabel as well
         //panel.add(bottomPanel, BorderLayout.SOUTH);
         
         panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
         
     }
 
     public JComponent getDisplayComponent() {
         return panel;
     }
 
     public static void main(String[] args) throws InterruptedException, InvocationTargetException {
         EventQueue.invokeAndWait(new Runnable() {
             public void run() {
             	AlwaysBindingExample example = new AlwaysBindingExample();
                 JFrame frame = Examples.getFrameFor("Always Binding Example", example.panel);
                 System.out.println("crux");
                 frame.pack();
                 frame.setVisible(true);
                 
             }
         });
     }
 }
