 package cha.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import cha.domain.Board;
 import cha.event.EventBus;
 import cha.event.Event;
 import cha.event.IEventHandler;
 
 @SuppressWarnings("serial")
 public class ButtonPanel extends JPanel implements IEventHandler,
 ActionListener {
 
 	private JButton startMissionButton;
 	private JButton yesButton;
 	private JButton noButton;
 	public JButton nextButton;
 	private JButton doneButton;
 	private JButton setScoreButton;
 	private JLabel timer;
 	private TextPanel tp;
 
 	private JPanel startButtons;
 	private JPanel missionButtons;
 	private JPanel successButtons;
 	private JPanel currentPanel;
 	private JPanel challengePanel;
 	private JPanel setScore;
 
 	//challengePanel items.
 	int noOfOpponents;
 	String[] allTeams;
 	JComboBox<String> opponentCombo;
 	JComboBox<Integer> scoreCombo;
 	JButton startChallenge;
 	JButton challengeButton;
 	JLabel label;
 
 	public ButtonPanel(TextPanel panel) {
 		EventBus.getInstance().register(this);
 		this.tp = panel;
 		this.setPreferredSize(new Dimension(500, 50));
 
 
 		//**********PANELS*********************************************
 		
 		currentPanel = new JPanel();
 		startButtons = prettyPanel();
 		missionButtons = prettyPanel();
 		successButtons = prettyPanel();
 		challengePanel=prettyPanel();
 		setScore=prettyPanel();
 		//*************************************************************
 
 		//**********COMBO BOXES******************************************
 		opponentCombo=new JComboBox<String>();
 		opponentCombo.setPreferredSize(new Dimension(150, 30));
 		challengePanel.add(opponentCombo);
 		opponentCombo.setVisible(true);
 		
 		scoreCombo=new JComboBox<Integer>();//{1,2,3,4};
 		int[] bets={0,1,2,3,4,5,6,7};
 		for(int i:bets)
 			scoreCombo.addItem(bets[i]);
 		scoreCombo.setPreferredSize(new Dimension(150, 30));
 		setScore.add(scoreCombo);
 		scoreCombo.setVisible(true);
 		//*************************************************************
 
 		//**********BUTTONS********************************************
 		yesButton = prettyButton("Yes");
 		noButton = prettyButton("No");
 		nextButton = prettyButton("Next");
 		doneButton = prettyButton("Done");
 		startMissionButton=new JButton("Start Mission");
 		challengeButton = prettyButton("Start Challenge");
 		setScoreButton =prettyButton("Set Score");
 		//Add listeners to buttons
 		startMissionButton.addActionListener(this);
 		yesButton.addActionListener(this);
 		noButton.addActionListener(this);
 		nextButton.addActionListener(tp);
 		doneButton.addActionListener(this);
 		challengeButton.addActionListener(this);	
 		setScoreButton.addActionListener(this);		
 		//Divide buttons into panels
 		startButtons.add(startMissionButton);
 		missionButtons.add(nextButton, BorderLayout.WEST);
 		missionButtons.add(doneButton, BorderLayout.CENTER);
 		successButtons.add(noButton);
 		successButtons.add(yesButton);
 		challengePanel.add(challengeButton);
 		setScore.add(setScoreButton);
 		//Set visibility
 		startMissionButton.setVisible(true);
 		nextButton.setVisible(true);
 		doneButton.setVisible(true);
 		yesButton.setVisible(true);
 		noButton.setVisible(true);
 		challengeButton.setVisible(true);
 		setScoreButton.setVisible(true);
 		//*************************************************************
 
 		//**********TIMER**********************************************
 		timer = new JLabel();
 		timer.setFont(new Font("Dialog", Font.BOLD, 20));
 		missionButtons.add(timer, BorderLayout.EAST);
 		timer.setVisible(true);
 		//*************************************************************
 
 		//**********THIS PANEL*****************************************
 		this.setBackground(Color.GREEN);
 		this.add(startButtons, BorderLayout.CENTER);
 		this.add(missionButtons, BorderLayout.CENTER);
 		this.add(successButtons, BorderLayout.CENTER);
 		this.add(challengePanel, BorderLayout.CENTER);
 		this.add(setScore, BorderLayout.CENTER);
 		startButtons.setVisible(false);
 		missionButtons.setVisible(false);
 		successButtons.setVisible(false);
 		challengePanel.setVisible(false);
 		setScore.setVisible(false);
 		//*************************************************************
 
 		currentPanel=missionButtons;
 		setPanel();
 	}
 
 	/*
 	 * Makes the buttons look pretty.
 	 */
 	private JButton prettyButton(String s){
 		JButton temp= new JButton(s);
 		temp.setPreferredSize(new Dimension(150, 30));
 
 		return temp;
 	}
 	private JPanel prettyPanel(){
 		JPanel temp= new JPanel();
 		temp.setPreferredSize(new Dimension(400, 40));
 		temp.setBackground(Color.WHITE);
 		return temp;
 	}
 
 	/*
 	 * Switches between the different button-containing panels.
 	 */
 	private void setPanel(){
 		startButtons.setVisible(false);
 		missionButtons.setVisible(false);
 		successButtons.setVisible(false);
 		challengePanel.setVisible(false);
 		currentPanel.setVisible(true);
 	}
 
 	private void updateChallengeCombo(){
 		noOfOpponents=Board.getInstance().getNumberOfPieces()-1;
 		allTeams=new String[noOfOpponents];
 		for(int i=0; i<=noOfOpponents; i++){
 			String aTeam=Board.getInstance().getTeamName(i);
 			System.out.println("Active teams name:"+ Board.getInstance().getTurn().getPiece().getTeam().getName());
 			if(aTeam!=Board.getInstance().getTurn().getPiece().getTeam().getName()){
 				System.out.println("Adding "+aTeam+" to comboBox");
 				opponentCombo.addItem(aTeam);
 			}
 			else System.out.println("Won't add "+aTeam+" to comboBox");
 		}
 	}
 
 	private String teamChosen(){
 		return (String) opponentCombo.getSelectedItem();
 	}
 
 	@Override
 	public void action(Event e, Object o, Object p) {
 		if (e == Event.TimeOver) {
 			System.out.println("BP: TimeOver");
 			Board.getInstance().getTurn().getTurnType().missionDone();
 		}if (e == Event.MakeABet) {
 			System.out.println("BP: MakeABet");
 			currentPanel=startButtons;
 			setPanel();
 			startMissionButton.setVisible(false);
 		}
 		//Checked
 		if (e == Event.UpdateBet) {
 			System.out.println("BP: UpdateBet");
 			currentPanel=startButtons;
 			setPanel();
 			startMissionButton.setVisible(true);
 		//Checked	
 		} else if (e == Event.StartMission) {
 			System.out.println("BP: StartMission");
 			currentPanel=missionButtons;
 			setPanel();
 		} else if (e == Event.GetChallengeScore) {
 			System.out.println("BP: GetChallengeScore");
 			currentPanel=setScore;
 			setPanel();
 		} else if (e == Event.MissionOver) {
 			System.out.println("BP: NormalTurnDone");
 					currentPanel=successButtons;
 					setPanel();
 				
 		} else if (e == Event.NextPlayer) {
 			//DOES NOTHING
 		} else if (e == Event.TimeTick) {
 			//System.out.println("BP: TimeTick");
 			String time = (String) o;
 			timer.setText(time);
 		} else if (e == Event.IsChallenge) {
 			System.out.println("BP: IsChallenge");
 			updateChallengeCombo();
 			currentPanel=challengePanel;
 			setPanel();
 		}
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == startMissionButton) {
 			if(Board.getInstance().getTurn().getTurnType()==null){	//If NormalTurn is to be initiated
 				Board.getInstance().initNormalTurn();//TileContainerPanel.getTemporaryBet());
 			}
 			
 			//WHAT DOES THIS DO?
 			TileContainerPanel.setBetable(true);
 			for (TilePanel panel : TileContainerPanel.getTilePanels()) {
 				panel.notBetable();
 			}
 			Board.getInstance().getTurn().startMission();
 		} 
 		else if (e.getSource()==challengeButton){
 			String opp=teamChosen();
 			Board.getInstance().getTurn().setTempOpp(opp);
 			Board.getInstance().getTurn().setTurnType();
 			currentPanel=startButtons;
 			setPanel();
 		}
 		else if (e.getSource()==setScoreButton){
 			int score=(Integer)scoreCombo.getSelectedItem();
 			Board.getInstance().getTurn().getTurnType().setScore(score);
 			currentPanel=startButtons;
 			setPanel();
 		}
 		else if (e.getSource() == doneButton) {		//mission shall end
 			System.out.println("ButtonPanel: Done button pressed.");
 			Board.getInstance().stopMission();
 		} 
 		else if (e.getSource() == yesButton) {	//turn shall end
 			Board.getInstance().getTurn().finishTurn(true);
 			//Board.getInstance().newTurn();
 		} 
 		else if (e.getSource() == noButton) {		//turn shall end
 			Board.getInstance().getTurn().finishTurn(false);
 			//Board.getInstance().newTurn();
 
 		}
 	}
 }
