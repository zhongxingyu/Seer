 /*
  * investovator, Stock Market Gaming Framework
  *     Copyright (C) 2013  investovator
  *
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.investovator.ann.neuralnet;
 
 import org.encog.neural.networks.BasicNetwork;
 import org.investovator.ann.data.AnalysisDataManager;
 import org.investovator.ann.data.DataManager;
 import org.investovator.ann.nngaming.util.GameTypes;
 import org.investovator.core.data.api.utils.TradingDataAttribute;
 
 import java.util.ArrayList;
 
 /**
  *
  * @author: Hasala Surasinghe
  * @version: ${Revision}
  *
  */
 public class NNManager {
 
     final int GAMING_ITERATION_COUNT = 5000;
     final int ANALYSIS_ITERATION_COUNT = 10000;
     final double ERROR = 0.001;
     final int GAMING_INPUT_PARAM_COUNT = 6;
     final int OUTPUT_PARAM_COUNT = 1;
     final int ANALYSIS_INPUT_PARAM_COUNT = 7;
 
     private ArrayList<TradingDataAttribute> inputParameters;
     private ArrayList<String> stockIDs;
     private ArrayList<String> analysisParameters;
     private NNCreator nnCreator;
     private NNTrainer nnTrainer;
     private DataManager dataManager;
 
     public NNManager(ArrayList<TradingDataAttribute> inputParameters,ArrayList<String> stockIDs,
                      ArrayList<String> analysisParameters){
         this.inputParameters = inputParameters;
         this.stockIDs = stockIDs;
         this.analysisParameters = analysisParameters;
     }
 
     public void createGamingNeuralNetworks(){
 
         nnCreator = new NNCreator(GAMING_INPUT_PARAM_COUNT,OUTPUT_PARAM_COUNT);
         nnTrainer = new NNTrainer();
 
         int inputParameterCount = inputParameters.size();
 
         nnTrainer.setIterationCount(GAMING_ITERATION_COUNT);
         nnTrainer.setError(ERROR);
 
         for(int k = 0; k < stockIDs.size(); k++){
 
             dataManager = new DataManager(stockIDs.get(k),nnTrainer,inputParameters,GameTypes.TRADING_GAME);
 
             for(int i = 0;i < inputParameterCount; i++){
 
                 BasicNetwork network = nnCreator.createNetwork();
                 dataManager.prepareData(inputParameters.get(i));            //specifies predicting attribute
                 nnTrainer.TrainANN(network,stockIDs.get(k), GameTypes.TRADING_GAME);
 
             }
 
         }
 
     }
 
     public void createAnalysisNeuralNetworks(){
 
         AnalysisDataManager analysisDataManager;
         nnCreator = new NNCreator(ANALYSIS_INPUT_PARAM_COUNT,OUTPUT_PARAM_COUNT);
         nnTrainer = new NNTrainer();
 
         nnTrainer.setIterationCount(ANALYSIS_ITERATION_COUNT);
         nnTrainer.setError(ERROR);
 
         for(int k = 0; k < stockIDs.size(); k++){
 
             for(int j = 0; j < analysisParameters.size(); j++){
 
                if(stockIDs.get(k).equals(analysisParameters.get(j)))
                     continue;
 
                 analysisDataManager = new AnalysisDataManager(stockIDs.get(k),nnTrainer,inputParameters,
                         GameTypes.ANALYSIS_GAME,analysisParameters.get(j));
 
                     BasicNetwork network = nnCreator.createNetwork();
                     analysisDataManager.prepareData(TradingDataAttribute.CLOSING_PRICE);            //specifies predicting attribute
                     nnTrainer.TrainANN(network,stockIDs.get(k),GameTypes.ANALYSIS_GAME);
 
             }
         }
 
 
     }
 
 
 }
