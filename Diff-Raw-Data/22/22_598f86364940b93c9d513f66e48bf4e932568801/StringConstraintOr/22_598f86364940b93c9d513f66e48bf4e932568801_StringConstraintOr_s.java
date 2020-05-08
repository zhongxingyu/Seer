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
  * Implementation of {@link IStringConstraint} that ORs multiple
  * {@link IStringConstraint} together.
  */
 public class StringConstraintOr implements IStringConstraint {
 
 	private final IStringConstraint[] stringConstraints;
 
 	/**
 	 * Constructor for a string constraint that ORs multiple
 	 * {@link IStringConstraint}s together
 	 * 
 	 * @param stringConstraints
 	 *            the string constraints of which a input must satisfy at least
 	 *            one
 	 */
 	public StringConstraintOr(IStringConstraint...stringConstraints) {
 		this.stringConstraints = stringConstraints;
 	}
 	
 	/**
 	 * @return <code>true</code> if the input string satisfies any of the
 	 *         {@link IStringConstraint}s and <code>false</code> if the input
 	 *         string does not satisfy any of the {@link IStringConstraint}s.
 	 */
 	@Override
 	public boolean satisfiedBy(String input) {
 		for (IStringConstraint stringConstraint : stringConstraints) {
 			if (stringConstraint.satisfiedBy(input)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("<Or>");
		for (IStringConstraint sc : stringConstraints) {
			sb.append(sb.toString());
 		}
 		sb.append("</Or>");
 		return sb.toString();
 	}
 	
 }
