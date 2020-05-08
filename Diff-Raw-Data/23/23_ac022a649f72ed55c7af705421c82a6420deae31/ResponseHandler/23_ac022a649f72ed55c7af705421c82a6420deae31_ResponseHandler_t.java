 package de.hsrm.inspector.handler;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.util.concurrent.Callable;
 import java.util.concurrent.FutureTask;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpException;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.entity.ContentProducer;
 import org.apache.http.entity.EntityTemplate;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.protocol.HttpRequestHandler;
 
 import android.content.Context;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 
 import de.hsrm.inspector.gadgets.pool.GadgetEvent;
 import de.hsrm.inspector.gadgets.pool.ResponsePool;
 import de.hsrm.inspector.gadgets.pool.SystemEvent;
 import de.hsrm.inspector.handler.utils.InspectorRequest;
 import de.hsrm.inspector.handler.utils.JsonConverter;
 import de.hsrm.inspector.web.HttpServer;
 
 /**
  * {@link HttpRequestHandler} for requests on port
  * {@value HttpServer#STATE_PORT} for state requests.
  */
 public class ResponseHandler implements HttpRequestHandler {
 
 	private Context mContext;
 	private ResponsePool mResponsePool;
 	private InspectorRequest mStateRequest;
 	private Gson mGson = new Gson();
 
 	public ResponseHandler(Context context, ResponsePool pool) {
 		mContext = context;
 		mResponsePool = pool;
 	}
 
 	@Override
 	public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException,
 			IOException {
 		Object obj;
 
 		try {
 			mStateRequest = new InspectorRequest(request);
 
 			// Check if there are responses in queue to send to client.
 			if (mResponsePool.hasItems(mStateRequest.getBrowserId())) {
 				obj = processResponses();
 			} else {
 				FutureTask<JsonArray> queueWaiter = new FutureTask<JsonArray>(new QueueCallable());
 				queueWaiter.run();
 				try {
 					obj = queueWaiter.get();
 				} catch (Exception e) {
 					obj = JsonConverter.exceptionToJson(e, mContext);
 				}
 			}
 			response(obj.toString(), mStateRequest, response);
 		} catch (Exception e) {
 			e.printStackTrace();
 			obj = JsonConverter.exceptionToJson(e, mContext);
 		}
 	}
 
 	/**
 	 * Method to generate {@link HttpEntity} for {@link HttpResponse}.
 	 * 
 	 * @param content
 	 *            {@link String}
 	 * @param iRequest
 	 *            {@link InspectorRequest}
 	 * @param response
 	 *            {@link HttpResponse}
 	 */
 	private void response(final String content, InspectorRequest iRequest, HttpResponse response) {
 		final String jsonContent = iRequest.getCallback() + "(" + content + ");";
 		response.setHeader("Content-Type", "application/json");
 		response.addHeader("Access-Control-Allow-Origin", "*");
 		response.addHeader("Access-Control-Allow-Methods", "*");
 		HttpEntity entity = new EntityTemplate(new ContentProducer() {
 			public void writeTo(final OutputStream outputStream) throws IOException {
 				OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
 				writer.write(jsonContent);
 				writer.flush();
 			}
 		});
 		response.setEntity(entity);
 	}
 
 	/**
 	 * Pops all {@link GadgetEvent} objects from {@link #mResponsePool} and
 	 * processes them into {@link JsonObject} in a {@link JsonArray}.
 	 * 
 	 * @return {@link JsonArray}
 	 */
 	private JsonArray processResponses() {
 		JsonArray response = new JsonArray();
 		JsonObject tmp;
 		for (GadgetEvent res : mResponsePool.popAll(mStateRequest.getBrowserId())) {
 			try {
 				tmp = new JsonObject();
 				if (res instanceof SystemEvent) {
 					tmp.addProperty("gadget", ((SystemEvent) res).getName());
 					tmp.addProperty("event", ((SystemEvent) res).getResponse().toString());
 				} else {
 					tmp.addProperty("gadget", res.getGadget().getIdentifier());
 					tmp.addProperty("event", res.getEvent().name());
 				}
 				tmp.add("data", mGson.toJsonTree(res.getResponse()));
 				response.add(tmp);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		return response;
 	}
 
 	/**
 	 * {@link FutureTask} {@link Callable} to wait until
 	 * {@link ResponseHandler#mResponsePool} has items.
 	 */
 	private class QueueCallable implements Callable<JsonArray> {
 
 		private final int MAX_CHECKS = 2;
 		private int mChecks = 0;
 
 		@Override
 		public JsonArray call() throws Exception {
 			do {
 				if (mResponsePool.hasItems(mStateRequest.getBrowserId())) {
 					break;
 				}
 				Thread.sleep(20);
 				mChecks++;
 			} while (mChecks < MAX_CHECKS);
 			return processResponses();
 		}
 	}
 
 }
