 package fr.cg95.cvq.business.request;
 
 import java.text.Normalizer;
 import java.util.Map;
 import java.util.Set;
 
 import fr.cg95.cvq.exception.CvqLocalReferentialException;
 import java.util.LinkedHashMap;
 
 /**
  * Define a type of local referential data, eg food diet for a school canteen registration request.
  * 
  * @author bor@zenexity.fr
  */
 public class LocalReferentialType {
 
     /**
      * Name of data pointed to by this type
      */
     private String name;
 
     /**
      * Label of this local referential type FIXME equals to the name?
      */
     private String label;
 
     /**
      * Map of all entries indexed by their key
      *  - useful to ensure key uniqueness in entries tree
      *  - useful to ease fetching of a particular entry in the tree
      */
     private Map<String, LocalReferentialEntry> keyEntriesMap = new LinkedHashMap<String, LocalReferentialEntry>();
     
     /**
      * The root entry of the entries tree
      */
     private LocalReferentialEntry rootEntry = new LocalReferentialEntry();
     
     /**
      * Indicate if this local referential type is used as a multiple choice
      */
     private boolean multiple;
 
 
     public final boolean isMultiple() {
         return multiple;
     }
 
     public final void setMultiple(boolean multiple) {
         this.multiple = multiple;
     }
 
     /**
      * Who manages this local referential type. Can be "CapDémat" or an external service label.
      */
    private String manager;
 
     public final String getManager() {
         return manager;
     }
 
     public final void setManager(String externalService) {
         this.manager = externalService;
     }
 
     public final String getLabel() {
         return label;
     }
 
     public final void setLabel(String label) {
         this.label = label;
     }
 
     public final String getName() {
         return name;
     }
     
     public final void setName(String dataName) {
         this.name = dataName;
     }
     
     /**
      * @return The top level entries of the tree
      */
     public final Set<LocalReferentialEntry> getEntries() {
         return rootEntry.getEntries();
     }
     
     /**
      * Add an entry and generates its key if needed (if the candidate entry’s key is null).
      * @param lre Entry to add
      * @param parentLre Parent entry or null if lre should a top level entry
      * @throws CvqLocalReferentialException If an entry with the same key is already in the tree
      */
     public final void addEntry(final LocalReferentialEntry lre, 
             final LocalReferentialEntry parentLre) throws CvqLocalReferentialException {
         if (lre.getKey() == null)
             lre.setKey(generateEntryKey(lre, parentLre));
         putKeyEntryIfUnique(lre);
         LocalReferentialEntry parent = parentLre == null ? rootEntry : parentLre;
         parent.addEntry(lre);
     }
     
     public final void removeEntry(final LocalReferentialEntry lre) {
         removeKeyEntries(lre.getEntries());
         keyEntriesMap.remove(lre.getKey());
         lre.getParent().removeEntry(lre);
     }
     
     /**
      * @param key
      * @return The entry corresponding to the key key, or null if this local referential type does not contain such entry
      */
     public LocalReferentialEntry getEntryByKey(final String key) {
         return keyEntriesMap.get(key);
     }
     
     /**
      * Save an entry in an internal map.
      * @param lre The entry to add
      * @throws CvqLocalReferentialException if an entry with the same key already exist 
      */
     private void putKeyEntryIfUnique(LocalReferentialEntry lre) throws CvqLocalReferentialException {
         if (keyEntriesMap.containsKey(lre.getKey()))
             throw new CvqLocalReferentialException("localReferential.error.entryAlreadyExists");
         keyEntriesMap.put(lre.getKey(), lre);
     }
     
     private void removeKeyEntries(Set<LocalReferentialEntry> entries) {
         if (entries != null) {
             for (LocalReferentialEntry lre : entries) {
                 removeKeyEntries(lre.getEntries());
                 keyEntriesMap.remove(lre.getKey());
             }
         }
     }
     
     // TODO - How to manage i18n and key generation policy
     private String generateEntryKey(LocalReferentialEntry lre, LocalReferentialEntry parentLre) {
         String suffixKey = Normalizer.normalize(lre.getLabel(), Normalizer.Form.NFD).replaceAll("[^\\w\\.]", "-").replace('_', '-');
         return (parentLre != null ? parentLre.getKey() + "-" : "") + suffixKey;
     }
 
     public Set<String> getEntriesKeys() {
         return keyEntriesMap.keySet();
     }
 }
