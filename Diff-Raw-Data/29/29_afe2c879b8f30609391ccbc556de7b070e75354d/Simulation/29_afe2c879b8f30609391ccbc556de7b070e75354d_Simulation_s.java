 package backend;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import math.Vec;
 import backend.environment.Element;
 import backend.environment.Obstacle;
 import backend.environment.Waypoint;
 
 /**
  * Simulation is the class that handles the interaction
  * between elements and each other, as well as the
  * environment
  */
 class RenderObjectList extends ArrayList<RenderObject>	{
 	int timeTaken;
 	
 	public RenderObjectList(int size, int timeTaken)	{
 		super(size);
 		this.timeTaken = timeTaken;
 	}
 }
 
 public class Simulation extends Thread {
 	public UnforgivingArrayList<Element> elements;
 	public HeightMap hm = null;
 	private  ArrayList<RenderObjectList> snapshots;
 	
 	public int timeStep = 20, stepsPerSave = 10;
 	private volatile int time = 0, totalTime = 0;
 	
 	public boolean isRunning = false;
 	
 	public Simulation()	{
 		elements = new UnforgivingArrayList<Element>(0);
 		
 		//we add some waypoints for testing purposes
 		/*Waypoint prev = null, first = null;
 		for(int i = 0; i < 10; i++)	{
 			Waypoint cur = new Waypoint(new Vec(Math.random()*1000-500, Math.random()*1000-500));
 			if(prev != null)
 				prev.setTarget(cur);
 			if(first == null)
 				first = cur;
 			prev = cur;
 			elements.add(cur);
 		}
 		if(prev != null && first != null && prev != first)
 			prev.setTarget(first);*/
 		
 		//create an obstacle
 		/*elements.add(new Obstacle(
 				new Waypoint(50, 0),
 				new Waypoint(60, 50),
 				new Waypoint(0, 100),
 				new Waypoint(0, 0)
 		));*/
 		
 		snapshots = new ArrayList<RenderObjectList>();
 		
 		hm = new HeightMap(new File("./maps/GC2.map"));
 		//hm = new HeightMap();
 		setName("Simulation");
 		//hm = new HeightMap();
 	}
 	
 	public void run()	{
 		try {
 			while(true)	{
 				Thread.sleep(timeStep);
 				if(!isRunning)	continue;
 				boolean upTT = totalTime <= time;
 				time += timeStep;
 				if(upTT)
 					totalTime = time;
 				int ind = getSnapshotsIndex(time);
 				//System.out.println((ind < snapshots.size() ? snapshots.get(ind).timeTaken : -1) + " " + time);
 				//if(time % (timeStep*stepsPerSave) == 0)	{
 				if(time % (timeStep*stepsPerSave) == 0 && (time == totalTime || ind >= snapshots.size()))	{
 					System.out.println("dirty and added snapshot");
 					//snapshots.add(elements.clone());
 					RenderObjectList ss = new RenderObjectList(elements.size(), time);
 					for(Element e : elements)
 						ss.add(new RenderObject(e));
 					snapshots.add(ss);
 					elements.clean();
 				}
 				else if(ind < snapshots.size() && snapshots.get(ind).timeTaken == time)	{
 					System.out.println("dirty and updated snapshot");
 					if(elements.isDirty())	{
 						while(snapshots.size() > ind+1)
 							snapshots.remove(snapshots.size()-1);
 						RenderObjectList l = snapshots.get(ind);
 						for(Element e: elements.removed)
 							l.remove(new RenderObject(e));
 						for(Element e: elements.added)
 							l.add(new RenderObject(e));
 						setTotalTime(time);
 					}
 					apply(ind);	//this will also clean elements
 				}
 				else if(elements.isDirty())	{
 					System.out.println("dirty and inserted snapshot");
 					//create a new list
 					RenderObjectList ss = new RenderObjectList(elements.size(), time);
 					for(Element e : elements)
 						ss.add(new RenderObject(e));
 					//find where to insert this
 					while(snapshots.size() > 0
 							&& snapshots.get(snapshots.size()-1).timeTaken > time)
 						snapshots.remove(snapshots.size()-1);
 					snapshots.add(ss);
 					elements.clean();
 					setTotalTime(time);
 				}
 				update();
 			}
 		}
 		catch(InterruptedException ie)	{}
 	}
 	
 	private void apply(int i)	{
 		synchronized(elements)	{
 			elements.clear();
 			for(RenderObject r : snapshots.get(i))	{
 				r.apply();
 				elements.add(r.element);
 			}
 			elements.clean();
 		}
 	}
 	
 	private void update()	{
 		synchronized(elements)	{
 			for(Element e : elements)
 				e.calculateUpdate(elements, hm);
 			for(Element e : elements)
 				e.update();
 		}
 	}
 	
 	private int getRoundedTime(int t)	{
 		int timeDiff = t%timeStep;
 		if(timeDiff < timeStep*0.5)
 			timeDiff = -timeDiff;
 		else
 			timeDiff = timeStep-timeDiff;
 		return Math.min(Math.max(t + timeDiff, 0), totalTime);
 	}
 	
 	//returns the index of the latest snapshot before the given time
 	private int getSnapshotsIndex(int t)	{
 		//return (int)((double)(t/timeStep)/stepsPerSave);
 		for(int i = 0; i < snapshots.size(); i++)
 			if(snapshots.get(i).timeTaken >= t)
 				return i;
 		return snapshots.size();
 	}
 	
 	public void setTime(int t)	{
 		//time = Math.max(t - t%timeStep, 0);
 		time = getRoundedTime(t);
 		int ind = Math.min(snapshots.size()-1, getSnapshotsIndex(time));
 		
 		apply(ind);
 		
 		//now figure out how many frames to extrapolate for and then extrapolate
 		int steps = time/timeStep - ind*stepsPerSave;
 		for(int i = 0; i < steps; i++)
 			update();
 	}
 	
 	public void setTotalTime(int t)	{
 		totalTime = Math.max(t - t%timeStep, 0);
 	}
 	
 	public int getTime()	{
 		return time;
 	}
 	
 	public int getTotalTime()	{
 		return totalTime;
 	}
 	
 	public void loadHeightMap(File map) 	{
 		hm = new HeightMap(map);
 	}
 	
 	public void setHeightMap(HeightMap hm)	{
 		this.hm = hm;
 	}
 }
