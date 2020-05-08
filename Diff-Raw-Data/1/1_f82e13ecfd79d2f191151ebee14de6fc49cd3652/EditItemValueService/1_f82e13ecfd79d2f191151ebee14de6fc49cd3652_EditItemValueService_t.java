 package infowall.domain.service;
 
 import java.io.IOException;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.SerializationConfig;
 import org.codehaus.jackson.node.ObjectNode;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import infowall.domain.model.Dashboard;
 import infowall.domain.model.DashboardItem;
 import infowall.domain.model.EditItemValue;
 import infowall.domain.model.ItemRef;
 import infowall.domain.model.ItemValue;
 import infowall.domain.persistence.DashboardRepository;
 import infowall.domain.persistence.ItemValueRepository;
 import infowall.web.services.errorhandling.ErrorNotifier;
 
 @Component
 public class EditItemValueService {
 
     private final Logger logger = LoggerFactory.getLogger(EditItemValueService.class);
 
     private final ItemValueRepository itemValueRepository;
     private final DashboardRepository dashboardRepository;
     private final ObjectMapper objectMapper;
 
     @Autowired
     public EditItemValueService(final ItemValueRepository itemValueRepository, final DashboardRepository dashboardRepository) {
         this.itemValueRepository = itemValueRepository;
         this.dashboardRepository = dashboardRepository;
         objectMapper = new ObjectMapper();
         objectMapper.getSerializationConfig().enable(SerializationConfig.Feature.INDENT_OUTPUT);
     }
 
     public EditItemValue find(ItemRef itemRef){
         final Dashboard dashboard = dashboardRepository.get(itemRef.getDashboardId());
         if(dashboard == null){
             return null;
         }
 
         ItemValue itemValue = itemValueRepository.findMostRecentItemValue(itemRef);
 
         final DashboardItem dashboardItem = dashboard.find(itemRef);
         if(dashboardItem == null){
             return null;
         }
 
         String data = "";
         if(itemValue != null){
             try {
                 data = objectMapper.writeValueAsString(itemValue.getData());
             } catch (IOException e) {
                 logger.error("cannot convert json to string.",e);
             }
         }
         return new EditItemValue(itemRef,dashboard.getTitle(),dashboardItem.getTitle(), data);
     }
 
     public void save(final ItemRef itemRef,final String data,ErrorNotifier errorNotifier){
         try {
             ObjectNode json = objectMapper.readValue(data, ObjectNode.class);
 
             ItemValue itemValue = new ItemValue(itemRef,json);
             itemValueRepository.put(itemValue);
 
         }catch (IOException e){
             errorNotifier.addError("Could not write data.");
            logger.error("Could not write data '{}', {}",data,e);
         }
     }
 }
