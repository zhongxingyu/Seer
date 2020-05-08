 package services;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.net.URI;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityTransaction;
 import javax.persistence.Persistence;
 import javax.persistence.Query;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response.ResponseBuilder;
 import javax.ws.rs.core.UriBuilder;
 import javax.xml.bind.JAXBElement;
 
 import weka.core.Instance;
 
 import classification.Classifier;
 
 import datasource.ImapDAO;
 import training.AccountTrainer;
 
 import entities.Account;
 //import entities.Filter;
 import entities.Model;
 import filters.Filter;
 import filters.FilterManager;
 import general.Email;
 
 import messages.*;
 
 @Path("provider")
 public class ClassificationResource {
 
 	@POST
 	@Path("register")
 	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
 	@Consumes(MediaType.APPLICATION_XML)
 	public Response addAccount(JAXBElement<Account> account) {
 		Account message = account.getValue();
 		Thread registerationThread = new AccountTrainer(message.getEmail(),
 				message.getToken());
 		registerationThread.start();
 		// Prepare the response
 		ResponseBuilder responseBuilder = Response.created(getBaseURI());
 		responseBuilder.status(202);
 		return responseBuilder.build();
 	}
 
 	@POST
 	@Path("classify")
 	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
 	public Response requestClassification(
 			JAXBElement<IncomingEmailMessage> message) {
 		final long startTime = System.currentTimeMillis();
 		IncomingEmailMessage msg = message.getValue();
 
 		System.out.println("Classification Request: " + msg.getUsername()
 				+ ", " + msg.getEmailId());
 
 		final long emailId = Long.parseLong(msg.getEmailId());
 
 		final EntityManager entityManager = Persistence.createEntityManagerFactory(
 				"smart_email").createEntityManager();
 		List<Account> accounts = entityManager.createQuery(
 				"select c from Account c where c.email = '" + msg.getUsername()
 						+ "'", Account.class).getResultList();
 		List<Model> modelsBlob = entityManager.createQuery(
 				"select c from Model c", Model.class).getResultList();
 
 		final Account account = accounts.get(0);
 		account.setLastVisit(new Date());
 		account.setTotalClassified(account.getTotalClassified() + 1);
 		float accuracy = (account.getTotalClassified() - account.getTotalIncorrect()) / (float) account.getTotalClassified();
 		account.setAccuracy(accuracy);
 
 		Classifier constructedModel = null;
 		ByteArrayInputStream bais = new ByteArrayInputStream(modelsBlob.get(0)
 				.getModel());
 		ObjectInputStream ois;
 		Filter[] filtersList = null;
 
 		try {
 			ois = new ObjectInputStream(bais);
 			constructedModel = (Classifier) ois.readObject();
 			bais.close();
 			ois.close();
 
 			bais = new ByteArrayInputStream(account.getFiltersList());
 			ois = new ObjectInputStream(bais);
 			filtersList = (Filter[]) ois.readObject();
 			bais.close();
 			ois.close();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 
 		final Classifier model = constructedModel;
 		final Filter[] filters = filtersList;
 
 		new Thread(new Runnable() {
 			public void run() {
 				ImapDAO dao = new ImapDAO(account.getEmail(), account
 						.getToken());
 				Email email = dao.getEmailByUID(emailId);
 				FilterManager filterManager = new FilterManager(filters, false);
 				Instance instance = filterManager.makeInstance(email);
 				int labelIndex = (int) model.classifyInstance(instance);
 				String labelName = instance.classAttribute().value(labelIndex);
 				dao.applyLabel(emailId, labelName);
 				System.err.println("The email was classified as: " + labelName);
 				double responseTime = (System.currentTimeMillis() - startTime)/1000.0;

				double avgResponseTime = 0;
				if(account.getAvgResponseTime() == 0){ //first time
					avgResponseTime = responseTime;
				} else{
					avgResponseTime = (account.getAvgResponseTime() + responseTime) / 2;
				}
 				account.setAvgResponseTime((float) avgResponseTime);
 				EntityTransaction entr = entityManager.getTransaction();
 				entr.begin();
 				entityManager.merge(account);
 				entr.commit();
 			}
 		}).start();
 
 		return Response.ok().build();
 	}
 
 	@PUT
 	@Path("unregister")
 	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
 	public Response deleteAccount(JAXBElement<DeleteAccountMessage> message) {
 		DeleteAccountMessage msg = message.getValue();
 
 		System.out.println("Delete Account: " + msg.getUsername());
 
 		return Response.ok().build();
 	}
 
 	@DELETE
 	@Path("{username}")
 	public void deleteAccount(@PathParam("username") String username) {
 		try {
 			System.out.println("Received delete account request..");
 			EntityManager entityManager = Persistence
 					.createEntityManagerFactory("smart_email")
 					.createEntityManager();
 			Account account = entityManager.find(Account.class, username);
 			entityManager.getTransaction().begin();
 			entityManager.remove(account);
 			Query query = entityManager.createQuery(
 					"delete from Model c where c.id.email = :username",
 					Model.class);
 			query.setParameter("username", username);
 			query.executeUpdate();
 			entityManager.getTransaction().commit();
 		} catch (Exception ex) {
 			throw new WebApplicationException(ex,
 					Response.Status.INTERNAL_SERVER_ERROR);
 		}
 	}
 
 	@PUT
 	@Path("feedback")
 	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
 	public Response classificationFeedback(
 			JAXBElement<ClassificationFeedbackMessage> message) {
 		ClassificationFeedbackMessage msg = message.getValue();
 
 		// TODO: implementation
 		//TODO: don't forget to update account statistics in the database..
 		System.out.println("feedback: " + msg.getEmailId()
 				+ ", labels list size = " + msg.getLabels().size());
 
 		return Response.ok().build();
 	}
 
 	@GET
 	@Produces(MediaType.TEXT_HTML)
 	public String sayHtmlHello() {
 		return "<html> " + "<title>" + "Hello Classifier" + "</title>"
 				+ "<body><h1>" + "Hello Classifier" + "</body></h1>"
 				+ "</html> ";
 	}
 
 	private static URI getBaseURI() {
 		return UriBuilder.fromUri("http://localhost:8080/smart_email").build();
 	}
 }
