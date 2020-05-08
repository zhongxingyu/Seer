 package md.frolov.legume.client.gin;
 
 import javax.inject.Singleton;
 
 import com.google.gwt.inject.client.AbstractGinModule;
 
 import md.frolov.legume.client.Application;
 import md.frolov.legume.client.elastic.ElasticSearchService;
 import md.frolov.legume.client.elastic.ElasticSearchServiceImpl;
 import md.frolov.legume.client.service.ColorizeService;
 import md.frolov.legume.client.service.ConfigurationService;
 import md.frolov.legume.client.service.impl.ColorizeServiceImpl;
 import md.frolov.legume.client.service.impl.ConfigurationServiceImpl;
import md.frolov.legume.client.util.ColorUtils;
 
 public class BusinessModule extends AbstractGinModule {
 
     @Override
     protected void configure() {
         bind(Application.class).in(Singleton.class);
 
         bind(ConfigurationService.class).to(ConfigurationServiceImpl.class);
         bind(ElasticSearchService.class).to(ElasticSearchServiceImpl.class);
         bind(ColorizeService.class).to(ColorizeServiceImpl.class);

        bind(ColorUtils.class);
     }
 }
