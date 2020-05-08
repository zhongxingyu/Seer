 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package trading.data;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.List;
 import org.encog.ml.data.MLDataPair;
 import org.encog.ml.data.MLDataSet;
 import org.encog.ml.data.buffer.BufferedMLDataSet;
 import org.encog.neural.data.basic.BasicNeuralDataSet;
 import trading.common.NeuralContext;
 import trading.data.model.Bar;
 import trading.data.model.DataPair;
 import trading.data.model.InputEntity;
 import trading.data.model.IdealOutputEntity;
 import trading.data.model.BarEntity;
 
 /**
  * Load bar data to dataset
  *
  * @author pdg
  */
 public class MLBarDataLoader {
 
     /**
      * Get entity pairs from csv file
      * @return 
      */
     public static List<DataPair> getEntityPairs(String smallBarsFilePath, String mediumBarsFilepath, String largeBarsFilePath) throws FileNotFoundException, IOException{
         // Load from csv files to bar arrays
         List<Bar> smallSimpleBars = BarFileLoader.load(NeuralContext.Files.getSmallBarsFilePath());
         List<Bar> mediumSimpleBars = BarFileLoader.load(NeuralContext.Files.getMediumBarsFilePath());
         List<Bar> largeSimpleBars = BarFileLoader.load(NeuralContext.Files.getLargeBarsFilePath());
 
         // Transform bars to relative bars
         List<BarEntity> smallBars = MLBarDataConverter.barsToEntities(smallSimpleBars);
         List<BarEntity> mediumBars = MLBarDataConverter.barsToEntities(mediumSimpleBars);
         List<BarEntity> largeBars = MLBarDataConverter.barsToEntities(largeSimpleBars);
         
         // Bars dates validation
         validateBars(smallBars, mediumBars, largeBars);
 
         // Transform bars to change percents
         List<DataPair> pairs = getEntityPairs(smallBars, mediumBars, largeBars);
         return pairs;
         
     }
     
    
     /**
      * Create buffered ML data set from csv files for 3 bar periods Use file
      * name
      *
      * @return
      * @throws FileNotFoundException
      * @throws IOException
      */
     public static MLDataSet getMLDataSet() throws FileNotFoundException, IOException {
 
 
         // Transform bars to change percents
         List<DataPair> dataPairs = getEntityPairs(NeuralContext.Files.getSmallBarsFilePath(), NeuralContext.Files.getMediumBarsFilePath(), NeuralContext.Files.getLargeBarsFilePath());
         
         int firstIndex = Math.max((dataPairs.size()-1 - NeuralContext.Training.getSamplesCount()), 0);
         int lastIndex = Math.max(dataPairs.size()-1, 0);
         List<DataPair> samplesPairs = dataPairs.subList(firstIndex, lastIndex);
         // Create dataset for machine learning
         MLDataSet ds = getMLDataSet(dataPairs);
         return ds;
     }
 
     /**
      * Create BufferedMLDataSet from data entities
      * @param pairs 
      */
     private static MLDataSet getMLDataSet(List<DataPair> pairs){
        // Create buffered ml data set
         String fileName = NeuralContext.Files.getDataDir() + MLBarDataLoader.class.getName() + ".egb";
         File file = new File(fileName);
         BufferedMLDataSet ds = new BufferedMLDataSet(file);
         
         // Add data pairs
         ds.beginLoad(NeuralContext.NetworkSettings.getInputSize(), NeuralContext.NetworkSettings.getOutputSize());
         for(DataPair pair: pairs){
            MLDataPair mlDataPair = MLBarDataConverter.entityPairToMLDataPair(pair);
            ds.add(mlDataPair);
         }
         ds.endLoad();
         
         return ds;
     }
     
     /**
      * Get network data pairs from bar lists
      * @param smallBars
      * @param mediumBars
      * @param largeBars
      * @return 
      */
     public static List<DataPair> getEntityPairs(List<BarEntity> smallBars, List<BarEntity> mediumBars, List<BarEntity> largeBars){
         List<DataPair> pairs = new ArrayList<>();
 
         int smallPos, mediumPos, largePos;
         mediumPos = mediumBars.size() - 1;
         largePos = largeBars.size() - 1;
        int predictionInterval = (int)NeuralContext.NetworkSettings.getPredictionIntervalMillis();
        Calendar lastSmallTime = GregorianCalendar.getInstance();
        lastSmallTime.setTimeInMillis(smallBars.get(smallBars.size()-1).getTime().getTimeInMillis());
        lastSmallTime.add(Calendar.MILLISECOND, -predictionInterval);
        int startSmallPos = getLastPosBeforeTime(smallBars,lastSmallTime, smallBars.size()-1);
 
 
         // Go through small pos
        for (smallPos = startSmallPos; smallPos > NeuralContext.NetworkSettings.getSmallBarsWindowSize(); smallPos--) {
             // Get window with last x small bars, last y medium bars, last z large bars
             BarEntity smallBar = smallBars.get(smallPos);
            mediumPos = getLastPosBeforeTime(mediumBars, smallBar.getTime(), mediumPos); // Medium pos
            largePos = getLastPosBeforeTime(largeBars, smallBar.getTime(), largePos); // Large pos     
             
             InputEntity input = getInputEntity(smallBars, smallPos, mediumBars, mediumPos, largeBars, largePos);
             IdealOutputEntity ideal = getOutputEntity(smallBars, smallBar);
 
             // Create input/ideal pair
             if (input != null && ideal != null) {
                 DataPair pair = new DataPair(input, ideal);
                 pairs.add(pair);
             }
         }
         
         return pairs;
     }
 
     /**
      * Validate if medium and large bars matches
      */
     private static void validateBars(List<BarEntity> smallBars, List<BarEntity> mediumBars, List<BarEntity> largeBars) {
 
         for (BarEntity smallBar : smallBars) {
             // Medium bars validation
            int lastMediumPos = getLastPosBeforeTime(mediumBars, smallBar.getTime(), mediumBars.size() - 1);
             if (lastMediumPos == -1) {
                 throw new Error(String.format("Can't find medium bar for date %s", smallBar.getTime().getTime().toString()));
             }
             if (lastMediumPos - NeuralContext.NetworkSettings.getMediumBarsWindowSize() < 0) {
                 throw new Error(String.format("Can't find medium bars window with size: %d for date %s", NeuralContext.NetworkSettings.getMediumBarsWindowSize(), smallBar.getTime().getTime().toString()));
             }
 
             // Large bars validation
            int lastLargePos = getLastPosBeforeTime(largeBars, smallBar.getTime(), largeBars.size() - 1);
             if (lastLargePos == -1) {
                 throw new Error(String.format("Can't find large bar for date %s", smallBar.getTime().getTime().toString()));
             }
             if (lastLargePos - NeuralContext.NetworkSettings.getLargeBarsWindowSize() < 0) {
                 throw new Error(String.format("Can't find large bars window with size: %d for date %s", NeuralContext.NetworkSettings.getLargeBarsWindowSize(), smallBar.getTime().getTime().toString()));
             }
         }
     }
 
 
 
     /**
      * Returns window which contains small, medium, large bars before current
      * time This function changes local small, medium, large pos
      *
      * @param smallPos position of small bar
      * @return
      */
     private static InputEntity getInputEntity(List<BarEntity> smallBars, int smallPos, List<BarEntity> mediumBars, int mediumPos, List<BarEntity> largeBars, int largePos) {
         BarEntity smallBar = smallBars.get(smallPos);
        mediumPos = getLastPosBeforeTime(mediumBars, smallBar.getTime(), mediumPos); // Medium pos
        largePos = getLastPosBeforeTime(largeBars, smallBar.getTime(), largePos); // Large pos
         // Get data windows for small, medium, large bars
         List<BarEntity> smallWindow = getInputWindow(smallBars, smallPos, NeuralContext.NetworkSettings.getSmallBarsWindowSize());
         List<BarEntity> mediumWindow = getInputWindow(mediumBars, mediumPos, NeuralContext.NetworkSettings.getMediumBarsWindowSize());
         List<BarEntity> largeWindow = getInputWindow(largeBars, largePos, NeuralContext.NetworkSettings.getLargeBarsWindowSize());
 
         InputEntity input = new InputEntity(smallWindow, mediumWindow, largeWindow);
 
         return input;
     }
 
 
     
     
     /**
      * Get bars inside the window
      *
      * @param bars
      * @param endIndex
      * @param windowSize
      * @return
      */
     private static List<BarEntity> getInputWindow(List<BarEntity> bars, int endIndex, int windowSize) {
         List<BarEntity> result = new ArrayList<>();
         if (endIndex - windowSize < 0) {
             return result;
         }
         result = bars.subList(endIndex - windowSize, endIndex);
         return result;
     }
 
     /**
      * Get data for decision result
      *
      * @param bars
      * @param pos
      * @param currentTime
      * @return
      */
     private static IdealOutputEntity getOutputEntity(List<BarEntity> bars, BarEntity bar) {
         int startIndex = bars.indexOf(bar);
         if (startIndex == -1) {
             return null;
         }
         IdealOutputEntity result = null;
         
         long currentMillis = bar.getTime().getTimeInMillis();
         long predictionIntervalMillis = NeuralContext.NetworkSettings.getPredictionIntervalMillis();
         double highBoundAbsolute = 0;
         double lowBoundAbsolute = Double.MAX_VALUE;
         
         // Get high and low boundds within interval
         for (int i = startIndex; i < bars.size(); i++) {
             BarEntity currentBar = bars.get(i);
             long intervalMillis = currentBar.getTime().getTimeInMillis() - currentMillis;
 
             // If bar after time interval
             if (intervalMillis >= predictionIntervalMillis) {
                 //outputBar = bar;
                 break;
             }
             highBoundAbsolute = Math.max(highBoundAbsolute, currentBar.getAbsoluteBar().getHigh());
             lowBoundAbsolute = Math.min(lowBoundAbsolute, currentBar.getAbsoluteBar().getLow());
         }
         result = new IdealOutputEntity(bar, highBoundAbsolute, lowBoundAbsolute);
         // Null if no bars after interval
         return result;
     }
 
     /**
      * Get last position of a bar before the time
      *
      * @param bars
      * @param time
      * @param initialPos
      * @return
      */
    private static int getLastPosBeforeTime(List<BarEntity> bars, Calendar time, int initialPos) {
         if (bars.isEmpty()) {
             return -1;
         }
         int pos = -1;
         for (int i = initialPos; i >= 0; i--) {
             BarEntity bar = bars.get(i);
             // We found the bar closed before current time
             if (bar.getTime().getTimeInMillis() <= time.getTimeInMillis()) {
                 pos = i;
                 break;
             }
         }
 
         return pos;
     }
     
 }
