 package Graph;
 
 import GameConfig.GameConfig;
 import run.GameSession;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Random;
 
 /**
  * Created with IntelliJ IDEA.
  * User: User
  * Date: 24.08.13
  * Time: 17:57
  * To change this template use File | Settings | File Templates.
  */
 public class ChangeUserButton extends JButton {
 
     private boolean active = true;
     private GameSession session = GameSession.getInstance();
     public ChangeUserButton(){
         addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 if (!active) return;
                 try {
                     Integer available = session.getAviableMatches();
 
                     session.changeCurrentUser();
                     session.getCurrentUser().resetCount(available);
                     if (session.getCurrentUser().getType().equals("robot")) {
                         robotTurn();
                     }
                     repaint();
                 } catch (Exception e1) {
                     e1.printStackTrace();
                 }
                 System.out.println("available matches=" + session.getAviableMatches());
             }
         });
         Integer x = GameConfig.windowXSize/2-50;
         setBounds(x, 40, 100, 20);
         setMargin(new Insets(3, 2, 3, 2));
         setBorderPainted(false);
         setText("передать ход");
         setBackground(Color.gray);
     }
     public void setInactive(){
         setForeground(Color.darkGray);
         active = false;
     }
 
     public void setActive(){
         setForeground(Color.black);
         active = true;
     }
    private Boolean robotTurn() throws Exception {
         if(session.getAviableMatches() <= 0){
             throw new Exception("no matches for this turn");
         }
         else if(session.getAviableMatches() <= 10){
             this.takeMatches(session.getAviableMatches());
            return true;
         }
         else{
             Random r = new Random();
             Integer m = Math.abs(r.nextInt(9)+1);
             this.takeMatches(m);
             System.out.println("select matches=" + m);
            return false;
         }
     }
 
     private void takeMatches(int count){
         for(int i=1; i<=count; i++){
            session.getLastMatche().move();
         }
     }
 }
