 package adsim.core;
 
 import lombok.*;
 import lombok.extern.slf4j.Slf4j;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 @Slf4j
 public class Session {
     private int DEFAULT_STEP_LIMIT = 100;
     private int DEFAULT_MAX_FRIENDSHIPS = 10;
     private int DEFAULT_MIN_FRIENDSHIPS = 1;
     private ICase cas;
 
     @Getter
     private long step;
 
     @Getter
     private long stepLimit;
 
     @Getter
     private long createdMessagesCount;
 
     @Getter
     private long reachedMessagesCount;
 
     public List<Node> getNodes() {
         return cas.getNodes();
     }
 
     private @Getter
     @Setter(AccessLevel.PROTECTED)
     WorkerState state;
 
     private final @Getter
     Field field;
 
     public Session(ICase cas) {
         this.cas = cas;
         this.field = new Field();
         this.step = 0;
         this.stepLimit = cas.getStepLimit();
         stepCheck();
         log.debug("Session for " + cas.toString() + " initialized");
     }
 
     private void stepCheck() {
         if (stepLimit == 0) {
             log.warn("This case has no StepLimit, applying default limit");
             stepLimit = DEFAULT_STEP_LIMIT;
         }
     }
 
     public void next() {
         log.debug("Step " + this.step);
         step += 1;
         for (val node : cas.getNodes()) {
             node.next(this);
         }
         this.field.next();
     }
 
     public void start() {
         try {
             while (step < stepLimit) { // TODO: REMOVE THIS GUARD
                 next();
             }
             log.info("Session finished.");
         } catch (SessionFinishedException e) {
             log.info("Session aborted.");
         }
     }
 
     /**
      * メッセージを送信する際に全くランダムに送るのでは無く、「知っている人物に定期的に送る」という挙動を実現するために事前に"Friends"
      * として各ノードにメッセージの送信先ノードを登録しておきます
      * 。事前に登録されるFriendの数は、DEFAULT_MAX_FRIENDSHIPS以下
      * 、DEFAULT_MIN_FRIENDSHIPS以上で、疑似一様分布に従って選択されます。
      */
     public void createFriendships() {
         val rand = new Random();
         val nodes = cas.getNodes();
         val max = (int) Math
                 .min(DEFAULT_MAX_FRIENDSHIPS, nodes.size());
         val min = (int) Math.min(max, DEFAULT_MIN_FRIENDSHIPS);
         val nodeCount = nodes.size();
         for (val me : nodes) {
             // generate random number between max and min
            val friendCount =
                    max == 1
                            ? 1
                            : rand.nextInt(max - min) + min;
             val friends = new ArrayList<Integer>(friendCount);
             while (friends.size() < friendCount) {
                val nextCandidate =
                        nodeCount == 1
                                ? 0
                                : rand.nextInt(nodeCount - 1) - 1;
                 if (!friends.contains(nextCandidate)) {
                     val newfriend = nodes.get(nextCandidate);
                     me.addFriend(newfriend);
                     friends.add(nextCandidate);
                 }
             }
         }
     }
 
     public void onMessageCreated(Node node, Message msg) {
         createdMessagesCount += 1;
     }
 
     public void onMessageReached(Node node, Message msg) {
         reachedMessagesCount += 1;
     }
 }
