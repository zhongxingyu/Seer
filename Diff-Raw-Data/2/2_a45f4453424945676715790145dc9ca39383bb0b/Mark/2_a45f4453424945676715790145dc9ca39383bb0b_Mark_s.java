 package examtool.model;
 
 import org.apache.commons.lang3.Validate;
 import org.apache.commons.lang3.builder.EqualsBuilder;
 import org.apache.commons.lang3.builder.HashCodeBuilder;
 
 /**
 * Author: Yury Chuyko
 * Date: 24.06.13
 */
 public final class Mark {
     private final int score;
 
     public int getScore() {
         return score;
     }
 
     public Mark(final int score) {
         Validate.inclusiveBetween(0, 20, score, "invalid score: " + score);
         this.score = score;
     }
 
     @Override
     public boolean equals(final Object o) {
        return EqualsBuilder.reflectionEquals(this, 0);
     }
 
     @Override
     public int hashCode() {
         return HashCodeBuilder.reflectionHashCode(this);
     }
 }
