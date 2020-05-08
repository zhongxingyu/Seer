 package de.spieleck.ingress.hackstat;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Map;
 import java.util.Set;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import org.apache.log4j.Logger;
 
 import static de.spieleck.ingress.hackstat.GenericHelper.*;
 import static de.spieleck.ingress.hackstat.HackFilter.*;
 
 public class Phase1
     implements Globals
 {
   private final static Logger L = Logger.getLogger(Phase1.class);
 
   private final static int WEEK = 60*60*24*7;
 
   private static HackFilter[] times;
   private final static Map<String,String> ABBR = new HashMap<>();
   private final static String[] CHANGE_DATES = new String[] { "13-08-27", "13-08-08", "13-06-02" };
   static
   {
       int cd1 = CHANGE_DATES.length-1;
       times = new HackFilter[cd1+2];
       int idx = cd1 + 1;
       try {
           times[idx--] = new BeforeThanFilter(CHANGE_DATES[cd1]);
           for(int i = cd1; i > 0; i--) {
              times[idx--] = new BetweenDateFilter(CHANGE_DATES[i-1],CHANGE_DATES[i]);
           }
           times[idx--] = new LaterThanFilter(CHANGE_DATES[0]);
       } catch  ( Exception ex ) {
           L.warn("Cannot construct time filters", ex);
       }
       ABBR.put("ADA Refactor", "ADARef");
       ABBR.put("JARVIS Virus", "JARVIS");
       ABBR.put("Link Amplifier", "LinkAmp");
       ABBR.put("Resonator", "Reso");
       ABBR.put("Force Amplifier", "ForceAmp");
   }
 
   // No output without subsummarizers.
   private final static Summarizer NO_SUMMARY = new CombinedSummarizer();
 
 	private List<HackResult> allHacks = new ArrayList<HackResult>();
 
   private final DataParser parser = new DataParser();
 
   private final static int SHORT = 0;
   private final static int ADAPTIVE = 1;
   private final static int LONG = 2;
 
   private int longMode;
 
   private double startTime = Double.MAX_VALUE;
 
 	public Phase1(int longMode)
 	{
       this.longMode = longMode;
 	}
 
 	public void add(File fi)
 		throws IOException
 	{
 		FileReader fr = new FileReader(fi);
 		List<HackResult> d = parser.grok(fi);
     for(HackResult hackResult : d) {
         startTime = Math.min(startTime, hackResult.timestamp);
         int hackLevel = hackResult.getLevel();
         for(HackItem hackItem : hackResult.hack.items) {
             if ( MEDIA.equals(hackItem.object) && hackItem.level > 0 ) {
                 hackItem.level = 0;
                 L.debug("Media fixed@"+hackResult);
             }
             if ( CUBE.equals(hackItem.object) && hackItem.level != hackLevel ) plausi("WrongCube", fi, hackItem, hackResult);
             else if ( hackItem.level  > 0 && hackItem.level > hackLevel+2 ) plausi("ItemTooHigh", fi, hackItem, hackResult);
             else if ( hackItem.level  > 0 && hackItem.level < hackLevel-1 ) plausi("ItemTooLow", fi, hackItem, hackResult);
         }
     }
 		allHacks.addAll(d);
     L.info("***** #allHacks="+allHacks.size()+", startTime="+startTime);
 	}
 
   private static void plausi(String mark, File fi, HackItem item, HackResult hack)
   {
       L.warn(String.format("%-12s@%13s:%-22s %-4s %s",mark, fi, hack._id,item,hack));
   }
 
   /**
    * Base way to attack KNIME or something common to the data.
    * Please add columns as required!
    */
   public void dumpCSV(String fName, String sep)
       throws IOException
   {
       File fi = new File(fName);
       PrintWriter pw = new PrintWriter(new FileWriter(fi));
       for(HackResult hackResult : allHacks) {
           pw.print(hackResult.timestamp);
           pw.print(sep);
           pw.print(hackResult.getPortalLevel());
           pw.print(sep);
           pw.print(hackResult.getPlayerLevel());
           pw.print(sep);
           pw.print(hackResult.getItemCount());
           pw.print(sep);
           pw.print(FRIEND.equals(hackResult.hack.type)? 1 : 0);
           pw.println();
       }
       pw.close();
       L.info("Finished <"+fName+">.");
   }
 
 	public FullResult stats(Summarizer out, HackFilter... filters)
       throws Exception
 	{
 		Map<String,Integer> types = new HashMap<>();
 		Map<Integer,Integer> levels = new HashMap<>();
 		Map<Integer,Integer> levelTotals = new HashMap<>();
 		Map<Integer,Integer> counts = new HashMap<>();
 		Map<Integer,Integer> noOfItems = new HashMap<>();
 		Map<Integer,Integer> noOfResos = new HashMap<>();
 		Map<Integer,Integer> noOfXmps = new HashMap<>();
 		Map<Integer,Integer> noOfOther = new HashMap<>();
 		Map<String,Integer> noOfPattern = new HashMap<>();
 		Map<String,Integer> noOfPatternBig = new HashMap<>();
 		Map<String,Integer> noOfPatternHuge = new HashMap<>();
 		Map<String,Integer> levelPattern = new HashMap<>();
 		Map<String,Integer> rareItems = new HashMap<>();
 		Map<String,Integer> hackers = new HashMap<>();
 		Map<String,Integer> weeks = new HashMap<>();
 		IMatrix<Integer,Integer> levelResults = new IMatrix<>();
 		Map<Integer,Stats1D> levelResults26 = new HashMap<>();
 		Map<Integer,Integer> levelCounts = new HashMap<>();
 		Map<String,Stats1D> crossItems = new HashMap<>();
 		Map<Integer,Stats1D> playerLevelVsKeys = new HashMap<>();
 		Map<Integer,Stats1D> hackLevelVsKeys = new HashMap<>();
 		Stats2D overHacks = new Stats2D();
 		Stats2D overHacksNPC = new Stats2D();
 		int totalCount = 0;
     // XXX Stupid duplication of code to determine all possible item types to count zeros!!
     Set<String> allFullItems = new HashSet<>();
 outerloop1:    
 		for(HackResult hackResult : allHacks) {
 		  int hackLevel = hackResult.getLevel();
 		  for(HackFilter fi : filters) {
 			  if ( !fi.accept(hackResult) ) continue outerloop1;
 		  }
 		  for(HackItem hackItem : hackResult.hack.items) {
         String fullItem = shortItemName(hackItem);
         if ( hackItem.level > 0 ) {
           int relLevel = hackItem.level - hackLevel;
           fullItem += "."+relLevel;
 			  }
         allFullItems.add(fullItem);
       }
     }
 outerloop:		
 		for(HackResult hackResult : allHacks) {
 		  int hackLevel = hackResult.getLevel();
       HashSet<String> notSeenItems = new HashSet<>(allFullItems);
 		  for(HackFilter fi : filters) {
 			  if ( !fi.accept(hackResult) ) continue outerloop;
 		  }
 		  totalCount++;
 		  int sumCount = 0;
 		  int sumResoCount = 0;
 		  int sumXmpCount = 0;
 		  int sumOtherCount = 0;
 		  int sumKeyCount = 0;
 		  int sumShieldCount = 0;
 		  int sumCubeCount = 0;
 		  String levelBase = Integer.toString(hackResult.getOverLevel());
 		  int relLevelCount = 0;
 		  int relLevelSum = 0;
 		  int relLevelCountNPC = 0;
 		  int relLevelSumNPC = 0;
       int[] hackLevelSum = new int[9];
       increment(hackers, hackResult.hacker.name, 1);
       long week = ((long) (hackResult.timestamp/ WEEK))*WEEK * 1000;
       increment(weeks, String.format("%ty-%<tm-%<td", week), 1);
       increment(levelTotals, hackLevel, 1);
 		  for(HackItem hackItem : hackResult.hack.items) {
         int count = hackItem.quantity;
         sumCount += count;
         increment(counts, count, 1);
         String shortName = shortItemName(hackItem);
         increment(types, shortName, count);
         switch ( hackItem.object ) {
           case RESO: sumResoCount += hackItem.quantity; break;
           case XMP: sumXmpCount += hackItem.quantity; break;
           case KEY: sumKeyCount += hackItem.quantity; break;
           case SHIELD: sumShieldCount += hackItem.quantity; break;
           case CUBE: sumCubeCount += hackItem.quantity; break;
         }
         String fullItem = shortName;
         increment(levelCounts, hackLevel, 1);
         hackLevelSum[hackItem.level] += count;
         if ( hackItem.hasRarity() ) {
             increment(rareItems, hackItem.toString(), count);
         }
         if ( hackItem.level > 0 ) {
           levelResults.inc(hackLevel, hackItem.level, count);
           // XXX this somehow assumes L8 player!
           int relLevel = hackItem.level - hackLevel;
           fullItem += "."+relLevel;
           relLevelCount++;
           relLevelSum += relLevel;
           if ( !CUBE.equals(hackItem.object) ) {
             relLevelCountNPC++;
             relLevelSumNPC += relLevel;
           }
           increment(levels, relLevel, count);
           increment(levelPattern, levelBase+relLevel, count);
         }
         else {
           sumOtherCount += count;
         }
         if ( hackLevel > 1 && hackLevel < 7 ) {
             for(int i = -1; i < 3; i++) {
                 increment(levelResults26, i, hackLevelSum[i+hackLevel]);
             }
         }
         notSeenItems.remove(fullItem);
         increment(crossItems, fullItem, count);
       }
       for(String noItem : notSeenItems) increment(crossItems, noItem, 0);
       increment(noOfResos, sumResoCount, 1);
       increment(noOfXmps, sumXmpCount, 1);
       increment(noOfOther, sumOtherCount, 1);
       increment(noOfPattern, Integer.toString(sumResoCount) + sumXmpCount, 1);
       increment(noOfPatternBig, Integer.toString(sumResoCount) + sumXmpCount + sumOtherCount, 1);
 		  increment(noOfPatternHuge, Integer.toString(sumResoCount) + sumXmpCount + "-" + sumKeyCount + sumShieldCount, 1);
 		  increment(noOfItems, sumCount, 1);
       increment(playerLevelVsKeys, hackResult.getPlayerLevel(), sumKeyCount);
       increment(hackLevelVsKeys, hackLevel, sumKeyCount);
 		  int overLevel = hackResult.getOverLevel();
 		  if ( relLevelCount > 0 ) {
 			  overHacks.add(overLevel,1.0*relLevelSum/relLevelCount);
 		  }
 		  if ( relLevelCountNPC > 0 ) {
 			  overHacksNPC.add(overLevel,1.0*relLevelSumNPC/relLevelCountNPC);
 		  }
 		}
     if ( totalCount == 0 ) return null;
     // if (longMode != LONG || totalCount < 10 ) return null;
     FullResult res = new FullResult(filters, out);
     out.startColumn(Util.append(new StringBuilder(), filters));
 		res.summary("hack levels", levelTotals, totalCount);
 		res.summary("Items", noOfItems, totalCount);
 		res.summary("Resos", noOfResos, totalCount);
 		res.summary("Xmps", noOfXmps, totalCount);
 		res.summary("Other", noOfOther, totalCount);
 		res.summary("Short Patterns", noOfPattern, totalCount);
 		res.summary("Rare Items", rareItems, totalCount);
 		if(longMode == LONG) res.summary("Long Patterns", noOfPatternBig, totalCount);
 		if(longMode == LONG) res.summary("Huge Patterns", noOfPatternHuge, totalCount);
 		res.summary("Items by Type", types, totalCount);
 		res.summary("Items by Level", levels, totalCount);
 		if(longMode == LONG) res.summary("Patterns of Items by Overlevel, Level", levelPattern, totalCount);
 		res.summary2("Items x Level", crossItems, totalCount, true);
 		if(longMode == LONG) res.summary2("Player Level vs Keys", playerLevelVsKeys, totalCount, true);
 		if(longMode == LONG) res.summary2("Hack Level vs Keys", hackLevelVsKeys, totalCount, true);
     if(longMode == LONG ) {
         for(int i = 1; i <= 8; i++) {
             if ( longMode == LONG || i == 1 || i == 7 || i == 8 ) res.summary("Hack Level L"+i, levelResults.getRow(i), levelCounts.get(i), false);
             if ( longMode != LONG && i == 2 ) {
                 res.summary2("Hack Level L2-6 rel.", levelResults26, totalCount, true);
             }
         }
 		}
 		res.summary("Hackers", hackers, totalCount);
 		res.summary("Weeks", weeks, totalCount);
 		out.value("overHacking-Correlation", overHacks.correlation());
 		out.value("overHacking-NonPC-Correlation", overHacksNPC.correlation());
     out.endColumn();
     return res;
   }
 
   public String shortItemName(HackItem item)
   {
       String name = item.object;
       String name2 = ABBR.get(name);
       return name2 == null ? name : name2;
   }
 
   /**
    * Class to collect all data, to allow something like a G-Test for "relevancy".
    * The details might probably be programmed out in a "next" version of the programm,
    * since we might still consider it nice, to have all the data analyzed.
    */
   public class FullResult
   {
       private HackFilter[] filters;
       private Summarizer out;
       private HashMap<String,DiscreteDistr> allData = new HashMap<>();
 
       private FullResult(HackFilter[] filters, Summarizer out)
       {
           this.filters = filters;
           this.out = out;
       }
 
       public HackFilter[] getFilters() { return filters; }
 
       public Set<String> keys() { return allData.keySet(); }
 
       public DiscreteDistr get(String key) { return allData.get(key); }
 
       private void summary(String label, Map<? extends Object, Integer> data, Integer norm)
           throws Exception
       {
           summary(label, data, norm, true);
       }
 
       private void summary(String label, Map<? extends Object, Integer> data, Integer norm, boolean average)
           throws Exception
       {
         if ( data == null ) return;
         int sum = 0;
         boolean allInt = true;
         Object[] keys = new Object[data.size()];
         int i = 0;
         for(Object key : data.keySet()) {
           keys[i++] = key;
           sum += data.get(key);
           allInt &= (key instanceof Integer);
         }
         Arrays.sort(keys);
         if ( norm == null ) {
             norm = Integer.valueOf(sum);
         }
         double hsum = 0.0;
         double f = 100.0 / norm;
         double f2 = 100.0 / sum;
         if ( allInt ) {
             for(Object key : keys) {
                 hsum += data.get(key) * ((Integer)key);
             }
         }
         out.start(label);
         String description = String.format("total=%d, norm=%d ", sum, norm);
         if ( allInt && average) description += String.format(", average=%.2f ", hsum / norm);
         out.description(description);
         out.setNorms(f, f2);
         DiscreteDistr distr = prepareDistr(label);
         for(Object key : keys) {
             out.item(key, data.get(key));
             distr.inc(key, (int) data.get(key));
         }
         out.finish(sum);
       }
 
       private void summary2(String label, Map<? extends Object, Stats1D> data, Integer norm, boolean average)
           throws Exception
       {
         if ( data == null ) return;
         boolean allInt = true;
         Object[] keys = new Object[data.size()];
         int i = 0, sum = 0;
         for(Object key : data.keySet()) {
           keys[i++] = key;
           sum += data.get(key).sum();
           allInt &= (key instanceof Integer);
         }
         Arrays.sort(keys);
         if ( norm == null ) {
             norm = Integer.valueOf(sum);
         }
         double hsum = 0.0;
         if ( allInt ) {
             for(Object key : keys) {
                 hsum += data.get(key).sum() * ((Integer)key);
             }
         }
         out.start(label);
         String description = String.format("total=%d, norm=%d ", sum, norm);
         if ( allInt && average) description += String.format(", average=%.2f ", hsum / norm);
         out.description(description);
         DiscreteDistr distr = prepareDistr(label);
         for(Object key : keys) {
             out.item(key, data.get(key));
             distr.inc(key, (int) data.get(key).sum());
         }
         out.finish(sum);
       }
 
       private DiscreteDistr prepareDistr(String label)
       {
           DiscreteDistr res = new DiscreteDistr();
           allData.put(label, res);
           return res;
       }
 
       public String toString() {
           StringBuilder sb = new StringBuilder();
           sb.append("F[");
           Util.append(sb, filters);
           sb.append("]");
           return sb.toString();
       }
   }
 
   private static void compareDist(FullResult base, FullResult res)
   {
       System.err.println(Util.append(new StringBuilder(), res.getFilters()));
       double sum = 0.0;
       double max = 0.0;
       for(String key : base.keys()) {
           DiscreteDistr d1 = base.get(key);
           DiscreteDistr d2 = res.get(key);
           Set freedom = d1.combinedKeys(d2);
           if ( d2 == null ) continue;
           double gtest = d1.gtest(d2);
           double chiBound = SFunc.critchi(0.95, freedom.size()-1);
           double normed = gtest / chiBound;
           System.err.println(String.format("   gtest(%-23s)=%8.2e  (%3d;%7.2e) %10.0f%%",
                   key, gtest, freedom.size()-1, chiBound, normed));
           if ( gtest > 0 ) {
               sum += normed;
               max = Math.max(max, normed);
           }
       }
       System.err.println(String.format("   === %8.2e/%8.2e", sum, max));
       System.err.println();
   }
 
   private static void selectiveAnalysis(String key, List<FullResult> resultList)
   {
       FullResult[] results = resultList.toArray(new FullResult[resultList.size()]);
       double[] score = new double[results.length];
       double sum = 0.0;
       int count = 0;
       for(int i = 0; i < results.length; i++) {
           FullResult res1 = results[i];
           if ( res1 == null ) continue;
           DiscreteDistr d1 = res1.get(key);
           if ( d1 == null ) continue;
           for(int j = i+1; j < results.length; j++) {
               FullResult res2 = results[j];
               if ( res2 == null ) continue;
               if ( Math.abs(res1.getFilters().length - res2.getFilters().length) != 1 ) continue;
               DiscreteDistr d2 = res2.get(key);
               if ( d2 == null ) continue;
               Set freedom = d1.combinedKeys(d2);
               double gtest = DiscreteDistr.gtest(d1, d2);
               if ( gtest <= 0.0 ) continue;
               double chiBound = SFunc.critchi(0.95, freedom.size()-1);
               if ( Double.isNaN(chiBound) ) continue;
               double normed = gtest / chiBound;
               if ( normed <= 1.0 ) continue;
               double deltaScore = (normed-1.0)*normed;
               score[i] += deltaScore/2.0;
               score[j] += deltaScore/2.0;
               sum += deltaScore;
               count++;
           }
       }
       double average = sum/count;
       for(int i = 0; i < score.length; i++) if ( score[i] > 3.0 * average ) {
           System.out.println(String.format("%s %2d %7.2g %s", key, i, score[i]/average, results[i]));
       }
   }
 
   public List<FullResult> runFilterStack(int longMode, Summarizer o)
       throws Exception
   {
     List<FullResult> res = new ArrayList<FullResult>();
     FullResult base1 = stats(o, NO_FILTER);
     res.add(base1);
     if(longMode == LONG) {
         for(HackFilter f0 : FRIEND_OR_FOE) {
             FullResult res2 = stats(o, f0);
             res.add(res2);
         }
     }
    int time = 0;
    for(HackFilter tFilter : times) {
        time++;
         if ( longMode == LONG ) {
             FullResult res1 = stats(o, tFilter);
             res.add(res1);
         }
         for(HackFilter f0 : FRIEND_OR_FOE) {
             FullResult res2 = stats(o, tFilter, f0);
             res.add(res2);
             //
             if(longMode == LONG) {
                 res.add(stats(o, tFilter, f0, R8_FILTER));
             }
             if(longMode == LONG) {
                 res.add(stats(o, tFilter, f0, R8_FILTER, NON_P8_FILTER ));
             }
             if(longMode == LONG) {
                 res.add(stats(o, tFilter, f0, HL1_FILTER));
             }
            if(longMode != LONG && time == 1) {
                 res.add(stats(o, tFilter, f0, L26_FILTER));
             }
             if(longMode == LONG) {
                 res.add(stats(o, tFilter, f0, HL2_FILTER));
             }
             if(longMode == LONG) {
                 res.add(stats(o, tFilter, f0, HL3_FILTER));
             }
             if(longMode == LONG) {
                 res.add(stats(o, tFilter, f0, HL4_FILTER));
             }
             if(longMode == LONG) {
                 res.add(stats(o, tFilter, f0, HL5_FILTER));
             }
             if(longMode == LONG) {
                 res.add(stats(o, tFilter, f0, HL6_FILTER));
             }
            if(longMode == LONG || time == 1) {
                 res.add(stats(o, tFilter, f0, HL7_FILTER));
             }
            if(longMode == LONG || time == 1) {
                 res.add(stats(o, tFilter, f0, HL8_FILTER));
             }
         }
     }
     o.close();
     return res;
 	}
 
 	public static void main(String[] args)
 		throws Exception
 	{
     int longMode = SHORT;
 		Phase1 p1 = new Phase1(longMode);
     CombinedSummarizer o = new CombinedSummarizer();
     o.addSummarizer(new XSLTSummarizer());
     o.addSummarizer(new XSLTSummarizer("layout1.xsl", "out.html"));
 		for(String arg : args) p1.add(new File(arg));
     p1.dumpCSV("out.csv",";");
     p1.runFilterStack(SHORT, o);
     List<FullResult> allFRes = p1.runFilterStack(LONG, NO_SUMMARY);
     FullResult f0 = allFRes.get(0);
     for(String key : f0.keys()) {
         selectiveAnalysis(key, allFRes);
     }
   }
 
 }
