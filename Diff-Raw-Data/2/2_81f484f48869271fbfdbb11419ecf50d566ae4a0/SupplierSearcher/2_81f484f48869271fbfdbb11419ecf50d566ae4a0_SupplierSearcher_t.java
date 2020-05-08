 package com.iappsam.search;
 
 import com.iappsam.Supplier;
 
 public class SupplierSearcher extends AbstractSearcher<Supplier> {
 
 	public SupplierSearcher() {
		super(Supplier.class, "name", "address", "contactPerson.person.name");
 	}
 }
