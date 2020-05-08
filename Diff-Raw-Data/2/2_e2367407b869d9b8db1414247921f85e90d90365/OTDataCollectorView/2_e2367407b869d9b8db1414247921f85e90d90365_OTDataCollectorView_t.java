 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Created on Apr 5, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.concord.datagraph.state;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Insets;
 
 import javax.swing.JComponent;
 
 import org.concord.datagraph.engine.DataGraphAutoScaler;
 import org.concord.datagraph.ui.DataGraph;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.view.AbstractOTJComponentView;
 import org.concord.framework.otrunk.view.OTDefaultComponentProvider;
 import org.concord.framework.otrunk.view.OTJComponentViewContext;
 import org.concord.framework.otrunk.view.OTJComponentViewContextAware;
 import org.concord.framework.otrunk.view.OTLabbookManager;
 import org.concord.framework.otrunk.view.OTLabbookViewProvider;
 import org.concord.graph.ui.GraphWindow;
 
 /**
  * @author scott
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class OTDataCollectorView extends AbstractOTJComponentView 
 	implements OTJComponentViewContextAware, OTDefaultComponentProvider, OTLabbookViewProvider
 {
     AbstractOTJComponentView view;
     OTDataCollector dataCollector;
     boolean multipleGraphableEnabled = false;
 	protected OTJComponentViewContext jComponentViewContext;
         
     /* (non-Javadoc)
      * @see org.concord.framework.otrunk.view.OTJComponentView#getComponent(boolean)
      */
     public JComponent getComponent(OTObject otObject)
     {
         this.dataCollector = (OTDataCollector)otObject;
         if(dataCollector.getSingleValue()) {
             view = new SingleValueDataView(dataCollector);
         }
         else {
             view = new DataCollectorView(dataCollector, getControllable(), true);
         }
         
         // We need to intialize the view so it can access it services correctly.
         view.setViewContext(viewContext);
         if (view instanceof OTJComponentViewContextAware){
         	((OTJComponentViewContextAware)view).setOTJComponentViewContext(jComponentViewContext);
         }
         
         return view.getComponent(otObject);
     }
 
     /* (non-Javadoc)
      * @see org.concord.framework.otrunk.view.OTJComponentView#viewClosed()
      */
     public void viewClosed()
     {
         if(view != null) {
             view.viewClosed();
         }
     }
 
 	public void setOTJComponentViewContext(OTJComponentViewContext viewContext)
     {
 	    this.jComponentViewContext = viewContext;
     }
 	
 	public boolean getControllable()
 	{
		return dataCollector.getShowControlBar();
 	}
 	
 	public DataCollectorView getDataCollectorView(){
 		if (view instanceof DataCollectorView){
 			return (DataCollectorView)view;
 		} else {
 			return null;
 		}
 	}
 
 	public Component getDefaultComponent()
     {
 		if (view instanceof DataCollectorView)
 			return ((DataCollectorView)view).getDataGraph().getGraph();
 		else
 			return view.getComponent(dataCollector);
     }
 	
 	/**
 	 * For OTLabbookViewProvider. Here we just clone the object
 	 */
 	public OTObject copyObjectForSnapshot(OTObject otObject)
     {
 	    try {
 	        return otObject.getOTObjectService().copyObject(otObject, -1);
         } catch (Exception e) {
 	        // TODO Auto-generated catch block
 	        e.printStackTrace();
         }
         return otObject;
     }
 
 	/**
 	 * For OTLabbookViewProvider.
 	 */
 	public boolean drawtoolNeededForAlbum()
     {
 	    // TODO Auto-generated method stub
 	    return false;
     }
 
 	/**
 	 * For OTLabbookViewProvider. This returns the regular view with the graph set to not
 	 * be controllable
 	 */
 	public JComponent getLabbookView(OTObject otObject)
     {
 		((OTDataCollector)otObject).getSource().setControllable(false);
 		
 		// we have to call getComponent to make sure the dataGraph gets created
 		getComponent(otObject);
 		
 	     //   view.getComponent(otObject);
 			if (view instanceof DataCollectorView){
 				DataGraph graph = ((DataCollectorView)view).getDataGraph(true, false);
 				graph.setAutoFitMode(DataGraph.AUTO_SCALE_MODE);
 				final DataGraphAutoScaler autoscaler = graph.getAutoScaler();
 				autoscaler.setAutoScaleX(true);
 				autoscaler.setAutoScaleY(true);
 				
 				return graph;
 			} else
 				return view.getComponent(dataCollector);
     }
 	
 	/**
 	 * For OTLabbookViewProvider. This returns a scaled-down graph without the toolbars and
 	 * with a smaller title.
 	 */
 	public JComponent getThumbnailView(OTObject otObject, int height)
     {
 		((OTDataCollector)otObject).getSource().setControllable(false);
 		
 		getComponent(otObject);
 		
      //   view.getComponent(otObject);
 		if (view instanceof DataCollectorView){
 			DataGraph graph = ((DataCollectorView)view).getDataGraph(false, false);
 			graph.setScale(2, 2);
 			graph.setAutoFitMode(DataGraph.AUTO_SCALE_MODE);
 			graph.setInsets(new Insets(0,8,8,0));
 			graph.setTitle(graph.getTitle(), 9);
 			final DataGraphAutoScaler autoscaler = graph.getAutoScaler();
 			autoscaler.setAutoScaleX(true);
 			autoscaler.setAutoScaleY(true);
 			EventQueue.invokeLater(new Runnable(){
 
 				public void run()
                 {
 					EventQueue.invokeLater(new Runnable(){
 
 						public void run()
 		                {
 							autoscaler.handleUpdate();
 		                }});
                 }});
 			
 			graph.setPreferredSize(new Dimension((int) (height*1.3), height));
 			return graph;
 		} else
 			return view.getComponent(dataCollector);
 			
     }
 }
