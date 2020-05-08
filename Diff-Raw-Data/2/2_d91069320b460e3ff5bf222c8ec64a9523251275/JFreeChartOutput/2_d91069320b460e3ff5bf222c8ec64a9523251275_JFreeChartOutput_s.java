 /*
  * Copyright 2007 Pentaho Corporation.  All rights reserved. 
  * This software was developed by Pentaho Corporation and is provided under the terms 
  * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
  * this file except in compliance with the license. If you need a copy of the license, 
  * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
  * BI Platform.  The Initial Developer is Pentaho Corporation.
  *
  * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
  * the license for the specific language governing your rights and limitations.
  *
  * @created Mar 4, 2008 
  * @author wseyler
  */
 
 
 package org.pentaho.chart.plugin.jfreechart.outputs;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.Writer;
 import java.io.StringWriter;
 
 import org.jfree.chart.ChartRenderingInfo;
 import org.jfree.chart.ChartUtilities;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.entity.StandardEntityCollection;
 import org.jfree.chart.imagemap.ImageMapUtilities;
 import org.pentaho.chart.plugin.api.IOutput;
 import org.pentaho.chart.plugin.api.PersistenceException;
 
 
 /**
  * @author wseyler
  *
  */
 public class JFreeChartOutput implements IOutput {
 
   private JFreeChart chart;
   private ChartRenderingInfo info;
 
   public JFreeChartOutput(final JFreeChart chart)
   {
     if (chart == null)
     {
       throw new NullPointerException();
     }
     this.chart = chart;
   }
 
   //  /* (non-Javadoc)
 //   * @see org.pentaho.chart.plugin.api.IOutput#persistChart(java.lang.String, int)
 //   */
 //  public void persistChart(String filePath, IOutput.OutputTypes fileType) throws PersistenceException {
 //    info = new ChartRenderingInfo(new StandardEntityCollection());
 //    if (filePath != null && filePath.length() > 0) {
 //      if (fileType == IOutput.OutputTypes.FILE_TYPE_JPEG) {
 //        try {
 //          ChartUtilities.saveChartAsJPEG(new File(filePath), chart, 400, 400, info);
 //        } catch (IOException e) {
 //          throw new PersistenceException(e);
 //        }
 //      } else if (fileType == IOutput.OutputTypes.FILE_TYPE_PNG) {
 //        try {
 //          ChartUtilities.saveChartAsPNG(new File(filePath), chart, 400, 400, info);
 //        } catch (IOException e) {
 //          throw new PersistenceException(e);
 //        }
 //      }
 //    }
 //
 //  }
 
   /* (non-Javadoc)
    * @see org.pentaho.chart.plugin.api.IOutput#persistChart(java.io.OutputStream, int)
    */
   public OutputStream persistChart(OutputStream outputStream, IOutput.OutputTypes fileType, int width, int height) throws PersistenceException {
     info = new ChartRenderingInfo(new StandardEntityCollection());
     if (outputStream == null) {
       outputStream = new ByteArrayOutputStream();
     }
     
     try {
       outputStream.flush();
     } catch (IOException e1) {
       throw new PersistenceException(e1);
     }
     if (fileType == IOutput.OutputTypes.FILE_TYPE_JPEG) {
       try {
         ChartUtilities.writeChartAsJPEG(outputStream, chart, width, height, info);
       } catch (IOException e) {
         throw new PersistenceException(e);
       }
    } else if (fileType == IOutput.OutputTypes.FILE_TYPE_PNG) {
       try {
         ChartUtilities.writeChartAsPNG(outputStream, chart, width, height, info);
       } catch (IOException e) {
         throw new PersistenceException(e);
       }
     }
     return outputStream;
   }
 
   /* (non-Javadoc)
    * @see org.pentaho.chart.plugin.api.IOutput#persistMap(java.io.OutputStream, java.lang.String)
    */
   public Writer persistMap(Writer outputStream, String mapName) throws PersistenceException {
     if (outputStream == null) {
       outputStream = new StringWriter();
     }
 
     final String mapString = getMap(mapName);
     try {
       outputStream.write(mapString);
       outputStream.flush();
     } catch (IOException e) {
       throw new PersistenceException(e);
     }
     return outputStream;
   }
 
   /* (non-Javadoc)
    * @see org.pentaho.chart.plugin.api.IOutput#getMap(java.lang.String)
    */
   private String getMap(String mapName) {
     return ImageMapUtilities.getImageMap(mapName, info);
   }
 //
 //  /* (non-Javadoc)
 //   * @see org.pentaho.chart.plugin.api.IOutput#draw(java.awt.Graphics2D, java.awt.geom.Rectangle2D)
 //   */
 //  public void draw(Graphics2D graphics, Rectangle2D rectangle) {
 //    info = new ChartRenderingInfo(new StandardEntityCollection());
 //    chart.draw(graphics, rectangle, info);
 //  }
 
   public Object getDrawable()
   {
     return chart;
   }
 }
