 package at.ac.tuwien.swag.webapp.in.messages;
 
 import java.text.Format;
 import java.text.SimpleDateFormat;
 import java.util.List;
 
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.data.DataView;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 
 import at.ac.tuwien.swag.model.dto.MessageDTO;
 import at.ac.tuwien.swag.webapp.SwagWebSession;
 import at.ac.tuwien.swag.webapp.in.provider.MessageSortableDataProvider;
 import at.ac.tuwien.swag.webapp.service.MessageService;
 
 import com.google.inject.Inject;
 
 public class Notifications extends Panel {
     private static final long serialVersionUID = -4045913776508864182L;
 
     @Inject
     private MessageService messages;
 
     public Notifications(String id) {
         super(id);
 
         String username = ((SwagWebSession) getSession()).getUsername();
 
         List<MessageDTO> notificationList = messages.getNotifications(username);
 
         final Format formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
 
         DataView<MessageDTO> dataView =
            new DataView<MessageDTO>("notificationList", new MessageSortableDataProvider(notificationList)) {
                 private static final long serialVersionUID = -7500357470052232668L;
 
                 @Override
                 protected void populateItem(Item<MessageDTO> item) {
                     MessageDTO message = item.getModelObject();
                     PageParameters param = new PageParameters();
                     param.add("id", message.getId());
 
                     item.add(new Label("subject", message.getSubject()));
                     item.add(new Label("date", formatter.format(message.getTimestamp())));
                     item.add(new BookmarkablePageLink<String>("view", MessageDetail.class, param));
                 }
             };
 
         dataView.setItemsPerPage(15);
         add(dataView);
 
         add(new PagingNavigator("navigator", dataView));
     }
 
 }
