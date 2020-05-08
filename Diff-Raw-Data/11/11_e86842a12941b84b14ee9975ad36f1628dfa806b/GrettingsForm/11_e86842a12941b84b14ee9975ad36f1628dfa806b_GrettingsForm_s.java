 package my.application.client.form;
 
 import com.extjs.gxt.ui.client.widget.TabItem;
 import com.extjs.gxt.ui.client.widget.TabPanel;
 import com.google.gwt.user.client.ui.HTML;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: smiler
  * Date: 7/22/12
  * Time: 1:37 PM
  * To change this template use File | Settings | File Templates.
  */
 public class GrettingsForm extends TabPanel {
 
     public GrettingsForm() {
         super();
         initTabBar();
         addStyleName("contentPanel");
         setAnimScroll(true);
         setBorders(true);
//        setDeferHeight(true);
 //        setHeight("300px");
 
     }
 
     private void initTabBar() {
         TabItem tabItem = new TabItem();
 
         tabItem.setText("Просмотр всех комментариев");
        tabItem.scrollIntoView(this);
        for (int i = 0; i < 50; i++) {
             tabItem.add(new HTML("bla-bla-bla"));
         }
         add(tabItem);
 
         tabItem = new TabItem("Добавить комментарий");
         tabItem.addText("blu-blu-blu");
         add(tabItem);
 
         tabItem = new TabItem("Редактировать комментарий");
         tabItem.addText("bli-bli-bli");
         add(tabItem);
 
         tabItem = new TabItem("Удалить комментарий");
         tabItem.addText("ble-ble-ble");
         add(tabItem);
 
     }
 
 }
