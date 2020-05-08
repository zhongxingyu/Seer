 package com.adaptiweb.utils.poi;
 
 import java.util.Arrays;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.adaptiweb.utils.commons.StringUtils;
 
 public class HSSFRowSourceTest {
 	
 	private static final String TEST_RESOURCE = "/dummy-excel.xls";
 	
 	private static final List<String[]> DATA_SHEET1 = Arrays.asList(
 			new String[] {"col1", "emptycol", "col2", "col3"},
 			new String[] {"123.0", "", "10/07/10", "aaaaa"},
 			new String[] {"456.0", "", "11/08/11", "bbbb"});
 
 	private static final List<String[]> DATA_SHEET2 = Arrays.asList(
 			new String[] {"colA", "colB"},
 			new String[] {"10.0", "aaaaa"},
 			new String[] {"20.0", "bbbb"});
 	
 	HSSFRowSource source;
 	
 	@Before
 	public void prepareReader() {
		source = HSSFRowSource.fromResource(TEST_RESOURCE);
 		Assert.assertNotNull(source);
 	}
 	
 	@Test
 	public void testReader() {
 		source.useSheet(0);
 		for (String[] tested : DATA_SHEET1) {
 			Assert.assertEquals(StringUtils.join(tested, ";"), StringUtils.join(source.getStringArray(), ";"));
 		}
 		Assert.assertNull(source.getStringArray());
 		
 		source.useSheet("test2");
 		for (String[] tested : DATA_SHEET2) {
 			Assert.assertEquals(StringUtils.join(tested, ";"), StringUtils.join(source.getStringArray(), ";"));
 		}
 		Assert.assertNull(source.getStringArray());
 	}
 	
 }
