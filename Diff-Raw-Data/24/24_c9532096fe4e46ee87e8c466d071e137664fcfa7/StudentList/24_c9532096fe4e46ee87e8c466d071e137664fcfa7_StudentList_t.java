 package com.sickle.medu.ms.client.iportal.list;
 
 import java.util.List;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.sickle.medu.ms.client.datasource.MemberDataSource;
 import com.sickle.medu.ms.client.form.withwidget.AbstractListGrid;
 import com.sickle.medu.ms.client.rpc.ClassesService;
 import com.sickle.medu.ms.client.rpc.ClassesServiceAsync;
 import com.sickle.pojo.edu.Member;
 import com.smartgwt.client.data.Criteria;
 import com.smartgwt.client.data.DataSource;
 import com.smartgwt.client.types.SelectionStyle;
 import com.smartgwt.client.util.SC;
 import com.smartgwt.client.widgets.grid.HeaderSpan;
 
 /**
  * 学生list
  * 
  * @author chenhao
  *
  */
 public class StudentList extends AbstractListGrid
 {
 
 	public StudentList()
 	{
 		setWidth( "90%" );
 		setHeight( "90%" );
 		setHeaderHeight(40); 
 		setAutoFetchData( false );
 		setSelectionType( SelectionStyle.SINGLE );
 		DataSource ds = getDataSource();
 		maskAndSetFields( ds );
 		
 		ds.getField( "name" ).setTitle( "学生名字" );
 		ds.getField( "resume" ).setTitle( "情况简述" );
 		setHeaderSpans(new HeaderSpan("学生列表", ds.getFieldNames( )));
 	}
 	
 	public DataSource getDataSource( )
 	{
 		return MemberDataSource.getInstance( ).getDataSource( Member.class );
 	}
 	
 	public void fetchStudentByClassid(final int classid )
 	{
 		ClassesServiceAsync service = ClassesService.Util.getInstance( );
 		service.findStudents( classid, new AsyncCallback<List<Member>>( ) {
 			
 			@Override
 			public void onSuccess( List<Member> result )
 			{
 				Integer[] stuids = new Integer[result.size( )];
 				int i = 0;
 				for(Member member : result )
 				{
 					stuids[i++] = member.getId( );
 				}
 				Criteria criteria = new Criteria();
 				if(stuids.length == 0)
 				{
 					stuids = new Integer[]{ -1 };
 				}
 				criteria.addCriteria( "id", stuids );
 				fetchData( criteria );
				
				/*List<Record> records = new ArrayList<Record>();
				for( Member m : result )
				{
					ListGridRecord record = new ListGridRecord();
					MemberDataSource.getInstance( ).copyValues( m, record );
					records.add( record );
				}
				setData( records.toArray( new Record[records.size( )]) );*/
 			}
 			
 			@Override
 			public void onFailure( Throwable caught )
 			{
 				SC.say( "loading error" + caught.getMessage( ));
 			}
 		} );
 	}
 }
