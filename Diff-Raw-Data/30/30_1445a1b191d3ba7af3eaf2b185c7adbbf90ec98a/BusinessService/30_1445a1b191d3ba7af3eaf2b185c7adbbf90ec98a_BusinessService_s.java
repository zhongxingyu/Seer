 package com.kelkoo.agile.solution4;
 
 import com.kelkoo.agile.solution4.Cart;
 import com.kelkoo.agile.solution4.collaborators.Client;
 import com.kelkoo.agile.solution4.collaborators.Product;
 
 public class BusinessService {
 
 	private MailBuilder mailBuilder;
 	private CartRepository cartRepository;
 
 	public Cart getCartForClient(Client client) throws Exception {
 		return cartRepository.find(client);
 	}
 	
 	public void addProduct(Client client, Product product) throws Exception {
 		Cart cart = getCartForClient(client);
 		cart.addProduct(product);
 		cartRepository.save(cart);
 	}
 	
 	public void sendMailTo(Client client) throws Exception {
 		Cart cart = getCartForClient(client);
		cart.accept(mailBuilder);
		String content = mailBuilder.getMailContent();
 		// mailer.sentMailTo(Client client, content);
 	}
 	
 	public void pay(Client client) throws Exception {
 		Cart cart = getCartForClient(client);
 		// payment.pay(client, cart);
 	}
 	
 	// other services
     // ...
 }
