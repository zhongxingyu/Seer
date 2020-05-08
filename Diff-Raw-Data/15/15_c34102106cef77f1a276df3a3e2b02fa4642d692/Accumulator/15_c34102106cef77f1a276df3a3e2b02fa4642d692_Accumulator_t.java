 package net.forje.taskcoach.effortreport;
 
 import net.forje.taskcoach.types.Strings;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 public class Accumulator {
 
     private static final String TO_STRING_PATTERN = "%s : %d";
     private final String _name;
     private String _description;
     private Properties _properties = new Properties();
     private List _effortComments = new ArrayList();
 
     private int _accumulator = 0;
    private int _roundingFactor = 3;
 
     public Accumulator(String name) {
         _name = name;
     }
 
     public void addEffortComment (String comment) {
         _effortComments.add(comment);
     }
 
     public Iterator getEffortComments() {
         return _effortComments.iterator();
     }
 
     public void add(final int value) {
 
         int chunks = (value / _roundingFactor);
 
         long mod = value % _roundingFactor;
 
         if (mod > 0) {
             chunks++;
         }
 
         int rounded = chunks * _roundingFactor;
 
         _accumulator = _accumulator + rounded;
 
     }
 
     public String toString() {
         return String.format(TO_STRING_PATTERN, _name, _accumulator);
     }
 
     public String getName() {
         return _name;
     }
 
     public int getValue() {
         return _accumulator;
     }
 
     public String getDescription() {
         return _description;
     }
 
     public void setDescription(String description) {
 
         if (Strings.isEmpty(_description)) {
             _description = description;
         }
 
     }
 
     public Properties getProperties() {
         return _properties;
     }
 
     public void setProperties(final Properties properties) {
         _properties = properties;
     }

 }
