 public class Question {
String ask, choice1, choice2, choice3, choice4;	
 
	    public Question(String q, String C1, String C2, String C3, String C4)
 	    {
 	    	ask = q;
	    	choice1 = C1;
	    	choice2 = C2;
	    	choice3 = C3;
	    	choice4 = C4;
 	    }
 	    
 	    public String getQuestion()
 	    {
 	        return ask;
 	    }
	    public String toString()
	    {
	    	return (ask + ":\n1." + choice1 + "\n2." + choice2 + "\n3." + choice3 + "\n4." + choice4);
	    }
 	} // main method
 
