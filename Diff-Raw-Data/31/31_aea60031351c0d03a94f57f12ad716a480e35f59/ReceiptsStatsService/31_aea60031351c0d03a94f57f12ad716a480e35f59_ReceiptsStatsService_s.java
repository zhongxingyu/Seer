 /*
  * Copyright 2012 C24 Technologies.
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), 
  * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
  * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL C24 TECHNOLOGIES BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
  * SOFTWARE.
  * 
  */
 package biz.c24.retaildemo.service;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.context.Lifecycle;
 import org.springframework.data.gemfire.GemfireTemplate;
 
 import biz.c24.retaildemo.model.Product;
 import biz.c24.retaildemo.model.Receipt;
 
 import com.gemstone.gemfire.cache.query.SelectResults;
 import com.gemstone.gemfire.cache.query.Struct;
 
 /*
  * Simple service wrapper
  */
 public class ReceiptsStatsService {
 	
	@Qualifier("gemfireReceiptTemplate")
 	@Autowired(required=true)
 	private GemfireTemplate template;
 	
	/* Disabled until Spring Data Gemfire upgraded to Spring Data Commons 1.3. See gemfire-context.xml
 	@Autowired
 	private ReceiptRepository receiptRepository;
 	
 	public ReceiptRepository getReceiptRepository() {
 		return receiptRepository;
 	}
 
 	public void setReceiptRepository(ReceiptRepository receiptRepository) {
 		this.receiptRepository = receiptRepository;
 	}
 	*/
 	
	@Autowired(required=true)
	@Qualifier("liveReceiptsInboundAdpater")
	private Lifecycle queueInboundAdpater;
 	
 	public void startLiveReceiptFlow() {
		queueInboundAdpater.start();
 	}
 	
 	public void stopLiveReceiptFlow() {
		queueInboundAdpater.stop();
 	}
 	
 	public int getNumReceipts() {
 		SelectResults<Receipt> res = template.find("select * from /receipts");
 		return res.size();
 	}
 	
 	
 	public int getNumBasketItems() {
 		SelectResults<Object> res = template.find("select * from /receipts, items");
 		return res.size();
 	}
 	
 	public Collection<Receipt> getReceipts() {
 		return template.find("select * from /receipts");
 	}
 	
 	public Collection<Receipt> getReceipts(Date startDateTime, Date endDateTime) {
 		return template.find("select * from /receipts where checkoutTime >= $1 and checkoutTime < $2", startDateTime, endDateTime);
 	}
 	
 	public Collection<Receipt> getReceipts(Date startDateTime, Date endDateTime, String customerProfile) {
 		//return template.find("select * from /receipts where checkoutTime >= $1 and checkoutTime < $2 and customerId in (select c.id from /customers c where c.profile = $3)", startDateTime, endDateTime, customerProfile);
 		Collection<String> customerIds = template.find("select id from /customers where profile = $1", customerProfile);
 		Collection<Receipt> retVal =  getReceipts(startDateTime, endDateTime);
 		Iterator<Receipt> itr = retVal.iterator();
 		while(itr.hasNext()) {
 			Receipt curr = itr.next();
 			if(!customerIds.contains(curr.getCustomerId())) {
 				itr.remove();
 			}
 		}
 		
 		return retVal;
 
 	}
 	
 	public Collection<Receipt> getReceipts(Date startDateTime, Date endDateTime, int productId, String customerProfile) {
 		//return template.find("select distinct r from /receipts r, r.items i where r.checkoutTime >= $1 and r.checkoutTime < $2 and r.customerId in (select c.id from /customers c where c.profile = $3) and i.productId = $4", startDateTime, endDateTime, customerProfile, productId);
 		Collection<String> customerIds = template.find("select id from /customers where profile = $1", customerProfile);
 		Collection<Receipt> retVal =  getReceipts(startDateTime, endDateTime, productId);
 		Iterator<Receipt> itr = retVal.iterator();
 		while(itr.hasNext()) {
 			Receipt curr = itr.next();
 			if(!customerIds.contains(curr.getCustomerId())) {
 				itr.remove();
 			}
 		}
 		
 		return retVal;
 	}
 
 	/*
 	 * Find all receipts between the specified date ranges where the basket contained the specified product
 	 */
 	public Collection<Receipt> getReceipts(Date startDateTime, Date endDateTime, int productId) {
 		return template.find("select distinct r from /receipts r, items i where r.checkoutTime >= $1 and r.checkoutTime < $2 and i.productId = $3", startDateTime, endDateTime, productId);
 		//return receiptRepository.findByCheckoutTimeBetweenAndItemsProductId(startDateTime, endDateTime, productId);
 	}
 	
 	public Collection<String> getCustomerProfiles() {
 		return template.find("select distinct profile from /customers");
 	}
 	
 	public Collection<Product> getProducts() {
 		return template.find("select * from /products");
 	}
 	
 	public Collection<Struct> getItemsByProfile(Date startDateTime, Date endDateTime, String customerProfile) {
 		return template.find("select r.checkoutTime, i.productId, i.price, i.quantity from /receipts r, items i where r.checkoutTime >= $1 and r.checkoutTime < $2 and r.customerId in (select c.id from /customers c where c.profile = $3)", startDateTime, endDateTime, customerProfile);
 	}
 	
 	public Collection<Struct> getItems(Date startDateTime, Date endDateTime, String category, String customerProfile) {
 		return template.find("select r.checkoutTime, i.productId, i.price, i.quantity from /receipts r, items i where r.checkoutTime >= $1 and r.checkoutTime < $2 and i.productId in (select distinct p.id from /products p where p.category = $3) and r.customerId in (select c.id from /customers c where c.profile = $4)", startDateTime, endDateTime, category, customerProfile);
 	}
 	
 	public Collection<Struct> getItems(Date startDateTime, Date endDateTime, int productId, String customerProfile) {
 		return template.find("select r.checkoutTime, i.productId, i.price, i.quantity from /receipts r, items i where r.checkoutTime >= $1 and r.checkoutTime < $2 and i.productId = $3 and r.customerId in (select c.id from /customers c where c.profile = $3)", startDateTime, endDateTime, productId, customerProfile);
 	}
 
 	public Collection<Struct> getItems(Date startDateTime, Date endDateTime) {
 		return template.find("select r.checkoutTime, i.productId, i.price, i.quantity from /receipts r, items i where r.checkoutTime >= $1 and r.checkoutTime < $2", startDateTime, endDateTime);
 	}
 	
 	public Collection<Struct> getItemsByCategory(Date startDateTime, Date endDateTime, String category) {
 		return template.find("select r.checkoutTime, i.productId, i.price, i.quantity from /receipts r, items i where r.checkoutTime >= $1 and r.checkoutTime < $2 and i.productId in (select distinct p.id from /products p where p.category = $3)", startDateTime, endDateTime, category);
 	}
 	
 	public Collection<Struct> getItems(Date startDateTime, Date endDateTime, int productId) {
 		return template.find("select r.checkoutTime, i.productId, i.price, i.quantity from /receipts r, items i where r.checkoutTime >= $1 and r.checkoutTime < $2 and i.productId = $3", startDateTime, endDateTime, productId);
 	}
 
 	public GemfireTemplate getTemplate() {
 		return template;
 	}
 
 	public void setTemplate(GemfireTemplate template) {
 		this.template = template;
 	}
 	
	public Lifecycle getQueueInboundAdpater() {
		return queueInboundAdpater;
 	}
 

	public void setQueueInboundAdpater(Lifecycle queueInboundAdpater) {
		this.queueInboundAdpater = queueInboundAdpater;
 	}
 
 
 	public Object findUnique(String query) {
 		return template.findUnique(query, null);
 	}
 	
 	public SelectResults<Object> find(String query) {
 		SelectResults<Object> res = template.find(query, 1);
 		return res;
 	}
 
 }
