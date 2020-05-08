 package com.roshka.raf.context;
 
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.servlet.ServletContext;
 
 import com.roshka.raf.beans.GeneralResponse;
 import com.roshka.raf.encoding.BaseRAFEncoder;
 import com.roshka.raf.encoding.GSONRAFEncoder;
 import com.roshka.raf.encoding.XMLRAFEncoder;
 import com.roshka.raf.exception.RAFException;
 
 public class RAFContext {
 	
 	public static final String RAF_CONTEXT_SERVLET_CONTEXT_KEY = "raf_ctx.rshk";
 	
 	private Map<String, Object> attributes;
 	private Map<String, BaseRAFEncoder> encoders;
 	private BaseRAFEncoder defaultEncoder;
 	private static boolean _initialized;
 	
 	public static void initialize(ServletContext ctx)
 	{
 		if (!_initialized)
 			doInitialize(ctx);
 	}
 	
 	private static void doInitialize(ServletContext ctx)
 	{
 		String contextClassName = ctx.getInitParameter("raf_ctx_class");
 		RAFContext rc = null;
 		if (contextClassName != null) {
 			try {
 				Class<?> contextClass = Class.forName(contextClassName);
				if (contextClass.isAssignableFrom(RAFContext.class)) {
 					rc = (RAFContext) contextClass.newInstance();
 				} else {
 					rc = new RAFContext();
 				}
 			} catch (Throwable e) {
 				rc = new RAFContext();
 			}
 		} else {
 			rc = new RAFContext();
 		}
 		rc.initialize();
 		ctx.setAttribute(RAF_CONTEXT_SERVLET_CONTEXT_KEY, rc);
 		_initialized = true;
 	}
 
 	public RAFContext()
 	{
 		attributes = new ConcurrentHashMap<String, Object>();
 		encoders = new ConcurrentHashMap<String, BaseRAFEncoder>();
 		doRegisterBasicEncoders();
 	}
 	
 	private void doRegisterBasicEncoders()
 	{
 		GSONRAFEncoder grenc = new GSONRAFEncoder();
 		registerEncoder("json", grenc);
 		setDefaultEncoder(grenc);	// this will be default encoder
 		XMLRAFEncoder xmlrenc = new XMLRAFEncoder();
 		registerEncoder("xml", xmlrenc);
 	}
 	
 	public void initialize()
 	{
 		//
 		
 		
 	}
 	
 	public Object getAttribute(String key)
 	{
 		return attributes.get(key);
 	}
 	
 	public void setAttribute(String key, Object value) 
 	{
 		attributes.put(key, value);
 	}
 	
 	public BaseRAFEncoder getEncoder(String extension)
 	{
 		return encoders.get(extension);
 	}
 	
 	public void registerEncoder(String extension, BaseRAFEncoder encoder)
 	{
 		encoders.put(extension, encoder);
 	}
 	
 	public Object getGeneralError(Throwable t)
 	{
 		GeneralResponse gr = null;
 		if (t instanceof RAFException) {
 			RAFException re = (RAFException)t;
 			gr = new GeneralResponse("ERR", re.getCode(), re.getMessage());
 		} else {
 			gr = new GeneralResponse("ERR", RAFException.ERRCODE_UNEXPECTED_EXCEPTION, "Unexpected Error: " + t.getMessage());
 		}
 		return gr;
 	}
 
 	public BaseRAFEncoder getDefaultEncoder() {
 		return defaultEncoder;
 	}
 
 	public void setDefaultEncoder(BaseRAFEncoder defaultEncoder) {
 		this.defaultEncoder = defaultEncoder;
 	}
 	
 }
