 /*
  * $Id: CustomLayoutAjaxAutocompleteTextField.java 648 2006-04-03 02:22:11 -0700
  * (Mon, 03 Apr 2006) joco01 $ $Revision: 1420 $ $Date: 2006-04-03 02:22:11 -0700
  * (Mon, 03 Apr 2006) $
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
 
 import org.apache.wicket.PageParameters;
 
 /**
  * Autocomplete text field that allows for customized layout of autocomplete
  * entries. A user defined {@link CustomLayoutAutocompleteResultsPageContribution} 
  * is used to display the autocomplete results.
  *
  * @author <a href="mailto:wireframe6464@users.sourceforge.net">Ryan Sonnek</a>
  */
 public class CustomLayoutAjaxAutocompleteTextField extends AutocompleteTextFieldSupport
 {
 	private static final long serialVersionUID = 1L;
 	private CharSequence url;
 
 	public CustomLayoutAjaxAutocompleteTextField(String id,
 			Class page)
 	{
 		super(id);
 
 		PageParameters parameters = new PageParameters();
 		parameters.put("fieldName", this.getMarkupId());
 		this.url = urlFor(null, page, parameters);
 	}
 
 	protected String getAutocompleteType()
 	{
 		return "Ajax.Autocompleter";
 	}
 
 	protected String getThirdAutocompleteArgument()
 	{
		return "'" + url + "'";
 	}
 }
