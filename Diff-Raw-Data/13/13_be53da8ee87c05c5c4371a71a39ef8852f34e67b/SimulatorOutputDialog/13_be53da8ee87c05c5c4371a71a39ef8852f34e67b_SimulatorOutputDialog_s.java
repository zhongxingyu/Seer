 package jDistsim.ui.dialog;
 
 import jDistsim.ui.control.button.ImageButton;
 import jDistsim.ui.panel.OutputPanel;
 import jDistsim.ui.panel.listener.OutputTabListener;
 import jDistsim.utils.resource.Resources;
 import jDistsim.utils.ui.control.IconBackgroundColorHoverStyle;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 11.3.13
  * Time: 23:19
  */
 public class SimulatorOutputDialog extends BaseDialog {
 
     private ImageButton copyToClipboardButton;
     private ImageButton scrollToEndButton;
     private ImageButton trashButton;
     private OutputTabListener listener;
     private JTextArea textArea;
 
     public SimulatorOutputDialog(JFrame parent, OutputTabListener listener, JTextArea textArea) {
         super(parent, "Simulator output");
         this.listener = listener;
         this.textArea = textArea;
     }
 
     @Override
     protected void initializeUI() {
 
     }
 
     @Override
     protected void buildWindowBody() {
         setSize(500, 500);
         gridPanel.setLayout(new BorderLayout());
         gridPanel.add(makeContent(), BorderLayout.CENTER);
     }
 
     @Override
     protected boolean okButtonLogic() {
         return true;
     }
 
     private OutputPanel makeContent() {
         final OutputPanel outputPanel = new OutputPanel(textArea);
 
         IconBackgroundColorHoverStyle buttonHoverStyle = new IconBackgroundColorHoverStyle();
         int buttonPadding = 3;
 
         copyToClipboardButton = new ImageButton(Resources.getImage("system/panels/lp_copy.png"), buttonHoverStyle, new Dimension(16, 16), buttonPadding);
         copyToClipboardButton.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent mouseEvent) {
                 listener.onCopyToClipboardButtonClick(copyToClipboardButton, mouseEvent);
             }
         });
 
         scrollToEndButton = new ImageButton(Resources.getImage("system/panels/lp_dock.png"), buttonHoverStyle, new Dimension(16, 16), buttonPadding);
         scrollToEndButton.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent mouseEvent) {
                listener.onScrollToEndButtonClick(scrollToEndButton, mouseEvent);
             }
         });
 
         trashButton = new ImageButton(Resources.getImage("system/panels/lp_trash.png"), buttonHoverStyle, new Dimension(16, 16), buttonPadding);
         trashButton.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent mouseEvent) {
                 listener.onTrashButtonClick(trashButton, mouseEvent);
             }
         });
 
         outputPanel.getLogTextArea().getControlPanel().addButton(copyToClipboardButton);
         outputPanel.getLogTextArea().getControlPanel().addButton(scrollToEndButton);
         outputPanel.getLogTextArea().getControlPanel().addButton(trashButton);
 
         return outputPanel;
     }
 }
