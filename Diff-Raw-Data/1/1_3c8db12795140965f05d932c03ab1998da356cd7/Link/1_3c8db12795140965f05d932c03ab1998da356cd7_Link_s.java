 
 public class Link {
     public Word otherWord;
     public long rating;
     public long highestRating;
 
     public Link(Word otherWord) {
         this.otherWord = otherWord;
         this.rating = 1;
     }
 
     public void plusRate() {
         this.rating++;
         if (this.rating > this.highestRating) {
             this.highestRating = this.rating;
         }
     }
     
     public void downRate() {
         this.rating -= (int) ((Math.random() * this.rating) + 1);
     }
     
     public void restoreRate() {
         this.rating += (int) (Math.random() * (this.highestRating - this.rating));
     }
 
     @Override
     public String toString() {
         return this.otherWord.toString() + " : " + this.rating;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Link link = (Link) o;

         return otherWord.toString().equals(((Link) link).otherWord.toString());
     }
 
     @Override
     public int hashCode() {
         int result = otherWord != null ? otherWord.hashCode() : 0;
         result = 31 * result + (int) (rating ^ (rating >>> 32));
         return result;
     }
 }
