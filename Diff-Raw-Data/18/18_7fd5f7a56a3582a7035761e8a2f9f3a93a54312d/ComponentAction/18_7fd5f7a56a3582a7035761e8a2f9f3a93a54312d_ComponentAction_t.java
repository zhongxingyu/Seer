 package com.redshape.ui.actions;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 
 import javax.swing.AbstractAction;
 
 import com.redshape.ui.Dispatcher;
 import com.redshape.ui.components.IComponent;
 import com.redshape.ui.components.events.ComponentEvents;
 import com.redshape.ui.events.AppEvent;
 import com.redshape.ui.events.EventType;
 import com.redshape.ui.events.IEventHandler;
 
 public class ComponentAction extends AbstractAction {
 	private static final long serialVersionUID = 7695946569256843381L;
 	private IComponent component;
 	private IEventHandler listener;
 	private EventType event;
 	private boolean contextChange;
 	
 	public ComponentAction( String name, IComponent component ) {
		this( name, component, true );
	}
	
	public ComponentAction( String name, IComponent component, boolean doContextChange ) {
 		this( name, component, (EventType) null);
		
		this.doContextChange(doContextChange);
 	}
 	
 	public ComponentAction( String name, IComponent component, EventType event ) {
 		super( name );
 	
 		this.init();
 		
 		this.event = event;
 		this.component = component;
 	}
 	
 	public ComponentAction( String name, IComponent component, IEventHandler listener ) {
 		super( name );
 	
 		this.init();
 		
 		this.listener = listener;
 		this.component = component;
 	}
 	
 	protected void init() {
 		this.doContextChange(true);
 	}
 	
 	public void doContextChange( boolean value ) {
 		this.contextChange = value;
 	}
 	
 	public boolean doContextChange() {
 		return this.contextChange;
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if ( this.listener != null ) {
 			this.listener.handle( new AppEvent( ComponentEvents.ActionPerformed, e, this.component ) );
 		} else {
 			Dispatcher.get().forwardEvent( new AppEvent( this.event ), this.doContextChange() );
 		}
 	}
 
 }
