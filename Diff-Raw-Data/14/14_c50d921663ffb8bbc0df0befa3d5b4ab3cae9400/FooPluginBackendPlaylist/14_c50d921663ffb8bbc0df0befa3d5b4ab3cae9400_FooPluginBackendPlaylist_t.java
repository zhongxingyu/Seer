 package org.dyndns.schuschu.xmms2client.backend;
 
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Set;
 import java.util.Vector;
 
 import org.dyndns.schuschu.xmms2client.view.element.FooInterfaceViewElement;
 
 import se.fnord.xmms2.client.Client;
 import se.fnord.xmms2.client.commands.Command;
 import se.fnord.xmms2.client.commands.Playlist;
 import se.fnord.xmms2.client.types.CollectionBuilder;
 import se.fnord.xmms2.client.types.CollectionExpression;
 import se.fnord.xmms2.client.types.CollectionNamespace;
 
 /**
  * @author schuschu
  * 
  */
 public class FooPluginBackendPlaylist extends Observable implements
 		Serializable, FooInterfaceBackend {
 
 	/**
 	 * I have no idea what that stupid thing is for...
 	 */
 	private static final long serialVersionUID = -2744778880227415342L;
 
 	/**
 	 * Specifies the XMMS2 client (the instance of the object not THIS client
 	 * ... I didn't name it ...)
 	 */
 	private Client client;
 
 	private FooInterfaceViewElement view;
 
 	private CollectionExpression filteredConetent;
 
 	private String[] playlistDatabase;
 
 	private List<Integer> playListOrder;
 
 	/**
 	 * This is the Backend which provides the baseContent.
 	 * 
 	 */
 	private FooInterfaceBackend contentProvider;
 
 	/**
 	 * executeFilterCommand builds the xmms2-command for filtering the
 	 * xmms2-database by the values gotten from the selected values and executes
 	 * it, writing the result into filteredConetent
 	 * 
 	 * @param indices
 	 *            the indices which are currently selected in view
 	 * @throws InterruptedException
 	 *             but i don't know why :)
 	 */
 	public void executeFilterCommand() throws InterruptedException {
 
 		Command command = Playlist.listEntries(Playlist.ACTIVE_PLAYLIST);
 
 		List<Integer> ids = command.executeSync(client);
 
 		CollectionBuilder cb = new CollectionBuilder();
 		cb.addIds(ids);
 		CollectionExpression ce = cb.build();
 
 		playListOrder = ids;
 
 		setFilteredConetent(ce);
 
 	}
 
 	/**
 	 * executeBaseCommand builds the xmms2-command for an database from
 	 * baseContent and executes it, writing the result into baseDatabase
 	 * 
 	 * @throws InterruptedException
 	 *             but i don't know why :)
 	 */
 	public void executeBaseCommand() throws InterruptedException {
 
 		Command c = Playlist.listPlaylists();
 
 		Map<CollectionNamespace, Set<String>> map = c.executeSync(client);
 
 		Vector<String> content = new Vector<String>();
 
 		// TODO: some sort of append but i don't know what i get here
 		for (Set<String> names : map.values()) {
 			for (String name : names) {
 				if (!name.startsWith("_")) {
 					content.add(name);
 				}
 			}
 			// TODO: find better way to do this
 			this.setPlaylistDatabase(content
 					.toArray(new String[content.size()]));
 		}
 
 	}
 
 	/**
 	 * runs executeFilterCommand which generates the filterdContent and then
 	 * writes it into the FooPluginViewElementList next
 	 */
 	public void generateFilteredContent() {
 
 		try {
 			executeFilterCommand();
 		} catch (InterruptedException e) {
 			Thread.currentThread().interrupt();
 			e.printStackTrace();
 		}
 
 		setChanged();
 		notifyObservers();
 	}
 
 	/**
 	 * runs executeBaseCommand which generates the baseDatabase and displays the
 	 * Vector created by createContend into the List (a.k.a. output)
 	 */
 	public void refresh() {
 		try {
 			executeBaseCommand();
 		} catch (InterruptedException e) {
 			Thread.currentThread().interrupt();
 			e.printStackTrace();
 		}
 		
 		int[] selection = view.getIndices();
 
 		view
 				.setContent(new Vector<String>(Arrays
 						.asList(getPlaylistDatabase())));
 		
 		view.setSelection(selection);
 
 		setChanged();
		notifyObservers();
 	}
 
 	/**
 	 * setToAll sets the baseContent to all media in the medialib
 	 */
 	public void setToAll() {
 		// TODO: what is all here? Do I need this?
 	}
 
 	/**
 	 * The one and only constructor of FooPluginViewElementList
 	 * 
 	 * @param format
 	 *            Formating String i.e.: %album% - %artist%
 	 * @param filter
 	 *            Which tag should be filtered by the list
 	 * @param client
 	 *            the xmms2-client
 	 * @param view
 	 *            the view element associated with this backend (wont crunch
 	 *            numbers for nothing)
 	 */
 	public FooPluginBackendPlaylist(Client client, FooInterfaceViewElement view) {
 		this.view = view;
 		this.playlistDatabase = null;
 		this.setClient(client);
 
 		// There can only be one...
 		view.setSingleSelectionMode();
 
 		refresh();
 
 		try {
 			view.setSelection(new int[] { getActivePlaylistId() });
 		} catch (InterruptedException e) {
 			Thread.currentThread().interrupt();
 			e.printStackTrace();
 		}
 
 	}
 
 	public int getActivePlaylistId() throws InterruptedException {
 
 		Command c = Playlist.currentActive();
 
 		String name = c.executeSync(client);
 
 		for (int i = 0; i < playlistDatabase.length; i++) {
 			if (name.equals(playlistDatabase[i])) {
 				return i;
 			}
 		}
 		System.out.println("I SAID NO!");
 		// you should NEVER get here!
 		return 0;
 
 	}
 
 	/**
 	 * setter function for the CollectionExpression baseContent
 	 * 
 	 * @param baseConetent
 	 */
 	public void setBaseConetent(CollectionExpression baseConetent) {
 		// Stub
 	}
 
 	/**
 	 * getter function for the CollectionExpression filteredConent
 	 * 
 	 * @return
 	 */
 	public CollectionExpression getFilteredConetent() {
 		return filteredConetent;
 	}
 
 	/**
 	 * setter function for the CollectionExpression filteredContent
 	 * 
 	 * @param filteredConetent
 	 */
 	public void setFilteredConetent(CollectionExpression filteredConetent) {
 		this.filteredConetent = filteredConetent;
 	}
 
 	/**
 	 * getter function for the Client client
 	 * 
 	 * @return
 	 */
 	public Client getClient() {
 		return client;
 	}
 
 	/**
 	 * setter function for the Client client
 	 * 
 	 * @param client
 	 */
 	public void setClient(Client client) {
 		this.client = client;
 	}
 
 	public void loadPlaylist() {
 
 		int selection = view.getIndices()[0];
 
 		if (selection >= 0) {
 
 			Command c = Playlist.load(playlistDatabase[selection]);
 
 			try {
 				c.executeSync(client);
 			} catch (InterruptedException e) {
 				Thread.currentThread().interrupt();
 				e.printStackTrace();
 			}
 			generateFilteredContent();
 		}
 	}
 
 	public String[] getPlaylistDatabase() {
 		return playlistDatabase;
 	}
 
 	public void setPlaylistDatabase(String[] playlistDatabase) {
 		this.playlistDatabase = playlistDatabase;
 	}
 
 	@Override
 	public FooInterfaceBackend getContentProvider() {
 		return contentProvider;
 	}
 
 	@Override
 	public void setContentProvider(FooInterfaceBackend contentProvider) {
 		this.contentProvider = contentProvider;
 		contentProvider.addObserver(this);
 
 	}
 
 	@Override
 	public void update(Observable o, Object arg) {
 		contentProvider.getFilteredConetent();
 
 	}
 
 	public void setPlayListOrder(List<Integer> playListOrder) {
 		this.playListOrder = playListOrder;
 	}
 
 	public List<Integer> getPlayListOrder() {
 		return playListOrder;
 	}
 
 	@Override
 	public FooInterfaceViewElement getView() {
 		return view;
 	}
 
 	@Override
 	public void selectionChanged() {
 		loadPlaylist();
 	}
 }
