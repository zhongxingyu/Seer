 package org.iucn.sis.shared.api.structures;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.iucn.sis.client.api.utils.FormattedDate;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.PrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.DatePrimitiveField;
 
 import com.extjs.gxt.ui.client.Style.Orientation;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Widget;
 import com.solertium.util.gwt.api.DatePicker;
 
 public class SISDate extends SISPrimitiveStructure<Date> {
 
 	private DatePicker datePicker;
 
 	public SISDate(String struct, String descript, String structID) {
 		super(struct, descript, structID);
 		buildContentPanel(Orientation.HORIZONTAL);
 	}
 
 	@Override
 	protected DatePrimitiveField getNewPrimitiveField() {
 		return new DatePrimitiveField(getId(), null);
 	}
 	
 	@Override
 	public void clearData() {
 		datePicker.clearText();
 	}
 
 	@Override
 	public Widget createLabel() {
 		clearDisplayPanel();
 		displayPanel.add(descriptionLabel);
 		displayPanel.add(datePicker);
 		return displayPanel;
 	}
 
 	@Override
 	public Widget createViewOnlyLabel() {
 		clearDisplayPanel();
 		displayPanel.add(descriptionLabel);
 		displayPanel.add(new HTML(datePicker.getText()));
 		return displayPanel;
 	}
 
 	@Override
 	public void createWidget() {
 		descriptionLabel = new HTML(description);
 		datePicker = new DatePicker("images/office-calendar.png", DatePicker.InternationalDate, false, false);
 		datePicker.setSeparator("-");
 	}
 
 	@Override
 	public String getData() {
 		String value = datePicker.getText();
 		if ("".equals(value))
 			value = null;
 		
		if (value != null)
			value = Long.toString(FormattedDate.SHORT.getDate(value).getTime());
		
 		return value;
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
		prettyData.add(offset, formatTimestamp(rawData.get(offset)));
 		return ++offset;
 	}
 	
 	@Override
 	public boolean hasChanged(PrimitiveField<Date> field) {
 		Date oldValue = field == null ? null : field.getValue();
 		
		String newValue = formatTimestamp(getData());
 		
 		if (newValue == null)
 			return oldValue != null;
 		else
 			if (oldValue == null)
 				return true;
 			else {
 				String oldValueFormatted = FormattedDate.impl.getDate(oldValue);
 				Debug.println("Comparing {0} to {1}", oldValue, newValue);
 				return !newValue.equals(oldValueFormatted);
 			}
 	}
 
 	@Override
 	public void setData(PrimitiveField<Date> field) {
 		final Date datum;
 		if (field == null)
 			datum = null;
 		else
 			datum = field.getValue();
 		
 		if (datum != null)
 			datePicker.setText(FormattedDate.impl.getDate(datum));
 		else
 			datePicker.setText("");
 	}
 
 	@Override
 	public void setEnabled(boolean isEnabled) {
 		datePicker.setEnabled(isEnabled);
 	}
	
	private String formatTimestamp(String value) {
		try {
			return formatTimestamp(Long.valueOf(value));
		} catch (Exception e) {
			return null;
		}
	}
	
	private String formatTimestamp(long value) {
		return FormattedDate.SHORT.getDate(new Date(value));
	}
 
 }
