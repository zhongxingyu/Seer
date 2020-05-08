 package model.mail.transformerimpl;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Scanner;
 
 import model.parser.mime.MimeHeader;
 import model.util.Base64Util;
 
 public class LeetTransformer implements Transformer {
 
 	@Override
 	public StringBuilder transform(StringBuilder text,
 			Map<String, MimeHeader> partheaders) throws IOException {
 		MimeHeader contentType = partheaders.get("Content-Type");
 		MimeHeader contentTransferEncoding = partheaders.get("Content-Transfer-Encoding");
 		if (contentType == null) {
 			return text;
 		}
 		if (contentTransferEncoding == null && contentType.getValue().startsWith("text/plain")) {
 			return new StringBuilder(toLeet(text.toString()));
 		} else if ("base64".equals(contentTransferEncoding.getValue())) {
			File decodedText;
 			File convertedText = File.createTempFile("encode_", ".tmp");
 			FileWriter writer = new FileWriter(convertedText);
 			StringBuilder builder = new StringBuilder();
 			try {
				decodedText = Base64Util.decodeUsingOS(text.toString()
						.replace("\r", ""));
				Scanner decodedTextScanner = new Scanner(decodedText);
				while(decodedTextScanner.hasNextLine()){
					String line = decodedTextScanner.nextLine();
					writer.append(toLeet(line)); 
 				}
				decodedTextScanner.close();
 				writer.close();
 				File encodedText = Base64Util.encodeToFile(convertedText);
 				Scanner encodedTextScaner = new Scanner(encodedText);
 				while(encodedTextScaner.hasNextLine()){
 					String line = encodedTextScaner.nextLine();
					builder.append(toLeet(line)); 
 				}
 				encodedTextScaner.close();
 			} catch (InterruptedException e) {
 				throw new IOException();
 			}
 			return builder;
 		} else { // quoted-printable
 			
 		}
 		return text;
 	}
 
 	private String toLeet(String text) {
 		String textString = text;
 		textString = textString.replace("a", "4");
 		textString = textString.replace("e", "3");
 		textString = textString.replace("i", "1");
 		textString = textString.replace("a", "4");
 		return textString;
 	}
 
 }
