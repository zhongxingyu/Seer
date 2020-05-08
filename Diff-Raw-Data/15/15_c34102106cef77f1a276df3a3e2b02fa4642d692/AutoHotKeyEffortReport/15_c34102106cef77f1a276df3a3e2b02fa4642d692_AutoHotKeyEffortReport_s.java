 package net.forje.taskcoach.effortreport;
 
 import net.forje.taskcoach.types.Duration;
 import net.forje.taskcoach.types.DurationFormat;
 import net.forje.taskcoach.types.Strings;
 
 import java.io.File;
 import java.io.PrintStream;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Properties;
 
 public class AutoHotKeyEffortReport
         extends AbstractEffortReport
         implements FileProvider {
 
     public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
 
     protected static final String CLIENT = "client";
     protected static final String PROJECT = "project";
     protected static final String PHASE = "phase";
     protected static final String ACTIVITY = "activity";
     protected static final String TASK = "task";
     protected static final String DETAILS = "details";
 
     public AutoHotKeyEffortReport(final TaskVisitor visitor) {
         super(visitor);
     }
 
     public File getFile(final Date date) {
 
         final String dateString = DATE_FORMAT.format(date);
         return new File(".", "taskcoach_" + dateString + ".ahks");
 
     }
 
     public void print(final PrintStream out) {
 
         verifyData();
         prepareReport(out);
 
     }
 
     private void verifyData() {
         final Iterator iterator = Accumulators.accumulators();
 
         Accumulator accumulator;
         String description;
         String name;
         DescriptionParser descriptionParser;
 
         while (iterator.hasNext()) {
 
             accumulator = (Accumulator) iterator.next();
             name = accumulator.getName();
 
             final Properties properties = accumulator.getProperties();
 
         }
 
     }
 
     private void prepareReport(final PrintStream out) {
 
         final DurationFormat format = DurationFormat.getDecimalFormat();
 
         final Iterator iterator = Accumulators.accumulators();
 
         while (iterator.hasNext()) {
 
             Accumulator accumulator = (Accumulator) iterator.next();
 
             final String name = accumulator.getName();
             final int value = accumulator.getValue();
 
             final int i = name.lastIndexOf(">>");
             String substring = name.substring(i + 2).trim();
             final String[] split = substring.split(" ");
             substring = split[0].trim();
 
             final Duration duration = new Duration(value);
 
             final Properties properties = accumulator.getProperties();
 
            final String client = properties.getProperty(PropertyNames.Customer.toString());
             final String project = properties.getProperty(PropertyNames.Project.toString());
             final String phase = properties.getProperty(PropertyNames.Phase.toString());
             final String activity = properties.getProperty(PropertyNames.Activity.toString());
             final String task = properties.getProperty(PropertyNames.Task.toString());
             final String details = properties.getProperty(PropertyNames.Details.toString());
 
             ArrayList missing = new ArrayList(5);
 
             checkProperties(properties, missing);
 
             if (missing.size() > 0) {
                 System.out.println("task [" + name + "] missing the following properties [" + missing.toString() + "]");
             } else {
 
                 out.println("{TAB}");
                 out.println("{TAB}");
                 out.println(format.format(duration));
                 out.println("{TAB}");
                 out.println(client);
                 out.println("{TAB}");
                 out.println(project);
                 out.println("{TAB}");
                 out.println(phase);
                 out.println("{TAB}");
                 out.println(activity);
                 out.println("{TAB}");
                 if (!Strings.isEmpty(task)) {
                     out.println(task);
                 }
                 out.println("{TAB}");
                 if (!Strings.isEmpty(details)) {
                     out.println(details);
                 }
                 out.println("{TAB}");
                 out.println("!f");
                 out.println("BREAK");
                 out.println("BREAK");
                 out.println("!n");
                 out.println("BREAK");
 
             }
         }
 
         out.println("BREAK");
         out.println("!c");
        
     }
 
     private void checkProperties(final Properties properties, final ArrayList missing) {
         checkProperty(properties, missing, CLIENT);
         checkProperty(properties, missing, PROJECT);
         checkProperty(properties, missing, PHASE);
         checkProperty(properties, missing, ACTIVITY);
     }
 
     private void checkProperty(final Properties properties,
                                final ArrayList missing,
                                final String property) {
         if (Strings.isEmpty(properties.getProperty(property))) {
             missing.add(property);
         }
     }
 
 }
