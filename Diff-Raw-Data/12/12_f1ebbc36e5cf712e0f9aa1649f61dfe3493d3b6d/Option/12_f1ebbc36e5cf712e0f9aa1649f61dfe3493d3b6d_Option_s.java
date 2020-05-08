 package ca.celticminstrel.cookbook;
 
 import java.util.List;
 
 import org.bukkit.Material;
 import org.bukkit.util.config.Configuration;
 
 public abstract class Option {
 	public static OptionBoolean PERMISSIONS_BY_RESULT = new OptionBoolean("permissions-by-result", false);
 	public static OptionInteger VIEW_WAND = new OptionInteger("wands.view", Material.BOOK.getId());
 	public static OptionInteger SHAPED_WAND = new OptionInteger("wands.shaped", Material.IRON_SWORD.getId());
 	public static OptionInteger SHAPELESS_WAND = new OptionInteger("wands.shapeless", Material.BUCKET.getId());
 	public static OptionInteger SMELT_WAND = new OptionInteger("wands.smelt", Material.COAL.getId());
 	protected String node;
 	protected Object def;
 	protected static Configuration config;
 	
 	@SuppressWarnings("hiding")
 	protected Option(String node, Object def) {
 		this.node = node;
 		this.def = def;
 	}
 	
 	public abstract Object get();
 	
 	public void set(Object value) {
 		config.setProperty(node, value);
 	}
 	
 	public void reset() {
 		set(def);
 	}
 	
 	public static void setConfiguration(Configuration c) {
 		config = c;
 	}
 	
 	public static class OptionBoolean extends Option {
 		@SuppressWarnings("hiding") OptionBoolean(String node, boolean def) {
 			super(node, def);
 		}
 
 		@Override
 		public Boolean get() {
 			return config.getBoolean(node, (Boolean) def);
 		}
 	}
 
 	public static class OptionString extends Option {
 		@SuppressWarnings("hiding") OptionString(String node, String def) {
 			super(node, def);
 		}
 
 		@Override
 		public String get() {
 			return config.getString(node, (String) def);
 		}
 	}
 
 	public static class OptionInteger extends Option {
 		@SuppressWarnings("hiding") OptionInteger(String node, int def) {
 			super(node, def);
 		}
 
 		@Override
 		public Integer get() {
 			return config.getInt(node, (Integer) def);
 		}
 	}
 
 	public static class OptionDouble extends Option {
 		@SuppressWarnings("hiding") OptionDouble(String node, double def) {
 			super(node, def);
 		}
 
 		@Override
 		public Double get() {
 			return config.getDouble(node, (Double) def);
 		}
 	}
 
 	public static class OptionStringList extends Option {
 		@SuppressWarnings("hiding") OptionStringList(String node, List<String> def) {
 			super(node, def);
 		}
 
 		@Override@SuppressWarnings("unchecked")
 		public List<String> get() {
 			return config.getStringList(node, (List<String>) def);
 		}
 	}
 
 	public static class OptionIntegerList extends Option {
 		@SuppressWarnings("hiding") OptionIntegerList(String node, List<Integer> def) {
 			super(node, def);
 		}
 
 		@Override@SuppressWarnings("unchecked")
 		public List<Integer> get() {
 			return config.getIntList(node, (List<Integer>) def);
 		}
 	}
 }
