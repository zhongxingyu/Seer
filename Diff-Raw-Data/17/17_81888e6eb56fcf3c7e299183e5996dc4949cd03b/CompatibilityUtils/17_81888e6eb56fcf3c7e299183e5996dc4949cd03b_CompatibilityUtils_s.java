 package org.jtrim.swing.access;
 
 import java.awt.Component;
 import java.util.concurrent.TimeUnit;
 import javax.swing.JPanel;
 import org.jtrim.access.AccessChangeAction;
 import org.jtrim.property.BoolPropertyListener;
 import org.jtrim.property.swing.DelayedGlassPane;
 import org.jtrim.property.swing.GlassPaneFactory;
 
 /**
  *
  * @author Kelemen Attila
  */
 final class CompatibilityUtils {
     public static BoolPropertyListener toBoolPropertyListener(final AccessChangeAction listener) {
         return new BoolPropertyListener() {
             @Override
             public void onChangeValue(boolean newValue) {
                 listener.onChangeAccess(newValue);
             }
         };
     }
 
     public static DecoratorPanelFactory toDecoratorFactory(final GlassPaneFactory factory) {
         return new DecoratorPanelFactory() {
             @Override
             public JPanel createPanel(Component decorated) {
                 return factory.createGlassPane();
             }
         };
     }
 
     public static DelayedDecorator toDelayedDecorator(DelayedGlassPane glassPanes) {
         return new DelayedDecorator(
                 toDecoratorFactory(glassPanes.getImmediateGlassPane()),
                 toDecoratorFactory(glassPanes.getMainGlassPane()),
                 glassPanes.getGlassPanePatience(TimeUnit.NANOSECONDS),
                 TimeUnit.NANOSECONDS);
     }
 }
