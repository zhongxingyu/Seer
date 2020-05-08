 package gov.cida.sedmap.data;
 
 import static org.junit.Assert.*;
 
 import java.sql.Date;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.List;
 
 import gov.cida.sedmap.data.Column;
 import gov.cida.sedmap.io.IoUtils;
 import gov.cida.sedmap.mock.MockDataSource;
 import gov.cida.sedmap.mock.MockResultSet;
 import gov.cida.sedmap.mock.MockRowMetaData;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class CharFormatterTest {
 
 	MockDataSource     ds;
 	MockResultSet      rs;
 	MockRowMetaData    md;
 	String sql;
 
 	List<Column> cols;
 
 	@Before
 	@SuppressWarnings("deprecation")
 	public void setup() throws Exception {
 		// init values
 		ds  = new MockDataSource();
 		rs  = new MockResultSet();
 		md  = new MockRowMetaData();
 
 		cols = new ArrayList<Column>();
 		cols.add( new Column("Site_Id",     Types.VARCHAR, 10, false) );
 		cols.add( new Column("Latitude",    Types.NUMERIC,  3, false) );
 		cols.add( new Column("Longitude",   Types.NUMERIC,  3, false) );
 		cols.add( new Column("create_date", Types.DATE,     8, false) ); // TODO 8 is a place-holder
 		md.addMetadata(cols);
 
 		rs.addMockRow("1234567891",40.1,-90.1,new Date(01,1-1,1));
 		rs.addMockRow("12345678|2",40.2,-90.2,new Date(02,2-1,2));
 		rs.addMockRow("1234567893",40.3,-90.3,new Date(03,3-1,3));
 
 		sql = "select * from dual";
 		// populate result sets
 		ds.put(sql, rs);
 		ds.put(sql, md);
 	}
 
 
 	@Test
	public void transform_comma2pip() throws Exception {
 		CharSepFormatter pip = new CharSepFormatter("","|","");
 		CharSepFormatter com = new CharSepFormatter("",",","");
 		String expect = "Site_Id|Latitude|Longitude|create_date";
		String actual = com.transform("Site_Id,Latitude,Longitude,create_date", pip);
 		assertEquals(expect,actual);
 	}
 
 
 	@Test
 	public void transform_noChange() throws Exception {
 		CharSepFormatter pip = new CharSepFormatter("","|","");
 		String expect = "Site_Id|Latitude|Longitude|create_date";
 		String actual = pip.transform(expect, pip);
 		assertEquals(expect,actual);
 		assertTrue(expect==actual); // pointer check intentional
 	}
 
 
 	//	@Test
 	//	public void getTableColumns() throws Exception {
 	//		List<Column> cols = new CharSepFormatter("","","").getTableColumns(rs);
 	//
 	//		assertEquals(4, cols.size());
 	//		assertEquals("Site_Id", cols.get(0).name);
 	//		assertEquals("Longitude", cols.get(2).name);
 	//		assertEquals(Types.VARCHAR, cols.get(0).type);
 	//		assertEquals(Types.DATE, cols.get(3).type);
 	//		assertEquals(3, cols.get(1).size);
 	//		assertEquals(8, cols.get(3).size);
 	//	}
 
 
 	@Test
 	public void getFileHeader() throws Exception {
 		String actual = new CharSepFormatter("","|","").fileHeader(cols);
 		String expect = "Site_Id|Latitude|Longitude|create_date"+IoUtils.LINE_SEPARATOR;
 		assertEquals(expect,actual);
 	}
 
 
 	@Test
 	public void getFileRows() throws Exception {
 		rs.open();
 		rs.next();
 		String actual = new CharSepFormatter("","|","").fileRow(new ResultSetColumnIterator(rs));
 		String expect = "1234567891|40.1|-90.1|1901-01-01"+IoUtils.LINE_SEPARATOR;
 		assertEquals(expect,actual);
 	}
 
 
 	@Test
 	public void getFileRows_ensureQuoteAroundDataContainingDelimitor() throws Exception {
 		rs.open();
 		rs.next(); // by pass first row
 		rs.next();
 		String actual = new CharSepFormatter("","|","").fileRow( new ResultSetColumnIterator(rs) );
 		String expect = "\"12345678|2\"|40.2|-90.2|1902-02-02"+IoUtils.LINE_SEPARATOR;
 		assertEquals(expect,actual);
 	}
 
 
 	@Test
 	public void constructor() throws Exception {
 		String contentType   = "a";
 		String separator     = "b";
 		String type          = "c";
 		CharSepFormatter frm = new CharSepFormatter(contentType, separator, type);
 
 		assertEquals(contentType, frm.getContentType());
 		assertEquals(separator, frm.getSeparator());
 		assertEquals(type, frm.getType());
 		assertEquals("."+type, frm.getFileType());
 
 	}
 
 
 }
