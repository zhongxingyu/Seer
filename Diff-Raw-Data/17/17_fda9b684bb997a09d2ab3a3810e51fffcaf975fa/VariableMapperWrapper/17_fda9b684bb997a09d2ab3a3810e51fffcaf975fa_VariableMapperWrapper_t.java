 /**
  * Licensed under the Common Development and Distribution License,
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.sun.com/cddl/
  *   
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  */
 
 package com.sun.facelets.el;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.el.ELException;
 import javax.el.ValueExpression;
 import javax.el.VariableMapper;
 
 /**
  * Utility class for wrapping another VariableMapper with a new context,
  * represented by a {@link java.util.Map Map}. Modifications occur to the Map
  * instance, but resolve against the wrapped VariableMapper if the Map doesn't
  * contain the ValueExpression requested.
  * 
  * @author Jacob Hookom
 * @version $Id: VariableMapperWrapper.java,v 1.4 2007-09-21 16:31:02 youngm Exp $
  */
 public final class VariableMapperWrapper extends VariableMapper {
 
 	private final VariableMapper target;
 
 	private Map vars;
 
 	/**
 	 * 
 	 */
 	public VariableMapperWrapper(VariableMapper orig) {
 		super();
 		this.target = orig;
 	}
 
 	/**
	 * First tries to resolve against the wrapped ValueExpression and
	 * then the inner.
 	 * 
 	 * @see javax.el.VariableMapper#resolveVariable(java.lang.String)
 	 */
 	public ValueExpression resolveVariable(String variable) {
 		ValueExpression ve = null;
 		try {
			if(this.target != null)
			ve = this.target.resolveVariable(variable);
			if (ve == null && vars != null) {
				return (ValueExpression) this.vars.get(variable); 
 			}
 			return ve;
 		} catch (StackOverflowError e) {
 			throw new ELException("Could not Resolve Variable [Overflow]: " + variable, e);
 		}
 	}
 
 	/**
 	 * Set the ValueExpression on the inner Map instance.
 	 * 
 	 * @see javax.el.VariableMapper#setVariable(java.lang.String,
 	 *      javax.el.ValueExpression)
 	 */
 	public ValueExpression setVariable(String variable,
 			ValueExpression expression) {
 		if (this.vars == null) {
 			this.vars = new HashMap();
 		}
 		return (ValueExpression) this.vars.put(variable, expression);
 	}
 }
