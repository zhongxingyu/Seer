 package org.iplantc.de.client.views.windows;
 
 import org.iplantc.core.uicommons.client.models.WindowState;
 import org.iplantc.core.uicommons.client.views.IsMinimizable;
 import org.iplantc.de.client.utils.DEWindowManager;
 import org.iplantc.de.client.views.windows.configs.WindowConfig;
 
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.sencha.gxt.core.client.Style.AnchorAlignment;
 import com.sencha.gxt.core.client.util.Point;
 import com.sencha.gxt.widget.core.client.Component;
 import com.sencha.gxt.widget.core.client.Window;
 import com.sencha.gxt.widget.core.client.event.ActivateEvent.HasActivateHandlers;
 import com.sencha.gxt.widget.core.client.event.DeactivateEvent.HasDeactivateHandlers;
 import com.sencha.gxt.widget.core.client.event.HideEvent.HasHideHandlers;
 import com.sencha.gxt.widget.core.client.event.MinimizeEvent.HasMinimizeHandlers;
 import com.sencha.gxt.widget.core.client.event.ShowEvent.HasShowHandlers;
 
 /**
  * This interface is intended to be used by the {@link DEWindowManager} for all primary iPlant windows.
  * 
  * FIXME Rename this file to "IPlantWindow" and rename "IPlantWindow" -> "IPlantWindowImpl"
  * 
  * @author jstroot
  * 
  */
 public interface IPlantWindowInterface extends HasActivateHandlers<Window>,
  HasDeactivateHandlers<Window>, HasMinimizeHandlers, HasHideHandlers, HasShowHandlers, IsWidget, IsMinimizable {
 
     /**
      * @see Component#getStateId()
      */
     String getStateId();
 
     /**
      * @see Component#setStateId(String)
      */
     void setStateId(String stateId);
 
     void setPagePosition(int new_x, int new_y);
 
     void show();
     
     <C extends WindowConfig> void update(C config);
 
     void toFront();
 
     Point getPosition3(boolean b);
 
     WindowState getWindowState();
     
     boolean isVisible();
 
    boolean isResizable();

     boolean isMaximized();
 
     void setMaximized(boolean maximized);
 
     void setMinimized(boolean min);
 
     void setTitle(String wintitle);
 
     String getTitle();
 
     void setPixelSize(int width, int height);
 
     void setPosition(int left, int top);
 
     int getHeaderOffSetHeight();
 
     void alignTo(Element e, AnchorAlignment align, int[] offsets);
 
     void hide();
 
     /**
      * Takes a Point representing a potential position for this window and returns an adjusted position
      * that accounts for this window's width and height.
      * 
      * @param position A Point representing a potential position for this window.
      * @return An adjusted position that accounts for this window's width and height.
      */
     Point adjustPositionForView(Point position);
 }
