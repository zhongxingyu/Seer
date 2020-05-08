 package ch.unige.tpgcrowd.manager.impl;
 
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 
 import android.content.Context;
 import android.util.Log;
 import ch.unige.tpgcrowd.manager.ITPGStops;
 import ch.unige.tpgcrowd.model.DepartureList;
 import ch.unige.tpgcrowd.model.StopList;
 import ch.unige.tpgcrowd.model.ITPGModelEntity;
 import ch.unige.tpgcrowd.net.TpgJsonObjectRequest;
 import ch.unige.tpgcrowd.net.listener.TPGObjectListener;
 
 public class TPGStopsImpl extends TPGAbstractImpl implements ITPGStops {
 
 	public TPGStopsImpl(Context context) {
 		super(context);
 	}
 
 	@Override
 	public void getAllStops(final TPGObjectListener<StopList> listener) {
 		addRequest(TpgJsonObjectRequest.METHOD_GET_STOPS, EMPTY_ARGS, listener);
 	}
 
 	@Override
 	public void getStopsByCodes(final List<String> stopCodes,
 			final TPGObjectListener<StopList> listener) {
 
 		if (stopCodes.isEmpty()) {
 			listener.onFailure();
 		} 
 		else {
 			final String arguments = STOPCODE_PARAM
 					+ StringUtils.join(stopCodes, ",");
 			Log.d("TPGStopsImpl", "arguments : " + arguments);
 			addRequest(TpgJsonObjectRequest.METHOD_GET_STOPS, arguments,
 					listener);
 		}
 	}
 
 	@Override
 	public void getStopsByName(final String stopName,
 			final TPGObjectListener<StopList> listener) {
 
 		if (stopName.isEmpty()) {
 			listener.onFailure();
 		} 
 		else {
 			final String arguments = STOPNAME_PARAM + stopName;
 			addRequest(TpgJsonObjectRequest.METHOD_GET_STOPS, arguments,
 					listener);
 		}
 
 	}
 
 	@Override
 	public void getStopsByLine(final String line,
 			final TPGObjectListener<StopList> listener) {
 		if (line.isEmpty()) {
 			listener.onFailure();
 		} else {
 			final String arguments = LINE_PARAM + line;
 			addRequest(TpgJsonObjectRequest.METHOD_GET_STOPS, arguments,
 					listener);
 		}
 	}
 
 	@Override
 	public void getStopsByPosition(final Double lat, final Double lon,
 			final TPGObjectListener<StopList> listener) {
 		if (lat != null && lon != null) {
 			listener.onFailure();
 		} 
 		else {
 			final String arguments = LATITUDE_PARAM + lat + "&"
 					+ LONGITUDE_PARAM + lon;
 			addRequest(TpgJsonObjectRequest.METHOD_GET_STOPS, arguments,
 					listener);
 		}
 	}
 
 	@Override
 	public void getAllPhysicalStops(TPGObjectListener<StopList> listener) {
 		addRequest(TpgJsonObjectRequest.METHOD_GET_PHYSICAL_STOPS, EMPTY_ARGS,
 				listener);
 	}
 
 	@Override
 	public void getPhysicalStopsByCodes(final List<String> stopCodes,
 			final TPGObjectListener<StopList> listener) {
 
 		if (stopCodes.isEmpty()) {
 			listener.onFailure();
 		} 
 		else {
 			final String arguments = STOPCODE_PARAM
 					+ StringUtils.join(stopCodes, ",");
 			addRequest(TpgJsonObjectRequest.METHOD_GET_PHYSICAL_STOPS,
 					arguments, listener);
 		}
 
 	}
 
 	@Override
 	public void getPhysicalStopsByName(final String name,
 			final TPGObjectListener<StopList> listener) {
 
 		if (name.isEmpty()) {
 			listener.onFailure();
 		} 
 		else {
 			final String arguments = STOPNAME_PARAM + name;
 			addRequest(TpgJsonObjectRequest.METHOD_GET_PHYSICAL_STOPS,
 					arguments, listener);
 		}
 
 	}
 
 	@Override
 	protected Class<? extends ITPGModelEntity> getResponseClass() {
		return StopList.class;
 	}
 }
