 package de.switajski.priebes.flexibleorders.domain;
 
 import java.util.Date;
 
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.ManyToOne;
 import javax.validation.constraints.NotNull;
 
 import de.switajski.priebes.flexibleorders.web.entities.ReportItem;
 
 @Inheritance(strategy=InheritanceType.SINGLE_TABLE)
 @Entity
 public class HandlingEvent extends GenericEntity implements Comparable<HandlingEvent>{
 
 	@NotNull
 	private Integer quantity;
 	
 	@ManyToOne(fetch = FetchType.EAGER)
 	private Report report;
 	
 	@NotNull
 	private HandlingEventType type;
 	
 	@NotNull
 	@ManyToOne
 	private OrderItem orderItem;
 
 	protected HandlingEvent() {}
 
 	//TODO: refactor to constructor with minimal attributes
 	public HandlingEvent(Report report, HandlingEventType handlingEventType, OrderItem item, 
 			Integer quantity, Date created) {
 		if (report == null || handlingEventType == null || item == null || quantity == null) 
 			throw new IllegalArgumentException();
 		this.report = report;
 		this.type = handlingEventType;
 		this.orderItem = item;
 		this.quantity = quantity;
 		setCreated(created);
 		
 		if (!orderItem.getDeliveryHistory().contains(this))
 			orderItem.addHandlingEvent(this);
 		if (report.getEvents().contains(this)) return ;
 		report.addEvent(this);
 	}
 	
 	public String toString(){
 		return (this.getId()+": Report:"+getReport().getDocumentNumber())
 				+" Type: " + getType().toString()
 				+" Quantity: "+getQuantity();
 	}
 	
 	public int getQuantity() {
 		return quantity;
 	}
 
 	public void setQuantity(int quantity) {
 		this.quantity = quantity;
 	}
 
 	public Report getReport() {
 		return report;
 	}
 	
 	public DeliveryNotes getInvoice(){
 		if (getReport() instanceof DeliveryNotes)
 			return (DeliveryNotes) getReport();
 		return null;
 	}
 	
 	public Receipt getReceipt(){
 		if (getReport() instanceof Receipt)
 			return (Receipt) getReport();
 		return null;
 	}
 	
 	public ConfirmationReport getOrderConfirmation(){
 		if (getReport() instanceof ConfirmationReport)
 			return (ConfirmationReport) getReport();
 		return null;
 	}
 	
 	public void setReport(Report report) {
 		if (report.getEvents().contains(this)) return;
 		this.report = report;
 		report.addEvent(this);
 	}
 
 	public OrderItem getOrderItem() {
 		return orderItem;
 	}
 
 	public void setOrderItem(OrderItem item) {
 		this.orderItem = item;
 		
 		// handle bidirectional relationship:
 		if (item.getDeliveryHistory().contains(this)) return;
 		item.addHandlingEvent(this);
 	}
 
 	public HandlingEventType getType() {
 		return type;
 	}
 
 	public void setType(HandlingEventType type) {
 		this.type = type;
 	}
 
 	@Override
 	public int compareTo(HandlingEvent o) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	
 	public ReportItem toReportItem(){
 		//TODO: enhance mapping by a mapping framework
 		ReportItem item = new ReportItem();
 		item.setDocumentNumber(getReport().getDocumentNumber());
 		
 		if (this.getReport() instanceof ConfirmationReport){
 			item.setOrderConfirmationNumber(getReport().getDocumentNumber());
 		}
 		if (this.getReport() instanceof Invoice){
 			Invoice invoice = (Invoice) this.getReport();
 			item.setInvoiceNumber(getReport().getDocumentNumber());
 			item.setDeliveryNotesNumber(getReport().getDocumentNumber());
 			item.setPaymentConditions(invoice.getPaymentConditions());
 		}
 		if (this.getReport() instanceof DeliveryNotes){
 			DeliveryNotes deliveryNotes = (DeliveryNotes) this.getReport();
 			item.setDeliveryNotesNumber(getReport().getDocumentNumber());
 			item.setTrackNumber(deliveryNotes.getTrackNumber());
 			item.setPackageNumber(deliveryNotes.getPackageNumber());
 		}
 		if (this.getReport() instanceof Receipt){
 			item.setReceiptNumber(getReport().getDocumentNumber());
 		}
 		item.setId(getId());
 		item.setCreated(getCreated());
 		item.setCustomer(getOrderItem().getOrder().getCustomer().getId());
 		item.setId(getId());
 		item.setOrderNumber(getOrderItem().getOrder().getOrderNumber());
 		if (getOrderItem().getNegotiatedPriceNet() != null)
 			item.setPriceNet(getOrderItem().getNegotiatedPriceNet().getValue());
 		item.setProduct(getOrderItem().getProduct().getProductNumber());
 		item.setProductName(getOrderItem().getProduct().getName());
 		item.setStatus(provideStatus());
 		item.setCustomerNumber(getOrderItem().getOrder().getCustomer().getCustomerNumber());
 		item.setDocumentNumber(this.getReport().getDocumentNumber());
 		item.setQuantity(getQuantity());
 		item.setQuantityLeft(getOrderItem().getOrderedQuantity() - quantity);
 		return item;
 	}
 
 	private String provideStatus() {
 		String status = "";
 		switch (type){
			case CONFIRM: status = "besttigt"; break;
 			case SHIP: status = "ausgeliefert"; break;
 			case INVOICE: status = "in Rechnung gestellt"; break;
 			case PAID: status = "bezahlt"; break;
 			case FORWARD_TO_THIRD_PARTY: status = "zur Nherei"; break;
 			case CANCEL: status = "storniert"; break;
 		}
 		return status;
 	}
 
 }
