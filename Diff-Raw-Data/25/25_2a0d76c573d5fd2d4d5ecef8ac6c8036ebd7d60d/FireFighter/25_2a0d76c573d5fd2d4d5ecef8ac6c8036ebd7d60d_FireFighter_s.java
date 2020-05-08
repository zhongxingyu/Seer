 /*
  * Copyright (c) 2013. Tomasz Szuba, Paulina Schab, Michał Tkaczyk. All rights reserved.
  */
 
 package com.miniinf.OSPManager.data;
 
 import com.miniinf.OSPManager.validation.PESEL;
 import org.joda.time.LocalDate;
 import org.springframework.data.annotation.Id;
 import org.springframework.data.mongodb.core.mapping.Document;
 import org.springframework.format.annotation.DateTimeFormat;
 
 import javax.validation.Valid;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Past;
 import javax.validation.constraints.Pattern;
 import javax.validation.constraints.Size;
 import java.math.BigInteger;
 import java.util.List;
 
 @Document
 public class FireFighter implements BigIntegerEntity {
 
    private static final String NAME_PATTERN = "([A-Z][a-z]{2,})?";
 
     private static final String LEGITIMATION_PATTERN = "\\d+/\\d{4}";
 
     @Id
     private BigInteger id;
 
     @NotNull
     @Size(min = 3)
     @Pattern(regexp = NAME_PATTERN, message = "{com.miniinf.OSPManager.validation.name}")
     private String name;
 
     @Pattern(regexp = NAME_PATTERN, message = "{com.miniinf.OSPManager.validation.name}")
     private String secondName;
 
     @NotNull
     @Size(min = 3)
     @Pattern(regexp = NAME_PATTERN, message = "{com.miniinf.OSPManager.validation.name}")
     private String surname;
 
     @Past
     @NotNull
     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
     private LocalDate birthDate;
 
     @Valid
     private Address address = new Address();
 
     @PESEL
     private String pesel;
 
     @Pattern(regexp = LEGITIMATION_PATTERN, message = "{com.miniinf.OSPManager.validation.legitimation}")
     private String legitimation;
 
     private String rank;
 
     private List<String> awards;
 
     public BigInteger getId() {
         return id;
     }
 
     public Address getAddress() {
         return address;
     }
 
     public void setAddress(Address address) {
         this.address = address;
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
 
     public LocalDate getBirthDate() {
         return birthDate;
     }
 
     public void setBirthDate(LocalDate birthDate) {
         this.birthDate = birthDate;
     }
 
     public String getSecondName() {
         return secondName;
     }
 
     public void setSecondName(String secondName) {
         this.secondName = secondName;
     }
 
     public String getNameAndSurname() {
         return name + " " + surname;
     }
 
     public String getPesel() {
         return pesel;
     }
 
     public void setPesel(String pesel) {
         this.pesel = pesel;
     }
 
     public String getLegitimation() {
         return legitimation;
     }
 
     public void setLegitimation(String legitimation) {
         this.legitimation = legitimation;
     }
 
     public String getRank() {
         return rank;
     }
 
     public void setRank(String rank) {
         this.rank = rank;
     }
 
     public List<String> getAwards() {
         return awards;
     }
 
     public void setAwards(List<String> awards) {
         this.awards = awards;
     }
 }
