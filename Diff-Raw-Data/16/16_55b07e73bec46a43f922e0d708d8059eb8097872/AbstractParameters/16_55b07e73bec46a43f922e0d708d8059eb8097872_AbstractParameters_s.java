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
 
 package net.sf.okapi.lib.extra;
 
import java.io.InputStream;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.sf.okapi.common.BaseParameters;
 import net.sf.okapi.common.ListUtil;
 import net.sf.okapi.common.ParametersString;
 import net.sf.okapi.common.StringUtil;
 import net.sf.okapi.common.Util;
 
 /**
  * 
  * 
  * @version 0.1, 18.06.2009
  */
 
 public abstract class AbstractParameters extends BaseParameters implements INotifiable {
 	
 	protected Component owner = null;
 	private String data;
 
 	protected final Logger logger = Logger.getLogger(this.getClass().getName());
 	
 	public AbstractParameters() {
 		
 		super();		
 		
 		parameters_init();
 		reset();
 		toString(); // fill the list
 	}
 	
 	/**
 	 * Called from the parameters' constructor. Create local objects here or leave empty. Initial values are assigned in reset().
 	 */
 	protected void parameters_init() {};
 	
 	/**
 	 * Reset parameters values to defaults.
 	 */
 	protected abstract void parameters_reset();
 	
 	/**
 	 * Load from buffer. The protected buffer variable is visible in all subclasses of BaseParameters.<p>
 	 * @example myParam = buffer.getBoolean("myParam", false);
 	 */
 	protected abstract void parameters_load(ParametersString buffer);
 	
 	/**
 	 * Save to buffer. The protected buffer variable is visible in all subclasses of BaseParameters.<p>
 	 * @example buffer.setBoolean("myParam", myParam);
 	 */
 	protected abstract void parameters_save(ParametersString buffer);
 	
 	
 	final public void reset() {
 		
 		parameters_reset();
 	}
 	
 	final public void fromString(String data) {
 		
 		reset();
 		this.data = StringUtil.normalizeLineBreaks(data); 
 		buffer.fromString(this.data);
 //		buffer.fromString(StringUtil.normalizeLineBreaks(data));
 
 		parameters_load(buffer); // this.getClass()
 		
 		if (owner != null)
 			owner.exec(this, Notification.PARAMETERS_CHANGED, null);
 	}
 	
 	final public String toString () {
 		
 		buffer.reset();
 		parameters_save(buffer);
 		
 		return buffer.toString();
 	}
 	
 	public boolean exec(Object sender, String command, Object info) {
 				
 		if (command.equalsIgnoreCase(Notification.PARAMETERS_SET_OWNER)) {
 			
 			if (info instanceof Component)
 				owner = (Component) info;
 			
 			return true;
 		}
 		return false;
 	}
 	
 	public <T extends AbstractParameters> void loadGroup(ParametersString buffer, List<T> group, Class<T> elementClass) {
 		
 		if (elementClass == null) return;
 		
 		loadGroup(buffer, elementClass.getSimpleName(), group, elementClass);
 	}
 	
 	public <T extends AbstractParameters> void loadGroup(ParametersString buffer, String groupName, List<T> group, Class<T> elementClass) {
 		
 		if (buffer == null) return;
 		if (group == null) return;
 		if (Util.isEmpty(groupName)) return;
 		if (elementClass == null) return;
 		
 		group.clear();
 		int count = buffer.getInteger(String.format("%sCount", groupName));
 		
 		for (int i = 0; i < count; i++ ) {
 			
 			T item = null;
 			try {
 				item = elementClass.newInstance();
 				
 			} catch (InstantiationException e) {
 				
 				//e.printStackTrace();
 				logMessage(Level.FINE, "Group element instantiation failed: " + e.getMessage());
 				return;
 				
 			} catch (IllegalAccessException e) {
 				
 				//e.printStackTrace();
 				logMessage(Level.FINE, "Group element instantiation failed: " + e.getMessage());
 				return;
 			}
 			
 			if (item == null) return;
 			
 			item.parameters_load(new ParametersString(buffer.getGroup(String.format("%s%d", groupName, i))));
 			
 			group.add(item);
 		}
 		
 		T item = ListUtil.getFirstNonNullItem(group);
 		if (item == null) return;
 		
 		if (item.owner != null)
 			item.owner.exec(group, Notification.PARAMETERS_CHANGED, null);
 	}
 	
 	public <T extends AbstractParameters> void saveGroup(ParametersString buffer, String groupName, List<T> group) {
 		
 		if (buffer == null) return;
 		if (group == null) return;
 		if (Util.isEmpty(groupName)) return;
 		
 		buffer.setInteger(String.format("%sCount", groupName), group.size());
 		
 		for (int i = 0; i < group.size(); i++) {
 			
 			AbstractParameters item = group.get(i);
 			ParametersString tmp = new ParametersString(); 
 			
 			item.parameters_save(tmp);
 			buffer.setGroup(String.format("%s%d", groupName, i), tmp.toString());
 		}
 	}
 	
 	public <T extends AbstractParameters> void saveGroup(ParametersString buffer, List<T> group, Class<T> elementClass) {
 		
 		if (elementClass == null) return;
 		
 		saveGroup(buffer, elementClass.getSimpleName(), group);
 	}
 	
 	public boolean loadFromResource(String resourceLocation) {
 
 		URL url = this.getClass().getResource(resourceLocation);
         if (url == null) return false;
         
         try {
         	load(url.toURI(), false);
         }
         catch (URISyntaxException e) {
                
         	return false;
         }
         
 		return true;
 	}
 	
 	public boolean loadFromResource(Class<?> classRef, String resourceLocation) {
 
 		if (classRef == null) 
 			return loadFromResource(resourceLocation);
 		
 		//InputStream stream = classRef.getResourceAsStream(resourceLocation);
 		URL url = classRef.getResource(resourceLocation);
         if (url == null) return false;
         
         try {
         	load(url.toURI(), false);
         }
         catch (URISyntaxException e) {
                
         	return false;
         }
         
 		return true;
 	}
 	
 	public void saveToResource(String resourceLocation) {
 		
 		URL url = this.getClass().getResource(resourceLocation);
         if (url == null) return;
         
         save(url.getPath());
 	}
 	
 	// TODO Test
 	public void saveToResource(Class<?> classRef, String resourceLocation) {
 		
 		if (classRef == null) {
 			
 			saveToResource(resourceLocation);
 			return;
 		}
 			 		
 		URL url = classRef.getResource(resourceLocation);
         if (url == null) return;
         
         save(url.getPath());
 	}
 	
 	public String getData() {
 		
 		return data;
 	}
 	
 	protected void logMessage (Level level, String text) {
 		
 		if (logger != null)
 			logger.log(level, text);
 	}
 }
 
