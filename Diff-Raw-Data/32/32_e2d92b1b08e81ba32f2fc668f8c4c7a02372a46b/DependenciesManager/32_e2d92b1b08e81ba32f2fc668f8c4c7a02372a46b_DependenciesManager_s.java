 package com.codingcrayons.tnt.core;
 
 import javax.faces.component.UIOutput;
 import javax.faces.context.FacesContext;
 
 public class DependenciesManager {
 
 	public static final String TNT_AUTO_LOAD = "TNT.AUTO_LOAD";
 	public static final String TNT_DEPENDENCIES_MANAGER = "TNT.DEPENDENCIES_MANAGER";
 
 	protected FacesContext context;
 
 	public DependenciesManager(FacesContext context) {
 		this.context = context;
 	}
 
 	public static DependenciesManager getInstance(FacesContext context) {
 		if (!context.getAttributes().containsKey(TNT_DEPENDENCIES_MANAGER)) {
 			context.getAttributes().put(TNT_DEPENDENCIES_MANAGER, new DependenciesManager(context));
 		}
 		return (DependenciesManager)context.getAttributes().get(TNT_DEPENDENCIES_MANAGER);
 	}
 
 	public void insertTNTCSS() {
 		insertCSS("tnt.css", "css");
 	}
 
 	public void insertCSS(String name, String library) {
 
 		if (!canInsertDependency(name, library)) {
 			return;
 		}
 
 		UIOutput css = new UIOutput();
 		css.setRendererType("javax.faces.resource.Stylesheet");
 		css.getAttributes().put("name", name);
 		css.getAttributes().put("library", library);
 		context.getViewRoot().addComponentResource(context, css, "head");
 		context.getAttributes().put(buildResourceKey(name, library), true);
 	}
 
 	public void insertMetaViewport() {
 
 		final String name = "tnt.viewport";
 		final String library = "meta";
 
 		if (!canInsertDependency(name, library)) {
 			return;
 		}
 
 		UIOutput viewport = new UIOutput();
 		viewport.setValue("<meta name=\"viewport\" content=\"initial-scale=1, maximum-scale=1\"/>");
 		context.getViewRoot().addComponentResource(context, viewport, "head");
 		context.getAttributes().put(buildResourceKey(name, library), true);
 	}
 
 	public Boolean isAutoLoad() {
 		if (!context.getAttributes().containsKey(TNT_AUTO_LOAD)) {
 			Boolean autoLoad = Boolean.TRUE;
 
 			String autoLoadParam = context.getExternalContext().getInitParameter(TNT_AUTO_LOAD);
 			if (autoLoadParam != null && !autoLoadParam.isEmpty()) {
 				autoLoad = Boolean.parseBoolean(autoLoadParam);
 			}
 			context.getAttributes().put(TNT_AUTO_LOAD, autoLoad);
 		}
 		return (Boolean)context.getAttributes().get(TNT_AUTO_LOAD);
 	}
 
 	public boolean canInsertDependency(String name, String library) {
 		return isAutoLoad() && !context.getAttributes().containsKey(buildResourceKey(name, library));
 	}
 
 	protected String buildResourceKey(String name, String library) {
 		return name + "." + library;
 	}
 }
