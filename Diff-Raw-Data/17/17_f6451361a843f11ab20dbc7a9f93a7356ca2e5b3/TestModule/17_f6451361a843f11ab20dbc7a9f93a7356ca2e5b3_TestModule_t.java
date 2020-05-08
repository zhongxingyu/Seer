 /*
  * Copyright (c) 2009 ioko365 Ltd
  *
  * This file is part of ioko tapestry-commons.
  *
  *     ioko tapestry-commons is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     ioko tapestry-commons is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with ioko tapestry-commons.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.co.ioko.tapestry.javascript.services;
 
 import org.apache.tapestry5.Asset;
 import org.apache.tapestry5.SymbolConstants;
 import org.apache.tapestry5.annotations.Path;
 import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
 import org.apache.tapestry5.ioc.annotations.SubModule;
 
 /**
  * Created by IntelliJ IDEA. User: ben Date: Jul 7, 2009 Time: 2:04:30 PM
  */
 @SubModule(JavascriptModule.class)
 public class TestModule {
 
	public static void contributeJavascriptStack(
			@Path("classpath:/uk/co/ioko/tapestry/javascript/jquery-1.3.2.js") Asset jquery,
			@Path("classpath:/uk/co/ioko/tapestry/javascript/jquery-1.3.2.min.js") Asset jquerymin,
			@Path("classpath:/uk/co/ioko/tapestry/javascript/jquery-1.3.2.min.js") Asset jquerymin2,
			OrderedConfiguration<Asset> stack) {
		stack.add("jquery", jquery);
 		// As a test add jquery in 2 more times. The first should be added (as it is a different filename) the second shouldn't
		stack.add("jquerymin",jquerymin);
		stack.add("jquerymin2", jquerymin2);
 	}
 
 	public static void contributeApplicationDefaults(MappedConfiguration<String, String> configuration) {
 
 		configuration.add(SymbolConstants.APPLICATION_VERSION, "1.0");
 		configuration.add(SymbolConstants.PRODUCTION_MODE, "false");
 	}
 }
