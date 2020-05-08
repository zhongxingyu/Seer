 package models.domain;
 
 import com.google.code.morphia.annotations.Entity;
 import com.google.common.base.Function;
 import com.google.common.collect.ComparisonChain;
 import play.modules.morphia.Model;
 
 
 import javax.annotation.Nullable;
 import java.util.Date;
 
 /**
  * .
  *
  * @author fsznajderman
  *         date :  05/05/12
  */
 @Entity
 public class Statistiques extends Model {
 
     private String bestNamePost;
     private String bestTitlePost;
     private String bestGoogleID;
     private String plusOneMatrix;
     private String sharedMatrix;
     private String titleMatrix;
     private Long sumPlusOne;
     private Long sumShared;
     private Date statisticDate;
     private boolean current = false;
 
 
     public String getBestNamePost() {
         return bestNamePost;
     }
 
     public void setBestNamePost(String bestNamePost) {
         this.bestNamePost = bestNamePost;
     }
 
     public String getBestTitlePost() {
         return bestTitlePost;
     }
 
     public void setBestTitlePost(String bestTitlePost) {
         this.bestTitlePost = bestTitlePost;
     }
 
     public String getPlusOneMatrix() {
         return plusOneMatrix;
     }
 
     public void setPlusOneMatrix(String plusOneMatrix) {
         this.plusOneMatrix = plusOneMatrix;
     }
 
     public String getSharedMatrix() {
         return sharedMatrix;
     }
 
     public void setSharedMatrix(String sharedMatrix) {
         this.sharedMatrix = sharedMatrix;
     }
 
     public Date getStatisticDate() {
         return statisticDate;
     }
 
     public void setStatisticDate(Date statisticDate) {
         this.statisticDate = statisticDate;
     }
 
     public Long getSumPlusOne() {
         return sumPlusOne;
     }
 
     public void setSumPlusOne(Long sumPlusOne) {
         this.sumPlusOne = sumPlusOne;
     }
 
     public Long getSumShared() {
         return sumShared;
     }
 
     public void setSumShared(Long sumShared) {
         this.sumShared = sumShared;
     }
 
     public String getTitleMatrix() {
         return titleMatrix;
     }
 
     public void setTitleMatrix(String titleMatrix) {
         this.titleMatrix = titleMatrix;
     }
 
     public String getBestGoogleID() {
         return bestGoogleID;
     }
 
     public void setBestGoogleID(String bestGoogleID) {
         this.bestGoogleID = bestGoogleID;
     }
 
     public boolean isCurrent() {
         return current;
     }
 
     public void setCurrent(boolean current) {
         this.current = current;
     }
 
     @Override
     public String toString() {
         final StringBuilder sb = new StringBuilder();
         sb.append("Statistiques");
         sb.append("{bestNamePost='").append(bestNamePost).append('\'');
         sb.append(", bestTitlePost='").append(bestTitlePost).append('\'');
         sb.append(", plusOneMatrix='").append(plusOneMatrix).append('\'');
         sb.append(", sharedMatrix='").append(sharedMatrix).append('\'');
         sb.append(", titleMatrix='").append(titleMatrix).append('\'');
         sb.append(", sumPlusOne=").append(sumPlusOne);
         sb.append(", sumShared=").append(sumShared);
         sb.append(", statisticDate=").append(statisticDate);
         sb.append('}');
         return sb.toString();
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o){ return true;}
         if (o == null || getClass() != o.getClass()){ return false; }
 
 
         Statistiques that = (Statistiques) o;
 
 
        return ComparisonChain.start()
                 .compare(bestNamePost, that.bestNamePost)
                 .compare(bestTitlePost, that.bestTitlePost)
                 .compare(plusOneMatrix, that.plusOneMatrix)
                 .compare(sharedMatrix, that.sharedMatrix)
                 .compare(sumPlusOne, that.sumPlusOne)
                 .compare(this.sumShared, that.sumShared)
                .result() == 0;
 
 
 
     }
 
     @Override
     public int hashCode() {
         int result = super.hashCode();
         result = 31 * result + (bestNamePost != null ? bestNamePost.hashCode() : 0);
         result = 31 * result + (bestTitlePost != null ? bestTitlePost.hashCode() : 0);
         result = 31 * result + (bestGoogleID != null ? bestGoogleID.hashCode() : 0);
         result = 31 * result + (plusOneMatrix != null ? plusOneMatrix.hashCode() : 0);
         result = 31 * result + (sharedMatrix != null ? sharedMatrix.hashCode() : 0);
         result = 31 * result + (titleMatrix != null ? titleMatrix.hashCode() : 0);
         result = 31 * result + (sumPlusOne != null ? sumPlusOne.hashCode() : 0);
         result = 31 * result + (sumShared != null ? sumShared.hashCode() : 0);
         return result;
     }
 }
