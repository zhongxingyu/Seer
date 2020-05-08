 /*
  * Copyright (c) 2003, Rafael Steil
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided 
  * that the following conditions are met:
  * 
  * 1) Redistributions of source code must retain the above 
  * copyright notice, this list of conditions and the 
  * following  disclaimer.
  * 2)  Redistributions in binary form must reproduce the 
  * above copyright notice, this list of conditions and 
  * the following disclaimer in the documentation and/or 
  * other materials provided with the distribution.
  * 3) Neither the name of "Rafael Steil" nor 
  * the names of its contributors may be used to endorse 
  * or promote products derived from this software without 
  * specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT 
  * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
  * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
  * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
  * IN CONTRACT, STRICT LIABILITY, OR TORT 
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
  * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
  * 
  * Created on Jan 11, 2005 11:44:06 PM
  * The JForum Project
  * http://www.jforum.net
  */
 package net.jforum.view.forum;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import net.jforum.Command;
 import net.jforum.JForum;
 import net.jforum.SessionFacade;
 import net.jforum.entities.Karma;
 import net.jforum.entities.Post;
 import net.jforum.model.DataAccessDriver;
 import net.jforum.model.KarmaModel;
 import net.jforum.model.PostModel;
 import net.jforum.repository.SecurityRepository;
 import net.jforum.security.SecurityConstants;
 import net.jforum.util.I18n;
 import net.jforum.util.preferences.ConfigKeys;
 import net.jforum.util.preferences.SystemGlobals;
 import net.jforum.view.forum.common.ViewCommon;
 
 /**
  * @author Rafael Steil
 * @version $Id: KarmaAction.java,v 1.5 2005/02/28 14:10:03 rafaelsteil Exp $
  */
 public class KarmaAction extends Command
 {
 	public void insert() throws Exception
 	{
 		if (!SecurityRepository.canAccess(SecurityConstants.PERM_KARMA_ENABLED)) {
 			this.error("Karma.featureDisabled", null);
 			return;
 		}
 
 		int postId = this.request.getIntParameter("post_id");
 		int fromUserId = SessionFacade.getUserSession().getUserId();
 
 		PostModel pm = DataAccessDriver.getInstance().newPostModel();
 		Post p = pm.selectById(postId);
 
 		if (fromUserId == SystemGlobals.getIntValue(ConfigKeys.ANONYMOUS_USER_ID)) {
 			this.error("Karma.anonymousIsDenied", p);
 			return;
 		}
 
 		if (p.getUserId() == fromUserId) {
 			this.error("Karma.cannotSelfVote", p);
 			return;
 		}
 
 		KarmaModel km = DataAccessDriver.getInstance().newKarmaModel();
 		if (!km.userCanAddKarma(fromUserId, postId)) {
 			this.error("Karma.alreadyVoted", p);
 			return;
 		}
 
 		Karma karma = new Karma();
 		karma.setFromUserId(fromUserId);
 		karma.setPostUserId(p.getUserId());
 		karma.setPostId(postId);
 		karma.setTopicId(p.getTopicId());
 		karma.setPoints(this.request.getIntParameter("points"));
 
 		km.addKarma(karma);
 
 		JForum.setRedirect(this.urlToTopic(p));
 	}
 
 	private void error(String message, Post p)
 	{
 		this.context.put("moduleAction", "message.htm");
 
 		if (p != null) {
 			this.context.put("message", I18n.getMessage(message, new String[] { this.urlToTopic(p) }));
 		}
 		else {
 			this.context.put("message", I18n.getMessage(message));
 		}
 	}
 
 	private String urlToTopic(Post p)
 	{
 		return JForum.getRequest().getContextPath() + "/posts/list/" 
 			+ ViewCommon.getStartPage()
 			+ "/" + p.getTopicId()
 			+ SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION)
 			+ "#" + p.getId();
 	}
 
 	/**
 	 * @see net.jforum.Command#list()
 	 */
 	public void list() throws Exception
 	{
 		this.context.put("moduleAction", "message.htm");
 		this.context.put("message", I18n.getMessage("invalidAction"));
 	}
 
 	/**
 	 * TODO: Make dynamic data format TODO: refactoring here to remove the duplicated code with the
 	 * method above. Performs a search over the users votes between two dates.
 	 * 
 	 * @throws Exception
 	 */
 	public void searchByPeriod() throws Exception
 	{
 		SimpleDateFormat formater = new SimpleDateFormat("dd/MM/yyyy");
 		Date firstPeriod, lastPeriod;
 		if ("".equals(this.request.getParameter("first_date"))) {
 			firstPeriod = formater.parse("01/01/1970");// extreme date
 		}
 		else {
 			firstPeriod = formater.parse(this.request.getParameter("first_date"));
 		}
 		if ("".equals(this.request.getParameter("last_date"))) {
 			lastPeriod = new Date();// now
 		}
 		else {
 			lastPeriod = formater.parse(this.request.getParameter("last_date"));
 		}
 
 		String orderField;
 		if ("".equals(this.request.getParameter("order_by"))) {
 			orderField = "total";
 		}
 		else {
 			orderField = this.request.getParameter("order_by");
 		}
 
 		int start = this.preparePagination(DataAccessDriver.getInstance().newUserModel().getTotalUsers());
 		int usersPerPage = SystemGlobals.getIntValue(ConfigKeys.USERS_PER_PAGE);
 		// Load all users with your karma
 		List users = DataAccessDriver.getInstance().newKarmaModel().getMostRatedUserByPeriod(usersPerPage, firstPeriod,
 				lastPeriod, orderField);
 		this.context.put("users", users);
 		this.context.put("moduleAction", "user_list_karma.htm");
 	}
 
 	/**
	 * TODO: Make dynamic date format
 	 * 
 	 * Performs a search over the users votes in a specific month.
 	 * 
 	 * @throws Exception
 	 */
 	public void searchByMonth() throws Exception
 	{
		SimpleDateFormat formater = new SimpleDateFormat("dd/MM/yyyy");
 		int month = Integer.parseInt(request.getParameter("month"));
 		int year = Integer.parseInt(request.getParameter("year"));
 
 		Calendar c = Calendar.getInstance();
 		Date firstPeriod, lastPeriod;
		firstPeriod = formater.parse("01/" + month + "/" + year);
 		// set the Calendar with the first day of the month
 		c.setTime(firstPeriod);
 		// Now get the last day of this month.
		lastPeriod = formater.parse(c.getActualMaximum(Calendar.DAY_OF_MONTH) + "/" + month + "/" + year);
 
 		String orderField;
 		if ("".equals(request.getParameter("order_by")) || request.getParameter("order_by") == null) {
 			orderField = "total";
 		}
 		else {
 			orderField = this.request.getParameter("order_by");
 		}
 
 		int start = this.preparePagination(DataAccessDriver.getInstance().newUserModel().getTotalUsers());
 		int usersPerPage = SystemGlobals.getIntValue(ConfigKeys.USERS_PER_PAGE);
 		// Load all users with your karma
 		List users = DataAccessDriver.getInstance().newKarmaModel().getMostRatedUserByPeriod(usersPerPage, firstPeriod,
 				lastPeriod, orderField);
 		this.context.put("users", users);
 		this.context.put("moduleAction", "user_list_karma.htm");
 
 	}
 
 	private int preparePagination(int totalUsers)
 	{
 		int start = ViewCommon.getStartPage();
 		int usersPerPage = SystemGlobals.getIntValue(ConfigKeys.USERS_PER_PAGE);
 
 		this.context.put("totalPages", new Double(Math.ceil((double) totalUsers / usersPerPage)));
 		this.context.put("recordsPerPage", new Integer(usersPerPage));
 		this.context.put("totalRecords", new Integer(totalUsers));
 		this.context.put("thisPage", new Double(Math.ceil((double) (start + 1) / usersPerPage)));
 		this.context.put("start", new Integer(start));
 
 		return start;
 	}
 }
