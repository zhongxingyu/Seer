 package org.iucn.sis.shared.api.structures;
 
 
 import java.util.ArrayList;
 import java.util.Map;
 
 import org.iucn.sis.client.panels.gwt.richtextarea.RichTextToolbar;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.PrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.TextPrimitiveField;
 import org.iucn.sis.shared.api.utils.FormattingStripper;
 import org.iucn.sis.shared.api.utils.clipboard.Clipboard;
 import org.iucn.sis.shared.api.utils.clipboard.UsesClipboard;
 
 import com.extjs.gxt.ui.client.Style.Orientation;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.RichTextArea;
 import com.google.gwt.user.client.ui.Widget;
 
 public class SISRichTextArea extends SISPrimitiveStructure<String> implements UsesClipboard {
 	
 	/*
 	 * Specified by data.get("size").  I explicitly want 
 	 * enumeration here in order to maintain consistency
 	 */
 	private enum NarrativeSize {
 		SMALL("small", "50px"), 
 		MEDIUM("medium", "100px"), 
 		LARGE("large", "225px"),
 		XL("xl", "450px"),
 		XXL("xxl", "600px");
 		
 		public static NarrativeSize fromString(String value) {
 			for (NarrativeSize current : NarrativeSize.values())
 				if (current.matches(value))
 					return current;
 			return XL; 
 		}
 		
 		private String name, height;
 		
 		private NarrativeSize(String name, String height) {
 			this.name = name;
 			this.height = height;
 		}
 		
 		public String getHeight() {
 			return height;
 		}
 		
 		public boolean matches(String value) {
 			return value != null && name.equals(value.toLowerCase());
 		}
 		
 	}
 
 	private Grid areaPanel;
 	private RichTextArea area;
 	private RichTextToolbar tb;
 
 	private String viewOnlyData;
 
 	public SISRichTextArea(String struct, String descript, String structID, Object data) {
 		super(struct, descript, structID, data);
 		buildContentPanel(Orientation.VERTICAL);
 	}
 
 	@Override
 	protected PrimitiveField<String> getNewPrimitiveField() {
 		return new TextPrimitiveField(getId(), null);
 	}
 	
 	@Override
 	public void clearData() {
 		viewOnlyData = "";
 		area.setHTML("");
 	}
 
 	public void copyToClipboard() {
 		Clipboard.getInstance().add(area.getHTML(), description);
 	}
 
 	@Override
 	public Widget createLabel() {
 		clearDisplayPanel();
 		displayPanel.setSize("100%", "100%");
 		displayPanel.add(descriptionLabel);
 		displayPanel.add(areaPanel);
 		return displayPanel;
 	}
 
 	@Override
 	public Widget createViewOnlyLabel() {
 		clearDisplayPanel();
 		displayPanel.add(descriptionLabel);
 		displayPanel.add(new HTML(viewOnlyData));
 		return displayPanel;
 	}
 
 	@SuppressWarnings("unchecked")
 	public void createWidget() {
 		descriptionLabel = new HTML(description);
 		
 		NarrativeSize size;
 		if (data != null && data instanceof Map)
 			size = NarrativeSize.fromString((String)((Map)data).get("size"));
 		else
 			size = NarrativeSize.XL;
 		
 		area = new RichTextArea();
 		area.ensureDebugId("cwRichText-area");
 		area.setHeight(size.getHeight());
 		area.setWidth("100%");
 		
 		tb = new RichTextToolbar(area);
 		tb.ensureDebugId("cwRichText-toolbar");
 		tb.setWidth("100%");
 		
 		areaPanel = new Grid(2, 1);
 		areaPanel.setStyleName("cw-RichText");
 		areaPanel.setWidget(0, 0, tb);
 		areaPanel.setWidget(1, 0, area);
 		areaPanel.setWidth("100%");
 		areaPanel.setHeight("95%");
 	}
 
 	@Override
 	public String getData() {
 		try {
 			viewOnlyData = area.getHTML();
 			// STRIP OF NON_RICHTEXT FORMATTING
 			viewOnlyData = FormattingStripper.stripText(viewOnlyData);
 			return viewOnlyData.replaceAll("[\\n\\r]", " ").trim();
 		} catch (Throwable e) {
			Debug.println(e);
 			Debug.println("Error getting rich text data...");
 			return "";
 		}
 	}
 
 	/**
 	 * Pass in the raw data from an Assessment object, and this will return
 	 * it in happy, displayable String form
 	 * 
 	 * @return ArrayList of Strings, having converted the rawData to nicely
 	 *         displayable String data. Happy days!
 	 */
 	@Override
 	public int getDisplayableData(ArrayList<String> rawData, ArrayList<String> prettyData, int offset) {
 		prettyData.add(offset, rawData.get(offset));
 		return ++offset;
 	}
 
 	public void pasteFromClipboard(ArrayList<Object> items) {
 		String text = area.getHTML();
 		for (int i = 0; i < items.size(); i++)
 			text += (String) items.get(i) + "<br/>";
 		area.setHTML(text);
 	}
 
 	@Override
 	public void setData(PrimitiveField<String> field) {
 		String datum = field != null ? field.getValue() : "";
 		viewOnlyData = datum.replaceAll("[\\n\\r]", " ").trim();
 		viewOnlyData = FormattingStripper.stripText(viewOnlyData);
 		area.setHTML(viewOnlyData);
 	}
 
 	@Override
 	public void setEnabled(boolean isEnabled) {
 		// textarea.setVisible(isEnabled);
 	}
 
 }
