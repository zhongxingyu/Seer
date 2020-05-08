 package com.kelkoo.agile.solution4;
 
 import com.kelkoo.agile.solution4.collaborators.Product;
 
 public interface CartVisitor {
 
	void visit(Cart cart);
 
	void visit(Product product);
 
 	void afterVisit();
 
 	void beforeVisit();
 
 }
