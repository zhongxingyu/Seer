 package pl.edu.agh.io.coordinator.utils.container;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import pl.edu.agh.io.coordinator.MainMapActivity;
 import pl.edu.agh.io.coordinator.resources.Group;
 import pl.edu.agh.io.coordinator.resources.Layer;
 import pl.edu.agh.io.coordinator.resources.MapItem;
 import pl.edu.agh.io.coordinator.resources.User;
 import pl.edu.agh.io.coordinator.resources.UserState;
 import android.app.Activity;
 import android.util.Log;
 
 public class DataContainer {
 
 	private OnDataContainerChangesListener listener;
 	private MainMapActivity activity;
 	private Map<MapItem, Layer> mapItems = Collections.synchronizedMap(new HashMap<MapItem, Layer>());
 	private Map<User, UserState> users = Collections.synchronizedMap(new HashMap<User, UserState>());
 	private Map<Group, Set<String>> groups = Collections.synchronizedMap(new HashMap<Group, Set<String>>());
 	private Map<User, Set<String>> userItems = Collections.synchronizedMap(new HashMap<User, Set<String>>());
 	
 	private Set<String> getGroupsForUser(User user) {
 		String id = user.getId();
 		Set<String> toReturn = new HashSet<String>();
 		synchronized (groups) {
 			for (Group g : groups.keySet()) {
 				if (groups.get(g).contains(id)) {
 					toReturn.add(g.getId());
 				}
 			}
 		}
 		return toReturn;
 	}
 	
 	private Set<String> getItemsForUser(User user) {
 		return userItems.get(user);
 	}
 	
 	public DataContainer(Activity activity) {
 		try {
 			this.listener = (OnDataContainerChangesListener) activity;
 		} catch (ClassCastException e) {
 			throw new ClassCastException(activity.toString() + " must implement OnDataContainerChangesListener");
 		}
 		this.activity = (MainMapActivity) activity;
 	}
 
 	public void newUsersSet(Map<User, UserState> newUsers) {
 		HashSet<User> toRemove = new HashSet<User>();
 		synchronized (users) {
 			for (User u : users.keySet()) {
 				if (!newUsers.containsKey(u)) {
 					listener.userRemoved(u);
 					toRemove.add(u);
 				}
 			}
 		}
 		for (User u : toRemove) {
 			users.remove(u);
 		}
 		for (User u : newUsers.keySet()) {
 			if ((!users.containsKey(u)) || (!users.get(u).equals(newUsers.get(u)))) {
 				users.put(u, newUsers.get(u));
 				if (activity.getUserFilter().isEligible(u.getId(), getItemsForUser(u), getGroupsForUser(u))) {
 					listener.userAdded(u, newUsers.get(u));
 				}
 			}
 		}
 	}
 	
 	public void newGroupsSet(Map<Group, Set<String>> newGroups) {
 		Log.d("DataContainer", "starting newGroupsSet");
 		HashSet<Group> toRemove = new HashSet<Group>();
 		synchronized (groups) {
 			for (Group g : groups.keySet()) {
 				if (!newGroups.containsKey(g)) {
 					toRemove.add(g);
 				}
 			}
 		}
 		for (Group g : toRemove) {
 			groups.remove(g);
 		}
 		for (Group g : newGroups.keySet()) {
 			groups.put(g, newGroups.get(g));
 		}
 	}
 
 	public void newUserItemsSet(Map<User, Set<String>> newUserItems) {
 		HashSet<User> toRemove = new HashSet<User>();
 		synchronized (userItems) {
 			for (User u : userItems.keySet()) {
 				if (!newUserItems.containsKey(u)) {
 					toRemove.add(u);
 				}
 			}
 		}
 		for (User u : toRemove) {
 			userItems.remove(u);
 		}
 		for (User u : newUserItems.keySet()) {
 			userItems.put(u, newUserItems.get(u));
 		}
 	}
 	
 	public void newMapItemsSet(Layer layer, Set<MapItem> newMapItems) {
 		HashSet<MapItem> toRemove = new HashSet<MapItem>();
 		synchronized (mapItems) {
 			for (MapItem i : mapItems.keySet()) {
 				if (mapItems.get(i).equals(layer) && (!newMapItems.contains(i))) {
 					listener.mapItemRemoved(layer, i);
 					toRemove.add(i);
 				}
 			}
 		}
 		for (MapItem i : toRemove) {
 			mapItems.remove(i);
 		}
 		for (MapItem i : newMapItems)
 			if (!mapItems.keySet().contains(i)) {
 				mapItems.put(i, layer);
 				if (activity.isLayerActive(layer.getName())) {
 					listener.mapItemAdded(layer, i);
 				}
 			}
 	}
 	
 	public void showLayer(String layer) {
 		synchronized (mapItems) {
 			for (MapItem i : mapItems.keySet()) {
 				if (mapItems.get(i).getName().equals(layer)) {
 					listener.mapItemAdded(new Layer(layer), i);
 				}
 			}
 		}
 	}
 	
 	public void hideLayer(String layer) {
 		synchronized (mapItems) {
 			for (MapItem i : mapItems.keySet()) {
 				if (mapItems.get(i).getName().equals(layer)) {
 					listener.mapItemRemoved(new Layer(layer), i);
 				}
 			}
 		}
 	}
 
 	public void repaintUsers() {
 		synchronized (users) {
 			for (User u : users.keySet()) {
 				if (activity.getUserFilter().isEligible(u.getId(), getItemsForUser(u), getGroupsForUser(u))) {
 					listener.userAdded(u, users.get(u));
 				} else {
 					listener.userRemoved(u);
 				}
 			}
 		}
 	}
 	
 	public interface OnDataContainerChangesListener {
 		public void mapItemAdded(Layer layer, MapItem mapItem);
 		public void mapItemRemoved(Layer layer, MapItem mapItem);
 		public void userAdded(User user, UserState state);
 		public void userRemoved(User user);
 	}
 }
