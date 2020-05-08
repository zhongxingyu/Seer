 package fr.jerep6.ogi.rest;
 
 import static org.fest.assertions.api.Assertions.*;
 import static org.mockito.Mockito.*;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 
 import fr.jerep6.ogi.enumeration.EnumCategory;
 import fr.jerep6.ogi.framework.test.AbstractTest;
 import fr.jerep6.ogi.persistance.bo.Address;
 import fr.jerep6.ogi.persistance.bo.Description;
 import fr.jerep6.ogi.service.ServiceRealProperty;
 import fr.jerep6.ogi.transfert.bean.DescriptionTo;
 import fr.jerep6.ogi.transfert.bean.RealPropertyTo;
 import fr.jerep6.ogi.utils.Data;
 
 /**
  * Aucun accès à la BD pour les TU du projet ws
  * 
  * @author jerep6
  */
 @ContextConfiguration(locations = { "classpath:META-INF/spring/tu-web-context.xml",
 		"classpath:META-INF/spring/web-context.xml" })
 public class WSRealPropertyTest extends AbstractTest {
 
 	@Autowired
 	private WSRealProperty		wsRealProperty;
 
 	@Autowired
 	private ServiceRealProperty	serviceRealProperty;
 
 	@Test
 	public void readPropertyLiveable() {
 		RealPropertyTo read = wsRealProperty.read("ref1");
 		assertThat(read).isNotNull();
 
 		assertThat(read.getCategory().getCode()).isEqualTo(EnumCategory.HOUSE.getCode());
 		assertThat(read.getEquipments()).containsOnly("Cheminée", "Interphone");
 
 		// Description
 		Description d = Data.getFarm().getDescriptions().iterator().next();
 		DescriptionTo d1 = new DescriptionTo();
 		d1.setType(d.getType().getCode());
 		d1.setLabel(d.getLabel());
		assertThat(read.getDescriptions()).contains(d1);
 
 		// Address
 		Address a = Data.getAddressTyrosse();
 		assertThat(read.getAddress().getNumber()).isEqualTo(a.getNumber());
 		assertThat(read.getAddress().getCity()).isEqualTo(a.getCity());
 		assertThat(read.getAddress().getLatitude()).isEqualTo(a.getLatitude());
 
 		// Type
 		assertThat(read.getType()).isEqualTo(Data.getTypeFarm().getLabel());
 
 		// Diagnosis
 		assertThat(read.getDiagnosis()).isNotNull();
 
 	}
 
 	@Before
 	public void setup() {
 		when(serviceRealProperty.readByReference("ref1")).thenReturn(Data.getFarm());
 	}
 }
