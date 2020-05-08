 package com.madalla.webapp.panelmenu;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Iterator;
 
 import org.apache.wicket.Application;
 import org.apache.wicket.Component;
 import org.apache.wicket.WicketRuntimeException;
 import org.apache.wicket.authroles.authorization.strategies.role.Roles;
 import org.apache.wicket.authroles.authorization.strategies.role.metadata.InstantiationPermissions;
 import org.apache.wicket.authroles.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.MarkupStream;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Menu Link that will switch form Panels.
  *
  * @author Eugene Malan
  *
  */
 public class PanelLink extends Link<Object> {
 
 	private static final long serialVersionUID = 1L;
 	private static final Logger log = LoggerFactory.getLogger(PanelLink.class);
 
 	final private String panelId;
 	final private IModel<String> key;
 	final private IModel<String> titleKey;
 	final private Class<? extends Panel> panelClass;
 	final private Object constructorArg;
 
 	public PanelLink(final String id, final String panelId, Class<? extends Panel> panelClass, 
 			final IModel<String> key, IModel<String> titleKey){
 		this(id, panelId, panelClass, key, titleKey, null);
 	}
 
 	public PanelLink(final String id, final String panelId, Class<? extends Panel> panelClass, 
 			final IModel<String> key){
 		this(id, panelId, panelClass, key, null);
 	}
 
     public PanelLink(final String id, final String panelId, Class<? extends Panel> panelClass) {
 		this(id, panelId, panelClass, null);
 	}
 
     public PanelLink(final String id, final String panelId, final Class<? extends Panel> panelClass, 
     		final IModel<String> key, final IModel<String> titleKey, final Object constructorArg) {
     	super(id);
 		this.panelId = panelId;
 		this.key = key;
 		this.titleKey = titleKey;
 		this.panelClass = panelClass;
 		this.constructorArg = constructorArg;
 		setAuthorization();
 	}
 
 	private void setAuthorization(){
     	final Application application = Application.get();
 		InstantiationPermissions permissions = application.getMetaData(MetaDataRoleAuthorizationStrategy.INSTANTIATION_PERMISSIONS);
 		if (permissions != null){
 	    	Roles roles = permissions.authorizedRoles(panelClass);
 	    	if (roles != null){
 	    		for (Iterator<String> iter = roles.iterator(); iter.hasNext();){
 	    			MetaDataRoleAuthorizationStrategy.authorize(this, ENABLE, iter.next());
 	    		}
 	    	}
 		}
     }
 
 	@Override
 	public boolean isEnabled() {
 		Component currentPanel = getPage().get(panelId);
 		if (currentPanel != null){
 			if (currentPanel.getClass().equals(panelClass) ){
 				if (constructorArg != null && currentPanel instanceof PanelMenuContructed) {
 					PanelMenuContructed menuConstructed = (PanelMenuContructed) currentPanel;
 					if (constructorArg.equals(menuConstructed.getConstructorArg())) {
 						return false;
 					}
 					return true;
 				}
 				return false;
 			}
 			return true;
 		} else {
 			return true;
 		}
 	}
 
 	@Override
 	protected void onComponentTag(ComponentTag tag) {
 		if (titleKey != null){
 			tag.put("title", titleKey.getObject());
 		}
 		super.onComponentTag(tag);
 	}
 
 	@Override
 	public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
 		if (key != null){
 			replaceComponentTagBody(markupStream, openTag, key.getObject());
 		} else {
 			super.onComponentTagBody(markupStream, openTag);
 		}
 	}
 
 	@Override
 	public void onClick() {
 		try {
 			final Panel panel;
 			if (constructorArg != null){
 				Constructor<? extends Panel> constructor = panelClass.getConstructor(String.class, 
 						constructorArg.getClass());
 				panel = constructor.newInstance(new Object[]{panelId, constructorArg});
 			} else {
 				Constructor<? extends Panel> constructor = panelClass.getConstructor(String.class);
 				panel = constructor.newInstance(new Object[]{panelId});
 			}
			getPage().addOrReplace(panel);
 		} catch (InvocationTargetException e){
 			log.error("Invocation Exception while creating admin panel. If this was caused by Authorized Exception, then take a look at why link was enabled???", e);
 			throw new WicketRuntimeException("Error while Creating new Admin Panel.", e);
 		} catch (Exception e) {
 			log.error("Error while creating admin panel.", e);
 			throw new WicketRuntimeException("Error while Creating new Admin Panel.", e);
 		}
 
 	}
 
 
 }
