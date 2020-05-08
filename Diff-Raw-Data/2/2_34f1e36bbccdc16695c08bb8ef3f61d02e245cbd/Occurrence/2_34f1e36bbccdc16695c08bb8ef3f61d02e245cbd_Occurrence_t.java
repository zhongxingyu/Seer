 package net.personaltt.model;
 
 import net.personaltt.utils.BaseIntervalsSet;
 
 /**
  * Occurrence represents definition of one time quantum in the time axis. It means its 
  * time domain and ID. Current allocation will be held separately
  * @author docx
  */
 public class Occurrence {
     
     /**
      * Intervals set representing occurrence's domain of what area on time axis it
      * can cover
      */
     BaseIntervalsSet<Integer> domain;
     
     /*
      * Lower bound of duration interval
      */
     int minDuration;
     
     /**
      * Upper bound of duration interval
      */
     int maxDuration;
     
     /**
      * ID of occurrence used to associate with other parts of algorithm and client
      */
     int id;
     
     /**
      * Reference to assigned allocation
      */
     OccurrenceAllocation allocation;
     
     /**
      * Preferred starting point
      */
     int preferredStart;
     
     /**
      * Priority of perturbing value of this occurrence. 
      * Greater value means higher priority of keeping unchanged.
      */
     int perturbationPriority = 0;
     
     /**
      * 
      * @param domain
      * @param minDuration
      * @param maxDuration
      * @param id 
      */
     public Occurrence(BaseIntervalsSet<Integer> domain, int minDuration, int maxDuration, int id) {
         this.domain = domain;
         this.minDuration = minDuration;
         this.maxDuration = maxDuration;
         this.id = id;
     }
 
     /**
      * Occurrences are equal iff their ids are equal
      * @param obj
      * @return 
      */
     @Override
     public boolean equals(Object obj) {
         if (obj instanceof Occurrence) {
             return this.id == ((Occurrence)(obj)).id;
         }
         
         return super.equals(obj);
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 53 * hash + this.id;
         return hash;
     }
     
     /**
      * Domain of occurrence. Set of intervals where can be occurrence allocated
      * @return 
      */
     public BaseIntervalsSet<Integer> getDomain() {
         return domain;
     }
 
     /**
      * Maximum duration of allocation of occurrence.
      * @return 
      */
     public int getMaxDuration() {
         return maxDuration;
     }
 
     /**
      * Minimum duration of allocation of occurrence
      * @return 
      */
     public int getMinDuration() {
         return minDuration;
     }
 
     @Override
     public String toString() {
         return String.valueOf(id);
     }
     
     /**
      * ID of occurrence.
      * @return 
      */
     public int getId() {
         return id;
     }
 
     /**
      * Shallow clone of occurrence. Since all fields of occurrence are atomic 
      * their are copied. Only domain is referenced to source. Allocation is not copied.
      * @return 
      */
     @Override
     public Object clone() {
         Occurrence o = new Occurrence(domain, minDuration, maxDuration, id);
         o.perturbationPriority = this.perturbationPriority;
         o.preferredStart = this.preferredStart;
         return o;
     }
 
     public OccurrenceAllocation getAllocation() {
         return allocation;
     }
 
     public void setAllocation(OccurrenceAllocation allocation) {
         this.allocation = allocation;
     }
     
     public void setInitialAllocation(OccurrenceAllocation allocation) {
         this.allocation = allocation;
         this.preferredStart = allocation.start;
     }
 
     public int getPerturbationPriority() {
         return perturbationPriority;
     }
 
     public int getPreferredStart() {
         return preferredStart;
     }
     
     /**
      * Allocation cost is made primarly 
      * @return 
      */
     public int getAllocationCost() {
        return (maxDuration - allocation.duration) * Math.abs(allocation.start - preferredStart) +  Math.abs(allocation.start - preferredStart);
     }
 }
