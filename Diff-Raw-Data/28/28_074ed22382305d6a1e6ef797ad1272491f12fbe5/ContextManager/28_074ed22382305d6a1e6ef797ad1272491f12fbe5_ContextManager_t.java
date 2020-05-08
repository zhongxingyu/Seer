 /*
  * Copyright 2010 Softgress - http://www.softgress.com/
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package sim.instrumentation.data;
 
 import java.util.ArrayDeque;
 import java.util.Deque;
 
 import sim.data.Context;
 
 /**
  * Global thread safe storage for keeping track of execution flow context data.
  * 
  * @see sim.instrumentation.annotation.CreateContext
  * @see sim.instrumentation.aop.aspectj.AbstractContextCreator
  * 
  * @author mcq
  * 
  */
 public final class ContextManager {
 	private static final ThreadLocal<Deque<Context>> storage = new ThreadLocal<Deque<Context>>();
 
 	public static Context createNewContext(String name, String tag) {
 		Deque<Context> s = storage.get();
 		if (s == null) {
 			s = new ArrayDeque<Context>();
 			storage.set(s);
 		}
		Context parent = getCurrentContext();
		String parentId = null;
		if (parent != null)
			parentId = parent.getId();
		Context c = Context.create(name, tag, parentId);
 		s.addFirst(c);
 		return c;
 	}
 
 	public static Context destroyCurrentContext() {
 		Deque<Context> s = storage.get();
 		if (s == null) {
 			return null;
 		} else {
 			Context c = s.pollFirst();
 			if (c != null) {
 				c.endContext();
 				Collector.addMeasurement(c);
 			}
 			return c;
 		}
 	}
 
 	public static Context getCurrentContext() {
 		Deque<Context> s = storage.get();
 		if (s == null) {
 			return null;
 		} else {
 			return s.peekFirst();
 		}
 	}
 
	public static void setCurrentContext(Context c) {
		if (c == null)
			return;
		Deque<Context> s = storage.get();
		if (s == null) {
			s = new ArrayDeque<Context>();
			storage.set(s);
		}
		s.addFirst(c);
 	}
 
 	private ContextManager() {
 		super();
 	}
 }
