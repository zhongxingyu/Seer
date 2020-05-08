 /*
  *  Copyright (C) 2012 The Animo Project
  *  http://animotron.org
  *
  *  This file is part of Animi.
  *
  *  Animotron is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as
  *  published by the Free Software Foundation, either version 3 of
  *  the License, or (at your option) any later version.
  *
  *  Animotron is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of
  *  the GNU Affero General Public License along with Animotron.
  *  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.animotron.animi.acts;
 
 import org.animotron.animi.cortex.*;
 
 /**
  * Активация простых нейронов при узнавании запомненной картины
  * 
  * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
  * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
  * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
  */
 public class Restructorization implements Act<CortexZoneComplex> {
 
 	public Restructorization() {}
 
     @Override
     public void process(CortexZoneComplex layer, final int x, final int y) {
     	
     	for (int z = 0; z < layer.deep; z++) {
     		NeuronSimple sn = layer.s[x][y][z];
     		if (sn.active > 0) {
     			for (Link link : sn.a_links) {
     				
     				NeuronComplex cn = (NeuronComplex) link.axon;
     				if (link.w > 0) {
     					normalization(cn, sn);
     				}
     			}
     		}
     	}
     }
 	public static void normalization(NeuronComplex cn, NeuronSimple sn) {
 		double sum = 0, delta = 0, wSum = 0;
 
     	for (Link link : cn.s_links) {
 			
 			if (link.synapse == sn) {
 				delta = cn.active * sn.active;
 			}
 
 			sum += link.stability;
 			wSum += Math.abs( link.w );
 		}
     	
     	if (sum == 0) {
     		System.out.println("WARNING: sum of stability == 0");
     		return;
     	}
     	
    	if (wSum == 0) {
    		System.out.println("WARNING: wSum of stability == 0");
        	for (Link link : cn.s_links) {
    			if (link.synapse == sn)
    				link.w += delta;
        	}
    		return;
    	}

     	if (Double.isNaN(delta))
     		return;
 
 //    	System.out.println("before "+delta);
     	
     	delta /= sum;
     	
     	if (Double.isNaN(delta))
     		return;
     	
     	System.out.println("=============================================================");
     	System.out.println(""+delta);
 
     	for (Link link : cn.s_links) {
     		if (!link.synapse.isOccupy())
     			continue;
     		
 			if (link.synapse == sn)
 				link.w += delta * Math.abs(link.w) / wSum;
 
 			link.w -= delta * Math.abs(link.w) / wSum;
 			
			if (Double.isNaN(link.w)) { 
 				link.w = 0;
 			}
 			System.out.println(link.w);
 		}
 	}
 }
