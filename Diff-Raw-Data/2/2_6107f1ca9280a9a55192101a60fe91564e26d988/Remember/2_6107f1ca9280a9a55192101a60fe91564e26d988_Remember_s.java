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
 
 import org.animotron.animi.RuntimeParam;
 import org.animotron.animi.cortex.*;
 
 /**
  * Запоминание
  * 
  * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
  * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
  * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
  */
 public class Remember implements Act<CortexZoneComplex> {
 	
 	//порог запоминания
 	@RuntimeParam(name="mRecLevel")
	private double mRecLevel = 0.3;
 
     public Remember () {}
     
     @Override
     public void process(CortexZoneComplex layer, final int x, final int y) {
     	NeuronComplex cn = layer.col[x][y];
 
     	NeuronSimple _sn_ = null;
     	
     	//есть ли свободные и есть ли добро на запоминание (интервал запоминания)
     	boolean found = false;
     	for (int i = 0; i < cn.s_links.length; i++) {
     		Link3d l = cn.s_links[i];
     		_sn_ = layer.s[l.x][l.y][l.z];
     		if (!_sn_.occupy) {
     			found = true;
     			break;
     		}
     	}
     	if (!found) return;
     	
     	//суммируем минусовку с реципторного слоя колоник
     	NeuronSimple sn = null;
     	double maxSnActive = 0;
     	
     	for (int i = 0; i < cn.s_links.length; i++) {
     		Link3d sl = cn.s_links[i];
     		NeuronSimple _sn = layer.s[sl.x][sl.y][sl.z];
     		
         	double snActive = 0;
     		for (int k = 0; k < _sn.n1; k++) {
     			Link2dZone inL = _sn.s_links[k];
     			
     			NeuronComplex in = inL.zone.col[inL.x][inL.y];
     			
     			snActive += in.minus;
     		}
     		if (snActive > maxSnActive) {
     			maxSnActive = snActive;
     			sn = _sn;
     		}
     	}
     	
 		//поверка по порогу
 //    	if (activeF > 0 && (active < mRecLevel && sn != null))
     	if (sn == null || maxSnActive / sn.n1 < mRecLevel) {
 			return;
     	}
 		
 //    	if (sn == null) {
 //    		sn = _sn_;
 //    		for (int k = 0; k < sn.n1; k++) {
 //    			Link2dZone inL = sn.s_links[k];
 //    			
 //    			NeuronComplex in = inL.zone.col[inL.x][inL.y];
 //    			inL.w = in.active;
 //
 //    			//занулить минусовку простого нейрона
 ////    			in.minus = 0;
 //    		}
 //    	} else {
 	    	
 			//перебираем свободные простые нейроны комплексного нейрона
 			//сумма сигнала синепсов простых неровнов с минусовки
 			//находим максимальный простой нейрон и им запоминаем (от минусовки)
 	    	
 	    	//вес синапса ставим по остаточному всечению
 			for (int k = 0; k < sn.n1; k++) {
 				Link2dZone inL = sn.s_links[k];
 				
 				NeuronComplex in = inL.zone.col[inL.x][inL.y];
 				inL.w = in.minus;
 	
 				//занулить минусовку простого нейрона
 				in.minus = 0;
 			}
 //    	}
     	sn.occupy = true;
     	
     	//присвоить веса сложного нейрона таким образом, чтобы 
     	
     	//текущая активность / на сумму активности (комплекстные нейроны)
 		double active = 0;
 		for (int k = 0; k < sn.n2; k++) {
 			Link2d cnL = sn.a_links[k];
 			
 			active += layer.col[cnL.x][cnL.y].active;
 		}
     	
 		for (int k = 0; k < sn.n2; k++) {
 			Link2d cnL = sn.a_links[k];
 			
 			if (active != 0)
 				cnL.w = layer.col[cnL.x][cnL.y].active / active;
 			else
 				cnL.w = 1;
 			//UNDERSTAND: перераспределять ли веса?
 		}
     }
 }
