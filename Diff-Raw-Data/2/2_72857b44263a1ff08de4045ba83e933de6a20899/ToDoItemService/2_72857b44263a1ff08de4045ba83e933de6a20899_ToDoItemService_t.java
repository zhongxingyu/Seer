 package replaceme.services;
 
 import org.apache.commons.lang.StringUtils;
 import org.jboss.resteasy.spi.validation.ValidateRequest;
 import replaceme.model.ToDoItem;
 import replaceme.model.ToDoItemDummyListFactory;
 
 import javax.validation.Valid;
 
 import javax.validation.constraints.NotNull;
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 @Path("/ToDo")
 public class ToDoItemService extends CRUDService<ToDoItem> {
 
 	//Just to provide a quick CRUD store...
     private static Map<String, ToDoItem> todos = ToDoItemDummyListFactory.createToDosDummyMap();
 
     //The same for ID
    private static int counter = 10;
 
     @Override
     @Path("/create")
     @POST
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     @ValidateRequest
     public ToDoItem create(@Valid ToDoItem resource) {        
         resource.setId(counter + "");
         counter++;        
         todos.put(resource.getId(), resource);
         return resource;
     }
 
     @Override
     @Path("read")
     @GET
     @ValidateRequest
     public ToDoItem read(@NotNull String id) {
         return todos.get(id);
     }
 
     @Override
     @Path("/update")
     @POST
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     @ValidateRequest
     public ToDoItem update(@Valid ToDoItem resource) {
         if(StringUtils.isNotBlank(resource.getId())) {
             todos.put(resource.getId(), resource);
         } else {
             throw new IllegalArgumentException("Can not update ToDoItem with no ID attribute!");
         }
         return todos.get(resource.getId());
     }
 
     @Override
     @Path("delete")
     @DELETE
     @Consumes(MediaType.APPLICATION_JSON)
     @ValidateRequest
     public void delete(@Valid ToDoItem resource) {
         if(StringUtils.isNotBlank(resource.getId())) {
             todos.put(resource.getId(), resource);
         } else {
             throw new IllegalArgumentException("Can not delete ToDoItem with no ID attribute!");
         }
         todos.remove(resource.getId());
     }
 
     @Override
     @Path("/all")
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public List<ToDoItem> all() {
         List<ToDoItem> toDoItemList = new ArrayList<ToDoItem>(todos.values());
         return toDoItemList;
     }
 
     @Path("share")
     @POST
     @ValidateRequest
     public void share(@NotNull String email) {
 
     }
 
 }
