 package pt.uc.dei.sd;
 
 
 public class Example {
     public static void main(String[] args) {
        IBetManager man = new BetManager();
 
         System.out.println("========= First Batch of Matches =========");        
         for (IMatch m : man.getMatches()) {
             System.out.println(m.getHomeTeam() + " vs " + m.getAwayTeam());
         }
         System.out.println("========= Results =========");
         for (IMatch m : man.getMatches()) {
             System.out.print(m + ": ");
             switch (man.getResult(m)) {
                 case HOME: 
                     System.out.println("1");
                     break;
                 case AWAY: 
                     System.out.println("2");
                     break;
                 default: 
                     System.out.println("X");
                     break;
             }
         }
         System.out.println("======= New Batch of Matches =======");
         man.refreshMatches();
         
         for (IMatch m : man.getMatches()) {
             System.out.println(m.getHomeTeam() + " vs " + m.getAwayTeam());
         }
         
     }
 }
