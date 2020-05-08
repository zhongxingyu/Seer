 package phylogenySurvey;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 
 import javax.swing.JApplet;
 
 public class SurveyApplet extends JApplet {
 
 	private SurveyUI surveyUI;
 	private boolean scoringEnabled;
 	private String password;
 
 	public void init() {
 		scoringEnabled = false;
 		String scoreModeString = getParameter("Scoring");
 		password = getParameter("Password");
 		if ((scoreModeString != null) && (password != null)) {
 			if (scoreModeString.equals("true")) {
 				scoringEnabled = true;
 			}
 		}
 		setSize(800, 800);
 	}
 
 	public void start() {
 		surveyUI = new SurveyUI(this.getContentPane());
 		surveyUI.setupUI(scoringEnabled, password);
 	}
 
 	public void stop() {
 		surveyUI.reset();
 	}
 
 	public String getTreeXML() {
 		String result = "";
 		try {
 			result = URLEncoder.encode(surveyUI.getState(), "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 	public void setTreeXML(String newTreeXML) {
		if (newTreeXML.indexOf("OrganismLabel") == -1) {
 			surveyUI.loadOrganisms();
 		} else {
 			try {
 				surveyUI.setState(URLDecoder.decode(newTreeXML, "UTF-8"));
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 }
