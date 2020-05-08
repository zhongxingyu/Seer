 package cc.warlock.core.script.internal;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import cc.warlock.core.client.IStream;
 import cc.warlock.core.client.IStreamListener;
 import cc.warlock.core.client.IStyledString;
 import cc.warlock.core.client.IWarlockClient;
 import cc.warlock.core.client.internal.Command;
 import cc.warlock.core.script.IScript;
 import cc.warlock.core.script.IScriptCommands;
 import cc.warlock.core.script.Match;
 
 public class ScriptCommands implements IScriptCommands, IStreamListener
 {
 
 	protected IWarlockClient client;
 	protected final Lock lock = new ReentrantLock();
 	
 	protected List<LinkedBlockingQueue<String>> textWaiters = Collections.synchronizedList(new ArrayList<LinkedBlockingQueue<String>>());
 	
 	protected final Condition nextRoom = lock.newCondition();
 	protected boolean roomWaiting = false;
 	
 	protected final Condition gotPromptCond = lock.newCondition();
 	protected int promptWaiters = 0;
 	protected boolean gotPrompt = false;
 	
 	protected boolean stopped = false;
 	
 	protected Thread pausedThread;
 	
 	public ScriptCommands (IWarlockClient client)
 	{
 		this.client = client;
 		
 		client.getDefaultStream().addStreamListener(this);
 	}
 	
 	public void echo (String text) {
 		client.getDefaultStream().echo(text);
 	}
 	
 	public void echo (IScript script, String text) {
 		client.getDefaultStream().echo("[" + script.getName() + "]: " + text);
 	}
 	
 	protected void assertPrompt() {
 		while (!client.getDefaultStream().isPrompting())
 		{
 			try {
 				Thread.sleep(200);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public Match matchWait (List<Match> matches) {
 		LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();
 		textWaiters.add(queue);
 		String text = null;
 
 		try {
 			// run until we get a match or are told to stop
 			matchWaitLoop: while(true) {
 				System.out.println("Waiting for text");
 				// wait for some text
 				while(text == null) {
 					try {
 						text = queue.poll(100L, TimeUnit.MILLISECONDS);
 					} catch(Exception e) {
 						e.printStackTrace();
 					}
 					if(stopped)
 						break matchWaitLoop;
 				}
 				System.out.println("Got text: " + text);
 				String[] lines = text.split("\\n");
 				for(String line : lines) {
 					// try all of our matches
 					for(Match match : matches) {
 						// System.out.println("Trying a match");
 						if(match.matches(line)) {
 							// System.out.println("matched a line");
 							return match;
 						}
 					}
 				}
 				text = null;
 			}
 		} finally {
 			System.out.println("Done with matchwait");
 			textWaiters.remove(queue);
 		}
 
 		return null;
 	}
 
 	public void move (String direction) {
 		client.send(direction);
 		client.getDefaultStream().echo(direction);
 		nextRoom();
 	}
 
 	public void nextRoom () {
 		lock.lock();
 		try {
 			roomWaiting = true;
 			while (!stopped && roomWaiting) {
 				nextRoom.await();
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 		} finally {
 			roomWaiting = false;
 			lock.unlock();
 		}
 	}
 
 	public void pause (int seconds) {
 		try {
 			// FIXME need to make this work for multiple users
 			pausedThread = Thread.currentThread();
 			Thread.sleep(seconds * 1000);
 		} catch(InterruptedException e) {
 			e.printStackTrace();
 		} finally {
 			pausedThread = null;
 		}
 	}
 
 	public void put (String text) {
 		// false command so it doesn't get added to the command history
 		Command command = new Command(text, new Date());
 		command.setInHistory(true);
 		
 		client.send(command);
 		client.getDefaultStream().echo(text);
 	}
 	
 	public void put (IScript script, String text) {
 		assertPrompt();
 		
 		// false command so it doesn't get added to the command history
 		Command command = new Command(text, new Date());
 		command.setInHistory(true);
 		
 		client.send(command);
 		client.getDefaultStream().echo("[" + script.getName() + "]: " + text);
 	}
 
 	public void waitFor (Match match) {
 		LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();
 		String text = null;
 
 		textWaiters.add(queue);
		waitForLoop: while(true) {
			while(text == null) {
				try {
					text = queue.poll(100L, TimeUnit.MILLISECONDS);
				} catch(Exception e) {
					e.printStackTrace();
 				}
				if(stopped)
					break waitForLoop;
			}
			if(match.matches(text)) {
				break;
 			}
			text = null;
 		}
		textWaiters.remove(queue);
 	}
 
 	public void waitForPrompt () {
 		lock.lock();
 		try {
 			promptWaiters++;
 			while(!stopped && !gotPrompt) {
 				gotPromptCond.await();
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 		} finally {
 			promptWaiters--;
 			if(promptWaiters == 0) {
 				gotPrompt = false;
 			}
 			lock.unlock();
 		}
 	}
 	
 	public IWarlockClient getClient() {
 		return this.client;
 	}
 		
 	public void streamCleared(IStream stream) {}
 	public void streamEchoed(IStream stream, String text) {}
 	
 	public void streamPrompted(IStream stream, String prompt) {
 		if(promptWaiters > 0) {
 			lock.lock();
 			try {
 				gotPrompt = true;
 				gotPromptCond.signalAll();
 			} catch(Exception e) {
 				e.printStackTrace();
 			} finally {
 				lock.unlock();
 			}
 		}
 	}
 	
 	public void streamDonePrompting (IStream stream) { }
 	
 	public void streamReceivedText(IStream stream, IStyledString string) {
 		String text = string.getBuffer().toString();
 		for(LinkedBlockingQueue<String>  queue : textWaiters) {
 			System.out.println("Signaling a waiter");
 			try {
 				queue.put(text);
 			} catch(Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public void movedToRoom() {
 		lock.lock();
 		try {
 			roomWaiting = false;
 			nextRoom.notifyAll();
 		} finally {
 			lock.unlock();
 		}
 	}
 
 	
 	public void stop() {
 		lock.lock();
 		stopped = true;
 		try {
 			// gotTextCond.signalAll(); perhaps signal everyone here?
 			gotPromptCond.signalAll();
 			nextRoom.signalAll();
 			if(pausedThread != null)
 				pausedThread.interrupt();
 		} catch(Exception e) {
 			e.printStackTrace();
 		} finally {
 			lock.unlock();
 		}
 	}
 }
