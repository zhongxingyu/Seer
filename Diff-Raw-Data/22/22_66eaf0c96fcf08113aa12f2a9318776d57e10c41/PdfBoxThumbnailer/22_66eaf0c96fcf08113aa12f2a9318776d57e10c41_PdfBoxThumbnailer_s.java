 package com.horsefire.filecabinet.thumb;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.apache.pdfbox.pdmodel.PDDocument;
 import org.apache.pdfbox.util.PDFImageWriter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.io.ByteStreams;
 import com.horsefire.filecabinet.MimeType;
 
 public class PdfBoxThumbnailer implements Thumbnailer {
 
 	private static final Logger LOG = LoggerFactory
 			.getLogger(PdfBoxThumbnailer.class);
 
 	private File findFile(File prefix) {
 		for (File file : prefix.getParentFile().listFiles()) {
 			if (!file.getAbsolutePath().equals(prefix.getAbsolutePath())
 					&& file.getAbsolutePath().startsWith(
 							prefix.getAbsolutePath())) {
 				return file;
 			}
 		}
 		return null;
 	}
 
 	public byte[] createThumbnail(byte[] in) throws IOException {
 		File thumbnail = null;
 		InputStream fileRead = null;
 		PDDocument document = PDDocument.load(new ByteArrayInputStream(in));
 		try {
 			int imageType = BufferedImage.TYPE_INT_RGB;
 			PDFImageWriter imageWriter = new PDFImageWriter();
 			thumbnail = File.createTempFile("document", "pdf");
 			imageWriter.writeImage(document, "png", null, 1, 1,
 					thumbnail.getAbsolutePath() + ".", imageType, 96);
 			File tmp = findFile(thumbnail);
 			if (tmp == null) {
 				LOG.info("Could not find thumbnail file for {}", thumbnail);
 				throw new IOException("Could not generate thumbnail");
 			}
 
 			fileRead = new FileInputStream(tmp);
			return ByteStreams.toByteArray(fileRead);
 		} finally {
 			if (fileRead != null) {
 				fileRead.close();
 			}
 			if (thumbnail != null) {
 				thumbnail.delete();
 			}
 			document.close();
 		}
 	}
 
 	public MimeType incomingFormat() {
 		return MimeType.PDF;
 	}
 
 	public MimeType outgoingFormat() {
 		return MimeType.PNG;
 	}
 
 	public String suggestedName() {
 		return "pdf_box";
 	}
 }
