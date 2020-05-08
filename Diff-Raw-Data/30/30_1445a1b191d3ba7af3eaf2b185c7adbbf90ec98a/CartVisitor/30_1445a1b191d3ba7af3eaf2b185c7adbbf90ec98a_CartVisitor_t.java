 package com.kelkoo.agile.solution4;
 
 import com.kelkoo.agile.solution4.collaborators.Product;
 
 public interface CartVisitor {
 
	void visit(Cart cart); // alternative : decompose to fields => visit(Date creationDate, Client client)
 
	void visit(Product product); // alternative : decompose to fields => visit(String name, float price);
 
 	void afterVisit();
 
 	void beforeVisit();
 
 }

