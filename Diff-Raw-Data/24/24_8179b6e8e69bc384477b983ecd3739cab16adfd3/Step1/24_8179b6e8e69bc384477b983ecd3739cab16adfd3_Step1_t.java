 package com.printapp.edit;
 
 import com.printapp.dialog.MainPanel;
 import com.printapp.util.*;
 
 import javax.swing.*;
 import javax.swing.border.CompoundBorder;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.LineBorder;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 public class Step1 extends JPanel{
 
 
     protected JButton i18nSwitchBtn;
     private static String[] langList = {"ru", "en"};
     protected JPanel bannerPanel;
     protected Image bannerImg;
     protected JPanel centerPanel;
     protected JPanel centerLeftPanel;
     protected JPanel centerRightPanel;
     protected JButton newBtn;
     protected JButton openBtn;
     protected JButton binBtn;
     private JLabel helloLabel;
     
     private Image topLeftImg;
     private Image bottomLeftImg;
 
     public Step1(){
 
         super();
         init();
     }
 
     private void setLanguageIcon(JButton jButton){
         if (jButton.getText().equalsIgnoreCase("ru"))
             jButton.setIcon(new ImageIcon(ImageResource.getImageIcon("ru.png").getImage().getScaledInstance(70, 40, Image.SCALE_DEFAULT)));
         else
             jButton.setIcon(new ImageIcon(ImageResource.getImageIcon("en.png").getImage().getScaledInstance(70, 40, Image.SCALE_DEFAULT)));
         jButton.repaint();
     }
 
     public void init(){
 
        bannerImg = ImageResource.getImageIcon("logo1.png").getImage();
 
         i18nSwitchBtn = new JButton(){
             public String getText() {
                 String txt = Configuration.getLanguage();
                 if(Utils.isNull(txt))
                     txt = Configuration.default_language;
                 return txt;
             }
         };
 
         i18nSwitchBtn.setAction(new AbstractAction() {
             public void actionPerformed(ActionEvent e) {
                 String lang = "" + Configuration.getLanguage();
                 int index = -1;
                 for(int j = 0; j < langList.length; j++)
                     if(lang.equals(langList[j])){
                         index = j;
                         break;
                     }
                 index++;
                 if(index >= langList.length)
                     index = 0;
                 Configuration.setLanguage(langList[index]);
                 Utils.getMainPanel().setCurrentState(Utils.getMainPanel().getCurrentState());
                 setLanguageIcon(i18nSwitchBtn);
                 repaint();
             }
         });
         i18nSwitchBtn.setBackground(ColorScheme.dataBkgColor);
         i18nSwitchBtn.setContentAreaFilled(false);
         i18nSwitchBtn.setIcon(new ImageIcon(ImageResource.getImageIcon("ru.png").getImage().getScaledInstance(70, 40, Image.SCALE_DEFAULT)));
 
         setLayout(new GridBagLayout());
         this.topLeftImg = ImageResource.getImageIcon("leftLogo.png").getImage();
         this.bottomLeftImg = ImageResource.getImageIcon("txt2c.png").getImage();
         bannerPanel = new JPanel(){
             protected void paintComponent(Graphics g) {
                 super.paintComponent(g);
                g.drawImage(bannerImg, 0, 0, getWidth(), 71, this);
             }
         };
 
        bannerPanel.setPreferredSize(new Dimension(50, 73));
 
         JPanel tmpPanel = new JPanel(new GridBagLayout());
         Utils.constrain(tmpPanel, bannerPanel, 0, 0, 3, 1, GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, 1, 1, 1, 1, 1, 1);
 //        Utils.constrain(tmpPanel, i18nSwitchBtn, 3, 0, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, 0.3, 1, 1, 1, 1, 1);
 
         tmpPanel.setBorder(new CompoundBorder(new EmptyBorder(1, 0, 1, 0), new LineBorder(Color.black) {
             public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                 Graphics2D g2D = (Graphics2D) g;
                 g2D.setPaint(new GradientPaint(0, height - 1, Color.gray, width / 2, height - 1, Color.gray.brighter().brighter().brighter()));
                 g2D.drawLine(0, height - 1, width / 2, height - 1);
                 g2D.setPaint(new GradientPaint(width / 2, height - 1, Color.gray.brighter().brighter().brighter(), width, height - 1, Color.gray));
                 g2D.drawLine(width / 2, height - 1, width, height - 1);
             }
         }));
 
         centerLeftPanel = new JPanel(){
             protected void paintComponent(Graphics g) {
                 super.paintComponent(g);
                g.drawImage(topLeftImg, (getWidth() - 292) / 2, 2 + 10, 292, 250, this);
                g.drawImage(bottomLeftImg, (getWidth() - 308) / 2, getHeight() / 2 + 4 + 10, 308, 153, this);
             }
         };
 
         centerLeftPanel.setBorder(new CompoundBorder(new EmptyBorder(1, 1, 1, 1), new LineBorder(Color.black){
             public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                 Graphics2D g2D = (Graphics2D ) g;
                 g2D.setPaint(new GradientPaint(width - 1, 0, Color.gray, width - 1, height / 2, Color.gray.brighter().brighter().brighter()));
                 g2D.drawLine(width - 1, 0, width - 1, height / 2);
                 g2D.setPaint(new GradientPaint(width - 1, height / 2, Color.gray.brighter().brighter().brighter(), width - 1, height, Color.gray));
                 g2D.drawLine(width - 1, height / 2, width - 1, height);
             }
         }));
         
         centerRightPanel = new JPanel(new BorderLayout());
         centerRightPanel.add(tmpPanel, BorderLayout.NORTH);
         
         helloLabel = Utils.getLabel("helloLabel");
         helloLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
         
         newBtn = Utils.getButton("New");
         newBtn.setIcon(ImageResource.getImageIcon("New32.png"));
         newBtn.setContentAreaFilled(false);
         newBtn.setHorizontalAlignment(AbstractButton.LEFT);
         newBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 Utils.forwardState(Utils.getMainPanel());
             }
         });
         newBtn.setBorderPainted(false);
         newBtn.setFocusPainted(false);
 
         openBtn = Utils.getButton("Open");
         openBtn.setIcon(ImageResource.getImageIcon("Open32.png"));
         openBtn.setContentAreaFilled(false);
         openBtn.setHorizontalAlignment(AbstractButton.LEFT);
         openBtn.setBorderPainted(false);
         openBtn.setFocusPainted(false);
 
         binBtn = Utils.getButton("Bin");
         binBtn.setIcon(new ImageIcon(ImageResource.getImageIcon("bin.png").getImage().getScaledInstance(32, 32, Image.SCALE_DEFAULT)));
         binBtn.setContentAreaFilled(false);
         binBtn.setHorizontalAlignment(AbstractButton.LEFT);
         binBtn.setBorderPainted(false);
         binBtn.setFocusPainted(false);
 
         Utils.setupHyperlinkView(newBtn);
         Utils.setupHyperlinkView(openBtn);
         Utils.setupHyperlinkView(binBtn);
 
         JPanel tmpPanel2 = new JPanel();
         tmpPanel2.setLayout(new BoxLayout(tmpPanel2, BoxLayout.Y_AXIS));
         tmpPanel2.add(Box.createVerticalStrut(50));
         tmpPanel2.add(makeBtnPanel(helloLabel));
         tmpPanel2.add(Box.createVerticalStrut(50));
         tmpPanel2.add(makeBtnPanel(newBtn));
         tmpPanel2.add(makeBtnPanel(openBtn));
         tmpPanel2.add(makeBtnPanel(binBtn));
         tmpPanel2.add(Box.createVerticalStrut(50));
 
         centerRightPanel.add(tmpPanel2, BorderLayout.SOUTH);
         
         centerPanel = new JPanel(new GridLayout());
         
         centerPanel.add(centerLeftPanel);
         centerPanel.add(centerRightPanel);
 
         setLayout(new GridBagLayout());
         
         Utils.constrain(this, centerPanel, 0, 0, 4, 1, GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, 1, 1, 0, 0, 0, 0);
         
     }
 
     private JPanel makeBtnPanel(JComponent btn){
         JPanel panel = new JPanel();
         panel.setLayout(new BorderLayout());
         panel.add(btn, BorderLayout.CENTER);
         panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.setMaximumSize(new Dimension(180, 50));
        panel.setSize(new Dimension(180, 50));
         return panel;
     }
 }
