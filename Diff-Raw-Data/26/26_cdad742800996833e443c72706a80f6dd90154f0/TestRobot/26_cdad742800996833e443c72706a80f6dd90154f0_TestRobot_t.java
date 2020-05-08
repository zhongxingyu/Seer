 package entities;
 
 import java.io.FileWriter;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.lang.String;
 
 import wifi.Communication;
 
 public class TestRobot{
 	public static void main(String args[]){
 		// creation du robot
 		Robot r1 = new Robot("192.168.0.36");
 		Robot r2 = new Robot("192.168.0.37");
 
 		// creation des commandes du robot 1
 		Command c0 = new Command();
 		c0.setAction(Command.TURN);
 		c0.addParameter(new Parameter("angle",0),0);
 		Command c1 = new Command();
 		c1.setAction(Command.TURN);
 		c1.addParameter(new Parameter("angle",-179),0);
 		Command c2 = new Command();
 		c2.setAction(Command.FORWARD);
 		c2.addParameter(new Parameter("distance",155),0);
 		Command c3 = new Command();
 		c3.setAction(Command.TURN);
 		c3.addParameter(new Parameter("angle",-91),0);
 		Command c4 = new Command();
 		c4.setAction(Command.FORWARD);
 		c4.addParameter(new Parameter("distance",90),0);
 		Command c5 = new Command();
 		c5.setAction(Command.TURN);
 		c5.addParameter(new Parameter("angle",90),0);
 		Command c6 = new Command();
 		c6.setAction(Command.FOLLOW_WALL);
 		c6.addParameter(new Parameter("wall",1),0);
 		c6.addParameter(new Parameter("distance",270),1);
 		Command c7 = new Command();
 		c7.setAction(Command.TURN);
 		c7.addParameter(new Parameter("angle",0),0);
 		Command c8 = new Command();
 		c8.setAction(Command.FOLLOW_WALL);
 		c8.addParameter(new Parameter("wall",1),0);
 		c8.addParameter(new Parameter("distance",190),1);
 		Command c9 = new Command();
 		c9.setAction(Command.TURN);
 		c9.addParameter(new Parameter("angle",45),0);
 		Command c10 = new Command();
 		c10.setAction(Command.FORWARD);
 		c10.addParameter(new Parameter("distance",14),0);
 		Command c11 = new Command();
 		c11.setAction(Command.TURN);
 		c11.addParameter(new Parameter("angle",45),0);
 		Command c12 = new Command();
 		c12.setAction(Command.FORWARD);
 		c12.addParameter(new Parameter("distance",70),0);
 		Command c13 = new Command();
 		c13.setAction(Command.TURN);
 		c13.addParameter(new Parameter("angle",45),0);
 		Command c14 = new Command();
 		c14.setAction(Command.FORWARD);
 		c14.addParameter(new Parameter("distance",7),0);
 		Command c15 = new Command();
 		c15.setAction(Command.STOP);
 
 		// creation des commandes du robot 2
 
 		Command d1 = new Command();
 		d1.setAction(Command.FOLLOW_WALL);
 		d1.addParameter(new Parameter("wall",2),0);
		d1.addParameter(new Parameter("distance",40),1);
 		Command d2 = new Command();
 		d2.setAction(Command.STOP);
 
 		// ajout des commandes 
 		r1.addCommand(c0,0);
 		r1.addCommand(c1,1);
 		r1.addCommand(c2,2);
 		r1.addCommand(c3,3);
 		r1.addCommand(c4,4);
 		r1.addCommand(c5,5);
 		r1.addCommand(c6,6);
 		r1.addCommand(c7,7);
 		r1.addCommand(c8,8);
 		r1.addCommand(c9,9);
 		r1.addCommand(c10,10);
 		r1.addCommand(c11,11);
 		r1.addCommand(c12,12);
 		r1.addCommand(c13,13);
 		r1.addCommand(c14,14);
 		r1.addCommand(c15,15);
 
 		r2.addCommand(d1,0);
 		r2.addCommand(d2,1);
 		 
 		// execution des commandes par le robot
 		r1.executeStack();
 		r2.executeStack();
 
 		//// Méthode pour stocker les logs dans le fichier log.txt
 		//String fileName = "log.txt";
 		//String filePath = System.getProperty("user.dir")+"/"+fileName;
 		//log = log.replaceAll("<br/>","\n");
 		//try{
 		//	FileWriter fw = new FileWriter(filePath,true);
 		//	BufferedWriter output = new BufferedWriter(fw);
 		//	output.write(log);
 		//	output.flush();
 		//	output.close();
 		//} catch(IOException e){
 		//	e.printStackTrace();
 		//}
 		//
 
 		//// Test de l'affichage des robots connectés
 		//String[] robotList = Communication.getOnlineRobots();
 		//for (int j = 0;j < robotList.length; j++){
 		//	System.out.println(robotList[j]);
 		//}
 
 	}
 
 
 
 }
