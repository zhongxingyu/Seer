 /*
  * Copyright 2010-2012 Davincium Ltd 
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  * 
  *  http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  * 
  */
 
 package org.linkedin.schema;
 
 import com.google.gson.Gson;
 
 public class Person    
 {
 	public String id;
 	public String firstName;
 	public String lastName;
 	public String headline;
 	public String industry;
 	public String status;
 	public Long distance;
 	public Long currentStatusTimestamp;
 	public Long numRecommenders;
 	public Long numConnections;
 	public Boolean isNumConnectionsCapped;
 	public String summary;
 	public String publicProfileUrl;
 	public String interests;
 	public String associations;
 	public String honors;
 	public String specialties;
 	public String pictureUrl;
 	public PictureUrls pictureUrls;
 	public String mainAddress;
 	public String path;
 	public Connections connections;
 	public PhoneNumbers phoneNumbers;
 	public DateOfBirth dateOfBirth;
 	public Location location;
 	public CurrentShare currentShare;
 	public RelationToViewer relationToViewer;
 	public Certifications certifications;
 	public Patents patents;
 	public Publications publications;
 	public Skills skills;
 	public Languages languages;
 	public Positions positions;
 	public ThreeCurrentPositions threeCurrentPositions;
 	public ThreePastPositions threePastPositions;
 	public Educations educations;
 	public MemberUrlResources memberUrlResources;
 	public ApiStandardProfileRequest apiStandardProfileRequest;
 	public SiteStandardProfileRequest siteStandardProfileRequest;
 	public RecommendationsGiven recommendationsGiven;
 	public RecommendationsReceived recommendationsReceived;
 	public MemberGroups memberGroups;
 	public PersonActivities personActivities;
 	public ImAccounts imAccounts;
 	public TwitterAccounts twitterAccounts;
 	public TwitterAccount primaryTwitterAccount;
 	
     /**
      * Gets the value of the id property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
 	public String getId()
     {
     	return id;
     }
 
     /**
      * Gets the value of the firstName property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
 	public String getFirstName()
     {
     	return firstName;
     }
 
     /**
      * Gets the value of the lastName property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
 	public String getLastName()
     {
     	return lastName;
     }
 
     /**
      * Gets the value of the headline property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
 	public String getHeadline()
     {
     	return headline;
     }
 
     /**
      * Gets the value of the location property.
      * 
      * @return
      *     possible object is
      *     {@link Location }
      *     
      */
 	public Location getLocation()
     {
     	return location;
     }
 
     /**
      * Gets the value of the industry property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
 	public String getIndustry()
     {
     	return industry;
     }
 
     /**
      * Gets the value of the connections property.
      * 
      * @return
      *     possible object is
      *     {@link Connections }
      *     
      */
 	public Connections getConnections()
     {
     	return connections;
     }
 
     /**
      * Gets the value of the currentStatus property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
 	public String getCurrentStatus()
     {
     	return status;
     }
 
     /**
      * Gets the value of the currentShare property.
      * 
      * @return
      *     possible object is
      *     {@link CurrentShare }
      *     
      */
 	public CurrentShare getCurrentShare()
     {
     	return currentShare;
     }
 
     /**
      * Gets the value of the distance property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
 	public Long getDistance()
     {
     	return distance;
     }
 
     /**
      * Gets the value of the currentStatusTimestamp property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
 	public Long getCurrentStatusTimestamp()
     {
     	return currentStatusTimestamp;
     }
 
     /**
      * Gets the value of the numRecommenders property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
 	public Long getNumRecommenders()
     {
     	return numRecommenders;
     }
 
     /**
      * Gets the value of the numConnections property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
 	public Long getNumConnections()
     {
     	return numConnections;
     }
 
     /**
      * Gets the value of the numConnectionsCapped property.
      * 
      * @return
      *     possible object is
      *     {@link Boolean }
      *     
      */
 	public Boolean isNumConnectionsCapped()
     {
     	return isNumConnectionsCapped;
     }
 
     /**
      * Gets the value of the relationToViewer property.
      * 
      * @return
      *     possible object is
      *     {@link RelationToViewer }
      *     
      */
 	public RelationToViewer getRelationToViewer()
     {
     	return relationToViewer;
     }
 
     /**
      * Gets the value of the summary property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
 	public String getSummary()
     {
     	return summary;
     }
 
     /**
      * Gets the value of the publicProfileUrl property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
 	public String getPublicProfileUrl()
     {
     	return publicProfileUrl;
     }
 
     /**
      * Gets the value of the interests property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
 	public String getInterests()
     {
     	return interests;
     }
 
     /**
      * Gets the value of the associations property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
 	public String getAssociations()
     {
     	return associations;
     }
 
     /**
      * Gets the value of the honors property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
 	public String getHonors()
     {
     	return honors;
     }
 
     /**
      * Gets the value of the specialties property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
 	public String getSpecialties()
     {
     	return specialties;
     }
 
     /**
      * Gets the value of the certifications property.
      * 
      * @return
      *     possible object is
      *     {@link Certifications }
      *     
      */
 	public Certifications getCertifications()
     {
     	return certifications;
     }
 
     /**
      * Gets the value of the patents property.
      * 
      * @return
      *     possible object is
      *     {@link Patents }
      *     
      */
 	public Patents getPatents()
     {
     	return patents;    	
     }
 
     /**
      * Gets the value of the publications property.
      * 
      * @return
      *     possible object is
      *     {@link Publications }
      *     
      */
 	public Publications getPublications()
     {
     	return publications;
     }
 
     /**
      * Gets the value of the skills property.
      * 
      * @return
      *     possible object is
      *     {@link Skills }
      *     
      */
 	public Skills getSkills()
     {
     	return skills;
     }
 
     /**
      * Gets the value of the languages property.
      * 
      * @return
      *     possible object is
      *     {@link Languages }
      *     
      */
 	public Languages getLanguages()
     {
     	return languages;
     }
 
     /**
      * Gets the value of the positions property.
      * 
      * @return
      *     possible object is
      *     {@link Positions }
      *     
      */
 	public Positions getPositions()
     {
     	return positions;
     }
 
     /**
      * Gets the value of the threeCurrentPositions property.
      * 
      * @return
      *     possible object is
      *     {@link ThreeCurrentPositions }
      *     
      */
 	public ThreeCurrentPositions getThreeCurrentPositions()
     {
     	return threeCurrentPositions;
     }
 
     /**
      * Gets the value of the threePastPositions property.
      * 
      * @return
      *     possible object is
      *     {@link ThreePastPositions }
      *     
      */
 	public ThreePastPositions getThreePastPositions()
     {
     	return threePastPositions;
     }
 
     /**
      * Gets the value of the educations property.
      * 
      * @return
      *     possible object is
      *     {@link Educations }
      *     
      */
 	public Educations getEducations()
     {
     	return educations;
     }
 
     /**
      * Gets the value of the memberUrlResources property.
      * 
      * @return
      *     possible object is
      *     {@link MemberUrlResources }
      *     
      */
 	public MemberUrlResources getMemberUrlResources()
     {
     	return memberUrlResources;
     }
 
     /**
      * Gets the value of the apiStandardProfileRequest property.
      * 
      * @return
      *     possible object is
      *     {@link ApiStandardProfileRequest }
      *     
      */
 	public ApiStandardProfileRequest getApiStandardProfileRequest()
     {
     	return apiStandardProfileRequest;
     }
 
     /**
      * Gets the value of the siteStandardProfileRequest property.
      * 
      * @return
      *     possible object is
      *     {@link SiteStandardProfileRequest }
      *     
      */
 	public SiteStandardProfileRequest getSiteStandardProfileRequest()
     {
     	return siteStandardProfileRequest;
     }
 
     /**
      * Gets the value of the pictureUrl property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
 	public String getPictureUrl()
     {
    	if (pictureUrls!=null && pictureUrls.total>0 || pictureUrls.values.size()>0)
     		return pictureUrls.values.get(0);
     	return pictureUrl;
     }
 
     /**
      * Gets the value of the pictureUrls property.
      * 
      * @return
      *     possible object is
      *     {@link PictureUrls }
      *     
      */
 	public PictureUrls getPictureUrls()
     {
     	return pictureUrls;
     }
     
     /**
      * Gets the value of the recommendationsGiven property.
      * 
      * @return
      *     possible object is
      *     {@link RecommendationsGiven }
      *     
      */
 	public RecommendationsGiven getRecommendationsGiven()
     {
     	return recommendationsGiven;
     }
 
     /**
      * Gets the value of the recommendationsReceived property.
      * 
      * @return
      *     possible object is
      *     {@link RecommendationsReceived }
      *     
      */
 	public RecommendationsReceived getRecommendationsReceived()
     {
     	return recommendationsReceived;
     }
 
     /**
      * Gets the value of the memberGroups property.
      * 
      * @return
      *     possible object is
      *     {@link MemberGroups }
      *     
      */
 	public MemberGroups getMemberGroups()
     {
     	return memberGroups;
     }
 
     /**
      * Gets the value of the personActivities property.
      * 
      * @return
      *     possible object is
      *     {@link PersonActivities }
      *     
      */
 	public PersonActivities getPersonActivities()
     {
     	return personActivities;
     }
 
     /**
      * Gets the value of the imAccounts property.
      * 
      * @return
      *     possible object is
      *     {@link ImAccounts }
      *     
      */
 	public ImAccounts getImAccounts()
     {
     	return imAccounts;
     }
 
     /**
      * Gets the value of the twitterAccounts property.
      * 
      * @return
      *     possible object is
      *     {@link TwitterAccounts }
      *     
      */
 	public TwitterAccounts getTwitterAccounts()
     {
     	return twitterAccounts;
     }
 
     /**
      * Gets the value of the dateOfBirth property.
      * 
      * @return
      *     possible object is
      *     {@link DateOfBirth }
      *     
      */
 	public DateOfBirth getDateOfBirth()
     {
     	return dateOfBirth;
     }
 
     /**
      * Sets the value of the dateOfBirth property.
      * 
      * @param value
      *     allowed object is
      *     {@link DateOfBirth }
      *     
      */
 	public void setDateOfBirth(DateOfBirth value)
     {
     	dateOfBirth = value;
     }
 
     /**
      * Gets the value of the mainAddress property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
 	public String getMainAddress()
     {
     	return mainAddress;
     }
 
     /**
      * Sets the value of the mainAddress property.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
 	public void setMainAddress(String value)
     {
     	mainAddress = value;
     }
 
     /**
      * Gets the value of the phoneNumbers property.
      * 
      * @return
      *     possible object is
      *     {@link PhoneNumbers }
      *     
      */
 	public PhoneNumbers getPhoneNumbers()
     {
     	return phoneNumbers;
     }
 
     /**
      * Sets the value of the phoneNumbers property.
      * 
      * @param value
      *     allowed object is
      *     {@link PhoneNumbers }
      *     
      */
 	public void setPhoneNumbers(PhoneNumbers value)
     {
     	phoneNumbers = value;
     }
 
     /**
      * Gets the value of the path property.
      * 
      * @return
      *     possible object is
      *     {@link String }
      *     
      */
 	public String getPath()
     {
     	return path;
     }
 
     /**
      * Sets the value of the path property.
      * 
      * @param value
      *     allowed object is
      *     {@link String }
      *     
      */
 	public void setPath(String value)
     {
     	path = value;
     }
 
     
     // Parsing
 	public static Person fromJson(String json,Gson gson_) 
 	{
 		Gson gson = gson_;
 		if (gson==null)
 			gson = new Gson();
 		return gson.fromJson(json,Person.class);		
 	}
 	
 	public static Person fromJson(String json)
 	{
 		return fromJson(json,null);
 	}
 }
