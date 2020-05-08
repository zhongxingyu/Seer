 package tools;
 
 import static utils.FindFiles.delete;
 import static utils.FindFiles.findFiles;
 import static utils.FindFiles.getFilter;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.yaml.snakeyaml.Yaml;
 
 import com.flagstone.translate.Profile;
 
 public class ActionscriptGenerator {
 
     private static final String RESOURCE_DIR =
     	"src/test/resources/profiles";
     private static final String REFERENCE_DIR =
     	"src/test/resources/actionscript/reference";
     private static final String SUITE_DIR =
     	"src/test/resources/actionscript/models";
     private static final String DEST_DIR =
     	"resources";
 
     private static final String PROFILE_OPT = "--profile";
     private static final String CLEAN_OPT = "--clean";
     private static final String PROFILES_ALL = "ALL";
 
     private static final String ACTIONSCRIPT = "actionscript";
     private static final String FLASH = "flash";
     private static final String PLAYER = "player";
     private static final String TYPE = "type";
 
     private static final String PROFILES = "profiles";
     private static final String IDENTIFIER = "id";
     private static final String REFID = "refid";
     private static final String ELEMENTS = "elements";
     private static final String PARAMETERS = "parameters";
     private static final String SCRIPT = "script";
     private static final String SKIP = "skip";
     private static final String FILE = "file";
 
     private static boolean clean = false;
 
     private static final List<Profile> profiles = new ArrayList<Profile>();
 
     private static final Map<String, List<String>> features =
     	new LinkedHashMap<String, List<String>>();
 
     public static void main(final String[] args) {
 
 		final File modelDir;
 
 		if (System.getProperty("test.suite") == null) {
 			modelDir = new File(SUITE_DIR);
 		} else {
 			modelDir = new File(System.getProperty("test.suite"));
 		}
 
 		setOptions(args);
     	loadReference(features, new File(REFERENCE_DIR));
 
     	if (clean) {
     		delete(new File(DEST_DIR).listFiles());
     	}
 
     	setup();
 
     	for (Profile profile : profiles) {
     		setupProfile(profile);
     		generateScripts(profile, modelDir);
     	}
     }
 
     private static void setOptions(final String[] options) {
 		for (int i = 0; i < options.length; i++) {
 			if (PROFILE_OPT.equals(options[i])) {
 				String name = options[++i].toUpperCase();
 
 				if (PROFILES_ALL.equals(name)) {
 					profiles.clear();
 					profiles.addAll(EnumSet.allOf(Profile.class));
					break;
 				} else {
 					Profile profile = Profile.fromName(name);
 					if (profile == null) {
 						throw new IllegalArgumentException(
 								"Unsupported profile: " + name);
 					}
 					profiles.add(profile);
 				}
 			} else if (CLEAN_OPT.equals(options[i])) {
 				clean = true;
 			} else {
 				throw new IllegalArgumentException(
 						"Unrecognised argument: " + options[i]);
 			}
 		}
     }
 
     @SuppressWarnings("unchecked")
     private static void loadReference(final Map<String, List<String>> table,
     		final File dir) {
 		Yaml yaml = new Yaml();
 		List<String> files = new ArrayList<String>();
 		findFiles(files, dir, getFilter(".yaml"));
 
 		FileInputStream stream = null;
 		Map<String,Object> map;
 
 		for (String yamlFile : files) {
 			try {
 		        stream = new FileInputStream(yamlFile);
 		        for (Object entry : (List<Object>)yaml.load(stream)) {
 		        	map = (Map<String,Object>) entry;
 		        	if (map.containsKey(ELEMENTS)) {
 		        		for (Object element : (List<Object>) map.get(ELEMENTS)) {
 			        		loadProfile(table, (Map<String,Object>) element);
 		        		}
 		        	} else {
 		        		loadProfile(table, (Map<String,Object>) entry);
 		        	}
 		        }
 		        stream.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 				System.err.println(yamlFile + ": " + e.getMessage());
 			}
 		}
     }
 
     @SuppressWarnings("unchecked")
     private static void loadProfile(final Map<String, List<String>> profiles,
     		final Map<String,Object> entry) {
     	if (!entry.containsKey(PROFILES)) {
     		throw new IllegalArgumentException(
     				"Missing profiles: " + entry.toString());
     	}
 
     	profiles.put((String)entry.get(IDENTIFIER),
     			(List<String>)entry.get(PROFILES));
     }
 
     @SuppressWarnings("unchecked")
     private static void generateScripts(final Profile profile, final File dir) {
 
 		Yaml yaml = new Yaml();
 		List<String> files = new ArrayList<String>();
 		findFiles(files, dir, getFilter(".yaml"));
 
 		FileInputStream stream = null;
 
 		for (String yamlFile : files) {
 			try {
 		        stream = new FileInputStream(yamlFile);
 		        for (Object tests : (List<Object>)yaml.load(stream)) {
 		        	generateScript(profile, (Map<String,Object>) tests);
 		        }
 		        stream.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 				System.err.println(yamlFile + ": " + e.getMessage());
 			}
 		}
     }
 
     private static void setup() {
 	    final String[] TYPES = { "frame", "button", "movieclip" };
 
 	    try {
     		copyfile(new File(RESOURCE_DIR, "publish.jsfl"),
     				new File(DEST_DIR, "publish.jsfl"));
 
         	for (String type : TYPES) {
         		copyfile(new File(RESOURCE_DIR, type + ".fla"),
         				new File(DEST_DIR, type + ".fla"));
         	}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     }
 
     private static void setupProfile(Profile profile) {
 		String publish;
 	    File dir;
 	    File file;
 
 	    try {
 			File publishFile = new File(RESOURCE_DIR, "publish.xml");
 			Map<String,Object>map = new LinkedHashMap<String, Object>();
             map.put(ACTIONSCRIPT, profile.getScriptVersion());
             map.put(FLASH, profile.getFlashVersion());
             map.put(PLAYER, profile.getPlayer());
 
             publish = contentsOfFile(publishFile);
         	publish = replaceTokens(map, publish);
 
     		dir = new File(DEST_DIR, profile.name());
     		if (!dir.exists() && !dir.mkdirs()) {
     			throw new IOException("Cannot create directory: " + dir.getPath());
     		}
 
     		file = new File(dir, "publish.xml");
     		writeScript(file, publish);
 
 	    } catch (Exception e) {
 			e.printStackTrace();
 		}
     }
 
     @SuppressWarnings("unchecked")
     private static void generateScript(final Profile profile,
     		final Map<String,Object>list)
     		throws IOException {
         String script = (String)list.get(SCRIPT);
         String reference = (String)list.get(REFID);
 
         if (reference == null) {
         	throw new IOException("No reference defined in test.");
         }
 
         List<String> versions = features.get(reference);
 
         if (versions == null) {
         	throw new IOException("No profiles defined for: " + reference);
         }
 
         List<Object> parameters = (List<Object>)list.get(PARAMETERS);
 
         String path;
         String type;
 
         if (list.containsKey(FILE)) {
         	path = (String)list.get(FILE);
         } else if (list.containsKey(REFID)) {
         	path = reference + ".as";
         } else {
         	throw new IllegalArgumentException("No file specified");
         }
 
         if (script.startsWith("onClipEvent")) {
         	type = "movieclip";
         } else if (script.startsWith("on")) {
         	type = "button";
         } else {
         	type = "frame";
         }
 
         File dir;
         File file;
         int index;
 
         Map<String, Object> vars;
 
         if (versions.contains(profile.name())) {
         	dir = dirForTest(profile.name(), type);
 	        index = 0;
 
         	if (parameters == null) {
             	if (!list.containsKey(SKIP)) {
                 	writeScript(new File(dir, path), script);
             	}
         	} else {
             	for (Object set : parameters) {
                 	vars = (Map<String, Object>)set;
                 	if (vars.containsKey(SKIP)) {
                 		continue;
                 	}
             		if (vars.containsKey(FILE)) {
 	                	file = new File(dir, (String)vars.get(FILE));
             		} else if (vars.containsKey(REFID)) {
 	                	file = new File(dir, (String)vars.get(REFID) + ".as");
             		} else {
 				        file = new File(dir, String.format(path, index++));
             		}
                 	script = replaceTokens(vars, script);
                 	writeScript(file, script);
             	}
         	}
         }
     }
 
     private static File dirForTest(String name, String type)
     		throws IOException {
     	Profile profile = Profile.fromName(name);
 
     	if (profile == null) {
     		throw new IOException("Invalid profile name: " + name);
     	}
 
 		String path = String.format("%s/%s", name, type);
 		File dir = new File(DEST_DIR, path);
 
 		if (!dir.exists() && !dir.mkdirs()) {
 			throw new IOException("Cannot create directory: " + dir.getPath());
 		}
 		return dir;
     }
 
 	private static String replaceTokens(final Map<String,Object>values,
 			final String script) {
 		String str = script;
     	for (Map.Entry<String, Object> set: values.entrySet()) {
     		str = str.replaceAll("%"+set.getKey()+"%",
     				set.getValue().toString());
     	}
     	return str;
 	}
 
 	private static void writeScript(final File file, final String script)
 	   		throws IOException {
 		File dir = file.getParentFile();
 
 		if (!dir.exists() && !dir.mkdirs()) {
 			throw new IOException("Cannot create directory: " + dir.getPath());
 		}
 		PrintWriter writer = new PrintWriter(file);
 		writer.write(script);
 		writer.flush();
 		writer.close();
 	}
 
     private static String contentsOfFile(File file)
     		throws FileNotFoundException, IOException {
         String script = "";
         byte[] fileIn = new byte[(int)file.length()];
         FileInputStream fileContents = new FileInputStream(file);
         fileContents.read(fileIn);
         script = new String(fileIn);
         fileContents.close();
         return script;
     }
 
     private static void copyfile(File src, File dest) throws IOException {
         InputStream in = new FileInputStream(src);
         OutputStream out = new FileOutputStream(dest);
 
         byte[] buf = new byte[1024];
 	    int len;
 	    while ((len = in.read(buf)) > 0){
 	        out.write(buf, 0, len);
 	    }
 	    in.close();
 	    out.close();
     }
 }
