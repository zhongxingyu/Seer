 package com.iappsam.search;
 
 import com.iappsam.Supplier;
 
 public class SupplierSearcher extends AbstractSearcher<Supplier> {
 
 	public SupplierSearcher() {
		super(Supplier.class, "person.name", "address", "contactPerson.person.name");
 	}
 }
