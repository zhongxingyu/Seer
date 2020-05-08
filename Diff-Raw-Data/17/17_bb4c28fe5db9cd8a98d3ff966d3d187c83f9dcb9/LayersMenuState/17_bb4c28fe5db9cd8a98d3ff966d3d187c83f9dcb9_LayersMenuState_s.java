 package pl.edu.agh.io.coordinator.utils.layersmenu;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import pl.edu.agh.io.coordinator.resources.Group;
 import pl.edu.agh.io.coordinator.resources.Layer;
 import pl.edu.agh.io.coordinator.resources.User;
 import pl.edu.agh.io.coordinator.resources.UserItem;
 import android.os.Parcel;
 import android.os.Parcelable;
 
 public class LayersMenuState implements Parcelable {
 
 	public int expandedGroup;
 	public Set<UserItem> items;
 	public Set<User> people;
 	public Set<Group> groups;
 	public Set<Layer> layers;
 	public Map<String, Boolean> itemsChecks;
 	public Map<String, Boolean> peopleChecks;
 	public Map<String, Boolean> groupsChecks;
 	public Map<String, Boolean> layersChecks;
 	
 	public LayersMenuState() {
 		this.expandedGroup = -1;
 		this.items = null;
 		this.people = null;
 		this.groups = null;
 		this.layers = null;
 		this.itemsChecks = null;
 		this.peopleChecks = null;
 		this.groupsChecks = null;
 		this.layersChecks = null;
 	}
 	
 	public static final Creator<LayersMenuState> CREATOR = new Creator<LayersMenuState>() {
 		@Override
 		public LayersMenuState createFromParcel(Parcel source) {
 			LayersMenuState lms = new LayersMenuState();
 			lms.expandedGroup = source.readInt();
 			int itemsSize = source.readInt();
 			lms.items = new HashSet<UserItem>();
 			for (int i = 0; i < itemsSize; ++i) {
				UserItem ui = source.readParcelable(null);
 				lms.items.add(ui);
 			}
 			int peopleSize = source.readInt();
 			lms.people = new HashSet<User>();
 			for (int i = 0; i < peopleSize; ++i) {
				User u = source.readParcelable(null);
 				lms.people.add(u);
 			}
 			int groupsSize = source.readInt();
 			lms.groups = new HashSet<Group>();
 			for (int i = 0; i < groupsSize; ++i) {
				Group g = source.readParcelable(null);
 				lms.groups.add(g);
 			}
 			int layersSize = source.readInt();
 			lms.layers = new HashSet<Layer>();
 			for (int i = 0; i < layersSize; ++i) {
				Layer l = source.readParcelable(null);
 				lms.layers.add(l);
 			}
 			int itemsChecksSize = source.readInt();
 			lms.itemsChecks = new HashMap<String, Boolean>();
 			for (int i = 0; i < itemsChecksSize; ++i) {
 				String s = source.readString();
 				boolean[] b = new boolean[1];
 				source.readBooleanArray(b);
 				lms.itemsChecks.put(s, b[0]);
 			}
 			int peopleChecksSize = source.readInt();
 			lms.peopleChecks = new HashMap<String, Boolean>();
 			for (int i = 0; i < peopleChecksSize; ++i) {
 				String s = source.readString();
 				boolean[] b = new boolean[1];
 				source.readBooleanArray(b);
 				lms.peopleChecks.put(s, b[0]);
 			}
 			int groupsChecksSize = source.readInt();
 			lms.groupsChecks = new HashMap<String, Boolean>();
 			for (int i = 0; i < groupsChecksSize; ++i) {
 				String s = source.readString();
 				boolean[] b = new boolean[1];
 				source.readBooleanArray(b);
 				lms.groupsChecks.put(s, b[0]);
 			}
 			int layersChecksSize = source.readInt();
 			lms.layersChecks = new HashMap<String, Boolean>();
 			for (int i = 0; i < layersChecksSize; ++i) {
 				String s = source.readString();
 				boolean[] b = new boolean[1];
 				source.readBooleanArray(b);
 				lms.layersChecks.put(s, b[0]);
 			}
 			return lms;
 		}
 		@Override
 		public LayersMenuState[] newArray(int size) {
 			return new LayersMenuState[size];
 		}
 	};
 	
 	@Override
 	public int describeContents() {
 		return 0;
 	}
 	
 	@Override
 	public void writeToParcel(Parcel dest, int flags) {
 		dest.writeInt(this.expandedGroup);
 		dest.writeInt(this.items.size());
 		for (UserItem ui : items) {
 			dest.writeParcelable(ui, 0);
 		}
 		dest.writeInt(this.people.size());
 		for (User u : people) {
 			dest.writeParcelable(u, 0);
 		}
 		dest.writeInt(this.groups.size());
 		for (Group g : groups) {
 			dest.writeParcelable(g, 0);
 		}
 		dest.writeInt(this.layers.size());
 		for (Layer l : layers) {
 			dest.writeParcelable(l, 0);
 		}
 		dest.writeInt(this.itemsChecks.size());
 		for (String s : itemsChecks.keySet()) {
 			boolean[] b = new boolean[1];
 			b[0] = itemsChecks.get(s);
 			dest.writeString(s);
 			dest.writeBooleanArray(b);
 		}
 		dest.writeInt(this.peopleChecks.size());
 		for (String s : peopleChecks.keySet()) {
 			boolean[] b = new boolean[1];
 			b[0] = peopleChecks.get(s);
 			dest.writeString(s);
 			dest.writeBooleanArray(b);
 		}
 		dest.writeInt(this.groupsChecks.size());
 		for (String s : groupsChecks.keySet()) {
 			boolean[] b = new boolean[1];
 			b[0] = groupsChecks.get(s);
 			dest.writeString(s);
 			dest.writeBooleanArray(b);
 		}
 		dest.writeInt(this.layersChecks.size());
 		for (String s : layersChecks.keySet()) {
 			boolean[] b = new boolean[1];
 			b[0] = layersChecks.get(s);
 			dest.writeString(s);
 			dest.writeBooleanArray(b);
 		}
 	}
 	
 }
