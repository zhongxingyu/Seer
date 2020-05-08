 /**
 *   CASSP.java
 *
 *   Copyright (c) 2013 Vladimir Brigant
 *   This software is distributed under the terms of the GNU General Public License.
 */
 
 package cassp;
 
 
 import java.io.*;
 import java.util.*;
 
 import org.apache.log4j.Logger;
 
 import cassp.ea.*;
 import cassp.ca.*;
 import cassp.data.*;
 import cassp.utils.*;
 import cassp.config.*;
 import cassp.ea.stats.*;
 import cassp.ca.rules.*;
 
 
 
 /**
 * CASSP = cellular automata secondary structure predictor<p>
 * This class provides API for training, testing, cross-validating and predicting
 * protein secondary structure based on specified dataset.
 * @see Data
 */
 public class CASSP {
 
     static Logger logger = Logger.getLogger(CASSP.class);
 
     private SimConfig config;
     private Data data;
     private Data testData;
     private Data[] cvData;
     private CARule rule;
     private EAStats eaStats;
     private AccuracyStats accStats;
 
 
     /**
     * CASSP Constructor
     * @param config configuration object
     */
     public CASSP(SimConfig config){
         this.config = config;
         this.accStats = null;
     }
 
 
     /**
     * Training to find out the fittest rule.
     * @return Best rule.
     */
     public double train(){
         this.setupData();
         double accuracy = 0.0;
 
         if (this.config.getCVFolds() > 0)
             accuracy = this.crossValidate(this.config.getCVFolds());
         else
             this.rule = this.trainRule(this.data);
         this.saveRule();
         return accuracy;
     }
 
 
     // load data and load/compute neccessary coefficients
     private void setupData(){
         this.data = new Data(this.config.getDataPath());
 
         // get Chou-Fasman coefficients
         String cfPath = this.config.getDataCFPath();
         if (cfPath != null && cfPath.length() > 0)
             this.data.loadChouFasman(this.config.getDataCFPath());
         else
             this.data.computeChouFasman();
 
         // get conformation coefficients
         String ccPath = this.config.getDataCCPath();
         if (ccPath != null && ccPath.length() > 0)
             this.data.loadConformCoeffs(this.config.getDataCCPath());
         else
             this.data.computeConformCoeffs();
     }
 
 
     private CARule trainRule(Data data){
         SSPEA evolAlg = new SSPEA(config, data);
         CARule rule = null;
 
         try {
             rule = evolAlg.evolve();
             rule.setAminoAcids(data.getAminoAcids());
             this.eaStats = evolAlg.getStats();
         }
         catch (Exception e) {
             e.printStackTrace();
         }
         return rule;
     }
 
 
     /**
     * Application of trained rule to test dataset.
     * @return accuracy of specified type (Q3 or SOV)
     */
     public double test(){
         this.testData = new Data(this.config.getTestDataPath());
         this.rule = this.loadRule();
         this.testData.setAminoAcids(this.rule.getAminoAcids());
         this.testRule(this.testData);
 
         return this.computeAccuracy(this.testData);
     }
 
 
     /**
     *
     */
     public double testPsipred(){
 
         Data data = new Data(this.config.getTestDataPath());
         Psipred psipred = new Psipred(this.config.getPsipredPath());
 
         // 1. run Psipred
         for (DataItem dataItem: data.getData()){
             psipred.predict(dataItem);
         }
 
         // 2. run CA if Psipred result is not reliable
         for (DataItem dataItem: data.getData()){
             dataItem.repairPsipred(this.predict(dataItem.getAaSeq()));
         }
 
         return this.computeAccuracy(data);
     }
 
 
     private double computeAccuracy(Data data){
         double accuracy = 0.0;
 
         if (this.config.getAccuracyType() == SimConfig.Q3)
             accuracy = Utils.q3(data);
         else if (this.config.getAccuracyType() == SimConfig.SOV)
             accuracy = Utils.sov(data);
         return accuracy;
     }
 
 
     private void testRule(Data testData){
         for (DataItem di: testData.getData()){
             this.predict(di);
         }
     }
 
 
     /**
     * Secondary structure prediction of amino acid sequence.
     *
     * @param aaSeq amino acid sequence
     * @return predicted secondary structure sequence
     */
     public String predict(String aaSeq){
         DataItem di = new DataItem();
         di.setAaSeq(aaSeq);
 
         CellularAutomaton ca = new CellularAutomaton(di, this.config);
         ca.run(this.rule, this.testData);
 
         return ca.getPredictedSeq();
     }
 
 
     /**
     * Secondary structure prediction of DataItem object
     * containing amino acid sequence.
     *
     * @param di data item
     * @return predicted secondary structure sequence
     */
     public String predict(DataItem di){
         CellularAutomaton ca = new CellularAutomaton(di, this.config);
         ca.run(this.rule, this.testData);
         return di.getPredSeq();
     }
 
 
     /**
     * Cross-validation - dataset is divided to <folds> parts.
     *
     * @param folds number of sub-trainings and sub-testings
     * @return mean accuracy of sub-tests
     */
     public double crossValidate(int folds){
         if (folds < 1) return -1;
 
         // structures creation + data splitting
         this.cvData = new Data[folds];
         for (int i = 0; i < folds; i++) {
             this.cvData[i] = new Data();
             this.cvData[i].setAminoAcids(this.data.getAminoAcids());
         }
 
         for (int i = 0; i < this.data.length(); i++) {
             this.cvData[i % folds].add(this.data.get(i));
         }
 
         // training & testing "folds" times
         double acc_sum = 0.0;
 
         for (int i = 0; i < folds; i++) {
             // merge data parts
             Data mergedData = new Data();
             Data testData = cvData[i];
 
             for (int j = 0; j < folds; j++) {
                 if (j != i)
                     mergedData.merge(cvData[j]);
             }
 
             this.rule = this.trainRule(mergedData);
             this.testRule(testData);
 
             if (this.config.getAccuracyType() == SimConfig.Q3)
                 acc_sum += Utils.q3(testData);
             else if (this.config.getAccuracyType() == SimConfig.SOV)
                 acc_sum += Utils.sov(testData);
         }
 
         // average accuracy
         return  acc_sum/folds;
     }
 
 
     /**
     * Creates PNG image of fitness value evolution in generations.
     * @param name file name of created image
     */
     public void createEvolutionImage(String name){
         if (this.data == null){
             logger.warn("No training was performed!");
             return;
         }
 
         if (this.accStats == null)
             this.computeAccuracyStats();
         this.eaStats.createImage(this.config.getStatsPath(), name);
         Utils.removeTXTFiles(this.config.getStatsPath());
     }
 
 
     /**
     * Creates PNG file of accuracy depending on reliabiliry classes.
     * @param name file name of created image
     */
     public void createReliabImage(String name){
         if (this.testData == null){
            if (this.data == null){
                 logger.warn("No training or testing was performed!.");
                 return;
            }
         }
         else{
             this.data = this.testData;
             this.data.computeConformCoeffs();
             this.data.computeChouFasman();
         }
 
         if (this.accStats == null)
             this.computeAccuracyStats();
 
         this.accStats.createReliabImage(this.config.getStatsPath(), name);
         Utils.removeTXTFiles(this.config.getStatsPath());
     }
 
 
     /**
     * Creates PNG file illustrating histogram of accuracy classes.
     * @param name file name of created image
     */
     public void createAccClassesImage(String name){
         if (this.testData == null){
            if (this.data == null){
                 logger.warn("No training or testing was performed!.");
                 return;
            }
         }
         else{
             this.data = this.testData;
             this.data.computeConformCoeffs();
             this.data.computeChouFasman();
         }
 
         if (this.accStats == null)
             this.computeAccuracyStats();
         this.accStats.createAccClassesImage(this.config.getStatsPath(), name);
         Utils.removeTXTFiles(this.config.getStatsPath());
     }
 
 
     /**
     * Computes all neccessary statistics.
     */
     public void computeAccuracyStats(){
         this.accStats = new AccuracyStats(
             this.rule.getMaxPropsDiff(new double[]{this.data.getMaxCF(), this.data.getMaxCC()}),
             this.config.getReliabClasses(),
             this.config.getAccClasses()
         );
         this.accStats.parseStats(this.data, this.config.getAccuracyType());
     }
 
 
     /**
     * Rule serialization.
     */
     public void saveRule(){
         try{
             FileOutputStream fileOut = new FileOutputStream(this.config.getBestRulePath());
             ObjectOutputStream out = new ObjectOutputStream(fileOut);
             out.writeObject(this.rule);
             out.close();
             fileOut.close();
         }catch(IOException i){
             i.printStackTrace();
         }
     }
 
 
 
     /**
     * Rule deserialization.
     */
     public CARule loadRule(){
         try{
             FileInputStream fileIn = new FileInputStream(this.config.getBestRulePath());
             ObjectInputStream in = new ObjectInputStream(fileIn);
             this.rule = (CARule) in.readObject();
             in.close();
             fileIn.close();
         }catch(IOException i){
             i.printStackTrace();
         }catch(ClassNotFoundException c){
             c.printStackTrace();
         }
         return this.rule;
     }
 
 
 
     /* Getters & setters */
 
     public AccuracyStats getAccuracyStats(){
         return this.accStats;
     }
 
     public EAStats getEAStats(){
         return this.eaStats;
     }
 }
