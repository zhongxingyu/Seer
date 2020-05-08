 package gr.dsigned.springcrudutils;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author nk
  */
 public class Pager {
 
     String baseURL;
     int totalItemCount;
     int currentPage;
     int perPage;
     private List<String> urls;
 
     public Pager(String baseURL, int totalItemCount) {
         this.baseURL = baseURL;
         this.totalItemCount = totalItemCount;
         this.currentPage = 0;
         this.perPage = 10;
         init();
     }
 
     public Pager(String baseURL, int totalItemCount, int currentPage) {
         this.baseURL = baseURL;
         this.totalItemCount = totalItemCount;
         this.currentPage = currentPage;
         this.perPage = 10;
         init();
     }
 
     public Pager(String baseURL, int totalItemCount, int currentPage, int perPage) {
         this.baseURL = baseURL;
         this.totalItemCount = totalItemCount;
         this.currentPage = currentPage;
         this.perPage = perPage;
         init();
     }
 
     private void init() {
         List<String> list = new ArrayList<String>();
         for (int i = 0; i < getTotalPageNumber(); i++) {
             if (i != 0) {
                 String url = getBaseURL() + "?page=" + i + "&sizeNo=" + perPage;
                 list.add(url);
             } else {
                 String url = getBaseURL() + "?sizeNo=" + perPage;
                 list.add(url);
             }
         }
         urls = ImmutableList.copyOf(list);
     }
 
     public String getFirstPage() {
         return baseURL + "?sizeNo=" + perPage;
     }
 
     public String getNextPage(){
        if(currentPage + 1 > urls.size()){
             return getLastPage();
         } else {
             return urls.get(currentPage+1);
         }
     }
     public String getPreviousPage(){
         if(currentPage-1 > urls.size()){
             return getFirstPage();
         } else {
             return urls.get(currentPage-1);
         }
     }
 
     public String getLastPage() {
         return baseURL + "?page=" + getTotalPageNumber() + "&sizeNo=" + perPage;
     }
 
     public int getTotalPageNumber() {
         return (int) Math.ceil(totalItemCount / (float) perPage);
     }
 
     public String getBaseURL() {
         return baseURL;
     }
 
 
 
     public int getCurrentPage() {
         return currentPage;
     }
 
     public void setCurrentPage(int currentPage) {
         Preconditions.checkPositionIndex(currentPage, getTotalPageNumber(), "Page number out of bounds.");
         this.currentPage = currentPage;
     }
 
     public int getPerPage() {
         return perPage;
     }
 
 
     public int getTotalItemCount() {
         return totalItemCount;
     }
 
 
     public List<String> getUrls() {
         return urls;
     }
 
     @Override
     public String toString() {
         return "Pager{" + "baseURL=" + baseURL + ", totalItemCount=" + totalItemCount + ", currentPage=" + currentPage + ", perPage=" + perPage + ", urls=" + urls + '}';
     }
 
 }
