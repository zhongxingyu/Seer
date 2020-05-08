 package experiments.resources;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.ParameterizedType;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import experiments.exceptions.NotSupportedException;
 import experiments.exceptions.QuerySyntaxException;
 
 /**
  * @author zlatka
  * 
  */
 public abstract class RestResource<T> {
 
     public abstract Collection<T> getResources() throws NotSupportedException;
 
     public abstract T getResource(Long resourceId) throws NotSupportedException;
 
     public abstract T saveResource(T resource) throws NotSupportedException;
 
     public abstract T updateResource(T resource) throws NotSupportedException;
 
     public abstract void deleteResource(Long resourceId) throws NotSupportedException;
 
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public Response getAllResources() throws NotSupportedException {
         return Response.ok(getResources()).build();
     }
 
     @GET
     @Path("{resourceId}")
     @Produces(MediaType.APPLICATION_JSON)
     public Response find(@PathParam("resourceId") Long resourceId) throws NotSupportedException {
         return Response.ok(getResource(resourceId)).build();
     }
 
     @POST
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.APPLICATION_JSON)
     public Response save(T resource) throws NotSupportedException {
         return Response.ok(saveResource(resource)).build();
     }
 
     @PUT
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.APPLICATION_JSON)
     public Response update(T resource) throws NotSupportedException {
         return Response.ok(updateResource(resource)).build();
     }
 
     @DELETE
     @Path("{resourceId}")
     @Produces(MediaType.APPLICATION_JSON)
     public Response delete(@PathParam("resourceId") Long resourceId) throws NotSupportedException {
         deleteResource(resourceId);
         return Response.ok().build();
     }
 
     @GET
     @Path("exactmatch")
     @Produces(MediaType.APPLICATION_JSON)
     public Response filterResourcesBy(@QueryParam("attributes") String attributes, @QueryParam("values") String values)
             throws NotSupportedException, QuerySyntaxException {
 
         if (attributes == null && values == null) {
             return Response.ok(getResources()).build();
         } else if (attributes == null || values == null) {
             throw new QuerySyntaxException();
         } else if (attributes == "" || values == "") {
             throw new QuerySyntaxException();
         }
 
         String[] attrs = attributes.split(",");
         String[] vals = values.split(",");
 
         if (attrs.length != vals.length) {
             throw new QuerySyntaxException();
         }
 
         return Response.ok(getFilteredResources(attrs, vals)).build();
     }
 
     @GET
     @Path("{attribute}/{value}")
     @Produces(MediaType.APPLICATION_JSON)
     public Response filterByAttribute(@PathParam("attribute") String attribute, @PathParam("value") String value)
             throws NotSupportedException {
         return Response.ok(getFilteredResourcesWithExactMatch(attribute, value)).build();
     }
 
     public Collection<T> getFilteredResources(String[] attributes, String[] values) throws NotSupportedException {
         List<T> result = new ArrayList<T>();
         for (int i = 0; i < attributes.length; i++) {
             result.addAll(getFilteredResourcesWithExactMatch(attributes[i], values[i]));
         }
         return result;
     }
 
     private Collection<T> getFilteredResourcesWithExactMatch(String attribute, String value) throws NotSupportedException {
         List<T> result = new ArrayList<T>();
         Method method = null;
         Class<T> type = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
         for (Method m : type.getDeclaredMethods()) {
             if (m.getName().equalsIgnoreCase("get" + attribute)) {
                 method = m;
             }
         }
         if (method != null) {
             for (T t : getResources()) {
                 try {
                     Class<?> returnType = method.getReturnType();
                     Object o = method.invoke(t);
                     if (returnType.cast(o).equals(returnType.getConstructor(String.class).newInstance(value))) {
                         result.add(t);
                     }
                 } catch (Exception e) {
                     // TODO log and show exception
                 }
             }
         }
         return result;
     }
 
 }
