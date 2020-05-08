 package org.netvogue.server.neo4japi.service;
 
 import org.netvogue.server.neo4japi.common.ResultStatus;
 import org.netvogue.server.neo4japi.common.Utils;
 import org.netvogue.server.neo4japi.domain.Editorial;
 import org.netvogue.server.neo4japi.domain.Gallery;
 import org.netvogue.server.neo4japi.domain.PrintCampaign;
 import org.netvogue.server.neo4japi.domain.Collection;
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
 		return userRepo.searchGalleryByName(username, Utils.SerializeQueryParamForSearch(name));
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
 		return userRepo.searchPrintCampaignByName(username, Utils.SerializeQueryParamForSearch(name));
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
 		return userRepo.searchEditorialByName(username, Utils.SerializeQueryParamForSearch(name));
 	}
 	
 	//Queries related to collections
 	public Iterable<Collection> getCollections(User user) {
 		if(null != user) {
 			return userRepo.getCollections(user.getUsername());
 		}
 		return null;
 	}
 	
 	public Iterable<Collection> searchCollectionByName(User user, String name) {
 		return searchCollectionByName(user.getUsername(), name);
 	}
 	
 	public Iterable<Collection> searchCollectionByName(String username, String seasonname) {
 		return userRepo.searchCollectionByName(username, Utils.SerializeQueryParamForSearch(seasonname));
 	}
 	
 	public Iterable<Collection> searchCollections(String username, String seasonname, String category, String brandname){
		if(category.isEmpty() && 
 		return userRepo.searchCollectionByName(username, Utils.SerializeQueryParamForSearch(seasonname));
 	}
 }
