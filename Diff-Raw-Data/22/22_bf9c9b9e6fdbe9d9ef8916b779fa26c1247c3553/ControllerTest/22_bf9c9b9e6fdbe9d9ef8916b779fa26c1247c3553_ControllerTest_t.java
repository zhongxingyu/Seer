 package com.challenge.dropboxdiet;
 
 import java.util.ArrayList;
 
 import org.junit.Test;
 
 
 public class ControllerTest {
 	
 	@Test
 	public void TestFindClaorieNeutralPerkGroups()
 	{
 		ArrayList<Perk> perkList = new ArrayList<Perk>();
 		perkList.add(new Perk(1,"free=lunch",802));
 		perkList.add(new Perk(2,"mixed-nuts", 421));
 		perkList.add(new Perk(3,"orange-juce", 143));
 		perkList.add(new Perk(4, "heavy-ddr-session", -302));
 		perkList.add(new Perk(5, "cheese-snaks", 137));
 		perkList.add(new Perk(6,"cookies",316));
 		perkList.add(new Perk(7,"mexican-coke",150));
 		perkList.add(new Perk(8,"dropballers-basktball",-611));
 		perkList.add(new Perk(9,"coding-siz-hours",-466));
		perkList.add(new Perk(10,"riding-scooter",-42));
		perkList.add(new Perk(11,"rock-band",-195));
		perkList.add(new Perk(12,"playing-drums",-295));
 		Controller controller = new Controller(perkList);
 		ArrayList<ArrayList<Perk>> calorieNeutralGroups = controller.findCalorieNeutralPerkGroups();
		for(ArrayList<Perk> group:calorieNeutralGroups)
		{
			System.out.println("Perk Group");
			for(Perk perk : group)
				perk.printPerk();
		}
 	}
 
 }
