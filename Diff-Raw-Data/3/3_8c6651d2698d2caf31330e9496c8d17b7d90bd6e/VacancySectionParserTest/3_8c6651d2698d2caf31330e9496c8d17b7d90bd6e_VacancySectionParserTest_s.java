 package ru.xrm.app.parsers;
 
 import static org.junit.Assert.*;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import ru.xrm.app.config.Config;
 import ru.xrm.app.domain.Section;
 import ru.xrm.app.util.SectionSet;
 
 public class VacancySectionParserTest {
 
 	String html="";
 	
 	@Before
 	public void setup() throws IOException{
 
 		BufferedReader br;
 
 		InputStream in=getClass().getResourceAsStream("/sectionsparser/index.htm");
 		br=new BufferedReader(new InputStreamReader(in, "windows-1251"));	
 
 		StringBuilder content=new StringBuilder();
 		String line;
 
 		while((line=br.readLine())!=null){
 			content.append(line);
 		}
 		br.close();
 
 		html=content.toString();
 	}
 
 	@Test
 	public void test() {
 		
 		Config config=Config.getInstance();
 		try{
 			config.load("config.xml");
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 
 		VacancySectionParser parser=new VacancySectionParser(config, html);
 		List<Section> list=parser.parse();
 		
 		assertEquals(23, list.size());
 		assertEquals("Государственная служба", list.get(0).getName());
 		assertEquals("Информационные технологии и Интернет", list.get(1).getName());
 		assertEquals("Логистика, склад, ВЭД", list.get(2).getName());
 		assertEquals("Маркетинг, Реклама, PR", list.get(3).getName());
 		assertEquals("Медицина и фармация", list.get(4).getName());
 		assertEquals("Недвижимость", list.get(5).getName());
 		assertEquals("Образование и воспитание", list.get(6).getName());
 		assertEquals("Оптовая торговля", list.get(7).getName());
 		assertEquals("Продажа по каталогам, МЛМ", list.get(8).getName());
 		assertEquals("Производство", list.get(9).getName());
 		assertEquals("Розничная торговля", list.get(10).getName());
 		assertEquals("Руководители", list.get(11).getName());
 		assertEquals("Связь", list.get(12).getName());
 		assertEquals("Секретариат и АХО", list.get(13).getName());
 		assertEquals("СМИ, Издательство, полиграфия", list.get(14).getName());
 		assertEquals("Страхование", list.get(15).getName());
 		assertEquals("Строительство и архитектура", list.get(16).getName());
 		assertEquals("Сфера услуг", list.get(17).getName());
 		assertEquals("Транспорт, автобизнес", list.get(18).getName());
 		assertEquals("Управление персоналом", list.get(19).getName());
 		assertEquals("Финансы, бухгалтерия, банк", list.get(20).getName());
 		assertEquals("Юриспруденция", list.get(21).getName());
 		assertEquals("Другие сферы деятельности", list.get(22).getName());
 		
 		assertEquals(new Long(23), list.get(0).getId());
 		assertEquals(new Long(10), list.get(1).getId());
 		assertEquals(new Long(15), list.get(2).getId());
 		assertEquals(new Long(13), list.get(3).getId());
 		assertEquals(new Long(4),  list.get(4).getId());
 		assertEquals(new Long(7),  list.get(5).getId());
 		assertEquals(new Long(5),  list.get(6).getId());
 		assertEquals(new Long(20), list.get(7).getId());
 		assertEquals(new Long(24), list.get(8).getId());
 		assertEquals(new Long(6),  list.get(9).getId());
 		assertEquals(new Long(21), list.get(10).getId());
 		assertEquals(new Long(18), list.get(11).getId());
 		assertEquals(new Long(25), list.get(12).getId());
 		assertEquals(new Long(8),  list.get(13).getId());
 		assertEquals(new Long(9),  list.get(14).getId());
 		assertEquals(new Long(16), list.get(15).getId());
 		assertEquals(new Long(12), list.get(16).getId());
 		assertEquals(new Long(11), list.get(17).getId());
 		assertEquals(new Long(14), list.get(18).getId());
 		assertEquals(new Long(19), list.get(19).getId());
 		assertEquals(new Long(3),  list.get(20).getId());
 		assertEquals(new Long(17), list.get(21).getId());
 		assertEquals(new Long(2),  list.get(22).getId());
 		
		for(Section s:list){
			System.out.format("%d - %s\n", s.getId(), s.getName());
		}
 	}
 
 }
