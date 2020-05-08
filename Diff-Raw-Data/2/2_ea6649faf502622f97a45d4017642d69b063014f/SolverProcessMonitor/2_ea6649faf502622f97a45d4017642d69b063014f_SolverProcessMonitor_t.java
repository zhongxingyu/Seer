 package edu.mit.cci.amtprojects.solver;
 
 import edu.mit.cci.amtprojects.BatchProcessMonitor;
 import edu.mit.cci.amtprojects.DbProvider;
 import edu.mit.cci.amtprojects.HitManager;
 import edu.mit.cci.amtprojects.kickball.cayenne.Batch;
 import edu.mit.cci.amtprojects.kickball.cayenne.Hits;
 import edu.mit.cci.amtprojects.kickball.cayenne.TurkerLog;
 import edu.mit.cci.amtprojects.util.Mailer;
 import edu.mit.cci.amtprojects.util.MturkUtils;
 import edu.mit.cci.amtprojects.util.Utils;
 import jsc.datastructures.MatchedData;
 import jsc.descriptive.MeanVar;
 import jsc.relatedsamples.FriedmanTest;
 import org.apache.cayenne.DataObjectUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.log4j.Logger;
 import org.apache.wicket.ajax.json.JSONException;
 import org.apache.wicket.ajax.json.JSONObject;
 
 import javax.mail.MessagingException;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.*;
 
 /**
  * User: jintrone
  * Date: 10/15/12
  * Time: 2:55 PM
  */
 public class SolverProcessMonitor extends BatchProcessMonitor {
 
 
     private static Logger logger = Logger.getLogger(SolverProcessMonitor.class);
 
     public SolverProcessMonitor(Batch b) {
         super(b);
     }
 
     public static SolverProcessMonitor get(Batch b) {
         try {
             return (SolverProcessMonitor) get(b, SolverProcessMonitor.class);
         } catch (NoSuchMethodException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (InvocationTargetException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (IllegalAccessException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (InstantiationException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         return null;
     }
 
 
     public void update() throws UnsupportedEncodingException, JSONException {
         logger.info("Checking workflow status");
         Batch b = batch();
         SolverTaskModel model = new SolverTaskModel(b);
         if (model.getCurrentStatus().getPhase() == Phase.COMPLETE) {
 
             t.cancel();
         }
 
         int currentRound = model.getCurrentStatus().getCurrentRound();
         HitManager manager = HitManager.get(b);
         manager.updateHits();
         List<TurkerLog> logs = manager.getNewHitResults(true);
         List<TurkerLog> roundLogs = findCurrentLogs(currentRound, model.getCurrentStatus().getPhase(), logs);
         logger.info("Got "+roundLogs.size()+" logs");
         boolean shouldLaunch = true;
         if (!roundLogs.isEmpty()) {
             if (model.getCurrentStatus().getPhase() == Phase.INIT) {
                 if (doneRanking(model, roundLogs)) {
                     setProcessed(roundLogs);
                     updateRanks(b, model, roundLogs);
                     model.getCurrentStatus().setPhase(Phase.GENERATE);
                 } else {
                     shouldLaunch = false;
                 }
 
             } else if (model.getCurrentStatus().getPhase() == Phase.RANK) {
                 if (doneRanking(model, roundLogs)) {
                     updateRanks(b, model, roundLogs);
                     pruneSolutions(model);
                     if (currentRound + 1 == model.getNumberOfRounds()) {
                         model.getCurrentStatus().setPhase(Phase.COMPLETE);
                     } else {
                         model.getCurrentStatus().setPhase(Phase.GENERATE);
                         model.getCurrentStatus().setCurrentRound(currentRound + 1);
                     }
                 } else {
                     shouldLaunch = false;
                 }
 
 
             } else if (model.getCurrentStatus().getPhase() == Phase.GENERATE) {
                 updateAnswers(model, roundLogs);
                 model.getCurrentStatus().setPhase(Phase.VALIDATION);
 
             } else if (model.getCurrentStatus().getPhase() == Phase.VALIDATION) {
                 if (checkValidation(model, roundLogs)) {
                     setProcessed(roundLogs);
                     model.getCurrentStatus().setPhase(Phase.RANK);
                 } else {
                     shouldLaunch = false;
                 }
 
             }
 
             model.updateCurrentStatus();
             DbProvider.getContext().commitChanges();
             if (model.getCurrentStatus().getPhase() != Phase.COMPLETE && shouldLaunch) {
                 SolverHitCreator.getInstance().launch(null, null, b);
 
             }
 
         }
 
 
     }
 
     private boolean doneRanking(SolverTaskModel model, List<TurkerLog> roundLogs) {
         int dimcount = model.getRankDimensions().length;
         Set<String> ids = new HashSet<String>();
         for (TurkerLog l : roundLogs) {
            ids.add(MturkUtils.extractAnswer(l,"dimension"));
         }
         logger.info("Num dims available = "+dimcount+", dims found = "+ids);
         return ids.size() == dimcount;
 
     }
 
     private boolean checkValidation(SolverTaskModel model, List<TurkerLog> roundLogs) throws JSONException {
         Map<Long, Solution> needsAttention = new HashMap<Long, Solution>();
         int validated = 0;
         for (Solution s : model.getCurrentStatus().getCurrentAnswers()) {
             if (s.getValidEnum() == Solution.Valid.UNKNOWN) {
                 needsAttention.put(s.getId(), s);
                 validated++;
             } else if (s.getValidEnum() == Solution.Valid.NEEDS_APPROVAL) {
                 validated++;
             } else if (s.getValidEnum() == Solution.Valid.INVALID) {
                 model.getCurrentStatus().removeFromCurrentAnswers(s);
                 HitManager.get(model.getBatch()).rejectAssignments(new String[]{s.getAssignmentId()}, "The answer you provided was judged to be invalid");
             }
         }
         if (!needsAttention.isEmpty()) {
             for (TurkerLog log : roundLogs) {
                 String phase = MturkUtils.extractAnswer(log, "phase");
                 if (!Phase.VALIDATION.name().equals(phase)) continue;
                 Solution t = needsAttention.get(Long.parseLong(MturkUtils.extractAnswer(log, "answerId")));
                 if (t != null) {
                     boolean isEmpty = MturkUtils.extractAnswer(log, "blank") != null;
                     boolean isNonesense = MturkUtils.extractAnswer(log, "nonsense") != null;
                     Set<String> copies = new HashSet<String>();
                     String copiedIds = MturkUtils.extractAnswer(log, "copies");
                     if (copiedIds != null && !copiedIds.isEmpty()) {
                         for (String c : copiedIds.split("\\|")) {
                             if (c == null || c.isEmpty()) continue;
                             Solution ps = DataObjectUtils.objectForPK(DbProvider.getContext(), Solution.class, Long.parseLong(c));
                             if (ps == null) {
                                 logger.error("Cannot identify solution " + c);
 
                             } else {
                                 copies.add(c);
 
                             }
                         }
 
 
                     }
 
                     if (isEmpty || isNonesense || !copies.isEmpty()) {
                         Map<String, Object> jsonMap = Utils.mapify("empty", isEmpty, "nonsense", isNonesense, "copyof", copies);
                         t.setMeta(new JSONObject(jsonMap).toString());
                         t.setValid(Solution.Valid.NEEDS_APPROVAL);
                         notifyBatchOwner();
 
                     } else {
                         t.setValid(Solution.Valid.VALID);
                         validated--;
                     }
 
 
                 }
             }
         }
 
         return validated == 0;
     }
 
     //TODO
     private void notifyBatchOwner() {
 
         Mailer mailer = Mailer.get();
         if (mailer == null) {
             logger.warn("Mailer could not be configured, not messaging");
         } else {
             try {
                 mailer.sendMail("jintrone@gmail.com", batch().getContactEmail(), "[TURKMDR] A hit is awaiting your approval", "New hits have been added to the approval page");
             } catch (MessagingException e) {
                 logger.warn("Error sending message");
             }
         }
 
     }
 
 
     private void pruneSolutions(SolverTaskModel model) throws JSONException {
         List<Solution> sol = new ArrayList<Solution>(model.getCurrentStatus().getCurrentAnswers());
         if (sol.size() > model.getSizeOfFront()) {
             Collections.sort(sol, new Comparator<Solution>() {
 
                 public int compare(Solution solution, Solution solution1) {
                     Float r1 = solution.getLastRank().getRankValue();
                     Float r2 = solution1.getLastRank().getRankValue();
                     return -1 * r1.compareTo(r2);
 
                 }
             });
 
             sol = sol.subList(0, model.getSizeOfFront());
             model.getCurrentStatus().setCurrentAnswers(sol);
             model.updateCurrentStatus();
         }
 
     }
 
 
     private void updateAnswers(SolverTaskModel model, List<TurkerLog> roundLogs) {
         for (TurkerLog l : roundLogs) {
             String text = MturkUtils.extractAnswer(l, "solutiontext");
 
             Solution s = DbProvider.getContext().newObject(Solution.class);
             s.setText(text);
             s.setAssignmentId(l.getAssignmentId());
             s.setCreation(l.getDate());
             s.setRound(Integer.parseInt(MturkUtils.extractAnswer(l, "round")));
             s.setWorkerId(l.getWorkerId());
             s.setToQuestion(model.getQuestion());
             s.setValid(Solution.Valid.UNKNOWN);
 
 
             String parentText = MturkUtils.extractAnswer(l, "parents");
             if (parentText != null && !parentText.isEmpty()) {
                 String[] parents = MturkUtils.extractAnswer(l, "parents").split("\\|");
                 for (String p : parents) {
                     if (p == null || p.isEmpty()) continue;
                     Solution ps = DataObjectUtils.objectForPK(DbProvider.getContext(), Solution.class, Long.parseLong(p));
                     if (ps == null) {
                         logger.error("Cannot identify parent " + p + " for new solution");
 
                     } else {
                         s.addToToParents(ps);
                     }
                 }
 
             }
             DbProvider.getContext().commitChanges();
             model.getCurrentStatus().addToCurrentAnswers(s);
 
         }
         setProcessed(roundLogs);
         DbProvider.getContext().commitChanges();
     }
 
     public void setProcessed(List<TurkerLog> logs) {
         Set<Hits> hits = new HashSet<Hits>();
         for (TurkerLog l : logs) {
             if (hits.add(l.getHit())) {
                 if (l.getHit() == null) {
                     logger.warn("hit for log " + l.getObjectId() + " is null");
                 } else {
                     l.getHit().setProcessed(true);
                 }
             }
         }
     }
 
     private void updateRanks(Batch b, SolverTaskModel model, List<TurkerLog> logs) throws JSONException {
 
 
         //map of each turkers assessment
 
         //debug
         if (logs.size() == 1) {
             TurkerLog log = new TurkerLog();
             log.setData(logs.get(0).getData());
             logs.add(log);
         }
 
         String hitId = logs.get(0).getHit().getId();
         int round = Integer.parseInt(MturkUtils.extractAnswer(logs.get(0), "round"));
 
         Map<TurkerLog, double[]> response = new HashMap<TurkerLog, double[]>();
         SolverTaskStatus status = model.getCurrentStatus();
         List<Solution> answers = new ArrayList<Solution>(status.getCurrentAnswers());
 
         Map<Integer, double[]> ranksByDim = new HashMap<Integer, double[]>();
 
 
         for (TurkerLog log : logs) {
 
             String phase = MturkUtils.extractAnswer(log, "phase");
             if (!Phase.RANK.name().equals(phase) && !Phase.INIT.name().equals(phase)) continue;
             List<Double> ranks = new ArrayList<Double>();
 
             for (Solution s : answers) {
                 double rank = (1 + answers.size() - Integer.parseInt(MturkUtils.extractAnswer(log, "Solution." + s.getId()))) / (double) (answers.size());
 
 
                 ranks.add(rank);
             }
             double[] rankingArray = ArrayUtils.toPrimitive(ranks.toArray(new Double[ranks.size()]), 0d);
             response.put(log, rankingArray);
             int dim = Integer.parseInt(MturkUtils.extractAnswer(log, "dimension"));
             if (!ranksByDim.containsKey(dim)) {
                 ranksByDim.put(dim, new double[answers.size()]);
             }
             for (int i = 0; i < rankingArray.length; i++) {
                 ranksByDim.get(dim)[i] += rankingArray[i] / (double) model.getRankDimensions().length;
             }
 
 
         }
 
 
         double[][] ranks = response.values().toArray(new double[response.size()][]);
         logger.info("Ranks are:");
         for (double[] rank1 : ranks) {
             logger.info(Arrays.toString(rank1));
         }
 
         double[] finalRanks = new double[answers.size()];
         float w = (float) new FriedmanTest(new MatchedData(ranks)).getW();
         logger.info("W = " + w);
         //update ranks
         for (int s = 0; s < answers.size(); s++) {
             double[] row = new double[logs.size()];
             for (int r = 0; r < logs.size(); r++) {
                 row[r] = ranks[r][s];
             }
             MeanVar mv = new MeanVar(row);
             SolutionRank solutionRank = DbProvider.getContext().newObject(SolutionRank.class);
             solutionRank.setDate(new Date());
             solutionRank.setRound(round);
             solutionRank.setHitId(hitId);
             try {
                 finalRanks[s] = mv.getMean();
                 solutionRank.setRank(w, (float) mv.getSd(), (float) finalRanks[s]);
             } catch (JSONException ex) {
                 logger.error("Could not serialize ranks");
             }
             answers.get(s).addToToRanks(solutionRank);
         }
 
         setProcessed(logs);
 
         DbProvider.getContext().commitChanges();
 
         //assign bonus to rankers based on kendall's W
         for (int r = 0; r < logs.size(); r++) {
             int dim = Integer.parseInt(MturkUtils.extractAnswer(logs.get(r), "dimension"));
             float agreement = (float) new FriedmanTest(new MatchedData(new double[][]{ranksByDim.get(dim), ranks[r]})).getW();
             float bonus = agreement * model.getMaxRankingBonus();
             String feedback = String.format("Your ranking was similar to the mean ranking along dimension " + model.getRankDimensions()[dim] + "with a score of %.2f as assessed by Kendall's W. You are thus granted a bonus " +
                     " of %.2f * %.2f = $%.2f", agreement, agreement, model.getMaxRankingBonus(), bonus);
             if (logs.get(r).getAssignmentId() == null || logs.get(r).getAssignmentId().isEmpty()) {
                 continue;
             }
             logger.info("Attempting to assign bonus of "+bonus+" to "+logs.get(r).getAssignmentId()+" on dimension "+model.getRankDimensions()[dim]);
             HitManager.get(b).bonusAssignments(new String[]{logs.get(r).getAssignmentId()}, feedback, bonus);
         }
 
         //assign bonus to generators
         Collections.sort(answers, new Comparator<Solution>()
 
         {
             public int compare(Solution solution, Solution solution1) {
                 return -1 * ((Float) solution.getLastRank().getRankValue()).compareTo(solution1.getLastRank().getRankValue());
             }
         });
 
         for (int i = 0; i < Math.min(model.getSizeOfFront(), answers.size()); i++) {
             Solution s = answers.get(i);
             if (s.getToParents().isEmpty() && s.getRound() == status.getCurrentRound()) {
                 float rank = s.getLastRank().getRankValue();
                 float bonus = rank * model.getMaxGeneratingBonus();
                 String feedback = String.format("Your solution achieved a rank of %.2f (on a 0 - 1 scale) and so" +
                         "you are granted a bonus of %.2f * $%.2f = %.2f", rank, rank, model.getMaxGeneratingBonus(), bonus);
                 HitManager.get(b).bonusAssignments(new String[]{s.getAssignmentId()}, feedback, bonus);
 
             } else if (!s.getToParents().isEmpty() && s.getRound() == status.getCurrentRound()) {
                 float old = 0.0f;
                 int count = 0;
                 for (Solution sp : s.getToParents()) {
                     count++;
                     old = Math.max(sp.getLastRank().getRankValue(), old);
 
                 }
 
                 float improvement = s.getLastRank().getRankValue() - old;
                 float maxbonus = count > 1 ? model.getMaxCombiningBonus() : model.getMaxImprovingBonus();
                 float bonus = improvement * maxbonus;
                 bonus = Float.parseFloat(String.format("%.2f", bonus));
                 if (bonus > 0) {
                     String feedback = String.format("Your solution achieved an improvement of %.2f (on a 0 - 1 scale) over its progenitors and so" +
                             "you are granted a bonus of %.2f * $%.2f = %.2f", improvement, improvement, maxbonus, bonus);
                     HitManager.get(b).bonusAssignments(new String[]{s.getAssignmentId()}, feedback, bonus);
                 }
             }
         }
 
     }
 
     private List<TurkerLog> findCurrentLogs(int currentRound, Phase currentPhase, List<TurkerLog> logs) {
         for (Iterator<TurkerLog> itl = logs.iterator(); itl.hasNext(); ) {
             TurkerLog log = itl.next();
             if (Integer.parseInt(MturkUtils.extractAnswer(log, "round")) != currentRound || !MturkUtils.extractAnswer(log, "phase").equals(currentPhase.name())) {
                 logger.warn("Removing a log (" + log.getObjectId() + ") this is odd!");
                 itl.remove();
             }
 
         }
         return logs;
     }
 
 
     public static enum Phase {
         INIT, GENERATE, RANK, VALIDATION, COMPLETE
     }
 
 }
