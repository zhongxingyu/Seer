 package org.bardes.state;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.bardes.entities.Cue;
 import org.bardes.entities.Show;
 import org.bardes.html.WSS;
 
 
 public class DisplayPool
 {
 	private static Map<String, DisplayState> projectors = new HashMap<String, DisplayState>();
 	private static ExecutorService threadPool = Executors.newCachedThreadPool();
 	
 	private static Show show;
 	private static List<Cue> cues;
 	
 	private static Cue currentCue = null;
 	private static Thread t;
 
 	public static DisplayState get(String projectorId)
 	{
 		return projectors.get(projectorId);
 	}
 
 	public static void startup()
 	{
 		t = new Thread(new Runnable() {
 
 			public void run()
 			{
 				try
 				{
 					Thread.sleep(10000);
 				}
 				catch (InterruptedException e)
 				{
 					e.printStackTrace();
 					return;
 				}
 				
 				WSS wss = WSS.getInstance();
 				DB db = new DB();
 				show = db.getShow();
 				
 				cues = db.getCues();
 				Collections.sort(cues);
 				
				for (int i = 1; i < show.getMaxProjectors(); i++)
 				{
 					String uri = "/display/"+i;
 					
 					ProjectorState projectorState = new ProjectorState(i);
 					projectors.put(uri, projectorState);
 					threadPool.submit(projectorState);
 					
 					wss.registerDisplayStateCallback(uri, projectorState);
 				}
 				
 				t = null;
 			}
 		});
 		
 		t.start();
 	}
 	
 	public static void goCue(Cue cue)
 	{
 		currentCue = cue;
 		if (projectors == null)
 			return;
 		
 		for (DisplayState d : projectors.values())
 		{
 			d.goCue(cue);
 		}
 	}
 	
 	public static void goCue(String cue)
 	{
 		int n;
 		if (cue.equalsIgnoreCase("next")) 
 		{
 			n = cues.indexOf(currentCue);
 			n++;
 		}
 		else if (cue.equalsIgnoreCase("prev")) 
 		{
 			n = cues.indexOf(currentCue);
 			n--;
 		}
 		else
 		{
 			Cue x = new Cue();
 			x.setCue(cue);
 			
 			n = cues.indexOf(x);
 		}
 		if (n >= 0 && n < cues.size())
 		{
 			Cue c = cues.get(n);
 			goCue(c);
 		}
 	}
 	
 	public static Cue go()
 	{
 		if (cues == null || cues.size() == 0)
 			return null;
 		
 		if (currentCue == null)
 		{
 			goCue(cues.get(0));
 		}
 		else
 		{
 			int n = cues.indexOf(currentCue);
 			if (n+1 < cues.size())
 			{
 				goCue(cues.get(n+1));
 			}
 		}
 		return currentCue;
 	}
 	
 	public static void join() throws InterruptedException
 	{
 		t.join();
 	}
 
 	public static void shutdown()
 	{
 		WSS wss = WSS.getInstance();
 		wss.closeAll();
 		
 		for (DisplayState s : projectors.values())
 		{
 			s.shutdown();
 		}
 		threadPool.shutdownNow();
 	}
 
 	public static void refresh()
 	{
 		DB db = new DB();
 		show = db.getShow();
 		
 		cues = db.getCues();
 		Collections.sort(cues);
 		
 		WSS wss = WSS.getInstance();
 		for (int projectorId = 1; projectorId <= show.getMaxProjectors(); projectorId++)
 		{
 			try
 			{
 				wss.sendDisplay(projectorId, "refresh");
 			}
 			catch (InterruptedException ignore)
 			{
 			}
 		}
 	}
 
 	public static void blank(String p) 
 	{
 		String[] split = p.split(",");
 		
 		Double cueNum = Double.valueOf(split[0]);
 		int projector = Integer.valueOf(split[1]);
 		
 		DB db = new DB();
 		
 		db.blankSlide(cueNum, projector);
 		refresh();
 	}
 
 	public static void track(String p) 
 	{
 		String[] split = p.split(",");
 		
 		Double cueNum = Double.valueOf(split[0]);
 		int projector = Integer.valueOf(split[1]);
 		
 		DB db = new DB();
 		
 		db.trackSlide(cueNum, projector);
 		refresh();
 	}
 }
