 package com.golddigger.utils.legacy;
 
 import static org.junit.Assert.*;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.golddigger.model.Game;
 import com.golddigger.services.SquareMoveService;
 import com.golddigger.services.NextService;
 import com.golddigger.services.ViewService;
 import com.golddigger.templates.GameTemplate;
 import com.golddigger.templates.generators.TemplateGenerator;
 
 import static com.golddigger.utils.LegacyTemplateParser.*;
 
 public class FieldFileTemplateGeneratorTest {
 	File tmp;
 //	TODO: Work out why files cant be deleted
 //	@Before
 //	public void setup() throws IOException{
 //		tmp = new File("tmp");
 //		if (tmp.exists()) delete(tmp);
 //		assertFalse(tmp.exists());
 //		assertTrue(tmp.mkdir());
 //		assertTrue(tmp.exists());
 //
 //		String str = buildSection(TILES, "wwwww\nw.b.w\nwwwww\n");
 //		str += buildSection(COSTS, "b=300");
 //		str += buildAttribute(LINE_OF_SIGHT, "3");
 //		
 //		File one = new File(tmp, "1.field");
 //		FileOutputStream fos = new FileOutputStream(one);
 ////		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(fos));
 ////		out.write(str);
 //
 //		fos.write(str.getBytes());
 ////		out.flush();
 ////		out.close();
 //		fos.flush();
 //		fos.close();
 //	}
 //	
 //	@After
 //	public void cleanup() throws IOException{
 //		delete(tmp);
 //		assertFalse(tmp.exists());
 //	}
 //	
 //	@Test
 //	public void test() throws IOException {
 //		TemplateGenerator gen = new FieldFileTemplateGenerator(tmp);
 //		GameTemplate[] templates = gen.generate();
 //		assertEquals(1, templates.length);
 //		
 //		int i=0;
 //		for (GameTemplate template : templates){
 //			Game game = template.build();
 //			List<ViewService> views = game.getServices(ViewService.class);
 //			assertEquals(1,views.size());
 //			if (i == 0){
 //				assertEquals(3, views.get(0).getLineOfSight());
 //			}
 //			List<MoveService> moves = game.getServices(MoveService.class);
 //			assertEquals(1, moves.size());
 //			if (i == 0){
 //				assertEquals(300, moves.get(0).getCost("b"));
 //			}
 //			assertEquals(1,game.getServices(MoveService.class).size());
 //			assertEquals(1,game.getServices(GrabService.class).size());
 //			assertEquals(1,game.getServices(DropService.class).size());
 //			assertEquals(1,game.getServices(CarryingService.class).size());
 //			assertEquals(1,game.getServices(ScoreService.class).size());
 //			assertEquals(1,game.getServices(NextService.class).size());
 //			i++;
 //		}
 //	}
 //	
 //	public static void delete(File target) throws IOException{
 //		if (target.isDirectory()) {
 //			for (File file : target.listFiles()){
 //				delete(file);
 //			}
 //		} else Files.delete(target.toPath());
 //	}
 
 }
