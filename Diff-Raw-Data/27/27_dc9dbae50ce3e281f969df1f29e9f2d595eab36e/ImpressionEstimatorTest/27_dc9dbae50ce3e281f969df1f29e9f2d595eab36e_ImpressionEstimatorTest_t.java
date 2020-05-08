 package simulator.predictions;
 
 import edu.umich.eecs.tac.props.*;
 import models.queryanalyzer.AbstractQueryAnalyzer;
 import models.queryanalyzer.CarletonQueryAnalyzer;
 import models.queryanalyzer.ds.QAInstance;
 import models.queryanalyzer.iep.*;
 import models.usermodel.ParticleFilterAbstractUserModel.UserState;
 import simulator.parser.GameStatus;
 import simulator.parser.GameStatusHandler;
 
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.*;
 
 public class ImpressionEstimatorTest {
 
 
    public ImpressionEstimatorTest() {
       //Use default configuration
    }
 
    public ImpressionEstimatorTest(boolean sampleAvgPositions, boolean perfectImps, boolean useWaterfallPriors) {
       SAMPLED_AVERAGE_POSITIONS = sampleAvgPositions;
       PERFECT_IMPS = perfectImps;
       USE_WATERFALL_PRIORS = useWaterfallPriors;
    }
 
   private double PRIOR_STDEV_MULTIPLIER = .5; // 0 -> perfectPredictions. 1 -> stdev=1*meanImpsPrior
    private boolean SAMPLED_AVERAGE_POSITIONS = false;
    public static boolean PERFECT_IMPS = true;
    boolean USE_WATERFALL_PRIORS = true;
    boolean CONSIDER_ALL_PARTICIPANTS = true;
    GameSet GAMES_TO_TEST = GameSet.test2010;
 
    //if we're sampling average positions, do we want to remove agents that received no samples?
    //(if we're using exact average positions anyway, true/false has no effect)
    boolean REMOVE_SAMPLE_NANS = false;
 
 
    BufferedWriter bufferedWriter = null;
 
    int NUM_SLOTS = 5;
    public static final int MAX_F0_IMPS = 10969;
    public static final int MAX_F1_IMPS = 1801;
    public static final int MAX_F2_IMPS = 1468;
    public static int LDS_ITERATIONS_1 = 10;
    public static int LDS_ITERATIONS_2 = 10;
    private static boolean REPORT_FULLPOS_FORSELF = true;
 
    //Performance metrics
    int numInstances = 0;
    int numSkips = 0;
    int numImprsPredictions = 0;
    int numOrderingPredictions = 0;
    int numCorrectlyOrderedInstances = 0;
    double aggregateAbsError = 0;
 
    private enum SolverType {
       CP, MIP, LDSMIP
    }
 
    private enum GameSet {
       finals2010, semifinals2010, test2010
    }
 
    public ArrayList<String> getGameStrings() {
       String baseFile = "./game";
       int min = 1;
       int max = 8;
 
       if (GAMES_TO_TEST == GameSet.test2010) {
          min = 1;
          max = 1;
       }
 
       ArrayList<String> filenames = new ArrayList<String>();
       for (int i = min; i <= max; i++) {
          filenames.add(baseFile + i + ".slg");
       }
       return filenames;
    }
 
 
    /**
     * Debugging method used to print out whatever data I want from the game logs
     *
     * @throws ParseException
     * @throws IOException
     */
    public void printGameLogInfo() throws IOException, ParseException {
       ArrayList<String> filenames = getGameStrings();
       for (int gameIdx = 0; gameIdx < filenames.size(); gameIdx++) {
          String filename = filenames.get(gameIdx);
          GameStatus status = new GameStatusHandler(filename).getGameStatus();
          double reserve = status.getReserveInfo().getRegularReserve();
          double promotedReserve = status.getReserveInfo().getPromotedReserve();
          double numPromotedSlots = status.getSlotInfo().getPromotedSlots();
 //			double approxPromotedReserve = getApproximatePromotedReserveScore(status);
 //			System.out.println("reserve="+reserve+", promotedReserve="+promotedReserve+", approxPromotedReserve="+approxPromotedReserve+", numPromotedSlots="+numPromotedSlots);
          // Make the query space
          LinkedHashSet<Query> querySpace = new LinkedHashSet<Query>();
          querySpace.add(new Query(null, null)); //F0
          for (Product product : status.getRetailCatalog()) {
             querySpace.add(new Query(product.getManufacturer(), null)); // F1 Manufacturer only
             querySpace.add(new Query(null, product.getComponent())); // F1 Component only
             querySpace.add(new Query(product.getManufacturer(), product.getComponent())); // The F2 query class
          }
          Query[] queryArr = new Query[querySpace.size()];
          querySpace.toArray(queryArr);
          int numQueries = queryArr.length;
 
          // Make predictions for each day/query in this game
          int numReports = 57;//57; //TODO: Why?
          for (int d = 0; d < numReports; d++) {
             for (int queryIdx = 0; queryIdx < numQueries; queryIdx++) {
                Query query = queryArr[queryIdx];
                Integer[] imps = getAgentImpressions(status, d, query);
                Integer[] promotedImps = getAgentPromotedImpressions(status, d, query);
                System.out.println("promotedImps=" + Arrays.toString(promotedImps) + "\timps=" + Arrays.toString(imps));
             }
          }
       }
       throw new RuntimeException("Finished printing game log. Aborting.");
    }
 
 
    public void logGameData() throws IOException, ParseException {
 
       //------ CREATE DATA FILE
       String logFilename = "gamedata.txt";
       try {
          //Construct the BufferedWriter object
          bufferedWriter = new BufferedWriter(new FileWriter(logFilename));
 
          //Create header
          StringBuffer sb = new StringBuffer();
          sb.append("game\t");
          sb.append("agent\t");
          sb.append("query\t");
          sb.append("day\t");
          sb.append("bid\t");
          sb.append("squashed.bid\t");
          sb.append("budget\t");
          sb.append("imps\t");
          sb.append("clicks\t");
          sb.append("convs\t");
          sb.append("avg.pos\t");
          sb.append("cost");
 
          //Start writing to the output stream
          bufferedWriter.write(sb.toString());
          bufferedWriter.newLine();
       } catch (FileNotFoundException ex) {
          ex.printStackTrace();
       } catch (IOException ex) {
          ex.printStackTrace();
       }
 
 
       //------------------- GET GAME DATA ---------------
 
       ArrayList<String> filenames = getGameStrings();
       for (int gameIdx = 0; gameIdx < filenames.size(); gameIdx++) {
          String filename = filenames.get(gameIdx);
 
          // Load this game and its basic parameters
          GameStatus status = new GameStatusHandler(filename).getGameStatus();
          int NUM_PROMOTED_SLOTS = status.getSlotInfo().getPromotedSlots();
          HashMap<QueryType, Double> promotedReserveScore = getApproximatePromotedReserveScore(status);
 
          // Make the query space
          LinkedHashSet<Query> querySpace = new LinkedHashSet<Query>();
          querySpace.add(new Query(null, null)); //F0
          for (Product product : status.getRetailCatalog()) {
             querySpace.add(new Query(product.getManufacturer(), null)); // F1 Manufacturer only
             querySpace.add(new Query(null, product.getComponent())); // F1 Component only
             querySpace.add(new Query(product.getManufacturer(), product.getComponent())); // The F2 query class
          }
 
          Query[] queryArr = new Query[querySpace.size()];
          querySpace.toArray(queryArr);
          int numQueries = queryArr.length;
 
          // Make predictions for each day/query in this game
          int numReports = 57; //TODO: Why?
          for (int d = 0; d < numReports; d++) {
             for (int queryIdx = 0; queryIdx < numQueries; queryIdx++) {
                Query query = queryArr[queryIdx];
                String[] agents = status.getAdvertisers();
                Double[] bids = getBids(status, d, query);
                Double[] squashedBids = getSquashedBids(status, d, query);
                Double[] budgets = getBudgets(status, d, query);
                Integer[] impressions = getAgentImpressions(status, d, query);
                Integer[] clicks = getAgentClicks(status, d, query);
                Integer[] conversions = getAgentConversions(status, d, query);
                Double[] actualAveragePositions = getAveragePositions(status, d, query);
                Double[] cost = getAgentCosts(status, d, query);
                //Double[] sampledAveragePositions = getSampledAveragePositions(status, d, query);
                //Integer[] promotedImpressions = getAgentPromotedImpressions(status, d, query);
                //Boolean[] promotionEligibility = getAgentPromotionEligibility(status, d, query, promotedReserveScore.get(query.getType()));
 
                StringBuffer sb = new StringBuffer();
                for (int a = 0; a < agents.length; a++) {
                   sb.append(filename + "\t");
                   sb.append(agents[a] + "\t");
                   sb.append(query + "\t");
                   sb.append(d + "\t");
                   sb.append(bids[a] + "\t");
                   sb.append(squashedBids[a] + "\t");
                   sb.append(budgets[a] + "\t");
                   sb.append(impressions[a] + "\t");
                   sb.append(clicks[a] + "\t");
                   sb.append(conversions[a] + "\t");
                   sb.append(actualAveragePositions[a] + "\t");
                   sb.append(cost[a] + "\n");
                }
                writeToLog(sb.toString());
             }
          }
       }
       closeLog();
    }
 
 
    /**
     * Load a game
     * For each day,
     * Get all query reports that came in on that day
     * From these, infer: numSlots, numAgents, avgPos[], agentIds[], ourAgentIdx, ourImpressions, impressionsUB
     * also infer squashed bid ordering.
     * Input these into the given Impression Estimator (via a QA instance)
     * Compare output impsPer
     *
     * @param impressionEstimatorIdx
     * @throws IOException
     * @throws ParseException
     */
    public void impressionEstimatorPredictionChallenge(SolverType impressionEstimatorIdx, boolean orderingKnown) throws IOException, ParseException {
       //printGameLogInfo();
 
 
       StringBuffer sb1 = new StringBuffer();
       sb1.append("iePred");
       sb1.append("." + impressionEstimatorIdx);
       sb1.append(SAMPLED_AVERAGE_POSITIONS ? ".sampled" : ".exact");
       sb1.append(PERFECT_IMPS ? ".impsPerfect" : ".impsUB");
       sb1.append(USE_WATERFALL_PRIORS ? ".prior" : ".noPrior");
       sb1.append("." + GAMES_TO_TEST);
       sb1.append(".txt");
       String logFilename = sb1.toString();
 
 
       initializeLog(logFilename);
       numInstances = 0;
       numSkips = 0;
       numImprsPredictions = 0;
       numOrderingPredictions = 0;
       numCorrectlyOrderedInstances = 0;
       aggregateAbsError = 0;
       ArrayList<String> filenames = getGameStrings();
 
       for (int gameIdx = 0; gameIdx < filenames.size(); gameIdx++) {
          String filename = filenames.get(gameIdx);
 
          // Load this game and its basic parameters
          GameStatus status = new GameStatusHandler(filename).getGameStatus();
          int NUM_PROMOTED_SLOTS = status.getSlotInfo().getPromotedSlots();
          HashMap<QueryType, Double> promotedReserveScore = getApproximatePromotedReserveScore(status);
 
          // Make the query space
          LinkedHashSet<Query> querySpace = new LinkedHashSet<Query>();
          querySpace.add(new Query(null, null)); //F0
          for (Product product : status.getRetailCatalog()) {
             querySpace.add(new Query(product.getManufacturer(), null)); // F1 Manufacturer only
             querySpace.add(new Query(null, product.getComponent())); // F1 Component only
             querySpace.add(new Query(product.getManufacturer(), product.getComponent())); // The F2 query class
          }
 
          Query[] queryArr = new Query[querySpace.size()];
          querySpace.toArray(queryArr);
          int numQueries = queryArr.length;
 
          // Make predictions for each day/query in this game
          int numReports = 57; //TODO: Why?
			for (int d=0; d<=10; d++) {
//         for (int d = 0; d < numReports; d++) {
 
 //				for (int queryIdx=11; queryIdx<=11; queryIdx++) {
             for (int queryIdx = 0; queryIdx < numQueries; queryIdx++) {
                numInstances++;
 
                Query query = queryArr[queryIdx];
 
 //               if(!"pg".equals(query.getManufacturer()) ||
 //                       !"audio".equals(query.getComponent())) continue;
 
                System.out.println("Game " + (gameIdx + 1) + "/" + filenames.size() + ", Day " + d + "/" + numReports + ", query=" + queryIdx);
 
                // Get avg position for each agent
                Double[] actualAveragePositions = getAveragePositions(status, d, query);
 
                // Get avg position for each agent
                Double[] sampledAveragePositions = getSampledAveragePositions(status, d, query);
 
                Double[] bids = getBids(status, d, query);
 
                Double[] advertiserEffects = getAdvertiserEffects(status, d, query);
 
                // Get squashed bids for each agent
                Double[] squashedBids = getSquashedBids(status, d, query);
 
                Double[] budgets = getBudgets(status, d, query);
 
                Double[] globalBudgets = getGlobalBudgets(status, d);
 
                // Get total number of impressions for each agent
                Integer[] impressions = getAgentImpressions(status, d, query);
 
                Integer[] clicks = getAgentClicks(status, d, query);
 
                Integer[] conversions = getAgentConversions(status, d, query);
 
                Integer[] promotedImpressions = getAgentPromotedImpressions(status, d, query);
 
                Boolean[] promotionEligibility = getAgentPromotionEligibility(status, d, query, promotedReserveScore.get(query.getType()));
 
                //Determine whether each agent hit their query or global budget (1 if yes, 0 if not)
                Boolean[] hitBudget = getAgentHitBudget(status, d, query);
 
                double[] impsDistMean = getAgentImpressionsDistributionMeanOrStdev(status, query, d, true, PRIOR_STDEV_MULTIPLIER);
                double[] impsDistStdev = getAgentImpressionsDistributionMeanOrStdev(status, query, d, false, PRIOR_STDEV_MULTIPLIER);
                if (!USE_WATERFALL_PRIORS) {
                   Arrays.fill(impsDistMean, -1);
                   Arrays.fill(impsDistStdev, -1);
                }
 
                // Determine how many agents actually participated
                int numParticipants = 0;
                for (int a = 0; a < actualAveragePositions.length; a++) {
                   //if (!actualAveragePositions[a].isNaN()) numParticipants++;
                   if (!Double.isNaN(actualAveragePositions[a]) && (!Double.isNaN(sampledAveragePositions[a]) || !SAMPLED_AVERAGE_POSITIONS || !REMOVE_SAMPLE_NANS)) {
                      numParticipants++;
                   }
                }
 
                // Reduce to only auction participants
                double[] reducedAvgPos = new double[numParticipants];
                double[] reducedSampledAvgPos = new double[numParticipants];
                double[] reducedBids = new double[numParticipants];
                int[] reducedImps = new int[numParticipants];
                int[] reducedPromotedImps = new int[numParticipants];
                boolean[] reducedPromotionEligibility = new boolean[numParticipants];
                boolean[] reducedHitBudget = new boolean[numParticipants];
                double[] reducedImpsDistMean = new double[numParticipants];
                double[] reducedImpsDistStdev = new double[numParticipants];
                int rIdx = 0;
                for (int a = 0; a < actualAveragePositions.length; a++) {
                   if (!Double.isNaN(actualAveragePositions[a]) && (!Double.isNaN(sampledAveragePositions[a]) || !SAMPLED_AVERAGE_POSITIONS || !REMOVE_SAMPLE_NANS)) {
                      reducedAvgPos[rIdx] = actualAveragePositions[a];
                      reducedSampledAvgPos[rIdx] = sampledAveragePositions[a]; //TODO: need to handle double.nan cases...
                      reducedBids[rIdx] = squashedBids[a];
                      reducedImps[rIdx] = impressions[a];
                      reducedPromotedImps[rIdx] = promotedImpressions[a];
                      reducedPromotionEligibility[rIdx] = promotionEligibility[a];
                      reducedHitBudget[rIdx] = hitBudget[a];
                      reducedImpsDistMean[rIdx] = impsDistMean[a];
                      reducedImpsDistStdev[rIdx] = impsDistStdev[a];
                      rIdx++;
                   }
                }
 
 
                // Get ordering of remaining squashed bids
                int[] ordering;
                if (orderingKnown) {
                   ordering = getIndicesForDescendingOrder(reducedBids);
                } else {
                   ordering = new int[reducedBids.length];
                   for (int i = 0; i < ordering.length; i++) {
                      ordering[i] = i;
                   }
                }
 
                int impressionsUB = getAgentImpressionsUpperBound(status, d, query, reducedImps, ordering);
 
 
                // If any agents have the same squashed bids, we won't know the definitive ordering.
                // (TODO: Run the waterfall to determine the correct ordering (or at least a feasible one).)
                // For now, we'll drop any instances with duplicate squashed bids.
                if (duplicateSquashedBids(reducedBids, ordering)) {
                   System.out.println("Duplicate squashed bids found. Skipping instance.");
                   numSkips++;
                   continue;
                }
 
                //More generally, make sure our data isn't corrupt
                if (!validData(reducedBids)) {
                   System.out.println("Invalid data. Skipping instance.");
                   numSkips++;
                   continue;
                }
 
                // Some params needed for the QA instance
                // TODO: Have some of these configurable.
                int[] agentIds = new int[numParticipants]; //Just have them all be negative. TODO: Is carleton using this?
                for (int i = 0; i < agentIds.length; i++) {
                   agentIds[i] = -(i + 1);
                }
 
 
                //FIXME DEBUG
                //For now, skip anything with a NaN in sampled impressions
                boolean hasNaN = false;
                for (int i = 0; i < reducedSampledAvgPos.length; i++) {
                   if (Double.isNaN(reducedSampledAvgPos[i])) {
                      hasNaN = true;
                   }
                }
                if (!hasNaN) {
                   numSkips++;
                   continue;
                }
                //FIXME END DEBUG
 
                
                
                
                // DEBUG: Print out some game values.
                System.out.println("d=" + d + "\tq=" + query + "\treserve=" + status.getReserveInfo().getRegularReserve() + "\tpromoted=" + status.getReserveInfo().getPromotedReserve() + "\t" + status.getSlotInfo().getPromotedSlots() + "/" + status.getSlotInfo().getRegularSlots());
                System.out.println("d=" + d + "\tq=" + query + "\tagents=" + Arrays.toString(status.getAdvertisers()));
                System.out.println("d=" + d + "\tq=" + query + "\taveragePos=" + Arrays.toString(actualAveragePositions));
                System.out.println("d=" + d + "\tq=" + query + "\tsampledAveragePos=" + Arrays.toString(sampledAveragePositions));
                System.out.println("d=" + d + "\tq=" + query + "\tsquashing=" + status.getPubInfo().getSquashingParameter());
                System.out.println("d=" + d + "\tq=" + query + "\tadvertiserEffects=" + Arrays.toString(advertiserEffects));
                System.out.println("d=" + d + "\tq=" + query + "\tsquashedBids=" + Arrays.toString(squashedBids));
                System.out.println("d=" + d + "\tq=" + query + "\timpressions=" + Arrays.toString(impressions));
                System.out.println("d=" + d + "\tq=" + query + "\thitBudget=" + Arrays.toString(hitBudget));
                System.out.println("d=" + d + "\tq=" + query + "\tqueryBudgets=" + Arrays.toString(budgets));
                System.out.println("d=" + d + "\tq=" + query + "\tquerySpend=" + Arrays.toString(getAgentQuerySpend(status, d, query)));
                System.out.println("d=" + d + "\tq=" + query + "\tbids=" + Arrays.toString(bids));
                System.out.println("d=" + d + "\tq=" + query + "\tglobalBudgets=" + Arrays.toString(globalBudgets));
                System.out.println("d=" + d + "\tq=" + query + "\tglobalSpend=" + Arrays.toString(getAgentGlobalSpend(status, d)));
                System.out.println("d=" + d + "\tq=" + query + "\tglobalBids=" + Arrays.toString(getAgentGlobalMaxAvgCPC(status, d)));
                System.out.println("d=" + d + "\tq=" + query + "\tpromotedImpressions=" + Arrays.toString(promotedImpressions));
                System.out.println("d=" + d + "\tq=" + query + "\tpromotionEligibility=" + Arrays.toString(promotionEligibility));
                System.out.println("d=" + d + "\tq=" + query + "\timpressionsUB=" + impressionsUB);
                System.out.println("d=" + d + "\tq=" + query + "\timpressionsDistMean=" + Arrays.toString(impsDistMean));
                System.out.println("d=" + d + "\tq=" + query + "\timpressionsDistStdev=" + Arrays.toString(impsDistStdev));
                System.out.println();
 
                System.out.println("d=" + d + "\tq=" + query + "\treducedAvgPos=" + Arrays.toString(reducedAvgPos));
                System.out.println("d=" + d + "\tq=" + query + "\treducedSampledAvgPos=" + Arrays.toString(reducedSampledAvgPos));
                System.out.println("d=" + d + "\tq=" + query + "\treducedBids=" + Arrays.toString(reducedBids));
                System.out.println("d=" + d + "\tq=" + query + "\tordering=" + Arrays.toString(ordering));
 
                
 
                // For each agent, make a prediction (each agent sees a different num impressions)
 //					for (int ourAgentIdx=0; ourAgentIdx<=0; ourAgentIdx++) {
                for (int ourAgentIdx = 0; ourAgentIdx < numParticipants; ourAgentIdx++) {
             	   System.out.println("ourAgentIdx=" + ourAgentIdx);
                   double start = System.currentTimeMillis(); //time the prediction time on this instance
 
                   int ourImps = reducedImps[ourAgentIdx];
                   int ourPromotedImps = reducedPromotedImps[ourAgentIdx];
                   boolean ourPromotionEligibility = reducedPromotionEligibility[ourAgentIdx];
                   boolean ourHitBudget = reducedHitBudget[ourAgentIdx];
 
                   //DEBUG TEMP FIXME: Just want to see how much promotion constraint helps.
                   //if (ourPromotedImps <= 0) continue;
                   //ourPromotionEligibility = false; //FIXME just temporary
                   //if (ourPromotionEligibility == false) continue;
 
 
                   //FIXME: we should be able to choose more elegantly at runtime what class we're going to load.
                   //This is annoying... ImpressionEstimator requires a QAInstance in the constructor,
                   //but this QAInstance isn't ready until now.
                   //Terrible, band-aid solution is to have an integer corresponding to each test.
                   QAInstance inst;
                   ImpressionAndRankEstimator fullModel = null;
                   AbstractImpressionEstimator model = null;
 
 
                   //Remove any average position information that the agent doesn't have access to.
                   double[] avgPos;
                   double[] sAvgPos;
                   if (SAMPLED_AVERAGE_POSITIONS) {
                      //Avg position is -1 except for our agent.
                      avgPos = new double[reducedAvgPos.length];
                      Arrays.fill(avgPos, -1);
                      avgPos[ourAgentIdx] = reducedAvgPos[ourAgentIdx];
                      //Sampled avgPositions are their actual values
                      sAvgPos = reducedSampledAvgPos.clone();
                   } else {
                      //Avg positions are their actual values
                      avgPos = reducedAvgPos.clone();
                      //Sampled avgPositions are -1
                      sAvgPos = new double[reducedAvgPos.length];
                      Arrays.fill(sAvgPos, -1);
                   }
 
                   //Remove any ordering information that the agent doesn't have access to.
                   int[] knownInitialPositions = ordering.clone();
                   if (!orderingKnown) {
                      Arrays.fill(knownInitialPositions, -1);
                   }
 
 
                   //---------------- Create new QAInstance --------------                 
                   boolean considerPaddingAgents = false;
                   inst = new QAInstance(NUM_SLOTS, NUM_PROMOTED_SLOTS, numParticipants, avgPos, sAvgPos, agentIds, ourAgentIdx,
                                         ourImps, ourPromotedImps, impressionsUB, considerPaddingAgents, ourPromotionEligibility, ourHitBudget,
                                         reducedImpsDistMean, reducedImpsDistStdev, SAMPLED_AVERAGE_POSITIONS, knownInitialPositions);
 
 
                   //---------------- SOLVE INSTANCE ----------------------
                   if (impressionEstimatorIdx == SolverType.CP) {
                      if (orderingKnown) {
                         model = new ImpressionEstimator(inst);
                         fullModel = new ConstantImpressionAndRankEstimator(model, ordering);
                      } else {
                     	 model = new ImpressionEstimator(inst);
                     	 fullModel = new LDSImpressionAndRankEstimator(model);
                      }
                   }
                   if (impressionEstimatorIdx == SolverType.MIP) {
                      boolean useRankingConstraints = !orderingKnown; //Only use ranking constraints if you don't know the ordering
                      model = new EricImpressionEstimator(inst, useRankingConstraints);
                      fullModel = new ConstantImpressionAndRankEstimator(model, ordering);
                   }
                   if (impressionEstimatorIdx == SolverType.LDSMIP) {
                      boolean useRankingConstraints = false;
                      model = new EricImpressionEstimator(inst, useRankingConstraints);
                      if (orderingKnown) {
                         fullModel = new ConstantImpressionAndRankEstimator(model, ordering);
                      } else {
                         fullModel = new LDSImpressionAndRankEstimator(model);
                      }
                   }
 
 
                   //---------------- SOLVE FOR RESULT -----------------------
                   IEResult result = fullModel.getBestSolution();
                   //Get predictions (also provide dummy values for failure)
                   int[] predictedImpsPerAgent;
 
                   if (result != null) {
                      predictedImpsPerAgent = result.getSol();
                   } else {
                 	  System.out.println("Result is null.");
                      predictedImpsPerAgent = new int[reducedImps.length];
                      Arrays.fill(predictedImpsPerAgent, -1);
                   }
 
                   double stop = System.currentTimeMillis();
                   double secondsElapsed = (stop - start) / 1000.0;
 
                   //System.out.println("predicted: " + Arrays.toString(predictedImpsPerAgent));
                   //System.out.println("actual: " + Arrays.toString(reducedImps));
 
                   //Update performance metrics
                   updatePerformanceMetrics(predictedImpsPerAgent, reducedImps, ordering, ordering);
                   //outputPerformanceMetrics();
 
 
                   //LOGGING
                   double[] err = new double[predictedImpsPerAgent.length];
                   for (int a = 0; a < predictedImpsPerAgent.length; a++) {
                      err[a] = Math.abs(predictedImpsPerAgent[a] - reducedImps[a]);
                   }
                   StringBuffer sb = new StringBuffer();
                   sb.append("result=" + result + "\n");
                   sb.append("err=" + Arrays.toString(err) + "\t");
                   sb.append("pred=" + Arrays.toString(predictedImpsPerAgent) + "\t");
                   sb.append("actual=" + Arrays.toString(reducedImps) + "\t");
                   sb.append("g=" + gameIdx + " ");
                   sb.append("d=" + d + " a=" + ourAgentIdx + " q=" + query + " avgPos=" + Arrays.toString(reducedAvgPos) + " ");
                   sb.append("sampAvgPos=" + Arrays.toString(reducedSampledAvgPos) + " ");
                   sb.append("bids=" + Arrays.toString(reducedBids) + " ");
                   sb.append("imps=" + Arrays.toString(reducedImps) + " ");
                   sb.append("order=" + Arrays.toString(ordering) + " ");
                   sb.append(((impressionEstimatorIdx == SolverType.CP) ? "CP" : "MIP"));
                   System.out.println(sb);
 
 
                   //Save all relevant data to file
                   sb = new StringBuffer();
                   for (int predictingAgentIdx = 0; predictingAgentIdx < numParticipants; predictingAgentIdx++) {
                      int ourBidRank = -1;
                      int oppBidRank = -1;
                      for (int i = 0; i < ordering.length; i++) {
                         if (ordering[i] == ourAgentIdx) {
                            ourBidRank = i;
                         }
                         if (ordering[i] == predictingAgentIdx) {
                            oppBidRank = i;
                         }
                      }
 
 
                      sb.append(model.getName() + ",");
                      sb.append(gameIdx + ",");
                      sb.append(d + ",");
                      sb.append(queryIdx + ",");
                      sb.append(ourBidRank + ",");
                      sb.append(oppBidRank + ",");
                      sb.append(reducedAvgPos[ourAgentIdx] + ","); //our avgPos
                      sb.append(reducedAvgPos[predictingAgentIdx] + ","); //opponent avgPos
                      sb.append(reducedSampledAvgPos[predictingAgentIdx] + ","); //opponent sampleAvgPos
                      sb.append(query.getType() + ",");
                      sb.append(numParticipants + ",");
                      sb.append(reducedImps[predictingAgentIdx] + ","); //actual
                      sb.append(predictedImpsPerAgent[predictingAgentIdx] + ","); //prediction
                      sb.append(secondsElapsed + "\n");
                   }
                   //Log the result (for later loading into R)
                   writeToLog(sb.toString());
                }
             }
          }
       }
 
       outputPerformanceMetrics();
       closeLog();
    }
 
 
    /**
     * Gets a QAInstance in the format that Carleton's algorithm wants.
     *
     * @param NUM_SLOTS
     * @param NUM_PROMOTED_SLOTS
     * @param numParticipants
     * @param reducedAvgPos
     * @param reducedSampledAvgPos
     * @param agentIds
     * @param ourAgentIdx
     * @param ourImps
     * @param ourPromotedImps
     * @param impressionsUB
     * @param ourPromotionEligibility
     * @param reducedImpsDistMean
     * @param reducedImpsDistStdev
     * @return
     */
    private QAInstance getCarletonQAInstance(int NUM_SLOTS, int NUM_PROMOTED_SLOTS,
                                             int numParticipants, double[] reducedAvgPos,
                                             double[] reducedSampledAvgPos, int[] agentIds, int ourAgentIdx,
                                             int ourImps, int ourPromotedImps, int impressionsUB,
                                             boolean ourPromotionEligibility, boolean ourHitBudget,
                                             double[] reducedImpsDistMean, double[] reducedImpsDistStdev,
                                             int[] ordering) {
 
       double[] avgPos = new double[reducedAvgPos.length];
       boolean considerPadding = false;
 
       //If exact average positions, just return a standard query instance (with no padding)
       if (!SAMPLED_AVERAGE_POSITIONS) {
          avgPos = reducedAvgPos.clone();
          return new QAInstance(NUM_SLOTS, NUM_PROMOTED_SLOTS, numParticipants, avgPos, reducedSampledAvgPos, agentIds, ourAgentIdx,
                                ourImps, ourPromotedImps, impressionsUB, false, ourPromotionEligibility, ourHitBudget,
                                reducedImpsDistMean, reducedImpsDistStdev, SAMPLED_AVERAGE_POSITIONS, null);
       } else {
          avgPos = reducedSampledAvgPos.clone();
 
          if (REPORT_FULLPOS_FORSELF) {
             avgPos[ourAgentIdx] = reducedAvgPos[ourAgentIdx];
          }
 
          //If any agents have a NaN sampled average position, give them a dummy average position
          //equal to Min(their starting position, numSlots)
          for (int i = 0; i < avgPos.length; i++) {
             if (Double.isNaN(avgPos[ordering[i]])) {
                avgPos[ordering[i]] = Math.min(i + 1, NUM_SLOTS);
             }
          }
 
          System.out.println("actual=" + Arrays.toString(reducedAvgPos) + ", sample=" + Arrays.toString(reducedSampledAvgPos) + ", newSample=" + Arrays.toString(avgPos));
          return new QAInstance(NUM_SLOTS, NUM_PROMOTED_SLOTS, numParticipants, avgPos, reducedSampledAvgPos, agentIds, ourAgentIdx,
                                ourImps, ourPromotedImps, impressionsUB, false, ourPromotionEligibility, ourHitBudget,
                                reducedImpsDistMean, reducedImpsDistStdev, SAMPLED_AVERAGE_POSITIONS, null);
       }
    }
 
 
    /**
     * Load a game
     * For each day,
     * Get all query reports that came in on that day
     * From these, infer: numSlots, numAgents, avgPos[], agentIds[], ourAgentIdx, ourImpressions, impressionsUB
     * also infer squashed bid ordering.
     * Input these into the given Impression Estimator (via a QA instance)
     * Compare output impsPer
     *
     * @param impressionEstimatorIdx
     * @throws IOException
     * @throws ParseException
     */
    public void rankedImpressionEstimatorPredictionChallenge(SolverType impressionEstimatorIdx) throws IOException, ParseException {
       //printGameLogInfo();
 
       initializeLog("riePred" + impressionEstimatorIdx + ".txt");
       numInstances = 0;
       numImprsPredictions = 0;
       numOrderingPredictions = 0;
       numCorrectlyOrderedInstances = 0;
       aggregateAbsError = 0;
       ArrayList<String> filenames = getGameStrings();
 
 
       for (int gameIdx = 0; gameIdx < filenames.size(); gameIdx++) {
          String filename = filenames.get(gameIdx);
 
          // Load this game and its basic parameters
          GameStatus status = new GameStatusHandler(filename).getGameStatus();
          int NUM_PROMOTED_SLOTS = status.getSlotInfo().getPromotedSlots();
          HashMap<QueryType, Double> promotedReserveScore = getApproximatePromotedReserveScore(status);
 
 
          // Make the query space
          LinkedHashSet<Query> querySpace = new LinkedHashSet<Query>();
          querySpace.add(new Query(null, null)); //F0
          for (Product product : status.getRetailCatalog()) {
             querySpace.add(new Query(product.getManufacturer(), null)); // F1 Manufacturer only
             querySpace.add(new Query(null, product.getComponent())); // F1 Component only
             querySpace.add(new Query(product.getManufacturer(), product.getComponent())); // The F2 query class
          }
 
          Query[] queryArr = new Query[querySpace.size()];
          querySpace.toArray(queryArr);
          int numQueries = queryArr.length;
 
          String[] advertisers = status.getAdvertisers();
          ArrayList<String> advList = new ArrayList<String>();
 
          for (int i = 0; i < advertisers.length; i++) {
             advList.add(advertisers[i]);
          }
 
          //Create Query Analyzer for each advertiser
          ArrayList<AbstractQueryAnalyzer> RIEList = new ArrayList<AbstractQueryAnalyzer>();
          for (int i = 0; i < advertisers.length; i++) {
             AbstractQueryAnalyzer model = null;
             if (impressionEstimatorIdx == SolverType.CP) {
                model = new CarletonQueryAnalyzer(querySpace, advList, advertisers[i], LDS_ITERATIONS_1, LDS_ITERATIONS_2, REPORT_FULLPOS_FORSELF, SAMPLED_AVERAGE_POSITIONS);
             } else if (impressionEstimatorIdx == SolverType.MIP) {
                //TODO
             }
             RIEList.add(model);
          }
 
 
          // Make predictions for each day/query in this game
          int numReports = 57; //TODO: Why?
          for (int d = 0; d < numReports; d++) {
 
 
             HashMap<Query, Boolean> skipQuery = new HashMap<Query, Boolean>();
 
             HashMap<Query, Double[]> actualAveragePositionsMap = new HashMap<Query, Double[]>();
             HashMap<Query, Double[]> sampledAveragePositionsMap = new HashMap<Query, Double[]>();
             HashMap<Query, Integer> maxImpsMap = new HashMap<Query, Integer>();
             HashMap<Query, Integer> numParticipantsMap = new HashMap<Query, Integer>();
 
             HashMap<Query, double[]> reducedAvgPosMap = new HashMap<Query, double[]>();
             HashMap<Query, double[]> reducedSampledAvgPosMap = new HashMap<Query, double[]>();
             HashMap<Query, double[]> reducedBidsMap = new HashMap<Query, double[]>();
             HashMap<Query, int[]> reducedImpsMap = new HashMap<Query, int[]>();
             HashMap<Query, int[]> reducedPromotedImpsMap = new HashMap<Query, int[]>();
             HashMap<Query, boolean[]> reducedPromotionEligibilityMap = new HashMap<Query, boolean[]>();
             HashMap<Query, double[]> reducedImpsDistMeanMap = new HashMap<Query, double[]>();
             HashMap<Query, double[]> reducedImpsDistStdevMap = new HashMap<Query, double[]>();
 
             HashMap<Query, int[]> orderingMap = new HashMap<Query, int[]>();
 
             //Data pre-processing step
             for (int queryIdx = 0; queryIdx < numQueries; queryIdx++) {
                numInstances++;
                Query query = queryArr[queryIdx];
 
                System.out.println("Game " + (gameIdx + 1) + "/" + filenames.size() + ", Day " + d + "/" + numReports + ", query=" + queryIdx);
 
                // Get avg position for each agent
                Double[] actualAveragePositions = getAveragePositions(status, d, query);
                actualAveragePositionsMap.put(query, actualAveragePositions);
 
                // Get avg position for each agent
                Double[] sampledAveragePositions = getSampledAveragePositions(status, d, query);
                sampledAveragePositionsMap.put(query, sampledAveragePositions);
 
                Double[] bids = getBids(status, d, query);
 
                Double[] advertiserEffects = getAdvertiserEffects(status, d, query);
 
                // Get squashed bids for each agent
                Double[] squashedBids = getSquashedBids(status, d, query);
 
                Double[] budgets = getBudgets(status, d, query);
 
                // Get total number of impressions for each agent
                Integer[] impressions = getAgentImpressions(status, d, query);
 
                Integer[] clicks = getAgentClicks(status, d, query);
 
                Integer[] conversions = getAgentConversions(status, d, query);
 
                Integer[] promotedImpressions = getAgentPromotedImpressions(status, d, query);
 
                Boolean[] promotionEligibility = getAgentPromotionEligibility(status, d, query, promotedReserveScore.get(query.getType()));
 
                double[] impsDistMean = getAgentImpressionsDistributionMeanOrStdev(status, query, true);
 
                double[] impsDistStdev = getAgentImpressionsDistributionMeanOrStdev(status, query, false);
 
                // Determine how many agents actually participated
                int numParticipants = 0;
                for (int a = 0; a < actualAveragePositions.length; a++) {
                   //if (!actualAveragePositions[a].isNaN()) numParticipants++;
                   if (CONSIDER_ALL_PARTICIPANTS || !Double.isNaN(actualAveragePositions[a])) {
                      numParticipants++;
                   }
                }
                numParticipantsMap.put(query, numParticipants);
 
                // Reduce to only auction participants
                double[] reducedAvgPos = new double[numParticipants];
                double[] reducedSampledAvgPos = new double[numParticipants];
                double[] reducedBids = new double[numParticipants];
                int[] reducedImps = new int[numParticipants];
                int[] reducedPromotedImps = new int[numParticipants];
                boolean[] reducedPromotionEligibility = new boolean[numParticipants];
                double[] reducedImpsDistMean = new double[numParticipants];
                double[] reducedImpsDistStdev = new double[numParticipants];
                int rIdx = 0;
                for (int a = 0; a < actualAveragePositions.length; a++) {
                   if (CONSIDER_ALL_PARTICIPANTS || !actualAveragePositions[a].isNaN()) {
                      reducedAvgPos[rIdx] = actualAveragePositions[a];
                      reducedSampledAvgPos[rIdx] = sampledAveragePositions[a]; //TODO: need to handle double.nan cases...
                      reducedBids[rIdx] = squashedBids[a];
                      reducedImps[rIdx] = impressions[a];
                      reducedPromotedImps[rIdx] = promotedImpressions[a];
                      reducedPromotionEligibility[rIdx] = promotionEligibility[a];
                      reducedImpsDistMean[rIdx] = impsDistMean[a];
                      reducedImpsDistStdev[rIdx] = impsDistStdev[a];
                      rIdx++;
                   }
                }
 
                reducedAvgPosMap.put(query, reducedAvgPos);
                reducedSampledAvgPosMap.put(query, reducedSampledAvgPos);
                reducedBidsMap.put(query, reducedBids);
                reducedImpsMap.put(query, reducedImps);
                reducedPromotedImpsMap.put(query, reducedPromotedImps);
                reducedPromotionEligibilityMap.put(query, reducedPromotionEligibility);
                reducedImpsDistMeanMap.put(query, reducedImpsDistMean);
                reducedImpsDistStdevMap.put(query, reducedImpsDistStdev);
 
                // Get ordering of remaining squashed bids
                int[] ordering = getIndicesForDescendingOrder(reducedBids);
                orderingMap.put(query, ordering);
 
                int impressionsUB = getAgentImpressionsUpperBound(status, d, query, reducedImps, ordering);
                maxImpsMap.put(query, impressionsUB);
 
 
 //					// DEBUG: Print out some game values.
 //					System.out.println("d=" + d + "\tq=" + query + "\treserve=" + status.getReserveInfo().getRegularReserve() + "\tpromoted=" + status.getReserveInfo().getPromotedReserve() + "\t" + status.getSlotInfo().getPromotedSlots() + "/" + status.getSlotInfo().getRegularSlots());
 //					System.out.println("d=" + d + "\tq=" + query + "\tagents=" + Arrays.toString(status.getAdvertisers()));
 //					System.out.println("d=" + d + "\tq=" + query + "\taveragePos=" + Arrays.toString(actualAveragePositions));
 //					System.out.println("d=" + d + "\tq=" + query + "\tsampledAveragePos=" + Arrays.toString(sampledAveragePositions));
 //					System.out.println("d=" + d + "\tq=" + query + "\tbids=" + Arrays.toString(bids));
 //					System.out.println("d=" + d + "\tq=" + query + "\tsquashing=" + status.getPubInfo().getSquashingParameter());
 //					System.out.println("d=" + d + "\tq=" + query + "\tadvertiserEffects=" + Arrays.toString(advertiserEffects));
 //					System.out.println("d=" + d + "\tq=" + query + "\tsquashedBids=" + Arrays.toString(squashedBids));
 //					System.out.println("d=" + d + "\tq=" + query + "\tbudgets=" + Arrays.toString(budgets));
 //					System.out.println("d=" + d + "\tq=" + query + "\timpressions=" + Arrays.toString(impressions));
 //					System.out.println("d=" + d + "\tq=" + query + "\tpromotedImpressions=" + Arrays.toString(promotedImpressions));
 //					System.out.println("d=" + d + "\tq=" + query + "\tpromotionEligibility=" + Arrays.toString(promotionEligibility));
 //					System.out.println("d=" + d + "\tq=" + query + "\timpressionsUB=" + impressionsUB);
 //					System.out.println("d=" + d + "\tq=" + query + "\timpressionsDistMean=" + Arrays.toString(impsDistMean));
 //					System.out.println("d=" + d + "\tq=" + query + "\timpressionsDistStdev=" + Arrays.toString(impsDistStdev));
 //					System.out.println();
 
 
                // If any agents have the same squashed bids, we won't know the definitive ordering.
                // (TODO: Run the waterfall to determine the correct ordering (or at least a feasible one).)
                // For now, we'll drop any instances with duplicate squashed bids.
                if (duplicateSquashedBids(reducedBids, ordering)) {
                   System.out.println("Duplicate squashed bids found. Skipping instance.");
                   skipQuery.put(query, true);
                }
 
                //More generally, make sure our data isn't corrupt
                if (!validData(reducedBids)) {
                   System.out.println("Invalid data. Skipping instance.");
                   skipQuery.put(query, true);
                }
             }
 
             //Update RIE models
             for (int i = 0; i < advertisers.length; i++) {
                AbstractQueryAnalyzer model = RIEList.get(i);
 
                QueryReport queryReport = status.getQueryReports().get(advertisers[i]).get(d);
                SalesReport salesreport = status.getSalesReports().get(advertisers[i]).get(d);
                BidBundle bidBundle = status.getBidBundles().get(advertisers[i]).get(d);
 
 
 //					//DEBUG: Have query report contain exact info
 //					for (Query query : queryArr) {
 //						Double[] actualPositionsForAgents = getAveragePositions(status, d, query);
 //						for (int a=0; a<advertisers.length; a++) {	
 //							String advertiser = "adv" + (a+1);
 //							queryReport.setPosition(query, advertiser, actualPositionsForAgents[a]);
 //						}
 //					}
 //					//END DEBUG
 
 
                model.updateModel(queryReport, salesreport, bidBundle, maxImpsMap);
             }
 
 
             for (int queryIdx = 0; queryIdx < numQueries; queryIdx++) {
                Query query = queryArr[queryIdx];
 
                Double[] actualAveragePositions = actualAveragePositionsMap.get(query);
                Double[] sampledAveragePositions = sampledAveragePositionsMap.get(query);
                Integer maxImp = maxImpsMap.get(query);
                Integer numParticipants = numParticipantsMap.get(query);
 
                double[] reducedAvgPos = reducedAvgPosMap.get(query);
                double[] reducedSampledAvgPos = reducedSampledAvgPosMap.get(query);
                double[] reducedBids = reducedBidsMap.get(query);
                int[] reducedImps = reducedImpsMap.get(query);
                int[] reducedPromotedImps = reducedPromotedImpsMap.get(query);
                boolean[] reducedPromotionEligibility = reducedPromotionEligibilityMap.get(query);
                double[] reducedImpsDistMean = reducedImpsDistMeanMap.get(query);
                double[] reducedImpsDistStdev = reducedImpsDistStdevMap.get(query);
 
 
                int[] ordering = orderingMap.get(query); //[a b c d] means highest ranked agent is the one with index a
                int[] actualBidOrdering = getActualBidOrdering(ordering, reducedImps); //[a b c d] means the agent with index 0 has rank a
                int[] sampledBidOrdering = getSampledBidOrdering(ordering, reducedImps, reducedSampledAvgPos); //The initial positions of agents that had at least one sample
                int[] sampledBidRelativeOrdering = getRelativeOrdering(sampledBidOrdering); //The ordering for agents that had at least one sample
 
                //May have decided to skip query in pre-processing
                Boolean skip = skipQuery.get(query);
                if (skip != null && skip) {
                   numSkips++;
                   continue;
                }
 
 
                // DEBUG: Print out some game values.
                System.out.println("---------------------------------------------");
                System.out.println("d=" + d + "\tq=" + query + "\treserve=" + status.getReserveInfo().getRegularReserve() + "\tpromoted=" + status.getReserveInfo().getPromotedReserve() + "\t" + status.getSlotInfo().getPromotedSlots() + "/" + status.getSlotInfo().getRegularSlots());
                System.out.println("d=" + d + "\tq=" + query + "\tagents=" + Arrays.toString(status.getAdvertisers()));
                System.out.println("d=" + d + "\tq=" + query + "\taveragePos=" + Arrays.toString(actualAveragePositions));
                System.out.println("d=" + d + "\tq=" + query + "\tsampledAveragePos=" + Arrays.toString(sampledAveragePositions));
                System.out.println("d=" + d + "\tq=" + query + "\tbids=" + Arrays.toString(reducedBids));
                System.out.println("d=" + d + "\tq=" + query + "\timpressions=" + Arrays.toString(reducedImps));
                System.out.println("d=" + d + "\tq=" + query + "\tActualBidOrdering=" + Arrays.toString(actualBidOrdering));
                System.out.println("d=" + d + "\tq=" + query + "\tSampledBidOrdering=" + Arrays.toString(sampledBidOrdering));
                System.out.println("d=" + d + "\tq=" + query + "\tSampledBidRelativeOrdering=" + Arrays.toString(sampledBidRelativeOrdering));
                System.out.println();
 
 
                // For each agent, make a prediction (each agent sees a different num impressions)
                int ourAgentIdx = 0;
                for (int i = 0; i < advertisers.length; i++) {
                   if (Double.isNaN(actualAveragePositions[i])) {
                      continue;
                   }
 
                   double start = System.currentTimeMillis(); //time the prediction time on this instance
 
                   AbstractQueryAnalyzer model = RIEList.get(i);
 
                   System.out.println("   a=" + i);
                   System.out.println("   Predicted impressions: " + Arrays.toString(model.getImpressionsPrediction(query)));
                   System.out.println("   Predicted ordering: " + Arrays.toString(model.getOrderPrediction(query)));
 
                   //Get predictions
                   int[] predictedImpsPerAgent = model.getImpressionsPrediction(query);
                   int[] predictedOrdering = model.getOrderPrediction(query);
                   int[] predictedRelativeOrdering = getRelativeOrdering(predictedOrdering);
 
                   double stop = System.currentTimeMillis();
                   double secondsElapsed = (stop - start) / 1000.0;
 
                   //System.out.println("predicted: " + Arrays.toString(predictedImpsPerAgent));
                   //System.out.println("actual: " + Arrays.toString(reducedImps));
 
                   //Update performance metrics
                   updatePerformanceMetrics(predictedImpsPerAgent, reducedImps, predictedOrdering, sampledBidOrdering);
 //						updatePerformanceMetrics(predictedImpsPerAgent, reducedImps, predictedRelativeOrdering, sampledBidRelativeOrdering);
                   //outputPerformanceMetrics();
 
 
                   //LOGGING
                   double[] err = new double[predictedImpsPerAgent.length];
                   for (int a = 0; a < predictedImpsPerAgent.length; a++) {
                      err[a] = Math.abs(predictedImpsPerAgent[a] - reducedImps[a]);
                   }
                   StringBuffer sb = new StringBuffer();
                   sb.append("err=" + Arrays.toString(err) + "\t");
                   sb.append("pred=" + Arrays.toString(predictedImpsPerAgent) + "\t");
                   sb.append("actual=" + Arrays.toString(reducedImps) + "\t");
                   sb.append("g=" + gameIdx + " ");
                   sb.append("d=" + d + " a=" + ourAgentIdx + " q=" + query + " avgPos=" + Arrays.toString(reducedAvgPos) + " ");
                   sb.append("sampAvgPos=" + Arrays.toString(reducedSampledAvgPos) + " ");
                   sb.append("bids=" + Arrays.toString(reducedBids) + " ");
                   sb.append("imps=" + Arrays.toString(reducedImps) + " ");
                   sb.append("order=" + Arrays.toString(ordering) + " ");
                   sb.append(((impressionEstimatorIdx == SolverType.CP) ? "CP" : "MIP"));
                   System.out.println(sb);
 
 
                   //Save all relevant data to file
                   sb = new StringBuffer();
                   for (int predictingAgentIdx = 0; predictingAgentIdx < numParticipants; predictingAgentIdx++) {
                      int ourBidRank = -1;
                      int oppBidRank = -1;
                      for (int j = 0; j < ordering.length; j++) {
                         if (ordering[j] == ourAgentIdx) {
                            ourBidRank = j;
                         }
                         if (ordering[j] == predictingAgentIdx) {
                            oppBidRank = j;
                         }
                      }
 
 
                      sb.append(model.toString() + ",");
                      sb.append(gameIdx + ",");
                      sb.append(d + ",");
                      sb.append(queryIdx + ",");
                      sb.append(ourBidRank + ",");
                      sb.append(oppBidRank + ",");
                      sb.append(reducedAvgPos[ourAgentIdx] + ","); //our avgPos
                      sb.append(reducedAvgPos[predictingAgentIdx] + ","); //opponent avgPos
                      sb.append(reducedSampledAvgPos[predictingAgentIdx] + ","); //opponent sampleAvgPos
                      sb.append(query.getType() + ",");
                      sb.append(numParticipants + ",");
                      sb.append(reducedImps[predictingAgentIdx] + ","); //actual
                      sb.append(predictedImpsPerAgent[predictingAgentIdx] + ","); //prediction
                      sb.append(secondsElapsed + "\n");
                   }
                   //Log the result (for later loading into R)
                   writeToLog(sb.toString());
 
                   ourAgentIdx++;
                }
             }
          }
       }
 
       outputPerformanceMetrics();
       closeLog();
    }
 
 
    /**
     * Rank agents that saw at least one impression by squashed bid.
     *
     * @param ordering
     * @param reducedImps
     * @return
     */
    private int[] getActualBidOrdering(int[] ordering, int[] reducedImps) {
       int[] actualBidOrdering = new int[ordering.length]; //[a b c d] means the agent with index 0 has rank a
       Arrays.fill(actualBidOrdering, -1);
       int rank = 0;
       for (int i = 0; i < actualBidOrdering.length; i++) {
          if (reducedImps[ordering[i]] > 0) {
             actualBidOrdering[ordering[i]] = rank;
             rank++;
          }
       }
       return actualBidOrdering;
    }
 
 
    /**
     * This gives, for each agent that was in the auction (i.e. saw at least one impression):
     * The actual squashed bid ranking of the agent, if the agent had at least one sample
     * -1, if the agent didn't see any samples.
     *
     * @param ordering
     * @param reducedImps
     * @param sampleAvgPos
     * @return
     */
    private int[] getSampledBidOrdering(int[] ordering, int[] reducedImps, double[] sampleAvgPos) {
       int[] actualBidOrdering = new int[ordering.length]; //[a b c d] means the agent with index 0 has rank a
       Arrays.fill(actualBidOrdering, -1);
       int rank = 0;
       for (int i = 0; i < actualBidOrdering.length; i++) {
          if (reducedImps[ordering[i]] > 0) {
             if (!Double.isNaN(sampleAvgPos[ordering[i]])) {
                actualBidOrdering[ordering[i]] = rank;
             }
             rank++;
          }
       }
       return actualBidOrdering;
    }
 
 
    /**
     * Takes as input the initial position each sampled agent was in.
     * Returns the relative ordering of each agent.
     *
     * @param initialPositions
     * @return
     */
    private int[] getRelativeOrdering(int[] initialPositions) {
       int[] relativeOrdering = new int[initialPositions.length];
       Arrays.fill(relativeOrdering, -1);
       for (int i = 0; i < relativeOrdering.length; i++) {
          if (initialPositions[i] == -1) {
             continue;
          }
          int numWithLowerPosition = 0;
          for (int j = 0; j < relativeOrdering.length; j++) {
             if (initialPositions[j] == -1) {
                continue;
             }
             if (initialPositions[j] < initialPositions[i]) {
                numWithLowerPosition++;
             }
          }
          relativeOrdering[i] = numWithLowerPosition;
       }
       return relativeOrdering;
    }
 
 
    /**
     * Varifies that data is valid for testing.
     * Currently just makes sure that squashed bids aren't NaN values.
     *
     * @param reducedBids
     * @return
     */
    private boolean validData(double[] reducedBids) {
       for (int i = 0; i < reducedBids.length; i++) {
          if (Double.isNaN(reducedBids[i])) {
             return false;
          }
       }
       return true;
    }
 
 
    private boolean duplicateSquashedBids(double[] reducedBids, int[] ordering) {
       // If any agents have the same squashed bids, we won't know the definitive ordering.
       // (TODO: Run the waterfall to determine the correct ordering (or at least a feasible one).)
       // For now, we'll drop any instances with duplicate squashed bids.
       for (int i = 1; i < reducedBids.length; i++) {
          if (reducedBids[ordering[i]] == reducedBids[ordering[i - 1]]) {
             return true;
          }
       }
       return false;
    }
 
 
    private int getAgentImpressionsUpperBound(GameStatus status, int d, Query q, int[] impressions, int[] order) {
       int imps = 0;
 
       if (PERFECT_IMPS) {
          for (Product product : status.getRetailCatalog()) {
             HashMap<UserState, Integer> userDist = status.getUserDistributions().get(d).get(product);
             if (q.getType() == QueryType.FOCUS_LEVEL_ZERO) {
                imps += userDist.get(UserState.F0);
                imps += (1.25 / 3.0) * userDist.get(UserState.IS);
             } else if (q.getType() == QueryType.FOCUS_LEVEL_ONE) {
                if (product.getComponent().equals(q.getComponent()) || product.getManufacturer().equals(q.getManufacturer())) {
                   imps += (1.25 / 2.0) * userDist.get(UserState.F1);
                   imps += (1.25 / 6.0) * userDist.get(UserState.IS);
                }
             } else {
                if (product.getComponent().equals(q.getComponent()) && product.getManufacturer().equals(q.getManufacturer())) {
                   imps += userDist.get(UserState.F2);
                   imps += (1.25 / 3.0) * userDist.get(UserState.IS);
                }
             }
          }
       } else {
          if (q.getType().equals(QueryType.FOCUS_LEVEL_ZERO)) {
             imps = MAX_F0_IMPS;
          } else if (q.getType().equals(QueryType.FOCUS_LEVEL_ONE)) {
             imps = MAX_F1_IMPS;
          } else {
             imps = MAX_F2_IMPS;
          }
       }
 
       if (impressions.length > 0) {
          int numSlots = status.getSlotInfo().getRegularSlots();
 
          ArrayList<Integer> impsBeforeDropout = new ArrayList<Integer>();
          PriorityQueue<Integer> queue = new PriorityQueue<Integer>();
          for (int i = 0; i < numSlots && i < impressions.length; i++) {
             queue.add(impressions[order[i]]);
          }
 
          int lastIdx = queue.size();
 
          while (!queue.isEmpty()) {
             int val = queue.poll();
             impsBeforeDropout.add(val);
 
             if (lastIdx < impressions.length) {
                queue.add(impressions[order[lastIdx]] + val);
                lastIdx++;
             }
          }
 
          int maxImps = impsBeforeDropout.get(impsBeforeDropout.size() - 1);
 
          //If any agent had more impressions than this upper bound, clearly the upper bound
          //was not high enough.
          imps = Math.max(imps, maxImps);
 
       }
 
       return imps;
    }
 
    private double[] getAgentImpressionsDistributionMeanOrStdev(GameStatus status, Query query, int d, boolean getMean, double noiseFactor) {
       String[] agents = status.getAdvertisers();
       double[] meanOrStdevImps = new double[agents.length];
       for (int a = 0; a < agents.length; a++) {
          String agentName = agents[a];
          int imps = status.getQueryReports().get(agentName).get(d).getImpressions(query);         
 
     	 //Potentially add some noise to the mean imps prior
     	 Random r = new Random();
    	 double noisyImps = Math.max(0, imps + r.nextGaussian() * imps * noiseFactor);
 
          if (getMean) {
             meanOrStdevImps[a] = noisyImps;
          } else {
             meanOrStdevImps[a] = Math.max(1, imps * noiseFactor); //Ensure stdev is never 0
          }
       }
       return meanOrStdevImps;
    }
 
    private double[] getAgentImpressionsDistributionMeanOrStdev(GameStatus status, Query query, boolean getMean) {
       String[] agents = status.getAdvertisers();
       double[] meanOrStdevImps = new double[agents.length];
       for (int a = 0; a < agents.length; a++) {
          ArrayList<Double> dailyImps = new ArrayList<Double>();
          String agentName = agents[a];
          LinkedList<QueryReport> agentQueryReports = status.getQueryReports().get(agentName);
          for (int d = 0; d < agentQueryReports.size(); d++) {
             int imps = agentQueryReports.get(d).getImpressions(query);
             if (imps > 0) {
                dailyImps.add(new Double(imps));
             }
          }
          double[] meanAndStdev = getStdDevAndMean(dailyImps);
          if (getMean) {
             meanOrStdevImps[a] = meanAndStdev[0];
          } else {
             meanOrStdevImps[a] = meanAndStdev[1];
          }
       }
       return meanOrStdevImps;
    }
 
    private double[] getAgentImpressionsDistributionStdev(GameStatus status, Query query) {
       String[] agents = status.getAdvertisers();
       double[] meanImps = new double[agents.length];
       for (int a = 0; a < agents.length; a++) {
          ArrayList<Double> dailyImps = new ArrayList<Double>();
          String agentName = agents[a];
          LinkedList<QueryReport> agentQueryReports = status.getQueryReports().get(agentName);
          for (int d = 0; d < agentQueryReports.size(); d++) {
             int imps = agentQueryReports.get(d).getImpressions(query);
             if (imps > 0) {
                dailyImps.add(new Double(imps));
             }
          }
          double[] meanAndStdev = getStdDevAndMean(dailyImps);
          meanImps[a] = meanAndStdev[1];
       }
       return meanImps;
    }
 
 
    private double[] getAgentImpressionsDistributionMean2(GameStatus status, Query query) {
       String[] agents = status.getAdvertisers();
       double[] meanImps = new double[agents.length];
       for (int a = 0; a < agents.length; a++) {
          int daysWithImps = 0;
          int aggregateImps = 0;
 
          String agentName = agents[a];
          LinkedList<QueryReport> agentQueryReports = status.getQueryReports().get(agentName);
          for (int d = 0; d < agentQueryReports.size(); d++) {
             int imps = agentQueryReports.get(d).getImpressions(query);
             if (imps > 0) {
                daysWithImps++;
                aggregateImps += imps;
             }
          }
          if (daysWithImps > 0) {
             meanImps[a] = aggregateImps / (double) daysWithImps;
          } else {
             meanImps[a] = Double.NaN;
          }
       }
       return meanImps;
    }
 
    private double[] getAgentImpressionsDistributionStdev2(GameStatus status, Query query) {
       String[] agents = status.getAdvertisers();
       double[] stdevImps = new double[agents.length];
 
       for (int a = 0; a < agents.length; a++) {
          double daysWithImps = 0;
          double aggregateImps = 0;
          double aggregateSquaredImps = 0;
 
          String agentName = agents[a];
          LinkedList<QueryReport> agentQueryReports = status.getQueryReports().get(agentName);
          for (int d = 0; d < agentQueryReports.size(); d++) {
             int imps = agentQueryReports.get(d).getImpressions(query);
             if (imps > 0) {
                daysWithImps++;
                aggregateImps += imps;
                aggregateSquaredImps += Math.pow(imps, 2);
             }
          }
          if (daysWithImps > 0) {
             double mean = aggregateImps / daysWithImps;
             double variance = (aggregateSquaredImps / daysWithImps) - Math.pow(mean, 2);
             stdevImps[a] = Math.sqrt(variance);
          } else {
             stdevImps[a] = Double.NaN;
          }
       }
       return stdevImps;
    }
 
 
    //=======================================================================//
    //=============================== LOGGING ===============================//
    //=======================================================================//
 
    private void initializeLog(String filename) {
       try {
          //Construct the BufferedWriter object
          bufferedWriter = new BufferedWriter(new FileWriter(filename));
 
          //Create header
          StringBuffer sb = new StringBuffer();
          sb.append("model,");
          sb.append("game.idx,");
          sb.append("day.idx,");
          sb.append("query.idx,");
          sb.append("our.bid.rank,");
          sb.append("opp.bid.rank,");
          sb.append("our.avg.pos,");
          sb.append("opp.avg.pos,");
          sb.append("opp.sample.avg.pos,");
          sb.append("focus.level,");
          sb.append("num.participants,");
          sb.append("actual.imps,");
          sb.append("predicted.imps,");
          sb.append("seconds");
 
          //Start writing to the output stream
          bufferedWriter.write(sb.toString());
          bufferedWriter.newLine();
 
       } catch (FileNotFoundException ex) {
          ex.printStackTrace();
       } catch (IOException ex) {
          ex.printStackTrace();
       }
    }
 
    private void closeLog() {
       try {
          if (bufferedWriter != null) {
             bufferedWriter.flush();
             bufferedWriter.close();
          }
       } catch (IOException ex) {
          ex.printStackTrace();
       }
    }
 
    private void writeToLog(String data) {
       try {
          bufferedWriter.write(data);
       } catch (Exception e) {
          e.printStackTrace();
       }
    }
 
 
    /**
     * @param predictedImpsPerAgent
     * @param actualImpsPerAgent
     */
    private void updatePerformanceMetrics(int[] predictedImpsPerAgent, int[] actualImpsPerAgent, int[] predictedOrdering, int[] actualOrdering) {
       assert (predictedImpsPerAgent.length == actualImpsPerAgent.length);
 
       for (int a = 0; a < predictedImpsPerAgent.length; a++) {
          numImprsPredictions++;
          aggregateAbsError += Math.abs(predictedImpsPerAgent[a] - actualImpsPerAgent[a]);
       }
 
       //Keep track of the number of times the ordering was perfect
       //(defined to be "perfect" if every agent that actually saw an impression is in its proper order)
       //(the agent doesn't need to properly order agents that didn't even appear)
       numOrderingPredictions++;
       boolean isCorrectOrdering = true;
       for (int a = 0; a < actualImpsPerAgent.length; a++) {
          if (actualImpsPerAgent[a] > 0) {
             if (predictedOrdering[a] != actualOrdering[a]) {
                isCorrectOrdering = false;
             }
          }
       }
       if (isCorrectOrdering) {
          numCorrectlyOrderedInstances++;
       }
 
       System.out.println("    (predicted ordering): " + Arrays.toString(predictedOrdering));
       System.out.println("       (actual ordering): " + Arrays.toString(actualOrdering));
       System.out.println("   (is correct ordering): " + isCorrectOrdering);
       System.out.println();
 
 
    }
 
    private void outputPerformanceMetrics() {
       double meanAbsError = aggregateAbsError / numImprsPredictions;
       double pctCorrectOrdering = numCorrectlyOrderedInstances / (double) numOrderingPredictions;
       double pctSkipped = (numSkips) / ((double) numInstances);
       System.out.println("Pct Instances Skipped: " + pctSkipped);
       System.out.println("Mean absolute error: " + meanAbsError);
       System.out.println("Pct correctly ordered instances: " + numCorrectlyOrderedInstances + "/" + numOrderingPredictions + "=" + pctCorrectOrdering);
    }
 
 
    /**
     * Get an array of average positions (one element for each agent), for the given day/query
     *
     * @param status
     * @param d
     * @param query
     * @return
     */
    private Double[] getAveragePositions(GameStatus status, int d, Query query) {
       String[] agents = status.getAdvertisers();
       Double[] averagePositions = new Double[agents.length];
       for (int a = 0; a < agents.length; a++) {
          String agentName = agents[a];
          try {
             averagePositions[a] = status.getQueryReports().get(agentName).get(d).getPosition(query);
          } catch (Exception e) {
             //May get here if the agent doesn't have a query report (does this ever happen?)
             averagePositions[a] = Double.NaN;
             throw new RuntimeException("Exception when getting average positions");
          }
       }
       return averagePositions;
    }
 
    private Double[] getSampledAveragePositions(GameStatus status, int d, Query query) {
       String[] agents = status.getAdvertisers();
       Double[] sampledAveragePositions = new Double[agents.length];
 
 //		//DEBUG
 //		for (int b=0; b<agents.length; b++) {
 //		String arbitraryAgentName = agents[b];
 //		StringBuffer sb = new StringBuffer();
 //		sb.append(arbitraryAgentName + ": ");
 //		for (String advertiser : status.getQueryReports().get(arbitraryAgentName).get(d).advertisers(query)) {
 //		double pos = status.getQueryReports().get(arbitraryAgentName).get(d).getPosition(query, advertiser);
 //		sb.append(advertiser+"=" + pos + ", ");
 //		}
 //		System.out.println(sb);
 //		}
 
       //Samples should be the same for every agent. (TODO: verify this)
       String arbitraryAgentName = agents[0];
       for (int a = 0; a < agents.length; a++) {
          String agentName = "adv" + (a + 1); //TODO: does this correspond to names of agents?
          try {
             sampledAveragePositions[a] = status.getQueryReports().get(arbitraryAgentName).get(d).getPosition(query, agentName);
          } catch (Exception e) {
             //May get here if the agent doesn't have a query report (does this ever happen?)
             sampledAveragePositions[a] = Double.NaN;
             throw new RuntimeException("Exception when getting sampled average positions");
          }
       }
       System.out.println("sampleAvgPos=" + Arrays.toString(sampledAveragePositions) + ", agent=" + arbitraryAgentName + ", d=" + d + ", q=" + query);
 
       return sampledAveragePositions;
    }
 
    private Double[] getBids(GameStatus status, int d, Query query) {
       String[] agents = status.getAdvertisers();
       Double[] bids = new Double[agents.length];
       for (int a = 0; a < agents.length; a++) {
          String agentName = agents[a];
          try {
             bids[a] = status.getBidBundles().get(agentName).get(d).getBid(query);
          } catch (Exception e) {
             bids[a] = Double.NaN;
             throw new RuntimeException("Exception when getting bids");
          }
       }
       return bids;
    }
 
    private Double[] getSquashedBids(GameStatus status, int d, Query query) {
       String[] agents = status.getAdvertisers();
       Double[] squashedBids = new Double[agents.length];
       UserClickModel userClickModel = status.getUserClickModel();
       double squashing = status.getPubInfo().getSquashingParameter();
       for (int a = 0; a < agents.length; a++) {
          String agentName = agents[a];
          try {
             double advEffect = userClickModel.getAdvertiserEffect(userClickModel.queryIndex(query), a);
             double bid = status.getBidBundles().get(agentName).get(d).getBid(query);
             squashedBids[a] = bid * Math.pow(advEffect, squashing);
          } catch (Exception e) {
             squashedBids[a] = Double.NaN;
          }
       }
       return squashedBids;
    }
 
 
    private Double[] getBudgets(GameStatus status, int d, Query query) {
       String[] agents = status.getAdvertisers();
       Double[] budgets = new Double[agents.length];
       for (int a = 0; a < agents.length; a++) {
          String agentName = agents[a];
          try {
             budgets[a] = status.getBidBundles().get(agentName).get(d).getDailyLimit(query);
          } catch (Exception e) {
             budgets[a] = Double.NaN;
          }
       }
       return budgets;
    }
 
 
    private Double[] getGlobalBudgets(GameStatus status, int d) {
       String[] agents = status.getAdvertisers();
       Double[] globalBudgets = new Double[agents.length];
       for (int a = 0; a < agents.length; a++) {
          String agentName = agents[a];
          try {
             globalBudgets[a] = status.getBidBundles().get(agentName).get(d).getCampaignDailySpendLimit();
          } catch (Exception e) {
             globalBudgets[a] = Double.NaN;
          }
       }
       return globalBudgets;
    }
 
 
    private Double[] getAdvertiserEffects(GameStatus status, int d, Query query) {
       String[] agents = status.getAdvertisers();
       Double[] advEffects = new Double[agents.length];
       UserClickModel userClickModel = status.getUserClickModel();
       for (int a = 0; a < agents.length; a++) {
          String agentName = agents[a];
          try {
             advEffects[a] = userClickModel.getAdvertiserEffect(userClickModel.queryIndex(query), a);
          } catch (Exception e) {
             advEffects[a] = Double.NaN;
          }
       }
       return advEffects;
    }
 
    private Integer[] getAgentImpressions(GameStatus status, int d, Query query) {
       String[] agents = status.getAdvertisers();
       Integer[] agentImpressions = new Integer[agents.length];
       for (int a = 0; a < agents.length; a++) {
          String agentName = agents[a];
          try {
             agentImpressions[a] = status.getQueryReports().get(agentName).get(d).getImpressions(query);
          } catch (Exception e) {
             //May get here if the agent doesn't have a query report (does this ever happen?)
             agentImpressions[a] = null;
          }
       }
       return agentImpressions;
    }
 
    private Integer[] getAgentClicks(GameStatus status, int d, Query query) {
       String[] agents = status.getAdvertisers();
       Integer[] agentClicks = new Integer[agents.length];
       for (int a = 0; a < agents.length; a++) {
          String agentName = agents[a];
          try {
             agentClicks[a] = status.getQueryReports().get(agentName).get(d).getClicks(query);
          } catch (Exception e) {
             //May get here if the agent doesn't have a query report (does this ever happen?)
             agentClicks[a] = null;
          }
       }
       return agentClicks;
    }
 
    private Integer[] getAgentConversions(GameStatus status, int d, Query query) {
       String[] agents = status.getAdvertisers();
       Integer[] agentConversions = new Integer[agents.length];
       for (int a = 0; a < agents.length; a++) {
          String agentName = agents[a];
          try {
             agentConversions[a] = status.getSalesReports().get(agentName).get(d).getConversions(query);
          } catch (Exception e) {
             //May get here if the agent doesn't have a query report (does this ever happen?)
             agentConversions[a] = null;
          }
       }
       return agentConversions;
    }
 
 
    private Double[] getAgentCosts(GameStatus status, int d, Query query) {
       String[] agents = status.getAdvertisers();
       Double[] agentCosts = new Double[agents.length];
       for (int a = 0; a < agents.length; a++) {
          String agentName = agents[a];
          try {
             agentCosts[a] = status.getQueryReports().get(agentName).get(d).getCost(query);
          } catch (Exception e) {
             //May get here if the agent doesn't have a query report (does this ever happen?)
             agentCosts[a] = null;
          }
       }
       return agentCosts;
    }
 
 
    private Integer[] getAgentPromotedImpressions(GameStatus status, int d, Query query) {
       String[] agents = status.getAdvertisers();
       Integer[] agentImpressions = new Integer[agents.length];
       for (int a = 0; a < agents.length; a++) {
          String agentName = agents[a];
          try {
             agentImpressions[a] = status.getQueryReports().get(agentName).get(d).getPromotedImpressions(query);
          } catch (Exception e) {
             //May get here if the agent doesn't have a query report (does this ever happen?)
             agentImpressions[a] = null;
          }
       }
       return agentImpressions;
    }
 
 
    private Boolean[] getAgentPromotionEligibility(GameStatus status, int d, Query query, double promotedReserveScore) {
       Double[] squashedBids = getSquashedBids(status, d, query);
       Boolean[] promotionEligibility = new Boolean[squashedBids.length];
       //double promotedReserveScore = status.getReserveInfo().getPromotedReserve(); //This is always returning 0.
       for (int a = 0; a < squashedBids.length; a++) {
          try {
             //TODO: Squashed bid must be > or >= promoted reserve score?
             if (squashedBids[a] >= promotedReserveScore) {
                promotionEligibility[a] = true;
             } else {
                promotionEligibility[a] = false;
             }
          } catch (Exception e) {
             //May get here if the agent doesn't have a query report (does this ever happen?)
             promotionEligibility[a] = false;
          }
       }
       return promotionEligibility;
    }
 
 
    /**
     * Determines whether or not each agent hit their query or global budget for the given day.
     * FIXME: it is assumed that an agent hits their budget if an additional click would put them
     * over their spending limit. We are also being slightly liberal about who hit their budget,
     * since we're assuming that the CPC for the last click equals the agent's average CPC.
     * We should check to see how hitting budget is actually determined. (i.e. make sure it's not
     * bids that determine whether the agent has hit its budget)
     *
     * @param status
     * @param d
     * @param query
     * @return
     */
    private Boolean[] getAgentHitBudget(GameStatus status, int d, Query query) {
       String[] agents = status.getAdvertisers();
       Boolean[] agentHitBudget = new Boolean[agents.length];
       for (int a = 0; a < agentHitBudget.length; a++) {
          String agentName = agents[a];
          QueryReport queryReport = status.getQueryReports().get(agentName).get(d);
          BidBundle bidBundle = status.getBidBundles().get(agentName).get(d);
 
          //Get total amount spent for this agent (across queries)
          double globalSpend = 0;
          double maxBid = 0;
          for (Query q : queryReport.keys()) {
             globalSpend += queryReport.getCost(q);
 
             double bid = queryReport.getCPC(q); //double bid = bidBundle.getBid(q);
             if (!Double.isNaN(bid) && bid > maxBid) {
                maxBid = bid;
                //maxBid = Math.max(maxBid, bidBundle.getBid(q) );
             }
          }
 
          //Get amount spent for this agent (for this query only)
          double querySpend = queryReport.getCost(query);
          double queryBid = queryReport.getCPC(query); //double queryBid = bidBundle.getBid(query);
 
          //Get budgets for this agent
          double maxQuerySpend = bidBundle.getDailyLimit(query);
          double maxGlobalSpend = bidBundle.getCampaignDailySpendLimit();
 
          //If an extra click would have put us over a spending limit, we've hit our budget
          //NOTE: This is a liberal estimate: we could say we hit our budget even though we didn't. See method description.
          //TODO: > or >=? What about global budget? Does agent drop out of every auction at once when global budget is hit,
          //  or would it stay in auctions that it can still afford? (should we use maxBid or queryBid?)
          if (querySpend + queryBid > maxQuerySpend ||
                  globalSpend + maxBid > maxGlobalSpend) {
             agentHitBudget[a] = true;
          } else {
             agentHitBudget[a] = false;
          }
       }
       return agentHitBudget;
    }
 
 
    private Double[] getAgentQuerySpend(GameStatus status, int d, Query query) {
       String[] agents = status.getAdvertisers();
       Double[] agentCost = new Double[agents.length];
       for (int a = 0; a < agents.length; a++) {
          String agentName = agents[a];
          agentCost[a] = status.getQueryReports().get(agentName).get(d).getCost(query);
       }
       return agentCost;
    }
 
    private Double[] getAgentGlobalSpend(GameStatus status, int d) {
       String[] agents = status.getAdvertisers();
       Double[] agentCost = new Double[agents.length];
       for (int a = 0; a < agents.length; a++) {
          String agentName = agents[a];
          QueryReport queryReport = status.getQueryReports().get(agentName).get(d);
 
          //Get total amount spent for this agent (across queries)
          double globalSpend = 0;
          for (Query q : queryReport.keys()) {
             globalSpend += queryReport.getCost(q);
          }
          agentCost[a] = globalSpend;
       }
       return agentCost;
    }
 
    private Double[] getAgentGlobalMaxAvgCPC(GameStatus status, int d) {
       String[] agents = status.getAdvertisers();
       Double[] maxCPCs = new Double[agents.length];
       for (int a = 0; a < agents.length; a++) {
          String agentName = agents[a];
          QueryReport queryReport = status.getQueryReports().get(agentName).get(d);
          BidBundle bidBundle = status.getBidBundles().get(agentName).get(d);
 
          //Get total amount spent for this agent (across queries)
          double maxCPC = 0;
          for (Query q : queryReport.keys()) {
             double cpc = queryReport.getCPC(q);
             //System.out.println("a=" + a + ", q=" + q + ", bid=" + bidBundle.getBid(q) + ", CPC=" + queryReport.getCPC(q));
             if (!Double.isNaN(cpc) && cpc > maxCPC) {
                maxCPC = cpc;
             }
          }
          maxCPCs[a] = maxCPC;
       }
       return maxCPCs;
    }
 
 
    //TODO: Make this more precise. (Isn't this value logged anywhere??)
    private HashMap<QueryType, Double> getApproximatePromotedReserveScore(GameStatus status) {
 
       //This is our approximation of promoted reserve score: the lowest score for which someone received a promoted slot.
       HashMap<QueryType, Double> currentLowPromotedScore = new HashMap<QueryType, Double>();
       //double currentLowPromotedScore = Double.POSITIVE_INFINITY;
       currentLowPromotedScore.put(QueryType.FOCUS_LEVEL_ZERO, Double.POSITIVE_INFINITY);
       currentLowPromotedScore.put(QueryType.FOCUS_LEVEL_ONE, Double.POSITIVE_INFINITY);
       currentLowPromotedScore.put(QueryType.FOCUS_LEVEL_TWO, Double.POSITIVE_INFINITY);
 
       // Make the query space
       //TODO: Don't hardcode this here.
       LinkedHashSet<Query> querySpace = new LinkedHashSet<Query>();
       querySpace.add(new Query(null, null)); //F0
       for (Product product : status.getRetailCatalog()) {
          querySpace.add(new Query(product.getManufacturer(), null)); // F1 Manufacturer only
          querySpace.add(new Query(null, product.getComponent())); // F1 Component only
          querySpace.add(new Query(product.getManufacturer(), product.getComponent())); // The F2 query class
       }
       int numDays = 57; //TODO: Don't hardcode this.
       for (int d = 0; d < numDays; d++) {
          for (Query q : querySpace) {
             Integer[] promotedImps = getAgentPromotedImpressions(status, d, q);
             Double[] squashedBids = getSquashedBids(status, d, q);
 
             for (int a = 0; a < promotedImps.length; a++) {
                if (promotedImps[a] > 0 && squashedBids[a] < currentLowPromotedScore.get(q.getType())) {
                   currentLowPromotedScore.put(q.getType(), squashedBids[a]);
                }
             }
          }
       }
       return currentLowPromotedScore;
    }
 
 
    //Get the indices of the vals, starting with the highest val and decreasing.
    public static int[] getIndicesForDescendingOrder(double[] valsUnsorted) {
       double[] vals = valsUnsorted.clone(); //these values will be modified
       int length = vals.length;
 
       int[] ids = new int[length];
       for (int i = 0; i < length; i++) {
          ids[i] = i;
       }
 
       for (int i = 0; i < length; i++) {
          for (int j = i + 1; j < length; j++) {
             if (vals[i] < vals[j]) {
                double tempVal = vals[i];
                int tempId = ids[i];
 
                vals[i] = vals[j];
                ids[i] = ids[j];
 
                vals[j] = tempVal;
                ids[j] = tempId;
             }
          }
       }
       return ids;
    }
 
    private double[] getStdDevAndMean(ArrayList<Double> list) {
       double n = list.size();
       double sum = 0.0;
       for (Double data : list) {
          sum += data;
       }
       double mean = sum / n;
 
       double variance = 0.0;
 
       for (Double data : list) {
          variance += (data - mean) * (data - mean);
       }
 
       variance /= (n - 1);
 
       double[] stdDev = new double[2];
       stdDev[0] = mean;
       stdDev[1] = Math.sqrt(variance);
       return stdDev;
    }
 
 
    public static void runAllTests() throws IOException, ParseException {
       boolean[] trueOrFalseArr = {false, true};
       for (boolean orderingKnown : trueOrFalseArr) {
          for (boolean sampledAvgPositions : trueOrFalseArr) {
             for (boolean perfectImps : trueOrFalseArr) {
                for (boolean useWaterfallPriors : trueOrFalseArr) {
                   ImpressionEstimatorTest evaluator = new ImpressionEstimatorTest(sampledAvgPositions, perfectImps, useWaterfallPriors);
                   double start;
                   double stop;
                   double secondsElapsed;
 
                   for (SolverType solverType : SolverType.values()) {
                      System.out.println("SUMMARY: ---------------");
                      System.out.println("SUMMARY: STARTING TEST: sampled=" + sampledAvgPositions + ", perfectImps=" + perfectImps + ", usePrior=" + useWaterfallPriors + ", solver=" + solverType);
                      start = System.currentTimeMillis();
                      evaluator.impressionEstimatorPredictionChallenge(solverType, orderingKnown);
                      stop = System.currentTimeMillis();
                      secondsElapsed = (stop - start) / 1000.0;
                      System.out.println("SUMMARY: Seconds elapsed: " + secondsElapsed);
                   }
                }
             }
          }
       }
    }
 
 
    public static void main(String[] args) throws IOException, ParseException {
 
 
 //	   ImpressionEstimatorTest.runAllTests();
       boolean ORDERING_KNOWN = true;
       ImpressionEstimatorTest evaluator = new ImpressionEstimatorTest();
       double start;
       double stop;
       double secondsElapsed;
       System.out.println("\n\n\n\n\nSTARTING TEST 1");
       start = System.currentTimeMillis();
 //	   evaluator.impressionEstimatorPredictionChallenge(SolverType.LDSMIP, ORDERING_KNOWN);
 //	   evaluator.impressionEstimatorPredictionChallenge(SolverType.MIP, ORDERING_KNOWN);
       evaluator.impressionEstimatorPredictionChallenge(SolverType.CP, ORDERING_KNOWN);
       stop = System.currentTimeMillis();
       secondsElapsed = (stop - start) / 1000.0;
       System.out.println("SECONDS ELAPSED: " + secondsElapsed);
 
 
 //      ImpressionEstimatorTest evaluator = new ImpressionEstimatorTest();
 //      double start;
 //      double stop;
 //      double secondsElapsed;
 
 //      System.out.println("\n\n\n\n\nSTARTING TEST 1");
 //      start = System.currentTimeMillis();
 //      evaluator.rankedImpressionEstimatorPredictionChallenge(SolverType.CP);
 //      stop = System.currentTimeMillis();
 //      secondsElapsed = (stop - start) / 1000.0;
 //      System.out.println("SECONDS ELAPSED: " + secondsElapsed);
 
 //      System.out.println("\n\n\n\n\nSTARTING TEST 1");
 //      start = System.currentTimeMillis();
 //      evaluator.impressionEstimatorPredictionChallenge(SolverType.CP);
 //      stop = System.currentTimeMillis();
 //      secondsElapsed = (stop - start) / 1000.0;
 //      System.out.println("SECONDS ELAPSED: " + secondsElapsed);
 
 //		System.out.println("\n\n\n\n\nSTARTING TEST 2");
 //		start = System.currentTimeMillis();
 //		evaluator.impressionEstimatorPredictionChallenge(SolverType.MIP);
 //		stop = System.currentTimeMillis();
 //		secondsElapsed = (stop - start)/1000.0;
 //		System.out.println("SECONDS ELAPSED: " + secondsElapsed);
 
       //evaluator.logGameData();
 
       //		double[] vals = {2, 7, 3, 4};
 //		System.out.println(Arrays.toString(ImpressionEstimatorTest.getIndicesForDescendingOrder(vals)));
    }
 
 }
