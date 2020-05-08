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
 import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.data.DataView;
 import org.apache.wicket.markup.repeater.data.IDataProvider;
 import org.apache.wicket.model.Model;
 
 import com.socialsite.BasePanel;
 import com.socialsite.dataprovider.StaffCourseDataProvider;
 import com.socialsite.dataprovider.UniversityCourseDataProvider;
 import com.socialsite.dataprovider.UserCourseDataProvider;
 import com.socialsite.image.ImagePanel;
 import com.socialsite.image.ImageType;
 import com.socialsite.persistence.Admin;
 import com.socialsite.persistence.Course;
 import com.socialsite.persistence.Staff;
 import com.socialsite.persistence.Student;
 import com.socialsite.persistence.User;
 
 /**
  * @author Ananth
  */
 public class AllCoursesPanel extends BasePanel
 {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public AllCoursesPanel(String id)
 	{
 		super(id);
 
 		User user = getUser();
 		IDataProvider<Course> dataProvider = null;
 		if (user instanceof Student)
 		{
 			dataProvider = new UserCourseDataProvider(user.getId());
 		}
 		else if (user instanceof Staff)
 		{
 
 			dataProvider = new StaffCourseDataProvider(user.getId());
 		}
 		else if (user instanceof Admin)
 		{
 			dataProvider = new UniversityCourseDataProvider(((Admin)user).getUniversity().getId());
 		}
 
		final DataView<Course> courseList = new DataView<Course>("courses", dataProvider, 12)
 		{
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void populateItem(final Item<Course> item)
 			{
 				Course course = item.getModelObject();
 				CourseLink courseLink;
 				item.add(courseLink = new CourseLink("courselink", new Model<Course>(course)));
 				courseLink.add(new Label("coursename", course.getName()));
 
 				CourseLink courseImageLink;
 				item.add(courseImageLink = new CourseLink("courseimagelink", new Model<Course>(
 						course)));
 				courseImageLink.add(new ImagePanel("image", course.getId(), ImageType.COURSE,
 						course.getLastModified(), false, false));
 			}
 
 		};
 
		add(new PagingNavigator("paging", courseList));
		add(courseList);
 	}
 }
