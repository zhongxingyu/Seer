 package org.fourdnest.androidclient.test;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.fourdnest.androidclient.Egg;
 import org.fourdnest.androidclient.EggManager;
 import org.fourdnest.androidclient.Tag;
 
 import android.test.AndroidTestCase;
 
 public class EggManagerTest extends AndroidTestCase {
 	
 	EggManager eggManager;
 	EggManager eggManager2;
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		this.eggManager	 = new EggManager(this.getContext(), "default");
 		this.eggManager2 = new EggManager(this.getContext(), "alternate");
 		
 		this.eggManager.deleteAllEggs();
 		this.eggManager2.deleteAllEggs();
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
		
		this.eggManager.close();
		this.eggManager2.close();
 	}
 	
 	public void testSaveAndGetEgg() {
 		long now = System.currentTimeMillis();
 		ArrayList<Tag> tags = new ArrayList<Tag>();
 		tags.add(new Tag("tag1"));
 		tags.add(new Tag("tagi toinen"));
 		Egg e = new Egg(
 			null,
 			1,
 			"Matti",
 			null,
 			null,
 			"TestCaption",
 			tags,
 			now,
 			new Date()
 		);
 		
 		e = this.eggManager.saveEgg(e);
 		
 		Egg fetchedEgg = this.eggManager.getEgg(e.getId());
 		assertTrue(e.equals(fetchedEgg));
 		
 		
 	}
 	
 	public void testSaveEggToMultipleManagers() {
 		long now = System.currentTimeMillis();
 		ArrayList<Tag> tags = new ArrayList<Tag>();
 		Egg e = new Egg(
 			null,
 			1,
 			"Matti",
 			null,
 			null,
 			"TestCaption",
 			tags,
 			now,
 			new Date()
 		);
 		Egg e2 = e;
 		
 		e = this.eggManager.saveEgg(e);
 		e2 = this.eggManager2.saveEgg(e);
 		
 		Egg fetchedEgg = this.eggManager.getEgg(e.getId());
 		Egg fetchedEgg2 = this.eggManager2.getEgg(e2.getId());
 		assertTrue(e.equals(fetchedEgg));
 		assertTrue(e2.equals(fetchedEgg2));
 	
 	}
 
 	public void testListEggs() {
 		long now = System.currentTimeMillis();
 		ArrayList<Tag> tags = new ArrayList<Tag>();
 		
 		Egg e1 = new Egg(
 			null,
 			1,
 			"Matti",
 			null,
 			null,
 			"CaptionForEgg",
 			tags,
 			now,
 			new Date()
 		);
 		e1 = this.eggManager.saveEgg(e1);
 		
 		Egg e2 = new Egg(
 			null,
 			2,
 			"Matti",
 			null,
 			null,
 			"CaptionToo!",
 			tags,
 			now,
 			new Date()
 		);
 		e2 = this.eggManager.saveEgg(e2);
 		
 		Egg e3 = new Egg(
 			null,
 			10,
 			"Matti",
 			null,
 			null,
 			null,
 			tags,
 			now,
 			new Date()
 		);
 		e3 = this.eggManager.saveEgg(e3);
 		
 		List<Egg> eggs = this.eggManager.listEggs();
 		
 		assertTrue(eggs.get(0).equals(e1));
 		assertTrue(eggs.get(1).equals(e2));
 		assertTrue(eggs.get(2).equals(e3));
 		
 	}
 	
 	public void testDeleteEgg() {
 		long now = System.currentTimeMillis();
 		ArrayList<Tag> tags = new ArrayList<Tag>();
 		
 		Egg e1 = new Egg(
 				0,
 				1,
 				"Matti",
 				null,
 				null,
 				"CaptionForEgg",
 				tags,
 				now,
 				new Date()
 		);
 		e1 = this.eggManager.saveEgg(e1);
 		
 		// Check that 1 nest is deleted
 		int result = this.eggManager.deleteEgg(e1.getId());
 		assertEquals(1, result);
 		
 		// Check that nest with that id no longer exists
 		Egg e = this.eggManager.getEgg(e1.getId());
 		assertNull(e);
 	}
 	
 	public void testDeleteAllNests() {			
 		long now = System.currentTimeMillis();
 		ArrayList<Tag> tags = new ArrayList<Tag>();
 		
 		Egg e1 = new Egg(
 				null,
 				1,
 				"Matti",
 				null,
 				null,
 				"CaptionForEgg",
 				tags,
 				now,
 				new Date()
 		);
 		e1 = this.eggManager.saveEgg(e1);
 			
 		Egg e2 = new Egg(
 				null,
 				2,
 				"Matti",
 				null,
 				null,
 				"CaptionToo!",
 				tags,
 				now,
 				new Date()
 		);
 		e2 = this.eggManager.saveEgg(e2);
 		
 		this.eggManager.deleteAllEggs();
 		
 		assertNull(this.eggManager.getEgg(1));
 		assertNull(this.eggManager.getEgg(2));
 		
 		assertEquals(this.eggManager.listEggs().size(), 0);		
 	}
 	
 
 }
