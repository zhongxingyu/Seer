 /*
  * $Id:$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 package org.mule.module.mappack;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.mule.api.MuleMessage;
 import org.mule.api.expression.ExpressionManager;
 import org.mule.api.lifecycle.InitialisationException;
 import org.mule.api.transformer.TransformerException;
 import org.mule.module.mappack.elements.Maporder;
 import org.mule.module.mappack.elements.Mapvalue;
 import org.mule.module.mappack.i18n.MapPackMessages;
 import org.mule.transformer.AbstractMessageTransformer;
 import org.mule.util.TemplateParser;
 
 public class NestedMapTransformer extends AbstractMessageTransformer
 {
     private List<Mapvalue> mapvalues;
     private List<Maporder> maporders;
     private Boolean trimToLength = false;
     private Boolean addSpace = false;
     private Boolean singleMap = false;
     private Boolean toList = false;
     private Boolean toObjectList = false;
     private Boolean skipTop = false;
 
     protected final TemplateParser.PatternInfo patternInfo = TemplateParser.createMuleStyleParser().getStyle();
 
     protected ExpressionManager expressionManager;
 
     @Override
     public void initialise() throws InitialisationException
     {
         super.initialise();
 
         expressionManager = muleContext.getExpressionManager();
     }
 
     @Override
     public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException
     {
     	
         if (mapvalues == null || mapvalues.size() == 0)
         {
             throw new TransformerException(MapPackMessages.noMapValueNameDefined());
         }
 
     	// create a new tracking map everytime
         HashMap<String, HashMap> tempmap = new HashMap<String, HashMap>();
         HashMap<String, HashMap> outmap;
         
         for (int i = 0; i < mapvalues.size(); i++)
         {
             Mapvalue mapvalue = mapvalues.get(i);
             Object value = mapvalue.evaluateMapValue(message,  muleContext, expressionManager, patternInfo);
             String mapkey = mapvalue.getMapKey();
             String mapname;
             
             if (mapvalue.getLength() != null && trimToLength && !mapvalue.getNotString())
             {
             	String svalue = (String) value;
                 int length = Integer.parseInt(mapvalue.getLength());
                 int vlen = svalue.length();
                 if (vlen < length)
                 	length = vlen;
                 value = svalue.substring(0, length);
             }
             
             // check to see if a space is needed
             if (addSpace && !mapvalue.getNotString())
             {
             	String svalue = (String) value;
             	if (svalue.length() == 0) {
             		value = " ";
             	}
             }
             
             mapname = (singleMap) ? null : mapvalue.getMapName();
             
             putValue(mapname, mapkey, value, tempmap);
 
         }
         
         if (maporders != null && !singleMap) {
         	outmap = new HashMap<String, HashMap>();
         	for (int i = 0; i < maporders.size(); i++) {
         		Maporder maporder = maporders.get(i);
         		constructNestedMap(maporder.getParentMap(), maporder.getChildMap(), tempmap, outmap);
         	}
         }
         else {
         	if (singleMap) {
         		outmap = tempmap.remove(null);
         	}
         	else {
         		// default map
         		outmap = tempmap;        		
         	}
         }
         
         if (!skipTop) {
         	// Move over the ones initially mapped to null
         	HashMap<String, HashMap> nullmap = tempmap.get(null);
         	if (nullmap != null) {
         		outmap.putAll(nullmap);
         	}
         }
         
         // Note: return is in an if-else block
         if (toList && !toObjectList) {
         	ArrayList<Map<String,Object>> outlist = new ArrayList<Map<String,Object>>();
         	outlist.add((Map) outmap);
         	return outlist;
         } else if (toObjectList)
         {
         	ArrayList<Object> outlist = new ArrayList<Object>();
         	outlist.add((Object) outmap);        	
         	return outlist;
         } else 
         {
         	return outmap;
         }
     }
 
 
     public List<Maporder> getMaporders()
     {
         return maporders;
     }
 
     public void setMaporders(List<Maporder> maporders)
     {
         this.maporders = maporders;
     }
 
     public List<Mapvalue> getMapvalues()
     {
         return mapvalues;
     }
 
     public void setMapvalues(List<Mapvalue> mapvalues)
     {
         this.mapvalues = mapvalues;
     }
 
     public Boolean getTrimToLength()
     {
         return trimToLength;
     }
 
     public void setTrimToLength(Boolean trimToLength)
     {
         this.trimToLength = trimToLength;
     }
     
     public Boolean getAddSpace()
     {
     	return addSpace;
     }
     
     public void setAddSpace(Boolean addSpace)
     {
     	this.addSpace = addSpace;
     }
     
     public Boolean getSingleMap()
     {
     	return singleMap;
     }
     
     public void setSingleMap(Boolean singleMap)
     {
     	this.singleMap = singleMap;
     }
     
     public Boolean getToList()
     {
     	return toList;
     }
     
     public void setToList(Boolean toList)
     {
     	this.toList = toList;
     }
     
     public Boolean getToObjectList()
     {
     	return toObjectList;
     }
     
     public void setToObjectList(Boolean toObjectList)
     {
     	this.toObjectList = toObjectList;
     }
     
     public Boolean getSkipTop()
     {
     	return skipTop;
     }
     
     public void setSkipTop(Boolean skipTop)
     {
     	this.skipTop = skipTop;
     }
     
 	@SuppressWarnings(value = "unchecked")
 	private void constructNestedMap(String parentName, String childName, HashMap<String, HashMap> tempmap, HashMap<String, HashMap> outmap)
 			throws TransformerException {
 		
 		HashMap<String, HashMap> childmap = (HashMap<String, HashMap>) tempmap
 				.get(childName);
 		
 		if (childmap == null)
 			// Maybe created during the ordering part?
 			childmap = (HashMap<String, HashMap>) outmap.get(childName);
 		
 		if (childmap == null)
 			// Still not found
			throw new TransformerException(MapPackMessages.noChildDefined());
 		
 		if (parentName != null) {
 
 			// Handling parent map
 			HashMap<String, HashMap> parentmap = (HashMap<String, HashMap>) outmap
 					.remove(parentName); // Remove since a map can be successively nested
 			if (parentmap == null)
 				parentmap = (HashMap<String, HashMap>) tempmap.get(parentName); // Maybe not yet touched
 			if (parentmap == null)
 				parentmap = new HashMap<String, HashMap>();
 			// put map
 			parentmap.put(childName, childmap);
 			outmap.remove(childName); // to prevent dupe in the output
 			outmap.put(parentName, parentmap);
 		}
 		else {
 			outmap.put(childName, childmap);
 		}
 	}
     
     @SuppressWarnings(value="unchecked")
     private void putValue(String mapName, String mapKey, Object value, HashMap<String, HashMap> tempmap) {
     	HashMap<String, Object> map = (HashMap<String, Object>) tempmap.get(mapName);
     	if (map == null) {
     		map = new HashMap<String, Object>();
     	}
     	map.put(mapKey, value);
     	tempmap.put(mapName, map);
     }
 }
