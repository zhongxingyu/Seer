 package devhead.ru.jlaby;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.FilenameFilter;
 
 import javax.swing.GroupLayout;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 
 /**
  * jLaby according by laby
  * 
  * @author Kuznetsov Andrey
  * 
  */
 public class Laby extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 
 	final String PROGRAM_NAME = "Laby programming game (Swing version)";
 	final String DEVELOPER = "Andrey Kyznetsov";
 	final String EMAIL = "andreykyz@gmail.com";
 	
 	final static String LEVEL_PATH = "data/levels";
 	final static String IMAGES_PATH = "data/tiles";
 	
 	public Laby() {
 		final JFrame frame = this;
 		JPanel panel = new JPanel();
 		
 		JMenuItem miExit = new JMenuItem("Exit");
 		miExit.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 		});
 		JMenuItem miAbout = new JMenuItem("About");
 		miAbout.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JOptionPane.showMessageDialog(frame, DEVELOPER + "\n" + EMAIL, PROGRAM_NAME,
 						JOptionPane.PLAIN_MESSAGE);
 			}
 		});
 
 		JMenu mFile = new JMenu("File");
 		mFile.setMnemonic(KeyEvent.VK_F);
 		mFile.addSeparator();
 		mFile.add(miExit);
 
 		JMenu mHelp = new JMenu("Help");
 		mHelp.setMnemonic(KeyEvent.VK_H);
 		mHelp.add(miAbout);
 
 		JMenuBar menuBar = new JMenuBar();
 		menuBar.add(mFile);
 		menuBar.add(mHelp);
 		setJMenuBar(menuBar);
 		// setPreferredSize(new Dimension(DisplayWrapper.TABLE_WIDTH,
 		// DisplayWrapper.TABLE_HEIGHT));
 		FilenameFilter filenamefilter = new FilenameFilter() {
 			public boolean accept(File directory, String fileName) {
 				return fileName.endsWith(".laby");
 			}
 		};
 		File path = new File(Laby.LEVEL_PATH);// + Laby.class.getProtectionDomain().getCodeSource().getLocation().getPath());
 		JLabel languageLabel = new JLabel("Language:");
 		String[] language = {"C", "Python", "Java"};
 		JComboBox languageChooser = new JComboBox(language);
 		JLabel levelLabel = new JLabel("Level:");
		File[] arFiles = path.listFiles(filenamefilter);
		JComboBox levelChooser = new JComboBox(arFiles);
		LevelMap levelMap = new LevelMap("data/levels/1c.laby");
//		JLabel levelMap = new JLabel("field");
 		
 		JLabel helpArea = new JLabel("<html><b>This is help</b><br>First line<br>Second line</html>");
 
 		
 		JTextArea codeArea = new JTextArea();
 		
 		/* Layouts*/
 		GroupLayout layout = new GroupLayout(panel);
 		panel.setLayout(layout);
 		layout.setAutoCreateGaps(true);
 		layout.setAutoCreateContainerGaps(true);
 		
         layout.setHorizontalGroup(layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                         .addComponent(helpArea)
                         .addComponent(levelMap))
                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                         .addGroup(layout.createSequentialGroup()
                                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                         .addComponent(languageLabel)
                                         .addComponent(levelLabel))
                                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                         .addComponent(languageChooser)
                                         .addComponent(levelChooser))
                         )
                         .addComponent(codeArea))
         );
 		
         layout.setVerticalGroup(layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                         .addComponent(helpArea)
                         .addGroup(layout.createSequentialGroup()
                                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                         .addComponent(languageLabel)
                                         .addComponent(languageChooser))
                                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                         .addComponent(levelLabel)
                                         .addComponent(levelChooser))
                                 ))
                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                         .addComponent(levelMap)
                         .addComponent(codeArea))
         );
 
 		this.setContentPane(panel);
 		pack();
		levelMap.repaint();
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 	}
 
 	public static void main(String[] args) {
 		new Laby().setVisible(true);
 	}
 
 }
