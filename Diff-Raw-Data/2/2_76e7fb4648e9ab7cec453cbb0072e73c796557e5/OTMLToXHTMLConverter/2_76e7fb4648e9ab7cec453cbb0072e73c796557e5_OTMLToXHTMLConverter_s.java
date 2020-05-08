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
 
 package org.concord.otrunk;
 
 import java.awt.Dimension;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.JComponent;
 import javax.swing.SwingUtilities;
 
 import org.concord.framework.otrunk.OTControllerService;
 import org.concord.framework.otrunk.OTID;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTObjectService;
 import org.concord.framework.otrunk.OTUser;
 import org.concord.framework.otrunk.OTrunk;
 import org.concord.framework.otrunk.view.OTControllerServiceFactory;
 import org.concord.framework.otrunk.view.OTJComponentService;
 import org.concord.framework.otrunk.view.OTJComponentServiceFactory;
 import org.concord.framework.otrunk.view.OTJComponentView;
 import org.concord.framework.otrunk.view.OTPrintDimension;
 import org.concord.framework.otrunk.view.OTView;
 import org.concord.framework.otrunk.view.OTViewContainer;
 import org.concord.framework.otrunk.view.OTViewContext;
 import org.concord.framework.otrunk.view.OTViewEntry;
 import org.concord.framework.otrunk.view.OTViewFactory;
 import org.concord.framework.otrunk.view.OTXHTMLHelper;
 import org.concord.framework.otrunk.view.OTXHTMLView;
 import org.concord.swing.util.ComponentScreenshot;
 
 public class OTMLToXHTMLConverter
     implements Runnable, OTXHTMLHelper
 {
 
 	private OTObject[] topLevelOTObjects;
 	private OTViewEntry [] topLevelViewEntries;
 	
 	private OTViewContainer viewContainer;
 
 	private OTViewFactory viewFactory;
 
 	private int containerDisplayWidth;
 
 	private int containerDisplayHeight;
 
 	private File outputFile;
 
 	OTJComponentService jComponentService;
 	private OTControllerService controllerService;
 
 	public OTMLToXHTMLConverter()
 	{
 
 	}
 
 	public OTMLToXHTMLConverter(OTViewFactory viewFactory,
 	        OTViewContainer viewContainer)
 	{
 		setViewContainer(viewContainer);
 		setViewFactory(viewFactory, null);
 	}
 
 	public OTMLToXHTMLConverter(OTViewFactory viewFactory, OTObject otObject)
 	{
 		this(viewFactory, otObject, null);
 	}
 
 	public OTMLToXHTMLConverter(OTViewFactory viewFactory, OTObject otObject,
 	        String mode)
 	{
 		this(viewFactory, otObject, null, mode);
 	}
 
 	public OTMLToXHTMLConverter(OTViewFactory viewFactory, OTObject otObject,
 		OTViewEntry viewEntry, String mode)
 	{
 		setViewFactory(viewFactory, mode);
 		this.topLevelOTObjects = new OTObject[1];
 		this.topLevelViewEntries = new OTViewEntry[1];
 		this.topLevelOTObjects[0] = otObject;
 		this.topLevelViewEntries[0] = viewEntry;
 	}
 	
 	public OTMLToXHTMLConverter(OTViewFactory viewFactory, OTObject[] otObjects)
 	{
 		this(viewFactory, otObjects, OTViewFactory.NO_VIEW_MODE);
 	}
 
 	public OTMLToXHTMLConverter(OTViewFactory viewFactory, OTObject[] otObjects,
 	        String mode)
 	{
 		setViewFactory(viewFactory, mode);
 		this.topLevelOTObjects = otObjects;
 		this.topLevelViewEntries = new OTViewEntry[otObjects.length];
 	}
 
 	public void setViewContainer(OTViewContainer viewContainer)
 	{
 		this.viewContainer = viewContainer;
 		if (viewContainer.getCurrentObject() != null) {
 			this.topLevelOTObjects = new OTObject[1];
 			this.topLevelOTObjects[0] = viewContainer.getCurrentObject();
 			this.topLevelViewEntries = new OTViewEntry[1];
 			this.topLevelViewEntries[0] = viewContainer.getCurrentViewEntry();
 		}
 	}
 
 	public void setViewFactory(OTViewFactory viewFactory, String mode)
 	{
 		this.viewFactory = viewFactory.getViewContext().createChildViewFactory();
 		this.viewFactory.setDefaultViewMode(mode);
 		this.viewFactory.getViewContext().setProperty(OTXHTMLView.ALWAYS_EMBED_XHTML_VIEW, 
 			"true");
 	}
 
 	// This is not applicable.
 	// public void setPfDocument(PfDocument pfDocument) {
 	// this.pfDocument = pfDocument;
 	// }
 
 	public void setXHTMLParams(File file, int pageWidth, int pageHeight)
 	{
 		outputFile = file;
 		containerDisplayWidth = pageWidth;
 		containerDisplayHeight = pageHeight;
 	}
 
 	public void run()
 	{
 		if (outputFile == null) {
 			return;
 		}
 
 		// FIXME: only the first object is used here it is not clear how to 
 		// handle this. It is possible a collection of objects could be converted
 		// with different object services, so they should have different controller
 		// services.
 		if(topLevelOTObjects[0] != null){
 			controllerService = topLevelOTObjects[0].getOTObjectService().createControllerService();
 			
 			OTControllerServiceFactory controllerServiceFactory = new OTControllerServiceFactory(){
 
 				public OTControllerService createControllerService()
                 {
 					OTControllerService subControllerService = 
 						((OTControllerServiceImpl) controllerService).createSubControllerService();						
 					return subControllerService;
                 }
 				
 			};
 			OTViewContext factoryContext = viewFactory.getViewContext();
 			factoryContext.addViewService(OTControllerServiceFactory.class, controllerServiceFactory);
 		}
 
 		String allTexts = "";
 		for (int i = 0; i < topLevelOTObjects.length; i++) {
 
 			if (topLevelOTObjects[i] == null) {
 				continue;
 			}
 			
 			String text = null;
 
 			OTJComponentView objView = getOTJComponentView(topLevelOTObjects[i], null, topLevelViewEntries[i]);
 
 			OTXHTMLView xhtmlView = null;
 			String bodyText = "";
 			if (objView instanceof OTXHTMLView) {
 				xhtmlView = (OTXHTMLView) objView;
 				bodyText = xhtmlView.getXHTMLText(topLevelOTObjects[i]);
 
 				Pattern p = Pattern.compile("<object refid=\"([^\"]*)\"([^>]*)>");
 				Matcher m = p.matcher(bodyText);
 				StringBuffer parsed = new StringBuffer();
 				OTObjectService objectService = topLevelOTObjects[i].getOTObjectService();
 				while (m.find()) {
 					String id = m.group(1);
 					OTID otid = objectService.getOTID(id);
 					OTObject referencedObject = null;
 					try {
 						referencedObject = objectService.getOTObject(otid);
 					} catch (Exception e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					} 
 
 					Pattern userPat = Pattern.compile("user=\"([^\"]*)\"");
 					Matcher userMatcher = userPat.matcher(m.group(2));
 					if(userMatcher.find()){
 						String userId = userMatcher.group(1);
 						referencedObject = getRuntimeObject(referencedObject, userId);
 					}
 					
 		        	OTViewEntry viewEntry = null;
 					Pattern viewPat = Pattern.compile("viewid=\"([^\"]*)\"");
 					Matcher viewMatcher = viewPat.matcher(m.group(2));
 					if(viewMatcher.find()){
						String viewId = userMatcher.group(1);
 			        	if(viewId != null && viewId.length() > 0) {
 							OTID viewOTid = objectService.getOTID(viewId);
 			        		try {
 	                            viewEntry = (OTViewEntry)
 	                            	objectService.getOTObject(viewOTid);
                             } catch (Exception e) {
 	                            e.printStackTrace();
                             }
 			        	}
 					}
 
 					// System.out.println(referencedObject.getClass());
 					String url = embedOTObject(referencedObject, viewEntry);
 					// String url = objView.getXHTMLText(folder,
 					// containerDisplayWidth, containerDisplayHeight);
 					if (url != null) {
 						try {
 							m.appendReplacement(parsed, url);
 						} catch (IllegalArgumentException e) {
 							System.err.println("bad replacement: " + url);
 							e.printStackTrace();
 						}
 					}
 				}
 				m.appendTail(parsed);
 				text = "<div>" + parsed.toString() + "</div><hr/>";
 			} else {
 				text = embedOTObject(topLevelOTObjects[i], topLevelViewEntries[i]);
 			}
 
 			allTexts = allTexts + text;
 		}
 
 		try {
 			FileWriter fos = new FileWriter(outputFile);
 			fos.write("<html><body>" + allTexts + "</body></html>");
 			fos.close();
 		} catch (FileNotFoundException exp) {
 			exp.printStackTrace();
 		} catch (IOException exp) {
 			exp.printStackTrace();
 		}
 	}
 
 	/**
 	 * This code attempts to save an image of the component. It does 3 things
 	 * that are a bit odd but seem to make things work. 1. It calls addNotify on
 	 * the component. This tricks it into thinking it has a parent, so it can be
 	 * laid out. 2. It calls validate on the component that makes it get laid
 	 * out.
 	 * 
 	 * 3. The image saving code is placed into a invoke and wait call. Both
 	 * setSize and validate cause events to be queued so we use Invoke and wait
 	 * so these events get processed before we save the image by calling paint
 	 * on it.
 	 * 
 	 */
 	public String embedComponent(JComponent comp, float scaleX, float scaleY,
 	    OTObject otObject)
 	{
 		Dimension compSize = comp.getSize();
 
 		if (compSize.height <= 0 || compSize.width <= 0) {
 			throw new RuntimeException("Component size width: "
 			        + compSize.width + " height: " + compSize.height
 			        + " cannot be <=0");
 		}
 
 		comp.addNotify();
 		comp.validate();
 
 		String outputFileNameWithoutExtension = outputFile.getName().substring(
 		        0, outputFile.getName().lastIndexOf('.'));
 		File folder = new File(outputFile.getParent(),
 		        outputFileNameWithoutExtension + "_files");
 		if (!folder.exists())
 			folder.mkdir();
 		if (!folder.isDirectory())
 			return null;
 
 		ImageSaver saver = new ImageSaver(comp, folder, folder.getName(),
 		        otObject, scaleX, scaleY);
 
 		try {
 			SwingUtilities.invokeAndWait(saver);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 
 		return saver.getText();
 	}
 
 	protected OTJComponentView getOTJComponentView(OTObject obj, OTViewEntry viewEntry)
 	{
 		return getOTJComponentView(obj, null, viewEntry);
 	}
 
 	protected OTJComponentView getOTJComponentView(OTObject obj, String mode, OTViewEntry viewEntry)
 	{
 		if (jComponentService == null) {
 			OTViewContext viewContext = viewFactory.getViewContext();
 			OTJComponentServiceFactory serviceFactory =
 			    (OTJComponentServiceFactory) viewContext.getViewService(OTJComponentServiceFactory.class);
 			jComponentService = serviceFactory.createOTJComponentService(viewFactory, false);
 		}
 		if (viewEntry != null) {
 			return jComponentService.getObjectView(obj, viewContainer, mode, viewEntry);
 		} else {
 			return jComponentService.getObjectView(obj, viewContainer, mode);
 		}
 	}
 
 	protected JComponent getJComponent(OTObject obj, OTViewEntry viewEntry)
 	{
 		OTJComponentView objView = getOTJComponentView(obj, null, viewEntry);
 
 		JComponent comp = objView.getComponent(obj);
 		return comp;
 	}
 
 	public String embedOTObject(OTObject obj, OTViewEntry viewEntry)
 	{
 		OTView view = viewFactory.getView(obj, OTPrintDimension.class);
 		if (view == null) {
 			view = (OTView) viewFactory.getView(obj, OTJComponentView.class);
 		}
 		
 		if (view instanceof OTXHTMLView) {
 			String objectText = ((OTXHTMLView) view).getXHTMLText(obj);
 			return objectText;
 		}
 
 		view = getOTJComponentView(obj, null, viewEntry);
 		JComponent comp = ((OTJComponentView)view).getComponent(obj);
 
 		Dimension printDim = null;
 		if (view instanceof OTPrintDimension) {
 			OTPrintDimension dimView = (OTPrintDimension) view;
 			printDim = dimView.getPrintDimension(obj, containerDisplayWidth,
 			        containerDisplayHeight);
 		}
 
 		if (printDim != null) {
 			comp.setSize(printDim);
 		} else {
 			Dimension dim2 = comp.getPreferredSize();
 			if (dim2.width == 0)
 				dim2.width = 1;
 			if (dim2.height == 0)
 				dim2.height = 1;
 			comp.setSize(dim2);
 		}
 
 		String url = embedComponent(comp, 1, 1, obj);
 		url = "<img src='" + url + "' />";
 
 		return url;
 		// return null;
 	}
 
 	class ImageSaver
 	    implements Runnable
 	{
 		JComponent comp;
 
 		File folder;
 
 		OTObject otObject;
 
 		String text = null;
 
 		float scaleX = 1;
 
 		float scaleY = 1;
 
 		private String folderPath;
 
 		ImageSaver(JComponent comp, File folder, String folderPath,
 		        OTObject otObject, float scaleX, float scaleY)
 		{
 			this.comp = comp;
 			this.folder = folder;
 			this.otObject = otObject;
 			this.scaleX = scaleX;
 			this.scaleY = scaleY;
 			this.folderPath = folderPath;
 		}
 
 		public void run()
 		{
 			// TODO Auto-generated method stub
 			try {
 				String id = otObject.otExternalId();
 				id = id.replaceAll("/", "_");
 				id = id.replaceAll("'", "");
 				id = id.replaceAll("!", "") + ".png";
 				
 				if (!folder.isDirectory()) {
 					text = null;
 					return;
 				}
 
 				File newFile = new File(folder, id);
 
 				BufferedImage bim = ComponentScreenshot
 				        .makeComponentImageAlpha(comp, scaleX, scaleY);
 				ComponentScreenshot.saveImageAsFile(bim, newFile, "png");
 
 				text = folderPath + "/" + id;
 				return;
 
 			} catch (Throwable t) {
 				t.printStackTrace();
 			}
 			text = null;
 		}
 
 		String getText()
 		{
 			return text;
 		}
 	}	
 	
 	public OTObject getRuntimeObject(OTObject object, String userStr) {
 		try {
 			OTObjectService objectService = object.getOTObjectService();
 			OTrunk otrunk = (OTrunk)objectService.getOTrunkService(OTrunk.class);
 			OTID userId = objectService.getOTID(userStr);
 			OTUser user = (OTUser) objectService.getOTObject(userId);
 			return otrunk.getUserRuntimeObject(object, user);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
     
 
 }
