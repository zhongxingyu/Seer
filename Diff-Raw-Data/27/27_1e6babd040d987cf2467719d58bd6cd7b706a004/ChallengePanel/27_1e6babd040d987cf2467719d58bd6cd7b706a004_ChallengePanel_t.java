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
		outerloop:
 		while (true) {
 			try {
 				inputOppTeam = JOptionPane.showInputDialog(null,"Which team would you like to compete against?",
 						"Chose an opponent team", JOptionPane.QUESTION_MESSAGE);
 
 				if (inputOppTeam == null) {
 					return;
 				}
 
 				for (int i = 0; i < numberOfTeams; i++) {
 					if (inputOppTeam.contains(Board.getInstance()
							.getTeamName(i))){
						if(inputOppTeam.contains(Board.getInstance().getActivePiece().getTeam().getName())){
							
							JOptionPane.showMessageDialog(null, "You can't compete against yourself",
									"Error", JOptionPane.ERROR_MESSAGE);
							continue outerloop;
						}
						else
 						oppTeam = Board.getInstance().getPiece(i);
 						Board.getInstance().startChallenge(oppTeam);
 						return;
						
 					}
 				}
 			} catch (NumberFormatException e) {
 			}
 
 			JOptionPane.showMessageDialog(null, "There is no such team");
 		}		
 
 
 	}
 
 
 	public static int pointsEarned() {
 		String pointInput;
 		int points;
 
 
 		while (true) {
 			pointInput = JOptionPane
					.showInputDialog(null, "How many missions did you complete?", "Missions", JOptionPane.QUESTION_MESSAGE);
 
 			try {
 				points = Integer.parseInt(pointInput);
 				if (pointInput == null)
 					continue;
 
 				if (points >= 0 && points <= 7) {
 					System.out.println("Missions accepted " + points);
					return points;
 				}
 			} catch (NumberFormatException e) {
 			}
			JOptionPane.showMessageDialog(null, "The number of completed missions has to be  between 0-7",

			"Error", JOptionPane.ERROR_MESSAGE);
 		}
 
		
 
 	}
 }
