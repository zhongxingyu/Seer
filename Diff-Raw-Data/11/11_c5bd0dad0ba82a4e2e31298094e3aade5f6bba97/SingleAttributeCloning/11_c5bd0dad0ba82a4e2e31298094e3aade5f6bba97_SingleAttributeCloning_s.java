 /*******************************************************************************
  * Stefan Meyer, 2012 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  ******************************************************************************/
 package org.atemsource.atem.utility.clone;
 
 public class SingleAttributeCloning extends AttributeCloning
 {
 
 	@Override
 	public void clone(Object original, Object clone, CloningContext ctx)
 	{
 		Object value = getAttribute().getValue(original);
 		Object existingValue = getAttribute().getValue(clone);
 		if (existingValue != null && value.getClass() == existingValue.getClass())
 		{
			getEntityOperation(getAttribute().getTargetType(existingValue)).clone(value, ctx, existingValue);
 		}
 		else
 		{
 			Object clonedValue = clone(value, ctx);
 			getAttribute().setValue(clone, clonedValue);
 		}
 	}
 
 }
