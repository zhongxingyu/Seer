 package de.tum.in.dbs.project.wis.model;
 
 import de.tum.in.dbs.project.wis.Mode;
 
 /**
  * Data class for an aggregated vote
  * 
  * @author Marcus Vetter
  * 
  */
 public class AggregatedVote {
 
 	/**
 	 * Id of the constituency
 	 */
 	private int constituencyId;
 
 	/**
 	 * Name of the party
 	 */
 	private String party;
 
 	/**
 	 * The votes [first2009, second2009, first2005, second2005]
 	 */
	private int[] votes = new int[4];
 
 	/**
 	 * Constructor to create an object with all fields
 	 * 
 	 * @param constituencyId
 	 * @param party
 	 * @param firstvotes2009
 	 * @param secondvotes2009
 	 * @param firstvotes2005
 	 * @param secondvotes2005
 	 */
 	public AggregatedVote(int constituencyId, String party, int firstvotes2009,
 			int secondvotes2009, int firstvotes2005, int secondvotes2005) {
 		super();
 		this.constituencyId = constituencyId;
 		this.party = party;
		
 		this.votes[0] = firstvotes2009;
 		this.votes[1] = secondvotes2009;
 		this.votes[2] = firstvotes2005;
 		this.votes[3] = secondvotes2005;
 	}
 
 	/**
 	 * @return the constituencyId
 	 */
 	public int getConstituencyId() {
 		return constituencyId;
 	}
 
 	/**
 	 * @return the party
 	 */
 	public String getParty() {
 		return party;
 	}
 
 	/**
 	 * 
 	 * @param mode
 	 *            {@link Mode}
 	 * @return the votes
 	 */
 	public int getVotes(Mode mode) {
 		switch (mode) {
 		case first2009:
 			return this.votes[0];
 		case second2009:
 			return this.votes[1];
 		case first2005:
 			return this.votes[2];
 		case second2005:
 			return this.votes[3];
 		}
 		return -1;
 	}
 }
