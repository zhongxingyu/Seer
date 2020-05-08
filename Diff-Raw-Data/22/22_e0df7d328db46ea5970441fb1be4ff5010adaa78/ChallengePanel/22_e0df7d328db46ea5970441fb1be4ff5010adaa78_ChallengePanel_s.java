 package cha.gui;
 
 import javax.swing.JOptionPane;
 
 import cha.domain.Board;
 import cha.domain.Challenge;
 
 public class ChallengePanel {
 	
 	public ChallengePanel(){
 		String inputOppTeam;
 		int numberOfTeams = Board.getInstance().getNumberOfPieces();
				
 		while (true) {
 			try {
 				inputOppTeam = JOptionPane.showInputDialog("Which team would you like to compete against?");
 
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
 		
		//Vet inte varf�r jag inte kan anropa getActivePiece()...
		//Du försöker anropa den i den här klassen, måste göras i Board. /Johan
		new Challenge(Board.getInstance().getActivePiece(), inputOppTeam, getTile(getActivePiece().getPosition()).getCategory());
 		
 	}
 }
