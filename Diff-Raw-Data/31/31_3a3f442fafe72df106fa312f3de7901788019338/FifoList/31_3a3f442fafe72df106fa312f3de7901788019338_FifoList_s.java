 /*Copyright [2010-2011] [David Van de Ven]
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 
  */
 package org.wahtod.wififixer.utility;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 public class FifoList extends ArrayList<Object> {
 	/*
 	 * Behaves as fixed-size FIFO
 	 */
 	private static final long serialVersionUID = -9019587832538873253L;
 	private int length;
 
 	public FifoList(int s) {
 		length = s;
 	}
 
 	@Override
 	public boolean add(Object object) {
 		if (this.size() < length)
 			this.add(0, object);
 		else {
 			this.remove(this.size() - 1);
 			this.add(0, object);
 		}
 		return true;
 	}
 
	@Override
	public boolean containsAll(Collection<?> collection) {
 		if (collection.size() > this.size())
 			return false;
 		else {
 			int tocheck = this.size() - collection.size();
 			int current = 0;
 			while (current < tocheck) {
				if (this.subList(current, collection.size() + current).equals(
						collection))
 					return true;
				current++;
 			}
 			return false;
 		}
 
 	}
 }
