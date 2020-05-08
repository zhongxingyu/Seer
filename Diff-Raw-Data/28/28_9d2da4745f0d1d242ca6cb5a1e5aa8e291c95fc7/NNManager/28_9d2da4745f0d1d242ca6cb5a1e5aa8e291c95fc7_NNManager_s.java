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
 import org.investovator.ann.data.DataManager;
 import org.investovator.core.data.api.utils.TradingDataAttribute;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  *
  * @author: Hasala Surasinghe
  * @version: ${Revision}
  *
  */
 public class NNManager {
 
    final int iterationCount = 10000;
    final double error = 0.001;
 
     private HashMap<String,String> newParameters;
     private ArrayList<TradingDataAttribute> inputParameters;
     private String stockID;
     private NNCreator nnCreator;
     private NNTrainer nnTrainer;
     private boolean status;
     private int inputParamCount;
     private DataManager dataManager;
 
     public NNManager(HashMap newParameters,ArrayList<TradingDataAttribute> inputParameters,String stockID){
         this.newParameters = newParameters;
         this.inputParameters = inputParameters;
         this.stockID = stockID;
         this.inputParamCount = inputParameters.size() + newParameters.size();
         status = false;
     }
 
     public NNManager(ArrayList<TradingDataAttribute> inputParameters,String stockID){
         this.inputParameters = inputParameters;
         this.stockID = stockID;
         this.inputParamCount = inputParameters.size();
        this.inputParamCount = 6;
         status = false;
     }
 
     public boolean createNeuralNetwork(){
 
         nnCreator = new NNCreator(inputParamCount,1);
         nnTrainer = new NNTrainer();
         dataManager = new DataManager(stockID,nnTrainer,inputParameters);
 
         int inputParameterCount = inputParameters.size();
 
        nnTrainer.setIterationCount(iterationCount);
        nnTrainer.setError(error);
 
         for(int i = 0;i < inputParameterCount; i++){
 
             BasicNetwork network = nnCreator.createNetwork();
             dataManager.prepareData(inputParameters.get(i));            //specifies predicting attribute
             status = nnTrainer.TrainANN(network,stockID);
 
        }
 
 
 
         return status;
     }
 
 
 }
