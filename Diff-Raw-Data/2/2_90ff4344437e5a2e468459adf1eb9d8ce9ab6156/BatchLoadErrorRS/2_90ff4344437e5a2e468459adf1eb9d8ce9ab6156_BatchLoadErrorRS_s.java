 package gov.osha.dteAdmin;
 
 
 import org.hibernate.StatelessSession;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.Response;
 import java.math.BigDecimal;
 import java.util.List;
 
 @Path("/Log")
 public class BatchLoadErrorRS {
     @Context
     HttpHeaders headers;
 
     private DteUser getCurrentUser(String oshaCn) {
         DteUserDao dteUserDao = DaoFactory.getDteUserDao();
         StatelessSession currentSession = HibernateUtil.getSessionFactory().openStatelessSession();
         currentSession.beginTransaction();
         DteUser currentUser = dteUserDao.getUserByOshaCN(oshaCn);
         currentSession.close();
         return currentUser;
     }
 
     @GET
     @Produces({"application/xml", "application/json"})
     @Path("{logId}")
    public BatchLoadError findCourseById(@PathParam("logId") BigDecimal logId) {
         DteUser currentUser = getCurrentUser(headers.getRequestHeader("OSHA_CN").get(0));
 
         if (!currentUser.getUserType().equals("S")) {
             throw new WebApplicationException(Response.Status.FORBIDDEN);
         }
 
         BatchLoadErrorDao batchLoadErrorDao = DaoFactory.getBatchLoadErrorDao();
         BatchLoadError retBatchLoadError = (BatchLoadError) batchLoadErrorDao.getById(logId);
 
         return retBatchLoadError;
     }
 
     @GET
     @Produces({"application/xml", "application/json"})
     public List<BatchLoadError> getBatchLoadErrors() {
         DteUser currentUser = getCurrentUser(headers.getRequestHeader("OSHA_CN").get(0));
 
         if (!currentUser.getUserType().equals("S")) {
             throw new WebApplicationException(Response.Status.FORBIDDEN);
         }
 
         List<BatchLoadError> retList;
 
         BatchLoadErrorDao batchLoadErrorDao = DaoFactory.getBatchLoadErrorDao();
         retList = batchLoadErrorDao.getBatchLoadErrors();
 
         return retList;
     }
 
     @POST
     @Consumes("application/json")
     public Response doPost(BatchLoadError inBatchLoadError) {
         DteUser currentUser = getCurrentUser(headers.getRequestHeader("OSHA_CN").get(0));
 
         inBatchLoadError.setUserCn(currentUser.getOshaCn());
         BatchLoadErrorDao batchLoadErrorDao = DaoFactory.getBatchLoadErrorDao();
         batchLoadErrorDao.save(inBatchLoadError);
 
         return Response.ok().build();
     }
 }
