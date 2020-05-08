 /*
 Musicians Assistant
     Copyright (C) 2014  Zdeněk Janeček <jan.zdenek@gmail.com>
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package kiv.janecekz.ma.tuner;
 
 import kiv.janecekz.ma.tuner.Classificator.Result;
 
 public class MedianFilter {
 	private class ResultEntry {
 		private Result res;
 		public int age;
 
 		public ResultEntry(Result res) {
 			this.res = res;
 			this.age = 0;
 		}
 	}
 
	private final int SIZE = 7;
 
 	ResultEntry[] values = new ResultEntry[SIZE + 1];
 	int added = 0;
 
 	public void addValue(Result val) {
 		// make them older
 		for (int i = 0; i < added; i++) {
 			values[i].age++;
 		}
 
 		// insert to the end of array
 		values[added++] = new ResultEntry(val);
 
 		if (added == 1)
 			return;
 
 		// bubble it
 		ResultEntry swapTmp;
 		for (int i = added - 1; i > 0
 				&& values[i].res.getFreq() < values[i - 1].res.getFreq(); i--) {
 			
 			swapTmp = values[i];
 			values[i] = values[i - 1];
 			values[i - 1] = swapTmp;
 		}
 
 		// remove the oldest
 		if (added > SIZE) {
 			int index = 0;
 			int max = Integer.MIN_VALUE;
 
 			for (int i = 0; i < added; i++) {
 				if (values[i].age > max) {
 					index = i;
 					max = values[i].age;
 				}
 			}
 
 			// remove index
 			for (int i = index; i < added - 1; i++) {
 				values[i] = values[i + 1];
 			}
 
 			added = SIZE;
 		}
 	}
 
 	public Result getMedian() {
 		if (added == 0)
 			return null;
 		else
 			return values[added / 2].res;
 	}
 }
