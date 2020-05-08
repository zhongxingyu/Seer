 package nl.codebasesoftware.produx.search.result;
 
 import com.google.common.base.Joiner;
 import nl.codebasesoftware.produx.domain.dto.listing.ListingCourseDTO;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * User: rvanloen
  * Date: 13-8-13
  * Time: 23:47
  */
 public class SearchResult {
 
     private Collection<ListingCourseDTO> courses = new ArrayList<>();
     private Collection<FacetFieldView> facetFieldViews = new ArrayList<>();
     private long totalFound;
     private Integer resultsPerPage;
     private List<String> baseDirs;
 
     private SearchResult(Builder builder) {
         courses = builder.courses;
         facetFieldViews = builder.facetFieldViews;
         totalFound = builder.totalFound;
         resultsPerPage = builder.resultsPerPage;
         baseDirs = builder.baseDirs;
     }
 
     public Collection<ListingCourseDTO> getCourses() {
         return courses;
     }
 
     public Collection<FacetFieldView> getFacetFieldViews() {
         return facetFieldViews;
     }
 
     public long getTotalFound() {
         return totalFound;
     }
 
     public Integer getResultsPerPage() {
         return resultsPerPage;
     }
 
     public int getNumberOfResultPages() {
         double l = (double) totalFound / resultsPerPage;
        double pages = Math.ceil(l - 1) + 1;
         return (int) pages;
     }
 
     public String getBasePath() {
         return Joiner.on("/").join(baseDirs);
     }
 
     public static class Builder {
 
         private Collection<ListingCourseDTO> courses = new ArrayList<>();
         private Collection<FacetFieldView> facetFieldViews = new ArrayList<>();
         private long totalFound;
         private Integer resultsPerPage;
         private List<String> baseDirs;
 
         public Builder addCourse(ListingCourseDTO course) {
             this.courses.add(course);
             return this;
         }
 
         public Builder addFacetField(FacetFieldView facetFieldView) {
             this.facetFieldViews.add(facetFieldView);
             return this;
         }
 
         public Builder setBaseDirs(List<String> baseDirs) {
             this.baseDirs = baseDirs;
             return this;
         }
 
         public SearchResult build() {
             return new SearchResult(this);
         }
 
         public Builder setTotalFound(long totalFound) {
             this.totalFound = totalFound;
             return this;
         }
 
         public Builder setResultsPerPage(int resultsPerPage) {
             this.resultsPerPage = resultsPerPage;
             return this;
         }
     }
 }
