 /*
  * Copyright 2006 jWic group (http://www.jwic.de)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * de.jwic.ecolib.controls.ChartControl.java
  * Created on Sep 14, 2006
  * $Id: ChartControl.java,v 1.2 2006/09/23 20:46:47 lordsam Exp $
  * @author jbornema
  */
 package de.jwic.ecolib.controls.chart;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.Serializable;
 
 import javax.imageio.IIOImage;
 import javax.imageio.ImageWriteParam;
 import javax.imageio.ImageWriter;
 import javax.imageio.stream.MemoryCacheImageOutputStream;
 
 import org.jfree.chart.JFreeChart;
 
 import com.sun.imageio.plugins.png.PNGImageWriter;
 
 import de.jwic.base.IControlContainer;
 import de.jwic.controls.ImageControl;
 
 /* 
  * ChartControl uses JFreeChart library to create nice chart images.
  * 
  * Please visit http://www.jfree.org/jfreechart for documentation and more info!
  *  
  * Created on Jun 7, 2006
  * @author jbornema
  */
 
 public class ChartControl extends ImageControl implements Serializable {
 
 	protected JFreeChart chart = null;
	protected long rendered = 0;
 	
 	/**
 	 * Constructor.
 	 * @param container
 	 */
 	public ChartControl(IControlContainer container) {
 		this(container, null);
 	}
 	
 	/**
 	 * Constructor.
 	 * @param container
 	 * @param name
 	 */
 	public ChartControl(IControlContainer container, String name) {
 		super(container, name);
 	}
 	
	/* (non-Javadoc)
 	 * @see de.jwic.controls.ImageControl#getFilename()
 	 */
 	public String getFilename() {
 		if (chart == null) {
 			return getName();
 		}
		return getName() + "_" + chart.getTitle() + "(" + rendered++ + "," + System.currentTimeMillis() + ").png";				
 	}
 
 	/* (non-Javadoc)
 	 * @see de.jwic.controls.ImageControl#renderImage()
 	 */
 	public void renderImage() throws IOException {
 		// create image to draw into
 		BufferedImage bi = new BufferedImage(width < 10 ? 10 : width, height < 10 ? 10 : height, BufferedImage.TYPE_INT_ARGB);
 		Graphics2D g2d = bi.createGraphics();
 		
 		if (chart != null) {		
 			chart.setBackgroundPaint(Color.WHITE);
 			chart.draw(g2d, new Rectangle2D.Double(0, 0, width < 10 ? 10 : width, height < 10 ? 10 : height));
 		} else {			
 			g2d.setColor(Color.BLACK);
 			g2d.drawString("No chart has been assigned.", 1, 20);
 		}
 		// finish drawing
 		g2d.dispose();
 		
 		// write image data into output stream
 		ByteArrayOutputStream out = getImageOutputStream();
 		out.reset();
 		// create a PNG image
 		ImageWriter imageWriter = new PNGImageWriter(null);
 		ImageWriteParam param = imageWriter.getDefaultWriteParam();
 		imageWriter.setOutput(new MemoryCacheImageOutputStream(out));
 		imageWriter.write(null, new IIOImage(bi, null, null), param);
 		imageWriter.dispose();
 		
 		setMimeType(MIME_TYPE_PNG);
 	}
 
 	/**
 	 * Set JFreeChart to display.
 	 * @param chart
 	 */
 	public void setChart(JFreeChart chart) {
 		this.chart = chart;
 		requireRedraw();
 	}
 	
 	/**
 	 * @return Returns the JFreeChart.
 	 */
 	public JFreeChart getChart() {
 		return chart;
 	}
 
 }
