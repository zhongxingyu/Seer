 public class Main {
 
 	private static SemanticInfo semInfo;
     
     public static void main (String args[]) 
     {
     	semInfo = SemanticInfo.instantiate();
     	semInfo.getQuestion();
     	VisualManager.instantiate(semInfo.getQuestion());
     	View.instantiate();
     }
 }
