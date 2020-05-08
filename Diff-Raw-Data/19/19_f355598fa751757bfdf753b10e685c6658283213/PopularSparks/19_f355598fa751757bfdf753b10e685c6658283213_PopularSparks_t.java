 package net.sparkmuse.discussion;
 
 import net.sparkmuse.data.Cacheable;
 import net.sparkmuse.data.paging.PagingState;
 import net.sparkmuse.data.entity.SparkVO;
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
  * @created: Jul 18, 2010
  */
 public class PopularSparks extends BasicSparkSearchResponse
     implements Cacheable<PopularSparks> {
 
   public PopularSparks(final List<SparkVO> sparks) {
     super(newTreeSet(sparks), SparkSearchRequest.Filter.POPULAR);
   }
 
   @Override
   public TreeSet<SparkVO> getSparks(PagingState state) {
     List<SparkVO> toReturn = Lists.newArrayList(getSparks());
    int endIndex = state.pageSize() * state.currentPage() > toReturn.size() ? toReturn.size() : state.pageSize() * state.currentPage();
    return newTreeSet(toReturn.subList(state.pageSize() * (state.currentPage() - 1), endIndex));
   }
 
   public CacheKey<PopularSparks> getKey() {
     return CacheKeyFactory.newPopularSparksKey();
   }
 
   public PopularSparks getInstance() {
     return this;
   }
 
   private static TreeSet<SparkVO> newTreeSet(List<SparkVO> sparks) {
     final TreeSet<SparkVO> treeSet = new TreeSet<SparkVO>(new Orderings.ByRating());
     treeSet.addAll(NullTo.empty(sparks));
     return treeSet;
   }
 
 }
