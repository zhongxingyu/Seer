 // ExtDynamicElement.java
 
 /*
  * Copyright (c) 2008, Gennady & Michael Kushnir
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  * 
  * 	•	Redistributions of source code must retain the above copyright notice, this
  * 		list of conditions and the following disclaimer.
  * 	•	Redistributions in binary form must reproduce the above copyright notice,
  * 		this list of conditions and the following disclaimer in the documentation
  * 		and/or other materials provided with the distribution.
  * 	•	Neither the name of the RUJEL nor the names of its contributors may be used
  * 		to endorse or promote products derived from this software without specific 
  * 		prior written permission.
  * 		
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
  * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
  * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
  * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package net.rujel.reusables;
 
 import com.webobjects.appserver.*;
 import com.webobjects.appserver._private.WODynamicElementCreationException;
 import com.webobjects.foundation.*;
 
 public class ExtDynamicElement extends WODynamicElement {
 	protected WOElement children;
 	protected NSDictionary bindingsDict;
 
 	public ExtDynamicElement(String name, NSDictionary associations, WOElement template) {
 		super(name, associations, template);
 		bindingsDict = associations;
 		children = template;
 	}
 
 	public void appendToResponse(WOResponse aResponse, WOContext aContext) {
 		if(children != null)
 			children.appendToResponse(aResponse, aContext);
 	}
 	
 	public WOActionResults invokeAction(WORequest aRequest, WOContext aContext) {
     	if(aContext.elementID().equals(aContext.senderID()))
     		return action(aContext);
    	if(!aContext.senderID().startsWith(aContext.elementID() + '.') &&
    			!aContext.elementID().startsWith(aContext.senderID() + '.'))
     		return null;
 		if(children != null)
 			return children.invokeAction(aRequest, aContext);
 		return null;
 	}
 	
 	protected WOActionResults action(WOContext aContext) {
 		return aContext.page();
 	}
 	
 	public void takeValuesFromRequest(WORequest aRequest, WOContext aContext) {
 		if(children != null)
 			children.takeValuesFromRequest(aRequest, aContext);
 	}
 	
 	public NSArray bindingKeys() {
 		if(bindingsDict != null)
 			return bindingsDict.allKeys();
 		return null;
 	}
 
 	public boolean hasBinding(String aBindingName, WOContext context) {
 		if(bindingsDict == null)
 			return false;
 		WOAssociation association = (WOAssociation)bindingsDict.valueForKey(aBindingName);
 		return (association != null);
 	}
 	
 	public Object valueForBinding(String aBindingName, WOContext context) {
 		if(bindingsDict == null)
 			return null;
 		WOAssociation association = (WOAssociation)bindingsDict.valueForKey(aBindingName);
 		if(association == null)
 			return null;
 		return association.valueInComponent(context.component());
 	}
 	
 	public void setValueForBinding(Object aValue, String aBindingName, WOContext context) {
 		if(bindingsDict == null)
 			throw new IllegalStateException("No bindings a re set");
 		WOAssociation association = (WOAssociation)bindingsDict.valueForKey(aBindingName);
 		if(association == null)
 			throw new IllegalArgumentException("No such binding found: " + aBindingName);
 		association.setValue(aValue, context.component());
 	}
 	
 	protected void checkRequired (NSDictionary associations, String attribute) {
 		if(associations.objectForKey(attribute) == null) {
 			throw new WODynamicElementCreationException('<' + getClass().getName() + 
 					"> Missing required attribute '" + attribute + '\'');
 		}
 	}
 }
