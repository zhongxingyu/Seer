 package tr2.client.series;
 
 import java.io.IOException;
 
 import tr2.client.http.Proxy;
 import tr2.client.http.RequestType;
 import tr2.server.common.entity.Interval;
 import tr2.server.common.series.protocol.Messages;
 import tr2.server.common.series.protocol.SeriesRequest;
 import tr2.server.common.util.JSONHelper;
 
 public class CalculatorManager implements Runnable {
 
 	@Override
 	public void run() {
 		while (true) {
 			/*
 			 * Here we're using just one thread to calculate, but it can be
 			 * easily extended to use more
 			 */
 			Calculator c = new Calculator();
 			Interval i = c.calculate(getSeriesInterval());
 			try {
 				Thread.sleep(10000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			sendResultsToServer(i);
 		}
 	}
 
 	private void sendResultsToServer(Interval interval) {
 		try {
 			if (interval == null)
 				return;
 			
 			Proxy proxy = Proxy.instance();
 
 			SeriesRequest s = new SeriesRequest();
 			String request = s.prepare(Messages.INTERVAL_CALCULATED,
 					interval.toJSON());
 			System.out
					.println("[CALCULATOR MANAGER] Sending request to server: ");
			System.out.println("\t" + request);
 			proxy.request(request, RequestType.SERIES);
 
 		} catch (IOException e) {
 			//e.printStackTrace();
 		}
 	}
 
 	private Interval getSeriesInterval() {
 		Proxy proxy = null;
 		String response = null;
 		try {
 			SeriesRequest s = new SeriesRequest();
 			String request = s.prepare(Messages.GET_INTERVAL, null);
 			proxy = Proxy.instance();
 			response = proxy.request(request, RequestType.SERIES);
 			/* Parses JSON String to Interval object. */
 		} catch (IOException e) {
 			//e.printStackTrace();
 		}
 		return JSONHelper.fromJSON(response, Interval.class);
 	}
 
 }
