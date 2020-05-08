 package com.ft.hack.dynamite.model;
 
 /**
  * User: anuragkapur
  * Date: 20/05/2013 10:43
  */
 
 public class Recommendation implements Comparable<Recommendation> {
 
     private String articleTitle;
     private String articleUrl;
     private String publishedDate;
    private int count;
 
     public int getCount() {
         return count;
     }
 
     public void setCount(int count) {
         this.count = count;
     }
 
     public String getArticleTitle() {
         return articleTitle;
     }
 
     public void setArticleTitle(String articleTitle) {
         this.articleTitle = articleTitle;
     }
 
     public String getArticleUrl() {
         return articleUrl;
     }
 
     public void setArticleUrl(String articleUrl) {
         this.articleUrl = articleUrl;
     }
 
     public String getPublishedDate() {
         return publishedDate;
     }
 
     public void setPublishedDate(String publishedDate) {
         this.publishedDate = publishedDate;
     }
 
     @Override
     public int compareTo(Recommendation recommendation) {
         int compareCount = ((Recommendation) recommendation).getCount();
 
         //descending order
         return compareCount - this.count;
     }
 }
