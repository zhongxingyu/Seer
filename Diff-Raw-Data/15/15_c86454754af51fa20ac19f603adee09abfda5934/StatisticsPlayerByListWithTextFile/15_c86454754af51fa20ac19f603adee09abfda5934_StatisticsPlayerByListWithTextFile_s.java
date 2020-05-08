 package fr.iutvalence.java.mp.BrickBreaker;
 
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.List;
 
 
 /**
  * This class use files and lists in order to manages players
  * @author soulierc
  */
 public class StatisticsPlayerByListWithTextFile extends StatisticsPlayerByList
 {
 
     /**This method load player from a file
      * @see fr.iutvalence.java.mp.BrickBreaker.StatisticsPlayerByList#loadPlayer(java.lang.String)
      */
     public void loadPlayers() throws ImpossibleDataAccess
     {
         
         DataInputStream in;
         try
         {
             in = new DataInputStream(new FileInputStream(new File("players.stats")));        
             String player;
             String str[] = new String[3];
             while((player = in.readLine()) != null)
             {
                 Player res = new Player();
                 str = player.split(" ");
                 res.setPseudo(str[0]);
                 res.setScore(Integer.parseInt(str[1]));
                 res.setGameNumber(Integer.parseInt(str[2]));
                 this.addPlayer(res);
             }
         }
         catch (IOException e)
         {
             throw new ImpossibleDataAccess();
         }
         catch (AlreadyExists e)
         {
             // TODO Auto-generated catch block
             System.err.println("Abording datas players loading");
             e.printStackTrace();
         }
         
     }
     
     /**
      * Players are stocked into a file
      * @see fr.iutvalence.java.mp.BrickBreaker.StatisticsPlayerByList#savePlayers()
      */
    public void savePlayers()
     {
         DataOutputStream out;
         List<Player> listToSave = this.getListe();
         try
         {
             out = new DataOutputStream(new FileOutputStream(new File("players.stats")));
             String str="";
             
             for(int i =0;   i < listToSave.size();   i++)
             {
                 str = listToSave.get(i).getPseudo() + " " + listToSave.get(i).getScore() + " " + listToSave.get(i).getGameNumber() + "\n";
                 out.writeBytes(str);
             }
            out.flush();
            out.close();
         }
         catch (IOException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         
     }
     
 }
