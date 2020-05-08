 /**
  *     Copyright SocialSite (C) 2009
  *
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.socialsite.message;
 
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import com.socialsite.BasePanel;
 import com.socialsite.authentication.SocialSiteRoles;
 import com.socialsite.dao.MessageDao;
 import com.socialsite.persistence.Message;
 
 
 public class DeleteMsgLink<T extends Message> extends AjaxLink<T>
 {
 
 	/** spring dao to handle message object */
 	@SpringBean(name = "messageDao")
 	private MessageDao<Message> messageDao;
 
 	MarkupContainer dependent;
 	BasePanel panel;
 	long senderId;
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public DeleteMsgLink(final String id, final IModel<T> model, final MarkupContainer dependent,
 			final BasePanel panel, final long senderId)
 	{
 		super(id, model);
 		this.dependent = dependent;
 		this.panel = panel;
 		this.senderId = senderId;
 	}
 
 	@Override
 	public boolean isVisible()
 	{
 		// allow the owner and the sender to delete the msg
 		if (panel.hasRole(SocialSiteRoles.OWNER))
 		{
 			return true;
 		}
 		else
 		{
 			return senderId == panel.getSessionUserId();
 		}
 	}
 
 	@Override
 	public void onClick(final AjaxRequestTarget target)
 	{
 
		messageDao.delete(getModelObject());
 		target.addComponent(dependent);
 		panel.firePostAjaxUpdateEvent(target);
 	}
 }
