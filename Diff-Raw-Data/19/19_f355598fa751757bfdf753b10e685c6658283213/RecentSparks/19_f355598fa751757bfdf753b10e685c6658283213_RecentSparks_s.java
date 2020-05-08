 package net.sparkmuse.discussion;
 
 import net.sparkmuse.data.entity.SparkVO;
 import net.sparkmuse.data.Cacheable;
 import net.sparkmuse.data.paging.PagingState;
 import net.sparkmuse.common.CacheKey;
 import net.sparkmuse.common.CacheKeyFactory;
 import net.sparkmuse.common.NullTo;
 import net.sparkmuse.common.Orderings;
 
 import java.util.List;
 import java.util.TreeSet;
 
 import com.google.common.collect.Lists;
 
 /**
  * Created by IntelliJ IDEA.
  *
  * @author neteller
  * @created: Nov 25, 2010
  */
 public class RecentSparks extends BasicSparkSearchResponse
     implements Cacheable<RecentSparks> {
 
   public RecentSparks(final List<SparkVO> sparks) {
     super(newTreeSet(sparks), SparkSearchRequest.Filter.RECENT);
   }
 
   @Override
   public TreeSet<SparkVO> getSparks(PagingState state) {
     List<SparkVO> toReturn = Lists.newArrayList(getSparks());
    return newTreeSet(toReturn.subList(state.pageSize() * (state.currentPage() - 1), state.pageSize() * state.currentPage()));
   }
 
   public CacheKey<RecentSparks> getKey() {
     return CacheKeyFactory.newRecentSparksKey();
   }
 
   public RecentSparks getInstance() {
     return this;
   }
 
   private static TreeSet<SparkVO> newTreeSet(List<SparkVO> sparks) {
     final TreeSet<SparkVO> treeSet = new TreeSet<SparkVO>(new Orderings.ByRecency());
     treeSet.addAll(NullTo.empty(sparks));
     return treeSet;
   }
 
 }
