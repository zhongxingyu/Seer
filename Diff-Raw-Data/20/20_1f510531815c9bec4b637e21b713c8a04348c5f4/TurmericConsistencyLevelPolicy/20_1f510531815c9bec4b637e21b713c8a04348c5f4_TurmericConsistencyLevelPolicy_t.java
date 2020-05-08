 package org.ebayopensource.turmeric.utils.cassandra.hector;
 
 import me.prettyprint.cassandra.service.OperationType;
 import me.prettyprint.hector.api.ConsistencyLevelPolicy;
 import me.prettyprint.hector.api.HConsistencyLevel;
 
 public class TurmericConsistencyLevelPolicy implements ConsistencyLevelPolicy {
 
    @Override
    public HConsistencyLevel get(OperationType op) {
       switch (op) {
          case READ:
             return HConsistencyLevel.QUORUM;
          case WRITE:
            return HConsistencyLevel.QUORUM;
       }
      return HConsistencyLevel.QUORUM;
    }
 
    @Override
    public HConsistencyLevel get(OperationType op, String cfName) {
       switch (op) {
          case READ:
             return HConsistencyLevel.QUORUM;
          case WRITE:
            return HConsistencyLevel.QUORUM;
       }
      return HConsistencyLevel.QUORUM;
    }
 
 }
