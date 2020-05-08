 package org.seamoo.webapp.controllers;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.seamoo.competition.LeagueOrganizer;
 import org.seamoo.daos.LeagueDao;
import org.seamoo.daos.MemberQualificationDao;
 import org.seamoo.daos.SubjectDao;
 import org.seamoo.daos.matching.MatchDao;
 import org.seamoo.entities.League;
 import org.seamoo.entities.Member;
import org.seamoo.entities.MemberQualification;
 import org.seamoo.entities.Subject;
 import org.seamoo.entities.matching.Match;
 import org.seamoo.webapp.filters.MemberInjectionFilter;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 @RequestMapping("/matches")
 public class MatchController {
 	@Autowired
 	MatchDao matchDao;
 	@Autowired
 	LeagueDao leagueDao;
 	@Autowired
 	SubjectDao subjectDao;
 	@Autowired
 	LeagueOrganizer leagueOrganizer;
	@Autowired
	MemberQualificationDao memberQualificationDao;
 
 	@RequestMapping("/participate")
	public ModelAndView participate(HttpServletRequest request, @RequestParam("leagueId") long leagueId) {
		Member member = MemberInjectionFilter.getInjectedMember(request);
 		League league = leagueDao.findByKey(leagueId);
 		Subject subject = subjectDao.findByKey(league.getSubjectAutoId());
		MemberQualification mq = memberQualificationDao.findByMemberAndSubject(member.getAutoId(), subject.getAutoId());
		ModelAndView mav;
		if (leagueOrganizer.canMemberJoinLeague(mq, league)) {
			mav = new ModelAndView("matches.participate");
			mav.addObject("title", "Tham gia giải đấu");
			mav.addObject("league", league);
		} else {
			mav = new ModelAndView("matches.participate.forbidden");
			mav.addObject("title", "Tham gia vượt cấp");
		}
 		mav.addObject("subject", subject);
 		return mav;
 	}
 
 	@RequestMapping("/{matchId}/{matchDescription}")
 	public ModelAndView view(@PathVariable("matchId") long matchId, @PathVariable("matchDescription") String matchDescription,
 			@RequestParam(required = false, defaultValue = "no", value = "rejoin") String rejoin) {
 		ModelAndView mav = new ModelAndView("matches.detail");
 		mav.addObject("title", "Xem trận đấu");
 		Match match = matchDao.findByKey(matchId);
 		League league = leagueDao.findByKey(match.getLeagueAutoId());
 		Subject subject = subjectDao.findByKey(league.getSubjectAutoId());
 		mav.addObject("match", match);
 		mav.addObject("league", league);
 		mav.addObject("subject", subject);
 		mav.addObject("rejoin", rejoin.equals("yes"));
 		return mav;
 	}
 
 	@RequestMapping("/rejoin")
 	public String rejoin(HttpServletRequest request, @RequestParam("leagueId") long leagueId) {
 		Member member = MemberInjectionFilter.getInjectedMember(request);
 		leagueOrganizer.getMatchOrganizer(leagueId).escapeCurrentMatch(member.getAutoId());
 		return "redirect:/matches/participate?leagueId=" + leagueId;
 	}
 
 }
