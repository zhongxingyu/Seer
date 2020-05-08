 package org.iucn.sis.shared.api.structures;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.iucn.sis.shared.api.data.LookupData;
 import org.iucn.sis.shared.api.data.LookupData.LookupDataValue;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.PrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyPrimitiveField;
 import org.iucn.sis.shared.api.utils.XMLUtils;
 
 import com.extjs.gxt.ui.client.Style.Orientation;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class SISSelect extends SISPrimitiveStructure<Integer> implements DominantStructure<PrimitiveField<Integer>> {
 
 	private ListBox listbox;
 
 	public SISSelect(String struct, String descript, String structID, Object data) {
 		super(struct, descript, structID, data);
 		if (isSingle())
 			buildContentPanel(Orientation.HORIZONTAL);
 		else
 			buildContentPanel(Orientation.VERTICAL);
 	}
 
 	@Override
 	protected PrimitiveField<Integer> getNewPrimitiveField() {
 		return new ForeignKeyPrimitiveField(getId(), null);
 	}
 	
 	@Override
 	public void addListenerToActiveStructure(ChangeHandler changeListener, ClickHandler clickListener,
 			KeyUpHandler keyboardListener) {
 		listbox.addChangeHandler(changeListener);
 		DOM.setEventListener(listbox.getElement(), listbox);
 	}
 
 	@Override
 	public void clearData() {
 		listbox.setSelectedIndex(0);
 	}
 
 	@Override
 	public Widget createLabel() {
 		clearDisplayPanel();
 
 		if (isSingle()) {
 			((HorizontalPanel) displayPanel).setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
 			((HorizontalPanel) displayPanel).setSpacing(5);
 		} else
 			((VerticalPanel) displayPanel).setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
 
 		displayPanel.add(descriptionLabel);
 		displayPanel.add(listbox);
 		return displayPanel;
 	}
 
 	@Override
 	public Widget createViewOnlyLabel() {
 		clearDisplayPanel();
 
 		if (isSingle()) {
 			((HorizontalPanel) displayPanel).setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
 			((HorizontalPanel) displayPanel).setSpacing(5);
 		} else
 			((VerticalPanel) displayPanel).setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
 
 		displayPanel.add(descriptionLabel);
 		if (listbox.isItemSelected(0)) {
 			displayPanel.add(new HTML("None Selected"));
 		} else {
 			String text = "";
 			int numSelected = 0;
 			int numWritten = 0;
 			for (int i = 0; i < listbox.getItemCount(); i++) {
 				if (listbox.isItemSelected(i))
 					numSelected++;
 			}
 			for (int i = 0; i < listbox.getItemCount(); i++) {
 				if (listbox.isItemSelected(i)) {
 					text += listbox.getItemText(i);
 					text += (++numWritten < numSelected) ? "," : "";
 				}
 			}
 			displayPanel.add(new HTML(text));
 		}
 		return displayPanel;
 	}
 
 	@Override
 	public void createWidget() {
 		try {
 			descriptionLabel = new HTML(description);
 		} catch (Exception e) {
 		}
 
		listbox = new ListBox(!isSingle());
 		LookupData myData = ((LookupData)data);
 		List<LookupDataValue> listItemsToAdd = myData.getValues();
 				
 		listbox.addItem("--- Select ---");
 
 		int index = 1;
 		for (LookupDataValue value : listItemsToAdd) {
 			listbox.addItem(value.getLabel(), value.getID());
 			
 			if (myData.getDefaultValues().contains(value.getID())) {
 				listbox.setSelectedIndex(index);
 			}
 			index++;
 		}
 		
 		if (isSingle())
 			listbox.setVisibleItemCount(1);
 		else
 			listbox.setVisibleItemCount(listbox.getItemCount());
 
 		if (!isSingle()) {
 			listbox.addClickHandler(new ClickHandler() {
 				public void onClick(ClickEvent event) {
 					if (listbox.isItemSelected(0)) {
 						for (int i = 1; i < listbox.getItemCount(); i++) {
 							listbox.setItemSelected(i, false);
 						}
 					}
 				}
 			});
 		}
 	}
 	
 	@Override
 	public String getData() {
 		if (listbox.getSelectedIndex() == 0)
 			return null;
 		else
 			return listbox.getValue(listbox.getSelectedIndex());
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
 		String selectedValue = rawData.get(offset);
 
 		String pretty = ((LookupData)data).getLabel(selectedValue);
 		if (pretty == null)
 			pretty = "(Not Specified)";
 
 		prettyData.add(offset, pretty);
 		
 		return ++offset;
 	}
 
 	public ListBox getListbox() {
 		return listbox;
 	}
 
 	@Override
 	public boolean isActive(Rule activityRule) {
 		try {
 			if (isSingle())
 				return ((SelectRule) activityRule).isSelected(listbox.getSelectedIndex());
 			else
 				return listbox.isItemSelected(((SelectRule) activityRule).getIndexInQuestion());
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean isSingle() {
 		return structure.equalsIgnoreCase(XMLUtils.SINGLE_SELECT_STRUCTURE);
 	}
 	
 	@Override
 	public void setData(PrimitiveField<Integer> field) {
 		String value = field != null ? field.getRawValue() : "";
 		listbox.setSelectedIndex(0);
 		try {
 			for (int i = 1; i < listbox.getItemCount(); i++)
 				if (listbox.getValue(i).equals(value))
 					listbox.setSelectedIndex(i);
 		} catch (IndexOutOfBoundsException unlikely) {
 			Debug.println("Empty select list");
 		}
 	}
 
 	@Override
 	public void setEnabled(boolean isEnabled) {
 		listbox.setEnabled(isEnabled);
 	}
 
 }
