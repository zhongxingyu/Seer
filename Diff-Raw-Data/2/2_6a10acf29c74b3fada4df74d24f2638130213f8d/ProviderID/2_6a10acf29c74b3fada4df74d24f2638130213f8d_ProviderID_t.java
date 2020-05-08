 /**
  *
  * Copyright 2013 the original author or authors.
  * Copyright 2013 Sorcersoft.com S.A.
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
 
 package sorcer.jini.lookup;
 
 import net.jini.core.lookup.ServiceID;
 
 
 public class ProviderID implements Comparable<Object> {
 	private ServiceID serviceID;
 
 	
 	public ProviderID(ServiceID id) {
 		serviceID = id;
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Comparable#compareTo(java.lang.Object)
 	 */
 	@Override
 	public int compareTo(Object arg) {
 		if (arg instanceof ProviderID) {
 			long l1 = serviceID.getMostSignificantBits();
			long l2 = ((ProviderID) arg).serviceID.getMostSignificantBits();
 			if ((l1 - l2) == 0) {
 				return 0;
 			} else if ((l1 - l2) > 0) {
 				return 1;
 			} else {
 				return -1;
 			}
 		}
 		throw new RuntimeException("Wrong argument to compare: " + arg);
 	}
 
 }
