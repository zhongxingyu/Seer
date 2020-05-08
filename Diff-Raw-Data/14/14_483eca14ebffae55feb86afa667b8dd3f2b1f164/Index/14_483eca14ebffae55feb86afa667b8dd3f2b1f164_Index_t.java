 package ca.hullabaloo.content.impl.storage;
 
 import ca.hullabaloo.content.api.IdSet;
 import ca.hullabaloo.content.impl.ArrayIdSet;
 import ca.hullabaloo.content.impl.query.IdSets;
 import com.google.common.base.Predicate;
 import com.google.common.base.Supplier;
 
 import java.util.BitSet;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 final class Index<T> implements Supplier<IdSet<T>> {
   private final Class<T> type;
   private final String fieldName;
   private final Predicate predicate;
   private final BitSet adds = new BitSet();
   private final BitSet removes = new BitSet();
   private volatile boolean dirty = true;
   private volatile IdSet<T> ids;
 
   public Index(Class<T> type, String fieldName, Predicate<?> predicate) {
     this.type = checkNotNull(type);
     this.fieldName = checkNotNull(fieldName);
     this.predicate = checkNotNull(predicate);
     this.ids = IdSets.empty();
   }
 
   @Override
   public IdSet<T> get() {
     IdSet<T> ids = this.ids;
     return dirty ? build() : ids;
   }
 
   private synchronized IdSet<T> build() {
     if (!dirty) {
       return this.ids;
     }
     this.dirty = false;
 
     IdSet<T> ids = this.ids;
     ids = ids.or(asIdSet(this.adds));
     ids = ids.andNot(asIdSet(this.removes));
     this.ids = ids;
     return ids;
   }
 
   private IdSet<T> asIdSet(BitSet bs) {
     int[] ids = new int[bs.cardinality()];
     int pos = 0;
     for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
       ids[pos++] = i;
     }
     bs.clear();
     return ArrayIdSet.ofSorted(ids);
   }
 
   public synchronized void update(UpdateBatch updates) {
     //if the event matches field
     //  - and matches predicate, add to id set
     //  - and not matches, remove from id set
     boolean makeDirty = false;
     for (UpdateRecord update : updates) {
       if ((this.type == update.wholeType || this.type == update.fractionType) && this.fieldName.equals(update.field)) {
         makeDirty = true;
        if (this.predicate.apply(update.value)) {
           this.adds.set(update.id);
           this.removes.clear(update.id);
         } else {
           this.adds.clear(update.id);
           this.removes.set(update.id);
         }
       }
       if (makeDirty && !dirty) {
         dirty = true;
       }
     }
   }
 
   public int hashCode() {
     return fieldName.hashCode() + 31 * predicate.hashCode();
   }
 
   public boolean equals(Object o) {
     if (o instanceof Index) {
       Index that = (Index) o;
       return o == this ||
           (this.type.equals(that.type)
               && this.fieldName.equals(that.fieldName)
               && this.predicate.equals(that.predicate));
     }
     return false;
   }
 }
