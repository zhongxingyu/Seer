 /*
     TrayRSS - simply notification of feed information
     (c) 2009-2011 TrayRSS Developement Team
     visit the project at http://trayrss.nullpointer.at/
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program. If not, see <http://www.gnu.org/licenses/>.
 
  */
 package at.nullpointer.trayrss.messages;
 
 import java.beans.Beans;
 import java.util.Locale;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 public class MessageResolverImpl implements MessageResolver {
 	////////////////////////////////////////////////////////////////////////////
 	//
 	// Constructor
 	//
 	////////////////////////////////////////////////////////////////////////////
 	public MessageResolverImpl(String bundleName) {
 		this.bundleName = bundleName;
 		this.resourceBundle = loadBundle();
 	}
 	////////////////////////////////////////////////////////////////////////////
 	//
 	// Bundle access
 	//
 	////////////////////////////////////////////////////////////////////////////
 	private String bundleName = "at.nullpointer.trayrss.messages.configurationmessages"; //$NON-NLS-1$
 	private static Locale LOCALE = Locale.ENGLISH; 
 	private ResourceBundle resourceBundle;
 	private ResourceBundle loadBundle() {
 		return ResourceBundle.getBundle(bundleName, LOCALE);
 	}
 	////////////////////////////////////////////////////////////////////////////
 	//
 	// Strings access
 	//
 	////////////////////////////////////////////////////////////////////////////
 	public String getString(String key, String defaultValue) {
 		try {
 			ResourceBundle bundle = Beans.isDesignTime() ? loadBundle() : this.resourceBundle;
 			return bundle.getString(key);
 		} catch (MissingResourceException e) {
 			return defaultValue;
 		}
 	}
 	
 	public void chanceLocale(Locale locale){
 		LOCALE = locale;
 		resourceBundle = loadBundle();
 	}
 }
