 package org.iucn.sis.shared.api.structures;
 
 import java.util.ArrayList;
 
 import org.iucn.sis.shared.api.models.PrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.FloatPrimitiveField;
 
 import com.extjs.gxt.ui.client.Style.Orientation;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.ChangeListener;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.KeyboardListener;
 import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 public class SISNumber extends SISPrimitiveStructure<Float> implements DominantStructure<PrimitiveField<Float>> {
 
 	private TextBox textbox;
 
 	public SISNumber(String struct, String descript, String structID) {
 		super(struct, descript, structID);
 		// displayPanel = new ContentPanel();
 		buildContentPanel(Orientation.HORIZONTAL);
 	}
 	
 	@Override
 	protected PrimitiveField<Float> getNewPrimitiveField() {
 		return new FloatPrimitiveField(getId(), null);
 	}
 
 	@Override
 	public void addListenerToActiveStructure(ChangeListener changeListener, ClickHandler clickListener,
 			KeyboardListener keyboardListener) {
 		textbox.addKeyboardListener(keyboardListener);
 	}
 
 	@Override
 	public void clearData() {
 		textbox.setText("");
 	}
 
 	@Override
 	public Widget createLabel() {
 		clearDisplayPanel();
 		displayPanel.add(descriptionLabel);
 		displayPanel.add(textbox);
 		return displayPanel;
 	}
 
 	@Override
 	public Widget createViewOnlyLabel() {
 		clearDisplayPanel();
 		displayPanel.add(descriptionLabel);
 		displayPanel.add(new HTML(textbox.getText()));
 		return displayPanel;
 	}
 
 	@Override
 	public void createWidget() {
 		this.descriptionLabel = new HTML(this.description);
 		this.textbox = new TextBox();
		textbox.addKeyboardListener(new KeyboardListenerAdapter() {
			@Override
			public void onKeyPress(Widget sender, char keyCode, int modifiers) {
				if (!Character.isDigit(keyCode)) {// || Character.isWhitespace(
					// keyCode)) {
					((TextBox) sender).cancelKey();
				}
				/*
				 * if (!Character.isDigit(keyCode)) {
				 * ((TextBox)sender).cancelKey(); }
				 */
 			}
 		});
 	}
 
 	/**
 	 * Returns an ArrayList of descriptions (as Strings) for this structure, and
 	 * if it contains multiples structures, all of those, in order.
 	 */
 	@Override
	public ArrayList extractDescriptions() {
		ArrayList ret = new ArrayList();
 		ret.add(description);
 		return ret;
 	}
 
 	@Override
 	public String getData() {
		return textbox.getText();
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
 
 	public TextBox getTextbox() {
 		return textbox;
 	}
 
 	@Override
 	public boolean isActive(Rule activityRule) {
 		return ((ContentRule) activityRule).matchesContent(textbox.getText());
 	}
 	
 	@Override
 	public void setData(PrimitiveField<Float> field) {
 		Float datum = field != null ? field.getValue() : null;
 		textbox.setText(datum == null ? "" : String.valueOf(datum));
 	}
 
 	@Override
 	public void setEnabled(boolean isEnabled) {
 		textbox.setEnabled(isEnabled);
 	}
 
 	public String toXML() {
 		return StructureSerializer.toXML(this);
 	}
 }
