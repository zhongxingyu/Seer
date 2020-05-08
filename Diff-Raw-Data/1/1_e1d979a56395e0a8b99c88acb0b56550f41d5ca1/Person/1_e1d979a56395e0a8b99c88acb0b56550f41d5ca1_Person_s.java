 package com.jjw.addressbook.pojo;
 
 public class Person
 {
     private String myName;
     private String myAddress;
     private String myCity;
     private String myState;
     private String myPhoneNumber;
 
     /**
      * Default constructor
      */
     public Person()
     {
     }
 
     /**
      * Constructor used to initialize all values
      * 
      * @param name
      * @param address
      * @param city
      * @param state
      * @param phoneNumber
      */
     public Person(String name, String address, String city, String state, String phoneNumber)
     {
        super();
         myName = name;
         myAddress = address;
         myCity = city;
         myState = state;
         myPhoneNumber = phoneNumber;
     }
 
     /**
      * @return the name
      */
     public String getName()
     {
         return myName;
     }
 
     /**
      * @param name the name to set
      */
     public void setName(String name)
     {
         myName = name;
     }
 
     /**
      * @return the address
      */
     public String getAddress()
     {
         return myAddress;
     }
 
     /**
      * @param address the address to set
      */
     public void setAddress(String address)
     {
         myAddress = address;
     }
 
     /**
      * @return the city
      */
     public String getCity()
     {
         return myCity;
     }
 
     /**
      * @param city the city to set
      */
     public void setCity(String city)
     {
         myCity = city;
     }
 
     /**
      * @return the state
      */
     public String getState()
     {
         return myState;
     }
 
     /**
      * @param state the state to set
      */
     public void setState(String state)
     {
         myState = state;
     }
 
     /**
      * @return the phoneNumber
      */
     public String getPhoneNumber()
     {
         return myPhoneNumber;
     }
 
     /**
      * @param phoneNumber the phoneNumber to set
      */
     public void setPhoneNumber(String phoneNumber)
     {
         myPhoneNumber = phoneNumber;
     }
 
     @Override
     public String toString()
     {
         return "name: " + myName + ", address: " + myAddress + ", city: " + myCity + ", state: " + myState
                 + ", phoneNumber: " + myPhoneNumber;
     }
 }
