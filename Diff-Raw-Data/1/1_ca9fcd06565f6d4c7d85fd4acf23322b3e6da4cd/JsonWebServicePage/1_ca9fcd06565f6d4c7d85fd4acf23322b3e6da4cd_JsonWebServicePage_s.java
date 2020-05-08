 package org.innobuilt.wicket.rest;
 
 import java.io.*;
 
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.markup.MarkupStream;
 import org.apache.wicket.model.Model;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 public abstract class JsonWebServicePage extends AbstractWebServicePage {
 
 	private transient GsonBuilder builder;
 	private Class clazz;
 
 	public Class getClazz() {
 		return clazz;
 	}
 
 	public void setClazz(Class clazz) {
 		this.clazz = clazz;
 	}
 
 	public JsonWebServicePage(PageParameters params) {
 		super(params);
 		builder = new GsonBuilder();
 	}
 
 	/**
 	 * Call this constructor from subclass if you plan to receive json in PUT or POST.
 	 * 
 	 * @param params
 	 * @param clazz - The class type you want returned by getModelObject(). 
 	 */
 	public JsonWebServicePage(PageParameters params, Class clazz) {
 		this(params);
 		//TODO override doPut and doPost if clazz is not set, throw an exception
 		this.clazz = clazz;
 		
 	}
 	
 	protected final void onRender(MarkupStream markupStream) {
 		if (!isAuthorized()) {
 			return;
 		}
 
 		Writer pw = null;
 		try {
 			pw = new OutputStreamWriter(getResponse().getOutputStream(), "UTF-8");
 			pw.write(getJson());
 		} catch ( UnsupportedEncodingException e ) {
 			// nope - UTF-8 always included
 		} catch (IOException e) {
 			// probably just closing
 		} finally {
 			if (pw != null) {
 				try {
 					pw.close();
 				} catch ( IOException e ) {
 					// ignore
 				}
 			}
 		}
 	}
 
 	private String getJson() {
 		Gson gson = builder.create();
 		return gson.toJson(getDefaultModelObject());
 	}
 
 	@Override
 	public final String getMarkupType() {
 		return "text/json";
 	}
 
 	public GsonBuilder getBuilder() {
 		return builder;
 	}
 
 	@Override
 	protected void setModelFromBody(String body) {
 		Gson gson = builder.create();
 		setDefaultModel(new Model((Serializable)gson.fromJson(body, clazz)));
 	}
 
 }
