 package entities;
 
 import java.io.FileWriter;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.lang.String;
 
 import wifi.Communication;
 
 public class TestRobot{
 	public static void main(String args[]){
 		// creation du robot
 		Robot r = new Robot("192.168.0.36");
 
 		// creation de la commande
 		Command c = new Command();
 		//c.setAction(Command.FIND_CORNER);
 		c.setAction(Command.CROSSING);
 
 		// creation des parametres et ajout a la commande c
 		c.addParameter(new Parameter("noeud",1),0);
		c.addParameter(new Parameter("wall",1),1);
		c.addParameter(new Parameter("cote",2),2);
 
 //		Command corner = new Command();
 //		corner.setAction(Command.FIND_CORNER);
 //
 //		corner.addParameter(new Parameter("wall",2),0);
 //
 //		// creation d'une deuxieme commande
 //		Command c2 = new Command();
 //		c2.setAction(Command.CROSSING);
 //
 //		// creation des parametres et ajout a la commande c2
 //		c2.addParameter(new Parameter("noeud",1),0);
 //		c2.addParameter(new Parameter("direction",1),1);
 //		c2.addParameter(new Parameter("cote",2),2);
 //		
 //		// creation d'une troisieme commande
 //		Command c3 = new Command();
 //		c3.setAction(Command.FOLLOW_WALL);
 //
 //		// creation des parametres et ajout a la commande c3
 //		c3.addParameter(new Parameter("wall",2),0);
 //		c3.addParameter(new Parameter("dist",130),1);
 //		  
 //		// creation d'une quatrieme commande
 //		Command c4 = new Command();
 //		c4.setAction(Command.FIND_CORNER);
 //
 //		// creation des parametres et ajout a la commande c4
 //		c4.addParameter(new Parameter("wall",2),0);
 
 		// Commande stop
 		Command c5 = new Command();
 		c5.setAction(Command.STOP);
 
 		// ajout des commandes dans la liste des commandes a effectuer par le robot
 		r.addCommand(c,0);
 		//r.addCommand(corner,1);
 		//r.addCommand(c2,1);
 		//r.addCommand(c3,2);
 		//r.addCommand(c4,3);
 		r.addCommand(c5,1);
 		 
 		// execution des commandes par le robot
 		r.executeStack();
 
 		// Méthode pour traiter les Feedback 
 		String log = r.getFeedback();
 		String[] tmp = log.split("<br/>");
 		for (int i = 0; i < tmp.length; i++){
 			Feedback f = new Feedback(tmp[i]);
 			System.out.println(f.getAction()+f.getDate()+f.getDetails());
 		}
 
 		
 		// Méthode pour stocker les logs dans le fichier log.txt
 		String fileName = "log.txt";
 		String filePath = System.getProperty("user.dir")+"/"+fileName;
 		log = log.replaceAll("<br/>","\n");
 		try{
 			FileWriter fw = new FileWriter(filePath,true);
 			BufferedWriter output = new BufferedWriter(fw);
 			output.write(log);
 			output.flush();
 			output.close();
 		} catch(IOException e){
 			e.printStackTrace();
 		}
 		
 		// Test de la suppression des commandes
 		//r.clearCommands();
 
 		// Test de l'affichage des robots connectés
 		String[] robotList = Communication.getOnlineRobots();
 		for (int j = 0;j < robotList.length; j++){
 			System.out.println(robotList[j]);
 		}
 
 	}
 
 
 
 }
