 package de.hsrm.inspector.handler;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.util.HashMap;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpException;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.entity.ContentProducer;
 import org.apache.http.entity.EntityTemplate;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.protocol.HttpRequestHandler;
 
 import android.content.Context;
 import android.net.Uri;
 import android.util.Log;
 
 import com.google.gson.Gson;
 
 import de.hsrm.inspector.R;
 import de.hsrm.inspector.WebServer;
 import de.hsrm.inspector.exceptions.GadgetException;
 import de.hsrm.inspector.gadgets.intf.Gadget;
 import de.hsrm.inspector.gadgets.intf.GadgetObserver;
 import de.hsrm.inspector.handler.utils.InspectorRequest;
 
 public class PatternHandler implements HttpRequestHandler, GadgetObserver {
 
 	private ConcurrentHashMap<String, Gadget> mGadgetConfiguration;
 	private ConcurrentHashMap<String, Gadget> mGadgetInstances;
 	private Context mContext;
 	private WebServer mServer;
 
 	public PatternHandler(Context context, WebServer server) {
 		mContext = context;
 		mGadgetInstances = new ConcurrentHashMap<String, Gadget>();
 		mServer = server;
 	}
 
 	@Override
 	public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException,
 			IOException {
 		mServer.stopTimeout();
 		Log.d("REQUEST:", request.getRequestLine().toString());
 		Uri requestLine = Uri.parse(request.getRequestLine().getUri());
 		Object tmpResponseContent = null;
 		final Object responseContent;
 		Gson gson = new Gson();
 
 		try {
 			InspectorRequest iRequest = new InspectorRequest(requestLine);
 
 			if (iRequest.getSegments().contains("destroy")) {
 				try {
 					destroyGadget(iRequest.getGadgetIdentifier());
 					tmpResponseContent = iRequest.getCallback() + "(DESTROYED)";
 				} catch (Exception e) {
 					tmpResponseContent = iRequest.getCallback() + "(ERROR)";
 				}
 
 			} else {
 				initGadget(iRequest.getGadgetIdentifier());
 
 				Gadget instance = mGadgetInstances.get(iRequest.getGadgetIdentifier());
 				tmpResponseContent = instance.gogo(mContext, iRequest, request, response, context);
				tmpResponseContent = iRequest.getCallback() + "(" + gson.toJson(tmpResponseContent) + ")";
 				instance.startTimeout();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			StringBuilder b = new StringBuilder();
 			for (StackTraceElement s : e.getStackTrace()) {
 				b.append(s.toString() + "\n");
 			}
 			HashMap<String, String> map = new HashMap<String, String>();
 			map.put(mContext.getString(R.string.exception_message), e.getMessage());
 			map.put(mContext.getString(R.string.exception_stacktrace), b.toString());
 			tmpResponseContent = gson.toJson(map);
 		}
		response.setHeader("Content-Type", mContext.getString(R.string.mime_json));
 		response.addHeader("Access-Control-Allow-Origin", "*");
 		response.addHeader("Access-Control-Allow-Methods", "*");
		responseContent = gson.toJson(tmpResponseContent);
 
 		HttpEntity entity = new EntityTemplate(new ContentProducer() {
 			public void writeTo(final OutputStream outputStream) throws IOException {
 				OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
 				writer.write(responseContent.toString());
 				writer.flush();
 			}
 		});
 		response.setEntity(entity);

 	}
 
 	@Override
 	public void notifyGadgetEvent(EVENT event, Gadget gadget) {
 		synchronized (mGadgetInstances) {
 			if (mGadgetInstances.containsKey(gadget.getIdentifier())) {
 				if (event == EVENT.DESTROY) {
 					mGadgetInstances.remove(gadget.getIdentifier());
 				}
 			}
 			if (mGadgetInstances.size() == 0) {
 				mServer.startTimeout();
 			}
 		}
 	}
 
 	public void setGadgetConfiguration(ConcurrentHashMap<String, Gadget> config) {
 		mGadgetConfiguration = config;
 	}
 
 	public void initGadget(String identifier) throws GadgetException {
 		synchronized (mGadgetInstances) {
 			if (mGadgetInstances.containsKey(identifier)) {
 				mGadgetInstances.get(identifier).cancelTimeout();
 				return;
 			}
 			if (!mGadgetConfiguration.containsKey(identifier))
 				throw new GadgetException("Unknown gadget identifier: " + identifier);
 
 			try {
 				Gadget instance = mGadgetConfiguration.get(identifier).createInstance(mContext);
 				instance.onRegister(mContext);
 				instance.setObserver(this);
 				mGadgetInstances.put(instance.getIdentifier(), instance);
 			} catch (GadgetException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void destroyGadget(String identifier) throws Exception {
 		synchronized (mGadgetInstances) {
 			mGadgetInstances.get(identifier).onUnregister(mContext);
 			mGadgetInstances.get(identifier).onDestroy(mContext);
 			mGadgetInstances.remove(identifier);
 		}
 	}
 }
