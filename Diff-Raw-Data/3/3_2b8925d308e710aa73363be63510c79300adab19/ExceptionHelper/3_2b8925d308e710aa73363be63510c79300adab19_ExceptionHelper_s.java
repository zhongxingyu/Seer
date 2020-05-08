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
 
 import java.util.Map;
 
 import uk.ac.diamond.scisoft.analysis.rpc.flattening.IRootFlattener;
 
 public class ExceptionHelper extends MapFlatteningHelper<Exception> {
 
 	public ExceptionHelper() {
 		super(Exception.class);
 	}
 
 	@Override
 	public Exception unflatten(Map<?, ?> thisMap, IRootFlattener rootFlattener) {
 		String all = (String) thisMap.get(CONTENT);
 		int i = all.lastIndexOf(": ");
 		String msg = all.substring(i+2).trim();
 		all = all.substring(0, i);
 		i = all.lastIndexOf('\n');
 		return i < 0 ? new Exception(msg) : new Exception(msg, new Exception(all.substring(0, i)));
 	}
 
 	@Override
 	public Object flatten(Object obj, IRootFlattener rootFlattener) {
 		Exception thisException = (Exception) obj;
 		Map<String, Object> outMap = createMap(getTypeCanonicalName());
 		outMap.put(CONTENT, thisException.getLocalizedMessage());
 		return outMap;
 	}
 
 }
