 package experiment;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 
 import coalitiongames.Agent;
 import coalitiongames.SearchResult;
 import coalitiongames.SimpleSearchResult;
 import experiment.ProblemGenerator.SimpleSearchAlgorithm;
 
 public abstract class OutputPrinter {
 
     private static final String OUTPUT_FILE = "outputFiles/";
     
     private static final String DESCRIPTION_STRING = "descr";
     
     private static final String DESCRIPTION_HEADER = 
         "n,kMin,kMax,algorithm,solver,dataFileName\n";
     
     private static final String SUMMARY_STRING = "summary";
     
     private static final String SUMMARY_HEADER = 
         "runNumber,runTimeInMillis,numberOfTeams\n";
     
     private static final String RESULT_STRING = "results";
     
     private static final String RESULTS_HEADER = 
         "runNumber,playerId,isCaptain,budget,budgetRank,"
         + "rsdOrderIndex,teamSizeWithSelf,teamUtility," 
             + "teamUtilityNoJitter,meanTeammateUtility,"
         + "meanTeammateUtilityNoJitter,fractionOfTotalUtility," 
             + "fractionOfTotalUtilityNoJitter," 
             + "envyAmount,envyAmountNoJitter,"
         + "envyAmountMinusSingleGood,envyAmountMinusSingleGoodNoJitter," 
             + "meanTeammateRank,meanTeammateRankNoJitter,sumOfReversedRanks,"
         + "sumOfReversedRanksNoJitter,favTeammateRank," 
             + "favTeammateRankNoJitter,leastFavTeammateRank," 
             + "leastFavTeammateRankNoJitter\n";
     private static final char COMMA = ',';
     
     private static final char NEWLINE = '\n';
     
     private static final char UNDERBAR = '_';
     
     public static void main(final String[] args) {
         final List<SimpleSearchResult> searchResults = 
             new ArrayList<SimpleSearchResult>();
         SimpleSearchResult result = 
             ProblemGenerator.getSimpleSearchResult(
                 "inputFiles/bkfrat_1.txt", 
                 SimpleSearchAlgorithm.RSD_OPT
             );
         searchResults.add(result);
         result = ProblemGenerator.getSimpleSearchResult(
                 "inputFiles/bkfrat_1.txt", 
                 SimpleSearchAlgorithm.RSD_OPT
             );
         searchResults.add(result);
         final String algorithmName = "rsdOpt";
         final String solverName = "cplex";
         final String inputFilePrefix = "bkfrat";
         printOutput(searchResults, algorithmName, solverName, inputFilePrefix);
     }
     
     public static void printOutput(
         final List<SimpleSearchResult> searchResults,
         final String algorithmName,
         final String solverName,
         final String inputFilePrefix
     ) {
         printDescriptionOutput(
             searchResults.get(0), algorithmName, solverName, inputFilePrefix
         );
         printSummaryOutput(searchResults, algorithmName, inputFilePrefix);
         printResultOutput(searchResults, algorithmName, inputFilePrefix);
     }
     
     private static void printDescriptionOutput(
         final SimpleSearchResult searchResult,
         final String algorithmName,
         final String solverName,
         final String inputFilePrefix
     ) {
         final String descriptionFileName = 
             OUTPUT_FILE + algorithmName + UNDERBAR + inputFilePrefix + UNDERBAR 
             + DESCRIPTION_STRING + FileHandler.CSV_EXTENSION;
         final StringBuilder builder = new StringBuilder();
         builder.append(DESCRIPTION_HEADER);
         
         final int n = searchResult.getAgents().size();
         final int kMin = searchResult.getkMin();
         final int kMax = searchResult.getkMax();
         builder.append(n).append(COMMA).append(kMin).append(COMMA).
             append(kMax).append(COMMA).append(algorithmName).append(COMMA).
             append(solverName).append(COMMA).
             append(inputFilePrefix).append(NEWLINE);
         FileHandler.writeToFile(descriptionFileName, builder.toString());
     }
     
     private static void printSummaryOutput(
         final List<SimpleSearchResult> searchResults,
         final String algorithmName,
         final String inputFilePrefix
     ) {
         final String summaryFileName = 
             OUTPUT_FILE + algorithmName + UNDERBAR + inputFilePrefix + UNDERBAR 
             + SUMMARY_STRING + FileHandler.CSV_EXTENSION;
         final StringBuilder builder = new StringBuilder();
         builder.append(SUMMARY_HEADER);
         for (int i = 0; i < searchResults.size(); i++) {
             final SimpleSearchResult searchResult = searchResults.get(i);
             final int runNumber = i + 1;
             final long runTimeInMillis = searchResult.getDurationMillis();
             final int numTeams = searchResult.getNumberOfTeams();
             builder.append(runNumber).append(COMMA).
                 append(runTimeInMillis).append(COMMA).
                 append(numTeams).append(NEWLINE);
         }
         
         FileHandler.writeToFile(summaryFileName, builder.toString());
     }
     
     private static void printResultOutput(
         final List<SimpleSearchResult> searchResults,
         final String algorithmName,
         final String inputFilePrefix
     ) {
         final String resultsFileName = 
             OUTPUT_FILE + algorithmName + UNDERBAR + inputFilePrefix + UNDERBAR 
             + RESULT_STRING + FileHandler.CSV_EXTENSION;
         Writer output = null;
         try {
             output = new BufferedWriter(
                 new FileWriter(
                     FileHandler.getFileAndCreateIfNeeded(resultsFileName)
                 ));
             output.write(RESULTS_HEADER);
             
             for (
                 int resultIndex = 0; 
                 resultIndex < searchResults.size(); 
                 resultIndex++
             ) {
                 final SimpleSearchResult result = 
                     searchResults.get(resultIndex);
                 final int runNumber = resultIndex + 1;
                 final List<Agent> agents = result.getAgents();
                 final List<Integer> isCaptainList = result.isCaptain();
                 List<Integer> budgetRanks = null;
                 if (result instanceof SearchResult) {
                     final SearchResult searchResult = (SearchResult) result;
                     budgetRanks = searchResult.getBudgetRanks();
                 }
                 final List<Integer> rsdOrderIndexes = result.getRsdIndexes();
                 final List<Integer> teamSizes = result.getTeamSizesWithSelf();
                 final List<Double> teamUtilities = result.getTeamUtilities();
                 final List<Integer> teamUtilitiesNoJitter = 
                     result.getTeamUtilitiesNoJitter();
                 final List<Double> meanTeammateUtilities = 
                     result.getMeanTeammateUtilities();
                 final List<Double> meanTeammateUtilitiesNoJitter =
                     result.getMeanTeammateUtilitiesNoJitter();
                 final List<Double> fractionsOfTotalUtility =
                     result.getFractionsOfTotalUtility();
                 final List<Double> fractionsOfTotalUtilityNoJitter =
                     result.getFractionsOfTotalUtilityNoJitter();
                 final List<Double> envyAmounts = result.getEnvyAmounts();
                 final List<Integer> envyAmountsNoJitter =
                     result.getEnvyAmountsNoJitter();
                 final List<Double> envyMinusSingleGood = 
                     result.getEnvyAmountsMinusSingleGood();
                 final List<Integer> envyMinusSingleGoodNoJitter =
                     result.getEnvyAmountsMinusSingleGoodNoJitter();
                 final List<Double> meanTeammateRanks = 
                     result.meanTeammateRanks();
                 final List<Double> meanTeammateRanksNoJitter =
                     result.meanTeammateRanksNoJitter();
                 final List<Integer> sumOfReversedRanks = 
                     result.sumsOfReversedRanks();
                 final List<Double> sumOfReversedNoJitter =
                     result.sumsOfReversedRanksNoJitter();
                 final List<Integer> favTeammateRanks =
                     result.favTeammateRanks();
                 final List<Double> favTeammateRanksNoJitter =
                     result.favTeammateRanksNoJitter();
                 final List<Integer> leastFavTeammateRanks =
                     result.leastFavTeammateRanks();
                 final List<Double> leastFavTeammateRanksNoJitter =
                     result.leastFavTeammateRanksNoJitter();
                 
                 for (int j = 0; j < agents.size(); j++) {
                     final Agent agent = agents.get(j);
                     final StringBuilder sb = new StringBuilder();
                     final int playerId = agent.getId();
                     final int isCaptain = isCaptainList.get(j);
                     final double budget = agent.getBudget();
                     int budgetRank;
                     if (budgetRanks == null) {
                         budgetRank = -1;
                     } else {
                         budgetRank = budgetRanks.get(j);
                     }
                     int rsdOrderIndex;
                     if (rsdOrderIndexes == null) {
                         rsdOrderIndex = -1;
                     } else {
                         rsdOrderIndex = rsdOrderIndexes.get(j);
                     }
                     final int teamSize = teamSizes.get(j);
                     final double teamUtility = teamUtilities.get(j);
                     final int teamUtilityNoJitter = 
                         teamUtilitiesNoJitter.get(j);
                     final double meanTeammateUtility = 
                         meanTeammateUtilities.get(j);
                     final double meanTeammateUtilityNoJitter =
                         meanTeammateUtilitiesNoJitter.get(j);
                     final double fractionOfTotalUtility =
                         fractionsOfTotalUtility.get(j);
                     final double fractionOfTotalUtilityNoJitter =
                         fractionsOfTotalUtilityNoJitter.get(j);
                     final double envyAmount = envyAmounts.get(j);
                     final int envyAmountNoJitter = envyAmountsNoJitter.get(j);
                     final double envyMinusSingle = envyMinusSingleGood.get(j);
                     final int envyMinusSingleNoJitter = 
                         envyMinusSingleGoodNoJitter.get(j);
                     final double meanTeammateRank = meanTeammateRanks.get(j);
                     final double meanTeammateRankNoJitter = 
                         meanTeammateRanksNoJitter.get(j);
                     final int sumOfReversedRank = sumOfReversedRanks.get(j);
                     final double sumOfReversedRankNoJitter =
                         sumOfReversedNoJitter.get(j);
                     final int favTeammateRank = favTeammateRanks.get(j);
                     final double favTeammateRankNoJitter =
                         favTeammateRanksNoJitter.get(j);
                     final int leastFavTeammateRank = 
                         leastFavTeammateRanks.get(j);
                     final double leastFavTeammateRankNoJitter =
                         leastFavTeammateRanksNoJitter.get(j);                  
                     sb.append(runNumber).append(COMMA).
                         append(playerId).append(COMMA).
                         append(isCaptain).append(COMMA).
                         append(budget).append(COMMA).
                         append(budgetRank).append(COMMA).
                         append(rsdOrderIndex).append(COMMA).
                         append(teamSize).append(COMMA).
                         append(teamUtility).append(COMMA).
                         append(teamUtilityNoJitter).append(COMMA).
                         append(meanTeammateUtility).append(COMMA).
                         append(meanTeammateUtilityNoJitter).append(COMMA).
                         append(fractionOfTotalUtility).append(COMMA).
                         append(fractionOfTotalUtilityNoJitter).append(COMMA).
                         append(envyAmount).append(COMMA).
                         append(envyAmountNoJitter).append(COMMA).
                         append(envyMinusSingle).append(COMMA).
                         append(envyMinusSingleNoJitter).append(COMMA).
                         append(meanTeammateRank).append(COMMA).
                         append(meanTeammateRankNoJitter).append(COMMA).
                         append(sumOfReversedRank).append(COMMA).
                         append(sumOfReversedRankNoJitter).append(COMMA).
                         append(favTeammateRank).append(COMMA).
                         append(favTeammateRankNoJitter).append(COMMA).
                         append(leastFavTeammateRank).append(COMMA).
                        append(leastFavTeammateRankNoJitter).append(COMMA);
                     sb.append(NEWLINE);
                     output.write(sb.toString());
                 }
             }
             output.write(NEWLINE);
             output.flush();
             output.close();
         } catch (IOException e) {
             if (output != null) {
                 try {
                     output.close();
                 } catch (IOException e1) {
                     e1.printStackTrace();
                 }
             }
             e.printStackTrace();
             return;
         }
     }
 }
