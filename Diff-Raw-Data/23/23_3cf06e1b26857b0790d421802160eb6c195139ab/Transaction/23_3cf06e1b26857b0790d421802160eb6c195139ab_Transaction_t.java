 package com.feefighers.model;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.XStreamer;
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.io.xml.StaxDriver;
 
 @XStreamAlias("transaction")
 public class Transaction implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 	
 	public enum TransactionType {
 		Purchase, authorize
 	}
 	
 	@XStreamAlias("reference_id") 
 	private String referenceId; // response
 	
 	@XStreamAlias("transaction_token")
 	private String transactionToken; // response
 	
 	@XStreamAlias("created_at")
 	private Date createdAt; // response	
 
 	@XStreamAlias("descriptor")
 	private String descriptor; // request
 	
 	@XStreamAlias("custom")
 	private String custom; // request	
 
 	@XStreamAlias("billing_reference")
 	private String billingReference; // request
 	
 	@XStreamAlias("customer_reference")
 	private String customerReference; // request
 
 	@XStreamAlias("transaction_type")
 	private TransactionType type; // request
 		
 	@XStreamAlias("amount")
 	private String amount; // request
 
 	@XStreamAlias("currency_code")
 	private String currencyCode; // request		
 	
 	@XStreamAlias("processor_token")
	private String processorToken; // response
 		
 	@XStreamAlias("processor_response")
 	private ProcessorResponse processorResponse; // response
 
 	@XStreamAlias("payment_method")
 	private PaymentMethod paymentMethod; // response
 	
 	@XStreamAlias("payment_method_token")
 	private String paymentMethodToken; // request	
 	
 	static {
 		XmlMarshaller.registerModelClass(Transaction.class);
 	}
 	
 	public String toXml() {
 		return XmlMarshaller.toXml(this);
 	}
 	
 	public static Transaction fromXml(String xml) {
 		return (Transaction) XmlMarshaller.fromXml(xml);
 	}
 
 	public Transaction(TransactionType type) {
 		this.type = type;
 	}
 	
 	public String getAmount() {
 		return amount;
 	}
 
 	public void setAmount(String amount) {
 		this.amount = amount;
 	}
 
 	public TransactionType getType() {
 		return type;
 	}	
 
 	public String getPaymentMethodToken() {
 		return paymentMethodToken;
 	}
 
 	public void setPaymentMethodToken(String paymentMethodToken) {
 		this.paymentMethodToken = paymentMethodToken;
 	}
 
 	public String getCurrencyCode() {
 		return currencyCode;
 	}
 
 	public void setCurrencyCode(String currencyCode) {
 		this.currencyCode = currencyCode;
 	}
 
 	public String getDescriptor() {
 		return descriptor;
 	}
 
 	public void setDescriptor(String descriptor) {
 		this.descriptor = descriptor;
 	}
 
 	public String getCustom() {
 		return custom;
 	}
 
 	public void setCustom(String custom) {
 		this.custom = custom;
 	}
 
 	public String getCustomerReference() {
 		return customerReference;
 	}
 
 	public void setCustomerReference(String customerReference) {
 		this.customerReference = customerReference;
 	}
 
 	public String getBillingReference() {
 		return billingReference;
 	}
 
 	public void setBillingReference(String billingReference) {
 		this.billingReference = billingReference;
 	}
 
 	public String getReferenceId() {
 		return referenceId;
 	}
 
 	public void setReferenceId(String referenceId) {
 		this.referenceId = referenceId;
 	}
 
 	public String getTransactionToken() {
 		return transactionToken;
 	}
 
 	public void setTransactionToken(String transactionToken) {
 		this.transactionToken = transactionToken;
 	}
 
 	public Date getCreatedAt() {
 		return createdAt;
 	}
 
 	public void setCreatedAt(Date createdAt) {
 		this.createdAt = createdAt;
 	}
 
 	public PaymentMethod getPaymentMethod() {
 		return paymentMethod;
 	}
 
 	public void setPaymentMethod(PaymentMethod paymentMethod) {
 		this.paymentMethod = paymentMethod;
 	}
 
 	public void setType(TransactionType type) {
 		this.type = type;
 	}
 
	public ProcessorResponse getProcessorResponse() {
		return processorResponse;
	}
	
	public String getProcessorToken() {
		return processorToken;
	}
 	
	public void setProcessorToken(String processorToken) {
		this.processorToken = processorToken;
	}
 }
