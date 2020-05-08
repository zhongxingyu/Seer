 package cc.warlock.core.stormfront.script.internal;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import cc.warlock.core.client.IProperty;
 import cc.warlock.core.client.IPropertyListener;
 import cc.warlock.core.script.IScript;
 import cc.warlock.core.script.Match;
 import cc.warlock.core.script.internal.RegexMatch;
 import cc.warlock.core.script.internal.ScriptCommands;
 import cc.warlock.core.stormfront.client.IStormFrontClient;
 import cc.warlock.core.stormfront.script.IStormFrontScriptCommands;
 
 public class StormFrontScriptCommands extends ScriptCommands implements IStormFrontScriptCommands, IPropertyListener<Integer> {
 
 	protected IStormFrontClient sfClient;
 	protected IScript script;
 	protected List<Match> actions;
 	
 	public StormFrontScriptCommands (IStormFrontClient client, IScript script)
 	{
 		super(client);
 		this.sfClient = client;
 		waitingForRoundtime = false;
 		this.script = script;
 		
 		client.getRoundtime().addListener(this);
 	}
 	
 	public IStormFrontClient getStormFrontClient() {
 		return sfClient;
 	}
 	
 	protected boolean waitingForRoundtime;
 	public void waitForRoundtime ()
 	{
 		if(!interrupted) {
 			assertPrompt();
 			try {
 				while(sfClient.getRoundtime().get() > 0 && !interrupted) {
 					Thread.sleep((sfClient.getRoundtime().get() + 1) * 1000);
 				}
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	@Override
 	public void nextRoom() {
 		try {
 			super.nextRoom();
 		} finally {
 			waitForRoundtime();
 		}
 	}
 	
 	@Override
 	public void pause(int seconds) {
 		try {
 			super.pause(seconds);
 		} finally {
 			waitForRoundtime();
 		}
 	}
 	
 	@Override
 	public void waitFor(Match match) {
 		try {
 			super.waitFor(match);
 		} finally {
 			waitForRoundtime();
 		}
 	}
 	
 	@Override
 	public void waitForPrompt() {
 		try {
 			super.waitForPrompt();
 		} finally {
 			waitForRoundtime();
 		}
 	}
 	
 	
 	public void propertyActivated(IProperty<Integer> property) {}
 	public void propertyChanged(IProperty<Integer> property, Integer oldValue) {
 		if (property.getName().equals("roundtime"))
 		{
 			if (property.get() == 0) waitingForRoundtime = false;
 		}
 	}
 	
 	public void propertyCleared(IProperty<Integer> property, Integer oldValue) {}
 	
 	protected  class ScriptActionThread implements Runnable {
 		public void run() {
 			System.out.println("Starting action thread");
 			LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();
 			textWaiters.add(queue);
 			actionLoop: while(true) {
 				String text = null;
 
 				while(text == null) {
 					try {
 						text = queue.poll(100L, TimeUnit.MILLISECONDS);
 						if(actions == null || interrupted) {
 							break actionLoop;
 						}
 					} catch(Exception e) {
 						e.printStackTrace();
 					}
 				}
 				System.out.println("ACTION: matching with \"" + text + "\"");
 				for(Match action : actions) {
 					if(action.matches(text)) {
						System.out.println("ACTION: matched");
 						String command = (String)action.getAttribute("action");
 						script.execute(command);
 						break;
 					}
 				}
 			}
 			textWaiters.remove(queue);
 		}
 	}
 	
 	public void addAction(String action, String text) {
 		if(actions == null) {
 			actions = Collections.synchronizedList(new ArrayList<Match>());
 			new Thread(new ScriptActionThread()).start();
 		}
 		Match m = new RegexMatch(text);
 		m.setAttribute("action", action);
 		m.setAttribute("name", text);
 		actions.add(m);
 	}
 	
 	public void clearActions() {
 		actions = null;
 	}
 	
 	public void removeAction(String text) {
 		Iterator<Match> iter = actions.iterator();
 		while(iter.hasNext()) {
 			// remove the element with the same name as text
 			if(iter.next().getAttribute("name").equals(text)) {
 				iter.remove();
 			}
 		}
 		if(actions.size() == 0) {
 			actions = null;
 		}
 	}
 	
 }
