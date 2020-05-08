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
 
 import java.util.ArrayList;
 import java.util.Random;
 
 /**
  * @author: Hasala Surasinghe
  * @version: ${Revision}
  */
 public class BidGenerator {
 
     private Random random = new Random();
     private float sampleSpread = 2;
     private int range = 2;
     private ArrayList<Float> orders;
 
     public BidGenerator(){
 
         orders = new ArrayList<>();
 
     }
 
     private void generateSellOrders(double closingPrice, int sellOrderCount){
 
         for (int i = 0; i < sellOrderCount; i++) {
 
            float order = (float) (closingPrice-sampleSpread/2 - random.nextFloat()*range);
             orders.add(order);
 
         }
 
     }
 
     private void generateBuyOrders(double closingPrice, int buyOrderCount){
 
 
         for (int i = 0; i < buyOrderCount; i++) {
 
            float order = (float) (closingPrice+sampleSpread/2 + random.nextFloat()*range);
             orders.add(order);
 
         }
 
     }
 
    public ArrayList<Float> getOrders(double closingPrice, int sellOrderCount, int buyOrderCount){
 
        generateSellOrders(closingPrice, sellOrderCount);
         generateBuyOrders(closingPrice, buyOrderCount);
 
         return orders;
 
     }
 }
