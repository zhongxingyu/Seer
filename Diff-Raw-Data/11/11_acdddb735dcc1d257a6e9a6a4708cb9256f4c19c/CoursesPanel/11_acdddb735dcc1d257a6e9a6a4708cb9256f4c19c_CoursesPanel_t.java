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
 
 package com.socialsite.course;
 
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.data.DataView;
 
 import com.socialsite.dataprovider.CoursesDataProvider;
import com.socialsite.friend.FriendsPage;
 import com.socialsite.image.ImagePanel;
 import com.socialsite.image.ImageType;
 import com.socialsite.persistence.Course;
 import com.socialsite.persistence.University;
import com.socialsite.util.ShowAllLink;
 
 /**
  * 
  * shows the list of courses in the university
  * 
  * @author Ananth
  */
 public class CoursesPanel extends Panel
 {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * constructor
 	 * 
 	 * @param id
 	 *            id
 	 * @param university
 	 *            university
 	 */
 	public CoursesPanel(final String id, final University university)
 	{
 		super(id);
 		final DataView<Course> courseView = new DataView<Course>("courses",
				new CoursesDataProvider(university), 9)
 		{
 
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void populateItem(final Item<Course> item)
 			{
 				final Course course = item.getModelObject();
 				CourseLink courseImageLink;
 				item.add(courseImageLink = new CourseLink("imagelink", item.getModel()));
 				courseImageLink.add(new ImagePanel("coursethumb", course.getId(), ImageType.COURSE,
 						course.getLastModified(), true));
 
 				// course link
 				CourseLink courseLink;
 				item.add(courseLink = new CourseLink("name", item.getModel()));
 				courseLink.add(new Label("coursename", course.getName()));
 			}
 		};
 		add(courseView);
		// TODO create a page for courses
		add(new ShowAllLink<Course>("showall", courseView.getDataProvider(), FriendsPage.class));
 	}
 }
