 package sf.pnr.base;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  */
 public class ConfigurationTest {
 
    public static void main(String[] args) throws IOException {
         if (args.length != 1) {
             throw new IllegalArgumentException("Config directory expected");
         }
 
         final List<String> testFiles = new ArrayList<String>();
         testFiles.add("pos.epd");
         testFiles.add("best7.epd");
         testFiles.add("wnperm.epd");
         testFiles.add("qtest_easy.epd");
         testFiles.add("en passant.epd");
         testFiles.add("ans.epd");
 
         final File[] files = new File(args[0]).listFiles(new FileFilter() {
             @Override
             public boolean accept(final File file) {
                 return file.isFile() && file.getName().endsWith(".ini");
             }
         });
 
         final Map<String, String> defaults = new HashMap<String, String>();
         for (Configurable.Key key: Configurable.Key.values()) {
             final String value = Configuration.getInstance().getString(key);
             defaults.put(StringUtils.toUciOption(key), value);
         }
         final Map<String, String> postSearchOptions = new LinkedHashMap<String, String>();
 //        postSearchOptions.put("Command", "releaseEngine");
 
 //        final Properties defaultValues = new Properties();
 //        for (Configurable.Key key: Configurable.Key.values()) {
 //            final String value = Configuration.getInstance().getString(key);
 //            defaultValues.setProperty(key.getKey(), value);
 //        }
 //        defaultValues.store(new FileWriter("defaults.ini"), "Default values");
 
         final List<UciRunner> runners = new ArrayList<UciRunner>(files.length);
         for (File file: files) {
             final Properties properties = new Properties();
            final FileReader reader = new FileReader(file.getName());
             try {
                 properties.load(reader);
             } finally {
                 reader.close();
             }
             final Map<Configurable.Key, String> configs = Configuration.preprocess(properties);
             final Map<String, String> uciOptions = new HashMap<String, String>(defaults);
             for (Map.Entry<Configurable.Key, String> entry: configs.entrySet()) {
                 uciOptions.put(StringUtils.toUciOption(entry.getKey()), entry.getValue());
             }
             runners.add(new UciRunner(
                 "Pawns N' Roses - " + file.getName(), uciOptions, postSearchOptions, new PipedUciProcess()));
         }
         try {
             new EpdProcessor().process(testFiles, new MultiEngineSearchTask(runners, 6, 0, 20));
         } finally {
             for (UciRunner runner : runners) {
                 runner.close();
             }
         }
     }
 }
