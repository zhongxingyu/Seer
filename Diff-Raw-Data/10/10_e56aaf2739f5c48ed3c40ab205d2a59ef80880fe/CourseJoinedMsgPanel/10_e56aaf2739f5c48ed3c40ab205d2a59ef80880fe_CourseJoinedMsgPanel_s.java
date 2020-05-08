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
 
import java.text.DateFormat;

 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 
 import com.socialsite.BasePanel;
 import com.socialsite.course.CourseLink;
 import com.socialsite.image.ImagePanel;
 import com.socialsite.image.ImageType;
 import com.socialsite.persistence.Course;
 import com.socialsite.persistence.CourseJoinedMsg;
 import com.socialsite.persistence.User;
 import com.socialsite.user.UserLink;
 
 /**
  * @author Ananth
  */
 public class CourseJoinedMsgPanel extends BasePanel
 {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public CourseJoinedMsgPanel(String id, IModel<CourseJoinedMsg> model,
 			final MarkupContainer dependent)
 	{
 		super(id);
 		final CourseJoinedMsg courseJoinedMsg = model.getObject();
 		final User sender = courseJoinedMsg.getSender();
 		final Course course = courseJoinedMsg.getCourse();
 
 		// user image
 		UserLink<User> userImageLink;
 		Model<User> senderModel = new Model<User>(sender);
 		add(userImageLink = new UserLink<User>("imagelink", senderModel));
 		userImageLink.add(new ImagePanel("userthumb", sender.getId(), ImageType.USER, sender
 				.getLastModified(), true));
 		Link<User> name;
 		add(name = new UserLink<User>("home", senderModel));
 		name.add(new Label("username", sender.getUserName()));
 
 		CourseLink courseLink;
 		add(courseLink = new CourseLink("course", new Model<Course>(course)));
 		courseLink.add(new Label("courseName", course.getName()));
 
		final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
				DateFormat.SHORT);
		add(new Label("date", dateFormat.format(courseJoinedMsg.getTime())));
 
 		// delete link
 		add(new DeleteMsgLink<CourseJoinedMsg>("delete", model, dependent, this, sender.getId()));
 		setOutputMarkupId(true);
 	}
 
 }
