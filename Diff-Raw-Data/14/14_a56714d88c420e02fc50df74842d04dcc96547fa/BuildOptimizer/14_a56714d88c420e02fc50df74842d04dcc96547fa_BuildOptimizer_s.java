 package sc2build.optimizer;
 
 import java.util.ArrayList;
 import java.util.Deque;
 import java.util.LinkedList;
 import java.util.List;
 
 import sc2build.optimizer.SC2Planner.Entity;
 import sc2build.optimizer.SC2Planner.Race;
 
 public class BuildOptimizer
 {
 	private static final int TIME_THRESHOLD =  60 * 2 * 100; // 2 min
 	private static final int LEVEL_THRESHOLD = 4;
 	
 	public static class Node
 	{
 		private final Entity entity;
 		
 		private int time;
 		private Node parent;
 		private List<Node> children = new LinkedList<>();
 		
 		private boolean leafNode = false; 
 		
 		public Node(Node parent, Entity entity, int time)
 		{
 			this.setParent(parent);
 			this.setTime(time);
 			this.entity = entity;
 		}
 		
 		public void addNode(Node node)
 		{
 			this.children.add(node);
 		}
 		
 		public List<Node> getChildren()
 		{
 			return this.children;
 		}
 
 		public Node getParent()
 		{
 			return parent;
 		}
 
 		public void setParent(Node parent)
 		{
 			this.parent = parent;
 		}
 
 		public int getTime()
 		{
 			return time;
 		}
 
 		public void setTime(int time)
 		{
 			this.time = time;
 		}
 		
 		public void setLeafNode(boolean isFailed)
 		{
 			this.leafNode = isFailed;
 		}
 		
 		public boolean isLeafNode()
 		{
 			return leafNode;
 		}
 
 		public int getAccumTime()
 		{
 			//return (this.parent == null ? 0 : this.parent.getAccumTime()) + this.time;
 			return this.time;
 		}
 		
 		public boolean isBuildDone(List<Entity> requried)
 		{
 			List<Entity> entitiesToDone = new ArrayList<>(requried);
 			Node node = this;
 			do
 			{
 				entitiesToDone.remove(node.entity);
 			}
 			while ((node = node.parent).entity != null);
 			
 			return entitiesToDone.isEmpty();
 		}
 
 		public Entity getEntity()
 		{
 			return this.entity;
 		}
 
 		public void dump() {
 			if(this.parent!=null)this.parent.dump();
 			System.out.println(time+" : "+ this.entity);
 		}
 	}
 
 	private List<Node> curentLevelNodes = new LinkedList<>();
 	private int minTime = Integer.MAX_VALUE;
 	private Node minNode = null;
 	private int level = 0;
 	private SC2Planner planner;
 	
 	public BuildOptimizer(SC2Planner planner) {
 		this.planner = planner;
 	}
 
 	private void fillBuild(Node node, Deque<Entity> build)
 	{
 		if (node.entity == null)
 		{
 			return;
 		}
 		build.addFirst(node.entity);
 		if (node.parent != null)
 		{
 			this.fillBuild(node.parent, build);
 		}
 	}
 	
 	public void printBuild(Node node)
 	{
 		LinkedList<Entity> build = new LinkedList<>();
 		this.fillBuild(node, build);
 		
 		for (Entity item : build)
 		{
 			System.out.print(item.name);
 			System.out.print("-");
 		}
 	}
 	
 	private void putEntity(Node parent, Entity entity, List<Entity> requried)
 	{
 		int time = this.planner.getCurrentTime();
 		
 		Node node = new Node(parent, entity, time);
 		parent.addNode(node);
 		if (node.isBuildDone(requried) || 
 				node.getAccumTime() > TIME_THRESHOLD)
 		{
 			if (node.isBuildDone(requried))
 			{
 				this.calcMinTime(node);
 			}
 			node.setLeafNode(true);
 		}
 		else
 		{
 			this.curentLevelNodes.add(node);
 		}
 	}
 	
 	private void calcMinTime(Node node)
 	{
 		if (node.getAccumTime() < this.minTime)
 		{
 			this.minTime = node.getAccumTime();
 			this.minNode  = node;
 		}
 	}
 
 	public void buildRaceTree(Race race, List<Entity> requried)
 	{
 		Node root = new Node(null, null, 0);
 		this.curentLevelNodes.add(root);
 		this.buildNewLevel(race, requried);
 	}
 	
 	private void buildNewLevel(Race race, List<Entity> requried)
 	{
 		if (this.curentLevelNodes.size() == 0) return;
 		if (++this.level > LEVEL_THRESHOLD) return;
 		
 		List<Node> pastLevelNodes = new LinkedList<>(this.curentLevelNodes);
 		this.curentLevelNodes.clear();
 		
 		for (Node node : pastLevelNodes)
 		{
 			for (Entity entity : race.entities)
 			{
 				if (entity.section != Section.resource && this.isAllowedToAdd(node, entity))
 				{
 					this.putEntity(node, entity, requried);
 				}
 			}
 		}
 		this.buildNewLevel(race, requried);
 	}
 
 	private boolean isAllowedToAdd(Node node, Entity entity)
 	{
 		this.planner.clearBuilds();
 		LinkedList<Entity> build = new LinkedList<>();
 		this.fillBuild(node, build);
 		
 		for (Entity item : build)
 		{
 			this.planner.insertIntoBuild(item);
 		}
 		
 		this.planner.insertIntoBuild(entity);
 		
		return planner.isSuccessfull();
 	}
 	
 	public Node getMinNode()
 	{
 		return this.minNode;
 	}
 }
