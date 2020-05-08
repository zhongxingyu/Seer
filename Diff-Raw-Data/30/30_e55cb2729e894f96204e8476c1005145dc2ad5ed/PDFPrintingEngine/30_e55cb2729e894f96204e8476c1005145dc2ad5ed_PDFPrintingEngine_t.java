 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.awe.report.pdf;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 
 import net.refractions.udig.project.IMap;
 import net.refractions.udig.project.internal.command.navigation.SetViewportBBoxCommand;
 import net.refractions.udig.project.render.RenderException;
 import net.refractions.udig.project.ui.ApplicationGIS;
 import net.refractions.udig.project.ui.SelectionStyle;
 import net.refractions.udig.project.ui.ApplicationGIS.DrawMapParameter;
 
 import org.amanzi.awe.report.charts.Charts;
 import org.amanzi.awe.report.model.Chart;
 import org.amanzi.awe.report.model.IReportPart;
 import org.amanzi.awe.report.model.Report;
 import org.amanzi.awe.report.model.ReportMap;
 import org.amanzi.awe.report.model.ReportText;
 import org.apache.log4j.Logger;
 import org.jfree.chart.ChartUtilities;
 import org.jfree.chart.JFreeChart;
 
 import com.lowagie.text.Document;
 import com.lowagie.text.Image;
 import com.lowagie.text.PageSize;
 import com.lowagie.text.Paragraph;
 import com.lowagie.text.Rectangle;
 import com.lowagie.text.pdf.PdfContentByte;
 import com.lowagie.text.pdf.PdfWriter;
 
 /**
  * TODO Purpose of
  * <p>
  * </p>
  * 
  * @author Pechko_E
  * @since 1.0.0
  */
 public class PDFPrintingEngine {
     private static final Logger LOGGER = Logger.getLogger(PDFPrintingEngine.class);
     private int top;
     private int left;
     private final String REPORT_DIRECTORY="Amanzi report";
     public void printReport(Report report) {
         top=0;
         final Rectangle paperSize = PageSize.A4;
         Rectangle paperRectangle = paperSize;
         Document document = new Document(paperRectangle, 0f, 0f, 0f, 0f);
         String fileName=System.getProperty("user.home")+File.separator+REPORT_DIRECTORY;
         File directory = new File(fileName);
         if (!directory.exists()) {
             directory.mkdir();
         }
         String reportFileName = report.getFile();
         if (reportFileName == null || reportFileName.length() == 0) {
             fileName += File.separator + "report" + System.currentTimeMillis() + ".pdf";
         } else {
             fileName += File.separator + reportFileName;
         }
         LOGGER.debug("filename "+fileName);
         File outputPdfFile = new File(fileName);
 
         PdfWriter writer=null;
         try {
             writer = PdfWriter.getInstance(document, new FileOutputStream(outputPdfFile));
             writer.setPageEvent(new AWEPageEvent());
             document.open();
             PdfContentByte cb = writer.getDirectContent();
             Graphics2D graphics = cb.createGraphics(paperRectangle.getWidth(), paperRectangle.getHeight());
             final Paragraph title = new Paragraph(report.getName());
             title.setIndentationLeft(30);
             title.setIndentationRight(30);
             title.setAlignment(Paragraph.ALIGN_CENTER);
             document.add(title);
             
             final List<IReportPart> parts = report.getParts();
             for (IReportPart part : parts) {
                 if (part instanceof ReportMap) {
                     IMap map = ((ReportMap)part).getMap();
 //                    BufferedImage bI = new BufferedImage((int)width, (int)height, BufferedImage.TYPE_INT_ARGB);
                     BufferedImage bI = new BufferedImage(part.getWidth(), part.getHeight(), BufferedImage.TYPE_INT_ARGB);
                     Graphics graphics2 = bI.getGraphics();
 
                     draw((Graphics2D)graphics2,map, part.getWidth(), part.getHeight());
                     final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                     ImageIO.write(bI, "png", baos);
                     Image img = Image.getInstance(baos.toByteArray());
                     float scalefactorH=Math.min(1,paperSize.getHeight()/img.getHeight());
                     float scalefactorW=Math.min(1,(paperSize.getWidth()-2*30)/img.getWidth());
                     float scalefactor=Math.min(scalefactorH, scalefactorW);
                     img.scalePercent(100*scalefactor);
                     img.setIndentationLeft(30);
                     img.setIndentationRight(30);
                     document.add(img);
                 }else if (part instanceof Chart){
 //                    document.newPage();
                     Chart chart=(Chart)part;
                     final JFreeChart jFreeChart = Charts.createChart(chart);
                     jFreeChart.setTitle(chart.getTitle());
                     ChartUtilities.applyCurrentTheme(jFreeChart);
                     final BufferedImage bI = jFreeChart.createBufferedImage(chart.getWidth(), chart.getHeight());
                     Graphics graphics2 = bI.getGraphics();
                     jFreeChart.draw((Graphics2D)graphics2, new java.awt.Rectangle(chart.getWidth(), chart.getHeight()));
                     final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                     ImageIO.write(bI, "png", baos);
                     Image img = Image.getInstance(baos.toByteArray());
                     float scalefactorH=Math.min(1,paperSize.getHeight()/img.getHeight());
                     float scalefactorW=Math.min(1,(paperSize.getWidth()-2*30)/img.getWidth());
                     float scalefactor=Math.min(scalefactorH, scalefactorW);
                     img.scalePercent(100*scalefactor);
                     img.setIndentationLeft(30);
                     img.setIndentationRight(30);
                     document.add(img);
                 }else if (part instanceof ReportText){
                     ReportText text=(ReportText)part;
                     final Paragraph paragraph = new Paragraph(text.getText());
                     paragraph.setIndentationLeft(30);
                     paragraph.setIndentationRight(30);
                     document.add(paragraph);
                 }
             }
             graphics.dispose();
             document.newPage();
         } catch (Exception e) {
            LOGGER.error(e);
         } finally{
         document.close();
         writer.close();
         }
     }
     public void draw( Graphics2D graphics, IMap map, int width, int height) {
         try {
             Dimension size = new Dimension(width, height);
             //reduce set a 1 pixel clip bound around outside to prevent 
             //some Graphics2D implementations (itext!) from bleeding into space
             //outside the graphics canvas
             graphics.setClip(1, 1, size.width-2, size.height-2);
             
             java.awt.Dimension awtSize = new java.awt.Dimension(
                     size.width, size.height);
             IMap modifiedMap = null;
                 //ApplicationGIS.drawMap(new DrawMapParameter(graphics, awtSize, getMap(), monitor, true));
                 modifiedMap = ApplicationGIS.drawMap(new DrawMapParameter(graphics, awtSize, map, null /*use current scale*/, 90, SelectionStyle.EXCLUSIVE_ALL_SELECTION, null, true, true));
             
 
             //ApplicationGIS.drawMap makes a copy of the map, and may change its bounds.  If it does change
             //the bounds then update the original map to match (this will force the mapgraphics to update too)
             if (!map.getViewportModel().getBounds().equals(modifiedMap.getViewportModel().getBounds())) {
                 SetViewportBBoxCommand cmdBBox = new SetViewportBBoxCommand(modifiedMap.getViewportModel().getBounds());
                 map.sendCommandSync(cmdBBox);
             }
             
             //restore regular clip rectangle
             graphics.setClip(0, 0, size.width, size.height);
             
         } catch (RenderException e) {
             e.printStackTrace();
         }
     }
 }
