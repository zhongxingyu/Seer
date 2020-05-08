 package com.pathfindersdk.books;
 
 import com.pathfindersdk.enums.BookSectionType;
 
 public class BookSection extends Book
 {
   protected BookSectionType type;
   
   public BookSection(BookSectionType type)
   {
     super(type.toString());
     this.type = type;
   }
   
   public BookSectionType getType()
   {
     return type;
   }
   
   @Override
  public void index()
  {
    Index.getInstance().addIndex(this);
  }
 
 }
