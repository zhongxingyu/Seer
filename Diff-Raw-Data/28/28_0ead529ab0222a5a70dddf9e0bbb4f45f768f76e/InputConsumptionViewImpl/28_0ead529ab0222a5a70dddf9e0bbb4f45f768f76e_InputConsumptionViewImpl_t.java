 package com.binar.core.inventoryManagement.consumptionList.inputConsumption;
 
 import java.util.Date;
 import java.util.Map;
 
 import com.binar.core.requirementPlanning.inputRequierementPlanning.inputEditForm.InputFormView;
 import com.binar.core.requirementPlanning.inputRequierementPlanning.inputEditForm.InputFormView.InputFormListener;
 import com.binar.entity.GoodsConsumption;
 import com.binar.entity.ReqPlanning;
 import com.binar.generalFunction.GeneralFunction;
 import com.binar.generalFunction.StockFunction;
 import com.binar.generalFunction.TextManipulator;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.shared.ui.label.ContentMode;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.ComboBox;
 import com.vaadin.ui.DateField;
 import com.vaadin.ui.GridLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.TextArea;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.FormLayout;
 
 /*
  * Isian yang dibutuhkan : 
  * -> Select Obat
  * -> jumlah pengeluaran
  * -> pilih hari
  * -> label satuan
  */
 
 public class InputConsumptionViewImpl extends FormLayout implements InputConsumptionView, 
 Button.ClickListener, ValueChangeListener{
 
 	private ComboBox inputGoodsSelect;
 	private TextField inputGoodsQuantity;
 	private DateField inputConsumptionDate;
 	private TextArea information;
 	private ComboBox inputWardSelect;
 	private Label labelSatuan;
 	private Button buttonReset;
 	private Button buttonSubmit;
 	private Button buttonCancel;
 	private TextManipulator text;
 	private Label labelErrorQuantity; //untuk error realtime
 	private Label labelGeneralError; //error saat tombol submit ditekan
 	private Label labelErrorDate; //jika tanggal tidak valid
 	private InputConsumptionListener listener;
 	
 	
 	GeneralFunction function;
 	StockFunction stock;
 	
 	public InputConsumptionViewImpl(GeneralFunction function){
 		this.function=function;
 		this.stock=function.getStock();
 	}
 
 	@Override
 	public void init() {
 		text = new TextManipulator();
 		final String WIDTH="350px";
 		labelErrorQuantity=new Label(){
 			{
 				setVisible(false);
 				addStyleName("form-error");
 				setContentMode(ContentMode.HTML);
 			}
 		};
 		labelErrorDate=new Label(){
 			{
 				setVisible(false);
 				addStyleName("form-error");
 				setContentMode(ContentMode.HTML);
 			}
 		};
 		labelGeneralError=new Label(){
 			{
 				setVisible(false);
 				addStyleName("form-error");
 				setContentMode(ContentMode.HTML);
 			}
 		};
 		inputGoodsQuantity=new TextField("Jumlah Pengeluaran"){
 			{
 				setWidth(function.FORM_WIDTH);
 				setImmediate(true);
 			}
 		};
 		inputGoodsQuantity.addValueChangeListener(this);
 		
 		inputGoodsSelect=new ComboBox("Nama Barang"){
 			{
 				setImmediate(true);
 				setWidth(function.FORM_WIDTH);
 			}
 		};
 		inputGoodsSelect.addValueChangeListener(this);
 		
 		inputConsumptionDate = new DateField("Tanggal Pengeluaran"){
 			{
 				setWidth(function.FORM_WIDTH);
 				setImmediate(true);
 			}
 		};
 		inputConsumptionDate.addValueChangeListener(this);
 		
 		inputWardSelect = new ComboBox("Instalasi"){
 			{
 				setWidth(function.FORM_WIDTH);
 				setImmediate(true);
 			}
 		};
 		inputWardSelect.addValueChangeListener(this);
 		
 		information = new TextArea("informasi");
 		information.setMaxLength(function.MAX_TEXTAREA_LENGTH);
 
 		labelSatuan =new Label("Satuan");
 		
 		buttonReset=new Button("Reset");
 		buttonReset.addClickListener(this);
 		
 		buttonSubmit=new Button("Submit");
 		buttonSubmit.addClickListener(this);
 		buttonSubmit.addStyleName("primary");
 		buttonCancel =new Button("Batal");
 		buttonCancel.addClickListener(this);
 		construct();
 		
 		//kurang tanggal
 	}
 
 	private void construct() {
 		setMargin(true);
 		this.setSpacing(true);
 
 		this.addComponent(inputConsumptionDate);
 		this.addComponent(labelErrorDate);
 		this.addComponent(inputGoodsSelect);
 		this.addComponent(inputGoodsQuantity);
 		this.addComponent(labelErrorQuantity);
 		this.addComponent(labelSatuan);
 		this.addComponent(inputWardSelect);
 		this.addComponent(information);
		this.addComponent(labelGeneralError);
 		this.addComponent(new GridLayout(3, 1){
 			{
 				addComponent(buttonSubmit, 0,0);
 				addComponent(buttonReset, 1, 0);
 				addComponent(buttonCancel, 2, 0);
 				this.setMargin(true);
 				this.setSpacing(true);
 			}
 		});
 	}
 	
 	public void setSelectedMonth(Date selectedDate){
 		inputConsumptionDate.setValue(selectedDate);
 	}
 
 	@Override
 	public void valueChange(ValueChangeEvent event) {
 		
 		if(event.getProperty()==inputGoodsQuantity){
 			listener.realTimeValidator("inputGoodsQuantity");
 			
 		}else if(event.getProperty()==inputGoodsSelect){
 			listener.realTimeValidator("inputGoodsSelect");			
 		}
 		
 		else if(event.getProperty()==inputConsumptionDate){
 			listener.realTimeValidator("inputConsumptionDate");			
 		}
 		
 		else if(event.getProperty()==inputWardSelect){
 			listener.realTimeValidator("inputWardSelect");			
 		}
 	}
 
 	@Override
 	public void buttonClick(ClickEvent event) {
 		if(event.getSource()==buttonReset){
 			listener.buttonClick("reset");
 		}else if(event.getSource()==buttonSubmit){
 			listener.buttonClick("submit");
 		}else if(event.getSource()==buttonCancel){
 			listener.buttonClick("cancel");
 		}
 	}
 	
 	@Override
 	public void resetForm() {
 		this.inputGoodsQuantity.setValue("");
 		this.inputWardSelect.setValue("");
 		this.information.setValue("");	
 		inputGoodsQuantity.setEnabled(true);
 		inputConsumptionDate.setEnabled(true);
 
 	}
 	
 	public void setDataSubmit(){
 		buttonSubmit.setCaption("Submit");
 		resetForm();
 	}
 	
 	public void setDataEdit(GoodsConsumption data){
 		
 		inputGoodsQuantity.setValue(data.getQuantity()+"");
 		inputGoodsSelect.setValue(data.getGoods().getIdGoods());
 		inputConsumptionDate.setValue(data.getConsumptionDate());
 		inputWardSelect.setValue(data.getWard());
 		System.out.println("Data instalasi : "+(data.getWard()));
 		information.setValue(data.getInformation());
 		inputConsumptionDate.setEnabled(false);
 		buttonSubmit.setCaption("Simpan Perubahan");
 		if(stock.isAnyNewestItem(data)){
 			inputGoodsQuantity.setEnabled(false);
 		}else{
 			inputGoodsQuantity.setEnabled(true);
 		}
 	}
 
 	@Override
 	public void showError(ErrorLabel label, String content) {
 		switch (label) {
 		case GENERAL:labelGeneralError.setVisible(true);
 					 labelGeneralError.setValue(content);	
 					 break;
 		case QUANTITY:labelErrorQuantity.setVisible(true);
 					  labelErrorQuantity.setValue(content);
 					  break;
 		case ERROR_DATE: labelErrorDate.setVisible(true);
 						labelErrorQuantity.setValue(content);
 						break;
 		default:
 			break;
 		}
 	}
 
 	@Override
 	public void hideError(ErrorLabel label) {
 		switch (label) {
 		case GENERAL:labelGeneralError.setVisible(false);break;
 		case QUANTITY:labelErrorQuantity.setVisible(false);break;
 		case ERROR_DATE:labelErrorDate.setVisible(false);break;
 		default:
 			break;
 		}
 	}
 
 	@Override
 	public void hideAllError() {
 		labelErrorQuantity.setVisible(false);
 		labelGeneralError.setVisible(false);
 		labelErrorDate.setVisible(false);
 	}
 
 	@Override
 	public void setUnit(String text) {
 		labelSatuan.setValue("Satuan : "+text);
 	}
 
 	@Override
 	public void addListener(InputConsumptionPresenter listener) {
 		this.listener=listener;
 	}
 
 	@Override
 	public void setSelectGoodsData(Map<String, String> data) {
 		for (Map.Entry<String, String> entry : data.entrySet()) {
         	inputGoodsSelect.addItem(entry.getKey());
         	inputGoodsSelect.setItemCaption(entry.getKey(), entry.getValue());
         }
 	}
 	
 	@Override
 	public void setSelectWardData(Map<String, String> data) {
 		for (Map.Entry<String, String> entry : data.entrySet()) {
         	inputWardSelect.addItem(entry.getKey());
         	inputWardSelect.setItemCaption(entry.getKey(), entry.getValue());
         }		
 		System.out.println("JUMLAH DATA INSTALASI : " + data.size());
 	}	
 	
 	protected ComboBox getInputGoodsSelect() {		
 		return inputGoodsSelect;
 	}
 
 	protected void setInputGoodsSelect(ComboBox inputGoodsSelect) {
 		this.inputGoodsSelect = inputGoodsSelect;
 	}
 
 	protected TextField getInputGoodsQuantity() {
 		return inputGoodsQuantity;
 	}
 
 	protected void setInputGoodsQuantity(TextField inputGoodsQuantity) {
 		this.inputGoodsQuantity = inputGoodsQuantity;
 	}
 	
 	protected DateField getInputConsumptionDate() {
 		return inputConsumptionDate;
 	}
 
 	protected void setInputConsumptionDate(DateField inputConsumptionDate) {
 		this.inputConsumptionDate = inputConsumptionDate;
 	}
 
 	protected TextArea getInformation() {
 		return information;
 	}
 
 	protected void setInformation(TextArea information) {
 		this.information = information;
 	}
 	
 	protected ComboBox getWard() {
 		return inputWardSelect;
 	}
 
 	protected void setWard(ComboBox ward) {
 		inputWardSelect = ward;
 	}
 
 	@Override
 	public void setInputEditView(boolean isEdit) {
 		if(isEdit){
 	//		inputConsumptionDate.setEnabled(false);
 			inputGoodsSelect.setEnabled(false);
 		}else{
 		//	inputConsumptionDate.setEnabled(true);
 			inputGoodsSelect.setEnabled(true);
 
 		}
 	}
 }
