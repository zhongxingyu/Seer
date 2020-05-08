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
 package com.tysanclan.site.projectewok.beans.impl;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.joda.time.LocalDate;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.jeroensteenbeeke.hyperion.events.IEventDispatcher;
 import com.tysanclan.site.projectewok.beans.MailService;
 import com.tysanclan.site.projectewok.entities.AcceptanceVote;
 import com.tysanclan.site.projectewok.entities.AcceptanceVoteVerdict;
 import com.tysanclan.site.projectewok.entities.ChancellorElection;
 import com.tysanclan.site.projectewok.entities.CompoundVote;
 import com.tysanclan.site.projectewok.entities.CompoundVoteChoice;
 import com.tysanclan.site.projectewok.entities.Donation;
 import com.tysanclan.site.projectewok.entities.Election;
 import com.tysanclan.site.projectewok.entities.Group;
 import com.tysanclan.site.projectewok.entities.GroupLeaderElection;
 import com.tysanclan.site.projectewok.entities.Impeachment;
 import com.tysanclan.site.projectewok.entities.ImpeachmentVote;
 import com.tysanclan.site.projectewok.entities.JoinApplication;
 import com.tysanclan.site.projectewok.entities.JoinVerdict;
 import com.tysanclan.site.projectewok.entities.Rank;
 import com.tysanclan.site.projectewok.entities.Regulation;
 import com.tysanclan.site.projectewok.entities.RegulationChange;
 import com.tysanclan.site.projectewok.entities.RegulationChange.ChangeType;
 import com.tysanclan.site.projectewok.entities.RegulationChangeVote;
 import com.tysanclan.site.projectewok.entities.SenateElection;
 import com.tysanclan.site.projectewok.entities.UntenabilityVote;
 import com.tysanclan.site.projectewok.entities.UntenabilityVoteChoice;
 import com.tysanclan.site.projectewok.entities.User;
 import com.tysanclan.site.projectewok.entities.dao.AcceptanceVoteDAO;
 import com.tysanclan.site.projectewok.entities.dao.AcceptanceVoteVerdictDAO;
 import com.tysanclan.site.projectewok.entities.dao.ChancellorElectionDAO;
 import com.tysanclan.site.projectewok.entities.dao.CompoundVoteChoiceDAO;
 import com.tysanclan.site.projectewok.entities.dao.CompoundVoteDAO;
 import com.tysanclan.site.projectewok.entities.dao.DonationDAO;
 import com.tysanclan.site.projectewok.entities.dao.GroupDAO;
 import com.tysanclan.site.projectewok.entities.dao.GroupLeaderElectionDAO;
 import com.tysanclan.site.projectewok.entities.dao.ImpeachmentDAO;
 import com.tysanclan.site.projectewok.entities.dao.ImpeachmentVoteDAO;
 import com.tysanclan.site.projectewok.entities.dao.JoinApplicationDAO;
 import com.tysanclan.site.projectewok.entities.dao.RegulationChangeDAO;
 import com.tysanclan.site.projectewok.entities.dao.RegulationChangeVoteDAO;
 import com.tysanclan.site.projectewok.entities.dao.RegulationDAO;
 import com.tysanclan.site.projectewok.entities.dao.SenateElectionDAO;
 import com.tysanclan.site.projectewok.entities.dao.UntenabilityVoteChoiceDAO;
 import com.tysanclan.site.projectewok.entities.dao.UntenabilityVoteDAO;
 import com.tysanclan.site.projectewok.entities.dao.UserDAO;
 import com.tysanclan.site.projectewok.entities.dao.filters.AcceptanceVoteFilter;
 import com.tysanclan.site.projectewok.entities.dao.filters.ChancellorElectionFilter;
 import com.tysanclan.site.projectewok.entities.dao.filters.DonationFilter;
 import com.tysanclan.site.projectewok.entities.dao.filters.GroupLeaderElectionFilter;
 import com.tysanclan.site.projectewok.entities.dao.filters.JoinApplicationFilter;
 import com.tysanclan.site.projectewok.entities.dao.filters.RegulationChangeFilter;
 import com.tysanclan.site.projectewok.entities.dao.filters.SenateElectionFilter;
 import com.tysanclan.site.projectewok.entities.dao.filters.UntenabilityVoteFilter;
 import com.tysanclan.site.projectewok.entities.dao.filters.UserFilter;
 import com.tysanclan.site.projectewok.event.MemberStatusEvent;
 import com.tysanclan.site.projectewok.util.DateUtil;
 import com.tysanclan.site.projectewok.util.HTMLSanitizer;
 import com.tysanclan.site.projectewok.util.MemberUtil;
 
 /**
  * @author Jeroen Steenbeeke
  */
 @Component
 @Scope("request")
 class DemocracyServiceImpl implements
 		com.tysanclan.site.projectewok.beans.DemocracyService {
 	@Autowired
 	private ChancellorElectionDAO chancellorElectionDAO;
 
 	@Autowired
 	private SenateElectionDAO senateElectionDAO;
 
 	@Autowired
 	private GroupLeaderElectionDAO groupLeaderElectionDAO;
 
 	@Autowired
 	private UserDAO userDAO;
 
 	@Autowired
 	private GroupDAO groupDAO;
 
 	@Autowired
 	private AcceptanceVoteDAO acceptanceVoteDAO;
 
 	@Autowired
 	private AcceptanceVoteVerdictDAO acceptanceVoteVerdictDAO;
 
 	@Autowired
 	private CompoundVoteDAO compoundVoteDAO;
 	@Autowired
 	private CompoundVoteChoiceDAO compoundVoteChoiceDAO;
 
 	@Autowired
 	private JoinApplicationDAO joinApplicationDAO;
 
 	@Autowired
 	private ImpeachmentDAO impeachmentDAO;
 
 	@Autowired
 	private ImpeachmentVoteDAO impeachmentVoteDAO;
 
 	@Autowired
 	private UntenabilityVoteDAO untenabilityVoteDAO;
 
 	@Autowired
 	private UntenabilityVoteChoiceDAO untenabilityVoteChoiceDAO;
 
 	@Autowired
 	private RegulationDAO regulationDAO;
 
 	@Autowired
 	private RegulationChangeDAO regulationChangeDAO;
 
 	@Autowired
 	private RegulationChangeVoteDAO regulationChangeVoteDAO;
 
 	@Autowired
 	private DonationDAO donationDAO;
 
 	@Autowired
 	private com.tysanclan.site.projectewok.beans.MailService mailService;
 
 	@Autowired
 	private com.tysanclan.site.projectewok.beans.UserService userService;
 
 	@Autowired
 	private com.tysanclan.site.projectewok.beans.LogService logService;
 
 	@Autowired
 	private com.tysanclan.site.projectewok.beans.NotificationService notificationService;
 
 	@Autowired
 	private IEventDispatcher dispatcher;
 
 	public void setDispatcher(IEventDispatcher dispatcher) {
 		this.dispatcher = dispatcher;
 	}
 
 	/**
 	 * @param donationDAO
 	 *            the donationDAO to set
 	 */
 	public void setDonationDAO(DonationDAO donationDAO) {
 		this.donationDAO = donationDAO;
 	}
 
 	public void setNotificationService(
 			com.tysanclan.site.projectewok.beans.NotificationService notificationService) {
 		this.notificationService = notificationService;
 	}
 
 	/**
 	 * @param regulationChangeVoteDAO
 	 *            the regulationChangeVoteDAO to set
 	 */
 	public void setRegulationChangeVoteDAO(
 			RegulationChangeVoteDAO regulationChangeVoteDAO) {
 		this.regulationChangeVoteDAO = regulationChangeVoteDAO;
 	}
 
 	public void setMembershipService(
 			com.tysanclan.site.projectewok.beans.UserService userService) {
 		this.userService = userService;
 	}
 
 	/**
 	 * @param userDAO
 	 *            the userDAO to set
 	 */
 	public void setUserDAO(UserDAO userDAO) {
 		this.userDAO = userDAO;
 	}
 
 	/**
 	 * @param compoundVoteDAO
 	 *            the compoundVoteDAO to set
 	 */
 	public void setCompoundVoteDAO(CompoundVoteDAO compoundVoteDAO) {
 		this.compoundVoteDAO = compoundVoteDAO;
 	}
 
 	/**
 	 * @param compoundVoteChoiceDAO
 	 *            the compoundVoteChoiceDAO to set
 	 */
 	public void setCompoundVoteChoiceDAO(
 			CompoundVoteChoiceDAO compoundVoteChoiceDAO) {
 		this.compoundVoteChoiceDAO = compoundVoteChoiceDAO;
 	}
 
 	/**
 	 * @param acceptanceVoteDAO
 	 *            the acceptanceVoteDAO to set
 	 */
 	public void setAcceptanceVoteDAO(AcceptanceVoteDAO acceptanceVoteDAO) {
 		this.acceptanceVoteDAO = acceptanceVoteDAO;
 	}
 
 	/**
 	 * @param joinApplicationDAO
 	 *            the joinApplicationDAO to set
 	 */
 	public void setJoinApplicationDAO(JoinApplicationDAO joinApplicationDAO) {
 		this.joinApplicationDAO = joinApplicationDAO;
 	}
 
 	/**
 	 * @param mailService
 	 *            the mailService to set
 	 */
 	public void setMailService(MailService mailService) {
 		this.mailService = mailService;
 	}
 
 	/**
 	 * @param groupDAO
 	 *            the groupDAO to set
 	 */
 	public void setGroupDAO(GroupDAO groupDAO) {
 		this.groupDAO = groupDAO;
 	}
 
 	/**
 	 * @param impeachmentDAO
 	 *            the impeachmentDAO to set
 	 */
 	public void setImpeachmentDAO(ImpeachmentDAO impeachmentDAO) {
 		this.impeachmentDAO = impeachmentDAO;
 	}
 
 	/**
 	 * @param impeachmentVoteDAO
 	 *            the impeachmentVoteDAO to set
 	 */
 	public void setImpeachmentVoteDAO(ImpeachmentVoteDAO impeachmentVoteDAO) {
 		this.impeachmentVoteDAO = impeachmentVoteDAO;
 	}
 
 	/**
 	 * @param logService
 	 *            the logService to set
 	 */
 	public void setLogService(
 			com.tysanclan.site.projectewok.beans.LogService logService) {
 		this.logService = logService;
 	}
 
 	/**
 	 * @param acceptanceVoteVerdictDAO
 	 *            the acceptanceVoteVerdictDAO to set
 	 */
 	public void setAcceptanceVoteVerdictDAO(
 			AcceptanceVoteVerdictDAO acceptanceVoteVerdictDAO) {
 		this.acceptanceVoteVerdictDAO = acceptanceVoteVerdictDAO;
 	}
 
 	/**
 	 * @param userService
 	 *            the userService to set
 	 */
 	public void setUserService(
 			com.tysanclan.site.projectewok.beans.UserService userService) {
 		this.userService = userService;
 	}
 
 	/**
 	 * @param regulationDAO
 	 *            the regulationDAO to set
 	 */
 	public void setRegulationDAO(RegulationDAO regulationDAO) {
 		this.regulationDAO = regulationDAO;
 	}
 
 	/**
 	 * @param regulationChangeDAO
 	 *            the regulationChangeDAO to set
 	 */
 	public void setRegulationChangeDAO(RegulationChangeDAO regulationChangeDAO) {
 		this.regulationChangeDAO = regulationChangeDAO;
 	}
 
 	/**
 	 * @param untenabilityVoteChoiceDAO
 	 *            the untenabilityVoteChoiceDAO to set
 	 */
 	public void setUntenabilityVoteChoiceDAO(
 			UntenabilityVoteChoiceDAO untenabilityVoteChoiceDAO) {
 		this.untenabilityVoteChoiceDAO = untenabilityVoteChoiceDAO;
 	}
 
 	/**
 	 * @param untenabilityVoteDAO
 	 *            the untenabilityVoteDAO to set
 	 */
 	public void setUntenabilityVoteDAO(UntenabilityVoteDAO untenabilityVoteDAO) {
 		this.untenabilityVoteDAO = untenabilityVoteDAO;
 	}
 
 	@Transactional(propagation = Propagation.REQUIRED)
 	@Override
 	public void removeAcceptanceVotes(User user) {
 		AcceptanceVoteFilter filter = new AcceptanceVoteFilter();
 		filter.setTrialMember(user);
 
 		List<AcceptanceVote> votes = acceptanceVoteDAO.findByFilter(filter);
 		for (AcceptanceVote vote : votes) {
 			acceptanceVoteDAO.delete(vote);
 		}
 	}
 
 	@Override
 	public void resetSenateElectionIfUserIsParticipating(User user) {
 		SenateElection election = getCurrentSenateElection();
 		if (election != null) {
 			if (election.isNominationOpen()
 					&& election.getCandidates().contains(user)) {
 				election.getCandidates().remove(user);
 
 			} else if (election.getCandidates().contains(user)) {
 				// Reset election to nomination period
 				election.setStart(new Date());
 				for (CompoundVote vote : election.getVotes()) {
 					for (CompoundVoteChoice choice : vote.getChoices()) {
 						compoundVoteChoiceDAO.delete(choice);
 					}
 					compoundVoteDAO.delete(vote);
 
 					notificationService
 							.notifyUser(
 									vote.getCaster(),
 									"The Senate election was restarted due to a candidate's membership being terminated. You will need to vote again in a week");
 
 				}
 
 				logService
 						.logSystemAction("Democracy",
 								"Senate election restarted due to candidate membership termination");
 			}
 
 			senateElectionDAO.update(election);
 		}
 	}
 
 	@Override
 	public void resetChancellorElectionIfUserIsParticipating(User user) {
 		ChancellorElection election = getCurrentChancellorElection();
 		if (election != null) {
 			if (election.isNominationOpen()
 					&& election.getCandidates().contains(user)) {
 				election.getCandidates().remove(user);
 
 			} else if (election.getCandidates().contains(user)) {
 				// Reset election to nomination period
 				election.setStart(new Date());
 				for (CompoundVote vote : election.getVotes()) {
 					for (CompoundVoteChoice choice : vote.getChoices()) {
 						compoundVoteChoiceDAO.delete(choice);
 					}
 					compoundVoteDAO.delete(vote);
 
 					notificationService
 							.notifyUser(
 									vote.getCaster(),
 									"The Chancellor election was restarted due to a candidate's membership being terminated. You will need to vote again in a week");
 
 				}
 
 				logService
 						.logSystemAction("Democracy",
 								"Chancellor election restarted due to candidate membership termination");
 			}
 
 			chancellorElectionDAO.update(election);
 		}
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#isEligibleChancellorCandidate(com.tysanclan.site.projectewok.entities.User)
 	 */
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public boolean isEligibleChancellorCandidate(User _user) {
 		User user = userDAO.load(_user.getId());
 
 		if (user.isRetired()) {
 			return false;
 		}
 
 		if (user.getEndorsedBy().size() >= getRequiredChancellorEndorsements()) {
 			return true;
 		}
 
 		Calendar cal = DateUtil.getCalendarInstance();
 		cal.add(Calendar.MONTH, -6);
 
 		DonationFilter filter = new DonationFilter();
 		filter.setFrom(cal.getTime());
 		filter.setDonator(user);
 
 		BigDecimal value = BigDecimal.ZERO;
 		List<Donation> donations = donationDAO.findByFilter(filter);
 		for (Donation donation : donations) {
 			value = value.add(donation.getAmount());
 		}
 
 		if (value.compareTo(new BigDecimal(30)) >= 0) {
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#isEligibleSenateCandidate(com.tysanclan.site.projectewok.entities.User)
 	 */
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public boolean isEligibleSenateCandidate(User _user) {
 		User user = userDAO.load(_user.getId());
 
 		if (user.isRetired()) {
 			return false;
 		}
 
 		if (user.getEndorsedForSenateBy().size() >= getRequiredSenateEndorsements()) {
 			return true;
 		}
 
 		Calendar cal = DateUtil.getCalendarInstance();
 		cal.add(Calendar.MONTH, -6);
 
 		DonationFilter filter = new DonationFilter();
 		filter.setFrom(cal.getTime());
 		filter.setDonator(user);
 
 		BigDecimal value = BigDecimal.ZERO;
 		List<Donation> donations = donationDAO.findByFilter(filter);
 		for (Donation donation : donations) {
 			value = value.add(donation.getAmount());
 		}
 
 		if (value.compareTo(new BigDecimal(20)) > 0) {
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#createChancellorElection()
 	 */
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public ChancellorElection createChancellorElection() {
 		ChancellorElection election = new ChancellorElection();
 		election.setStart(DateUtil.getCalendarInstance().getTime());
 
 		chancellorElectionDAO.save(election);
 
 		logService.logSystemAction("Democracy", "Chancellor election started");
 
 		return election;
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#createGroupLeaderElection(com.tysanclan.site.projectewok.entities.Group)
 	 */
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public GroupLeaderElection createGroupLeaderElection(Group group) {
 		GroupLeaderElection election = new GroupLeaderElection();
 		election.setStart(DateUtil.getCalendarInstance().getTime());
 		election.setGroup(group);
 		groupLeaderElectionDAO.save(election);
 
 		logService.logSystemAction("Groups", "Group leader election for group "
 				+ group.getName() + " started");
 
 		return election;
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#createSenateElection()
 	 */
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public SenateElection createSenateElection() {
 
 		UserFilter filter = new UserFilter();
 		filter.addRank(Rank.CHANCELLOR);
 		filter.addRank(Rank.TRUTHSAYER);
 		filter.addRank(Rank.SENATOR);
 		filter.addRank(Rank.REVERED_MEMBER);
 		filter.addRank(Rank.SENIOR_MEMBER);
 		filter.addRank(Rank.FULL_MEMBER);
 		filter.addRank(Rank.JUNIOR_MEMBER);
 		filter.addRank(Rank.TRIAL);
 		filter.setRetired(false);
 
 		long memberCount = userDAO.countByFilter(filter);
 		int seats = calculateSenateSeats(memberCount);
 
 		SenateElection election = new SenateElection();
 		election.setStart(DateUtil.getCalendarInstance().getTime());
 		election.setSeats(seats);
 
 		senateElectionDAO.save(election);
 
 		logService.logSystemAction("Democracy", "Senate election started with "
 				+ seats + " available seats");
 
 		return election;
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#resolveChancellorElection(com.tysanclan.site.projectewok.entities.ChancellorElection)
 	 */
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void resolveChancellorElection(ChancellorElection _election) {
 		ChancellorElection election = chancellorElectionDAO.load(_election
 				.getId());
 
 		Map<User, Integer> scores = determineUserScoreTotals(election);
 
 		Set<User> winners = determineBordaWinners(scores, 1);
 		if (winners.isEmpty()) {
 			// Restart election
 			election.setStart(new Date());
 			chancellorElectionDAO.update(election);
 
 			logService.logSystemAction("Democracy",
 					"Chancellor election restarted due to lack of winners");
 		} else {
 			election.setWinner(winners.iterator().next());
 			chancellorElectionDAO.update(election);
 
 			UserFilter filter = new UserFilter();
 			filter.addRank(Rank.CHANCELLOR);
 
 			List<User> chancellors = userDAO.findByFilter(filter);
 			// Should only be 0 or 1, but hey, who knows!
 			for (User chancellor : chancellors) {
 				if (!election.getWinner().equals(chancellor)) {
 					logService.logUserAction(chancellor, "Election",
 							"Has not been reelected as Chancellor, and has assumed the rank of "
 									+ chancellor.getRank().toString());
 					notificationService.notifyUser(chancellor,
 							"You were not reelected as Chancellor");
 				}
 				chancellor.setRank(MemberUtil
 						.determineRankByJoinDate(chancellor.getJoinDate()));
 				userDAO.update(chancellor);
 
 			}
 
 			election.getWinner().setRank(Rank.CHANCELLOR);
 			userDAO.update(election.getWinner());
 
 			if (chancellors.contains(election.getWinner())) {
 				logService.logUserAction(election.getWinner(), "Election",
 						"Has been reelected as Chancellor");
 				notificationService.notifyUser(election.getWinner(),
 						"You were reelected as Chancellor");
 			} else {
 				logService.logUserAction(election.getWinner(), "Election",
 						"Has been elected as Chancellor");
 				notificationService.notifyUser(election.getWinner(),
 						"You were elected as Chancellor");
 			}
 		}
 
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#resolveGroupLeaderElection(com.tysanclan.site.projectewok.entities.GroupLeaderElection)
 	 */
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void resolveGroupLeaderElection(GroupLeaderElection _election) {
 		GroupLeaderElection election = groupLeaderElectionDAO.load(_election
 				.getId());
 
 		Map<User, Integer> scores = determineUserScoreTotals(election);
 
 		Set<User> winners = determineBordaWinners(scores, 1);
 
 		if (winners.isEmpty()) {
 			// Restart election
 			election.setStart(new Date());
 			groupLeaderElectionDAO.update(election);
 
 			logService.logSystemAction("Democracy",
 					"Group leader election for "
 							+ election.getGroup().getName()
 							+ " restarted due to lack of winners");
 		} else {
 			election.setWinner(winners.iterator().next());
 
 			groupLeaderElectionDAO.update(election);
 
 			election.getGroup().setLeader(election.getWinner());
 
 			logService.logUserAction(election.getWinner(), "Election",
 					"Has become the new leader of "
 							+ election.getGroup().getName());
 			notificationService.notifyUser(election.getWinner(),
 					"You were elected as leader of "
 							+ election.getGroup().getName());
 
 			groupDAO.update(election.getGroup());
 		}
 	}
 
 	@Transactional(propagation = Propagation.REQUIRED)
 	protected Set<User> determineBordaWinners(final Map<User, Integer> scores,
 			int amount) {
 		List<User> winners = new ArrayList<User>(scores.size());
 		Set<User> result = new HashSet<User>();
 
 		winners.addAll(scores.keySet());
 
 		Collections.sort(winners, new Comparator<User>() {
 
 			@Override
 			public int compare(User o1, User o2) {
 
 				int diff = scores.get(o2).compareTo(scores.get(o1));
 
 				if (diff == 0) {
 					return o1.getJoinDate().compareTo(o2.getJoinDate());
 				}
 
 				return diff;
 			}
 
 		});
 
 		for (int i = 0; i < amount; i++) {
 			if (i < winners.size()) {
 				result.add(winners.get(i));
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * Counts the total score gained by each candidate in an election
 	 * 
 	 * @param election
 	 *            The election to count the score for
 	 * @return A data structure mapping each user to his total score
 	 */
 	@Transactional(propagation = Propagation.REQUIRED)
 	protected Map<User, Integer> determineUserScoreTotals(Election election) {
 		// Election election = electionDAO.load(_election
 		// .getId());
 
 		Set<CompoundVote> votes = election.getVotes();
 		Map<User, Integer> scores = new HashMap<User, Integer>();
 
 		for (CompoundVote vote : votes) {
 			for (CompoundVoteChoice choice : vote.getChoices()) {
 				User votesFor = choice.getVotesFor();
 
 				if (!election.getEligibility().isEligible(votesFor)) {
 					continue;
 				}
 
 				int score = choice.getScore();
 				if (!scores.containsKey(votesFor)) {
 					scores.put(votesFor, score);
 				} else {
 					scores.put(votesFor, score + scores.get(votesFor));
 				}
 
 			}
 		}
 		return scores;
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#resolveSenateElection(com.tysanclan.site.projectewok.entities.SenateElection)
 	 */
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void resolveSenateElection(SenateElection _election) {
 		SenateElection election = senateElectionDAO.load(_election.getId());
 
 		if (!election.getWinners().isEmpty())
 			return;
 
 		int seats = election.getSeats();
 		int candidateCount = election.getCandidates().size();
 
 		if (seats >= candidateCount) {
 			seats = Math.max(2, candidateCount - 1);
 
 			logService.logSystemAction("Democracy",
 					"Available Senate seats adjusted to " + seats
 							+ " due to lack of candidates");
 		}
 
 		Map<User, Integer> scores = determineUserScoreTotals(election);
 
 		Set<User> winners = determineBordaWinners(scores, seats);
 
 		if (!winners.isEmpty()) {
 			StringBuilder winnerString = new StringBuilder();
 			winnerString.append("New Senate: ");
 			int i = 0;
 			for (User winner : winners) {
 				if (i++ > 0) {
 					winnerString.append(", ");
 				}
 
 				winnerString.append(winner.getUsername());
 			}
 
 			election.setWinners(winners);
 			senateElectionDAO.update(election);
 
 			UserFilter filter = new UserFilter();
 			filter.addRank(Rank.SENATOR);
 
 			List<User> senators = userDAO.findByFilter(filter);
 			for (User senator : senators) {
 				senator.setRank(MemberUtil.determineRankByJoinDate(senator
 						.getJoinDate()));
 				if (!winners.contains(senator)) {
 					logService.logUserAction(senator, "Election",
 							"Has not been reelected as Senator, and has assumed the rank of "
 									+ senator.getRank().toString());
 					notificationService.notifyUser(senator,
 							"You were not reelected as Senator");
 				} else {
 					logService.logUserAction(senator, "Election",
 							"Has been reelected as Senator");
 					notificationService.notifyUser(senator,
 							"You were reelected as Senator");
 				}
 
 				userDAO.update(senator);
 			}
 
 			for (User senator : election.getWinners()) {
 				senator.setRank(Rank.SENATOR);
 				userDAO.update(senator);
 
 				if (!senators.contains(senator)) {
 					logService.logUserAction(senator, "Election",
 							"Has been elected as Senator");
 					notificationService.notifyUser(senator,
 							"You were elected as Senator");
 				}
 			}
 		} else {
 			election.setStart(new Date());
 			senateElectionDAO.update(election);
 
 			logService.logSystemAction("Democracy",
 					"Senate election restarted due to lack of winners");
 		}
 
 	}
 
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void checkAcceptanceVote(User user) {
 		User _user = userDAO.load(user.getId());
 
 		AcceptanceVoteFilter filter = new AcceptanceVoteFilter();
 		filter.setTrialMember(user);
 
 		long count = acceptanceVoteDAO.countByFilter(filter);
 
 		if (count == 0) {
 			AcceptanceVote acceptanceVote = new AcceptanceVote();
 			acceptanceVote.setTrialMember(_user);
 			acceptanceVote.setStart(new Date());
 			acceptanceVoteDAO.save(acceptanceVote);
 
 			logService.logUserAction(_user, "Membership",
 					"Acceptance vote has started");
 			notificationService.notifyUser(_user,
 					"Your acceptance vote has started");
 		}
 
 	}
 
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void resolveAcceptanceVote(AcceptanceVote _vote) {
 		AcceptanceVote vote = acceptanceVoteDAO.load(_vote.getId());
 
 		User user = vote.getTrialMember();
 
 		int inFavor = 0, total = 0;
 
 		for (AcceptanceVoteVerdict verdict : vote.getVerdicts()) {
 			if (MemberUtil.isMember(verdict.getCaster())) {
 				if (verdict.isInFavor())
 					inFavor++;
 				total++;
 			}
 		}
 
 		Integer factor = total > 0 ? (100 * inFavor) / total : null;
 
 		boolean accepted = factor != null
 				&& factor >= MEMBERSHIP_ACCEPTANCE_VOTE_PERCENTAGE_REQUIRED;
 
 		String body = mailService.getAcceptanceVoteNotificationMail(user,
 				accepted, inFavor, total);
 
 		mailService.sendHTMLMail(user.getEMail(),
 				"Your Tysan Clan trial period is over", body);
 
 		user.setRank(accepted ? Rank.JUNIOR_MEMBER : Rank.FORUM);
 
 		if (accepted) {
 			notificationService.notifyUser(user, "You are now a Junior Member");
 
 			dispatcher
 					.dispatchEvent(new MemberStatusEvent(
 							com.tysanclan.site.projectewok.entities.MembershipStatusChange.ChangeType.MEMBERSHIP_GRANTED,
 							user));
 
 		} else {
 			dispatcher
 					.dispatchEvent(new MemberStatusEvent(
 							com.tysanclan.site.projectewok.entities.MembershipStatusChange.ChangeType.MEMBERSHIP_DENIED,
 							user));
 		}
 
 		user.setMentor(null);
 
 		userDAO.update(user);
 
 		acceptanceVoteDAO.delete(vote);
 
 		logService.logUserAction(user, "Membership", "User has "
 				+ (accepted ? "passed" : "failed")
 				+ " his or her acceptance vote ("
 				+ (factor != null ? factor : 0) + "%)");
 
 	}
 
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void resolveJoinApplication(JoinApplication _application) {
 		JoinApplication application = joinApplicationDAO.load(_application
 				.getId());
 		if (application != null) {
 			int inFavor = 0, total = 0;
 			for (JoinVerdict verdict : application.getVerdicts()) {
 				if (verdict.isInFavor()) {
 					inFavor++;
 				}
 				total++;
 			}
 
 			User mentor = application.getMentor();
 			Date startDate = application.getStartDate();
 			LocalDate start = new LocalDate(startDate);
 			LocalDate now = LocalDate.now();
 
 			boolean have3DaysPassed = start.plusDays(3).isBefore(now);
 
 			boolean accepted = false;
 
 			if (have3DaysPassed) {
 				// If we managed to let 3 days pass without the member getting
 				// accepted,
 				// check for mentor is no longer relevant, all we need to know
 				// is if a Senator
 				// voted against
 				if (total == 0) {
 					accepted = true;
 				} else {
 					if (inFavor == 0) {
 						accepted = false;
 					} else {
 						accepted = true;
 					}
 				}
 			} else {
 				// If this method gets invoked earlier, however, then member
 				// needs a mentor and 1
 				// vote in favor - otherwise do not resolve
 				if (inFavor > 0 && mentor != null) {
 					accepted = true;
 				} else {
 					return;
 				}
 			}
 
 			User applicant = application.getApplicant();
 
 			if (accepted) {
 				applicant.setMentor(application.getMentor());
 				applicant.setRank(Rank.TRIAL);
 				applicant.setJoinDate(new Date());
 				applicant.setLoginCount(0);
 				applicant.setLastAction(new Date());
 				userDAO.update(applicant);
 
 			}
 
 			joinApplicationDAO.delete(application);
 
 			mailService.sendHTMLMail(applicant.getEMail(),
 					"Result of your Tysan Clan Application", mailService
 							.getJoinApplicationMail(application, accepted,
 									inFavor, total));
 
 			logService.logUserAction(applicant, "Membership", "User has "
 					+ (accepted ? "been" : "not been")
 					+ " granted a trial membership");
 
 			if (accepted) {
 				notificationService.notifyUser(applicant,
 						"You are now a Trial Member");
 
 				dispatcher
 						.dispatchEvent(new MemberStatusEvent(
 								com.tysanclan.site.projectewok.entities.MembershipStatusChange.ChangeType.TRIAL_GRANTED,
 								applicant));
 			} else {
 				dispatcher
 						.dispatchEvent(new MemberStatusEvent(
 								com.tysanclan.site.projectewok.entities.MembershipStatusChange.ChangeType.TRIAL_DENIED,
 								applicant));
 			}
 		}
 
 	}
 
 	/**
 	 * @param chancellorElectionDAO
 	 *            the chancellorElectionDAO to set
 	 */
 	public void setChancellorElectionDAO(
 			ChancellorElectionDAO chancellorElectionDAO) {
 		this.chancellorElectionDAO = chancellorElectionDAO;
 	}
 
 	/**
 	 * @param senateElectionDAO
 	 *            the senateElectionDAO to set
 	 */
 	public void setSenateElectionDAO(SenateElectionDAO senateElectionDAO) {
 		this.senateElectionDAO = senateElectionDAO;
 	}
 
 	/**
 	 * @param groupLeaderElectionDAO
 	 *            the groupLeaderElectionDAO to set
 	 */
 	public void setGroupLeaderElectionDAO(
 			GroupLeaderElectionDAO groupLeaderElectionDAO) {
 		this.groupLeaderElectionDAO = groupLeaderElectionDAO;
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#getCurrentChancellorElection()
 	 */
 	@Override
 	@Transactional(propagation = Propagation.SUPPORTS)
 	public ChancellorElection getCurrentChancellorElection() {
 		ChancellorElectionFilter filter = new ChancellorElectionFilter();
 		Calendar twoWeeksAgo = DateUtil.getCalendarInstance();
 		twoWeeksAgo.add(Calendar.WEEK_OF_YEAR, -2);
 
 		filter.setStartBefore(new Date());
 		filter.setStartAfter(twoWeeksAgo.getTime());
 
 		List<ChancellorElection> elections = chancellorElectionDAO
 				.findByFilter(filter);
 
 		if (elections.size() > 0) {
 			return elections.get(0);
 		}
 
 		return null;
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#getCurrentSenateElection()
 	 */
 	@Override
 	@Transactional(propagation = Propagation.SUPPORTS)
 	public SenateElection getCurrentSenateElection() {
 		SenateElectionFilter filter = new SenateElectionFilter();
 		Calendar twoWeeksAgo = DateUtil.getCalendarInstance();
 		twoWeeksAgo.add(Calendar.WEEK_OF_YEAR, -2);
 
 		filter.setStartBefore(new Date());
 		filter.setStartAfter(twoWeeksAgo.getTime());
 
 		List<SenateElection> elections = senateElectionDAO.findByFilter(filter);
 
 		if (elections.size() > 0) {
 			return elections.get(0);
 		}
 
 		return null;
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#isElectionCandidate(com.tysanclan.site.projectewok.entities.User)
 	 */
 	@Override
 	@Transactional(propagation = Propagation.SUPPORTS)
 	public boolean isElectionCandidate(User user) {
 		SenateElection sElection = getCurrentSenateElection();
 
 		boolean res = false;
 
 		if (sElection != null) {
 			res |= sElection.getCandidates().contains(user);
 		}
 
 		ChancellorElection cElection = getCurrentChancellorElection();
 
 		if (cElection != null) {
 			res |= cElection.getCandidates().contains(user);
 		}
 
 		return res;
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#getRequiredChancellorEndorsements()
 	 */
 	@Override
 	@Transactional(propagation = Propagation.SUPPORTS)
 	public long getRequiredChancellorEndorsements() {
 		long memberCount = userService.countMembers();
 		long required = (5 * memberCount) / 100;
 
 		if (required == 0) {
 			required = 1;
 		}
 
 		return required;
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#getRequiredSenateEndorsements()
 	 */
 	@Override
 	@Transactional(propagation = Propagation.SUPPORTS)
 	public long getRequiredSenateEndorsements() {
 		long memberCount = userService.countMembers();
 		long required = (3 * memberCount) / 100;
 
 		if (required == 0) {
 			required = 1;
 		}
 
 		return required;
 	}
 
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void endorseUserForChancellor(User endorser, User target) {
 		User _endorser = userDAO.load(endorser.getId());
 		User _target = target != null ? userDAO.load(target.getId()) : null;
 
 		User _current = _endorser.getEndorses();
 
 		if (_current != null) {
 			// Can not change endorsements of running candidates
 			if (isElectionCandidate(_current)) {
 				return;
 			}
 		}
 
 		if (MemberUtil.canUserGrantEndorsement(_endorser)
 				&& (_target == null || MemberUtil
 						.canUserGrantEndorsement(target))) {
 			_endorser.setEndorses(_target);
 			userDAO.update(_endorser);
 
 			if (_target != null) {
 				logService.logUserAction(_endorser, "Democracy",
 						"User has endorsed " + _target.getUsername()
 								+ " for Chancellor");
 				notificationService.notifyUser(
 						_target,
 						"You are endorsed for Chancellor by "
 								+ _endorser.getUsername());
 			} else {
 				logService
 						.logUserAction(_endorser, "Democracy",
 								"User is no longer endorsing any member for Chancellor");
 			}
 		}
 
 	}
 
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void endorseUserForSenate(User endorser, User target) {
 		User _endorser = userDAO.load(endorser.getId());
 		User _target = target != null ? userDAO.load(target.getId()) : null;
 
 		User _current = _endorser.getEndorsesForSenate();
 
 		if (_current != null) {
 			// Can not change endorsements of running candidates
 			if (isElectionCandidate(_current)) {
 				return;
 			}
 		}
 
 		if (MemberUtil.canUserGrantEndorsement(_endorser)
 				&& (_target == null || MemberUtil
 						.canUserGrantEndorsement(target))) {
 			_endorser.setEndorsesForSenate(_target);
 			userDAO.update(_endorser);
 
 			if (_target != null) {
 				logService.logUserAction(_endorser, "Democracy",
 						"User has endorsed " + _target.getUsername()
 								+ " for Senator");
 				notificationService.notifyUser(
 						_target,
 						"You are endorsed for Senator by "
 								+ _endorser.getUsername());
 			} else {
 				logService.logUserAction(_endorser, "Democracy",
 						"User is no longer endorsing any member for Senator");
 			}
 		}
 
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#addChancellorCandidate(com.tysanclan.site.projectewok.entities.User)
 	 */
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public boolean addChancellorCandidate(User user) {
 		ChancellorElection election = getCurrentChancellorElection();
 
 		if (!user.isRetired() && election != null) {
 			if (user.getEndorsedBy().size() >= getRequiredChancellorEndorsements()
 					|| user.hasDonatedAtLeast(new BigDecimal(30))) {
 				if (!isElectionCandidate(user) && election.isNominationOpen()) {
 					election.getCandidates().add(user);
 					chancellorElectionDAO.update(election);
 
 					logService.logUserAction(user, "Democracy",
 							"Has decided to run for Chancellor!");
 
 					return true;
 				}
 			}
 		}
 
 		return false;
 
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#addSenateCandidate(com.tysanclan.site.projectewok.entities.User)
 	 */
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public boolean addSenateCandidate(User user) {
 		SenateElection election = getCurrentSenateElection();
 
 		if (!user.isRetired() && election != null) {
 			if (user.getEndorsedForSenateBy().size() >= getRequiredSenateEndorsements()
 					|| user.hasDonatedAtLeast(new BigDecimal(20))) {
 				if (!isElectionCandidate(user) && election.isNominationOpen()) {
 					election.getCandidates().add(user);
 					senateElectionDAO.update(election);
 
 					logService.logUserAction(user, "Democracy",
 							"Has decided to run for Senator!");
 
 					return true;
 				}
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#addGroupLeaderCandidate(com.tysanclan.site.projectewok.entities.User,
 	 *      com.tysanclan.site.projectewok.entities.GroupLeaderElection)
 	 */
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
 	public boolean addGroupLeaderCandidate(User user,
 			GroupLeaderElection election) {
 		GroupLeaderElection _election = groupLeaderElectionDAO.load(election
 				.getId());
 		User _user = userDAO.load(user.getId());
 
 		if (_election != null) {
 			if (!_election.getCandidates().contains(_user)
 					&& _election.isNominationOpen()) {
 				_election.getCandidates().add(_user);
 				groupLeaderElectionDAO.update(_election);
 				groupLeaderElectionDAO.flush();
 
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#createVote(com.tysanclan.site.projectewok.entities.User,
 	 *      java.util.List, com.tysanclan.site.projectewok.entities.Election)
 	 */
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public CompoundVote createVote(User caster, List<User> votesFor,
 			Election election) {
 		CompoundVote vote = new CompoundVote();
 		vote.setCaster(caster);
 		vote.setElection(election);
 		compoundVoteDAO.save(vote);
 
 		int score = votesFor.size() - 1;
 
 		for (User user : votesFor) {
 			CompoundVoteChoice choice = new CompoundVoteChoice();
 			choice.setCompoundVote(vote);
 			choice.setVotesFor(user);
 			choice.setScore(score--);
 			compoundVoteChoiceDAO.save(choice);
 
 		}
 		compoundVoteDAO.update(vote);
 
 		return vote;
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#castAcceptanceVote(com.tysanclan.site.projectewok.entities.AcceptanceVote,
 	 *      com.tysanclan.site.projectewok.entities.User, boolean)
 	 */
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void castAcceptanceVote(AcceptanceVote vote, User caster,
 			boolean inFavor) {
 		AcceptanceVote _vote = acceptanceVoteDAO.load(vote.getId());
 		User _caster = userDAO.load(caster.getId());
 
 		AcceptanceVoteVerdict myVerdict = null;
 
 		for (AcceptanceVoteVerdict v : _vote.getVerdicts()) {
 			if (v.getCaster().equals(_caster)) {
 				myVerdict = v;
 				break;
 			}
 		}
 
 		if (myVerdict == null) {
 			myVerdict = new AcceptanceVoteVerdict();
 			myVerdict.setCaster(_caster);
 			myVerdict.setVote(_vote);
 			myVerdict.setInFavor(inFavor);
 
 			acceptanceVoteVerdictDAO.save(myVerdict);
 			acceptanceVoteDAO.evict(_vote);
 
 		} else {
 			myVerdict.setInFavor(inFavor);
 			acceptanceVoteVerdictDAO.update(myVerdict);
 		}
 
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#createImpeachmentVote(com.tysanclan.site.projectewok.entities.User)
 	 */
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void createImpeachmentVote(User initiator) {
 		UserFilter filter = new UserFilter();
 		filter.addRank(Rank.CHANCELLOR);
 		List<User> chancellors = userDAO.findByFilter(filter);
 		if (!chancellors.isEmpty()) {
 			User chancellor = chancellors.get(0);
 
 			Impeachment impeachment = new Impeachment();
 			impeachment.setChancellor(chancellor);
 			impeachment.setInitiator(initiator);
 			impeachment.setStart(new Date());
 
 			impeachmentDAO.save(impeachment);
 
 			logService
 					.logUserAction(initiator, "Democracy",
 							"An impeachment vote against the Chancellor has been started");
 			notificationService.notifyUser(chancellor,
 					"An impeachment vote against you was started by "
 							+ initiator.getUsername());
 
 		}
 
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#castImpeachmentVote(com.tysanclan.site.projectewok.entities.User,
 	 *      boolean)
 	 */
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void castImpeachmentVote(User voter, boolean inFavor) {
 		User _voter = userDAO.load(voter.getId());
 
 		List<Impeachment> impeachments = impeachmentDAO.findAll();
 
 		if (!impeachments.isEmpty()) {
 			Impeachment impeachment = impeachments.get(0);
 
 			ImpeachmentVote vote = null;
 
 			for (ImpeachmentVote next : impeachment.getVotes()) {
 				if (next.getCaster().equals(_voter)) {
 					vote = next;
 					break;
 				}
 			}
 
 			if (vote == null) {
 				vote = new ImpeachmentVote();
 				vote.setImpeachment(impeachment);
 				vote.setInFavor(inFavor);
 				vote.setCaster(_voter);
 
 				impeachmentVoteDAO.save(vote);
 			} else {
 				vote.setInFavor(inFavor);
 
 				impeachmentVoteDAO.update(vote);
 			}
 		}
 
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#resolveImpeachment()
 	 */
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void resolveImpeachment() {
 		List<Impeachment> impeachments = impeachmentDAO.findAll();
 
 		if (!impeachments.isEmpty()) {
 			Calendar calendar = DateUtil.getCalendarInstance();
 			calendar.add(Calendar.DAY_OF_MONTH, -4);
 
 			Impeachment impeachment = impeachments.get(0);
 
 			if (impeachment.getStart().before(calendar.getTime())) {
 
 				int inFavor = 0;
 				int total = impeachment.getVotes().size();
 
 				User chancellor = impeachment.getChancellor();
 
 				for (ImpeachmentVote vote : impeachment.getVotes()) {
 					if (vote.isInFavor()) {
 						inFavor++;
 					}
 				}
 
 				int percentage = (100 * inFavor) / total;
 
 				if (percentage >= 66) {
 					notificationService.notifyUser(chancellor,
 							"You have been impeached");
 
 					logService.logUserAction(chancellor, "Democracy",
 							"Chancellor has been impeached (" + percentage
 									+ ")");
 
 					chancellor.setRank(MemberUtil
 							.determineRankByJoinDate(chancellor.getJoinDate()));
 					userDAO.update(chancellor);
 				}
 
 				impeachmentDAO.delete(impeachment);
 			}
 		}
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#stepDownAsChancellor(com.tysanclan.site.projectewok.entities.User)
 	 */
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void stepDownAsChancellor(User chancellor) {
 		User user = userDAO.load(chancellor.getId());
 
 		user.setRank(MemberUtil.determineRankByJoinDate(user.getJoinDate()));
 
 		logService.logUserAction(user, "Democracy",
 				"User has stepped down as Chancellor");
 
 		userDAO.update(user);
 
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#stepDownAsSenator(com.tysanclan.site.projectewok.entities.User)
 	 */
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void stepDownAsSenator(User senator) {
 		User user = userDAO.load(senator.getId());
 
 		user.setRank(MemberUtil.determineRankByJoinDate(user.getJoinDate()));
 
 		logService.logUserAction(user, "Democracy",
 				"User has stepped down as Senator");
 
 		userDAO.update(user);
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#stepDownAsTruthsayer(com.tysanclan.site.projectewok.entities.User)
 	 */
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void stepDownAsTruthsayer(User truthsayer) {
 		User user = userDAO.load(truthsayer.getId());
 
 		user.setRank(MemberUtil.determineRankByJoinDate(user.getJoinDate()));
 
 		logService.logUserAction(user, "Democracy",
 				"User has stepped down as Truthsayer");
 
 		userDAO.update(user);
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#castUntenabilityVote(com.tysanclan.site.projectewok.entities.User,
 	 *      com.tysanclan.site.projectewok.entities.UntenabilityVote, boolean)
 	 */
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void castUntenabilityVote(User user, UntenabilityVote vote,
 			boolean inFavor) {
 		UntenabilityVote _vote = untenabilityVoteDAO.load(vote.getId());
 		User _user = userDAO.load(user.getId());
 
 		UntenabilityVoteChoice myChoice = null;
 
 		for (UntenabilityVoteChoice choice : _vote.getChoices()) {
 			if (choice.getCaster().equals(_user)) {
 				myChoice = choice;
 				break;
 			}
 		}
 
 		if (myChoice == null) {
 			myChoice = new UntenabilityVoteChoice();
 			myChoice.setCaster(_user);
 			myChoice.setInFavor(inFavor);
 			myChoice.setVote(_vote);
 
 			untenabilityVoteChoiceDAO.save(myChoice);
 		} else {
 			myChoice.setInFavor(inFavor);
 			untenabilityVoteChoiceDAO.update(myChoice);
 		}
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#createUntenabilityVote(com.tysanclan.site.projectewok.entities.User,
 	 *      com.tysanclan.site.projectewok.entities.Regulation)
 	 */
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void createUntenabilityVote(User user, Regulation regulation) {
 		Regulation _regulation = regulationDAO.load(regulation.getId());
 		User _user = userDAO.load(user.getId());
 
 		UntenabilityVoteFilter filter = new UntenabilityVoteFilter();
 		filter.setRegulation(_regulation);
 
 		if (untenabilityVoteDAO.countByFilter(filter) == 0) {
 			UntenabilityVote vote = new UntenabilityVote();
 			vote.setRegulation(_regulation);
 			vote.setStart(new Date());
 
 			untenabilityVoteDAO.save(vote);
 
 			logService.logUserAction(_user, "Democracy", "Regulation "
 					+ _regulation.getName()
 					+ " declared untenable, vote started");
 		}
 
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#resolveUntenabilityVote(com.tysanclan.site.projectewok.entities.UntenabilityVote)
 	 */
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void resolveUntenabilityVote(UntenabilityVote vote) {
 		UntenabilityVote _vote = untenabilityVoteDAO.load(vote.getId());
 
 		int inFavor = 0;
 		int total = _vote.getChoices().size();
 
 		Regulation regulation = _vote.getRegulation();
 
 		for (UntenabilityVoteChoice choice : _vote.getChoices()) {
 			if (choice.isInFavor()) {
 				inFavor++;
 			}
 		}
 
 		if (total > 0) {
 
 			int percentage = (100 * inFavor) / total;
 
 			if (percentage >= 51) {
 				logService.logSystemAction("Democracy", "Regulation "
 						+ regulation.getName()
 						+ " was repealled by untenability vote (" + percentage
 						+ "%)");
 
 				regulationDAO.delete(regulation);
 
 			} else {
 				logService.logSystemAction("Democracy", "Regulation "
 						+ regulation.getName()
 						+ " was maintained by untenability vote (" + percentage
 						+ "%)");
 			}
 		}
 
 		untenabilityVoteDAO.delete(_vote);
 
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#createAddRegulationVote(com.tysanclan.site.projectewok.entities.User,
 	 *      java.lang.String, java.lang.String)
 	 */
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public RegulationChange createAddRegulationVote(User user, String title,
 			String description) {
 		User _user = userDAO.load(user.getId());
 
 		RegulationChange change = new RegulationChange();
 		change.setChangeType(ChangeType.ADD);
 		change.setTitle(HTMLSanitizer.stripTags(title));
 		change.setDescription(HTMLSanitizer.sanitize(description));
 		change.setStart(new Date());
 		change.setVeto(false);
 		change.setRegulation(null);
 		change.setProposer(_user);
 
 		regulationChangeDAO.save(change);
 
 		return change;
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#createModifyRegulationVote(com.tysanclan.site.projectewok.entities.User,
 	 *      com.tysanclan.site.projectewok.entities.Regulation,
 	 *      java.lang.String, java.lang.String)
 	 */
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public RegulationChange createModifyRegulationVote(User user,
 			Regulation regulation, String newTitle, String newDescription) {
 		User _user = userDAO.load(user.getId());
 		Regulation _regulation = regulationDAO.load(regulation.getId());
 
 		RegulationChange change = new RegulationChange();
 		change.setChangeType(ChangeType.MODIFY);
 		change.setTitle(HTMLSanitizer.stripTags(newTitle));
 		change.setDescription(HTMLSanitizer.sanitize(newDescription));
 		change.setStart(new Date());
 		change.setVeto(false);
 		change.setRegulation(_regulation);
 		change.setProposer(_user);
 
 		regulationChangeDAO.save(change);
 
 		return change;
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#createRepealRegulationVote(com.tysanclan.site.projectewok.entities.User,
 	 *      com.tysanclan.site.projectewok.entities.Regulation)
 	 */
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public RegulationChange createRepealRegulationVote(User user,
 			Regulation regulation) {
 		User _user = userDAO.load(user.getId());
 		Regulation _regulation = regulationDAO.load(regulation.getId());
 
 		RegulationChange change = new RegulationChange();
 		change.setChangeType(ChangeType.REPEAL);
 		change.setVeto(false);
 		change.setStart(new Date());
 		change.setRegulation(_regulation);
 		change.setProposer(_user);
 
 		regulationChangeDAO.save(change);
 
 		return change;
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#resolveRegulationVote(com.tysanclan.site.projectewok.entities.RegulationChange)
 	 */
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void resolveRegulationVote(RegulationChange change) {
 		RegulationChange _change = regulationChangeDAO.load(change.getId());
 
 		UserFilter filter = new UserFilter();
 		filter.addRank(Rank.SENATOR);
 
 		long total = _change.isVeto() ? userDAO.countByFilter(filter) : _change
 				.getVotes().size();
 		int required_factor = _change.isVeto() ? 66 : 51;
 
 		int inFavor = 0;
 
 		for (RegulationChangeVote vote : _change.getVotes()) {
 			if (vote.isInFavor()) {
 				inFavor++;
 			}
 		}
 
 		long factor = (100 * inFavor) / total;
 
 		if (factor >= required_factor) {
 			// Execute regulation change
 			switch (_change.getChangeType()) {
 				case ADD:
 					createRegulation(_change, factor);
 					break;
 				case MODIFY:
 					modifyRegulation(_change, factor);
 					break;
 				case REPEAL:
 					repealRegulation(_change, factor);
 					break;
 			}
 		} else {
 			switch (_change.getChangeType()) {
 				case ADD:
 					logService.logSystemAction("Regulations",
 							"The proposed regulation " + _change.getTitle()
 									+ " did not pass (" + factor + "%)");
 					break;
 				case MODIFY:
 					logService.logSystemAction("Regulations",
 							"The proposed modifications to regulation "
 									+ _change.getTitle() + " did not pass ("
 									+ factor + "%)");
 					break;
 				case REPEAL:
 					logService.logSystemAction(
 							"Regulations",
 							"The proposed repeal of regulation "
 									+ _change.getTitle() + " did not pass ("
 									+ factor + "%)");
 					break;
 			}
 		}
 
 		for (RegulationChangeVote rcv : _change.getVotes()) {
 			regulationChangeVoteDAO.delete(rcv);
 		}
 
 		regulationChangeDAO.delete(_change);
 
 	}
 
 	@Transactional(propagation = Propagation.REQUIRED)
 	private void repealRegulation(RegulationChange _change, long factor) {
 		Regulation regulation = _change.getRegulation();
 
 		logService.logSystemAction("Regulations",
 				"The Senate has voted to repeal the Regulation named "
 						+ regulation.getName() + " (" + factor + "%)");
 		regulationDAO.delete(regulation);
 	}
 
 	@Transactional(propagation = Propagation.REQUIRED)
 	private void modifyRegulation(RegulationChange _change, long factor) {
 		Regulation regulation = _change.getRegulation();
 
 		logService.logSystemAction("Regulations",
 				"The Senate has voted to modify the Regulation named "
 						+ regulation.getName() + " (" + factor + "%)");
 
 		regulation.setContents(_change.getDescription());
 		regulation.setName(_change.getTitle());
 		regulationDAO.update(regulation);
 
 	}
 
 	@Transactional(propagation = Propagation.REQUIRED)
 	private void createRegulation(RegulationChange _change, long factor) {
 		Regulation regulation = new Regulation();
 
 		logService.logSystemAction("Regulations",
 				"The Senate has voted to create a new Regulation named "
 						+ _change.getTitle() + " (" + factor + "%)");
 
 		regulation.setContents(_change.getDescription());
 		regulation.setDrafter(_change.getProposer());
 		regulation.setName(_change.getTitle());
 		regulationDAO.save(regulation);
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#voteForRegulation(com.tysanclan.site.projectewok.entities.User,
 	 *      com.tysanclan.site.projectewok.entities.RegulationChange, boolean)
 	 */
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void voteForRegulation(User senator, RegulationChange change,
 			boolean inFavor) {
 		RegulationChange _change = regulationChangeDAO.load(change.getId());
 
 		RegulationChangeVote myVote = null;
 
 		for (RegulationChangeVote vote : _change.getVotes()) {
 			if (vote.getSenator().equals(senator)) {
 				myVote = vote;
 			}
 		}
 
 		if (myVote == null) {
 			myVote = new RegulationChangeVote();
 			myVote.setInFavor(inFavor);
 			myVote.setRegulationChange(_change);
 			myVote.setSenator(senator);
 			regulationChangeVoteDAO.save(myVote);
 			regulationChangeDAO.evict(_change);
 		} else {
 			myVote.setInFavor(inFavor);
 			regulationChangeVoteDAO.update(myVote);
 		}
 
 	}
 
 	/**
 	 * @see com.tysanclan.site.projectewok.beans.DemocracyService#vetoRegulationChange(com.tysanclan.site.projectewok.entities.User,
 	 *      com.tysanclan.site.projectewok.entities.RegulationChange)
 	 */
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void vetoRegulationChange(User chancellor, RegulationChange change) {
 		RegulationChange _change = regulationChangeDAO.load(change.getId());
 
 		UserFilter filter = new UserFilter();
 		filter.addRank(Rank.SENATOR);
 
 		String status;
 
 		if (userDAO.countByFilter(filter) < 2
 				|| _change.getProposer().equals(chancellor)) {
 			regulationChangeDAO.delete(_change);
 
 			status = " was vetoed and automatically withdrawn";
 		} else {
 			_change.setVeto(true);
 
 			regulationChangeDAO.update(_change);
 
 			status = " was vetoed. A 66% Senate majority is now required";
 		}
 
 		switch (_change.getChangeType()) {
 			case ADD:
 				logService.logUserAction(chancellor, "Regulations",
 						"The proposed regulation " + _change.getTitle()
 								+ status);
 				break;
 			case MODIFY:
 				logService.logUserAction(
 						chancellor,
 						"Regulations",
 						"The proposed modifications to regulation "
 								+ _change.getTitle() + status);
 				break;
 			case REPEAL:
 				logService.logUserAction(
 						chancellor,
 						"Regulations",
 						"The proposed repeal of regulation "
 								+ _change.getTitle() + status);
 				break;
 		}
 
 	}
 
 	public static int calculateSenateSeats(long memberCount) {
 		return (int) Math.max(2, 1 + (memberCount / 20));
 	}
 
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void checkAcceptanceVotes() {
 
 		for (User user : userDAO.getTrialMembersReadyForVote()) {
 			checkAcceptanceVote(user);
 		}
 	}
 
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void resolveAcceptanceVotes() {
 		Calendar threeDaysAgo = DateUtil.getCalendarInstance();
 
 		threeDaysAgo.add(Calendar.DAY_OF_YEAR, -3);
 
 		AcceptanceVoteFilter filter = new AcceptanceVoteFilter();
 		filter.setStartBefore(threeDaysAgo.getTime());
 
 		List<AcceptanceVote> eligibles = acceptanceVoteDAO.findByFilter(filter);
 		for (AcceptanceVote vote : eligibles) {
 			resolveAcceptanceVote(vote);
 		}
 
 	}
 
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void checkChancellorElections() {
 		Calendar calendar = DateUtil.getCalendarInstance();
 		calendar.add(Calendar.MONTH, -6);
 
		ChancellorElectionFilter filter = new ChancellorElectionFilter();
		filter.setStartBefore(calendar.getTime());
		filter.addOrderBy("start", false);

		ChancellorElectionFilter filter2 = new ChancellorElectionFilter();
		filter2.setStartAfter(calendar.getTime());
		filter2.addOrderBy("start", false);
 
 		ChancellorElection current = getCurrentChancellorElection();
 
		UserFilter ufilter = new UserFilter();
		ufilter.addRank(Rank.CHANCELLOR);
 
		if (chancellorElectionDAO.countAll() == 0
				|| (chancellorElectionDAO.countByFilter(filter) > 0
						&& chancellorElectionDAO.countByFilter(filter2) == 0 && current == null)
				|| userDAO.countByFilter(ufilter) == 0) {
 			createChancellorElection();
 		}
 
 	}
 
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void resolveChancellorElections() {
 		ChancellorElectionFilter filter = new ChancellorElectionFilter();
 		Calendar cal = DateUtil.getCalendarInstance();
 		cal.add(Calendar.WEEK_OF_YEAR, -2);
 		filter.setStartBefore(cal.getTime());
 		filter.setNoWinner(true);
 		List<ChancellorElection> elections = chancellorElectionDAO
 				.findByFilter(filter);
 		for (ChancellorElection election : elections) {
 			resolveChancellorElection(election);
 		}
 
 	}
 
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void checkSenatorElections() {
 		Calendar calendar = DateUtil.getCalendarInstance();
 		calendar.add(Calendar.MONTH, -6);
 
 		SenateElectionFilter filter = new SenateElectionFilter();
 		filter.setStartBefore(calendar.getTime());
 		filter.addOrderBy("start", false);
 
 		SenateElectionFilter filter2 = new SenateElectionFilter();
 		filter2.setStartAfter(calendar.getTime());
 		filter2.addOrderBy("start", false);
 
 		SenateElection current = getCurrentSenateElection();
 
 		if (senateElectionDAO.countAll() == 0
 				|| (senateElectionDAO.countByFilter(filter) > 0
 						&& senateElectionDAO.countByFilter(filter2) == 0 && current == null)) {
 			createSenateElection();
 		} else {
 			SenateElectionFilter filter3 = new SenateElectionFilter();
 			filter3.addOrderBy("start", false);
 			List<SenateElection> elections = senateElectionDAO
 					.findByFilter(filter3);
 
 			UserFilter filter4 = new UserFilter();
 			filter4.addRank(Rank.SENATOR);
 
 			for (SenateElection election : elections) {
 				int seats = election.getWinners().size();
 
 				if (seats > 0) {
 					long senators = userDAO.countByFilter(filter4);
 
 					int fraction = (int) ((senators * 100) / seats);
 
 					if (fraction < 40) {
 						createSenateElection();
 					}
 				}
 				break;
 
 			}
 		}
 
 	}
 
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void resolveSenatorElections() {
 		SenateElectionFilter filter = new SenateElectionFilter();
 		Calendar twoWeeksAgo = DateUtil.getCalendarInstance();
 		Calendar threeWeeksAgo = DateUtil.getCalendarInstance();
 		twoWeeksAgo.add(Calendar.WEEK_OF_YEAR, -2);
 		threeWeeksAgo.add(Calendar.WEEK_OF_YEAR, -3);
 		filter.setStartBefore(twoWeeksAgo.getTime());
 		filter.setStartAfter(threeWeeksAgo.getTime());
 		List<SenateElection> elections = senateElectionDAO.findByFilter(filter);
 		for (SenateElection election : elections) {
 			resolveSenateElection(election);
 		}
 	}
 
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void resolveGroupLeaderElections() {
 		GroupLeaderElectionFilter filter = new GroupLeaderElectionFilter();
 		Calendar twoWeeksAgo = DateUtil.getCalendarInstance();
 		Calendar threeWeeksAgo = DateUtil.getCalendarInstance();
 		twoWeeksAgo.add(Calendar.WEEK_OF_YEAR, -2);
 		threeWeeksAgo.add(Calendar.WEEK_OF_YEAR, -3);
 		filter.setStartBefore(twoWeeksAgo.getTime());
 		filter.setStartAfter(threeWeeksAgo.getTime());
 
 		List<GroupLeaderElection> elections = groupLeaderElectionDAO
 				.findByFilter(filter);
 		for (GroupLeaderElection election : elections) {
 			resolveGroupLeaderElection(election);
 		}
 
 	}
 
 	@Transactional(propagation = Propagation.REQUIRED)
 	@Override
 	public void resolveJoinApplications() {
 		Calendar cal = DateUtil.getCalendarInstance();
 		cal.add(Calendar.DAY_OF_YEAR, -3);
 
 		JoinApplicationFilter filter = new JoinApplicationFilter();
 		filter.setDateBefore(cal.getTime());
 
 		List<JoinApplication> applications = joinApplicationDAO
 				.findByFilter(filter);
 		for (JoinApplication application : applications) {
 			resolveJoinApplication(application);
 		}
 	}
 
 	@Transactional(propagation = Propagation.REQUIRED)
 	@Override
 	public void resolveRegulationVotes() {
 		RegulationChangeFilter filter = new RegulationChangeFilter();
 		Calendar cal = DateUtil.getMidnightCalendarInstance();
 		cal.add(Calendar.WEEK_OF_YEAR, -1);
 		filter.setStartBefore(cal.getTime());
 
 		for (RegulationChange change : regulationChangeDAO.findByFilter(filter)) {
 			resolveRegulationVote(change);
 		}
 	}
 
 	@Override
 	@Transactional(propagation = Propagation.REQUIRED)
 	public void resolveUntenabilityVotes() {
 		Calendar cal = DateUtil.getCalendarInstance();
 		cal.add(Calendar.WEEK_OF_YEAR, 1);
 
 		UntenabilityVoteFilter filter = new UntenabilityVoteFilter();
 		filter.setStartBefore(cal.getTime());
 
 		List<UntenabilityVote> votes = untenabilityVoteDAO.findByFilter(filter);
 
 		for (UntenabilityVote vote : votes) {
 			resolveUntenabilityVote(vote);
 		}
 
 	}
 }
