 package fr.iutvalence.ubpe.commons.services;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.Map;
 
 import fr.iutvalence.ubpe.core.events.DefaultMetadataField;
 import fr.iutvalence.ubpe.core.exceptions.ParsingException;
 import fr.iutvalence.ubpe.core.interfaces.DataEvent;
 import fr.iutvalence.ubpe.core.interfaces.DataEventParserForwarder;
 import fr.iutvalence.ubpe.core.interfaces.MetadataField;
 import fr.iutvalence.ubpe.core.services.AbstractService;
 import fr.iutvalence.ubpe.ubpe2011.UBPE2011DataEvent;
 
 public class UBPEInputStreamDataEventReaderService extends AbstractService
 {
 	public final static int UBPE_START_FRAME_FLAG = (byte) 0x23; // # character
 	
 	private InputStream in;
 
 	private OutputStream out;
 	
 	private final String eventType;
 	
 	private final String readerName;
 
 	/**
 	 * Data events parsers (value) to be used, for each event type supported (key).
 	 */
 	private final Map<String, DataEventParserForwarder> parsers;
 
 	/**
 	 * Creates a new <tt>UBPEInputStreamDataEventReaderService</tt> instance, reading bytes from a given stream, using a given collection of parsers (depending of event types)
 	 * @param inStream input stream
 	 * @param parsers data event parsers to be used, for each supported event type.
 	 */
 	public UBPEInputStreamDataEventReaderService(InputStream inStream, Map<String, DataEventParserForwarder> parsers, String eventType, String readerName)
 	{
 		this.in = inStream;
 		this.out = null;
 		this.parsers = parsers;
 		this.eventType = eventType;		
 		this.readerName = readerName;
 	}
 
 	public UBPEInputStreamDataEventReaderService(InputStream inStream, OutputStream outStream, Map<String, DataEventParserForwarder> parsers, String eventType, String readerName)
 	{
 		this.in = inStream;
 		this.out = outStream;
 		this.parsers = parsers;
 		this.eventType = eventType;
 		this.readerName = readerName;
 	}
 
 	public void serve()
 	{
 		System.out.println("<UBPEInputStreamDataEventReader-service-"+this.hashCode()+">: waiting for event");
 		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
 
 		int state = 0;
 		while (state != 2)
 		{
 			int currentByte = -1;
 			try
 			{
 				currentByte = this.in.read();
 				if (currentByte == -1) 
 				{
 					//System.out.print("*");
 					continue;
 				}				
 			}
 			catch (IOException e)
 			{
 				// "IO exception while reading raw data"
 				this.mustStop();
 				System.err.println("<UBPEInputStreamDataEventReader-service-"+this.hashCode()+">: broken connection...bye!");
 				return;
 			}
 			
 			//System.out.print(".");
 	
 			try
 			{
 				if (this.out != null)
 				{
 					this.out.write(currentByte);
 					this.out.flush();
 				}
 			}
 			catch (IOException e)
 			{
 				// Ignoring output writing failure
 			}
 			
 			if (currentByte == UBPE_START_FRAME_FLAG)
 			{
 				buffer.reset();
 				state = 1;
 				continue;
 			}
 	
 			switch (state)
 			{
 			case 0:
 				break;
 			case 1:
 				if ((currentByte == (byte) 0x0A) || (currentByte == (byte) 0x0D))
 					state = 2;
 				else
 					buffer.write(currentByte);
 				break;
 			}
 		}
 	
 		System.out.println("<UBPEInputStreamDataEventReader-service-"+this.hashCode()+">: ending event processing");
 		
 		byte[] dataBytes = buffer.toByteArray();
 		String dataString = null;
 		try 
 		{
 			dataString = new String(buffer.toByteArray(), "US-ASCII");
 		} 
 		catch (UnsupportedEncodingException e) 
 		{	
 			// "IO exception while reading raw data"
 			this.mustStop();
 			System.err.println("<UBPEInputStreamDataEventReader-service-"+this.hashCode()+">: ASCII not supported (!)...bye!");
 			return;
 			
 		}
 		
 		DataEventParserForwarder parser = this.parsers.get(this.eventType);
 		if (parser == null)
 		{
 			System.err.println("<UBPEInputStreamDataEventReader-service-"+this.hashCode()+">:  no suitable parser found...bye!");
 			return;
 		}
 		
 		try
 		{
			System.out.println(dataString);
			parser.parseAndForward((this.eventType+"@"+this.readerName+"#"+dataString).getBytes("US-ASCII"));
 		}
 		catch (Exception e)
 		{
 			System.err.println("<UBPEInputStreamDataEventReader-service-"+this.hashCode()+">:  unable to parse event...bye!");
 			return;
 		} 
 	}
 }
