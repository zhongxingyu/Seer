 /**
  * Warlock, the open-source cross-platform game client
  *  
  * Copyright 2008, Warlock LLC, and individual contributors as indicated
  * by the @authors tag. 
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package cc.warlock.core.client.settings.internal;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 
 import cc.warlock.core.client.settings.IVariable;
 import cc.warlock.core.client.settings.IVariableProvider;
 
 public class VariableConfigurationProvider extends ClientConfigurationProvider implements IVariableProvider {
 
 	protected HashMap<String, IVariable> variables = new HashMap<String, IVariable>();
 	
 	public VariableConfigurationProvider ()
 	{
 		super("variables");
 		
 		setHandleChildren(false);
 	}
 	
 	public void addVariable(IVariable variable) {
 		variables.put(variable.getIdentifier(), variable);
 	}
	
	public void setVariable(String id, IVariable variable) {
		variables.put(id, variable);
	}
 
 	public IVariable getVariable(String identifier) {
 		if (variables.containsKey(identifier)) {
 			return variables.get(identifier);
 		}
 		return null;
 	}
 
 	public Collection<? extends IVariable> getVariables() {
 		return variables.values();
 	}
 
 	public void removeVariable(IVariable variable) {
 		if (variables.containsKey(variable.getIdentifier()))
 		{
 			variables.remove(variable.getIdentifier());
 		}
 	}
 	
 	@Override
 	protected void parseData() {}
 
 	@Override
 	protected void parseChild(Element child) {
 		if (child.getName().equals("variable"))
 		{
 			String name = child.attributeValue("id");
 			String value = child.getStringValue();
 			
 			Variable variable = new Variable(this, name, value);
 			variables.put(name, variable);
 		}
 	}
 	
 	@Override
 	protected void saveTo(List<Element> elements) {
		Element varsElement = DocumentHelper.createElement("variables");
		
 		for (Map.Entry<String, IVariable> entry : variables.entrySet())
 		{
			Element element = varsElement.addElement("variable");
 			element.addAttribute("id", entry.getKey());
 			element.addText(entry.getValue().getValue());
 			
 		}
		elements.add(varsElement);
 	}
 }
