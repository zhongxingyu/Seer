 package org.sukrupa.student;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 
 import java.util.List;
 
 public class StudentListPage {
     private final List<Student> students;
     private final int pageNumber;
     private final int maxPageNumber;
     private final String queryString;
 
     public StudentListPage(List<Student> students, int pageNumber, int maxPageNumber, String queryString) {
         this.students = students;
         this.pageNumber = pageNumber;
         this.maxPageNumber = maxPageNumber;
         this.queryString = queryString;
     }
 
     public List<Student> getStudents() {
         return students;
     }
 
     public boolean equals(Object other) {
         return EqualsBuilder.reflectionEquals(this, other);
     }
 
     public int hashCode() {
         return HashCodeBuilder.reflectionHashCode(this);
     }
 
     public String toString() {
         return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
     }
 
     public int getPageNumber() {
         return pageNumber;
     }
 
     public boolean isPreviousEnabled() {
         return pageNumber > 1;
     }
 
     public int getNextPage() {
         return pageNumber + 1;
     }
 
     public int getPreviousPage() {
         return pageNumber - 1;
     }
 
     public boolean isNextEnabled() {
         return pageNumber < maxPageNumber;
     }
 
     public String getNextPageUrl() {
        if (queryString==null || queryString.isEmpty()) {
             return "?page=" + getNextPage();
         }
         if (queryString.indexOf("page=") != -1) {
             return "?" + queryString.replaceFirst("page=[0-9]*","page=" + getNextPage());
         }
         return "?" + queryString + "&page=" + getNextPage();
       // return "&page=2";
     }

 }
