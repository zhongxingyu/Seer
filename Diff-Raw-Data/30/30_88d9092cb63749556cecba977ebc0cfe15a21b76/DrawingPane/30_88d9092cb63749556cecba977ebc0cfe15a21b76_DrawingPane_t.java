 package com.tranhoangdai.korengui.client.ui;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import org.vectomatic.dom.svg.OMNode;
 import org.vectomatic.dom.svg.OMSVGLength;
 import org.vectomatic.dom.svg.OMSVGSVGElement;
 import org.vectomatic.dom.svg.utils.OMSVGParser;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ContextMenuEvent;
 import com.google.gwt.event.dom.client.ContextMenuHandler;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.tranhoangdai.korengui.client.model.GeneralModel;
 import com.tranhoangdai.korengui.client.model.Host;
 import com.tranhoangdai.korengui.client.model.Link;
 import com.tranhoangdai.korengui.client.model.Switch;
 import com.tranhoangdai.korengui.client.service.util.ClientServiceHelper;
 import com.tranhoangdai.korengui.client.ui.svg.AbstractElementSvg;
 import com.tranhoangdai.korengui.client.ui.svg.HostSvg;
 import com.tranhoangdai.korengui.client.ui.svg.LinkSvg;
 import com.tranhoangdai.korengui.client.ui.svg.NodeSvg;
 import com.tranhoangdai.korengui.client.ui.svg.SvgTransformationHelper;
 import com.tranhoangdai.korengui.client.ui.svg.SwitchSvg;
 import com.tranhoangdai.korengui.client.ui.widget.PanelContextMenu;
 
 public class DrawingPane extends Composite implements ContextMenuHandler {
 
 	private static DrawingPaneUiBinder uiBinder = GWT.create(DrawingPaneUiBinder.class);
 
 	interface DrawingPaneUiBinder extends UiBinder<Widget, DrawingPane> {
 	}
 
 	OMSVGSVGElement svgElement = null;
 	@UiField
 	HTMLPanel htmlPanel;
 	int id = 0;
 	int width = 0;
 	int height = 0;
 	static int uniqueID = 0;
 	private PanelContextMenu contextMenu;
 	protected float centery = 0;
 	protected float centerx = 0;
 
 	public DrawingPane() {
 		initWidget(uiBinder.createAndBindUi(this));
 		id = ++uniqueID;
		
		
 		contextMenu = new PanelContextMenu(this);
 		addDomHandler(this, ContextMenuEvent.getType());
 		svgElement = OMSVGParser.currentDocument().createSVGSVGElement();
 		getElement().appendChild(svgElement.getElement());
 
 	}
 
 	public void drawGlobalTopology() {
 		setupSvgElement();
 
 		NodeSvg nodeSvgClass = new NodeSvg(null);
 		LinkSvg linkSvgClass = new LinkSvg(null);
 
 		List<NodeSvg> nodeSvgs = createSvgElements(nodeSvgClass, ClientServiceHelper.INSTANCE.getTopologySwitches().values());
 		List<LinkSvg> linkSvgs = createSvgElements(linkSvgClass, ClientServiceHelper.INSTANCE.getTopologyLinks().values());
 
 		drawNodeElementsSvg(nodeSvgs);
 		drawLinkSvgs(linkSvgs, nodeSvgs);
 	}
 
 	private void setupSvgElement() {
 
 		if (svgElement.getChildNodes().getLength() > 0) {
 			svgElement = new OMSVGSVGElement();
 		}
 		width = getElement().getClientWidth() - 10;
 		height = getElement().getClientHeight() - 10;
 
 		svgElement.setWidth(OMSVGLength.SVG_LENGTHTYPE_PX, width);
 		svgElement.setHeight(OMSVGLength.SVG_LENGTHTYPE_PX, height);
 
 	}
 
 	@Override
 	public void onContextMenu(ContextMenuEvent event) {
 		contextMenu.show(event.getNativeEvent().getClientX(), event.getNativeEvent().getClientY());
 		event.preventDefault();
 		event.stopPropagation();
 	}
 
 	protected Object createSvgElement(Class svgType, GeneralModel model) {
 		AbstractElementSvg svg = null;
 
 		if (svgType.equals(SwitchSvg.class)) {
 			svg = new SwitchSvg((Switch) model);
 		} else if (svgType.equals(NodeSvg.class)) {
 			svg = new NodeSvg((Switch) model);
 		} else if (svgType.equals(LinkSvg.class)) {
 			svg = new LinkSvg((Link) model);
 		}
 
 		return svg;
 	}
 
 	protected <E, T> List<T> createSvgElements(T svgType, Collection<E> models) {
 
 		List<T> svgs = new ArrayList<T>();
 		AbstractElementSvg svg = null;
 		for (E mod : models) {
 			if (svgType instanceof SwitchSvg) {
 				svg = new SwitchSvg((Switch) mod);
 			}
 
 			else if (svgType instanceof HostSvg) {
 				svg = new HostSvg((Host) mod);
 			}
 
 			else if (svgType instanceof NodeSvg) {
 				svg = new NodeSvg((Switch) mod);
 			}
 
 			else if (svgType instanceof LinkSvg) {
 				svg = new LinkSvg((Link) mod);
 			}
 
 			svgs.add((T) svg);
 
 		}
 
 		return svgs;
 	}
 
 	protected <E> void drawNodeElementsSvg(List<? extends AbstractElementSvg> eSvgs) {
 		float radius = (float) (getElement().getClientHeight() / 3);
 		calCenter();
 
 		float slice = (float) (2 * Math.PI / eSvgs.size());
 
 		int counter = 1;
 		try {
 			for (AbstractElementSvg element : eSvgs) {
 
 				element.formElement();
 				int x = (int) (radius * Math.cos(counter * slice) + centerx);
 				int y = (int) (radius * Math.sin(counter * slice) + centery);
 				SvgTransformationHelper.translateTo(element, x, y);
 				++counter;
 				svgElement.appendChild((OMNode) element);
 			}
 		} catch (Exception e) {
 			System.err.println(e);
 		}
 	}
 
 	/**
 	 * This method must be call after all the nodes have been drawn * otherwise
 	 * it cannot setup the coordinate of line also insert line as first element
 	 * to make them stay behind the nodes
 	 *
 	 * @param <E>
 	 */
 	protected <E> void drawLinkSvgs(List<LinkSvg> linkSvgs, List<E> nodeSvgs) {
 
 		for (LinkSvg linkSvg : linkSvgs) {
 			linkSvg.findAndMatchMultipleNodes(nodeSvgs);
 			svgElement.getElement().insertFirst(linkSvg.getElement());
 		}
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	protected void calCenter() {
 		centerx = getElement().getClientWidth() / 2;
 		centery = getElement().getClientHeight() / 2;
 	}
 
 	public void drawZoom(Switch zoomSwitchModel, Map<String, Host> childHosts, Map<Integer, Link> childLinks) {
 		SwitchSvg zoomSwitchSvg = (SwitchSvg) createSvgElement(SwitchSvg.class, zoomSwitchModel);
 
 		HostSvg hostSvgClass = new HostSvg();
 		LinkSvg linkSvgClass = new LinkSvg();
 		List<LinkSvg> linkSvgs = createSvgElements(linkSvgClass, childLinks.values());
 		List<HostSvg> hostSvgs = createSvgElements(hostSvgClass, childHosts.values());
 
 		drawZoomSwitchSvg(zoomSwitchSvg);
 		drawNodeElementsSvg(hostSvgs);
 		drawZoomLinkSvgs(zoomSwitchSvg, hostSvgs, linkSvgs);
 	}
 
 	private void drawZoomLinkSvgs(SwitchSvg zoomElementSvg, List<HostSvg> hostSvgs, List<LinkSvg> linkSvgs) {
 
 		for (int i = 0; i < hostSvgs.size(); i++) {
 			LinkSvg linkSvg = linkSvgs.get(i);
 			HostSvg hostSvg = hostSvgs.get(i);
 			linkSvg.findAndMatchZoomNode(zoomElementSvg, hostSvg);
 			svgElement.getElement().insertFirst(linkSvg.getElement());
 		}
 
 	}
 
 	private void drawZoomSwitchSvg(SwitchSvg zoomSvg) {
 		calCenter();
 		zoomSvg.formElement();
 		SvgTransformationHelper.translateTo(zoomSvg, (int) centerx, (int) centery);
 		svgElement.appendChild((OMNode) zoomSvg);
 	}
 
 }
