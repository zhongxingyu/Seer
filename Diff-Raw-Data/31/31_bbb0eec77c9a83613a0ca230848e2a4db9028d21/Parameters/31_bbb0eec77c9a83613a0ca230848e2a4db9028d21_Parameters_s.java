 /*===========================================================================
   Copyright (C) 2010 by the Okapi Framework contributors
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
 
 package net.sf.okapi.filters.railsyaml;
 
 import net.sf.okapi.common.BaseParameters;
 import net.sf.okapi.common.filters.InlineCodeFinder;
 
 public class Parameters extends BaseParameters {
 
 	public boolean useCodeFinder;
 	public InlineCodeFinder codeFinder;
 
 	public Parameters () {
 		codeFinder = new InlineCodeFinder();
 		reset();
 		toString(); // fill the list
 	}
 	
 	public void reset () {
 		useCodeFinder = true;
 		codeFinder.reset();
 		codeFinder.setSample("%s, %d, {1}, \\n, \\r, \\t, {{var}} etc.");
 		codeFinder.setUseAllRulesWhenTesting(true);
		// Default in-line codes: special escaped-chars and printf-style variable
		codeFinder.addRule("%(([-0+#]?)[-0+#]?)((\\d\\$)?)(([\\d\\*]*)(\\.[\\d\\*]*)?)[dioxXucsfeEgGpnYyBbHhSMm]");
 		codeFinder.addRule("(\\\\r\\\\n)|\\\\a|\\\\b|\\\\f|\\\\n|\\\\r|\\\\t|\\\\v");
 		codeFinder.addRule("\\{\\{\\w.*?\\}\\}");
 	}
 
 	@Override
 	public String toString () {
 		buffer.reset();
 		buffer.setBoolean("useCodeFinder", useCodeFinder);
 		buffer.setGroup("codeFinderRules", codeFinder.toString());
 		return buffer.toString();
 	}
 	
 	public void fromString (String data) {
 		reset();
 		buffer.fromString(data);
 		useCodeFinder = buffer.getBoolean("useCodeFinder", useCodeFinder);
 		codeFinder.fromString(buffer.getGroup("codeFinderRules", ""));
 	}
 
 }
