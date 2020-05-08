 /*
  * $Id: AutocompleteTextField.java 528 2006-01-08 04:14:46 -0800 (Sun, 08 Jan
  * 2006) jdonnerstag $ $Revision: 1546 $ $Date: 2006-01-08 04:14:46 -0800 (Sun,
  * 08 Jan 2006) $
  *
  * ==================================================================== Licensed
  * under the Apache License, Version 2.0 (the "License"); you may not use this
  * file except in compliance with the License. You may obtain a copy of the
  * License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.wicketstuff.scriptaculous.autocomplete;
 
 /**
  * TextField that provides a static list of options to autocomplete.
  *
  * @author <a href="mailto:wireframe6464@users.sourceforge.net">Ryan Sonnek</a>
  */
 public class AutocompleteTextField extends AutocompleteTextFieldSupport
 {
 	private static final long serialVersionUID = 1L;
 	private final String[] results;
 
 	public AutocompleteTextField(String id, String[] results)
 	{
 		super(id);
 
 		this.results = results;
 	}
 
 	private String buildResults()
 	{
 		String result = "[";
 		for (int x = 0; x < results.length; x++) {
 			String value = results[x];
 			result += "'" + value + "'";
 			if (x < results.length - 1)
 			{
 				result += ",";
 			}
 		}
 		result += "]";
 		return result;
 	}
 
 	protected String getAutocompleteType()
 	{
		return "Autocompleter.Local(";
 	}
 
 	protected String getThirdAutocompleteArgument()
 	{
 		return buildResults();
 	}
 }
