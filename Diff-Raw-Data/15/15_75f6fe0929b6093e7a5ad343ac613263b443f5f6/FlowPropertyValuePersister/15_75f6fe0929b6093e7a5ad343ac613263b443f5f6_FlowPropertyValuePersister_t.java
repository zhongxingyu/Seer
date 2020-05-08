 /*
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy
  * of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed
  * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
  * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
  * the specific language governing permissions and limitations under the
  * License.
  */
 package org.amplafi.flow.flowproperty;
 
 import org.amplafi.flow.FlowPropertyDefinition;
 
 /**
  * Implementors know how to save the FlowProperty in persistent storage.
  * @see org.amplafi.flow.FlowPropertyValueProvider
  * @author patmoore
 * @param <T>
  *
  */
 public interface FlowPropertyValuePersister<T extends FlowPropertyProvider> {
     void saveChanges(T FlowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition);
 }
