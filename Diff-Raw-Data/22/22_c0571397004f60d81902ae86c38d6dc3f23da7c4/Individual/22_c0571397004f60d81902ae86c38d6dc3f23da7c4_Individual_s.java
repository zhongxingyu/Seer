 /*
* $Id: Individual.java,v 3.0 2005/01/27 18:19:03 jcbarret Exp $
 * WHITEHEAD INSTITUTE
 * SOFTWARE COPYRIGHT NOTICE AGREEMENT
 * This software and its documentation are copyright 2002 by the
 * Whitehead Institute for Biomedical Research.  All rights are reserved.
 *
 * This software is supplied without any warranty or guaranteed support
 * whatsoever.  The Whitehead Institute can not be responsible for its
 * use, misuse, or functionality.
 */
 
 package edu.mit.wi.pedfile;
 
 import java.util.Vector;
 import java.util.Iterator;
 
 /**
  * stores information about an individual from a pedigree file
  *
  * this class is not thread safe (untested)
  *
  * @author Julian Maller
  */
 public class Individual {
     private String familyID;
     private String individualID;
     private String momID;
     private String dadID;
     private int gender;
     private int affectedStatus;
 
     private String reasonImAxed;
     private int liability; //optional
     private Vector markers;
     private double numGoodMarkers;
     private boolean[] zeroed;
     //this is used to keep track of the index of the last marker added
     private int currMarker;
 
 	public final static int FEMALE = 2;
 	public final static int MALE = 1;
 	public final static int AFFACTED = 2;
 	public final static int UNAFFACTED = 1;
 	public final static String DATA_MISSING ="0";
 
 
 
     public Individual(int numLines) {
         this.markers = new Vector(numLines);
         //this.zeroed = new Vector(numLines);
         this.zeroed = new boolean[numLines];
         this.currMarker = 0;
     }
 
     /**
      * gets the family ID
      * @return The familyID for this individual
      */
     public String getFamilyID() {
         return familyID;
     }
     /**
      * sets the family ID
      * @param familyID
      */
     public void setFamilyID(String familyID) {
         this.familyID = familyID;
     }
     /**
      * gets the Individual ID
      * @return The individualID for this individual
      */
     public String getIndividualID() {
         return individualID;
     }
     /**
      * sets the individual ID
      * @param individualID
      */
     public void setIndividualID(String individualID) {
         this.individualID = individualID;
     }
     /**
      * gets the momID for this individual
      * @return momID
      */
     public String getMomID() {
         return momID;
     }
     /**
      * sets the momid
      * @param momID
      */
     public void setMomID(String momID) {
         this.momID = momID;
     }
     /**
      * gets the dad ID for this individual
      * @return dadID
      */
     public String getDadID() {
         return dadID;
     }
     /**
      * sets the dadID
      * @param dadID
      */
     public void setDadID(String dadID) {
         this.dadID = dadID;
     }
     /**
      * gets the gender for this individual
      * @return gender
      */
     public int getGender() {
         return gender;
     }
     /**
      * sets the gender
      * @param gender
      */
     public void setGender(int gender) {
         this.gender = gender;
     }
     /**
      * gets the affected status for this individual
      * @return affectedStatus
      */
     public int getAffectedStatus() {
         return affectedStatus;
     }
     /**
      * sets the affected status
      * @param affectedStatus
      */
     public void setAffectedStatus(int affectedStatus) {
         this.affectedStatus = affectedStatus;
     }
     /**
      * gets the liability class for this individual
      * @return liability
      */
     public int getLiability() {
         return liability;
     }
     /**
      * sets the liability class
      * @param liability
      */
     public void setLiability(int liability) {
         this.liability = liability;
     }
 
     /**
      * gets the markers vector for this individual
      * @return Vector markers
      */
     public Vector getMarkers() {
         return markers;
     }
 
     public void setMarkers(Vector m){
         markers = m;
     }
 
     /**
      * returns the number of markers for this individual
      * @return integer count of markers
      */
     public int getNumMarkers(){
         return this.markers.size();
     }
 
     /**
      * returns a two byte array of the marker at the index specified by location
      * @param location the index in the markers vector
      * @return two byte array with the marker values
      */
     public byte[] getMarker(int location){
         return (byte[])this.markers.get(location);
     }
 
     /**
      * adds a marker to the markers vector
      * @param marker - two byte array with first marker in index 0 and second in index 1
      */
     public void addMarker(byte[] marker){
         this.markers.add(marker);
         //this.zeroed.add(new Boolean(false));
         this.zeroed[currMarker] = false;
         this.currMarker++;
         if (!(marker[0] == 0 || marker[1] == 0)){
             numGoodMarkers++;
         }
     }
 
     /**
      * checks to see if a marker has been zeroed out
      * @param location - which marker to check
      * @return true if marker is zeroed, false otherwise
      */
     public boolean getZeroed(int location){
         //return ((Boolean)zeroed.get(location)).booleanValue();
         return zeroed[location];
     }
 
     /**
      * sets the bit that this marker has been zeroed out for this indiv (e.g. because it has a mendel error)
      * @param i - marker to be zeroed
      */
     public void zeroOutMarker(int i){
         //this.zeroed.set(i, new Boolean(true));
         this.zeroed[i] = true;
     }
     /**
      * returns an iterator for the markers Vector
      * @return iterator for the markers Vector
      */
     public Iterator markerIterator(){
         return this.markers.iterator();
     }
 
     public String getReasonImAxed() {
         return reasonImAxed;
     }
 
     public void setReasonImAxed(String reasonImAxed) {
         this.reasonImAxed = reasonImAxed;
     }
 
     public double getGenoPC(){
         return numGoodMarkers/markers.size();
     }
 }
 
