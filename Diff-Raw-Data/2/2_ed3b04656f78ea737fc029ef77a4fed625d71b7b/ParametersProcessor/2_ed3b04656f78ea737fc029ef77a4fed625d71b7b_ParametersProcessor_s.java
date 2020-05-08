 package com.roshka.raf.params;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.roshka.raf.exception.RAFException;
 import com.roshka.raf.refl.RAFParameter;
 import com.roshka.raf.route.Route;
 
 public class ParametersProcessor {
 
 	private HttpServletRequest req;
 	private Route route;
 	
 	public ParametersProcessor(HttpServletRequest req, Route route) {
 		this.req = req;
 		this.route = route;
 	}
 	
 	private Object getNullOrZero(Class<?> clazz)
 	{
 		Object ret = null;
 		if (clazz.isPrimitive()) {
 			if (clazz.equals(Byte.TYPE)) {
 				ret = (byte)0;
 			} else if (clazz.equals(Short.TYPE)) {
 				ret = (short)0;
 			} else if (clazz.equals(Integer.TYPE)) {
 				ret = 0;
 			} else if (clazz.equals(Long.TYPE)) {
 				ret = 0L;
 			} else if (clazz.equals(Float.TYPE)) {
 				ret = 0.0f;
 			} else if (clazz.equals(Double.TYPE)) {
 				ret = 0.0;
 			} else if (clazz.equals(Boolean.TYPE)) {
 				ret = false;
 			} else if (clazz.equals(Character.TYPE)) {
 				ret = '\0';
 			}
 		} else {
 			ret = null;
 		}
 		return ret;
 	}
 	
 	private Object getValue(Class<?> clazz, RAFParameter rp, String value) 
 		throws RAFException
 	{
 		String parameterName;
 		parameterName = rp.getParameterName();
 		
 		Object ret = null;
 		if (clazz.isPrimitive()) {
 			
 			if (clazz.equals(Byte.TYPE)) {
 				ret = NumberProcessor.parseByte(parameterName, value);
 			} else if (clazz.equals(Short.TYPE)) {
 				ret = NumberProcessor.parseShort(parameterName, value);
 			} else if (clazz.equals(Integer.TYPE)) {
 				ret = NumberProcessor.parseInt(parameterName, value);
 			} else if (clazz.equals(Long.TYPE)) {
				ret = NumberProcessor.parseShort(parameterName, value);
 			} else if (clazz.equals(Float.TYPE)) {
 				ret = NumberProcessor.parseFloat(parameterName, value);
 			} else if (clazz.equals(Double.TYPE)) {
 				ret = NumberProcessor.parseDouble(parameterName, value);
 			} else if (clazz.equals(Boolean.TYPE)) {
 				ret = BooleanProcessor.parseBoolean(parameterName, value);
 			} else if (clazz.equals(Character.TYPE)) {
 				ret = CharacterProcessor.parseCharacter(parameterName, value);
 			}
 			
 		} else {
 			if (clazz.equals(String.class)) {
 				ret = value;
 			} else if (clazz.equals(BigDecimal.class)) {
 				ret = NumberProcessor.parseBigDecimal(parameterName, value);
 			} else if (clazz.equals(BigInteger.class)) {
 				ret = NumberProcessor.parseBigInteger(parameterName, value);
 			} else if (clazz.equals(java.util.Date.class)) {
 				ret = DateProcessor.parseUtilDate(parameterName, rp.getDateFormat(), value);
 			} else if (clazz.equals(java.sql.Date.class)) {
 				ret = DateProcessor.parseSqlDate(parameterName, rp.getDateFormat(), value);
 			} else if (clazz.isAssignableFrom(java.util.Calendar.class)) {
 				ret = DateProcessor.parseCalendar(parameterName, rp.getDateFormat(), value);
 			}
 			
 		}
 		return ret;
 	}
 	
 	public Object[] getParameters()
 		throws RAFException
 	{
 		List<RAFParameter> rafParameters = route.getActionMethod().getParameters();
 		List<Object> objects = new ArrayList<Object>();
 		for (RAFParameter rafParameter : rafParameters) {
 			String paramValue = req.getParameter(rafParameter.getParameterName());
 			
 			// check mandatory parameter
 			if (paramValue == null && rafParameter.isMandatory()) {
 				throw new RAFException(RAFException.ERRCODE_INVALID_PARAMETER_VALUE, String.format("Parameter %s is mandatory", rafParameter.getParameterName()));
 			} else if (paramValue != null) {
 				objects.add(getValue(rafParameter.getClazz(), rafParameter, paramValue));
 			} else {
 				// add null (or default value parameter)
 				if (!rafParameter.getDefaultValue().equalsIgnoreCase(Globals.DEFAULT_UNASSIGNED_PARAMETER)) {
 					objects.add(getValue(rafParameter.getClazz(), rafParameter, rafParameter.getDefaultValue()));
 				} else {
 					objects.add(getNullOrZero(rafParameter.getClazz()));
 				}
 			}
 		}
 		return objects.toArray(new Object[0]);
 	}
 	
 }
