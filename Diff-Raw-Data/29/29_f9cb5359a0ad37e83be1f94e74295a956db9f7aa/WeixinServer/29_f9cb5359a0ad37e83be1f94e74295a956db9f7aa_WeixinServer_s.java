 package com.louishong.ibg.weixin;
 
 import com.louishong.ibg.weixin.model.*;
 
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 
 import javax.xml.stream.XMLEventFactory;
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLEventWriter;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.Characters;
 import javax.xml.stream.events.EndDocument;
 import javax.xml.stream.events.EndElement;
 import javax.xml.stream.events.StartDocument;
 import javax.xml.stream.events.StartElement;
 import javax.xml.stream.events.XMLEvent;
 
 @WebServlet("/")
 public class WeixinServer extends HttpServlet {
 
 	private static final long serialVersionUID = 7277121887678658652L;
 
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) {
 		// System.out.println(request.getRemoteHost());
 		PrintWriter out = null;
 		try {
 			out = response.getWriter();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		// Set Response Type
 		response.setContentType("text/xml;charset=UTF-8");
 		try {
 			request.setCharacterEncoding("UTF-8");
 			out = response.getWriter();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		// Get Request Parameters
 		String requestSignature = request.getParameter("signature");
 		String requestTimeStamp = request.getParameter("timestamp");
 		String requestNonce = request.getParameter("nonce");
 		String requestEchoStr = request.getParameter("echostr");
 
 		// System.out.println(requestSignature);
 		// System.out.println(requestTimeStamp);
 		// System.out.println(requestNonce);
 		// System.out.println(requestEchoStr);
 
 		// Return XML
 		out.print(requestEchoStr);
 
 		out.flush();
 		out.close();
 
 	}
 
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) {

 		// Get Writer
 		try {
 			PrintWriter out = response.getWriter();
 			System.out.println("Connection Success");
 			respondRequest(request, out);
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 	}
 
 	protected WeixinMessage readRequest(HttpServletRequest request) {
 		// Set stream up with the Stax Parser
 		try {
 			InputStream requestInputStream = request.getInputStream();
 			XMLInputFactory factory = XMLInputFactory.newInstance();
 			XMLEventReader eventReader = factory
 					.createXMLEventReader(requestInputStream);
 
 			// init Item Text
 			WeixinMessage message = new WeixinMessage();
 
 			// Loop through each node and inserts data into the object message
 			while (eventReader.hasNext()) {
 				XMLEvent event = eventReader.nextEvent();
 
 				if (event.isStartElement()) {
 					// get element as StartElement and inputs into the
 					// WeixinMessage
 					StartElement startElement = event.asStartElement();
 					// if it is ToUserName
 					if (startElement.getName().getLocalPart()
 							.equals(WeixinMessage.TO_USER_NAME)) {
 						event = eventReader.nextEvent();
 						message.setToUserName(event.asCharacters().getData());
 						continue;
 					}
 
 					// if it is FromUserName
 					if (startElement.getName().getLocalPart()
 							.equals(WeixinMessage.FROM_USER_NAME)) {
 						event = eventReader.nextEvent();
 						message.setFromUserName(event.asCharacters().getData());
 						continue;
 					}
 
 					// if it is CreateTime
 					if (startElement.getName().getLocalPart()
 							.equals(WeixinMessage.CREATE_TIME)) {
 						event = eventReader.nextEvent();
 						message.setCreateTime(event.asCharacters().getData());
 						continue;
 					}
 					// if it is MsgType
 					if (startElement.getName().getLocalPart()
 							.equals(WeixinMessage.MSG_TYPE)) {
 						event = eventReader.nextEvent();
 						String type = event.asCharacters().getData();
 						message.setMsgType(type);
 
 						if (type.equals(WeixinMessage.TEXT)) {
 							Text text = new Text(message);
 
 							while (eventReader.hasNext()) {
 								event = eventReader.nextEvent();
 								if (event.isStartElement()) {
 									StartElement textStartElement = event
 											.asStartElement();
 									if (textStartElement.getName()
 											.getLocalPart()
 											.equals(Text.CONTENT)) {
 										event = eventReader.nextEvent();
 										text.setContent(event.asCharacters()
 												.getData());
 									}
 									if (textStartElement.getName()
 											.getLocalPart().equals(Text.MSGID)) {
 										event = eventReader.nextEvent();
 										text.setMsgId(event.asCharacters()
 												.getData());
 									}
 								}
 							}
 							message = text;
 							return message;
 						}
 					}
 				}
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (XMLStreamException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	protected void respondRequest(HttpServletRequest request, PrintWriter out) {
 		WeixinMessage message = readRequest(request);
 		// if (((Text) message).getMsgType().equals(WeixinMessage.TEXT)) {
 		// String response = String.format("<xml>\n"
 		// + "<ToUserName><![CDATA[%s]]></ToUserName>\n"
 		// + "<FromUserName><![CDATA[%s]]></FromUserName>\n"
 		// + "<CreateTime>%s</CreateTime>\n"
 		// + "<MsgType><![CDATA[text]]></MsgType>\n"
 		// + "<Content><![CDATA[%s]]></Content>\n"
 		// + "<FuncFlag>0</FuncFlag>\n" + "</xml>",
 		// ((Text) message).getFromUserName(),
 		// ((Text) message).getToUserName(),
 		// ((Text) message).getCreateTime(), "Hello World!");
 		// System.out.println(message.getToUserName());
 		// return response;
 		// }
 
 		try {
 			XMLEventFactory xmlEventFactory = XMLEventFactory.newInstance();
 			XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
 			XMLEventWriter writer = outputFactory.createXMLEventWriter(out);
 
 			StartDocument startDocument = xmlEventFactory.createStartDocument(
 					"UTF-8", "1.0");
 			writer.add(startDocument);
			
			StartElement rootStartElement = xmlEventFactory.createStartElement("", "", "xml");
 
 			StartElement toUserNameStartElement = xmlEventFactory
 					.createStartElement("", "", "ToUserName");
 			Characters toUserNameChar = xmlEventFactory.createCData(message
 					.getFromUserName());
 			EndElement toUserNameEndElement = xmlEventFactory.createEndElement(
 					"", "", "ToUserName");
 
 			StartElement fromUserNameStartElement = xmlEventFactory
 					.createStartElement("", "", "FromUserName");
 			Characters fromUserNameChar = xmlEventFactory.createCData(message
 					.getToUserName());
 			EndElement fromUserNameEndElement = xmlEventFactory
 					.createEndElement("", "", "FromUserName");
 
 			StartElement createTimeStartElement = xmlEventFactory
 					.createStartElement("", "", "CreateTime");
 			Characters createTimeChar = xmlEventFactory.createCData(message
 					.getCreateTime());
 			EndElement createTimeEndElement = xmlEventFactory.createEndElement(
 					"", "", "CreateTime");
 
 			StartElement msgTypeStartElement = xmlEventFactory
 					.createStartElement("", "", "MsgType");
 			Characters msgTypeChar = xmlEventFactory.createCData(message
 					.getMsgType());
 			EndElement msgTypeEndElement = xmlEventFactory.createEndElement("",
 					"", "Type");
 
 			writer.add(rootStartElement);
			
 			writer.add(toUserNameStartElement);
 			writer.add(toUserNameChar);
 			writer.add(toUserNameEndElement);
 
 			writer.add(fromUserNameStartElement);
 			writer.add(fromUserNameChar);
 			writer.add(fromUserNameEndElement);
 
 			writer.add(createTimeStartElement);
 			writer.add(createTimeChar);
 			writer.add(createTimeEndElement);
 
 			writer.add(msgTypeStartElement);
 			writer.add(msgTypeChar);
 			writer.add(msgTypeEndElement);
 
 			if (message.getMsgType().equals(WeixinMessage.TEXT)) {
 
 				StartElement contentStartElement = xmlEventFactory
 						.createStartElement("", "", "Content");
 				Characters contentChar = xmlEventFactory
 						.createCData(((Text) message).getContent());
 				EndElement contentEndElement = xmlEventFactory
 						.createEndElement("", "", "Content");
 
 				StartElement funcFlagStartElement = xmlEventFactory
 						.createStartElement("", "", "funcFlagStartElement");
 				Characters funcFlagChar = xmlEventFactory.createCharacters("0");
				EndElement funcFlagEndElement = xmlEventFactory.createEndElement("", "", "FuncFlag");
 
 				writer.add(contentStartElement);
 				writer.add(contentChar);
 				writer.add(contentEndElement);
 
 				writer.add(funcFlagStartElement);
 				writer.add(funcFlagChar);
 				writer.add(funcFlagEndElement);
 			}
			
			EndElement rootEndElement = xmlEventFactory.createEndElement("", "", "xml");
 			writer.add(rootEndElement);
			
 			EndDocument endDocument = xmlEventFactory.createEndDocument();
 			writer.add(endDocument);
			
 			writer.flush();
 			writer.close();
 
 		} catch (XMLStreamException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 }
