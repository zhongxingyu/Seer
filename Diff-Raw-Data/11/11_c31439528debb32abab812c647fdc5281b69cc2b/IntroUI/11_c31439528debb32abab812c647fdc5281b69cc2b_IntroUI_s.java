 package ee.ut.math.tvt.FutureTech;
 
 import java.awt.Font;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 import org.apache.log4j.Logger;
 import net.miginfocom.swing.MigLayout;
 
 public class IntroUI {
 
 	private final Logger log = Logger.getLogger(IntroUI.class);
 
 	private final JFrame frame = new JFrame();
 
 	private final JPanel contentPane = new JPanel();
 
 	public IntroUI() {
 		log.info("Creating frame");
 		this.createFrame();
 	}
 
 	private void createFrame() {
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setBounds(100, 100, 510, 500);
 
 		try {
 			Properties prop = new Properties();
 			log.info("Trying to read the application.properties file");
 			prop.load(new FileInputStream("conf//application.properties"));
 
 			log.info("Getting team data");
 			List<String> data = this.getData(prop);
 			JLabel logo = this.getLogo(prop);
 
 			prop.clear();
 
 			log.info("Trying to read the version.properties file");
 			prop.load(new FileInputStream("conf//version.properties"));
 
 			log.info("Getting version number");
 			String version = this.getSoftwareVersion(prop);
 
 			log.info("Creating panel for frame");
 			this.createPanel(data, logo, version);
 
 			frame.setContentPane(contentPane);
 			frame.setVisible(true);
 			log.info("Frame is now visible");
 		} catch (Exception e) {
 			log.error("Creating frame failed - " + e.getMessage());
 		}
 	}
 
 	private void createPanel(List<String> data, JLabel logo, String version) {
 		contentPane.setLayout(new MigLayout());
 
 		JLabel nameLabel = new JLabel(data.get(0));
 		nameLabel.setFont(new Font("", Font.PLAIN, 20));
 		nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		contentPane.add(nameLabel, "cell 0 0, center, height 50");
 
 		JLabel leaderLabel = new JLabel("Team leader: ");
 		leaderLabel.setText(leaderLabel.getText() + data.get(1));
 		contentPane.add(leaderLabel, "cell 0 1");
 
 		JLabel mailLabel = new JLabel("Team leader e-mail: ");
 		mailLabel.setText(mailLabel.getText() + data.get(2));
 		contentPane.add(mailLabel, "cell 0 2");
 
 		JLabel membersLabel = new JLabel("Team members: ");
 		membersLabel.setText(membersLabel.getText() + data.get(3));
 		contentPane.add(membersLabel, "cell 0 3");
 
 		contentPane.add(logo, "cell 0 4, center, pad 10 0 0 0, width 10, height 10");
 
 		JLabel versionLabel = new JLabel("Version: ");
 		versionLabel.setText(versionLabel.getText() + version);
 		contentPane.add(versionLabel, "cell 0 5, left, height 20");
 	}
 
 	private List<String> getData(Properties prop) {
 		List<String> data = new ArrayList<String>();
 
		data.add(prop.getProperty("teamName"));
		data.add(prop.getProperty("teamLeader"));
		data.add(prop.getProperty("teamLeaderMail"));
		data.add(prop.getProperty("teamMembers"));
 
 		return data;
 	}
 
 	private JLabel getLogo(Properties prop) throws IOException {
		BufferedImage myPicture = ImageIO.read(new File(prop.getProperty("teamLogo")));
 		return new JLabel(new ImageIcon(myPicture));
 	}
 
 	private String getSoftwareVersion(Properties prop) {
 		return prop.getProperty("build.number");
 	}
 }
