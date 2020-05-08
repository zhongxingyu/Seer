 package org.mule.galaxy.web.client.ui.panel;
 
 import java.util.List;
 
 import com.google.gwt.user.client.Timer;
 
 /**
  *
  * Base implementation for refreshable component.
  *
  */
 public abstract class AbstractRefreshableComponent extends AbstractShowableContainer {
 
     private final Timer refreshTimer = new Timer() {
         @Override
         public void run() {
             refresh();
         }
     };
     private final int refreshPeriodMillis;
     private static final int DEFAULT_REFRESH_PERIOD_MILLIS = 10000;
 
     public AbstractRefreshableComponent() {
         this(AbstractRefreshableComponent.DEFAULT_REFRESH_PERIOD_MILLIS);
     }
 
     public AbstractRefreshableComponent(final int refreshPeriodMillis) {
         this.refreshPeriodMillis = refreshPeriodMillis;
     }
 
     protected abstract void refresh();
 
     public final Timer getTimer() {
         return this.refreshTimer;
     }
 
     @Override
     public void showPage(final List<String> params) {
         super.showPage(params);
 
         this.refreshTimer.scheduleRepeating(this.refreshPeriodMillis);
     }
 
     @Override
     public void hidePage() {
         super.hidePage();
 
         this.refreshTimer.cancel();
     }
 
 }
