 package ee.ut.math.tvt.BSS;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Toolkit;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import org.apache.log4j.Logger;
 
 
 public class IntroUI extends JFrame{
 	
 	private static final Logger log = Logger.getLogger(IntroUI.class);	
 	
 	private JLabel TName;
 	private JLabel TLeader;
 	private JLabel TLEmail;
 	private JLabel TMember1;
 	private JLabel TMember2;
 	private JLabel TMember3;
 	private JLabel TMember4;
 	private ImageIcon Logo;
 	private JLabel LogoLabel;
 	private JLabel Version;
 	
 	
 	public IntroUI(){
         super("IntroUI");
 		log.info("starting IntroUI");
 		try {
 
 			setLayout(new GridBagLayout());
 			GridBagConstraints c = new GridBagConstraints();
 
 			TName = new JLabel("Team name: Brewery Software Solutions");
 			c.gridx = 0;
 			c.gridy = 0;
 			c.weighty = 1.0;
 			c.weightx = 1.0;
 			add(TName, c);
 
 			TLeader = new JLabel("Team leader: Silver Tiik");
 			c.gridx = 0;
 			c.gridy = 1;
 			add(TLeader, c);
 
 			TLEmail = new JLabel("Team leader email: silvertiik7@gmail.com");
 			c.gridx = 0;
 			c.gridy = 2;
 			add(TLEmail, c);
 
 			TMember1 = new JLabel("Silver Tiik");
 			c.gridx = 0;
 			c.gridy = 3;
 			add(TMember1, c);
 
 			TMember2 = new JLabel("Kristian Hunt");
 			c.gridx = 0;
 			c.gridy = 4;
 			add(TMember2, c);
 
 			TMember3 = new JLabel("Tanel Joosep");
 			c.gridx = 0;
 			c.gridy = 5;
 			add(TMember3, c);
 
			TMember4 = new JLabel("Denis Žadan");
 			c.gridx = 0;
 			c.gridy = 6;
 			add(TMember4, c);
 
 			Version = new JLabel("Software version: 0.1");
 			c.gridx = 0;
 			c.gridy = 7;
 			add(Version, c);
 
 			Logo = new ImageIcon("logo.png");
 			LogoLabel = new JLabel(Logo);
 			c.gridx = 1;
 			c.gridy = 0;
 			c.gridheight = 8;
 			add(LogoLabel, c);
 
 			int width = 700;
 			int height = 400;
 			setSize(width, height);
 			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
 			setLocation((screen.width - width) / 2,
 					(screen.height - height) / 2);
 			setVisible(true);
 			log.info("Intro window is opened");
 		} catch (Exception e) {
 			log.error(e.getMessage());
 		} 
 		finally {
 		}       
 	}
 	
 	
 }
