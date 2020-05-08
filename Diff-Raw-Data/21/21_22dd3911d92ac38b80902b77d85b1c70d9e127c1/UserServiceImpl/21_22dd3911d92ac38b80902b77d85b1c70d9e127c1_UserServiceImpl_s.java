 package org.netvogue.server.neo4japi.service;
 
 import java.util.Date;
 import java.util.Set;
 
 import org.netvogue.server.neo4japi.common.Constants;
 import org.netvogue.server.neo4japi.common.NetworkStatus;
 import org.netvogue.server.neo4japi.common.ResultStatus;
 import org.netvogue.server.neo4japi.common.USER_TYPE;
 import org.netvogue.server.neo4japi.common.Utils;
 import org.netvogue.server.neo4japi.domain.Editorial;
 import org.netvogue.server.neo4japi.domain.Gallery;
 import org.netvogue.server.neo4japi.domain.PrintCampaign;
 import org.netvogue.server.neo4japi.domain.User;
 import org.netvogue.server.neo4japi.repository.UserRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.neo4j.support.Neo4jTemplate;
 
 public class UserServiceImpl implements UserService{
 	@Autowired Neo4jTemplate		neo4jTemplate;
 	@Autowired UserRepository		userRepo;
 	
 	public ResultStatus SaveUser(User user, String error){
 		try {
 			//New Categories node will be created an relationship will also be added for this.
 			//Saving it through Template instead of boutiquerepo so that categories node can also be saved
 			neo4jTemplate.save(user);
 			System.out.println("Updated User Successfully");
 			return ResultStatus.SUCCESS;
 		} catch(Exception e) {
 			System.out.println("There was an error for" + user.getEmail() + " - " + e.toString());
 			error = e.toString();
 			return ResultStatus.FAILURE;
 		}
 	}
 	
 	public void getBrandsCarriedAndCategories(User user) {
 		neo4jTemplate.fetch(user.getUsersCarried());
 		neo4jTemplate.fetch(user.getProductLinesCarried());
 	}
 	
 	public UserData getUserDataByUsername(String username) {
 		return userRepo.getUserByusername(username);
 	}
 	
 	public User getUserByUsername(String username) {
 		return userRepo.findByusername(username);
 	}
 	
 	public ResultStatus ValidateEmailAndId(String email, Long id) {
 		if(email.isEmpty()) {
 			return ResultStatus.FAILURE;
 		}
 		User b = userRepo.findByemailAndId(email, id);
 		if(null == b) {
 			return ResultStatus.SUCCESS;
 		}
 		return ResultStatus.USER_EXISTS;
 	}
 	
 	//Search related
 	public Iterable<User> doBasicSearch(String query) {
 		query = "username:*" + query + "* OR " + "name:*" + query + "*";
 		return userRepo.doBasicSearch(query, Constants.BASICSEARCH_LIMIT, 0);
 	}
 	
 	public Iterable<User> getAllUsers(String query) {
 		query = "username:*" + query + "* OR " + "name:*" + query + "*";
 		return userRepo.doBasicSearch(query, Constants.ADVSEARCH_LIMIT, 0);
 	}
 	
 	public Iterable<User> doAdvancedSearch(USER_TYPE userType, String name, String location, 
 					Set<String> categories, Set<String> usersCarried, long fromPrice, long toPrice) {
 		
 		//Query
 		String query = "username:*" + name + "*";
 		if(!name.isEmpty()) {
 			query = "username:*" + name + "* OR " + "name:*" + name + "*" ;
 		}
 		if(!location.isEmpty()) {
 			if(!query.isEmpty()) {
 				query += (" AND ");
 			}
 			query += ("city:*" + location  + "* OR " + "country:*" + location + "*");
 		}
 		if(name.isEmpty() && location.isEmpty()) {
 			query = "username:*";
 		}
 		Long fromprice = new Long(0);
 		Long toprice = Long.MAX_VALUE;
 		if(0 != fromPrice)
 			fromprice = fromPrice;
 		if(0 != toPrice)
 			toprice = toPrice;
 		//Categories
 		String cat = "productline:gth";
 		if(!categories.isEmpty())
 			cat = Utils.SerializeQueryParamForSet(Constants.Category_Productline, categories);
 		//Users carried
 		String userscarried = "username:sdf";
 		if(!usersCarried.isEmpty())
 			userscarried = Utils.SerializeQueryParamForSet(Constants.User_Username, usersCarried);
 		
 		System.out.println(query);
 		System.out.println(cat);
 		System.out.println(userscarried);
 		return userRepo.doAdvancedSearch(cat, query, userType.toString(), userscarried,
											fromprice, toprice);
 	}
 	
 	//Network related
 	public NetworkStatus getNetworkStatus(String username1, String username2) {
 		return userRepo.getNetworkStatus(username1, username2);
 	}
 	
 	//References
 	public Iterable<ReferenceData> getReferences(String username, int pagenumber) {
 		if(username.isEmpty()) 
 			return null;
 		return userRepo.getReferences(username, Constants.REFERENCES_PER_CALL * pagenumber, Constants.REFERENCES_PER_CALL);
 	}
 	
 	//Queries related to galleries
 	public Iterable<Gallery> GetGalleries(User user) {
 		if(null != user) {
 			return userRepo.getGalleries(user.getUsername());
 		}
 		return null;
 	}
 	
 	public Iterable<Gallery> searchGalleryByName(User user, String name) {
 		return searchGalleryByName(user.getUsername(), name);
 	}
 	
 	public Iterable<Gallery> searchGalleryByName(String username, String name) {
 		return userRepo.searchGalleryByName(username, Utils.SerializePropertyParamForSearch(name));
 	}
 	
 	//Queries related to printcampaigns
 	public Iterable<PrintCampaign> getPrintCampaigns(User user) {
 		if(null != user) {
 			return userRepo.getPrintCampaigns(user.getUsername());
 		}
 		return null;
 	}
 	
 	public Iterable<PrintCampaign> searchPrintCampaignByName(User user, String name) {
 		return searchPrintCampaignByName(user.getUsername(), name);
 	}
 	
 	public Iterable<PrintCampaign> searchPrintCampaignByName(String username, String name) {
 		return userRepo.searchPrintCampaignByName(username, Utils.SerializePropertyParamForSearch(name));
 	}
 	
 	//Queries related to Editorials
 	public Iterable<Editorial> getEditorials(User user) {
 		if(null != user) {
 			return userRepo.getEditorials(user.getUsername());
 		}
 		return null;
 	}
 	
 	public Iterable<Editorial> searchEditorialByName(User user, String name) {
 		return searchEditorialByName(user.getUsername(), name);
 	}
 	
 	public Iterable<Editorial> searchEditorialByName(String username, String name) {
 		return userRepo.searchEditorialByName(username, Utils.SerializePropertyParamForSearch(name));
 	}
 	
 	//Queries related to collections
 	public Iterable<CollectionData> getCollections(User user) {
 		if(null != user) {
 			if(USER_TYPE.BOUTIQUE == user.getUserType()) {
 				return userRepo.getMyNetworkCollections(user.getUsername());
 			} else if(USER_TYPE.BRAND == user.getUserType()) {
 				return userRepo.getCollections(user.getUsername());
 			}
 		}
 		return null;
 	}
 	
 	public Iterable<CollectionData> searchCollections(User user, String seasonname, Set<String> categories, String brandname){
 		if(null != user) {
 			String username = user.getUsername();
 			String season = Utils.SerializePropertyParamForSearch(seasonname);
 			String brand = Utils.SerializePropertyParamForSearch(brandname);
 			String category = Utils.SerializeQueryParamForSet(Constants.Category_Productline, categories);
 			System.out.println("user name is" + username);
 			System.out.println("Season name is" + season);
 			System.out.println("brand name is" + brand);
 			System.out.println("Category is" + category);
 			if(USER_TYPE.BOUTIQUE == user.getUserType()) {
 				return userRepo.searchMyNetworkCollections(username, season, category, brand);
 			} else if(USER_TYPE.BRAND == user.getUserType()) {
 				return userRepo.searchCollections(username, season, category);
 			}
 		}
 		return null; 
 	}
 	
 	public Iterable<StylesheetData> getStylesheets(User user) {
 		if(null != user) {
 			return userRepo.getStylesheets(user.getUsername());
 		}
 		return null;
 	}
 	
 	public Iterable<StylesheetData> searchStylesheets(User user, String name, Set<String> categories) {
 		if(null != user) {
 			String username = user.getUsername();
 			String stylesheetname = Utils.SerializePropertyParamForSearch(name);
 			String category = Utils.SerializeQueryParamForSet(Constants.Category_Productline, categories);
 			System.out.println("user name is" + username);
 			System.out.println("Stylesheet name is" + stylesheetname);
 			System.out.println("Category is" + category);
 			return userRepo.searchStylesheets(username, stylesheetname, category);
 		}
 		return null;
 	}
 	
 	public Iterable<LinesheetData> getLinesheets(User user) {
 		if(null != user) {
 			if(USER_TYPE.BOUTIQUE == user.getUserType()) {
 				return userRepo.getMyNetworkLinesheets(user.getUsername());
 			} else if(USER_TYPE.BRAND == user.getUserType()) {
 				return userRepo.getLinesheets(user.getUsername());
 			}
 			return userRepo.getLinesheets(user.getUsername());
 		}
 		return null;
 	}
 	
 	public Iterable<LinesheetData> searchLinesheets(User user, String seasonname, 
 														 Set<String> categories,
 														 Date fromDate, Date toDate,
 														 long fromPrice, long toPrice,
 														 String brandname) {
 		if(null != user) {
 			String username = user.getUsername();
 			String season = Utils.SerializePropertyParamForSearch(seasonname);
 			String brand = Utils.SerializePropertyParamForSearch(brandname);
 			String category = Utils.SerializeQueryParamForSet(Constants.Category_Productline, categories);
 			System.out.println("user name is" + username);
 			System.out.println("Season name is" + season);
 			System.out.println("brand name is" + brand);
 			System.out.println("Category is" + category);
 		
 			Long fromprice = new Long(0);
 			Long toprice = Long.MAX_VALUE;
 			if(0 != fromPrice)
 				fromprice = fromPrice;
 			if(0 != toPrice)
 				toprice = toPrice;
 			
 			String fromdate = String.valueOf(fromDate.getTime());
 			String todate = String.valueOf(toDate.getTime());
 			System.out.println("From Price is" + fromprice);
 			System.out.println("To Price is" + toprice);
 			System.out.println("From Date is" + fromDate.getTime());
 			System.out.println("To Date is" + toDate.getTime());
 			if(USER_TYPE.BOUTIQUE == user.getUserType()) {
 				return userRepo.searchMyNetworkLinesheets(username, season, category, 
 														fromdate, todate, fromprice, toprice, brand);
 			} else if(USER_TYPE.BRAND == user.getUserType()) {
 				return userRepo.searchLinesheets(username, season, category,
 															fromdate, todate, fromPrice, toPrice);
 			}
 		}
 		return null;
 	}
 }
