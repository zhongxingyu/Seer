 package utils;
 
 import models.City;
 import models.Country;
 import models.Project;
 import models.User;
 import play.Logger;
 import play.jobs.Job;
 import play.jobs.OnApplicationStart;
 import play.test.MorphiaFixtures;
 
 import static utils.Constants.COUNTRY_GERMANY;
 
 @OnApplicationStart
 public class Bootstrap extends Job {
  
     public void doJob() {
     	Logger.info("Checking database for bootstrap");
         // Check if the database is empty
     	if(User.find("byUsername", "admin").first() == null){
     		Logger.info("Proceeding to load initial data");
     		MorphiaFixtures.loadModels("initial-data.yml");
     		
     		fixCityCountries();
     		fixUsers();
     		fixProjects();
     	}
     }
 
     private void fixProjects() {
     	Project project = Project.find("name", "My first project!").get();
     	User user = User.find("username","admin").get();
     	City city = City.find("name", "Berlin").get();
     	
     	project.owner = user;
     	project.city = city;
     	project.save();
 	}
 
 	/**
      * Adds the relationships to cities due to Morphia bug with 1.2.4
      */
 	private void fixCityCountries() {
 		Country germany = Country.find("name", COUNTRY_GERMANY).get();
 		
 		City city = City.find("name", "Berlin").get();
 		city.country = germany;
 		city.save();
 		
 		city = City.find("name", "Munich").get();
 		city.country = germany;
 		city.save();		
 	}
 	
 	/**
      * Adds the relationships for Users due to Morphia bug
      */
 	private void fixUsers() {
 		Country germany = Country.find("name", COUNTRY_GERMANY).get();
 		User user = User.find("username","admin").get();
 		
		user.country = germany;
 		user.save();
 	}
  
 }
