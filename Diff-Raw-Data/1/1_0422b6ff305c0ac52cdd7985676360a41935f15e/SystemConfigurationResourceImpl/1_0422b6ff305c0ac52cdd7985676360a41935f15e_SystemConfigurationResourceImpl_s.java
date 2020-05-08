 package org.tcrun.slickij.data;
 
 import com.google.code.morphia.Key;
 import com.google.code.morphia.query.Query;
 import com.google.inject.Inject;
import com.sun.xml.internal.bind.annotation.OverrideAnnotationOf;
 import org.bson.types.ObjectId;
 import org.tcrun.slickij.api.SystemConfigurationResource;
 import org.tcrun.slickij.api.data.InvalidDataError;
 import org.tcrun.slickij.api.data.SystemConfiguration;
 import org.tcrun.slickij.api.data.dao.SystemConfigurationDAO;
 import org.tcrun.slickij.api.data.AbstractSystemConfiguration;
 
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.QueryParam;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: jcorbett
  * Date: 12/3/12
  * Time: 1:37 PM
  */
 public class SystemConfigurationResourceImpl implements SystemConfigurationResource
 {
     private SystemConfigurationDAO sysconfigDAO;
 
     @Inject
     public SystemConfigurationResourceImpl(SystemConfigurationDAO sysconfigDAO)
     {
         this.sysconfigDAO = sysconfigDAO;
     }
 
     @Override
     public List<SystemConfiguration> getMatchingConfigurations(@QueryParam("name") @DefaultValue("") String name, @QueryParam("config-type") @DefaultValue("") String configType)
     {
         Query<AbstractSystemConfiguration> query = sysconfigDAO.createQuery();
         if(name != null && !name.equals(""))
         {
             query.criteria("name").equal(name);
         }
         if(configType != null && !configType.equals(""))
         {
             query.criteria("configurationType").equal(configType);
         }
         return new ArrayList<SystemConfiguration>(query.asList());
     }
 
     @Override
     public SystemConfiguration getConfigurationById(@PathParam("id") ObjectId id)
     {
         SystemConfiguration retval = sysconfigDAO.get(id);
         if(retval == null)
             throw new NotFoundError(SystemConfiguration.class, id.toString());
         return retval;
     }
 
     @Override
     public SystemConfiguration updateConfiguration(@PathParam("id") ObjectId id, SystemConfiguration configuration) throws InvalidDataError
     {
         AbstractSystemConfiguration original = (AbstractSystemConfiguration) getConfigurationById(id);
         original.update(configuration);
         original.validate();
         sysconfigDAO.save(original);
         return getConfigurationById(id);
     }
 
     @Override
     public SystemConfiguration createNewConfiguration(SystemConfiguration configuration) throws InvalidDataError
     {
         if(!(AbstractSystemConfiguration.class.isAssignableFrom(configuration.getClass())))
             throw new InvalidDataError(AbstractSystemConfiguration.class.getName(), "className", "All system configuration types must inherit from AbstractSystemConfiguration.");
         AbstractSystemConfiguration asc = (AbstractSystemConfiguration) configuration;
         asc.validate();
         sysconfigDAO.save(asc);
         return sysconfigDAO.get(asc.getObjectId());
     }
 
     @Override
     public SystemConfiguration deleteConfigurationById(@PathParam("id") ObjectId id)
     {
         AbstractSystemConfiguration config = (AbstractSystemConfiguration)getConfigurationById(id);
         sysconfigDAO.delete(config);
         return config;
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public <T extends SystemConfiguration> List<T> getMatchingConfigurationsOfType(Class<T> type, String name, String configType)
     {
         Query<AbstractSystemConfiguration> query = sysconfigDAO.createQuery();
         query.disableValidation();
         query.criteria("className").equal(type.getName());
         if(name != null && !name.equals(""))
             query.criteria("name").equal(name);
         if(configType != null && !configType.equals(""))
             query.criteria("configurationType").equal(configType);
         ArrayList<T> retval = new ArrayList<T>();
         for(AbstractSystemConfiguration config : query.fetch())
         {
             if(type.isAssignableFrom(config.getClass()))
                 retval.add((T)config);
         }
         return retval;
     }
 }
