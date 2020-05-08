 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.lib.extra.filters;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.LinkedList;
 
 import net.sf.okapi.common.BaseParameters;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.ParametersString;
 import net.sf.okapi.lib.extra.AbstractParameters;
 import net.sf.okapi.lib.extra.Notification;
 
 /**
  * Compound Filter parameters.
  * 
  * @version 0.1, 10.06.2009
  */
 
 public class CompoundFilterParameters extends AbstractParameters {
 
 	private String parametersClass = "";
 	private LinkedList<IParameters> parameters = new LinkedList<IParameters>();
 	private IParameters activeParameters = null;
 	private String defParametersClass = "";
 	
 	public IParameters getActiveParameters() {
 		
 		return activeParameters;
 	}
 
 	protected void setActiveParameters(IParameters activeParameters) {
 		
 		this.activeParameters = activeParameters;
 	}	
 	
 	protected <T extends BaseParameters> boolean addParameters(Class<T> parametersClass) {
 		
 		if (parameters == null) return false;
 		boolean res = false;
 	
 		IParameters params = null;
 		BaseParameters bp = null;
 		
 		try {
 			
 			if (!BaseParameters.class.isAssignableFrom(parametersClass)) return false;
 				
 			Constructor<T> bpc;
 			try {
 				bpc = (Constructor<T>) parametersClass.getConstructor(new Class[] {});			
 				if (bpc == null) return false;			
 										
 				bp = bpc.newInstance(new Object[] {});
 				
 			} catch (SecurityException e) {
 				
 				return false;
 				
 			} catch (NoSuchMethodException e) {
 				
 				return false;
 				
 			} catch (IllegalArgumentException e) {
 				
 				return false;
 				
 			} catch (InvocationTargetException e) {
 				
 				return false;				
 			}
 			
 			res = parameters.add(bp);
 			if (!res) return false;
 			
 			params = parameters.getLast();
 			if (params == null) return false;
 						
 		} catch (InstantiationException e2) {
 			
 			return false;
 			
 		} catch (IllegalAccessException e2) {
 			
 			return false;
 		}
 		
 		if (activeParameters == null) {
 			
 			activeParameters = params;  // The first non-empty registered one will become active
 			
 			if (params == null) return false;
 			if (params.getClass() == null) return false;
 			
 			defParametersClass = params.getClass().getName();
 		}
 							
 		return res;
 	}
 
 	public boolean setActiveParameters(String parametersClass) {
 		
 		IParameters params = findParameters(parametersClass);
 		if (params == null) return false; 
 		
 		if (activeParameters != params) {
 			
 			// Some finalization of the previous one might be needed
 			activeParameters = params;
 			this.parametersClass = parametersClass;
 		}
 		
 		if (owner != null)
 			owner.exec(this, Notification.PARAMETERS_CHANGED, parametersClass);
 		
 		return true;
 	}
 	
 	private IParameters findParameters(String parametersClass) {
 					
 		if (parameters == null) return null;
 		
 		for (IParameters params : parameters) {
 			
 			if (params == null) continue;
 			if (params.getClass() == null) continue;
 			
 			if (params.getClass().getName().equalsIgnoreCase(parametersClass)) 
 				return params;
 		}
 		
 		return null;
 	}
 
 	protected void setParametersClassName(String parametersClass) {
 		
 		this.parametersClass = parametersClass;
 
 		setActiveParameters(parametersClass);		
 	}
 
 	public void setParametersClass(Class<?> parametersClass) {
 	
 		if (parametersClass == null) return;
 		setParametersClassName(parametersClass.getName());
 	}
 	
 	public String getParametersClassName() {
 		
 		return parametersClass;
 	}
 	
 	public Class<?> getParametersClass() {		
 		try {
 			return Class.forName(parametersClass);
 			
 		} catch (ClassNotFoundException e) {
 			return null;
 		}
 	}
 
 	public LinkedList<IParameters> getParameters() {
 		
 		return parameters;
 	}
 
 	@Override
 	protected void parameters_init() {
 		
 	}
 
 	@Override
 	protected void parameters_load(ParametersString buffer) {
 		
 		setParametersClassName(buffer.getString("parametersClass", defParametersClass));
 		setActiveParameters(getParametersClassName());
 				
		// Load active parameters
		if (activeParameters != null)			
			activeParameters.fromString(getData());
 		
 		if (owner != null) // activeParameters.getClass()
 		owner.exec(this, Notification.PARAMETERS_CHANGED, parametersClass);
 	}
 
 	@Override
 	protected void parameters_reset() {
 		
 		setParametersClassName(defParametersClass);
 	}
 
 	@Override
 	protected void parameters_save(ParametersString buffer) {
 		
 		//!!! Do not change the sequence
 
 		// Store active parameters		
 		if (activeParameters != null)			
 			buffer.fromString(activeParameters.toString());
 		
 //		if (activeParameters == null)
 //			setParametersClassName(defParametersClass);
 //		else
 //			setParametersClassName(activeParameters.getClass().getName()); 
 //
 //		buffer.setString("parametersClass", getParametersClassName());
 		
 		 
 			if (activeParameters == null)
 				this.parametersClass = defParametersClass;
 			else
 				this.parametersClass = activeParameters.getClass().getName(); 
 	
 			buffer.setString("parametersClass", this.parametersClass);
 	}
 	
 }
