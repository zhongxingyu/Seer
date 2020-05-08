 package model.parser.mime;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Scanner;
 
 import model.mail.Mail;
 import model.mail.MailTransformer;
 import model.mail.MimeHeaderCollection;
 
 import org.apache.log4j.Logger;
 
 public class MailMimeParser {
 
 	protected static final Logger logger = Logger.getLogger(MailMimeParser.class);
 
 	private MimeHeaderParser headerParser;
 
 	public MailMimeParser() {
 		headerParser = new MimeHeaderParser();
 	}
 
 	public Mail parse(File source, MailTransformer transformer) throws IOException {
 		logger.debug("Parsing mail: " + source.getAbsolutePath());
 		File destination = File.createTempFile("transformed_", source.getName());
 		Mail mail = new Mail(destination);
 		Scanner sourceScanner = new Scanner(source);
 		FileWriter destinationWriter = new FileWriter(destination);
 		parseFileAndTransform(new ParseParameters(mail, sourceScanner, destinationWriter, transformer));
 		sourceScanner.close();
 		destinationWriter.flush();
 		destinationWriter.close();
 		// apply external transformations
 		transformer.transformComplete(mail);
 		return mail;
 	}
 
 	private void parseFileAndTransform(ParseParameters parseParams) throws IOException {
 		headerParser.parse(parseParams);
 		boolean endOfMail = false;
 		String line = parseParams.sourceScanner.nextLine();
 		if (parseParams.mail.isMultiPart()) {
 			String boundary = parseParams.mail.getBoundaryKey();
 			do {
 				parsePart(parseParams, boundary, line);
				line = skipLineBreaks(parseParams.sourceScanner.nextLine(), parseParams);
 				endOfMail = line.equals(".");
 				if (!endOfMail && !line.startsWith("--" + boundary)) {
 					throw new IllegalStateException("Unexpected line: " + line + ". Expected end of bondary.");
 				}
 			} while (!endOfMail);
 		} else {
 			MimeHeaderCollection headers = parseParams.mail.getHeaders();
 			StringBuilder text = new StringBuilder();
 			do {
 				endOfMail = line.equals(".");
 				if (!endOfMail) {					
 					text.append(line + "\r\n");
 					line = parseParams.sourceScanner.nextLine();
 				}
 			} while (!endOfMail);
 			parseParams.destinationWriter.append(parseParams.transformer.transformPart(headers, text));
 			parseParams.destinationWriter.append("\r\n");
 		}
 		parseParams.destinationWriter.append(line + "\r\n");
 	}
 
 	private void parsePart(ParseParameters parseParams, String boundaryKey, String lastLine) throws IOException {
		lastLine = skipLineBreaks(lastLine, parseParams);
 		if (!lastLine.equals("--" + boundaryKey)) {
 			throw new IllegalStateException("Expected beggining of boundary");
 		}
 		parseParams.destinationWriter.append("--" + boundaryKey + "\r\n");
 		Scanner sourceScanner = parseParams.sourceScanner;
 		MimeHeaderCollection headers = readBoundaryHeaders(parseParams);
 		MimeHeader contentType = headers.get("Content-Type");
 		String subBoundary = (contentType == null) ? null : contentType.getExtraValue("boundary");
 		if (subBoundary != null) {
 			logger.info("Sub Part " + subBoundary + " found. Recursively parsing.");
 			parsePart(parseParams, subBoundary, sourceScanner.nextLine());
 			return;
 		}
 		StringBuilder text = new StringBuilder();
 		boolean endOfBoundary = false;
 		String line;
 		do {
 			line = sourceScanner.nextLine();
 			endOfBoundary = line.startsWith("--" + boundaryKey);
 			if (!endOfBoundary) {
 				text.append(line + "\r\n");
 			}
 		} while (!endOfBoundary);
 		parseParams.destinationWriter.append(parseParams.transformer.transformPart(headers, text));
 		if (line.equals("--" + boundaryKey)) {
 			parsePart(parseParams, boundaryKey, line);
 			return;
 		} else if (line.equals("--" + boundaryKey + "--")) {
 			parseParams.destinationWriter.append(line + "\r\n");
 			return;
 		} else {
 			throw new IllegalStateException("Unexpected line: " + line);
 		}
 	}
 
 	private MimeHeaderCollection readBoundaryHeaders(ParseParameters parseParams) throws IOException {
 		MimeHeaderCollection headers = new MimeHeaderCollection();
 		Scanner sourceScanner = parseParams.sourceScanner;
 		String lastReadLine = sourceScanner.nextLine();
 		while (sourceScanner.hasNextLine()) {
 			boolean endOfHeader;
 			String line = lastReadLine;
 			do {
 				endOfHeader = true;
 				lastReadLine = sourceScanner.nextLine();
 				if (lastReadLine.startsWith("\t") || lastReadLine.startsWith(" ") || lastReadLine.startsWith(".")) {
 					line += "\r\n" + lastReadLine;
 					endOfHeader = false;
 				}
 			} while (!endOfHeader);			
 			MimeHeader header = new MimeHeader(line);
 			headers.add(header);
 			parseParams.destinationWriter.append(header.toString() + "\r\n");
 			if (header.getKey().equalsIgnoreCase("content-type")) {
 				parseParams.mail.addAttachmentsExtension(header.getValue());
 			}
 			logger.debug("Parsed Part header => " + header);
 			// Empty line marks the end of sub-boundary headers
 			if (lastReadLine.isEmpty()) {
 				parseParams.destinationWriter.append(line + "\r\n");
 				break;
 			}
 		}
 		return headers;
 	}
 
	private String skipLineBreaks(String currLine, ParseParameters parseParams) throws IOException {
		while (currLine.isEmpty()) {
			parseParams.destinationWriter.append("\r\n");
			currLine = parseParams.sourceScanner.nextLine();
		}
		return currLine;
	}
 }
