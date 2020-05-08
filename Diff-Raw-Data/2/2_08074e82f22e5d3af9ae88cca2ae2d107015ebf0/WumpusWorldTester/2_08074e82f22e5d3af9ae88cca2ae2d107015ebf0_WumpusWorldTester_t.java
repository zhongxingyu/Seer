 package tests;
 
import demos.wumpus.WumpusWorld;
 
 import vitro.*;
 import vitro.graph.*;
 import vitro.util.*;
 import java.util.*;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class WumpusWorldTester {
 
 	@Test
 	public void testDone() {
 		WumpusWorld w = new WumpusWorld();
 		Node node0 = w.createNode();
 		Node node1 = w.createNode();
 		
 		node0.actors.add(w.createWumpus());
 		node1.actors.add(w.createHunter());
 		assertFalse(w.done());
 		
 		w.actors.clear();
 		node0.actors.add(w.createWumpus());
 		assertTrue(w.done());
 		
 		w.actors.clear();
 		node1.actors.add(w.createWumpus());
 		assertTrue(w.done());
 		
 		w.actors.clear();
 		assertTrue(w.done());
 	}
 	
 	@Test
 	public void testAlive() {
 		WumpusWorld w = new WumpusWorld();
 		
 		WumpusWorld.Hunter hunter = w.createHunter();
 		assertFalse(hunter.alive());
 		
 		w.actors.add(hunter);
 		assertTrue(hunter.alive());
 	}
 	
 	@Test
 	public void testFlapping() {
 		WumpusWorld w = new WumpusWorld();
 		Node node0 = w.createNode();
 		Node node1 = w.createNode();
 		Node node2 = w.createNode();
 		w.createEdge(node0, node1);
 		w.createEdge(node1, node2);
 		
 		WumpusWorld.Hunter hunter = w.createHunter();
 		node0.actors.add(hunter);
 		
 		node1.actors.add(w.createBat());
 		assertTrue(hunter.flapping());
 		
 		node1.actors.clear();
 		node2.actors.add(w.createBat());
 		assertFalse(hunter.flapping());
 	}
 	
 	@Test
 	public void testWind() {
 		WumpusWorld w = new WumpusWorld();
 		Node node0 = w.createNode();
 		Node node1 = w.createNode();
 		Node node2 = w.createNode();
 		w.createEdge(node0, node1);
 		w.createEdge(node1, node2);
 		
 		WumpusWorld.Hunter hunter = w.createHunter();
 		node0.actors.add(hunter);
 		
 		node1.actors.add(w.createPit());
 		assertTrue(hunter.wind());
 		
 		node1.actors.clear();
 		node2.actors.add(w.createPit());
 		assertFalse(hunter.wind());
 	}
 	
 	@Test
 	public void testScent() {
 		WumpusWorld w = new WumpusWorld();
 		Node node0 = w.createNode();
 		Node node1 = w.createNode();
 		Node node2 = w.createNode();
 		w.createEdge(node0, node1);
 		w.createEdge(node1, node2);
 		
 		WumpusWorld.Hunter hunter = w.createHunter();
 		node0.actors.add(hunter);
 		
 		node1.actors.add(w.createWumpus());
 		assertTrue(hunter.scent());
 		
 		node1.actors.clear();
 		node2.actors.add(w.createWumpus());
 		assertFalse(hunter.scent());
 	}
 	
 	@Test
 	public void testWhistle() {
 		WumpusWorld w = new WumpusWorld();
 		Node node0 = w.createNode();
 		Node node1 = w.createNode();
 		w.createEdge(node0, node1);
 		
 		WumpusWorld.Hunter hunter = w.createHunter();
 		node0.actors.add(hunter);
 		
 		Action action = Groups.firstOfType(WumpusWorld.ShootAction.class, hunter.actions());
 		WumpusWorld.ShootAction shoot = (WumpusWorld.ShootAction)action;
 		assertFalse(shoot == null);
 		
 		shoot.apply();
 		assertTrue(hunter.whistle());
 	}
 	
 	@Test
 	public void testWalk() {
 		WumpusWorld w = new WumpusWorld();
 		Node node0 = w.createNode();
 		Node node1 = w.createNode();
 		Node node2 = w.createNode();
 		Node node3 = w.createNode();
 		Edge edge1 = w.createEdge(node0, node1);
 		Edge edge2 = w.createEdge(node0, node2);
 		Edge edge3 = w.createEdge(node0, node3);
 		
 		WumpusWorld.Bat    bat    = w.createBat();
 		WumpusWorld.Pit    pit    = w.createPit();
 		WumpusWorld.Wumpus wumpus = w.createWumpus();
 		
 		node1.actors.add(bat);
 		node2.actors.add(pit);
 		node3.actors.add(wumpus);
 		
 		WumpusWorld.Hunter hunter = w.createHunter();
 		node0.actors.add(hunter);
 		hunter.arrows = 0;
 		
 		Set<Action> actions = hunter.actions();
 		assertTrue(actions.size() == 3);
 	}
 	
 	@Test
 	public void testWalkInToBat() {
 		WumpusWorld w = new WumpusWorld();
 		Node node0 = w.createNode();
 		Node node1 = w.createNode();
 		Edge edge1 = w.createEdge(node0, node1);
 		
 		WumpusWorld.Bat bat = w.createBat();
 		node1.actors.add(bat);
 		
 		WumpusWorld.Hunter hunter = w.createHunter();
 		node0.actors.add(hunter);
 		hunter.arrows = 0;
 		
 		Set<Action> actions = hunter.actions();
 		assertTrue(actions.size() == 1);
 		
 		Action action = Groups.first(actions);
 		action.apply();
 		assertTrue(hunter.alive());
 	}
 	
 	@Test
 	public void testWalkInToPit() {
 		WumpusWorld w = new WumpusWorld();
 		Node node0 = w.createNode();
 		Node node1 = w.createNode();
 		Edge edge1 = w.createEdge(node0, node1);
 		
 		WumpusWorld.Pit pit = w.createPit();
 		node1.actors.add(pit);
 		
 		WumpusWorld.Hunter hunter = w.createHunter();
 		node0.actors.add(hunter);
 		hunter.arrows = 0;
 		
 		Set<Action> actions = hunter.actions();
 		assertTrue(actions.size() == 1);
 		
 		Action action = Groups.first(actions);
 		action.apply();
 		assertFalse(hunter.alive());
 	}
 	
 	@Test
 	public void testWalkInToWumpus() {
 		WumpusWorld w = new WumpusWorld();
 		Node node0 = w.createNode();
 		Node node1 = w.createNode();
 		Edge edge1 = w.createEdge(node0, node1);
 		
 		WumpusWorld.Wumpus wumpus = w.createWumpus();
 		node1.actors.add(wumpus);
 		
 		WumpusWorld.Hunter hunter = w.createHunter();
 		node0.actors.add(hunter);
 		hunter.arrows = 0;
 		
 		Set<Action> actions = hunter.actions();
 		assertTrue(actions.size() == 1);
 		
 		Action action = Groups.first(actions);
 		action.apply();
 		assertFalse(hunter.alive());
 	}
 	
 	@Test
 	public void testShoot() {
 		WumpusWorld w = new WumpusWorld();
 		Node node0 = w.createNode();
 		Node node1 = w.createNode();
 		w.createEdge(node0, node1);
 		
 		WumpusWorld.Hunter hunter = w.createHunter();
 		node0.actors.add(hunter);
 		
 		{
 			hunter.arrows = 0;
 		
 			Action action = Groups.firstOfType(WumpusWorld.ShootAction.class, hunter.actions());
 			WumpusWorld.ShootAction shoot = (WumpusWorld.ShootAction)action;
 			assertTrue(shoot == null);
 		}
 		
 		{
 			hunter.arrows = 1;
 		
 			Action action = Groups.firstOfType(WumpusWorld.ShootAction.class, hunter.actions());
 			WumpusWorld.ShootAction shoot = (WumpusWorld.ShootAction)action;
 			assertFalse(shoot == null);
 		
 			shoot.apply();
 			Actor actor = Groups.firstOfType(WumpusWorld.Arrow.class, w.actors);
 			assertFalse(actor == null);
 		
 			assertTrue(hunter.arrows == 0);
 		}
 	}
 	
 	@Test
 	public void testShootKillWumpus() {
 		WumpusWorld w = new WumpusWorld();
 		Node node0 = w.createNode();
 		Node node1 = w.createNode();
 		w.createEdge(node0, node1);
 		
 		WumpusWorld.Hunter hunter = w.createHunter();
 		node0.actors.add(hunter);
 
 		WumpusWorld.Wumpus wumpus = w.createWumpus();
 		node1.actors.add(wumpus);
 		
 		Action action = Groups.firstOfType(WumpusWorld.ShootAction.class, hunter.actions());
 		WumpusWorld.ShootAction shoot = (WumpusWorld.ShootAction)action;
 		assertFalse(shoot == null);
 		
 		shoot.apply();
 		Actor actor = Groups.firstOfType(WumpusWorld.Arrow.class, w.actors);
 		assertFalse(actor == null);
 		
 		WumpusWorld.Arrow arrow = (WumpusWorld.Arrow)actor;
 		DestroyAction kill = new DestroyAction(w, wumpus, arrow);
 		assertTrue(arrow.actions().contains(kill));
 		
 		kill.apply();
 		assertTrue(w.done());
 		
 		kill.undo();
 		assertFalse(w.done());
 	}
 }
