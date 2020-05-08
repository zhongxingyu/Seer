 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 
 import ntu.csie.oop13spring.POOAction;
 import ntu.csie.oop13spring.POOArena;
 import ntu.csie.oop13spring.POOCoordinate;
 
 
 public class Dragonrider extends Mypet{
     public Dragonrider() throws IOException
     {
 	int i;
 	hp=120;
 	maxhp=120;
 	agility=90;
 	ap=9;
 	mp=5;
 	maxap=9;
 	maxmp=5;
 	nation=0;
 	patk=40;
 	matk=40;
 	pdef=10;
 	mdef=10;
 	portrait=new ImageIcon();
 	fullportrait=new ImageIcon();
 	this.setcoordinate(new Mycoordinate(3,7));
 	name="Dragon Rider";
	subscription="<b><font color='red'>(Boss)</font></b> Controled by another player please<br/>A rider that want to test your ability<br/>she may join you if you prove <br/>your strength\n";
 	portrait.setImage(ImageIO.read(new File("./images/dragonriderportrait.jpg")));
 	fullportrait.setImage(ImageIO.read(new File("./images/dragonrider.jpg")));
 	action.add((Myskill)new BloodyBite());
 	action.add((Myskill)new Meteor());
 	action.add((Myskill)new Spitfire());
 	action.add((Myskill)new Ancient_Curse());
     }
 }
