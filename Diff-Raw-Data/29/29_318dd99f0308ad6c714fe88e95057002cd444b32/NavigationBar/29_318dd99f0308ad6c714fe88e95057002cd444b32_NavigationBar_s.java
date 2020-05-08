 package com.dianping.dobby.view;
 
import com.dianping.dobby.ticket.TicketPage;

 import org.unidal.web.mvc.Page;
 
 public class NavigationBar {
    public Page[] getVisiblePages() {
       return new Page[] {
   
      TicketPage.HOME
 
		};
    }
 }
