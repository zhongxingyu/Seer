 /*
  * Created on Sep 14, 2006
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.lungu.asimplesample.model.beings;
 
 import java.util.Random;
 
 import org.lungu.asimplesample.model.things.Kebab;
 import org.lungu.asimplesample.model.things.Laptop;
 import org.lungu.asimplesample.util.PatentGenerator;
 
 
 /**
  * @author mircea
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class InformaticsPHD extends Human{
 	Laptop laptop;
 	public InformaticsPHD()
 	{
 		laptop = new Laptop();
 		stamina = 1000;
 		doSomeWork();
 	}
 	
 	public void speakYourMind()
 	{
 		if (stamina > 500)
 		{
 			System.out.println("[iPhD] I want to play MMORPG's! ");	
 		}
 		else
 		{
 			System.out.println("[iPhD] I want to sleep");
 		}
 	}
 	public void doSomeWork()
 	{
 		laptop.turnOn();
 		laptop.runVariousPrograms();
 		stamina -= 20+ (new Random(100)).nextInt();
 	}
 	public void eat()
 	{
 		stamina += (new Kebab()).calories();
 	}
 
 	public void generatePatent(PatentGenerator gen) {
 		stamina -= 10;
		
 	}
 }
