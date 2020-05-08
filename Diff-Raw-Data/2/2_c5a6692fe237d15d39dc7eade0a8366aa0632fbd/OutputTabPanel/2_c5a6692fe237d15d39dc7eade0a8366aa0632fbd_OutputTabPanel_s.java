 package jDistsim.ui.panel.tab;
 
 import jDistsim.ui.control.button.ImageButton;
 import jDistsim.ui.panel.EnvironmentPanel;
 import jDistsim.ui.panel.OutputPanel;
 import jDistsim.ui.panel.container.LightContainer;
 import jDistsim.ui.panel.listener.OutputTabListener;
 import jDistsim.utils.resource.Resources;
 import jDistsim.utils.ui.ListenerablePanel;
 import jDistsim.utils.ui.control.IconBackgroundColorHoverStyle;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 19.2.13
  * Time: 0:40
  */
 public class OutputTabPanel extends ListenerablePanel<OutputTabListener> {
 
     private ImageButton copyToClipboardButton;
     private ImageButton scrollToEndButton;
     private ImageButton trashButton;
     private ImageButton mainStatisticsButton;
     private ImageButton remoteStatisticsButton;
     private ImageButton moduleStatisticsButton;
     private ImageButton otherStatisticsButton;
     private JTextArea textArea;
 
     private LightContainer outputContainer;
 
     public OutputTabPanel(JTextArea textArea) {
         this.textArea = textArea;
         initialize();
     }
 
     private void initialize() {
         setLayout(new BorderLayout());
         setBorder(new EmptyBorder(5, 5, 5, 5));
 
         LightContainer environmentContainer = makeEnvironment();
         environmentContainer.setPreferredSize(new Dimension(190, getHeight()));
 
         outputContainer = makeSimulatorOutput();
 
 
         JPanel contentPane = new JPanel(new BorderLayout());
         contentPane.setOpaque(false);
         contentPane.setBorder(new EmptyBorder(0, 0, 0, 3));
         contentPane.add(environmentContainer, BorderLayout.CENTER);
 
         add(contentPane, BorderLayout.WEST);
         add(outputContainer, BorderLayout.CENTER);
     }
 
     public LightContainer getOutputContainer() {
         return outputContainer;
     }
 
     private LightContainer makeSimulatorOutput() {
         final OutputPanel outputPanel = new OutputPanel(textArea);
         final LightContainer outputContainer = new LightContainer("Simulator output", outputPanel);
 
         IconBackgroundColorHoverStyle buttonHoverStyle = new IconBackgroundColorHoverStyle();
         int buttonPadding = 3;
 
         copyToClipboardButton = new ImageButton(Resources.getImage("system/panels/lp_copy.png"), buttonHoverStyle, new Dimension(16, 16), buttonPadding);
         copyToClipboardButton.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent mouseEvent) {
                 getListener().onCopyToClipboardButtonClick(copyToClipboardButton, mouseEvent);
             }
         });
 
         scrollToEndButton = new ImageButton(Resources.getImage("system/panels/lp_dock.png"), buttonHoverStyle, new Dimension(16, 16), buttonPadding);
         scrollToEndButton.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent mouseEvent) {
                getListener().onScrollToEndButtonClick(scrollToEndButton, mouseEvent);
             }
         });
 
         trashButton = new ImageButton(Resources.getImage("system/panels/lp_trash.png"), buttonHoverStyle, new Dimension(16, 16), buttonPadding);
         trashButton.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent mouseEvent) {
                 getListener().onTrashButtonClick(trashButton, mouseEvent);
             }
         });
 
         outputContainer.setImageButton(new ImageButton(Resources.getImage("system/panels/ip_new_window.png"), new IconBackgroundColorHoverStyle(), new Dimension(16, 16), 4));
         outputContainer.getImageButton().addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent mouseEvent) {
                 getListener().onSimulatorOutputDialogOpenButtonClick(outputContainer.getImageButton(), mouseEvent);
             }
         });
 
         outputPanel.getLogTextArea().getControlPanel().addButton(copyToClipboardButton);
         outputPanel.getLogTextArea().getControlPanel().addButton(scrollToEndButton);
         outputPanel.getLogTextArea().getControlPanel().addButton(trashButton);
 
         return outputContainer;
     }
 
     private LightContainer makeEnvironment() {
         EnvironmentPanel environmentPanel = new EnvironmentPanel();
         LightContainer environmentContainer = new LightContainer("Environment", environmentPanel);
 
         IconBackgroundColorHoverStyle buttonHoverStyle = new IconBackgroundColorHoverStyle();
         int buttonPadding = 3;
 
         mainStatisticsButton = new ImageButton(Resources.getImage("system/panels/op_main.png"), buttonHoverStyle, new Dimension(16, 16), buttonPadding);
         mainStatisticsButton.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent mouseEvent) {
                 getListener().onCopyToClipboardButtonClick(copyToClipboardButton, mouseEvent);
             }
         });
         moduleStatisticsButton = new ImageButton(Resources.getImage("system/panels/op_local.png"), buttonHoverStyle, new Dimension(16, 16), buttonPadding);
         moduleStatisticsButton.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent mouseEvent) {
                 getListener().onCopyToClipboardButtonClick(copyToClipboardButton, mouseEvent);
             }
         });
         remoteStatisticsButton = new ImageButton(Resources.getImage("system/panels/op_remote.png"), buttonHoverStyle, new Dimension(16, 16), buttonPadding);
         remoteStatisticsButton.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent mouseEvent) {
                 getListener().onCopyToClipboardButtonClick(copyToClipboardButton, mouseEvent);
             }
         });
 
         otherStatisticsButton = new ImageButton(Resources.getImage("system/panels/op_other.png"), buttonHoverStyle, new Dimension(16, 16), buttonPadding);
         otherStatisticsButton.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent mouseEvent) {
                 getListener().onTrashButtonClick(trashButton, mouseEvent);
             }
         });
         otherStatisticsButton.setActive(true);
         environmentPanel.getControlPanel().add(otherStatisticsButton);
         environmentPanel.getControlPanel().add(mainStatisticsButton);
         environmentPanel.getControlPanel().add(moduleStatisticsButton);
         environmentPanel.getControlPanel().add(remoteStatisticsButton);
 
         return environmentContainer;
     }
 }
