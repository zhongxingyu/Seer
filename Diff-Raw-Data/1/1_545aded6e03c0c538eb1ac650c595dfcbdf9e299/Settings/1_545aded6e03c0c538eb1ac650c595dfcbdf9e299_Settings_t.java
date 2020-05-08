 package tajmi;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 import scala.Tuple2;
 import tajmi.functional.instances.seq.Seq;
 import tajmi.functional.interfaces.Fun;
 import org.openscience.cdk.io.formats.IChemFormat;
 import org.openscience.cdk.io.formats.SMILESFormat;
 import tajmi.som.StatusUpdater.Verbosity;
 
 /**
  *
  * @author badi
  */
 public class Settings {
 
     public enum Variables {
 
         name,
         molecules_directories,
         molecule_names_file,
         molecule_names,
         write_cluster_mcss,
         cluster_mcss_directory,
         mcss_prefix,
         mcss_format,
 
         field_length,
         field_width,
 
         max_iterations,
 
         show_opengl_viewer,
 
         verbosity
     }
     Map<Variables, Tuple2<String, String>> keywords_and_types;
     Map<Variables, String> defaults;
     Map<Variables, Object> configuration;
     final String DEFAULT_ROOT_DIRECTORY = "input_data";
     final String ROOT = DEFAULT_ROOT_DIRECTORY + File.separator;
     final String DEFAULT_MCSS_DIRECTORY = ROOT + "mcss_files";
     private final String COMMENT_CHAR = "#";
     private final String ASSIGNMENT_CHAR = "=";
     private final String CONCAT_SEPARATOR = ":";
 
     public Settings(String config_file_path) throws FileNotFoundException, IOException, InvalidConfigurationType, Exception {
         keywords_and_types = new Hashtable<Settings.Variables, Tuple2<String, String>>(Variables.values().length);
         defaults = new Hashtable<Variables, String>(Variables.values().length);
 
         keywords_and_types.put(Variables.name, new Tuple2("name", "String"));
         defaults.put(Variables.name, "No name specified");
 
         keywords_and_types.put(Variables.molecules_directories, new Tuple2("molecules directories", "List<String>"));
         defaults.put(Variables.molecules_directories, ROOT + "molecules");
 
         keywords_and_types.put(Variables.molecule_names_file, new Tuple2("molecule names file", "String"));
         defaults.put(Variables.molecule_names_file, ROOT + "molecule-names.txt");
 
         keywords_and_types.put(Variables.write_cluster_mcss, new Tuple2("write cluster mcss", "Boolean"));
         defaults.put(Variables.write_cluster_mcss, "false");
 
         keywords_and_types.put(Variables.cluster_mcss_directory, new Tuple2("cluster mcss directory", "String"));
         defaults.put(Variables.cluster_mcss_directory, DEFAULT_MCSS_DIRECTORY);
 
         keywords_and_types.put(Variables.mcss_prefix, new Tuple2("mcss prefix", "String"));
         defaults.put(Variables.mcss_prefix, DEFAULT_MCSS_DIRECTORY + File.separator + "mcss_");
 
         keywords_and_types.put(Variables.mcss_format, new Tuple2("mcss format", "IChemFormat"));
         defaults.put(Variables.mcss_format, "smiles");
 
         keywords_and_types.put(Variables.field_length, new Tuple2("field length", "Integer"));
         defaults.put(Variables.field_length, "10");
 
         keywords_and_types.put(Variables.field_width, new Tuple2("field width", "Integer"));
         defaults.put(Variables.field_width, "10");
 
         keywords_and_types.put(Variables.show_opengl_viewer, new Tuple2("show opengl viewer", "Boolean"));
         defaults.put(Variables.show_opengl_viewer, "false");
 
         keywords_and_types.put(Variables.verbosity, new Tuple2("verbosity", "StatusUpdater.Verbosity"));
         defaults.put(Variables.verbosity, "Verbose");
 
         keywords_and_types.put(Variables.max_iterations, new Tuple2("max iterations", "Integer"));
         defaults.put(Variables.max_iterations, "1");
 
 
         List<String> config_lines = read_decommented_file(config_file_path);
         Map<Variables, Object> config = generate_configuration(keywords_and_types, config_lines);
         List<String> molecule_paths = build_molecule_paths(
                 config.get(Variables.molecules_directories),
                 config.get(Variables.molecule_names_file));
         config.put(Variables.molecule_names, molecule_paths);
 
         configuration = config;
     }
 
     private List<String> build_molecule_paths(Object directory_paths, Object names_list) throws IOException, FileNotFoundException, Exception {
         List<String> names =  read_decommented_file((String) names_list);
 
         List<String> molecules = new LinkedList<String>();
         for (String mname : (List<String>) names) {
             boolean found = false;
 
             for (String dir : (List<String>) directory_paths) {
                 File m = new File(dir + File.separator + mname);
                 if (m.exists()) {
                     found = true;
                     molecules.add(m.getAbsolutePath());
                     break;
                 }
             }
 
             if (!found) {
                 throw new RuntimeException("File [" + mname + "] not found in " + directory_paths);
             }
         }
 
         return molecules;
     }
 
     public Map<Variables, Object> get_configuration() {
         return configuration;
     }
 
     private List<String> read_decommented_file(String path) throws FileNotFoundException, IOException, Exception {
         List<String> lines = read_lines_as_rows(path);
         List<String> uncommented_lines = filter_comments_out(lines);
 
         return uncommented_lines;
     }
 
     private List<String> filter_comments_out(List<String> lines) {
         class NoComments implements Fun {
 
             List<String> lines;
             String line;
 
             public Fun copy() {
                 return new NoComments().curry(lines).curry(line);
             }
 
             public Fun curry(Object arg) {
                 if (lines == null) {
                     lines = (List<String>) arg;
                 } else if (line == null) {
                     line = (String) arg;
                 }
 
                 return this;
             }
 
             public List<String> call()  {
                 String l = line;
                 if (line.contains(COMMENT_CHAR)) {
                     l = line.substring(0, l.indexOf(COMMENT_CHAR));
                    l = l.trim();
                 }
                 if (l.length() > 0)
                     lines.add(l);
                 return lines;
             }
         }
 
         return (List<String>) Seq.fold(new NoComments(), new LinkedList(), lines);
     }
 
     private List<String> read_lines_as_rows(String path) throws IOException {
         BufferedReader reader = make_buffered_reader(path);
 
         List<String> lines = new LinkedList<String>();
 
         for (String line = reader.readLine(); line != null; line = reader.readLine()) {
             lines.add(line);
         }
 
         return lines;
     }
 
     private BufferedReader make_buffered_reader(String path) throws FileNotFoundException {
         return new BufferedReader(new FileReader(path));
     }
 
     private Map<Variables, Object> generate_configuration(Map<Variables, Tuple2<String, String>> keywords_types, List<String> lines) throws InvalidConfigurationType, InvalidFileFormat {
         Map<Variables, Object> config = new Hashtable(lines.size());
 
         for (Variables kw : keywords_types.keySet()) {
             String keyword = keywords_types.get(kw)._1();
             String kw_type = keywords_types.get(kw)._2();
 
             String val = get_value_for_kw(keyword, lines);
 
             if (val == null) {
                 val = defaults.get(kw);
             }
 
             if (kw_type.equals("String")) {
                 config.put(kw, val);
             } else if (kw_type.equals("Boolean")) {
                 config.put(kw, Boolean.parseBoolean(val));
             } else if (kw_type.equals("List<String>")) {
                 config.put(kw, split_by_concat_separator(val));
             } else if (kw_type.equals("IChemFormat")){
                 IChemFormat format = choose_file_format (val);
                 config.put(kw, format);
             } else if (kw_type.equals("Integer")) {
                 config.put(kw, Integer.parseInt(val));
             } else if (kw_type.equals("StatusUpdater.Verbosity")) {
                 config.put(kw, Verbosity.valueOf(val));
             } else {
                 throw new InvalidConfigurationType(keywords_types, kw_type);
             }
         }
 
         return config;
     }
 
     private IChemFormat choose_file_format (String description) throws InvalidFileFormat {
         IChemFormat format = null;
 
         if (description.equalsIgnoreCase("smiles"))
             format = (IChemFormat) SMILESFormat.getInstance();
         else throw new InvalidFileFormat(description);
 
         return format;
     }
 
     private List<String> split_by_concat_separator(String list) {
         List<String> dirs = new LinkedList<String>();
         for (String dir_path : list.split(CONCAT_SEPARATOR)) {
             dirs.add(dir_path);
         }
 
         return dirs;
     }
 
     private String get_value_for_kw(String keyword, List<String> lines) {
         String val = null;
         for (String line : lines) {
             if (line.startsWith(keyword)) {
                 Pattern pat = Pattern.compile("\\s*" + ASSIGNMENT_CHAR + "\\s*");
                 String[] bitsnpieces = pat.split(line);
                 val = bitsnpieces[1];
                 break;
             }
         }
         return val;
     }
 
     public class InvalidConfigurationType extends Exception {
 
         Map<Variables, Tuple2<String, String>> keywords_types;
         String bad_type;
 
         public InvalidConfigurationType(Map<Variables, Tuple2<String, String>> keywords_types, String bad_type) {
             super();
             this.keywords_types = keywords_types;
             this.bad_type = bad_type;
         }
 
         @Override
         public String getLocalizedMessage() {
             String msg = "Bad type specified [" + bad_type + "].\n" +
                     "The valid types are\n";
             for (Tuple2<String, String> kw_types : keywords_types.values()) {
                 msg += "\t" + kw_types._2() + "\n";
             }
 
             return msg;
         }
     }
 
     public class InvalidFileFormat extends Exception {
         String bad_format_name;
 
         public InvalidFileFormat (String format_name) {
             this.bad_format_name = format_name;
         }
 
         @Override
         public String getLocalizedMessage() {
             return "Unknown attempted format [" + bad_format_name + "]. Try [" + "smiles" + "]";
         }
 
 
     }
 }
