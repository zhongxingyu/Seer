 package ro.btanase.chordlearning.frames;
 
 import java.awt.Component;
 import java.awt.Desktop;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.net.URI;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.border.BevelBorder;
 import javax.swing.border.Border;
 import javax.swing.border.EmptyBorder;
 
 import net.miginfocom.swing.MigLayout;
 
 import org.apache.log4j.Logger;
 
 import ro.btanase.chordlearning.ChordLearningApp;
 import ro.btanase.chordlearning.dao.LessonDao;
 import ro.btanase.chordlearning.dao.ScoreDao;
 import ro.btanase.chordlearning.domain.Lesson;
 import ro.btanase.chordlearning.domain.LessonType;
 import ca.odell.glazedlists.swing.EventListModel;
 
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import javax.swing.KeyStroke;
 import javax.swing.UIManager;
 import javax.swing.JLabel;
 import javax.swing.ImageIcon;
 import javax.swing.border.LineBorder;
 import java.awt.Color;
 import javax.swing.JTextField;
 import java.awt.Font;
 import java.awt.Toolkit;
 import javax.swing.SwingConstants;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 public class ApplicationMainWindow extends JFrame {
 
   private JPanel contentPane;
   private LessonDao m_lessons;
   private JList jlistLessons;
   private JButton btnStartTest;
   private JMenuBar menuBar;
   private JMenu mnStatistics;
   private JMenuItem menuItemChordAccuracy;
   private JMenuItem menuItemLessonEvolution;
   private JMenu mnConfiguration;
   private JMenuItem menuItemChordManager;
   private JMenuItem menuItemLessonManager;
   private JMenu mnHelp;
   private JMenuItem menuItemHelp;
   private JMenuItem menuItemAbout;
   private JTextPane txtpnToStartTraining;
   private static Logger log = Logger.getLogger(ApplicationMainWindow.class);
   private JMenuItem menuItemScoreReset;
   private JPanel panel;
   private JLabel lblNewLabel;
 
 
   /**
    * Create the frame.
    */
   @Inject
   public ApplicationMainWindow(LessonDao m_lessons) {
     setIconImage(Toolkit.getDefaultToolkit().getImage(ApplicationMainWindow.class.getResource("/res/tme_small.png")));
     addWindowListener(new WindowAdapter() {
       @Override
       public void windowOpened(WindowEvent e) {
         jlistLessons.requestFocusInWindow();
       }
     });
     this.m_lessons = m_lessons;
     setTitle("Train My Ear " + "version: " + ChordLearningApp.VERSION);
     setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     setBounds(100, 100, 501, 480);
     
     menuBar = new JMenuBar();
     setJMenuBar(menuBar);
     
     mnStatistics = new JMenu("Statistics");
     mnStatistics.setMnemonic('s');
     mnStatistics.setIcon(null);
     menuBar.add(mnStatistics);
     
     menuItemChordAccuracy = new JMenuItem("Chord Accuracy ...");
     menuItemChordAccuracy.setMnemonic('c');
     menuItemChordAccuracy.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         showChordAccuracy();
       }
     });
     menuItemChordAccuracy.setIcon(null);
     mnStatistics.add(menuItemChordAccuracy);
     
     menuItemLessonEvolution = new JMenuItem("Lesson Evolution Chart ...");
     menuItemLessonEvolution.setMnemonic('l');
     menuItemLessonEvolution.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         showEvolutionChart();
       }
     });
     menuItemLessonEvolution.setIcon(null);
     mnStatistics.add(menuItemLessonEvolution);
     
     mnConfiguration = new JMenu("Configuration");
     mnConfiguration.setMnemonic('c');
     menuBar.add(mnConfiguration);
     
     menuItemChordManager = new JMenuItem("Manage Chord Samples ...");
     menuItemChordManager.setMnemonic('s');
     menuItemChordManager.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         showChordManager();
       }
     });
     mnConfiguration.add(menuItemChordManager);
     
     menuItemLessonManager = new JMenuItem("Manage Lessons ...");
     menuItemLessonManager.setMnemonic('l');
     menuItemLessonManager.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         showLessonManager();
       }
     });
     mnConfiguration.add(menuItemLessonManager);
     
     menuItemScoreReset = new JMenuItem("Reset Scores!");
     menuItemScoreReset.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         resetScores();
       }
     });
     mnConfiguration.add(menuItemScoreReset);
     
     mnHelp = new JMenu("Help");
     mnHelp.setMnemonic('h');
     menuBar.add(mnHelp);
     
     menuItemHelp = new JMenuItem("Online Help (opens browser)");
     menuItemHelp.setMnemonic('h');
     menuItemHelp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
     menuItemHelp.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         try{
          URI uri = new URI("http://chords.btanase.ro/documentation");
           Desktop.getDesktop().browse(uri);
         }catch(Exception ex){
           log.error("Can't launch user browser ", ex);
           JOptionPane.showMessageDialog(ApplicationMainWindow.this, "Web browser cannot be launched automatically. \n " +
                 "Please go to chords.btanase.ro to read the documentation ", "Error launching browser", JOptionPane.ERROR_MESSAGE);
         }
       }
     });
     mnHelp.add(menuItemHelp);
     
     menuItemAbout = new JMenuItem("About ...");
     menuItemAbout.setMnemonic('a');
     menuItemAbout.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         showAboutWindow();
       }
     });
     mnHelp.add(menuItemAbout);
     contentPane = new JPanel();
     contentPane.setBackground(new Color(33, 98, 120));
     contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
     setContentPane(contentPane);
     contentPane.setLayout(new MigLayout("inset 0", "[][280:355.00,grow][left]", "[48.00][][23.00,grow]"));
     
     txtpnToStartTraining = new JTextPane();
     txtpnToStartTraining.setEditable(false);
     txtpnToStartTraining.setText("To start training select a lesson below and click Go\r\nDon't forget that you can (and I highly recommend to) create your own lessons!\r\nJust go to the menu above: Configuration -> Manage Lessons");
     Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
     
     panel = new JPanel();
     panel.setBackground(new Color(33, 98, 120));
     panel.setBorder(null);
     contentPane.add(panel, "cell 0 0,grow");
     panel.setLayout(new MigLayout("inset 0", "[56px]", "[80px][]"));
     
     lblNewLabel = new JLabel("");
     lblNewLabel.setBackground(new Color(33, 98, 120));
     lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
     lblNewLabel.setIcon(new ImageIcon(ApplicationMainWindow.class.getResource("/res/tme_small.png")));
     panel.add(lblNewLabel, "cell 0 0,alignx center,aligny center");
 
     txtpnToStartTraining.setBorder(UIManager.getBorder("Button.border"));
     contentPane.add(txtpnToStartTraining, "cell 1 0,grow");
     
     btnStartTest = new JButton("Go!");
     btnStartTest.setFont(new Font("Tahoma", Font.BOLD, 16));
     btnStartTest.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         onBtnStartTestActionPerformed();
       }
     });
     contentPane.add(btnStartTest, "cell 2 0,alignx left,growy");
     
     JScrollPane scrollPane = new JScrollPane();
     contentPane.add(scrollPane, "cell 0 1 3 2,grow");
     
     jlistLessons = new JList();
     jlistLessons.addMouseListener(new MouseAdapter() {
 
       @Override
       public void mouseClicked(MouseEvent evt) {
         if (evt.getClickCount() == 2){
           btnStartTest.doClick();
         }
       }
       
     });
     jlistLessons.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
     scrollPane.setViewportView(jlistLessons);
     
     
     jlistLessons.addKeyListener(new KeyListener() {
       
       @Override
       public void keyTyped(KeyEvent e) {
         // TODO Auto-generated method stub
         
       }
       
       @Override
       public void keyReleased(KeyEvent e) {
         // TODO Auto-generated method stub
         
       }
       
       @Override
       public void keyPressed(KeyEvent e) {
         if (e.getKeyCode() == KeyEvent.VK_ENTER){
           btnStartTest.doClick();
         }
         
       }
     });
     initModels();
     
   }
 
 
   protected void resetScores() {
     int result = JOptionPane.showConfirmDialog(this, "Are you sure you wish to RESET all scores? \n " +
         "You will loose all progress!", "Reset scores", JOptionPane.YES_NO_OPTION);
     
     if (result == JOptionPane.YES_OPTION){
       ScoreDao scoreDao = ChordLearningApp.getInjector().getInstance(ScoreDao.class);
       scoreDao.resetScores();
     }
   }
 
 
   private void showLessonManager() {
     Injector injector = ChordLearningApp.getInjector();
     
     LessonManagerFrame frame = injector.getInstance(LessonManagerFrame.class);
     frame.setLocationRelativeTo(this);
     frame.setVisible(true);
     
   }
 
 
   private void showEvolutionChart() {
     Injector injector = ChordLearningApp.getInjector();
     LessonEvolutionDialog led = injector.getInstance(LessonEvolutionDialog.class);
     led.setLocationRelativeTo(this);
     led.setVisible(true);
     
   }
 
 
   private void showChordAccuracy() {
     ChordAccuracyFrame caFrame = ChordLearningApp.getInjector().getInstance(ChordAccuracyFrame.class);
     caFrame.setLocationRelativeTo(this);
     caFrame.setVisible(true);
     
   }
 
 
   private void showChordManager() {
     ChordManagerFrame cdf = ChordLearningApp.getInjector().getInstance(ChordManagerFrame.class);
     cdf.setLocationRelativeTo(this);
     cdf.setVisible(true);
     
   }
 
 
   private void onBtnStartTestActionPerformed() {
     Lesson lesson = (Lesson) jlistLessons.getSelectedValue();
     
     if (lesson == null){
       JOptionPane.showMessageDialog(this, "You must select a lesson!", "No Selection",
           JOptionPane.WARNING_MESSAGE);
       return;
     }
     
     
     if (lesson.getType().equals(LessonType.SINGLE)){
       LessonSSRFrame ltf = new LessonSSRFrame(lesson);
       ChordLearningApp.getInjector().injectMembers(ltf);
       ltf.setLocationRelativeTo(this);
       ltf.setVisible(true);
     }else if (lesson.getType().equals(LessonType.PROGRESSION)){
       
       LessonCPRFrame lcp = new LessonCPRFrame(lesson);
       ChordLearningApp.getInjector().injectMembers(lcp);
       lcp.setLocationRelativeTo(this);
       lcp.setVisible(true);
     }
     
     
   }
 
 
   private void initModels(){
     EventListModel<Lesson> listModel = new EventListModel<Lesson>(m_lessons.getAll());
     jlistLessons.setModel(listModel);
   }
   private void addPopup(final Component component, final JPopupMenu popup) {
   }
 
 
   private void showAboutWindow() {
     AboutDialog aboutDialog = ChordLearningApp.getInjector().getInstance(AboutDialog.class);
     aboutDialog.setLocationRelativeTo(this);
     aboutDialog.setVisible(true);
   }
 }
