 package com.sickle.medu.ms.client.iportal.list;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.sickle.medu.ms.client.datasource.ClassDataSource;
 import com.sickle.medu.ms.client.datasource.StudentDataSource;
 import com.sickle.medu.ms.client.iportal.panel.StudentListPanel;
 import com.sickle.medu.ms.client.rpc.ClassesService;
 import com.sickle.medu.ms.client.rpc.ClassesServiceAsync;
 import com.sickle.pojo.edu.Cls;
 import com.sickle.pojo.edu.Student;
 import com.smartgwt.client.data.DataSource;
 import com.smartgwt.client.data.Record;
 import com.smartgwt.client.util.SC;
 import com.smartgwt.client.widgets.Canvas;
 import com.smartgwt.client.widgets.grid.ListGrid;
 import com.smartgwt.client.widgets.grid.ListGridRecord;
 
 
 public class ClassList extends ListGrid
 {
 
 	public ClassList()
 	{
 		setWidth( "90%" );
 		setHeight( "90%" );
 		setAutoFetchData( false );
 		setDrawAheadRatio(4); 
 		setCanExpandRecords(true);
 		setDataSource( getDataSource( ) );
 	}
 	
 	public DataSource getDataSource( )
 	{
 		return ClassDataSource.getInstance( ).getDataSource( Cls.class );
 	}
 
 	@Override
 	public DataSource getRelatedDataSource( ListGridRecord record )
 	{
 		return StudentDataSource.getInstance( ).getDataSource( Student.class );
 	}
 
 	@Override
 	protected Canvas getExpansionComponent( ListGridRecord record )
 	{
 		//TODO 根据选择的学校信息展现student list
 		int classid = record.getAttributeAsInt( "id" );
 		StudentListPanel panel = new StudentListPanel(classid);
 		panel.getStudentlist( ).fetchStudentByClassid( classid );
 		return panel;
 	}
 	
 	public void fetchClassByMemberid(final int memberid )
 	{
 		ClassesServiceAsync service = ClassesService.Util.getInstance( );
 		service.listClasses( memberid, new AsyncCallback<List<Cls>>( ) {
 			
 			@Override
 			public void onSuccess( List<Cls> classes )
 			{
 				List<Record> records = new ArrayList<Record>();
 				for( Cls cls : classes )
 				{
 					ListGridRecord record = new ListGridRecord();
 					ClassDataSource.getInstance( ).copyValues( cls, record );
 					records.add( record );
 				}
 				Record[] rs = records.toArray( new Record[records.size( )]);
 				setData( rs );
 			}
 			
 			@Override
 			public void onFailure( Throwable caught )
 			{
 				SC.say( "loading error" + caught.getMessage( ));
 			}
 		} );
 	}
 }
