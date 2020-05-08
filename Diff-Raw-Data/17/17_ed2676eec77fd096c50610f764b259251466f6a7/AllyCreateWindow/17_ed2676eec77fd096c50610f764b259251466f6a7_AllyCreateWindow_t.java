 package FRC_Score_Sys.AllyCreate;
 
 import java.awt.Component;
 import java.awt.GridLayout;
import java.awt.event.WindowEvent;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import FRC_Score_Sys.MainMenu;
 import FRC_Score_Sys.TeamRankObj;
 
 public class AllyCreateWindow extends JFrame {
 	private static final long	serialVersionUID	= 1L;
 	private MainMenu myParent;
 	
 	private int TeamCount = 0;
 	
 	private int SelectedAlly = 0;
 	private int SelectedPick = 1;
 	
 	private JPanel LPanel;
 	private JPanel RPanel;
 	
 	public AllyCreateWindow(MainMenu parent){
 		myParent = parent;
 		this.setTitle("Create Alliances");
 		setLayout(new GridLayout(0, 2, 0, 0));
 		String s = "Select 'Yes' for 3v3.\nSelect 'No' for 2v2.";
 		String tit = "How many teams per alliances?";
 		int perform = JOptionPane.showConfirmDialog(null, s, tit, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
 		if(perform == JOptionPane.YES_OPTION) TeamCount = 3;
 		if(perform == JOptionPane.NO_OPTION) TeamCount = 2;
 		List<TeamRankObj> teams = myParent.CommHandle.SqlTalk.FetchTeamlist(true, null);
 		
 		LPanel = new JPanel();
 		RPanel = new JPanel();
 		LPanel.setLayout(new GridLayout(0, 1, 0, 0));
 		RPanel.setLayout(new GridLayout(0, 3, 0, 0));
 		getContentPane().add(LPanel);
 		getContentPane().add(RPanel);
 		
 		int i = 0;
 		for(TeamRankObj team : teams){
 			i++;
 			if(i <= 8){
 				// Create top 8.
 				AllyTopRow SRow = new AllyTopRow(this, i, team.ID, TeamCount);
 				LPanel.add(SRow);
 			} else {
 				// Create others.
 				AllyAvailRow SRow = new AllyAvailRow(this, i, team.ID);
 				RPanel.add(SRow);
 			}
 		}
 		this.pack();
 		UpdateSelect();
 	}
 	
 	private void UpdateSelect(){
 		boolean Complete = false;
 		if(SelectedAlly == 8 && SelectedPick == 1) {
 			if(TeamCount == 3) {
 				SelectedPick = 2;
 				SelectedAlly = 9;
 			} else {
 				Complete = true;
 			}
 		} else if(SelectedAlly == 1 && SelectedPick == 2){
 			Complete = true;
 		}
 		if(!Complete){
 			int Moving = 1;
 			if(SelectedPick == 2) Moving = -1;
 			SelectedAlly = SelectedAlly + Moving;
 			for (Component c : LPanel.getComponents()) {
 				if (c instanceof AllyTopRow) { 
 					AllyTopRow r = ((AllyTopRow)c);
 					if(r.Rank() == SelectedAlly){
 						r.DoSelect(SelectedPick);
 					} else {
 						r.DoSelect(0);
 					}
 				}
 			}
 		} else {
			String msg = "You are about to move into Quarterfinal Mode. Once this is done, you can not move back to Qualifications.\nWarning: If you select 'NO' you're changes will be lost.\n Continue?";
 			String tit = "Save Alliances?";
 			int perform = JOptionPane.showConfirmDialog(null, msg, tit, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
 			if(perform == JOptionPane.YES_OPTION) PerformSave();
			pullThePlug();
 		}
 	}
 	
 	private void PerformSave(){
 		
 		//TODO: Write to new table, allies
 		
 		String a11 = "11 " + GetMatchString(1) + " " + GetMatchString(8);
 		String a12 = "12 " + GetMatchString(1) + " " + GetMatchString(8);
 		String a13 = "13 " + GetMatchString(1) + " " + GetMatchString(8);
 		
 		String a21 = "21 " + GetMatchString(2) + " " + GetMatchString(7);
 		String a22 = "22 " + GetMatchString(2) + " " + GetMatchString(7);
 		String a23 = "23 " + GetMatchString(2) + " " + GetMatchString(7);
 		
 		String a31 = "31 " + GetMatchString(3) + " " + GetMatchString(6);
 		String a32 = "32 " + GetMatchString(3) + " " + GetMatchString(6);
 		String a33 = "33 " + GetMatchString(3) + " " + GetMatchString(6);
 		
 		String a41 = "41 " + GetMatchString(4) + " " + GetMatchString(5);
 		String a42 = "42 " + GetMatchString(4) + " " + GetMatchString(5);
 		String a43 = "43 " + GetMatchString(4) + " " + GetMatchString(5);
 		
 		String[] out = {a11,a12,a13, a21,a22,a23, a31,a32,a33, a41,a42,a43};
 		myParent.SwitchToElims(out);
 	}
 	
 	public void SetCapt(int Rank, int Team){
 		for (Component c : LPanel.getComponents()) {
 			if (c instanceof AllyTopRow) { 
 				AllyTopRow r = ((AllyTopRow)c);
 				if(r.Rank() == Rank) r.SetCapt(Team);
 			}
 		}
 	}
 	
 	public int GetCapt(int Rank){
 		for (Component c : LPanel.getComponents()) {
 			if (c instanceof AllyTopRow) { 
 				AllyTopRow r = ((AllyTopRow)c);
 				if(r.Rank() == Rank) return r.TeamID();
 			}
 		}
 		return 0;
 	}
 	private String GetMatchString(int Rank){
 		for (Component c : LPanel.getComponents()) {
 			if (c instanceof AllyTopRow) { 
 				AllyTopRow r = ((AllyTopRow)c);
 				if(r.Rank() == Rank){
 					String t1 = String.valueOf(r.TeamID());
 					String t2 = String.valueOf(r.GetPick(1));
 					String t3 = String.valueOf(r.GetPick(2));
 					return t1 + " 0 " + t2 + " 0 " + t3 + " 0";
 				}
 			}
 		}
 		return "";
 	}
 	public boolean SetValue(int NewVal, boolean isSeed){
 		for (Component c : LPanel.getComponents()) {
 			if (c instanceof AllyTopRow) { 
 				AllyTopRow r = ((AllyTopRow)c);
 				if(r.Rank() == SelectedAlly) {
 					r.SetValue(SelectedPick, NewVal);
 					if(isSeed){
 						int LowTeam = GetNextTeam();
 						int t = SelectedAlly;
 						int LastCapt = GetCapt(8); 
 						SetCapt(8,LowTeam);
 						for(int i=7;i!=t;i--){
 							int thisCapt = GetCapt(i); 
 							SetCapt(i,LastCapt);
 							LastCapt = thisCapt;
 						}
 					}
 					UpdateSelect();
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	private int GetNextTeam(){
 		int LowRank = 99999;
 		int LowTeam = 0;
 		for (Component c : RPanel.getComponents()) {
 			if (c instanceof AllyAvailRow) { 
 				AllyAvailRow r = ((AllyAvailRow)c);
 				if(r.Rank() < LowRank && r.CanBePicked){
 					LowRank = r.Rank();
 					LowTeam = r.TeamID();
 				}
 			}
 		}
 		for (Component c : RPanel.getComponents()) {
 			if (c instanceof AllyAvailRow) { 
 				AllyAvailRow r = ((AllyAvailRow)c);
 				if(r.Rank() == LowRank){
 					r.setCantPick();
 				}
 			}
 		}
 		return LowTeam;
 	}
	public void pullThePlug() {
		WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
		dispatchEvent(wev);
	}
 }
