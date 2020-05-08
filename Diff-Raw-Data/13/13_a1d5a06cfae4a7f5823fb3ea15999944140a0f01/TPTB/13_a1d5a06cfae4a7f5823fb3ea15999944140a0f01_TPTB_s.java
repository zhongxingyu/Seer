 import java.util.LinkedList;
 
 public class TPTB{
     /** The pro and con list */
     private LinkedList<Argument> con, pro;
    
     /**
      * Creates this with a pro- and a con-list.
      */
     public TPTB(){
         pro = new LinkedList<Argument>();
         con = new LinkedList<Argument>();
     }
 
     /**
      * Prints the calculated decision.
      */
     public void printDecision(){
         System.out.println("========================================");
         System.out.println("+            The decision is:          +");
         System.out.println("========================================");
         System.out.println("");
         System.out.println("The pro-arguments are: \n");
         for(Argument arg: pro){
            System.out.println(arg.getArgument() + "(" + arg.getRelevance() + ")");
         }
         System.out.println("");
         System.out.println("The con-arguments are: \n");
         for(Argument arg: con){
            System.out.println(arg.getArgument() + "(" + arg.getRelevance() + ")");
         }
         System.out.println("");
         if(calculateDecision())
             System.out.println("Do it!");
         else
             System.out.println("Don't do it");
     }
 
     /**
      * Calculate decision.
      * 
      * @return true if it should be done
      */
     private boolean calculateDecision(){
         int provalue = 0;
         for(Argument arg: pro){
             provalue += arg.getRelevance();
         }
 
         int convalue = 0;
         for(Argument arg: con){
             convalue += arg.getRelevance();
         }
 
         return (provalue > convalue) ? true : false;
     }
 
     /**
      * Inserts a new argument to given list.
      * 
      * @param contra if true add to contra-list else to pro-list
      * @param argument is the argument
      * @param relevance is the importance of the argument
      */
     public void insertArgument(boolean contra, String argument, int relevance){
         if(contra)
             con.add(new Argument(argument,relevance));
         else
             pro.add(new Argument(argument,relevance));
     }
 }
