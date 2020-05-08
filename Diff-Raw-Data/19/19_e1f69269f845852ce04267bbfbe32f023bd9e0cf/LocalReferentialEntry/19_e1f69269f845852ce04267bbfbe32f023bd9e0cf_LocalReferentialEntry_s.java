 package fr.cg95.cvq.business.authority;
 
 import java.util.Map;
 import java.util.Set;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 
 public class LocalReferentialEntry {
 
     /**
      * Entry unique key.
      */
     private String key;
 
     /**
      * Map of labels keyed by their two-letters language code.
      */
     private Map<String, String> labelsMap;
 
     /**
      * Map of precisions fields for users keyed by their two-letters language code.
      */
     private Map<String, Map<String, String>> precisionsMap;
 
     /**
      * Map of additional messages keyed by their two-letters language code.
      */
     private Map<String, String> messagesMap;
 
     /**
      * Children entries.
      */
     private Set<LocalReferentialEntry> entries;
 
     private boolean entriesSupportPriority;
     private boolean entriesSupportPrecision;
     private boolean entriesSupportMultiple;
 
     public final Set<LocalReferentialEntry> getEntries() {
         return entries;
     }
 
     public final void setEntries(Set<LocalReferentialEntry> entries) {
         this.entries = entries;
     }
 
     public final void addEntry(final LocalReferentialEntry lre) {
         if (entries == null)
             entries = new LinkedHashSet<LocalReferentialEntry>();
         entries.add(lre);
     }
     
     public final void removeEntry(final LocalReferentialEntry lre) {
         if (entries == null)
             return;
         entries.remove(lre);
     }
 
     public final boolean getEntriesSupportPrecision() {
         return entriesSupportPrecision;
     }
 
     public final void setEntriesSupportPrecision(boolean entriesSupportPrecision) {
         this.entriesSupportPrecision = entriesSupportPrecision;
     }
 
     public final boolean getEntriesSupportPriority() {
         return entriesSupportPriority;
     }
 
     public final void setEntriesSupportPriority(boolean entriesSupportPriority) {
         this.entriesSupportPriority = entriesSupportPriority;
     }
 
     public final boolean getEntriesSupportMultiple() {
         return entriesSupportMultiple;
     }
 
     public final void setEntriesSupportMultiple(boolean entriesSupportMultiple) {
         this.entriesSupportMultiple = entriesSupportMultiple;
     }
 
     public final String getKey() {
         return key;
     }
     public final void setKey(String key) {
         this.key = key;
     }
 
     public final Map<String,String> getLabelsMap() {
         return labelsMap;
     }
 
     public final void addLabel(final String lang, final String value) {
         if (labelsMap == null)
             labelsMap = new LinkedHashMap<String, String>();
         labelsMap.put(lang, value);
     }
 
     public final Map<String,String> getMessagesMap() {
         return messagesMap;
     }
 
     public final void addMessage(final String lang, final String value) {
         if (messagesMap == null)
             messagesMap = new LinkedHashMap<String, String>();
         messagesMap.put(lang, value);
     }
 
     public final Map<String, Map<String, String>> getPrecisionsMap() {
         return precisionsMap;
     }
 
     public final void addPrecision(final String key, final Map<String, String> labelsMap) {
         if (precisionsMap == null)
             precisionsMap = new LinkedHashMap<String, Map<String, String>>();
         precisionsMap.put(key, labelsMap);
     }
     
     public final void addLangage(String lang) {
         addLabel(lang, "");
         addMessage(lang, "");
     }
 }
