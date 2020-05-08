 package ch.cern.atlas.apvs.server;
 
 import java.io.IOException;
 import java.net.ConnectException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 
 import ch.cern.atlas.apvs.client.DosimeterService;
 import ch.cern.atlas.apvs.domain.Dosimeter;
 import ch.cern.atlas.apvs.dosimeter.server.DosimeterReader;
 import ch.cern.atlas.apvs.dosimeter.server.DosimeterWriter;
 import ch.cern.atlas.apvs.server.ResponseHandler.Response;
 
 /**
  * @author Mark Donszelmann
  */
 @SuppressWarnings("serial")
 public class DosimeterServiceImpl extends ResponsePollService implements
 		DosimeterService, Runnable {
 
 	private static final String name = "DosimeterSocket";
 	private static final String host = "localhost";
 	private static final int port = 4001;
 	private static final int RECONNECT_INTERVAL = 20000;
 	private boolean stopped = false;
 	private DosimeterReader dosimeterReader;
 
 	public DosimeterServiceImpl() {
 		System.out.println("Creating DosimeterService...");
 	}
 	
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 		System.out.println("Starting DosimeterService...");
 
 		Thread t = new Thread(this);
 		t.start();
 	}
 
 	@Override
 	public void run() {
 		
 		while (!stopped) {
 			if (dosimeterReader == null) {
 				try {
 					Socket socket = new Socket(host, port);
 					System.err.println("Connected to "+name+" on "+host+":"+port);
 					
 					DosimeterWriter dosimeterWriter = new DosimeterWriter(socket);
 					Thread writer = new Thread(dosimeterWriter);
 					writer.start();
 
 					dosimeterReader = new DosimeterReader(socket);
 					dosimeterReader
 							.addValueChangeHandler(getSerialNumbersResponseHandler);
 					dosimeterReader
 							.addValueChangeHandler(getDosimeterResponseHandler);
 					dosimeterReader
 							.addValueChangeHandler(getDosimeterMapResponseHandler);
 					Thread reader = new Thread(dosimeterReader);
 					reader.start();
 					reader.join();
 					dosimeterReader = null;
 					continue;
 				} catch (UnknownHostException e) {
 					System.err.println(getClass()+" "+e);
 				} catch (ConnectException e) {
 					System.err.println("Could not connect to "+name+" on "+host+":"+port);
 				} catch (IOException e) {
 					System.err.println(getClass()+" "+e);
 				} catch (InterruptedException e) {
 					System.err.println(getClass()+" "+e);
 				} 
 				dosimeterReader = null;
 			}
 			
 			System.err.println("Sleep");
 			try {
 				Thread.sleep(RECONNECT_INTERVAL);
 			} catch (InterruptedException e) {
 				// ignored
 			}
 		}
 	}
 
 	@Override
 	public void destroy() {
 		super.destroy();
 		
 		stopped = true;
 
 		try {
 			if (dosimeterReader != null) {
 				dosimeterReader.close();
 			}
 		} catch (IOException e) {
 			// ignored
 		}
 	}
 
 	private ResponseHandler<Map<Integer, Dosimeter>, List<Integer>> getSerialNumbersResponseHandler = new ResponseHandler<Map<Integer, Dosimeter>, List<Integer>>(
 			this);
 
 	@Override
	public List<Integer> getSerialNumbers(int currentHashCode) {
 		return getSerialNumbersResponseHandler.respond(currentHashCode,
 				new Response<Map<Integer, Dosimeter>, List<Integer>>() {
 
 					@Override
 					public List<Integer> getValue(Map<Integer, Dosimeter> object) {
 						return dosimeterReader.getDosimeterSerialNumbers();
 					}
 				});
 	}
 
 	private ResponseHandler<Map<Integer, Dosimeter>, Dosimeter> getDosimeterResponseHandler = new ResponseHandler<Map<Integer, Dosimeter>, Dosimeter>(
 			this);
 
 	@Override
	public Dosimeter getDosimeter(final int serialNo, int currentHashCode) {
 		return getDosimeterResponseHandler.respond(currentHashCode,
 				new Response<Map<Integer, Dosimeter>, Dosimeter>() {
 
 					@Override
 					public Dosimeter getValue(Map<Integer, Dosimeter> object) {
 						return dosimeterReader.getDosimeter(serialNo);
 					}
 				});
 	}
 
 	private ResponseHandler<Map<Integer, Dosimeter>, Map<Integer, Dosimeter>> getDosimeterMapResponseHandler = new ResponseHandler<Map<Integer, Dosimeter>, Map<Integer, Dosimeter>>(
 			this);
 
 	@Override
	public Map<Integer, Dosimeter> getDosimeterMap(int currentHashCode) {
 		return getDosimeterMapResponseHandler.respond(currentHashCode,
 				new Response<Map<Integer, Dosimeter>, Map<Integer, Dosimeter>>() {
 
 					@Override
 					public Map<Integer, Dosimeter> getValue(Map<Integer, Dosimeter> object) {
 						return dosimeterReader.getDosimeterMap();
 					}
 				});
 	}
 
 }
