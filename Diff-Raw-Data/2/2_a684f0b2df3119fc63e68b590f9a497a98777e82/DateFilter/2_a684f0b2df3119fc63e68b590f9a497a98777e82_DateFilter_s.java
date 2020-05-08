  /**
 *
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, NCSA.  All rights reserved.
 *
 * Developed by:
 * The Automated Learning Group
 * University of Illinois at Urbana-Champaign
 * http://www.seasr.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimers.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimers in
 * the documentation and/or other materials provided with the distribution.
 *
 * Neither the names of The Automated Learning Group, University of
 * Illinois at Urbana-Champaign, nor the names of its contributors may
 * be used to endorse or promote products derived from this Software
 * without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *
 */
 
 package org.seasr.meandre.components.transform.xml;
 
 import org.apache.velocity.VelocityContext;
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.components.abstracts.AbstractExecutableComponent;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.BasicDataTypesTools;
 import org.seasr.meandre.components.tools.Names;
 import org.seasr.meandre.support.generic.html.VelocityTemplateService;
 
 @Component(
 		name = "Date Filter",
 		creator = "Lily Dong",
 		baseURL = "meandre://seasr.org/components/foundry/",
 		firingPolicy = FiringPolicy.all,
 		mode = Mode.compute,
 		rights = Licenses.UofINCSA,
 		tags = "date, xsl, filter",
 		description = "This component generates an xsl template to filter an xml file and include only " +
 		"the dates between the minimum and maximum year. This is used to filter the Simile xml file that is generated.",
 		dependency = {"protobuf-java-2.2.0.jar", "velocity-1.6.2-dep.jar"},
 		resources = { "DateFilter.vm" }
 )
 
 public class DateFilter extends AbstractExecutableComponent {
 	 //------------------------------ PROPERTIES --------------------------------------------------
 
     @ComponentProperty(
     		defaultValue="1600",
             name=Names.PORT_MIN_VALUE,
             description = "The minimum year to include in the xsl template."
     )
     protected static final String PROP_MIN_VALUE = Names.PORT_MIN_VALUE;
 
     @ComponentProperty(
     		defaultValue="1800",
             name=Names.PORT_MAX_VALUE,
             description = "The maximum year to include in the xsl template."
     )
     protected static final String PROP_MAX_VALUE = Names.PORT_MAX_VALUE;
 
    //------------------------------ OUTPUTS -----------------------------------------------------
 
 	@ComponentOutput(
 			name = Names.PORT_XSL,
 			description = "The XSL template for filtering dates."
 	)
 	private final static String OUT_XSL = Names.PORT_XSL;
 
 	@ComponentOutput(
 			name = Names.PORT_MIN_YEAR,
 			description = "The minimum year."
 	)
 	private final static String OUT_MIN_YEAR = Names.PORT_MIN_YEAR;
 
 	@ComponentOutput(
 			name = Names.PORT_MAX_YEAR,
 			description = "The maximum year."
 	)
 	private final static String OUT_MAX_YEAR = Names.PORT_MAX_YEAR;
 
    static final String DEFAULT_TEMPLATE = "org/seasr/meandre/components/tools/xml/io/DateFilter.vm";
 
     /** The min year and max year to use */
 	private String minYear, maxYear;
 
 	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
 	}
 
 	public void executeCallBack(ComponentContext cc) throws Exception {
 		minYear = cc.getProperty(PROP_MIN_VALUE);
 		maxYear = cc.getProperty(PROP_MAX_VALUE);
 
 		VelocityTemplateService velocity = VelocityTemplateService.getInstance();
 		VelocityContext context;
 
 		context = velocity.getNewContext();
 
 		context.put("min_year", minYear);
 		context.put("max_year", maxYear);
 
 		String xsl = velocity.generateOutput(context, DEFAULT_TEMPLATE);
 
 		cc.pushDataComponentToOutput(OUT_MIN_YEAR, BasicDataTypesTools.stringToStrings(minYear));
 		cc.pushDataComponentToOutput(OUT_MAX_YEAR, BasicDataTypesTools.stringToStrings(maxYear));
 		cc.pushDataComponentToOutput(OUT_XSL, BasicDataTypesTools.stringToStrings(xsl));
 	}
 
 	public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
 	}
 }
