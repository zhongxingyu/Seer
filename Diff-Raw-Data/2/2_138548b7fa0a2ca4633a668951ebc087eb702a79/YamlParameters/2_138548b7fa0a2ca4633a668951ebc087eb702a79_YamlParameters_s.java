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
 
 package net.sf.okapi.filters.openxml;
 
 import java.io.File;
 import java.net.URL;
 
 import net.sf.okapi.common.BaseParameters;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.filters.yaml.TaggedFilterConfiguration;
 
 /**
  * {@link IParameters} based facade around the YAML configuration format.
  * 
  */
 public class YamlParameters extends BaseParameters {
	private static final String DEFAULT_PARAMETERS = "defaultConfiguration.yml";
 
 	public static String getDefualtParameterFile() {
 		return DEFAULT_PARAMETERS;
 	}
 
 	private TaggedFilterConfiguration taggedConfig;
 
 	public YamlParameters() {
 		reset();
 	}
 	
 	public YamlParameters(URL configPath) {
 		setTaggedConfig(new TaggedFilterConfiguration(configPath));
 	}
 
 	public YamlParameters(File configFile) {
 		setTaggedConfig(new TaggedFilterConfiguration(configFile));
 	}
 
 	public YamlParameters(String configAsString) {
 		setTaggedConfig(new TaggedFilterConfiguration(configAsString));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.sf.okapi.common.IParameters#fromString(java.lang.String)
 	 */
 	public void fromString(String data) {
 		taggedConfig = new TaggedFilterConfiguration(data);
 	}
 
 	@Override
 	public String toString() {
 		return taggedConfig.toString();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.sf.okapi.common.IParameters#reset()
 	 */
 	public void reset() {		
 		taggedConfig = new TaggedFilterConfiguration(OpenXMLContentFilter.class.getResource(DEFAULT_PARAMETERS));
 	}
 
 	public TaggedFilterConfiguration getTaggedConfig() {
 		return taggedConfig;
 	}
 
 	/**
 	 * @param IFilter Configuration
 	 */
 	public void setTaggedConfig(TaggedFilterConfiguration taggedConfig) {
 		this.taggedConfig = taggedConfig;
 	}	
 }
