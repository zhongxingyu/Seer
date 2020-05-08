 package de.hronopik.icfp2009.vm;
 
 import de.hronopik.icfp2009.model.ROM;
 import de.hronopik.icfp2009.util.*;
 
 /**
  * This class represents the memory of the VM.
  *
  * @author Alexander Kiel
  * @version $Id$
  */
 class Memory implements ROM {
 
     private final Array<Double> oldMemory;
 
     private final List<Double> newMemory;
 
     private final boolean status;
 
     private final int insertPos;
 
     /**
      * Creates a new memory.
      * <p/>
      * <b>Note:</b> The values have to be a reversed list of the memory content.
      *
      * @param values a list of the values in the memory; have to be reversed
      * @param status the value of the status register
      */
     Memory(List<Double> values, boolean status) {
         this.oldMemory = new ReadOnlyArray<Double>(values);
         this.newMemory = de.hronopik.icfp2009.util.List.nil();
         this.status = status;
         this.insertPos = 0;
     }
 
     Memory(Array<Double> oldMemory, List<Double> newMemory, boolean status, int insertPos) {
         if (insertPos < oldMemory.size()) {
             this.oldMemory = oldMemory;
             this.newMemory = newMemory;
             this.status = status;
             this.insertPos = insertPos;
         } else {
             this.oldMemory = new ReadOnlyArray<Double>(newMemory);
             this.newMemory = de.hronopik.icfp2009.util.List.nil();
             this.status = status;
             this.insertPos = 0;
         }
     }
 
     //---------------------------------------------------------------------------------------------
     //
     //---------------------------------------------------------------------------------------------
 
     public double getValue(final int address) {
         return (address >= insertPos
                 ? oldMemory.get(oldMemory.size() - address - 1)
                 : newMemory.drop(insertPos - address - 1).head()
        ).maybe(new FailContinuation(address));
     }
 
     public boolean isStatus() {
         return status;
     }
 
     /**
      * Sets the given value at the insert position.
      *
      * @param value the value to set
      * @return a new memory instance
      */
     Memory setValue(double value) {
         return new Memory(oldMemory, new LinkedList<Double>(value, newMemory), status, insertPos + 1);
     }
 
     Memory setStatus(boolean value) {
         return new Memory(oldMemory, new LinkedList<Double>(getValue(insertPos), newMemory), value,
                 insertPos + 1);
     }
 
     Memory copy() {
         return new Memory(oldMemory, new LinkedList<Double>(getValue(insertPos), newMemory), status,
                 insertPos + 1);
     }
 
     //---------------------------------------------------------------------------------------------
     // Overridden Object Methods
     //---------------------------------------------------------------------------------------------
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder("[");
         for (int i = 0; i < oldMemory.size(); i++) {
             if (i != 0) {
                 sb.append(',');
             }
             sb.append(getValue(i));
         }
         return sb.append(']').toString();
     }

    //---------------------------------------------------------------------------------------------
    //
    //---------------------------------------------------------------------------------------------

    private class FailContinuation implements MaybeC<Double, Double> {

        private final int address;

        private FailContinuation(int address) {
            this.address = address;
        }

        public Double c(Double r) {
            return r;
        }

        public Double c() {
            throw new RuntimeException("Illegal memory access at address " + address +
                    ". insertPos = " + insertPos +
                    ", old memory size = " + oldMemory.size() +
                    ", new memory size = " + insertPos
            );
        }
    }
 }
