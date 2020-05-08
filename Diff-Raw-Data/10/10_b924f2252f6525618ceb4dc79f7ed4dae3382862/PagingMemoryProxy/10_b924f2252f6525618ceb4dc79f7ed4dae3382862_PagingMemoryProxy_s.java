 package com.safedoor.testing.client;
 
 import java.util.List;
 
 import com.google.gwt.core.client.Callback;
 import com.sencha.gxt.data.shared.loader.MemoryProxy;
 import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
 import com.sencha.gxt.data.shared.loader.PagingLoadResult;
 import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;
 
 public class PagingMemoryProxy extends MemoryProxy<PagingLoadConfig, PagingLoadResult<Data>> {
 	
 	private List<Data> totalList;
 	
 	public PagingMemoryProxy(List<Data> totalList) {
 		super(null);//data is useless in this case, memoryProxy only designed to hold, not to search
 		this.totalList = totalList;
 	}
 	
 	@Override
 	public void load(PagingLoadConfig config, Callback<PagingLoadResult<Data>, Throwable> callback) {
 		List<Data> results = totalList.subList(config.getOffset(),
 				Math.min(config.getLimit()+config.getOffset(),totalList.size()-1));// Get results list based on the data the proxy was created with
		callback.onSuccess(new PagingLoadResultBean<Data>(results,config.getLimit(),config.getOffset()));// again, data from the config
 	}
 }
