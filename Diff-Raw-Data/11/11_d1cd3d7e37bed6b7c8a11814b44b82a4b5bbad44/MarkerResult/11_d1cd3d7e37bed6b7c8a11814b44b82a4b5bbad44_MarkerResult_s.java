 /*
 * $Id: MarkerResult.java,v 1.1 2003/08/01 19:36:19 jmaller Exp $
  * WHITEHEAD INSTITUTE
  * SOFTWARE COPYRIGHT NOTICE AGREEMENT
  * This software and its documentation are copyright 2003 by the
  * Whitehead Institute for Biomedical Research.  All rights are reserved.
  *
  * This software is supplied without any warranty or guaranteed support
  * whatsoever.  The Whitehead Institute can not be responsible for its
  * use, misuse, or functionality.
  */
 
 package edu.mit.wi.pedfile;
 
 import java.text.*;
 
 /**
  * <p>Title: MarkerResult.java </p>
  * <p>Description: Gets the result for a marker
  * Result includes observed heterozyosity, predicted heterozygosity,
  * Hardy-Weinberg test p-value, genotyped percent, number of families with
  * a fully genotyped trio, number of Mendelian inheritance errors and rating.</p>
  * @author Hui Gong
  * @version $Revision 1.2 $
  */
 
 public class MarkerResult {
 
 	private double _obsHET;
 	private double _predHET;
 	private double _HWpval;
 	private double _genoPercent;
 	private int _famTrioNum;
 	private int _mendErrNum;
 	private int _rating;
 	private String _name="";
	NumberFormat nf = NumberFormat.getInstance();
 
 	public MarkerResult() {
 		nf.setMinimumFractionDigits(3);
 		nf.setMaximumFractionDigits(3);
	}

 	/**
 	 * Sets the marker name
 	 */
 	public void setName(String name){
 		this._name = name;
 	}
 
 	/**
 	 * Gets the marker name
 	 */
 	public String getName(){
 		return this._name;
 	}
 
 	/**
 	 * Sets observed heterozygosity
 	 */
 	public void setObsHet(double obsHet){
 		this._obsHET = obsHet;
 	}
 
 	/**
 	 * Sets predicted heterozygosity
 	 */
 	public void setPredHet(double predHet){
 		this._predHET = predHet;
 	}
 
 	/**
 	 * Sets Hardy-Weinberg test p-value
 	 */
 	public void setHWpvalue(double pvalue){
 		this._HWpval = pvalue;
 	}
 
 	/**
 	 * Sets percent of individuals genotyped
 	 */
 	public void setGenoPercent(double genoPct){
 		this._genoPercent = genoPct;
 	}
 
 	/**
 	 * Sets # of families with a fully genotyped trio
 	 */
 	public void setFamTrioNum(int num){
 		this._famTrioNum = num;
 	}
 
 	/**
 	 * Sets # of Mendelian inheritance errors
 	 */
 	public void setMendErrNum(int num){
 		this._mendErrNum = num;
 	}
 
 	/**
 	 * Sets the data rating
 	 */
 	public void setRating(int rating){
 		this._rating = rating;
 	}
 
 	/**
 	 * Gets observed heterozygosity
 	 */
 	public double getObsHet(){
 		return new Double(nf.format(this._obsHET)).doubleValue();
 	}
 
 	/**
 	 * Gets predicted heterozygosity
 	 */
 	public double getPredHet(){
 		return new Double(nf.format(this._predHET)).doubleValue();
 	}
 
 	/**
 	 * Gets Hardy-Weinberg test p-value
 	 */
 	public double getHWpvalue(){
 		return new Double(nf.format(this._HWpval)).doubleValue();
 	}
 
 	/**
 	 * Gets percent of individuals genotyped
 	 */
 	public double getGenoPercent(){
 		return new Double(nf.format(this._genoPercent/100)).doubleValue();
 	}
 
 	/**
 	 * Gets # of families with a fully genotyped trio
 	 */
 	public int getFamTrioNum(){
 		return this._famTrioNum;
 	}
 
 	/**
 	 * Gets # of Mendelian inheritance errors
 	 *
 	 */
 	public int getMendErrNum(){
 		return this._mendErrNum;
 	}
 
 
 	/**
 	 * Gets the data rating,
 	 * rating is -1 if obsHet < 0.01, -2 if geno < 75, -3 if HWpval < .01,
 	 * -4 if Mendel > 1, and 1 if all criteria pass
 	 */
 	public int getRating(){
 		return this._rating;
 	}
 
 	public String toString(){
 		StringBuffer buffer = new StringBuffer();
 		NumberFormat format=NumberFormat.getInstance();
 		format.setMaximumFractionDigits(3);
 		format.setMinimumFractionDigits(3);
 
 		buffer.append(format.format(this._obsHET) +"\t"
 		        + format.format(this._predHET) + "\t");
 
 		format.setMaximumFractionDigits(2);
 		format.setMinimumFractionDigits(0);
 		buffer.append(format.format(this._HWpval) +"\t");
 
 		format.setMaximumFractionDigits(1);
 		format.setMinimumFractionDigits(1);
 		buffer.append(format.format(this._genoPercent) + "\t");
 		buffer.append(this._famTrioNum + "\t");
 		if(this._mendErrNum < 0) buffer.append("   \t");
 		else buffer.append(this._mendErrNum+"\t");
 		buffer.append(this._rating);
 		return buffer.toString();
 	}
 }
