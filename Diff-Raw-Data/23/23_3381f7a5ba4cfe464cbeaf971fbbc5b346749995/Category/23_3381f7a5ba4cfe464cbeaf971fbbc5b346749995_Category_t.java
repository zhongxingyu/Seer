 package com.tools.tvguide.data;
 
 import java.io.Serializable;
 import java.util.List;
 
 public class Category implements Serializable 
 {
     private static final long serialVersionUID = 1L;
     
     public String name = "";
    public String tvmaoId = "";
     public String link = "";
     
     public enum Next { CategoryList, ChannelList, None };
     public Next next = Next.None;
     public List<Category> categoryList;
     public List<Channel> channelList;
 }
