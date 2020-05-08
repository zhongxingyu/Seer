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
 
 import java.util.ArrayList;
 import java.util.List;
 
import net.sf.okapi.common.MimeTypeMapper;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.lib.extra.OkapiComponent;
 
 /**
  * The root of the filters hierarchy. Defines generic methods for all kinds of filters.
  * 
  * @version 0.1, 10.06.2009
  */
 
 public abstract class AbstractBaseFilter extends OkapiComponent implements IFilter {
 	
 	private String mimeType;
 	private String displayName;
 	
 	List<FilterConfiguration> configList = new ArrayList<FilterConfiguration>();
 	EncoderManager encoderManager;
 
 
 //	@Override
 //	protected void component_create() {
 //	public AbstractFilter() {
 //		
 //		configList = new ArrayList<FilterConfiguration>();
 //	}
 	
 	public AbstractBaseFilter() {
 		super();
 	}
 
 	protected void setMimeType(String mimeType) {
 		this.mimeType = mimeType;
 	}
 
 	public String getMimeType() {
 		return mimeType;
 	}
 	
 	protected void setDisplayName (String displayName) {
 		this.displayName = displayName;
 	}
 	
 	public String getDisplayName () {
 		return displayName;
 	}
 	
 	public EncoderManager getEncoderManager () {
 		if ( encoderManager == null ) {
 			encoderManager = new EncoderManager();
 			encoderManager.setAllKnownMappings();
 		}
 		return encoderManager;
 	}
 
 	protected boolean addConfiguration(			
 			boolean clearAllExisting,
 			String configId,
 			String name,
 			String description,
 			String parametersLocation) {
 		
 		if (configList == null) return false;
 		
 		if (clearAllExisting) configList.clear();
 		
 		return configList.add(new FilterConfiguration(
 				configId,
 				getMimeType(),
 				getClass().getName(),
 				name, description, parametersLocation));
 	}
 	
 	protected boolean addConfigurations(List<FilterConfiguration> configs) {
 	
 		if (configList == null) return false;
 		
 		return configList.addAll(configs);
 	}
 	
 	protected FilterConfiguration findConfiguration(String configId) {
 		
 		if (Util.isEmpty(configList)) return null;
 		
 		for (FilterConfiguration config : configList) {
 			
 			if (config == null) continue;
 			if (config.configId.equalsIgnoreCase(configId)) 
 				return config;
 		}
 		
 		return null;
 	}
 	
 	protected boolean removeConfiguration(String configId) {
 		
 		return configList.remove(findConfiguration(configId));
 	}
 				
 	public List<FilterConfiguration> getConfigurations () {
 		
 		List<FilterConfiguration> res = new ArrayList<FilterConfiguration>();
 		
 		for (FilterConfiguration fc : configList) 
 			res.add(new FilterConfiguration(
 				fc.configId,
 				getMimeType(),
 				getClass().getName(),
 				fc.name, fc.description, fc.parametersLocation));
 		
 		return res;
 	}
 
 	public boolean setConfiguration(String configId) {
 		
 		return true;
 	}
 	
 
 }
 
