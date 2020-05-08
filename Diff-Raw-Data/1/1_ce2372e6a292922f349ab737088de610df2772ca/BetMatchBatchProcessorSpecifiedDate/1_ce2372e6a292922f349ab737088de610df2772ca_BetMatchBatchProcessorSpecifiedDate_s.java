 package BetMatchProcessing;
 
 import HandicapProcessing.HandicapProcessing;
 import Util.MongoDBUtil;
 import Util.Props;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 /**
  * Created with IntelliJ IDEA.
  * User: snowhyzhang
  * Date: 13-1-16
  * Time: 上午12:35
  * To change this template use File | Settings | File Templates.
  */
 public class BetMatchBatchProcessorSpecifiedDate {
 
     private int processingMatch;
 
     private String dateFrom;
     private String dateTo;
     private MongoDBUtil dbUtil;
     ExecutorService executorService;
 
     public BetMatchBatchProcessorSpecifiedDate(ExecutorService executorService, String dateFrom, String dateTo) {
         this.dateFrom = dateFrom;
         this.dateTo = dateTo;
         this.executorService = executorService;
     }
 
     public static void main(String args[]) {
         String fromDate = null;
         String toDate = null;
 
         if (args.length == 0) {
             fromDate = Props.getProperty("SpecifiedDateFrom");
             toDate = Props.getProperty("SpecifiedDateTo");
         } else {
             fromDate = args[0];
             toDate = args[1];
         }
         ExecutorService executorService = Executors.newFixedThreadPool(Integer.parseInt(Props.getProperty("thread")));
         BetMatchBatchProcessorSpecifiedDate betMatchBatchProcessor = new BetMatchBatchProcessorSpecifiedDate(executorService, fromDate, toDate);
 
         double minExpectation = Double.parseDouble(Props.getProperty("minExpectation"));//0.03;
         double minProbability = Double.parseDouble(Props.getProperty("minProbability"));//0.58;
         betMatchBatchProcessor.betBatchMatchHandicapGuarantee(minExpectation, minProbability);
     }
 
     public void betBatchMatchHandicapGuarantee(double minExpectation, double minProbability) {
         long t1 = System.currentTimeMillis();
         int cpuNum = Runtime.getRuntime().availableProcessors();
 
 
         final List<DBObject> matchList = getAllBettingMatch();
         dbUtil.drop(Props.getProperty("MatchBatchBetSpecifiedDate"));
 
         processingMatch = 0;
         final int[] BetOnMatch = {0};
         final double minExp = minExpectation;
         final double minPro = minProbability;
         List<Future> futures = new ArrayList<Future>();
         for (final DBObject match : matchList) {
             Future future = executorService.submit(new Runnable() {
 
                 public void run() {
                     iBetMatchProcessing bmp = new BetHandicapMatchGuarantee();
                     HandicapProcessing hp = new HandicapProcessing();
 
                     bmp.setCollection(Props.getProperty("MatchBatchBetSpecifiedDate"));
                     double win = ((Number) match.get("w1")).doubleValue();
                     double push = ((Number) match.get("p1")).doubleValue();
                     double lose = ((Number) match.get("l1")).doubleValue();
                     double winRate = ((Number) match.get("h1")).doubleValue();
                     double loseRate = ((Number) match.get("h2")).doubleValue();
 
                     String matchId = (String) match.get("matchId");
                     double ch = ((Number) match.get("ch")).doubleValue();
                     String cid = (String) match.get("cid");
                     int abFlag = ((Number) match.get("abFlag")).intValue();
                     Date matchTime = ((Date) match.get("time"));
 
                     double handicap = getHandicap(ch, abFlag);
                     if (handicap == -999) {
                         System.out.println("ERROR handicap!");
                         return;
                     }
                     if (handicap >= 3 || handicap <= -3) {
                         System.out.println("The handicap is out of range: " + handicap);
                         return;
                     }
                     hp.setMatch(win, push, lose, handicap, winRate, loseRate, matchId, "snow", cid, matchTime);
                     int isBet = hp.getResult(10000, 10, false);
                     if (isBet != 0) {
                         return;
                     }
                     isBet = bmp.betMatch(minExp, minPro, 10, hp);
                     if (isBet == 0) {
                         ++BetOnMatch[0];
                     }
 
                 }
             });
             futures.add(future);
         }
         int index = 0;
         for (Future f : futures) {
             try {
                 f.get();
                 System.out.println("done with" + index++);
             } catch (InterruptedException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             } catch (ExecutionException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
         }
         long t2 = System.currentTimeMillis();
 
         System.out.println("\n****\nTotal Match: " + matchList.size() + "\nBet on match: " + BetOnMatch[0] + "\ntotal time:" + (t2 - t1));
     }
 
     private double getHandicap(double type, int abFlag) {
         double handicap = type / 4.0;
         if (abFlag == 0) {              //让球
             return handicap;
         } else if (abFlag == 1) {       //受让让球
             return handicap * -1;
         } else {
             return -999;
         }
     }
 
     private List<DBObject> getAllBettingMatch() {
         String cid = Props.getProperty("betCId");
 
         DBObject query = new BasicDBObject();
         query.put("ch", new BasicDBObject("$ne", null));
         query.put("abFlag", new BasicDBObject("$ne", null));
         query.put("cid", cid);
         try {
             query.put("time", new BasicDBObject("$gte", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateFrom + " 00:00:00")).append("$lte", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTo + " 00:00:00")));
         } catch (Exception ex) {
             ex.printStackTrace();
         }
 
         DBObject field = new BasicDBObject();
         field.put("h1", 1);
         field.put("h2", 1);
         field.put("abFlag", 1);
         field.put("ch", 1);
         field.put("matchId", 1);
         field.put("cid", 1);
         field.put("w1", 1);
         field.put("p1", 1);
         field.put("l1", 1);
         field.put("time", 1);
 
         dbUtil = MongoDBUtil.getInstance(Props.getProperty("MongoDBRemoteHost"),
                 Props.getProperty("MongoDBRemotePort"),
                 Props.getProperty("MongoDBRemoteName"));
 
         List<DBObject> matchList = dbUtil.findAll(query, field, Props.getProperty("MatchHistoryResult"));
 
         return matchList;
     }
 }
