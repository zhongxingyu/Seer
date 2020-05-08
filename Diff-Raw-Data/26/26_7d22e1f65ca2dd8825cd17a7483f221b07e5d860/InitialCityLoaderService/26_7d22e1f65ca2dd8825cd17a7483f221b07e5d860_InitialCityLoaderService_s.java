 package oshop.services;
 
 import com.fasterxml.jackson.core.type.TypeReference;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.context.ApplicationListener;
 import org.springframework.context.event.ContextRefreshedEvent;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import oshop.dao.GenericDao;
 import oshop.model.City;
 
 import javax.annotation.Resource;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 @Service
 public class InitialCityLoaderService implements ApplicationListener<ContextRefreshedEvent> {
 
     public static final String CITIES_FILE_NAME = "/cities/ua-cities.json";
 
     private static final Log log = LogFactory.getLog(InitialCityLoaderService.class);
 
     @Resource
     private GenericDao<City, Integer> cityDao;
 
     private volatile boolean citiesLoaded = false;
     private final Object monitor = new Object();
 
     @Override
     @Transactional(readOnly = false)
     public void onApplicationEvent(ContextRefreshedEvent event) {
         if (citiesLoaded) {
             return;
         }
 
         synchronized (monitor) {
             if (citiesLoaded) {
                 return;
             }
 
             int size = cityDao.list(null, null).size();
 
             if (size == 0) {
                 List<City> cities = null;
                 try {
                     cities = getCities();
                 } catch (IOException e) {
                     log.error("Could not get cities list: ", e);
                     return;
                 }
 
                 int i = 1;
                 for (City city: cities) {
                     cityDao.add(city);
 
                     if ( i % 20 == 0 ) {
                         cityDao.getSession().flush();
                         cityDao.getSession().clear();
                     }
 
                     i++;
                 }
             }
 
             citiesLoaded = true;
         }
     }
 
     private List<City> getCities() throws IOException {
         InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(CITIES_FILE_NAME);
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         IOUtils.copy(is, baos);
 
         String citiesAsString = new String(baos.toByteArray(), "UTF-8");
 
         ObjectMapper mapper = new ObjectMapper();
         return mapper.readValue(citiesAsString, new TypeReference<List<City>>(){});
     }
 }
