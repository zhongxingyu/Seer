 package sc2build.optimizer;
 import java.util.*;
 import java.util.Map.Entry;
 
 
 /*
  * Created on 11.06.2012
  *
  * Copyright 2003-2012 Eclipse SP LLC. All rights reserved. 
  * */
 
 /**
  * @author Zhdanov Artem
  *
  */
 public class SC2Planner
 {
 	private static final EntityLoader loader = new EntityLoader();
 	
 	public SC2Planner()
 	{
 		SC2Planner.loader.init();
 	}
 	
 	public enum Faction
 	{
 		TERRAN("Terran"),
 		PROTOSS("Protoss"),
 		ZERG("Zerg");
 		
 		public List<Entity> getEnities()
 		{
 			return SC2Planner.loader.load(this.name);
 		}
 		
 		private final String name;
 		private Faction (String name)
 		{
 			this.name = name;
 		}
 		
 		public String getName()
 		{
 			return this.name;
 		}
 	}
 	
 	public static class Race
 	{
 		String name;
 		List<Entity> entities;
 	}
 	
 	public static class Cost
 	{
 		String name;
 		Integer amount;
 		String error;
 	}
 	
 	public static class NeedEntity
 	{
 		String name;
 		String error;
 	}
 	public static class AtMost
 	{
 		public String name;
 		public Integer amount;
 		public String error;
 		public String as;
 	}
 	
 	public static class Entity
 	{
 		String name;
 		Section section;
 		Integer start;
 		String style;
 		int[] value;
 		List<Entity> products = null;
 		List<NeedEntity> need = null;
 		
 		String adding;
 		String addsto;
 		List<String> conditions = null;
 		List<Cost> costs = null;
 		
 		String multi;
 		
 		Integer cap;
 		public boolean autocheck;
 		public Integer time;
 		public Integer amount;
 		public String save;
 		public Integer idle;
 		public AtMost atmost = null;
 		public boolean eventualError;
 		public String currentError;
 		@Override
 		public String toString() {
 			return "Entity [name=" + name + ", section=" + section + ", start="
 					+ start + ", style=" + style + ", value="
 					+ Arrays.toString(value) + ", adding=" + adding + ", addsto="
 					+ addsto +   ", multi=" + multi + ", cap=" + cap
 					+ ", autocheck=" + autocheck + ", time=" + time
 					+ ", amount=" + amount + ", save=" + save + ", idle="
 					+ idle + ", atmost=" + atmost + ", eventualError="
 					+ eventualError + ", currentError=" + currentError + "]";
 		}
 		
 		
 	}
 	
 	class Event
 	{
 		protected int time;
 		public String event;
 		public String name;
 		public int actInd;
 		public boolean active;
 		@Override
 		public String toString() {
 			return "Event [time=" + time + ", event=" + event + ", name="
 					+ name + ", actInd=" + actInd + ", active=" + active + "]";
 		}
 		
 		
 	}
 	class Category
 	{
 		public int[] value = new int[50];
 	}
 	
 	LinkedList<Event> events;
 	int currentTime;
 	private String factionName;
 	private Map<String, Entity> entities = new HashMap<String, Entity>();
 	private Map<String, Entity> entitiesByKey = new HashMap<String, Entity>();
 	private List<Entity> build;
 	private List<Integer> delays;
 	private List<String> food;
 	private List<String> eventualError;
 	private int currentPosition;
 	private int stopAtTime;
 	private int chronoboost;
	private int activeEvents;
 	private ArrayList<String> chronoTarget;
 	private ArrayList<Integer> chronoAmount;
 	private ArrayList<Integer> chronoFinished;
 	private Map<Section, Category> category = new HashMap<Section, Category>();
 	private boolean isDelayed;
 	
 	int sum(int[] d)
 	{
 		int b = d[0];
 		for ( int c = 1; c < d.length; c = c + 1)
 		{
 			b = b + d[c];
 		}
 		return b;
 	}
 	int max(int[] d)
 	{
 		int b = d[0];
 		for ( int c = 1; c < d.length; c = c + 1)
 		{
 			b = Math.max(b, d[c]);
 		}
 		return b;
 	}
 	
 	int maxIndexOf(int[] b)
 	{
 		int c = 0;
 		if (b.length == 0)
 		{
 			return -1;
 		}
 		for ( int a = 1; a < b.length; a = a + 1)
 		{
 			if (b[a] > b[c])
 			{
 				c = a;
 			}
 		}
 		return c;
 	}
 	
 	int minIndexOf(int[] b)
 	{
 		int c = 0;
 		if (b.length == 0)
 		{
 			return -1;
 		}
 		for ( int a = 1; a < b.length; a++)
 		{
 			if (b[a] < b[c])
 			{
 				c = a;
 			}
 		}
 		return c;
 	}
 	
 	String addZeros(String b, int a)
 	{
 		StringBuilder c = new StringBuilder("" + b);
 		while (c.length() < a)
 		{
 			c.insert(0, "0");
 		}
 		return c.toString();
 	}
 	
 	String fixString(String a)
 	{
 		return a.replace("[ :]", "");
 	}
 	
 	void reset()
 	{
 		this.init(Faction.valueOf(this.factionName));
 	}
 	
 	void init (Faction faction)
 	{
 		if (faction == null)
 		{
 			throw new IllegalStateException("Error: no faction: ");
 		}
 		this.factionName = faction.name();
 		//this.entities = new ArrayList<Integer>();
 		//this.entitiesByKey = new ArrayList<Integer>();
 		this.build = new ArrayList<Entity>();
 		this.delays = new ArrayList<Integer>();
 		this.food = new ArrayList<String>();
 		this.eventualError = new ArrayList<String>();
 		this.currentPosition = -1;
 		List<Entity> entities = faction.getEnities();
 		for (Entity entity : entities)
 		{
 			this.entities.put(entity.name, entity);
 			if (entity.save != null)
 			{
 				this.entitiesByKey.put(entity.save, entity);
 			}
 			this.initEntity(entity);
 		}
 		this.stopAtTime = -1;
 		//this.readBuild();
 		this.updateCenter(false, false, 0, false);
 		//this.updateBuild(false);
 	}
 	
 	public String getFactionName() {
 		return factionName;
 	}
 	void reset(Entity a)
 	{
 		a.value = new int[] {a.start};
 		a.idle = a.start;
 	}
 	
 	void initEntity (Entity a)
 	{
 		if (a.start == null)
 		{
 			a.start = 0;
 		}
 		//if (a.autocheck == null)
 		//{
 		//	a.autocheck = false;
 		//}
 		if (a.time==null)
 		{
 			a.time = 0;
 		}
 		if (a.products != null)
 		{
 			for ( Entity b : a.products)
 			{
 				if (b.name == null)
 				{
 					b.name = a.name;
 				}
 				if (b.amount==null)
 				{
 					b.amount = 1;
 				}
 			}
 		}
 		else
 		{
 			Entity ent = new Entity();
 			ent.name = a.name;
 			ent.amount = 1;
 			a.products = new ArrayList<Entity>();
 			a.products.add(ent);
 		}
 		this.reset(a);
 	}
 	
 	public String exportBuild()
 	{
 		String str = "";
 		for (int b = 0; b < this.build.size(); b++)
 		{
 			Entity a = this.build.get(b);
 			if (a.style != "nonumber" && this.food.get(b) != null && this.food.get(b) != "")
 			{
 				str += this.food.get(b) + " : " + this.build.get(b).name + "\n";
 			}
 		}
 		return str;
 	}
 	
 	public void insertIntoBuild (Entity b, int a)
 	{
 		this.stopAtTime = -1;
 		
 		if (b.section == Section.pause)
 		{
 			if (this.currentPosition >= 0
 					&& this.build.get(this.currentPosition).section == Section.pause)
 			{
 				this.delays.set(this.currentPosition, this.delays.get(this.currentPosition) + a);
 			} 
 			else
 			{
 				if (this.currentPosition < (this.build.size() - 1)
 						&& this.build.get(this.currentPosition + 1).section == Section.pause)
 				{
 					this.delays.set(this.currentPosition, this.delays.get(this.currentPosition + 1) + a);
 				}
 				else
 				{
 					this.currentPosition += 1;
 					this.build.add(this.currentPosition, b);
 					this.delays.add(this.currentPosition, a);
 				}
 			}
 		} 
 		else
 		{
 			if (this.currentPosition >= 0
 					&& this.build.get(this.currentPosition).adding != null
 					&& this.build.get(this.currentPosition).adding == b.name)
 			{
 				this.build.set(this.currentPosition, this.entities.get(this.build.get(this.currentPosition).addsto));
 				this.delays.set(this.currentPosition, 0);
 			} 
 			else
 			{
 				this.currentPosition += 1;
 				this.build.add(this.currentPosition, b);
 				this.delays.add(this.currentPosition, 0);
 			}
 		}
 		
 		this.updateCenter(true, false, this.currentPosition, false);
 		//this.updateBuild(true)
 	}
 	
 	public void setPosition (int a)
 	{
 		this.currentPosition = a;
 		this.stopAtTime = -1;
 		this.updateCenter(false, false, 0, true);
 	}
 	
 	
 	String errorDoing (Entity entity, int index, boolean c)
 	{
 		if (entity.style.equals("single") && entity.value[index] > 0)
 		{
 			return "Already researched / built.";
 		}
 		if (entity.conditions != null)
 		{
 			for (String condition : entity.conditions)
 			{
 				if (max(this.entities.get(condition).value) <= 0)
 				{
 					return condition + " needed.";
 				}
 			}
 		}
 		if (entity.need != null && entity.need.size() > 0)
 		{
 			for (NeedEntity need : entity.need)
 			{
 				if (this.entities.get(need.name).idle <= 0)
 				{
 					return need.error;
 				}
 			}
 		}
 		boolean d = (entity.name.equals("Slow Mineral Patch"));
 		boolean b = (entity.name.equals("Slow Gold Mineral Patch"));
 		boolean a = (entity.name.equals("Fast Mineral Patch"));
 		boolean l = (this.entities.get("Slow Gold Mineral Patch").idle > 0);
 		boolean k = (this.entities.get("Fast Mineral Patch").idle > 0);
 		boolean i = (this.entities.get("Fast Gold Mineral Patch").idle > 0);
 		if ((d && (l || k || i)) || (b && (k || i)) || (a && i))
 		{
 			return "Faster patch available.";
 		}
 		if (entity.name.equals("Slow Gas Patch")
 				&& this.entities.get("Fast Gas Patch").idle > 0)
 		{
 			return "Faster patch available.";
 		}
 		int f = 0;
 		if (entity.costs!=null && entity.costs.size() > 0)
 		{
 			for (Cost cost : entity.costs)
 			{
 				if (c)
 				{
 					if (cost.name.equals("Minerals")
 							&& this.entities.get("Mineral Drone")!=null
 							&& this.entities.get("Mineral Drone").value[0] > 0)
 					{
 						continue;
 					}
 					if (cost.name.equals("Minerals")
 							&& this.entities.get("Mineral Probe")!=null
 							&& this.entities.get("Mineral Probe").value[0] > 0)
 					{
 						continue;
 					}
 					if (cost.name.equals("Minerals")
 							&& this.entities.get("Mineral SCV")!=null
 							&& this.entities.get("Mineral SCV").value[0] > 0)
 					{
 						continue;
 					}
 					if (cost.name.equals("Gas") && this.entities.get("Gas Drone")!=null
 							&& this.entities.get("Gas Drone").value[0] > 0)
 					{
 						continue;
 					}
 					if (cost.name.equals("Gas") && this.entities.get("Gas Probe")!=null
 							&& this.entities.get("Gas Probe").value[0] > 0)
 					{
 						continue;
 					}
 					if (cost.name.equals("Gas") && this.entities.get("Gas SCV")!=null
 							&& this.entities.get("Gas SCV").value[0] > 0)
 					{
 						continue;
 					}
 					if (cost.name.equals("Larva"))
 					{
 						continue;
 					}
 					if (cost.name.equals("Energy")
 							&& this.entities.get("Energy Spawner").value[0] > 0)
 					{
 						continue;
 					}
 				}
 				if (cost.name.equals("Energy") && entity.name.equals("Chronoboost")
 						&& this.chronoboost > 0)
 				{
 					if (max(this.entities.get("Energy").value) < cost.amount
 							- this.chronoboost * 11.25 / 1000)
 					{
 						return cost.error;
 					}
 				} else
 				{
 					if (max(this.entities.get(cost.name).value) < cost.amount)
 					{
 						return cost.error;
 					}
 				}
 				if (entity.atmost != null && cost.name == entity.atmost.name)
 				{
 					f = cost.amount;
 				}
 			}
 		}
 		if (entity.atmost != null)
 		{
 			Entity v = this.entities.get(entity.atmost.name);
 			if (entity.atmost.amount!=null && entity.atmost.amount > 0)
 			{
 				if (v.value.length>index && (v.value[index] - f > entity.atmost.amount))
 				{
 					return entity.atmost.error;
 				}
 			}
 			if (entity.atmost.as != null && entity.atmost.as!="")
 			{
 				if (v.value.length>index && (v.value[index] - f > max(this.entities.get(entity.atmost.as).value)))
 				{
 					return entity.atmost.error;
 				}
 			}
 		}
 		return null;
 	}
 	
 	void autoCheck (Entity b, int index)
 	{
 		String error = this.errorDoing(b, index, false);
 		if (error != null && error.length() > 0)
 		{
 			Event event = new Event();
 			event.event = "check";
 			
 			event.name = b.name;
 			event.actInd = index;
 			event.active = false;
 			
 			if (b.name == "Larva Spawner")
 			{
 				event.time = (this.currentTime + 100);
 			}
 			else
 			{
 				event.time = this.currentTime + Integer.valueOf(3 * b.time / 4);
 			}
 			this.events.add(event);
 			Collections.sort(this.events, new Comparator<Event>()
 			{
 				public int compare(Event o1, Event o2)
 				{
 					return o1.time - o2.time;
 				}
 			});
 		}
 		else
 		{
 			this.startDoing(b, b.time, index, false);
 		}
 	}
 	
 	void startDoing (Entity d, int addedTime, int actInd, boolean isActive)
 	{
 		if (d.costs != null)
 		{
 			for (Cost f : d.costs)
 			{
 				Entity l = this.entities.get(f.name);
 				int a = f.amount;
 				int c = maxIndexOf(l.value);
 				l.value[c] -= a;
 				l.idle -= a;
 				if (a < 0 && l.autocheck)
 				{
 					int g = 0;
 					if (l.multi.equals(d.name))
 					{
 						g = actInd;
 						if (l.value.length<=g)
 						{
 							int [] n = new int[g+1];
 							n[g] = 0;
 							System.arraycopy(l.value, 0, n, 0, l.value.length);
 							l.value = n;
 						}
 					}
 					else
 					{
 						g = 0;
 					}
 					for (int k = 0; k < -a; k++)
 					{
 						this.autoCheck(l, l.value[g] - (-a) + k);
 					}
 				}
 			}
 		}
 		if (d.need != null)
 		{
 			for (NeedEntity f : d.need)
 			{
 				this.entities.get(f.name).idle -= 1;
 			}
 		}
 		if (d.name == "Chronoboost")
 		{
 			this.chronoboost += 1000;
 		}
 		if (isActive)
 		{
 			this.activeEvents += 1;
 		}
 		Event event = new Event();
 		event.event = "execute";
 		event.time = this.currentTime + addedTime;
 		event.name = d.name;
 		event.actInd = actInd;
 		event.active = isActive;
 		this.events.add(event);
 		Collections.sort(this.events, new Comparator<Event>()
 		{
 			public int compare(Event o1, Event o2)
 			{
 				return o1.time - o2.time;
 			}
 		});
 	}
 	
 	void finishDoing (Entity g, int index, boolean resetActiveEvents)
 	{
 		boolean autocheckNeedError = false;
 		if (g.need != null && g.autocheck)
 		{
 			for (NeedEntity a : g.need)
 			{
 				if (this.entities.get(a.name).idle < 0)
 				{
 					autocheckNeedError = true;
 				}
 			}
 		}
 		if (g.products != null && !autocheckNeedError)
 		{
 			for (Entity a : g.products)
 			{
 				Entity f = this.entities.get(a.name);
 				int amount = a.amount;
 				int useIndex = 0;
 				if (f.multi!=null && f.multi.equals(g.name))
 				{
 					useIndex = index;
 					/*if (f.value[useIndex] != 0)
 					{
 						f.value[useIndex] = 0;
 					}*/
 					if (f.value.length <= useIndex)
 					{
 						int[] n = new int[useIndex];
 						System.arraycopy(f.value, 0, n, 0, f.value.length);
 						f.value = n;
  						f.value[useIndex] = 0;
 					}
 				}
 				else
 				{
 					useIndex = minIndexOf(f.value);
 				}
 				if (f.cap != null && f.cap>0 && f.value[useIndex] + amount > f.cap)
 				{
 					amount = f.cap - f.value[useIndex];
 				}
 				f.value[useIndex] += amount;
 				f.idle += amount;
 				if (f.autocheck)
 				{
 					for (int b = 0; b < amount; b++)
 					{
 						this.autoCheck(f, f.value[useIndex] - amount + b);
 					}
 				}
 			}
 		}
 		if (g.need != null)
 		{
 			for (NeedEntity a : g.need)
 			{
 				this.entities.get(a.name).idle += 1;
 			}
 		}
 		if (resetActiveEvents)
 		{
 			this.activeEvents -= 1;
 		}
 		if (g.autocheck)
 		{
 			this.autoCheck(g, index);
 		}
 	}
 	void updateCenter (boolean initError, boolean notInit, int position, boolean j)
 	{
 		if (!notInit)
 		{
 			this.updateCenter(initError, true, position, j);
 		}
 		if (initError)
 		{
 			for (int k = position; k < this.build.size(); k++)
 			{
 				this.eventualError.add(k, null);
 			}
 		}
 		this.currentTime = 100;
 		this.chronoboost = 0;
 		this.chronoTarget = new ArrayList<String>(500);
 		this.chronoAmount = new ArrayList<Integer>(500);
 		this.chronoFinished = new ArrayList<Integer>(500);
 		this.activeEvents = 0;
 		//int r = 20000;
 		
 		if (!notInit)
 		{
 			this.category = new HashMap<Section, SC2Planner.Category>();
 			this.category.put(Section.pause, new Category());
 			this.category.put(Section.worker, new Category());
 			this.category.put(Section.special, new Category());
 			this.category.put(Section.building, new Category());
 			this.category.put(Section.upgrade, new Category());
 			this.category.put(Section.unit, new Category());
 		}
 		this.events = new LinkedList<Event>();
 		for (Entity action : this.entities.values())
 		{
 			this.reset(action);
 		}
 		for (Entity action : this.entities.values())
 		{			
 			if (action.autocheck && !action.name.equals("Fast Mineral Patch")
 					&& !action.name.equals("Slow Mineral Patch"))
 			{
 				for (int q = 0; q < action.value[0]; q++)
 				{
 					this.autoCheck(action, 0);
 				}
 			}
 		}
 		for (int k = 0; k < 16; k++)
 		{
 			Event event = new Event();
 			event.event = "check";
 			event.time = Integer.valueOf(10 + ((k + 1) / 2) * 100);
 			event.name = "Fast Mineral Patch";
 			event.actInd = 0;
 			this.events.add(event);
 		}
 		for (int k = 0; k < 8; k++)
 		{
 			Event event = new Event();
 			event.event = "check";
 			event.time = Integer.valueOf(1002 + k * 100);
 			event.name = "Slow Mineral Patch";
 			event.actInd = 0;
 			this.events.add(event);
 		}
 		Collections.sort(this.events, new Comparator<Event>()
 		{
 			public int compare(Event o1, Event o2)
 			{
 				return o1.time - o2.time;
 			}
 		});
 		assertEvents();
 		//dumpState(this);
 		for (int k = 0; k < this.build.size(); k++)
 		{
 			if (notInit
 					&& ((this.stopAtTime != -1 && this.currentTime > this.stopAtTime) || (this.stopAtTime == -1 && k > this.currentPosition)))
 			{
 				break;
 			}
 			Entity action = this.build.get(k);
 			String proceedMessage = null;
 			this.isDelayed = false;
 			int actionTime = 0;
 			if (!(this.eventualError.size()>k && this.eventualError.get(k) != null))
 			{
 				String evError = null;
 				do
 				{
 					if (proceedMessage != null)
 					{
 						Event e = this.events.pop();
 						//System.out.println(action+" waits for "+e);
 						if (this.stopAtTime != -1)
 						{
 							if (this.currentTime <= this.stopAtTime
 									&& e.time > this.stopAtTime)
 							{
 								this.currentTime = this.stopAtTime;
 								//this.updateClock();
 								//this.updateAmounts();
 								if (this.currentPosition == -2)
 								{
 									this.currentPosition = k - 1;
 									//this.updateBuild(true);
 								}
 							}
 						}
 						this.currentTime = e.time;
 						if (e.event.equals("execute"))
 						{
 							this.finishDoing(this.entities.get(e.name), e.actInd,
 									e.active);
 						}
 						if (e.event.equals("check"))
 						{
 							this.autoCheck(this.entities.get(e.name), e.actInd);
 						}
 						if (e.event.equals("start"))
 						{
 							proceedMessage = null;
 							break;
 						}
 					}
 					proceedMessage = this.errorDoing(action, maxIndexOf(action.value), false);
 					evError = this.errorDoing(action,maxIndexOf(action.value), true);
 					
 					if ((proceedMessage == null || proceedMessage.length() == 0) && k > 0 && this.delays.get(k - 1) > 0)
 					{
 						if (!this.isDelayed)
 						{
 							Event event = new Event();
 							event.event = "start";
 							event.time = this.currentTime + this.delays.get(k - 1) * 100;
 							event.name = action.name;
 							this.events.add(event);
 							
 							Collections.sort(this.events, new Comparator<Event>()
 							{
 								public int compare(Event o1, Event o2)
 								{
 									return o1.time - o2.time;
 								}
 							});
 							this.isDelayed = true;
 						}
 						proceedMessage = "Delaying.";
 					}
 				} while (proceedMessage != null && proceedMessage.length() > 0 && (this.activeEvents > 0 || evError == null));
 				actionTime = action.time;
 				assertEvents();
 				int chronoTargetSize = 0;
 				if (this.chronoboost > 0 && action.name.equals("Chronoboost")
 						&& action.need != null && action.need.size() > 0)
 				{
 					this.chronoAmount.set(chronoTargetSize, this.chronoboost);
 					this.chronoTarget.add(action.need.get(0).name);
 					chronoTargetSize++;
 					this.chronoboost = 0;
 				}
 				for (int m = 0; m < this.chronoTarget.size(); m++)
 				{
 					if (chronoTarget.get(m) == null) continue;
 					
 					if (action.need != null && action.need.size() > 0
 							&& action.need.get(0).name.equals(chronoTarget.get(m)))
 					{
 						if (this.chronoFinished.size() > m && this.chronoFinished.get(m) != null
 								&& this.currentTime < this.chronoFinished.get(m))
 						{
 							continue;
 						}
 						if (this.chronoFinished.size() > m && this.chronoFinished.get(m) != null)
 						{
 							this.chronoAmount.set(m, Math.max(
 											this.chronoAmount.get(m)
 													- (this.currentTime - this.chronoFinished.get(m))
 													/ 2, 0));
 						}
 						if (actionTime < this.chronoAmount.get(m) * 3)
 						{
 							int o = Integer.valueOf(actionTime * 1 / 3);
 							actionTime = actionTime - o;
 							this.chronoAmount.set(m, this.chronoAmount.get(m) - o);
 							this.chronoFinished.set(m, this.currentTime + actionTime);
 						} else
 						{
 							actionTime = actionTime - this.chronoAmount.get(m);
 							this.chronoTarget.set(m, null);
 							this.chronoAmount.set(m, null);
 							if (this.chronoFinished.get(m) != null)
 							{
 								this.chronoFinished.set(m, null);
 							}
 						}
 						break;
 					}
 				}
 				if (!(notInit && ((this.stopAtTime != -1 && this.currentTime > this.stopAtTime) || (this.stopAtTime == -1 && k > this.currentPosition))))
 				{
 					if (proceedMessage == null)
 					{
 						if (!notInit)
 						{
 							String f = String.valueOf(this.entities.get("Food").value[0]);
 							if(k==this.food.size()) 
 								this.food.add(f); 
 							else
 								this.food.set(k, f);
 						}
 						this.startDoing(action, actionTime, action.value[0], true);
 					}
 					else
 					{
 						this.eventualError.set(k,  proceedMessage);
 						this.updateCenter(false, false, position, j);
 						if(this.food.size()<=k)this.food.add(k, ""); else
 						this.food.set(k, "");
 						return;
 					}
 				}
 			}
 			else
 			{
 				actionTime = action.time;
 				proceedMessage = this.eventualError.size()<=k ? null : this.eventualError.get(k);
 			}
 			
 			final boolean b = notInit;
 
 			if (!b && !j && !action.name.equals("Chronoboost"))
 			{
 				//$("#action_" + k).remove();
 				//actionDiv = $("<div></div>");
 				//actionDiv.attr("id", "action_" + k);
 				//actionDiv.css("left", parseInt((this.currentTime * 0.05))
 				//		+ "px");
 				if (action.style.equals("instant") || action.style.equals("action"))
 				{
 					//actionDiv.css("width", "150px");
 					//actionDiv.addClass("action_event")
 				} else
 				{
 					/*if (action.section == "pause")
 					{
 						actionDiv.css("width",
 								(parseInt(this.delays[k] * 100 * 0.05) - 1)
 										+ "px");
 						actionDiv.addClass("pause")
 					} else
 					{
 						actionDiv.css("width",
 								(parseInt(actionTime * 0.05) - 1) + "px");
 						actionDiv.addClass("action")
 					}*/
 				}
 				/*if (action.section != Section.pause)
 				{
 					actionDiv.append($("<img></img>").attr("id",
 							"actionImage_" + k).attr("src",
 							"images/" + action.icon + ".png"));
 					actionText = $("<p></p>").attr("id", "actionText_" + k);
 					if (action.style != "notext"
 							&& (actionTime > 1200 || (action.style == "instant" || action.style == "action")))
 					{
 						actionText.html(action.name)
 					} else
 					{
 						actionText.html("")
 					}
 					actionDiv.append(actionText)
 				}*/
 
 					{
 					}
 
 				int h = 0;
 				//var d = null;
 				if (k > 0 && delays.get(k-1) > 0)
 				{
 					h = delays.get(k-1);
 					//d = $("#action_" + (k - 1));
 					//pauseHighlight = $("#actionHighlight_" + (k - 1));
 					//pauseHighlight.attr("startTime", this.currentTime - 100
 					//		* this.delays[k - 1]);
 					//pauseHighlight.attr("endTime", this.currentTime)
 				}
 				this.insertAction(action, h, actionTime);	
 				/*highlightDiv = $("<div></div>").addClass("highlight").attr(
 						"id", "actionHighlight_" + k).attr("positionId", k)
 						.bind(
 								"click",
 								function(c)
 								{
 									sc.removeOrder(c, $(this)
 											.attr("positionId"), true, false)
 								}).bind("mouseover", function(c)
 						{
 							sc.highlight($(this).attr("positionId"), 0)
 						}).bind("mouseout", function(c)
 						{
 							sc.lowlight($(this).attr("positionId"))
 						}).css("width",
 								(parseInt(actionDiv.css("width")) + 2) + "px")
 						.css("top", "-1px").css("left", "-1px").css("height",
 								"26px").attr("startTime", this.currentTime)
 						.attr("endTime", this.currentTime + actionTime);
 				if (action.section == "pause")
 				{
 					highlightDiv.attr("endTime", this.currentTime
 							+ this.delays[k] * 100)
 				}
 				actionDiv.append(highlightDiv);*/
 				/*if (n)
 				{
 					highlightDiv.addClass("disabled");
 					highlightDiv.removeClass("highlight");
 					highlightDiv.attr("error", n)
 				} else
 				{
 					highlightDiv.addClass("highlight");
 					highlightDiv.removeClass("disabled");
 					highlightDiv.attr("error", "")
 				}*/
 			}
 			//r = Math.max(r, this.currentTime + action.time, this.stopAtTime);
 			if (!b && !j && action.name.equals("Chronoboost"))
 			{
 				//$("#action_" + k).remove();
 				//$("#chrono_action_" + k).remove()
 			}
 			if (!b && this.stopAtTime == -1 && k == this.currentPosition)
 			{
 				this.updateClock();
 				this.updateAmounts();;
 			}
 		}
 		/*if (!b && !j)
 		{
 			k = this.build.length;
 			while (true)
 			{
 				actionDiv = $("#action_" + k);
 				if (!actionDiv.length)
 				{
 					break
 				}
 				$("#chrono_" + actionDiv.attr("id")).remove();
 				actionDiv.remove();
 				k++
 			}
 			totalHeight = 29;
 			for ( var q in this.category)
 			{
 				k = this.category[q].length;
 				while (true)
 				{
 					sectionDiv = $("#" + q + "_" + k);
 					if (!sectionDiv.length)
 					{
 						break
 					}
 					sectionDiv.remove();
 					k++
 				}
 				category = $("#" + q);
 				if (category.length)
 				{
 					totalHeight += category.height()
 				}
 			}
 			totalHeight = Math.max(totalHeight, 680);
 			$("#timeLine").css("height", totalHeight + "px")
 		}*/
 		/*if (!b)
 		{
 			r = Math.max(r, this.currentTime, this.stopAtTime);
 			//$(".section").css("width", (parseInt(r * 0.05) + 300) + "px");
 			timeSteps = Math.max((r / 2000) + 2, 7);
 			for ( var k = 0; k < timeSteps; k++)
 			{
 				if (!$("#time_" + k).length)
 				{
 					var p = addZeros(((k + 1) % 3) * 20, 2);
 					var f = addZeros(parseInt((k + 1) / 3), 2);
 					$("#time").append(
 							$("<div></div>").css("left", (k + 1) * 100 + "px")
 									.addClass("time").attr("id", "time_" + k)
 									.append(
 											$("<span></span>")
 													.html(f + ":" + p)
 													.addClass("time_text")))
 				}
 			}
 		}*/
 		//dumpState(this);
 		if (!notInit)
 		{
 			assertEvents();
 			while (this.stopAtTime != -1 && this.currentTime <= this.stopAtTime
 					&& this.events.size() > 0)
 			{
 				if (this.currentPosition == -2)
 				{
 					this.currentPosition = this.build.size() - 1;
 					//this.updateBuild(true);
 				}
 				Event e = this.events.pop();
 				if (e.time > this.stopAtTime)
 				{
 					this.currentTime = this.stopAtTime;
 					this.updateClock();
 					this.updateAmounts();
 					break;
 				}
 				this.currentTime = e.time;
 				if (e.event.equals("execute"))
 				{
 					this.finishDoing(this.entities.get(e.name), e.actInd, e.active);
 				}
 				if (e.event.equals("check"))
 				{
 					this.autoCheck(this.entities.get(e.name), e.actInd);
 				}
 			}
 		}
 		else
 		{
 			assertEvents();
 			while (this.activeEvents > 0)
 			{
 				Event e = this.events.pop();
 				this.currentTime = e.time;
 				if (e.event.equals("execute"))
 				{
 					this.finishDoing(this.entities.get(e.name), e.actInd, e.active);
 				}
 				if (e.event.equals("check"))
 				{
 					this.autoCheck(this.entities.get(e.name), e.actInd);
 				}
 			}
 			assertEvents();
 			for (Entity g : this.entities.values())
 			{
 				String n = this.errorDoing(g, maxIndexOf(g.value), true);
 				if (n != null)
 				{
 					g.eventualError = true;
 					g.currentError = n;
 				} else
 				{
 					g.eventualError = false;
 					g.currentError = "";
 				}
 			}
 			assertEvents();
 		}
 		//dumpState(this);
 	}
 	/*this.highlight = function(c, b)
 	{
 		ah = $("#actionHighlight_" + c);
 		oh = $("#orderHighlight_" + c);
 		if (ah.length)
 		{
 			if (ah.is(".disabled"))
 			{
 				ah.css("opacity", "0.8")
 			} else
 			{
 				ah.css("opacity", "0.4")
 			}
 		}
 		if (b == 0)
 		{
 			var a = $("#center_window");
 			this.showInfo(this.build[c], ah.attr("error"), Math.max(ah.parent()
 					.position().left
 					- a.scrollLeft(), 0), ah.parent().parent().position().top
 					- a.scrollTop() + 230, ah.attr("startTime"), ah
 					.attr("endTime"))
 		} else
 		{
 			var d = $("#build_window");
 			if (ah)
 			{
 				this.showInfo(this.build[c], ah.attr("error"), 120 + Math.max(
 						oh.parent().position().left - d.scrollLeft(), 0), 170,
 						ah.attr("startTime"), ah.attr("endTime"))
 			} else
 			{
 				this.showInfo(this.build[c], "", 120 + Math.max(
 						oh.parent().position.left() - d.scrollLeft(), 0), 170,
 						null, null)
 			}
 		}
 		if (oh.length)
 		{
 			if (oh.is(".disabled_order"))
 			{
 				oh.css("opacity", "0.8")
 			} else
 			{
 				oh.css("opacity", "0.2")
 			}
 		}
 	};
 	this.lowlight = function(a)
 	{
 		ah = $("#actionHighlight_" + a);
 		if (ah.length)
 		{
 			if (ah.is(".disabled"))
 			{
 				ah.css("opacity", "0.7")
 			} else
 			{
 				ah.css("opacity", "0")
 			}
 		}
 		oh = $("#orderHighlight_" + a);
 		if (oh.length)
 		{
 			if (oh.is(".disabled_order"))
 			{
 				oh.css("opacity", "0.7")
 			} else
 			{
 				oh.css("opacity", "0")
 			}
 		}
 		this.hideInfo()
 	};*/
 	void updateClock()
 	{
 		/*var b = addZeros((parseInt(this.currentTime / 100) % 60), 2);
 		var a = addZeros(parseInt((this.currentTime / 100) / 60), 2);
 		$("#clock").html(a + ":" + b);
 		$("#timeLine").css("left", parseInt(this.currentTime * 0.05) + "px");
 		centerDiv = $("#center_window");
 		minimum = this.currentTime * 0.05 - 590;
 		maximum = this.currentTime * 0.05 - 100;
 		if (centerDiv.scrollLeft() < minimum)
 		{
 			centerDiv.scrollLeft(minimum)
 		}
 		if (centerDiv.scrollLeft() > maximum)
 		{
 			centerDiv.scrollLeft(maximum)
 		}*/
 	}
 	void updateAmounts()
 	{
 		/*for ( var c in this.entities)
 		{
 			var a = this.entities[c];
 			if (a.kind != "hidden")
 			{
 				amountParagraph = $("#entity_amount_" + fixString(c));
 				if (a.style == "supply")
 				{
 					amountParagraph.html(this.entities.Food.value[0] + " / "
 							+ a.value[0])
 				} else
 				{
 					if (a.style == "action" || a.style == "pause"
 							|| (sum(a.value) == 0 && a.kind != "resource"))
 					{
 						amountParagraph.html("");
 						amountParagraph.css("visibility", "hidden")
 					} else
 					{
 						amountParagraph.css("visibility", "visible");
 						if (a.style == "single")
 						{
 							amountParagraph
 									.html('<img src="images/icons/check.png" alt="" width="14px"/>')
 						} else
 						{
 							amountParagraph.html(a.value[0]);
 							for ( var b = 1; b < a.value.length; b++)
 							{
 								amountParagraph.html(amountParagraph.html()
 										+ " / " + a.value[b])
 							}
 						}
 					}
 				}
 				if (a.kind != "resource")
 				{
 					overlayButton = $("#entity_" + fixString(c));
 					error = this.errorDoing(a, maxIndexOf(a.value), false);
 					if (error && a.eventualError
 							&& !(a.style == "single" && a.value[0] == 1))
 					{
 						overlayButton.addClass("disabled");
 						overlayButton.removeClass("highlight");
 						overlayButton.css("opacity", 0.7);
 						overlayButton.unbind("mouseenter mouseleave")
 					} else
 					{
 						overlayButton.addClass("highlight");
 						overlayButton.removeClass("disabled");
 						overlayButton.css("opacity", 0);
 						overlayButton.hover(function()
 						{
 							$(this).css("opacity", 0.2)
 						}, function()
 						{
 							$(this).css("opacity", 0)
 						});
 						if (error)
 						{
 							a.currentError = error
 						} else
 						{
 							a.currentError = ""
 						}
 					}
 				}
 			}
 		}*/
 	};
 	void insertAction (Entity g, int m, int d)
 	{
 		int k = this.currentTime + d;
 		if (g.style.equals("action") || g.style.equals("instant"))
 		{
 			k = this.currentTime + Math.max(d, 2500);
 		}
 		Category cat = this.category.get(g.section);
 		int a = cat.value.length;
 		int j = this.currentTime - m * 100;
 		
 		for (int h = 0; h < cat.value.length; h++)
 		{
 			if (j >= cat.value[h])
 			{
 				cat.value[h] = k;
 				a = h;
 				break;
 			}
 		}
 		if (a == cat.value.length)
 		{
 			int[] newArr = new int[cat.value.length + 1];
 			System.arraycopy(cat.value, 0, newArr, 0, cat.value.length);
 			cat.value[cat.value.length - 1] = k;
 		}
 		/*String sectionId = g.section + "_" + a;
 		sectionDiv = $("#" + sectionId);
 		if (!sectionDiv.length)
 		{
 			sectionDiv = $("<div></div>").addClass("section").attr("id",
 					sectionId);
 			$("#" + g.section).append(sectionDiv)
 		}
 		if (m > 0)
 		{
 			sectionDiv.append(b)
 		}
 		sectionDiv.append(c);
 		$("#chrono_" + c.attr("id")).remove();*/
 		if (g.time != d)
 		{
 			int f = k + g.time - d;
 			cat.value[a] = f;
 			/*sectionDiv.append($("<div></div>").css("left",
 					parseInt((k * 0.05) - 1) + "px").css("zIndex", 0).css(
 					"width", parseInt(((g.time - d) * 0.05)) + "px").addClass(
 					"action").attr("id", "chrono_" + c.attr("id")).css(
 					"opacity", "0.5").css("height", "24px").css("border",
 					"1px solid #004").css("backgroundImage",
 					"url(images/chronolines.png)"))*/
 		}
 	}
 	/*this.startUp()
 	/*}
 	$(document).ready(function()
 	{
 		sc = new StarcraftBuilder();
 		$(window).bind("hashchange", function()
 		{
 			sc = new StarcraftBuilder();
 			sc.startUp()
 		})
 	});*/
 			
 	public static void main(String args[])
 	{
 		//if (args.length != 1) throw new IllegalArgumentException("Zerg, Terran or Protoss should be defined");
 		
 		SC2Planner sc = new SC2Planner();
 		//sc.init(Faction.valueOf(args[0]));
 		sc.init(Faction.PROTOSS);
 		//sc.stopAtTime = 1000000;
 		sc.insertIntoBuild(sc.entities.get("Assimilator"));
 		sc.insertIntoBuild(sc.entities.get("Gas Probe"));
 		sc.insertIntoBuild(sc.entities.get("Gas Probe"));
 		sc.insertIntoBuild(sc.entities.get("Gas Probe"));
 		sc.insertIntoBuild(sc.entities.get("Probe"));
 		sc.insertIntoBuild(sc.entities.get("Probe"));
 		sc.insertIntoBuild(sc.entities.get("Probe"));
 		sc.insertIntoBuild(sc.entities.get("Pylon"));
 		sc.insertIntoBuild(sc.entities.get("Probe"));
 		sc.insertIntoBuild(sc.entities.get("Probe"));
 		sc.insertIntoBuild(sc.entities.get("Probe"));
 		sc.insertIntoBuild(sc.entities.get("Gateway"));
 		sc.insertIntoBuild(sc.entities.get("Zealot"));
 		sc.insertIntoBuild(sc.entities.get("Zealot"));
 		
 		sc.insertIntoBuild(sc.entities.get("Pylon"));
 		sc.insertIntoBuild(sc.entities.get("Cybernetics Core"));
 		sc.insertIntoBuild(sc.entities.get("Stalker"));
 		sc.insertIntoBuild(sc.entities.get("Stalker"));
 		sc.insertIntoBuild(sc.entities.get("Zealot"));
 		dumpState(sc);
 	}
 	private static void dumpState(SC2Planner sc) {
 		sc.assertEvents();
 		System.out.println("delays");
 		for(int i : sc.delays){
 			System.out.println(i);
 		}
 		System.out.println("food");
 		for(String i : sc.food){
 			System.out.println(i);
 		}
 		System.out.println("Events");
 		//sc.updateCenter(true, false, 0, false);
 		for(Event i :sc.events){
 			System.out.println(i);
 		}
 		System.out.println("BUILD::");
 		for(Entity i :sc.build){
 			System.out.println(i);
 		}
 		for(Entry<Section, Category> i:sc.category.entrySet()){
 			System.out.println("cat:"+i.getKey());
 			for(int j : i.getValue().value){
 				System.out.print(" "+j);
 			}
 			System.out.println();
 		}
 		
 		for(Entry<String, Entity> a : sc.entities.entrySet()){
 			if(a.getValue().value!=null && a.getValue().value.length>0 && a.getValue().value[0]!=0){
 				System.out.println(a);
 			}
 		}
 	}
 	
 	private void assertEvents() {
 		int actualActive = 0;
 		for(Event i : events){
 			if(i.active) actualActive++;
 		}
 		if(actualActive!=this.activeEvents){
 			throw new IllegalStateException(" active "+actualActive+" but should be "+activeEvents);
 		}
 	}
 	public void insertIntoBuild(Entity entity)
 	{
 		this.insertIntoBuild(entity,0);
 	}
 	public Entity getEntityByName(String name)
 	{
 		return this.entities.get(name);
 	}
 	public Map<String, Entity> getEntities()
 	{
 		return entities;
 	}
 	public Race getRace(String name)
 	{
 		return SC2Planner.loader.getRace(name);
 	}
 	public boolean isSuccessfull()
 	{
 		for(Entity i: entities.values())
 		{
 			if(i.eventualError)
 			{
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public void clearBuilds()
 	{
 		SC2Planner.loader.init();
 		this.reset();
 		this.build.clear();
 		this.delays.clear();
 		this.food.clear();
 		
 		this.currentPosition = -1;
 	}
 	
 	public int getCurrentTime()
 	{
 		return this.currentTime;
 	}
 }
