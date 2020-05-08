 /**
  * 
  */
 package org.concord.otrunk.view;
 
 import java.util.HashMap;
 
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.view.OTJComponentService;
 import org.concord.framework.otrunk.view.OTJComponentView;
 import org.concord.framework.otrunk.view.OTJComponentViewContext;
 import org.concord.framework.otrunk.view.OTJComponentViewContextAware;
 import org.concord.framework.otrunk.view.OTView;
 import org.concord.framework.otrunk.view.OTViewContainer;
 import org.concord.framework.otrunk.view.OTViewContainerAware;
 import org.concord.framework.otrunk.view.OTViewContext;
 import org.concord.framework.otrunk.view.OTViewContextAware;
 import org.concord.framework.otrunk.view.OTViewEntry;
 import org.concord.framework.otrunk.view.OTViewFactory;
 import org.concord.framework.otrunk.view.OTXHTMLView;
 import org.concord.otrunk.view.document.OTDocumentView;
 
 /**
  * @author scott
  *
  */
 public class OTJComponentServiceImpl implements OTJComponentService 
 {
 	OTViewFactory viewFactory;
 	
 	// For now we'll keep these in a regular hashtable we might need to do
 	// some weak referenceing here
 	HashMap objToView = new HashMap();
 	HashMap objToComponent = new HashMap();
 	
 	public OTJComponentServiceImpl(OTViewFactory viewFactory)
 	{
 		this.viewFactory = viewFactory;
 	}
 	
 	public JComponent getComponent(OTObject otObject,
 		OTViewContainer container) 
 	{
         OTJComponentView view = getObjectView(otObject, container);
 
         if(view == null) {
             return new JLabel("No view for object: " + otObject);
         }
 
         return getComponent(otObject, view);
 	}
 	
 	public JComponent getComponent(OTObject otObject, OTJComponentView view)	
 	{
 		JComponent component = view.getComponent(otObject);
 		
 		return component;
 	}
 
 	public OTJComponentView getObjectView(OTObject otObject,
 			OTViewContainer container) 
 	{
 		return getObjectView(otObject, container, null, null);
 	}
 	
 	public OTJComponentView getObjectView(OTObject otObject, OTViewContainer container, 
 	                                      String mode) 
 	{
 		return getObjectView(otObject, container, mode, null);
 	}
 	
 	public OTJComponentView getObjectView(OTObject otObject, OTViewContainer container, 
         String mode, OTViewEntry viewEntry)
     {
     		return getObjectView(otObject, container, mode, viewEntry, null, null);
     }
 
 	/* (non-Javadoc)
      * @see org.concord.framework.otrunk.view.OTJComponentService#getObjectView(org.concord.framework.otrunk.OTObject, org.concord.framework.otrunk.view.OTViewContainer, java.lang.String, org.concord.framework.otrunk.view.OTViewEntry)
      */
     public OTJComponentView getObjectView(OTObject otObject, OTViewContainer container, 
                                           String mode, OTViewEntry viewEntry, OTViewContext passedViewContext,
                                           OTJComponentViewContext passedJComponentViewContext)
     {
     	OTView genericView = null;
     	if(viewEntry != null) {
     		genericView = viewFactory.getView(otObject, viewEntry, mode);
     	} else {
     		genericView = viewFactory.getView(otObject, OTJComponentView.class, mode);
         	if(genericView == null) {
         		genericView = viewFactory.getView(otObject, OTView.class, mode);
         		
         		// check if we can handle translating this to a OTJComponentView
         		// currently only OTXHTMLViews can be translated
         		if(!(genericView instanceof OTXHTMLView)){
         			System.err.println("No OTJComponentView or compatible view for the object");
         			System.err.println("  obj: " + otObject);
         			System.err.println("  mode: " + mode);
         		}
         	}
     	}
 
     	if(genericView == null) {
     		System.err.println("Cannot find view for object");
     		System.err.println("  obj: " + otObject);
     		System.err.println("  mode: " + mode);
     		System.err.println("  viewEntry: " + viewEntry);
     		return null;
     	}
 
     	OTJComponentView view = null;
 
     	if(genericView instanceof OTJComponentView){
     		view = (OTJComponentView) genericView;
     	} else {
     		// FIXME this should abstracted so new translations can
     		// be plugged in for example a SWT translation.
     		if(genericView instanceof OTXHTMLView){
     			// make an OTDocumentView with this as the text
     			// but to maintain the correct lifecycle order this can't
     			// happen until the getComponent is called on the view
     			// so a wrapper view is used which does this on the getComponent method    			
     			OTXHTMLView xhtmlView = (OTXHTMLView) genericView;
 
     			OTXHTMLWrapperDoc wrapperDoc = new OTXHTMLWrapperDoc(xhtmlView, otObject);
     			
     			// we look up a view for the wrapper doc in the default view entries 
     			view = (OTJComponentView) viewFactory.getView(wrapperDoc, OTJComponentView.class, OTViewFactory.NO_VIEW_MODE);
     			if(view == null){
     				System.err.println("No view entry found for OTDocument this is required to use a OTXHTMLView");
     			}   
     			
     			// FIXME by having to cast to this to OTDocumentView we are breaking the abstraction 
     			view = new OTXHTMLWrapperView((OTDocumentView)view, wrapperDoc);    			
     		}
 
     	}
     	
     	
     	if(view == null){
     		// We could not translate the genericView to a OTJComponentView
     		System.err.println("Could not translate genericView to OTJComponentView");
     		System.err.println("  obj: " + otObject);
     		System.err.println("  genericView: " + genericView);
     		System.err.println("  mode: " + mode);
     		System.err.println("  viewEntry: " + viewEntry);
     		return null;
     	}
     	
         if(view instanceof OTViewContainerAware){
         	((OTViewContainerAware)view).setViewContainer(container);
         }
         
         if(view instanceof OTJComponentViewContextAware){
         	if (passedJComponentViewContext == null){
         		((OTJComponentViewContextAware)view).setOTJComponentViewContext(viewContext);
         	} else {
         		((OTJComponentViewContextAware)view).setOTJComponentViewContext(passedJComponentViewContext);
         	}
         }
         
         // This will actually override the viewContext that was set by the view factory.
         if (view instanceof OTViewContextAware){
         	if (passedViewContext != null){
         		((OTViewContextAware)view).setViewContext(passedViewContext);
         	}
         }
         
         objToView.put(otObject, view);
         
         return view;
     }
     
     OTJComponentViewContext viewContext = new OTJComponentViewContext()
     {
 
 		public JComponent getComponentByObject(OTObject obj)
         {
 			return (JComponent)objToComponent.get(obj);
         }
 
 		public OTView getViewByObject(OTObject obj)
         {
 			return (OTView)objToView.get(obj);
         }
 
 		public Object[] getAllObjects()
         {
 	        return  objToView.keySet().toArray();
         }
     	
     };
 
     /**
      * @see org.concord.framework.otrunk.view.OTJComponentService#getJComponentViewContext()
      */
 	public OTJComponentViewContext getJComponentViewContext()
     {
 	    return viewContext;
     }
 	
 	public OTViewFactory getViewFactory(){
 		return viewFactory;
 	}
 
 }
