 package com.friendfeed.web.pages;
 
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import com.friendfeed.core.domain.FriendFeed;
import com.friendfeed.core.domain.User;

 public class FriendFeedHome extends FriendFeedPage {
 
     private static final long serialVersionUID = 5966860435038911759L;
 
    public FriendFeedHome() {
        add(new Label("message", FriendFeed.getInstance().toString()));
        List<User> users = new ArrayList<>(FriendFeed.getInstance().getUsers());
        add(new ListView<User>("users", users) {
            private static final long serialVersionUID = 7041497838941274242L;

            @Override
            protected void populateItem(ListItem<User> item) {
                item.add(new Label("label", item.getModel().getObject().getScreenName()));
            }
        });
    }
 }
