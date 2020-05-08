 /*===========================================================================
   Copyright (C) 2009-2010 by the Okapi Framework contributors
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
 
 package net.sf.okapi.filters.xmlstream;
 
 import java.io.File;
 import java.net.URL;
 import java.util.logging.Logger;
 
 import net.htmlparser.jericho.EndTag;
 import net.htmlparser.jericho.StartTag;
 import net.htmlparser.jericho.Tag;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.MimeTypeMapper;
 import net.sf.okapi.common.UsingParameters;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.filters.abstractmarkup.AbstractMarkupEventBuilder;
 import net.sf.okapi.filters.abstractmarkup.AbstractMarkupFilter;
 import net.sf.okapi.filters.yaml.TaggedFilterConfiguration;
 
 @UsingParameters(Parameters.class)
 public class XmlStreamFilter extends AbstractMarkupFilter {
 
 	private static final Logger LOGGER = Logger.getLogger(XmlStreamFilter.class.getName());
 
 	private Parameters parameters;
 	
 	public XmlStreamFilter() {
 		super();			
 		setMimeType(MimeTypeMapper.XML_MIME_TYPE);
 		setFilterWriter(createFilterWriter());
 		setParameters(new Parameters());
 		setName("okf_xmlstream"); //$NON-NLS-1$
 		setDisplayName("XML Stream Filter (BETA)"); //$NON-NLS-1$
 		addConfiguration(new FilterConfiguration(getName(), MimeTypeMapper.XML_MIME_TYPE,
 				getClass().getName(), "XML Stream", "Large XML Documents", //$NON-NLS-1$
 				Parameters.DEFAULT_PARAMETERS));
		addConfiguration(new FilterConfiguration(getName(), MimeTypeMapper.XML_MIME_TYPE,
 				getClass().getName(), "DITA", "DITA XML", //$NON-NLS-1$
 				Parameters.DITA_PARAMETERS));
		addConfiguration(new FilterConfiguration(getName(), MimeTypeMapper.XML_MIME_TYPE,
				getClass().getName(), "Java Properties XML", "Java Properties XML with Embedded HTML", //$NON-NLS-1$
 				Parameters.PROPERTY_XML_PARAMETERS));
 	}
 
 	/**
 	 * Initialize rule state and parser. Called before processing of each input.
 	 */
 	@Override
 	protected void startFilter() {
 		super.startFilter();
 		getEventBuilder().initializeCodeFinder(getConfig().isUseCodeFinder(), 
 				getConfig().getCodeFinderRules());
 	}
 
 	@Override
 	protected void handleStartTag(StartTag startTag) {
 		super.handleStartTag(startTag);
 	}
 
 	@Override
 	protected void handleEndTag(EndTag endTag) {
 		super.handleEndTag(endTag);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see net.sf.okapi.common.markupfilter.BaseMarkupFilter#normalizeName(java. lang.String)
 	 */
 	@Override
 	protected String normalizeAttributeName(String attrName, String attrValue, Tag tag) {
 		// normalize values for XML
 		String normalizedName = attrName;
 
 		// <x xml:lang="en">
 		if (attrName.equalsIgnoreCase("xml:lang")) {
 			normalizedName = Property.LANGUAGE;
 		}
 
 		return normalizedName;
 	}
 
 	@Override
 	protected TaggedFilterConfiguration getConfig() {
 		return parameters.getTaggedConfig();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see net.sf.okapi.common.filters.IFilter#setParameters(net.sf.okapi.common .IParameters)
 	 */
 	public void setParameters(IParameters params) {
 		this.parameters = (Parameters) params;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see net.sf.okapi.common.filters.IFilter#getParameters()
 	 */
 	public IParameters getParameters() {
 		return parameters;
 	}
 
 	/**
 	 * Initialize filter parameters from a URL.
 	 * 
 	 * @param config
 	 */
 	public void setParametersFromURL(URL config) {
 		parameters = new Parameters(config);
 	}
 
 	/**
 	 * Initialize filter parameters from a Java File.
 	 * 
 	 * @param config
 	 */
 	public void setParametersFromFile(File config) {
 		parameters = new Parameters(config);
 	}
 
 	/**
 	 * Initialize filter parameters from a String.
 	 * 
 	 * @param config
 	 */
 	public void setParametersFromString(String config) {
 		parameters = new Parameters(config);
 	}
 	
 	/**
 	 * @return the {@link AbstractMarkupEventBuilder}
 	 */
 	public AbstractMarkupEventBuilder getEventBuilder() {
 		return (AbstractMarkupEventBuilder)super.getEventBuilder();
 	}
 }
