 package com.redshape.ui;
 
 import com.redshape.ui.events.EventType;
 import com.redshape.ui.events.AppEvent;
 import com.redshape.ui.annotations.Action;
 import com.redshape.ui.events.UIEvents;
 import com.redshape.ui.utils.UIConstants;
 import com.redshape.ui.utils.UIRegistry;
 
 import java.awt.Container;
 import java.lang.reflect.Method;
 
 import java.util.HashSet;
 import java.util.Set;
 import org.apache.log4j.Logger;
 
 /**
  * Created by IntelliJ IDEA.
  * User: nikelin
  * Date: 07.01.11
  * Time: 2:54
  * To change this template use File | Settings | File Templates.
  */
 public abstract class AbstractController implements IController {
     private static final Logger log = Logger.getLogger( AbstractController.class );
 
     private Set<EventType> events = new HashSet<EventType>();
     private Set<IController> childs = new HashSet<IController>();
     private Set<IView> initialized = new HashSet<IView>();
 
     private IView activeView;
 
     protected void registerEvent( EventType type ) {
         this.events.add(type);
     }
 
     @Override
     public Set<EventType> getRegisteredEvents() {
         return this.events;
     }
 
     @Override
     public void addChild( IController controller ) {
         this.childs.add( controller );
     }
 
     @Override
     public void forwardToView( IView view, AppEvent event ) {
     	this.forwardToView( view, event, true );
     }
     
     @Override
     public void forwardToView( IView view, AppEvent event, boolean unload ) {
     	if ( this.activeView != null && unload ) {
             this.activeView.unload( UIRegistry.<Container>get( UIConstants.Area.CENTER ) );
         }
 
         if ( !this.initialized.contains(view) ) {
             view.init();
             this.initialized.add(view);
         }
 
         view.handle(event);
         
         view.render( UIRegistry.<Container>get( UIConstants.Area.CENTER ) );
         
         Dispatcher.get().forwardEvent( UIEvents.Core.Repaint );
 
         this.activeView = view;
     }
 
     @Override
     public void unload() {
         if ( this.getActiveView() != null ) {
             this.getActiveView().unload( UIRegistry.<Container>get( UIConstants.Area.CENTER ) );
         }
 
         Dispatcher.get().forwardEvent( UIEvents.Core.Repaint );
     }
 
     protected IView getActiveView() {
         return this.activeView;
     }
 
     @Override
     public void handle( AppEvent event ) {
         for ( Method method : this.getClass().getMethods() ) {
             Action annotation = method.getAnnotation( Action.class );
             if ( annotation == null ) {
                 continue;
             }
 
             if ( annotation.eventType() != null &&
                     event.getType().getCode().equals( annotation.eventType() ) ) {
                 try {
                     method.invoke( this, event );
                    break;
                 } catch ( Throwable e ) {
                     log.error( e.getMessage(), e );
                     break;
                 }
             }
         }
     }
 
 }
