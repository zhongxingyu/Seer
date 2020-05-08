 
 package jcaverntest;
 
 import junit.framework.*;
 import java.net.*;
 
 import jcavern.*;
 
 /**
 * Tests functionality of Mission.
  *
  * @author	Bill Walker
 * @version	$Id$
  */
 public class MissionTest extends TestCase
 {	
 	/**
 	 * Creates a suite of tests.
 	 */
 	public MissionTest(String name)
 	{
 		super(name);
 	}
 
 	public void testQuota() throws MalformedURLException
 	{
 		MonsterFactory.loadPrototypes(new URL("file:bin/monster.dat"));
 		
 		Player		aPlayer = new Player("Mr. Test");
 		Mission		aMission = MonsterFactory.createMission(aPlayer);
 		
 		for (int index = 0; index < aMission.getQuota(); index++)
 		{
 			// System.out.println("MissionTest.testQuota() " + aMission);
 			
 			assert("mission not complete", ! aMission.getCompleted());
 			assert("mission name says not complete", aMission.toString().indexOf("completed") == -1);
 			aMission.adjustQuota((Thing) aMission.getTarget().clone());
 		}
 			
 		// System.out.println("MissionTest.testQuota() " + aMission);
 
 		assert("mission complete", aMission.getCompleted());
 		assert("mission name says complete", aMission.toString().indexOf("completed") >= 0);
 	}
 }
