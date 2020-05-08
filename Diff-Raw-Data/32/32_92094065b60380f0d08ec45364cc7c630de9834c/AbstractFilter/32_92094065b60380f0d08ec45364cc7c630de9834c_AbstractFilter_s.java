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
 
 package net.sf.okapi.filters.plaintext.common;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.okapi.common.BaseParameters;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.exceptions.OkapiBadFilterParametersException;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.filters.IFilter;
 
 /**
  * The root of the filters hierarchy. Defines generic methods for all kinds of filters.
  * 
  * @version 0.1, 10.06.2009
  * @author Sergei Vasilyev
  */
 
 public abstract class AbstractFilter implements IFilter, INotifiable {
 
 	private String filterName;
 	private String mimeType;
 	private IParameters params;
 	private String parametersClassName;
 	List<FilterConfiguration> configList = new ArrayList<FilterConfiguration>();
 	
 	public AbstractFilter() {
 		
 		super();
 	}
 
 	public IParameters getParameters() {
 		
 		return params;
 	}
 	
 	public void setParameters(IParameters params) {
 		
 		this.params = (BaseParameters) params;
 		
 		if (params instanceof INotifiable)
 			((INotifiable) params).notify(Notification.PARAMETERS_SET_OWNER, this);
 		
 //		if (!Util.isEmpty(parametersClassName)) return; // This name is set by the first call from the filter's constructor
 		if (params == null) return;
 		if (params.getClass() == null) return;
 		
 		parametersClassName = params.getClass().getName();
 	}
 
 	@SuppressWarnings("unchecked")
 	protected <A> A getParameters(Class<?> expectedClass) {
 		
 		if (params == null) {
 			throw new OkapiBadFilterParametersException("Empty filter parameters");			
 		}
 		
 		if (!expectedClass.isInstance(params)) {
 			
 			String st = "null";
 			if (params.getClass() != null) st = params.getClass().getName();
 			
 			throw new OkapiBadFilterParametersException(
 					String.format("Parameters of class <%s> expected, but are <%s>",
 							expectedClass.getName(), st));			
 		}
 		
 		return (A) params;
 	}
 
 	/**
 	 * Sets the input document mime type.
 	 * 
 	 * @param mimeType the new mime type
 	 */
 	protected void setMimeType(String mimeType) {
 		
 		this.mimeType = mimeType;
 	}
 
 	/**
 	 * Gets the input document mime type.
 	 * 
 	 * @return the mime type
 	 */
 
 	public String getMimeType() {
 		
 		return mimeType;
 	}
 	
 	protected void setName(String filterName) {
 		
 		this.filterName = filterName;
 	}
 	
 	public String getName() {
 		
 		return filterName;
 	}
 
 	protected boolean addConfiguration(			
 			boolean clearAllExisting,
 			String configId,
 			String name,
 			String description,
			String parameters) {
 		
 		if (configList == null) return false;
 		
 		if (clearAllExisting) configList.clear();
 		
 		return configList.add(new FilterConfiguration(
 				configId,
 				getMimeType(),
 				getClass().getName(),
				name, description, parameters));
 	}
 	
 	protected boolean addConfigurations(List<FilterConfiguration> configs) {
 	
 		if (configList == null) return false;
 		
 		return configList.addAll(configs);
 	}
 	
 	public List<FilterConfiguration> getConfigurations () {
 		
		return configList;
 	}
 
 	public boolean setConfiguration(String configId) {
 		
 		return true;
 	}
 	
 	public String getParametersClassName() {
 		
 		return parametersClassName;
 	}
 
 	public boolean notify(String notification, Object info) {
 				
 		return false;		
 	}
 }
 
