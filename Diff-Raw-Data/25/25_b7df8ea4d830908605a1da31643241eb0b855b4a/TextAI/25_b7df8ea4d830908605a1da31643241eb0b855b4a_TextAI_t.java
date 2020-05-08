 package com.louishong.ibg.weixin.ai;
 
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.StartElement;
 import javax.xml.stream.events.XMLEvent;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import com.louishong.ibg.weixin.command.WeixinShiftCommand;
 import com.louishong.ibg.weixin.model.Text;
 import com.louishong.ibg.weixin.model.WeixinMessage;
 import com.louishong.database.IBGWeixinServerResponseDatabase;
 
 public class TextAI {
 
 	public static WeixinMessage input(WeixinMessage message, XMLEvent event,
 			XMLEventReader eventReader) {
 		try {
 			// Downwards casting
 			Text text = new Text(message);
 			while (eventReader.hasNext()) {
 				event = eventReader.nextEvent();
 				if (event.isStartElement()) {
 					StartElement textStartElement = event.asStartElement();
 
 					// If the Element is CONTENT generate a response and put it
 					// inside the text object
 					if (textStartElement.getName().getLocalPart()
 							.equals(Text.CONTENT)) {
 						event = eventReader.nextEvent();
 						String inputString = event.asCharacters().getData();
 						// Input the inputString into the textEngine to
 						// generate a response
 						text.setContent(textEngine(inputString));
 					}
 					// If the element is MSGID then set MSGID to the original
 					// MESGID
 					if (textStartElement.getName().getLocalPart()
 							.equals(Text.MSGID)) {
 						event = eventReader.nextEvent();
 						text.setMsgId(event.asCharacters().getData());
 					}
 				}
 			}
 			message = text;
 			return message;
 		} catch (XMLStreamException e) {
 			e.printStackTrace();
 		}
 		return message;
 	}
 
 	public static String textEngine(String inputString) {
 		// try {
 		// // ResultSet dbResults =
 		// // IBGWeixinServerResponseDatabase.searchResponse(inputString);
 		//
 		// IBGWeixinServerResponseDatabase responseDatabase = new
 		// IBGWeixinServerResponseDatabase();
 		// String result2 =
 		// responseDatabase.searchResponse(inputString).toString();
 		// responseDatabase.cutConnection();
 		// String result1 = WeixinShiftCommand.execute("Today");
 		// return result1 + "\n" + result2;
 		// } catch (SQLException e) {
		//
 		// e.printStackTrace();
 		// return "";
 		// }
 
 		try {
 			IBGWeixinServerResponseDatabase responseDatabase = new IBGWeixinServerResponseDatabase();
 			ResultSet dbResults = responseDatabase.searchResponse(inputString);
 			boolean isDynamic = dbResults.getString("Dynamic").equals("1");
 			String response = dbResults.getString("Responses");
 
 			if (isDynamic) {
 				Pattern pattern = Pattern.compile("(.*?):(.*)");
 				Matcher matcher = pattern.matcher(response);
 				matcher.find();
 				String protocol = matcher.group(1);
 				String command = matcher.group(2);
 
 				if (protocol.equals("WeixinShift")) {
 					return WeixinShiftCommand.execute(command);
 				}
 
 			}
 			// If nothing matches but no exceptions are thrown, Then As backup,
 			// return this line of text.
 			return response;
 		} catch (SQLException e) {
 			// If database doesn't contain the preset for the input, then return
 			// an apologies
 			return "~~~Բ";
 		} catch (Exception e1) {
 			e1.printStackTrace();
 			return "ҵԴˣ˸㿴룺" + e1.toString();
 		}
 	}
 
 	public static WeixinMessage input(WeixinMessage message, XMLEvent event,
 			XMLEventReader eventReader, int statusCode) {
 		message.setMsgType("text");
 		Text text = new Text(message);
 		text.setContent("ҵķֹ~~~~ ϴ: " + statusCode);
 		while (eventReader.hasNext()) {
 			try {
 				event = eventReader.nextEvent();
 				if (event.isStartElement()) {
 					StartElement textStartElement = event.asStartElement();
 					if (textStartElement.getName().getLocalPart()
 							.equals(Text.MSGID)) {
 						event = eventReader.nextEvent();
 						text.setMsgId(event.asCharacters().getData());
 					}
 				}
 			} catch (XMLStreamException e) {
 				e.printStackTrace();
 			}
 		}
 		message = text;
 		return message;
 	}
 
 }
