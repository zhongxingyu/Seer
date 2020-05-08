 package fr.crafter.tickleman.realplugin;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.lang.reflect.Field;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Level;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 //###################################################################################### RealConfig
 public class RealConfig
 {
 
 	private final String fileName;
 
 	public  boolean    debug = false;
 	public  String     language = "en"; 
 	public  String     permissionsPlugin = "none";
 	private JavaPlugin plugin;
 	public  boolean    pluginLog = false;
 
 	private Set<Field> volatileFields = new HashSet<Field>();
 
 	//---------------------------------------------------------------------------------------- Config
 	public RealConfig(final JavaPlugin plugin)
 	{
 		this(plugin, "config");
 	}
 
 	//---------------------------------------------------------------------------------------- Config
 	public RealConfig(final JavaPlugin plugin, String fileName)
 	{
 		this.plugin = plugin;
 		this.fileName = getPlugin().getDataFolder().getPath() + "/" + fileName + ".txt";
 	}
 
 	//---------------------------------------------------------------------------------------- Config
 	public RealConfig(final JavaPlugin plugin, String fileName, RealConfig mainConfig)
 	{
 		this(plugin, fileName);
 		copyFrom(mainConfig);
 		setVolatileFields(mainConfig.getClass());
 	}
 
 	//----------------------------------------------------------------------------------------- clone
 	private void copyFrom(RealConfig config)
 	{
 		for (Field field : getClass().getFields()) {
 			try {
 				field.set(this, field.get(config));
 			} catch (Exception e) {
 			}
 		}
 	}
 
 	//------------------------------------------------------------------------------------- getPlugin
 	protected JavaPlugin getPlugin()
 	{
 		return plugin;
 	}
 
 	//------------------------------------------------------------------------------------------ load
 	public RealConfig load()
 	{
 		try {
 			BufferedReader reader = new BufferedReader(new FileReader(fileName));
 			String buffer;
 			while ((buffer = reader.readLine()) != null) {
 				if (buffer.charAt(0) != '#') {
 					String[] line = buffer.split("=");
 					if (line.length >= 2) {
 						String key = line[0].trim();
 						String value = line[1].trim();
 						Field field = getClass().getField(key);
 						if ((field == null) || volatileFields.contains(field)) {
 							getPlugin().getServer().getLogger().log(
 								Level.WARNING, "[" + getPlugin().getDescription().getName() + "] "
 								+ " ignore configuration option " + key
 								+ " in " + fileName + " (unknown keyword)"
 							);
 						} else {
 							String fieldClass = field.getType().getName();
 							try {
 								if ((fieldClass.equals("boolean")) || (fieldClass.equals("java.lang.Boolean"))) {
 									field.set(this, RealVarTools.parseBoolean(value));
 								} else if ((fieldClass.equals("double")) || (fieldClass.equals("java.lang.Double"))) {
 									field.set(this, Double.parseDouble(value));
 								} else if ((fieldClass.equals("int")) || (fieldClass.equals("java.lang.Integer"))) {
 									field.set(this, Integer.parseInt(value));
 								} else {
 									field.set(this, value);
 								}
 							} catch (Exception e) {
 								getPlugin().getServer().getLogger().log(
 									Level.SEVERE, "[" + getPlugin().getDescription().getName() + "] "
 									+ " ignore configuration option " + key
 									+ " in " + fileName + " (" + e.getMessage() + ")"
 								);
 							}
 						}
 					}
 				}
 			}
 			reader.close();
 		} catch (Exception e) {
 			getPlugin().getServer().getLogger().log(
 				Level.WARNING, "[" + getPlugin().getDescription().getName() + "] "
 				+ " auto-create default " + fileName
 			);
 			save();
 		}
 		return this;
 	}
 
 	//------------------------------------------------------------------------------------------ save
 	public void save()
 	{
 		try {
 			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
 			for (Field field : getClass().getFields()) {
 				if (!volatileFields.contains(field)) {
 					String value = ((field.get(this) == null) ? "null" : field.get(this).toString());
 					writer.write(field.getName() + "=" + value + "\n");
 				}
 			}
 			writer.flush();
 			writer.close();
 		} catch (Exception e) {
 			getPlugin().getServer().getLogger().log(
 				Level.SEVERE, "[" + getPlugin().getDescription().getName() + "]"
 				+ " file save error " + fileName + " (" + e.getMessage() + ")"
 			);
 		}
 	}
 
 	//----------------------------------------------------------------------------- setVolatileFields
 	private void setVolatileFields(Class<?> applyClass)
 	{
 		volatileFields.clear();
 		for (Field field : applyClass.getFields()) {
 			volatileFields.add(field);
 		}
 	}
 
 }
