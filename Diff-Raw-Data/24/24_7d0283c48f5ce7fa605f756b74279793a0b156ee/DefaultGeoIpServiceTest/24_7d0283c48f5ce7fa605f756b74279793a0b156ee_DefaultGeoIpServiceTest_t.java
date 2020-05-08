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
 
import java.io.File;

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
 
    private static final String TARGET_DIR = new File(DefaultGeoIpService.class.getResource(".").getPath(), "target").getPath();

     @Before
     public void setUp() {
         MockitoAnnotations.initMocks(this);
     }
 
     @Test
     public void defineCountryTest() {
        String netherlandsLabel = "label.country.netherlands";
        Country netherlands = new Country(netherlandsLabel);
        when(entityRepository.findByField(Country.class, "name", netherlandsLabel)).thenReturn(netherlands);
         BeanUtil.setDeclaredPropertyForced(geoIpService, "geoIpFilePath", "/GeoIP.dat");
        BeanUtil.setDeclaredPropertyForced(geoIpService, "geoIpFileLocalPath", TARGET_DIR);
         assertEquals(netherlands, geoIpService.defineCountry("145.53.39.106"));
     }
 
     @Test
     public void defineCountryTestWithException() {
         BeanUtil.setDeclaredPropertyForced(geoIpService, "geoIpFilePath", "/emptyFile.txt");
        BeanUtil.setDeclaredPropertyForced(geoIpService, "geoIpFileLocalPath", TARGET_DIR);
         assertNull(geoIpService.defineCountry("91.218.213.156"));
     }
 }
