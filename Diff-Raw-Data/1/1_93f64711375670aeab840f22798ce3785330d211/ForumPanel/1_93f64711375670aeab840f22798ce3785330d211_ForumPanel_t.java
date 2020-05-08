 /**
  * Tysan Clan Website
  * Copyright (C) 2008-2013 Jeroen Steenbeeke and Ties van de Ven
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.tysanclan.site.projectewok.components;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.Session;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.image.ContextImage;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.data.DataView;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import com.jeroensteenbeeke.hyperion.data.ModelMaker;
 import com.tysanclan.site.projectewok.TysanSession;
 import com.tysanclan.site.projectewok.beans.ForumService;
 import com.tysanclan.site.projectewok.entities.Event;
 import com.tysanclan.site.projectewok.entities.Forum;
 import com.tysanclan.site.projectewok.entities.ForumPost;
 import com.tysanclan.site.projectewok.entities.ForumThread;
 import com.tysanclan.site.projectewok.entities.JoinApplication;
 import com.tysanclan.site.projectewok.entities.Trial;
 import com.tysanclan.site.projectewok.entities.User;
 import com.tysanclan.site.projectewok.entities.dao.EventDAO;
 import com.tysanclan.site.projectewok.entities.dao.ForumPostDAO;
 import com.tysanclan.site.projectewok.entities.dao.ForumThreadDAO;
 import com.tysanclan.site.projectewok.entities.dao.JoinApplicationDAO;
 import com.tysanclan.site.projectewok.entities.dao.TrialDAO;
 import com.tysanclan.site.projectewok.pages.forum.CreateThreadPage;
 
 public class ForumPanel extends Panel {
 	private static final long serialVersionUID = 1L;
 
 	@SpringBean
 	private ForumThreadDAO forumThreadDAO;
 
 	@SpringBean
 	private ForumService forumService;
 
 	public ForumPanel(String id, final Forum forum, final long pageId,
 			final boolean publicView) {
 		super(id);
 
 		DataView<ForumThread> threads = new DataView<ForumThread>("threads",
 				ForumDataProvider.of(forum, forumThreadDAO), 10) {
 			private static final long serialVersionUID = 1L;
 
 			@SpringBean
 			private EventDAO eventDAO;
 
 			@SpringBean
 			private TrialDAO trialDAO;
 
 			@SpringBean
 			private JoinApplicationDAO joinApplicationDAO;
 
 			@SpringBean
 			private ForumPostDAO forumPostDAO;
 
 			@Override
 			protected void populateItem(Item<ForumThread> item) {
 
 				ForumThread current = item.getModelObject();
 				TysanSession session = (TysanSession) Session.get();
 
 				int unreadCount = (session != null && session.getUser() != null) ? forumService
 						.getForumThreadUnreadCount(session.getUser(), current)
 						: 0;
 
 				Event ev = current.getEvent();
 				Trial tr = current.getTrial();
 				JoinApplication ja = current.getApplication();
 
 				if (ev != null) {
 					item.add(new ContextImage("thread",
 							"images/icons/clock.png").add(
 							AttributeModifier.replace("alt", new Model<String>(
 									"Event"))).add(
 							AttributeModifier.replace("title",
 									new Model<String>("Event"))));
 				} else if (ja != null) {
 					item.add(new ContextImage("thread",
 							"images/icons/user_add.png").add(
 							AttributeModifier.replace("alt", new Model<String>(
 									"Join Application"))).add(
 							AttributeModifier.replace("title",
 									new Model<String>("Join Application"))));
 				} else if (tr != null) {
 					item.add(new ContextImage("thread", "images/icons/bell.png")
 							.add(new AttributeModifier("alt",
 									new Model<String>("User Trial"))).add(
 									AttributeModifier.replace("title",
 											new Model<String>("User Trial"))));
 				} else {
 					item.add(new ContextImage("thread",
 							"images/icons/page_white.png"));
 
 				}
 
 				item.add(new ContextImage("sticky", "images/icons/link.png")
 						.setVisible(current.isPostSticky()));
 				item.add(new ContextImage("locked", "images/icons/lock.png")
 						.setVisible(current.isLocked()));
 				item.add(new ContextImage("new", "images/icons/new.png")
 						.setVisible(unreadCount > 0));
 
 				item.add(new AutoThreadLink("postlink", current));
 
 				item.add(new LastPostLink("last", current));
 
 				TimeZone ny = TimeZone.getTimeZone("America/New_York");
 
 				Calendar cal = Calendar.getInstance(ny);
 				cal.set(Calendar.HOUR_OF_DAY, 0);
 				cal.set(Calendar.MINUTE, 0);
 
 				Calendar cal2 = (Calendar) cal.clone();
 				cal2.add(Calendar.DAY_OF_MONTH, 1);
 
 				SimpleDateFormat sdf = new SimpleDateFormat(
 						"EEE MMM dd, yyyy h:mma zzz", Locale.US);
 				sdf.setTimeZone(ny);
 
 				SimpleDateFormat sdfToday = new SimpleDateFormat(
 						"'Today' h:mma zzz", Locale.US);
 				sdfToday.setTimeZone(ny);
 
 				item.add(new Label("poster",
 						current.getPoster() != null ? current.getPoster()
 								.getUsername() : "System"));
 				item.add(new Label("postcount", new Model<Long>(
 						ForumDataProvider.of(current, forumPostDAO).size())));
 
 				Date lastPost = current.getLastPost();
 				if (lastPost != null) {
 					item.add(new DateTimeLabel("lastresponse", current
 							.getLastPost()));
 				} else {
 					item.add(new Label("lastresponse", "Never"));
 				}
 
 				if (current.getPosts().size() > 0) {
 					ForumPost last = null;
 					for (int i = current.getPosts().size() - 1; i >= 0; i--) {
 						ForumPost cp = current.getPosts().get(i);
 						if (cp.isShadow())
 							continue;
 
 						last = cp;
						break;
 					}
 
 					if (last != null) {
 						item.add(new Label("lastposter",
 								last.getPoster() != null ? last.getPoster()
 										.getUsername() : "System"));
 					} else {
 						item.add(new Label("lastposter", "Nobody"));
 					}
 
 				} else {
 					item.add(new Label("lastposter", "Nobody"));
 				}
 
 			}
 
 		};
 
 		threads.setItemsPerPage(10);
 
 		Link<Forum> markAsReadLink = new Link<Forum>("markasread",
 				ModelMaker.wrap(forum)) {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick() {
 				TysanSession session = (TysanSession) TysanSession.get();
 
 				if (session != null && session.getUser() != null) {
 					forumService.clearUnreadPosts(session.getUser(),
 							getModelObject());
 
 				}
 
 			}
 
 		};
 
 		markAsReadLink.add(new ContextImage("icon", "images/icons/eye.png"));
 
 		TysanSession session = (TysanSession) TysanSession.get();
 		User user = null;
 
 		if (session != null) {
 			user = session.getUser();
 		}
 
 		markAsReadLink.setVisible(user != null);
 
 		add(markAsReadLink);
 
 		add(threads);
 
 		Link<Forum> createThread = new Link<Forum>("threadlink",
 				ModelMaker.wrap(forum)) {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick() {
 				setResponsePage(new CreateThreadPage(getModelObject()));
 			}
 		};
 
 		createThread.setVisible(!publicView && forum.canCreateThread(user));
 
 		add(createThread);
 
 		add(new PagingNavigator("navigation", threads));
 	}
 
 	/**
 	 * @return the forumThreadDAO
 	 */
 	public ForumThreadDAO getForumThreadDAO() {
 		return forumThreadDAO;
 	}
 
 	/**
 	 * @param forumThreadDAO
 	 *            the forumThreadDAO to set
 	 */
 	public void setForumThreadDAO(ForumThreadDAO forumThreadDAO) {
 		this.forumThreadDAO = forumThreadDAO;
 	}
 
 }
