 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  * 
  */
 package net.rptools.parser.tree;
 
 import net.rptools.lib.datavalue.DataValue;
 import net.rptools.lib.datavalue.DataValueFactory;
 import net.rptools.parser.ScriptContext;
 
 /**
  * Script node that represents constants.
  */
 class ConstantNode implements ScriptTreeNode {
 
 
 	/** The value of the constant. */
 	private final DataValue value;
 	
 	/**
 	 * Creates a new ConstantNode representing a long value.
 	 * 
 	 * @param val The value of the constant.
 	 */
 	public ConstantNode(long val) {
 		value = DataValueFactory.longValue(val);
 	}
 	
 	/**
 	 * Creates a new ConstantNode representing a double value.
 	 * 
 	 * @param val The value of the constant.
 	 */
 	public ConstantNode(double val) {
 		value = DataValueFactory.doubleValue(val);
 	}
 	
 	/**
 	 * Creates a new ConstantNode representing a String value.
 	 * 
 	 * @param val The value of the constant.
 	 */
 	public ConstantNode(String val) {
 		assert val != null : "String constant can not be null";
 
 		value = DataValueFactory.stringValue(val);
 	}
	
 	@Override
 	public DataValue evaluate(ScriptContext context) {
 		return value;
 	}
 
 }
