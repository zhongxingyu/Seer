 /**
  * Erstellt eine kleine Anwendung welche mittels Parser User Inputs liest und dies dem Kontroller zur Weiter-
  * arbeitung geebergibt.
  *
  * @author  Dave Kramer, Simon Schwarz
  * @version 1.0
  */
 
 public class Anwendung {
 	
 	private static Parser parser;
 	private static Kontroller kontroller;
 
 	public Anwendung()
 	{
 		
 	}
 	
     public static void main(String args[])
     {
     	Anwendung anwendung = new Anwendung();
     	anwendung.start();
     }
     
    
     public void start()
     {
     	parser = new Parser();
     	kontroller = new Kontroller();
     	boolean weiter = true;
     	
     	while(weiter)
     	{
     		Befehl input = parser.liefereBefehl();
     		weiter = kontroller.verarbeiteBefehl(input);
     	}
     }
     
     
 }
