 package team.splunk.csc480.handler;
 
 import team.splunk.csc480.data.DataItem;
 import team.splunk.csc480.data.DataItem.*;
 import team.splunk.csc480.data.DataSource;
 import team.splunk.csc480.data.FileDataSource;
 import team.splunk.csc480.researcher.ErrorResearcher;
 import team.splunk.csc480.researcher.FrequentAccessResearcher;
 import team.splunk.csc480.researcher.Researcher;
 
 import java.io.FileNotFoundException;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Starts all available Researchers in a new thread and gives them a callback
  * to use when reporting security threats.
  *
  * @author Team Splunk
  * @version 3.14
  * @date Nov. 13, 2012
  */
 public class Commander implements ThreatHandler, Broadcaster {
 
    private final static class ResearcherAvg {
       private long sumScores;
       private int numScores;
 
       public ResearcherAvg() { sumScores = 0; numScores = 0; }
 
       public void addScore(long score) { sumScores += score; numScores++; }
       public double getScoreAvg() { return sumScores / (numScores > 0 ? numScores : -1); }
    }
 
    private final class ScoreCard {
       public final long total;
       public final long current;
       private final long updated;
 
       public static final long NOW = -1;
 
       public ScoreCard(long total, long current, long updated) {
          this.total = total;
          this.current = current;
          this.updated = updated;
       }
 
       public ScoreCard calculate(long severity, long newTime) {
          long pointsOff = (newTime - updated) / msPerPoint;
          long score = (current - pointsOff) + severity;
 
          return new ScoreCard(total + severity, (score > 0) ? score : 0, newTime);
       }
 
       /**
        * Shortcut for this.calculate(0, time)
        *
        * @param time the time at which to retrieve this ScoreCard
        * @return a new ScoreCard with totals for the time indicated
        */
       public ScoreCard at(long time) {
          return calculate(0, (time == NOW) ? System.currentTimeMillis() : time);
       }
 
       @Override
       public String toString() {
          return "Total: " + total + ", Current: " + current;
       }
    }
 
    private List<DataSource> sources;
    private Map<String, Researcher> researchers;
    private Map<String, ResearcherAvg> resAvg = new HashMap<String, ResearcherAvg>();
 
    private Map<IPAddress, ScoreCard> levels = new HashMap<IPAddress, ScoreCard>();
 
    private long threshold = 3000000L;
    private long msPerPoint = 1000L;
 
    private static final int DEFAULT_OFFENDERS = 10;
 
    private static final String QUIT_PATTERN = "(?i)(q|quit)$";
    private static final String HELP_PATTERN = "(?i)(h|help)$";
    private static final String VIEW_PATTERN = "(?i)(v|view)$";
    private static final String NOTIFY_PATTERN = "(?i)(n|notify) (\\d*)$";
 
    private static final String HELP_TEXT = "Usage: \n" +
      " quit: stops the program\n" +
      " view: views the top 10 offenders at this point in time\n" +
      " worst: views the top 10 high scores since we started collecting data\n" +
      " notify [num]: sets a threshold for notification; user receives message for any" +
        " IP above [num] score\n" +
      " help: this message";
 
    /**
     * Initializes the list of sources and researchers, then sets itself to be
     * the ThreatHandler for each Researcher.
     *
     * @param researchers the list of Researchers to run on the data
     * @param sources the list of sources to receive events and forward them to
     *                each researcher
     */
    public Commander(Map<String, Researcher> researchers, List<DataSource> sources) {
       this.researchers = researchers;
       this.sources = sources;
 
       for (String r : researchers.keySet()) {
          researchers.get(r).setThreatHandler(this);
          resAvg.put(r, new ResearcherAvg());
       }
 
       for (DataSource ds : sources) {
          ds.setBroadcaster(this);
       }
    }
 
    /**
     * Start/resume all of the DataSources and run for 30 secs.
     */
    public void start() {
       for (DataSource source : sources) {
          source.startThread();
       }
    }
 
    /**
     * Stop all DataSource.
     */
    public void stop() {
       for (DataSource source : sources) {
          source.stopNow();
       }
    }
 
    /**
     * Receives threats from Researchers and does any required processing or
     * reporting on them.
     *
     * @param t a threat from a Researcher
     */
    @Override
    public synchronized void reportThreat(Threat t, String from) {
       ScoreCard threat = levels.get(t.addr);
 
       resAvg.get(from).addScore(t.level.severity);
       long severity = Math.round(t.level.severity / resAvg.get(from).getScoreAvg() * ThreatLevel.ORANGE.severity);
 
       if (threat == null) {
          threat = new ScoreCard(t.level.severity, t.level.severity, t.timeMillis);
       }
       else {
          threat = threat.calculate(t.level.severity, t.timeMillis);
       }
 
       levels.put(t.addr, threat);
 
       if (threat.current > getThreshold()) {
          Date date = new Date(t.timeMillis);
          System.out.println("[" + date + "] IP over Threshold: " + t.addr + ", " + threat);
       }
    }
 
    /**
     * Receives events from DataSources and broadcasts them to all Researchers.
     *
     * @param item the DataItem to broadcast
     */
    @Override
    public synchronized void broadcastEvent(DataItem item) {
       for (Researcher r : researchers.values())
          r.reportEvent(item);
    }
 
    /**
     * Returns a list of the n worst offenders, where n = DEFAULT_OFFENDERS,
     * and their scores
     *
     * @return a list of ThreatResults
     */
    public List<ThreatResult> getWorstOffenders() {
       return getWorstOffenders(DEFAULT_OFFENDERS, ScoreCard.NOW);
    }
 
    /**
     * Returns a list of the n worst offenders at the time specified
     * TODO: add this option to the user interface
     *
     * @return a list of ThreatResults representing the worst offenders
     */
    public List<ThreatResult> getWorstOffenders(Date date) {
       return getWorstOffenders(DEFAULT_OFFENDERS, date.getTime());
    }
 
    /**
     * Notifies the user about any IP address with a current score greater
     * than the threshold given. Any subsequent calls to this method clears
     * the previously set threshold.
     *
     * @param threshold the minimum score to notify the user about
     */
    public synchronized void setThreshold(int threshold) {
       this.threshold = threshold;
    }
 
    /**
     * Gets the current score threshold for notification
     *
     * @return the score threshold
     */
    public synchronized long getThreshold() {
       return threshold;
    }
 
    /**
     * Returns a list of the `num` worst offenders and their scores
     *
     * @param num the number of offenders to return
     * @return a list of ThreatResults
     */
    public synchronized List<ThreatResult> getWorstOffenders(int num, long time) {
       List<ThreatResult> offenders = new ArrayList<ThreatResult>();
       List<ThreatResult> offNow = new ArrayList<ThreatResult>(num);
 
       for (IPAddress addr : levels.keySet()) {
          offenders.add(new ThreatResult(addr, levels.get(addr).current));
       }
 
       Collections.sort(offenders, new Comparator<ThreatResult>() {
          @Override
          public int compare(ThreatResult o1, ThreatResult o2) {
             if (o1.result == o2.result)
                return 0;
             else
                return o1.result > o2.result ? 1 : -1;
          }
       });
 
       for (int i = 0, tot = offenders.size(); i < num && i < tot; i++) {
          ThreatResult tr = offenders.get(i);
          offNow.add(new ThreatResult(tr.address, levels.get(tr.address).at(time).current));
       }
 
       return offNow;
    }
 
    /**
     * Main method
     */
    public static void main(String[] args) {
       Map<String, Researcher> researchers = new HashMap<String, Researcher>();
       List<DataSource> sources = new ArrayList<DataSource>();
 
       try {
          researchers.put("error", new ErrorResearcher("error"));
          researchers.put("frequent", new FrequentAccessResearcher("frequent"));
          sources.add(new FileDataSource("error_log"));
          sources.add(new FileDataSource("access_log"));
       }
       catch (FileNotFoundException e) {
          e.printStackTrace();
       }
 
       Commander shepard = new Commander(researchers, sources);
 
       try {
          String input;
          Scanner sysIn = new Scanner(System.in);
          shepard.start();
 
          Matcher match;
          Pattern quit = Pattern.compile(QUIT_PATTERN);
          Pattern help = Pattern.compile(HELP_PATTERN);
          Pattern view = Pattern.compile(VIEW_PATTERN);
          Pattern notify = Pattern.compile(NOTIFY_PATTERN);
 
          while ((input = sysIn.nextLine()) != null) {
             if (quit.matcher(input).find())
                break;
 
             else if (help.matcher(input).find())
                System.out.println(HELP_TEXT);
 
             else if (view.matcher(input).find()) {
                List<ThreatResult> results = shepard.getWorstOffenders();
                System.out.println("CURRENT STATS:");
 
                for (ThreatResult res : results) {
                   System.out.println(res.toString());
                }
             }
 
             else if ((match = notify.matcher(input)).find()) {
                Integer threshold = Integer.parseInt(match.group(2));
                shepard.setThreshold(threshold);
             }
          }
       }
       catch (Exception e) {
          System.err.println("ERROR!!");
          e.printStackTrace();
       }
       finally {
          shepard.stop();
       }
    }
 }
