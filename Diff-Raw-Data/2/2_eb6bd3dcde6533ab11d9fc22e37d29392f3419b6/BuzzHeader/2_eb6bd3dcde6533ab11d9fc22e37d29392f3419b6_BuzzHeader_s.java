 /** ==== BEGIN LICENSE =====
    Copyright 2012 - BeeQueue.org
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  
  *  ===== END LICENSE ====== */
 package org.beequeue.json;
 
 import org.beequeue.piles.MapList;
 import org.beequeue.util.BeeException;
 import org.beequeue.util.BeeOperation;
 import org.beequeue.util.TypeFactory;
 
 public class BuzzHeader  {
 
 	public final class BuzzColumns extends MapList<String, BuzzAttribute> {
 		private static final long serialVersionUID = 1L;
		private BuzzColumns() {
 			super(BuzzAttribute.op_GET_NAME);
 		}
 		@Override
 		public void refresh() {
 			stringRepresentation = null;
 			super.refresh();
 		}
 	}
 
 	public BuzzColumns columns = new BuzzColumns();
 	public static TypeFactory<BuzzHeader> TF = new TypeFactory<BuzzHeader>(BuzzHeader.class) ;
 	
 	
 
 	public int colIndex(String name) {
 		Integer idx = columns.indexMap().get(name);
 		BeeException.throwIfTrue(idx==null, "idx==null when name:"+name );
 		return idx;
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if(this == o) return true;
 		if (o instanceof BuzzHeader) {
 			BuzzHeader that = (BuzzHeader) o;
 			return this.toString().equals(that.toString());
 		}
 		return false;
 	}
 
 	@Override
 	public int hashCode() {
 		return this.toString().hashCode();
 	}
 
 	private volatile String stringRepresentation = null;
 	@Override
 	public String toString() {
 		String s = this.stringRepresentation;
 		if(s==null){
 			s = TF.op_OBJ_TO_COMPACT.execute(this);
 			this.stringRepresentation = s;
 		}
 		return s;
 	}
 
 	public void copy(BuzzHeader execute) {
 		this.columns.addAll(execute.columns);
 		
 	}
 
 	
 	
 
 }
