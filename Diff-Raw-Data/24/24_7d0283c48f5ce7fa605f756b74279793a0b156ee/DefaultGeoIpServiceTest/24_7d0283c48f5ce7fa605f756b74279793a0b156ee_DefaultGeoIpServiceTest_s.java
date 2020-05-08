 package com.aciertoteam.fastappoint.services.model;
 
 import com.aciertoteam.common.repository.EntityRepository;
 import com.aciertoteam.geo.entity.Country;
 import com.aciertoteam.geo.services.impl.DefaultGeoIpService;
 import jodd.bean.BeanUtil;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import static org.mockito.Mockito.when;
 
 /**
  * @author Bogdan Nechyporenko
  */
 public class DefaultGeoIpServiceTest {
 
     @Mock
     protected EntityRepository entityRepository;
 
     @InjectMocks
     private DefaultGeoIpService geoIpService = new DefaultGeoIpService();
 
     @Before
     public void setUp() {
         MockitoAnnotations.initMocks(this);
     }
 
     @Test
     public void defineCountryTest() {
        Country netherlands = new Country("label.country.netherlands");
        when(entityRepository.findByField(Country.class, "name", "label.country.netherlands")).thenReturn(netherlands);
         BeanUtil.setDeclaredPropertyForced(geoIpService, "geoIpFilePath", "/GeoIP.dat");
         assertEquals(netherlands, geoIpService.defineCountry("145.53.39.106"));
     }
 
     @Test
     public void defineCountryTestWithException() {
         BeanUtil.setDeclaredPropertyForced(geoIpService, "geoIpFilePath", "/emptyFile.txt");
         assertNull(geoIpService.defineCountry("91.218.213.156"));
     }
 }
