 /**
  * Copyright (c) 2012, Cloudera, Inc. All Rights Reserved.
  *
  * Cloudera, Inc. licenses this file to you under the Apache License,
  * Version 2.0 (the "License"). You may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
  * CONDITIONS OF ANY KIND, either express or implied. See the License for
  * the specific language governing permissions and limitations under the
  * License.
  */
 package com.cloudera.science.matching.graph;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.math.MathContext;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.giraph.graph.EdgeListVertex;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 
 import com.cloudera.science.matching.VertexState;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 /**
  *
  */
 public class BipartiteMatchingVertex extends EdgeListVertex<Text, VertexState, IntWritable, AuctionMessage> {
   
   public Map<Text, IntWritable> getEdges() {
     Map<Text, IntWritable> out = Maps.newHashMap();
     for (Text vertexId : this) {
       out.put(vertexId, getEdgeValue(vertexId));
     }
     return out;
   }
   
   private AuctionMessage getMax(List<AuctionMessage> values, Text currentMatchId) {
     Collections.sort(values);
     if (currentMatchId == null || currentMatchId.toString().isEmpty()) {
       return values.get(0);
     } else {
       AuctionMessage max = values.get(0);
       if (max.getVertexId().equals(currentMatchId)) {
         return max;
       } else {
         AuctionMessage currentValue = null;
         for (int i = 1; i < values.size(); i++) {
           if (values.get(i).getVertexId().equals(currentMatchId)) {
             currentValue = values.get(i);
             break;
           }
         }
         if (currentValue != null) {
           BigDecimal plusEps = currentValue.getValue().add(getEpsilon());
           if (max.getValue().compareTo(plusEps) <= 0) {
             return currentValue;
           }
         }
         return max;
       }
     }
   }
   
   @Override
   public void compute(Iterator<AuctionMessage> msgIterator) throws IOException {
     long superstep = getSuperstep();
     VertexState state = getVertexValue();
     if (state.isBidder()) {
       if (superstep % 2 == 0) {
         // Need to track who I own.
         VertexPriceData vpd = new VertexPriceData(msgIterator, state.getPriceIndex());
         if (vpd.newMatchedId != null) {
           Text currentMatchId = state.getMatchId();
           if (currentMatchId != null && !currentMatchId.toString().isEmpty()) {
             sendMsg(currentMatchId, newSignal(-1));
           }
           state.setMatchId(vpd.newMatchedId);
         } else if (vpd.newLostId != null) {
           state.setMatchId(null);
         }
         List<AuctionMessage> values = Lists.newArrayList();
         for (Text vertexId : this) {
           BigDecimal value = new BigDecimal(getEdgeValue(vertexId).get()).subtract(vpd.getPrice(vertexId));
           values.add(new AuctionMessage(vertexId, value));
         }
         Text currentMatchId = state.getMatchId();
         AuctionMessage target = getMax(values, currentMatchId);
         if (currentMatchId == null || !currentMatchId.equals(target.getVertexId())) {
           BigDecimal bid = null;
           if (values.size() > 1) {
             AuctionMessage runnerUp = values.get(1);
             BigDecimal inc = target.getValue().subtract(runnerUp.getValue()).add(getEpsilon());
             bid = vpd.getPrice(target.getVertexId()).add(inc);
           }
           sendMsg(target.getVertexId(), newMsg(bid));
         } else {
           // Otherwise, I'm happy.
           this.voteToHalt();
         }
       }
     } else {
       if (superstep % 2 == 1) {
         BigDecimal price = state.getPrice();
         List<AuctionMessage> bids = sortBids(msgIterator);
         AuctionMessage rejectionSignal = popRejection(bids);
         if (rejectionSignal != null) {
           state.setMatchId(null);
         }
         if (!bids.isEmpty()) {
           Text currentMatchId = state.getMatchId();
           AuctionMessage winningBid = bids.get(0);
           Text newMatchId = winningBid.getVertexId();
           if (currentMatchId == null ||
               (!currentMatchId.equals(newMatchId) && winningBid.getValue().compareTo(price) > 0)) {
             state.setMatchId(newMatchId);
             state.setPrice(winningBid.getValue());
             // Need to send the owners a heads up.
             if (currentMatchId != null && !currentMatchId.toString().isEmpty()) {
               sendMsg(currentMatchId, newSignal(-1));
             }
             sendMsg(newMatchId, newSignal(1));
           }
           // Announce my price to all the bidders.
           sendMsgToAllEdges(newMsg(state.getPrice()));
         }
       } else {
         // Objects always vote to halt on mod zero iterations.
         this.voteToHalt();
       }
     }
   }
 
   static class VertexPriceData {
     public Map<Text, BigDecimal> prices;
     public Text newMatchedId;
     public Text newLostId;
     
     public VertexPriceData(Iterator<AuctionMessage> iter, Map<Text, BigDecimal> priceIndex) {
       this.prices = priceIndex;
       while (iter.hasNext()) {
         AuctionMessage msg = iter.next();
         if (msg.getSignal() > 0) {
           newMatchedId = msg.getVertexId();
         } else if (msg.getSignal() < 0) {
           newLostId = msg.getVertexId(); 
         } else {
           prices.put(msg.getVertexId(), msg.getValue());
         }
       }
     }
     
     public BigDecimal getPrice(Text vertexId) {
       return prices.containsKey(vertexId) ? prices.get(vertexId) : BigDecimal.ZERO;
     }
   }
   
   private BigDecimal getEpsilon() {
     BigDecimal two = new BigDecimal(2);
    BigDecimal den = BigDecimal.ONE.add(new BigDecimal(getNumVertices()));
     return two.divide(den, MathContext.DECIMAL64);
   }
   
   private AuctionMessage newSignal(int signal) {
     return new AuctionMessage(getVertexId(), signal);
   }
   
   private AuctionMessage newMsg(BigDecimal value) {
     return new AuctionMessage(getVertexId(), value);
   }
   
   private AuctionMessage popRejection(List<AuctionMessage> bids) {
     if (bids.get(bids.size() - 1).getSignal() < 0) {
       return bids.remove(bids.size() - 1);
     }
     return null;
   }
   
   private List<AuctionMessage> sortBids(Iterator<AuctionMessage> msgIterator) {
     List<AuctionMessage> bids = Lists.newArrayList(msgIterator);
     Collections.sort(bids);
     return bids;
   }
 }
