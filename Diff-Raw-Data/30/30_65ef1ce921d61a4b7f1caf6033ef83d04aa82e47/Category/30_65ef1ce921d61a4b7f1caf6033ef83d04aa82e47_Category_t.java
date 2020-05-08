 package com.example.restfulclient.helpers;
 
 import java.util.Collection;
 
 public class Category
 {
     
     private String categoryId;
     
     private String categoryName;
     
     private Collection<Book> books;
     
     public Category()
     {
 	
     }
     
     public Category(String id, String name)
     {
 	categoryId = id;
 	categoryName = name;
     }
     
     public Category(String id, String name, Collection<Book> books2)
     {
 	categoryId = id;
 	categoryName = name;
 	books = books2;
     }
     
     public String getCategoryId()
     {
 	return categoryId;
     }
     
     public void setCategoryId(String categoryId)
     {
 	this.categoryId = categoryId;
     }
     
     public String getCategoryName()
     {
 	return categoryName;
     }
     
     public void setCategoryName(String categoryName)
     {
 	this.categoryName = categoryName;
     }
     
     public Collection<Book> getBooks()
     {
 	return books;
     }
     
     public void setBooks(Collection<Book> books)
     {
 	this.books = books;
     }
     
     @Override
     public boolean equals(Object o)
     {
 	Category c2 = (Category) o;
 	if (categoryId.equals(c2.categoryId))
 	{
 	    if (categoryName.equals(c2.categoryName))
 	    {
 		if ((c2.getBooks() == null && books == null) || books.containsAll(c2.getBooks()))
 		    return true;
 	    }
 	}
 	return false;
     }
     
     @Override
     public int hashCode()
     {
 	int hash = 1;
 	hash = hash * 11 + categoryId.hashCode();
 	hash = hash * 13 + categoryName.hashCode();
 	return hash;
     }
    
    @Override
    public String toString()
    {
	return new String("[ " + categoryId + ", " + categoryName + " ]");
    }
 }
