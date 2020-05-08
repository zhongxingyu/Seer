 package model;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  *
  */
 public class Field {
     public static final int SLICE_COUNT = 16;
 
     /**Map of Slice# to Slice data */
    public Map<Integer,Slice> slices = new HashMap<Integer,Slice>();
 
     /**Map of Slice# to Slice data. These are not managed by this node, but are required to calculate the ones that are.*/
    public Map<Integer,Slice> external_slices = new HashMap<Integer,Slice>();
 
     public int version = -1;
 
     public Field() {
         for(int i = 0; i < SLICE_COUNT; i++) {
             slices.put(i,new Slice(i,0));
         }
 
         this.version = 0;
     }
 
     public Field(Field other) {
         this.version = other.version;
 //        for(Slice slice : other.slices.values()) {
 //            this.slices.put(slice.number,slice.clone());
 //        }
     }
 
     /**
      * The slices this Field manages.
      */
     public Map<Integer,Slice> internalSlices() {
         return slices;
     }
 
     /**
      * The external slices this Field is storing.
      */
     public Map<Integer,Slice> externalSlices() {
         return external_slices;
     }
 
     /**
      * Get the state of a cell
      *
      * @param x first coord in slice
      * @param y second coord in slice
      * @param z slice number
      * @return -1 if not found, 0 if off, 1+ if on (ticks spent on)
      */
     public int getCell( int x , int y , int z ) {
         Slice slice = getSlice(z);
         if(slice != null)
             return slice.cells[x][y];
         else
             return -1;
     }
 
     /**
      * Request any slices required to update the slices managed by this Field.
      */
     public void requestRequiredSlices() {
         Set<Integer> required = new HashSet<Integer>();
         for(Integer slice_num : slices.keySet()) {
             required.add((slice_num+1)%SLICE_COUNT);            //add after
             required.add((slice_num+SLICE_COUNT-1)%SLICE_COUNT);//add before
             required.remove(slice_num);                         //remove current
         }
 
         for(Integer slice_num : required ) {
             if(!external_slices.containsKey(slice_num))
                 ;//TODO: request slice
         }
     }
 
     /**
      * Do we have all the slices we need to update?
      */
     public boolean hasRequiredSlices() {
         Set<Integer> required = new HashSet<Integer>();
         for(Integer slice_num : slices.keySet()) {
            required.add((slice_num+1)%SLICE_COUNT);            //add after
            required.add((slice_num+SLICE_COUNT-1)%SLICE_COUNT);//add before
             required.remove(slice_num);                         //remove current
         }
 
         for(Integer slice_num : required ) {
             if(!external_slices.containsKey(slice_num))
                 return false;
         }

         return true;
     }
 
     /**
      * Get the specified slice
      * @param slice_num
      * @return null if the slice was not found, the slice otherwise. Slice may not be managed by this Field.
      */
     public Slice getSlice(int slice_num) {
         if(slice_num < 0)
             slice_num = (slice_num + SLICE_COUNT);
         slice_num = slice_num % SLICE_COUNT;
 
         if(slices.containsKey(slice_num))
             return slices.get(slice_num);
         else if(external_slices.containsKey(slice_num))
             return external_slices.get(slice_num);
         else
             return null;
     }
 
     /**
      * Updates the field to the next version. Does not modify original.
      * @return The updated field.
      */
     public Field updateToCopy() {
         if(!hasRequiredSlices())
             throw new IllegalStateException("Field does not have all required slices. Cannot update.");
 
         Field field = new Field();
         field.version = this.version + 1; //returning next version
 
         for(Slice slice : this.slices.values()) {
            Slice prev = getSlice(slice.number-1);
             Slice next = getSlice(slice.number + 1);
             if(prev == null || next == null)
                 throw new IllegalStateException("Not all required slices were present.");
             this.slices.put(slice.number,slice.updateToCopy(prev, next));
         }
 
         return field;
     }
 }
