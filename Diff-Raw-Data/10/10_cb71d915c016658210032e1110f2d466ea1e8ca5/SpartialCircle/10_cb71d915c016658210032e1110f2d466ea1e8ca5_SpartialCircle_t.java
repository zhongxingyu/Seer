 /*
  GossipNetSim
  Copyright (C) 2012  michael theodorides <mc.theodorides@gmail.com>
 
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package us.elfua.gossipnetsim.simulator.algorithm;
 
 import static java.lang.Math.pow;
 import java.util.LinkedList;
 import java.util.List;
 import us.elfua.gossipnetsim.helpers.Randomness;
 import us.elfua.gossipnetsim.simulator.Node;
 import us.elfua.gossipnetsim.simulator.Settings;
 import us.elfua.gossipnetsim.simulator.geometry.Position;
 import us.elfua.gossipnetsim.simulator.interfaces.IAlgorithm;
 import us.elfua.gossipnetsim.simulator.interfaces.IGeometry;
 
 /**
  *
  * @author michael theodorides
  */
 public class SpartialCircle implements IAlgorithm {
 
     private List<Node> accessible;
     private Position pos;
     private Settings settings;
     private double C;
     private double[] probabilities;
     private double q;
     private double sumOfPropabilities = 0;
 
     @Override
     public LinkedList<Node> getNextNodes(Position pos, LinkedList<Node> accessible, Settings set) {
 
         if (this.settings == null) {
 
             this.settings = set;
 
         } else if (!(this.settings.hashCode() == set.hashCode())) {
 
             this.settings = set;
         }
 
         if (this.accessible == null || this.pos == null) {
 
             this.q = this.settings.getQ();
 
            
                 this.accessible = accessible;
                 this.pos = pos;
                 this.culcC();
                 this.culProbabilities();
 
            
 
         } else if (!(this.accessible.hashCode() == accessible.hashCode()) || !(this.pos.hashCode() == pos.hashCode())) {
 
             this.q = this.settings.getQ();
 
             while (this.sumOfPropabilities > 1.01 || this.sumOfPropabilities < 0.9) {
 
                 this.accessible = accessible;
                 this.pos = pos;
                 this.culcC();
                 this.culProbabilities();
 
                 if (this.sumOfPropabilities > 1) {
                     this.q -= 0.00001;
                 } else {
                     this.q += 0.00001;
                 }
             }
 
             
         }
 
         return getList();
     }
 
     private void culcSumOfPropabilities() {
 
         int propSize = this.probabilities.length;
 
         for (int i = 0; i < propSize; i++) {
 
             this.sumOfPropabilities += this.probabilities[i];
 
         }
     }
 
     private void culcC() {
        
        
         int N = this.accessible.size();
         IGeometry geo = this.settings.getGeometry();
         double sum = 0;
 
 
         for (int i = 0; i < N; i++) {
             if (!(this.accessible.get(i).getPosition().hashCode() == this.pos.hashCode())) {
                sum += 1 / ( pow((geo.getDistance(pos, this.accessible.get(i).getPosition())), (2 * this.q)) );
             }
         }
 
        this.C = 1 / sum;
        
        
     }
 
     private void culProbabilities() {
         int N = this.accessible.size();
         IGeometry geo = this.settings.getGeometry();
         this.probabilities = new double[N];
 
         for (int i = 0; i < N; i++) {
 
             this.probabilities[i] = this.C / pow((geo.getDistance(pos, this.accessible.get(i).getPosition())), (2 * this.q));
         }
     }
 
     private LinkedList<Node> getList() {
         LinkedList<Node> temp = new LinkedList<Node>();
         int N = this.accessible.size();
         Randomness rand = null;
 
 
 
         for (int i = 0; i < N; i++) {
 
             rand = new Randomness();
             double d = rand.getRandomDouble();
             double pr = this.probabilities[i];
 
             if (d <= pr) {
                 temp.add(this.accessible.get(i));
             }
         }
 
         return temp;
     }
 }
