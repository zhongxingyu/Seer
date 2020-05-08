 package cha.gui;
 
 import javax.swing.JOptionPane;
 
 import cha.domain.Board;
 
 public class ChallengePanel {
 	
 	public ChallengePanel(){
 		int numberOfTeams = Board.getInstance().getNumberOfPieces();
 				
 		while (true) {
 			try {
				String inputOppTeam = JOptionPane.showInputDialog("Which team would you like to compete against?");
 
 				if (inputOppTeam == null){
 					return;
 				}
 				
 				for(int i = 0; i < numberOfTeams; i++){
 					if(inputOppTeam.contains(Board.getInstance().getTeamName(i))){
 					break;
 					}
 				}
 				}				
 				catch (NumberFormatException e) {
 			}
 
 			JOptionPane.showMessageDialog(null, "There is no such team");
 		}
 	}
	
 }
