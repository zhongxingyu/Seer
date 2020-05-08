 package ee.ut.math.tvt.includeName;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 import org.apache.log4j.Logger;
 
 public class IntroUI {
 	static JFrame UIFrame;
 	private static final Logger log = Logger.getLogger(IntroUI.class);	//For every "Catch" block, add a call to this
 	//TODO Discuss what exactly do we want to catch with this logger.
 	
 	public void display() {
 		UIFrame = new JFrame();
 		GridBagConstraints c = new GridBagConstraints();
 		UIFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		UIFrame.setSize(420, 200);
 		UIFrame.setLayout(new GridBagLayout());
 		
 		Properties appProp = new Properties();
 		Properties verProp = new Properties();
 		BufferedImage teamIcon = null;
 		
 		try {
			appProp.load(new FileInputStream("application.properties"));
			verProp.load(new FileInputStream("version.properties"));
 		} catch(Exception e) {
 			log.debug(e.getMessage());
 			System.err.println("Failed to load properties files!");
 		}
 		
 		try {
			teamIcon = ImageIO.read(new File("etc/"+appProp.getProperty("team.logo.file")));
 		} catch (IOException e) {
 			log.debug(e.getMessage());
 			System.err.println("Team image file not found!");
 		}
 		
 		String members[] = appProp.getProperty("team.members").split(",");
 
 		int yPos = 0;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.ipadx = 20;
 		c.gridx = 0;
 		c.gridy = yPos++;
 		UIFrame.add(new JLabel("Team name:  "
 				+ appProp.getProperty("team.name")), c);
 		
 		c.gridy = yPos++;
 		UIFrame.add(new JLabel("Team leader:  "
 				+ appProp.getProperty("team.leader")), c);
 		
 		c.gridy = yPos++;
 		UIFrame.add(new JLabel("Team leader email:  "
 				+ appProp.getProperty("team.leader.email")), c);
 		
 		c.gridy = yPos++;
 		UIFrame.add(new JLabel("Members:"), c);
 		
 		for (int i = 0; i < members.length; i++) {
 			c.gridy = yPos++;
 			UIFrame.add(new JLabel("        " + members[i]), c);
 		}
 		
 		c.gridy = yPos++;
 		UIFrame.add(new JLabel("Version:  "
 				+ verProp.getProperty("build.number")), c);
 		
 		c.gridy = 0;
 		c.gridx = 1;
 		c.gridheight = yPos-1;
 		UIFrame.add(new JLabel(new ImageIcon(teamIcon.getScaledInstance(128, 128, Image.SCALE_SMOOTH))), c);
 
 		UIFrame.setVisible(true);
 		UIFrame.repaint();
 	}
 }
