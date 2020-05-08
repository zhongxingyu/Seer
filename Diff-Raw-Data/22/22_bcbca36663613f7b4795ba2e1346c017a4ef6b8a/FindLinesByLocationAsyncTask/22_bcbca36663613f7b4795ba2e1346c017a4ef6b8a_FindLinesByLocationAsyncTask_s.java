 package com.hypermurea.hslpushdroid.reittiopas;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import com.hypermurea.hslpushdroid.TaskResultListener;
 
 import android.location.Location;
 import android.os.AsyncTask;
 
 public class FindLinesByLocationAsyncTask extends AsyncTask<Location,List<TransportLine>,List<TransportLine>>  {
 
 	private ReittiopasService service;
 	
 	private TaskResultListener<List<TransportLine>> listener;
 
 	public FindLinesByLocationAsyncTask(ReittiopasService service, TaskResultListener<List<TransportLine>> listener) {
 		this.service = service;
 		this.listener = listener;
 	}
 	
 	@Override
 	public void onPreExecute() {
 		listener.backgroundTaskStarted();
 	}
 	@Override
 	protected List<TransportLine> doInBackground(Location... location) {
 
 		Set<String> exhaustedLineCodes = new HashSet<String>();
 		Set<String> stops = service.findStopsByLocation(location[0]);
 		for(String stop: stops) {
 			Set<String> lineCodes = service.findLinesPassingStop(stop);
 			lineCodes.removeAll(exhaustedLineCodes);
			List<TransportLine> lines = service.findTransportLinesByName(lineCodes.toArray(new String[lineCodes.size()]));
			this.publishProgress(lines);
			exhaustedLineCodes.addAll(lineCodes);
 		}
 		
 		return new ArrayList<TransportLine>();
 	}
 	
 	@Override
 	public void onProgressUpdate(List<TransportLine>... lines) {
 		listener.receiveResults(lines[0]);
 	}
 		
 	@Override
 	public void onPostExecute(List<TransportLine> result) {
 		listener.backgroundTaskStopped();
 	}
 
 }
