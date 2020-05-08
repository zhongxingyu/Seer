 /*
  * Copyright 2011 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.rpc.flattening.helpers;
 
 import java.util.ArrayList;
 import java.util.Map;
 
import uk.ac.diamond.scisoft.analysis.roi.IROI;
 import uk.ac.diamond.scisoft.analysis.rpc.flattening.IRootFlattener;
 
abstract public class ROIListHelper<ListType extends ArrayList<ItemType>, ItemType extends IROI> extends
 		MapFlatteningHelper<ListType> {
 
 	public ROIListHelper(Class<ListType> type) {
 		super(type);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Object flatten(Object obj, IRootFlattener rootFlattener) {
 		ListType roiList = (ListType) obj;
 		Map<String, Object> returnMap = createMap(getTypeCanonicalName());
 		Object[] flattenedContents = new Object[roiList.size()];
 		int i = 0;
 		for (Object element : roiList) {
 			ItemType thisROI = (ItemType) element;
 			flattenedContents[i++] = rootFlattener.flatten(thisROI);
 		}
 		returnMap.put(CONTENT, flattenedContents);
 		return returnMap;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public ListType unflatten(Map<?, ?> inMap, IRootFlattener rootFlattener) {
 		ListType outList = newList();
 
 		Object[] flattenedContent = (Object[]) inMap.get(CONTENT);
 		for (Object element : flattenedContent) {
 			ItemType e = (ItemType) (rootFlattener.unflatten(element));
 			outList.add(e);
 		}
 		return outList;
 	}
 
 	public abstract ListType newList();
 }
