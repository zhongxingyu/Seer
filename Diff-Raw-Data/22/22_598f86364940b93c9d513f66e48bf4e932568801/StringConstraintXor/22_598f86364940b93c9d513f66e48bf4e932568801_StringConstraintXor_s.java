 /*******************************************************************************
  * Copyright 2011 Lance Feagan
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
 package net.vectorcomputing.base.string.constraint;
 
 /**
  * Implementation of {@link IStringConstraint} that XORs multiple
  * {@link IStringConstraint}s together.
  */
 public class StringConstraintXor implements IStringConstraint {
 	
 	private final IStringConstraint[] stringConstraints;
 
 	/**
 	 * Constructor for a string constraint that XORs multiple
 	 * {@link IStringConstraint}s together
 	 * 
 	 * @param stringConstraints
 	 *            the string constraints of which a input must satisfy one and
 	 *            only one
 	 */
 	public StringConstraintXor(IStringConstraint...stringConstraints) {
 		this.stringConstraints = stringConstraints;
 	}
 
 	/**
 	 * @return <code>true</code> if the input string satisfies exactly one of
 	 *         the string constraints and <code>false</code> if the input string
 	 *         satisfies zero or more than one of the string constraints
 	 */
 	@Override
 	public boolean satisfiedBy(String input) {
 		int satisfiedByCount = 0;
 		for (IStringConstraint constraint : stringConstraints) {
 			if (constraint.satisfiedBy(input)) {
 				++satisfiedByCount;
 				// Early termination if satisfiedByCount exceeds 1 
 				if (satisfiedByCount > 1) {
 					return false;
 				}
 			}
 		}
 		return satisfiedByCount == 1;
 	}
 	
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("<Xor>");
		for (IStringConstraint sc : stringConstraints) {
			sb.append(sb.toString());
 		}
 		sb.append("</Xor>");
 		return sb.toString();
 	}
 	
 }
