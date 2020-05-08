 ﻿package model;
 
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import model.data.exceptions.RecordNotFoundException;
 import model.exceptions.*;
 
 import model.exceptions.FalseIDException;
 
 public class InRent {
 	
 	private int rID;
 	private int customerID;
 	private Customer customer = null;
 	private int videoUnitID;
 	private VideoUnit videoUnit = null;
 	private Date date;
 	private int duration;
 	
 	private static Map<Integer, InRent> inRentList;
 	private static int minrID;
 	
 	public InRent(Customer customer, VideoUnit videoUnit, Date date, int duration) 
 			throws FalseIDException, FalseFieldException, CurrentDateException{
 		this(minrID, customer.getID(), videoUnit.getID(), date, duration); 		
 		minrID++;
 		this.customer = customer;
 		this.videoUnit = videoUnit;
 	}
 	
 	private InRent(int rID, int customerID, int videoUnitID, Date date, int duration) 
 			throws FalseIDException, FalseFieldException, CurrentDateException{
 		this.rID = rID;
 		this.customerID = customerID;
 		this.videoUnitID = videoUnitID;
 		this.date = date;
 		this.duration = duration;
 		checkIDs();
 		checkRentDate();
 		checkDuration();
 	}
 	
 	public static InRent reCreate(int rID, int customerID, int videoUnitID, Date date, int duration) 
 			throws FalseIDException, FalseFieldException, CurrentDateException{
 		return new InRent(rID, customerID, videoUnitID, date, duration);
 	}
 	
 	public static void setMinID(int newMinrID) throws FalseIDException{
 		if(newMinrID > 0){
 		minrID = newMinrID;
 		}else{
 			throw new FalseIDException("Übergebene MinID für InRent ist kleiner 0!!!");
 		}
 	}
 	
 	private void checkRentDate() throws FalseFieldException, CurrentDateException{
 		if( this.date.compareTo(CurrentDate.get()) != 0) 
 			throw new FalseFieldException("Bitte Datum überprüfen");
 	}
 	
 	private void checkDuration() throws FalseFieldException{
 		if( this.duration < 1 || this.duration > 5 ) throw new FalseFieldException();
 	}
 	
 	public int getID(){
 		return this.rID;
 	}
 	
 	private void checkIDs() throws FalseIDException{
 		int rID = this.rID;
 		int customerID = this.customerID;
 		int videoUnitID = this.videoUnitID;
 		if( rID < 1 || customerID < 1 || videoUnitID < 1 ) 	throw new FalseIDException();
 	}
 	
 	public Customer getCustomer(){
 		if( this.customer == null){
 			// TODO: hier nach Customer objekt suchen mit der id = customerID und this.customer darauf verweisen
 		}
 		return this.customer;
 	}
 	
 	public VideoUnit getVideoUnit(){
 		if( this.videoUnit == null ){
 			// TODO: hier nach VideoUnit objekt suchen mit der id = videoUnitID und this.videoUnit darauf verweisen
 		}
 		return this.videoUnit;
 	}
 	
 
 	public static InRent findByID(int inRentID) throws RecordNotFoundException{
 		if(inRentList.containsKey(inRentID)){
 			return inRentList.get(inRentID);
 		}else{
 			throw new RecordNotFoundException("Ausleihe", "AusleihNummer", Integer.toString(inRentID));
 		}
 	}
 	
 	public static List<InRent> findByCustomer(Customer customer){
 		List<InRent> foundInRents = new LinkedList<InRent>();
 		for(InRent ir : inRentList.values()){
 			if(ir.customer.getID() == customer.getID()){
 				foundInRents.add(ir);
 			}
 		}
 		return foundInRents;
 	}
 	
	public static InRent findByVideoUnit(VideoUnit videoUnit) throws RecordNotFoundException{
 		for(InRent ir : inRentList.values()){
 			if(ir.videoUnitID == videoUnit.getID()){
				return ir;
 			}
 		}
		
		throw new RecordNotFoundException("Ausleihe", "VideoExemplarNr.", Integer.toString(videoUnit.getID()));
 	}
 	
 	public static List<InRent> findByDate(Date date){
 		List<InRent> foundInRents = new LinkedList<InRent>();
 		for(InRent ir : inRentList.values()){
 			if(ir.date.equals(date)){
 				foundInRents.add(ir);
 			}
 		}
 		return foundInRents;
 	}
 
 	public static void setInRentList(Map<Integer, InRent> newInRentList){
 		if(newInRentList != null){
 			inRentList = newInRentList;
 		}
 	}
 	
 }
