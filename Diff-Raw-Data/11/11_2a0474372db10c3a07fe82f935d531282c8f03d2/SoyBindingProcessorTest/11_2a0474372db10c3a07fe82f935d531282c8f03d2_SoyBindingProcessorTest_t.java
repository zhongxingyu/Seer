 package org.atemsource.atem.impl.soydata;
 
 import javax.inject.Inject;
 
 import junit.framework.Assert;
 
 import org.atemsource.atem.api.EntityTypeRepository;
 import org.atemsource.atem.api.type.EntityType;
 import org.atemsource.atem.impl.soydata.entities.EntityA;
 import org.atemsource.atem.impl.soydata.entities.EntityB;
 import org.atemsource.atem.utility.binding.Binder;
 import org.atemsource.atem.utility.transform.api.SimpleTransformationContext;
 import org.atemsource.atem.utility.transform.impl.EntityTypeTransformation;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
import com.google.template.soy.data.SoyMapData;

 
 @ContextConfiguration(locations = {"classpath:/test/atem/soydata/transform.xml"})
 @RunWith(SpringJUnit4ClassRunner.class)
 public class SoyBindingProcessorTest
 {
 
 	@Inject
 	private EntityTypeRepository entityTypeRepository;
 	
 	@Inject
 	private Binder binder;
 
 	@Test
 	public void test()
 	{
 		EntityType<SoyMapData> soyType = entityTypeRepository.getEntityType("soy:" + EntityA.class.getName());
 		
 		EntityA a = new EntityA();
 		a.setB(new EntityB());
 		a.getB().setInteger(100);
 			EntityTypeTransformation<EntityA, SoyMapData> transformation =
 				binder.getTransformation(EntityA.class);
 		SoyMapData soy = transformation.getAB().convert(a, new SimpleTransformationContext(entityTypeRepository));
 		Assert.assertEquals(100, soy.getMapData("b").getInteger("integer"));
 
 	}
 }
