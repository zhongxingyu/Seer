 package org.data2semantics.indexer;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.List;
 import java.util.Vector;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.pdfbox.examples.pdmodel.Annotation;
 import org.apache.pdfbox.pdmodel.PDDocument;
 import org.apache.pdfbox.pdmodel.PDPage;
 import org.apache.pdfbox.pdmodel.common.PDRectangle;
 import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
 import org.apache.pdfbox.pdmodel.font.PDFont;
 import org.apache.pdfbox.pdmodel.font.PDType1Font;
 import org.apache.pdfbox.pdmodel.graphics.color.PDGamma;
 import org.apache.pdfbox.pdmodel.interactive.action.type.PDActionURI;
 import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLine;
 import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
 import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationSquareCircle;
 import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
 import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
 import org.apache.pdfbox.util.PDFTextStripper;
 import org.apache.pdfbox.util.TextPosition;
 
 /**
  * Chunked PDF stripper extending PDFTextStripper overwriting processTextPosition.
  * The resulting chunks of texts keeps position information so that it can be used later on for visualization.
  * 
  * @author wibisono
  *
  */
 public class D2S_ChunkedPDFStripper extends PDFTextStripper {
 	
 	private Log log = LogFactory.getLog(D2S_ChunkedPDFStripper.class);
 	Vector<D2S_DocChunk> documentChunks = new Vector<D2S_DocChunk>();
 	File originalFile=null;
 	
 	public D2S_ChunkedPDFStripper() throws IOException {
 		super();
 		
 	}
 	
 	/**
 	 * Just convinient wrapper to process document, without writing since we are more interested on chunked result.
 	 * @param doc
 	 * @param pdfFile 
 	 */
 	public void processPDDocument(PDDocument doc, File pdfFile){
 
 		StringWriter dummyWriter = new StringWriter();
		documentChunks.clear();
 		originalFile = pdfFile;
 		try {
 			countPage=0;countChunk=0; 
 			beginNewChunk();
 			writeText(doc, dummyWriter);
 		} catch (IOException e) {
 			log.error("Failed to process PDDocument");
 		}
 	}
 	float top, left, bottom, right;
 	int countPage=0, countChunk = 0;
 
 	StringBuffer currentChunk;
 	
 	/**
 	 * This method is provided by PDFText stripper to be overwritten, 
 	 * It is called whenever original PDFText stripper process a character with position.
 	 * Now this processing is done while chunking the text, which ended with . (Assuming it will be a sentence).
 	 */
 	@Override
 	protected void processTextPosition(TextPosition text) {
 		updateChunkBoundingBox(text);
 		currentChunk.append(text.getCharacter());
 		if(text.getCharacter().equals(".")){
 			++countChunk;
 			D2S_DocChunk newChunk = new D2S_DocChunk( originalFile.getName(),
 					countPage, countChunk, currentChunk.toString(), 
 					top, left, bottom, right); 
 			documentChunks.add(newChunk);
 			beginNewChunk();
 		}
 	}
 	
 	
 	/**
 	 * Called by parent everytime we started a new page.
 	 */
 	@Override
 	protected void startPage(PDPage page) throws IOException {
 		// TODO Auto-generated method stub
 		super.startPage(page);
 		countPage ++;
 		beginNewChunk();
 	}
 
 	/**
 	 * Beginning of a new chunk, resetting boundingbox parameters and the currentChunk string buffer used to keep the 
 	 */
 	private void beginNewChunk() {
 		top = 10000; left = 10000; 
 		bottom = 0; right = 0;
 		currentChunk = new StringBuffer();		
 	}
 	
 	/**
 	 * Does what its methodname said, 
 	 * looking for the bounding box by checking each character/text position.
 	 * @param text
 	 */
 	private void updateChunkBoundingBox(TextPosition text) {
 		if(left > text.getX()) {
 			left = text.getX();
 		}
 		
 		if(top > text.getY()-text.getHeight()) {
 			top = text.getY()-text.getHeight();
 		}
 		
 		if(bottom < text.getY()+text.getHeight()) {
 			bottom = text.getY()+text.getHeight();
 		}
 		
 		if(right < text.getX() + text.getWidth()){
 			right = text.getX()+text.getWidth();
 		}
 		
 	}
 
 	/**
 	 * I don't remember why I am exposing these variables.
 	 * These list of characters are temporary storage for text being processed by PDFTextStripper.
 	 * @return
 	 */
 
 	Vector<List<TextPosition>> getCharacterByArticles(){
 		return charactersByArticle;
 	}
 	
 	
 	/**
 	 * Getters of the resulting document chunks.
 	 * @return
 	 */
 	
 	public List<D2S_DocChunk> getDocumentChunks(){
 		return documentChunks;
 	}
 	
 
 }
