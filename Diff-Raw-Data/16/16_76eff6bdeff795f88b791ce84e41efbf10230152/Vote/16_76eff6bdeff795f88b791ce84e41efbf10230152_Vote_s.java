 /**
  * Copyright (C) 2012-2013  Olexandr Tyshkovets
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.github.aint.jblog.web.controller;
 
 import java.io.IOException;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.aint.jblog.model.dao.hibernate.ArticleHibernateDao;
 import com.github.aint.jblog.model.dao.hibernate.CommentHibernateDao;
 import com.github.aint.jblog.model.dao.hibernate.UserHibernateDao;
 import com.github.aint.jblog.model.dao.hibernate.VoiceForArticleHibernateDao;
 import com.github.aint.jblog.model.dao.hibernate.VoiceForCommentHibernateDao;
 import com.github.aint.jblog.model.entity.Article;
 import com.github.aint.jblog.model.entity.Comment;
 import com.github.aint.jblog.model.entity.User;
 import com.github.aint.jblog.model.entity.VoiceValue;
 import com.github.aint.jblog.service.data.ArticleService;
 import com.github.aint.jblog.service.data.CommentService;
 import com.github.aint.jblog.service.data.UserService;
 import com.github.aint.jblog.service.data.impl.ArticleServiceImpl;
 import com.github.aint.jblog.service.data.impl.CommentServiceImpl;
 import com.github.aint.jblog.service.data.impl.UserServiceImpl;
 import com.github.aint.jblog.service.exception.data.EntityNotFoundException;
 import com.github.aint.jblog.service.exception.data.UserNotFoundException;
 import com.github.aint.jblog.service.exception.other.XmlUserRankConfigurationException;
 import com.github.aint.jblog.service.other.UserRankConfiguration;
 import com.github.aint.jblog.service.other.impl.XmlUserRankConfiguration;
 import com.github.aint.jblog.service.util.HibernateUtil;
 import com.github.aint.jblog.web.constant.ConstantHolder;
 import com.github.aint.jblog.web.constant.SessionConstant;
 
 /**
  * This servlet votes for an article or a comment.
  * 
  * @author Olexandr Tyshkovets
  */
 @WebServlet("/vote")
 public class Vote extends HttpServlet {
     private static final long serialVersionUID = -880762514442559473L;
     private static final Logger logger = LoggerFactory.getLogger(Vote.class);
     private static final String SHOWSTORY_SERVLET_URL_PATTERN = ConstantHolder.MAPPING
             .getProperty("mapping.servlet.displayArticle");
     private static final String USER_RANK_XML_PATH = "/com/github/aint/jblog/service/other/userRank-config.xml";
     private static final String USER_RANK_DTD_PATH = "/com/github/aint/jblog/service/other/userRank-config.dtd";
    private CommentService commentService;
     private ArticleService articleService;
     private UserService userService;
     private UserRankConfiguration userRankConfig;
     private Long articleId;
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void init(ServletConfig config) throws ServletException {
         articleService = new ArticleServiceImpl(new ArticleHibernateDao(HibernateUtil.getSessionFactory()));
         commentService = new CommentServiceImpl(new CommentHibernateDao(HibernateUtil.getSessionFactory()));
         userService = new UserServiceImpl(new UserHibernateDao(HibernateUtil.getSessionFactory()));
         try {
             userRankConfig = new XmlUserRankConfiguration(getClass().getResourceAsStream(USER_RANK_XML_PATH),
                     getClass().getResourceAsStream(USER_RANK_DTD_PATH));
         } catch (XmlUserRankConfigurationException e) {
             logger.error("Failed to configurate XmlUserRank", e);
             throw new ServletException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         User user = null;
         try {
             user = userService.getByUserName((String) request.getSession(true).getAttribute(
                     SessionConstant.USER_NAME_SESSION_ATTRIBUTE));
         } catch (UserNotFoundException e) {
             logger.error("The user not found", e);
             response.sendError(HttpServletResponse.SC_FORBIDDEN);
             return;
         }
 
         VoiceValue voiceForArticle = VoiceValue.lookUp(request.getParameter("forArticle"));
         if (voiceForArticle != null) {
             Article article = null;
             try {
                 this.articleId = Long.parseLong(request.getParameter("articleId"));
                 article = articleService.get(this.articleId);
                 articleService.voteForArticle(article, user, voiceForArticle,
                         new VoiceForArticleHibernateDao(HibernateUtil.getSessionFactory()));
             } catch (NumberFormatException e) {
                 logger.warn("The article's id parameter is uncorrect {} {}" + e.getClass() + " " + e.getMessage());
                 response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                 return;
             } catch (EntityNotFoundException e) {
                 logger.info("The article not found", e);
                 response.sendError(HttpServletResponse.SC_NOT_FOUND);
                 return;
             }
 
             userService.updateRating(article.getAuthor(), voiceForArticle);
             userService.updateRank(article.getAuthor(), userRankConfig);
         }
 
         VoiceValue voiceForComment = VoiceValue.lookUp(request.getParameter("forComment"));
         if (voiceForComment != null) {
             Comment comment = null;
             try {
                 comment = commentService.get(Long.parseLong(request.getParameter("commentId")));
                 commentService.voteForComment(comment, user, voiceForComment,
                         new VoiceForCommentHibernateDao(HibernateUtil.getSessionFactory()));
                 this.articleId = comment.getArticle().getId();
             } catch (NumberFormatException e) {
                 logger.error("The comment's id parameter is uncorrect {} {}" + e.getClass() + " " + e.getMessage());
                 response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                 return;
             } catch (EntityNotFoundException e) {
                 logger.error("The comment not found", e);
                 response.sendError(HttpServletResponse.SC_NOT_FOUND);
                 return;
             }
 
             userService.updateRating(comment.getAuthor(), voiceForComment);
             userService.updateRank(comment.getAuthor(), userRankConfig);
         }
 
         response.sendRedirect(SHOWSTORY_SERVLET_URL_PATTERN + "/" + articleId);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         doGet(request, response);
     }
 
 }
