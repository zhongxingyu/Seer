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
 import org.romaframework.aspect.view.ViewAspect;
 import org.romaframework.aspect.view.ViewHelper;
 import org.romaframework.aspect.view.area.AreaComponent;
 import org.romaframework.aspect.view.feature.ViewClassFeatures;
 import org.romaframework.aspect.view.html.HtmlViewAspect;
 import org.romaframework.aspect.view.html.HtmlViewAspectHelper;
 import org.romaframework.aspect.view.html.HtmlViewSession;
 import org.romaframework.aspect.view.html.area.HtmlViewScreenArea;
 import org.romaframework.aspect.view.html.area.HtmlViewScreenAreaInstance;
 import org.romaframework.aspect.view.html.area.HtmlViewScreenPopupAreaInstance;
 import org.romaframework.aspect.view.html.component.HtmlViewContentForm;
 import org.romaframework.aspect.view.html.constants.RequestConstants;
 import org.romaframework.aspect.view.html.exception.DefaultScreenAreaNotDefinedException;
 import org.romaframework.aspect.view.html.transformer.Transformer;
 import org.romaframework.aspect.view.html.transformer.manager.TransformerManager;
 import org.romaframework.core.Roma;
 import org.romaframework.core.domain.type.TreeNodeMap;
 import org.romaframework.core.schema.SchemaClass;
 
 public class HtmlViewBasicScreen implements HtmlViewScreen, Serializable {
 
 	protected Long												id;
 
 	protected String											name;
 
 	protected HtmlViewScreenAreaInstance	rootArea;
 
	private static Log										log							= LogFactory.getLog(HtmlViewConfigurableScreen.class);
 
 	protected String											lastUsedArea;
 
	protected Stack<String>								popupFormStack	= new Stack<String>();
 
	protected Map<String, Object>					popupOpeners		= new HashMap<String, Object>();
 	protected String											activeArea;
 
 	protected HtmlViewBasicScreen(final Object iObj) {
 	}
 
 	public HtmlViewBasicScreen() {
 		rootArea = new HtmlViewScreenAreaInstance(null, MAIN);
 		rootArea.addChild(new HtmlViewScreenAreaInstance(rootArea, POPUPS));
 		rootArea.addChild(new HtmlViewScreenAreaInstance(rootArea, MENU));
 		rootArea.addChild(new HtmlViewScreenAreaInstance(rootArea, DEFAULT_SCREEN_AREA));
 	}
 
 	private void refreshAfterPopupClose(final HtmlViewScreenArea popupArea) {
 		((HtmlViewScreenAreaInstance) popupArea.getParent()).setDirty(true);
 		if (popupFormStack.empty()) {
 			HtmlViewScreenAreaInstance baseScreen = ((HtmlViewScreenAreaInstance) ((HtmlViewScreenAreaInstance) popupArea.getParent()).getParent());
 			baseScreen.setDirty(true);
 		}
 	}
 
 	public HtmlViewScreenAreaInstance getPopupsScreenArea() {
 		return (HtmlViewScreenAreaInstance) rootArea.searchNode("//" + HtmlViewScreen.POPUPS);
 	}
 
 	public AreaComponent getArea(String areaName) {
 		if (areaName == null) {
			return rootArea.searchArea(DEFAULT_SCREEN_AREA);
 		}
 
 		// Search for a popup whit the given name if doesn't exists search it on the rootArea
 		if (getPopupsScreenArea() != null && getPopupsScreenArea().searchArea(areaName.trim()) != null) {
 			return getPopupsScreenArea().searchArea(areaName.trim());
 		}
 
 		// Search in the root area
 		AreaComponent searchNode = rootArea.searchArea(areaName.trim());
 
 		if (searchNode == null) {
 			// Return the default area
			searchNode = rootArea.searchArea(DEFAULT_SCREEN_AREA);
 		}
 		return searchNode;
 	}
 
 	public void setVisibleArea(final String areaName, final boolean value) {
 		final HtmlViewScreenAreaInstance area = (HtmlViewScreenAreaInstance) getArea(areaName);
 		area.setVisible(value);
 	}
 
 	public String view(final String areaName, final Object iPojo) {
 
 		String iArea = null;
 
 		if (areaName == null) {
 			// USE LAST AREA POSITION
 			iArea = lastUsedArea;
 		}
 
 		if (iArea != null && !iArea.equals(lastUsedArea) && iPojo != null) {
 			// UPDATE LAST AREA
 			lastUsedArea = iArea;
 		}
 
 		if (iArea.equals(NULL)) {
 			// DO NOTHING
 		} else if (iArea.startsWith(POPUP) || iArea.startsWith(SCREEN_POPUP)) {
 			addPopup(iPojo, iArea);
 		} else {
 			final HtmlViewScreenAreaInstance area = (HtmlViewScreenAreaInstance) rootArea.searchArea(iArea);
 			if (area == null) {
 				log.warn("[HtmlViewBasicScreen.view] area " + iArea + " not found in current screen");
 				return null;
 			}
 			area.setDirty(true);
 			displayArea(iPojo, area);
 		}
 		return iArea;
 	}
 
 	public void addPopup(final Object iPojo, String areaName) {
 
 		String iAreaName = areaName;
 
 		// If the object is null don't add the popup
 		if (iPojo == null) {
 			return;
 		}
 		final HtmlViewContentForm form;
 		if (iPojo instanceof HtmlViewContentForm) {
 			form = (HtmlViewContentForm) iPojo;
 		} else {
 			final HtmlViewAspect viewAspect = (HtmlViewAspect) Roma.aspect(ViewAspect.class);
 			// Create the form
 			form = (HtmlViewContentForm) viewAspect.createForm(Roma.session().getSchemaObject(iPojo), null, null);
 			form.setContent(iPojo);
 		}
 		if (iAreaName == null) {
 			iAreaName = (String) form.getSchemaObject().getFeature(ViewClassFeatures.LAYOUT);
 		}
 		HtmlViewScreenArea popupArea = null;
 		if (getPopupsScreenArea().searchArea("popup") == null) {
 			popupArea = new HtmlViewScreenPopupAreaInstance(getPopupsScreenArea(), "popup");
 			getPopupsScreenArea().addChild((TreeNodeMap) popupArea);
 			this.rootArea.setDirty(true);
 			addFirstToOpenPopup(form, form.getContent());
 		} else {
 			popupArea = (HtmlViewScreenArea) getPopupsScreenArea().getChild("popup");
 			if (!isFirstToOpenPopup(form.getContent()))
 				addFirstToOpenPopup(form, form.getContent());
 		}
 		form.setScreenArea(popupArea);
 		displayArea(form, popupArea);
 	}
 
 	private void addFirstToOpenPopup(final HtmlViewContentForm iForm, final Object content) {
 		popupOpeners.put(String.valueOf(iForm.getId()), content);
 		popupFormStack.add(String.valueOf(iForm.getId()));
 	}
 
 	public boolean isFirstToOpenPopup(final Object iObject) {
 		for (final Map.Entry<String, Object> entry : popupOpeners.entrySet()) {
 			Object tempObject = entry.getValue();
 			tempObject = ViewHelper.getWrappedContent(tempObject);
 
 			if (iObject == tempObject) {
 				return entry.getKey().equals(popupFormStack.peek());
 			}
 		}
 		return false;
 	}
 
 	private String getPopupOpened(final Object content) {
 		for (final Map.Entry<String, Object> entry : popupOpeners.entrySet()) {
 			if (content == entry.getValue()) {
 				return entry.getKey();
 			}
 		}
 		return null;
 	}
 
 	public void close(final Object toClose) {
 		HtmlViewContentForm popupForm;
 		if (toClose instanceof HtmlViewContentForm) {
 			popupForm = (HtmlViewContentForm) toClose;
 		} else {
 			popupForm = (HtmlViewContentForm) ((HtmlViewAspect) Roma.aspect(ViewAspect.class)).getFormByObject(toClose);
 		}
 
 		if (popupForm != null) {
 			String popupAreaName = getPopupOpened(toClose);
 			final HtmlViewScreenArea popupArea = popupForm.getScreenAreaObject();
 			ViewHelper.invokeOnDispose(popupForm.getContent());
 			((HtmlViewAspect) Roma.aspect(ViewAspect.class)).removeObjectFormAssociation(popupForm.getContent(), null);
 			if (isFirstToOpenPopup(toClose)) {
 				popupFormStack.pop();
 			}
 			if (popupFormStack.isEmpty()) {
 				getPopupsScreenArea().removeChild(popupArea);
 				popupOpeners.remove(popupAreaName);
 				popupOpeners.clear();
 			} else {
 				popupOpeners.remove(popupAreaName);
 				Object newContent = popupOpeners.get(popupFormStack.peek());
 				final SchemaClass schemaClass = Roma.schema().getSchemaClass(newContent);
 				HtmlViewContentForm form;
 				if (((HtmlViewScreenAreaInstance) popupArea).getComponentInArea().getSchemaObject().getSchemaClass().equals(schemaClass)) {
 					form = ((HtmlViewScreenAreaInstance) popupArea).getComponentInArea();
 				} else {
 					final HtmlViewAspect viewAspect = (HtmlViewAspect) Roma.aspect(ViewAspect.class);
 					// Create the form
 					form = (HtmlViewContentForm) viewAspect.createForm(Roma.session().getSchemaObject(newContent), null, null);
 				}
 				form.setContent(newContent);
 				form.setScreenArea(popupArea);
 				displayArea(form, popupArea);
 			}
 			refreshAfterPopupClose(popupArea);
 		} else {
 			// Maybe the object was the opener
 			if (isFirstToOpenPopup(toClose)) {
 				final String openedPopupName = getPopupOpened(toClose);
 				final HtmlViewScreenArea area = (HtmlViewScreenArea) getPopupsScreenArea().searchArea(openedPopupName);
 				if (area.getForm() != null) {
 					ViewHelper.invokeOnDispose(area.getForm().getContent());
 				}
 				((HtmlViewAspect) Roma.aspect(ViewAspect.class)).releaseForm(area.getForm());
 				getPopupsScreenArea().removeChild(area);
 				popupOpeners.remove(openedPopupName);
 				popupFormStack.pop();
 				refreshAfterPopupClose(area);
 			}
 		}
 	}
 
 	private void displayArea(final Object iPojo, final HtmlViewScreenArea iareaInstance) {
 		if (iPojo instanceof HtmlViewContentForm) {
 			iareaInstance.bindForm((HtmlViewContentForm) iPojo);
 		} else {
 			iareaInstance.bindPojo(iPojo);
 		}
 		if (iareaInstance.getName().toLowerCase().contains("popup")) {
 			((HtmlViewScreenAreaInstance) iareaInstance.getParent()).setDirty(true);
 			((HtmlViewScreenAreaInstance) iareaInstance).setDirty(true);
 		}
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
		final AreaComponent defaultArea = rootArea.searchArea(DEFAULT_SCREEN_AREA);
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
 		return activeArea;
 	}
 
 	public void setActiveArea(String area) {
 		this.activeArea = area;
 	}
 
 }
