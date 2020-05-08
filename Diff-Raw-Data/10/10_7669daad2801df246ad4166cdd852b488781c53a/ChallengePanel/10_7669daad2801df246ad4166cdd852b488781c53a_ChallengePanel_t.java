 package cha.gui;
 
 import javax.swing.JOptionPane;
 
 import cha.domain.Board;
 import cha.domain.Challenge;
 import cha.domain.Piece;
 
 public class ChallengePanel {
 
 	public ChallengePanel() {
 		String inputOppTeam;
 		int numberOfTeams = Board.getInstance().getNumberOfPieces();
 		Piece oppTeam = null;
 		
 		while (true) {
 			try {
 				inputOppTeam = JOptionPane
 						.showInputDialog("Which team would you like to compete against?");
 
 				if (inputOppTeam == null) {
 					return;
 				}
 
 				for (int i = 0; i < numberOfTeams; i++) {
 					if (inputOppTeam.contains(Board.getInstance()
 							.getTeamName(i))) {
 						oppTeam = Board.getInstance().getPiece(i);
						return;
 					}
 				}
 			} catch (NumberFormatException e) {
 			}
 
 			JOptionPane.showMessageDialog(null, "There is no such team");
 		}
 	}
 
 	public int pointsEarned() {
 		String pointInput;
 		int points;
 
 		
 		while (true) {
 			pointInput = JOptionPane
 					.showInputDialog("How many missions did you complete?");
 
 			try {
 				points = Integer.parseInt(pointInput);
 				if (pointInput == null)
					continue;
 
 				if (points >= 0 && points <= 7) {
 					System.out.println("Missions accepted " + points);
 					break;
 				}
 			} catch (NumberFormatException e) {
 			}
 		}
 
 		return points;
 
 	}
 }
