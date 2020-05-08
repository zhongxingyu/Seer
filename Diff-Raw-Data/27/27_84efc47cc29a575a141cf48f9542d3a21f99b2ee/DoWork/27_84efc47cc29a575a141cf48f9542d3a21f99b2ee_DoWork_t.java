 
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author strivedi
  */
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.Scanner;
 import javax.swing.JTextField;
 
 
 public class DoWork {
 private final static String newline = "\n";   
       
    /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         int[][] playerScores;     
         playerScores = new int[6][9];
         int playerOneHandicap = 0;
         int playerTwoHandicap = 0;
         int playerThreeHandicap = 0;
         int playerFourHandicap = 0;
         int playerOneTotal;
         int playerTwoTotal;
         int playerThreeTotal;
         int playerFourTotal;
         int teamOneTotalScore = 0;
         int teamTwoTotalScore = 0;
         int handicapDifference;
         double teamOnePoints;
         double teamTwoPoints;
        
         
         
        // Get User Input
        //GetInput(playerScores, playerOneHandicap, playerTwoHandicap, playerThreeHandicap, playerFourHandicap);
   
        
        
        // Start Array
         // startArray(playerScores);
        
         
         // Print Handicap Holes Numbers:
         //System.out.print("Handicap    | ");
         //for (int row = 0; row < 1; row++) {
         //    for (int column = 0; column < playerScores[row].length; column++) {
         //        System.out.print(playerScores[row][column] + " ");
         //    }
         //    System.out.println(" ");
         //}
         
         
         // Print Orignal Submitted Values
         System.out.println("Player submitted values");
         
         // Print Hole Numbers
         //DisplayHoles(playerScores);
         
         
         // Print Original Player Scores and Totals
         //DisplayInitialScores(playerScores, playerOneHandicap, playerTwoHandicap, playerThreeHandicap, playerFourHandicap);
         
         
         // Display Team Totals and Award Points
         //System.out.println();
         //DisplayTeamScoresAndPoints();
         //System.out.println();
         
         
         
         
         //Display Current Scores
         System.out.println();
         
         //DisplayHeadsUpScores(playerScores);
         
         //Heads Up Match
         //HeadsUpMatch(playerScores);
         
         
     }
    public static void GetPlayerData() throws MalformedURLException, IOException {
         String tempArray[];
         tempArray = new String [140];
         String tempMatchArray[] = new String [20];
         
         try {
          java.net.URL url = new java.net.URL("http://tgl.snafilter.com/Team_Players_Handicap.txt");
             Scanner input = new Scanner(url.openStream());
             // Store variables in a temp array
             
             for (int i = 0; i < tempArray.length; i++) {
             tempArray[i] = input.next();
             }
         }
         catch (MalformedURLException ex) {
             ex.printStackTrace();
         }
         catch (IOException ex) {
             System.out.println("I/O Errors: no such file");
         }
         try {
             java.net.URL url = new java.net.URL("http://tgl.snafilter.com/Matches.txt");
             Scanner input = new Scanner(url.openStream());
             // Store variables in a temp array
             
             for (int i = 0; i < tempMatchArray.length; i++) {
             tempMatchArray[i] = input.next();
             }
         }
         catch (MalformedURLException ex) {
             System.out.println("Mathces File not found on the webserver");
         }
         catch (IOException ex) {
             System.out.println("I/O Errors: no such file");
         }
       
         
         //Define Your File
         //java.io.File fileToInput = new java.io.File("Team_Player_Handicap.txt");
         /* JFileChooser fileChooser = new JFileChooser();
         if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
             java.io.File fileToInput = fileChooser.getSelectedFile();
         
         */
         
         Global.teamPlayerHandicap = new String[20][7];
 
         
         int arryLgt = 0;
         for (int row = 0; row < Global.teamPlayerHandicap.length; row++){
             
             
             for (int column = 0; column < Global.teamPlayerHandicap[row].length; column++) {
 
                 //System.out.print(arryLgt);
                 Global.teamPlayerHandicap[row][column] = tempArray[arryLgt];
                 arryLgt = arryLgt + 1;
             }
         }
         
         //Display Ifo
         for (int row = 0; row < Global.teamPlayerHandicap.length; row++) {
             for (int column = 0; column < Global.teamPlayerHandicap[row].length; column++) {
                 System.out.print(Global.teamPlayerHandicap[row][column] + " ");
                 //TestFrame.jTextArea1.append(Global.teamPlayerHandicap[row][column] + " ");
             }
             System.out.println("");
             
         }
         
         Global.schedule = new int[10][2];
         arryLgt = 0;
         for (int row = 0; row < Global.schedule.length; row++){
             
             
             for (int column = 0; column < Global.schedule[row].length; column++) {
 
                 //System.out.print(arryLgt);
                 Global.schedule[row][column] = Integer.parseInt(tempMatchArray[arryLgt]);
                 arryLgt = arryLgt + 1;
             }
         }
         
         //Display Info
         for (int row = 0; row < Global.schedule.length; row++) {
             for (int column = 0; column < Global.schedule[row].length; column++) {
                 System.out.print(Global.schedule[row][column] + " ");
                 //TestFrame.jTextArea1.append(Global.schedule[row][column] + " ");
             }
             System.out.println("");
             
         }
     }
     
     public static void SaveValues() {
         Global.playerScores = new int[6][9];
       /*
         Global.playerOneHandicap = Integer.parseInt(TwilightGolfLeague.jTxt_PlayerOneHandicap.getText());
         Global.playerTwoHandicap = Integer.parseInt(TwilightGolfLeague.jTxt_PlayerTwoHandicap.getText());
         Global.playerThreeHandicap = Integer.parseInt(TwilightGolfLeague.jTxt_PlayerThreeHandicap.getText());
         Global.playerFourHandicap = Integer.parseInt(TwilightGolfLeague.jTxt_PlayerFourHandicap.getText());
         Global.teamOneNumber = Integer.parseInt(TwilightGolfLeague.jTxt_TeamOneNumber.getText());
         Global.teamTwoNumber = Integer.parseInt(TwilightGolfLeague.jTxt_TeamTwoNumber.getText());
        */
          
         //TwilightGolfLeague.jTxt_Player2Hole0.setText("5");
 
         // Really Should Remove This Eventually
          
         Global.playerScores[2][0] = Integer.parseInt(TwilightGolfLeague.jTxt_Player2Hole0.getText());
         Global.playerScores[2][1] = Integer.parseInt(TwilightGolfLeague.jTxt_Player2Hole1.getText());
         Global.playerScores[2][2] = Integer.parseInt(TwilightGolfLeague.jTxt_Player2Hole2.getText());
         Global.playerScores[2][3] = Integer.parseInt(TwilightGolfLeague.jTxt_Player2Hole3.getText());
         Global.playerScores[2][4] = Integer.parseInt(TwilightGolfLeague.jTxt_Player2Hole4.getText());
         Global.playerScores[2][5] = Integer.parseInt(TwilightGolfLeague.jTxt_Player2Hole5.getText());
         Global.playerScores[2][6] = Integer.parseInt(TwilightGolfLeague.jTxt_Player2Hole6.getText());
         Global.playerScores[2][7] = Integer.parseInt(TwilightGolfLeague.jTxt_Player2Hole7.getText());
         Global.playerScores[2][8] = Integer.parseInt(TwilightGolfLeague.jTxt_Player2Hole8.getText());
        
         Global.playerScores[3][0] = Integer.parseInt(TwilightGolfLeague.jTxt_Player3Hole0.getText());
         Global.playerScores[3][1] = Integer.parseInt(TwilightGolfLeague.jTxt_Player3Hole1.getText());
         Global.playerScores[3][2] = Integer.parseInt(TwilightGolfLeague.jTxt_Player3Hole2.getText());
         Global.playerScores[3][3] = Integer.parseInt(TwilightGolfLeague.jTxt_Player3Hole3.getText());
         Global.playerScores[3][4] = Integer.parseInt(TwilightGolfLeague.jTxt_Player3Hole4.getText());
         Global.playerScores[3][5] = Integer.parseInt(TwilightGolfLeague.jTxt_Player3Hole5.getText());
         Global.playerScores[3][6] = Integer.parseInt(TwilightGolfLeague.jTxt_Player3Hole6.getText());
         Global.playerScores[3][7] = Integer.parseInt(TwilightGolfLeague.jTxt_Player3Hole7.getText());
         Global.playerScores[3][8] = Integer.parseInt(TwilightGolfLeague.jTxt_Player3Hole8.getText());
         
         Global.playerScores[4][0] = Integer.parseInt(TwilightGolfLeague.jTxt_Player4Hole0.getText());
         Global.playerScores[4][1] = Integer.parseInt(TwilightGolfLeague.jTxt_Player4Hole1.getText());
         Global.playerScores[4][2] = Integer.parseInt(TwilightGolfLeague.jTxt_Player4Hole2.getText());
         Global.playerScores[4][3] = Integer.parseInt(TwilightGolfLeague.jTxt_Player4Hole3.getText());
         Global.playerScores[4][4] = Integer.parseInt(TwilightGolfLeague.jTxt_Player4Hole4.getText());
         Global.playerScores[4][5] = Integer.parseInt(TwilightGolfLeague.jTxt_Player4Hole5.getText());
         Global.playerScores[4][6] = Integer.parseInt(TwilightGolfLeague.jTxt_Player4Hole6.getText());
         Global.playerScores[4][7] = Integer.parseInt(TwilightGolfLeague.jTxt_Player4Hole7.getText());
         Global.playerScores[4][8] = Integer.parseInt(TwilightGolfLeague.jTxt_Player4Hole8.getText());
         
         Global.playerScores[5][0] = Integer.parseInt(TwilightGolfLeague.jTxt_Player5Hole0.getText());
         Global.playerScores[5][1] = Integer.parseInt(TwilightGolfLeague.jTxt_Player5Hole1.getText());
         Global.playerScores[5][2] = Integer.parseInt(TwilightGolfLeague.jTxt_Player5Hole2.getText());
         Global.playerScores[5][3] = Integer.parseInt(TwilightGolfLeague.jTxt_Player5Hole3.getText());
         Global.playerScores[5][4] = Integer.parseInt(TwilightGolfLeague.jTxt_Player5Hole4.getText());
         Global.playerScores[5][5] = Integer.parseInt(TwilightGolfLeague.jTxt_Player5Hole5.getText());
         Global.playerScores[5][6] = Integer.parseInt(TwilightGolfLeague.jTxt_Player5Hole6.getText());
         Global.playerScores[5][7] = Integer.parseInt(TwilightGolfLeague.jTxt_Player5Hole7.getText());
         Global.playerScores[5][8] = Integer.parseInt(TwilightGolfLeague.jTxt_Player5Hole8.getText());
         
         //Define Handicap Numbers
         if (TwilightGolfLeague.jRadioButton_FrontNine.isSelected()) {
             System.out.println("Front Nine");
             Global.playerScores[0][0] = 9;
             Global.playerScores[0][1] = 7;
             Global.playerScores[0][2] = 4;
             Global.playerScores[0][3] = 5;
             Global.playerScores[0][4] = 1;
             Global.playerScores[0][5] = 3;
             Global.playerScores[0][6] = 2;
             Global.playerScores[0][7] = 6;
             Global.playerScores[0][8] = 8;
             
         } else {
             System.out.println("Back Nine");
             Global.playerScores[0][0] = 9;
             Global.playerScores[0][1] = 6;
             Global.playerScores[0][2] = 4;
             Global.playerScores[0][3] = 1;
             Global.playerScores[0][4] = 8;
             Global.playerScores[0][5] = 3;
             Global.playerScores[0][6] = 7;
             Global.playerScores[0][7] = 5;
             Global.playerScores[0][8] = 2;
         }
         
     }
     public static void PopulateSubmitScoresInfo(){
         //TwilightGolfLeague.jComboBox_TeamSelection.getSelectedIndex();
         System.out.println(TwilightGolfLeague.jComboBox_TeamSelection.getSelectedIndex());
         int selectedTeam = TwilightGolfLeague.jComboBox_TeamSelection.getSelectedIndex();
       
         for (int row = 0; row < Global.schedule.length; row++ ){
             int temp = Global.schedule[row][0];
             if (temp == selectedTeam) {
                 Global.teamOneNumber = Global.schedule[row][0];
                 Global.teamTwoNumber = Global.schedule[row][1];
                 System.out.println("Team: " + Global.teamOneNumber + "vs Team: " + Global.teamTwoNumber);
             }
         }
       
         for (int row = 0; row < Global.schedule.length; row++ ){
              int temp = Global.schedule[row][1];
             if (temp == selectedTeam) {
                 Global.teamOneNumber = Global.schedule[row][0];
                 Global.teamTwoNumber = Global.schedule[row][1];
                 System.out.println("Team: " + Global.teamOneNumber + "vs Team: " + Global.teamTwoNumber);
             }
         }
         
         // Populate the rest now that you know the team numbers
         for (int row = 0; row < Global.teamPlayerHandicap.length; row++ ){
             //Global.teamOneNumber = Global.schedule[row][0];
           
             if (Global.teamOneNumber == Integer.parseInt(Global.teamPlayerHandicap[row][0])) { 
                 System.out.print("Team #" + Global.teamPlayerHandicap[row][0] + " Player One: " + Global.teamPlayerHandicap[row][1] + " (" + Global.teamPlayerHandicap[row][2] + "/" + Global.teamPlayerHandicap[row][3] + ")");                      
                 System.out.print(" Player Two: " + Global.teamPlayerHandicap[row][4] + " (" + Global.teamPlayerHandicap[row][5] + "/" + Global.teamPlayerHandicap[row][6] + ")");  
                 Global.playerOneName = Global.teamPlayerHandicap[row][1];
                 Global.playerOneAverageScore = Integer.parseInt(Global.teamPlayerHandicap[row][2]);
                 Global.playerOneHandicap = Integer.parseInt(Global.teamPlayerHandicap[row][3]);
                 Global.playerTwoName = Global.teamPlayerHandicap[row][4];
                 Global.playerTwoAverageScore = Integer.parseInt(Global.teamPlayerHandicap[row][5]);
                 Global.playerTwoHandicap = Integer.parseInt(Global.teamPlayerHandicap[row][6]);
             }
              if (Global.teamTwoNumber == Integer.parseInt(Global.teamPlayerHandicap[row][0])) { 
                 System.out.print("Team #" + Global.teamPlayerHandicap[row][0] + " Player One: " + Global.teamPlayerHandicap[row][1] + " (" + Global.teamPlayerHandicap[row][2] + "/" + Global.teamPlayerHandicap[row][3] + ")");                      
                 System.out.print(" Player Two: " + Global.teamPlayerHandicap[row][4] + " (" + Global.teamPlayerHandicap[row][5] + "/" + Global.teamPlayerHandicap[row][6] + ")");  
                 Global.playerThreeName = Global.teamPlayerHandicap[row][1];
                 Global.playerThreeAverageScore = Integer.parseInt(Global.teamPlayerHandicap[row][2]);
                 Global.playerThreeHandicap = Integer.parseInt(Global.teamPlayerHandicap[row][3]);
                 Global.playerFourName = Global.teamPlayerHandicap[row][4];
                 Global.playerFourAverageScore = Integer.parseInt(Global.teamPlayerHandicap[row][5]);
                 Global.playerFourHandicap = Integer.parseInt(Global.teamPlayerHandicap[row][6]);
              }
              
              TwilightGolfLeague.jLabel_MatchupOne.setText(Global.playerOneName  + " (" + Global.playerOneAverageScore + "/" + Global.playerOneHandicap + ") vs " + Global.playerThreeName + " (" + Global.playerThreeAverageScore + "/" + Global.playerThreeHandicap + ")");
              TwilightGolfLeague.jLabel_MatchupTwo.setText(Global.playerTwoName  + " (" + Global.playerTwoAverageScore + "/" + Global.playerTwoHandicap + ") vs " + Global.playerFourName + " (" + Global.playerFourAverageScore + "/" + Global.playerFourHandicap + ")");
         }
     }
     public static void ChangeLabels() {
         
         TwilightGolfLeague.jLabel_TeamOnePlayerOneScoreDisplay.setText("Team #" + Global.teamOneNumber + ": Player 1");
         TwilightGolfLeague.jLabel_TeamOnePlayerTwoScoreDisplay.setText("Team #" + Global.teamOneNumber + ": Player 2");
         TwilightGolfLeague.jLabel_TeamTwoPlayerOneScoreDisplay.setText("Team #" + Global.teamTwoNumber + ": Player 1");
         TwilightGolfLeague.jLabel_TeamTwoPlayerTwoScoreDisplay.setText("Team #" + Global.teamTwoNumber + ": Player 2");
     }
     public static void DisplayHoles(int[][] holes) {
         //Set Values for Hole Numbers   
         for (int column = 0; column < holes[1].length; column++) {
                 holes[1][column] = column + 1;
         } 
         System.out.print("Hole Number | ");
         TwilightGolfLeague.jTextArea_Details.append("Hole Number | ");
         for (int row = 1; row < 2; row++) {
             for (int column = 0; column < holes[row].length; column++) {
                 System.out.print(holes[row][column] + " ");
                 TwilightGolfLeague.jTextArea_Details.append(Integer.toString(holes[row][column]) + " ");
             }
             TwilightGolfLeague.jTextArea_Details.append("" + newline);
             System.out.println(" ");
             System.out.println("-------------------------------");
             TwilightGolfLeague.jTextArea_Details.append("--------------------------------------------" + newline);
 
         }
         
         
     }
     public static void DisplayInitialScores(int[][] playerScores, int playerOneHandicap, int playerTwoHandicap, int playerThreeHandicap, int playerFourHandicap) {
         for (int row = 2; row < playerScores.length; row++) {
           int sum = 0;  
           System.out.print("Player #" + (row - 1) + "    | ");
           TwilightGolfLeague.jTextArea_Details.append("Player #" + (row - 1) + "        | ");
             for (int column = 0; column < playerScores[row].length; column++) {
                 System.out.print(playerScores[row][column] + " ");
                 TwilightGolfLeague.jTextArea_Details.append(playerScores[row][column] + " ");
                 sum +=playerScores[row][column];
                 
             }
             
             switch (row) {
                 case 2: Global.playerOneScore = sum;
                 System.out.println("Net: " + (Global.playerOneScore - playerOneHandicap) + " (Score: " + Global.playerOneScore + " - Handicap: " + playerOneHandicap + ")");
                 TwilightGolfLeague.jTextArea_Details.append("   Net: " + (Global.playerOneScore - playerOneHandicap) + " (Score: " + Global.playerOneScore + " - Handicap: " + playerOneHandicap + ")" + newline);
                 Global.playerOneScore = sum - playerOneHandicap;  
                 TwilightGolfLeague.jLabel_PlayerOneScore.setText(Integer.toString(sum));
                 TwilightGolfLeague.jLabel_PlayerOneHDCP.setText(Integer.toString(Global.playerOneHandicap));
                 TwilightGolfLeague.jLabel_PlayerOneNet.setText(Integer.toString(Global.playerOneScore));
                 break;
                 case 3: Global.playerTwoScore = sum;
                 System.out.println("Net: " + (Global.playerTwoScore - playerTwoHandicap) + " (Score: " + Global.playerTwoScore + " - Handicap: " + playerTwoHandicap + ")");
                 TwilightGolfLeague.jTextArea_Details.append("   Net: " + (Global.playerTwoScore - playerTwoHandicap) + " (Score: " + Global.playerTwoScore + " - Handicap: " + playerTwoHandicap + ")" + newline);
                 Global.playerTwoScore = sum - playerTwoHandicap;
                 TwilightGolfLeague.jLabel_PlayerTwoScore.setText(Integer.toString(sum));
                 TwilightGolfLeague.jLabel_PlayerTwoHDCP.setText(Integer.toString(Global.playerTwoHandicap));
                 TwilightGolfLeague.jLabel_PlayerTwoNet.setText(Integer.toString(Global.playerTwoScore));
                     break;
                 
                 case 4: Global.playerThreeScore = sum;
                 System.out.println("Net: " + (Global.playerThreeScore - playerThreeHandicap) + " (Score: " + Global.playerThreeScore + " - Handicap: " + playerThreeHandicap + ")");
                 TwilightGolfLeague.jTextArea_Details.append("   Net: " + (Global.playerThreeScore - playerThreeHandicap) + " (Score: " + Global.playerThreeScore + " - Handicap: " + playerThreeHandicap + ")" + newline);
                 Global.playerThreeScore = sum - playerThreeHandicap;   
                 TwilightGolfLeague.jLabel_PlayerThreeScore.setText(Integer.toString(sum));
                 TwilightGolfLeague.jLabel_PlayerThreeHDCP.setText(Integer.toString(Global.playerThreeHandicap));
                 TwilightGolfLeague.jLabel_PlayerThreeNet.setText(Integer.toString(Global.playerThreeScore));
                 break;
                 case 5: Global.playerFourScore = sum;
                 System.out.println("Net: " + (Global.playerFourScore - playerFourHandicap) + " (Score: " + Global.playerFourScore + " - Handicap: " + playerFourHandicap + ")");
                 TwilightGolfLeague.jTextArea_Details.append("   Net: " + (Global.playerFourScore - playerFourHandicap) + " (Score: " + Global.playerFourScore + " - Handicap: " + playerFourHandicap + ")" + newline);
                 Global.playerFourScore = sum - playerFourHandicap;   
                 TwilightGolfLeague.jLabel_PlayerFourScore.setText(Integer.toString(sum));
                 TwilightGolfLeague.jLabel_PlayerFourHDCP.setText(Integer.toString(Global.playerFourHandicap));
                 TwilightGolfLeague.jLabel_PlayerFourNet.setText(Integer.toString(Global.playerFourScore));
                 break;
             } 
            
         }
     }
     public static void StartIndividualSection(int[][] playerScores, int playerOneHandicap, int playerTwoHandicap, int playerThreeHandicap, int playerFourHandicap){
         int handicapDifference1;
         int handicapDifference2;
         TwilightGolfLeague.jTextArea_Details.append(newline);
         System.out.println("--- Starting Individual Section ---");
         System.out.println();
         TwilightGolfLeague.jTextArea_Details.append("--- Starting Individual Section ---" + newline);
          
         // Adjust Player Scores Player One or Player Three
         if (playerOneHandicap > playerThreeHandicap) {
             // Calculate Handicap Differnece
             handicapDifference1 = playerOneHandicap - playerThreeHandicap;
             System.out.println("Adjusting player one by " + handicapDifference1 + " strokes.");
             TwilightGolfLeague.jTextArea_Details.append("Adjusting player one by " + handicapDifference1 + " strokes." + newline);
             // Update array with new score
             UpdateScoresArray(playerScores, handicapDifference1, 2);
             
         } else {   
             
            handicapDifference1 = playerThreeHandicap - playerOneHandicap;
            System.out.println("Adjusting player three by " + handicapDifference1 + " strokes.");
            TwilightGolfLeague.jTextArea_Details.append("Adjusting player three by " + handicapDifference1 + " strokes." + newline);
              UpdateScoresArray(playerScores, handicapDifference1, 4);
         }
         // Adjust Player Scores Player Two or Player Four
         if (playerTwoHandicap > playerFourHandicap) {
             
             // Calculate Handicap Differnece
             handicapDifference2 = playerTwoHandicap - playerFourHandicap;
             System.out.println("4H: " + playerFourHandicap + " - P2: " + playerTwoHandicap + " = " + handicapDifference2);
             System.out.println("Adjusting player two by " + handicapDifference2 + " strokes.");
             TwilightGolfLeague.jTextArea_Details.append("Adjusting player two by " + handicapDifference2 + " strokes.");
             // Update array with new score
             UpdateScoresArray(playerScores, handicapDifference2, 3);
             
         } else {   
             
            handicapDifference2 = playerFourHandicap - playerTwoHandicap;
            System.out.println("4H: " + playerFourHandicap + " - P2: " + playerTwoHandicap + " = " + handicapDifference2);
            System.out.println("Adjusting player four by " + handicapDifference2 + " strokes.");
            TwilightGolfLeague.jTextArea_Details.append("Adjusting player four by " + handicapDifference2 + " strokes.");
              UpdateScoresArray(playerScores, handicapDifference2, 5);
         }
     }   
     public static void DisplayHeadsUpScores(int[][] playerScores){
         TwilightGolfLeague.jTextArea_Details.append(newline);
         
         for (int row = 2; row < playerScores.length; row++) {  
           
           System.out.print("Player #" + (row - 1) + "   | ");
           TwilightGolfLeague.jTextArea_Details.append("Player #" + (row - 1) + "   | ");
             for (int column = 0; column < playerScores[row].length; column++) {
                 System.out.print(playerScores[row][column] + " ");
                 TwilightGolfLeague.jTextArea_Details.append(playerScores[row][column] + " ");
                 
             } 
            System.out.println(" ");
            TwilightGolfLeague.jTextArea_Details.append(newline);
         }
         
     }
     
     public static void DisplayTeamScoresAndPoints() {
         TwilightGolfLeague.jTextArea_Details.append(newline);    
         System.out.println("--- Starting Team Totals ----");
         TwilightGolfLeague.jTextArea_Details.append("--- Starting Team Totals ----" + newline);
         int teamOneTotalScore;
         int teamTwoTotalScore;
         // Get Total Scores
         teamOneTotalScore = Global.playerOneScore + Global.playerTwoScore;
         teamTwoTotalScore = Global.playerThreeScore + Global.playerFourScore;
         
         TwilightGolfLeague.jLabel_TeamOneNet.setText(Integer.toString(teamOneTotalScore));
         TwilightGolfLeague.jLabel_TeamTwoNet.setText(Integer.toString(teamTwoTotalScore));
         
         //Display Total Scores
         System.out.println("Team One Total: " + teamOneTotalScore);
         TwilightGolfLeague.jTextArea_Details.append("Team One Total: " + teamOneTotalScore + newline);
         System.out.println("Team Two Total: " + teamTwoTotalScore);
         TwilightGolfLeague.jTextArea_Details.append("Team Two Total: " + teamTwoTotalScore + newline);
         // Assign points based on lower score
          if (teamOneTotalScore < teamTwoTotalScore) {
              Global.teamOnePoints = 1.0;
              TwilightGolfLeague.jLabel_TeamOneTeamPoint.setText("1.0");
              TwilightGolfLeague.jLabel_TeamTwoTeamPoint.setText("0.0");
          } else if (teamTwoTotalScore < teamOneTotalScore) {
              Global.teamTwoPoints = 1.0;   
              TwilightGolfLeague.jLabel_TeamTwoTeamPoint.setText("1.0");
              TwilightGolfLeague.jLabel_TeamOneTeamPoint.setText("0.0");
          } else {
              Global.teamOnePoints = 0.5;
              Global.teamTwoPoints = 0.5;
              TwilightGolfLeague.jLabel_TeamOneTeamPoint.setText("0.5");
              TwilightGolfLeague.jLabel_TeamTwoTeamPoint.setText("0.5");
          }
         System.out.println();
         System.out.println("Points after Team Calculations");
         TwilightGolfLeague.jTextArea_Details.append(newline);
         TwilightGolfLeague.jTextArea_Details.append("Points after Team Calculations" + newline);
         DisplayCurrentPoints();
          
 }
     public static void DisplayCurrentPoints(){
         // Display Current Points
          System.out.println("Team One Points: " + Global.teamOnePoints);
          System.out.println("Team Two Points: " + Global.teamTwoPoints);
          TwilightGolfLeague.jTextArea_Details.append("Team One Points: " + Global.teamOnePoints + newline);
          TwilightGolfLeague.jTextArea_Details.append("Team Two Points: " + Global.teamTwoPoints + newline);
          
         TwilightGolfLeague.jLabel_TeamOneTotalPoints.setText(Double.toString(Global.teamOnePoints));
         TwilightGolfLeague.jLabel_TeamTwoTotalPoints.setText(Double.toString(Global.teamTwoPoints));
         
         TwilightGolfLeague.jLabel_PlayerOnePoints.setText(Double.toString(Global.playerOnePoints));
         TwilightGolfLeague.jLabel_PlayerTwoPoints.setText(Double.toString(Global.playerTwoPoints));
         TwilightGolfLeague.jLabel_PlayerThreePoints.setText(Double.toString(Global.playerThreePoints));
         TwilightGolfLeague.jLabel_PlayerFourPoints.setText(Double.toString(Global.playerFourPoints));
     }
     
     public static void HeadsUpMatch(int[][] playerScores) {
         int playerOne = 2;
         int playerTwo = 3;
         int playerThree = 4;
         int playerFour = 5;
         int holesWonPlayerOne = 0;
         int holesWonPlayerTwo = 0;
         int holesWonPlayerThree = 0;
         int holesWonPlayerFour = 0;
         
         // Get Scores for Players 1 Vs 3
         for (int hole = 0; hole < 9; hole++) {
             if (playerScores[playerOne][hole] < playerScores[playerThree][hole]) {
                 holesWonPlayerOne = holesWonPlayerOne + 1;  
             } else if (playerScores[playerThree][hole] < playerScores[playerOne][hole]) {
                 holesWonPlayerThree = holesWonPlayerThree + 1;
             } else {
                 //Do Nothing No Score Update
             }
         }
        
 
         // Get Scores for Players 2 Vs 4
         for (int hole = 0; hole < 9; hole++) {
             if (playerScores[playerTwo][hole] < playerScores[playerFour][hole]) {
                 holesWonPlayerTwo = holesWonPlayerTwo + 1;  
             } else if (playerScores[playerFour][hole] < playerScores[playerTwo][hole]) {
                 holesWonPlayerFour = holesWonPlayerFour + 1;
             } else {
                 //Do Nothing No Score Update
             }
         }
         System.out.println();
         TwilightGolfLeague.jTextArea_Details.append(newline);
         System.out.println("Holes Won: "
                 + "Player One: " + holesWonPlayerOne + " | "
                 + "Player Three: " + holesWonPlayerThree + " | "
                 + "Player Two: " + holesWonPlayerTwo + " | "
                 + "Player Four: " + holesWonPlayerFour + " | "
                 );      
         TwilightGolfLeague.jTextArea_Details.append("Holes Won: " + newline
                 + "Player One: " + holesWonPlayerOne + " | "
                 + "Player Three: " + holesWonPlayerThree + newline
                 + "Player Two: " + holesWonPlayerTwo + " | "
                 + "Player Four: " + holesWonPlayerFour
                 + newline);
         // Award Points
         if (holesWonPlayerOne > holesWonPlayerThree) {
             Global.teamOnePoints = Global.teamOnePoints + 1;
             Global.playerOnePoints = 1.0;
         } else if (holesWonPlayerThree > holesWonPlayerOne){
             Global.teamTwoPoints = Global.teamTwoPoints + 1;
             Global.playerThreePoints = 1.0;
         } else {
             Global.teamOnePoints = Global.teamOnePoints + 0.5;
             Global.teamTwoPoints = Global.teamTwoPoints + 0.5;
             Global.playerOnePoints = 0.5;
             Global.playerThreePoints = 0.5;
         }
         // Award Points
         if (holesWonPlayerTwo > holesWonPlayerFour) {
             Global.teamOnePoints = Global.teamOnePoints + 1;
             Global.playerTwoPoints = 1.0;
         } else if (holesWonPlayerFour > holesWonPlayerTwo){
             Global.teamTwoPoints = Global.teamTwoPoints + 1;
             Global.playerFourPoints = 1.0;
         } else {
             Global.teamOnePoints = Global.teamOnePoints + 0.5;
             Global.teamTwoPoints = Global.teamTwoPoints + 0.5;
             Global.playerTwoPoints = 0.5;
             Global.playerFourPoints = 0.5;
         }
         
         System.out.println();
         TwilightGolfLeague.jTextArea_Details.append(newline);
         System.out.println("Points After Heads Up Matches");
         TwilightGolfLeague.jTextArea_Details.append("Points After Heads Up Matches");
                 
         DisplayCurrentPoints();
     }
     public static void startArray(int[][] playerScores) {
        
         
         //Player 1 Scores
         playerScores[2][0] = 5;
         playerScores[2][1] = 5;
         playerScores[2][2] = 5;
         playerScores[2][3] = 5;
         playerScores[2][4] = 5;
         playerScores[2][5] = 5;
         playerScores[2][6] = 5;
         playerScores[2][7] = 5;
         playerScores[2][8] = 5;
         
          //Player 2 Scores
         playerScores[3][0] = 7;
         playerScores[3][1] = 7;
         playerScores[3][2] = 7;
         playerScores[3][3] = 7;
         playerScores[3][4] = 7;
         playerScores[3][5] = 7;
         playerScores[3][6] = 7;
         playerScores[3][7] = 7;
         playerScores[3][8] = 7;
         
         //Player 3 Scores
         playerScores[4][0] = 4;
         playerScores[4][1] = 4;
         playerScores[4][2] = 4;
         playerScores[4][3] = 4;
         playerScores[4][4] = 4;
         playerScores[4][5] = 4;
         playerScores[4][6] = 4;
         playerScores[4][7] = 4;
         playerScores[4][8] = 4;
         
          //Player 4 Scores
         playerScores[5][0] = 6;
         playerScores[5][1] = 6;
         playerScores[5][2] = 6;
         playerScores[5][3] = 6;
         playerScores[5][4] = 6;
         playerScores[5][5] = 6;
         playerScores[5][6] = 6;
         playerScores[5][7] = 6;
         playerScores[5][8] = 6;
     }
     public static void UpdateScoresArray(int[][] holes, int handicap, int player) {
         int fullrun = 0;
         int remainder = 0;
         
         if (handicap > 9) {
             fullrun = (handicap / 9);
             remainder = (handicap % 9);
             TwilightGolfLeague.jTextArea_Details.append(newline);
             //System.out.println("Fullrun= " + fullrun);
             //System.out.println("Remainder= " + remainder);
         
         // If handicap is greater than 10 run the for loop below more than once
         for (int f = 0; f < fullrun; f++ ) {
          // Actual loop that updates scores
             for (int h = 0; h < 9; h++) {
                int modHole = holes[0][h] - 1;
                //System.out.println("Modifying Hole #" + modHole);
                //System.out.println("Current Score: " +playerScores[4][modHole]);
                holes[player][modHole] = holes[player][modHole] - 1;
                System.out.println("Modifing player " + (player - 1) + " and hole number " + (modHole + 1) + " by 1");
                TwilightGolfLeague.jTextArea_Details.append("Modifing player " + (player - 1) + " and hole number " + (modHole + 1) + " by one stroke" + newline);
                //System.out.println("New Score: " + newScore);
            }  
          }
         // Run it the remainder times
         for (int r = 0; r < remainder; r++) {
             
                int modHole = holes[0][r] -1;
                
                //System.out.print("Modifying Hole #" + (modHole  + 1)+ "  | ");
                //System.out.print("Current Score: " +holes[player][modHole] + "  | ");
                holes[player][modHole] = holes[player][modHole] - 1;
                System.out.println("Modifing player " + (player - 1) + " and hole number " + (modHole + 1) + " by 1");
                TwilightGolfLeague.jTextArea_Details.append("Modifing player " + (player - 1) + " and hole number " + (modHole + 1) + " by one stroke" + newline);
                //System.out.println("New Score: " + holes[player][modHole]+ "  | ");
            
         }
         } else if (handicap > 0 && handicap < 9) {
             // Actual loop that updates scores
             TwilightGolfLeague.jTextArea_Details.append(newline);
             for (int h = 0; h < handicap; h++) {
                int modHole = holes[0][h] - 1;
                //System.out.println("Modifying Hole #" + modHole);
                //System.out.println("Current Score: " +playerScores[4][modHole]);
                holes[player][modHole] = holes[player][modHole] - 1;
                System.out.println("Modifing player " + (player - 1) + " and hole number " + (modHole + 1) + " by 1");
                TwilightGolfLeague.jTextArea_Details.append("Modifing player " + (player - 1) + " and hole number " + (modHole + 1) + " by one stroke" + newline);
                
                //System.out.println("New Score: " + newScore);
            }  
         }
         
     }
    
     public static void printArray(int[][] playerScores){
       for (int row = 0; row < playerScores.length ; row++) {
           for (int column = 0; column < playerScores[row].length; column++) {
             System.out.print(playerScores[row][column] + " "); }
             System.out.println();
           }
     }
     public static int returnNumber(int number, int choice) {
         int random = (int)(Math.random() * number);
         if (choice >= 1) {
             if (random < 10) {
                 random = random + 10;
             }
         }
         if (choice <= 0) {
             if (random < 3 ) {
                 random = random + 4;
             } 
         }
         return random;
         
         
     }
     public static void DefaultValues(){
        
        System.out.println("Handicap as seen in default values");
        System.out.println(Global.playerOneHandicap);
      
         JTextField arrJTxtPlayer2Hole[] = new JTextField[9];
          arrJTxtPlayer2Hole[0] = TwilightGolfLeague.jTxt_Player2Hole0;
          arrJTxtPlayer2Hole[1] = TwilightGolfLeague.jTxt_Player2Hole1;
          arrJTxtPlayer2Hole[2] = TwilightGolfLeague.jTxt_Player2Hole2;
          arrJTxtPlayer2Hole[3] = TwilightGolfLeague.jTxt_Player2Hole3;
          arrJTxtPlayer2Hole[4] = TwilightGolfLeague.jTxt_Player2Hole4;
          arrJTxtPlayer2Hole[5] = TwilightGolfLeague.jTxt_Player2Hole5;
          arrJTxtPlayer2Hole[6] = TwilightGolfLeague.jTxt_Player2Hole6;
          arrJTxtPlayer2Hole[7] = TwilightGolfLeague.jTxt_Player2Hole7;
          arrJTxtPlayer2Hole[8] = TwilightGolfLeague.jTxt_Player2Hole8;
          
          
          
         for (int i =0; i < 9; i++) { 
             arrJTxtPlayer2Hole[i].setText(Integer.toString(returnNumber(10, 0)));  
         }
         
         JTextField arrJTxtPlayer3Hole[] = new JTextField[9];
          arrJTxtPlayer3Hole[0] = TwilightGolfLeague.jTxt_Player3Hole0;
          arrJTxtPlayer3Hole[1] = TwilightGolfLeague.jTxt_Player3Hole1;
          arrJTxtPlayer3Hole[2] = TwilightGolfLeague.jTxt_Player3Hole2;
          arrJTxtPlayer3Hole[3] = TwilightGolfLeague.jTxt_Player3Hole3;
          arrJTxtPlayer3Hole[4] = TwilightGolfLeague.jTxt_Player3Hole4;
          arrJTxtPlayer3Hole[5] = TwilightGolfLeague.jTxt_Player3Hole5;
          arrJTxtPlayer3Hole[6] = TwilightGolfLeague.jTxt_Player3Hole6;
          arrJTxtPlayer3Hole[7] = TwilightGolfLeague.jTxt_Player3Hole7;
          arrJTxtPlayer3Hole[8] = TwilightGolfLeague.jTxt_Player3Hole8;
       
          for (int i =0; i < 9; i++) {
             arrJTxtPlayer3Hole[i].setText(Integer.toString(returnNumber(10, 0)));
         }
          
         JTextField arrJTxtPlayer4Hole[] = new JTextField[9];
          arrJTxtPlayer4Hole[0] = TwilightGolfLeague.jTxt_Player4Hole0;
          arrJTxtPlayer4Hole[1] = TwilightGolfLeague.jTxt_Player4Hole1;
          arrJTxtPlayer4Hole[2] = TwilightGolfLeague.jTxt_Player4Hole2;
          arrJTxtPlayer4Hole[3] = TwilightGolfLeague.jTxt_Player4Hole3;
          arrJTxtPlayer4Hole[4] = TwilightGolfLeague.jTxt_Player4Hole4;
          arrJTxtPlayer4Hole[5] = TwilightGolfLeague.jTxt_Player4Hole5;
          arrJTxtPlayer4Hole[6] = TwilightGolfLeague.jTxt_Player4Hole6;
          arrJTxtPlayer4Hole[7] = TwilightGolfLeague.jTxt_Player4Hole7;
          arrJTxtPlayer4Hole[8] = TwilightGolfLeague.jTxt_Player4Hole8;
          
         for (int i =0; i < 9; i++) {
            arrJTxtPlayer4Hole[i].setText(Integer.toString(returnNumber(10, 0)));
         }
        
         JTextField arrJTxtPlayer5Hole[] = new JTextField[9];
          arrJTxtPlayer5Hole[0] = TwilightGolfLeague.jTxt_Player5Hole0;
          arrJTxtPlayer5Hole[1] = TwilightGolfLeague.jTxt_Player5Hole1;
          arrJTxtPlayer5Hole[2] = TwilightGolfLeague.jTxt_Player5Hole2;
          arrJTxtPlayer5Hole[3] = TwilightGolfLeague.jTxt_Player5Hole3;
          arrJTxtPlayer5Hole[4] = TwilightGolfLeague.jTxt_Player5Hole4;
          arrJTxtPlayer5Hole[5] = TwilightGolfLeague.jTxt_Player5Hole5;
          arrJTxtPlayer5Hole[6] = TwilightGolfLeague.jTxt_Player5Hole6;
          arrJTxtPlayer5Hole[7] = TwilightGolfLeague.jTxt_Player5Hole7;
          arrJTxtPlayer5Hole[8] = TwilightGolfLeague.jTxt_Player5Hole8;
          
         for (int i =0; i < 9; i++) {
             arrJTxtPlayer5Hole[i].setText(Integer.toString(returnNumber(10, 0)));
         }
         
     }
     public static void ResetValues(){
     
         JTextField arrJTxtPlayer2Hole[] = new JTextField[9];
          arrJTxtPlayer2Hole[0] = TwilightGolfLeague.jTxt_Player2Hole0;
          arrJTxtPlayer2Hole[1] = TwilightGolfLeague.jTxt_Player2Hole1;
          arrJTxtPlayer2Hole[2] = TwilightGolfLeague.jTxt_Player2Hole2;
          arrJTxtPlayer2Hole[3] = TwilightGolfLeague.jTxt_Player2Hole3;
          arrJTxtPlayer2Hole[4] = TwilightGolfLeague.jTxt_Player2Hole4;
          arrJTxtPlayer2Hole[5] = TwilightGolfLeague.jTxt_Player2Hole5;
          arrJTxtPlayer2Hole[6] = TwilightGolfLeague.jTxt_Player2Hole6;
          arrJTxtPlayer2Hole[7] = TwilightGolfLeague.jTxt_Player2Hole7;
          arrJTxtPlayer2Hole[8] = TwilightGolfLeague.jTxt_Player2Hole8;
          
          
          
         for (int i =0; i < 9; i++) {
             arrJTxtPlayer2Hole[i].setText("");  
         }
         
         JTextField arrJTxtPlayer3Hole[] = new JTextField[9];
          arrJTxtPlayer3Hole[0] = TwilightGolfLeague.jTxt_Player3Hole0;
          arrJTxtPlayer3Hole[1] = TwilightGolfLeague.jTxt_Player3Hole1;
          arrJTxtPlayer3Hole[2] = TwilightGolfLeague.jTxt_Player3Hole2;
          arrJTxtPlayer3Hole[3] = TwilightGolfLeague.jTxt_Player3Hole3;
          arrJTxtPlayer3Hole[4] = TwilightGolfLeague.jTxt_Player3Hole4;
          arrJTxtPlayer3Hole[5] = TwilightGolfLeague.jTxt_Player3Hole5;
          arrJTxtPlayer3Hole[6] = TwilightGolfLeague.jTxt_Player3Hole6;
          arrJTxtPlayer3Hole[7] = TwilightGolfLeague.jTxt_Player3Hole7;
          arrJTxtPlayer3Hole[8] = TwilightGolfLeague.jTxt_Player3Hole8;
         for (int i =0; i < 9; i++) {
             arrJTxtPlayer3Hole[i].setText("");  
         }
         JTextField arrJTxtPlayer4Hole[] = new JTextField[9];
          arrJTxtPlayer4Hole[0] = TwilightGolfLeague.jTxt_Player4Hole0;
          arrJTxtPlayer4Hole[1] = TwilightGolfLeague.jTxt_Player4Hole1;
          arrJTxtPlayer4Hole[2] = TwilightGolfLeague.jTxt_Player4Hole2;
          arrJTxtPlayer4Hole[3] = TwilightGolfLeague.jTxt_Player4Hole3;
          arrJTxtPlayer4Hole[4] = TwilightGolfLeague.jTxt_Player4Hole4;
          arrJTxtPlayer4Hole[5] = TwilightGolfLeague.jTxt_Player4Hole5;
          arrJTxtPlayer4Hole[6] = TwilightGolfLeague.jTxt_Player4Hole6;
          arrJTxtPlayer4Hole[7] = TwilightGolfLeague.jTxt_Player4Hole7;
          arrJTxtPlayer4Hole[8] = TwilightGolfLeague.jTxt_Player4Hole8;
         for (int i =0; i < 9; i++) {
             arrJTxtPlayer4Hole[i].setText("");  
         }
         JTextField arrJTxtPlayer5Hole[] = new JTextField[9];
          arrJTxtPlayer5Hole[0] = TwilightGolfLeague.jTxt_Player5Hole0;
          arrJTxtPlayer5Hole[1] = TwilightGolfLeague.jTxt_Player5Hole1;
          arrJTxtPlayer5Hole[2] = TwilightGolfLeague.jTxt_Player5Hole2;
          arrJTxtPlayer5Hole[3] = TwilightGolfLeague.jTxt_Player5Hole3;
          arrJTxtPlayer5Hole[4] = TwilightGolfLeague.jTxt_Player5Hole4;
          arrJTxtPlayer5Hole[5] = TwilightGolfLeague.jTxt_Player5Hole5;
          arrJTxtPlayer5Hole[6] = TwilightGolfLeague.jTxt_Player5Hole6;
          arrJTxtPlayer5Hole[7] = TwilightGolfLeague.jTxt_Player5Hole7;
          arrJTxtPlayer5Hole[8] = TwilightGolfLeague.jTxt_Player5Hole8;
         for (int i =0; i < 9; i++) {
             arrJTxtPlayer5Hole[i].setText("");  
         }
         TwilightGolfLeague.jTextArea_Details.setText("");
         TwilightGolfLeague.jLabel_TeamOnePlayerOneScoreDisplay.setText("Team #1: Player 1");
         TwilightGolfLeague.jLabel_TeamOnePlayerTwoScoreDisplay.setText("Team #1: Player 2");
         TwilightGolfLeague.jLabel_TeamTwoPlayerOneScoreDisplay.setText("Team #2: Player 1");
         TwilightGolfLeague.jLabel_TeamTwoPlayerTwoScoreDisplay.setText("Team #2: Player 2");
         TwilightGolfLeague.jLabel_PlayerOneScore.setText("");
         TwilightGolfLeague.jLabel_PlayerOneHDCP.setText("");
         TwilightGolfLeague.jLabel_PlayerOneNet.setText("");
         TwilightGolfLeague.jLabel_PlayerTwoScore.setText("");
         TwilightGolfLeague.jLabel_PlayerTwoHDCP.setText("");
         TwilightGolfLeague.jLabel_PlayerTwoNet.setText("");
         TwilightGolfLeague.jLabel_PlayerThreeScore.setText("");
         TwilightGolfLeague.jLabel_PlayerThreeHDCP.setText("");
         TwilightGolfLeague.jLabel_PlayerThreeNet.setText("");
         TwilightGolfLeague.jLabel_PlayerFourScore.setText("");
         TwilightGolfLeague.jLabel_PlayerFourHDCP.setText("");
         TwilightGolfLeague.jLabel_PlayerFourNet.setText("");
         TwilightGolfLeague.jLabel_TeamOneTotalPoints.setText("");
         TwilightGolfLeague.jLabel_TeamTwoTotalPoints.setText("");
         TwilightGolfLeague.jLabel_TeamOneTeamPoint.setText("");
         TwilightGolfLeague.jLabel_TeamTwoTeamPoint.setText("");
         TwilightGolfLeague.jLabel_PlayerOnePoints.setText("");
         TwilightGolfLeague.jLabel_PlayerTwoPoints.setText("");
         TwilightGolfLeague.jLabel_PlayerThreePoints.setText("");
         TwilightGolfLeague.jLabel_PlayerFourPoints.setText("");
         TwilightGolfLeague.jLabel_TeamOneNet.setText("");
         TwilightGolfLeague.jLabel_TeamTwoNet.setText("");
         
     }
    
     public static void ResetPoints() {
         Global.teamTwoPoints = 0.0;
         Global.teamOnePoints = 0.0;
         Global.playerOnePoints = 0.0;
         Global.playerTwoPoints = 0.0;
         Global.playerThreePoints = 0.0;
         Global.playerFourPoints = 0.0;
         Global.playerOneScore = 0;
         Global.playerTwoScore = 0;
         Global.playerThreeScore = 0;
         Global.playerFourScore = 0;
        
     }
     
     /*
     public static void GetInput(int[][] playerScores, int playerOneHandicap, int playerTwoHandicap, int playerThreeHandicap, int playerFourHandicap) {
         Scanner input = new Scanner(System.in);
         // Get Handicaps
         System.out.print("Enter Player One Handicap: ");
         Global.playerOneHandicap = input.nextInt();
         System.out.print("Enter Player Two Handicap: ");
         Global.playerTwoHandicap = input.nextInt();
         System.out.print("Enter Player Three Handicap: ");
         Global.playerThreeHandicap = input.nextInt();
         System.out.print("Enter Player Four Handicap: ");
         Global.playerFourHandicap = input.nextInt();
         
         
         System.out.println("Enter all scores one player per line");
         for (int row = 2; row < playerScores.length; row++){
           System.out.println("Player" + (row - 1) + " :");
           for (int column = 0; column < playerScores[row].length; column++){
                playerScores[row][column] = input.nextInt();
           }
         }
         
  
     } */
    
 }
