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
 
 package org.investovator.ann.nngaming;
 
 import org.investovator.ann.data.DataRetriever;
 import org.investovator.ann.data.datanormalizing.DataNormalizer;
 import org.investovator.core.data.api.utils.TradingDataAttribute;
 
 import java.util.ArrayList;
 
 /**
  * @author: Hasala Surasinghe
  * @version: ${Revision}
  */
 public class PredictionValueManager {
 
    private int numOfDays;
     private float[] predictedValues;
     private NNPredictor nnPredictor;
     private double[] inputData;
     private ArrayList<TradingDataAttribute> attributes;
     private DataNormalizer dataNormalizer;
     private String stockID;
 
     public PredictionValueManager(String stockID){
         this.stockID = stockID;
        this.numOfDays = 5;                         //todo
         this.nnPredictor = new NNPredictor();
        this.predictedValues = new float[numOfDays];
         this.attributes = new ArrayList<>();
         attributes.add(TradingDataAttribute.HIGH_PRICE);
         attributes.add(TradingDataAttribute.LOW_PRICE);
         attributes.add(TradingDataAttribute.CLOSING_PRICE);
         attributes.add(TradingDataAttribute.SHARES);
         attributes.add(TradingDataAttribute.TRADES);
         attributes.add(TradingDataAttribute.TURNOVER);
 
     }
 
     public float[] getAllPredictionValues(){
 
         DataRetriever dataRetriever = new DataRetriever();
         inputData = dataRetriever.getGamingData(stockID,attributes);
         normalizeData();
 
        for (int i = 0; i < numOfDays; i++){
 
             predictedValues[i] = (float) nnPredictor.getPredictedValue(stockID,
                     TradingDataAttribute.CLOSING_PRICE, inputData);
 
             updateInputData();
 
         }
 
         return predictedValues;
     }
 
     private void updateInputData(){
 
         int attributeCount = attributes.size();
         double[] temp = new double[attributeCount];
 
         for(int i = 0; i < attributeCount; i ++){
 
             temp[i] = nnPredictor.getPredictedValue(stockID,attributes.get(i),inputData);
 
         }
         inputData = temp;
         normalizeData();
     }
 
     private void normalizeData(){
 
         dataNormalizer = new DataNormalizer(stockID);
 
         for(int i = 0;i < inputData.length; i++){
 
             inputData[i] = dataNormalizer.getNormalizedValue(inputData[i],attributes.get(i));
 
         }
 
     }
 }
