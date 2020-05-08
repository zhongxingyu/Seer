 /*
  * Copyright (c) 2013. Tomasz Szuba, Paulina Schab, Michał Tkaczyk. All rights reserved.
  */
 
 package com.miniinf.OSPManager.data;
 
 import com.mysema.query.annotations.QueryEntity;
 import org.joda.time.Duration;
 import org.joda.time.LocalDate;
 import org.joda.time.LocalTime;
 import org.springframework.data.annotation.Id;
 import org.springframework.data.mongodb.core.mapping.Document;
 import org.springframework.format.annotation.DateTimeFormat;
 
 import javax.validation.Valid;
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Pattern;
 import java.io.Serializable;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Class describes Operation, it's participants, firetrucks etc.
  * Created by Tomasz Szuba
  * Date: 05.11.12
  */
 @QueryEntity
 @Document
 public class Operation implements BigIntegerEntity, Serializable {
 
     @Id
     private BigInteger id;
 
     @Valid
     private Address place = new Address();
 
     @Min(0)
     private int number;
 
     private String owner;
 
     private String interventionType;
 
     private String victim;
 
     private String[] cooperatingUnits;
 
     private String means;
 
     private String quantity;
 
     private String declarant;
 
     private String description;
 
     private int nrPSP;
 
     private String author;
 
     @NotNull
     @Pattern(regexp = FireTruck.CODENAME_PATTERN, message = "{com.miniinf.OSPManager.validation.codename}")
     private String truckCodeName;
 
     @Min(0)
     private int mileageStart;
 
     private int mileageEnd;
 
     private LocalTime startTime;
 
     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
     private LocalDate startDate;
 
     private LocalTime endTime;
 
     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
     private LocalDate endDate;
 
     private List<FireFighter> participants = new ArrayList<FireFighter>();
 
     /**
      * Returns ID of Operation in database
      *
      * @return Operation's ID
      */
     public BigInteger getId() {
         return id;
     }
 
     /**
      * Returns address (where operation took place)
      *
      * @return Operation's address
      */
     public Address getPlace() {
         return place;
     }
 
     /**
      * Sets where Operation's took place
      *
      * @param place Operation's address
      */
     public void setPlace(Address place) {
         this.place = place;
     }
 
     /**
      * Returns list of the FireFighters who participated in Operation
      *
      * @return list of Operation's FireFighters
      */
     public List<FireFighter> getParticipants() {
         return participants;
     }
 
     /**
      * Sets the list of the FireFighters who participated in Operation
      *
      * @param participants list of Operation's FireFighters
      */
     public void setParticipants(List<FireFighter> participants) {
         this.participants = participants;
     }
 
     /**
      * Returns number of Operation's FireFighters
      *
      * @return
      */
     public int getParticipantsCount() {
         return participants.size();
     }
 
     /**
      * Returns the Operation's number
      *
      * @return
      */
     public int getNumber() {
         return number;
     }
 
     /**
      * Sets the Operation's number
      *
      * @param number
      */
     public void setNumber(int number) {
         this.number = number;
     }
 
     /**
      * Returns Operation's owner name
      *
      * @return Operation's owner name
      */
     public String getOwner() {
         return owner;
     }
 
     /**
      * Sets Operation's owner name
      *
      * @param owner Operation's owner name
      */
     public void setOwner(String owner) {
         this.owner = owner;
     }
 
     /**
      * Returns Operation's intervention type
      *
      * @return Operation's intervention type
      */
     public String getInterventionType() {
         return interventionType;
     }
 
     /**
      * Sets Operation's intervention type
      *
      * @param interventionType Operation's intervention type
      */
     public void setInterventionType(String interventionType) {
         this.interventionType = interventionType;
     }
 
     /**
      * Returns victims names in one string
      *
      * @return Operation's victims names
      */
     public String getVictim() {
         return victim;
     }
 
     /**
      * Sets victims names as one string
      *
      * @param victim victims names in one string
      */
     public void setVictim(String victim) {
         this.victim = victim;
     }
 
     /**
      * Returns table of strings of units who helped in Operation
      *
      * @return table of names of units who helped in Operation
      */
     public String[] getCooperatingUnits() {
         return cooperatingUnits;
     }
 
     /**
      * Sets table of strings of units who helped in Operation
      *
      * @param cooperatingUnits table of name of units who helped in Operation
      */
     public void setCooperatingUnits(String[] cooperatingUnits) {
         this.cooperatingUnits = cooperatingUnits;
     }
 
     /**
      * Returns name of the author who made the raport about this operation
      *
      * @return author name
      */
     public String getAuthor() {
         return author;
     }
 
     /**
      * Sets name of the Operation's author
      *
      * @param author author name
      */
     public void setAuthor(String author) {
         this.author = author;
     }
 
     /**
      * Returns declarant name
      *
      * @return declarant name
      */
     public String getDeclarant() {
         return declarant;
     }
 
     /**
      * Sets declarant name
      *
      * @param declarant declarant name
      */
     public void setDeclarant(String declarant) {
         this.declarant = declarant;
     }
 
     /**
      * Returns description about Operation
      *
      * @return description about Operation
      */
     public String getDescription() {
         return description;
     }
 
     /**
      * Sets description about Operation
      *
      * @param description description about Operation
      */
     public void setDescription(String description) {
         this.description = description;
     }
 
     /**
      * Returns PSP number
      *
      * @return
      */
     public int getNrPSP() {
         return nrPSP;
     }
 
     /**
      * Sets PSP number
      *
      * @param nrPSP
      */
     public void setNrPSP(int nrPSP) {
         this.nrPSP = nrPSP;
     }
 
     /**
      * Returns codename of the truck assigned to this Operation
      *
      * @return truck codename
      */
     public String getTruckCodeName() {
         return truckCodeName;
     }
 
     /**
      * Sets codename of the truck assigned to this Operation
      *
      * @param truckCodeName truck codename
      */
     public void setTruckCodeName(String truckCodeName) {
         this.truckCodeName = truckCodeName;
     }
 
     /**
      * Returns the state of the mileage at the start of the operation
      *
      * @return
      */
     public int getMileageStart() {
         return mileageStart;
     }
 
     /**
      * Sets the state of the mileage at the start of the operation
      *
      * @param mileageStart
      */
     public void setMileageStart(int mileageStart) {
         this.mileageStart = mileageStart;
     }
 
     /**
      * Returns the state of the mileage at the end of the operation
      *
      * @return
      */
     public int getMileageEnd() {
         return mileageEnd;
     }
 
     /**
      * Sets state of the mileage at the end of the operation
      *
      * @param mileageEnd
      */
     public void setMileageEnd(int mileageEnd) {
         this.mileageEnd = mileageEnd;
     }
 
     public LocalTime getStartTime() {
         return startTime;
     }
 
     public void setStartTime(LocalTime startTime) {
         this.startTime = startTime;
     }
 
     public LocalDate getStartDate() {
         return startDate;
     }
 
     public void setStartDate(LocalDate startDate) {
         this.startDate = startDate;
     }
 
     public LocalTime getEndTime() {
         return endTime;
     }
 
     public void setEndTime(LocalTime endTime) {
         this.endTime = endTime;
     }
 
     public LocalDate getEndDate() {
         return endDate;
     }
 
     public void setEndDate(LocalDate endDate) {
         this.endDate = endDate;
     }
 
     public String getMeans() {
         return means;
     }
 
     public void setMeans(String means) {
         this.means = means;
     }
 
     public String getQuantity() {
         return quantity;
     }
 
     public void setQuantity(String quantity) {
         this.quantity = quantity;
     }
 
     public static class FireFighter implements Serializable, Comparable<FireFighter> {
 
         private BigInteger id;
 
         private String name;
 
         private String surname;
 
         private Duration operatingTime;
 
         private Duration paidTime;
 
         public BigInteger getId() {
             return id;
         }
 
         public void setId(BigInteger id) {
             this.id = id;
         }
 
         public String getName() {
             return name;
         }
 
         public void setName(String name) {
             this.name = name;
         }
 
         public String getSurname() {
             return surname;
         }
 
         public void setSurname(String surname) {
             this.surname = surname;
         }
 
         public Duration getOperatingTime() {
             return operatingTime;
         }
 
         public void setOperatingTime(Duration operatingTime) {
             this.operatingTime = operatingTime;
         }
 
         public Duration getPaidTime() {
             return paidTime;
         }
 
         public void setPaidTime(Duration paidTime) {
             this.paidTime = paidTime;
         }
 
 
         @Override
         public int compareTo(FireFighter o) {
             return id.compareTo(o.id);
         }
     }
 }
