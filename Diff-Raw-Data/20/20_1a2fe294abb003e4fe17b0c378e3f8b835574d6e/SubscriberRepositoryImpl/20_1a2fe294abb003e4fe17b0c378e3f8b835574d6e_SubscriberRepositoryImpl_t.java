 package com.raitongorganicsfarm.app.mb.repository;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.data.mongodb.core.query.Criteria;
 import org.springframework.data.mongodb.core.query.Order;
 import org.springframework.data.mongodb.core.query.Query;
 
 import com.raitongorganicsfarm.app.mb.entity.Subscriber;
 
 public class SubscriberRepositoryImpl implements SubscriberRepositoryCustom {
 
 	private MongoTemplate mongoTemplate;
 
 	@Override
 	public void removeByCustomerNo(String customerNo) {
 		Query query = Query.query(Criteria.where("customerNo").is(customerNo));
 		mongoTemplate.remove(query, Subscriber.class);
 	}
 
 	@Override
 	public List<Subscriber> findAllOrderByCustomerNo() {
 		Query query = new Query();
 		query.sort().on("customerNo", Order.ASCENDING);
 		return mongoTemplate.find(query, Subscriber.class);
 	}
 
 	@Override
 	public String nextCustomerNumber() {
 		Query query = SubscriberQueryHelper.lastestSubscriberQuery();
		List<Subscriber> list = mongoTemplate.find(query, Subscriber.class);
 		long counter = 1;
		if(!list.isEmpty()) {
			Subscriber subscriber = list.get(0);
 			String c = subscriber.getCustomerNo();
 			counter = Long.parseLong(c.substring(3)) + 1l;
 		}
 		String customerNo = String.format("CSA%1$04d", counter);
 		return customerNo;
 	}
 
 	@Autowired
 	public void setMongoTemplate(MongoTemplate mongoTemplate) {
 		this.mongoTemplate = mongoTemplate;
 	}
 
 	@Override
 	public synchronized Subscriber generateCustomerNoAndSave(Subscriber subscriber) {
 		String customerNo =  nextCustomerNumber();
 		subscriber.setCustomerNo(customerNo);
 		mongoTemplate.save(subscriber);
 		return subscriber;
 	}
 
 }
