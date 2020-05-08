 /*  
  *   This file is part of the computer assignment for the
  *   Information Retrieval course at KTH.
  * 
  *   First version:  Johan Boye, 2010
  *   Second version: Johan Boye, 2012
  */  
 
 package ir;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.io.Serializable;
 
 /**
  *   A list of postings for a given word.
  */
 public class PostingsList implements Comparable<PostingsList>, Serializable {
     
     /** The postings list as a linked list, MUST be sorted. */   
     public LinkedList<PostingsEntry> list = new LinkedList<PostingsEntry>();
 
     public int compareTo(PostingsList other) {
         int a = size();
         int b = other.size();
 
         if(a<b)
             return -1;
         else if(b<a)
             return 1;
         else
             return 0;
     }
     /**  Number of postings in this list  */
     public int size() {
         return list.size();
     }
 
     /**  Returns the ith posting */
     public PostingsEntry get( int i ) {
         return list.get( i );
     }
 
     public Object clone() throws CloneNotSupportedException {
         PostingsList pl = new PostingsList();
         pl.list = (LinkedList<PostingsEntry>) list.clone();
         return pl;
     }
 
     /** Merges the existing Postinglist (this) med other. */
     public void merge_pl(PostingsList other) {
         int i = 0;
         int j = 0;
         while (i < size() && j < other.size()) {
             PostingsEntry a = get(i);
             PostingsEntry b = other.get(j);
             int ai = a.docID;
             int bj = b.docID;
             if (bj < ai) {
                 list.add(i, b);
                 i++;
                 j++;
             } else if (bj > ai) {
                 i++;
             } else { // the same document. keep the old one.
                 i++;
                 j++;
             }
         }
         while (j < other.size()) {
             list.add(i, other.get(j));
             j++;
             i++;
         }
     }
     public void merge_pl(PostingsList other, double weight) {
         int i = 0;
         int j = 0;
         while (i < size() && j < other.size()) {
             PostingsEntry a = get(i);
             PostingsEntry b = other.get(j);
             int ai = a.docID;
             int bj = b.docID;
             if (bj < ai) {
                 b.score += weight;
                 list.add(i, b);
                 i++;
                 j++;
             } else if (bj > ai) {
                 i++;
             } else { // the same document. keep the old one.
                 a.score += b.score + weight;
                 i++;
                 j++;
             }
         }
         while (j < other.size()) {
             list.add(i, other.get(j));
             j++;
             i++;
         }
     }
 
     /** Adds a new offset entry to the PostingsEntry that has the specified docID */
     public void add(int docID, int offset) {
         // Find the PostingsEntry specified by the docID
         PostingsEntry entry = null;
         int list_index = 0; // Used to know where to insert the new PostingsEnty.
         for(PostingsEntry pe : list) { // For each loop preserves order.
             if(docID == pe.docID) {
                 entry = pe;
                 break;
             } else if(pe.docID > docID) { // Were no PostingsEntry in list.
                 break;
             } else {
                 list_index++;
             }
         }
 
 
         // There was no entry for the docID - now we should add it
         if(entry == null) {
             try {
                 list.add(list_index, new PostingsEntry(docID, offset, 0));
             } catch (IndexOutOfBoundsException e) {
                 System.out.println("LIST_INDEX BROKEN");
             }
         } else {
             entry.add_offset(offset);
         }
     }
 
     public static PostingsList union(PostingsList a, PostingsList b)
     {
         PostingsList result = new PostingsList();
         int i = 0;
         int j = 0;
         if(a == null && b == null)
         {
             return result;
         }
         else if(b == null)
         {
             i = 0;
             while(i < a.size())
             {
                 result.list.addLast(new PostingsEntry(a.get(i).docID, 0, 0));
                 i++;
             }
             return result;
         }
         else if(a == null)
         {
             i = 0;
             while(i < b.size())
             {
                 result.list.addLast(new PostingsEntry(b.get(i).docID, 0, 0));
                 i++;
             }
         }
 
         while(i < a.size() && j < b.size())
         {
             int ai = a.get(i).docID;
             int bj = b.get(j).docID;
             if(ai == bj)
             {
                 result.list.addLast(new PostingsEntry(ai, 0, 0));
                 i++;
                 j++;
             }
             else if(ai < bj)
             {
                 result.list.addLast(new PostingsEntry(ai, 0, 0));
                 i++;
             }
             else
             {
                 result.list.addLast(new PostingsEntry(bj, 0, 0));
                 j++;
             }
         }
         while(i < a.size())
         {
             result.list.addLast(new PostingsEntry(a.get(i).docID, 0, 0));
             i++;
         }
         while(j < b.size())
         {
             result.list.addLast(new PostingsEntry(b.get(j).docID, 0, 0));
             j++;
         }
         return result;
     }
 
     public static PostingsList intersect_query(PostingsList a, PostingsList b) {
         PostingsList result = new PostingsList();
         int i = 0;
         int j = 0;
 
         while(i < a.size() && j < b.size())
         {
             int ai = a.get(i).docID;
             int bj = b.get(j).docID;
             if(ai == bj){
                 result.list.add(new PostingsEntry(ai,0,0));
                 i++;
                 j++;
             } else if(ai < bj) {
                 i++;
             } else {
                 j++;
             }
         }
         return result;
     }
 
     public static PostingsList phrase_query(PostingsList a, PostingsList b) {
         PostingsList result = new PostingsList();
         int i = 0;
         int j = 0;
 
         while(i < a.size() && j < b.size())
         {
             int ai = a.get(i).docID;
             int bj = b.get(j).docID;
             if(ai == bj){
                 ArrayList<Integer> offsets = PostingsEntry.is_followed_by(a.get(i), b.get(j));
                 for( int off : offsets) {
                     result.add(ai, off);
                 }
                 i++;
                 j++;
             } else if(ai < bj) {
                 i++;
             } else {
                 j++;
             }
         }
         return result;
     }
 
     public static PostingsList add_wildcard(PostingsList a)
     {
         PostingsList result = new PostingsList();
         int i = 0;
         
         while(i < a.size())
         {
             for(int off : a.get(i).offsets)
             {
                 result.add(a.get(i).docID, off + 1);
             }
             i++;
         }
         return result;
     }
 
     public void addScore(int docID, double newScore)
     {
         for( PostingsEntry pe : list )
         {
             if(docID == pe.docID)
             {
                 pe.score += newScore;
             }
         }
         return;
     }
 
     public String toString() {
         return list.toString();
         //return "" +size();
     }
 }
