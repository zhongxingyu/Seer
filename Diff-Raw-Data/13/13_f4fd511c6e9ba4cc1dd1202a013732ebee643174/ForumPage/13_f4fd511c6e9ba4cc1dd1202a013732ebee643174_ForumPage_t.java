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
 
 import java.util.Set;
 
 import org.apache.wicket.RestartResponseAtInterceptPageException;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValueConversionException;
 
 import com.jeroensteenbeeke.hyperion.data.ModelMaker;
 import com.tysanclan.site.projectewok.TysanPage;
 import com.tysanclan.site.projectewok.components.ForumPanel;
 import com.tysanclan.site.projectewok.entities.Forum;
 import com.tysanclan.site.projectewok.entities.dao.ForumDAO;
 import com.tysanclan.site.projectewok.entities.dao.ForumThreadDAO;
 
 /**
  * @author Jeroen Steenbeeke
  */
 public class ForumPage extends TysanPage {
 	private static final long serialVersionUID = 1L;
 
 	@SpringBean
 	private ForumDAO forumDAO;
 
 	@SpringBean
 	private ForumThreadDAO forumThreadDAO;
 
 	private IModel<Forum> forumModel;
 
 	public ForumPage(PageParameters params) {
 		super("");
 
 		Set<String> namedKeys = params.getNamedKeys();
 
 		if (!namedKeys.contains("forumid") || !namedKeys.contains("pageid")) {
 			throw new RestartResponseAtInterceptPageException(
 					AccessDeniedPage.class);
 		}
 
		Long id, pageNumber;

		try {
			id = params.get("forumid").toOptionalLong();
			pageNumber = params.get("pageid").toOptionalLong();
		} catch (StringValueConversionException svce) {
			throw new RestartResponseAtInterceptPageException(
					AccessDeniedPage.class);
		}
 
 		if (id == null || pageNumber == null || pageNumber == 0) {
 			throw new RestartResponseAtInterceptPageException(
 					AccessDeniedPage.class);
 		}
 
 		Forum forum = forumDAO.get(id);
 
 		if (forum == null) {
 			throw new RestartResponseAtInterceptPageException(
 					AccessDeniedPage.class);
 		}
 
 		if (!forum.canView(getUser())) {
 			throw new RestartResponseAtInterceptPageException(
 					AccessDeniedPage.class);
 		}
 
 		initComponents(forum, pageNumber);
 	}
 
 	public ForumPage(final Forum forum) {
 		super("");
 		initComponents(forum, 1L);
 	}
 
 	public ForumPage(final Forum forum, final long pageNumber) {
 		super("");
 		initComponents(forum, pageNumber);
 	}
 
 	/**
 	 * @return the forumDAO
 	 */
 	public ForumDAO getForumDAO() {
 		return forumDAO;
 	}
 
 	/**
 	 * @return the forumThreadDAO
 	 */
 	public ForumThreadDAO getForumThreadDAO() {
 		return forumThreadDAO;
 	}
 
 	public void initComponents(final Forum forum, final long pageNumber) {
 		forumModel = ModelMaker.wrap(forum);
 
 		boolean publicView = getUser() == null;
 
 		setPageTitle(forum.getName());
 
 		add(new Link<Void>("overview") {
 			private static final long serialVersionUID = 1L;
 
 			/**
 			 * @see org.apache.wicket.markup.html.link.Link#onClick()
 			 */
 			@Override
 			public void onClick() {
 				setResponsePage(new ForumOverviewPage());
 
 			}
 		});
 
 		add(new ForumPanel("threads", forum, pageNumber, publicView));
 
 	}
 
 	/**
 	 * @param forumDAO
 	 *            the forumDAO to set
 	 */
 	public void setForumDAO(ForumDAO forumDAO) {
 		this.forumDAO = forumDAO;
 	}
 
 	public void setForumThreadDAO(ForumThreadDAO forumThreadDAO) {
 		this.forumThreadDAO = forumThreadDAO;
 	}
 
 	/**
 	 * @see org.apache.wicket.Page#onDetach()
 	 */
 	@Override
 	protected void onDetach() {
 		super.onDetach();
 		if (forumModel != null) {
 			forumModel.detach();
 
 		}
 	}
 
 	public Forum getForum() {
 		return forumModel.getObject();
 	}
 
 }
