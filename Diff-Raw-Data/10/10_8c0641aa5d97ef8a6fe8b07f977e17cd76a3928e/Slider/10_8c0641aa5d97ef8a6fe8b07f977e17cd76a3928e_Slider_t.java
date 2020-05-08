 /*
  * $Id: Slider.java 5132 2006-03-26 02:13:41 -0800 (Sun, 26 Mar 2006)
  * jdonnerstag $ $Revision: 5189 $ $Date: 2006-03-26 02:13:41 -0800 (Sun, 26 Mar
  * 2006) $
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
 package wicket.contrib.markup.html.yui.slider;
 
 import java.util.HashMap;
 import java.util.Map;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 import wicket.AttributeModifier;
import wicket.Component;
 import wicket.MarkupContainer;
 import wicket.behavior.HeaderContributor;
 import wicket.contrib.markup.html.yui.AbstractYuiPanel;
 import wicket.markup.html.IHeaderResponse;
 import wicket.markup.html.WebMarkupContainer;
 import wicket.markup.html.WebPage;
 import wicket.markup.html.form.FormComponent;
 import wicket.markup.html.image.Image;
 import wicket.model.AbstractReadOnlyModel;
 import wicket.model.IModel;
 import wicket.model.Model;
 import wicket.model.PropertyModel;
 import wicket.util.resource.TextTemplateHeaderContributor;
 
 /**
  * Slider component based on the Slider of Yahoo UI Library.
  * 
  * @author Eelco Hillenius
  * @author Joshua Lim
  */
 public class Slider<T> extends AbstractYuiPanel<T> {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * The id of the background element.
 	 */
 	private String backgroundElementId;
 
 	/**
 	 * The id of the image element.
 	 */
 	private String imageElementId;
 
 	/**
 	 * The JavaScript variable name of the slider component.
 	 */
 	private String javaScriptId;
 
	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(Component.class);
 
 	/**
 	 * Construct.
 	 * 
 	 * @param parent
 	 *            The parent
 	 * @param id
 	 *            the component id
 	 * @param element
 	 *            the form component to cooperate with
 	 * @param settings
 	 *            The slider settings
 	 */
 	public Slider(MarkupContainer parent, String id,
 			final FormComponent element, final SliderSettings settings) {
 		this(parent, id, null, element, settings);
 	}
 
 	/**
 	 * Construct.
 	 * 
 	 * @param parent
 	 *            The parent
 	 * @param id
 	 *            the component id
 	 * @param model
 	 *            the model for this component
 	 * @param element
 	 *            the form component to cooperate with
 	 * @param settings
 	 *            The slider settings
 	 */
 	public Slider(MarkupContainer parent, String id, IModel<T> model,
 			final FormComponent element, final SliderSettings settings) {
 		super(parent, id, model);
 
 		add(HeaderContributor.forJavaScript(Slider.class, "slider.js"));
 		add(HeaderContributor.forCss(Slider.class, "css/slider.css"));
 
 		// handle form element
 		if (element != null) {
 			element.add(new AttributeModifier("id", true,
 					new AbstractReadOnlyModel<String>() {
 						private static final long serialVersionUID = 1L;
 
 						@Override
 						public String getObject() {
 							return element.getId();
 						}
 					}));
 		}
 
 		IModel<Map> variablesModel = new AbstractReadOnlyModel<Map>() {
 			private static final long serialVersionUID = 1L;
 
 			/**
 			 * @see wicket.model.AbstractReadOnlyModel#getObject(wicket.Component)
 			 */
 			@Override
 			public Map getObject() {
 				Map<String, CharSequence> variables = new HashMap<String, CharSequence>(
 						7);
 				variables.put("javaScriptId", javaScriptId);
 				variables.put("backGroundElementId", backgroundElementId);
 				variables.put("imageElementId", imageElementId);
 				variables.put("leftUp", settings.getLeftUp());
 				variables.put("rightDown", settings.getRightDown());
 				variables.put("tick", settings.getTick());
 				variables.put("formElementId", element.getId());
 				return variables;
 			}
 		};
 
 		add(TextTemplateHeaderContributor.forJavaScript(Slider.class,
 				"init.js", variablesModel));
 
 		// LEFT Corner Images & Tick Marks
 		Image leftTickImg = new Image(this, "leftTickImg", settings
 				.getLeftTickResource());
 		leftTickImg.add(new AttributeModifier("onclick", true,
 				new AbstractReadOnlyModel<String>() {
 
 					private static final long serialVersionUID = 1L;
 
 					@Override
 					public String getObject() {
 						return javaScriptId + ".setValue(" + javaScriptId
 								+ ".getXValue() - " + settings.getTick() + ");";
 					}
 				}));
 		leftTickImg.setVisible(settings.isShowTicks());
 
 		Image leftCornerImg = new Image(this, "leftCornerImg", settings
 				.getLeftCornerResource());
 		leftCornerImg.add(new AttributeModifier("onclick", true,
 				new AbstractReadOnlyModel<String>() {
 					private static final long serialVersionUID = 1L;
 
 					@Override
 					public String getObject() {
 						return javaScriptId + ".setValue(-"
 								+ settings.getLeftUp() + ")";
 					}
 				}));
 
 		// RIGHT Corner Images & Tick Marks
 		Image rightCornerImg = new Image(this, "rightCornerImg", settings
 				.getRightCornerResource());
 		rightCornerImg.add(new AttributeModifier("onclick", true,
 				new AbstractReadOnlyModel<String>() {
 					private static final long serialVersionUID = 1L;
 
 					@Override
 					public String getObject() {
 						return javaScriptId + ".setValue("
 								+ settings.getRightDown() + ")";
 					}
 				}));
 
 		Image rightTickImg = new Image(this, "rightTickImg", settings
 				.getRightTickResource());
 		rightTickImg.add(new AttributeModifier("onclick", true,
 				new AbstractReadOnlyModel<String>() {
 					private static final long serialVersionUID = 1L;
 
 					@Override
 					public String getObject() {
 						return javaScriptId + ".setValue(" + javaScriptId
 								+ ".getXValue() + " + settings.getTick() + ");";
 					}
 				}));
 		rightTickImg.setVisible(settings.isShowTicks());
 
 		/*
 		 * Background Div
 		 */
 
 		WebMarkupContainer backgroundElement = new WebMarkupContainer(this,
 				"backgroundDiv");
 		backgroundElement.add(new AttributeModifier("id", true,
 				new PropertyModel<CharSequence>(this, "backgroundElementId")));
 		backgroundElement.add(new AttributeModifier("style", true,
 				new Model<String>(settings.getBackground().getStyle())));
 
 		/*
 		 * Element Div and Thumb Div
 		 */
 
 		WebMarkupContainer imageElement = new WebMarkupContainer(
 				backgroundElement, "handleDiv");
 		imageElement.add(new AttributeModifier("id", true,
 				new PropertyModel<CharSequence>(this, "imageElementId")));
 		imageElement.add(new AttributeModifier("style", true,
 				new Model<String>(settings.getHandle().getStyle())));
 
 		WebMarkupContainer thumbElement = new WebMarkupContainer(imageElement,
 				"thumbDiv");
 		thumbElement.add(new AttributeModifier("style", true,
 				new Model<String>(settings.getThumb().getStyle())));
 
 	}
 
 	/**
 	 * Contruct. creates a default Slider.
 	 * 
 	 * @param id
 	 * @param model
 	 * @param leftUp
 	 * @param rightDown
 	 * @param tick
 	 * @param element
 	 */
 
 	public Slider(MarkupContainer parent, String id, IModel<T> model,
 			final int leftUp, final int rightDown, final int tick,
 			final FormComponent element) {
 		this(parent, id, model, element, SliderSettings.getDefault(leftUp,
 				rightDown, tick));
 	}
 
 	/**
 	 * Gets backgroundElementId.
 	 * 
 	 * @return backgroundElementId
 	 */
 	public final String getBackgroundElementId() {
 		return backgroundElementId;
 	}
 
 	/**
 	 * Gets imageElementId.
 	 * 
 	 * @return imageElementId
 	 */
 	public final String getImageElementId() {
 		return imageElementId;
 	}
 
 	@Override
 	public void renderHead(IHeaderResponse response) {
 		((WebPage) getPage()).getBodyContainer().addOnLoadModifier(
 				"init" + javaScriptId + "();", this);
 		super.renderHead(response);
 	}
 
 	/**
 	 * TODO implement
 	 */
 	public void updateModel() {
 		log.info("updateModel");
 	}
 
 	/**
 	 * @see wicket.Component#onAttach()
 	 */
 	@Override
 	protected void onAttach() {
 		super.onAttach();
 
 		// initialize lazily
 		if (backgroundElementId == null) {
 			// assign the markup id
 			String id = getMarkupId();
 			backgroundElementId = id + "Bg";
 			imageElementId = id + "Img";
 			javaScriptId = backgroundElementId + "JS";
 		}
 	}
 }
