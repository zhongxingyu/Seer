 package org.vaadin.notifique.sample;
 
 import java.util.Date;
 
 import org.vaadin.notifique.Notifique;
 import org.vaadin.notifique.Notifique.ClickListener;
 import org.vaadin.notifique.Notifique.HideListener;
 import org.vaadin.notifique.Notifique.Message;
 
 import com.vaadin.Application;
 import com.vaadin.terminal.Resource;
 import com.vaadin.terminal.ThemeResource;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Window;
 import com.vaadin.ui.Button.ClickEvent;
 
 public class NotifiqueSampleApplication extends Application implements
         ClickListener, HideListener {
 
     private static final long serialVersionUID = -7235009716318488720L;
     private Notifique stack;
     private Notifique flow;
 
     @Override
     public void init() {
         Window w = new Window("Notifique Sample Application");
         stack = new Notifique(false);
         stack.setClickListener(this);
         stack.setHideListener(this);
         stack.setWidth("100%");
         w.addComponent(stack);
 
         HorizontalLayout btns = new HorizontalLayout();
         w.addComponent(btns);
 
         String[] styles = new String[] { Notifique.Styles.INFO,
                 Notifique.Styles.SUCCESS, Notifique.Styles.WARNING,
                 Notifique.Styles.ERROR, Notifique.Styles.MESSAGE,
                 Notifique.Styles.MAGIC_BLACK, Notifique.Styles.MAGIC_GRAY,
                 Notifique.Styles.MAGIC_WHITE, Notifique.Styles.BROWSER_FF,
                 Notifique.Styles.BROWSER_IE,
                 Notifique.Styles.BROWSER_CHROME,
                 Notifique.Styles.VAADIN_BLACK,
                 Notifique.Styles.VAADIN_BEIGE,
                 Notifique.Styles.VAADIN_RED,
                 Notifique.Styles.VAADIN_GREEN,
                 Notifique.Styles.VAADIN_BLUE,
                 Notifique.Styles.VAADIN_ORANGE };
         for (int i = 0; i < styles.length; i++) {
             final String s = styles[i];
             btns.addComponent(new Button("" + s, new Button.ClickListener() {
                 private static final long serialVersionUID = 1L;
 
                 public void buttonClick(ClickEvent event) {
                     stack
                             .add(
                                     themeIcon("email", 32),
                                     "This is a message at <b>"
                                             + new Date()
                                             + "</b>. This is something longer with this.",
                                     s);
                 }
             }));
         }
         btns.addComponent(new Button("Clear", new Button.ClickListener() {
             private static final long serialVersionUID = 1L;
 
             public void buttonClick(ClickEvent event) {
                 stack.clear();
             }
         }));
 
         final Resource icon = new ThemeResource("../runo/icons/32/email.png");
 
         flow = new Notifique(true);
         flow.setFillFromTop(true);
         flow.setClickListener(this);
         flow.setHideListener(this);
         flow.setVisibleCount(4);
         flow.setWidth("200px");
         flow.setHeight("400px");
         w.addComponent(flow);
         w.addComponent(new Button("add", new Button.ClickListener() {
             private static final long serialVersionUID = 1L;
 
             public void buttonClick(ClickEvent event) {
                 flow.add(icon, "This is a message at <b>" + new Date()
                         + "</b>. This is something longer with this.", true,
                         null, false);
             }
         }));
 
         setMainWindow(w);
     }
 
     /**
      * Get resource for one of the "Runo" theme icons.
      *
      * @param name
      * @param size
      * @return
      */
     protected Resource themeIcon(String name, int size) {
         return new ThemeResource("../runo/icons/" + size + "/" + name + ".png");
     }
 
     public void messageClicked(Message message) {
         getMainWindow().showNotification("message clicked");
         message.hide();
     }
 
     public void messageHide(Message message) {
         getMainWindow().showNotification("message hide");
     }
 }
