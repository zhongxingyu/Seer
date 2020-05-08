 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.hyushik.registration.test.entities;
 
 /**
  *
  * @author McAfee
  */
 public class Participant {
     
     private String name = "";
     private String email = "";
     private String address = "";
     private String city = "";
     private String state = "";
 
     private String zip = "";
     private String phone = "";
     
     public Gender gender = Gender.MALE;
     
     private String instructorName = "";
     private String schoolName = "";
     private String schoolAddress = "";
     private String schoolCity = "";
     private String schoolState = "";
     private String schoolZip = "";
     private String schoolPhone = "";
     private String schoolEmail = "";
     
     private Rank rank = Rank.WHITE;
     private int age = 0;
     private int weight = 0;
     
     private boolean weapons = false;
     private boolean breaking = false;
     private boolean sparring = false;
     private boolean point = false;
     private boolean olympic = false;
     
     private int numberOfBoards = 0;
     
     public static enum Gender {
         MALE("male"),
         FEMALE("female");
         
         private String text="";
         
         Gender(String text) {
             this.text = text;
         }
         
         @Override
         public String toString(){
             return text;
         }
     }
     
     public static enum Rank {
         WHITE("White"),
         YELLOW_ORANGE("Yellow/Orange"),
         GREEN("Green"),
         BLUE("Blue"),
         BROWN_RED("Brown/Red"),
         BLACK("Black");
         
         private String text = ""; 
         Rank(String text) {
             this.text = text;
         }
         
         @Override
         public String toString(){
             return text;
         }
     }
 
     public Participant() {
     }
     
     
     //minimal for Registration
     public Participant(String name, String email, String address, String city, 
             String state, String zip, String phone, String instructor, 
             String school, int age, int weight) {
         this.name = name;
         this.email = email;
         this.address = address;
         this.city = city;
         this.state = state;
         this.zip = zip;
         this.phone = phone;
         this.instructorName = instructor;
         this.schoolName = school;
         this.age = age;
         this.weight = weight;
     }
 
     public Participant(String name, String email, String address, String city, 
             String state, String zip, String phone, Gender gender, 
             String instructor, String school, int age, int weight
             ){
         this(name, email, address, city, state, zip, phone, instructor, school, 
                 age, weight);
         this.gender = gender;
     }
     
     public Participant(String name, String email, String address, String city, 
             String state, String zip, String phone, Gender gender, 
             String instructorName, String schoolName, String schoolAddress, 
             String schoolCity, String schoolState, String schoolZip, String schoolPhone, 
             String schoolEmail, Rank rank, int age, int weight){
         this(name, email, address, city, state, zip, phone, gender, instructorName, schoolName, 
                 age, weight);
         this.schoolAddress = schoolAddress;
         this.schoolCity = schoolCity ;
         this.schoolState = schoolState;
         this.schoolPhone = schoolPhone;
         this.schoolEmail = schoolEmail;
         this.schoolZip = schoolZip;
         this.rank = rank;
     }
    
     
     public Participant(String name, String email, String address, String city, 
             String state, String zip, String phone, Gender gender, 
             String instructorName, String schoolName, String schoolAddress, 
             String schoolCity, String schoolState, String schoolZip, String schoolPhone,
             String schoolEmail, Rank rank, int age, int weight, boolean weapons,
             boolean breaking, boolean sparring, boolean point, boolean olympic,
             int numberOfBoards){
         this(name, email, address, city, state, zip, phone, gender, 
             instructorName, schoolName, schoolAddress, 
             schoolCity, schoolState, schoolZip, schoolPhone, 
             schoolEmail, rank, age, weight);
         
         this.weapons=weapons;
         this.breaking = breaking;
         this.sparring = sparring;
         this.point = point; 
         this.olympic = olympic;
         this.numberOfBoards = numberOfBoards;
                 
     }
 
     public String getName() {
         return name;
     }
 
     public String getEmail() {
         return email;
     }
 
     public String getAddress() {
         return address;
     }
 
     public String getCity() {
         return city;
     }
 
     public String getState() {
         return state;
     }
 
     public String getZip() {
         return zip;
     }
 
     public String getPhone() {
         return phone;
     }
 
     public Gender getGender() {
         return gender;
     }
 
     public String getInstructorName() {
         return instructorName;
     }
 
     public String getSchoolName() {
         return schoolName;
     }
 
     public String getSchoolAddress() {
         return schoolAddress;
     }
 
     public String getSchoolCity() {
         return schoolCity;
     }
 
     public String getSchoolState() {
         return schoolState;
     }
 
     public String getSchoolZip() {
         return schoolZip;
     }
     
     public String getSchoolPhone() {
         return schoolPhone;
     }
 
     public String getSchoolEmail() {
         return schoolEmail;
     }
 
     public Rank getRank() {
         return rank;
     }
 
     public int getAge() {
         return age;
     }
 
     public int getWeight() {
         return weight;
     }
 
     public boolean isWeapons() {
         return weapons;
     }
 
     public boolean isBreaking() {
         return breaking;
     }
 
     public boolean isSparring() {
         return sparring;
     }
 
     public boolean isPoint() {
         return point;
     }
 
     public boolean isOlympic() {
         return olympic;
     }
 
     public int getNumberOfBoards() {
         return numberOfBoards;
     }
     
     public String[] toCSVLine(){
  
         return new String[]{name,email,address,city,state,zip,phone,gender.toString(),
            instructorName, schoolName, schoolAddress, schoolCity, schoolState, schoolZip, 
             schoolPhone, schoolEmail, rank.toString(), Integer.toString(age), 
             Integer.toString(weight), boolToStringRep(weapons), 
             boolToStringRep(breaking), boolToStringRep(sparring), 
             boolToStringRep(point), boolToStringRep(olympic), 
             Integer.toString(numberOfBoards)};
     }
     
     private String boolToStringRep(boolean bool){
         return bool ? "Yes": "No";
     }
 
 
     
    
     
     
 
 
     
     
     
     
 
     
 }
