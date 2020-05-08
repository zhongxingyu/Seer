 package de.gzockoll.prototype.camel.encashment.service;
 
 import static org.joda.money.CurrencyUnit.EUR;
 
 import java.util.Collection;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import org.apache.camel.CamelContext;
 import org.apache.camel.Endpoint;
 import org.apache.camel.Exchange;
 import org.apache.camel.Message;
 import org.apache.camel.Producer;
 import org.apache.commons.lang.Validate;
 import org.joda.money.Money;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import de.gzockoll.prototype.camel.encashment.entity.AbstractEntity;
 import de.gzockoll.prototype.camel.encashment.entity.Customer;
 import de.gzockoll.prototype.camel.encashment.entity.EncashmentEntry;
 import de.gzockoll.prototype.camel.encashment.entity.EncashmentStatus;
 import de.gzockoll.prototype.camel.encashment.entity.EncashmentType;
 import de.gzockoll.prototype.camel.encashment.entity.Merchant;
 
 @SuppressWarnings("javadoc")
 @Service
 @Transactional
 public class EncashmentService {
 	Logger logger = LoggerFactory.getLogger(EncashmentService.class);
 	@PersistenceContext
 	private EntityManager em;
 	@Autowired
 	private CamelContext context;
 
 	public AbstractEntity processEncashmentOrder(Merchant m, Customer c,
 			String text, Money amount) {
 		AbstractEntity entry = new EncashmentEntry(m, c, text, amount);
 		em.persist(em);
 		return entry;
 	}
 
 	public void processCredit(Merchant m, Customer c, String text, Money amount) {
 	}
 
 	public void processPayment(Merchant m, Customer c, String text, Money amount) {
 
 	}
 
 	public void onError(byte[] body, Exchange exchange) {
 		EncashmentEntry entry = em.find(EncashmentEntry.class,
 				exchange.getProperty("encashmentId"));
 		entry.deliveryError();
 		em.persist(entry);
 	}
 
 	public void onSuccess(byte[] body, Exchange exchange) {
 		EncashmentEntry entry = em.find(EncashmentEntry.class,
 				exchange.getProperty("encashmentId"));
 		entry.successfulDelivered();
 		em.persist(entry);
 	}
 
 	public void startProcessing() {
 		logger.debug("Processing started!");
 		Validate.notNull(em);
 		Query query = em
				.createQuery("SELECT e FROM EncashmentEntry e WHERE e.status != 'DELIVERED'");
 		Collection<EncashmentEntry> entries = query.getResultList();
 		for (EncashmentEntry e : entries) {
 			if (e.getStatus() != EncashmentStatus.DELIVERED)
 				deliver(e);
 		}
 	}
 
 	private void deliver(AbstractEntity e) {
 		try {
 			// create an exchange with a normal body and attachment to be
 			// produced
 			// as email
 			Endpoint endpoint = context.getEndpoint("direct:input");
 
 			// create the exchange with the mail message that is multipart with
 			// a
 			// file and a Hello World text/plain message.
 			Exchange exchange = endpoint.createExchange();
 			exchange.setProperty("TYPE", EncashmentType.ORDER.name());
 			exchange.setProperty("encashmentId", e.getId());
 			Message in = exchange.getIn();
 			in.setBody(e);
 
 			// create a producer that can produce the exchange (= send the mail)
 			Producer producer = endpoint.createProducer();
 			// start the producer
 			producer.start();
 			// and let it go (processes the exchange by sending the email)
 			producer.process(exchange);
 		} catch (Exception ex) {
 			logger.error("Delivery failed:", e);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private Collection<EncashmentEntry> findAll() {
 		Validate.notNull(em);
 		Query query = em.createQuery("SELECT e FROM EncashmentEntry e");
 		return query.getResultList();
 
 	}
 
 	public void populateDatabase() {
 		Merchant m = new Merchant("Zalando");
 		em.persist(m);
 		Customer c = new Customer("Vera Müller");
 		em.persist(c);
 		AbstractEntity entry = new EncashmentEntry(m, c, "Schuhe",
 				Money.ofMajor(EUR, 10));
 		em.persist(entry);
 	}
 }
