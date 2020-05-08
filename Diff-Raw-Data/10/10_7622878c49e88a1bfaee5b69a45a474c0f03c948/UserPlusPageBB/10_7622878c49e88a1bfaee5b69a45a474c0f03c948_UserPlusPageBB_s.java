 package com.mycompany.library.beans;
 
 import com.mycompany.library.core.Book;
 import com.mycompany.library.core.BorrowedItem;
 import com.mycompany.library.core.Creator;
 import com.mycompany.library.core.Item;
 import com.mycompany.library.core.QueryProccessor;
 import com.mycompany.library.core.WebbLib;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import javax.enterprise.context.SessionScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 /**
  * Backing bean for the UserPlusPage.xhtml page.
  * @author Fredrik
  */
 @Named("userPlus")
 @SessionScoped
 public class UserPlusPageBB implements Serializable {
     
     private CreatorBean creatorCollection;
     private ItemBean itemCollection;
     private String id;
     private String title;
     private String creators;
     private String publisher;
     private String image;
     private String description;
     private int pageNum;
     private int loan_period;
     private int fee;
     private int year_release;
     private String genre;
     private String language;
     private int quantity;   
     private String editId;
     private String editTitle;
     private String editCreators;
     private String editPublisher;
     private String editImage;
     private String editDescription;
     private int editPageNum;
     private int editLoan_period;
     private int editFee;
     private int editYear_release;
     private String editGenre;
     private String editLanguage;
     private int editQuantity;
     private List<BorrowedItem> isbnSearchResult;
     
     // Default constructor.
     public UserPlusPageBB() {}
     @Inject
     public UserPlusPageBB(CreatorBean creatorCollection, ItemBean itemCollection) {
         this.creatorCollection = creatorCollection;
         this.itemCollection = itemCollection;
     }
     public List<Item> getAll() {
         return itemCollection.getAll();
     }
     
     public void createItem() {
         List<Creator> cs = new ArrayList<>();
         String[] creatorStrings = this.creators.split(",");
         for (String name: creatorStrings) {
             Creator creator = creatorCollection.getByName(name);
             if(creator == null){
                 creator = new Creator(name);
             }
             cs.add(creator);
         }
         itemCollection.update(new Book(id, title, cs, publisher, language, year_release, pageNum,
                 genre, image, description, quantity, loan_period, fee));
     }
     
     public void prepareEdit(String id, String title, String creators, String publisher,
             String image, String description, int pageNum, int loan_period, int fee,
             int year_released, String genre, String language, int quantity) {
         editId = id;
         editTitle = title;
         editCreators = creators;
         editPublisher = publisher;
         editImage = image;
         editDescription = description;
         editPageNum = pageNum;
         editLoan_period = loan_period;
         editFee = fee;
         editYear_release = year_released;
         editGenre = genre;
         editLanguage = language;
         editQuantity = quantity;
     }
     
     public void updateItem(String id) {
         Book b = (Book) itemCollection.find(id);
         b.setTitle(editTitle);
         List<Creator> cs = new ArrayList<>();
         String[] creatorStrings = this.editCreators.split(",");
         for (String s: creatorStrings) {
             Creator creator = new Creator(s);
             cs.add(creator);
             creatorCollection.update(creator);
         }
         b.setCreators(cs);
         b.setPublisher(editPublisher);
         b.setImage(editImage);
         b.setDescription(editDescription);
         b.setPageNum(editPageNum);
         b.setLoan_period(editLoan_period);
         b.setFee(editFee);
         b.setYear(editYear_release);
         b.setGenre(editGenre);
         b.setLanguage(editLanguage);
         b.setQuantity(editQuantity);
         itemCollection.update(b);
     }
     
     public void removeItem(String id) {
         itemCollection.remove(id);
     }
 
     public String action() {
         return "userPlusPage?faces-redirect=true";
     }
     public List<BorrowedItem> getAllBorrowedItems(){
         QueryProccessor q = WebbLib.INSTANCE.getQueryProccessor();
         return q.getAllBorrowedItems();
     }
     
         
     public List<BorrowedItem> getAllBorrowedItemByISBN(String isbn){
         List<BorrowedItem> tmp = null;        
         QueryProccessor query = WebbLib.INSTANCE.getQueryProccessor();
         for(BorrowedItem b: query.getAllBorrowedItems()){
             
             if(b.getItem().getId().equals(isbn)){
                 tmp.add(b); 
             }
         }
         isbnSearchResult = tmp;
         return tmp;
     }
     
     public void checkCollectDatePassed(){
         List<BorrowedItem> temp = this.getAllBorrowedItems();
         for(int i = 0; i < temp.size(); i++){
             temp.get(i).checkCollectDatePassed();
         }
     }
     
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getCreators() {
         return creators;
     }
 
     public void setCreators(String creators) {
         this.creators = creators;
     }
 
     public String getPublisher() {
         return publisher;
     }
 
     public void setPublisher(String publisher) {
         this.publisher = publisher;
     }
 
     public int getPageNum() {
         return pageNum;
     }
 
     public void setPageNum(int pageNum) {
         this.pageNum = pageNum;
     }
 
     public String getEditPublisher() {
         return editPublisher;
     }
 
     public void setEditPublisher(String editPublisher) {
         this.editPublisher = editPublisher;
     }
 
     public int getEditPageNum() {
         return editPageNum;
     }
 
     public void setEditPageNum(int editPageNum) {
         this.editPageNum = editPageNum;
     }
 
     public String getImage() {
         return image;
     }
 
     public void setImage(String image) {
         this.image = image;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public int getLoan_period() {
         return loan_period;
     }
 
     public void setLoan_period(int loan_period) {
         this.loan_period = loan_period;
     }
 
     public int getFee() {
         return fee;
     }
 
     public void setFee(int fee) {
         this.fee = fee;
     }
 
     public int getYear_release() {
         return year_release;
     }
 
     public void setYear_release(int year_release) {
         this.year_release = year_release;
     }
 
     public String getGenre() {
         return genre;
     }
 
     public void setGenre(String genre) {
         this.genre = genre;
     }
 
     public String getLanguage() {
         return language;
     }
 
     public void setLanguage(String language) {
         this.language = language;
     }
 
     public int getQuantity() {
         return quantity;
     }
 
     public void setQuantity(int quantity) {
         this.quantity = quantity;
     }
 
     public String getEditId() {
         return editId;
     }
 
     public void setEditId(String editId) {
         this.editId = editId;
     }
 
     public String getEditTitle() {
         return editTitle;
     }
 
     public void setEditTitle(String editTitle) {
         this.editTitle = editTitle;
     }
 
     public String getEditCreators() {
         return editCreators;
     }
 
     public void setEditCreators(String editCreators) {
         this.editCreators = editCreators;
     }
 
     public String getEditImage() {
         return editImage;
     }
 
     public void setEditImage(String editImage) {
         this.editImage = editImage;
     }
 
     public String getEditDescription() {
         return editDescription;
     }
 
     public void setEditDescription(String editDescription) {
         this.editDescription = editDescription;
     }
 
     public int getEditLoan_period() {
         return editLoan_period;
     }
 
     public void setEditLoan_period(int editLoan_period) {
         this.editLoan_period = editLoan_period;
     }
 
     public int getEditFee() {
         return editFee;
     }
 
     public void setEditFee(int eidtFee) {
         this.editFee = eidtFee;
     }
 
     public int getEditYear_release() {
         return editYear_release;
     }
 
     public void setEditYear_release(int editYear_release) {
         this.editYear_release = editYear_release;
     }
 
     public String getEditGenre() {
         return editGenre;
     }
 
     public void setEditGenre(String editGenre) {
         this.editGenre = editGenre;
     }
 
     public String getEditLanguage() {
         return editLanguage;
     }
 
     public void setEditLanguage(String editLanguage) {
         this.editLanguage = editLanguage;
     }
 
     public int getEditQuantity() {
         return editQuantity;
     }
 
     public void setEditQuantity(int editQuantity) {
         this.editQuantity = editQuantity;
     }
 
     public List<BorrowedItem> getIsbnSearchResult() {
         return isbnSearchResult;
     }
 
     public void setIsbnSearchResult(List<BorrowedItem> isbnSearchResult) {
         this.isbnSearchResult = isbnSearchResult;
     }
     
     
     
     
 }
