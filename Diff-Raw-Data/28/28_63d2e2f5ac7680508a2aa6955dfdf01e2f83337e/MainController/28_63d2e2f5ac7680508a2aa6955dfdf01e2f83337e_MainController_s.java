 package de.phaenovum.controller;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import uk.co.caprica.vlcj.binding.LibVlc;
 import uk.co.caprica.vlcj.runtime.RuntimeUtil;
 
 import com.sun.jna.Native;
 
 import de.phaenovum.model.Question;
 import de.phaenovum.model.Team;
 import de.phaenovum.util.Logger;
 import de.phaenovum.view.PatternPanel;
 import de.phaenovum.view.QuestionPanel;
 import de.phaenovum.view.RankingPanel;
 import de.phaenovum.view.ScoreLabel;
 import de.phaenovum.view.ScorePanel;
 
 //import uk.co.caprica.vlcj.binding.LibVlc;
 //import uk.co.caprica.vlcj.runtime.RuntimeUtil;
 
 //import com.sun.jna.Native;
 
 /*
  *   Main entrypoint for the program
  */
 public class MainController{
 	public static final Color ORANGE = new Color(255, 230, 20);
 	public static final Color SELECTED = new Color(120,250,250);
 	//public static final String IMAGE_PATH = "/home/johannes/Bilder/MetaksiBilder/";
 	public static final String IMAGE_PATH = "/home/phaenovum/Wettbewerbsprogramm/media_metaksi/";
 	public static final String XML_PATH = "sample.xml";
 	public static final String XML_TEAM = "team.xml";
 	//public static final String SOUND_PATH = "/home/johannes/Musik/";
 	public static final String SOUND_PATH = "/home/phaenovum/Wettbewerbsprogramm/media_metaksi/";
 	int values[] = {0,1,2,3,4,5};
 	private Logger logger;
 	public JFrame frame;
 	private JPanel pnlCheck = new JPanel();
 	private RankingPanel pnlRanking;
 	public HashMap<Integer,Question> questionTable;
 	private JLabel lblImage;
 	private ScorePanel scorePanel;
 	public QuestionPanel questPanel;
 	public PatternPanel patternPanel = new PatternPanel(values, "Frage auswählen");
 
 	private ScoreLabel[] lblTeam;
 
 	public TeamController teamController;
 	public SerialController serialController;
 
 	private ArrayList<HashMap<String,String>> questionList;
 	private ArrayList<HashMap<String,String>> teamList;
 	private XMLController xmlQuestion;
 	private XMLController xmlTeam;
 
 	private int changeID;
 	public MainController(int changeID){
 		this.changeID = changeID;
 		init();
 	}
 	/**
 	 * Main methode of melius
 	 * @param args
 	 */
 
 	public void init(){
 
 		/*
 		logger = new Logger("melius.log");
 		logger.logMessage("Testlog", true);
 		 */
 		// Load vlcJ Library into our runtime
 		// @info: If you are using this software, this is the critical step
 
 		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(),LibVlc.class);
 		System.out.println(RuntimeUtil.getLibVlcLibraryName());
 		System.out.println(LibVlc.class);
 		frame = new JFrame("METAXI");
 		ImageIcon img = new ImageIcon(IMAGE_PATH + "icon-metaksi.png");
 		frame.setIconImage(img.getImage());
 		frame.setSize(400, 400);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		scorePanel = new ScorePanel();
 		
 		frame.setVisible(true);
 		frame.getContentPane().add(BorderLayout.NORTH,scorePanel);
 		/*
 		frame.getContentPane().add(BorderLayout.CENTER, panel);
 		panel.start();
 		 */
 		// Init the XML Controller 
 		xmlQuestion = new XMLController(new File(XML_PATH),"frage");
 		xmlQuestion.readXMLFile();
 		questionList = xmlQuestion.getQuestionList();
 		if(questionList.get(0) == null){
 			System.out.println("Something went wrong");
 		}
 		// Loop through all questions and create an ID <-> Question HashMap 
 		questionTable = new HashMap<Integer,Question>();
 
 		for(int i=0;i<questionList.size();i++){
 			System.out.println(""+i);
 			Question quest = new Question(questionList.get(i));
 			System.out.println("Parsed question with id: "+questionList.get(i).get("id"));
 			questionTable.put(Integer.parseInt(questionList.get(i).get("id")),quest);
 		}
 		
 		xmlTeam = new XMLController(new File(XML_TEAM),"team");
 		xmlTeam.readXMLFile();
 		teamList = xmlTeam.getTeamList();
 		teamController = new TeamController();
 		lblTeam = new ScoreLabel[teamList.size()];
 		for(int i = 0; i < teamList.size(); i++){
 			System.out.println(""+i);
 			teamController.addTeam(new Team(teamList.get(i)));
 			System.out.println("Parsed Team with id: "+teamList.get(i).get("id"));
 			lblTeam[i] = new ScoreLabel(i, "", Color.BLACK);
 			scorePanel.addTeam(lblTeam[i]);
 		}
 		updateTeam();
 		loadEmpty();
 	}
 
 	public void reloadTeam(String path){
 		System.out.println("reload team: " + path);
 		xmlTeam = new XMLController(new File(path),"team");
 		xmlTeam.readXMLFile();
 		teamList = xmlTeam.getTeamList();
 		teamController = new TeamController();
 		lblTeam = new ScoreLabel[teamList.size()];
 		scorePanel.removeAll();
 		for(int i = 0; i < teamList.size(); i++){
 			System.out.println(""+i);
 			teamController.addTeam(new Team(teamList.get(i)));
 			System.out.println("Parsed Team with id: "+teamList.get(i).get("id"));
 			lblTeam[i] = new ScoreLabel(i, "", Color.BLACK);
 			scorePanel.addTeam(lblTeam[i]);
 		}
 	}
 	
 	public void reloadXML(String path){
 		System.out.println("Reload XML: " + path);
 		xmlQuestion = new XMLController(new File(path),"frage");
 		xmlQuestion.readXMLFile();
 		loadEmpty();
 		questionList = xmlQuestion.getQuestionList();
 		if(questionList.get(0) == null){
 			System.out.println("Something went wrong");
 		}
 		// Loop through all questions and create an ID <-> Question HashMap 
 		questionTable = new HashMap<Integer,Question>();
 
 		for(int i=0;i<questionList.size();i++){
 			System.out.println(""+i);
 			Question quest = new Question(questionList.get(i));
 			System.out.println("Parsed question with id: "+questionList.get(i).get("id"));
 			questionTable.put(Integer.parseInt(questionList.get(i).get("id")),quest);
 		}
 	}
 
 	public void loadQuestion(int id){
 		//frame.setVisible(false);
 		if(questPanel != null) questPanel.stop();
 		if(questionTable.get(id).getType().equals("pattern")){
 			patternPanel = questionTable.get(id).getPatternPanel();
 			loadPattern();
 			return;
 		}
 		frame.getContentPane().removeAll();
 		frame.getContentPane().add(BorderLayout.NORTH,scorePanel);
 		questPanel = questionTable.get(id).getQuestionPanel();
 		frame.getContentPane().add(BorderLayout.CENTER,questPanel);
 		frame.invalidate();
 		frame.validate();
 		frame.repaint();
 		questPanel.start();
 	}
 
 	public void loadPattern(){
 		//frame.setVisible(false);
 		if(questPanel != null) questPanel.stop();
 		frame.getContentPane().removeAll();
 		frame.getContentPane().add(BorderLayout.NORTH,scorePanel);
 		frame.getContentPane().add(BorderLayout.CENTER, patternPanel);
 		frame.invalidate();
 		frame.validate();
 		frame.repaint();
 	}
 
 	public void loadEmpty(){
 		if(questPanel != null) questPanel.stop();
 		frame.getContentPane().removeAll();
 		frame.invalidate();
 		frame.validate();
 		frame.repaint();
 	}
 
 	public void correct(){
 		if(questPanel != null) questPanel.stop();
 		int rand = ((int) (Math.random()*3) + 1);
 		BufferedImage bufferedImage;
 		try {
 			bufferedImage = ImageIO.read(new File(IMAGE_PATH + "richtig" + rand + ".png"));
 		} catch (IOException e) {
 			bufferedImage = null;
 			e.printStackTrace();
 		}
 		lblImage = new JLabel(new ImageIcon(bufferedImage));
 		lblImage.setBackground(Color.GREEN);
 		frame.getContentPane().removeAll();
 		frame.getContentPane().add(BorderLayout.NORTH,scorePanel);
 		frame.getContentPane().add(BorderLayout.CENTER, lblImage);
 		frame.invalidate();
 		frame.validate();
 		frame.repaint();
 	}
 
 	public void wrong(){
 		if(questPanel != null) questPanel.stop();
 		int rand = ((int) (Math.random()*3) + 1);
 		BufferedImage bufferedImage;
 		try {
 			bufferedImage = ImageIO.read(new File(IMAGE_PATH + "falsch" + rand + ".png"));
 		} catch (IOException e) {
 			bufferedImage = null;
 			e.printStackTrace();
 		}
 		lblImage = new JLabel(new ImageIcon(bufferedImage));
 		lblImage.setBackground(Color.RED);
 		frame.getContentPane().removeAll();
 		frame.getContentPane().add(BorderLayout.NORTH,scorePanel);
 		frame.getContentPane().add(BorderLayout.CENTER, lblImage);
 		frame.invalidate();
 		frame.validate();
 		frame.repaint();	
 	}
 
 	public void splash(){
 		if(questPanel != null) questPanel.stop();
 		int rand = ((int) (Math.random()*4) + 1);
 		BufferedImage bufferedImage;
 		try {
 			bufferedImage = ImageIO.read(new File(IMAGE_PATH + "splash" + rand + ".png"));
 		} catch (IOException e) {
 			bufferedImage = null;
 			e.printStackTrace();
 		}
 		lblImage = new JLabel(new ImageIcon(bufferedImage));
 		frame.getContentPane().removeAll();
 		frame.getContentPane().add(BorderLayout.NORTH,scorePanel);
 		frame.getContentPane().add(BorderLayout.CENTER, lblImage);
 		frame.invalidate();
 		frame.validate();
 		frame.repaint();	
 	}
 
 	public void points(){
 		if(questPanel != null) questPanel.stop();
 		pnlRanking = new RankingPanel(teamController, true);
 		frame.getContentPane().removeAll();
 		//frame.getContentPane().add(BorderLayout.NORTH,scorePanel);
 		frame.getContentPane().add(BorderLayout.CENTER, pnlRanking);
 		frame.invalidate();
 		frame.validate();
 		frame.repaint();	
 	}
 	
 	public void score(){
 		if(questPanel != null) questPanel.stop();
 		pnlRanking = new RankingPanel(teamController, false);
 		frame.getContentPane().removeAll();
 		//frame.getContentPane().add(BorderLayout.NORTH,scorePanel);
 		frame.getContentPane().add(BorderLayout.CENTER, pnlRanking);
 		frame.invalidate();
 		frame.validate();
 		frame.repaint();	
 	}
 
 	public JLabel getImageLabel(){
 		JLabel label = new JLabel(lblImage.getIcon());
 		return label;
 	}
 
 	public void updateTeam(){
 		Team team = null;
 		for(int i = 0; i < teamController.teamList.size(); i++){
 			team = teamController.getTeam(i);
 			lblTeam[i].setBackground(team.getTeamColor());
 			lblTeam[i].setText(team.teamName + ": " + team.points);
 		}
 		frame.invalidate();
 		frame.validate();
 		frame.repaint();
 	}
 
 	public static void main(String[] args){
 		MainController mc = new MainController(0);
 		ViewController vc = new ViewController(mc);
		SerialController serialController = new SerialController(vc);
 	}
 }
