 package com.pedrero.eclihand.controller.security;
 
 import com.vaadin.ui.HasComponents.ComponentAttachEvent;
 import com.vaadin.ui.HasComponents.ComponentAttachListener;
 import com.vaadin.ui.Layout;
 
 public class SecurityComponentAttachListener implements ComponentAttachListener {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -3627695762622287208L;
 
 	private final Layout layout;
 
 	private final SecurityController securityController;
 
 	public SecurityComponentAttachListener(
 			SecurityController securityController, Layout layout) {
 		super();
 		this.layout = layout;
 		this.securityController = securityController;
 	}
 
 	@Override
 	public void componentAttachedToContainer(ComponentAttachEvent event) {
		if (!securityController.canBeShown(event.getAttachedComponent())) {
			layout.removeComponent(event.getAttachedComponent());
 		}
 	}
 
 }
