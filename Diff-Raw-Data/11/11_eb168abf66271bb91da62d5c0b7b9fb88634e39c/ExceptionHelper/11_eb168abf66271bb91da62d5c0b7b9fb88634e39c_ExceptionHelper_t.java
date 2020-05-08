/*-
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
 
 import uk.ac.diamond.scisoft.analysis.rpc.AnalysisRpcRemoteException;
 import uk.ac.diamond.scisoft.analysis.rpc.flattening.IRootFlattener;
 
 public class ExceptionHelper extends MapFlatteningHelper<Exception> {
 	public static final String EXECTYPESTR = "exctypestr";
 	public static final String EXECVALUESTR = "excvaluestr";
 	public static final String TRACEBACK = "traceback";
 	public static final String PYTHONTEXTS = "pythontexts";
 
 	public ExceptionHelper() {
 		super(Exception.class);
 	}
 
 	@Override
 	public AnalysisRpcRemoteException unflatten(Map<?, ?> thisMap, IRootFlattener rootFlattener) {
 		String exctypestr = (String) rootFlattener.unflatten(thisMap
 				.get(EXECTYPESTR));
 		String excvaluestr = (String) rootFlattener.unflatten(thisMap
 				.get(EXECVALUESTR));
 		String message = exctypestr;
 		if (excvaluestr != null) {
 			message += ": " + excvaluestr;
 		}
 		final StackTraceElement[] stackTrace;
 		if (!thisMap.containsKey(TRACEBACK)) {
 			StackTraceElement ste = new StackTraceElement(
 					"<ExceptionInOtherEndDuringAnalysisRpcCall>", "<unknown>",
 					"<unknown>", -1);
 			stackTrace = new StackTraceElement[] { ste };
 		} else {
 			stackTrace = (StackTraceElement[]) rootFlattener.unflatten(thisMap
 					.get(TRACEBACK));
 		}
 		if (thisMap.containsKey(PYTHONTEXTS)) {
 			final AnalysisRpcRemoteException e = new AnalysisRpcRemoteException(message);
 			e.setStackTrace(stackTrace);
 			Object unflatten = rootFlattener.unflatten(thisMap.get(PYTHONTEXTS));
 			String[] texts = (String[]) unflatten;
 			e.setStackTraceTexts(texts);
 			return e;
 		} else {
 			final AnalysisRpcRemoteException e = new AnalysisRpcRemoteException(message);
 			e.setStackTrace(stackTrace);
 			return e;
 		}
 	}
 
 	@Override
 	public Object flatten(Object obj, IRootFlattener rootFlattener) {
 		Exception thisException = (Exception) obj;
 		Map<String, Object> outMap = createMap(getTypeCanonicalName());
 		outMap.put(EXECTYPESTR, rootFlattener.flatten(thisException.getClass()
 				.getCanonicalName()));
 		outMap.put(EXECVALUESTR,
 				rootFlattener.flatten(thisException.getLocalizedMessage()));
 		outMap.put(TRACEBACK,
 				rootFlattener.flatten(thisException.getStackTrace()));
 		if (thisException instanceof AnalysisRpcRemoteException) {
 			AnalysisRpcRemoteException excWithTexts = (AnalysisRpcRemoteException) thisException;
 			outMap.put(PYTHONTEXTS,
 					rootFlattener.flatten(excWithTexts.getStackTraceTexts()));
 		}
 		return outMap;
 	}
 
 }
