 
 /**
  * Wraps the (document, score) pair as a result of a query
  * @author mircea
  *
  */
 public class QueryResult implements Comparable<QueryResult> {
     
     private String docId;
     private double score;
     
     public QueryResult(String docId, double score) {
         this.docId = docId;
         this.score = score;
     }
     
     public String getDocId() {
         return docId;
     }
 
     public void setDocId(String docId) {
         this.docId = docId;
     }
 
     public double getScore() {
         return score;
     }
 
     public void setScore(double score) {
         this.score = score;
     }
 
     @Override
     public int compareTo(QueryResult result) {
         if (score > result.score) {
             return -1;
         }
         else {
             return 1;
         }
     }
     
     @Override
     public boolean equals(Object entry) {
         if (!entry.getClass().equals(QueryResult.class))
             return false;
         
         return docId.equals(((QueryResult) entry).getDocId());
     }
     
     public String toString() {
         return "(" + docId + ", " + score + ")";
     }
 
 }
