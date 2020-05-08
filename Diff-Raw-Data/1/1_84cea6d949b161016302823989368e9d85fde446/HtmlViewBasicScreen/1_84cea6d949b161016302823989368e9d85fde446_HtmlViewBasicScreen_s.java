 package org.romaframework.aspect.view.html.screen;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Stack;
 
 import javax.servlet.ServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.romaframework.aspect.view.area.AreaComponent;
 import org.romaframework.aspect.view.html.HtmlViewAspectHelper;
 import org.romaframework.aspect.view.html.HtmlViewSession;
 import org.romaframework.aspect.view.html.area.HtmlViewScreenAreaInstance;
 import org.romaframework.aspect.view.html.constants.RequestConstants;
 import org.romaframework.aspect.view.html.exception.DefaultScreenAreaNotDefinedException;
 import org.romaframework.aspect.view.html.transformer.Transformer;
 import org.romaframework.aspect.view.html.transformer.manager.TransformerManager;
 import org.romaframework.core.Roma;
 
 public class HtmlViewBasicScreen implements HtmlViewScreen, Serializable {
 
 	private static final long							serialVersionUID	= -6748849977057967217L;
 
 	protected Long												id;
 
 	protected String											name;
 
 	protected HtmlViewScreenAreaInstance	rootArea;
 
 	private static Log										log								= LogFactory.getLog(HtmlViewConfigurableScreen.class);
 
 	protected String											lastUsedArea;
 
 	protected Stack<String>								popupFormStack		= new Stack<String>();
 
 	protected Map<String, Object>					popupOpeners			= new HashMap<String, Object>();
 	protected String											activeArea;
 	protected String											defautlArea;
 	protected String											renderSet;
 
 	protected HtmlViewBasicScreen(final Object iObj) {
 	}
 
 	public HtmlViewBasicScreen() {
 		rootArea = new HtmlViewScreenAreaInstance(null, MAIN);
 		rootArea.addChild(new HtmlViewScreenAreaInstance(rootArea, POPUPS));
 		rootArea.addChild(new HtmlViewScreenAreaInstance(rootArea, MENU));
 		rootArea.addChild(new HtmlViewScreenAreaInstance(rootArea, DEFAULT_SCREEN_AREA));
 	}
 
 	public HtmlViewScreenAreaInstance getPopupsScreenArea() {
 		return (HtmlViewScreenAreaInstance) rootArea.searchNode("//" + HtmlViewScreen.POPUPS);
 	}
 
 	public AreaComponent getArea(String areaName) {
 		if (areaName == null) {
 			return rootArea.searchArea("//" + DEFAULT_SCREEN_AREA);
 		}
 
 		// Search for a popup whit the given name if doesn't exists search it on the rootArea
 		if (getPopupsScreenArea() != null && getPopupsScreenArea().searchArea(areaName.trim()) != null) {
 			return getPopupsScreenArea().searchArea(areaName.trim());
 		}
 
 		// Search in the root area
 		AreaComponent searchNode = rootArea.searchArea(areaName.trim());
 
 		if (searchNode == null) {
 			// Return the default area
 			searchNode = rootArea.searchArea("//" + DEFAULT_SCREEN_AREA);
 		}
 		return searchNode;
 	}
 
 	public void setVisibleArea(final String areaName, final boolean value) {
 		final HtmlViewScreenAreaInstance area = (HtmlViewScreenAreaInstance) getArea(areaName);
 		area.setVisible(value);
 	}
 
 	/**
 	 * @return the rootArea
 	 */
 	public HtmlViewScreenAreaInstance getRootArea() {
 		return rootArea;
 	}
 
 	/**
 	 * @param rootArea
 	 *          the rootArea to set
 	 */
 	public void setRootArea(final HtmlViewScreenAreaInstance rootArea) {
 		this.rootArea = rootArea;
 	}
 
 	public void render(Writer writer) throws IOException {
 		getTransformer().transform(this, writer);
 	}
 
 	public void render(final ServletRequest request, final boolean css, final boolean js, Writer writer) {
 		String jspUrl = RequestConstants.JSP_PATH + "screen/" + getName() + ".jsp";
 		try {
 			// TODO test jsp existence, instead of catching exception...
 			HtmlViewAspectHelper.getHtmlFromJSP(request, jspUrl, writer);
 
 		} catch (final Exception e) {
 
 			log.error("[HtmlViewBasicScreen.render] Error on loading jsp " + jspUrl, e);
 			try {
 				render(writer);
 			} catch (final Exception e2) {
 				log.error("could not render screen: " + e);
 			}
 		}
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(final String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @return the id
 	 */
 	public long getId() {
 		if (id == null) {
 			final HtmlViewSession session = HtmlViewAspectHelper.getHtmlViewSession();
 			id = session.addRenderableBinding(this);
 		}
 		return id;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.romaframework.aspect.view.html.area.HtmlViewRenderable#getTransformer()
 	 */
 	public Transformer getTransformer() {
 		return Roma.component(TransformerManager.class).getComponent(HtmlViewScreen.SCREEN);
 	}
 
 	public String getHtmlId() {
 		return "screen";
 	}
 
 	public AreaComponent getDefaultArea() {
 		final AreaComponent defaultArea = rootArea.searchArea("//" + DEFAULT_SCREEN_AREA);
 		if (defaultArea == null) {
 			throw new DefaultScreenAreaNotDefinedException();
 		}
 		return defaultArea;
 	}
 
 	public boolean validate() {
 		boolean result = true;
 		if (!rootArea.validate()) {
 			result = false;
 		}
 		return result;
 	}
 
 	public void resetValidation() {
 		rootArea.resetValidation();
 	}
 
 	public void renderPart(final String part, Writer writer) {
 		// TODO It should render only a sub screen area ???
 	}
 
 	public String getActiveArea() {
 		if (activeArea == null)
 			return getDefautlArea();
 		return activeArea;
 	}
 
 	public void setActiveArea(String area) {
 		this.activeArea = area;
 	}
 
 	public String getRenderSet() {
 		return renderSet;
 	}
 
 	public void setRenderSet(String renderSet) {
 		this.renderSet = renderSet;
 	}
 
 	public String getDefautlArea() {
 		return defautlArea;
 	}
 
 	public void setDefautlArea(String defautlArea) {
 		this.defautlArea = defautlArea;
 	}
 
	@Override
 	public void clear() {
 		getRootArea().clear();
 	}
 	
 }
