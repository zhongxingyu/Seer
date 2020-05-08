 package org.vaadin.highlighter;
 
 import org.vaadin.highlighter.client.state.ComponentHighlighterState;
 
 import com.vaadin.server.AbstractClientConnector;
 import com.vaadin.server.AbstractExtension;
 import com.vaadin.server.VaadinService;
 
 /**
  * <p>
  * This extension adds an eye-catching label to an extended component on the
  * client-side that displays the component's fully-qualified class name by
  * default. This label will only be active when the application is running in
  * Vaadin's debug mode. In production mode, the client-side component will not
  * be touched.
  * </p>
  * <p>
  * The highlighting labels will not be visible right away. They first have to be
  * activated by using Vaadin's <code>debug</code> URL parameter. You can either
  * add <code>?debug</code> to the current URL to make the labels visible. Or you
  * can alternatively use <code>?debug=quiet</code> to simultaneously hide
  * Vaadin's debug console at the same time.
  * </p>
  * 
  * @author Roland Krüger
  * 
  */
 public class ComponentHighlighterExtension extends AbstractExtension {
 
   public ComponentHighlighterExtension() {
     super();
   }
 
   /**
    * Creates a new extension object and immediately extends the specified
    * component. The component will only be extended, when the application is
    * running in Vaadin's debug mode.
    * 
    * @param target
    *          Connector to be extended. This can be any connector provided that
    *          the corresponding widget will be rendered on the client-side in
    *          some sort of HTML container, such as a DIV. That is because the
    *          highlighting label's span-element will be added as the first child
    *          of that container. If <code>null</code> is given, nothing will be
    *          extended and no error will be thrown. This will have the same
    *          effect as calling the default constructor.
    */
   public ComponentHighlighterExtension(AbstractClientConnector target) {
     super();
     if (target != null) {
       extend(target);
     }
   }
 
   /**
    * <p>
    * The given connector will only be extended with the highlighting label, if
    * the application is currently being run in debug mode. This operation will
    * do nothing if running in production mode. More specifically, the
    * client-side widget of the extended connector will not be touched when
    * running in production mode.
    * </p>
    * <p>
    * The extended component will display its fully qualified class name in the
    * highlighting label by default. This can be adjusted with method
    * {@link #setComponentDebugLabel(String)}.
    * </p>
    */
   @Override
   public void extend(AbstractClientConnector target) {
     if (VaadinService.getCurrent().getDeploymentConfiguration().isProductionMode()) {
       return;
     }
     super.extend(target);
     setComponentDebugLabel(target.getClass().getCanonicalName());
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   protected ComponentHighlighterState getState() {
     return (ComponentHighlighterState) super.getState();
   }
 
   /**
    * Sets the text which will be displayed by the highlighting label. By
    * default, this label shows the fully qualified class name of the extended
    * component.
    * 
    * @param label
    *          the new text for the highlighting label
    */
   public void setComponentDebugLabel(String label) {
     getState().debugLabel = label;
   }
 }
