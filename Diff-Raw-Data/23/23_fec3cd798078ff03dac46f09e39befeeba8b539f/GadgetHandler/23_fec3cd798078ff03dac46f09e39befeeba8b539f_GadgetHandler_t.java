 package de.hsrm.inspector.handler;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpException;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.entity.ContentProducer;
 import org.apache.http.entity.EntityTemplate;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.protocol.HttpRequestHandler;
 
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonObject;
 
 import de.hsrm.inspector.R;
 import de.hsrm.inspector.constants.GadgetConstants;
 import de.hsrm.inspector.exceptions.GadgetException;
 import de.hsrm.inspector.exceptions.constants.GadgetExceptionConstants;
 import de.hsrm.inspector.gadgets.communication.GadgetEvent;
 import de.hsrm.inspector.gadgets.communication.GadgetEvent.EVENT_TYPE;
 import de.hsrm.inspector.gadgets.communication.ResponsePool;
 import de.hsrm.inspector.gadgets.intf.Gadget;
 import de.hsrm.inspector.gadgets.intf.GadgetObserver;
 import de.hsrm.inspector.gadgets.utils.TimeoutTimer;
 import de.hsrm.inspector.handler.utils.InspectorRequest;
 import de.hsrm.inspector.handler.utils.JsonConverter;
 import de.hsrm.inspector.services.ServerService;
 import de.hsrm.inspector.web.HttpServer;
 
 /**
  * {@link HttpRequestHandler} for {@link HttpServer} to handle every request on
  * configured network port. This {@link GadgetHandler} manages all configured
  * {@link Gadget} and dispatches the {@link HttpRequest} to the {@link Gadget}.
  * 
  * @author dobae
  */
 public class GadgetHandler implements HttpRequestHandler, GadgetObserver {
 
 	private final int PERMISSION_REQUEST;
 	private final int PERMISSION_DISABLED;
 
 	private ConcurrentHashMap<String, Gadget> mGadgets;
 	private ConcurrentHashMap<String, ArrayList<String>> mPermissions;
 	private ResponsePool mResponsePool;
 	private Context mContext;
 	private AtomicInteger mRunningInstances;
 	private Gson mGson;
 
 	public GadgetHandler(Context context, ResponsePool pool) {
 		mResponsePool = pool;
 		mContext = context;
 		mRunningInstances = new AtomicInteger(0);
 		mPermissions = new ConcurrentHashMap<String, ArrayList<String>>();
 		mGson = new Gson();
 
 		PERMISSION_REQUEST = Integer.parseInt(mContext.getString(R.string.auth_type_permission));
 		PERMISSION_DISABLED = Integer.parseInt(mContext.getString(R.string.auth_type_disabled));
 	}
 
 	@Override
 	public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException,
 			IOException {
 		stopServerTimeout();
 		Object tmpResponseContent = "";
 		try {
 			InspectorRequest iRequest = new InspectorRequest(request);
 			try {
 				Gadget gadget = checkGadget(iRequest);
 				gadget.cancelTimeout();
 
 				checkKeepAlive(iRequest, gadget);
 
 				gadget.startTimeout();
 			} catch (Exception e) {
 				e.printStackTrace();
 				tmpResponseContent = e;
 			} finally {
 				response(iRequest, tmpResponseContent, response);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Checks if {@link InspectorRequest#getCommand()} is a keep-alive message.
 	 * If this message was keep-alive, nothing will happen and the
 	 * {@link Gadget} {@link TimeoutTimer} will restart.
 	 * 
 	 * @param iRequest
 	 *            {@link InspectorRequest}
 	 * @param gadget
 	 *            {@link Gadget}
 	 */
 	private void checkKeepAlive(InspectorRequest iRequest, Gadget gadget) throws Exception {
		// If request is not keep-alive or gadget isn't running and processing,
		// go further!
		if (!iRequest.getCommand().equals(GadgetConstants.COMMAND_KEEP_ALIVE)
				|| (!gadget.isRunning() && !gadget.isProcessing())) {
 			checkPermission(iRequest, gadget);
 		}
 	}
 
 	/**
 	 * Checks permission setting of requested gadget. If gadget is disabled a
 	 * {@link GadgetException} will be thrown with error code
 	 * {@link GadgetExceptionConstants#GADGET_IS_DISABLED}. If gadget needs
 	 * permission by user, {@link GadgetException} will be thrown with error
 	 * code {@link GadgetExceptionConstants#GADGET_NEEDS_PERMISSION}.
 	 * 
 	 * If user already has permitted this gadget on the calling website or
 	 * gadget is enabled by default, all arguments will be passed to
 	 * {@link Gadget#gogo(Context, InspectorRequest, HttpRequest, HttpResponse, HttpContext)}
 	 * which return value will be returned.
 	 * 
 	 * @param iRequest
 	 *            {@link InspectorRequest}
 	 * @param gadget
 	 *            {@link Gadget}
 	 * @return {@link Object} value of
 	 *         {@link Gadget#gogo(Context, InspectorRequest, HttpRequest, HttpResponse, HttpContext)}
 	 *         call
 	 * @throws Exception
 	 */
 	private void checkPermission(InspectorRequest iRequest, Gadget gadget) throws Exception {
 		if (iRequest.hasParameter("permission") && iRequest.getParameter("permission").toString().equals("true")) {
 			mPermissions.get(gadget.getIdentifier()).add(iRequest.getReferer());
 		}
 
 		if (gadget.getPermissionType() == PERMISSION_REQUEST
 				&& !mPermissions.get(gadget.getIdentifier()).contains(iRequest.getReferer())) {
 			throw new GadgetException(gadget.getIdentifier() + " needs permission",
 					GadgetExceptionConstants.GADGET_NEEDS_PERMISSION, iRequest.getUrlParams());
 
 		} else if (gadget.getPermissionType() == PERMISSION_DISABLED) {
 			throw new GadgetException(gadget.getIdentifier() + " is disabled",
 					GadgetExceptionConstants.GADGET_IS_DISABLED);
 
 		} else {
 			initGadget(gadget);
 
 			if (iRequest.hasParameter(GadgetConstants.PARAM_BROWSER_ID)) {
 				mResponsePool.addBrowserGadget(iRequest.getParameter(GadgetConstants.PARAM_BROWSER_ID).toString(),
 						gadget);
 			}
 
 			gadget.gogo(iRequest);
 		}
 	}
 
 	/**
 	 * Checks if gadget identifier of request URL is available.
 	 * 
 	 * @param iRequest
 	 *            {@link InspectorRequest}
 	 * @throws GadgetException
 	 *             Throws {@link GadgetException} with error code
 	 *             {@link GadgetExceptionConstants#GADGET_NOT_AVAILABLE} if
 	 *             gadget isn't available in {@link #mGadgets}.
 	 * @return {@link Gadget}
 	 */
 	private Gadget checkGadget(InspectorRequest iRequest) throws GadgetException {
 		if (!mGadgets.containsKey(iRequest.getGadgetIdentifier())) {
 			throw new GadgetException("Gadget " + iRequest.getGadgetIdentifier() + " isn't available",
 					GadgetExceptionConstants.GADGET_NOT_AVAILABLE);
 		}
 		return mGadgets.get(iRequest.getGadgetIdentifier());
 	}
 
 	/**
 	 * Writes content of argument {@link Object} into argument
 	 * {@link HttpResponse}.
 	 * 
 	 * @param content
 	 *            {@link Object}
 	 * @param response
 	 *            {@link HttpResponse}
 	 */
 	private void response(InspectorRequest iRequest, Object content, HttpResponse response) {
 		final String jsonContent;
 		JsonObject obj;
 		if (content instanceof Exception) {
 			obj = JsonConverter.exceptionToJson((Exception) content, mContext);
 		} else {
 			obj = new JsonObject();
 			obj.addProperty("event", EVENT_TYPE.STATE.name().toLowerCase());
 			obj.add("data", mGson.toJsonTree(content));
 		}
 		content = obj.toString();
 		jsonContent = iRequest.getCallback() + "(" + content.toString() + ");";
 
 		response.setHeader("Content-Type", "application/json");
 		response.addHeader("Access-Control-Allow-Origin", "*");
 		response.addHeader("Access-Control-Allow-Methods", "*");
 		HttpEntity entity = new EntityTemplate(new ContentProducer() {
 			public void writeTo(final OutputStream outputStream) throws IOException {
 				OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
 				writer.write(jsonContent.toString());
 				writer.flush();
 			}
 		});
 		response.setEntity(entity);
 	}
 
 	@Override
 	public void notifyGadgetEvent(GadgetEvent event) {
 		if (mGadgets.containsKey(event.getGadget().getIdentifier())) {
 			if (event.getEvent() == EVENT_TYPE.DESTROY) {
 				event.getGadget().removeObserver();
 				mResponsePool.removeGadget(event.getGadget());
 				mRunningInstances.decrementAndGet();
 			} else {
 				mResponsePool.add(event);
 			}
 		}
 		if (mRunningInstances.get() == 0) {
 			startServerTimeout();
 		}
 	}
 
 	/**
 	 * Sets configured gadgets as {@link ConcurrentHashMap}.
 	 * 
 	 * @param config
 	 *            {@link ConcurrentHashMap}
 	 */
 	public void setGadgetConfiguration(ConcurrentHashMap<String, Gadget> config) {
 		mGadgets = config;
 		for (Gadget g : config.values()) {
 			mPermissions.put(g.getIdentifier(), new ArrayList<String>());
 		}
 	}
 
 	/**
 	 * Initializes gadget on request. If gadget is already initialized its
 	 * timeout will be reseted otherwise {@link Gadget#onCreate(Context)} and
 	 * {@link Gadget#onProcessStart(Context)} will be called and this
 	 * {@link GadgetHandler} will be registered as {@link GadgetObserver}.
 	 * 
 	 * @param gadget
 	 *            {@link Gadget}
 	 * @throws GadgetException
 	 */
 	public void initGadget(Gadget gadget) throws Exception {
 		if (!gadget.isRunning()) {
 			gadget.onCreate(mContext);
 			gadget.setObserver(this);
 			gadget.onProcessStart();
 			mRunningInstances.incrementAndGet();
 		}
 	}
 
 	/**
 	 * Starts timeout task of {@link HttpServer} by sending {@link Intent} to
 	 * {@link ServerService}.
 	 */
 	private void startServerTimeout() {
 		String uri = "inspect://" + ServerService.CMD_START_TIMEOUT;
 		Intent request = new Intent("de.inspector.intents");
 		request.setData(Uri.parse(uri));
 		mContext.startService(request);
 	}
 
 	/**
 	 * Stops timeout task of {@link HttpServer} by sending {@link Intent} to
 	 * {@link ServerService}.
 	 */
 	private void stopServerTimeout() {
 		String uri = "inspect://" + ServerService.CMD_STOP_TIMEOUT;
 		Intent request = new Intent("de.inspector.intents");
 		request.setData(Uri.parse(uri));
 		mContext.startService(request);
 	}
 }
