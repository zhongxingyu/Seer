 /*
  * $Id: CustomLayoutAjaxAutocompleteTextField.java 648 2006-04-03 02:22:11 -0700
  * (Mon, 03 Apr 2006) joco01 $ $Revision$ $Date: 2006-04-03 02:22:11 -0700
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
 package wicket.contrib.scriptaculous.autocomplete;
 
 import wicket.MarkupContainer;
 import wicket.Page;
 import wicket.PageParameters;
 import wicket.contrib.scriptaculous.JavascriptBuilder;
 import wicket.markup.html.internal.HtmlHeaderContainer;
 
 /**
  * Autocomplete text field that allows for customized layout of autocomplete
  * entries. A user defined <code>PageContribution</code> is used to display
  * the autocomplete information.
  *
  * @author <a href="mailto:wireframe6464@users.sourceforge.net">Ryan Sonnek</a>
  */
 public class CustomLayoutAjaxAutocompleteTextField<T> extends AutocompleteTextFieldSupport<T>
 {
 	private static final long serialVersionUID = 1L;
 	private final Class<? extends Page> page;
 
 	public CustomLayoutAjaxAutocompleteTextField(MarkupContainer parent, String id,
 			Class<? extends Page> page)
 	{
 		super(parent, id);
 		this.page = page;
 	}
 
 	public void renderHead(HtmlHeaderContainer container)
 	{
 		super.renderHead(container);
 
 		PageParameters parameters = new PageParameters();
		parameters.put("fieldName", this.getId());
 		CharSequence url = urlFor(null, page, parameters);
 
 		JavascriptBuilder builder = new JavascriptBuilder();
 		builder.addLine("new Ajax.Autocompleter(");
 		builder.addLine("  '" + getMarkupId() + "', ");
 		builder.addLine("  '" + getAutocompleteId() + "', ");
 		builder.addLine("  '" + url + "', {} );");
 	}
 }
