 package org.techhouse.shirts.display.web.pages;
 
 import org.apache.wicket.behavior.SimpleAttributeModifier;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.techhouse.shirts.data.enums.Role;
 import org.techhouse.shirts.display.web.WicketApplication;
 import org.techhouse.shirts.display.web.security.WicketSession;
 import org.techhouse.shirts.service.DeadlineService;
 
 public abstract class TemplatePage extends BasePage {
 
 	@SpringBean
 	private DeadlineService deadlineService;
 
 	public TemplatePage() {
 		super();
 
 		add(new BookmarkablePageLink<BallotPage>("logoLink", WicketApplication.get().getHomePage()));
 
 		add(new FeedbackPanel("feedback") {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public boolean isVisible() {
 				return anyMessage();
 			}
 		});
 
 		add(new WebMarkupContainer("main") {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public boolean isTransparentResolver() {
 				return true;
 			}
 		}.add(new SimpleAttributeModifier("id", this.getClass().getSimpleName())));
 
 		add(new Label("username", new AbstractReadOnlyModel<String>() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public String getObject() {
 				if (WicketSession.get().isSignedIn()) {
 					return WicketSession.get().getMember().getName();
 				} else {
 					return getString("nav.user.anonymous");
 				}
 			}
 		}));
 
 		add(new Link<Void>("logout") {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick() {
 				WicketSession.get().signOut();
				setResponsePage(WicketApplication.get().getSignInPageClass());
 			}
 		});
 
 		final WebMarkupContainer deadlineNotice = new WebMarkupContainer("deadlineNotice");
 		add(deadlineNotice);
 		deadlineNotice.setVisible(deadlineService.isDeadlineSet());
 		deadlineNotice.add(new Label("dateTime", new AbstractReadOnlyModel<String>() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public String getObject() {
 				if (deadlineService.isDeadlineSet()) {
					return deadlineService.getDeadline().withZone(WicketApplication.get().getDefaultDateTimeZone()).toString("EEEE, MMMM d, YYYY, 'at' h:mm a z");
 				} else {
 					return "none";
 			}
 		}}));
 
 		final WebMarkupContainer adminNavContainer = new WebMarkupContainer("admin");
 		adminNavContainer.setVisible(WicketSession.get().hasRole(Role.ADMIN) || WicketApplication.get().isDevelopment());
 		add(adminNavContainer);
 
 		adminNavContainer.add(new IndicatingPageLink("designAdd", DesignEditPage.class));
 
 		adminNavContainer.add(new IndicatingPageLink("designEdit", DesignListPage.class));
 
 		adminNavContainer.add(new IndicatingPageLink("configuration", ConfigurationPage.class));
 
 		final WebMarkupContainer resultsNavContainer = new WebMarkupContainer("results");
 		resultsNavContainer.setVisible(WicketSession.get().hasRole(Role.ADMIN) || WicketApplication.get().isDevelopment()
 				|| deadlineService.hasDeadlinePassed());
 		add(resultsNavContainer);
 
 		resultsNavContainer.add(new IndicatingPageLink("stats", StatisticsPage.class));
 	}
 }
