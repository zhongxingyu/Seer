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
 import java.util.ArrayList;
 
 import com.google.common.collect.Lists;
 
 /**
  * Created by IntelliJ IDEA.
  *
  * @author neteller
  * @created: Nov 25, 2010
  */
 public class MostDiscussedSparks extends BasicSparkSearchResponse
     implements Cacheable<MostDiscussedSparks> {
 
   public MostDiscussedSparks(final List<SparkVO> sparks) {
     super(newTreeSet(sparks), SparkSearchRequest.Filter.DISCUSSED);
   }
 
   @Override
   public TreeSet<SparkVO> getSparks(PagingState state) {
     List<SparkVO> toReturn = Lists.newArrayList(getSparks());
    return newTreeSet(toReturn.subList(state.pageSize() * (state.currentPage() - 1), state.pageSize() * state.currentPage()));
   }
 
   public CacheKey<MostDiscussedSparks> getKey() {
     return CacheKeyFactory.newMostDiscussedSparksKey();
   }
 
   public MostDiscussedSparks getInstance() {
     return this;
   }
 
   private static TreeSet<SparkVO> newTreeSet(List<SparkVO> sparks) {
     final TreeSet<SparkVO> treeSet = new TreeSet<SparkVO>(new Orderings.ByPostCount());
     treeSet.addAll(NullTo.empty(sparks));
     return treeSet;
   }
 
 }
