 package com.adaptiweb.utils.csvbind;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.beans.IntrospectionException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringReader;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import au.com.bytecode.opencsv.bean.MappingStrategy;
 
 import com.adaptiweb.utils.csvbind.CsvReaderTest.CsvTestRecord3.TestEnum;
 import com.adaptiweb.utils.csvbind.annotation.CsvField;
 import com.adaptiweb.utils.csvbind.bean.Record1;
 import com.adaptiweb.utils.csvbind.bean.Record2;
 import com.adaptiweb.utils.csvbind.editor.CsvFieldDateEditor;
 import com.adaptiweb.utils.csvbind.editor.CsvFieldStringEditor;
 import com.adaptiweb.utils.poi.HSSFRowSource;
 
 public class CsvReaderTest {
     public static final String datePattern = "d.m.yyyy";
 	
     private Reader readerTest = null;
     private Reader readerImport = null;
     private HSSFRowSource excelSource = null;
 
     @Before
     public void setUp() throws Exception {
         InputStream is = CsvReaderTest.class.getClassLoader().getResourceAsStream("testCsvReader.csv");
         assertNotNull(is);
         InputStream cs = CsvReaderTest.class.getClassLoader().getResourceAsStream("testImport.csv");
         assertNotNull(cs);
         readerTest = new InputStreamReader(is);
         assertNotNull(readerTest);
         readerImport = new InputStreamReader(cs);
         assertNotNull(readerImport);
        excelSource = HSSFRowSource.fromResource("/testCsvReader.xls", CsvReaderTest.class);
         assertNotNull(excelSource);
     }
 
     @After
     public void tearDown() throws Exception {
         if (readerTest != null) readerTest.close();
         if (readerImport != null) readerImport.close();
     }
 
     private void testCsvReader(CsvReader<Record1> r1) {
         assertNotNull(r1);
         Iterator<Record1> it1 = r1.iterator();
         assertNotNull(it1);
         assertTrue(it1.hasNext());
         int c1 = 0;
         while (it1.hasNext()) {
             Record1 rec = it1.next();
             assertNotNull(rec);
             if (c1 == 0) {
                 //ADD;CREDIT;55555555555;DIRECT;backend processing;valid=1;quantity=10
                 assertEquals("ADD", rec.getAction());
                 assertEquals("CREDIT", rec.getSubject());
                 assertEquals("55555555555", rec.getCustomer());
                 assertEquals("DIRECT", rec.getChannel());
                 assertEquals("backend processing", rec.getSource());
                 assertNotNull(rec.getParameters());
                 assertEquals(2, rec.getParameters().size());
                 assertEquals("valid=1", rec.getParameters().get(0));
                 assertEquals("quantity=10", rec.getParameters().get(1));
             } else if (c1 == 1) {
                 //REMOVE;SERVICE;66666666666;DIRECT;backend processing;param1=value1;param2=value2;param3=value3
                 assertEquals("REMOVE", rec.getAction());
                 assertEquals("SERVICE", rec.getSubject());
                 assertEquals("66666666666", rec.getCustomer());
                 assertEquals("DIRECT", rec.getChannel());
                 assertEquals("backend processing", rec.getSource());
                 assertNotNull(rec.getParameters());
                 assertEquals(3, rec.getParameters().size());
                 assertEquals("param1=value1", rec.getParameters().get(0));
                 assertEquals("param2=value2", rec.getParameters().get(1));
                 assertEquals("param3=value3", rec.getParameters().get(2));
             } else if (c1 == 2) {
                 //ADD;RADIO;55555555555;DIRECT;"backend processing; ""test"""
                 assertEquals("ADD", rec.getAction());
                 assertEquals("RADIO", rec.getSubject());
                 assertEquals("55555555555", rec.getCustomer());
                 assertEquals("DIRECT", rec.getChannel());
                 assertEquals("backend processing; \"test\"", rec.getSource());
             } else if (c1 == 4) {
                 //SELL;MATERIAL1;11111111111;DIRECT;backend processing;a;b;c;d;
                 assertEquals("SELL", rec.getAction());
                 assertEquals("MATERIAL1", rec.getSubject());
                 assertEquals("11111111111", rec.getCustomer());
                 assertEquals("DIRECT", rec.getChannel());
                 assertEquals("backend processing", rec.getSource());
                 assertNotNull(rec.getParameters());
                 assertEquals(4, rec.getParameters().size());
                 assertEquals("a", rec.getParameters().get(0));
                 assertEquals("b", rec.getParameters().get(1));
                 assertEquals("c", rec.getParameters().get(2));
                 assertEquals("d", rec.getParameters().get(3));
             } else if (c1 == 7) {
                 //123
                 assertEquals("123", rec.getAction());
                 assertNull(rec.getSubject());
                 assertNull(rec.getCustomer());
                 assertNull(rec.getChannel());
                 assertNull(rec.getSource());
             }
             c1++;
         }
         assertTrue(c1 >= 8);
         r1.closeResources(); 
     }
     
     /**
      * Test method for {@link com.adaptiweb.utils.csvbind.CsvReader#iterator()}.
      */
     @Test
     public final void testIterator() {
         // Record1 reader from CSV file
     	testCsvReader(new CsvReader<Record1>(readerTest, Record1.class));
 
     	// Record1 reader from XLS file
     	testCsvReader(new CsvReader<Record1>(excelSource, Record1.class));
     	
         // Record2 from file
         CsvReader<Record2> r2 = new CsvReader<Record2>(readerImport, Record2.class);
         assertNotNull(r2);
         Iterator<Record2> it2 = r2.iterator();
         assertNotNull(it2);
         assertTrue(it2.hasNext());
         int c2 = 0;
         while (it2.hasNext()) {
             Record2 rec = it2.next();
             assertNotNull(rec);
             if (c2 == 0) {
                 assertEquals("48882467972", rec.getCustomer());
             } else if (c2 == 3) {
                 assertEquals("agdvr\"g;;;r", rec.getCustomer());
             }
             c2++;
         }
         assertTrue(c2 >= 4);
         r2.closeResources(); 
 
         // CsvTestRecord reader from string
         CsvReader<CsvTestRecord2> r3 = new CsvReader<CsvTestRecord2>(new StringReader("1;2;3;31.12.2008\n3;4\n5;6"), CsvTestRecord2.class);
         assertNotNull(r3);
         Iterator<CsvTestRecord2> it3 = r3.iterator();
         assertNotNull(it3);
         assertTrue(it3.hasNext());
         int c3 = 0;
         while (it3.hasNext()) {
             CsvTestRecord rec = it3.next();
             assertNotNull(rec);
             if (c3 == 0) {
                 assertEquals("1", rec.getField1());
                 assertEquals(2, rec.getField2());
             } else if (c3 == 1) {
                 assertEquals("3", rec.getField1());
                 assertEquals(4, rec.getField2());
             } else if (c3 == 2) {
                 assertEquals("5", rec.getField1());
                 assertEquals(6, rec.getField2());
                 assertFalse(it3.hasNext());
             } else {
                 fail();
             }
             c3++;
         }
         assertEquals(3, c3);
         r3.closeResources();
 
         // CsvTestRecord reader with array parsing
         CsvReader<CsvTestRecord3> r4 = new CsvReader<CsvTestRecord3>(
                 new StringReader("1;2;3;31.12.2008;" + TestEnum.CCC.name() + ";a1;a2;a3;a4\n3;4;;;" + TestEnum.BBB.name() + ";b1;b2;b3\n5;6"), 
                 CsvTestRecord3.class);
         assertNotNull(r4);
         Iterator<CsvTestRecord3> it4 = r4.iterator();
         assertNotNull(it4);
         assertTrue(it4.hasNext());
         int c4 = 0;
         while (it4.hasNext()) {
             CsvTestRecord3 rec = it4.next();
             assertNotNull(rec);
             if (c4 == 0) {
                 assertEquals("1", rec.getField1());
                 assertEquals(2, rec.getField2());
                 assertEquals(TestEnum.CCC, rec.getField5());
                 assertEquals(4, rec.getField6().size());
                 assertTrue(rec.getField6().get(0).equals("a1"));
                 assertTrue(rec.getField6().get(1).equals("a2"));
                 assertTrue(rec.getField6().get(2).equals("a3"));
                 assertTrue(rec.getField6().get(3).equals("a4"));
             } else if (c4 == 1) {
                 assertEquals("3", rec.getField1());
                 assertEquals(4, rec.getField2());
                 assertEquals(TestEnum.BBB, rec.getField5());
                 assertEquals(3, rec.getField6().size());
                 assertTrue(rec.getField6().get(0).equals("b1"));
                 assertTrue(rec.getField6().get(1).equals("b2"));
                 assertTrue(rec.getField6().get(2).equals("b3"));
             } else if (c4 == 2) {
                 assertEquals("5", rec.getField1());
                 assertEquals(6, rec.getField2());
                 assertFalse(it4.hasNext());
                 assertNull(rec.getField5());
             } else {
                 fail();
             }
             c4++;
         }
         assertEquals(3, c4);
         r4.closeResources();
     }
 
     /**
      * Test method for {@link au.com.bytecode.opencsv.bean.CsvToBean#processLine(au.com.bytecode.opencsv.bean.MappingStrategy, java.lang.String[])}.
      */
     @Test
     public final void testProcessLine() {
         CsvReaderPublic<CsvTestRecord3> r = new CsvReaderPublic<CsvTestRecord3>(new StringReader(""), CsvTestRecord3.class);
         try {
             CsvTestRecord2 t2 = null;
             // null line
             try {
                 t2 = (CsvTestRecord2) r.processLine(r.getMapping(), null);
             } catch (Exception e) {
                 assertTrue(e instanceof NullPointerException);
             }
             // wrong line, not parseable number
             try {
                 t2 = (CsvTestRecord2) r.processLine(r.getMapping(), new String[] {"", "2qe", "", ""});
             } catch (Exception e) {
                 assertTrue(e instanceof NumberFormatException);
             }
             // empty line
             t2 = (CsvTestRecord2) r.processLine(r.getMapping(), new String[] {});
             assertNotNull(t2);
             assertNull(t2.getField1());
             assertEquals(0, t2.getField2());
             assertNull(t2.getField3());
             assertNull(t2.getField4());
             // normal line
             t2 = (CsvTestRecord2) r.processLine(r.getMapping(), new String[] {"abcdefgh", "12345678", "aaaa5", "31.12.2008"});
             assertNotNull(t2);
             assertEquals("abcdefgh", t2.getField1());
             assertEquals(12345678, t2.getField2());
             assertEquals("aaaa5", t2.getField3());
             // too many columns in line, ignoring not defined columns
             t2 = (CsvTestRecord2) r.processLine(r.getMapping(), new String[] {"abcdefgh", "12345678", "aaaa5", "1.1.2008", "2", "3", "4", "5", "6", "7", "8", "9", "10"});
             assertNotNull(t2);
             assertEquals("abcdefgh", t2.getField1());
             assertEquals(12345678, t2.getField2());
             assertEquals("aaaa5", t2.getField3());
             
         } catch (Exception e) {
             e.printStackTrace();
             fail(e.getClass().getName() + ": " + e.getMessage());
         }
     }
     
     @Test
     public final void testRemoveComment() {
     	CsvReaderPublic<CsvTestRecord2> r = new CsvReaderPublic<CsvTestRecord2>(new StringReader(""), CsvTestRecord2.class);
     	assertNotNull(r);
     	String[] line = new String[] {"1", "2", "3", "4"};
     	assertFalse(r.removeComment(line));
     	assertTrue(Arrays.equals(new String[] {"1", "2", "3", "4"}, line));
     	line = new String[] {"#1", "2"};
     	assertTrue(r.removeComment(line));
     	assertTrue(Arrays.equals(new String[] {"", ""}, line));
     	line = new String[] {"1", "#2", "3"};
     	assertTrue(r.removeComment(line));
     	assertTrue(Arrays.equals(new String[] {"1", "", ""}, line));
     	line = new String[] {"1", "2", "3#abc"};
     	assertFalse(r.removeComment(line));
     	assertTrue(Arrays.equals(new String[] {"1", "2", "3#abc"}, line));
     }
     
     @Test
     public final void testIsEmptyLine() {
     	CsvReaderPublic<CsvTestRecord2> r = new CsvReaderPublic<CsvTestRecord2>(new StringReader(""), CsvTestRecord2.class);
     	assertNotNull(r);
     	String[] line = new String[] {};
     	assertTrue(r.isEmptyLine(line));
     	line = new String[] {"", "", ""};
     	assertTrue(r.isEmptyLine(line));
     	line = new String[] {"", null, "", null};
     	assertTrue(r.isEmptyLine(line));
     	line = new String[] {"", null, "", "1"};
     	assertFalse(r.isEmptyLine(line));
     	line = new String[] {"4", "2", "3", "1"};
     	assertFalse(r.isEmptyLine(line));
     }
 
     @Test
     public final void testAlignLineLeft() {
     	CsvReaderPublic<CsvTestRecord2> r = new CsvReaderPublic<CsvTestRecord2>(new StringReader(""), CsvTestRecord2.class);
     	assertNotNull(r);
     	Integer index = null;
     	String[] line = new String[] {};
     	index = r.alignLineLeft(line);
     	assertTrue(Arrays.equals(new String[] {}, line));
     	assertEquals((Integer) 0, index);
     	line = new String[] {"1", "2"};
     	index = r.alignLineLeft(line);
     	assertTrue(Arrays.equals(new String[] {"1", "2"}, line));
     	assertEquals((Integer) 0, index);
     	line = new String[] {"", "", "1", "2", ""};
     	index = r.alignLineLeft(line);
     	assertTrue(Arrays.equals(new String[] {"1", "2", "", "", ""}, line));
     	assertEquals((Integer) 2, index);
     	line = new String[] {"", "1"};
     	index = r.alignLineLeft(line);
     	assertTrue(Arrays.equals(new String[] {"1", ""}, line));
     	assertEquals((Integer) 1, index);
     	line = new String[] {"", "", "", "", "1"};
     	index = r.alignLineLeft(line);
     	assertTrue(Arrays.equals(new String[] {"1", "", "", "", ""}, line));
     	assertEquals((Integer) 4, index);
     	line = new String[] {"", "", "", "", "", "1", "2", "3", "", "", ""};
     	index = r.alignLineLeft(line);
     	assertTrue(Arrays.equals(new String[] {"1", "2", "3", "", "", "", "", "", "", "", ""}, line));
     	assertEquals((Integer) 5, index);
     }
 
     public static class CsvTestRecord {
         @CsvField(index = 0)
         private String field1;
         @CsvField(index = 1)
         private int field2;
         
         public CsvTestRecord() {}
         
         public CsvTestRecord(String field1, int field2) {
             this.field1 = field1;
             this.field2 = field2;
         }
         public String[] getOutput() {
             return new String[] {field1, "field2:" + field2};
         }
         public String getField1() {
             return field1;
         }
         public void setField1(String field1) {
             this.field1 = field1;
         }
         public int getField2() {
             return field2;
         }
         public void setField2(int field2) {
             this.field2 = field2;
         }
     }
     
     public static class CsvTestRecord2 extends CsvTestRecord {
     	@CsvField(index = 2)
     	private String field3;
     	@CsvField(index = 3, editor = CsvFieldDateEditor.class, patterns = {datePattern})
     	private Date field4;
 
     	public CsvTestRecord2() {}
     	
 		public CsvTestRecord2(String field1, int field2, String field3, Date field4) {
 			super(field1, field2);
 			this.field3 = field3;
 			this.field4 = field4;
 		}
 		
 		public String getField3() {
 			return field3;
 		}
 		public void setField3(String field3) {
 			this.field3 = field3;
 		}
 		public Date getField4() {
 			return field4;
 		}
 		public void setField4(Date field4) {
 			this.field4 = field4;
 		}
     }
 
     public static class CsvTestRecord3 extends CsvTestRecord2 {
     	public enum TestEnum {
     		AAA,
     		BBB,
     		CCC,
     		DDD;
     	}
     	
     	@CsvField(index = 4, enumType=TestEnum.class)
     	TestEnum field5;
     	
         @CsvField(index = 5, list = true, editor=CsvFieldStringEditor.class)
         private List<String> field6;
 
         public CsvTestRecord3() {}
         
         public CsvTestRecord3(String field1, int field2, String field3, Date field4, TestEnum field5, List<String> field6) {
             super(field1, field2, field3, field4);
             this.field5 = field5;
             this.field6 = field6;
         }
         
         public List<String> getField6() {
             return field6;
         }
         public void setField6(List<String> field6) {
             this.field6 = field6;
         }
 
 		public TestEnum getField5() {
 			return field5;
 		}
 
 		public void setField5(TestEnum field5) {
 			this.field5 = field5;
 		}
     }
 
     public static class CsvReaderPublic<T> extends CsvReader<T> {
         public CsvReaderPublic(Reader reader, Class<T> recordClass) {
             super(reader, recordClass);
         }
 		@Override
         public T processLine(MappingStrategy<T> mappingstrategy, String[] as) throws IllegalAccessException,
                 InvocationTargetException, InstantiationException, IntrospectionException {
             return super.processLine(mappingstrategy, as);
         }
         public MappingStrategy<T> getMapping() {
             return mapping;
         }
 		@Override
 		public boolean removeComment(String[] line) {
 			return super.removeComment(line);
 		}
 		@Override
 		public boolean isEmptyLine(String[] line) {
 			return super.isEmptyLine(line);
 		}
 		@Override
 		protected Integer alignLineLeft(String[] line) {
 			return super.alignLineLeft(line);
 		}
     }
 
 }
