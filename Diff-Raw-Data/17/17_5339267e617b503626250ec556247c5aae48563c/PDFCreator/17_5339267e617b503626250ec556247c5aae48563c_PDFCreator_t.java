 package de.ptb.epics.eve.util.pdf;
 
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.apache.pdfbox.exceptions.COSVisitorException;
 import org.apache.pdfbox.pdmodel.PDDocument;
 import org.apache.pdfbox.pdmodel.PDPage;
 import org.apache.pdfbox.pdmodel.common.PDRectangle;
 import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
 import org.apache.pdfbox.pdmodel.font.PDFont;
 import org.apache.pdfbox.pdmodel.font.PDType1Font;
 import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 
 /**
  * @author Marcus Michalsky
  * @since 1.16
  */
 public final class PDFCreator {
 	
 	private PDFCreator() {
 	}
 	
 	private static final Logger LOGGER = Logger.getLogger(PDFCreator.class
 			.getName());
 	
 	/**
 	 * 
 	 * @param image
 	 * @param file
 	 * @param scanDescription
 	 * @param statList
 	 * @param monitor
 	 * @return
 	 */
 	public static IStatus createPlotPage(BufferedImage image, File file,
 			String scanDescription, List<PlotStats> statList,
 			IProgressMonitor monitor) {
 		try {
 			monitor.beginTask("creating PDF", 8);
 			monitor.subTask("creating document");
 			PDDocument document = new PDDocument();
 			PDPage page = new PDPage(PDPage.PAGE_SIZE_A4);
 			page.setRotation(90);
 			document.addPage(page);
 			
 			monitor.worked(1);
 			if (monitor.isCanceled()) {
 				return Status.CANCEL_STATUS;
 			}
 			
 			monitor.subTask("calculating plot image ratio");
 			
 			PDRectangle pdfPageSize = page.findMediaBox();
 			float margin = 28.33f;
 			
 			int headerHeight = 25;
 			
 			final int tableCol1X = 550;
 			final int tableCol2X = 620;
 			final int tableCol3X = 720;
 			
 			int table1HeadY = 560;
 			int table2HeadY = (int)pdfPageSize.getWidth()/2-10;
 			
 			int plotWidth = tableCol1X - 15 - (int)margin;
 			float plotXRatio = image.getWidth() / plotWidth;
 			int plotHeight = (int)(image.getHeight() * plotXRatio);
 			
 			if (plotHeight > pdfPageSize.getWidth() - headerHeight - 2 * margin) {
 				plotHeight = (int) (pdfPageSize.getWidth() - headerHeight - 2 * margin);
 			}
 			
 			monitor.worked(1);
 			if (monitor.isCanceled()) {
 				return Status.CANCEL_STATUS;
 			}
 			
 			monitor.subTask("preparing image");
 			
 			BufferedImage resizedImage = new BufferedImage(plotWidth,
 					plotHeight, BufferedImage.TYPE_INT_RGB);
 			Graphics2D g = resizedImage.createGraphics();
 			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
 					RenderingHints.VALUE_INTERPOLATION_BICUBIC);
 			g.setRenderingHint(RenderingHints.KEY_RENDERING,
 					RenderingHints.VALUE_RENDER_QUALITY);
 			//g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 					//RenderingHints.VALUE_ANTIALIAS_ON);
 			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
 					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 			g.drawImage(image, 0, 0, plotWidth, plotHeight, null);
 			g.dispose();
 			
 			PDJpeg ximage = new PDJpeg(document, resizedImage, 1.0f);
 			
 			monitor.worked(1);
 			if (monitor.isCanceled()) {
 				return Status.CANCEL_STATUS;
 			}
 			
 			monitor.subTask("creating fonts");
 			
 			PDFont font = PDType1Font.HELVETICA;
			PDFont fontBold = PDType1Font.HELVETICA_BOLD;
 			
 			float fontSize = 12.0f;
 			float lineHeight = 25;
 			
 			PDRectangle pageSize = page.findMediaBox();
 			float pageWidth = pageSize.getWidth();
 			
 			monitor.worked(1);
 			if (monitor.isCanceled()) {
 				return Status.CANCEL_STATUS;
 			}
 			
 			monitor.subTask("setting landscape mode");
 			
 			PDPageContentStream contentStream = new PDPageContentStream(
 					document, page);
 			// add the rotation using the current transformation matrix
 			// including a translation of pageWidth to use the lower left corner
 			// as 0,0 reference
 			contentStream.concatenate2CTM(0, 1, -1, 0, pageWidth, 0);
 			
 			contentStream.beginText();
			contentStream.setFont(fontBold, 14);
 			contentStream.moveTextPositionByAmount(margin, table1HeadY);
 			contentStream.drawString((scanDescription == null) ? "noName"
 					: scanDescription + " - "
 							+ Calendar.getInstance().getTime().toString());
 			contentStream.endText();
 			
 			contentStream.drawImage(ximage, margin, margin);
 			
 			monitor.worked(1);
 			if (monitor.isCanceled()) {
 				return Status.CANCEL_STATUS;
 			}
 			
 			if (statList.size() > 0) {
 				monitor.subTask("creating first table");
 				PlotStats stats1 = statList.get(0);
 				contentStream.beginText();
				contentStream.setFont(fontBold, fontSize);
 				contentStream.moveTextPositionByAmount(tableCol2X, table1HeadY);
 				contentStream.drawString(stats1.getDetectorName());
 				contentStream.endText();
 				
 				contentStream.beginText();
				contentStream.setFont(fontBold, fontSize);
 				contentStream.moveTextPositionByAmount(tableCol3X, table1HeadY);
 				contentStream.drawString(stats1.getMotorName());
 				contentStream.endText();
 				
 				contentStream.drawLine(tableCol1X, table1HeadY - 5,
 						pdfPageSize.getHeight() - margin, table1HeadY - 5);
 				contentStream.drawLine(tableCol2X - 5, table1HeadY + 15,
 						tableCol2X - 5, table2HeadY + 35);
 				contentStream.drawLine(tableCol3X - 5, table1HeadY + 15,
 						tableCol3X - 5, table2HeadY + 35);
 				
 				contentStream.beginText();
 				contentStream.setFont(font, fontSize);
 				contentStream.moveTextPositionByAmount(tableCol1X, table1HeadY-25);
 				contentStream.drawString("Minimum");
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString("Maximum");
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString("Center");
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString("Edge");
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString("Average");
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString("Deviation");
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString("FWHM");
 				contentStream.endText();
 				
 				contentStream.beginText();
 				contentStream.setFont(font, fontSize);
 				contentStream.moveTextPositionByAmount(tableCol2X, table1HeadY-25);
 				contentStream.drawString(stats1.getMinimum().getR());
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString(stats1.getMaximum().getR());
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString(stats1.getCenter().getR());
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString(stats1.getEdge().getR());
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString(stats1.getAverage().getR());
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString(stats1.getDeviation().getR());
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString(stats1.getFullWidthHalfMinimum()
 						.getR());
 				contentStream.endText();
 				
 				contentStream.beginText();
 				contentStream.setFont(font, fontSize);
 				contentStream.moveTextPositionByAmount(tableCol3X, table1HeadY-25);
 				contentStream.drawString(stats1.getMinimum().getL());
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString(stats1.getMaximum().getL());
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString(stats1.getCenter().getL());
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString(stats1.getEdge().getL());
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString(stats1.getAverage().getL());
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString(stats1.getDeviation().getL());
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString(stats1.getFullWidthHalfMinimum()
 						.getL());
 				contentStream.endText();
 			}
 			
 			monitor.worked(1);
 			if (monitor.isCanceled()) {
 				return Status.CANCEL_STATUS;
 			}
 			
 			if (statList.size() > 1) {
 				monitor.subTask("creating second table");
 				PlotStats stats2 = statList.get(1);
 				contentStream.beginText();
				contentStream.setFont(fontBold, fontSize);
 				contentStream.moveTextPositionByAmount(tableCol2X, table2HeadY);
 				contentStream.drawString(stats2.getDetectorName());
 				contentStream.endText();
 				
 				contentStream.beginText();
				contentStream.setFont(fontBold, fontSize);
 				contentStream.moveTextPositionByAmount(tableCol3X, table2HeadY);
 				contentStream.drawString(stats2.getMotorName());
 				contentStream.endText();
 				
 				contentStream.drawLine(tableCol1X, table2HeadY - 5,
 						pdfPageSize.getHeight() - margin, table2HeadY - 5);
 				contentStream.drawLine(tableCol2X - 5, table2HeadY + 15,
 						tableCol2X - 5, margin);
 				contentStream.drawLine(tableCol3X - 5, table2HeadY + 15,
 						tableCol3X - 5, margin);
 				
 				contentStream.beginText();
 				contentStream.setFont(font, fontSize);
 				contentStream.moveTextPositionByAmount(tableCol1X, table2HeadY-25);
 				contentStream.drawString("Minimum");
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString("Maximum");
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString("Center");
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString("Edge");
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString("Average");
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString("Deviation");
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString("FWHM");
 				contentStream.endText();
 				
 				contentStream.beginText();
 				contentStream.setFont(font, fontSize);
 				contentStream.moveTextPositionByAmount(tableCol2X, table2HeadY-25);
 				contentStream.drawString(stats2.getMinimum().getR());
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString(stats2.getMaximum().getR());
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString(stats2.getCenter().getR());
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString(stats2.getEdge().getR());
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString(stats2.getAverage().getR());
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString(stats2.getDeviation().getR());
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString(stats2.getFullWidthHalfMinimum()
 						.getR());
 				contentStream.endText();
 				
 				contentStream.beginText();
 				contentStream.setFont(font, fontSize);
 				contentStream.moveTextPositionByAmount(tableCol3X, table2HeadY-25);
 				contentStream.drawString(stats2.getMinimum().getL());
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString(stats2.getMaximum().getL());
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString(stats2.getCenter().getL());
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString(stats2.getEdge().getL());
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString(stats2.getAverage().getL());
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString(stats2.getDeviation().getL());
 				contentStream.moveTextPositionByAmount(0, -lineHeight);
 				contentStream.drawString(stats2.getFullWidthHalfMinimum()
 						.getL());
 				contentStream.endText();
 			}
 			
 			contentStream.close();
 			
 			monitor.worked(1);
 			if (monitor.isCanceled()) {
 				return Status.CANCEL_STATUS;
 			}
 			
 			monitor.subTask("saving file");
 			document.save(file);
 			document.close();
 			
 			monitor.done();
 		} catch (IOException e) {
 			LOGGER.error(e.getMessage(), e);
 		} catch (COSVisitorException e) {
 			LOGGER.error(e.getMessage(), e);
 		}
 		return Status.OK_STATUS;
 	}
 }
