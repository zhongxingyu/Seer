 /*
  * Holds a team's info.
  */
 package footballpool.core;
 
 import java.util.ArrayList;
 
 /**
  *
  * @author scottl
  * @version 0.1
  */
 public class Team 
 {
     final public String name;
     private ArrayList<Integer> scores;
     
     
     public Team(String name)
     {
         this.name = name;
         
         int numWeeks = 20;
         scores = new ArrayList(numWeeks);
         for (int ct = 0; ct < numWeeks; ct++)
             scores.add(0);
     }
     
     
     public void setScore(int weekNumber, int points)
     {
         scores.set(weekNumber, points);
     }
     
     public int getScore(int week)
     {return scores.get(week);}
     
     
     /**
      * Two teams with the same name are equal.
      * @param team The Team to check against this Team.
      * @return true if their team names match.
      */
     @Override
     public boolean equals(Object team)
     {
      if (! (team instanceof Team))
          return false;
     return ((Team)team).equals(name);
     }
     
     @Override
     public String toString()
     {return name;}
 }
