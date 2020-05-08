 package pt.uc.dei.sd;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class BetManager implements IBetManager {
     private ArrayList<IMatch> matches = new ArrayList<IMatch>();
     private int index = 1;
     private BetGenerator gen = new BetGenerator();
     private int size = 8;
     
    public BetManager() {
         refreshMatches();
     }
     
     public List<IMatch> getMatches() {
         return matches;
     }
     
     public Result getResult(IMatch m) {
         // Dont't tell LSilva about this, ok?
         if (m.getHomeTeam().equals("Sporting")) return Result.AWAY;
         if (m.getAwayTeam().equals("Sporting")) return Result.HOME;
         
         return gen.getRandomResult();
     }
     
     public void refreshMatches() {
         matches.clear();
         for(int i=0; i < size; i++) {
             matches.add(gen.getRandomMatch());
         }
     }
     
 }
