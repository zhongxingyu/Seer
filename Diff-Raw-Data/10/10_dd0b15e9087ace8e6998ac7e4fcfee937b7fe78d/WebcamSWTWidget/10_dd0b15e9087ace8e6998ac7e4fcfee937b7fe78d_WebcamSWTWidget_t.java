 /*
  * Copyright 2010 Ricardo Pescuma Domenecci
  * 
  * This file is part of jfg.
  * 
  * jfg is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  * 
  * jfg is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License along with jfg. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.pescuma.jfg.gui.swt;
 
 import static org.pescuma.jfg.gui.swt.SWTUtils.*;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.SWTException;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.pescuma.jfg.Attribute;
 import org.pescuma.jfg.gui.WebcamGuiWidget;
 
 import de.ikoffice.widgets.SplitButton;
 
 class WebcamSWTWidget extends AbstractLabelControlSWTWidget implements WebcamGuiWidget
 {
 	private WebcamControl webcam;
 	private SplitButton buttom;
 	private Image snapshot;
 	private final Color[] colors = new Color[2];
 	private final Color backgrounds[] = new Color[3];
 	
 	private final Listener showWebcamListener = new Listener() {
 		@Override
 		public void handleEvent(Event event)
 		{
 			showWebcam();
 		}
 	};
 	private final Listener takeSnapshotListener = new Listener() {
 		@Override
 		public void handleEvent(Event event)
 		{
 			takeSnapshot();
 		}
 	};
 	private final Listener setImageListener = new Listener() {
 		@Override
 		public void handleEvent(Event event)
 		{
 			setImage();
 		}
 	};
 	private final Listener clearListener = new Listener() {
 		@Override
 		public void handleEvent(Event event)
 		{
 			clearImage();
 		}
 	};
 	private Composite webcamBorder;
 	
 	public WebcamSWTWidget(Attribute attrib, JfgFormData data)
 	{
 		super(attrib, data);
 	}
 	
 	@Override
 	protected Control createWidget(Composite parent)
 	{
 		Composite composite = data.componentFactory.createComposite(parent, SWT.NONE);
 		composite.setLayout(createBorderlessGridLayout(1, false));
 		
 		composite.addListener(SWT.Dispose, new Listener() {
 			@Override
 			public void handleEvent(Event event)
 			{
 				if (snapshot != null)
 				{
 					snapshot.dispose();
 					snapshot = null;
 				}
 				
 				cleanupButton();
 			}
 		});
 		
 		webcamBorder = data.componentFactory.createComposite(composite, SWT.NONE);
 		webcamBorder.setLayoutData(new GridData(GridData.FILL_BOTH));
 		GridLayout layout = new GridLayout(1, false);
 		layout.marginHeight = 2;
 		layout.marginWidth = 2;
 		webcamBorder.setLayout(layout);
 		
 		backgrounds[0] = colors[0] = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
 		backgrounds[1] = colors[1] = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
 		webcamBorder.addPaintListener(new PaintListener() {
 			@Override
 			public void paintControl(PaintEvent e)
 			{
 				e.gc.setForeground(colors[0]);
 				e.gc.drawRectangle(0, 0, webcamBorder.getSize().x - 1, webcamBorder.getSize().y - 1);
 				
 				e.gc.setForeground(colors[1]);
 				e.gc.drawRectangle(1, 1, webcamBorder.getSize().x - 3, webcamBorder.getSize().y - 3);
 			}
 		});
 		
 		webcam = new WebcamControl(webcamBorder, SWT.NONE);
 		webcam.setLayoutData(new GridData(GridData.FILL_BOTH));
 		webcam.addListener(SWT.Dispose, getDisposeListener());
 		// webcam.setCropImage(false);
 		
 		backgrounds[2] = webcam.getBackground();
 		
 		if (attrib.canWrite())
 		{
 			buttom = new SplitButton(composite, SWT.NONE);
 			buttom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 			updateButton();
 		}
 		
 		return composite;
 	}
 	
 	@Override
 	public Object getValue()
 	{
 		if (getAttribute().getType() == ImageData.class)
			return (snapshot == null ? null : snapshot.getImageData());
 		else
 			return snapshot;
 	}
 	
 	@Override
 	public void setValue(Object value)
 	{
 		if (value instanceof Image)
 			replaceSnapshot((Image) value, true);
 		else if (value instanceof ImageData)
 			replaceSnapshot(new Image(Display.getCurrent(), (ImageData) value), false);
 		else
 			replaceSnapshot(null, false);
 		
 		updateButton();
 	}
 	
 	@Override
 	protected void updateColor()
 	{
 		colors[0] = createColor(webcam, backgrounds[0]);
 		colors[1] = createColor(webcam, backgrounds[1]);
 		webcam.setBackground(createColor(webcam, backgrounds[2]));
 		webcamBorder.redraw();
 	}
 	
 	@Override
 	public void setEnabled(boolean enabled)
 	{
 		super.setEnabled(enabled);
 		
 		if (!enabled)
 			webcam.stopWebcam();
 		
 		webcam.setEnabled(enabled);
 		
 		if (buttom != null)
 			buttom.setEnabled(enabled);
 	}
 	
 	private void updateButton()
 	{
 		if (buttom == null)
 			return;
 		
 		buttom.setRedraw(false);
 		
 		try
 		{
 			cleanupButton();
 			
 			boolean hasWebcam = (webcam.getWebcamCount() > 0);
 			Menu menu = buttom.getMenu();
 			
 			if (webcam.isShowingWebcam())
 			{
 				createTakeImage();
 				createSetImage(menu);
 				createClear(menu);
 			}
 			else if (snapshot != null)
 			{
 				if (hasWebcam)
 				{
 					createWebcam();
 					createSetImage(menu);
 				}
 				else
 				{
 					createSetImage();
 				}
 				createClear(menu);
 			}
 			else
 			{
 				if (hasWebcam)
 				{
 					createWebcam();
 					createSetImage(menu);
 				}
 				else
 				{
 					createSetImage();
 				}
 			}
 			
 		}
 		finally
 		{
 			buttom.setRedraw(true);
 		}
 	}
 	
 	private void cleanupButton()
 	{
 		buttom.setImage(null);
 		
 		Menu menu = buttom.getMenu();
 		MenuItem[] items = menu.getItems();
 		if (items != null)
 			for (MenuItem item : items)
 				item.dispose();
 		
 		buttom.removeListener(SWT.Selection, showWebcamListener);
 		buttom.removeListener(SWT.Selection, takeSnapshotListener);
 		buttom.removeListener(SWT.Selection, setImageListener);
 		buttom.removeListener(SWT.Selection, clearListener);
 	}
 	
 	private void createTakeImage()
 	{
 		buttom.setText(data.textTranslator.translate("WebcamGuiWidget:Take picture"));
 		buttom.setImage(data.resourcesManager.newImage("icons/webcam.png"));
 		buttom.addListener(SWT.Selection, takeSnapshotListener);
 	}
 	
 	private void createWebcam()
 	{
 		buttom.setText(data.textTranslator.translate("WebcamGuiWidget:Webcam"));
 		buttom.setImage(data.resourcesManager.newImage("icons/webcam.png"));
 		buttom.addListener(SWT.Selection, showWebcamListener);
 	}
 	
 	private void createSetImage()
 	{
 		buttom.setText(data.textTranslator.translate("WebcamGuiWidget:Set image"));
 		buttom.setImage(data.resourcesManager.newImage("icons/picture.png"));
 		buttom.addListener(SWT.Selection, setImageListener);
 	}
 	
 	private void createClear(Menu menu)
 	{
 		MenuItem clear = new MenuItem(menu, SWT.PUSH);
 		clear.setText(data.textTranslator.translate("WebcamGuiWidget:Clear"));
 		clear.addListener(SWT.Selection, clearListener);
 	}
 	
 	private void createSetImage(Menu menu)
 	{
 		MenuItem setImage = new MenuItem(menu, SWT.PUSH);
 		setImage.setText(data.textTranslator.translate("WebcamGuiWidget:Set image"));
 		setImage.setImage(data.resourcesManager.newImage("icons/picture.png"));
 		setImage.addListener(SWT.Selection, setImageListener);
 	}
 	
 	private boolean showWebcam()
 	{
 		if (!webcam.showWebcam())
 			return false;
 		
 		updateButton();
 		return true;
 	}
 	
 	private void takeSnapshot()
 	{
 		webcam.stopWebcam();
 		
 		if (webcam.isEmpty())
 			webcam.showImage(snapshot);
 		else
 			replaceSnapshot(new Image(Display.getCurrent(), webcam.takeSnapshot()), false);
 		
 		updateButton();
 	}
 	
 	private void setImage()
 	{
 		Image newImage = openImage();
 		if (newImage == null)
 			return;
 		
 		replaceSnapshot(newImage, false);
 		
 		updateButton();
 	}
 	
 	private Image openImage()
 	{
 		FileDialog dialog = new FileDialog(webcam.getShell(), SWT.OPEN);
 		dialog.setFilterNames(new String[] {
 				data.textTranslator.translate("ImageBuilder:Image Files") + " (*.jpg;*.png;*.bmp;*.gif)",
 				data.textTranslator.translate("ImageBuilder:All Files") + " (*.*)" });
 		dialog.setFilterExtensions(new String[] { "*.jpg;*.png;*.bmp;*.gif", "*.*" });
 		String filename = dialog.open();
 		if (filename == null)
 			return null;
 		
 		try
 		{
 			return new Image(webcam.getDisplay(), filename);
 		}
 		catch (SWTException e)
 		{
 			return null;
 		}
 	}
 	
 	private void clearImage()
 	{
 		replaceSnapshot(null, false);
 		
 		updateButton();
 	}
 	
 	private void replaceSnapshot(Image newImage, boolean copyImage)
 	{
 		if (snapshot != null)
 			snapshot.dispose();
 		
 		if (newImage != null && copyImage)
 			snapshot = new Image(webcam.getDisplay(), newImage, SWT.IMAGE_COPY);
 		else
 			snapshot = newImage;
 		
 		webcam.showImage(snapshot);
 		
 		onWidgetModify();
 	}
 	
 	@Override
 	public boolean startWebcam()
 	{
 		if (!attrib.canWrite())
 			return false;
 		
 		if (webcam.isShowingWebcam())
 			return true;
 		
 		return showWebcam();
 	}
 	
 	@Override
 	public boolean stopWebcam()
 	{
 		if (!attrib.canWrite())
 			return false;
 		
 		if (!webcam.isShowingWebcam())
 			return true;
 		
 		webcam.showImage(snapshot);
 		updateButton();
 		return false;
 	}
 }
