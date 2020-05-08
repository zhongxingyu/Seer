 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.gda.dls.client.views;
 
 import gda.device.detectorfilemonitor.FileProcessor;
 import gda.factory.Findable;
 
 import java.io.File;
 import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.eclipse.ui.IPartListener2;
 import org.eclipse.ui.IPartService;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchPartReference;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.util.StringUtils;
 
 import uk.ac.diamond.scisoft.analysis.PlotServer;
 import uk.ac.diamond.scisoft.analysis.PlotServerProvider;
 import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
 import uk.ac.diamond.scisoft.analysis.plotserver.FileOperationBean;
 import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
 import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
 import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
 import uk.ac.diamond.scisoft.analysis.rcp.views.PlotView;
 
 /**
  * Sends contents of an image file to a view via PlotServer
  */
 public class ImageFileDisplayer implements FileProcessor, InitializingBean, Findable {
 	private final class PlotUpdateRunnable implements Runnable {
 		private DataBean loadImage;
 
 		private PlotUpdateRunnable(){}
 
 		DataBean getLoadImage(){
 			return loadImage;
 		}
 
		final AtomicBoolean scheduled = new AtomicBoolean(false);
 		@Override
 		public void run() {
			scheduled.set(false);
 			try {
 				if( !partVisible){
 					final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 					showView = window.getActivePage().showView(viewID);
 				}
 				plotView.processPlotUpdate(getLoadImage());
 			} catch (PartInitException e) {
 				logger.error("Error showing view or updating it", e);
 			} 							
 		}
 		
 		void process(DataBean loadImage){
 			this.loadImage = loadImage;
			if (scheduled.compareAndSet(false, true)) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(this);
 			}
 		}
 	}
 
 	private static final Logger logger = LoggerFactory.getLogger(ImageFileDisplayer.class);
 
 	String viewName;
 
 	String viewID;
 
 	private PlotView plotView;
 
 	boolean isPlotView = true;
 
 	public boolean isPlotView() {
 		return isPlotView;
 	}
 
 	public void setPlotView(boolean isPlotView) {
 		this.isPlotView = isPlotView;
 	}
 
 	boolean partVisible = false;
 
 	boolean thumbNail = false;
 
 	public boolean isThumbNail() {
 		return thumbNail;
 	}
 
 	public void setThumbNail(boolean thumbNail) {
 		this.thumbNail = thumbNail;
 	}
 
 	private IPartListener2 partListener;
 
 	private boolean listenerAdded = false;
 
 	private IViewPart showView;
 
 	private String name;
 
 	public String getViewID() {
 		return viewID;
 	}
 
 	public void setViewID(String viewID) {
 		this.viewID = viewID;
 	}
 
 	public String getViewName() {
 		return viewName;
 	}
 
 	public void setViewName(String viewName) {
 		this.viewName = viewName;
 	}
 
 	private boolean openViewAutomatically = true;
 	
 	public void setOpenViewAutomatically(boolean openViewAutomatically) {
 		this.openViewAutomatically = openViewAutomatically;
 	}
 	
 	void sendIfVisible(String filename) throws Exception {
 		if (showView!=null) {
 
 			File file = new File(filename);
 			long lastModified = file.lastModified();
 			Thread.sleep(50);
 			long lastModifiedNow = file.lastModified();
 			while(lastModifiedNow !=  lastModified){
 				lastModified = lastModifiedNow;
 				Thread.sleep(500);
 				lastModified = file.lastModified();
 			}
 
 
 			if (isPlotView()) {
 				int attempts = 0;
 				while( attempts<10){
 					try{
 						FileOperationBean fopBean = new FileOperationBean(FileOperationBean.GETIMAGEFILE);
 						Vector<String> files = new Vector<String>();
 						files.add(filename);
 						fopBean.setFiles(files);
 						DataBean loadImage = fopBean.loadImage(thumbNail, false);
 						if( loadImage.getData() == null || loadImage.getData().isEmpty()  || loadImage.getData().get(0).getData().getName().equals("Invalid Image")){
 							throw new Exception("Invalid image detected");
 						}
 						loadImage.setGuiPlotMode(GuiPlotMode.TWOD);
 						if( plotUpdateRunnable == null){
 							plotUpdateRunnable = new PlotUpdateRunnable();
 						}
 						plotUpdateRunnable.process(loadImage);
 						break;
 						
 					} catch( Exception e){
 						logger.warn("Error loading image from file:"+filename);
 						Thread.sleep(1000);
 						attempts++;
 					}
 				}
 
 			} else {
 				PlotServer plotServer = PlotServerProvider.getPlotServer();
 				GuiBean fileLoadBean = plotServer.getGuiState(viewName);
 				fileLoadBean = new GuiBean();
 				FileOperationBean fopBean = new FileOperationBean(FileOperationBean.GETIMAGEFILE);
 				Vector<String> files = new Vector<String>();
 				files.add(filename);
 				fopBean.setFiles(files);
 				fileLoadBean.put(GuiParameters.FILEOPERATION, fopBean);
 				fileLoadBean.put(GuiParameters.DISPLAYFILEONVIEW, viewName);
 				plotServer.updateGui(viewName, fileLoadBean);
 			}
 
 		}
 	}
 
 	void openViewandSendImage(String filename) throws Exception {
 		if (StringUtils.hasLength(filename)) {
 			registerListener();
 			if (showView == null) {
 				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable(){
 
 					@Override
 					public void run() {
 						try {
 							final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 							
 							if (openViewAutomatically) {
 								showView = window.getActivePage().showView(viewID);
 							} else {
 								showView = window.getActivePage().findView(viewID);
 							}
 							
 							if (isPlotView()) {
 								
 								if (showView == null) {
 									plotView = null;
 								}
 								
 								else if (!(showView instanceof PlotView))
 									throw new IllegalArgumentException(viewID + " is not a PlotView");
 								plotView = (PlotView) showView;
 							}
 							
 							partVisible = (showView != null);
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 				});
 			}
 			sendIfVisible(filename);
 		}
 	}
 
 	void registerListener() {
 		if (listenerAdded)
 			return;
 			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable(){
 
 				@Override
 				public void run() {
 					try {
 						IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 						IPartService service = activeWorkbenchWindow.getPartService();
 						service.addPartListener(partListener);
 						listenerAdded = true;
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 				
 			});
 	}
 
 
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		if (viewName == null)
 			throw new IllegalArgumentException("viewName is null");
 		if (viewID == null) {
 			throw new IllegalArgumentException("viewID == null");
 		}
 
 		partListener = new IPartListener2() {
 
 			boolean isMyView(IWorkbenchPartReference partRef) {
 				IWorkbenchPart part = partRef.getPart(false);
 				return part == showView;
 			}
 
 			@Override
 			public void partVisible(IWorkbenchPartReference partRef) {
 				if (isMyView(partRef)) {
 					if (!partVisible) {
 						partVisible = true;
 					}
 				}
 			}
 
 			@Override
 			public void partOpened(IWorkbenchPartReference partRef) {
 			}
 
 			@Override
 			public void partInputChanged(IWorkbenchPartReference partRef) {
 			}
 
 			@Override
 			public void partHidden(IWorkbenchPartReference partRef) {
 				if (isMyView(partRef)) {
 					partVisible = false;
 				}
 			}
 
 			@Override
 			public void partDeactivated(IWorkbenchPartReference partRef) {
 			}
 
 			@Override
 			public void partClosed(IWorkbenchPartReference partRef) {
 				if (isMyView(partRef)) {
 					showView = null;
 					plotView = null;
 					partVisible = false;
 				}
 			}
 
 			@Override
 			public void partBroughtToTop(IWorkbenchPartReference partRef) {
 			}
 
 			@Override
 			public void partActivated(IWorkbenchPartReference partRef) {
 			}
 		};
 
 	}
 
 	Boolean runAgain = false;
 	boolean threadRunning = false;
 
 	private String filename;
 
 	private PlotUpdateRunnable plotUpdateRunnable;
 
 	protected void queueProcess(String filename) {
 		synchronized (runAgain) {
 			this.filename=filename;
 			if( threadRunning){
 				runAgain = true;
 			} else {
 				Thread thread = new Thread(new Runnable() {
 					@Override
 					public void run() {
 						Boolean run = true;
 						int num = 0;
 						while (run) {
 							num++;
 							String filename;
 							try {
 								filename = ImageFileDisplayer.this.filename;
 								try {
 									openViewandSendImage(filename);
 								} catch (Exception e) {
 									ImageFileDisplayer.logger.error("Error sending " + filename,e);
 								}
 							} catch (Exception e1) {
 								ImageFileDisplayer.logger.error("Error getting filename ",e1);
 							}
 							synchronized (runAgain) {
 								run = runAgain;
 								runAgain = false;
 							}
 						}
 						threadRunning = false;
 						ImageFileDisplayer.logger.info("run -" + num);
 					}
 				});
 				thread.start();
 				threadRunning = true;
 			}
 		}
 	}
 
 	@Override
 	public void processFile(String filename) {
 		if(StringUtils.hasLength(filename)){
 			queueProcess(filename);
 		}
 	}
 
 	@Override
 	public void setName(String name) {
 		this.name = name;
 		
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 }
