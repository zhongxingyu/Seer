 /*
  * de.jwic.controls.AsyncRenderContainer 
  */
 package de.jwic.controls;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.spi.ThrowableInformation;
 import org.json.JSONException;
 import org.json.JSONWriter;
 
 import de.jwic.base.Control;
 import de.jwic.base.ControlContainer;
 import de.jwic.base.Dimension;
 import de.jwic.base.IControlContainer;
 import de.jwic.base.IResourceControl;
 import de.jwic.base.ImageRef;
 import de.jwic.base.JWicException;
 import de.jwic.base.JWicRuntime;
 import de.jwic.base.JavaScriptSupport;
 import de.jwic.base.RenderContext;
 import de.jwic.web.ContentRenderer;
 
 /**
  * Renders the controls within the container asynchronously without blocking
  * the main thread. This can be useful if either the creation of child controls or 
  * the rendering itself is slow. By performing the initialization and rendering
  * after the control has been placed into the UI, the user can continue using the 
  * application while a wait image/message is displayed until the rendering is completed
  * and the child controls are displayed.
  * 
  * Controls may either be added right away or be initialized only at the first rendering
  * attempt. For this, a LazyInitializationHandler must be registered, which is invoked
  * the first time the control is rendered.
  * 
  * @author lippisch
  *
  */
 @JavaScriptSupport
 public class AsyncRenderContainer extends ControlContainer implements IResourceControl {
 
 	private ControlContainer container;
 	private LazyInitializationHandler lazyInitializationHandler = null;
 	private boolean initialized = false;
 	private long seqNum = 0;
 	
 	private ImageRef waitImage = new ImageRef("/jwic/gfx/loading3.gif");
 	private Dimension waitBlockDimension = null;
 	private String waitText = null;
 	
 	private Throwable error;
 	
 	/**
 	 * Constructor.
 	 * @param parent
 	 */
 	public AsyncRenderContainer(IControlContainer parent) {
 		super(parent);
 		internalInit();
 	}
 	
 	/**
 	 * @param container
 	 * @param name
 	 */
 	public AsyncRenderContainer(IControlContainer parent, String name) {
 		super(parent, name);
 		internalInit();
 	}
 	
 	/**
 	 * Initialize the control itself.
 	 */
 	private void internalInit() {
 		this.container = new ControlContainer(this, "content");
 	}
 
 	/**
 	 * @return the lazyInitializationHandler
 	 */
 	public LazyInitializationHandler getLazyInitializationHandler() {
 		return lazyInitializationHandler;
 	}
 
 	/**
 	 * @param lazyInitializationHandler the lazyInitializationHandler to set
 	 */
 	public void setLazyInitializationHandler(LazyInitializationHandler lazyInitializationHandler) {
 		this.lazyInitializationHandler = lazyInitializationHandler;
 	}
 
 		
 	/**
 	 * Returns the container to be used for childs.  
 	 */
 	public IControlContainer getContainer() {
 		return container;
 	}
 	/* (non-Javadoc)
 	 * @see de.jwic.base.ControlContainer#registerControl(de.jwic.base.Control, java.lang.String)
 	 */
 	@Override
 	public void registerControl(Control control, String name) throws JWicException {
 		if (container == null) {
 			// until the container is created, add all controls as childs. This is most likely only
 			// the container itself.
 			super.registerControl(control, name);
 		} else {
 			container.registerControl(control, name);
 		}
 	}
 	
 	@Override
 	public Iterator<Control> getControls() {
 		if(this.container!=null){
 			return this.container.getControls();
 		}
 		return super.getControls();
 	}
 	/* (non-Javadoc)
 	 * @see de.jwic.base.IResourceControl#attachResource(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	@Override
 	public void attachResource(HttpServletRequest req, HttpServletResponse res) throws IOException {
 		res.setContentType("text/json; charset=UTF-8");
 		PrintWriter pw;
 		try {
 			pw = res.getWriter();
 		} catch (Exception e) {
 			log.error("Error getting writer!");
 			return;
 		}
 		JSONWriter jsonOut = new JSONWriter(pw);
 		// Initialize the content when the control is rendered the first time.
 		if (!initialized && lazyInitializationHandler != null) {
 			synchronized (this) {
 				if (!initialized) {
 					try{
 						lazyInitializationHandler.initialize(getContainer());
 					}catch(Throwable t){
 						Iterator<Control> it = this.getControls();
 						while(it.hasNext()){
 							Control c = it.next();
 							this.removeControl(c.getName());
 							try{
 								c.destroy();
 							}catch(Throwable t2){}//remove and try to destroy all the control to allow for recreation with same name
 						}
 						
 						try {
 							jsonOut.object().key("success").value(false).key("fail").value(true).endObject();//let the ui know about the grave problem with nifty little booleans
 						} catch (JSONException e) {
 						}						
 						pw.flush();
 						pw.close();
 						this.error = t;
 						return;
 					}
 					initialized = true;
 				}
 			}
 			
 			
 			
 		}
 		
 		try {
 			
 			// render child control
 			ByteArrayOutputStream out = new ByteArrayOutputStream();
 			PrintWriter buffer = new PrintWriter(out); 
 			RenderContext context = new RenderContext(req, res, buffer);
 			ContentRenderer cr = new ContentRenderer(container, context);
 			try {
 				cr.render();
 			} catch (Throwable t) {
 				log.error("Error rendering embedded container", t);
 				pw.print("Error rendering control: " + t.toString());
 			}
 			buffer.flush();
 			
 
 			jsonOut.object()
 			
 				.key("seqNum")
 				.value(req.getParameter("seqNum"))
 				
 				.key("controlId")
 				.value(getControlID())
 				
 				.key("html")
 				.value(out.toString());
 				
 				if (context.getScripts() != null) {
 					if (context.getScripts() != null && context.getScripts().size() > 0) {
 						jsonOut.key("scripts")
 							.array();
 						for (Map.Entry<String, String> entry : context.getScripts().entrySet()) {
 							jsonOut.object();
 							jsonOut.key("controlId");
 							jsonOut.value(entry.getKey());
 							jsonOut.key("script");
 							jsonOut.value(entry.getValue());
 							jsonOut.endObject();
 						}
 						jsonOut.endArray();	
 					}
 				}
 			
 			jsonOut.key("success").value(true);
 			jsonOut.key("fail").value(false);
 			jsonOut.endObject();
 			
 		} catch (JSONException e) {
 			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
 			log.error("Error generating JSON response", e);
 		}
 		pw.close();
 		this.requireRedraw();
 	}
 	
 	/**
 	 * @return the seqNum
 	 */
 	public long nextSeqNum() {
 		seqNum++;
 		return seqNum;
 	}
 
 	/**
 	 * @return the waitImage
 	 */
 	public ImageRef getWaitImage() {
 		return waitImage;
 	}
 
 	/**
 	 * @param waitImage the waitImage to set
 	 */
 	public void setWaitImage(ImageRef waitImage) {
 		this.waitImage = waitImage;
 	}
 
 	/**
 	 * @return the waitBlockDimension
 	 */
 	public Dimension getWaitBlockDimension() {
 		return waitBlockDimension;
 	}
 
 	/**
 	 * @param waitBlockDimension the waitBlockDimension to set
 	 */
 	public void setWaitBlockDimension(Dimension waitBlockDimension) {
 		this.waitBlockDimension = waitBlockDimension;
 	}
 
 	/**
 	 * @return the waitText
 	 */
 	public String getWaitText() {
 		return waitText;
 	}
 
 	/**
 	 * @param waitText the waitText to set
 	 */
 	public void setWaitText(String waitText) {
 		this.waitText = waitText;
 	}
 
 	public final void actionOnFail(){
 		if(this.lazyInitializationHandler!=null){
 			this.lazyInitializationHandler.fail(error);							
 		}
 		
 	}
 	
 	public final void actionOnSuccess(){
 		if(this.lazyInitializationHandler != null){
 			this.lazyInitializationHandler.success();
 		}
 	}
 	
 }
