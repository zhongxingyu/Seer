 package test;
 
 import java.util.Arrays;
 import java.util.Collection;
 
 import org.eweb4j.config.EWeb4JConfig;
 import org.eweb4j.orm.Db;
 import org.eweb4j.orm.config.ORMConfigBeanUtil;
 import org.eweb4j.orm.dao.DAOFactory;
 import org.eweb4j.util.CommonUtil;
 import org.eweb4j.util.ReflectUtil;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import test.po.Master;
 import test.po.Pet;
 
 /**
  * TODO
  * @author weiwei l.weiwei@163.com
  * @date 2013-1-22 下午04:02:15
  */
 public class TestDb {
 	@BeforeClass
 	public static void prepare() throws Exception {
 		String err = EWeb4JConfig.start("start.eweb.xml");
 		if (err != null) {
 			throw new Exception(err);
 		}
 	}
 	
 	@Test
	public void testDel() throws Exception{
		Db.ar(Pet.class).delete("byMaster", 12);
	}
	
 	public void test() throws Exception{
 		String query = ORMConfigBeanUtil.parseQuery("master <= ? and user > ?", Pet.class);
 		System.out.println(query);
 		Pet p = Db.ar(Pet.class).find("master <= ? and user > ?", 1, 2).first();
 		System.out.println(p);
 	}
 	
 	public void testOrmUtil() throws Exception{
 		String[] fields = new ReflectUtil(Master.class).getFieldsName();
 		System.out.println(Arrays.asList(fields));
 		Master m = DAOFactory.getDAO(Master.class).fetch("pets").selectAll().queryOne();
 		System.out.println(m);
 	}
 	
 	public void testBatchInsert() throws Exception {
 		Pet p1 = new Pet();
 		p1.setName("pet_1");
 		Pet p2 = new Pet();
 		p2.setName("pet_2");
 		Pet p3 = new Pet();
 		p3.setName("pet_3");
 		Pet p4 = new Pet();
 		p4.setName("pet_4");
 		
 //		Number[] ids = DAOFactory.getInsertDAO().batchInsert(p1, p2, p3, p4);
 //		System.out.println(Arrays.asList(ids));
 		Db.batchInsert(new Pet[]{p1,p2,p3,p4}, "name");
 		System.out.println(CommonUtil.toJson(Arrays.asList(p1, p2, p3, p4)));
 	}
 	
 	public void testBatchUpdate() throws Exception {
 		Pet p1 = new Pet();
 		p1.setPetId(14);
 		p1.setName("name up 11");
 		Pet p2 = new Pet();
 		p2.setPetId(15);
 		p2.setName("name up 22");
 		Pet p3 = new Pet();
 		p3.setPetId(16);
 		p3.setName("name up 33");
 		Pet p4 = new Pet();
 		p4.setPetId(17);
 		p4.setName("name up 44");
 		
 		Number[] rs = DAOFactory.getUpdateDAO().batchUpdate(new Pet[]{p1, p2, p3, p4}, "name");
 		System.out.println(Arrays.asList(rs));
 	}
 }
