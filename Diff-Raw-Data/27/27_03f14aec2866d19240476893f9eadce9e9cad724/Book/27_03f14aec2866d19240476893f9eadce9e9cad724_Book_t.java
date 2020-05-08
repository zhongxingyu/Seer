 /**AUTHOR: Laurence Toal
   *CLASS: CS 230 Data Structures
   *Final Project Phase 2 - Class outlines
   *LAST MODIFIED: 22 November 2011
 **/
 
 public class Book {
  
     //instance variables
     private String title;
     private String author;
     private String yearPublished;
     private String branchLocation;
     private String callNumber;
     private String barcodeNumber;
     private String status;
     private boolean isOverdue;
     private double overdueFee;
  
     /**Constructs a book object given a title, author, year published,
       *location, call number, barcode number, and status
     **/
     public Book(String title, String author, String yearPublished, String branchLocation, String callNumber, 
                 String barcodeNumber, String status) {
        this.title = title;
        this.author = author; 
        this.yearPublished = yearPublished;
        this.branchLocation = branchLocation;
        this.callNumber = callNumber;
        this.barcodeNumber = barcodeNumber;
        this.status = status;
        this.isOverdue = false;
        this.overdueFee = 0.0;
     }
  
     //getters
  
     /**Returns the title of the book
       *
       *@return title a string that is the book's title
     **/
     public String getTitle() {
         return title;
     }
  
     /**Returns the author of the book
       *
       *@return author a string that is the book's author
     **/
     public String getAuthor() {
         return author;
     }
  
     /**Returns the publication year of the book
       *
       *@return yearPublished a string that is the book's publication year
     **/
     public String getYearPublished() {
         return yearPublished;
     }
  
     /**Returns the branch location of the book
       *
       *@return branchLocation a string that is the book's location
     **/
     public String getBranchLocation() {
         return branchLocation;
     }
  
     /**Returns the call number of the book
       *
       *@return callNumber a string that is the book's call number
     **/
     public String getCallNumber() {
         return callNumber;
     }
  
     /**Returns the barcode number of the book
       *
       *@return barcodeNumber a string that is the book's barcode number
     **/
     public String getBarcodeNumber() {
         return barcodeNumber;
     }
  
     /**Returns the status of the book
       *
       *@return status a string that is the book's status
     **/
     public String getStatus() {
         return status;
     }
  
  
     //setters
  
     /**Changes the location of a book
       *
       *@param newLocation the book's new location
     **/
     public void setBranchLocation(String newLocation) {
         this.branchLocation = newLocation;
     }
  
     /**Changes the status of a book
       *Can be "checked out", "available", "requested" or "in transit"
       *
       *@param newStatus the new status of the book
     **/
     public void setStatus(String newStatus) {
         this.status = newStatus;
     } 
     
     /**Marks a book as overdue by a number of days
       * 
       *@param days an int representing how many days overdue the book is
       *@return
     **/
     public void setOverdue(int days) {
         this.isOverdue = true;
     }
 
     /**Calculates the overdue fee on a book
       *For simplicity's sake, assumes the fine is 50 cents
       *no matter what book is overdue
       *
       *@param daysOverdue an int representing how many days overdue the book is
       *@return overdueFee how much money is owed on the book
     **/
     public double calculateFee(int daysOverdue) {
         this.overdueFee = daysOverdue*0.50;
         return overdueFee;
     }

    /**Returns a string representation of the book
      *
      *@return a string represenaiton of the book
    **/
    public String toString() {
        return "Title: " + title + "\nAuthor: " + author + "\nYear Published: " + yearPublished + "\nLibrary: " + branchLocation + "\nCall Number: " + callNumber + "\tBarcode: " + barcodeNumber + "\nOverdue? " + isOverdue + "\tFine: " + overdueFee; 
    }
  
 }
