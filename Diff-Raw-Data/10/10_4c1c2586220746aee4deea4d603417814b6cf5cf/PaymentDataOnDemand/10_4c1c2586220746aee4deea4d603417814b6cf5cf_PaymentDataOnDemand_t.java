 package com.github.msl521.met.domain;
 
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.roo.addon.dod.RooDataOnDemand;
 
 @RooDataOnDemand(entity = Payment.class)
 public class PaymentDataOnDemand {
	
	@Autowired
    private ProviderDataOnDemand providerDataOnDemand;

	public void setPayee(Payment obj, int index) {
        Provider payee = providerDataOnDemand.getRandomProvider();
        obj.setPayee(payee);
    }
 }
