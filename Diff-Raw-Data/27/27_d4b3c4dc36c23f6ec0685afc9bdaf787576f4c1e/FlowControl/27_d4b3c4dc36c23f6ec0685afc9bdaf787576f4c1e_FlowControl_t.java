 package com.ratethisfest.client;
 
 import com.google.gwt.dom.client.Style.Position;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.RootPanel;
 
 /**
  * 
  * @author Amy
  * 
  */
 public class FlowControl {
   private static FlowControl instance;
 
   private FlowControl() {
   }
 
   public static void go(Composite c) {
     // not sure why we need this yet since everything is static.
     if (instance == null) {
       instance = new FlowControl();
     }
     RootPanel.get().clear();
     // not sure why, but GWT throws an exception without this. Adding to CSS
     // doesn't work.
     RootPanel.get().getElement().getStyle().setPosition(Position.RELATIVE);
     // add, determine height/width, center, then move. height/width are unknown
     // until added to document. Catch-22!
     RootPanel.get().add(c);
     History.newItem(c.getTitle());
   }
 
   /*
    * public static void go(String tokenString) { PageToken token =
    * getPageFromToken(tokenString); String param =
    * getParamFromToken(tokenString); if (token == null) { go(new
    * CoachellerViewComposite()); } else if
    * (PageToken.VIEW_COACHELLA.equals(token)) { go(new
    * CoachellerViewComposite()); } else if
    * (PageToken.EMAIL_COACHELLA.equals(token)) { go(new
    * CoachellerEmailComposite()); } else if
    * (PageToken.RATE_COACHELLA.equals(token)) { go(new
    * CoachellerRateComposite(param)); } else if
    * (PageToken.VIEW_LOLLA.equals(token)) { go(new
    * LollapaloozerViewComposite()); } else if
    * (PageToken.EMAIL_LOLLA.equals(token)) { go(new
    * LollapaloozerEmailComposite()); } else if
    * (PageToken.RATE_LOLLA.equals(token)) { go(new
    * LollapaloozerRateComposite(param)); } // TODO: add retrievePoll to
    * PollPhotoWidget // else if (PageToken.UPLOAD.equals(token)) { // go(new
    * PollPhotoWidget()); // } }
    */
 
   public static void go(String tokenString) {
     PageToken token = getPageFromToken(tokenString);
     String param = getParamFromToken(tokenString);
 
     if (token == null) {
       go(new CoachellerViewComposite());
     } else {
      String hostName = Window.Location.getHostName();
      if (hostName == null
          || (!hostName.contains("coacheller") && !hostName.contains("lollapaloozer"))) {
        // for now, localhost/ratethisfest will default to lollapaloozer
        hostName = "lollapaloozer";
      }
 
      if (hostName.contains("coacheller")) {
         if (PageToken.VIEW.equals(token)) {
           go(new CoachellerViewComposite());
         } else if (PageToken.EMAIL.equals(token)) {
           go(new CoachellerEmailComposite());
         } else if (PageToken.RATE.equals(token)) {
           go(new CoachellerRateComposite(param));
         } // End if host is coacheller
      } else if (hostName.contains("lollapaloozer")) {
         if (PageToken.VIEW.equals(token)) {
          go(new LollapaloozerViewComposite());
         } else if (PageToken.EMAIL.equals(token)) {
          go(new LollapaloozerEmailComposite());
         } else if (PageToken.RATE.equals(token)) {
          go(new LollapaloozerRateComposite(param));
         }
       } // End if host is lollapaloozer
     } // End if email is provided
 
     // TODO: add retrievePoll to PollPhotoWidget
     // else if (PageToken.UPLOAD.equals(token)) {
     // go(new PollPhotoWidget());
     // }
   }
 
   private static PageToken getPageFromToken(String tokenString) {
     String[] tokens = tokenString.split("=");
     if (tokens.length > 0) {
       String token = tokens[0];
       return PageToken.fromValue(token);
     }
     return null;
   }
 
   private static String getParamFromToken(String tokenString) {
     String[] tokens = tokenString.split("=");
     if (tokens.length > 1) {
       return tokens[1];
     }
     return null;
   }
 }
