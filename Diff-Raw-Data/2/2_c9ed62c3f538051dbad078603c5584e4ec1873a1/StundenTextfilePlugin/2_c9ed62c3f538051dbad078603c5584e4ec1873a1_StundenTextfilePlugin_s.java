 package nu.wasis.stunden.plugins.textfile;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.xeoh.plugins.base.annotations.PluginImplementation;
 import nu.wasis.stunden.exception.InvalidConfigurationException;
 import nu.wasis.stunden.model.Day;
 import nu.wasis.stunden.model.Entry;
 import nu.wasis.stunden.model.Project;
 import nu.wasis.stunden.model.WorkPeriod;
 import nu.wasis.stunden.plugin.InputPlugin;
 import nu.wasis.stunden.plugins.textfile.config.StundenTextfilePluginConfig;
 import nu.wasis.stunden.plugins.textfile.exception.InvalidEntryException;
 import nu.wasis.stunden.plugins.textfile.exception.InvalidFilenameException;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.io.filefilter.TrueFileFilter;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.joda.time.MutableDateTime;
 
 import com.joestelmach.natty.DateGroup;
 import com.joestelmach.natty.Parser;
 
 /**
  * Reads {@link WorkPeriod}s from simple text files - recursively of pointed to
  * a directory.
  * <p>
  * All entries in the file(s) must contain a parsable date in the file name and
  * have this format:
  * <pre>
  * hh:mm - hh:mm: Description # Comment
  * </pre>
  * The comment is optional.
  * </p>
  * <p>
  * For options, see {@link StundenTextfilePluginConfig}.
  * </p>
  */
 @PluginImplementation
 public class StundenTextfilePlugin implements InputPlugin {
 
     private static final Logger LOG = Logger.getLogger(StundenTextfilePlugin.class);
 
     /**
      * Reads the text files specified in the configuration.
      * 
      * @param configuration The configuration to use. Must be of type
      *        {@link StundenTextfilePluginConfig}. Configuration parameter
      *        `readFrom' must point to a file or directory.
      */
     @Override
     public WorkPeriod read(final Object configuration) {
     	if (null == configuration || !(configuration instanceof StundenTextfilePluginConfig)) {
 			throw new InvalidConfigurationException("Configuration null or wrong type. You probably need to fix your configuration file.");
 		}
         final StundenTextfilePluginConfig myConfig = (StundenTextfilePluginConfig) configuration;
         final String readFrom = myConfig.getReadFrom();
         if (null == readFrom) {
             throw new InvalidConfigurationException("Config must contain `readFrom' param.");
         }
         final File inputFile = new File(readFrom);
         if (!inputFile.exists()) {
             throw new InvalidConfigurationException("Arg `readFrom' must be an existing file or directory.");
         }
         final boolean stripProjectNamesOnEqualitySign = myConfig.getStripProjectNamesOnEqualitySign();
 
         LOG.info("Parsing `" + readFrom + "' ...");
 
         WorkPeriod workPeriod = null;
 
         try {
             if (inputFile.isDirectory()) {
                 workPeriod = readDirectory(inputFile, stripProjectNamesOnEqualitySign, myConfig.getNightLimit());
             } else {
                 workPeriod = readSingleFile(inputFile, stripProjectNamesOnEqualitySign, myConfig.getNightLimit());
             }
         } catch (final IOException e) {
             throw new RuntimeException("Something went horribly wrong.", e);
         }
         LOG.info("... done.");
         return workPeriod;
     }
 
     private WorkPeriod readDirectory(final File directory, final boolean stripProjectNamesOnEqualitySign, final int nightLimit) throws FileNotFoundException, IOException {
         WorkPeriod workPeriod = null;
         for (final File file : FileUtils.listFiles(directory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
             if (null == workPeriod) {
                 workPeriod = readSingleFile(file, stripProjectNamesOnEqualitySign, nightLimit);
             } else {
                 workPeriod.addAll(readSingleFile(file, stripProjectNamesOnEqualitySign, nightLimit));
             }
         }
         return workPeriod;
     }
 
     private WorkPeriod readSingleFile(final File file, final boolean stripProjectNamesOnEqualitySign, final int nightLimit) throws FileNotFoundException, IOException {
         LOG.debug("Parsing file `" + file.getAbsolutePath() + "'");
         final DateTime date = getDate(file);
         final WorkPeriod workPeriod = new WorkPeriod();
         final Day day = parseContent(date, file, stripProjectNamesOnEqualitySign, nightLimit);
         workPeriod.getDays().add(day);
         return workPeriod;
     }
 
     private DateTime getDate(final File file) {
         if (!file.isFile()) {
             throw new RuntimeException("Can only get dates from files, not directories :D - but I smile.");
         }
         final Parser dateParser = new Parser();
         final List<DateGroup> dateGroups = dateParser.parse(FilenameUtils.removeExtension(file.getName()));
         if (dateGroups.size() != 1) {
             throw new InvalidFilenameException("Filename " + file.getName() + " contains no or ambigous date(s).");
         }
         final DateGroup dateGroup = dateGroups.get(0);
         if (dateGroup.getDates().size() != 1) {
             throw new InvalidFilenameException("Filename " + file.getName() + " contains no or ambigous date(s).");
         }
         return new DateTime(dateGroup.getDates().get(0));
     }
 
     private Day parseContent(final DateTime date, final File file, final boolean stripProjectNamesOnEqualitySign, final int nightLimit) throws FileNotFoundException, IOException {
         @SuppressWarnings("resource")
 		UnicodeBOMInputStream unicodeBOMInputStream = new UnicodeBOMInputStream(new FileInputStream(file));
 		final List<String> lines = IOUtils.readLines(new InputStreamReader(unicodeBOMInputStream.skipBOM(), "UTF8"));
         final List<Entry> entries = new LinkedList<>();
 
         for (final String line : lines) {
         	if (line.startsWith("#") || line.isEmpty()) {
         		continue;
         	}
             if (line.length() < 15) {
                 // compare:
                 // 0123456789012345
                 // 10:00 - 10:45: Intern
                 throw new InvalidEntryException("Magic tells me that the line `" + line + "' in file `" + file + "' is not cool.");
             }
             try {
                 final String beginHourString = StringUtils.stripStart(line.substring(0, 2), "0");
 				int beginHour = 0;
 				try {
 					beginHour = Integer.parseInt(beginHourString);
 				} catch (final NumberFormatException e) {
 	                throw new InvalidEntryException("Unable to parse line `" + line + "' of file `" + file + "'. => invalid starting hour of `" + beginHourString + "'.", e);
 	            } 
                 int beginMinutes = 0;
                 final String beginMinutesString = line.substring(3, 5);
                 if (!"00".equals(beginMinutesString)) {
                 	beginMinutes = Integer.parseInt(beginMinutesString);
                 }
                final int endHour = Integer.parseInt(StringUtils.stripStart(line.substring(8, 10), "0"));
                 int endMinutes = 0;
                 final String endMinutesString = line.substring(11, 13);
 				if (!"00".equals(endMinutesString)) {
 					endMinutes = Integer.parseInt(endMinutesString);
 				}
 				String projectName = null;
 				int stopAt = -1;
 				if (stripProjectNamesOnEqualitySign) {
 					stopAt = line.indexOf("=");
 				}
 				if (-1 != stopAt) {
 					projectName = line.substring(15, stopAt).trim();
 				} else {
 					projectName = line.substring(15).trim();
 				}
 				
                 final MutableDateTime begin = new MutableDateTime(date);
                 begin.setHourOfDay(beginHour);
                 begin.setMinuteOfHour(beginMinutes);
                 final MutableDateTime end = new MutableDateTime(date);
                 end.setHourOfDay(endHour);
                 end.setMinuteOfHour(endMinutes);
                 if (endHour <= nightLimit) {
                 	LOG.debug("You seem to have worked overnight. Adjusting end time :)");
                 	end.addDays(1);
                 }
                 entries.add(new Entry(begin.toDateTime(), end.toDateTime(), new Project(projectName), false));
             } catch (final NumberFormatException e) {
                 throw new InvalidEntryException("Unable to parse line `" + line + "' of file `" + file + "'.", e);
             }
         }
 
         return new Day(date, entries);
     }
 
     @Override
     public Class<StundenTextfilePluginConfig> getConfigurationClass() {
         return StundenTextfilePluginConfig.class;
     }
 }
