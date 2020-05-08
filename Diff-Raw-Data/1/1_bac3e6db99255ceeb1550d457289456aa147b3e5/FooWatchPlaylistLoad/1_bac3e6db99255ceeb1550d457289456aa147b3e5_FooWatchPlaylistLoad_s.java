 package org.dyndns.schuschu.xmms2client.watch;
 
 import java.util.Vector;
import java.util.prefs.BackingStoreException;
 
 import org.dyndns.schuschu.xmms2client.interfaces.FooInterfaceViewElement;
 
 import se.fnord.xmms2.client.Client;
 import se.fnord.xmms2.client.commands.Command;
 import se.fnord.xmms2.client.commands.Playlist;
 
 public class FooWatchPlaylistLoad extends Thread {
 
 	private boolean running;
 	private Command c;
 	private Runnable r;
 	private final FooInterfaceViewElement view;
 	private int current;
 
 	public FooWatchPlaylistLoad(Client client,
 			final FooInterfaceViewElement view) {
 		this.view = view;
 		c = Playlist.currentPosBroadcast();
 		c.execute(client);
 
 		r = new Runnable() {
 			public void run() {
 				view.setSelection(new int[] { current });
 				view.getBackend().selectionChanged();
 			}
 		};
 
 	}
 
 	public void done() {
 		running = false;
 	}
 
 	public void run() {
 		running = true;
 		while (running) {
 			try {
 				String s = c.waitReply();
 
 				Vector<String> content = view.getBackend().getContent();
 
 				for (int i = 0; i < content.size(); i++) {
 					if(s.equals(content.get(i))){
 						current = i;
 						break;
 					}
 				}
 
 				view.getReal().getDisplay().asyncExec(r);
 			} catch (InterruptedException e) {
 				Thread.currentThread().interrupt();
 			}
 		}
 	}
 }
