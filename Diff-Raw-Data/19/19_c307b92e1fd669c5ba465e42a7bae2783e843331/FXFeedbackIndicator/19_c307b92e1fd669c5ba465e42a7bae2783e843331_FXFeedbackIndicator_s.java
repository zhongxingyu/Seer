 /*
  * $Id$ $Revision$ $Date$
  * 
  * ==============================================================================
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package wicket.contrib.markup.html.form.validation;
 
 
 import wicket.Component;
 import wicket.contrib.markup.html.tooltip.Tooltip;
 import wicket.markup.html.form.validation.FormComponentFeedbackIndicator;
 import wicket.markup.html.image.Image;
import wicket.model.Model;
 
 /**
  * Feedback indicator that adds a small image when validation fails. Also
  * creates an FXFeedbackTooltip connected to the image that shows the
  * errormessages for connected formcomponent.
  * 
  * @author Marco van de Haar
  * @author Ruud Booltink
  * 
  */
 public class FXFeedbackIndicator extends FormComponentFeedbackIndicator
 {
 	private Tooltip feedbackTooltip;
 	private Component toCheck;
 
 	/**
 	 * Construct
 	 * 
 	 * @param id
 	 *            Component ID
 	 */
 	public FXFeedbackIndicator(String id)
 	{
 		super(id);
 		initIndicator();
 		feedbackTooltip = new Tooltip("feedbacktooltip", new FXFeedbackTooltip(this, 20, 30));
 		add(feedbackTooltip);
 
 	}
 
 
 	/**
 	 * method to innitialize Indicator. Necessary here to load static Image.
 	 * subclasses can overwrite.
 	 */
 	protected void initIndicator()
 	{
		Image i = new Image("image", new Model("alerticon.gif"));
		add(i);
 	}
 
 	/**
 	 * @see wicket.markup.html.form.validation.FormComponentFeedbackIndicator#setIndicatorFor(wicket.Component)
 	 */
 	public void setIndicatorFor(Component component)
 	{
 		super.setIndicatorFor(component);
 		((FXFeedbackTooltip)feedbackTooltip.getTooltipPanel()).setComponentToCheck(component);
 	}
 }
