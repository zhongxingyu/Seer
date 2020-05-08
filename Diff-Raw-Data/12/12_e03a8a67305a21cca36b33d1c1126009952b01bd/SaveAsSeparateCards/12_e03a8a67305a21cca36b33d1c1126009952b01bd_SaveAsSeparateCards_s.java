 package org.jskat.extract;
 
 import java.awt.Rectangle;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import org.apache.batik.transcoder.SVGAbstractTranscoder;
 import org.apache.batik.transcoder.TranscoderException;
 import org.apache.batik.transcoder.TranscoderInput;
 import org.apache.batik.transcoder.TranscoderOutput;
 import org.apache.batik.transcoder.image.PNGTranscoder;
 
 public class SaveAsSeparateCards {
 
 	static PNGTranscoder trans = new PNGTranscoder();
 
 	public static void tile(TranscoderInput input, TranscoderOutput output,
 			Rectangle aoi, float scale) throws TranscoderException {
 		// Set hints to indicate the dimensions of the output image
 		// and the input area of interest.
 		trans.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, new Float(
 				aoi.width) * scale);
 		trans.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, new Float(
 				aoi.height) * scale);
 		trans.addTranscodingHint(SVGAbstractTranscoder.KEY_AOI, aoi);
 
 		trans.transcode(input, output);
 	}
 
 	public static void convertAllCards(ExtractConfiguration conf)
 			throws TranscoderException, IOException {
 
 		// Transcode the file.
 		String svgURI = new File(conf.location).toURL().toString();
 		TranscoderInput input = new TranscoderInput(svgURI);
 
 		for (int yCard = 0; yCard < conf.columnSymbols.length(); yCard++) {
 			for (int xCard = 0; xCard < conf.rowSymbols.length(); xCard++) {
 
 				String cardName = String.valueOf(conf.columnSymbols
 						.charAt(yCard))
 						+ "-"
 						+ String.valueOf(conf.rowSymbols.charAt(xCard))
 						+ ".png";
 
				OutputStream ostream = new FileOutputStream(cardName);
 				TranscoderOutput output = new TranscoderOutput(ostream);
 
 				int cardWidth = conf.width / conf.columns;
 				int cardHeight = conf.height / conf.rows;
 
 				tile(input, output, new Rectangle(cardWidth * xCard, cardHeight
 						* yCard, cardWidth, cardHeight), conf.scale);
 
 				// Flush and close the output.
 				ostream.flush();
 				ostream.close();
 
 				System.out.println(cardName + " created");
 			}
 		}
 	}
 }
