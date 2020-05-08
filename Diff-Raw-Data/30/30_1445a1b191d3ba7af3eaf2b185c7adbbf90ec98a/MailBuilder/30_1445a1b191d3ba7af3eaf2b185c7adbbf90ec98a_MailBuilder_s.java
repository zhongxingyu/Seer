 package com.kelkoo.agile.solution4;
 
 import com.kelkoo.agile.solution4.collaborators.Product;
 
 public class MailBuilder implements CartVisitor {
 
 	private StringBuilder cartContent;
 	private StringBuilder productsContent;
	private String mailContent = "";
 	
	public String getMailContent() {
		return mailContent;
 	}
 
 	public void beforeVisit() {
 	    cartContent = new StringBuilder();
 		productsContent = new StringBuilder();
 	}
 	
 	public void afterVisit() {
		mailContent = cartContent.append(productsContent).toString(); 
 	}
 	
 	public void visit(Cart cart) {
 		buildMailHeader(cart);
 	}
 
 	public void visit(Product product) {
 		addProductToMail(product);
 	}
 
 	private void buildMailHeader(Cart cart) {
 		cartContent.append("Bonjour,\n")
 		       .append("Votre panier composé le ")
 			   .append(cart.getCreationDate())
 			   .append(" comporte les éléments suivants :\n");
 	}
 
 	private void addProductToMail(Product product) {
 		productsContent.append("- ")
 			.append(product.getName())
 			.append(" au prix de ")
 			.append(product.getPrice()).append("\n");
 	}
 
 }
