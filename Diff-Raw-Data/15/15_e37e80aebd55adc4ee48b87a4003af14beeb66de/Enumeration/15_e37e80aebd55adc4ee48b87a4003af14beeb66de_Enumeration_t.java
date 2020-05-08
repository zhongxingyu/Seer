 package com.hapiware.utils.cmdline.constraint;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import com.hapiware.utils.cmdline.element.Description;
 
 
 public class Enumeration
 	implements
 		Constraint
 {
 	private List<Enum> _enumerations = new LinkedList<Enum>();
 	
 	public Enum value(String value)
 	{
 		Enum e = new Enum(value);
 		_enumerations.add(e);
 		return e;
 	}
 
 	public void evaluate(String argumentName, Object value) throws ConstraintException
 	{
 		String str = value.toString();
 		boolean isOk = false;
 		for(Enum e : _enumerations) {
 			if(e._ignoreCase) {
 				if(e.value().equalsIgnoreCase(str)) {
 					isOk = true;
 					break;
 				}
 			}
 			else {
 				if(e.value().equals(str)) {
 					isOk = true;
 					break;
 				}
 			}
 		}
 		
 		if(!isOk) {
 			String msg =
 				"Value for '" + argumentName + "' was [" + value + "] but it must be"
 					+ " one of these: " + _enumerations.toString();
 			throw new ConstraintException(msg);
 		}
 	}
 
 
 	public Description description()
 	{
 		Description description = new Description();
 		for(Enum e : _enumerations) {
 			if(e._description == null || e._description.trim().length() == 0)
 				return null;
 			description.strong(e.value()).description(", " + e._description).p();
 		}
 		return description;
 	}
 	
 	public static class Enum {
 		private String _value;
 		private boolean _ignoreCase;
 		private String _description;
 		
 		public Enum(String value)
 		{
 			_value = value;
 		}
 		
 		public Enum ignoreCase()
 		{
 			_ignoreCase = true;
 			return this;
 		}
 		
 		public void description(String description)
 		{
			if(description == null || description.trim().length() == 0)
				throw new ConfigurationException("'description' must have a value.");
 			_description = description;
 		}
 		
		public void d(String description)
		{
			description(description);
		}
		
 		public String value()
 		{
 			return _value;
 		}
 		
 		
 		@Override
 		public boolean equals(Object obj)
 		{
 			if(obj == this)
 				return true;
 
 			if(!(obj instanceof Enumeration.Enum))
 				return false;
 			Enumeration.Enum object = (Enumeration.Enum)obj;
 			
 			return _value.equals(object._value) && _ignoreCase == object._ignoreCase;
 		}
 
 		@Override
 		public int hashCode()
 		{
 			int resultHash = 17;
 			resultHash = 31 * resultHash + (_ignoreCase ? 1 : 0);
 			resultHash = 31 * resultHash + (_value == null ? 0 : _value.hashCode());
 			return resultHash;
 		}
 		
 		@Override
 		public String toString()
 		{
 			return _value.toString();
 		}
 	}
 }
