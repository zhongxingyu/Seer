 package com.hypermurea.hslpushdroid.reittiopas;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import com.hypermurea.hslpushdroid.LocationUpdateAgent;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.os.Bundle;
 
 public class FindLinesServiceImpl implements FindLinesService, LocationListener {
 
 	private String user;
 	private String password;
 	private String serviceUrl;
 	private LocationUpdateAgent locationUpdateAgent;
 	private FindLinesResultListener locationResultListener;
 
 	private LineCache cache = new LineCache();
 
 	private HashSet<String> queried = new HashSet<String>();
 
 	public FindLinesServiceImpl(String serviceUrl, String user, String password, LocationUpdateAgent locationUpdateAgent) {
 		this.serviceUrl = serviceUrl;
 		this.user = user;
 		this.password = password;
 		this.locationUpdateAgent = locationUpdateAgent;
 	}
 
 	@Override
 	public void findLinesByName(final FindLinesResultListener resultListener, String... query) {
 		Set<TransportLine> cachedResults = new HashSet<TransportLine>();
 		Set<String> refinedQuery = new HashSet<String>();
 		for(String queryString : query) {
 			TransportLine line = cache.getTransportLine(queryString);
 			if(line != null) {
 				cachedResults.add(line);
 			} else {
 				if(!queried.contains(queryString)) {
 					queried.add(queryString);
 					refinedQuery.add(queryString);
 				}
 			}
 		}
 
 		resultListener.receiveFindLinesResult(new ArrayList<TransportLine>(cachedResults));
 
 		
 		String[] refinedLines = refinedQuery.toArray(new String[refinedQuery.size()]);
 		// Make sure the query is at least somewhat meaningful
 		if(refinedLines.length > 0 && refinedLines[0].length() > 0) {
 
 			FindLinesByNameAsyncTask task = new FindLinesByNameAsyncTask(serviceUrl, user, password, 
 					new TaskResultListener<List<TransportLine>>() {
 
 				@Override
 				public void receiveResults(List<TransportLine> result) {
 					for(TransportLine line : result) {
 						cache.addTransportLine(line);
 					}
 					resultListener.receiveFindLinesResult(result);
 				}
 
 			});
			task.execute(refinedLines);
 
 		}
 
 	}
 
 	@Override
 	public void startFindingLinesByLocation(FindLinesResultListener resultListener) {
 		locationResultListener = resultListener;
 		locationUpdateAgent.startLocationUpdates(this);
 	}
 
 	@Override
 	public void stopFindingLinesByLocation() {
 		locationUpdateAgent.stopLocationUpdates(this);
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 	}
 
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub		
 	}
 
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub		
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		findStopsByLocation(location);
 	}
 
 	private void findStopsByLocation(Location location) {
 		FindStopsByLocationAsyncTask task = new FindStopsByLocationAsyncTask(serviceUrl, user, password, 
 				new TaskResultListener<Set<String>>() {
 
 			@Override
 			public void receiveResults(Set<String> result) {
 				for(String stopCode: result) {
 					findLinesPassingStop(stopCode, new TaskResultListener<Set<String>>() {
 
 						@Override
 						public void receiveResults(Set<String> result) {
 							findLinesByName(locationResultListener, result.toArray(new String[result.size()]));
 						}
 
 					});
 				}
 			}
 		});
 		task.execute(location);		
 	}
 
 	private void findLinesPassingStop(String stopCode, TaskResultListener<Set<String>> resultListener) {
 		// TODO run through cache, if no hit then user task
 		FindLinesPassingStopAsyncTask task = new FindLinesPassingStopAsyncTask(serviceUrl, user, password, resultListener);
 		task.execute(stopCode);
 	}
 
 
 
 }
