 package org.csstudio.swt.xygraph.util;
 
 import java.lang.reflect.Method;
 import java.util.List;
 
 import org.csstudio.swt.xygraph.figures.XYGraph;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.draw2d.FigureUtilities;
 import org.eclipse.draw2d.SWTGraphics;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.graphics.Transform;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.osgi.framework.Bundle;
 
 public class SingleSourceHelperImpl extends SingleSourceHelper {
 
 	@Override
 	protected Cursor createInternalCursor(Display display, ImageData imageData, int width, int height, int style) {
 		return GraphicsUtil.createCursor(display, imageData, width, height);
 	}
 
 	@Override
 	protected Image createInternalVerticalTextImage(String text, Font font, RGB color, boolean upToDown) {
 		final Dimension titleSize = FigureUtilities.getTextExtents(text, font);
 
 		final int w = titleSize.height;
 		final int h = titleSize.width + 1;
 		Image image = new Image(Display.getCurrent(), w, h);
 
 		final GC gc = GraphicsUtil.createGC(image);
 		final Color titleColor = new Color(Display.getCurrent(), color);
 		RGB transparentRGB = new RGB(240, 240, 240);
 
 		gc.setBackground(XYGraphMediaFactory.getInstance().getColor(
 				transparentRGB));
 		gc.fillRectangle(image.getBounds());
 		gc.setForeground(titleColor);
 		gc.setFont(font);
 		final Transform tr = new Transform(Display.getCurrent());
 		if (!upToDown) {
 			tr.translate(0, h);
 			tr.rotate(-90);
 			GraphicsUtil.setTransform(gc,tr);
 		} else {
 			tr.translate(w, 0);
 			tr.rotate(90);
 			GraphicsUtil.setTransform(gc,tr);
 		}
 		gc.drawText(text, 0, 0);
 		tr.dispose();
 		gc.dispose();
 		final ImageData imageData = image.getImageData();
 		image.dispose();
 		titleColor.dispose();
 		imageData.transparentPixel = imageData.palette.getPixel(transparentRGB);
 		image = new Image(Display.getCurrent(), imageData);
 		return image;
 	}
 
 	@Override
 	protected Image getInternalXYGraphSnapShot(XYGraph xyGraph) {
 		Rectangle bounds = xyGraph.getBounds();
 		Image image = new Image(null, bounds.width + 6, bounds.height + 6);
 		GC gc = GraphicsUtil.createGC(image);
 		SWTGraphics graphics = new SWTGraphics(gc);
 		graphics.translate(-bounds.x + 3, -bounds.y + 3);
 		graphics.setForegroundColor(xyGraph.getForegroundColor());
 		graphics.setBackgroundColor(xyGraph.getBackgroundColor());
 		xyGraph.paint(graphics);
 		gc.dispose();
 		return image;
 	}
 
 	/**
 	 * Use reflection so that we can single source without fragments.
 	 */
 	@Override
 	protected String getInternalImageSavePath(String[] filterExtensions) {
 		
 		try { // Swt use reflection
 			Class clazz = getClass().getClassLoader().loadClass("org.eclipse.swt.widgets.FileDialog");
 			
 			Object dialog = clazz.getConstructor(Shell.class, int.class).newInstance(Display.getDefault().getShells()[0], SWT.SAVE);
 			
 			Method setFilterNamesMethod = clazz.getMethod("setFilterNames", String[].class);
 			setFilterNamesMethod.invoke(dialog, new String[] { "PNG Files", "All Files (*.*)" });
 			
 			
 			if (filterExtensions==null) filterExtensions = new String[] { "*.png", "*.*" };
 			Method setFilterExtensionsMethod = clazz.getMethod("setFilterExtensions", String[].class);
 			setFilterExtensionsMethod.invoke(dialog, filterExtensions);
 			
 			Method openMethod = clazz.getMethod("open");
 			String path = (String)openMethod.invoke(dialog);
 			return path;
 			
 		} catch (Throwable ne) {
 			throw new RuntimeException(ne.getMessage(), ne);
 		}
 	}
 
 	@Override
 	protected IFile getProjectSaveFilePath(final String name) {
 		
 		try {
 			final Bundle bundle = Platform.getBundle("org.eclipse.emf.common.ui");
 			final Class  clazz  = bundle.loadClass("org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog");
 			
 			final Method openNewMethod = clazz.getMethod("openNewFile", Shell.class,String.class,String.class, IPath.class,  List.class);
 			
 			IFile exportTo = (IFile)openNewMethod.invoke(null, Display.getDefault().getActiveShell(), 
 	                "Create file to export to", 
 	                "Export data from "+name+"'", 
 	                null, null);
 	
			// TODO Auto-generated method stub
			return null;
 			
 		} catch (Throwable ne) {
 			throw new RuntimeException(ne.getMessage(), ne);
 		}
 	}
 
 }
