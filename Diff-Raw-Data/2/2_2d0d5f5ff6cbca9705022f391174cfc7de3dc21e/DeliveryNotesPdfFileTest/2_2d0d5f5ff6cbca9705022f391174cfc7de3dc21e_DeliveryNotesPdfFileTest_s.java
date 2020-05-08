 package de.switajski.priebes.flexibleorders.report.itextpdf;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockHttpServletResponse;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 import de.switajski.priebes.flexibleorders.domain.DeliveryNotes;
 import de.switajski.priebes.flexibleorders.domain.FlexibleOrder;
 import de.switajski.priebes.flexibleorders.domain.HandlingEventType;
 import de.switajski.priebes.flexibleorders.domain.OrderItem;
 import de.switajski.priebes.flexibleorders.domain.OriginSystem;
 import de.switajski.priebes.flexibleorders.domain.specification.ShippedSpecification;
 import de.switajski.priebes.flexibleorders.test.EntityBuilder.AddressBuilder;
 import de.switajski.priebes.flexibleorders.test.EntityBuilder.CatalogProductBuilder;
 import de.switajski.priebes.flexibleorders.test.EntityBuilder.HandlingEventBuilder;
 import de.switajski.priebes.flexibleorders.test.EntityBuilder.ItemBuilder;
 
 //TODO: split application context to use one part as unit test
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
 public class DeliveryNotesPdfFileTest {
 	
 	private static final String INVOICE_PDF_PATH = "src/test/java/de/switajski/priebes/flexibleorders/report/itextpdf/DeliveryNotesPdfFileTest.pdf";
 
 	DeliveryNotes deliveryNotes;
 	
 	private static final String I_NR = "13456";
 	
 	@Before
 	public void initData(){
 		deliveryNotes = new DeliveryNotes(I_NR, new ShippedSpecification(false, false),
				AddressBuilder.buildWithGeneratedAttributes(123));
 
 		OrderItem item1 = new ItemBuilder(
 			new FlexibleOrder(
 				"email@nowhere.com", 
 				OriginSystem.FLEXIBLE_ORDERS, 
 				I_NR),
 			CatalogProductBuilder.buildWithGeneratedAttributes(98760).toProduct(), 
 			0)
 		.generateAttributes(12)
 		.build();
 		
 		for (int i = 0; i< 35; i++){
 			item1.addHandlingEvent(
 					new HandlingEventBuilder(
 							HandlingEventType.SHIP, item1, i+1)
 					.setReport(deliveryNotes)
 					.build());
 		}
 	}
 	
 	@Transactional
 	@Test
 	public void shouldGenerateInvoice() throws Exception{
 		
 		DeliveryNotesPdfFile bpf = new DeliveryNotesPdfFile();
 		bpf.setFilePathAndName(INVOICE_PDF_PATH);
 		bpf.setLogoPath("C:/workspaces/gitRepos/FlexibleOrders/src/main/webapp/images/LogoGross.jpg");
         
 		Map<String,Object> model = new HashMap<String,Object>();
 		model.put("DeliveryNotes", deliveryNotes);
 
 		bpf.render(model, new MockHttpServletRequest(), new MockHttpServletResponse());
 
 	}
 }
