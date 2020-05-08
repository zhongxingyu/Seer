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
 package com.tysanclan.site.projectewok.pages.member;
 
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.TimeZone;
 import java.util.TreeMap;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.RestartResponseAtInterceptPageException;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.protocol.http.request.WebClientInfo;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.odlabs.wiquery.core.javascript.JsQuery;
 import org.odlabs.wiquery.core.javascript.JsStatement;
 import org.odlabs.wiquery.ui.draggable.DraggableBehavior;
 import org.odlabs.wiquery.ui.droppable.DroppableAjaxBehavior;
 
 import com.jeroensteenbeeke.hyperion.data.ModelMaker;
 import com.tysanclan.site.projectewok.TysanSession;
 import com.tysanclan.site.projectewok.components.MemberListItem;
 import com.tysanclan.site.projectewok.entities.Election;
 import com.tysanclan.site.projectewok.entities.User;
 import com.tysanclan.site.projectewok.entities.dao.UserDAO;
 import com.tysanclan.site.projectewok.util.DateUtil;
 
 /**
  * @author Jeroen Steenbeeke
  */
 public abstract class AbstractElectionPage<T extends Election> extends
 		AbstractMemberPage {
 	private static final long serialVersionUID = 1L;
 
 	private final Map<Integer, Long> votes;
 	private final int candidates;
 
 	public abstract AbstractManualElectionPage getInternetExplorerAlternativePage(
 			T election);
 
 	/**
 	 * 
 	 */
 	public AbstractElectionPage(String title, T election) {
 		super(title);
 
 		final boolean isNominationOpen = election.isNominationOpen();
 
 		WebClientInfo clientInfo = TysanSession.get().getClientInfo();
 
 		// If user is using IE7, offer him an alternative
 		if (!isNominationOpen
 				&& (clientInfo.getUserAgent().contains("MSIE") || clientInfo
 						.getUserAgent().contains("Trident"))) {
 			throw new RestartResponseAtInterceptPageException(
 					getInternetExplorerAlternativePage(election));
 		}
 
 		votes = new TreeMap<Integer, Long>();
 		candidates = election.getCandidates().size();
 
 		List<User> users = new LinkedList<User>();
 		users.addAll(election.getCandidates());
 
 		ListView<User> candidateView = new ListView<User>("candidates",
 				ModelMaker.wrap(users)) {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void populateItem(ListItem<User> item) {
 				MemberListItem mitem = new MemberListItem("candidate",
 						item.getModelObject());
 
 				if (!isNominationOpen) {
 					mitem.add(new DraggableBehavior() {
 						private static final long serialVersionUID = 1L;
 
 						/**
 						 * @see org.odlabs.wiquery.ui.draggable.DraggableBehavior#statement()
 						 */
 						@Override
 						public JsStatement statement() {
 							return new JsQuery(getComponent()).$().chain(
 									"draggable", "{ revert: true }");
 
 						}
 					});
 					mitem.setOutputMarkupId(true);
 				}
 
 				item.add(mitem);
 
 			}
 
 		};
 
 		add(new Label("label", isNominationOpen ? "Candidates"
 				: "Cast your vote!"));
 
 		TimeZone tz = getUser().getTimezone() != null ? TimeZone
 				.getTimeZone(getUser().getTimezone()) : DateUtil.NEW_YORK;
 
 		Calendar cal = Calendar.getInstance(tz, Locale.US);
 		cal.setTime(election.getStart());
 		cal.add(Calendar.WEEK_OF_YEAR, 1);
 
 		Label explanation = new Label(
 				"explanation",
 				isNominationOpen ? "Voting will commence "
 						+ DateUtil.getTimezoneFormattedString(cal.getTime(),
 								tz.getID())
 						: "Drag and drop the candidates of your choice to cast your vote. Place your favorites at the top and your least favorites at the bottom. The order you give them will determine their score. The higher their position the higher the score");
 
 		Form<Void> castVoteForm = new Form<Void>("voteform") {
 			private static final long serialVersionUID = 1L;
 
 			@SpringBean
 			private UserDAO userDAO;
 
 			/**
 			 * @see org.apache.wicket.markup.html.form.Form#onSubmit()
 			 */
 			@Override
 			protected void onSubmit() {
 				List<User> userList = new LinkedList<User>();
 
 				for (Integer position : getVotes().keySet()) {
 					userList.add(userDAO.load(getVotes().get(position)));
 				}
 
 				onVoteSubmit(userList);
 			}
 
 		};
 
 		castVoteForm.add(candidateView);
 
 		List<Integer> positions = new LinkedList<Integer>();
 		for (int i = 0; i < users.size(); i++) {
 			positions.add(users.size() - (i + 1));
 		}
 
 		castVoteForm.add(new ListView<Integer>("slots", positions) {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void populateItem(ListItem<Integer> item) {
 				item.add(new Label("position", new Model<Integer>(item
 						.getIndex() + 1)));
 
 				final int index = item.getIndex();
 
 				item.add(new Label("score", new Model<Integer>(item
 						.getModelObject())));
 				WebMarkupContainer container = new WebMarkupContainer("target");
 				container.setOutputMarkupId(true)
 						.setOutputMarkupPlaceholderTag(true);
 
 				if (!isNominationOpen) {
 					DroppableAjaxBehavior behavior = new DroppableAjaxBehavior() {
 						private static final long serialVersionUID = 1L;
 
 						protected void drop(AjaxRequestTarget target,
 								Component source, Component dropped) {
 
 							if (dropped instanceof MemberListItem) {
 								MemberListItem mli = (MemberListItem) dropped;
 
 								MemberListItem item2 = new MemberListItem(
 										"target", mli.getUser());
 								item2.setOutputMarkupId(true);
 								getComponent().replaceWith(item2);
 								mli.setVisible(false);
 
 								getVotes().put(index, mli.getUser().getId());
 
 								if (votes.size() == AbstractElectionPage.this.candidates) {
									Component submit = getPage().get(
											"accordion:voteform:submit");
									submit.setVisible(true);
 									if (target != null) {
										target.add(submit);
 									}
 								}
 
 								if (target != null) {
 
 									target.add(mli);
 									target.add(item2);
 								}
 							}
 
 						}
 					};
 
 					behavior.setHoverClass("election-hover");
 
 					container.add(behavior);
 				}
 
 				item.add(container
 
 				);
 			}
 
 		});
 
 		add(explanation);
 		add(castVoteForm);
		castVoteForm.add(new WebMarkupContainer("submit")
				.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true)
				.setVisible(false));
 	}
 
 	/**
 	 * @return the votes
 	 */
 	private Map<Integer, Long> getVotes() {
 		return votes;
 	}
 
 	public abstract void onVoteSubmit(List<User> userList);
 }
