 package controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import models.Dvd;
 
 import com.avaje.ebean.Page;
 
 public class DvdPage {
 
   public final Page<Dvd> page;
 
   public final List<DvdPageNav> nav;
 
   public final List<Dvd> list;
 
   public DvdPage(final Page<Dvd> page) {
     this.page = page;
     list = page.getList();
 
     final int totalPageCount = page.getTotalPageCount();
 
     nav = new ArrayList<DvdPageNav>();
 
     if (totalPageCount < 10) {
       for (int i = 0; i < totalPageCount; i++) {
         nav.add(new DvdPageNav(i));
       }
     } else {
 
       final int pageIndex = page.getPageIndex();
 
       // check if we are at the beginning
       if (pageIndex < 10) {
         for (int i = 0; i <= 10; i++) {
           nav.add(new DvdPageNav(i));
         }
         nav.add(new DvdPageNav("..."));
         nav.add(new DvdPageNav(totalPageCount - 1));
       }
 
       // check if we are in the middle
       if ((pageIndex < totalPageCount - 9) && pageIndex > 10 - 1) {
         nav.add(new DvdPageNav(0));
         nav.add(new DvdPageNav("..."));
 
         for (int i = pageIndex - 5; i < pageIndex + 5; i++) {
           nav.add(new DvdPageNav(i));
         }
 
         nav.add(new DvdPageNav("..."));
         nav.add(new DvdPageNav(totalPageCount - 1));
       }
 
      // check if we are at the end
      if (pageIndex > totalPageCount - 10 && pageIndex >= 10) {
        nav.add(new DvdPageNav(0));
        nav.add(new DvdPageNav("..."));
        for (int i = totalPageCount - 10; i < totalPageCount; i++) {
          nav.add(new DvdPageNav(i));
        }
      }



     }
 
   }
 
 }
