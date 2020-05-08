 /*
  * Copyright 2010 Ian Hilt
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.eastway.echarts.client.ui;
 
 import java.util.List;
 
 import org.eastway.echarts.client.common.ColumnDefinition;
 import org.eastway.echarts.client.style.GlobalResources;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.SpanElement;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Widget;
 
 public class DemographicsViewImpl<T> extends Composite implements DemographicsView<T> {
 	@SuppressWarnings("unchecked")
 	@UiTemplate("DemographicsView.ui.xml")
 	interface DemographicsViewUiBinder extends UiBinder<Widget, DemographicsViewImpl> { }
 
 	private static DemographicsViewUiBinder uiBinder = GWT
 			.create(DemographicsViewUiBinder.class);
 
 	//private Presenter<T> presenter;
 	private T rowData;
 	private List<ColumnDefinition<T>> columnDefinitions;
 	@UiField FlexTable table;
 	@UiField SpanElement error;
 
 	public DemographicsViewImpl() {
 		initWidget(uiBinder.createAndBindUi(this));
 		table.addStyleName(GlobalResources.styles().table());
 	}
 
 	@Override
 	public Widget asWidget() {
 		return this;
 	}
 
 	@Override
 	public void setPresenter(Presenter<T> presenter) {
 		//this.presenter = presenter;
 	}
 
 	@Override
 	public void setRowData(T rowData) {
 		if (rowData == null)
 			return;
		else
			error.setInnerText("");

 		this.rowData = rowData;
 		for (int i = 0; i < columnDefinitions.size(); i++) {
 			StringBuilder sb = new StringBuilder();
 			table.setHTML(i, 0, columnDefinitions.get(i).getHeader(this.rowData));
 			columnDefinitions.get(i).render(this.rowData, sb);
 			table.setHTML(i, 1, sb.toString());
 		}
 	}
 
 	@Override
 	public void setColumnDefinitions(List<ColumnDefinition<T>> columnDefinitions) {
 		this.columnDefinitions = columnDefinitions;
 	}
 
 	@Override
 	public void setError(String message) {
 		error.setInnerText(message);
 	}
 }
