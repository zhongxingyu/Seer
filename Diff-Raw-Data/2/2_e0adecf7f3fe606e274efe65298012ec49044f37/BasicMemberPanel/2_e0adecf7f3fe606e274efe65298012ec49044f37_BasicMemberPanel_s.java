 /**
  * Tysan Clan Website
  * Copyright (C) 2008-2011 Jeroen Steenbeeke and Ties van de Ven
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
 
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.TimeZone;
 
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.image.ContextImage;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import com.google.common.collect.Iterables;
 import com.jeroensteenbeeke.hyperion.data.ModelMaker;
 import com.tysanclan.site.projectewok.auth.TysanMemberSecured;
 import com.tysanclan.site.projectewok.auth.TysanRankSecured;
 import com.tysanclan.site.projectewok.beans.DemocracyService;
 import com.tysanclan.site.projectewok.beans.RoleService;
 import com.tysanclan.site.projectewok.components.IconLink.DefaultClickResponder;
 import com.tysanclan.site.projectewok.components.RequiresAttentionLink.AttentionType;
 import com.tysanclan.site.projectewok.components.RequiresAttentionLink.IRequiresAttentionCondition;
 import com.tysanclan.site.projectewok.entities.AcceptanceVote;
 import com.tysanclan.site.projectewok.entities.AcceptanceVoteVerdict;
 import com.tysanclan.site.projectewok.entities.Bug;
 import com.tysanclan.site.projectewok.entities.ChancellorElection;
 import com.tysanclan.site.projectewok.entities.CompoundVote;
 import com.tysanclan.site.projectewok.entities.Event;
 import com.tysanclan.site.projectewok.entities.GamePetition;
 import com.tysanclan.site.projectewok.entities.Group;
 import com.tysanclan.site.projectewok.entities.MumbleServer;
 import com.tysanclan.site.projectewok.entities.Profile;
 import com.tysanclan.site.projectewok.entities.Rank;
 import com.tysanclan.site.projectewok.entities.RealmPetition;
 import com.tysanclan.site.projectewok.entities.Role.RoleType;
 import com.tysanclan.site.projectewok.entities.RoleTransfer;
 import com.tysanclan.site.projectewok.entities.SenateElection;
 import com.tysanclan.site.projectewok.entities.SubscriptionPayment.UnpaidFilter;
 import com.tysanclan.site.projectewok.entities.User;
 import com.tysanclan.site.projectewok.entities.UserGameRealm;
 import com.tysanclan.site.projectewok.entities.dao.AcceptanceVoteDAO;
 import com.tysanclan.site.projectewok.entities.dao.BugDAO;
 import com.tysanclan.site.projectewok.entities.dao.ConversationParticipationDAO;
 import com.tysanclan.site.projectewok.entities.dao.EventDAO;
 import com.tysanclan.site.projectewok.entities.dao.GamePetitionDAO;
 import com.tysanclan.site.projectewok.entities.dao.GroupDAO;
 import com.tysanclan.site.projectewok.entities.dao.MumbleServerDAO;
 import com.tysanclan.site.projectewok.entities.dao.NotificationDAO;
 import com.tysanclan.site.projectewok.entities.dao.ProfileDAO;
 import com.tysanclan.site.projectewok.entities.dao.RealmPetitionDAO;
 import com.tysanclan.site.projectewok.entities.dao.RoleTransferDAO;
 import com.tysanclan.site.projectewok.entities.dao.TruthsayerNominationDAO;
 import com.tysanclan.site.projectewok.entities.dao.UntenabilityVoteDAO;
 import com.tysanclan.site.projectewok.entities.dao.UserDAO;
 import com.tysanclan.site.projectewok.entities.dao.filters.EventFilter;
 import com.tysanclan.site.projectewok.entities.dao.filters.NotificationFilter;
 import com.tysanclan.site.projectewok.entities.dao.filters.RoleTransferFilter;
 import com.tysanclan.site.projectewok.entities.dao.filters.TruthsayerNominationFilter;
 import com.tysanclan.site.projectewok.entities.dao.filters.UserFilter;
 import com.tysanclan.site.projectewok.pages.member.AIMOverviewPage;
 import com.tysanclan.site.projectewok.pages.member.AcceptanceVotePage;
 import com.tysanclan.site.projectewok.pages.member.BugOverviewPage;
 import com.tysanclan.site.projectewok.pages.member.CalendarPage;
 import com.tysanclan.site.projectewok.pages.member.ChancellorElectionPage;
 import com.tysanclan.site.projectewok.pages.member.ClanStatisticsPage;
 import com.tysanclan.site.projectewok.pages.member.CreateGamePetitionPage;
 import com.tysanclan.site.projectewok.pages.member.CreateGroupPage;
 import com.tysanclan.site.projectewok.pages.member.CreateRealmPetitionPage;
 import com.tysanclan.site.projectewok.pages.member.EditAccountsPage;
 import com.tysanclan.site.projectewok.pages.member.EditUserGalleryPage;
 import com.tysanclan.site.projectewok.pages.member.EndorsementPage;
 import com.tysanclan.site.projectewok.pages.member.FeelingLuckyPage;
 import com.tysanclan.site.projectewok.pages.member.FinancePage;
 import com.tysanclan.site.projectewok.pages.member.JoinGroupPage;
 import com.tysanclan.site.projectewok.pages.member.KeyRoleNominationAcceptancePage;
 import com.tysanclan.site.projectewok.pages.member.LogPage;
 import com.tysanclan.site.projectewok.pages.member.MemberPreferencesPage;
 import com.tysanclan.site.projectewok.pages.member.MessageListPage;
 import com.tysanclan.site.projectewok.pages.member.MumbleServerStatusPage;
 import com.tysanclan.site.projectewok.pages.member.NotificationsPage;
 import com.tysanclan.site.projectewok.pages.member.OverviewPage;
 import com.tysanclan.site.projectewok.pages.member.PastElectionsPage;
 import com.tysanclan.site.projectewok.pages.member.ProposeAchievementPage;
 import com.tysanclan.site.projectewok.pages.member.RequestAchievementPage;
 import com.tysanclan.site.projectewok.pages.member.RunForChancellorPage;
 import com.tysanclan.site.projectewok.pages.member.RunForSenatorPage;
 import com.tysanclan.site.projectewok.pages.member.SenateElectionPage;
 import com.tysanclan.site.projectewok.pages.member.SignGamePetitionsPage;
 import com.tysanclan.site.projectewok.pages.member.SignRealmPetitionsPage;
 import com.tysanclan.site.projectewok.pages.member.SubscriptionPaymentPage;
 import com.tysanclan.site.projectewok.pages.member.TruthsayerAcceptancePage;
 import com.tysanclan.site.projectewok.pages.member.TruthsayerComplaintPage;
 import com.tysanclan.site.projectewok.pages.member.UntenabilityVotePage;
 import com.tysanclan.site.projectewok.pages.member.justice.StartTrialPage;
 import com.tysanclan.site.projectewok.util.DateUtil;
 import com.tysanclan.site.projectewok.util.MemberUtil;
 
 /**
  * @author Jeroen Steenbeeke
  */
 @TysanMemberSecured
 public class BasicMemberPanel extends TysanOverviewPanel<Void> {
 	public class PendingSubscriptionPayment implements
 			IRequiresAttentionCondition {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public AttentionType requiresAttention() {
 			if (Iterables.filter(getUser().getPayments(), new UnpaidFilter())
 					.iterator().hasNext()) {
 				User treasurer = roleService.getTreasurer();
 				if (treasurer != null && treasurer.getPaypalAddress() != null) {
 					return AttentionType.ERROR;
 				}
 			}
 			return null;
 		}
 
 		@Override
 		public Long getDismissableId() {
 			return null;
 		}
 	}
 
 	public class BugCondition implements IRequiresAttentionCondition {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public AttentionType requiresAttention() {
 			if (getUser().isBugReportMaster()) {
 				for (Bug b : bugDAO.findAll()) {
 					if (b.getAssignedTo() == null)
 						return AttentionType.WARNING;
 				}
 
 			}
 			return null;
 		}
 
 		@Override
 		public Long getDismissableId() {
 			return null;
 		}
 	}
 
 	public class InvitedForGroupCondition implements
 			IRequiresAttentionCondition {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public AttentionType requiresAttention() {
 			List<Group> allGroups = groupDAO.findAll();
 			allGroups.removeAll(getUser().getGroups());
 
 			for (Group g : allGroups) {
 				if (g.getInvitedMembers().contains(getUser())) {
 					return AttentionType.WARNING;
 				}
 			}
 
 			return null;
 		}
 
 		@Override
 		public Long getDismissableId() {
 			return null;
 		}
 	}
 
 	public class VoteForChancellorCondition implements
 			IRequiresAttentionCondition {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public AttentionType requiresAttention() {
 			ChancellorElection cel = democracyService
 					.getCurrentChancellorElection();
 
 			if (cel != null && !cel.isNominationOpen()) {
 				boolean found = false;
 				for (CompoundVote cp : cel.getVotes()) {
 					if (cp.getCaster().equals(getUser())) {
 						found = true;
 						break;
 					}
 				}
 
 				if (!found) {
 					return AttentionType.WARNING;
 				}
 			}
 
 			return null;
 		}
 
 		@Override
 		public Long getDismissableId() {
 			return null;
 		}
 	}
 
 	public class ElectionPreparationInProgressCondition implements
 			IRequiresAttentionCondition {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public AttentionType requiresAttention() {
 			SenateElection sel = democracyService.getCurrentSenateElection();
 			ChancellorElection cel = democracyService
 					.getCurrentChancellorElection();
 
 			if (sel != null && sel.isNominationOpen()) {
 				return AttentionType.INFO;
 			}
 			if (cel != null && cel.isNominationOpen()) {
 				return AttentionType.INFO;
 			}
 
 			return null;
 		}
 
 		@Override
 		public Long getDismissableId() {
 			SenateElection sel = democracyService.getCurrentSenateElection();
 			ChancellorElection cel = democracyService
 					.getCurrentChancellorElection();
 			if (sel != null && sel.isNominationOpen()) {
 				return sel.getId();
 			}
 			if (cel != null && cel.isNominationOpen()) {
 				return cel.getId();
 			}
 
 			return null;
 		}
 	}
 
 	public class NeverTrueCondition implements IRequiresAttentionCondition {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public AttentionType requiresAttention() {
 			return null;
 		}
 
 		@Override
 		public Long getDismissableId() {
 			return null;
 		}
 	}
 
 	public class VoteForSenatorCondition implements IRequiresAttentionCondition {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public AttentionType requiresAttention() {
 			SenateElection sel = democracyService.getCurrentSenateElection();
 
 			if (sel != null && !sel.isNominationOpen()) {
 				boolean found = false;
 				for (CompoundVote cp : sel.getVotes()) {
 					if (cp.getCaster().equals(getUser())) {
 						found = true;
 						break;
 					}
 				}
 
 				if (!found) {
 					return AttentionType.WARNING;
 				}
 			}
 
 			return null;
 		}
 
 		@Override
 		public Long getDismissableId() {
 			return null;
 		}
 	}
 
 	public class RunForSenatorCondition implements IRequiresAttentionCondition {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public AttentionType requiresAttention() {
 			SenateElection sel = democracyService.getCurrentSenateElection();
 
 			if (sel != null) {
 
 				boolean canRun = MemberUtil.isEligibleForElectedRank(getUser(),
 						Rank.SENATOR)
 						&& !sel.getCandidates().contains(getUser());
 
 				if (canRun) {
 					return AttentionType.INFO;
 				}
 			}
 
 			return null;
 		}
 
 		@Override
 		public Long getDismissableId() {
 			return democracyService.getCurrentSenateElection() != null ? democracyService
 					.getCurrentSenateElection().getId() : null;
 		}
 	}
 
 	public class RunForChancellorCondition implements
 			IRequiresAttentionCondition {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public AttentionType requiresAttention() {
 			ChancellorElection cel = democracyService
 					.getCurrentChancellorElection();
 
 			if (cel != null) {
 
 				boolean canRun = MemberUtil.isEligibleForElectedRank(getUser(),
 						Rank.CHANCELLOR)
 						&& !cel.getCandidates().contains(getUser());
 
 				if (canRun) {
 					return AttentionType.INFO;
 				}
 			}
 
 			return null;
 		}
 
 		@Override
 		public Long getDismissableId() {
 			return democracyService.getCurrentChancellorElection() != null ? democracyService
 					.getCurrentChancellorElection().getId() : null;
 		}
 	}
 
 	private static final long serialVersionUID = 1L;
 
 	@SpringBean
 	private AcceptanceVoteDAO acceptanceVoteDAO;
 
 	@SpringBean
 	private DemocracyService democracyService;
 
 	@SpringBean
 	private GamePetitionDAO gamePetitionDAO;
 
 	@SpringBean
 	private RealmPetitionDAO realmPetitionDAO;
 
 	@SpringBean
 	private UntenabilityVoteDAO untenabilityVoteDAO;
 
 	@SpringBean
 	private TruthsayerNominationDAO truthsayerNominationDAO;
 
 	@SpringBean
 	private UserDAO userDAO;
 
 	@SpringBean
 	private ProfileDAO profileDAO;
 
 	@SpringBean
 	private NotificationDAO notificationDAO;
 
 	@SpringBean
 	private EventDAO eventDAO;
 
 	@SpringBean
 	private ConversationParticipationDAO participationDAO;
 
 	@SpringBean
 	private GroupDAO groupDAO;
 
 	@SpringBean
 	private MumbleServerDAO serverDAO;
 
 	@SpringBean
 	private RoleTransferDAO roleTransferDAO;
 
 	@SpringBean
 	private BugDAO bugDAO;
 
 	@SpringBean
 	private RoleService roleService;
 
 	/**
 	 * 
 	 */
 	public BasicMemberPanel(String id, User user) {
 		super(id, "Basic Overview");
 
 		addKeyRoleLinks(user);
 		addEmailChangeConfirmationPanel(user);
 		addMessagesLink();
 		addLogLink();
 		addJoinGroupLink(user);
 		addCreateGroupLink(user);
 		addFinanceLink();
 		addStatsLink();
 		addCalendarLink();
 		addAcceptanceVoteLink(user);
 		addTruthsayerAcceptanceLink(user);
 		addEndorsementLink(user);
 		addRunForSenatorLink(user);
 		addRunForChancellorLink(user);
 		addChancellorElectionLink(user);
 		addSenateElectionLink(user);
 		addPreferencesLink();
 		addUntenabilityVoteLink(user);
 		addStartTrialLink(user);
 		addPastElectionsLink();
 		addAIMOverviewLink();
 		addNotificationsLink(user);
 		addCreateGamePetitionLink(user);
 		addCreateRealmPetitionLink(user);
 		addSignGamePetitionLink(user);
 		addSignRealmPetitionLink(user);
 		addEditAccountsPage();
 		addBirthdays();
 		addAprilFools2010();
 		addGalleryLink(user);
 		addAchievementLinks(user);
 		addBugLink();
 		addSubscriptionPaymentLink();
 
 		add(new ListView<MumbleServer>("servers", ModelMaker.wrap(serverDAO
 				.findAll())) {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void populateItem(ListItem<MumbleServer> item) {
 				MumbleServer server = item.getModelObject();
 				Link<MumbleServer> link = new Link<MumbleServer>("link",
 						ModelMaker.wrap(server)) {
 
 					private static final long serialVersionUID = 1L;
 
 					@Override
 					public void onClick() {
 						setResponsePage(new MumbleServerStatusPage(
 								getModelObject()));
 					}
 
 				};
 
 				link.add(new Label("name", server.getName())
 						.setRenderBodyOnly(true));
 
 				item.add(link);
 
 			}
 		});
 	}
 
 	private void addSubscriptionPaymentLink() {
 		add(createConditionalVisibilityLink("pendingSubscription",
 				SubscriptionPaymentPage.class, "Subscription Payment",
 				new PendingSubscriptionPayment()));
 	}
 
 	private void addKeyRoleLinks(User user) {
 		RoleTransferFilter filter = new RoleTransferFilter();
 		filter.setAccepted(false);
 		filter.setUser(user);
 		filter.setRoleType(RoleType.HERALD);
 
 		addRoleLink(filter, "heraldAccept");
 		filter.setRoleType(RoleType.STEWARD);
 		addRoleLink(filter, "stewardAccept");
 		filter.setRoleType(RoleType.TREASURER);
 		addRoleLink(filter, "treasurerAccept");
 
 	}
 
 	/**
 	 * @param filter
 	 * @param id
 	 */
 	private void addRoleLink(RoleTransferFilter filter, String id) {
 		RoleTransfer transfer = roleTransferDAO.getUniqueByFilter(filter);
 
 		add(new IconLink.Builder("images/icons/delete.png",
 				new DefaultClickResponder<RoleTransfer>(
 						ModelMaker.wrap(transfer)) {
 					private static final long serialVersionUID = 1L;
 
 					@Override
 					public void onClick() {
 						setResponsePage(new KeyRoleNominationAcceptancePage(
 								getModelObject()));
 					}
 				})
 				.setText(
 						(transfer != null ? transfer.getRoleType().toString()
 								: "") + " nomination").newInstance(id)
 				.setVisible(transfer != null));
 	}
 
 	private void addBugLink() {
 		add(createLink("bugs", BugOverviewPage.class, "Bugs Reports",
 				new BugCondition()));
 		add(createLink("newbug", ReportBugPage.class, "Report new bug",
 				new BugCondition()));
 		add(createLink("features", FeatureOverviewPage.class,
				"FeatureRequests", NeverTrueCondition.get()));
 		add(createLink("newfeature", RequestFeaturePage.class,
 				"Request new feature", NeverTrueCondition.get()));
 	}
 
 	private void addGalleryLink(User user) {
 		add(new Link<User>("gallery", ModelMaker.wrap(user)) {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick() {
 				setResponsePage(new EditUserGalleryPage(getModelObject()));
 			}
 
 		});
 	}
 
 	/**
 	 * 
 	 */
 	private void addAprilFools2010() {
 		add(createConditionalVisibilityLink("lucky", FeelingLuckyPage.class,
 				"Feeling Lucky?", new AprilFools2010Condition()));
 
 	}
 
 	/**
 	 * 
 	 */
 	private void addBirthdays() {
 
 		List<Profile> _profiles = profileDAO.findAll();
 		List<Profile> profiles = new LinkedList<Profile>();
 
 		Calendar cal = DateUtil.getCalendarInstance();
 
 		String tzName = getUser().getTimezone();
 
 		if (tzName == null) {
 			tzName = DateUtil.NEW_YORK.getID();
 		}
 
 		for (Profile p : _profiles) {
 			if (p.getBirthDate() != null && MemberUtil.isMember(p.getUser())) {
 				Calendar cal2 = DateUtil.getMidnightCalendarByUnadjustedDate(
 						p.getBirthDate(), TimeZone.getTimeZone(tzName));
 				if (cal.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
 						&& cal.get(Calendar.DAY_OF_MONTH) == cal2
 								.get(Calendar.DAY_OF_MONTH)) {
 					profiles.add(p);
 				}
 			}
 		}
 
 		add(new WebMarkupContainer("birthdays").setVisible(!profiles.isEmpty()));
 
 		add(new ListView<Profile>("users", ModelMaker.wrap(profiles)) {
 			private static final long serialVersionUID = 1L;
 
 			/**
 			 * @see org.apache.wicket.markup.html.list.ListView#populateItem(org.apache.wicket.markup.html.list.ListItem)
 			 */
 			@Override
 			protected void populateItem(ListItem<Profile> item) {
 				Profile profile = item.getModelObject();
 				item.add(new MemberListItem("user", profile.getUser()));
 
 			}
 		});
 
 	}
 
 	/**
 	 * 
 	 */
 
 	private void addEditAccountsPage() {
 		add(createLink("accounts", EditAccountsPage.class,
 				"Games played / Accounts",
 				new UserDoesNotHaveAccountsCondition()));
 	}
 
 	private void addSignGamePetitionLink(User user) {
 		add(createConditionalVisibilityLink("signgamepetition",
 				SignGamePetitionsPage.class, "Sign Game Petition",
 				new UnsignedGamePetitionCondition()));
 	}
 
 	private void addSignRealmPetitionLink(User user) {
 		add(createConditionalVisibilityLink("signrealmpetition",
 				SignRealmPetitionsPage.class, "Sign Realm Petition",
 				new UnsignedRealmPetitionCondition()));
 	}
 
 	private void addCreateGamePetitionLink(User user) {
 		Link<?> petitionLink = new Link<Void>("creategamepetition") {
 			private static final long serialVersionUID = 1L;
 
 			/**
 			 * @see org.apache.wicket.markup.html.link.Link#onClick()
 			 */
 			@Override
 			public void onClick() {
 				setResponsePage(new CreateGamePetitionPage());
 			}
 		};
 
 		TysanRankSecured secured = CreateGamePetitionPage.class
 				.getAnnotation(TysanRankSecured.class);
 		boolean visible = false;
 
 		if (secured != null) {
 			for (Rank rank : secured.value()) {
 				if (rank == user.getRank()) {
 					visible = true;
 				}
 			}
 		}
 
 		petitionLink.setVisible(visible);
 
 		add(petitionLink);
 
 	}
 
 	private void addCreateRealmPetitionLink(User user) {
 		Link<?> petitionLink = new Link<Void>("createrealmpetition") {
 			private static final long serialVersionUID = 1L;
 
 			/**
 			 * @see org.apache.wicket.markup.html.link.Link#onClick()
 			 */
 			@Override
 			public void onClick() {
 				setResponsePage(new CreateRealmPetitionPage());
 			}
 		};
 
 		TysanRankSecured secured = CreateRealmPetitionPage.class
 				.getAnnotation(TysanRankSecured.class);
 		boolean visible = false;
 
 		if (secured != null) {
 			for (Rank rank : secured.value()) {
 				if (rank == user.getRank()) {
 					visible = true;
 				}
 			}
 		}
 
 		petitionLink.setVisible(visible);
 
 		add(petitionLink);
 
 	}
 
 	private void addNotificationsLink(User user) {
 		add(createLink("notification", NotificationsPage.class,
 				"Notifications", new ActiveNotificationCondition()));
 	}
 
 	/**
 	 * 
 	 */
 	private void addAIMOverviewLink() {
 		add(new Link<Void>("aim") {
 			private static final long serialVersionUID = 1L;
 
 			/**
 			 * @see org.apache.wicket.markup.html.link.Link#onClick()
 			 */
 			@Override
 			public void onClick() {
 				setResponsePage(new AIMOverviewPage());
 
 			}
 		});
 	}
 
 	public void addPastElectionsLink() {
 		add(new Link<Void>("pastelections") {
 			private static final long serialVersionUID = 1L;
 
 			/**
 			 * @see org.apache.wicket.markup.html.link.Link#onClick()
 			 */
 			@Override
 			public void onClick() {
 				setResponsePage(new PastElectionsPage());
 
 			}
 		});
 	}
 
 	/**
 	 * 
 	 */
 	private void addStartTrialLink(User user) {
 		Link<?> trialLink = new Link<Void>("trial") {
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = 1L;
 
 			/**
 			 * @see org.apache.wicket.markup.html.link.Link#onClick()
 			 */
 			@Override
 			public void onClick() {
 				setResponsePage(new StartTrialPage());
 
 			}
 		};
 
 		Link<?> complaintLink = new Link<Void>("complaint") {
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick() {
 				setResponsePage(new TruthsayerComplaintPage());
 			}
 		};
 
 		UserFilter filter = new UserFilter();
 		filter.addRank(Rank.TRUTHSAYER);
 		List<User> truthsayers = userDAO.findByFilter(filter);
 
 		trialLink.setVisible(!truthsayers.contains(user)
 				|| truthsayers.size() > 1);
 		complaintLink.setVisible(!truthsayers.contains(user)
 				|| truthsayers.size() > 1);
 
 		add(trialLink);
 		add(complaintLink);
 	}
 
 	/**
 	 * 
 	 */
 	private void addUntenabilityVoteLink(User user) {
 		IconLink untenabilityVoteLink = new IconLink.Builder(
 				"images/icons/error.png", new DefaultClickResponder<Void>() {
 					private static final long serialVersionUID = 1L;
 
 					/**
 					 * @see com.tysanclan.site.projectewok.components.IconLink.DefaultClickResponder#onClick()
 					 */
 					@Override
 					public void onClick() {
 						setResponsePage(new UntenabilityVotePage());
 					}
 				}).setText("Untenable Regulations").newInstance("untenable");
 
 		untenabilityVoteLink.setVisible(untenabilityVoteDAO.countAll() > 0
 				&& MemberUtil.canUserVote(user));
 
 		add(untenabilityVoteLink);
 
 	}
 
 	/**
 	 * 
 	 */
 	private void addPreferencesLink() {
 		Link<User> preferencesLink = new Link<User>("preferences") {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick() {
 				setResponsePage(new MemberPreferencesPage());
 
 			}
 
 		};
 
 		add(preferencesLink);
 
 	}
 
 	private void addTruthsayerAcceptanceLink(User user) {
 		Link<?> acceptLink = new Link<Void>("accepttruthsayer") {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick() {
 				setResponsePage(new TruthsayerAcceptancePage());
 			}
 
 		};
 
 		TruthsayerNominationFilter filter = new TruthsayerNominationFilter();
 		filter.setNominee(user);
 		filter.setStartNotSet(true);
 
 		acceptLink.add(new ContextImage("icon", "images/icons/error.png"));
 
 		acceptLink
 				.setVisible(truthsayerNominationDAO.countByFilter(filter) > 0);
 
 		add(acceptLink);
 
 	}
 
 	/**
 	 * 
 	 */
 	private void addCreateGroupLink(User user) {
 		Link<?> createLink = new Link<Void>("creategroup") {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick() {
 				setResponsePage(new CreateGroupPage());
 
 			}
 
 		};
 
 		TysanRankSecured secured = CreateGroupPage.class
 				.getAnnotation(TysanRankSecured.class);
 		boolean visible = true;
 
 		if (secured != null) {
 			visible = false;
 			for (Rank rank : secured.value()) {
 				if (rank == user.getRank()) {
 					visible = true;
 				}
 			}
 		}
 
 		createLink.setVisible(visible);
 
 		add(createLink);
 
 	}
 
 	/**
 	 * 
 	 */
 	private void addJoinGroupLink(User user) {
 		add(createLink("joingroup", JoinGroupPage.class, "Join Group",
 				new InvitedForGroupCondition()));
 	}
 
 	/**
 	 * 
 	 */
 	private void addChancellorElectionLink(User user) {
 		ChancellorElection e = democracyService.getCurrentChancellorElection();
 
 		add(createConditionalVisibilityLink("chancellorElection",
 				ModelMaker.wrap(e), ChancellorElectionPage.class,
 				"Chancellor Election", new VoteForChancellorCondition()));
 	}
 
 	private void addSenateElectionLink(User user) {
 		add(createConditionalVisibilityLink("senateElection",
 				ModelMaker.wrap(democracyService.getCurrentSenateElection()),
 				SenateElectionPage.class, "Senate Election",
 				new VoteForSenatorCondition()));
 
 	}
 
 	private void addRunForChancellorLink(User user) {
 		add(createConditionalVisibilityLink("runforchancellor",
 				RunForChancellorPage.class, "Run for Chancellor",
 				new RunForChancellorCondition()));
 	}
 
 	private void addRunForSenatorLink(User user) {
 		add(createConditionalVisibilityLink("runforsenator",
 				RunForSenatorPage.class, "Run for Senator",
 				new RunForSenatorCondition()));
 	}
 
 	private void addEndorsementLink(User user) {
 		if (user.getEndorses() == null || user.getEndorsesForSenate() == null) {
 			add(createLink("endorsement", EndorsementPage.class,
 					"Endorsements",
 					new ElectionPreparationInProgressCondition()).setVisible(
 					MemberUtil.canUserGrantEndorsement(user)));
 		} else {
 			add(createLink("endorsement", EndorsementPage.class,
 					"Endorsements", new NeverTrueCondition()).setVisible(
 					MemberUtil.canUserGrantEndorsement(user)));
 		}
 
 	}
 
 	private void addAcceptanceVoteLink(User user) {
 		add(createLink("votes", AcceptanceVotePage.class, "Acceptance Votes",
 				new UserHasNotVotedInAcceptanceVoteCondition()));
 	}
 
 	/**
 	 * 
 	 */
 	private void addEmailChangeConfirmationPanel(User user) {
 		add(new EmailChangeConfirmationPanel("emailchange", user) {
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onConfirmed() {
 				setResponsePage(new OverviewPage());
 
 			}
 
 			@Override
 			public void onCancel() {
 				setResponsePage(new OverviewPage());
 
 			}
 		});
 	}
 
 	/**
 	 * 
 	 */
 	private void addCalendarLink() {
 		add(createLink("calendar", CalendarPage.class, "Calendar",
 				new EventTodayCondition()));
 	}
 
 	/**
 	 * 
 	 */
 	private void addFinanceLink() {
 		add(new Link<User>("finance") {
 			private static final long serialVersionUID = 1L;
 
 			/**
 			 * @see org.apache.wicket.markup.html.link.Link#onClick()
 			 */
 			@Override
 			public void onClick() {
 				setResponsePage(new FinancePage());
 
 			}
 		});
 
 	}
 
 	private void addStatsLink() {
 		add(new Link<User>("clanstats") {
 			private static final long serialVersionUID = 1L;
 
 			/**
 			 * @see org.apache.wicket.markup.html.link.Link#onClick()
 			 */
 			@Override
 			public void onClick() {
 				setResponsePage(new ClanStatisticsPage());
 
 			}
 		});
 
 	}
 
 	/**
 	 * 
 	 */
 	private void addMessagesLink() {
 		add(createLink("messages", MessageListPage.class, "Messages",
 				new UnreadMessageCondition()));
 	}
 
 	private void addAchievementLinks(User user) {
 		add(new Link<User>("requestAchievement") {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick() {
 				setResponsePage(new RequestAchievementPage());
 			}
 		});
 
 		add(new Link<User>("proposeAchievement") {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick() {
 				setResponsePage(new ProposeAchievementPage());
 			}
 		}.setVisible(MemberUtil.hasPermission(user,
 				ProposeAchievementPage.class)));
 	}
 
 	private void addLogLink() {
 		add(new Link<User>("log") {
 
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = 1L;
 
 			/**
 			 * @see org.apache.wicket.markup.html.link.Link#onClick()
 			 */
 			@Override
 			public void onClick() {
 				setResponsePage(new LogPage());
 
 			}
 		});
 	}
 
 	private class EventTodayCondition implements IRequiresAttentionCondition {
 		private static final long serialVersionUID = 1L;
 
 		private Event getNextEvent() {
 			EventFilter filter = new EventFilter();
 			filter.setDate(DateUtil.getMidnightCalendarInstance().getTime());
 
 			if (eventDAO.countByFilter(filter) > 0) {
 				return eventDAO.findByFilter(filter).get(0);
 			}
 
 			return null;
 		}
 
 		@Override
 		public AttentionType requiresAttention() {
 			if (getNextEvent() != null) {
 				return AttentionType.WARNING;
 			}
 
 			return null;
 		}
 
 		@Override
 		public Long getDismissableId() {
 			Event event = getNextEvent();
 
 			if (event != null) {
 				return event.getId();
 			}
 
 			return null;
 		}
 	}
 
 	private class UnreadMessageCondition implements IRequiresAttentionCondition {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public AttentionType requiresAttention() {
 			if (participationDAO.countUnreadMessages(getUser()) > 0) {
 				return AttentionType.WARNING;
 			}
 
 			return null;
 		}
 
 		@Override
 		public Long getDismissableId() {
 			return null;
 		}
 	}
 
 	private class ActiveNotificationCondition implements
 			IRequiresAttentionCondition {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public AttentionType requiresAttention() {
 			NotificationFilter nfilter = new NotificationFilter();
 			nfilter.setUser(getUser());
 
 			if (notificationDAO.countByFilter(nfilter) > 0) {
 				return AttentionType.WARNING;
 			}
 
 			return null;
 		}
 
 		@Override
 		public Long getDismissableId() {
 			return null;
 		}
 	}
 
 	private class AprilFools2010Condition implements
 			IRequiresAttentionCondition {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public AttentionType requiresAttention() {
 			Calendar cal = DateUtil.getCalendarInstance();
 			if (cal.get(Calendar.YEAR) == 2010 && cal.get(Calendar.MONTH) == 3
 					&& cal.get(Calendar.DAY_OF_MONTH) < 4) {
 				return AttentionType.INFO;
 			}
 
 			return null;
 		}
 
 		@Override
 		public Long getDismissableId() {
 			return 1L;
 		}
 	}
 
 	private class UnsignedGamePetitionCondition implements
 			IRequiresAttentionCondition {
 		private static final long serialVersionUID = 1L;
 
 		private GamePetition petition;
 
 		private GamePetition getPetition() {
 			if (petition == null) {
 				if (gamePetitionDAO.countAll() > 0
 						&& Arrays.asList(
 								new Rank[] { Rank.CHANCELLOR, Rank.SENATOR,
 										Rank.TRUTHSAYER, Rank.REVERED_MEMBER,
 										Rank.SENIOR_MEMBER, Rank.FULL_MEMBER,
 										Rank.JUNIOR_MEMBER }).contains(
 								getUser().getRank())) {
 					outer: for (GamePetition gp : gamePetitionDAO.findAll()) {
 
 						if (getUser().equals(gp.getRequester())) {
 							continue;
 						}
 						for (User signature : gp.getSignatures()) {
 							if (getUser().equals(signature)) {
 								continue outer;
 							}
 						}
 
 						petition = gp;
 
 					}
 				}
 			}
 
 			return petition;
 		}
 
 		@Override
 		public AttentionType requiresAttention() {
 			if (getPetition() != null) {
 				return AttentionType.WARNING;
 			}
 
 			return null;
 		}
 
 		@Override
 		public Long getDismissableId() {
 			if (getPetition() != null) {
 				return getPetition().getId();
 			}
 
 			return null;
 		}
 
 	}
 
 	private class UnsignedRealmPetitionCondition implements
 			IRequiresAttentionCondition {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 		private RealmPetition petition;
 
 		private RealmPetition getPetition() {
 			if (petition == null) {
 				if (realmPetitionDAO.countAll() > 0
 						&& Arrays.asList(
 								new Rank[] { Rank.CHANCELLOR, Rank.SENATOR,
 										Rank.TRUTHSAYER, Rank.REVERED_MEMBER,
 										Rank.SENIOR_MEMBER, Rank.FULL_MEMBER,
 										Rank.JUNIOR_MEMBER }).contains(
 								getUser().getRank())) {
 					outer: for (RealmPetition rp : realmPetitionDAO.findAll()) {
 
 						if (getUser().equals(rp.getRequester())) {
 							continue;
 						}
 						for (User signature : rp.getSignatures()) {
 							if (getUser().equals(signature)) {
 								continue outer;
 							}
 						}
 
 						petition = rp;
 
 					}
 				}
 
 			}
 
 			return petition;
 		}
 
 		@Override
 		public AttentionType requiresAttention() {
 			if (getPetition() != null) {
 				return AttentionType.WARNING;
 			}
 
 			return null;
 		}
 
 		@Override
 		public Long getDismissableId() {
 			if (getPetition() != null) {
 				return getPetition().getId();
 			}
 
 			return null;
 		}
 	}
 
 	private class UserDoesNotHaveAccountsCondition implements
 			IRequiresAttentionCondition {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public AttentionType requiresAttention() {
 			int countGames = 0, countAccounts = 0;
 			for (UserGameRealm ugr : getUser().getPlayedGames()) {
 				countGames++;
 				if (ugr.getAccounts().isEmpty()) {
 					countAccounts++;
 				}
 			}
 
 			if (countGames == 0 || countAccounts >= 1) {
 				return AttentionType.WARNING;
 			}
 
 			return null;
 		}
 
 		@Override
 		public Long getDismissableId() {
 			return null;
 		}
 	}
 
 	private class UserHasNotVotedInAcceptanceVoteCondition implements
 			IRequiresAttentionCondition {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public AttentionType requiresAttention() {
 			List<AcceptanceVote> votes = acceptanceVoteDAO.findAll();
 
 			if (!votes.isEmpty() && MemberUtil.canUserVote(getUser())) {
 
 				for (AcceptanceVote vote : votes) {
 					boolean found = false;
 					for (AcceptanceVoteVerdict verdict : vote.getVerdicts()) {
 						if (verdict.getCaster().equals(getUser())) {
 
 							found = true;
 						}
 					}
 
 					if (!found) {
 						return AttentionType.WARNING;
 					}
 				}
 
 			}
 
 			return null;
 		}
 
 		@Override
 		public Long getDismissableId() {
 			return null;
 		}
 	}
 }
