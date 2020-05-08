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
 package com.tysanclan.site.projectewok.components;
 
 import org.apache.wicket.Session;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.panel.Panel;
 
 import com.tysanclan.site.projectewok.TysanSession;
 import com.tysanclan.site.projectewok.entities.User;
 
 /**
  * @author Jeroen Steenbeeke
  * @param <L>
  *            The type of the link to use if logged in
  * @param <U>
  *            The type of the link to use if not logged in
  */
 public abstract class LoginAwareLink<L extends Link<?>, U extends Link<?>>
 		extends Panel {
 	private static final long serialVersionUID = 1L;
 
 	protected static final String LINK_LABEL = "link";
 
 	private L loggedInLink;
 
 	private U notLoggedInLink;
 
 	private final String text;
 
 	public LoginAwareLink(String id, String text) {
 		this(id, text, null, null);
 	}
 
 	public LoginAwareLink(String id, String text, L loggedInLink,
 			U notLoggedInLink) {
 		super(id);
 		this.loggedInLink = loggedInLink;
 		this.notLoggedInLink = notLoggedInLink;
 		this.text = text;
 
 		if (isLoggedInCondition()) {
 			if (loggedInLink != null) {
 				add(loggedInLink);
 				loggedInLink.add(new Label("name", text));
 			}
 		} else {
 			if (notLoggedInLink != null) {
 				add(notLoggedInLink);
 				notLoggedInLink.add(new Label("name", text));
 			}
 		}
 
 	}
 
 	protected boolean isLoggedInCondition() {
 		TysanSession sess = (TysanSession) Session.get();
 		User u = (sess != null) ? sess.getUser() : null;
 		return (u != null);
 	}
 
 	/**
 	 * @return the loggedInLink
 	 */
 	public L getLoggedInLink() {
 		return loggedInLink;
 	}
 
 	/**
 	 * @return the notLoggedInLink
 	 */
 	public U getNotLoggedInLink() {
 		return notLoggedInLink;
 	}
 
 	/**
 	 * @param loggedInLink
 	 *            the loggedInLink to set
 	 */
 	public void setLoggedInLink(L loggedInLink) {
 		this.loggedInLink = loggedInLink;
 		if (isLoggedInCondition()) {
 			add(loggedInLink);
			loggedInLink.add(new Label("name", text));
 		}
 	}
 
 	/**
 	 * @param notLoggedInLink
 	 *            the notLoggedInLink to set
 	 */
 	public void setNotLoggedInLink(U notLoggedInLink) {
 		this.notLoggedInLink = notLoggedInLink;
 		if (!isLoggedInCondition()) {
 			add(notLoggedInLink);
			notLoggedInLink.add(new Label("name", text));
 		}
 	}
 
 }
