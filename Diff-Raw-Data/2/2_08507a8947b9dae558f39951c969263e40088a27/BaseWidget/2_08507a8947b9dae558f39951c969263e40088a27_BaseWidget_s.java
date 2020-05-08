 package org.vaadin.smartgwt;
 
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.vaadin.terminal.PaintException;
 import com.vaadin.terminal.PaintTarget;
 import com.vaadin.ui.AbstractComponent;
 
 public abstract class BaseWidget extends AbstractComponent implements Serializable
 {
 	protected Map<String, Object> attributes = new HashMap<String, Object>();
 	private boolean isCreated = false;
 
 	boolean isCreated()
 	{
 		return isCreated;
 	}
 
 	public void setAttribute(String attribute, Object value, boolean allowPostCreate)
 	{
 		if (isCreated() && !allowPostCreate)
 		{
 			throw new IllegalArgumentException("Cannot modify property " + attribute + " once created");
 		}
 
 		attributes.put(attribute, value);
 	}
 
 	public void setAttribute(String attribute, Object value)
 	{
 		setAttribute(attribute, value, true);
 	}
 
 	public String getAttributeAsString(String attribute)
 	{
 		Object value = attributes.get(attribute);
 
 		if (value == null)
 			return null;
 		else
 			return value.toString();
 	}
 
 	public String getAttribute(String attribute)
 	{
 		return getAttributeAsString(attribute);
 	}
 
 	public Integer getAttributeAsInt(String attribute)
 	{
 		Object value = attributes.get(attribute);
 
 		if (value == null)
 			return null;
 		else
 			return Integer.valueOf(value.toString());
 	}
 
 	public Boolean getAttributeAsBoolean(String attribute)
 	{
 		Object value = attributes.get(attribute);
 
 		if (value == null)
 			return null;
 		else
 			return Boolean.valueOf(value.toString());
 	}
 
 	public Date getAttributeAsDate(String attribute)
 	{
 		Object value = attributes.get(attribute);
 
 		if (value == null)
 			return null;
 		else
 		{
 			DateFormat df = new SimpleDateFormat();
 			try
 			{
 				Date d = df.parse(value.toString());
 				return d;
 			}
 			catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 
 	public String[] getAttributeAsStringArray(String attribute)
 	{
 		Object value = attributes.get(attribute);
 
 		if (value == null)
 			return null;
 		else
 			return (String[]) value;
 	}
 
 	@Override
 	public void paintContent(PaintTarget target) throws PaintException
 	{
 		super.paintContent(target);
 
 		for (Map.Entry<String, Object> entry : attributes.entrySet())
 		{
			target.addAttribute(entry.getKey(), entry.getValue().toString());
 		}
 
 		// Since the paint is finished, set the created attribute
 		isCreated = true;
 	}
 
 }
