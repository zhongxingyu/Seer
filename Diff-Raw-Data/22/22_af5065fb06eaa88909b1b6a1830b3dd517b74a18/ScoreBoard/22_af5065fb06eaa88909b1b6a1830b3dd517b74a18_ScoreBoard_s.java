 package ceid.netcins.exo.catalog;
 
 import java.io.Serializable;
 import java.util.Iterator;
 import java.util.Vector;
 
 /**
  * This class represents a Catalog enhanced with some scoring of the
  * CatalogEntries
  * 
  * @author <a href="mailto:loupasak@ceid.upatras.gr">Andreas Loupasakis</a>
  * @author <a href="mailto:ntarmos@cs.uoi.gr">Nikos Ntarmos</a>
  * @author <a href="mailto:peter@ceid.upatras.gr">Peter Triantafillou</a>
  * 
  * "eXO: Decentralized Autonomous Scalable Social Networking"
  * Proc. 5th Biennial Conf. on Innovative Data Systems Research (CIDR),
  * January 9-12, 2011, Asilomar, California, USA.
  * @version 1.0
  */
 public class ScoreBoard  implements Serializable {
 
 	private static final long serialVersionUID = 4381039714176907029L;
 
 	public class Score {
 		private CatalogEntry entry;
 		private Float score;
 
 		Score(CatalogEntry entry, Float score) {
 			this.entry = entry;
 			this.score = score;
 		}
 
 		public CatalogEntry getEntry() {
 			return entry;
 		}
 
 		public Float getScore() {
 			return score;
 		}
 	}
 
	private Vector<Score> entries;
 
 	/**
 	 * rows and scoreValues must be sorted appropriately in Scorer!
 	 * 
 	 * @param tid
 	 * @param rows
 	 * @param scoreValues
 	 */
 	public ScoreBoard(Vector<CatalogEntry> catalogEntries,
 			Vector<Float> scoreValues) {
		if (catalogEntries == null || scoreValues == null ||
				catalogEntries.size() != scoreValues.size())
 			throw new RuntimeException("Illegal scoreboard initialization");
		entries = new Vector<ScoreBoard.Score>();
		Iterator<CatalogEntry> itce = catalogEntries.iterator();
		Iterator<Float> itf = scoreValues.iterator();
		while (itce.hasNext())
			entries.add(new Score(itce.next(), itf.next()));
 	}
 
 	public Vector<Float> getScores() {
 		Vector<Float> ret = new Vector<Float>();
 		for (Score sc : entries) {
 			ret.add(sc.score);
 		}
 		return ret;
 	}
 	
 	public Vector<CatalogEntry> getCatalogEntries(){
 		Vector<CatalogEntry> ret = new Vector<CatalogEntry>();
 		for (Score sc : entries) {
 			ret.add(sc.entry);
 		}
 		return ret;
 	}
 
 	public Vector<Score> getAllEntries() {
 		return entries;
 	}
 
 	public double computeBytes() {
 		double counter = 0;
 		counter += Float.SIZE * entries.size();
 
 		for (Score ce : entries) {
 			counter += ce.entry.computeTotalBytes();
 		}
 
 		return counter;
 	}
 }
