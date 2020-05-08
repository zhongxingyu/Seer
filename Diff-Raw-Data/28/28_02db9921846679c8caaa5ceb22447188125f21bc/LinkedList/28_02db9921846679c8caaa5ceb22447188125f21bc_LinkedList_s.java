 package com.jeromegagner.example.linkedlist;
 
 /**
  * Created by IntelliJ IDEA.
  * User: jgagner
  * Date: Apr 18, 2010
  * Time: 9:49:28 AM
  * To change this template use File | Settings | File Templates.
  */
 public class LinkedList {
     Element head;
     Element tail;
 
     public LinkedList(Element head) {
         this.head = head;
         this.tail = findTail();
     }
 
     public Element find(Object data) {
         Element head = this.head;
         while (head != null && head.data != data) {
             head = head.next;
         }
         return head;
     }
 
 
     public boolean remove(Element elem) {
         if(elem == this.head){
             this.head = this.head.next;
             return true;
         }
         Element current = this.head;
         while (current != null) {
             if (current.next == elem) {
                 current.next = elem.next;
                 if(current.next == null){
                     this.tail = current;
                 }
                 return true;
             }
            current = current.next;
         }
         return false; //if we get here, element was not found. I'd prefer keeping a *found* boolean flag, and return that to keep a single point of return
     }
 
     public boolean insertAfter(Element elem, Object data) {
 
         if(elem == null){
             Element e = new Element(data);
             e.next = this.head;
             this.head = e;
             return true;
         }
 
         Element current = this.head;
         while(current != null)
         {
             if(current == elem){
                 Element e = new Element(data);
                 e.next = current.next;
                 current.next = e;
                 if(e.next == null){
                   //handle new tail case
                   this.tail = e;  
                 }
                 return true;
             }
             current = current.next;
         }
         return false; //element not found
     }
 
     /**
      * utility for creating linked list of a certain size *
      */
     public static LinkedList createList(int numberOfItems) {
         Element head = new Element(0);
         Element previous = head;
         for (int i = 1; i < numberOfItems; i++) {
             Element item = new Element(i);
             previous.next = item;
             previous = item;
 
         }
         return new LinkedList(head);
     }
 
     /**
      * utiity methods
      */
     public Element findTail() {
         Element head = this.head;
        while (head.next != null) {
            head = head.next;
         }
         return head;
     }
 
     public int length() {
         int i = 0;
         Element head = this.head;
         while (head != null) {
             head = head.next;
             i++;
         }
         return i;
     }
 }
