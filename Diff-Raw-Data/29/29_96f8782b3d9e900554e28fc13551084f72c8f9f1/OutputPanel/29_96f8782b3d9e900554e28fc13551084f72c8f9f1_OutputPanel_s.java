 package jDistsim.ui.panel;
 
import jDistsim.ui.skins.ScrollBarUI;
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
    private JTextArea textArea;
 
     public OutputPanel(JTextArea textArea) {
         setLayout(new BorderLayout());
         setBorder(new EmptyBorder(0, 0, 0, 0));
 
        this.textArea = textArea;
        this.textArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        this.textArea.setBorder(new EmptyBorder(3, 6, 1, 2));
        this.textArea.setForeground(new Color(20, 20, 20));
        this.textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(this.textArea);
        scrollPane.setBorder(null);

        JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollBar.setUI(new ScrollBarUI());

         JPanel emptyPanel = new JPanel();
         emptyPanel.setOpaque(false);
         SwingUtil.setAbsoluteDimension(emptyPanel, leftPadding, getHeight());
 
        add(emptyPanel, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);
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
