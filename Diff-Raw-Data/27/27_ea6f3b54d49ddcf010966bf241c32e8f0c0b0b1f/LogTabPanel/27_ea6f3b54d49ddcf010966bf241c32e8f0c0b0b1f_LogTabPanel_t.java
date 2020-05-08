 package jDistsim.ui.panel.tab;
 
 import jDistsim.ui.control.button.ImageButton;
 import jDistsim.ui.panel.listener.LogTabListener;
 import jDistsim.ui.skins.ScrollBarUI;
 import jDistsim.utils.logging.LogMessage;
 import jDistsim.utils.logging.Logger;
 import jDistsim.utils.logging.handlers.ILoggerHandler;
 import jDistsim.utils.resource.Resources;
 import jDistsim.utils.ui.ListenerablePanel;
 import jDistsim.utils.ui.control.IconBackgroundColorHoverStyle;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import javax.swing.text.DefaultCaret;
 import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.Iterator;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 13.2.13
  * Time: 23:03
  */
 public class LogTabPanel extends ListenerablePanel<LogTabListener> {
 
     private ControlPanel controlPanel;
     private TextAreaPanel textAreaPanel;
 
     public LogTabPanel() {
         initialize();
     }
 
     private void initialize() {
         setLayout(new BorderLayout());
         setBorder(new EmptyBorder(5, 5, 5, 5));
 
         controlPanel = new ControlPanel();
         controlPanel.setPreferredSize(new Dimension(40, controlPanel.getHeight()));
 
         textAreaPanel = new TextAreaPanel();
 
         add(controlPanel, BorderLayout.WEST);
         add(textAreaPanel, BorderLayout.CENTER);
     }
 
     public ControlPanel getControlPanel() {
         return controlPanel;
     }
 
     public TextAreaPanel getTextAreaPanel() {
         return textAreaPanel;
     }
 
     public class ControlPanel extends JComponent {
 
         private ImageButton wordWrapButton;
         private ImageButton copyToClipboardButton;
         private ImageButton scrollToEndButton;
         private ImageButton trashButton;
 
         public ControlPanel() {
             setLayout(new FlowLayout(FlowLayout.LEADING));
 
             IconBackgroundColorHoverStyle hoverStyle = new IconBackgroundColorHoverStyle();
             int padding = 3;
 
             wordWrapButton = new ImageButton(Resources.getImage("system/panels/lp_wrap.png"), hoverStyle, new Dimension(16, 16), padding);
             wordWrapButton.addMouseListener(new MouseAdapter() {
                 @Override
                 public void mouseClicked(MouseEvent mouseEvent) {
                     getListener().onWordWrapButtonClick(wordWrapButton, mouseEvent);
                 }
             });
 
             copyToClipboardButton = new ImageButton(Resources.getImage("system/panels/lp_copy.png"), hoverStyle, new Dimension(16, 16), padding);
             copyToClipboardButton.addMouseListener(new MouseAdapter() {
                 @Override
                 public void mouseClicked(MouseEvent mouseEvent) {
                     getListener().onCopyToClipboardButtonClick(copyToClipboardButton, mouseEvent);
                 }
             });
 
             scrollToEndButton = new ImageButton(Resources.getImage("system/panels/lp_dock.png"), hoverStyle, new Dimension(16, 16), padding);
             scrollToEndButton.addMouseListener(new MouseAdapter() {
                 @Override
                 public void mouseClicked(MouseEvent mouseEvent) {
                     getListener().onScrollToEndButtonClick(scrollToEndButton, mouseEvent);
                 }
             });
 
             trashButton = new ImageButton(Resources.getImage("system/panels/lp_trash.png"), hoverStyle, new Dimension(16, 16), padding);
             trashButton.addMouseListener(new MouseAdapter() {
                 @Override
                 public void mouseClicked(MouseEvent mouseEvent) {
                     getListener().onTrashButtonClick(trashButton, mouseEvent);
                 }
             });
 
             add(wordWrapButton);
             add(copyToClipboardButton);
             add(scrollToEndButton);
             add(trashButton);
         }
 
         public ImageButton getWordWrapButton() {
             return wordWrapButton;
         }
 
         public ImageButton getCopyToClipboardButton() {
             return copyToClipboardButton;
         }
 
         public ImageButton getScrollToEndButton() {
             return scrollToEndButton;
         }
 
         public ImageButton getTrashButton() {
             return trashButton;
         }
 
         @Override
         protected void paintComponent(Graphics graphics) {
             super.paintComponent(graphics);
             int rightPadding = 6;
 
             Graphics2D graphics2D = (Graphics2D) graphics;
             graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
             graphics2D.setColor(new Color(214, 214, 214));
             graphics2D.fillRect(0, 0, getWidth(), getHeight());
 
             graphics2D.setColor(new Color(240, 240, 240));
             graphics2D.fillRect(getWidth() - rightPadding, 0, getWidth(), getHeight());
 
             graphics2D.setColor(new Color(192, 192, 192));
             graphics2D.drawLine(0, 0, getWidth() - 1, 0);
             graphics2D.drawLine(0, 0, 0, getHeight() - 1);
             graphics2D.drawLine(0, getHeight() - 1, getWidth() - 1, getHeight() - 1);
 
             graphics2D.setColor(new Color(128, 128, 128));
             Stroke currentStroke = graphics2D.getStroke();
             BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{2.0f}, 0.0f);
             graphics2D.setStroke(dashed);
 
             graphics2D.drawLine(getWidth() - 1, 1, getWidth() - 1, getHeight() - 2);
             graphics2D.setStroke(currentStroke);
         }
     }
 
     public class TextAreaPanel extends JComponent implements ILoggerHandler {
 
         private JTextArea textArea;
 
         public TextAreaPanel() {
             setBorder(new EmptyBorder(1, 1, 1, 1));
 
             textArea = new JTextArea();
             textArea.setFont(new Font("Consolas", Font.PLAIN, 11));
             textArea.setBorder(new EmptyBorder(3, 6, 1, 2));
             textArea.setForeground(new Color(70, 70, 70));
             //textArea.setEnabled(false);
             textArea.setEditable(false);
             JScrollPane scrollPane = new JScrollPane(textArea);
             scrollPane.getVerticalScrollBar().setUI(new ScrollBarUI());
             scrollPane.getHorizontalScrollBar().setUI(new ScrollBarUI());
             scrollPane.setBorder(null);
 
             Iterator<LogMessage> iterator = Logger.getMessages();
 
             while (iterator.hasNext()) {
                 LogMessage logMessage = iterator.next();
                 publishToTextArea(logMessage);
             }
             setCaretPosition();
 
             Logger.getLoggerHandlerManager().addHandler(this);
             setLayout(new BorderLayout());
             add(scrollPane, BorderLayout.CENTER);
         }
 
         @Override
         protected void paintComponent(Graphics graphics) {
             super.paintComponent(graphics);
             Graphics2D graphics2D = (Graphics2D) graphics;
 
             graphics2D.setColor(Color.white);
             graphics2D.fillRect(0, 0, getWidth(), getHeight());
 
             graphics2D.setColor(new Color(192, 192, 192));
             graphics2D.drawLine(0, 0, getWidth() - 1, 0);
             graphics2D.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight() - 1);
             graphics2D.drawLine(0, getHeight() - 1, getWidth() - 1, getHeight() - 1);
         }
 
         public JTextArea getTextArea() {
             return textArea;
         }
 
         public void setAutoCaretPosition(boolean autoCaretPosition) {
             DefaultCaret caret = (DefaultCaret) textArea.getCaret();
             if (autoCaretPosition) {
                 caret.setUpdatePolicy(DefaultCaret.OUT_BOTTOM);
             } else {
                 caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
             }
         }
 
         public void setWordWrap(boolean wordWrap) {
             textArea.setLineWrap(wordWrap);
             textArea.setWrapStyleWord(wordWrap);
         }
 
         private void setCaretPosition() {
             if (getControlPanel().getScrollToEndButton().isActive()) {
                 textArea.setCaretPosition(textArea.getDocument().getLength());
             }
         }
 
         @Override
         public void publish(LogMessage logMessage) {
             publishToTextArea(logMessage);
         }
 
         public void publishToTextArea(LogMessage logMessage) {
             addLineToTextArea("[" + logMessage.getLevel() + "] " + logMessage.getText());
         }
 
         private void addLineToTextArea(String text) {
             if (textArea.getText().equals("")) {
                 textArea.append(text);
             } else {
                 textArea.append("\n" + text);
             }
             setCaretPosition();
         }
     }
 
 }
