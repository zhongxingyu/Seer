 package com.binar.core.inventoryManagement.stockList;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.joda.time.DateTime;
 
 import com.binar.core.inventoryManagement.stockList.StockListView.TableData;
 import com.binar.entity.Goods;
 import com.binar.entity.GoodsConsumption;
 import com.binar.entity.GoodsReception;
 import com.binar.entity.Manufacturer;
 import com.binar.entity.PurchaseOrder;
 import com.binar.entity.SupplierGoods;
 import com.binar.generalFunction.DateManipulator;
 import com.binar.generalFunction.GeneralFunction;
 import com.binar.generalFunction.TableFilter;
 import com.binar.generalFunction.TextManipulator;
 import com.vaadin.data.Item;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.data.util.IndexedContainer;
 import com.vaadin.event.FieldEvents.TextChangeEvent;
 import com.vaadin.event.FieldEvents.TextChangeListener;
 import com.vaadin.server.ThemeResource;
 import com.vaadin.shared.ui.label.ContentMode;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.ComboBox;
 import com.vaadin.ui.DateField;
 import com.vaadin.ui.GridLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Notification.Type;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Table.RowHeaderMode;
 import com.vaadin.ui.Window;
 
 public class StockListViewImpl  extends VerticalLayout implements StockListView, ValueChangeListener{
 
 
 	private GeneralFunction function;
 	private StockListListener listener;
 
 	private Label title;
 	private Table table;
 	private IndexedContainer tableContainer;
 	private Label labelFilter;
 	private TextField inputFilter;
 	private ComboBox selectGoods;
 	
 	private Label labelGoodsName;
 	private Label labelUnit;
 	private Label labelPackage;
 	private Label labelManufacturer;
 	
 	private TableFilter filter;
 	private TextManipulator text;
 	private DateField selectStartDate;
 	private DateField selectEndDate;
 
 	private DateManipulator date;
 	public StockListViewImpl(GeneralFunction function){
 		this.function=function;
 		this.date=function.getDate();
 		this.text=function.getTextManipulator();
 		
 	}
 	
 	@Override
 	public void init() {
 		title=new Label("<h2>Stok Barang Gudang Farmasi</h2>", ContentMode.HTML);
 		labelFilter=new Label("<b style='font-size:14px'>Filter : </b> ", ContentMode.HTML);
 		
 		selectStartDate = new DateField("Rentang Tanggal Mulai", DateTime.now().minusDays(30).toDate());
 		selectEndDate =new DateField("Rentang Tanggal Akhir",DateTime.now().toDate());
 
 		selectStartDate.setImmediate(true);
 		selectEndDate.setImmediate(true);
 
 		
 		System.out.println("Bulan ini" + Calendar.getInstance().get(Calendar.MONTH));
 		selectStartDate.addStyleName("non-caption-form");
 		selectEndDate.addStyleName("non-caption-form");
 
 		selectEndDate.setWidth("120px");
 		selectStartDate.setWidth("120px");
 	
 		selectEndDate.addValueChangeListener(this);
 		selectStartDate.addValueChangeListener(this);
 		
 		selectGoods=new ComboBox("Pilih Barang");
 		selectGoods.addValueChangeListener(this);
 		selectGoods.setWidth("200px");
 		selectGoods.setImmediate(true);
 		labelGoodsName=new Label();
 		labelUnit=new Label();
 		labelPackage=new Label();
 		labelManufacturer=new Label();
 		
 		filter=function.getFilter("");
 		inputFilter=new TextField("Filter Data"){
 			{
 				setImmediate(true);
 				addTextChangeListener(new TextChangeListener() {
 					
 					@Override
 					public void textChange(TextChangeEvent event) {
 						if(filter!=null){
 							tableContainer.removeAllContainerFilters();
 						}
 						//set filter baru
 						filter.updateData(event.getText());
 						tableContainer.addContainerFilter(filter);
 					}
 				});
 			}
 		};
 		
 		table=new Table();
 		table.setSizeFull();
 		table.setWidth("100%");
 		table.setHeight("340px");
 		table.setPageLength(10);
 		table.setSortEnabled(true);
 		table.setImmediate(true);
 		table.setRowHeaderMode(RowHeaderMode.INDEX);
 		
 		tableContainer=new IndexedContainer(){
 			{
 				addContainerProperty("Tanggal", String.class,null);
 				addContainerProperty("PBF",String.class,null);
 				addContainerProperty("ED", String.class,null);
 				addContainerProperty("Jumlah Masuk",String.class,null);
 				addContainerProperty("Detail Masuk",Button.class,null);
 				addContainerProperty("_",Label.class,new Label(" | ", ContentMode.HTML));
 				addContainerProperty("Jumlah Keluar", String.class, null);
 				addContainerProperty("Sisa", String.class, null);
 				addContainerProperty("Keterangan", String.class, null);
 				addContainerProperty("Detail Keluar", Button.class,null);
 			}
 		};
 		table.setContainerDataSource(tableContainer);
 
 		construct();
 	}
 	
 	@Override
 	public void construct() {
 		this.addComponent(title);
 
 		GridLayout layoutFilter =new GridLayout(5,1);
 			layoutFilter.setMargin(true);
 			layoutFilter.setSpacing(true);
 			layoutFilter.addComponent(selectGoods, 0,0);
 			layoutFilter.addComponent(selectStartDate, 1, 0);
 			layoutFilter.addComponent(selectEndDate, 2, 0);
 			//untuk spasi
 			layoutFilter.addComponent(new Label("<div style='padding-left:20px'></div>", ContentMode.HTML), 3,0);
 			layoutFilter.addComponent(inputFilter, 4, 0);
 			
 		this.addComponent(layoutFilter);
 		this.addComponent(new GridLayout(2,5){
 			{
 				setMargin(true);
 				setSpacing(true);
 				addComponent(new Label("Nama Barang"), 0,1);
				addComponent(new Label("Stok Saat Ini"), 0,2);
				addComponent(new Label("Kemasan"), 0,3);
				addComponent(new Label("Pabrik"), 0,4);
 				addComponent(labelGoodsName, 1,1);
				addComponent(labelUnit, 1,2);
				addComponent(labelPackage, 1,3);
				addComponent(labelManufacturer, 1,4);
 			}
 		});
 		this.addComponent(table);
 	}
 
 
 	@Override
 	public void valueChange(ValueChangeEvent event) {
 		if(event.getProperty()==selectGoods){
 			listener.goodsChange();
 		}
 		if(event.getProperty()==selectEndDate){
 			listener.periodChange();
 		}
 		if(event.getProperty()==selectStartDate ){
 			listener.periodChange();
 		}
 	}
 
 	@Override
 	public void setListener(StockListListener listener) {
 		this.listener=listener;
 	}
 
 	public boolean updateTableData(Goods goods){
 		tableContainer.removeAllItems();
 		if(goods!=null){
 			labelGoodsName.setValue(goods.getName());
 			labelUnit.setValue(goods.getUnit());;
 			labelPackage.setValue(goods.getGoodsPackage());			
 		}
 		
 		return true;
 	}
 	@Override
 	public boolean updateTableData(List<TableData> data) {
 		tableContainer.removeAllItems(); //sempat error karena konten belum diremove
 		if(data.size()!=0){
 			String goodsName="";
 			String unit="";
 			String goodsPackage="";
 			String manufacturer="";
 			//jika penerimaan barang tidak kosong
 			if(data.get(0).getReception()!=null){
 				SupplierGoods goods=data.get(0).getReception().getInvoiceItem().
 						getPurchaseOrderItem().getSupplierGoods();
 				goodsName=goods.getGoods().getName();
 				unit=goods.getGoods().getUnit();
 				goodsPackage=goods.getGoods().getGoodsPackage();
 				manufacturer=goods.getManufacturer().getManufacturerName();
 			} //jika reception kosong, maka menggunakan data barang dari konsumsi barang 
 			else{
 				Goods goods=data.get(0).getConsumption().getGoods();
 				goodsName=goods.getName();
 				unit=goods.getUnit();
 				goodsPackage=goods.getGoodsPackage();
 			}
 			
 			labelGoodsName.setValue(goodsName);
 			labelUnit.setValue(unit);;
 			labelPackage.setValue(goodsPackage);;
 			labelManufacturer.setValue(manufacturer);
 			
 	        for (TableData datum:data) { 
 	        
 	        	System.out.println("Table data insert "+datum.getDate().toString());
 	        	final GoodsConsumption consumption=datum.getConsumption();
 	        	final GoodsReception reception=datum.getReception();
 	        	
 				Item item = tableContainer.addItem(datum.getDate().toDate());
 				/* add data ke tabel */
 				if(item==null){
 					System.err.println("Item null");
 				}
 				if(datum==null){
 					System.err.println("Datum null");
 				}
 				if(datum.getDate()==null){
 					System.out.println("Date null");
 				}
 				if(date==null){
 					System.err.println("Date Null");
 				}
 				
 				item.getItemProperty("Tanggal").setValue(date.dateToText(datum.getDate().toDate(), true));
 				item.getItemProperty("_").setValue(new Label(" | ", ContentMode.HTML));
 				if(reception!=null){
 					System.out.println("Resepsi tidak null");
 					item.getItemProperty("PBF").setValue(reception.getInvoiceItem().getPurchaseOrderItem().getSupplierGoods().getSupplier().getSupplierName());;
 					item.getItemProperty("ED").setValue(date.dateToText(reception.getExpiredDate(), true));
 					item.getItemProperty("Jumlah Masuk").setValue(text.intToAngka(reception.getQuantityReceived()));
 					item.getItemProperty("Detail Masuk").setValue(new Button(){ //mengeset operasi
 						{
 							{
 								setDescription("Lihat Lebih detail");
 								setIcon(new ThemeResource("icons/image/icon-detail.png"));
 								addStyleName("button-table");
 								
 								addClickListener(new ClickListener() {
 									
 									@Override
 									public void buttonClick(ClickEvent event) {
 										listener.showReceptionDetail(reception.getIdGoodsReceipt());
 									}
 								});
 							}
 						}});
 										
 				}
 				
 				if(consumption!=null){
 					item.getItemProperty("Jumlah Keluar").setValue(text.intToAngka(consumption.getQuantity()));
 					item.getItemProperty("Sisa").setValue(text.intToAngka(consumption.getStockQuantity()));
 					item.getItemProperty("Keterangan").setValue(consumption.getInformation());
 					item.getItemProperty("Detail Keluar").setValue(new Button(){ //mengeset operasi
 						{
 							{
 								setDescription("Lihat Lebih detail");
 								setIcon(new ThemeResource("icons/image/icon-detail.png"));
 								addStyleName("button-table");
 								
 								addClickListener(new ClickListener() {
 									
 									@Override
 									public void buttonClick(ClickEvent event) {
 										listener.showConsumptionDetail(consumption.getIdGoodsConsumption());
 									}
 								});
 							}
 						}});
 									
 				}
 
 			}
 			
 		}
 		return false;
 	}
 
 
 	private Window windowDetail;
 	private Label labelGoods; 
 	private Label labelQuantity;
 	private Label labelConsumptionDate;
 	private Label labelInformation;	
 	private Label labelWard;
 	private GridLayout layoutDetailConsumption;
 	
 	@Override
 	public void showDetailConsumption(GoodsConsumption consumption) {
 		if(layoutDetailConsumption==null){
 			layoutDetailConsumption=new GridLayout(2,5){
 				{
 					setSpacing(true);
 					setMargin(true);
 					setWidth("450px");
 					addComponent(new Label("Nama Barang"), 0, 0);
 					addComponent(new Label("Jumlah"), 0,1);
 					addComponent(new Label("Tanggal"), 0, 2);
 					addComponent(new Label("Instalasi"), 0,3);
 					addComponent(new Label("Keterangan"), 0,4);
 	
 				}
 			};
 			
 			labelGoods=new Label();
 			labelQuantity=new Label();
 			labelConsumptionDate=new Label();
 			labelInformation=new Label();
 			labelWard=new Label();
 			
 			layoutDetailConsumption.addComponent(labelGoods, 1,0);
 			layoutDetailConsumption.addComponent(labelQuantity, 1,1);
 			layoutDetailConsumption.addComponent(labelConsumptionDate, 1,2);
 			layoutDetailConsumption.addComponent(labelWard, 1,3);
 			layoutDetailConsumption.addComponent(labelInformation, 1, 4);
 		}
 		setLabelData(consumption);
 		if(windowDetail==null){
 			windowDetail=new Window("Detail Konsumsi Barang"){
 				{
 					center();
 					setWidth("500px");
 					
 				}
 			};
 		}
 		windowDetail.setContent(layoutDetailConsumption);
 		this.getUI().addWindow(windowDetail);
 
 	}
 	private void setLabelData(GoodsConsumption consumption){
 		labelGoods.setValue(consumption.getGoods().getName());
 		labelQuantity.setValue(consumption.getQuantity()+ " "+consumption.getGoods().getUnit());
 		labelConsumptionDate.setValue(date.dateToText(consumption.getConsumptionDate(), true));
 		labelInformation.setValue(consumption.getInformation());
 		String ward=function.getSetting().getWard().get(consumption.getWard());
 		if(ward==null){
 			ward=consumption.getWard();
 		}
 		labelWard.setValue(ward);
 
 	}
 	private Label labelGoodsReception; 
 	private Label labelSupplierName;
 	private Label labelQuantityReception;
 	private Label labelReceptionDate;
 	private Label labelInformationReception;	
 	private Label labelExpiredDate;
 	private GridLayout layoutDetailReception;
 
 	@Override
 	public void showDetailReception(GoodsReception reception) {
 		if(layoutDetailReception==null){
 			layoutDetailReception=new GridLayout(2,6){
 				{
 					setSpacing(true);
 					setMargin(true);
 					setWidth("450px");
 					addComponent(new Label("Nama Barang"), 0, 0);
 					addComponent(new Label("Nama Distributor"), 0,1);
 					addComponent(new Label("Jumlah"), 0, 2);
 					addComponent(new Label("Tanggal Penerimaan"), 0,3);
 					addComponent(new Label("Keterangan"), 0,4);
 					addComponent(new Label("Tanggal Kadaluarsa"), 0,5);
 				}
 			};
 			 labelGoodsReception=new Label(); 
 			 labelSupplierName=new Label();
 			 labelQuantityReception=new Label();
 			 labelReceptionDate=new Label();
 			 labelInformationReception=new Label();	
 			 labelExpiredDate=new Label();			
 			layoutDetailReception.addComponent(labelGoodsReception, 1,0);
 			layoutDetailReception.addComponent(labelSupplierName, 1,1);
 			layoutDetailReception.addComponent(labelQuantityReception, 1,2);
 			layoutDetailReception.addComponent(labelReceptionDate, 1,3);
 			layoutDetailReception.addComponent(labelInformationReception, 1,4);
 			layoutDetailReception.addComponent(labelExpiredDate, 1, 5);
 		}
 		setLabelDataReception(reception);
 		if(windowDetail==null){
 			windowDetail=new Window("Detail Penerimaan Barang"){
 				{
 					center();
 					setWidth("500px");
 				}
 			};
 		}
 		windowDetail.setContent(layoutDetailReception);
 		this.getUI().addWindow(windowDetail);
 
 
 	}
 	private void setLabelDataReception(GoodsReception reception){
 		 labelGoodsReception.setValue(reception.getInvoiceItem().getPurchaseOrderItem().getSupplierGoods().getGoods().getName()); 
 		 labelSupplierName.setValue(reception.getInvoiceItem().getPurchaseOrderItem().getSupplierGoods().getSupplier().getSupplierName());
 		 labelQuantityReception.setValue(reception.getQuantityReceived() + " "+reception.getInvoiceItem().getPurchaseOrderItem().getSupplierGoods().getGoods().getUnit());
 		 labelReceptionDate.setValue(date.dateToText(reception.getDate(), true));
 		 labelInformationReception.setValue(reception.getInformation());	
 		 labelExpiredDate.setValue(date.dateToText(reception.getDate(), true));			
 
 	}
 	@Override
 	public String getSelectedGoods() {
 		return (String) selectGoods.getValue();
 	}
 
 	public Date getSelectEndDate() {
 		return selectEndDate.getValue();
 	}
 	public Date getSelectStartDate() {
 		return selectStartDate.getValue();
 	}
 	@Override
 	public void setComboGoodsData(Map<String, String> data) {	
 		String selectValue="";
 		int i=0;
         for (Map.Entry<String, String> entry : data.entrySet()) {
         	selectGoods.addItem(entry.getKey());
         	if(i==0){
         		selectValue=entry.getKey();
         	}
         	selectGoods.setItemCaption(entry.getKey(), entry.getValue());
         	i++;
         }
        	selectGoods.setValue(selectValue);
 	}
 }
