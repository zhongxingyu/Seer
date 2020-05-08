 package coursegui;
 
 import net.miginfocom.swing.MigLayout;
 
 import javax.imageio.ImageIO;
 import javax.mail.*;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.swing.*;
 import javax.swing.border.Border;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.security.Security;
 import java.util.ArrayList;
 import java.util.Properties;
 
 class JImagePanel extends JPanel {
 
     private BufferedImage image;
 
     private static BufferedImage overlayImages(BufferedImage bgImage, BufferedImage fgImage) {
 
         if (fgImage.getHeight() > bgImage.getHeight() || fgImage.getWidth() > fgImage.getWidth()) {
             JOptionPane.showMessageDialog(null,
                     "Foreground Image Is Bigger In One or Both Dimensions"
                             + "\nCannot proceed with overlay."
                             + "\n\n Please use smaller Image for foreground");
             return bgImage;
         }
 
         Graphics2D g = bgImage.createGraphics();
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g.drawImage(bgImage, 0, 0, null);
         float alpha = 0.2f;
         AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
         g.setComposite(composite);
         g.drawImage(fgImage, 350, 200, null);
 
         g.dispose();
         return bgImage;
     }
 
     public JImagePanel(String path, boolean isPlayable) {
         try {
             image = ImageIO.read(new File(path));
             if (isPlayable)
                 image = JImagePanel.overlayImages(image, ImageIO.read(new File("resource/Silver-Play-Button.jpg")));
             Border blackline = BorderFactory.createLineBorder(Color.black);
             setBorder(blackline);
         } catch (IOException ex) {
             // handle exception...
         }
     }
 
     public void updateImage(String path, boolean isPlayable) {
         try {
             image = ImageIO.read(new File(path));
             if (isPlayable)
                 image = JImagePanel.overlayImages(image, ImageIO.read(new File("resource/Silver-Play-Button.jpg")));
             repaint();
         } catch (IOException ex) {
             // handle exception...
         }
     }
 
     @Override
     public void paintComponent(Graphics g) {
         /*
         int w = image.getWidth() / 2;
         int h = image.getHeight() / 2;
         int xStart = (getWidth() - w) / 2;
         int yStart = (getHeight() - h) / 2;
         g.drawImage(image.getScaledInstance(w, h, Image.SCALE_SMOOTH), xStart, yStart, null);
         */
 
         float fitToFrameScale = Math.min((float) getWidth() / (float) image.getWidth(), (float) getHeight() / (float) image.getHeight());
         int widthScaled = (int) (image.getWidth() * fitToFrameScale);
         int heightScaled = (int) (image.getHeight() * fitToFrameScale);
         int xStart = (getWidth() - widthScaled) / 2;
         int yStart = (getHeight() - heightScaled) / 2;
         g.drawImage(image.getScaledInstance(widthScaled, heightScaled, Image.SCALE_SMOOTH), xStart, yStart, null);
     }
 }
 
 class GoogleMailer {
 
     private static final String SMTP_HOST_NAME = "smtp.gmail.com";
     private static final String SMTP_PORT = "465";
     private static final String emailMsgTxt = "Test Message Contents";
     private static final String emailSubjectTxt = "A test from gmail";
     private static final String emailFromAddress = "";
     private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
     private static final String[] sendTo = {""};
 
 
     public void sendSSLMessage(String recipients[], String subject,
                                String message, String from) throws MessagingException {
         boolean debug = true;
 
         Properties props = new Properties();
         props.put("mail.smtp.host", SMTP_HOST_NAME);
         props.put("mail.smtp.auth", "true");
         props.put("mail.debug", "true");
         props.put("mail.smtp.port", SMTP_PORT);
         props.put("mail.smtp.socketFactory.port", SMTP_PORT);
         props.put("mail.smtp.socketFactory.class", SSL_FACTORY);
         props.put("mail.smtp.socketFactory.fallback", "false");
 
         Session session = Session.getDefaultInstance(props,
                 new javax.mail.Authenticator() {
 
                     protected PasswordAuthentication getPasswordAuthentication() {
                         return new PasswordAuthentication("alexander.myltsev@gmail.com", "My_password");
                     }
                 });
 
         session.setDebug(debug);
 
         Message msg = new MimeMessage(session);
         InternetAddress addressFrom = new InternetAddress(from);
         msg.setFrom(addressFrom);
 
         InternetAddress[] addressTo = new InternetAddress[recipients.length];
         for (int i = 0; i < recipients.length; i++) {
             addressTo[i] = new InternetAddress(recipients[i]);
         }
         msg.setRecipients(Message.RecipientType.TO, addressTo);
 
 // Setting the Subject and Content Type
         msg.setSubject(subject);
         msg.setContent(message, "text/plain");
         Transport.send(msg);
     }
 }
 
 public class MainFrame {
     private static final boolean OPAQUE = false;
     private int currentHeight = 0;
     private int currentWidth = 0;
     private String textOfQuestion = "\n\n\nType your question or query here and click \"send\" to receive a consultation";
     private int lectureNumber = 1;
     private int slideNumber = 1;
 
     private void paintGradient(Graphics g, JPanel panel) {
         //UIDefaults uid = UIManager.getDefaults();
         Graphics2D g2d = (Graphics2D) g;
         Dimension d = panel.getSize();
 
         //g2d.setPaint(new GradientPaint(0, 0, Color.WHITE, 0, d.height, Color.BLACK, true));
         java.awt.geom.Point2D center = new java.awt.geom.Point2D.Float(d.width / 2, d.height / 2);
         float radius = 2 * Math.min(d.width, d.height);
         float[] dist = {0.0f, 1.0f};
         Color[] colors = {new Color(0.0f, 0.0f, 0.0f, 0.0f), Color.BLACK};
         RadialGradientPaint p = new RadialGradientPaint(center, radius, dist, colors);
         g2d.setPaint(p);
         g2d.fillRect(0, 0, d.width, d.height);
     }
 
     private void postMail(String text) {
         //private void postMail(String recipients[], String subject, String message, String from) {
         String[] sendTo = {"edu.cuda@parallel-compute.com"};
         //String emailMsgTxt = "Test Message Contents";
         String emailMsgTxt = text;
         String emailSubjectTxt = "A test from gmail";
         String emailFromAddress = "alexander.myltsev@gmail.com";
 
         Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
 
         try {
             new GoogleMailer().sendSSLMessage(sendTo, emailSubjectTxt, emailMsgTxt, emailFromAddress);
             System.out.println("Sucessfully Sent mail to All Users");
         } catch (MessagingException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     private JScrollPane createTextAreaScroll(int rows, int cols, boolean hasVerScroll) {
         final String text = "\n\n\nType your question or query here and click \"send\" to receive a consultation";
         final JTextArea ta = new JTextArea(textOfQuestion, rows, cols);
         ta.setFont(UIManager.getFont("TextField.font"));
         ta.setWrapStyleWord(true);
         ta.setLineWrap(true);
 
         ta.getDocument().addDocumentListener(new DocumentListener() {
             @Override
             public void insertUpdate(DocumentEvent e) {
                 textOfQuestion = ta.getText();
             }
 
             @Override
             public void removeUpdate(DocumentEvent e) {
                 textOfQuestion = ta.getText();
             }
 
             @Override
             public void changedUpdate(DocumentEvent e) {
                 textOfQuestion = ta.getText();
             }
         });
 
         ta.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseEntered(MouseEvent e) {
                 if (ta.getText().equals(text)) {
                     ta.setText("");
                 }
             }
 
             @Override
             public void mouseExited(MouseEvent e) {
                 if (ta.getText().equals("")) {
                     ta.setText(text);
                     ta.setCaretPosition(0);
                 }
             }
         });
 
         JScrollPane scroll = new JScrollPane(
                 ta,
                 hasVerScroll ? ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED : ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                 ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 
         return scroll;
     }
 
     private String news = "<h2>Latest News</h2>\t\t<h3>February 1, 2012 &nbsp;&nbsp;&nbsp;&nbsp; </h3>\n" +
             "\t\t<a href=\"http://cuda-course.eventbrite.com/\">5-8 february On-site training and Consultancy and a 4 day advanced CUDA course at  Irish Supercomputing Center ICHEC, Dublin</a>\n" +
             "\n" +
             "\t\t<br/><br/>\n" +
             "\n" +
             "\t\t<h3>January 16, 2012 &nbsp;&nbsp;&nbsp;&nbsp; </h3>\n" +
             "\t\t<a href=\"http://parallel-compute.com/education/plan\">January 16-18, 2012. A three day CUDA course at the Technical University of Munich. Germany.</a>\n" +
             "\t\t\n" +
             "\t\t<br/><br/>\n" +
             "\t\n" +
             "\t\t\n" +
             "\t\t<h3>December 12, 2011 &nbsp;&nbsp;&nbsp;&nbsp;</h3>\n" +
             "\t\tGPU Computing and CUDA.&nbsp;Supercomputing winter school at MSU for \"T-Platforms'\n" +
             "\t\t\n" +
             "\t";
 
     private ArrayList<JButton> getSocialButtons() {
         String[][] pathsToIcons = {
                 {"resource/rss.png", "rss-clicked"},
                 {"resource/youtube.png", "youtube-clicked"},
                 {"resource/facebook.png", "facebook-clicked"},
                 {"resource/linkedin.png", "linkedin-clicked"},
                 {"resource/twitter.png", "twitter-clicked"}
         };
         ArrayList<JButton> buttons = new ArrayList<JButton>();
         ActionListener socialButtonsEventListener = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 //System.out.println(e.toString());
                 try {
                     URI u;
                     if ("rss-clicked".equals(e.getActionCommand()))
                         u = new URI("http://parallel-compute.com/news/");
                     else if ("facebook-clicked".equals(e.getActionCommand())) u = new URI("http://facebook.com");
                     else if ("linkedin-clicked".equals(e.getActionCommand())) u = new URI("http://linkedin.com");
                     else if ("twitter-clicked".equals(e.getActionCommand())) u = new URI("http://twitter.com");
                     else if ("youtube-clicked".equals(e.getActionCommand())) u = new URI("http://youtube.com");
                     else u = new URI("http://parallel-compute.com");
                     Desktop.getDesktop().browse(u);
                 } catch (URISyntaxException e1) {
                     e1.printStackTrace(); // TODO: Process the error correctly.
                 } catch (IOException e1) {
                     e1.printStackTrace(); // TODO: Process the error correctly.
                 }
             }
         };
         for (int i = 0; i < pathsToIcons.length; i++) {
             JButton button = new JButton("", new ImageIcon(pathsToIcons[i][0]));
             button.addActionListener(socialButtonsEventListener);
             button.setActionCommand(pathsToIcons[i][1]);
             buttons.add(button);
         }
         return buttons;
     }
 
     class LectureButtonListener implements ActionListener {
         private JImagePanel lectureContentViewer;
 
         LectureButtonListener(JImagePanel lectureContentViewer) {
             this.lectureContentViewer = lectureContentViewer;
         }
 
         @Override
         public void actionPerformed(ActionEvent e) {
             lectureNumber = Integer.parseInt(e.getActionCommand());
             String path = "resource/Lectures/Lecture" + lectureNumber + "/Slide1.PNG";
             lectureContentViewer.updateImage(path, true);
         }
     }
 
     private JPanel createLectureSelectorPanel() {
         final JPanel panel = new JPanel(new MigLayout(
                 "insets 10",
                "[grow,fill][]",
                "[grow,fill][]"), true
         ) {
             @Override
             protected void paintComponent(Graphics g) {
                 paintGradient(g, this);
             }
         };
 
 
         JImagePanel lectureContentViewer = new JImagePanel("resource/Lectures/Lecture" + lectureNumber + "/Slide1.PNG", true);
         lectureContentViewer.setCursor(new Cursor(Cursor.HAND_CURSOR));
         lectureContentViewer.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseReleased(MouseEvent e) {
                 System.out.println("Mouse clicked");
                 JPanel slidesSelectorPanel = createSlidesSelectorPanel();
                 frame.setVisible(false);
                 frame.setContentPane(slidesSelectorPanel);
                 frame.pack();
                 frame.setSize(currentWidth, currentHeight);
                 frame.setVisible(true);
             }
         });
         panel.add(lectureContentViewer, "cell 0 0");
 
         panel.add(createBanner(), "w 200!, h 100!, flowy, cell 1 0");
         panel.add(createNewsPanel(news, true), "w 200!, h 200!,flowy, cell 1 0");
        panel.add(new JLabel("Ask a question"), "gapx push, cell 1 0");
         panel.add(createTextAreaScroll(4, 30, true), "w 200!, flowy, grow, cell 1 0");
        panel.add(createSendButton(), "gapx push, gapy 0px, cell 1 0"); // pos visual.x2-pref visual.y2-pref-35
 
         LectureButtonListener lectureButtonActionListener = new LectureButtonListener(lectureContentViewer);
         for (int i = 1; i <= 4; i++) {
             JButton lectureButton = new JButton("Lecture " + i);
             lectureButton.setActionCommand("" + i);
             lectureButton.addActionListener(lectureButtonActionListener);
             panel.add(lectureButton, "cell 0 1,h 35!,flowx");
         }
 
         ArrayList<JButton> socialButtons = getSocialButtons();
         for (int i = 0; i < socialButtons.size(); i++) {
             panel.add(socialButtons.get(i), "gapx push,gapy push,w 35!,h 35!,cell 1 1");
         }
 
         return panel;
     }
 
     private JButton createSendButton() {
         JButton sendButton = new JButton("Send");
         sendButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 postMail(textOfQuestion);
             }
         });
         return sendButton;
     }
 
     private JScrollPane createNewsPanel(String news, boolean hasVerScroll) {
         JEditorPane ep = new JEditorPane();
         ep.setContentType("text/html");
         ep.setText(news);
         ep.setEditable(false);
         ep.addHyperlinkListener(new HyperlinkListener() {
             @Override
             public void hyperlinkUpdate(HyperlinkEvent e) {
                 //To change body of implemented methods use File | Settings | File Templates.
                 //System.out.println(e);
 
                 try {
                     if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
                         Desktop.getDesktop().browse(e.getURL().toURI());
                 } catch (IOException e1) {
                     e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 } catch (URISyntaxException e1) {
                     e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
             }
         });
         ep.setCaretPosition(0);
 
         JScrollPane scroll = new JScrollPane(
                 ep,
                 hasVerScroll ? ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED : ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                 ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         return scroll;
     }
 
     private JPanel createSlidesSelectorPanel() {
         JPanel panel = new JPanel(new MigLayout(
                 "insets 10",
                 "[][grow,fill][]",
                 "[grow,fill][]"), true) {
             @Override
             protected void paintComponent(Graphics g) {
                 paintGradient(g, this);
             }
         };
 
         final JImagePanel slideSelectorPanel = new JImagePanel("resource/Lectures/Lecture" + lectureNumber + "/Slide1.PNG", false);
 
         for (int i = 1; i < 23; i++) {
             JButton slideButton = new JButton("Slide " + i);
             final int slideNum = i;
             slideButton.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     slideNumber = slideNum;
                     String path = "resource/Lectures/Lecture" + lectureNumber + "/Slide" + slideNumber + ".PNG";
                     slideSelectorPanel.updateImage(path, false);
                 }
             });
             panel.add(slideButton, "cell 0 0,flowy,sg g1,w 70!");
         }
 
         panel.add(slideSelectorPanel, "grow");
 
         panel.add(createBanner(), "w 200!, h 100!, cell 2 0,flowy");
         panel.add(createNewsPanel(news, true), "w 200!, h 200!, grow, cell 2 0,flowy");
        panel.add(new JLabel("Ask a question"), "gapx push, cell 2 0");
         panel.add(createTextAreaScroll(4, 30, true), "w 200!, grow, cell 2 0,flowy");
        panel.add(createSendButton(), "cell 2 0, gapx push, gapy 0px");
 
         final JButton playPauseButton = new JButton("||");
         playPauseButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (playPauseButton.getText().equals("||"))
                     playPauseButton.setText("|>");
                 else
                     playPauseButton.setText("||");
             }
         });
         panel.add(playPauseButton, "cell 1 1,w 50!,h 35!");
         panel.add(new JSlider(0, 100, 0), "cell 1 1,growx 90,h 35!");
         //panel.add(new JButton("Mute"), "cell 1 1,w 65!");
         panel.add(new JLabel(new ImageIcon("resource/volume+icon.jpg")), "cell 1 1,w 30!,h 35!");
         JSlider jSlider = new JSlider(0, 100, 100);
         panel.add(jSlider, "cell 1 1,w 100!,h 35!");
 
         JButton backToLectureSelectionButton = new JButton("<html>Lecture<br/>selection</html>");
         backToLectureSelectionButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 System.out.println("Back to lecture selection is clicked");
                 JPanel lectureSelectorPanel = createLectureSelectorPanel();
                 frame.setVisible(false);
                 frame.setContentPane(lectureSelectorPanel);
                 frame.pack();
                 frame.setSize(currentWidth, currentHeight);
                 frame.setVisible(true);
             }
         });
        panel.add(backToLectureSelectionButton, "w 70!,h 35!,cell 0 1");
 
         ArrayList<JButton> socialButtons = getSocialButtons();
         for (int i = 0; i < socialButtons.size(); i++) {
             panel.add(socialButtons.get(i), "gapx push,gapy push,w 35!,h 35!,cell 2 1");
         }
 
         return panel;
     }
 
     private JLabel createBanner() {
         JLabel banner = new JLabel(new ImageIcon("resource/TESLA_200x100.jpg"));
         banner.setCursor(new Cursor(Cursor.HAND_CURSOR));
         banner.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseReleased(MouseEvent e) {
                 try {
                     Desktop.getDesktop().browse(new URI("http://www.nvidia.com/object/workstation-solutions-tesla.html"));
                 } catch (IOException e1) {
                     e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 } catch (URISyntaxException e1) {
                     e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
             }
         });
         return banner;
     }
 
     JFrame frame = new JFrame("CourseGUI");
 
     public static void main(final String[] args) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 try {
                     UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                 } catch (Exception ex) {
                     ex.printStackTrace();
                 }
 
                 Toolkit toolkit = Toolkit.getDefaultToolkit();
                 Dimension dimension = toolkit.getScreenSize();
                 System.out.println("Width: " + dimension.width);
                 System.out.println("Height: " + dimension.height);
 
                 final MainFrame mainFrame = new MainFrame();
 
                 mainFrame.currentHeight = (int) (dimension.height * 0.8);
                 mainFrame.currentWidth = (int) (dimension.width * 0.8);
 
                 int startX = (dimension.width - mainFrame.currentWidth) / 2;
                 int startY = (dimension.height - mainFrame.currentHeight) / 2;
 
                 //JFrame frame = new JFrame("CourseGUI");
                 mainFrame.frame.getContentPane().add(mainFrame.createLectureSelectorPanel());
                 //mainFrame.frame.getContentPane().add(mainFrame.createStartPanel());
                 mainFrame.frame.pack();
                 mainFrame.frame.setSize(mainFrame.currentWidth, mainFrame.currentHeight);
                 mainFrame.frame.setLocation(startX, startY);
                 mainFrame.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                 mainFrame.frame.setVisible(true);
 
                 mainFrame.frame.addComponentListener(new java.awt.event.ComponentAdapter() {
                     public void componentResized(ComponentEvent event) {
                         mainFrame.currentHeight = mainFrame.frame.getHeight();
                         mainFrame.currentWidth = mainFrame.frame.getWidth();
                         //mainFrame.currentHeight = Math.max(600, mainFrame.frame.getHeight());
                         //mainFrame.currentWidth = Math.max(1200, mainFrame.frame.getWidth());
                         //mainFrame.frame.setSize(mainFrame.currentWidth, mainFrame.currentHeight);
                     }
                 });
             }
         });
     }
 
     private JPanel createStartPanel() {
         JPanel mainPanel = new JPanel(new MigLayout()) {
             @Override
             protected void paintComponent(Graphics g) {
                 paintGradient(g, this);
             }
         };
 
         //mainPanel.add(new JTextField("Login"), "wrap, w 200!");
         //mainPanel.add(new JTextField("String to send"), "wrap, w 200!");
         //mainPanel.add(new JTextField("Your string"), "wrap, w 200!");
         JButton registrationButton = new JButton("Pass registration");
         registrationButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 System.out.println("Pass registration is clicked");
                 JPanel lectureSelectorPanel = createLectureSelectorPanel();
                 frame.setVisible(false);
                 frame.setContentPane(lectureSelectorPanel);
                 frame.pack();
                 frame.setSize(currentWidth, currentHeight);
                 frame.setVisible(true);
             }
         });
         mainPanel.add(registrationButton);
 
         return mainPanel;
     }
 }
