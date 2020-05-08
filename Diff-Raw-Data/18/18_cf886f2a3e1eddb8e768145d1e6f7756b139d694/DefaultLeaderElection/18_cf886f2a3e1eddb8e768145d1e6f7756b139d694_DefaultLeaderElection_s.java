 package com.hopper.quorum;
 
 import com.hopper.GlobalConfiguration;
 import com.hopper.server.ComponentManager;
 import com.hopper.server.ComponentManagerFactory;
 import com.hopper.server.Endpoint;
 import com.hopper.server.Server.ElectionState;
 import com.hopper.session.Message;
 import com.hopper.session.MessageService;
 import com.hopper.session.OutgoingSession;
 import com.hopper.verb.Verb;
 import com.hopper.verb.VerbMappings;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 public class DefaultLeaderElection implements LeaderElection {
     /**
      * Logger
      */
     private static final Logger logger = LoggerFactory.getLogger(DefaultLeaderElection.class);
 
     private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();
     private final GlobalConfiguration config = componentManager.getGlobalConfiguration();
     /**
      * Singleton instance
      */
     private final Paxos paxos = new Paxos();
 
     public Paxos getPaxos() {
         return paxos;
     }
 
     @Override
     public void startElecting() {
         if (componentManager.getDefaultServer().getElectionState() == ElectionState.LOOKING) {
             return;
         }
 
         componentManager.getDefaultServer().setElectionState(ElectionState.LOOKING);
 
         Thread electionThread = new Thread(new Runnable() {
             @Override
             public void run() {
                 asynElecting();
             }
         });
 
         electionThread.setName("election-thread");
         electionThread.setDaemon(true);
         electionThread.start();
     }
 
     public void asynElecting() {
 
         int repeated = 0;
         boolean retry = false;
 
         do {
             repeated++;
 
             logger.debug("Retrying to start election  [{}]...", repeated);
 
             try {
                 List<QueryLeader> replyResult = new ArrayList<QueryLeader>();
 
                 boolean initElection = canInitElection(replyResult);
 
                 // Can initialize a new election?
                 if (initElection) {
                     // Await completion for current running election
                     awaitIfNecessary();
 
                     // Start paxos progress normally
                     startPaxos();
                 } else {
 
                     // Retrieve the candidate leader
                     int candidateLeader = getCandidateLeader(replyResult);
 
                     // if the candidate is alive, takes it as local leader; Otherwise, waiting for a next period
                     testCandidateLeader(candidateLeader);
 
                     // if the candidate is alive, takes it as local leader.
                     LearnVerbHandler handler = (LearnVerbHandler) VerbMappings.getVerbHandler(Verb.PAXOS_LEARN);
                     handler.learnElectedLeader(paxos.getEpoch(), componentManager.getDefaultServer().getLeader(),
                             candidateLeader);
                 }
             } catch (NoQuorumException e) {
                 logger.debug("No enough nodes are alive, waiting for other nodes joining...");
                 retry = waitingNextElection(config.getPeriodForJoin());
             } catch (PaxosRejectedException e) {
                 // If ballot is lower, it indicates other nodes are in progress, so it must wait for a moment
                 if (e.reject == PaxosRejectedException.BALLOT_REJECT) {
                     logger.debug("Current ballot is lower, re-starts the paxos progress just a moment");
                     retry = waitingNextElection(config.getRetryElectionPeriod());
                     // If instance number is lower, it indicates other nodes had undergone some elections
                 } else {
                     logger.debug("Current instance is lower, re-starts the paxos immediately.");
                     retry = true;
                 }
             } catch (ElectionTerminatedException e) {
                 retry = false;
             } catch (TestLeaderFailureException e) {
                 logger.debug("Failed to test candidate leader:{},waiting {} milliseconds for the next election",
                         new Object[]{e.leader, config.getPeriodForJoin(), e});
 
                 retry = waitingNextElection(config.getPeriodForJoin());
             } catch (TimeoutException e) {
                 logger.debug("Timeout for get result,waiting {} milliseconds for next election",
                         config.getPeriodForJoin(), e);
                 retry = waitingNextElection(config.getPeriodForJoin());
             } catch (InterruptedException e) {
                 logger.debug("Stop current election instance because of interrupt.");
                 retry = false;
             } catch (Exception e) {
                 logger.debug("Unknown exception occurred, waiting {} milliseconds for the next election",
                         config.getPeriodForJoin(), e);
                 retry = waitingNextElection(config.getPeriodForJoin());
             }
         } while (retry);
     }
 
     /**
      * Whether current node can initialize a new election? It will communication
      * with all endpoints, if majority of them believe they can, it can initial;
      * otherwise, <b>must</b> be waiting.
      */
     private boolean canInitElection(final List<QueryLeader> replyResult) throws NoQuorumException,
             PaxosRejectedException {
 
         List<QueryLeader> queryResults = queryLeaders();
 
         // No majority nodes are alive
         if (queryResults.size() < config.getQuorumSize() - 1) {
             throw new NoQuorumException();
         }
 
         QueryLeader highestResult = queryResults.get(0);
 
         int localHighestEpoch = paxos.getEpoch();
 
         // This indicating that other nodes had undergone some election instances.  If the higher instance number has
         // found, re-starting the paxos progress.
         if (highestResult.getEpoch() > localHighestEpoch) {
             localHighestEpoch = highestResult.getEpoch();
             paxos.updateInstance(localHighestEpoch);
 
             throw new PaxosRejectedException(PaxosRejectedException.INSTANCE_REJECT);
         }
 
         int numMissingLeader = 0;
 
         // All results that missing leader or epochs are less than localHighestEpoch will be taken as "missing leader"
         for (QueryLeader queryResult : queryResults) {
             if (!queryResult.hasLeader() || queryResult.getEpoch() < localHighestEpoch) {
                 numMissingLeader++;
             }
         }
 
         replyResult.addAll(queryResults);
 
         // If majority misses leader, must re-start election
         return numMissingLeader >= config.getQuorumSize() - 1;
     }
 
     private List<QueryLeader> queryLeaders() {
         Message message = new Message();
         message.setVerb(Verb.QUERY_LEADER);
         message.setId(Message.nextId());
 
        List<Message> replies = componentManager.getMessageService().sendMessageToQuorum(message, 0);
 
         List<QueryLeader> leaders = new ArrayList<QueryLeader>(replies.size());
 
         for (Message reply : replies) {
             leaders.add((QueryLeader) reply.getBody());
         }
 
         // Sort leaders by epoch
         Collections.sort(leaders, new Comparator<QueryLeader>() {
             @Override
             public int compare(QueryLeader leader1, QueryLeader leader2) {
                 return leader2.getEpoch() - leader1.getEpoch();
             }
         });
 
         return leaders;
     }
 
     /**
      * Await completion for current running election. The behind idea is avoiding competing.
      */
     private void awaitIfNecessary() throws TimeoutException, InterruptedException {
         // It indicating that local server has voted for current instance, as optimization it may not start a new
         // election, instead of waiting until the electing complete.
         if (paxos.isVoted()) {
             // It indicating that election is running
             if (!componentManager.getDefaultServer().isKnownLeader()) {
                 // Waiting for the election complete
                 componentManager.getDefaultServer().awaitLeader(config.getWaitingPeriodForElectionComplete());
             }
         }
     }
 
     /**
      * Starting paxos algorithm
      */
     private void startPaxos() {
 
         int leader = phase1();
 
         // If the serverId from Phase1b is not the local server, it indicating there are some contention and
         // other servers may have completed Phase1, current node should abandon the subsequent election steps.
         if (config.getElectionMode() == GlobalConfiguration.ElectionMode.FAST) {
             if (leader != config.getLocalServerEndpoint().serverId) {
                 throw new ElectionTerminatedException();
             }
         }
 
         // starting phase2
         phase2(leader);
     }
 
     /**
      * Executes paxos Phase1
      */
     private int phase1() {
 
         // Executing Phase1a(Prepare)
         List<Message> replies = prepare();
 
         int promiseCount = 0;
         int rejectBallot = -1;
         int rejectEpoch = -1;
 
         // Sort all promise results by vrnd(voted round)
         Collections.sort(replies, new Comparator<Message>() {
             @Override
             public int compare(Message m1, Message m2) {
                 Promise p1 = (Promise) m1.getBody();
                 Promise p2 = (Promise) m2.getBody();
 
                 return p2.getVrnd() - p1.getVrnd();
             }
         });
 
         for (Message reply : replies) {
             Promise promise = (Promise) reply.getBody();
             if (promise.getStatus() == Promise.PROMISE) {
                 promiseCount++;
             } else if (promise.getStatus() == Promise.REJECT_BALLOT) {
                 rejectBallot = Math.max(rejectBallot, promise.getRnd());
             } else {
                 rejectEpoch = Math.max(rejectEpoch, promise.getEpoch());
             }
         }
 
         // majority promised
         if (promiseCount >= config.getQuorumSize() - 1) {
             Promise first = (Promise) replies.get(0).getBody();
 
             int leader = first.getVval();
 
             if (paxos.getRnd() == first.getRnd() && paxos.getVrnd() > first.getVrnd()) {
                 leader = paxos.getVval();
             }
 
             // Majority has no chosen any value, it may free to decide; otherwise, must pick up the first one
             if (leader < 0) {
                 leader = componentManager.getDefaultServer().getServerEndpoint().serverId;
             }
 
             return leader;
         }
 
         int reject = 0;
 
         // Majority has rejected the prepare ,if local epoch is smaller
         if (rejectEpoch > 0) {
             reject = PaxosRejectedException.INSTANCE_REJECT;
             paxos.updateInstance(rejectEpoch);
 
             // local ballot is smaller
         } else if (rejectBallot > 0) {
             reject = PaxosRejectedException.BALLOT_REJECT;
             paxos.setRnd(rejectBallot);
         }
 
         // restart phase1
         throw new PaxosRejectedException(reject);
     }
 
     /**
      * Execute Paxos prepare(Phase1a)
      *
      * @throws NoQuorumException           If no enough no-fault nodes are alive
      * @throws ElectionTerminatedException If election has been terminated
      */
     private List<Message> prepare() throws NoQuorumException, ElectionTerminatedException {
 
         if (componentManager.getDefaultServer().getElectionState() != ElectionState.LOOKING) {
             throw new ElectionTerminatedException();
         }
 
         // send prepare(Phase1a) message
         Message message = new Message();
         message.setId(Message.nextId());
         message.setVerb(Verb.PAXOS_PREPARE);
 
         Prepare prepare = new Prepare();
 
         // generate a ballot id
         final int serverBallotId = config.getServerBallotId(config.getLocalServerEndpoint().serverId);
         int ballot = BallotGenerator.generateBallot(serverBallotId, config.getGroupEndpoints().length, paxos.getRnd());
 
         paxos.setRnd(ballot);
 
         prepare.setBallot(ballot);
         prepare.setEpoch(paxos.getEpoch());
         message.setBody(prepare);
 
         // Receive the promise(Phase1b) message
         List<Message> replies = componentManager.getMessageService().sendMessageToQuorum(message,
                MessageService.WAITING_MODE_ALL);
 
         // if no majority responses, it can't work
         if (replies.size() < config.getQuorumSize() - 1) {
             throw new NoQuorumException();
         }
 
         return replies;
     }
 
     private void phase2(int leader) {
 
         if (componentManager.getDefaultServer().getElectionState() != ElectionState.LOOKING) {
             throw new ElectionTerminatedException();
         }
 
         Message message = new Message();
         message.setVerb(Verb.PAXOS_ACCEPT);
         message.setId(Message.nextId());
 
         Accept accept = new Accept();
         accept.setEpoch(paxos.getEpoch());
         accept.setVval(leader);
         accept.setBallot(paxos.getRnd());
 
         message.setBody(accept);
 
         List<Message> replies = componentManager.getMessageService().sendMessageToQuorum(message,
                 MessageService.WAITING_MODE_ALL);
 
         // No majority are alive
         if (replies.size() < config.getQuorumSize()) {
             throw new NoQuorumException();
         }
 
         int numAccepted = 0;
         int rejectedRnd = -1;
         int rejectedEpoch = -1;
 
         for (Message reply : replies) {
             Accepted accepted = (Accepted) reply.getBody();
             if (accepted.getStatus() == Accepted.ACCEPTED) {
                 numAccepted++;
             } else if (accepted.getStatus() == Accepted.REJECT_BALLOT) {
                 rejectedRnd = Math.max(rejectedEpoch, accepted.getRnd());
             } else if (accepted.getStatus() == Accepted.REJECT_EPOCH) {
                 rejectedEpoch = Math.max(rejectedEpoch, accepted.getEpoch());
             }
         }
 
         // Majority has reached consensus, send LEARN message
         if (numAccepted >= config.getQuorumSize() - 1) {
             Message learnMessage = makeLearnMessage();
 
             // Send Learn message to all nodes
             componentManager.getMessageService().sendLearnMessage(learnMessage);
             return;
         }
 
         if (rejectedEpoch != -1) {
             paxos.updateInstance(rejectedEpoch);
             throw new PaxosRejectedException(PaxosRejectedException.INSTANCE_REJECT);
         } else if (rejectedRnd != -1) {
             paxos.setRnd(rejectedRnd);
             throw new PaxosRejectedException(PaxosRejectedException.BALLOT_REJECT);
         }
     }
 
     /**
      * Make a learn message
      */
     private Message makeLearnMessage() {
 
         Message message = new Message();
         message.setVerb(Verb.PAXOS_LEARN);
         message.setId(Message.nextId());
 
         Learn learn = new Learn();
         learn.setEpoch(paxos.getEpoch());
         learn.setProposer(componentManager.getDefaultServer().getRpcEndPoint().serverId);
         learn.setVval(paxos.getVval());
 
         return message;
     }
 
     /**
      * Test the candidate leader, if can communicate with the candidate leader and the candidate owns the leadership,
      * then setting the candidate as the local leader. Otherwise, waiting the next communication period for retrying.
      */
     private void testCandidateLeader(int leader) {
 
         try {
             Message request = new Message();
             request.setVerb(Verb.TEST_LEADER);
             request.setId(Message.nextId());
 
             Future<Message> future = componentManager.getMessageService().send(request, leader);
 
             Message reply = future.get(config.getRpcTimeout(), TimeUnit.MILLISECONDS);
             byte[] body = (byte[]) reply.getBody();
 
             boolean isLeader = body[0] == 0;
 
             // Candidate is not a leader
             if (!isLeader) {
                 throw new TestLeaderFailureException(leader, "The candidate leader has loosen leadership");
             }
         } catch (TestLeaderFailureException e) {
             throw e;
         } catch (Exception e) {
             throw new TestLeaderFailureException(leader, e);
         }
     }
 
     /**
      * If majority nodes believe the own leader is alive(may be different from each other),
      * we takes the leader of holds maximum xid as the candidate leader
      */
     private int getCandidateLeader(List<QueryLeader> replies) {
 
         for (QueryLeader queryResult : replies) {
             if (queryResult.hasLeader()) {
                 return queryResult.getLeader();
             }
         }
 
         return -1;
     }
 
     private boolean waitingNextElection(long timeout) {
         try {
             Thread.sleep(timeout);
         } catch (InterruptedException e) {
             return false;
         }
 
         return true;
     }
 }
