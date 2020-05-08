 package org.mpg.grader.internals.data;
 
 import org.mpg.grader.data.GradeListDAO;
 import org.mpg.grader.entities.GradeList;
 
 public class GradeListDAOImpl extends BasicDAOImpl<GradeList> implements GradeListDAO {
 
	protected GradeListDAOImpl() {
 		super(GradeList.class);
 	}
 
 }
