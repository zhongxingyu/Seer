 package org.gridkit.gemfire.search.lucene;
 
 import com.gemstone.gemfire.cache.execute.Execution;
 import com.gemstone.gemfire.cache.execute.FunctionException;
 import com.gemstone.gemfire.cache.execute.FunctionService;
 import com.gemstone.gemfire.cache.execute.ResultCollector;
 import com.gemstone.gemfire.distributed.DistributedMember;
 import com.gemstone.gemfire.distributed.DistributedSystem;
 import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem;
 import com.gemstone.gemfire.distributed.internal.membership.InternalRole;
 import org.apache.lucene.search.Query;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 //TODO implement discovery retries logic
 //TODO implement index node balancing
 public class GemfireIndexSearcher {
     private final InternalRole searchServerRole = InternalRole.getRole(SearchServerFactory.searchServerRole);
     private final InternalDistributedSystem distributedSystem;
 
     public GemfireIndexSearcher(DistributedSystem distributedSystem) {
         this.distributedSystem = (InternalDistributedSystem)distributedSystem;
     }
 
     private final ConcurrentMap<String, List<DistributedMember>> indexLocationsMap =
         new ConcurrentHashMap<String, List<DistributedMember>>();
 
    public <K> List<K> search(String regionFullPath, Query query) {
         ResultCollector resultCollector = new IndexSearchResultCollector();
 
         DistributedMember indexMemberId = selectIndexMemberId(regionFullPath);
 
         if (indexMemberId == null)
             throw new FunctionException("Failed to find index member");
 
         Object[] arguments = new Object[2];
         arguments[0] = regionFullPath;
         arguments[1] = query;
 
         Execution execution = FunctionService.onMembers(distributedSystem, Collections.singleton(indexMemberId))
                                              .withArgs(arguments).withCollector(resultCollector);
 
        return (List<K>)execution.execute(IndexSearchFunction.Id).getResult();
     }
 
     private void findIndexMemberIds(String regionFullPath) {
         ResultCollector resultCollector = new IndexDiscoveryResultCollector();
 
         Execution execution = FunctionService.onMembers(distributedSystem, getSearchServerIds())
                                              .withArgs(regionFullPath)
                                              .withCollector(resultCollector);
 
         List<DistributedMember> indexMemberIds =
             (List<DistributedMember>)execution.execute(IndexDiscoveryFunction.Id).getResult();
 
         if (indexMemberIds.size() != 0)
             indexLocationsMap.put(regionFullPath, indexMemberIds);
     }
 
     private Set<DistributedMember> getSearchServerIds() {
         Set<DistributedMember> otherMemberIds = distributedSystem.getDistributionManager().getAllOtherMembers();
 
         Set<DistributedMember> searchServerIds = new HashSet<DistributedMember>();
 
         for (DistributedMember memberId : otherMemberIds) {
             if (memberId.getRoles().contains(searchServerRole))
                 searchServerIds.add(memberId);
         }
 
         return searchServerIds;
     }
 
     private DistributedMember selectIndexMemberId(String regionFullPath) {
         if (!indexLocationsMap.containsKey(regionFullPath))
             findIndexMemberIds(regionFullPath);
 
         List<DistributedMember> indexLocations = indexLocationsMap.get(regionFullPath);
 
         if (indexLocations != null)
             return indexLocations.get(0);
         else
             return null;
     }
 }
