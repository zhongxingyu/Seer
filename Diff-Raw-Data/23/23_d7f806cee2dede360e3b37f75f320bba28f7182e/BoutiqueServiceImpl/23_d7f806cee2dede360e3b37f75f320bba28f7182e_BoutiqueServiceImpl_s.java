 package org.netvogue.server.neo4japi.service;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.neo4j.support.Neo4jTemplate;
 
 import org.netvogue.server.neo4japi.common.ProductLines;
 import org.netvogue.server.neo4japi.common.ResultStatus;
 import org.netvogue.server.neo4japi.common.USER_TYPE;
 import org.netvogue.server.neo4japi.common.Utils;
 import org.netvogue.server.neo4japi.domain.*;
 import org.netvogue.server.neo4japi.repository.BoutiqueRefNodeRepository;
 import org.netvogue.server.neo4japi.repository.BoutiqueRepository;
 import org.netvogue.server.neo4japi.repository.CategoryRepository;
 import org.netvogue.server.neo4japi.repository.UserRepository;
 
 //@Component("BoutiqueService")
 public class BoutiqueServiceImpl implements BoutiqueService{
 
 	@Autowired BoutiqueRepository 			boutiqueRepo;
 	@Autowired BoutiqueRefNodeRepository	boutiqueRefNodeRepo;
 	@Autowired UserRepository				userRepo;
 	@Autowired CategoryRepository 			categoryRepo;
 	
 	@Autowired Neo4jTemplate				neo4jTemplate;
 	
 	public ResultStatus AddNewBoutique(Boutique newBoutique, String error) {
 		//Boutique newBoutique = new Boutique();
 		/*BoutiqueRefNode boutiqueRefNode = boutiqueRefNodeRepo.getrefnode();
 		newBoutique.AddtoRefNode(boutiqueRefNode);*/
 		//boutiqueRefNode.AddNewBoutique(newBoutique);
 		//boutiqueRefNodeRepo.save(boutiqueRefNode);
 		//neo4jTemplate.save(boutiqueRefNode);
 		//Object o = new Object();
 		try {
 			//New Categories node will be created an relationship will also be added for this.
 			//Saving it through Template instead of boutiquerepo so that categories node can also be saved
 			neo4jTemplate.save(newBoutique);
 			//boutiqueRepo.save(newBoutique);
 			System.out.println("Added Boutique Successfully");
 			return ResultStatus.SUCCESS;
 		} catch(Exception e) {
 			System.out.println("There was an error for" + newBoutique.getEmail() + " - " + e.toString());
 			error = e.toString();
 			return ResultStatus.FAILURE;
 		}
 		
 		//neo4jTemplate.save(o);
 	}
 	
 	public ResultStatus ValidateEmail(String email) {
 		if(email.isEmpty()) {
 			return ResultStatus.FAILURE;
 		}
 		User b = userRepo.findByemail(email);
 		if(null == b) {
 			return ResultStatus.SUCCESS;
 		}
 		return ResultStatus.USER_EXISTS;
 	}
 	
 	public ResultStatus ValidateUsername(String username) {
 		if(username.isEmpty()) {
 			return ResultStatus.FAILURE;
 		}
 		User b = userRepo.findByusername(username);
 		if(null == b) {
 			return ResultStatus.SUCCESS;
 		}
 		return ResultStatus.USER_EXISTS;
 	}
 
 	@Override
 	public Iterable<Boutique> GetBoutique(String boutiqueName) {
		String query = Utils.SerializeQueryParamWithSpacesNoPattern("name" , boutiqueName);
 		return boutiqueRepo.findBynameSearch(query, USER_TYPE.BOUTIQUE);
 	}
 	
 	public User GetUser(String name) {
 		User user =  userRepo.findByusername(name);
 		if(null == user) {
 			user = userRepo.findByemail(name);
 		}
 		return user;
 	}
 	
 	public User GetOrCreateUser(String name) {
 		//Serializing is not working when we use it with pattern matching. Have used it here, as there is no pattern matching
 		User user =  userRepo.findByusername(Utils.SerializeQueryParam(name));
 		if(null == user) {
 			user = new User();
 			user.setUsername(name);
 			user.setName(name);
 		}
 		return user;
 	}
 	
 	//@Transactional
 	public ResultStatus deleteAll() {
 		//neo4jTemplate.beginTx();
 		boutiqueRepo.deleteAll();
 		//boutiqueRepo.
 		
 		//Map<String, Object> temp = new HashMap<String, Object>();
 		//Result<Map<String, Object>> result = neo4jTemplate.query("START n = node(*) DELETE n", temp);
 		
 		return ResultStatus.SUCCESS;
 	}
 	
 	public User AuthenticateUser(String email, String password) {
 		 User user = GetUser(email);
 		 if(user == null) {
 			 System.out.println("\nNo User exists with this name");
 			 return null;
 		 }
 		 if(true == user.Authenticate(password)) {
 			 return user; 
 		 }
 		 
 		 System.out.println("Bad Password");
 		 return null;		 
 	}
 	//Add all other methods
 	public Category getCategory(ProductLines name) {
 		if(null == categoryRepo) {
 			System.out.println("Update Categories---categoryRepo is NULL"+ name);
 		}
 		Category newCategory = categoryRepo.findByproductline(name);
 		return newCategory;
 	}
 	
 	//Make sure you do template.save or repo.save
 	public Category getOrCreateCategory(ProductLines name) {
 		if(null == categoryRepo) {
 			System.out.println("Update Categories---categoryRepo is NULL"+ name);
 		}
 		Category newCategory = categoryRepo.findByproductline(name);
 		if(null == newCategory) {
 			newCategory = new Category(name);
 		}
 		return newCategory;
 	}
 }
