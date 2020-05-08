 package com.andreou.galc;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.TreeSet;
 
 public class Worker implements Comparable<Worker> {
 
 	private String							name;
 
 	private Double							est_rho;
 	private Double							est_mu;
 	private Double							est_sigma;
 
 	// The labels assigned by the worker
 	private Set<AssignedLabel>	labels;
 	// The labels normalized into empirical z-values (subtracted empirical mean, divided by stdev)
 	private Set<AssignedLabel>	zeta;
 
 	// True values
 	private Double							true_mu;
 	private Double							true_sigma;
 	private Double							true_rho;
 
 	public Worker(String name) {
 
 		this.name = name;
 		this.labels = new TreeSet<AssignedLabel>();
 	}
 
 	public void addAssignedLabel(AssignedLabel al) {
 
 		if (al.getWorker().equals(name)) {
 			this.labels.add(al);
 		}
 	}
 
 	public Set<AssignedLabel> getZetaValues() {
 
 		return zeta;
 	}
 
 	public void computeZetaValues() {
 
 		int n = labels.size();
 		double mu_worker = 0.0;
 		double mu_square = 0.0;
 		for (AssignedLabel al : labels) {
 			mu_worker += al.getLabel();
 			mu_square += Math.pow(al.getLabel(), 2);
 		}
 
 		this.est_mu = mu_worker / n;
 		this.est_sigma = Math.sqrt( (1.0 / n) * (mu_square - Math.pow(mu_worker, 2) / n));
 		//System.out.println(this.toString());
 
 		this.zeta = new HashSet<AssignedLabel>();
 		for (AssignedLabel al : labels) {
 			Double z = (al.getLabel() - this.est_mu) / this.est_sigma;
 			AssignedLabel zl = new AssignedLabel(al.getWorker(), al.getDatum(), z);
 			this.zeta.add(zl);
 		}
 	}
 
 	public Double getZeta(Double label) {
 
 		return (label - this.est_mu) / this.est_sigma;
 	}
 
 	public String getName() {
 
 		return name;
 	}
 
 	public void setName(String name) {
 
 		this.name = name;
 	}
 
 
 	public Double getBeta() {
 
 		// To avoid overflows
		if (Math.abs(this.est_rho)>0.999) return 500.0;
 		
 		return 1 / (1 - Math.pow(this.est_rho, 2));
 	}
 
 	public Double getTrueMu() {
 
 		return true_mu;
 	}
 
 	public void setTrueMu(Double mu) {
 
 		this.true_mu = mu;
 	}
 
 	public Double getTrueSigma() {
 
 		return true_sigma;
 	}
 
 	public void setTrueSigma(Double sigma) {
 
 		this.true_sigma = sigma;
 	}
 
 	public Double getTrueRho() {
 
 		return true_rho;
 	}
 
 	public void setTrueRho(Double rho) {
 
 		this.true_rho = rho;
 	}
 
 	@Override
 	public int hashCode() {
 
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (!(obj instanceof Worker))
 			return false;
 		Worker other = (Worker) obj;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		return true;
 	}
 
 	@Override
 	public int compareTo(Worker o) {
 
 		return this.getName().compareTo(o.getName());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 
 		return "Worker [name=" + name + ", est_rho=" + est_rho + ", true_rho=" + true_rho + ", est_mu=" + est_mu
 				+ ", true_mu=" + true_mu + ", est_sigma=" + est_sigma + ", true_sigma=" + true_sigma + "]";
 	}
 
 	
 	/**
 	 * @return the est_rho
 	 */
 	public Double getEst_rho() {
 	
 		return est_rho;
 	}
 
 	
 	/**
 	 * @param est_rho the est_rho to set
 	 */
 	public void setEst_rho(Double est_rho) {
 	
		if (est_rho>0.999) this.est_rho = 0.999;
		if (est_rho<-0.999) this.est_rho = -0.999;
 		
 		this.est_rho = est_rho;
 	}
 
 	
 	/**
 	 * @return the est_mu
 	 */
 	public Double getEst_mu() {
 	
 		return est_mu;
 	}
 
 	
 	/**
 	 * @param est_mu the est_mu to set
 	 */
 	public void setEst_mu(Double est_mu) {
 	
 		this.est_mu = est_mu;
 	}
 
 	
 	/**
 	 * @return the est_sigma
 	 */
 	public Double getEst_sigma() {
 	
 		return est_sigma;
 	}
 
 	
 	/**
 	 * @param est_sigma the est_sigma to set
 	 */
 	public void setEst_sigma(Double est_sigma) {
 	
 		this.est_sigma = est_sigma;
 	}
 
 }
