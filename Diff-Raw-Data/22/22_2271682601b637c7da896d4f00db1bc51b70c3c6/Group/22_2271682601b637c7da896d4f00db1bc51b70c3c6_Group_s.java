 /*
  * Copyright (c) 2005 Aetrion LLC.
  */
 package com.aetrion.flickr.groups;
 import com.aetrion.flickr.Flickr;
 
 /**
  * Class representing a Flickr Group.
  *
  * @author Anthony Eden
  */
 public class Group {
 
     private String id;
     private String name;
     private int members;
     private int online;
     private String chatId;
     private int inChat;
     private String privacy;
     private boolean admin;
     private int photoCount;
     private boolean eighteenPlus;
 
     public Group() {
 
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public int getMembers() {
         return members;
     }
 
     public void setMembers(int members) {
         this.members = members;
     }
 
     public void setMembers(String members) {
        if (members != null) setMembers(Integer.parseInt(members));
     }
 
     public int getOnline() {
         return online;
     }
 
     public void setOnline(int online) {
         this.online = online;
     }
 
     public void setOnline(String online) {
        if (online != null) setOnline(Integer.parseInt(online));
     }
 
     public String getChatId() {
         return chatId;
     }
 
     public void setChatId(String chatId) {
         this.chatId = chatId;
     }
 
     public int getInChat() {
         return inChat;
     }
 
     public void setInChat(int inChat) {
         this.inChat = inChat;
     }
 
     public void setInChat(String inChat) {
     	try {
     		if (inChat != null) setInChat(Integer.parseInt(inChat));
     	} catch (NumberFormatException nfe) {
     		setInChat(0);
     		if (Flickr.tracing) 
     			System.out.println("trace: Group.setInChat(String) encountered a number format " + 
    			"exception.  PhotoCount set to 0");
     	}
     }
 
     public String getPrivacy() {
         return privacy;
     }
 
     public void setPrivacy(String privacy) {
         this.privacy = privacy;
     }
 
     public boolean isAdmin() {
         return admin;
     }
 
     public void setAdmin(boolean admin) {
         this.admin = admin;
     }
 
     public int getPhotoCount() {
         return photoCount;
     }
 
     public void setPhotoCount(int photoCount) {
         this.photoCount = photoCount;
     }
 
     public void setPhotoCount(String photoCount) {
         if (photoCount != null) {
         	try {
         		setPhotoCount(Integer.parseInt(photoCount));
         	} catch (NumberFormatException nfe) {
         		setPhotoCount(0);
         		if (Flickr.tracing) 
         			System.out.println("trace: Group.setPhotoCount(String) encountered a number format " + 
         								"exception.  PhotoCount set to 0");
         	}
         }
     }
 
     public boolean isEighteenPlus() {
         return eighteenPlus;
     }
 
     public void setEighteenPlus(boolean eighteenPlus) {
         this.eighteenPlus = eighteenPlus;
     }
 
 }
