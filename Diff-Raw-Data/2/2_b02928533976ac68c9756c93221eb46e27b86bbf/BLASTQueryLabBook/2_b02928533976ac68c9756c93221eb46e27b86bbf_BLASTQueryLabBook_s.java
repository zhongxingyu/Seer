 package com.bioinformaticsapp.data;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 
 import com.bioinformaticsapp.models.BLASTQuery;
 import com.bioinformaticsapp.models.BLASTQuery.Status;
 import com.bioinformaticsapp.models.SearchParameter;
 
 public class BLASTQueryLabBook {
 
 	public BLASTQueryLabBook(Context context) {
 		this.context = context;
 	}
 
 	public BLASTQuery save(BLASTQuery aQuery) {
 		blastQueryController = new BLASTQueryController(context);
 		searchParameterController = new SearchParameterController(context);
 		BLASTQuery savedQuery = (BLASTQuery)aQuery.clone();
 		if(aQuery.getPrimaryKey() == null){
 			long queryPrimaryKey = blastQueryController.save(aQuery);
 			savedQuery.setPrimaryKeyId(queryPrimaryKey);
 		}else{
 			blastQueryController.update(aQuery);
 			searchParameterController.deleteParametersFor(aQuery.getPrimaryKey());
 		}
 		blastQueryController.close();
 		
 		for(SearchParameter parameter: aQuery.getAllParameters()){
 			searchParameterController.saveFor(savedQuery.getPrimaryKey(), parameter);
 		}
 		searchParameterController.close();
 		
 		return savedQuery;
 	}
 	
 	public BLASTQuery findQueryById(Long id) {
 		blastQueryController = new BLASTQueryController(context);
 		searchParameterController = new SearchParameterController(context);
 		BLASTQuery queryWithID = blastQueryController.findBLASTQueryById(id);
 		List<SearchParameter> parameters = searchParameterController.getParametersForQuery(id);
 		queryWithID.updateAllParameters(parameters);
 		blastQueryController.close();
 		searchParameterController.close();
 		return queryWithID;
 	}
 	
 	public List<BLASTQuery> findBLASTQueriesByStatus(
 			Status status) {
 		blastQueryController = new BLASTQueryController(context);
 		searchParameterController = new SearchParameterController(context);
 		List<BLASTQuery> queriesWithStatus = blastQueryController.findBLASTQueriesByStatus(status);
 		for(BLASTQuery query: queriesWithStatus){
 			List<SearchParameter> parameters = searchParameterController.getParametersForQuery(query.getPrimaryKey());
 			query.updateAllParameters(parameters);
 		}
 		blastQueryController.close();
 		searchParameterController.close();
 		
 		return queriesWithStatus;
 	}
 	
 	public List<BLASTQuery> findPendingBLASTQueriesFor(int vendor){
 		List<BLASTQuery> allPendingQueries = findBLASTQueriesByStatus(Status.PENDING);
 		if(allPendingQueries.isEmpty()){
 			return new ArrayList<BLASTQuery>();
 		}
 		
 		List<BLASTQuery> queriesPendingForVendor = queriesForVendor(allPendingQueries, vendor);
 		
 		return queriesPendingForVendor;
 	}
 	
	public List<BLASTQuery> submittedBLASTQueriesForVentor(int vendor) {
 		List<BLASTQuery> allSubmittedQueries = findBLASTQueriesByStatus(Status.SUBMITTED);
 		if(allSubmittedQueries.isEmpty()){
 			return new ArrayList<BLASTQuery>();
 		}
 		
 		List<BLASTQuery> queriesSubmittedToVendor = queriesForVendor(allSubmittedQueries, vendor);
 		
 		return queriesSubmittedToVendor;
 	}
 	
 	private List<BLASTQuery> queriesForVendor(List<BLASTQuery> queries, int vendor){
 		List<BLASTQuery> queriesForVendor = new ArrayList<BLASTQuery>();
 		for(BLASTQuery query: queries){
 			if(query.getVendorID() == vendor){
 				queriesForVendor.add(query);
 			}
 		}
 		
 		return queriesForVendor;
 	}
 
 	private Context context;
 	private BLASTQueryController blastQueryController;
 	private SearchParameterController searchParameterController;
 	
 }
