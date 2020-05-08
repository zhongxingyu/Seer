 package com.dianping.dobby.view;
 
 import org.unidal.web.mvc.Page;
 
import com.dianping.dobby.book.BookPage;

 public class NavigationBar {
    public Page[] getVisiblePages() {
       return new Page[] {
 
      BookPage.HOME

      };
    }
 }
