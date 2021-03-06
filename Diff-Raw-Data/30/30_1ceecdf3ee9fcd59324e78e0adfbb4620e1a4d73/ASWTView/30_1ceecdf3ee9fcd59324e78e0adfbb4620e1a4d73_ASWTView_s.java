 package org.caleydo.core.view.swt;
 
 import org.caleydo.core.manager.event.AEvent;
 import org.caleydo.core.manager.event.AEventListener;
 import org.caleydo.core.manager.event.IListenerOwner;
 import org.caleydo.core.manager.id.EManagedObjectType;
 import org.caleydo.core.view.AView;
 import org.caleydo.core.view.swt.widget.SWTNativeWidget;
 import org.eclipse.swt.widgets.Composite;
 
 public abstract class ASWTView
 	extends AView
 	implements ISWTView, IListenerOwner {
 
 	/**
 	 * Constructor.
 	 */
 	public ASWTView(int iParentContainerID, String sLabel, int iViewID) {
 		super(iParentContainerID, sLabel, iViewID);
 	}
 
 	@Override
 	public abstract void drawView();
 
 	@Override
 	public abstract void initViewSWTComposite(Composite parentComposite);
 
 	@Override
 	public final void initViewRCP(final Composite parentComposite) {
 		this.parentComposite = parentComposite;
 		initViewSWTComposite(parentComposite);
 	}
 
 	@Override
 	public void initView() {
 		/**
 		 * Method uses the parent container ID to retrieve the GUI widget by calling the createWidget method
 		 * from the SWT GUI Manager. formally this was the method: retrieveGUIContainer()
 		 */
 		SWTNativeWidget sWTNativeWidget =
 			(SWTNativeWidget) generalManager.getSWTGUIManager().createWidget(
				EManagedObjectType.GUI_SWT_NATIVE_WIDGET, iParentContainerId);
 
 		parentComposite = sWTNativeWidget.getSWTWidget();
 
 		initViewSWTComposite(parentComposite);
 	}
 
 	@Override
 	public synchronized void queueEvent(final AEventListener<? extends IListenerOwner> listener,
 		final AEvent event) {
 		parentComposite.getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				listener.handleEvent(event);
 			}
 		});
 	}
 
 	@Override
 	public Composite getComposite() {
 		return parentComposite;
 	}
 	
 	@Override
 	public void registerEventListeners() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void unregisterEventListeners() {
 		// TODO Auto-generated method stub
 
 	}
 }
