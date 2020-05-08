 package com.github.hvasoares.pageobjects.impl.mutability;
 
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import com.github.hvasoares.pageobjects.MutableAssertiveness;
 import com.github.hvasoares.pageobjects.PageObjectBuilder;
 import com.github.hvasoares.pageobjects.StatePageObject;
 import com.github.hvasoares.pageobjects.impl.assertivepageobject.Assertiveness;
 import com.github.hvasoares.pageobjects.impl.assertivepageobject.AssertivenessFactory;
 
 public class MutableAssertivenessImpl implements MutableAssertiveness {
 
 	private PageObjectBuilder builder;
 	private StatePageObject statePageObject;
 	public HashMap<String,FluidXpath> db;
 	private FluidXpath xpath;
 	
 	public MutableAssertivenessImpl(){
 		db = new HashMap<>();
 	}
 
 	@Override
 	public PageObjectBuilder add(String alias, String xpath) {
 		db.put(alias, new FluidXpath(xpath));
 		return builder;
 	}
 
 	@Override
 	public StatePageObject check(String alias, String... params) {
 		Assertiveness assertiveness = AssertivenessFactory.create();
 		xpath = db.get(alias);
		for(Entry<String, String> e : Utils.toMapSetEntry(params))
 			xpath.bind(e.getKey(), e.getValue());
 		assertiveness.pageAssertion(xpath.getTransformedXpath());
 		return statePageObject;
 	}
 
 	public void setStatePageObject(StatePageObject statePageObject) {
 		this.statePageObject = statePageObject;
 	}
 
 	public void setPageObjectBuilder(PageObjectBuilder pageObjectBuilder) {
 		this.builder = pageObjectBuilder;
 	}
 
 }
