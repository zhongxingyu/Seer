 package backend;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import math.Vec;
 import backend.environment.Element;
 import backend.environment.Obstacle;
 import backend.environment.Waypoint;
 
 public class Simulation extends Thread {
 	public CopyOnWriteArrayList<Element> elements;
 	public HeightMap hm = null;
 	private  ArrayList<Object> snapshots;
 	
 	public int timeStep = 20, stepsPerSave = 10;
 	private volatile int time = 0, totalTime = 0;
 	
 	public boolean isRunning = false;
 	
 	public Simulation()	{
 		elements = new CopyOnWriteArrayList<Element>();
 		
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
		elements.add(new Obstacle(
 				new Waypoint(50, 0),
 				new Waypoint(60, 50),
 				new Waypoint(0, 100),
 				new Waypoint(0, 0)
		));
 		
 		snapshots = new ArrayList<Object>();
 		
 		hm = new HeightMap(new File("./maps/GC2.map"));
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
 				if(time == totalTime && time % (timeStep*stepsPerSave) == 0)	{
 					snapshots.add(elements.clone());
 				}
 				for(Element e : elements)
 					e.calculateUpdate(elements);
 				for(Element e : elements)
 					e.update();
 			}
 		}
 		catch(InterruptedException ie)	{}
 	}
 	
 	public void setTime(int t)	{
 		time = Math.max(t - t%timeStep, 0);
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
