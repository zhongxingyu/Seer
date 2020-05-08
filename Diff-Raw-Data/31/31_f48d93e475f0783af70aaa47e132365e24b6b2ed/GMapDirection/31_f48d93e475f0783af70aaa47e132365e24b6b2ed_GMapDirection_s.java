 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package com.icesoft.faces.component.gmap;
 
 import java.io.IOException;
 
 import javax.faces.component.UICommand;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIPanel;
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 
 import org.w3c.dom.Element;
 
 import com.icesoft.faces.context.DOMContext;
 import com.icesoft.faces.context.effects.JavascriptContext;
 import com.icesoft.faces.renderkit.dom_html_basic.HTML;
 import com.icesoft.util.CoreComponentUtils;
 
 public class GMapDirection extends UIPanel{
 	public static final String COMPONENT_TYPE = "com.icesoft.faces.GMapDirection";
     public static final String COMPONENT_FAMILY = "com.icesoft.faces.GMapDirection";
 	private Boolean locateAddress;
    private boolean initilized = false;
     private String from;
     private String to;
     private String textualDivId;
    private String textualDivClientId;
     private String locale;
     
 	
 	public GMapDirection() {
 		setRendererType(null);
 	}
 
     public String getFamily() {
         return COMPONENT_FAMILY;
     }
 
     public String getComponentType() {
         return COMPONENT_TYPE;
     }
     
     public void encodeBegin(FacesContext context) throws IOException {
     	setRendererType(null);
         super.encodeBegin(context);    	
    	String textualDivId = getTextualDivId(); 
		GMap gmap = (GMap)this.getParent();
    	String mapId = gmap.getClientId(context);
    	String query = "";
    	String from = getFrom();
    	String to = getTo();
		if(!initilized)
			initilized = true;
    	else{
			if((isLocateAddress())) {
 			JavascriptContext.addJavascriptCall(context, 
    			"Ice.GoogleMap.loadDirection('"+ mapId +"', '"+ from +"', '" + to +"');");
    	    		
			}
 		}
     }
 
 	public boolean isLocateAddress() {
         if (locateAddress != null) {
             return locateAddress.booleanValue();
         }
         ValueBinding vb = getValueBinding("locateAddress");
         return vb != null ?
                ((Boolean) vb.getValue(getFacesContext())).booleanValue() :
                false;
 	}
 
 	public void setLocateAddress(boolean locateAddress) {
 		this.locateAddress = new Boolean(locateAddress);
 	}
 
 	public String getFrom() {
 	       if (from != null) {
 	            return from;
 	        }
 	        ValueBinding vb = getValueBinding("from");
 	        return vb != null ? (String) vb.getValue(getFacesContext()) : null;
 	}
 
 	public void setFrom(String from) {
 		this.from = from;
 	}
 
 	public String getTo() {
        if (to != null) {
             return to;
         }
         ValueBinding vb = getValueBinding("to");
         return vb != null ? (String) vb.getValue(getFacesContext()) : null;
 	}
 
 	public void setTo(String to) {
 		this.to = to;
 	}
 
 	public String getTextualDivId() {
        if (textualDivId != null) {
             return textualDivId;
         }
         ValueBinding vb = getValueBinding("textualDivId");
         return vb != null ? (String) vb.getValue(getFacesContext()) : null;
 	}
 
 	public void setTextualDivId(String textualDivId) {
 		this.textualDivId = textualDivId;
 	}
 
 	public String getLocale() {
 	       if (locale != null) {
 	            return locale;
 	        }
 	        ValueBinding vb = getValueBinding("locale");
 	        return vb != null ? (String) vb.getValue(getFacesContext()) : null;
 	}
 
 	public void setLocale(String locale) {
 		this.locale = locale;
 	}
 	
 	public String getClientIdOfTextualDiv(FacesContext context) {
 		String forString = getTextualDivId();
 		UIComponent forComponent = CoreComponentUtils.findComponent(forString, this);
 		String forClientId = forComponent.getClientId(context);
 		return (forClientId.indexOf(':') > 1)? forClientId : null;
 	}
 
     private transient Object values[];
     public void restoreState(FacesContext context, Object state) {
         values =  (Object[])state;
         super.restoreState(context, values[0]);
         to = (String)values[1];
         from = (String)values[2];
         textualDivId = (String)values[3];
         locale = (String)values[4];
         locateAddress = (Boolean)values[5];
        textualDivClientId = (String)values[6];
        initilized = ((Boolean) values[7]).booleanValue();
     }
 
     public Object saveState(FacesContext context) {
         if(values == null){
             values = new Object[8];
         }
         values[0] = super.saveState(context);
         values[1] = to;
         values[2] = from;
         values[3] = textualDivId;
         values[4] = locale;
         values[5] = locateAddress;  
        values[6] = textualDivClientId;
        values[7] = Boolean.valueOf(initilized);
         return values;
     }
 
 
 }
