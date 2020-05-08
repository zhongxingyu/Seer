 package com.github.hvasoares.pageobjects.impl.mutability;
 
 import com.github.hvasoares.pageobjects.impl.ProxyStatePageObjectAdapter;
 
 public class ProxyStatePageObject extends ProxyStatePageObjectAdapter {
 
 	private PageContextI ctx;
 
 	public ProxyStatePageObject(PageContextI context) {
 		this.ctx = context;
 	}
 
 	@Override
 	public void setState(String stateName) {
		setMutability(ctx.get(stateName,getOuter()));
 		super.setState(stateName);
 	}
 
 	
 }
