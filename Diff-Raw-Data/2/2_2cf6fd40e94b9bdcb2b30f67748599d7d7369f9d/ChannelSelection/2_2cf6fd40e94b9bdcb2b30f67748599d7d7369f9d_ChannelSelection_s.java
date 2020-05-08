 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package beamscheduling;
 
 import java.util.*;
 
 /**
  *
  * @author bmumey
  */
 public class ChannelSelection {
     // network
 
     Network network;
     // link channel rates along path
     // path
     Edge[] path;
     int pathLen;
     TreeSet<LinkChannel>[] availPairs;
     // bridging sets
     TreeSet<LinkChannel>[] bridgingSet;
     int[] numBridgingSubsets;
     // best PathCS
     PathCS[][] bestPathCS; // bestPathCS[i][k] = best PathCS for 1,..,i given kth bridging set
 
     public ChannelSelection(Network network) {
         this.network = network;
     }
 
     private void findAvailPairs() {
         availPairs = new TreeSet[pathLen];
         for (int i = 0; i < pathLen; i++) {
             availPairs[i] = new TreeSet();
             Edge e = path[i];
             for (int c = 0; c < e.channels.length; c++) {
                 if (e.channels[c] > 0.0) {
                     //System.out.println("i = " +i);
                     availPairs[i].add(new LinkChannel(i, c));
                 }
             }
         }
     }
 
     private boolean checkPathInterference(int channel, int i, int j) {
         Edge ei = path[i];
         Edge ej = path[j];
         //System.out.println("checking intf " + channel + " " + ei + " " + ej );
         return network.interferes[ei.id][ej.id][channel];
     }
 
     private void computeBridgingSet(int i) {
         bridgingSet[i] = new TreeSet();
 
         // add X_i:
         bridgingSet[i].addAll(availPairs[i]);
         // left side:
         TreeSet<LinkChannel> leftSide = new TreeSet();
         for (int j = 0; j < i; j++) {
             leftSide.addAll(availPairs[j]);
         }
         // right side:
         TreeSet<LinkChannel> rightSide = new TreeSet();
        for (int j = i + 1; j < pathLen; j++) {
             rightSide.addAll(availPairs[j]);
         }
 
         for (LinkChannel left : leftSide) {
             for (LinkChannel right : rightSide) {
                 if (left.channel == right.channel && checkPathInterference(left.channel, left.pathLinkIndex, right.pathLinkIndex)) {
                     bridgingSet[i].add(left); // check that dups not added
                     bridgingSet[i].add(right);
                 }
             }
         }
 
 //        System.out.print("bridging set " + i + ":");
 //        for (LinkChannel lc : bridgingSet[i]) {
 //            System.out.print(" " + lc);
 //        }
 //        System.out.println();
 
         long numSubSets = twoExp(bridgingSet[i].size());
         if (numSubSets > Integer.MAX_VALUE) {
             System.out.println("bridging set too large, exiting...");
             System.exit(-1);
         }
         numBridgingSubsets[i] = (int) numSubSets;
     }
 
     private long twoExp(int exp) {
         long result = 1;
         for (int i = 0; i < exp; i++) {
             result *= 2;
         }
         return result;
     }
 
     public double selectChannels(List<Edge> pathList) {
         PathCS result = new PathCS();
 
         pathLen = pathList.size();
         path = new Edge[pathLen];
         int a = 0;
         for (Edge e : pathList) {
             path[a] = e;
             a++;
         }
 
         // find available pair sets:
         findAvailPairs();
 
         // compute bridging sets:
         bridgingSet = new TreeSet[pathLen];
         numBridgingSubsets = new int[pathLen];
         for (int i = 0; i < pathLen; i++) {
             computeBridgingSet(i);
         }
 
         // initialiize dynamic programming:
         bestPathCS = new PathCS[pathLen][];
         for (int i = 0; i < pathLen; i++) {
             bestPathCS[i] = new PathCS[numBridgingSubsets[i]];
         }
         for (int k = 0; k < numBridgingSubsets[0]; k++) {
             bestPathCS[0][k] = new PathCS(); // with empty paths so far
         }
 
         for (int i = 1; i < pathLen; i++) {
             TreeSet<LinkChannel> newPart = (TreeSet<LinkChannel>) bridgingSet[i].clone();
             newPart.removeAll(bridgingSet[i - 1]);
             TreeSet<LinkChannel> oldPart = (TreeSet<LinkChannel>) bridgingSet[i].clone();
             oldPart.retainAll(bridgingSet[i - 1]);
 //            System.out.println("newPart set " + i + ":" + newPart);
 //            System.out.println("oldPart set " + i + ":" + oldPart);
 
             for (int k = 0; k < numBridgingSubsets[i - 1]; k++) {
                 PathCS bestPrev = bestPathCS[i - 1][k];
                 //System.out.println("bestPathCS[" + (i - 1) + "][" + k + "].throughput = " + bestPrev.throughput);
                 TreeSet<LinkChannel> prevBridgeSubset = fromInt(bridgingSet[i - 1], k);
                 TreeSet<LinkChannel> oldPartCopy = (TreeSet<LinkChannel>) oldPart.clone();
                 oldPartCopy.retainAll(prevBridgeSubset);
                 if (newPart.isEmpty()) {
                     int nextBridgeIndex = toInt(bridgingSet[i], oldPartCopy);
                     PathCS testPathCS = new PathCS();
                     testPathCS.selected = (ArrayList<TreeSet<LinkChannel>>) bestPrev.selected.clone();
                     testPathCS.throughput = bestPrev.throughput;
                     TreeSet<LinkChannel> prevBridgeSubsetCopy = (TreeSet<LinkChannel>) prevBridgeSubset.clone();
                     prevBridgeSubsetCopy.retainAll(availPairs[i - 1]);
                     testPathCS.selected.add(prevBridgeSubsetCopy);
                     evaluateLast(testPathCS, oldPartCopy);
                     if (bestPathCS[i][nextBridgeIndex] == null
                             || testPathCS.throughput > bestPathCS[i][nextBridgeIndex].throughput) {
                         bestPathCS[i][nextBridgeIndex] = testPathCS;
                         //System.out.println("setting bestPathCS[" + i + "][" + nextBridgeIndex + "] throughput = " + bestPathCS[i][nextBridgeIndex].throughput);
                     }
                 } else {
                     for (int l = 0; l < (1 << newPart.size()); l++) {
                         TreeSet<LinkChannel> nextBridgeSubset = fromInt(newPart, l);
                         nextBridgeSubset.addAll(oldPartCopy);
                         //System.out.println("nextBridgeSubset " + nextBridgeSubset);
                         int nextBridgeIndex = toInt(bridgingSet[i], nextBridgeSubset);
                         PathCS testPathCS = new PathCS();
                         testPathCS.selected = (ArrayList<TreeSet<LinkChannel>>) bestPrev.selected.clone();
                         testPathCS.throughput = bestPrev.throughput;
                         TreeSet<LinkChannel> prevBridgeSubsetCopy = (TreeSet<LinkChannel>) prevBridgeSubset.clone();
                         prevBridgeSubsetCopy.retainAll(availPairs[i - 1]);
                         testPathCS.selected.add(prevBridgeSubsetCopy);
                         evaluateLast(testPathCS, nextBridgeSubset);
                         if (bestPathCS[i][nextBridgeIndex] == null
                                 || testPathCS.throughput > bestPathCS[i][nextBridgeIndex].throughput) {
                             bestPathCS[i][nextBridgeIndex] = testPathCS;
                             //System.out.println("setting bestPathCS[" + i + "][" + nextBridgeIndex + "] throughput = " + bestPathCS[i][nextBridgeIndex].throughput);
                         }
                     }
                 }
             }
         }
 
         PathCS optPathCS = null;
         TreeSet<LinkChannel> empty = new TreeSet();
         for (int k = 0; k < numBridgingSubsets[pathLen - 1]; k++) {
             TreeSet<LinkChannel> nextBridgeSubset = fromInt(bridgingSet[pathLen - 1], k);
             PathCS bestPrev = bestPathCS[pathLen - 1][k];
             PathCS testPathCS = new PathCS();
             testPathCS.selected = (ArrayList<TreeSet<LinkChannel>>) bestPrev.selected.clone();
             testPathCS.throughput = bestPrev.throughput;
             TreeSet<LinkChannel> nextBridgeSubsetCopy = (TreeSet<LinkChannel>) nextBridgeSubset.clone();
             nextBridgeSubsetCopy.retainAll(availPairs[pathLen - 1]);
             testPathCS.selected.add(nextBridgeSubsetCopy);
 
             evaluateLast(testPathCS, empty);
             if (optPathCS == null || testPathCS.throughput > optPathCS.throughput) {
                 optPathCS = testPathCS;
             }
         }
 
         if (optPathCS != null) {
             System.out.println("Optimal channel selection: ");
             System.out.println(optPathCS.selected);
             System.out.println("Optimal throughput: " + optPathCS.throughput);
             return optPathCS.throughput;
         }
         return 0.0;
     }
 
     int toInt(TreeSet<LinkChannel> set, TreeSet<LinkChannel> subset) {
         int index = 0;
         int k = 0;
         for (LinkChannel lc : set) {
             if (subset.contains(lc)) {
                 index += (1 << k);
             }
             k++;
         }
         return index;
     }
 
     TreeSet<LinkChannel> fromInt(TreeSet<LinkChannel> set, int index) {
         TreeSet<LinkChannel> subset = new TreeSet();
         int k = 0;
         for (LinkChannel lc : set) {
             if ((index >>> k) % 2 == 1) {
                 subset.add(lc);
             }
             k++;
         }
         return subset;
     }
 
     void evaluateLast(PathCS pathCS, TreeSet<LinkChannel> bridgeSet) {
         int i = pathCS.selected.size() - 1;
         int maxCliqueSize = 1;
         if (pathLen > 1) {
             maxCliqueSize = 2;
         }
         // first check for size 3 cliques if i > 1
         if (i > 1 && i < pathLen - 1) {
             TreeSet<Integer> leftSide = new TreeSet();
             for (LinkChannel lc : pathCS.selected.get(i - 1)) {
                 leftSide.add(new Integer(lc.channel));
             }
             TreeSet<LinkChannel> availPairsCopy = (TreeSet<LinkChannel>) availPairs[i + 1].clone();
             availPairsCopy.retainAll(bridgeSet);
             TreeSet<Integer> rightSide = new TreeSet();
             for (LinkChannel lc : availPairsCopy) {
                 leftSide.add(new Integer(lc.channel));
             }
             leftSide.retainAll(rightSide);
             if (!leftSide.isEmpty()) {
                 maxCliqueSize = 3;
             }
         }
         // now check for channel cliques
         double linkThpt = 0.0;
         for (LinkChannel lc : pathCS.selected.get(i)) {
             int c = lc.channel;
             ArrayList<Integer> linkNums = new ArrayList();
             for (int j = 0; j < i; j++) {
                 LinkChannel lcTest = new LinkChannel(j, c);
                 if (pathCS.selected.get(j).contains(lcTest)) {
                     linkNums.add(j);
                 }
             }
             linkNums.add(i);
             for (LinkChannel lcb : bridgeSet) {
                 if (lcb.channel == c && lcb.pathLinkIndex > i) {
                     linkNums.add(lcb.pathLinkIndex);
                 }
             }
             int maxChanClqSize = Math.max(maxCliqueSize, findMaxCliqueContaining(linkNums, i, c));
             linkThpt += path[i].channels[c] / maxChanClqSize;
         }
         pathCS.throughput = Math.min(pathCS.throughput, linkThpt);
     }
 
     int findMaxCliqueContaining(ArrayList<Integer> linkNums, int loc, int channel) {
         // assumes interval graph
         //System.out.print("checking cliques " + linkNums + " loc " + loc + " channel " + channel);
         int maxSize = 0;
         int i = 0;
         while (i < linkNums.size()) {
             int j = i + 1;
             while (j < linkNums.size() && checkPathInterference(channel, linkNums.get(i), linkNums.get(j))) {
                 j++;
             }
             int size = j - i;
             if (i <= loc && loc <= j && size > maxSize) {
                 maxSize = size;
             }
             i++;
         }
         //System.out.println(" -> size = " + maxSize);
         return maxSize;
     }
 
     class LinkChannel implements Comparable<LinkChannel> {
 
         int pathLinkIndex;
         int channel;
 
         LinkChannel(int index, int c) {
             pathLinkIndex = index;
             channel = c;
         }
 
         public String toString() {
             return "(" + path[pathLinkIndex].id + "," + channel + ")";
         }
 
         public int compareTo(LinkChannel other) {
             if (pathLinkIndex == other.pathLinkIndex) {
                 return (channel - other.channel);
             } else {
                 return (pathLinkIndex - other.pathLinkIndex);
             }
         }
     }
 
     class PathCS {
 
         ArrayList<TreeSet<LinkChannel>> selected;
         double throughput;
 
         PathCS() {
             selected = new ArrayList<TreeSet<LinkChannel>>();
             throughput = Double.MAX_VALUE; // initial unlimited before using any links
         }
 
         void print() {
             for (TreeSet<LinkChannel> ts : selected) {
                 for (LinkChannel lc : ts) {
                     System.out.println(lc);
                 }
             }
         }
     }
 }
