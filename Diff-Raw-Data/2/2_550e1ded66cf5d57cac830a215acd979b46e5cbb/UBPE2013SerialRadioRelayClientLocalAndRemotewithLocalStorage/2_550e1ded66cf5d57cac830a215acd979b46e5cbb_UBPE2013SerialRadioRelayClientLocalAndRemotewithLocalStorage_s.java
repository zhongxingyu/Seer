 package fr.iutvalence.ubpe.main;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.HashMap;
 import java.util.Map;
 
 import fr.iutvalence.ubpe.commons.services.RawFileSystemStorageDataEventListenerService;
 import fr.iutvalence.ubpe.commons.services.SystemOutDebugDataEventListenerService;
 import fr.iutvalence.ubpe.commons.services.UBPEInputStreamDataEventReaderService;
 import fr.iutvalence.ubpe.commons.services.WebFrontEndExporterDataEventListenerService;
 import fr.iutvalence.ubpe.core.helpers.RawDataEventFileSystemStorage;
 import fr.iutvalence.ubpe.core.helpers.Serial600InputStream;
 import fr.iutvalence.ubpe.core.helpers.UBPEDataEventParserForwarder;
 import fr.iutvalence.ubpe.core.interfaces.DataEventParserForwarder;
 import gnu.io.NoSuchPortException;
 import gnu.io.PortInUseException;
 import gnu.io.UnsupportedCommOperationException;
 
 public class UBPE2013SerialRadioRelayClientLocalAndRemotewithLocalStorage
 {
 
 	/**
 	 * @param args
 	 * @throws
 	 */
 	public static void main(String[] args)
 	{
 		// args[0] station name
 		// args[1] serial port identifier
 		// args[2] relay server IP
 		// args[3] relay server port
 		// args[4] object name
 
 		if (args.length != 5)
 		{
 			System.err.println("Missing arguments, exiting...");
 			// System.err.println("(expected IP and port for local binding)");
 			System.exit(1);
 		}
 
 		// files are stored in current directory, under "station name" subfolder
 		System.out.println("Trying to configure serial port " + args[1] + " ...");
 		Serial600InputStream in = null;
 
 		try
 		{
 			in = new Serial600InputStream(args[1]);
 		}
 		catch (PortInUseException e)
 		{
 			System.err.println("Serial port is already in use, please close it before running this application again");
 			System.exit(1);
 		}
 		catch (NoSuchPortException e)
 		{
 			System.err.println("Specified port (" + args[1] + ") does not exist, please check serial port name before running this application again");
 			System.exit(1);
 		}
 		catch (UnsupportedCommOperationException e)
 		{
 			System.err.println("Specified port (" + args[1] + ") can not be configured properly, please check it before running this application again");
 			System.exit(1);
 		}
 		catch (IOException e)
 		{
 			System.err.println("Unable to read from specified port (" + args[1] + "), please check it before running this application again");
 			System.exit(1);
 		}
 		System.out.println("... done");
 
 		System.out.println("Creating and registering ubpe2013 event parser ...");
 		UBPEDataEventParserForwarder ubpe2013Parser = new UBPEDataEventParserForwarder(fr.iutvalence.ubpe.ubpe2013.UBPE2013DataEvent.class, "UBPE2013");
 		Map<String, DataEventParserForwarder> parsers = new HashMap<String, DataEventParserForwarder>();
 		parsers.put("UBPE2013", ubpe2013Parser);
 		System.out.println("... done");
 
 		System.out.println("Creating console debug service ...");
 		SystemOutDebugDataEventListenerService debugService = new SystemOutDebugDataEventListenerService();
 		System.out.println("... done");
 
 		System.out.println("Creating raw filesystem storage service ...");
 		RawDataEventFileSystemStorage storage = null;
 		RawFileSystemStorageDataEventListenerService storageService = null;
 		try
 		{
 			storage = new RawDataEventFileSystemStorage(args[0] + "-storage", new File(args[0]));
 			storageService = new RawFileSystemStorageDataEventListenerService(storage);
 		}
 		catch (FileNotFoundException e1)
 		{
 			System.err.println("Unable to create subdir in working directory, check permissions");
 			System.exit(1);
 		}
 		System.out.println("... done");
 
 		System.out.println("Creating remote Web frontend exporter service ...");
 		WebFrontEndExporterDataEventListenerService remoteExporterService = new WebFrontEndExporterDataEventListenerService(new InetSocketAddress(args[2],
 				Integer.parseInt(args[3])));
 		System.out.println("... done");
 		
 		System.out.println("Creating local Web frontend exporter service ...");
		WebFrontEndExporterDataEventListenerService localExporterService = new WebFrontEndExporterDataEventListenerService(new InetSocketAddress(Integer.parseInt(args[3])));
 		System.out.println("... done");
 
 		System.out.println("Registering console debug service as a parser listener ...");
 		ubpe2013Parser.registerDataEventListener(debugService);
 		System.out.println("... done");
 
 		System.out.println("Registering raw filesystem storage service as a parser listener ...");
 		ubpe2013Parser.registerDataEventListener(storageService);
 		System.out.println("... done");
 
 		System.out.println("Registering remote Web frontend exporter service as a parser listener ...");
 		ubpe2013Parser.registerDataEventListener(remoteExporterService);
 		System.out.println("... done");
 		
 		System.out.println("Registering local Web frontend exporter service as a parser listener ...");
 		ubpe2013Parser.registerDataEventListener(localExporterService);
 		System.out.println("... done");
 
 		System.out.println("Starting console debug service ...");
 		new Thread(debugService).start();
 		System.out.println("... done");
 
 		System.out.println("Starting raw filesystem storage service ...");
 		new Thread(storageService).start();
 		System.out.println("... done");
 
 		System.out.println("Starting remote Web frontend exporter service ...");
 		new Thread(remoteExporterService).start();
 		System.out.println("... done");
 		
 		System.out.println("Starting local Web frontend exporter service ...");
 		new Thread(localExporterService).start();
 		System.out.println("... done");
 
 		System.out.println("Starting serial event reader service ...");
 		UBPEInputStreamDataEventReaderService readerService = new UBPEInputStreamDataEventReaderService(in, parsers, "UBPE2013", args[0]);
 		new Thread(readerService).start();
 		System.out.println("... done");
 
 		System.out.println("Initialization completed.");
 	}
 }
