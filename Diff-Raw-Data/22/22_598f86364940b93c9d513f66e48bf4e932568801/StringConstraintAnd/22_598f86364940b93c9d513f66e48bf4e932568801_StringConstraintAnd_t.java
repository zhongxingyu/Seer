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
  *  A {@link IStringConstraint} that ANDs multiple {@link IStringConstraint}s together.
  */
 public class StringConstraintAnd implements IStringConstraint {
 
 	private final IStringConstraint[] stringConstraints;
 	
 	/**
 	 * Constructor for a string constraint that ANDs multiple
 	 * {@link IStringConstraint}s together
 	 * 
 	 * @param stringConstraints the string constraints a input string must satisfy
 	 */
 	public StringConstraintAnd(IStringConstraint...stringConstraints) {
 		this.stringConstraints = stringConstraints;
 	}
 
 	/**
 	 * @return <code>true</code> if the input string satisfies all of the
 	 *         {@link IStringConstraint}s or <code>false</code> if the
 	 *         input string does not satisfy all of the
 	 *         {@link IStringConstraint}s.
 	 */
 	@Override
 	public boolean satisfiedBy(String input) {
 		for (IStringConstraint stringMatcher : stringConstraints) {
 			if (!stringMatcher.satisfiedBy(input)) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("<And>");
		for (IStringConstraint stringConstraint : stringConstraints) {
			sb.append(stringConstraint.toString());
 		}
 		sb.append("</And>");
 		return sb.toString();
 	}
 	
 }
