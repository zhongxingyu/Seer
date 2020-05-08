 /*
  *    Copyright 2006 Wicket Stuff
  * 
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package wicket.extensions.markup.html.datepicker;
 
 import java.util.Date;
 import java.util.Locale;
 import java.util.Map;
 
 import wicket.AttributeModifier;
 import wicket.Component;
 import wicket.markup.html.WebMarkupContainer;
 import wicket.model.IModel;
 import wicket.model.Model;
import wicket.util.convert.Converter;
 import wicket.util.convert.IConverter;
import wicket.util.convert.ITypeConverter;
 import wicket.util.convert.converters.DateConverter;
 
 /**
  * Popup mode of the DatePicker
  * 
  * <p>
  * Link your datepicker to a textfield like this:
  * </p>
  * <p>
  * (Java)
  * 
  * <pre>
  * TextField dateField = new TextField(&quot;dateField&quot;, Date.class);
  * add(dateField);
  * add(new PopupDatePicker(&quot;dateFieldPicker&quot;, dateField));
  * </pre>
  * 
  * (html)
  * 
  * <pre>
  * &lt;input type=&quot;text&quot; wicket:id=&quot;dateField&quot; size=&quot;10&quot; /&gt;
  * &lt;span wicket:id=&quot;dateFieldPicker&quot; /&gt;
  * </pre>
  * 
  * </p>
  * <p>
  * Your target doesn't have to be a text field however, attach to any tag that
  * is supported by JSCalendar.
  * </p>
  * 
  * @author Frank Bille Jensen
  */
 public class PopupDatePicker extends DatePicker {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Attribute modifier that modifies/ adds an attribute with value of the
 	 * given component's path.
 	 */
 	private final static class PathAttributeModifier extends AttributeModifier
 	{
 		private static final long serialVersionUID = 1L;
 
 		/**
 		 * Construct.
 		 * 
 		 * @param attribute
 		 *            the attribute to modify
 		 * @param pathProvider
 		 *            the component that provides the path
 		 */
 		public PathAttributeModifier(String attribute, final Component pathProvider)
 		{
 			super(attribute, true, new Model()
 			{
 				private static final long serialVersionUID = 1L;
 
 				public Object getObject(Component component)
 				{
 					if (pathProvider.getOutputMarkupId())
 					{
 						return pathProvider.getMarkupId();
 					}
 					// do this lazily, so we know for sure we have the whole
 					// path including the page etc.
 					return pathProvider.getPath();
 				}
 			});
 		}
 	}
 	
 	/**
 	 * Button that triggers the popup.
 	 */
 	private final static class TriggerButton extends WebMarkupContainer
 	{
 		private static final long serialVersionUID = 1L;
 
 		/**
 		 * Construct.
 		 * 
 		 * @param id
 		 *            component id
 		 * @param resourceReference
 		 *            button icon reference
 		 */
 		public TriggerButton(final String id, final wicket.ResourceReference resourceReference)
 		{
 			super(id);
 			add(new PathAttributeModifier("id", this));
 			IModel srcReplacement = new Model()
 			{
 				private static final long serialVersionUID = 1L;
 
 				public Object getObject(Component component)
 				{
 					return urlFor(resourceReference);
 				};
 			};
 			add(new AttributeModifier("src", true, srcReplacement));
 		}
 	}
 
 	/** the receiving component. */
 	private final Component target;
 
 	/** the button that triggers the popup. */
 	private TriggerButton triggerButton;
 
 	/**
 	 * Construct with a default button and style.
 	 * 
 	 * @param id
 	 *            the component id
 	 * @param target
 	 *            the receiving component
 	 */
 	public PopupDatePicker(String id, Component target)
 	{
 		this(id, target, new DatePickerSettings());
 	}
 
 	/**
 	 * Construct with a default button and style.
 	 * 
 	 * @param id
 	 *            the component id
 	 * @param label
 	 *            the label for target component.
 	 * @param target
 	 *            the receiving component
 	 */
 	public PopupDatePicker(String id, Component label, Component target)
 	{
 		this(id, label, target, new DatePickerSettings());
 	}
 
 	/**
 	 * Construct.
 	 * 
 	 * @param id
 	 *            the component id
 	 * @param target
 	 *            the receiving component
 	 * @param settings
 	 *            datepicker properties
 	 */
 	public PopupDatePicker(final String id, final Component target, final DatePickerSettings settings)
 	{
 		this(id, null, target, settings);
 	}
 
 	/**
 	 * Construct.
 	 * 
 	 * @param id
 	 *            the component id
 	 * @param label
 	 *            the label component (may be null)
 	 * @param target
 	 *            the receiving component
 	 * @param settings
 	 *            datepicker properties
 	 */
 	public PopupDatePicker(final String id, final Component label, final Component target,
 			final DatePickerSettings settings)
 	{
 		super(id, settings);
 
 		if (target == null)
 		{
 			throw new IllegalArgumentException("Target must be not null");
 		}
 
 		target.add(new PathAttributeModifier("id", target));
 		this.target = target;
 
 		if (label != null)
 		{
 			label.add(new PathAttributeModifier("for", target));
 		}
 		add(triggerButton = new TriggerButton("trigger", settings.getIcon()));
 	}
 
 	protected void appendSettings(Map settings) {
 		String targetId = target.getOutputMarkupId() ? target.getMarkupId() : target.getPath();
 		settings.put("inputField", "\""+targetId+"\"");
 		settings.put("button", "\""+triggerButton.getPath()+"\"");
 	}
 
 	protected DateConverter getDateConverter() {
		DateConverter dateConverter = null;
		
		// TODO this should be much easier and nicer to do in 2.0
		IConverter converter = target.getConverter();
		if (converter instanceof Converter)
 		{
			ITypeConverter typeConverter = ((Converter)converter).get(Date.class);
			if (typeConverter instanceof DateConverter)
			{
				dateConverter = (DateConverter)typeConverter;
			}
		}
		
		if (dateConverter == null)
		{
			dateConverter = super.getDateConverter();
 		}
		
		return dateConverter;
 	}
 
 	protected Locale getDatePickerLocale() {
 		return target.getLocale();
 	}
 	
 }
