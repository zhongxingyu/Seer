 package at.fakeroot.sepm.client;
 
 import at.fakeroot.sepm.client.serialize.*;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 
 import com.google.gwt.dom.client.Style;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.InlineLabel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * TagCloud-Widget class
  * 
  * Draws a TagCloud of the tags within the ClientGeoObjects given to refresh()
  * @author Manuel Reithuber
  */
 public class TagCloud extends Composite implements ClickHandler {
 	/**
 	 * Panel in which the tags are drawn
 	 */
 	private FlowPanel tagPanel = new FlowPanel();
 	/**
 	 * Widget Container
 	 */
 	private VerticalPanel vPanel = new VerticalPanel();
 	/**
 	 * Tag reference count statistics
 	 */
 	private HashMap<String, Integer> tagStat = new HashMap<String, Integer>();
 	/**
 	 * GeoManager reference
 	 */
 	private IGeoManager geoManager;
 	
 	/**
 	 * Constructor
 	 * @param gm GeoManager reference
 	 */
 	public TagCloud(IGeoManager gm) {
 		geoManager = gm;
 		init();
 	}
 	
 	/**
 	 * Widget initialization
 	 */
 	private void init() {
 		Label l = new Label("TagCloud");
 		l.setHeight("1.2em");
 		vPanel.add(l);
 		vPanel.setCellHeight(l, "1.2em");
 		initWidget(vPanel);
 		setWidth("200px");
 		
 		Style s = l.getElement().getStyle();
 		s.setProperty("borderBottom", "1px solid black");
 		vPanel.add(tagPanel);
 	}
 	
 	/**
 	 * Add a String array of tags to the tag count statistics (tagStat)
 	 * 
 	 * Internal function, called by refresh()
 	 * @param tags tags to be added
 	 */
 	private void addTags(String[] tags) {
 		String tag;
 		int i;
 
 		for (i = 0; i < tags.length; i++) {
 			tag = tags[i];
 			if (tagStat.containsKey(tag)) {
 				tagStat.put(tag, tagStat.get(tag)+1);
 			}
 			else tagStat.put(tag, 1);
 		}
 	}
 	
 	/**
 	 * creates Anchor widgets for the tags, resizes them based on their reference count and adds ClickHandlers to them
 	 * 
 	 * Internal function, called by refresh()
 	 */
 	private void drawTags() {
 		Style s;
 		Anchor a;
 		String tag;
 		int i;
 		
 		Set<String> c = tagStat.keySet();
 		
 		int min = tagStat.size(), max = 0;
 
 		// get the minimal and maximal Tag frequency
 		Iterator<String> it = c.iterator();
 		while (it.hasNext()) {
 			tag = it.next();
 			
 			i = tagStat.get(tag);
 			if (i < min) min = i;
 			if (i > max) max = i;
 		}
 		
 		it = c.iterator();
 		
 		int fontsize = 100;
 		while (it.hasNext()) {
 			tag = it.next();
 			a = new Anchor(tag);
 			a.setTitle(tag);
 
 			s = a.getElement().getStyle();
 			// calculation source: German Wikipedia
 			if (max > min) fontsize = (int) (70+Math.ceil(130*(tagStat.get(tag)-min))/(max-min));
 
 			s.setProperty("fontSize", fontsize+"%");
 
 			a.addClickHandler(this);
 			tagPanel.add(a);
 			tagPanel.add(new InlineLabel(" ")); 
 		}
 	}
 	
 	/**
 	 * Refresh the TagCloud based on the tags given by @c it
 	 * @param it ClientGeoObject iterator that points at the new list of ClientGeoObjects
 	 */
 	public void refresh(Iterator<ClientGeoObject> it) {
 		ClientGeoObject o;
 
 		tagStat.clear();
 
 		while (it.hasNext()) {
 			o = it.next();
 			addTags(o.getTags());
 		}
 
 		tagPanel.clear();
 		drawTags();
 	}
 
 	/**
 	 * ClickEventHandler for the tag Anchors
 	 */
 	@Override
 	public void onClick(ClickEvent e) {
 		// Tag clicked
 		if (e.getSource() instanceof Anchor) {
 			Anchor a = (Anchor) e.getSource();
 			geoManager.addSearchTag(a.getText());
 		}
 	}
 }
