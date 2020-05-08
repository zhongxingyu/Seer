 /**
  * 
  */
 package org.cotrix.web.importwizard.client.step.csvpreview;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.cotrix.web.importwizard.client.resources.Resources;
 import org.cotrix.web.importwizard.client.step.csvpreview.PreviewGrid.DataProvider.PreviewData;
 import org.cotrix.web.share.client.resources.CommonResources;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.rpc.IsSerializable;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.ResizeComposite;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class PreviewGrid extends ResizeComposite {
 	
 	protected static final int HEADER_ROW = 0;
 	
 	protected ScrollPanel scroll;
 	protected FlexTable grid;
 	protected FlexTable loadingContainter;
 	
 	protected List<TextBox> headerFields = new ArrayList<TextBox>();
 	
 	protected DataProvider dataProvider;
 	
 	public PreviewGrid(){}
 	
 	public PreviewGrid(DataProvider dataProvider)
 	{
 		this.dataProvider = dataProvider;
 		
 		scroll = new ScrollPanel();
 		grid = new FlexTable();
 		grid.setStyleName(Resources.INSTANCE.css().preview());
 		scroll.setWidget(grid);
 		
 		setupLoadingContainer();
 		initWidget(scroll);
 	}
 
 	protected void setupLoadingContainer()
 	{
 		loadingContainter = new FlexTable();
 		loadingContainter.getElement().setAttribute("align", "center");
		loadingContainter.setWidth("100%");
		loadingContainter.setHeight("100%");
 		Image loader = new Image(CommonResources.INSTANCE.dataLoader());
 		loadingContainter.setWidget(0, 0, loader);
 	}
 	
 	public void loadData()
 	{
 		setLoading();
 		AsyncCallback<PreviewData> callback = new AsyncCallback<PreviewGrid.DataProvider.PreviewData>() {
 			
 			@Override
 			public void onSuccess(PreviewData result) {
 				setData(result);
 				unsetLoading();
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
 				unsetLoading();
 			}
 		};
 		dataProvider.getData(callback);
 	}
 	
 	protected void setLoading()
 	{
 		scroll.setWidget(loadingContainter);
 	}
 	
 	protected void unsetLoading()
 	{
 		scroll.setWidget(grid);
 	}
 	
 	protected void setData(PreviewData data)
 	{
 		grid.removeAllRows();
 		setupHeader(data.getHeadersLabels(), data.isHeadersEditable());
 		setRows(data.getRows());
 	}
 	
 	protected void setupHeader(List<String> headersLabels, boolean editable)
 	{
 		Log.trace("setupHeader "+headersLabels+" editable? "+editable);
 		headerFields.clear();
 		for (int i = 0; i < headersLabels.size(); i++) {
 			String headerLabel = headersLabels.get(i);
 			
 			Widget header = null;
 			if (editable) {
 				TextBox headerField = new TextBox();
 				headerField.setValue(headerLabel);
 				headerField.setStyleName(CommonResources.INSTANCE.css().textBox());
 				headerFields.add(headerField);
 				header = headerField;
 			} else {
 				header = new HTML(headerLabel);
 			}
 			
 			grid.setWidget(HEADER_ROW, i, header);
 			grid.getCellFormatter().setStyleName(HEADER_ROW, i, Resources.INSTANCE.css().previewHeader());
 		}
 	}
 	
 
 	protected void setRows(List<List<String>> rows) {
 		for (List<String> row:rows) addRow(row);
 	}
 	
 	protected void addRow(List<String> row)
 	{
 		int rowIndex = grid.getRowCount();
 		for (int i = 0; i < row.size(); i++) {
 			String cell = row.get(i);
 			grid.setWidget(rowIndex, i, new HTML(cell));
 			grid.getCellFormatter().setStyleName(rowIndex, i, Resources.INSTANCE.css().previewCell());
 		}
 	}
 
 	public List<String> getHeaders() {
 		ArrayList<String> headers = new ArrayList<String>();
 		for (TextBox headerField:headerFields) headers.add(headerField.getText());
 		return headers;
 	}
 	
 	
 	public interface DataProvider {
 
 		public class PreviewData implements IsSerializable {
 			
 			protected List<String> headersLabels;
 			protected boolean headersEditable;
 			protected List<List<String>> rows;
 			
 			public PreviewData(){}
 			
 			/**
 			 * @param headersLabels
 			 * @param headersEditable
 			 * @param rows
 			 */
 			public PreviewData(List<String> headersLabels,
 					boolean headersEditable, List<List<String>> rows) {
 				this.headersLabels = headersLabels;
 				this.headersEditable = headersEditable;
 				this.rows = rows;
 			}
 			/**
 			 * @return the headersLabels
 			 */
 			public List<String> getHeadersLabels() {
 				return headersLabels;
 			}
 			/**
 			 * @return the headersEditable
 			 */
 			public boolean isHeadersEditable() {
 				return headersEditable;
 			}
 			/**
 			 * @return the rows
 			 */
 			public List<List<String>> getRows() {
 				return rows;
 			}
 
 		}
 		public void getData(AsyncCallback<PreviewData> callback);
 	}
 }
