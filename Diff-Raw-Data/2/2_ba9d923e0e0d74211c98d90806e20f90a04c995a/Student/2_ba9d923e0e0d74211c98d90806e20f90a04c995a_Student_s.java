 package models;
 
 import java.util.List;
 
 /**
  * Represents a Tutor.me student
  */
 public class Student extends User {
 
   private static final String requestSelfFieldName = "requestingStudent";
   private static final String sessionSelfFieldName = "student";
 
   private static final long serialVersionUID = -6347603101193360297L;
 
   public static Finder<Long, Student> find = new Finder<Long, Student>(
       Long.class, Student.class);
 
   public static List<Student> all() {
     return find.all();
   }
 
   public static void create(Student student) {
     student.save();
   }
 
   public static void delete(Long id) {
     find.ref(id).delete();
   }
 
   /**
    * Creates a new tutoring session request
    * 
    * @param tutor: The tutor that is being request to hold the session
    * @param startTime: The start time of the tutoring session
    * @param endTime: The end time of the tutoring session
    * @return: The request that was created
    */
   public Request createRequest(Tutor tutor, long startTime, long endTime) {
     Request request = new Request(this, tutor, startTime, endTime);
     request.notifyTutor(true);
     return request;
   }
 
   /**
    * Cancels a tutoring session request
    * 
    * @param request: The request to be canceled
    */
   public void cancelRequest(Request request) {
     request.notifyTutor(false);
     request.delete();
   }
   
    /** 
    * Finds all tutors that teach the specified subject
    * 
    * @param subject: The subject the student is searching for a tutor for
    * 
    * @return: The tutors who teach the given subject
    */
   public List<Tutor> searchForTutors(String subject) {
     return searchForTutors(subject, 0, Double.MaxValue);
   }
   
    /** 
    * Finds all tutors that teach the specified subject
    * 
    * @param subject: The subject the student is searching for a tutor for
    * @param minCost: The minimum cost a tutor can have
    * @param maxCost: The maximum cose a tutor can have
    * 
    * @return: The tutors who teach the given subject, ordered by their cost
    */
   public List<Tutor> searchForTutors(String subject, double minCost, double maxCost) {
     searchForTutors(subject, minCost, maxCost, Double.MaxValue);
   }
   
   /** 
    * Finds all tutors that teach the specified subject
    * 
    * @param subject: The subject the student is searching for a tutor for
    * @param minCost: The minimum cost a tutor can have
    * @param maxCost: The maximum cose a tutor can have
    * @param minRating: The minimum rating a tutor can have
    * 
    * @return: The tutors who teach the given subject, ordered by their ratings
    */
   public List<Tutor> searchForTutors(String subject, double minCost, double maxCost, double minRating) {
     return Tutor.find.where()
       .contains("subjects", subject)
       .gte("costUSD", minCost)
       .lte("costUSD", maxCost)
       .gte("rating", minRating)
       .orderBy("rating");
   }
 
   /**
   * Rates a tutor on their performance s
    * 
    * @param tutor: The tutor to rate
    * @param rating: The rating to give the tutor
    */
   public static void rateTutor(Tutor tutor, int rating) {
     int numRaters = tutor.getNumRaters();
     int updateNumRaters = numRaters + 1;
     double ratingAggregate = tutor.getRating() * numRaters;
     tutor.setRating((ratingAggregate + rating) / updateNumRaters);
     tutor.setNumRaters(updateNumRaters);
   }
 }
