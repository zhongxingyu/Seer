 /**
  * Represents a Science Store.
  *
  * @author Eldar Damari, Ory Band
  */
 
 package company;
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.HashMap; 
 import java.util.ArrayList; 
 
 
 /**
  * Responsible for purchasing equipment, scientists and laboratories.
  */
 public class ScienceStore implements ScienceStoreInterface {
 
     private HashMap<String, ArrayList<EquipmentPackage>> equipmentPackages;
     private HashMap<String, ArrayList<Scientist>> scientists;
     private HashMap<String, ArrayList<Laboratory>> laboratories;
 
 
     /**
      * Besides initializing members,
      * sorts packages by amount, from largest to smallest,
      * and then by price, from cheapest to most expensive.
      * Scientists and labs are sorted by price the same way.
      */
     public ScienceStore( 
             HashMap<String, ArrayList<EquipmentPackage>> equipmentPackages,
             HashMap<String, ArrayList<Scientist>> scientists,
             HashMap<String, ArrayList<Laboratory>> laboratories) {
 
         this.equipmentPackages = equipmentPackages;
         this.scientists = scientists;
         this.laboratories = laboratories;
 
         // Sort equipment (by amount, then by price) from largest to smallest,
         // and sorts scientists & labs from cheapest to most expensive.
         //
         // See compareTo() implementation in corresponding classes.
         for (Map.Entry<String, ArrayList<EquipmentPackage>> entry
                 : this.equipmentPackages.entrySet()) {
 
             Collections.sort(entry.getValue());
             Collections.reverse(entry.getValue());
         }
 
         for (Map.Entry<String, ArrayList<Scientist>> entry :
                 this.scientists.entrySet()) {
 
             Collections.sort(entry.getValue());
         }
 
         for (Map.Entry<String, ArrayList<Laboratory>> entry :
                 this.laboratories.entrySet()) {
 
             Collections.sort(entry.getValue());
             Collections.reverse(entry.getValue());
         }
     }
 
 
     public synchronized void purchaseEquipmentPackages (
             Repository repository,
             Statistics statistics,
             Map<String, Integer> requestedEquipment) {
 
         // Iterate over each requested equipment type,
         // and search for the closest matching package size that overexceeds 
         // its size.
         EquipmentPackage equipmentPackage = null;
         for (Map.Entry<String, Integer> requestedEntry :
                 requestedEquipment.entrySet()) {
 
             String requestedType = requestedEntry.getKey();
             Integer requestedAmount = requestedEntry.getValue();
 
             ListIterator<EquipmentPackage> it =
                 this.equipmentPackages.get(requestedType).listIterator();
 
             // Search for the closest matching overexceeding package size.
             boolean isBiggerPackage = false,
                     isPackageTooSmall = false;
 
             // Search for bigger packages.
             while (it.hasNext() && ! isPackageTooSmall) {
                 equipmentPackage = it.next();
                 
                 if (equipmentPackage.getAmount() >= requestedAmount) {
                     isBiggerPackage = true;
                 } else {
                     isPackageTooSmall = true;
                 }
             }
 
             // Buy several small packages if there are no bigger ones.
             if ( ! isBiggerPackage ) {
 
                 if (it.hasPrevious()) {
                     it.previous();
 
                     // Purchase several small packages until we get our
                     // requested amount.
                     while (it.hasNext() && requestedAmount > 0) {
                         equipmentPackage = it.next();
 
                         requestedAmount -= equipmentPackage.getAmount();
                         this.PurchaseSingleEquipmentPackage(
                                 repository, statistics, equipmentPackage);
 
                         it.remove();  // Remove package from store.
                     }
 
                     // Special case where there aren't enough small packages to
                     // fill our request.
                     if (requestedAmount > 0) {
                        /*System.out.println(
                                 "Science Store: No matching package size for requested item '"
                                + requestedType + "'.");*/
                     }
                 // Special case where there aren't any packages at all.
                 } else {
                    /*System.out.println(
                             "Science Store: No packages for requested item '"
                            + requestedType + "'.");*/
                 }
             // If there's a big enough package, purchase it.
             } else {
                 EquipmentPackage e = it.previous();
 
                 // Get the last 'bigger' package if there was one.
                 if (isPackageTooSmall) {
                     e = it.previous();
                 }
 
                 this.PurchaseSingleEquipmentPackage(repository, statistics, e);
                 it.remove();  // Remove package from store
             }
         }
     }
 
 
     /**
      * Adds equipment package to repository, charges budget & updates statistics.
      * Note this function is synchronized to prevent bad statistics.
      *
      * @param repository for updating repository.
      * @param statistics for charging from the budget.
      * @param equipmentPackage shopping list {type : amount}
      */
     private synchronized void PurchaseSingleEquipmentPackage(
             Repository repository,
             Statistics statistics,
             EquipmentPackage equipmentPackage) {
 
         int amount = equipmentPackage.getAmount();
         String type = equipmentPackage.getType();
 
         statistics.addPurchasedEquipment(equipmentPackage);
         repository.addEquipmentToRepository(type, amount);
     }
 
 
     public synchronized void purchaseScientists (
             HeadOfLaboratory headOfLaboratory,
             Statistics statistics,
             Map<String, Integer> requestedScientists) {
 
         // Iterate over each requested specialization,
         // and search for the cheapest scientist of that specialization.
         Scientist scientist;
         for (Map.Entry<String, Integer> requestedEntry :
                 requestedScientists.entrySet()) {
 
             String requestedSpecialization = requestedEntry.getKey();
             Integer requestedAmount = requestedEntry.getValue();
 
             // Test is there are any scientist of requestd spec.
             boolean missing = false;
             if (this.scientists.containsKey(requestedSpecialization)
                 && this.scientists.get(
                     requestedSpecialization).size() >= requestedAmount) {
 
                 // Keep buying scientist from specialization until we get the
                 // amount we need.
                 Iterator<Scientist> it = this.scientists.get(
                         requestedSpecialization).iterator();
                 while (it.hasNext() && requestedAmount > 0) {
                     scientist = it.next();
 
                     statistics.addPurchasedScientist(scientist);
                     headOfLaboratory.addScientists(1);
 
                     requestedAmount --;
                     it.remove();  // Remove scientist from store.
                 }
 
                 // Flag in case there aren't enough scientists from this
                 // specialization in store.
                 if (requestedAmount > 0) {
                     missing = true;
                 }
             // In case there aren't any scientist in requesetd specialization.
             } else {
                 missing = true;
             }
 
             if (missing) {
                 System.out.println(
                     "Science Store: Not enough scientists for requested specialization '"
                     + requestedSpecialization + "'.");
             }
         }
     }
 
 
     public synchronized void purchaseLaboratory (
             ChiefScientist chiefScientist,
             Statistics statistics,
             String requestedSpecialization) {
 
         // Test if is there are is a laboratory from the specialization we need.
         if (this.laboratories.containsKey(requestedSpecialization)) {
             ArrayList<Laboratory> laboratories =
                 this.laboratories.get(requestedSpecialization);
 
             // Get first lab (most scientist, then cheapest).
             Laboratory laboratory = laboratories.get(0);
 
             statistics.addPurchasedLaboratory(laboratory);
 
             String name = laboratory.getName();
             String specialization = laboratory.getSpecialization();
             int numberOfScientists = laboratory.getNumOfScientists();
 
             // creating instance of head of laboratory to be used as
             // laboratory in chief.
             HeadOfLaboratory headOfLaboratory = new HeadOfLaboratory(
                     name, specialization, numberOfScientists);
 
             chiefScientist.addLaboratory(headOfLaboratory);
 
             laboratories.remove(0);  // Remove lab from store.
         } else {
             System.out.println(
                     "Science Store: No laboratory for requested specialization '"
                     + requestedSpecialization + "'.");
         }
     }
 
 
     public String toString() {
 
         StringBuilder result = new StringBuilder();
 
         String NEW_LINE = System.getProperty("line.separator");
 
         result.append("______________________________________" + NEW_LINE);
         result.append("         ---Science Store---:" + NEW_LINE);
 
         result.append("EquipmentPackages: " + NEW_LINE + this.equipmentPackages.values() + NEW_LINE);
         result.append("Scientists: " + NEW_LINE + this.scientists.values() + NEW_LINE);
         result.append("Laboratories: " + NEW_LINE + this.laboratories.values() + NEW_LINE);
 
         return result.toString();
     }
 }
