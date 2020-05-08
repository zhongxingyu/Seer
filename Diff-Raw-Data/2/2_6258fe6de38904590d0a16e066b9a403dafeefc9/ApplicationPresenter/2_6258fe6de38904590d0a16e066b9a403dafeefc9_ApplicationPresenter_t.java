 package net.premereur.mvp.example.swing.application;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JComponent;
 import javax.swing.Timer;
 
 import net.premereur.mvp.core.guice.BasePresenter;
 import net.premereur.mvp.example.support.ClickHandler;
 import net.premereur.mvp.example.swing.categorymgt.CategoryMgtBus;
 import net.premereur.mvp.example.swing.productmgt.ProductMgtBus;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 /**
  * Presenter for the main application window.
  * 
  * @author gpremer
  * 
  */
 @Singleton
 public class ApplicationPresenter extends BasePresenter<ApplicationFrame, ApplicationBus> {

     private final CategoryMgtBus categoryMgtBus;
 
     @Inject
     public ApplicationPresenter(final ApplicationBus eventBus, final CategoryMgtBus catMgtBus, final ApplicationFrame view) {
         super(eventBus, view);
         this.categoryMgtBus = catMgtBus;
     }
 
     /**
      * Handles application start events.
      */
     public final void onApplicationStarted() {
         ApplicationFrame view = getView();
 
         getEventBus(CategoryMgtBus.class).categoryMgtActivated();
         view.pack();
         view.setVisible(true);
         view.setExitListener(getExitClickHandler());
         view.setCategoryListener(getCategoryClickHandler());
         view.setProductListener(getProductClickHandler());
     }
 
     /**
      * Handles the set left component event.
      * 
      * @param component the component to attach
      */
     public final void onSetLeftComponent(final JComponent component) {
         getView().setLeftComponent(component);
     }
 
     /**
      * Clears the application frame.
      */
     public final void onClearScreen() {
         getView().clearLeftComponent();
         getView().clearCentralComponent();
     }
 
     /**
      * Handles the set central component event.
      * 
      * @param component the component to attach
      */
     public final void onSetCenterComponent(final JComponent component) {
         getView().setCentralComponent(component);
     }
 
     /**
      * Handles setting the feedback message.
      * 
      * @param text the text of the message
      */
     public final void onSetFeedback(final String text) {
         getView().setFeedback(text);
         Timer timer = new Timer(3000, new ActionListener() { // NOCS MAGIC 0
                     @Override
                     public void actionPerformed(final ActionEvent e) {
                         getView().setFeedback("");
 
                     }
                 });
         timer.setRepeats(false);
         timer.start();
     }
 
     public final ClickHandler getExitClickHandler() {
         return new ClickHandler() {
             @Override
             public void click() {
                 System.exit(0);
             }
         };
     }
 
     public final ClickHandler getCategoryClickHandler() {
         return new ClickHandler() {
             @Override
             public void click() {
                 categoryMgtBus.categoryMgtActivated();
             }
         };
     }
 
     public final ClickHandler getProductClickHandler() {
         return new ClickHandler() {
             @Override
             public void click() {
                 getEventBus(ProductMgtBus.class).productMgtActivated(); // avoids having to inject bus
             }
         };
     }
 }
