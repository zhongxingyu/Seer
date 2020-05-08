 package uk.ac.ox.map.carto.canvas;
 
 import java.awt.geom.Point2D;
 import java.awt.geom.Point2D.Double;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 
 import org.freedesktop.cairo.LinearPattern;
 import org.freedesktop.cairo.Surface;
 import org.gnome.gdk.Color;
 import org.gnome.gdk.Pixbuf;
 import org.gnome.pango.Alignment;
 import org.gnome.pango.FontDescription;
 import org.gnome.pango.Layout;
 import org.gnome.rsvg.Handle;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ox.map.carto.canvas.ContinuousScale.ColourStop;
 import uk.ac.ox.map.carto.canvas.ContinuousScale.ScaleAnnotation;
 import uk.ac.ox.map.carto.canvas.Rectangle.Anchor;
 import uk.ac.ox.map.carto.style.Palette;
 import uk.ac.ox.map.carto.util.AnnotationFactory;
 
 import com.vividsolutions.jts.geom.Envelope;
 
 public class MapCanvas extends BaseCanvas {
 
 	private final List<DataFrame> dataFrames = new ArrayList<DataFrame>();
 	final FontDescription fontDesc;
 	static final Logger logger = LoggerFactory.getLogger(MapCanvas.class);
 
 	public MapCanvas(Surface pdf, double width, double height) {
 		super(pdf, width, height);
 		
 		/*
 		 * Sensible defaults for font options.
 		 */
 		fontDesc = new FontDescription();
 		fontDesc.setFamily("Helvetica");
 		fontDesc.setSize(12);
 	}
 
 	public void setTitle(String text, Rectangle frame, double d) {
 	  
 	  if (text == null) {
 	    logger.debug("No text to draw");
 	    return;
 	  }
 	  
 		Layout layout = new Layout(cr);
 		fontDesc.setSize(d);
 		layout.setFontDescription(fontDesc);
 		layout.setWidth(frame.width);
 		layout.setAlignment(Alignment.CENTER);
 		layout.setMarkup(text);
 
 		//Title assumed to always be black.
 		cr.setSource(0.0, 0.0, 0.0);
 		cr.moveTo(frame.x, frame.y);
 		cr.showLayout(layout);
 	}
 	
 	public void setFontSize(double size){
 	  fontDesc.setSize(size);
 	}
 
 	public void drawTextFrame(String text, Rectangle frame, double fontSize, double paraSpacing) {
 	  
 	  if (text == null) {
 	    logger.debug("No text to draw");
 	    return;
 	  }
 	  
 		Layout layout = new Layout(cr);
 		fontDesc.setSize(fontSize);
 		layout.setFontDescription(fontDesc);
 		layout.setWidth(frame.width);
 		layout.setJustify(true);
 
 		cr.setSource(0.0, 0.0, 0.0);
 
 		double y = frame.y;
 		
 		String[] statArr = text.split("<br>");
 		for (int i = 0; i < statArr.length; i++) {
 			cr.moveTo(frame.x, y);
 			layout.setMarkup(statArr[i]);
 			cr.showLayout(layout);
 			y += layout.getPixelHeight() + paraSpacing;
 		}
 	}
 	
 	/**
 	 * Annotates the frame with the text at the specified position and text anchor.
 	 * A {@link Rectangle} is returned that contains the dimensions of the text box.
 	 * 
 	 * @param text the text to display
 	 * @param x the x coordinate of the anchor
 	 * @param y the y coordinate of the anchor
 	 * @param anchor the {@link Anchor} position
 	 * @return the {@link Rectangle} defining the text extent.
 	 */
 	public Rectangle annotateMap(String text, double x, double y, Anchor anchor) {
 
 		Layout layout = new Layout(cr);
 		layout.setFontDescription(fontDesc);
 		layout.setMarkup(text);
 		
 		Rectangle rect = new Rectangle(x,y,layout.getSizeWidth(), layout.getSizeHeight());
 		Double pt = rect.getOrigin(anchor);
 
 		cr.moveTo(pt.x, pt.y);
 		cr.showLayout(layout);
 		
 		return rect;
 	}
 
 	/**
 	 * Draws already stored dataframes, grids and borders.
 	 */
 	public void drawDataFrames() {
 		for (DataFrame df : dataFrames) {
 			Point2D.Double origin = df.getOrigin();
 			cr.setSource(df.getSurface(), origin.x, origin.y);
 			cr.paint();
 			
 			//Free resources used by dataframe.
 			df.finish();
 			
 			if (df.hasBorder()) {
 				setColour(df.getBorderColour());
 				cr.setLineWidth(0.2);
 				cr.rectangle(origin.x, origin.y, df.getWidth(), df.getHeight());
 				cr.stroke();
 			}
 			
 			if (df.hasGrid()) {
 				drawMapGrid(4.5, df);
 			}
 		}
 	}
 
 	/**
 	 * Draws the passed key items vertically.
 	 * 
 	 * @param rect
 	 * @param legend
 	 * @param fontSize
 	 * @return the last y position 
 	 */
 	public double drawKey(Rectangle rect, List<MapKeyItem> legend, double fontSize) {
 		/*
 		 * FIXME: Hacks to draw custom fillstyles
 		 */
 		double y = rect.y;
 		double x = rect.x;
 
 		/*
 		 * Could be configuration options?
 		 */
 		double patchWidth = 25;
 		double patchHeight = 12.5;
 		double spacing = 22;
 		double textMargin = 35;
 
 		fontDesc.setSize(fontSize);
 		for (MapKeyItem li : legend) {
 		  if (li.point) {
         cr.arc(x, y, 1.75, 0, 2 * Math.PI);
         setFillColour(Palette.BLACK.get());
         setFillColour();
         cr.fillPreserve();
         setLineColour(Palette.BLACK.get());
         setLineColour();
         cr.stroke();
 		    continue;
       }
 			setFillColour(li.colour);
 			cr.rectangle(x, y, patchWidth, patchHeight);
 			cr.fillPreserve();
 			setLineColour(Palette.BLACK.get());
 			cr.setLineWidth(0.15);
 			cr.strokePreserve();
 			if (li.hatched) {
 				// don't clip preserve
 				cr.save();
 				cr.clip();
 				paintCrossHatch();
 				cr.restore();
 			} else if (li.stippled) {
 				// don't clip preserve
 				cr.save();
 				cr.clip();
 				paintStipple();
 				cr.restore();
 			} else if (li.duffy) {
 				cr.save();
 				cr.clip();
 				paintDuffy();
 				cr.restore();
 			}
 			cr.setDash(new double[] { 1, 0 });
 			cr.stroke();
 
 			annotateMap(li.description, x + textMargin, y + (patchHeight / 2), Anchor.LC);
 			y += spacing;
 		}
 		return y;
 	}
 
 	private void drawMapGrid(double fontSize, DataFrame df) {
 		/*
 		 * TODO fix hacking - hardcoding, over complexity; Pass in interval??
 		 * More of a callers job really.
 		 */
 		int intervals[] = { 40, 20, 10, 5, 2, 1 };
 		fontDesc.setSize(fontSize);
 		setColour(df.getGridColour());
 		cr.setLineWidth(0.2);
 
 		// only draw grids for dataframes
 		Envelope env = df.getEnvelope();
 		Point2D.Double origin = df.getOrigin();
 
 		int chosen_interval = 0;
 		HashMap<Integer, Integer> possible_intervals = new HashMap<Integer, Integer>();
 
 		for (int i = 0; i < intervals.length; i++) {
 			/*
 			 * Floor the max and ceiling the min to get the position of the
 			 * lower and upper grid lines Same logic for both axes
 			 * 
 			 * TODO: all very brittle, no checking, hardcoding, ... in fact not
 			 * quite sure how it works.
 			 */
 			int intervalSize = intervals[i];
 			int nintervalX = (int) (Math.floor(env.getMaxX() / intervalSize) - Math.ceil(env.getMinX() / intervalSize));
 			int nintervalY = (int) (Math.floor(env.getMaxY() / intervalSize) - Math.ceil(env.getMinY() / intervalSize));
 			int n = Math.min(nintervalX, nintervalY);
 			if (n > 1 && n < 7) {
 				possible_intervals.put(n, intervalSize);
 			}
 		}
 		
 		
 		/*
 		 * Get the minimum interval
 		 */
 		try {
 			Integer minInterval = Collections.min(possible_intervals.keySet());
 	    chosen_interval = possible_intervals.get(minInterval);
 		} catch(Exception e){
 			chosen_interval = 1;
 		}
 
 		double x;
 		double x1;
 		double x2;
 		double y;
 		double y1;
 		double y2;
 		int tic_length = 3;
 		String anno;
 		for (int i = -180; i <= 180; i += chosen_interval) {
 			// Dual purpose: does a meridian and parallel
 			Point2D.Double mapPt = df.transform(i, i);
 			if (i > env.getMinX() && i < env.getMaxX()) {
 				x = origin.x + mapPt.x;
 				y1 = origin.y - tic_length;
 				y2 = origin.y + df.getHeight() + tic_length;
 
 				// meridian
 				cr.moveTo(x, y1);
 				cr.lineTo(x, y2);
 				cr.stroke();
 				anno = AnnotationFactory.gridText(i, "WE");
 				annotateMap(anno, x, y1, Anchor.CB);
 				annotateMap(anno, x, y2, Anchor.CT);
 			}
 
 			if (i > env.getMinY() && i < env.getMaxY()) {
 				y = origin.y + mapPt.y;
 				x1 = origin.x - tic_length;
 				x2 = origin.x + df.getWidth() + tic_length;
 
 				// meridian
 				cr.moveTo(x1, y);
 				cr.lineTo(x2, y);
 				cr.stroke();
 				anno = AnnotationFactory.gridText(i, "SN");
 				annotateMap(anno, x1, y, Anchor.RC);
 				annotateMap(anno, x2, y, Anchor.LC);
 			}
 
 		}
 
 	}
 
 	/**
 	 * Add a dataframe for later usage in drawing grids and borders.
 	 * 
 	 * @param df A dataframe
 	 */
 	public void addDataFrame(DataFrame df) {
 		dataFrames.add(df);
 	}
 
   public void drawImage(Rectangle frame, Pixbuf pbTri) {
     cr.save();
     
     double sx = frame.width / (double) pbTri.getWidth();
     double sy = frame.height / (double) pbTri.getHeight();
     
     cr.scale(sx, sy);
     cr.setSource(pbTri, frame.x/sx, frame.y/sy);
     
     cr.paint();
     
     cr.restore();
   }
 
   public void drawSVG(Rectangle frame, Handle h) {
     cr.save();
     
     double sx = frame.width / h.getDimensions().getWidth();
     double sy = frame.height / h.getDimensions().getHeight();
     logger.debug("sx: " + sx);
     logger.debug("sy: " + sy);
     
     cr.scale(sx, sy);
     cr.translate(frame.x/sx, frame.y/sy);
     cr.showHandle(h);
     
     cr.restore();
   }
 
   public void drawScaleBar(Rectangle frame, double scale, double barHeight, String units, double fontSize) {
     setFontSize(fontSize);
     cr.setSource(0.0, 0.0, 0.0);
     
     ScaleBar sb = new ScaleBar(frame.width, scale);
     
     double x = frame.x;
     //Draw zero
     annotateMap("0", x, frame.y, Anchor.CB);
     
     double intervalWidth = sb.getIntervalWidth();
     
     List<String> annotations = sb.getAnnotations();
     for (int i = 0; i < annotations.size(); i++) {
       
       String annotation = annotations.get(i);
       cr.rectangle(x, frame.y, intervalWidth, barHeight);
       
       if (i % 2 == 0) {
         cr.fillPreserve();
       }
       
       cr.stroke();
       x += intervalWidth;
       
       Rectangle rect = annotateMap(annotation, x, frame.y - 2, Anchor.CB);
       
       if (i == annotations.size() - 1) {
 		    annotateMap(units, rect.x + (rect.width / 2) + 5, rect.y, Anchor.LB);
       }
     }
     
   }
 
   	public void drawColourScale(ContinuousScale scale, Rectangle rect, String description, String title) {
   		Double startPoint; 
   		Double endPoint; 
   		
   		boolean horizontal = true;
   		if (rect.height >= rect.width) {
   		  horizontal = false;
   		}
   		
   		if (horizontal) {
   			startPoint = new Point2D.Double(rect.x, rect.y);
   			endPoint = new Point2D.Double(rect.x + rect.width, rect.y);
   		} else {
   			startPoint = new Point2D.Double(rect.x, rect.y + rect.height);
   			endPoint = new Point2D.Double(rect.x, rect.y);
   		}
   		
   		LinearPattern lp = new LinearPattern(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
   		
   		double[] cs;
   		
   		List<ColourStop> colourStops = scale.getColourStops();
   		
       double normalization = scale.getNormalization();
       
       for (ColourStop colourStop : colourStops) {
   			cs = colourStop.colourStop;
   			lp.addColorStopRGB(cs[0]*normalization, cs[1]*normalization, cs[2]*normalization, cs[3]*normalization);
   		}
   		
   		cr.save();
   		cr.setSource(lp);
   		cr.rectangle(rect.x, rect.y, rect.width, rect.height);
   		cr.fillPreserve();
   		
   		//TODO: hardcoded source colour
   		cr.setSource(0, 0, 0);
   		//TODO: hardcoded linewidth
   		cr.setLineWidth(0.2);
   		cr.stroke();
   		cr.restore();
   		
   		
   		List<ScaleAnnotation> scaleAnnotations = scale.getScaleAnnotations();
       for (ScaleAnnotation colourStop : scaleAnnotations) {
   			if (colourStop.annotation == null)
   				continue;
   			if (!horizontal) {
   		    	annotateMap(
   		    			colourStop.annotation, 
   		    			rect.x+rect.width+5, 
   						(rect.y + rect.height) - (rect.height * (colourStop.ratio * normalization)), 
   						Anchor.LC
   				);
   			} else {
   		    	annotateMap(
   		    			colourStop.annotation, 
   		    			(rect.x + (rect.width * (colourStop.ratio * normalization))),
   						(rect.y + rect.height),
   						Anchor.CT
   				);
   			}
   		}
   		
       	
   		//TODO: hackery with description
  		if (description == null || description.isEmpty()) {
   	    	annotateMap(title, rect.x, rect.y-5, Anchor.LB);
   		} else {
   	    	annotateMap(title, rect.x, rect.y-18, Anchor.LB);
   	    	annotateMap(description, rect.x, rect.y-5, Anchor.LB);
   		}
   	}
 
 }
 
 
