 /**
  * Copyright 2013 Simon Curd <simoncurd@gmail.com>
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.hula.lang.commands.data;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.hula.lang.commands.AbstractCommand;
 import com.hula.lang.model.RequiresReturnParam;
 import com.hula.lang.runtime.RuntimeConnector;
 
 /**
  * The NewMap command creates a new Map, optionally initialised
  * with some values. <br/><br/>
  * 
  * Example Usage: <br/><br/> 
  * 
  *  Creates an empty map <br/>
  * <code>NewMap as map1</code>
  * <br/><br/>
  * 
  * Create an map where name="Jeff"<br/>
  * <code>NewMap name="Jeff" as map2</code>
  * <br/><br/>
  * 
  * Create an nested map<br/>
  * <pre>
  * NewMap name="Jeff" as person1
  * NewMap person=$person1, postcode="SW19 4PU" as address
  * </pre>
  * 
  */
 @RequiresReturnParam
 public class NewMap extends AbstractCommand
 {
 
 	@Override
 	public void execute(RuntimeConnector connector)
 	{
 		//
 		// transfer properties
 		//
 
 		Map map = new HashMap();
 
 		for (String key : this.getSignatureParameters().keySet())
 		{
 			if (key.equals("default"))
 			{
 				continue;
 			}
			String value = getVariableValueAsString(key, connector);
 			map.put(key, value);
 		}
 
 		connector.setVariable(getReturnParameter(), map);
 	}
 
 
 }
