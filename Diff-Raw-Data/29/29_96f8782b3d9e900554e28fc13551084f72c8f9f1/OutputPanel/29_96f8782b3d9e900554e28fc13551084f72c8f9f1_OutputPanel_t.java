 package jDistsim.ui.panel;
 
import jDistsim.ui.control.LogTextArea;
 import jDistsim.utils.ui.SwingUtil;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import java.awt.*;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 21.2.13
  * Time: 18:27
  */
 public class OutputPanel extends JComponent {
 
     private int leftPadding = 6;
    private LogTextArea logTextArea;
 
     public OutputPanel(JTextArea textArea) {
         setLayout(new BorderLayout());
         setBorder(new EmptyBorder(0, 0, 0, 0));
 
         JPanel emptyPanel = new JPanel();
         emptyPanel.setOpaque(false);
         SwingUtil.setAbsoluteDimension(emptyPanel, leftPadding, getHeight());
 
        //add(emptyPanel, BorderLayout.WEST);
        logTextArea = new LogTextArea(false, textArea);
        add(logTextArea, BorderLayout.CENTER);
    }

    public LogTextArea getLogTextArea() {
        return logTextArea;
     }
 
     @Override
     protected void paintComponent(Graphics graphics) {
         super.paintComponent(graphics);
         Graphics2D graphics2D = (Graphics2D) graphics;
         graphics2D.setColor(new Color(243, 243, 243));
         graphics2D.fillRect(0, 0, leftPadding, getHeight());
 
         BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{2.0f}, 0.0f);
         Stroke currentStroke = graphics2D.getStroke();
         graphics2D.setStroke(dashed);
         graphics2D.setColor(new Color(146, 146, 146));
         graphics2D.drawLine(leftPadding - 1, 0, leftPadding - 1, getHeight());
         graphics2D.setStroke(currentStroke);
     }
 }
