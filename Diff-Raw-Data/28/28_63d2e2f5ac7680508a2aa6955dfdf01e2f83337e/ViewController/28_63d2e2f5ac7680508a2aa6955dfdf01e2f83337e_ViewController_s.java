 package de.phaenovum.controller;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.Enumeration;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.border.Border;
 import javax.swing.border.TitledBorder;
 
 import uk.co.caprica.vlcj.player.MediaPlayer;
 import uk.co.caprica.vlcj.player.MediaPlayerFactory;
 import de.phaenovum.model.Team;
 import de.phaenovum.view.BuzzerFrame;
 import de.phaenovum.view.BuzzerFrameOptions;
 import de.phaenovum.view.MultipleChoicePanel.MCListener;
 import de.phaenovum.view.OptionButton;
 import de.phaenovum.view.OptionsPanel;
 import de.phaenovum.view.PatternPanel;
 import de.phaenovum.view.PatternPanel.PListener;
 import de.phaenovum.view.RankingPanel;
 import de.phaenovum.view.ScoreLabel;
 import de.phaenovum.view.ScoreLabel.SlListener;
 import de.phaenovum.view.ScorePanel;
 import de.phaenovum.view.ScoreTable;
 import de.phaenovum.view.SortPanelOptions.WindowSortListener;
 import de.phaenovum.view.TeamFrame;
 import de.phaenovum.view.TeamFrame.TfListener;
 
 public class ViewController implements ActionListener, PListener, MCListener, SlListener, TfListener, WindowSortListener{
 	private int[] values = {0,1,2,3,4,5};
 	private int nextQuestionID = 0;
 	private boolean buzzerTeamShown = false;
 	private MainController mc;
 	private JFrame frame;
 	private JPanel pnlMain = new JPanel();
 	private JPanel pnlLeft = new JPanel();
 	private JPanel pnlRight = new JPanel();
 	private JPanel pnlSouth = new JPanel();
 	private JLabel lblImage = new JLabel();
 	private JPanel pnlSound = new JPanel();
 	private JPanel pnlScreen = new JPanel();
 	private RankingPanel pnlRanking;
 	private JPanel pnlAdd = new JPanel();
 	private JPanel pnlSub = new JPanel();
 	private JPanel pnlQuest = new JPanel();
 	private JButton btnAdd[];
 	private JButton btnSub[];
 	private JButton btnSoundBuzz = new JButton("Buzzer");
 	private JButton btnSoundCorrect = new JButton("Correct");
 	private JButton btnSoundWrong = new JButton("Wrong");
 	private JButton btnSoundApplause = new JButton("Applause");
 	private JButton btnSoundMelius = new JButton("Gedudel");
 	private JButton btnSoundStop = new JButton("Stop");
 	private OptionsPanel pnlOptions = new OptionsPanel();
 	private PatternPanel pnlPattern = new PatternPanel(values, "Frage auswählen");
 	private ScoreLabel[] lblTeam;
 	//private ScoreLabel[] lblTeam2;
 	private ScorePanel pnlScore = new ScorePanel();
 	//private ScorePanel pnlScore2 = new ScorePanel();
 	private JButton btnLoadQuest = new JButton("load Question");
 	private JButton btnLoadPattern = new JButton("load Pattern");
 	private JButton btnNextCategory = new JButton("next Category");
 	private JButton btnNextQuestion = new JButton("next Question");
 	private JButton btnLoadXML = new JButton("load XML");
 	private JButton btnLoadTeam = new JButton("load Teams");
 	private OptionButton btnCorrect = new OptionButton("Correct");
 	private OptionButton btnWrong = new OptionButton("Wrong");
 	private JButton btnPoints = new JButton("Points");
 	private JButton btnScore = new JButton("Score");
 	private ScoreTable tblScore;
 	private JPanel pnlTeam = new JPanel();
 	private JButton btnSaveRound = new JButton("save round");
 	private JButton btnReset = new JButton("reset points");
 	private JButton btnUnlockBuzzer = new JButton("Unlock Buzzer");
 	private JButton btnSplash = new JButton("Splash");
 	private JTextField tfQuest = new JTextField("0");
 	private Border border = BorderFactory.createEmptyBorder(20, 20, 20, 20);
 	private TitledBorder tbSlide;
 	private TitledBorder tbSound;
 	private TitledBorder tbTeam;
 	private TitledBorder tbScreen;
 
 	private BuzzerFrame buzzerFrame;
 	private BuzzerFrameOptions buzzerFrameOptions;
 	private final JFileChooser fcQuest = new JFileChooser();
 	private final JFileChooser fcTeam = new JFileChooser();
 
 	private int questionNum = 0;
 	private int correct = -1;
 
 	private boolean buzzerLocked = false;
 
 	private MediaPlayerFactory factory;
 	private MediaPlayer mediaPlayer;
 	public ViewController(MainController mc){
 		this.mc = mc;
 		init();
 	}
 
 	private void init(){
 		fcQuest.setCurrentDirectory(new File(MainController.XML_PATH));
 		fcTeam.setCurrentDirectory(new File(MainController.XML_TEAM));
 		frame = new JFrame("Control Frame");
 		frame.setIconImage(mc.frame.getIconImage());
 		frame.setSize(400, 400);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		pnlMain.setBorder(border);
 		pnlMain.setLayout(new BorderLayout(10, 10));
 
 		tbScreen = BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(3, 3,
 				3, 3, Color.black),"Screen");
 		tbScreen.setTitleJustification(TitledBorder.CENTER);
 		tbScreen.setTitleFont(new Font("Arial",Font.BOLD,20));
 		pnlScreen.setBorder(tbScreen);
 		tbSound = BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(3, 3,
 				3, 3, Color.black),"Sound");
 		tbSound.setTitleJustification(TitledBorder.CENTER);
 		tbSound.setTitleFont(new Font("Arial",Font.BOLD,20));
 		pnlSound.setBorder(tbSound);
 		tbTeam = BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(3, 3,
 				3, 3, Color.black),"Team");
 		tbTeam.setTitleJustification(TitledBorder.CENTER);
 		tbTeam.setTitleFont(new Font("Arial",Font.BOLD,20));
 		pnlTeam.setBorder(tbTeam);
 		tbSlide = BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(3, 3,
 				3, 3, Color.black),"Slide");
 		tbSlide.setTitleJustification(TitledBorder.CENTER);
 		tbSlide.setTitleFont(new Font("Arial",Font.BOLD,20));
 		pnlQuest.setBorder(tbSlide);
 
 		btnLoadQuest.addActionListener(this);
 		btnLoadPattern.addActionListener(this);
 		btnNextQuestion.addActionListener(this);
 		btnCorrect.button.addActionListener(this);
 		btnWrong.button.addActionListener(this);
 		btnUnlockBuzzer.addActionListener(this);
 		btnSplash.addActionListener(this);
 		btnSoundBuzz.addActionListener(this);
 		btnSoundCorrect.addActionListener(this);
 		btnSoundWrong.addActionListener(this);
 		btnSoundApplause.addActionListener(this);
 		btnSoundStop.addActionListener(this);
 		btnSoundMelius.addActionListener(this);
 		btnScore.addActionListener(this);
 		btnPoints.addActionListener(this);
 		btnLoadXML.addActionListener(this);
 		btnLoadTeam.addActionListener(this);
 		btnNextCategory.addActionListener(this);
 		btnReset.addActionListener(this);
 		btnSaveRound.addActionListener(this);
 
 		pnlQuest.setLayout(new BoxLayout(pnlQuest, BoxLayout.Y_AXIS));
 		pnlQuest.add(tfQuest);
 		pnlQuest.add(btnLoadQuest);
 		pnlQuest.add(btnNextQuestion);
 		pnlQuest.add(btnNextCategory);
 		pnlQuest.add(btnLoadPattern);
 		pnlQuest.add(btnUnlockBuzzer);
 		pnlQuest.add(btnLoadXML);
 		pnlQuest.add(btnLoadTeam);
 		
 		pnlLeft.setLayout(new BoxLayout(pnlLeft, BoxLayout.Y_AXIS));
 		pnlLeft.add(pnlQuest);
 
 		pnlScore = new ScorePanel();
 		//pnlScore2 = new ScorePanel();
 		lblTeam = new ScoreLabel[mc.teamController.teamList.size()];
 		//lblTeam2 = new ScoreLabel[mc.teamController.teamList.size()];
 		btnAdd = new JButton[lblTeam.length];
 		btnSub = new JButton[lblTeam.length];
 		for(int i = 0; i < lblTeam.length; i++){
 			Team team = mc.teamController.getTeam(i);
 			lblTeam[i] = new ScoreLabel(i, team.teamName + ": " + team.points + "/" + team.score, team.teamColor);
 			//lblTeam2[i] = new ScoreLabel(i, team.teamName + ": " + team.score, team.teamColor);
 			lblTeam[i].setSlListener(this);
 			//lblTeam2[i].setSlListener(this);
 			btnAdd[i] = new JButton(team.teamName + " + ");
 			btnSub[i] = new JButton(team.teamName + " - ");
 			btnAdd[i].addActionListener(this);
 			btnSub[i].addActionListener(this);
 			pnlAdd.add(btnAdd[i]);
 			pnlSub.add(btnSub[i]);
 			pnlScore.addTeam(lblTeam[i]);
 			//pnlScore2.addTeam(lblTeam2[i]);
 		}
 
 		pnlPattern.setListener(this);
 		pnlScreen.setLayout(new GridLayout(5,1));
 		pnlScreen.add(btnCorrect);
 		pnlScreen.add(btnWrong);
 		pnlScreen.add(btnSplash);
 		pnlScreen.add(btnPoints);
 		pnlScreen.add(btnScore);
 
 		pnlSound.setLayout(new GridLayout(6,1));
 		pnlSound.add(btnSoundBuzz);
 		pnlSound.add(btnSoundCorrect);
 		pnlSound.add(btnSoundWrong);
 		pnlSound.add(btnSoundApplause);
 		pnlSound.add(btnSoundMelius);
 		pnlSound.add(btnSoundStop);
 
 		pnlTeam.setLayout(new GridLayout(2,1));
 		pnlTeam.add(btnSaveRound);
 		pnlTeam.add(btnReset);
 		
 		pnlRight.setLayout(new BoxLayout(pnlRight, BoxLayout.Y_AXIS));
 		pnlScreen.setAlignmentX(JPanel.CENTER_ALIGNMENT);
 		pnlSound.setAlignmentX(JPanel.CENTER_ALIGNMENT);
 		pnlTeam.setAlignmentX(JPanel.CENTER_ALIGNMENT);
 		pnlRight.add(pnlScreen);
 		pnlRight.add(pnlSound);
 		pnlRight.add(pnlTeam);
 
 
 		tblScore = new ScoreTable(mc.teamController.teamList);
 		pnlSouth.setLayout(new BoxLayout(pnlSouth, BoxLayout.Y_AXIS));
 		pnlSouth.add(pnlAdd);
 		pnlSouth.add(pnlScore);
 		pnlSouth.add(pnlSub);
 		//pnlSouth.add(pnlScore2);
 		pnlSouth.add(tblScore);
 
 		pnlMain.add(pnlLeft, BorderLayout.LINE_START);
 		pnlMain.add(pnlSouth, BorderLayout.SOUTH);
 		pnlMain.add(pnlRight, BorderLayout.LINE_END);
 
 		frame.add(pnlMain);
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 
 		// Init audio interface
 		factory = new MediaPlayerFactory();
 		mediaPlayer = factory.newHeadlessMediaPlayer();
 
 		//Initial Splash screen
 		splash();
 	}
 
 	@Override
 	public void pAction(int id, int[] values) {
 		mc.patternPanel.selectQuestion(id);
 		tfQuest.setText(Integer.toString(values[id]));
 	}
 
 	private void loadPattern(){
 		mc.loadPattern();
 		pnlMain.removeAll();
 		pnlMain.add(BorderLayout.SOUTH,pnlSouth);
 		pnlMain.add(BorderLayout.LINE_START, pnlLeft);
 		pnlMain.add(BorderLayout.CENTER, pnlPattern);
 		pnlMain.add(BorderLayout.LINE_END, pnlRight);
 		frame.invalidate();
 		frame.validate();
 		frame.repaint();
 	}
 
 	private void loadQuestion(int id) throws Exception{
 		if(mc.questionTable.get(id).getType().equals("pattern")){
 			pnlPattern = mc.questionTable.get(id).getPatternPanel();
 			pnlPattern.setListener(this);
 			mc.loadQuestion(id);
 			loadPattern();
 			nextQuestionID = id + 70;
 			while(mc.questionTable.get(nextQuestionID) == null && nextQuestionID < 99999){nextQuestionID++;}	
 			return;
 		}
 		mc.loadQuestion(id);
 		pnlMain.removeAll();
 		pnlMain.add(BorderLayout.SOUTH,pnlSouth);
 		pnlMain.add(BorderLayout.LINE_START, pnlLeft);
 		pnlOptions = mc.questionTable.get(id).getOptionsPanel();
 		correct = mc.questionTable.get(id).correctAnswerID;
 		if(pnlOptions.pnlMC != null) pnlOptions.pnlMC.setMCListener(this);
 		if(pnlOptions.pnlSort != null) pnlOptions.pnlSort.setWindowSortListener(this);
 		pnlMain.add(BorderLayout.CENTER, pnlOptions);
 		pnlMain.add(BorderLayout.LINE_END, pnlRight);
 		frame.invalidate();
 		frame.validate();
 		frame.repaint();
 		int i = (id + 1);
 		while(mc.questionTable.get(i) == null && i < 99999){i++;}
 		if(i > nextQuestionID) nextQuestionID = i;
 	}
 
 	private void correct(){
 		mc.correct();
 		lblImage = mc.getImageLabel();
 		pnlMain.removeAll();
 		pnlMain.add(BorderLayout.SOUTH,pnlSouth);
 		pnlMain.add(BorderLayout.LINE_START, pnlLeft);
 		pnlMain.add(BorderLayout.CENTER, lblImage);
 		pnlMain.add(BorderLayout.LINE_END, pnlRight);
 		frame.invalidate();
 		frame.validate();
 		frame.repaint();
 		if(btnCorrect.check.isSelected()) mediaPlayer.playMedia(MainController.SOUND_PATH + "correct.wav");
 	}
 
 	private void wrong(){
 		mc.wrong();
 		lblImage = mc.getImageLabel();
 		pnlMain.removeAll();
 		pnlMain.add(BorderLayout.SOUTH,pnlSouth);
 		pnlMain.add(BorderLayout.LINE_START, pnlLeft);
 		pnlMain.add(BorderLayout.CENTER, lblImage);
 		pnlMain.add(BorderLayout.LINE_END, pnlRight);
 		frame.invalidate();
 		frame.validate();
 		frame.repaint();
 		if(btnWrong.check.isSelected()) mediaPlayer.playMedia(MainController.SOUND_PATH + "wrong.wav");
 	}
 
 	private void splash(){
 		mc.splash();
 		lblImage = mc.getImageLabel();
 		pnlMain.removeAll();
 		pnlMain.add(BorderLayout.SOUTH,pnlSouth);
 		pnlMain.add(BorderLayout.LINE_START, pnlLeft);
 		pnlMain.add(BorderLayout.CENTER, lblImage);
 		pnlMain.add(BorderLayout.LINE_END, pnlRight);
 		frame.invalidate();
 		frame.validate();
 		frame.repaint();
 	}
 	
 	private void points(){
 		mc.points();
 		pnlRanking = new RankingPanel(mc.teamController, true);
 		pnlMain.removeAll();
 		pnlMain.add(BorderLayout.SOUTH,pnlSouth);
 		pnlMain.add(BorderLayout.LINE_START, pnlLeft);
 		pnlMain.add(BorderLayout.CENTER, pnlRanking);
 		pnlMain.add(BorderLayout.LINE_END, pnlRight);
 		frame.invalidate();
 		frame.validate();
 		frame.repaint();
 	}
 
 	private void score(){
 		mc.score();
 		pnlRanking = new RankingPanel(mc.teamController, false);
 		pnlMain.removeAll();
 		pnlMain.add(BorderLayout.SOUTH,pnlSouth);
 		pnlMain.add(BorderLayout.LINE_START, pnlLeft);
 		pnlMain.add(BorderLayout.CENTER, pnlRanking);
 		pnlMain.add(BorderLayout.LINE_END, pnlRight);
 		frame.invalidate();
 		frame.validate();
 		frame.repaint();
 	}
 
 	private void loadXML(){
 		String path = MainController.XML_PATH;
 		int returnVal = fcQuest.showOpenDialog(btnLoadXML);
 		if(returnVal == JFileChooser.APPROVE_OPTION){
 			path = fcQuest.getSelectedFile().getPath();
 			pnlMain.removeAll();
 			mc.reloadXML(path);
 			nextQuestionID = 0;
 			tfQuest.setText("0");
 			splash();
 		}
 	}
 	
 	private void loadTeam(){
 		String path = MainController.XML_TEAM;
 		int returnVal = fcTeam.showOpenDialog(btnLoadTeam);
 		if(returnVal == JFileChooser.APPROVE_OPTION){
 			path = fcTeam.getSelectedFile().getPath();
 			mc.reloadTeam(path);
 			lblTeam = new ScoreLabel[mc.teamController.teamList.size()];
 			//lblTeam2 = new ScoreLabel[mc.teamController.teamList.size()];
 			pnlScore.removeAll();
 			//pnlScore2.removeAll();
 			pnlAdd.removeAll();
 			pnlSub.removeAll();
 			btnAdd = new JButton[lblTeam.length];
 			btnSub = new JButton[lblTeam.length];
 			for(int i = 0; i < lblTeam.length; i++){
 				Team team = mc.teamController.getTeam(i);
 				lblTeam[i] = new ScoreLabel(i, team.teamName + ": " + team.points + "/" + team.score, team.teamColor);
 				//lblTeam2[i] = new ScoreLabel(i, team.teamName + ": " + team.points, team.teamColor);
 				lblTeam[i].setSlListener(this);
 				//lblTeam2[i].setSlListener(this);
 				btnAdd[i] = new JButton(team.teamName + " + ");
 				btnSub[i] = new JButton(team.teamName + " - ");
 				btnAdd[i].addActionListener(this);
 				btnSub[i].addActionListener(this);
 				pnlAdd.add(btnAdd[i]);
 				pnlSub.add(btnSub[i]);
 				pnlScore.addTeam(lblTeam[i]);
 				//pnlScore2.addTeam(lblTeam2[i]);
 			}
 			updateTeam();
 		}
 	}
 
 	public void updateTeam(){
 		Team team = null;
 		for(int i = 0; i < mc.teamController.teamList.size(); i++){
 			team = mc.teamController.getTeam(i);
 			lblTeam[i].setBackground(team.getTeamColor());
 			lblTeam[i].setText(team.teamName + ": " + team.points + "/" + team.score);
 			//lblTeam2[i].setBackground(team.getTeamColor());
 			//lblTeam2[i].setText(team.teamName + ": " + team.score);
 			btnAdd[i].setText(team.teamName + " +");
 			btnSub[i].setText(team.teamName + " -");
 		}
 		tblScore.updateTeam();
 		frame.invalidate();
 		frame.validate();
 		frame.repaint();
 		mc.updateTeam();
 	}
 	
 	private void nextCategory(){
 		int q = Integer.parseInt(tfQuest.getText());
 		q = ((q/1000) + 1)*1000;
 		tfQuest.setText(Integer.toString(q));
 		int i = (q + 1);
 		while(mc.questionTable.get(i) == null && i < 99999){i++;}
 		if(i > nextQuestionID) nextQuestionID = i;
 	}
 	
 	private void saveRound(){
 		for(int i = 0; i < mc.teamController.getTeamList().size(); i++){
 			mc.teamController.getTeam(i).saveRound();
 		}
 		updateTeam();
 	}
 		
 	private void resetPoints(){
 		for(int i = 0; i < mc.teamController.getTeamList().size(); i++){
 			mc.teamController.getTeam(i).resetPoints();
 		}
 		updateTeam();
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if(e.getSource() == btnLoadPattern){
 			loadPattern();
 		}else if(e.getSource() == btnLoadXML){
 			loadXML();
 		}else if(e.getSource() == btnLoadTeam){
 			loadTeam();
 		}else if(e.getSource() == btnSoundBuzz){
 			mediaPlayer.playMedia(MainController.SOUND_PATH + "buzzer.wav");
 		}else if(e.getSource() == btnSoundCorrect){
 			mediaPlayer.playMedia(MainController.SOUND_PATH + "correct.wav");
 		}else if(e.getSource() == btnSoundWrong){
 			mediaPlayer.playMedia(MainController.SOUND_PATH + "wrong.wav");
 		}else if(e.getSource() == btnSoundApplause){
 			mediaPlayer.playMedia(MainController.SOUND_PATH + "applause.wav");
 		}else if(e.getSource() == btnSoundMelius){
 			mediaPlayer.playMedia(MainController.SOUND_PATH + "melius.wav");
 		}else if(e.getSource() == btnSoundStop){
 			mediaPlayer.stop();
 		}else if(e.getSource() == btnLoadQuest){
 			questionNum = Integer.parseInt(tfQuest.getText());
 			try{loadQuestion(questionNum);}
 			catch(Exception exception){JOptionPane.showMessageDialog(frame, "Frage " + questionNum + " nicht vorhanden oder fehlerhaft", "Frage nicht vorhanden", JOptionPane.ERROR_MESSAGE);
 			int i = questionNum;
 			while(mc.questionTable.get(i) == null && i < 99999){i++;}
 			if(i > nextQuestionID) nextQuestionID = i;}
 		}else if(e.getSource() == btnNextQuestion){
 			tfQuest.setText(Integer.toString(nextQuestionID));
 		}else if(e.getSource() == btnNextCategory){
 			nextCategory();
 		}else if(e.getSource() == btnCorrect.button){
 			correct();
 		}else if(e.getSource() == btnSplash){
 			splash();
 		}else if(e.getSource() == btnScore){
 			score();
 		}else if(e.getSource() == btnPoints){
 			points();
 		}else if(e.getSource() == btnSaveRound){
 			saveRound();
 		}else if(e.getSource() == btnReset){
 			resetPoints();
 		}else if(e.getSource() == btnUnlockBuzzer){
 			System.out.println("Unlock Buzzer");
 			buzzerLocked = false;
 			if(buzzerFrame.isVisible()){
 				buzzerFrame.setVisible(false);
 			}
 			if(buzzerFrameOptions != null && buzzerTeamShown){
 				pnlLeft.remove(buzzerFrameOptions.getContentPane()); 
 				buzzerTeamShown = false;
 			}
 			frame.invalidate();
 			frame.validate();
 			frame.repaint();
 			mc.frame.invalidate();
 			mc.frame.validate();
 			mc.frame.repaint();
 		}else if(e.getSource() == btnWrong.button){
 			wrong();
 		}else{
 			for(int i = 0; i < btnAdd.length; i++){
 				if(e.getSource() == btnAdd[i]){
 					mc.teamController.getTeam(i).addPoints(1);
 					updateTeam();
 				}else if(e.getSource() == btnSub[i]){
 					mc.teamController.getTeam(i).substractPoints(1);
 					updateTeam();
 				}
 			}
 		}
 
 	}
 
 	@Override
 	public void mcAction(int id) {
 		if(id == correct){
 			correct();
 		}else{
 			wrong();
 		}
 	}
 
 	@Override
 	public void mcSelect(int id) {
 		mc.questPanel.pnlMC.option[id].setColor(MainController.SELECTED);
 	}
 
 	@Override
 	public void mcDeselect(int id) {
 		mc.questPanel.pnlMC.option[id].setColor(MainController.ORANGE);	
 	}
 
 	@Override
 	public void slAction(int id) {
 		System.out.println("Change Team");
 		TeamFrame tf = new TeamFrame(mc.teamController.getTeam(id), this);
 	}
 
 
 	public void teamBuzzerd(int id){
 		if(!buzzerLocked){
 			mediaPlayer.playMedia(MainController.SOUND_PATH + "buzzer.wav");
 			Team buzzerTeam = mc.teamController.getTeam(id);
 			buzzerFrame = new BuzzerFrame(buzzerTeam, mc.frame.getIconImage());
 			if(buzzerFrameOptions != null && buzzerTeamShown){ pnlRight.remove(buzzerFrameOptions.getContentPane()); buzzerTeamShown = false;}
 			buzzerFrameOptions = new BuzzerFrameOptions(buzzerTeam, this, mc.frame.getIconImage());
 			pnlLeft.add(buzzerFrameOptions.getContentPane());
 			buzzerTeamShown = true;
 			frame.invalidate();
 			frame.validate();
 			frame.repaint();
 			buzzerFrame.repaint();
 			buzzerFrame.pack();
 			buzzerFrame.setVisible(true);
 			// Lock Buzzer 
 			this.buzzerLocked = true;
 		}else{
 			// Do nothing dumbdidumin
 		}
 
 	}
 
 	@Override
 	public void listCorrect() {
 		if(mc.questPanel.pnlSort != null) mc.questPanel.pnlSort.correct();
 	}
 
 	@Override
 	public void listWrong() {
 		if(mc.questPanel.pnlSort != null) mc.questPanel.pnlSort.wrong();
 	}
 
 	@Override
 	public void listChanged(Enumeration elementsLeft,
 			Enumeration elementsRight) {
 		mc.questPanel.pnlSort.updateList(elementsLeft, elementsRight);		
 	}
 }
