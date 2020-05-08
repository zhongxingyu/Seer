 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package visualization;
 
import java.util.LinkedList;
 import java.awt.Color;
 import java.awt.image.BufferedImage;
import java.util.Random;
 /**
  *
  * @author Riz
  */
 
 public class Book
 {
     private static Random r = new Random();
     public String title;
     public String author;
     public String description;
     public String publisher;
     public BufferedImage novelPic;
     public BufferedImage tagPic;
     public float price;
     public float moodIndex;
     public LinkedList<String> words;
     public LinkedList<Integer> wordWeights;
     public LinkedList<BookStats> statistics;
     public Color colour;
 
     public Book(String title, String author, String publisher, String description, float price, int moodIndex)
     {
         words = new LinkedList<String>();
         wordWeights = new LinkedList<Integer>();
         statistics = new LinkedList<BookStats>();
         this.moodIndex = r.nextFloat();
         this.title = title;
         this.author = author;
         this.description = description;
         this.price = price;
         this.publisher = publisher;
     }
 
     public BookStats addStats(int week, int year, int revenue, int ranking)
     {
         BookStats bookStats = new BookStats(week, year, revenue, ranking, this);
         if (statistics.size()> 0)
         {
             statistics.peekLast().next = bookStats;
             if (statistics.peekLast().week + 1 < week)
                 bookStats.isFirst = true;
         }
         else
             bookStats.isFirst = true;
         statistics.add(bookStats);
         return bookStats;
     }
 
     public void addWords()
     {
         
     }
 
     public String toString()
     {
         return title + ": " + author + ", " + publisher + ", $" + price + ", " + description + ", " + statistics.size() + "\n";
     }
 
     public class BookStats
     {
         public int week;
         public int year;
         public int revenue;
         public int ranking;
         public Book owner;
         public BookStats next;
         public boolean isFirst;
 
         public BookStats(int week, int year, int revenue, int ranking, Book owner)
         {
             this.week = week;
             this.year = year;
             this.revenue = revenue;
             this.ranking = ranking;
             this.owner = owner;
             next = null;
         }
     }
 }
