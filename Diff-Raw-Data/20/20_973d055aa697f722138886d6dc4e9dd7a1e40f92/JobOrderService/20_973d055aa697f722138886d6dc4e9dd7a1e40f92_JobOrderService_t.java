 package service;
 
 import models.contracts.JobOrder;
 import models.school.Prestation;
 import models.school.SoeExam;
 import models.user.User;
 import org.joda.time.LocalDate;
 import pdf.JobOrderPdfGenerator;
 import play.libs.MimeTypes;
 import plugin.s3.model.S3Blob;
 
import javax.persistence.NoResultException;
 import javax.persistence.Query;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.List;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
  */
 public class JobOrderService {
 
     public List<JobOrder> getOrdersForUser(User user) {
 
         Query query = JobOrder.em().createQuery("select o from JobOrder as o where o.user = :user");
         query.setParameter("user", user);
 
         return query.getResultList();
     }
 
     public void createOrder(List<Prestation> prestations, List<SoeExam> soeExams, User user) {
 
         JobOrder order = new JobOrder();
         order.creationDate = new LocalDate();
         order.user = user;
         order.contract = user.contract;
         order.soeExams = soeExams;
         order.realCoursesProfessors = prestations;
 
         order.total = 0;
 
         for (Prestation rcp : prestations) {
             order.total += rcp.getTotal();
         }
 
         for (SoeExam soe : soeExams) {
             order.total += soe.getTotal();
         }
 
         order.save();
 
         try {
             File folder = new File("pdf/orders");
             folder.mkdirs();
 
             File file = File.createTempFile("order", ".pdf");
             new JobOrderPdfGenerator().generate(order, file);
 
             order.pdf = new S3Blob();
             order.pdf.set(new FileInputStream(file), MimeTypes.getContentType(file.getName()));
 
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
 
         order.save();
 
         new PrestationService().addJobOrderToPrestations(order, prestations);
     }
 
    public JobOrder getByIdAndUser(long id, User user) {
 
        Query query = JobOrder.em().createQuery("select jo from JobOrder jo where jo.user = :user and jo.id = :id");
        query.setParameter("user", user)
                .setParameter("id", id);
 
         try {
             return (JobOrder) query.getSingleResult();
        } catch (NoResultException e) {
             return null;
         }
 
     }
 }
