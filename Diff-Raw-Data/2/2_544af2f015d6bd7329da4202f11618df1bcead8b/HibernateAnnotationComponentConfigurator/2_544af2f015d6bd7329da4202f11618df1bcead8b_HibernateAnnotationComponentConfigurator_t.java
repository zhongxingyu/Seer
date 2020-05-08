 package org.wicketstuff.hibernate.annotation;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.Component;
 import org.apache.wicket.application.IComponentOnBeforeRenderListener;
 import org.apache.wicket.behavior.AbstractBehavior;
 import org.apache.wicket.markup.html.form.FormComponent;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.validation.validator.StringValidator;
 import org.hibernate.validator.Length;
 import org.hibernate.validator.NotNull;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Configure a wicket component based on hibernate annotations.
  * <p>
  * Inspects the Model of a FormComponent and configures the component according
  * to the declared Hibernate Annotations used on the model object.  This means the
  * Component's Model <em>must</em> be known when {@link #configure(Component) configuring}
  * a Component.
  * </p>
  *
  * <p>
  * This object can be used as a behavior to configure a single component.
  * NOTE: this object is <em>stateless</em>, and the same instance can be reused to
  * configure multiple components.
  * </p>
  * <pre>
  * public class MyWebPage extends WebPage {
  *   public MyWebPage() {
  *     TextField name = new TextField("id", new PropertyModel(user, "name");
  *     name.addBehavior(new HibernateAnnotationComponentConfigurator());
  *
  *     add(name);
  *   }
  * }
  * </pre>
  *
  * <p>
  * Can also use this object as a component listener that will automatically configure <em>all</em>
  * form components based on hibernate annotations. This helps ensure that an entire application
  * respects  annotations without adding custom validators or behaviors to each form component.
  * </p>
  * <pre>
  * public class MyApplication extends WebApplication {
  *   public void init() {
 *     addComponentOnBeforeRenderListener(new HibernateAnnotationComponentConfigurator());
  *   }
  * }
  * </pre>
  *
  */
 @SuppressWarnings("serial")
 public class HibernateAnnotationComponentConfigurator extends AbstractBehavior implements IComponentOnBeforeRenderListener {
 	private static final Logger LOGGER = LoggerFactory.getLogger(HibernateAnnotationComponentConfigurator.class);
 
 	@SuppressWarnings("unchecked")
 	private static Map configs = new HashMap() {{
 		put(NotNull.class, new HibernateAnnotationConfig() {
 			public void onAnnotatedComponent(Annotation annotation, FormComponent component) {
 				component.setRequired(true);
 			}
 		});
 		put(Length.class, new HibernateAnnotationConfig() {
 			public void onAnnotatedComponent(Annotation annotation, FormComponent component) {
 				int max = ((Length)annotation).max();
 				component.add(new AttributeModifier("maxlength", new Model(Integer.toString(max))));
 				component.add(StringValidator.maximumLength(max));
 			}
 		});
 	}};
 
 	@Override
 	public void bind(Component component) {
 		super.bind(component);
 
 		configure(component);
 	}
 
 	public void onBeforeRender(Component component) {
 		configure(component);
 	}
 
 	void configure(Component component) {
 		if (!isApplicableFor(component)) {
 			return;
 		}
 		FormComponent formComponent = (FormComponent)component;
 		PropertyModel propertyModel = (PropertyModel) component.getModel();
 		String fieldName = propertyModel.getPropertyExpression();
 		Class type = propertyModel.getTarget().getClass();
 		try {
 			Field field = type.getDeclaredField(fieldName);
 			Annotation[] annotations = field.getAnnotations();
 			if (null != annotations) {
 				for (int y = 0; y < annotations.length; y++) {
 					Annotation annotation = annotations[y];
 					Class<? extends Annotation> annotationType = annotation.annotationType();
 					HibernateAnnotationConfig config = (HibernateAnnotationConfig) configs.get(annotationType);
 					if (null != config) {
 						config.onAnnotatedComponent(annotation, formComponent);
 					}
 				}
 			}
 		} catch (Exception e) {
 			throw new RuntimeException("Error configuring component: " + component, e);
 		}
 	}
 
 	private boolean isApplicableFor(Component component) {
 		if (!(component instanceof FormComponent)) {
 			return false;
 		}
 		IModel model = component.getModel();
 		if (null == model || !(model instanceof PropertyModel)) {
 			LOGGER.info("No PropertyModel available for configuring Component: " + component);
 			return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * simple interface to abstract performing work for a specific annotation.
 	 */
 	private static interface HibernateAnnotationConfig {
 		void onAnnotatedComponent(Annotation annotation, FormComponent component);
 	}
 }
