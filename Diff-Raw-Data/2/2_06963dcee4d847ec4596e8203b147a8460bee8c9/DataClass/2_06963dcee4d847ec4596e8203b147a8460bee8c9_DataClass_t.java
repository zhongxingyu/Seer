 /*
  * SmartGWT (GWT for SmartClient)
  * Copyright 2008 and beyond, Isomorphic Software, Inc.
  *
  * SmartGWT is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License version 3
  * as published by the Free Software Foundation.  SmartGWT is also
  * available under typical commercial license terms - see
  * http://smartclient.com/license
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  */
 
 package org.vaadin.smartgwt.server.core;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.vaadin.smartgwt.server.BaseWidget;
 import org.vaadin.smartgwt.server.data.Record;
 import org.vaadin.smartgwt.server.types.ValueEnum;
 import org.vaadin.smartgwt.server.util.JSONHelper;
 
 import com.vaadin.terminal.PaintException;
 import com.vaadin.terminal.PaintTarget;
 import com.vaadin.terminal.Paintable;
 import com.vaadin.terminal.gwt.server.JsonPaintTarget;
 import com.vaadin.ui.AbstractComponent;
 
 public class DataClass extends AbstractComponent {
 	private final Map<String, Object> attributes = new HashMap<String, Object>();
 
 	public DataClass() {
 
 	}
 
 	public void setAttribute(String property, String value) {
 		attributes.put(property, value);
 	}
 
 	public String getAttribute(String property) {
 		return (String) attributes.get(property);
 	}
 
 	public String getAttributeAsString(String property) {
 		return (String) attributes.get(property);
 	}
 
 	public void setAttribute(String property, int value) {
 		attributes.put(property, value);
 	}
 
 	public void setAttribute(String property, double value) {
 		attributes.put(property, value);
 	}
 
 	public void setAttribute(String property, long value) {
		attributes.put(property, new Double(value));
 	}
 
 	public Integer getAttributeAsInt(String property) {
 		return (Integer) attributes.get(property);
 	}
 
 	public void setAttribute(String property, boolean value) {
 		attributes.put(property, value);
 	}
 
 	public Boolean getAttributeAsBoolean(String property) {
 		return (Boolean) attributes.get(property);
 	}
 
 	public Double getAttributeAsDouble(String property) {
 		return (Double) attributes.get(property);
 	}
 
 	public Long getAttributeAsLong(String property) {
 		Double dVal = this.getAttributeAsDouble(property);
 		return dVal == null ? null : dVal.longValue();
 	}
 
 	public double[] getAttributeAsDoubleArray(String property) {
 		return (double[]) attributes.get(property);
 	}
 
 	public void setAttribute(String property, int[] value) {
 		attributes.put(property, value);
 	}
 
 	public void setAttribute(String property, Integer[] value) {
 		attributes.put(property, value);
 	}
 
 	public void setAttribute(String property, DataClass[] value) {
 		attributes.put(property, value);
 	}
 
 	public void setAttribute(String property, BaseClass[] value) {
 		attributes.put(property, value);
 	}
 
 	public void setAttribute(String property, BaseWidget[] value) {
 		attributes.put(property, value);
 	}
 
 	public int[] getAttributeAsIntArray(String property) {
 		return (int[]) attributes.get(property);
 	}
 
 	public void setAttribute(String property, String[] value) {
 		attributes.put(property, value);
 	}
 
 	public String[] getAttributeAsStringArray(String property) {
 		return (String[]) attributes.get(property);
 	}
 
 	public void setAttribute(String property, DataClass value) {
 		attributes.put(property, value);
 	}
 
 	public void setAttribute(String property, BaseClass value) {
 		attributes.put(property, value);
 	}
 
 	public void setAttribute(String property, Date value) {
 		attributes.put(property, value);
 	}
 
 	public void setAttribute(String property, double[] value) {
 		attributes.put(property, value);
 	}
 
 	public void setAttribute(String property, Boolean value) {
 		attributes.put(property, value);
 	}
 
 	public void setAttribute(String property, Map value) {
 		attributes.put(property, value);
 	}
 
 	public void setAttribute(String property, ValueEnum[] value) {
 		attributes.put(property, value);
 	}
 
 	public void setAttribute(String property, ValueEnum value) {
 		attributes.put(property, value);
 	}
 
 	/**
 	 * Set the attribute value as and Object. Note that this method converts the Java primitive Object types, Dates and Maps to the underyling
 	 * JavaScriptObject value. All other object types are set as Object type attributes and users are expected to call {@link #getAttributeAsObject(String)}
 	 * in order to retrieve them.
 	 *
 	 * @param property the attribute name
 	 * @param value the attribute value.
 	 */
 	public void setAttribute(String property, Object value) {
 		if (value instanceof String || value == null) {
 			setAttribute(property, (String) value);
 		} else if (value instanceof Integer) {
 			setAttribute(property, ((Integer) value).intValue());
 		} else if (value instanceof Float) {
 			setAttribute(property, ((Float) value).floatValue());
 		} else if (value instanceof Double) {
 			setAttribute(property, ((Double) value).doubleValue());
 		} else if (value instanceof Long) {
 			setAttribute(property, ((Long) value).longValue());
 		} else if (value instanceof Boolean) {
 			setAttribute(property, ((Boolean) value).booleanValue());
 		} else if (value instanceof Date) {
 			setAttribute(property, (Date) value);
 		} else if (value instanceof Map) {
 			setAttribute(property, (Map) value);
 		} else {
 			attributes.put(property, value);
 		}
 	}
 
 	public void setAttribute(String property, Double value) {
 		attributes.put(property, value);
 	}
 
 	public void setAttribute(String property, Integer value) {
 		attributes.put(property, value);
 	}
 
 	public void setAttribute(String property, Float value) {
 		attributes.put(property, value);
 	}
 
 	public Float getAttributeAsFloat(String property) {
 		return (Float) attributes.get(property);
 	}
 
 	public Date getAttributeAsDate(String property) {
 		return (Date) attributes.get(property);
 	}
 
 	public Object getAttributeAsObject(String property) {
 		return attributes.get(property);
 	}
 
 	public Map getAttributeAsMap(String property) {
 		return (Map) attributes.get(property);
 	}
 
 	/**
 	 * Get the attribute value as a Record.
 	 *
 	 * @param property the property name
 	 * @return the record value
 	 */
 	public Record getAttributeAsRecord(String property) {
 		return (Record) attributes.get(property);
 	}
 
 	public String[] getAttributes() {
 		return attributes.keySet().toArray(new String[0]);
 	}
 
 	@Override
 	public void paintContent(PaintTarget target) throws PaintException {
 		JsonPaintTarget jspt = (JsonPaintTarget) target;
 
 		for (Map.Entry<String, Object> entry : attributes.entrySet()) {
 			Object value = entry.getValue();
 			String name = entry.getKey();
 
 			if (value == null) {
 				target.addAttribute(name, "null");
 			} else if (value instanceof Boolean) {
 				target.addAttribute(name, "b" + String.valueOf(value));
 			} else if (value instanceof Integer) {
 				target.addAttribute(name, "i" + String.valueOf(value));
 			} else if (value instanceof Float) {
 				target.addAttribute(name, "f" + String.valueOf(value));
 			} else if (value instanceof Long) {
 				target.addAttribute(name, "l" + String.valueOf(value));
 			} else if (value instanceof Double) {
 				target.addAttribute(name, "d" + String.valueOf(value));
 			} else if (value instanceof String) {
 				target.addAttribute(name, "s" + String.valueOf(value));
 			} else if (value instanceof String[]) {
 				if (name.charAt(0) != '*')
 					name = "!" + name;
 
 				target.addAttribute(name, (String[]) value);
 			} else if (value instanceof Record[]) {
 				try {
 					String json = JSONHelper.getJsonString((Record[]) value);
 					System.out.println(json);
 					target.addAttribute(name, "j" + json);
 
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			} else if (value instanceof Paintable[]) {
 				List<String> references = new ArrayList<String>();
 
 				for (Paintable p : (Paintable[]) value) {
 					if (jspt.needsToBePainted(p))
 						p.paint(target);
 
 					references.add(jspt.getPaintIdentifier(p));
 				}
 
 				if (name.charAt(0) != '*')
 					name = "[" + name;
 
 				target.addAttribute(name, references.toArray()); // [ = array
 			} else if (value instanceof Paintable) {
 				String ref = jspt.getPaintIdentifier((Paintable) value);
 
 				if (jspt.needsToBePainted((Paintable) value))
 					((Paintable) value).paint(target);
 
 				if (name.charAt(0) != '*')
 					name = "#" + name;
 
 				target.addAttribute(name, ref); // # = reference
 			}
 		}
 	}
 }
