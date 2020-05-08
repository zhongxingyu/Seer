 package whiterabbit;
 
 public class JeffersonAirplane {
    public static final String WELCOME = "\n\n" +
            "Welcome.  My name is Hume.  I am an educational computer simulation. \n" +
            "I'm your guide to the Agile world.\n\n" +
            "In order the determine if Agile is right for you, I need to know how you'll \n" +
            "deal with the following scenarios. These are real-life experiences from Agile \n" +
            "projects. How you choose to deal with these will tell me if you're truly \n" +
            "ready to be on an Agile team.\n\n";
 
     private final Printer printer;
 
     public JeffersonAirplane() {
         printer = new Printer();
     }
 
     public JeffersonAirplane(Printer printer) {
         this.printer = printer;
     }
 
     public static void main(String[] args) {
         new JeffersonAirplane().run();
 	}
 
     void run() {
         printer.print(WELCOME);
         String test1 = "How sure are you that you want to be doing Agile development?";
         Question question = new Question(test1);
         question.addAnswer("A) very sure.");
         question.addAnswer("B) kind of sure.");
         question.addAnswer("C) Not very sure at all.");
         printer.print(question.toString());
     }
 
     static  class Printer {
         public void print(String toPrint){
             System.out.println(toPrint);
         }
     }
 }
