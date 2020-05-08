 package Packet;
 
 import java.io.IOException;
 import nu.xom.Element;
 import nu.xom.ParsingException;
 import nu.xom.ValidityException;
 
 import Util.IntegerHelper;
 import Util.XmlElementHelper;
 import Util.ZlibHelper;
 import java.util.zip.DataFormatException;
 import org.apache.log4j.Logger;
 public class PacketParser
 {
 	private static Logger logger = Logger.getLogger(PacketParser.class);
 	public static ComunicationObject parse(byte[] packet) throws ValidityException, ParsingException, IOException, DataFormatException
 	{
 		boolean isPrice = (packet[PacketContants.IsPriceIndex] == PacketContants.PRICE_MASK) ? true: false;
 		byte sessionLength = packet[PacketContants.SessionHeaderLengthIndex];
 		byte[] contentLengthBytes = new byte[PacketContants.ContentHeaderLength];
 		System.arraycopy(packet, PacketContants.ContentHeaderLengthIndex,contentLengthBytes, 0, contentLengthBytes.length);
 		int contentLength = IntegerHelper.toCustomerInt(contentLengthBytes);
 		int contentIndex = PacketContants.HeadTotalLength + sessionLength;
 		byte[] contentBytes = new byte[contentLength];
 		System.arraycopy(packet, contentIndex, contentBytes, 0, contentLength);
 		ComunicationObject result;
 		if (isPrice)
 		{
 			result = new ComunicationObject(null, contentBytes);
 		}
 		else
 		{
 			boolean isKeepAlive=(packet[0] & PacketContants.KEEP_ALIVE_VALUE) == PacketContants.KEEP_ALIVE_VALUE;
 			boolean isInitData = (packet[0] & PacketContants.PLAIN_STRING) == PacketContants.PLAIN_STRING;
 			String content;
 			if(isKeepAlive){
 				content = new String(contentBytes,PacketContants.InvokeIDEncoding);
 				boolean keepAliveResult = (packet[0] & PacketContants.KEEP_ALIVE_SUCCESS_MASKE) == PacketContants.KEEP_ALIVE_SUCCESS_MASKE;
 				result = new ComunicationObject(content,keepAliveResult);
 			}
 			else if(isInitData){
 				final int invokeIDLength = 36;
 				String invokeId =new String(contentBytes,0,invokeIDLength,PacketContants.ContentEncoding);
				byte[] decompressContentBytes = ZlibHelper.Decompress(contentBytes,invokeIDLength,contentBytes.length - invokeIDLength);
 				String rawContent = new String(decompressContentBytes,PacketContants.ContentEncoding);
 				result = ComunicationObject.CreateForInitData(invokeId,rawContent);
 			}
 			else{
 				int sessionIndex = PacketContants.HeadTotalLength;
 				byte[] sessionBytes = new byte[sessionLength];
 				System.arraycopy(packet, sessionIndex, sessionBytes, 0,	sessionLength);
 				String session = new String(sessionBytes,PacketContants.SessionEncoding);
 				content = new String(contentBytes,PacketContants.ContentEncoding);
 				Element xmlContent = XmlElementHelper.parse(content);
 				Element invokeIdElement = xmlContent.getFirstChildElement(PacketContants.CommandInvokeIdName);
 				String invokeId = invokeIdElement == null ? "" : invokeIdElement.getValue();
 				result = new ComunicationObject(session, invokeId.trim(), xmlContent);
 				result.setRawContent(content);
 			}
 		}
 		return result;
 	}
 }
