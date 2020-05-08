 /*===========================================================================
   Copyright (C) 2010 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.steps.imagemodification;
 
 import java.awt.AlphaComposite;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.imageio.ImageIO;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.UsingParameters;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.pipeline.BasePipelineStep;
 import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
 import net.sf.okapi.common.pipeline.annotations.StepParameterType;
 import net.sf.okapi.common.resource.RawDocument;
 
 @UsingParameters(Parameters.class)
 public class ImageModificationStep extends BasePipelineStep {
 
 	private static final Logger LOGGER = Logger.getLogger(ImageModificationStep.class.getName());
 
 	private boolean isDone;
 	private Parameters params;
 	private URI outputURI;
 	private String currentSuffix;
 	private List<String> suffixes;
 
 	public ImageModificationStep () {
 		params = new Parameters();
 	}
 	
 	@Override
 	public String getName () {
		return "Image Resizing";
 	}
 
 	@Override
 	public String getDescription () {
 		return "Create a modified copy of image files. "
 			+ "Expects: image raw document. Sends back: image raw document.";
 	}
 
 	@Override
 	public boolean isDone () {
 		return isDone;
 	}
 
 	@Override
 	public IParameters getParameters () {
 		return this.params;
 	}
 
 	@Override
 	public void setParameters (IParameters params) {
 		this.params = (Parameters)params;
 	}
 
 	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_URI)
 	public void setOutputURI (URI outputURI) {
 		this.outputURI = outputURI;
 	}
 	
 	@Override
 	protected Event handleStartBatchItem (Event event) {
 		suffixes = new ArrayList<String>(Arrays.asList(ImageIO.getWriterFileSuffixes()));
 		isDone = false;
 		return event;
 	}
 
 	@Override
 	public Event handleRawDocument (Event event) {
 		RawDocument rd = event.getRawDocument();
 		try {
 			URI uri = rd.getInputURI();
 			if ( uri != null ) {
 				File inputFile = new File(uri);
 				String newFormat = params.getFormat();
 
 				// If needed: detect the format of the original file
 				if ( newFormat.isEmpty() ) {
 					String ext = Util.getExtension(inputFile.getAbsolutePath()).toLowerCase();
 					if ( ext.length() > 1 ) ext = ext.substring(1); // Remove period
 					if ( !ext.equalsIgnoreCase(currentSuffix) ) {
 						if ( suffixes.contains(ext) ) {
 							currentSuffix = ext;
 							newFormat = currentSuffix;
 						}
 						else { // No such writer, fall back to "png"
 							LOGGER.warning(String.format("No image writer available for '%s'. Using 'png' instead.", newFormat));
 							currentSuffix = "png";
 							newFormat = "png";
 						}
 					}
 				}
 				else {
 					// Else: newFormat is set to the proper value
 					// And we can change the extension of the output file
 					//TODO: extension of output
 				}
 
 				// Convert
 				BufferedImage img1 = ImageIO.read(inputFile);
 				BufferedImage img2 = createResizedCopy(img1);
 				
 				// Save the output
				// Open the output
 				File outFile;
 				if ( isLastOutputStep() ) {
 					outFile = new File(outputURI);
 					Util.createDirectories(outFile.getAbsolutePath());
 				}
 				else {
 					try {
 						outFile = File.createTempFile("okp-im_", ".tmp");
 					}
 					catch ( Throwable e ) {
 						throw new OkapiIOException("Cannot create temporary output.", e);
 					}
 					outFile.deleteOnExit();
 				}
 			    ImageIO.write(img2, newFormat, outFile);
 			}
 			else {
 				throw new OkapiIOException("Input type not supported (must be URI).");
 			}
 		}
 		catch ( Throwable e ) {
 			throw new OkapiIOException("Error while loading or modifying the image.", e);
 		}
 		finally {
 			isDone = true;
 		}		
 		return event;
 	}
 
 	private BufferedImage createResizedCopy (Image originalImage) {
 		Graphics2D g2d = null;
 		try {
 			// Compute the new size
 			int oriWidth = originalImage.getWidth(null);
 			int oriHeight = originalImage.getHeight(null);
 			Double tmp = oriWidth * (params.getScaleWidth() / 100.0);
 			int newWidth = (tmp.intValue() < 1 ? 1 : tmp.intValue());
 			tmp = oriHeight * (params.getScaleHeight() / 100.0);
 			int newHeight = (tmp.intValue() < 1 ? 1 : tmp.intValue());
 			
 			// Perform the resizing
 			BufferedImage scaledImg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB); 
			g2d = scaledImg.createGraphics(); 
 			g2d.setComposite(AlphaComposite.Src); 
 			g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
 			return scaledImg;
 		}
 		finally {
 			if ( g2d != null ) g2d.dispose(); 
 		}
 	}
 
 }
