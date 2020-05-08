 package t.tools.xml;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.junit.Test;
 
 import com.ztools.xml.XMLBean;
 import com.ztools.xml.XMLHandler;
 import com.ztools.xml.XMLReader;
 import com.ztools.xml.XMLWriter;
 import com.ztools.xml.ZHandler;
 
 public class XMLRWTest {
 
     @Test
     public void testFilterChar() {
 
         int[][] RESTRICTED_CHAR_BLOCKS = { { 0x0, 0x8 }, { 0xB, 0xC },
                 { 0xE, 0x1F }, { 0x7F, 0x84 }, { 0x86, 0x9F },
                 { 0xFDD0, 0xFDDF }, { 0xFFFE, 0xFFFF } };
         List<String> sl = new ArrayList<String>();
         for (int i = 0; i < RESTRICTED_CHAR_BLOCKS.length; i++) {
             for (int j = RESTRICTED_CHAR_BLOCKS[i][0]; j <= RESTRICTED_CHAR_BLOCKS[i][1]; j++) {
                 sl.add(j + " input> 这是[" + (char) j + "]的输出测试");
             }
         }
 
         for (int i = 0; i < sl.size(); i++) {
             System.out.println(sl.get(i) + " "
                     + XMLWriter.toXmlString(sl.get(i)));
         }
     }
 
     @Test
     public void testObjectToXml() {
         Person p = new Person("Name" + 3, 3 + "", "f");
 
         Object[] arr = { 1, "2", 1 };
         p.setArr(arr);
 
         List<String> c = new ArrayList<String>();
         c.add("��&#1232;&amp;and;");
         c.add("xxx");
         p.setChildren(c);
         List<Integer> t = new ArrayList<Integer>();
         t.add(1);
         t.add(333);
         List<Integer> tt = new ArrayList<Integer>();
         tt.add(11);
         tt.add(333);
         p.setThr(tt);
 
         List<Object> objList = new ArrayList<Object>();
         objList.add(1); // int
         objList.add(1123132123123L); // long
         objList.add(1.00001D); // double
         objList.add(9.99f); // float
         objList.add((short) 5); // short
         objList.add((byte) 120); // byte
         objList.add(true);
 
         objList.add('c'); // char
         objList.add("hello<>&';:\"<![CDATA[abc]]> xml");
         objList.add(new Person("aaa", "12", "f"));
         objList.add(p);
         p.setObjlist(objList);
 
         List<House> houses = new ArrayList<House>();
         House h = new House("hourse1", "beijing", 100);
         List<Room> rooms = new ArrayList<Room>();
         rooms.add(new Room(1));
         rooms.add(new Room(2));
         rooms.add(new Room(3));
         h.setRooms(rooms);
         houses.add(h);
         houses.add(new House("hourse2", "wuhan", 200));
         houses.add(new House("hourse3", "wuhan", 120));
 
         p.setHouses(houses);
        List<Person> list = new ArrayList<Person>();
        list.add(p);
         // XMLBean xmlbean = new XMLBean("f:/person.xml");
         // xmlbean.setBean(p);
         // try {
         // XMLWriter.writeXmlBean(xmlbean);
         // } catch (IOException e) {
         // // TODO Auto-generated catch block
         // e.printStackTrace();
         // }
         System.out.println(XMLWriter.objectToXmlString(p));
     }
 
     public static String[] filterWord(String[] rows, String[] cols) {
         // 填充表格
         int rowCount = rows.length + 1;
         int colCount = cols.length + 1;
         int[][] tbl = new int[rowCount][colCount];
         for (int i = 0; i < rows.length; i++) {
 
             for (int j = 0; j < cols.length; j++) {
                 if (0 <= rows[i].indexOf(cols[j])) {
                     tbl[i][j] = 1;
                     tbl[i][colCount - 1] += 1;
                     tbl[rowCount - 1][j] += 1;
                 }
             }
         }
 
         // 过滤
         Set<Integer> filter = new HashSet<Integer>();
         for (int i = 0; i < rows.length; i++) {
             if (tbl[i][cols.length] >= 2) {
                 for (int j = 0; j < cols.length; j++) {
                     if (tbl[i][j] == 1) {
                         for (int k = 0; k < rows.length; k++) {
                             if (k != i
                                     && tbl[k][j] == 1
                                     && tbl[k][cols.length] < tbl[i][cols.length]) {
                                 filter.add(k);
                             }
                         }
                     }
                 }
             } else if (tbl[i][cols.length] == 0)
                 filter.add(i);
         }
 
         String[] ret = new String[rows.length - filter.size()];
         int index = 0;
         for (int i = 0; i < rows.length; i++) {
             if (!filter.contains(i)) {
                 ret[index++] = rows[i];
             }
         }
         System.out.println("filter: " + filter);
         // System.out.println(ret);
 
         for (int j = 0; j < colCount - 1; j++) {
             System.out.print("\t" + cols[j]);
         }
         System.out.println();
         for (int i = 0; i < rowCount; i++) {
             if (i < rowCount - 1)
                 System.out.print(rows[i] + "\t");
             else
                 System.out.print("\t");
             for (int j = 0; j < colCount; j++) {
                 System.out.print(tbl[i][j] + "\t");
             }
             System.out.println();
         }
         return ret;
     }
 
     @Test
     public void testTbl() {
         String[] r = { "奥迪Q1", "CC", "F3CC", "奥迪A4", "宝马X3", "奥迪A6", "奥迪A6L",
                 "大众CC", "宝马320", "力帆320" };
         String[] c = { "3" };// , "A6", "X3", "F3", "CC","320","宝", "马","Q"
         String[] ret = filterWord(r, c);
         System.out.println("过滤后的结果：");
         for (int i = 0; i < ret.length; i++) {
             System.out.println(ret[i]);
         }
     }
 
     @Test
     public void testPrint() throws Exception {
         Object[] os = { 1, "2" };
         Class<?> c = Class.forName("[Ljava.lang.Object;");// os.getClass();
         if (c.isArray()) {
             // c.
             System.out.println("arr.........." + c.getComponentType());
             System.out.println("arr..........");
         }
         // Array.get
         System.out.println(os.getClass().getName());
         System.out.println(Integer.toHexString('<'));
         System.out.println(Integer.toHexString('>'));
         System.out.println("abc<![CDATA[kkk]]>abc".replaceAll(
                 "(\\u003c!\\u005bCDATA\\u005b|\\u005d\\u005d\\u003e)", " "));
     }
 
     @Test
     public void testNReader() {
         ZHandler h = new ZHandler();
         XMLBean xb = new XMLBean("e:/person.xml");
         xb.setHandler(h);
         XMLReader.readXMLBean(xb);
         System.out.println(xb.getBean());
 
         // XMLBean xmlBean = new XMLBean("e:/person3.xml",Person.class);
         // List<Object > list = new ArrayList<Object>();
 
     }
 
     @Test
     public void testReader() {
         XMLBean xmlBean = new XMLBean("e:/person2.xml", Person.class);
         // xmlBean.setRootName("com.ztools.xml.IXMLProcessHexCodeImpl");
         ArrayList<Person> list = new ArrayList<Person>();
         xmlBean.setItemList(list);
         XMLReader.readXMLBean(xmlBean);
     }
 
     @Test
     public void testReadAndWrite() {
 
         XMLBean xmlBean = new XMLBean("e:/person.xml", Person.class);
         List<Person> list = new ArrayList<Person>();
         Person father = new Person("Fafala", "55", "f");
         father.setDate(new Date());
         father.setWifeCount(3);
         for (int i = 0; i < 1; i++) {
             Person p = new Person("Name" + i, i + "", "f");
 
             Object[] arr = { 1, "2", father };
             p.setArr(arr);
 
             List<String> c = new ArrayList<String>();
             c.add("��&#1232;&amp;and;");
             c.add("xxx");
             p.setChildren(c);
             p.setFather(father);
             List<Integer> t = new ArrayList<Integer>();
             t.add(1);
             t.add(333);
             List<Integer> tt = new ArrayList<Integer>();
             tt.add(11);
             tt.add(333);
             p.setThr(tt);
 
             List<Object> objList = new ArrayList<Object>();
             objList.add(1); // int
             objList.add(1123132123123L); // long
             objList.add(1.00001D); // double
             objList.add(9.99f); // float
             objList.add((short) 5); // short
             objList.add((byte) 120); // byte
             objList.add(true);
 
             objList.add('c'); // char
             objList.add("hello<>&';:\"<![CDATA[abc]]> xml");
             objList.add(new Person("aaa", "12", "f"));
             objList.add(p);
             p.setObjlist(objList);
 
             List<House> houses = new ArrayList<House>();
             House h = new House("hourse1", "beijing", 100);
             List<Room> rooms = new ArrayList<Room>();
             rooms.add(new Room(1));
             rooms.add(new Room(2));
             rooms.add(new Room(3));
             h.setRooms(rooms);
             houses.add(h);
             houses.add(new House("hourse2", "wuhan", 200));
             houses.add(new House("hourse3", "wuhan", 120));
 
             p.setHouses(houses);
             list.add(p);
         }
         xmlBean.setItemList(list);
         try {
             XMLWriter.writeXmlBean(xmlBean);
             Thread.sleep(1000);
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (InterruptedException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         XMLBean xmlBean2 = new XMLBean("e:/person.xml", Person.class);
         list = new ArrayList<Person>();
         xmlBean2.setItemList(list);
         xmlBean2.setHandler(new ZHandler());
         XMLReader.readXMLBean(xmlBean2);
         System.out.println(xmlBean2.getBean());
         xmlBean2.setPath("e:/person2.xml");
         try {
             XMLWriter.writeXmlBean(xmlBean2);
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     @Test
     public void testXmlString() {
         // com.ztoole.test.XMLRealBean
         XMLBean xmlBean2 = new XMLBean("e:/101378.xml");
         List<XMLRealBean> list;
         list = new ArrayList<XMLRealBean>();
         xmlBean2.setItemList(list);
         XMLReader.readXMLBean(xmlBean2);
 
         System.out.println(xmlBean2.getItemList());
 
         xmlBean2.setPath("e:/101378-2.xml");
         try {
             XMLWriter.writeXMLBean(xmlBean2);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     @Test
     public void testBaseDataType() {
         XMLBean xmlBean = new XMLBean("e:/base_data_type.xml", Object.class);
         List<Object> list = new ArrayList<Object>();
         String a = null;
         int i = 3;
         list.add(new String[] { "a", "b" });
         Map<String, String> m = new HashMap<String, String>();
         m.put("a", "a0001");
         m.put("b", "b0001");
         m.put("c", "c0001");
         list.add(m);
         list.add(i); // int
         list.add(a); // int
         list.add(i); // int
         list.add(1123132123123L); // long
         list.add(1.00001D); // double
         list.add(9.99f); // float
         list.add((short) 5); // short
         list.add((byte) 120); // byte
         list.add(true);
 
         list.add((char) 0x0ffff); // char
         list.add("hello<>&';:\" xml\u3FFFE;");
         Person p = new Person("aaa", "12", "");
         list.add(p);
 
         // xmlBean.setItemList(list);
         xmlBean.setBean(list);
         try {
             XMLWriter.writeXmlBean(xmlBean);
             // XMLWriter.writeXMLBean(xmlBean);
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         XMLBean xmlBean2 = new XMLBean("e:/base_data_type.xml", Person.class);
         // xmlBean.
         // list = new ArrayList<Object>();
         // xmlBean2.setItemList(list);
         // xmlBean2.setHandler(new ZHandler());
         XMLReader.readXMLBean(xmlBean2);
 
         System.out.println("item: " + xmlBean2.getBean());
         xmlBean2.setPath("e:/base_data_type2.xml");
         try {
             XMLWriter.writeXmlBean(xmlBean2);
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
     }
 
     public static void main(String[] args) {
 //        args = new String[] {"e:/person2.xml"};
         if (1 > args.length) {
             System.out
                     .println("XMLRWTest <filepath> [handle] [charset]"
                             + "\r\n\tfilepath : xml file path."
                             + "\r\n\t  handle : default and '0' is ZHandler, other is XMLHandler."
                             + "\r\n\t charset : utf-8 gbk gb2312 ...");
             return;
         }
         
         XMLBean xmlBean = new XMLBean(args[0]);
         if (2 <= args.length)  {
             if (!"0".equals(args[1])) {
                 xmlBean.setHandler(new XMLHandler(xmlBean));
             }
         }
         
         if (3 == args.length) {
             xmlBean.setCharset(args[2]);
         }
         System.out.print("Reading ...");
         XMLReader.readXMLBean(xmlBean);
         System.out.print(" over \r\nParse ...\r\n");
         Object obj = xmlBean.getBean();
         System.out.println(XMLWriter.objectToXmlString(obj));
         System.out.println("over!");
     }
     
     @Test
     public void testXmlStringToObject(){
     	String str = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<XMLBean class=\"com.ztoole.test.XMLNewsBean\" >\r\n<XMLNewsBean class=\"com.ztoole.test.XMLNewsBean\" hashcode=\"1234382199\"><location hashcode=\"813561857\" class=\"java.lang.String\"><![CDATA[新闻评论]]></location><content hashcode=\"1327290590\" class=\"java.lang.String\"><![CDATA[中共十七届六中全会提出推动社会主义文化大发展大繁荣，让我非常欣慰。这项决定是非常有战略眼光的。作为全国人大代表，我曾在2008年的全国两会上建议把文化发展作为国家战略。我认为，必须把文化发展放到国家战略的高度来认识和把握，尽快制定文化领域的各项法律、法规，以促进文化的发展和文化事业的建设。要实现文化大发展大繁荣，最根本的是要文化创新。繁荣和发展文化，要吸收中国传统文化和外国优秀文化，但是只停留在继承和传承的基础上是远远不够的。最根本的是要在继承和借鉴的基础上进行文化创新。文化创新首先要培养文化创造者，培养“大师”。我们需要培养的是文化界的“乔布斯”，一个乔布斯先后领导和推出了风靡全球亿万人的电子产品，深刻地改变了现代通讯、娱乐乃至生活的方式，而且鼓励了无数人致力于技术创新。而一个文化界的“乔布斯”不仅能够为我们带来丰富的文化产品，他带动的是一个文化创新的群体，这个群体会为我们创造出更多的优秀文化成果。应该重视“大师”的示范、带动效应，应该有意识地培育能够产生“大师”的土壤。要实现文化大繁荣大发展，应该致力于创造划时代的文化产品。文化强国必须有强国文化。我认为，强国文化不仅是优秀的文化产品，还必须具有时代标志性和创新理念。还是在2008年全国两会的小组发言上，我讲过这样一个故事，当时我7岁的孩子认为动画片都是西方出的，小孩还认为“没到迪士尼，就不算去了香港”。从这个小故事，我们可以看出文化的影响力之巨大。一个迪士尼早已不单单从事影视制作，支撑迪士尼运作的有五大事业部：影视娱乐、媒体网络、互动媒体、相关消费品、主题乐园。迪斯尼已经形成了一个完整的文化创意产业。我认为，我们缺的正是这样具有鲜明中国特色和时代特点的文化和文化产业。要实现文化大繁荣和大发展，应该鼓励打造划时代的文化作品。划时代的文化作品从内容上来看，应该是民族性和世界性的结合，并且具有创造性；是中国儒、道、佛思想和基督教为主体的西方文化的结合，在两种不同的文化之间寻找共通点；既保留了中华民族最有价值的文化，又符合世界文化的特点。这样才能发挥中国当代文化的创造力，使中国当代文化成为世界文化体系的一部分，并对世界文化有所贡献。从形式上来看，划时代的文化产品形态应该与高新技术相结合，这是我们所处的信息化时代的特点所要求的。文化产业只有充分利用高新技术，特别是要和现在的信息技术结合，才能适应信息社会的广泛需要。http://opinion.china.com.cn/opinion_73_29173.html]]></content><title hashcode=\"-698663213\" class=\"java.lang.String\"><![CDATA[只有强国文化才能文化强国]]></title><clickNum hashcode=\"0\" class=\"java.lang.String\"><![CDATA[]]></clickNum><replyNum hashcode=\"0\" object_ref=\"true\" class=\"java.lang.String\"/><auther hashcode=\"0\" object_ref=\"true\" class=\"java.lang.String\"/><pr hashcode=\"5\" class=\"java.lang.Integer\">5</pr><docId hashcode=\"84122053\" class=\"java.lang.String\"><![CDATA[170001345134]]></docId><extractData hashcode=\"-995329406\" class=\"java.lang.String\"><![CDATA[2011-12-02 01:00:00]]></extractData><siteName hashcode=\"638470240\" class=\"java.lang.String\"><![CDATA[优讯汽车]]></siteName><uri hashcode=\"475015319\" class=\"java.lang.String\"><![CDATA[http://opinion.china.com.cn/opinion_73_29173.html]]></uri><downloadDate hashcode=\"1333954784\" class=\"java.lang.String\"><![CDATA[0000-00-00]]></downloadDate><metaDescription hashcode=\"0\" object_ref=\"true\" class=\"java.lang.String\"/><metaKeyword hashcode=\"0\" object_ref=\"true\" class=\"java.lang.String\"/></XMLNewsBean>\r\n</XMLBean>";
     	
     	XMLReader.xmlStringToObject(str);
     }
 }
