 /**   
  * Copyright 2011 The Buzz Media, LLC
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
 package com.thebuzzmedia.simple.generator;
 
 /**
  * Class used to implement the default generator stubs for the JSON file format.
  * <p/>
  * Most all of the complex reflection and generation logic is implemented by
 * {@link AbstractGenerator} and subclasses only need to add simple stub logic
 * used to generate the correct format according to the specification being
 * followed.
  * 
  * @author Riyad Kalla (software@thebuzzmedia.com)
  * @since 1.1
  */
 public class JSONGenerator extends AbstractGenerator {
 	public JSONGenerator() {
 		super();
 	}
 
 	public JSONGenerator(IIndenter indenter) throws IllegalArgumentException {
 		super(indenter);
 	}
 
 	public JSONGenerator(IIndenter indenter, int initialBufferSize)
 			throws IllegalArgumentException {
 		super(indenter, initialBufferSize);
 	}
 
 	@Override
 	protected void writeObjectOpen(String fieldName, int level, boolean inList) {
 		if (!inList && level > 1)
 			append('"').append(fieldName).append("\": ");
 
 		append('{');
 	}
 
 	@Override
 	protected void writeObjectClose(String fieldName, int level, boolean inList) {
 		append('}');
 	}
 
 	@Override
 	protected void writeListOpen(String fieldName, int level, boolean inList) {
 		append('"').append(fieldName).append("\": [");
 	}
 
 	@Override
 	protected void writeListClose(String fieldName, int level, boolean inList) {
 		append(']');
 	}
 
 	@Override
 	protected void writeListSeparator() {
 		append(',');
 	}
 
 	@Override
 	protected void writeBoolean(String fieldName, Boolean value, int level,
 			boolean inList) {
 		if (!inList)
 			append('"').append(fieldName).append("\": ");
 
 		append(value);
 	}
 
 	@Override
 	protected void writeNumber(String fieldName, Number value, int level,
 			boolean inList) {
 		if (!inList)
 			append('"').append(fieldName).append("\": ");
 
 		append(value);
 	}
 
 	@Override
 	protected void writeString(String fieldName, String value, int level,
 			boolean inList) {
 		if (!inList)
 			append('"').append(fieldName).append("\": ");
 
 		append('"').append(value).append('"');
 	}
 }
