 package model.parser.mime;
 
 import java.io.IOException;
 import java.util.Scanner;
 
 import org.apache.log4j.Logger;
 
 public class MimeHeaderParser {
 
 	private static Logger logger = Logger.getLogger(MimeHeaderParser.class);
 
 	public void parse(ParseParameters parseParams) throws IOException {
 		Scanner scanner = parseParams.sourceScanner;
 		String lastReadLine = scanner.nextLine();
 		while (scanner.hasNextLine()) {
 			lastReadLine = createHeader(parseParams, lastReadLine);
 			if (lastReadLine.equals("")) {
 				parseParams.destinationWriter.append(lastReadLine + "\r\n");
 				break;
 			}
 		}
		if (!parseParams.mail.hasBoundaryKey()) {
			throw new IllegalStateException("boundary header could not be parsed");
		}
 	}
 	
 	private String createHeader(ParseParameters parseParams, String lastReadLine) throws IOException {
 		Scanner scanner = parseParams.sourceScanner;
 		boolean endOfHeader;
 		String line = lastReadLine;
 		do {
 			endOfHeader = true;
 			lastReadLine = scanner.nextLine();
 			if (lastReadLine.startsWith("\t") || lastReadLine.startsWith(" ") || lastReadLine.startsWith(".")) {
 				line += "\r\n" + lastReadLine;
 				endOfHeader = false;
 			}
 		} while (!endOfHeader);
 		try {
 			MimeHeader header = new MimeHeader(line);
 			parseParams.mail.addHeaders(header);
 			writeHeader(parseParams, header);
 			if (header.getKey().equals("Content-Type")) {
 				parseParams.mail.addAttachmentsExtension(header.getValue());
 			}
 			logger.debug("Parsed header => " + header);
 		} catch (IllegalArgumentException e) {
 			logger.error("Inavlid header: " + line + ". Ignoring...");
 		}
 		return lastReadLine;
 	}
 	
 	private void writeHeader(ParseParameters parseParams, MimeHeader header) throws IOException{
 		parseParams.transformer.transformHeader(header);
 		parseParams.destinationWriter.append(header + "\r\n");
 	}
 
 }
