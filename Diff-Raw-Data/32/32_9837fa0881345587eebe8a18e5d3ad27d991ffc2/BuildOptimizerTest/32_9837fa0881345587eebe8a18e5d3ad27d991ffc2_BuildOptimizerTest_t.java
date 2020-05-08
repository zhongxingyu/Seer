 package sc2build.optimizer;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.TreeSet;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import sc2build.optimizer.BuildOptimizer.Node;
 import sc2build.optimizer.SC2Planner.Entity;
 import sc2build.optimizer.SC2Planner.Faction;
 
 public class BuildOptimizerTest
 {
 	private static class Bean
 	{
 		private int time;
 	}
 	
 	@Test
 	public void test2ZealotsTree()
 	{
 		SC2Planner planner = new SC2Planner();
 		planner.init(Faction.PROTOSS);
 		
 		Entity zealot = planner.getEntities().get("Zealot");
 		
 		List<Entity> req = new LinkedList<>();
 		req.add(zealot);
 		req.add(zealot);
 		
 		BuildOptimizer bo = new BuildOptimizer(planner);
 		bo.buildRaceTree(planner.getRace(Faction.PROTOSS.getName()), req);
		bo.storeInFile();
 		Node node = bo.getMinNode();
		Assert.assertNotNull(node);
 		Assert.assertEquals("Zealot", node.getEntity().name);
 		node = node.getParent();
 		Assert.assertEquals("Zealot", node.getEntity().name);
 		node = node.getParent();
		Assert.assertEquals("Gateway", node.getEntity().name);
		node = node.getParent();
		Assert.assertEquals("Gateway", node.getEntity().name);
		node = node.getParent();
		Assert.assertEquals("Pylon", node.getEntity().name);
		Assert.assertNull(node.getParent().getEntity());
 	}
 	
 	@Test
 	public void test3StalkerRushTree()
 	{
 		SC2Planner planner = new SC2Planner();
 		planner.init(Faction.PROTOSS);
 		
 		Entity stalker = planner.getEntities().get("Stalker");
 		
 		List<Entity> req = new LinkedList<>();
 		req.add(stalker);
 		req.add(stalker);
 		req.add(stalker);
 		
 		BuildOptimizer bo = new BuildOptimizer(planner);
 		bo.buildRaceTree(planner.getRace(Faction.PROTOSS.getName()), req);
		Assert.assertNotNull(bo.getMinNode());
 		bo.getMinNode().dump();
 	}
 	
 	@Test
 	public void treeSetTest()
 	{
 		TreeSet<Bean> set = new TreeSet<>(new Comparator<Bean>() {
 			@Override
 			public int compare(Bean o1, Bean o2) {
 				return o1.time - o2.time;
 			}
 		});
 		
 		Bean bean = new Bean();
 		bean.time = 1;
 		
 		Bean bean2 = new Bean();
 		bean.time = 1;
 		set.add(bean);
 		set.add(bean2);
 		Assert.assertEquals(2, set.size());
 	}
 	
 	@Test
 	public void testWrite() throws Exception
 	{
 		SC2Planner planner = new SC2Planner();
 		planner.init(Faction.PROTOSS);
 		
 		BuildOptimizer bo = new BuildOptimizer(planner);
 		bo.buildRaceTree(planner.getRace(Faction.PROTOSS.getName()), Collections.<Entity>emptyList());
 	}
 }
