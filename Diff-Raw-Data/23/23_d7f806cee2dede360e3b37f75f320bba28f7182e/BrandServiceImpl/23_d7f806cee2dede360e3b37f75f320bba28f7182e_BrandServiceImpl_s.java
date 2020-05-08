 package org.netvogue.server.neo4japi.service;
 
 //Project specific
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.neo4j.support.Neo4jTemplate;
 
 import org.netvogue.server.neo4japi.domain.Brand;
 import org.netvogue.server.neo4japi.repository.*;
 import org.netvogue.server.neo4japi.common.*;
 
 public class BrandServiceImpl implements BrandService{
 
 	@Autowired BrandRepository 				brandRepo;
 	@Autowired BrandRefNodeRepository		brandRefNodeRepo;
 	@Autowired Neo4jTemplate				neo4jTemplate;
 	
 	public ResultStatus AddNewBrand(Brand newBrand, String error) {
 		try {
 			//New Categories node will be created an relationship will also be added for this.
 			//Saving it through Template instead of boutiquerepo so that categories node can also be saved
 			neo4jTemplate.save(newBrand);
 			//boutiqueRepo.save(newBoutique);
 			System.out.println("Added Brand Successfully");
 			return ResultStatus.SUCCESS;
 		} catch(Exception e) {
 			System.out.println("There was an error - " + e.toString());
 			error = e.toString();
 			return ResultStatus.FAILURE;
 		}		
 	}
 	
 	public ResultStatus ValidateEmail(String email) {
 		Brand b = brandRepo.findByPropertyValue("email", email);
 		if(null == b) {
 			return ResultStatus.SUCCESS;
 		}
 		return ResultStatus.USER_EXISTS;
 	}
 	
 	public Iterable<Brand> GetBrand(String name) {
		String query = "name:*" + name + "*";
 		return brandRepo.findBynameSearch(query, USER_TYPE.BRAND);
 	}
 }
