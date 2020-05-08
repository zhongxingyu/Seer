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
 
 package com.socialsite.persistence;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 public class Course implements AbstractDomain
 {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private long id;
 
 	private String name;
 	private byte[] image;
 	private byte[] thumb;
 
 	private University university;
 
 	private Staff staff;
 
 	private Set<Student> students = new HashSet<Student>();
 
 	private Date lastModified;
 
 	private Set<Question> questions = new HashSet<Question>();
 
 	public Course()
 	{
 	}
 
 	public Course(final String name, final Staff staff, final University university)
 	{
 		setName(name);
 		setStaff(staff);
 		addUniversity(university);
 		setLastModified(new Date());
 	}
 
 	public void addQuestions(final Question question)
 	{
 		getQuestions().add(question);
 		question.setCourse(this);
 	}
 
 	public void addUniversity(final University university)
 	{
 		setUniversity(university);
 		university.getCourses().add(this);
 	}
 
 	/**
 	 * changes the profile image
 	 * 
 	 * @param image
 	 *            image data in byte[]
 	 */
 	public void changeImage(final byte[] image)
 	{
 		setLastModified(new Date());
 	}
 
 
 	/**
 	 * changes the thumb
 	 * 
 	 * @param thumb
 	 *            thumb data in byte[]
 	 */
 	public void changeThumb(final byte[] thumb)
 	{
 		this.thumb = thumb;
 		setLastModified(new Date());
 	}
 
 	public long getId()
 	{
 		return id;
 	}
 
 	public byte[] getImage()
 	{
 		return image;
 	}
 
 	public Date getLastModified()
 	{
 		return lastModified;
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 
 	public Set<Question> getQuestions()
 	{
 		return questions;
 	}
 
 	public Staff getStaff()
 	{
 		return staff;
 	}
 
 	public Set<Student> getStudents()
 	{
 		return students;
 	}
 
 	public byte[] getThumb()
 	{
 		return thumb;
 	}
 
 	public University getUniversity()
 	{
 		return university;
 	}
 
 	public void setId(final long id)
 	{
 		this.id = id;
 	}
 
 	public void setImage(final byte[] image)
 	{
 		this.image = image;
 	}
 
 	public void setLastModified(final Date lastModified)
 	{
 		this.lastModified = lastModified;
 	}
 
 	public void setName(final String name)
 	{
 		this.name = name;
 	}
 
 	public void setQuestions(final Set<Question> questions)
 	{
 		this.questions = questions;
 	}
 
 	public void setStaff(final Staff staff)
 	{
 		this.staff = staff;
 	}
 
 	public void setStudents(final Set<Student> students)
 	{
 		this.students = students;
 	}
 
 	public void setThumb(final byte[] thumb)
 	{
 		this.thumb = thumb;
 	}
 
 	public void setUniversity(final University university)
 	{
 		this.university = university;
 	}
 }
