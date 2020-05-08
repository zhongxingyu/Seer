 package com.guille.league;
 
 public class Club {
 	
 	private String name;
 	private Country country;
 	private String city;
 	private String alias;
 
 	public Club(ClubBuilder builder) {
 		this.name = builder.name;
 		this.country = builder.country;
 		this.city = builder.city;
 		this.alias = builder.alias;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public Country getCountry() {
 		return country;
 	}
 
 	public void setCountry(Country country) {
 		this.country = country;
 	}
 
 	public String getCity() {
 		return city;
 	}
 
 	public void setCity(String city) {
 		this.city = city;
 	}
 
 	public String getName() {
 		return name;
 	}
 	
 	public String getAlias() {
 		return alias;
 	}
 
 	public void setAlias(String alias) {
 		this.alias = alias;
 	}
 
 	public static class ClubBuilder implements Builder{
 		
 		private final String name;
 		private final Country country;
 		private String city;
 		private String alias;
 		
 		public ClubBuilder(String name, Country country){
 			this.name = name;
 			this.country = country;
 		}
 		
 		public ClubBuilder city(String city){
 			this.city = city;
 			return this;
 		}
 		
 		public ClubBuilder alias(String alias){
 			this.alias = alias;
 			return this;
 		}
 		
 		public Club build(){
 			return new Club(this);
 		}
 		
 		public Club buildEnglishClub(){
 			return new Club( new ClubBuilder("English Club", Country.ENGLAND));
 		}
 		
 		public Club buildSpanishClub(){
			return new Club( new ClubBuilder("English Club", Country.SPAIN));
 		}
 		
 	}
 	
 }
