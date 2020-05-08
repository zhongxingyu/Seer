 package com.s1mbi0se.dmp.transformation;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.junit.Test;
 
 import br.com.s1mbi0se.zorbawrapper.JsonTransformation;
 import br.com.s1mbi0se.zorbawrapper.JsonTransformationException;
 
 public class TestBasicTransformations {
 
 	@Test
 	public void testTransform() throws JsonTransformationException, IOException {
 		try {
 			InputStream query = TestBasicTransformations.class
					.getResourceAsStream("/query.json");
 			JsonTransformation jsonTransformation = new JsonTransformation(
 					query);
 
 			InputStream originData1 = TestBasicTransformations.class
 					.getResourceAsStream("/origin_data.json");
 			InputStream originData2 = TestBasicTransformations.class
 					.getResourceAsStream("/origin_data2.json");
 			String o1 = JsonTransformation.readFile(originData1);
 			String o2 = JsonTransformation.readFile(originData2);
 			for (int i = 0; i < 100; i++) {
 				System.out.println("i=" + i);
				System.out.println("Transformation result 1 = "
						+ jsonTransformation.transform(o1));
				System.out.println("Transformation result 2 = "
						+ jsonTransformation.transform(o2));
 			}
 		} catch (Throwable t){
 			t.printStackTrace();
 		} finally {
 			JsonTransformation.finishAll();
 			System.exit(0);
 		}
 	}
 }
