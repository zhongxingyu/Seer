 package org.pakhama.vaadin.mvp.view.impl;
 
 import org.pakhama.vaadin.mvp.annotation.field.EventBus;
 import org.pakhama.vaadin.mvp.event.EventScope;
 import org.pakhama.vaadin.mvp.event.IEvent;
 import org.pakhama.vaadin.mvp.event.IEventBus;
 import org.pakhama.vaadin.mvp.view.IView;
 
 import com.vaadin.ui.Component;
 import com.vaadin.ui.VerticalLayout;
 
 public class View extends VerticalLayout implements IView {
 	private static final long serialVersionUID = 5552315123806395809L;
 
 	@EventBus
 	private IEventBus eventBus;
 
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		builder.append(getClass().getSimpleName());
 		builder.append(':');
 		builder.append('{');
 		if (eventBus == null) {
 			builder.append("<null event bus>");
 		} else {
 			builder.append(eventBus.getClass().getName());
 		}
 		builder.append('}');
 
 		return builder.toString();
 	}
 
 	@Override
 	public int dispatch(IEvent event) {
 		if (event == null) {
 			throw new IllegalArgumentException("The event parameter was null.");
 		}
 		
 		event.setSource(this);
 		return this.eventBus.propagate(event, EventScope.PARENT);
 	}
 
 	public void onBind() {
 		// Does nothing by default; intended to be overridden
 		// Overriding this is intended to be optional
 	}
 
 	public void onUnbind() {
 		// Get rid of instance variables
 		this.eventBus = null;
 	}
 
 	@Override
 	public Component getComponent() {
 		return this;
 	}
 
 }
