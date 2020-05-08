 package fr.csvbang.test.writer;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.BlockJUnit4ClassRunner;
 
 import fr.csvbang.configuration.CsvBangConfiguration;
 import fr.csvbang.exception.CsvBangException;
 import fr.csvbang.test.bean.writer.SimpleWriterBean;
 import fr.csvbang.util.ConfigurationUti;
 
 @RunWith(BlockJUnit4ClassRunner.class)
 public class WriterTest {
 
 	
 	private SimpleWriterTest<SimpleWriterBean> getSimpleWriter() throws CsvBangException{
 		CsvBangConfiguration conf = ConfigurationUti.loadCsvBangConfiguration(SimpleWriterBean.class);
 		return new SimpleWriterTest<SimpleWriterBean>(conf);
 	}
 	
 	@Test
 	public void simpleTest() throws CsvBangException{
 		SimpleWriterTest<SimpleWriterBean> writer = getSimpleWriter();
 		SimpleDateFormat format = new SimpleDateFormat(SimpleWriterBean.DATE_PATTERN);
 		
 		SimpleWriterBean bean = new SimpleWriterBean();
 		Calendar c = Calendar.getInstance();
 		bean.setBirthday(c);
 		bean.setId(125874);
 		bean.setPrice(28.35);
 		bean.setName("the name");
 		String result = "125874,the name,public Name: the name," + format.format(c.getTime()) + ",28.35";
 		
 		SimpleWriterBean bean2 = new SimpleWriterBean();
 		Calendar c2 = Calendar.getInstance();
 		bean2.setBirthday(c2);
 		bean2.setId(51445100);
 		bean2.setPrice(1287.45);
 		bean2.setName("super name");
 		result += "\n51445100,super name,public Name: super name," + format.format(c2.getTime()) + ",1287.45\n";
 		
 		writer.write(bean);
 		writer.write(bean2);
 		
 		Assert.assertEquals(result, writer.getResult());
 	
 	}
 	
 	@Test
 	public void simple2Test() throws CsvBangException{
 		SimpleWriterTest<SimpleWriterBean> writer = getSimpleWriter();
 		SimpleDateFormat format = new SimpleDateFormat(SimpleWriterBean.DATE_PATTERN);
 		
 		SimpleWriterBean bean = new SimpleWriterBean();
 		Calendar c = Calendar.getInstance();
 		bean.setBirthday(c);
 		bean.setId(125874);
 		bean.setName("the name");
		String result = "125874,the name,public Name: the name," + format.format(c.getTime());
 		
 		SimpleWriterBean bean2 = new SimpleWriterBean();
 		Calendar c2 = Calendar.getInstance();
 		bean2.setBirthday(c2);
 		bean2.setPrice(1287.45);
 		bean2.setName("super name");
		result += "\nsuper name,public Name: super name," + format.format(c2.getTime()) + ",1287.45\n";
 		
 		SimpleWriterBean bean3 = new SimpleWriterBean();
		bean.setId(125874);
 		bean3.setPrice(1287.45);
 		bean3.setName("super name");
		result += "\n125874,super name,public Name: super name," + format.format(c2.getTime()) + ",1287.45\n";
 		
 		writer.write(bean);
 		writer.write(bean2);
 		
 		Assert.assertEquals(result, writer.getResult());
 	
 	}
 }
