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
 import java.util.Set;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.validation.ConstraintViolation;
 import javax.validation.Validator;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.aint.jblog.model.dao.hibernate.ArticleHibernateDao;
 import com.github.aint.jblog.model.dao.hibernate.HubHibernateDao;
 import com.github.aint.jblog.model.dao.hibernate.UserHibernateDao;
 import com.github.aint.jblog.model.entity.Language;
 import com.github.aint.jblog.service.data.ArticleService;
 import com.github.aint.jblog.service.data.HubService;
 import com.github.aint.jblog.service.data.UserService;
 import com.github.aint.jblog.service.data.impl.ArticleServiceImpl;
 import com.github.aint.jblog.service.data.impl.HubServiceImpl;
 import com.github.aint.jblog.service.data.impl.UserServiceImpl;
 import com.github.aint.jblog.service.exception.data.HubNotFoundException;
 import com.github.aint.jblog.service.exception.data.UserNotFoundException;
 import com.github.aint.jblog.service.util.HibernateUtil;
 import com.github.aint.jblog.service.validation.Validation;
 import com.github.aint.jblog.web.constant.ConstantHolder;
 import com.github.aint.jblog.web.constant.SessionConstant;
 import com.github.aint.jblog.web.dto.ArticleDto;
 
 /**
  * This servlet adds an article.
  * 
  * @author Olexandr Tyshkovets
  */
 @WebServlet("/add-article")
 public class AddArticle extends HttpServlet {
     private static final long serialVersionUID = 318019731167585591L;
     private static Logger logger = LoggerFactory.getLogger(AddArticle.class);
     private static final String ADD_ARTICLE_JSP_PATH = ConstantHolder.PATH.getProperty("path.jsp.addArticle");
     private static final String ARTICLES_SERVLET_URL_PATTERN = ConstantHolder.MAPPING
             .getProperty("mapping.servlet.articles");
     private static final String ADD_ARTICLE_BUTTON = "addArticleButton";
     private static final String ARTICLE_HUB_FIELD = "articleHubField";
     private static final String ARTICLE_TITLE_FIELD = "articleTitleField";
     private static final String ARTICLE_PREVIEW_FIELD = "articlePreviewField";
     private static final String ARTICLE_BODY_FIELD = "articleBodyField";
     private static final String ARTICLE_KEYWORDS_FIELD = "articleKeywords";
     private ArticleService articleService;
     private HubService hubService;
     private UserService userService;
 
     @Override
     public void init(ServletConfig config) throws ServletException {
         articleService = new ArticleServiceImpl(new ArticleHibernateDao(HibernateUtil.getSessionFactory()));
         hubService = new HubServiceImpl(new HubHibernateDao(HibernateUtil.getSessionFactory()));
         userService = new UserServiceImpl(new UserHibernateDao(HibernateUtil.getSessionFactory()));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
 
        request.setAttribute("HUBS", hubService.getNamesOfAllPublicHubs());
 
         if (request.getParameter(ADD_ARTICLE_BUTTON) == null) {
             request.getRequestDispatcher(ADD_ARTICLE_JSP_PATH).forward(request, response);
             return;
         }
 
         String articleTitle = request.getParameter(ARTICLE_TITLE_FIELD);
         String articlePreview = request.getParameter(ARTICLE_PREVIEW_FIELD);
         String articleBody = request.getParameter(ARTICLE_BODY_FIELD);
         String articleKeywords = request.getParameter(ARTICLE_KEYWORDS_FIELD);
         String artcleHub = request.getParameter(ARTICLE_HUB_FIELD);
 
         ArticleDto articleDto = new ArticleDto(articleTitle, articlePreview, articleBody, articleKeywords, artcleHub);
         Language language = (Language)
                 request.getSession().getAttribute(SessionConstant.USER_LANGUAGE_SESSION_ATTRIBUTE);
         Validator validator = Validation.getValidator(language.getLocale());
         Set<ConstraintViolation<ArticleDto>> validationErrors = validator.validate(articleDto);
         if (!validationErrors.isEmpty()) {
             logger.debug("The article's validation error messages: {}", validationErrors);
             request.setAttribute("validationErrors", validationErrors);
             request.setAttribute(ARTICLE_TITLE_FIELD, articleTitle);
             request.setAttribute(ARTICLE_PREVIEW_FIELD, articlePreview);
             request.setAttribute(ARTICLE_BODY_FIELD, articleBody);
             request.setAttribute(ARTICLE_KEYWORDS_FIELD, articleKeywords);
             request.getRequestDispatcher(ADD_ARTICLE_JSP_PATH).forward(request, response);
             return;
         }
 
         try {
            articleService.add(
                    articleDto.createArticle(),
                    userService.getByUserName(
                            (String) request.getSession().getAttribute(SessionConstant.USER_NAME_SESSION_ATTRIBUTE)),
                    hubService.getByHubName(artcleHub)
                    );
        } catch (UserNotFoundException e) {
            logger.error("The user not found", e);
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
         } catch (HubNotFoundException e) {
             logger.error("The hub not found", e);
             response.sendError(HttpServletResponse.SC_NOT_FOUND);
             return;
         }
 
         response.sendRedirect(ARTICLES_SERVLET_URL_PATTERN);
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
