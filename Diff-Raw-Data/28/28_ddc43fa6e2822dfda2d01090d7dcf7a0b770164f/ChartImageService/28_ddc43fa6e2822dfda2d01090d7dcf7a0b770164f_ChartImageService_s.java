 /* 
  * This file is part of the Echo2 Chart Library.
  * Copyright (C) 2005 NextApp, Inc.
  *
  * Version: MPL 1.1/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * Alternatively, the contents of this file may be used under the terms of
  * either the GNU General Public License Version 2 or later (the "GPL"), or
  * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the MPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the MPL, the GPL or the LGPL.
  */
 
 package nextapp.echo2.chart.webcontainer;
 
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.jfree.chart.JFreeChart;
 
 import nextapp.echo2.app.Extent;
 import nextapp.echo2.chart.app.ChartDisplay;
 import nextapp.echo2.webcontainer.ContainerInstance;
 import nextapp.echo2.webcontainer.RenderContext;
 import nextapp.echo2.webcontainer.image.PngEncoder;
 import nextapp.echo2.webcontainer.propertyrender.ExtentRender;
 import nextapp.echo2.webrender.Connection;
 import nextapp.echo2.webrender.ContentType;
 import nextapp.echo2.webrender.Service;
 import nextapp.echo2.webrender.WebRenderServlet;
 
 /**
  * <code>Service</code> to render chart images.
  */
 public class ChartImageService 
 implements Service {

    private static final int DEFAULT_WIDTH = 400;
    private static final int DEFAULT_HEIGHT = 300;
     
     /**
      * Parameter keys used in generating service URI.
      */
     private static final String[] URI_PARAMETER_KEYS = {"chartId", "v"}; 
     
     /**
      * Returns the appropriate URI to display a chart for a specific 
      * <code>ChartDisplay</code> component.
      * 
      * @param rc the relevant <code>RenderContext</code>
      * @param chartDisplay the rendering <code>ChartDisplay</code>
      * @param version the version number of the chart 
      *        (incremented when chart is re-rendered due to an update)
      * @return the URI
      */
     static final String getUri(RenderContext rc, ChartDisplay chartDisplay, int version) {
         return rc.getContainerInstance().getServiceUri(INSTANCE, URI_PARAMETER_KEYS, new String[]{chartDisplay.getRenderId(), 
                 Integer.toString(version)});
     }
     
     /**
      * Singleton instance.
      */
     private static final ChartImageService INSTANCE = new ChartImageService();
     
     static {
         WebRenderServlet.getServiceRegistry().add(INSTANCE);
     }
     
     /** Self-instantiated singleton. */
     private ChartImageService() { }
     
     /**
      * @see nextapp.echo2.webrender.Service#getId()
      */
     public String getId() {
         return "Echo2Chart.ImageService";
     }
 
     /**
      * @see nextapp.echo2.webrender.Service#getVersion()
      */
     public int getVersion() {
         return DO_NOT_CACHE;
     }
 
     /**
      * @see nextapp.echo2.webrender.Service#service(nextapp.echo2.webrender.Connection)
      */
     public void service(Connection conn) throws IOException {
         try {
             ContainerInstance containerInstance = (ContainerInstance) conn.getUserInstance();
             HttpServletRequest request = conn.getRequest();
             String chartId = request.getParameter("chartId");
             ChartDisplay chartDisplay = (ChartDisplay) containerInstance.getApplicationInstance().getComponentByRenderId(chartId);
             synchronized (chartDisplay) {
                 if (chartDisplay == null || !chartDisplay.isRenderVisible()) {
                     throw new IllegalArgumentException("Invalid chart id.");
                 }
                 
                 int width = ExtentRender.toPixels((Extent) chartDisplay.getRenderProperty(ChartDisplay.PROPERTY_WIDTH), 
                        DEFAULT_WIDTH);
                 int height = ExtentRender.toPixels((Extent) chartDisplay.getRenderProperty(ChartDisplay.PROPERTY_HEIGHT), 
                        DEFAULT_HEIGHT);
                 JFreeChart chart =  chartDisplay.getChart();
                 BufferedImage image;
                 synchronized (chart) {
                     image = chart.createBufferedImage(width, height);
                 }
                 PngEncoder encoder = new PngEncoder(image, true, null, 3);
                 conn.setContentType(ContentType.IMAGE_PNG);
                 OutputStream out = conn.getOutputStream();
                 encoder.encode(out);
             }
         } catch (IOException ex) {
             // Internet Explorer appears to enjoy making half-hearted requests for images, wherein it resets the connection
             // leaving us with an IOException.  This exception is silently eaten.
             ex.printStackTrace();
         }
     }
 }
