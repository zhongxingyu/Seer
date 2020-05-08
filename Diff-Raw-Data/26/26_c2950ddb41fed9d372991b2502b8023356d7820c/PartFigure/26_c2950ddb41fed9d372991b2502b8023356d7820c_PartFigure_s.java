 package org.fritzing.fritzing.diagram.edit.parts;
 
 import java.util.Collections;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.RectangleFigure;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.swt.graphics.Image;
 import org.fritzing.fritzing.diagram.edit.PartLoader;
 import org.fritzing.fritzing.diagram.providers.FritzingElementTypes;
 
 /**
  * @generated NOT
  */
 public class PartFigure extends RectangleFigure implements IZoomableFigure {
 
 	protected Image image;
 	protected Double currentImageZoomLevel;
 	protected PartLoader partLoader;
 	protected String  contentsPath;
 
 	/**
 	 * @generated NOT
 	 */
 	public PartFigure(PartLoader partLoader) {
 		this.partLoader = partLoader;
 		createContents();	
 		this.setPreferredSize(new Dimension(partLoader.getSize()));
 		this.setMaximumSize(new Dimension(partLoader.getSize()));
 		this.setMinimumSize(new Dimension(partLoader.getSize()));
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	protected void createContents() {
 		setImageByZoomLevel(-1); // set to default
 
 		// TODO: SVGs
 		/*
 		 url = new URL("file", null, partLoader.getContentsPath() + partLoader.getSvgFilename());
 		 RenderedImage ri = RenderedImageFactory.getInstance(url);
 		 RenderInfo rinfo = ri.getRenderInfo();
 		 image = ri.getSWTImage();
 		 org.eclipse.swt.graphics.Rectangle r = image.getBounds();
 		 rinfo.setValues((int) (r.width * multiplier), (int) (r.height * multiplier),
 		 rinfo.shouldMaintainAspectRatio(), true,
 		 rinfo.getBackgroundColor(), rinfo.getForegroundColor());
 		 ri = ri.getNewRenderedImage(rinfo);
 		 try {
 			 image = ri.getSWTImage();
 		 } catch (Exception ex) {
 			 ex.printStackTrace();
 		 }
 		 baseFigure = new ScalableImageFigure(ri, true, true, false);
 		 this.add(baseFigure);
 		 */
 	}
 	
 	public void zoomFigure(double zoom) {
 		// TODO: optimize: only change image if necessary
 		setImageByZoomLevel(zoom);
 	}
 	
 	/*
 	 * Loads the best image for the given zoom level
 	 * and tries to find a default if zoom=-1 (level=1 or the smallest available)
 	 */
 	protected void setImageByZoomLevel(double zoom) {
 		if (Double.compare(zoom, -1) == 0) {
			zoom = partLoader.getBitmapFilenames().containsKey(1) ? 1 : 
 				getSortedImageLevels().firstElement();
 			currentImageZoomLevel = -1.0;
		} else if (!partLoader.getBitmapFilenames().containsKey(zoom)) {
 			Vector<Double> levels = getSortedImageLevels();
		    zoom = levels.lastElement();
 		    for (Double level: levels) {
 		    	if (level.compareTo(zoom) >= 0) {
		    		zoom = level;
 		    		break;
 		    	}
 		    }
 		}
		if (Double.compare(currentImageZoomLevel, zoom) != 0) {
 			try {
				String imageSrc = partLoader.getBitmapFilenames().get(new Double(zoom));
 				image = FritzingElementTypes.getImageRegistry().get(
 					partLoader.getContentsPath() + imageSrc);
				repaint();
				currentImageZoomLevel = zoom;
 			} catch (Exception ex) {
 				// inform the user?
 				ex.printStackTrace();
 			}
 		}
 	}
 	
 	protected Vector<Double> getSortedImageLevels() {
 		Hashtable<Double,String> images = partLoader.getBitmapFilenames();
 		// find the next biggest available image zoom level
 		Vector<Double> levels = new Vector<Double>(images.keySet());
 	    Collections.sort(levels);
 	    return levels;
 	}
 	
 	/**
 	 * @generated NOT
 	 */
 	public void paintFigure(Graphics g) {
 		// super.paintFigure(g);
 		if (image != null) {
 			Rectangle r = getBounds().getCopy();
 			Rectangle s = new org.eclipse.draw2d.geometry.Rectangle(
 					image.getBounds());
 			g.drawImage(image, 0, 0, s.width, s.height, r.x, r.y, r.width,
 					r.height);
 		}
 	}
 
 }
