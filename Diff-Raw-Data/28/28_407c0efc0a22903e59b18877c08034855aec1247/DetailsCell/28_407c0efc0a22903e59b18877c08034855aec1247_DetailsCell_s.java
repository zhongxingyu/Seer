 /*
  * Copyright (C) 2011 Openismus GmbH
  *
  * This file is part of GWT-Glom.
  *
  * GWT-Glom is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  *
  * GWT-Glom is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with GWT-Glom.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.glom.web.client.ui.details;
 
 import org.glom.web.client.Utils;
 import org.glom.web.shared.DataItem;
 import org.glom.web.shared.GlomNumericFormat;
 import org.glom.web.shared.layout.Formatting;
 import org.glom.web.shared.layout.LayoutItemField;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.Label;
 
 /**
  * Holds a label, data and a navigation button.
  * 
  * @author Ben Konrath <ben@bagu.org>
  * 
  */
 public class DetailsCell extends Composite {
 	private LayoutItemField layoutItemField;
 	private FlowPanel detailsData = new FlowPanel();
 	private Label detailsLabel = new Label();
 	private DataItem dataItem;
 
 	private Button openButton = null;
 	private HandlerRegistration openButtonHandlerReg = null;
 
 	public DetailsCell(LayoutItemField layoutItemField) {
 		// Labels (text in div element) are being used so that the height of the details-data element can be set for
 		// the multiline height of LayoutItemFeilds. This allows the the data element to display the correct height
 		// if style is applied that shows the height. This has the added benefit of allowing the order of the label and
 		// data elements to be changed for right-to-left languages.
 
 		Label detailsLabel = new Label(layoutItemField.getTitle() + ":");
 		detailsLabel.setStyleName("details-label");
 
 		detailsData.setStyleName("details-data");
 		Formatting formatting = layoutItemField.getFormatting();
 
 		// set the height based on the number of lines
 		detailsData.setHeight(formatting.getTextFormatMultilineHeightLines() + "em");
 
 		// set the alignment
 		switch (formatting.getHorizontalAlignment()) {
 		case HORIZONTAL_ALIGNMENT_LEFT:
 			detailsLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 			break;
 		case HORIZONTAL_ALIGNMENT_RIGHT:
 			detailsLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 			break;
 		case HORIZONTAL_ALIGNMENT_AUTO:
 		default:
 			detailsLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_DEFAULT);
 			break;
 		}
 
 		// set the text foreground and background colours
 		String foregroundColour = formatting.getTextFormatColourForeground();
 		if (foregroundColour != null && !foregroundColour.isEmpty())
 			detailsData.getElement().getStyle().setColor(foregroundColour);
 		String backgroundColour = formatting.getTextFormatColourBackground();
 		if (backgroundColour != null && !backgroundColour.isEmpty())
 			detailsData.getElement().getStyle().setBackgroundColor(backgroundColour);
 
 		FlowPanel mainPanel = new FlowPanel();
 		mainPanel.setStyleName("details-cell");
 
 		mainPanel.add(detailsLabel);
 		mainPanel.add(detailsData);
 
 		if (layoutItemField.getAddNavigation()) {
 			openButton = new Button("Open");
 			openButton.setStyleName("details-navigation");
 			openButton.setEnabled(false);
 			mainPanel.add(openButton);
 		}
 
 		this.layoutItemField = layoutItemField;
 
 		initWidget(mainPanel);
 	}
 
 	public DataItem getData() {
 		return dataItem;
 	}
 
 	public void setData(final DataItem dataItem) {
 		detailsData.clear();
 
 		if (dataItem == null)
 			return;
 
 		// FIXME use the cell renderers from the list view to render the inforamtion here
 		switch (layoutItemField.getType()) {
 		case TYPE_BOOLEAN:
 			final CheckBox checkBox = new CheckBox();
 			checkBox.setValue(dataItem.getBoolean());
 			checkBox.addClickHandler(new ClickHandler() {
 
 				@Override
 				public void onClick(ClickEvent event) {
					// don't let the user change the
 					checkBox.setValue(dataItem.getBoolean());
 				}
 			});
 			detailsData.add(checkBox);
 			break;
 		case TYPE_NUMERIC:
 			GlomNumericFormat glomNumericFormat = layoutItemField.getFormatting().getGlomNumericFormat();
 			NumberFormat gwtNumberFormat = Utils.getNumberFormat(glomNumericFormat);
 
 			// set the foreground colour to red if the number is negative and this is requested
 			if (glomNumericFormat.getUseAltForegroundColourForNegatives() && dataItem.getNumber() < 0) {
 				// The default alternative colour in libglom is red.
 				detailsData.getElement().getStyle().setColor("Red");
 			}
 
 			detailsLabel.setText(gwtNumberFormat.format(dataItem.getNumber()));
 			detailsData.add(detailsLabel);
 			break;
 		case TYPE_TEXT:
 			detailsLabel.setText(dataItem.getText());
 			detailsData.add(detailsLabel);
 		default:
 			break;
 		}
 
 		this.dataItem = dataItem;
 
 		// enable the navigation button if it's safe
 		if (openButton != null && openButtonHandlerReg != null && this.dataItem != null) {
 			openButton.setEnabled(true);
 		}
 
 	}
 
 	public LayoutItemField getLayoutItemField() {
 		return layoutItemField;
 	}
 
 	public HandlerRegistration setOpenButtonClickHandler(ClickHandler clickHandler) {
 		if (openButton != null) {
 			openButtonHandlerReg = openButton.addClickHandler(clickHandler);
 		}
 		return openButtonHandlerReg;
 	}
 
 }
