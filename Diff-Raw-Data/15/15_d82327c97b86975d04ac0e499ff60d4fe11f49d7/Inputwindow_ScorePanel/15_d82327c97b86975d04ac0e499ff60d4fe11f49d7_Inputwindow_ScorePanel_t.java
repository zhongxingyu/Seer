 package FRC_Score_Sys;
 
 import java.awt.Color;
 import java.awt.GridLayout;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 
 public class Inputwindow_ScorePanel extends JPanel {
 	/**
 	 * 
 	 */
 	private InputWindow			myParent;
 	private static final long	serialVersionUID	= 1;
 	// Standard Objects
 	private int					TotalScore			= 0;
 	private int					FinalScore			= 0;
 
 	private String				matchID;
 	private String				myColor;
 
 	// Input Objects
 	Inputwindow_SingleScoreRow	Low;
 	Inputwindow_SingleScoreRow	Mid;
 	Inputwindow_SingleScoreRow	Hig;
 	Inputwindow_SingleScoreRow	Pyr;
 
 	InputWindow_OptRow			R1;
 	InputWindow_OptRow			R2;
 	InputWindow_OptRow			R3;
 
 	Inputwindow_PenaltyInput	PenRow;
 
 	Inputwindow_PanelTotal		TotPanel;
 
 	public Inputwindow_ScorePanel(InputWindow parent, Color new_color, SingleMatch Match) {
 		myParent = parent;
 		setLayout(new GridLayout(0, 1, 0, 0));
 		setBackground(new_color);
 
 		setBorder(BorderFactory.createLineBorder(Color.BLACK));
 		// Header 1
 		String[] heads1 = { "", "Auto", "Tele", "Total" };
 		Inputwindow_SectionHeader Sect1 = new Inputwindow_SectionHeader("Disk Points", heads1);
 		this.add(Sect1);
 		matchID = Match.MatchID();
 		myColor = Match.aColor();
 
 		// Score Rows
 		Low = new Inputwindow_SingleScoreRow(this, "Low", 1, true, Match.DisksLA, Match.DisksLT);
 		Mid = new Inputwindow_SingleScoreRow(this, "Mid", 2, true, Match.DisksMA, Match.DisksMT);
 		Hig = new Inputwindow_SingleScoreRow(this, "High", 3, true, Match.DisksHA, Match.DisksHT);
 		Pyr = new Inputwindow_SingleScoreRow(this, "Pyramid", 5, false, 0, Match.DisksP);
 
 		Low.setBackground(new_color);
 		Mid.setBackground(new_color);
 		Hig.setBackground(new_color);
 		Pyr.setBackground(new_color);
 
 		this.add(Low);
 		this.add(Mid);
 		this.add(Hig);
 		this.add(Pyr);
 		// DQ/Climb Rows
 		// Header 2
 		String[] heads2 = { "Robot", "Climb", "DQ'ed?" };
 		Inputwindow_SectionHeader Sect2 = new Inputwindow_SectionHeader("Robot Options", heads2);
 		this.add(Sect2);
 
 		R1 = new InputWindow_OptRow(this, String.valueOf(Match.Robot1), Match.Climb1, Match.Dq1);
 		R2 = new InputWindow_OptRow(this, String.valueOf(Match.Robot2), Match.Climb2, Match.Dq2);
 		R3 = new InputWindow_OptRow(this, String.valueOf(Match.Robot3), Match.Climb3, Match.Dq3);
 		R1.setBackground(new_color);
 		R2.setBackground(new_color);
 		R3.setBackground(new_color);
 
 		this.add(R1);
 		this.add(R2);
 		this.add(R3);
 
 		// Penalties Row
 		// Header 3
 		String[] heads3 = { "Fouls", "T-Fouls" };
 		Inputwindow_SectionHeader Sect3 = new Inputwindow_SectionHeader("Penalties", heads3);
 		this.add(Sect3);
 		PenRow = new Inputwindow_PenaltyInput(this, Match.Foul, Match.TFoul);
 		this.add(PenRow);
 		// Total Rows
 		// Header 4
 		String[] heads4 = { "Score", "Fouls", "Final" };
 		Inputwindow_SectionHeader Sect4 = new Inputwindow_SectionHeader("Score Roundup", heads4);
 		this.add(Sect4);
 		TotPanel = new Inputwindow_PanelTotal();
 		this.add(TotPanel);
 		FinalScore = TotPanel.GetFinal(TotalScore, 0);
 	}
 
 	public int DoRefresh(int Penalties) {
 		TotalScore = Low.GetScore(3) + Mid.GetScore(3) + Hig.GetScore(3) + Pyr.GetScore(3) + R1.GetClimb() + R2.GetClimb() + R3.GetClimb();
 		FinalScore = TotPanel.GetFinal(TotalScore, Penalties);
 		return FinalScore;
 	}
 
 	public void ForwardRefresh() {
 		myParent.DoCalc();
 	}
 
 	public int GetFinalScore() {
 		return FinalScore;
 	}
 
 	public int GetPenalties() {
 		return PenRow.GetPenalties();
 	}
 
 	public SingleMatch GetRawData() {
 		SingleMatch A = new SingleMatch(matchID, myColor);
 		A.DisksLA = Low.GetAutoCount();
 		A.DisksLT = Low.GetTeleCount();
 		A.DisksMA = Mid.GetAutoCount();
 		A.DisksMT = Mid.GetTeleCount();
 		A.DisksHA = Hig.GetAutoCount();
 		A.DisksHT = Hig.GetTeleCount();
 		A.DisksP = Pyr.GetTeleCount();
		A.Climb1 = R1.GetRawClimb();
		A.Climb2 = R2.GetRawClimb();
		A.Climb3 = R3.GetRawClimb();
 		A.Dq1 = R1.isDQ();
 		A.Dq2 = R2.isDQ();
 		A.Dq3 = R3.isDQ();
 		A.Foul = PenRow.GetFoulCount();
 		A.TFoul = PenRow.GetTFoulCount();
 		A.Score = FinalScore;
 		return A;
 	}
 
 	public void RequestClear() {
 		Low.RequestClear();
 		Mid.RequestClear();
 		Hig.RequestClear();
 		Pyr.RequestClear();
 		R1.RequestClear();
 		R2.RequestClear();
 		R3.RequestClear();
 		PenRow.RequestClear();
 	}
 }
