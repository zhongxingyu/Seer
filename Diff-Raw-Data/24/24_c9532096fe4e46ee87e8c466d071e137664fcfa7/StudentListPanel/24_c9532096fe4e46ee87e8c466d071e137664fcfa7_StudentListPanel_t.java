 package com.sickle.medu.ms.client.iportal.panel;
 
 import com.sickle.medu.ms.client.iportal.dialog.StudentDialog;
 import com.sickle.medu.ms.client.iportal.list.StudentList;
 import com.sickle.medu.ms.client.ui.widget.MButton;
 import com.smartgwt.client.util.SC;
 import com.smartgwt.client.widgets.layout.HLayout;
 import com.smartgwt.client.widgets.layout.VLayout;
 
 
 public class StudentListPanel extends HLayout
 {
 
 	private VLayout buttonlayout = new VLayout();
 	
 	private StudentList studentlist;
 	
 	private int classid;
 	
 	public StudentListPanel(int _classid)
 	{
 		this.classid = _classid;
 		setHeight( 300 );
 		
 		studentlist = new StudentList();
 		
 		
 		MButton add = new MButton("增加学生"){
 			@Override
 			public void handleClick( )
 			{
 				StudentDialog dialog = new StudentDialog( ){
 					@Override
 					public void submitcallback( )
 					{
 						studentlist.fetchStudentByClassid( classid );
 					}
 				};
 				dialog.getStudentform( ).setValue( "classid", classid );
 				dialog.show( );
 			}
 		};
 		
 		MButton del = new MButton("删除学生"){
 			@Override
 			public void handleClick( )
 			{
 				if ( studentlist.getSelectedRecords( ).length == 0 )
 				{
 					SC.say( "请先选中一条要删除的记录" );
 					return;
 				}
 				studentlist.removeSelectedData( );
 			}
 		};
 		
 		MButton modify = new MButton("修改学生"){
 			@Override
 			public void handleClick( )
 			{
 				if ( studentlist.getSelectedRecords( ).length == 0 )
 				{
 					SC.say( "请先选中一条要修改的记录" );
 					return;
 				}
 				StudentDialog dialog = new StudentDialog( ){
 					@Override
 					public void submitcallback( )
 					{
 						studentlist.fetchStudentByClassid( classid );
 					}
 				};
 				dialog.getStudentform( ).editSelectedData( studentlist );
 				dialog.getStudentform( ).updateArea();
 				dialog.show( );
 			}
 		};
 		
 		buttonlayout.setMembersMargin( 5 );
 		
 		buttonlayout.addMember( add );
 		buttonlayout.addMember( modify );
 		buttonlayout.addMember( del );
 		
 		addMember( studentlist );
 		addMember( buttonlayout );
 	}
 
 	
 	
 	/**
 	 * @return the studentlist
 	 */
 	public StudentList getStudentlist( )
 	{
 		return studentlist;
 	}
 
 
 
 	
 	/**
 	 * @return the classid
 	 */
 	public int getClassid( )
 	{
 		return classid;
 	}
 
 
 
 	
 	/**
 	 * @param classid the classid to set
 	 */
 	public void setClassid( int classid )
 	{
 		this.classid = classid;
 	}
 	
 	
 }
