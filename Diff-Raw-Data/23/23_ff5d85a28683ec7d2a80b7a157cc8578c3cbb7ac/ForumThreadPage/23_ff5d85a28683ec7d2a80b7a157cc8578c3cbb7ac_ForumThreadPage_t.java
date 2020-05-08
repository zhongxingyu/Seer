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
 package com.tysanclan.site.projectewok.pages;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Set;
 
import javax.annotation.Nullable;

 import org.apache.wicket.RestartResponseAtInterceptPageException;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.image.ContextImage;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.data.DataView;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import com.jeroensteenbeeke.hyperion.data.ModelMaker;
 import com.tysanclan.site.projectewok.TysanPage;
 import com.tysanclan.site.projectewok.TysanSession;
 import com.tysanclan.site.projectewok.beans.ForumService;
 import com.tysanclan.site.projectewok.beans.MembershipService;
 import com.tysanclan.site.projectewok.components.AutoForumLink;
 import com.tysanclan.site.projectewok.components.AutoThreadLink;
 import com.tysanclan.site.projectewok.components.ForumDataProvider;
 import com.tysanclan.site.projectewok.components.ForumEventPanel;
 import com.tysanclan.site.projectewok.components.ForumThreadModeratorPanel;
 import com.tysanclan.site.projectewok.components.PostPanel;
 import com.tysanclan.site.projectewok.components.TrialPanel;
 import com.tysanclan.site.projectewok.entities.Event;
 import com.tysanclan.site.projectewok.entities.Forum;
 import com.tysanclan.site.projectewok.entities.ForumPost;
 import com.tysanclan.site.projectewok.entities.ForumThread;
 import com.tysanclan.site.projectewok.entities.Game;
 import com.tysanclan.site.projectewok.entities.JoinApplication;
 import com.tysanclan.site.projectewok.entities.JoinVerdict;
 import com.tysanclan.site.projectewok.entities.Rank;
 import com.tysanclan.site.projectewok.entities.Realm;
 import com.tysanclan.site.projectewok.entities.Trial;
 import com.tysanclan.site.projectewok.entities.User;
 import com.tysanclan.site.projectewok.entities.UserGameRealm;
 import com.tysanclan.site.projectewok.entities.dao.EventDAO;
 import com.tysanclan.site.projectewok.entities.dao.ForumPostDAO;
 import com.tysanclan.site.projectewok.entities.dao.ForumThreadDAO;
 import com.tysanclan.site.projectewok.entities.dao.JoinApplicationDAO;
 import com.tysanclan.site.projectewok.entities.dao.TrialDAO;
 import com.tysanclan.site.projectewok.pages.forum.ReplyPage;
 import com.tysanclan.site.projectewok.util.DateUtil;
 import com.tysanclan.site.projectewok.util.MemberUtil;
 
 /**
  * @author Jeroen Steenbeeke
  */
 public class ForumThreadPage extends TysanPage {
 
 	private static final long serialVersionUID = 1L;
 
 	@SpringBean
 	private ForumThreadDAO dao;
 
 	@SpringBean
 	private EventDAO eventDAO;
 
 	@SpringBean
 	private ForumService forumService;
 
 	@SpringBean
 	private JoinApplicationDAO joinApplicationDAO;
 
 	@SpringBean
 	private TrialDAO trialDAO;
 
 	private IModel<ForumThread> threadModel;
 
 	@SpringBean
 	private ForumPostDAO forumPostDAO;
 
 	public ForumThreadPage(PageParameters params) {
 		super("");
 
 		Set<String> namedKeys = params.getNamedKeys();
 
 		if (!namedKeys.contains("threadid") || !namedKeys.contains("pageid")) {
 			throw new RestartResponseAtInterceptPageException(
 					AccessDeniedPage.class);
 		}
 
 		Long threadId = params.get("threadid").toOptionalLong();
 		Integer pageId = params.get("pageid").toOptionalInteger();
 
 		if (threadId == null || pageId == null) {
 			throw new RestartResponseAtInterceptPageException(
 					AccessDeniedPage.class);
 		}
 
 		ForumThread t = dao.get(threadId);
 
 		if (t == null) {
 			throw new RestartResponseAtInterceptPageException(
 					AccessDeniedPage.class);
 		}
 
 		initComponents(t, pageId, getUser() == null);
 	}
 
 	public ForumThreadPage(final long threadId, int pageId,
 			final boolean publicView) {
 		super("");
 
 		initComponents(dao.load(threadId), pageId, publicView);
 	}
 
 	/**
 	 * @return the dao
 	 */
 	public ForumThreadDAO getDao() {
 		return dao;
 	}
 
 	protected void initComponents(ForumThread thread, final int pageId,
 			final boolean publicView) {
 		TysanSession sess = TysanSession.get();
 
 		Forum forum = thread.getForum();
 
 		if (!forumService.canView(getUser(), forum)) {
 			throw new RestartResponseAtInterceptPageException(
 					AccessDeniedPage.class);
 		}
 
 		boolean memberLoggedIn = sess != null;
 
 		setPageTitle(thread.getTitle());
 		threadModel = ModelMaker.wrap(thread);
 
 		initJoinComponents(thread, pageId);
 
 		Link<ForumThread> replyLink = new Link<ForumThread>("replylink") {
 			private static final long serialVersionUID = 1L;
 
 			/**
 			 * @see org.apache.wicket.markup.html.link.Link#onClick()
 			 */
 			@Override
 			public void onClick() {
 				setResponsePage(new ReplyPage(threadModel.getObject(), pageId));
 
 			}
 		};
 
 		WebMarkupContainer branchnote = new WebMarkupContainer("branchnote");
 		if (thread.getBranchFrom() != null) {
 			branchnote
 					.add(new AutoThreadLink("source", thread.getBranchFrom()));
 		} else {
 			branchnote.add(new WebMarkupContainer("source"));
 		}
 		add(branchnote);
 		branchnote.setVisible(thread.getBranchFrom() != null);
 
 		ForumThreadModeratorPanel modPanel = new ForumThreadModeratorPanel(
 				"moderatorToolbox", thread);
 
 		User currentUser = getUser();
 		boolean moderator = forumService.isModerator(currentUser,
 				thread.getForum());
 
 		modPanel.setVisible(moderator);
 
 		add(modPanel);
 
 		add(replyLink);
 
 		long parentPageIndex = determineParentPageIndex(thread);
 
 		add(new AutoForumLink("returnlink", thread.getForum(),
 				"Return to forum", parentPageIndex));
 		add(new AutoForumLink("returnlink2", thread.getForum(),
 				"Return to forum", parentPageIndex));
 
 		Event event = eventDAO.getEventByThread(thread);
 		if (event == null) {
 			add(new WebMarkupContainer("event").setVisible(false));
 		} else {
 			add(new ForumEventPanel("event", event, getUser()));
 		}
 
 		Trial trial = trialDAO.getTrialByThread(thread);
 		if (trial == null) {
 			add(new WebMarkupContainer("trial").setVisible(false));
 		} else {
 			add(new TrialPanel("trial", trial, getUser()));
 		}
 
 		DataView<ForumPost> postView = new DataView<ForumPost>("posts",
 				ForumDataProvider.of(thread, forumPostDAO)) {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void populateItem(Item<ForumPost> item) {
 				final ForumPost fp = item.getModelObject();
 
 				item.add(new PostPanel("postpanel", fp));
 			}
 
 		};
 
 		postView.setItemsPerPage(ForumThread.POSTS_PER_PAGE);
 		postView.setCurrentPage(pageId - 1);
 
 		add(postView);
 
 		add(new PagingNavigator("topnavigation", postView));
 		add(new PagingNavigator("bottomnavigation", postView));
 
 		replyLink.setVisible(memberLoggedIn && !thread.isLocked()
 				&& forumService.canReply(getUser(), thread.getForum())
 				&& mayReplyToTrial(trial, getUser()));
 
 	}
 
 	private boolean mayReplyToTrial(Trial trial, User user) {
 		if (trial != null) {
 			return user.getRank() == Rank.TRUTHSAYER
 					|| trial.getAccused().equals(user);
 
 		}
 
 		return true;
 	}
 
 	public ForumThread getThread() {
 		return threadModel.getObject();
 	}
 
 	private void initJoinComponents(ForumThread thread, long pageId) {
 		JoinApplication app = joinApplicationDAO
 				.getJoinApplicationByThread(thread);
 
 		addSenatorBox(app);
 
 		addMentorBox(app);
 
 		addStatusBox(app);
 
 	}
 
 	private void addStatusBox(JoinApplication app) {
 		WebMarkupContainer joinMessages = new WebMarkupContainer("joinMessages");
 
 		joinMessages
 				.add(new Label(
 						"mentor",
 						app != null ? app.getMentor() != null ? "<b>"
 								+ app.getMentor().getUsername()
 								+ "</b> has volunteered to be this candidate member's Mentor"
 								: "This candidate member has no mentor"
 								: "").setEscapeModelStrings(false));
 
 		int infavor = 0, total = 0;
 
 		Calendar calendar = DateUtil.getCalendarInstance();
 
 		if (app != null) {
 			for (JoinVerdict verdict : app.getVerdicts()) {
 				if (verdict.isInFavor()) {
 					infavor++;
 				}
 				total++;
 			}
 			calendar.setTime(app.getStartDate());
 			calendar.add(Calendar.DAY_OF_YEAR, 3);
 
 			if (app.getPrimaryGame() != null && app.getPrimaryRealm() != null) {
 				joinMessages.add(new Label("realms", app.getApplicant()
 						.getUsername()
 						+ " primarily plays "
 						+ app.getPrimaryGame().getName()
 						+ " on "
 						+ app.getPrimaryRealm().getName()));
 			} else {
 				joinMessages.add(new WebMarkupContainer("realms")
 						.setVisible(false));
 			}
 		} else {
 			joinMessages
 					.add(new WebMarkupContainer("realms").setVisible(false));
 		}
 
 		joinMessages.add(new Label("expirationTime", DateUtil
 				.getTimezoneFormattedString(calendar.getTime(),
 						getUser() != null ? getUser().getTimezone()
 								: DateUtil.NEW_YORK.getID()))
 				.setVisible(app != null));
 
 		int against = total - infavor;
 
 		joinMessages.add(new Label("senatestatus", new Model<String>(String
 				.format("%d Senators voted in favor, %d voted against",
 						infavor, against))));
 
 		joinMessages.setVisible(app != null);
 
 		add(joinMessages);
 	}
 
 	private void addMentorBox(JoinApplication app) {
 		WebMarkupContainer mentorBox = new WebMarkupContainer(
 				"mentorApplication");
 
 		boolean realmInCommon = false;
		final boolean activated = app != null ? app.getApplicant()
				.getActivation() == null : false;
 
 		if (app != null && getUser() != null) {
 			Game game = app.getPrimaryGame();
 			Realm realm = app.getPrimaryRealm();
 
 			if (game != null && realm != null) {
 				for (UserGameRealm ugr : getUser().getPlayedGames()) {
 					if (ugr.getGame().equals(game)
 							&& ugr.getRealm().equals(realm)) {
 						realmInCommon = true;
 					}
 				}
 			} else {
 				realmInCommon = true;
 				// If no realm supplied then anyone can mentor
 			}
 		}
 
 		Link<JoinApplication> yesLink = new Link<JoinApplication>("clickYes",
 				ModelMaker.wrap(app)) {
 			private static final long serialVersionUID = 1L;
 
 			@SpringBean
 			private MembershipService service;
 
 			@Override
 			public void onClick() {
 				User mentor = getUser();
 
 				service.setMentor(getModelObject(), mentor);
 
 				ForumThread thread = getModelObject().getJoinThread();
 
 				setResponsePage(new ForumThreadPage(thread.getId(), 1, true));
 			}
 
 		};
 
 		yesLink.add(new ContextImage("yes", new Model<String>(
 				"images/icons/tick.png")));
 		yesLink.add(new Label("username", app != null ? app.getApplicant()
 				.getUsername() : "-"));
 		yesLink.setVisible(realmInCommon && activated);
 		mentorBox.add(yesLink);
 
 		mentorBox
 				.add(new Label(
 						"realmWarning",
 						"You cannot be Mentor of this applicant, as you do not have a realm and game in common")
 						.setVisible(!realmInCommon));
 
 		mentorBox
 				.add(new Label(
 						"activationWarning",
 						"You cannot volunteer to mentor for this applicant until his or her account is activated")
 						.setVisible(realmInCommon && !activated));
 
 		mentorBox.setVisible(app != null
 				&& MemberUtil.canUserBeMentor(getUser())
 				&& app.getMentor() == null);
 
 		add(mentorBox);
 	}
 
	private void addSenatorBox(@Nullable JoinApplication app) {
 		boolean visible = getUser() != null
 				&& getUser().getRank() == Rank.SENATOR && app != null;
 
 		WebMarkupContainer senatorBox = new WebMarkupContainer(
 				"senatorApproval");
 		senatorBox.setVisible(visible);
 
		final boolean activated = visible ? app.getApplicant().getActivation() == null
				: false;
 
 		Boolean inFavor = null;
 		final String status;
 
 		if (app != null) {
 			for (JoinVerdict verdict : app.getVerdicts()) {
 				if (verdict.getUser().equals(getUser())) {
 					inFavor = verdict.isInFavor();
 
 				}
 
 			}
 		}
 
 		if (activated) {
 			if (inFavor != null) {
 				if (inFavor.booleanValue()) {
 					status = "You have voted in favor of this candidate";
 				} else {
 					status = "You have voted against this candidate";
 				}
 			} else {
 				status = "You have not yet cast your vote";
 			}
 
 		} else {
 			status = "You cannot vote until the applicant activates his account";
 		}
 
 		senatorBox.add(new Label("status", status));
 
 		Link<JoinApplication> yesLink = new Link<JoinApplication>("clickYes",
 				ModelMaker.wrap(app)) {
 			private static final long serialVersionUID = 1L;
 
 			@SpringBean
 			private MembershipService service;
 
 			@Override
 			public void onClick() {
 				JoinApplication _app = getModelObject();
 
 				User user = getUser();
 
 				service.setJoinApplicationVote(_app, user, true);
 
 				ForumThread thread = getModelObject().getJoinThread();
 
 				setResponsePage(new ForumThreadPage(thread.getId(), 1, true));
 			}
 
 		};
 
 		yesLink.add(new ContextImage("yes", new Model<String>(
 				"images/icons/tick.png")));
 
 		senatorBox.add(new WebMarkupContainer("question"));
 
 		senatorBox.add(yesLink);
 
 		Link<JoinApplication> noLink = new Link<JoinApplication>("clickNo",
 				ModelMaker.wrap(app)) {
 			private static final long serialVersionUID = 1L;
 
 			@SpringBean
 			private MembershipService service;
 
 			@Override
 			public void onClick() {
 				JoinApplication _app = getModelObject();
 
 				User user = getUser();
 
 				service.setJoinApplicationVote(_app, user, false);
 
 				ForumThread thread = getModelObject().getJoinThread();
 
 				setResponsePage(new ForumThreadPage(thread.getId(), 1, true));
 			}
 
 		};
 
 		noLink.add(new ContextImage("no", new Model<String>(
 				"images/icons/cross.png")));
 
 		senatorBox.add(noLink);
 
 		yesLink.setVisible(activated);
 		noLink.setVisible(activated);
 
 		add(senatorBox);
 	}
 
 	private long determineParentPageIndex(ForumThread thread) {
 
 		List<ForumThread> allThreads = forumService.fiterAndSortThreads(
 				getUser(), thread.getForum(), getUser() == null);
 
 		int index = allThreads.indexOf(thread);
 		int pos = 1 + new BigDecimal(index).divide(
 				new BigDecimal(ForumThread.POSTS_PER_PAGE), RoundingMode.FLOOR)
 				.intValue();
 		return pos;
 	}
 
 	/**
 	 * @see org.apache.wicket.Page#onDetach()
 	 */
 	@Override
 	protected void onDetach() {
 		super.onDetach();
 
 		threadModel.detach();
 	}
 }
