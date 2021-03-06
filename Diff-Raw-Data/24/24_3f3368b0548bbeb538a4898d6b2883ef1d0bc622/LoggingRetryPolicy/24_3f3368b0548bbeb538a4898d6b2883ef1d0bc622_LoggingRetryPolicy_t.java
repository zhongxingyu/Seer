 package com.datastax.driver.core.policies;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.apache.log4j.Level;
 
 import com.datastax.driver.core.*;
 
 /**
  * A retry policy that wraps another policy, logging the decision made by its sub-policy.
  * <p>
  * Note that this policy only log the IGNORE and RETRY decisions (since
  * RETHROW decisions just amount to propate the cassandra exception). The
  * logging is done at the INFO level.
  */
 public class LoggingRetryPolicy implements RetryPolicy {
 
     private static final Logger logger = LoggerFactory.getLogger(LoggingRetryPolicy.class);
     private final RetryPolicy policy;
 
     /**
      * Creates a new {@code RetryPolicy} that logs the decision of {@code policy}.
      *
      * @param policy the policy to wrap. The policy created by this constructor
      * will return the same decision than {@code policy} but will log them.
      */
     public LoggingRetryPolicy(RetryPolicy policy) {
         this.policy = policy;
     }
 
     private static ConsistencyLevel cl(ConsistencyLevel cl, RetryDecision decision) {
         return decision.getRetryConsistencyLevel() == null ? cl : decision.getRetryConsistencyLevel();
     }
 
     public RetryDecision onReadTimeout(ConsistencyLevel cl, int requiredResponses, int receivedResponses, boolean dataRetrieved, int nbRetry) {
         RetryDecision decision = policy.onReadTimeout(cl, requiredResponses, receivedResponses, dataRetrieved, nbRetry);
         switch (decision.getType()) {
             case IGNORE:
                String f1 = "Ignoring read timeout (initial consistency: %s, required responses: %d, received responses: %d, data retrieved: %b, retries: %d)";
                 logger.info(String.format(f1, cl, requiredResponses, receivedResponses, dataRetrieved, nbRetry));
                 break;
             case RETRY:
                String f2 = "Retrying on read timeout at consistency %s (initial consistency: %s, required responses: %d, received responses: %d, data retrieved: %b, retries: %d)";
                 logger.info(String.format(f2, cl(cl, decision), cl, requiredResponses, receivedResponses, dataRetrieved, nbRetry));
                 break;
         }
         return decision;
     }
 
     public RetryDecision onWriteTimeout(ConsistencyLevel cl, WriteType writeType, int requiredAcks, int receivedAcks, int nbRetry) {
         RetryDecision decision = policy.onWriteTimeout(cl, writeType, requiredAcks, receivedAcks, nbRetry);
         switch (decision.getType()) {
             case IGNORE:
                String f1 = "Ignoring write timeout (initial consistency: %s, write type: %s, required acknowledgments: %d, received acknowledgments: %d, retries: %d)";
                 logger.info(String.format(f1, cl, writeType, requiredAcks, receivedAcks, nbRetry));
                 break;
             case RETRY:
                String f2 = "Retrying on write timeout at consistency %s(initial consistency: %s, write type: %s, required acknowledgments: %d, received acknowledgments: %d, retries: %d)";
                 logger.info(String.format(f2, cl(cl, decision), cl, writeType, requiredAcks, receivedAcks, nbRetry));
                 break;
         }
         return decision;
     }
 
     public RetryDecision onUnavailable(ConsistencyLevel cl, int requiredReplica, int aliveReplica, int nbRetry) {
         RetryDecision decision = policy.onUnavailable(cl, requiredReplica, aliveReplica, nbRetry);
         switch (decision.getType()) {
             case IGNORE:
                String f1 = "Ignoring unavailable exception (initial consistency: %s, required replica: %d, alive replica: %d, retries: %d)";
                 logger.info(String.format(f1, cl, requiredReplica, aliveReplica, nbRetry));
                 break;
             case RETRY:
                String f2 = "Retrying on unavailable exception at consistency %s (initial consistency: %s, required replica: %d, alive replica: %d, retries: %d)";
                 logger.info(String.format(f2, cl(cl, decision), cl, requiredReplica, aliveReplica, nbRetry));
                 break;
         }
         return decision;
     }
 }
