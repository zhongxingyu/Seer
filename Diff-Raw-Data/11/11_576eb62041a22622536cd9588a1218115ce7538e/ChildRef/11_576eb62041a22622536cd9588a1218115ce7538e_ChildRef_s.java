 package org.atlasapi.media.entity;
 
 import java.util.Comparator;
 import java.util.List;
 
 import org.joda.time.DateTime;
 
 import com.google.common.base.Function;
 import com.google.common.base.Objects;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Ordering;
 
 public class ChildRef implements Comparable<ChildRef> {
 
     private static final Ordering<ChildRef> NATURAL = Ordering.natural();
     private static final Ordering<ChildRef> UPDATED_ORDERING = Ordering.from(new UpdatedComparator());
     
     private final String uri;
     private final String sortKey;
     private final DateTime updated;
 	private final EntityType type;
     
     public static List<ChildRef> dedupeAndSort(Iterable<ChildRef> childRefs) {
         return NATURAL.immutableSortedCopy(ImmutableSet.copyOf(UPDATED_ORDERING.immutableSortedCopy(childRefs)));
     }
     
     public ChildRef(String uri, String sortKey, DateTime updated, EntityType type) {
         this.uri = Preconditions.checkNotNull(uri);
         this.sortKey =  Preconditions.checkNotNull(sortKey);
         this.type = Preconditions.checkNotNull(type);
         this.updated = updated;
     }
     
     public String getUri() {
         return uri;
     }
     
     public String getSortKey() {
         return sortKey;
     }
     
     public DateTime getUpdated() {
         return updated;
     }
     
     public EntityType getType() {
 		return type;
 	}
     
     @Override
    public boolean equals(Object obj) {
        return uri.equals(uri);
     }
     
     @Override
     public int hashCode() {
         return uri.hashCode();
     }
     
     @Override
     public String toString() {
         return Objects.toStringHelper(this).addValue(uri).addValue(sortKey).addValue(updated).toString();
     }
 
     @Override
     public int compareTo(ChildRef comparableTo) {
         return sortKey.compareTo(comparableTo.sortKey);
     }
     
     public static class UpdatedComparator implements Comparator<ChildRef> {
 
         @Override
         public int compare(ChildRef c1, ChildRef c2) {
         	if (c1.getUpdated() == null && c2.getUpdated() == null) {
             	return 0;
             }
             if (c1.getUpdated() == null) {
             	return -1;
             }
             if (c2.getUpdated() == null) {
             	return 1;
             }
             return c1.getUpdated().compareTo(c2.getUpdated());
         }
     }
     
     public static Function<ChildRef, String> TO_URI = new Function<ChildRef, String>() {
         @Override
         public String apply(ChildRef input) {
             return input.getUri();
         }
     };
 }
