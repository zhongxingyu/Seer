 package net.marcuswhybrow.uni.g52gui.cw2.history;
 
 import net.marcuswhybrow.uni.g52gui.cw2.Page;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import net.marcuswhybrow.uni.g52gui.cw2.Browser;
 
 /**
  *
  * @author marcus
  */
 public class History implements Iterable<HistoryEntry>
 {
 	private static History h = null;
 
 	private ArrayList<HistoryEntry> history;
 	private HashMap<Page, Integer> visitCount;
 	private ArrayList<HistoryChangeListener> historyChangeListeners;
 
 	private History() {}
 
 	private void setup()
 	{
 		history = Browser.get().readFromHistoryFile("history.xml");
 		if (history == null)
 		{
 			history = new ArrayList<HistoryEntry>();
 			visitCount = new HashMap<Page, Integer>();
 		}
 		else
 			buildCountFromHistory();
 
 		historyChangeListeners = new ArrayList<HistoryChangeListener>();
 	}
 
 	public static History get()
 	{
 		if (h == null)
 		{
 			h = new History();
 			h.setup();
 		}
 		return h;
 	}
 
 	public void buildCountFromHistory()
 	{
 		visitCount = new HashMap<Page, Integer>();
 
 		for (HistoryEntry entry : history)
 			countPage(entry.getPage());
 	}
 
 	public void addHistoryEntry(Page page)
 	{
 		history.add(new HistoryEntry(page));
 
 		countPage(page);
 		
 		this.notifyHistoryChangeListeners();
 	}
 
 	private void countPage(Page page)
 	{
 		Integer countForAddress = visitCount.get(page);
 		if (countForAddress == null)
 			visitCount.put(page, 1);
 		else
 			visitCount.put(page, countForAddress + 1);
 	}
 
	public void notifyHistoryChangeListeners()
 	{
 		for (HistoryChangeListener hcl : historyChangeListeners)
 		{
 			hcl.historyHasChanged((ArrayList<HistoryEntry>) history.clone());
 
 			ArrayList<VisitCountEntry> l = new ArrayList<VisitCountEntry>();
 			for (Map.Entry entry : visitCount.entrySet())
 			{
 				Page page = (Page) entry.getKey();
 				int count = (Integer) entry.getValue();
 				l.add(new VisitCountEntry(page, count));
 			}
 			Collections.sort(l);
 
 			hcl.visitCountHasChanged(l);
 		}
 	}
 
 	public Iterator<HistoryEntry> iterator()
 	{
 		return history.iterator();
 	}
 
 	public ArrayList<HistoryEntry> getArrayList()
 	{
 		return history;
 	}
 
 	public void addHistoryChangeListener(HistoryChangeListener hcl)
 	{
 		this.historyChangeListeners.add(hcl);
 	}
 
 	public void removeHistoryChangeListener(HistoryChangeListener hcl)
 	{
 		this.historyChangeListeners.remove(hcl);
 	}
	
 }
