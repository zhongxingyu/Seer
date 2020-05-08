 package org.dyndns.schuschu.xmms2client.action.playlist;
 
import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.swing.JOptionPane;
 
 import org.dyndns.schuschu.xmms2client.interfaces.FooInterfaceClickable;
 
 import se.fnord.xmms2.client.Client;
 import se.fnord.xmms2.client.commands.Collection;
 import se.fnord.xmms2.client.commands.Command;
 import se.fnord.xmms2.client.commands.Playlist;
import se.fnord.xmms2.client.commands.internal.PlaylistChangedBroadcastCommand;
 import se.fnord.xmms2.client.types.CollectionNamespace;
 
 public class FooActionPlaylistDelete extends FooActionPlaylist {
 
 	public FooActionPlaylistDelete(FooInterfaceClickable clickable,
 			Client client) {
 		super(clickable, client);
 	}
 
 	@Override
 	public void clicked() {
 		// TODO: Dropdown dialog
 
 		Command get = Playlist.listPlaylists();
 
 		String input = null;
 
 		try {
 
 			Map<CollectionNamespace, Set<String>> map = get
 					.executeSync(getClient());
 
 			Vector<String> content = new Vector<String>();
 
 			for (Set<String> names : map.values()) {
 				for (String name : names) {
 					if (!name.startsWith("_")) {
 						content.add(name);
 					}
 				}
 			}
 
 			input = (String) JOptionPane.showInputDialog(null,
 					"Please choose the playlist you want to delete",
 					"Delete Playlist", JOptionPane.PLAIN_MESSAGE, null, content
 							.toArray(), content.get(0));
 
 		} catch (InterruptedException e) {
 			Thread.currentThread().interrupt();
 		}
 
 		if (input != null) {
 			Command c = Collection.remove(CollectionNamespace.PLAYLISTS, input);
 			c.execute(getClient());
 		}
 	}
 }
