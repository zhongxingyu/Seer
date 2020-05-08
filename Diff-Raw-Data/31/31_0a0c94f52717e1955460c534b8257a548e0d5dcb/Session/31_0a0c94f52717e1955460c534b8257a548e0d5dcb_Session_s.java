 package adsim.core;
 
 import lombok.*;
 import lombok.extern.slf4j.Slf4j;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 import java.util.TreeSet;
 
 import adsim.Util;
 import adsim.misc.Signal;
 import adsim.misc.SignalHandler;
 import adsim.misc.Vector;
 
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
 
     private Signal<Long> onNext;
 
     @Getter
     @Setter
     private int interval;
 
     public List<Node> getNodes() {
         return cas.getNodes();
     }
 
    private final @Getter
     Field field;
 
     public Session(ICase cas) {
         this.cas = cas;
         this.field = new Field(cas.getFieldSize());
         this.step = 0;
         this.stepLimit = cas.getStepLimit();
         this.onNext = new Signal.Sync<Long>();
         this.interval = 0;
         stepCheck();
         init();
         log.debug("Session for " + cas.toString() + " initialized");
     }
 
     private void init() {
         for (val node : getNodes()) {
             node.setSession(this);
         }
         initField();
         log.debug("Creating friendships...");
         createFriendships();
         log.debug("Creating roundpoints...");
         registerRoundPoints();
         for (val node : getNodes()) {
             node.onSessionInitialized();
         }
         watchNode(cas.getWatchNodeCount());
     }
 
     private void watchNode(int count) {
         for (int i = 0; i < count; i++) {
             val node = Util.randomSelect(getNodes());
             node.setVerbose(true);
         }
     }
 
     private void initField() {
         val nodes = cas.getNodes();
         field.expandCapacity(nodes.size());
         for (val node : nodes) {
             field.addDevice(node.getDevice());
         }
     }
 
     private void stepCheck() {
         if (stepLimit == 0) {
             log.warn("This case has no StepLimit, applying default limit");
             stepLimit = DEFAULT_STEP_LIMIT;
         }
     }
 
     public void next() {
         step += 1;
         printProgress();
         for (val node : cas.getNodes()) {
             node.next(this);
         }
         this.field.next();
     }
 
     private void printProgress() {
         val prevPercent = (int) ((((double) step - 1) / (double) stepLimit) * 100);
         val thisPercent = (int) ((((double) step) / (double) stepLimit) * 100);
         if (prevPercent / 10 < thisPercent / 10) {
            //log.info(String.format("%d%% Completed", thisPercent));
         }
     }
 
     public void start() {
         log.info("Starting session. - ID:" + cas.getId());
         try {
             while (step < stepLimit) {
                 if (onNext.hasHandler())
                     onNext.fire(this, new Long(step));
                 next();
                 if (interval != 0) {
                     try {
                         Thread.sleep(interval);
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                 }
             }
             onCompleted();
             log.info("Session finished.");
         } catch (SessionFinishedException e) {
             log.info("Session aborted.");
         }
     }
 
     protected void onCompleted() {
         log.info(String.format("Reach %d/%d(%f%%)", reachedMessagesCount,
                 createdMessagesCount, ((double) reachedMessagesCount) * 100
                         / ((double) createdMessagesCount)));
         cas.tellResult(new ResultReport(createdMessagesCount,
                 reachedMessagesCount, field.getWholeSentCount(), field
                         .getWholeDisposedCount()));
     }
 
     /**
      * メッセージを送信する際に全くランダムに送るのでは無く、「知っている人物に定期的に送る」という挙動を実現するために事前に"Friends"
      * として各ノードにメッセージの送信先ノードを登録しておきます
      * 。事前に登録されるFriendの数は、DEFAULT_MAX_FRIENDSHIPS以下
      * 、DEFAULT_MIN_FRIENDSHIPS以上で、疑似一様分布に従って選択されます。
      */
     private void createFriendships() {
         val nodes = cas.getNodes();
         if (nodes.size() < 2)
             return;
         val max = (int) Math
                 .min(DEFAULT_MAX_FRIENDSHIPS, nodes.size() - 1);
         val min = (int) Math.min(max, DEFAULT_MIN_FRIENDSHIPS);
         for (val me : nodes) {
             // generate random number between max and min
             val friendCount = Util.randInt(min, max);
             while (me.getFriends().size() < friendCount) {
                 val newfriend = Util.randomSelect(nodes);
                 me.addFriend(newfriend);
                 newfriend.addFriend(me);
             }
         }
     }
 
     private void registerRoundPoints() {
         int groupHop = 1;
         long pointCount = getNodes().size() / 2;
         for (int i = 0; i < pointCount; i++) {
             val centerNodes = Util.randomSelect(getNodes());
             val point = Util.createRandomPoint(cas.getFieldSize());
             recAppendRoundPoint(centerNodes, point, groupHop,
                     new TreeSet<Node>());
         }
         for (val node : getNodes()) {
             if (node.getRoundPoints().isEmpty()) {
                 node.addRoundPoint(Util.createRandomPoint(cas.getFieldSize()));
             }
         }
     }
 
     private void recAppendRoundPoint(Node tgt, Vector point, int recRemain,
             Set<Node> visited) {
         if (recRemain == 0 || visited.contains(tgt))
             return;
         else {
             visited.add(tgt);
             for (val friend : tgt.getFriends()) {
                 friend.addRoundPoint(point);
                 recAppendRoundPoint(friend, point, recRemain - 1, visited);
             }
         }
     }
 
     public void onMessageCreated(Node node, Message msg) {
         createdMessagesCount += 1;
     }
 
     public void onMessageAccepted(Node node, Message msg) {
         reachedMessagesCount += 1;
     }
 
     public void addOnNextHandler(SignalHandler<Long> handler) {
         onNext.register(handler);
     }
 
 }
