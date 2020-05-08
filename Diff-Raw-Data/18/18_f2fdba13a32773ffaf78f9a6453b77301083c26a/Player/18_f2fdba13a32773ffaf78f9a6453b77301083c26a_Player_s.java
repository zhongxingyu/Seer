 package org.tjug.chess.data;
 //
 //  This class represents a Player Data Object.
 //
 //  The XmlRootEmelemt annotation activates for JAXB (Java XML Binding).
 //    This allows for automatic conversion of the DAO to and from 
 //    both JSON (JavaScript Object Notation) and XML.
 //
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.xml.bind.annotation.XmlRootElement;
 
 //@Entity(name="PlayerEntity")
 @XmlRootElement(name="Player")
 public class Player {
 	
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private Long id;
 	private String name;
	private String clubRanking;
 	private String wins;
 	private String losses;
 	
 	public Player(){}
 	
	public Player(String name, String clubRanking, String wins, String losses){
 		setName(name);
		setClubRanking(clubRanking);
 		setWins(wins);
 		setLosses(losses);
 		
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getName() {
 		return name;
 	}
 
	public void setClubRanking(String clubRanking) {
		this.clubRanking = clubRanking;
 	}
 
	public String getClubRanking() {
		return clubRanking;
 	}
 
 	public void setWins(String wins) {
 		this.wins = wins;
 	}
 
 	public String getWins() {
 		return wins;
 	}
 
 	public void setLosses(String losses) {
 		this.losses = losses;
 	}
 
 	public String getLosses() {
 		return losses;
 	}
 }
