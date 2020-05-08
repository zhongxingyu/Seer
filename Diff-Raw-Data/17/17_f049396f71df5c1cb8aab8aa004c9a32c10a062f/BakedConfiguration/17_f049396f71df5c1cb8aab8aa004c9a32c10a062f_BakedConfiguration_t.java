 package bakedbeans.example;
 
 import bakedbeans.core.Configuration;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 
 public class BakedConfiguration extends Configuration {
 	
 	
 	@JsonProperty
 	PersonDetails nick = new PersonDetails();
 
	
	public PersonDetails getNick() {
		return nick;
	}
 }
