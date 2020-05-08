 /**
  * 
  */
 package model;
 
 /**
  * @author muelleml
  * 
  */
 public class Metric {
 
 	// delimiter
 	String del = "|";
 
 	// Cue Metrics
 	int cgold;
 	int csystem;
 
 	int ctp;
 	int cfp;
 	int cfn;
 
 	float cprec;
 	float crec;
 	float cf;
 
 	// scope Metrics
 	int sgold;
 	int ssystem;
 
 	int stp;
 	int sfp;
 	int sfn;
 
 	float sprec;
 	float srec;
 	float sf;
 
 	/**
 	 * Bulk add Cue Metrics
 	 * 
 	 * @param cgold
 	 *            # of Gold cue tokens
 	 * @param csystem
 	 *            # of System of cue tokens
 	 * @param ctp
 	 *            True Positives
 	 * @param cfp
 	 *            False Positives
 	 * @param cfn
 	 *            False Negatives
 	 * @param cprec
 	 *            Precision
 	 * @param crec
 	 *            Recall
 	 * @param cf
 	 *            F1 score
 	 */
 	public void addCueMetric(int cgold, int csystem, int ctp, int cfp, int cfn,
 			float cprec, float crec, float cf) {
 		this.cgold = cgold;
 		this.csystem = csystem;
 		this.ctp = ctp;
 		this.cfp = cfp;
 		this.cfn = cfn;
 		this.cprec = cprec;
 		this.crec = crec;
 		this.cf = cf;
 	}
 
 	/**
 	 * Bulk add Scope Metrics
 	 * 
 	 * @param sgold
 	 *            # of Gold scope tokens
 	 * @param ssystem
 	 *            # of System of scope tokens
 	 * @param stp
 	 *            True Positives
 	 * @param sfp
 	 *            False Positives
 	 * @param sfn
 	 *            False Negatives
 	 * @param sprec
 	 *            Precision
 	 * @param srec
 	 *            Recall
 	 * @param sf
 	 *            F1 score
 	 */
 	public void addScopeMetric(int sgold, int ssystem, int stp, int sfp,
 			int sfn, float sprec, float srec, float sf) {
 		this.sgold = sgold;
 		this.ssystem = ssystem;
 		this.stp = stp;
 		this.sfp = sfp;
 		this.sfn = sfn;
 		this.sprec = sprec;
 		this.srec = srec;
 		this.sf = sf;
 	}
 
 	public Metric() {
 		// Cue Metrics
 		cgold = 0;
 		csystem = 0;
 
 		ctp = 0;
 		cfp = 0;
 		cfn = 0;
 
 		cprec = 0;
 		crec = 0;
 		cf = 0;
 
 		// scope Metrics
 		sgold = 0;
 		ssystem = 0;
 
 		stp = 0;
 		sfp = 0;
 		sfn = 0;
 
 		sprec = 0;
 		srec = 0;
 		sf = 0;
 	}
 
 	/**
 	 * Cue Gold
 	 * 
 	 * @return the cgold
 	 */
 	public int getCgold() {
 		return cgold;
 	}
 
 	/**
 	 * Cue System
 	 * 
 	 * @return the csystem
 	 */
 	public int getCsystem() {
 		return csystem;
 	}
 
 	/**
 	 * Cue True Positives
 	 * 
 	 * @return the ctp
 	 */
 	public int getCtp() {
 		return ctp;
 	}
 
 	/**
 	 * Cue False Positives
 	 * 
 	 * @return the cfp
 	 */
 	public int getCfp() {
 		return cfp;
 	}
 
 	/**
 	 * Cue False Negatives
 	 * 
 	 * @return the cfn
 	 */
 	public int getCfn() {
 		return cfn;
 	}
 
 	/**
 	 * Cue Precision
 	 * 
 	 * @return the cprec
 	 */
 	public float getCprec() {
 		return cprec;
 	}
 
 	/**
 	 * Cue Recall
 	 * 
 	 * @return the crec
 	 */
 	public float getCrec() {
 		return crec;
 	}
 
 	/**
 	 * Cue F1 score
 	 * 
 	 * @return the cf
 	 */
 	public float getCf() {
 		return cf;
 	}
 
 	/**
 	 * Scope Gold
 	 * 
 	 * @return the sgold
 	 */
 	public int getSgold() {
 		return sgold;
 	}
 
 	/**
 	 * Scope System
 	 * 
 	 * @return the ssystem
 	 */
 	public int getSsystem() {
 		return ssystem;
 	}
 
 	/**
 	 * Scope True Positives
 	 * 
 	 * @return the stp
 	 */
 	public int getStp() {
 		return stp;
 	}
 
 	/**
 	 * Scope False Positives
 	 * 
 	 * @return the sfp
 	 */
 	public int getSfp() {
 		return sfp;
 	}
 
 	/**
 	 * Scope False Negatives
 	 * 
 	 * @return the sfn
 	 */
 	public int getSfn() {
 		return sfn;
 	}
 
 	/**
 	 * Scope Precision
 	 * 
 	 * @return the sprec
 	 */
 	public float getSprec() {
 		return sprec;
 	}
 
 	/**
 	 * Scope Recall
 	 * 
 	 * @return the srec
 	 */
 	public float getSrec() {
 		return srec;
 	}
 
 	/**
 	 * Scope F1 score
 	 * 
 	 * @return the sf
 	 */
 	public float getSf() {
 		return sf;
 	}
 
 	public String toString() {
 		StringBuilder r = new StringBuilder();
 
 		r.append("Cues:");
 		r.append(cgold);
 		r.append(del);
 		r.append(csystem);
 		r.append(del);
 		r.append(ctp);
 		r.append(del);
 		r.append(cfp);
 		r.append(del);
 		r.append(cfn);
 		r.append(del);
 		r.append(cprec);
 		r.append(del);
 		r.append(crec);
 		r.append(del);
 		r.append(cf);
 
		r.append(System.lineSeparator());
 
 		r.append("Scope tokens(no cue match)");
 		r.append(sgold);
 		r.append(del);
 		r.append(ssystem);
 		r.append(del);
 		r.append(stp);
 		r.append(del);
 		r.append(sfp);
 		r.append(del);
 		r.append(sfn);
 		r.append(del);
 		r.append(sprec);
 		r.append(del);
 		r.append(srec);
 		r.append(del);
 		r.append(sf);
 
 		return r.toString().trim();
 	}
 
 }
