 /*******************************************************************************
  * Stefan Meyer, 2012
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
  ******************************************************************************/
 package org.atemsource.atem.utility.compare.builder;
 
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.atemsource.atem.api.attribute.CollectionAttribute;
 import org.atemsource.atem.api.type.Type;
 import org.atemsource.atem.utility.compare.AttributeComparison;
 import org.atemsource.atem.utility.compare.CompareContext;
 import org.atemsource.atem.utility.compare.Difference;
 
 
 public class CollectionAttributeComparison extends AttributeComparison
 {
 
 	@Override
 	public Set<Difference> getDifferences(CompareContext context, Object a, Object b)
 	{
 		Set<Difference> differences = new HashSet<Difference>();
 		CompareContext childContext = context.createChild(getAttribute());
 		Collection<Object> additions = new ArrayList<Object>();
 		Collection<Object> removals = new ArrayList<Object>();
 		CollectionAttribute associationAttribute = ((CollectionAttribute) getAttribute());
 		Collection collectionA = associationAttribute.getElements(a);
 		Collection collectionB = associationAttribute.getElements(b);
 
 		Iterator ia = associationAttribute.getIterator(a);
 		Iterator ib = associationAttribute.getIterator(b);
 		removals.addAll(collectionA);
 		additions.addAll(collectionB);
 		for (; ia.hasNext();)
 		{
 			Object valueA = ia.next();
 			for (; ib.hasNext();)
 			{
 				Object valueB = ib.next();
				Type targetType = getTargetType(associationAttribute, valueA);
				Type compareType = getTargetType(associationAttribute, valueB);
 				if (targetType.equals(compareType) && getEntityOperation(compareType) != null)
 				{
 					Set<Difference> localDifferences = getEntityOperation(compareType).getDifferences(context, valueA, valueB);
 					if (localDifferences.size() == 0)
 					{
 						additions.remove(valueB);
 						removals.remove(valueA);
 						break;
 					}
 				}
 				else if (targetType.isEqual(valueA, valueB))
 				{
 					additions.remove(valueB);
 					removals.remove(valueA);
 					break;
 				}
 
 			}
 		}
 		for (Object value : removals)
 		{
 			differences.add(childContext.addRemoval(value));
 		}
 		for (Object value : additions)
 		{
 			differences.add(childContext.addAddition(value));
 		}
 		return differences;
 	}
	
	private Type getTargetType(CollectionAttribute associationAttribute, Object value) {
		Type targetType = associationAttribute.getTargetType(value);
		if (targetType == null)
			targetType = associationAttribute.getTargetType();
		return targetType;
	}
 }
