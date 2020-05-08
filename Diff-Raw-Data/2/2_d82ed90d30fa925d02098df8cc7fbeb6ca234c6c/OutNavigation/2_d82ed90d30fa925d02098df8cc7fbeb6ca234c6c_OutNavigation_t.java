 package at.ac.tuwien.out;
 
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.panel.Panel;
 
 public class OutNavigation extends Panel {
     private static final long serialVersionUID = 3384116956231703356L;
 
     public OutNavigation(String id) {
         super(id);
        add(new BookmarkablePageLink<String>("home", StartPage.class));
     }
 }
