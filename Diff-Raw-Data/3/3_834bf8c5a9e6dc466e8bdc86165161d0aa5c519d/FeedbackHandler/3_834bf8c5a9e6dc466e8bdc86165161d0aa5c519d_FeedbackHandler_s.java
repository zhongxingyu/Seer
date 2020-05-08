 package services;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.List;
 
 import javax.mail.MessagingException;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityTransaction;
 import javax.persistence.Persistence;
 
 import weka.core.Instance;
 import classification.Classifier;
 import entities.Account;
 import entities.Model;
 import entities.ModelPK;
 import filters.Filter;
 import filters.FilterManager;
 import general.Email;
 import messages.ClassificationFeedbackMessage;
 
 public class FeedbackHandler extends Thread{
 	
 	private ClassificationFeedbackMessage requestMessage;
 	
 	public FeedbackHandler(ClassificationFeedbackMessage requestMessage){
 		this.requestMessage = requestMessage;
 	}
 	
 	public void run(){
 		Email email = new Email(requestMessage.getRawEmail());
 		try {
 			email.setHeader("X-label", requestMessage.getLabel());
 			System.out.println("feedback: " + email.getSubject()
 					+ ", labels list size = " + requestMessage.getLabel());
 		} catch (MessagingException e1) {
 			e1.printStackTrace();
 		}
 
 		// retrieve the user account from the database
 		EntityManager entityManager = Persistence.createEntityManagerFactory(
 				"smart_email").createEntityManager();
 
 		List<Account> accounts = entityManager.createQuery(
 				"select c from Account c where c.email = '" + requestMessage.getUsername()
 						+ "'", Account.class).getResultList();
 		Account account = accounts.get(0);
 
 		ModelPK pk = new ModelPK();
 		pk.setEmail(requestMessage.getUsername());
 		pk.setType("onlinenaivebayes");
 //		pk.setType("sgd");
 		Model modelBlob = entityManager.find(Model.class, pk);
 		Classifier model = null;
 		Filter[] filters = null;
 		Model updatedModel = null;
 	
 		if (modelBlob != null) {
 			try {
 				ByteArrayInputStream bais = new ByteArrayInputStream(
 						modelBlob.getModel());
 				ObjectInputStream ois = null;
 
 				// Desrialize user model
 				ois = new ObjectInputStream(bais);
 				model = (Classifier) ois.readObject();
 				bais.close();
 				ois.close();
 
 				// Deserialize user filters
 				bais = new ByteArrayInputStream(account.getFiltersList());
 				ois = new ObjectInputStream(bais);
 				filters = (Filter[]) ois.readObject();
 				bais.close();
 				ois.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 
 			FilterManager filterManager = new FilterManager(filters, true);
 			Instance instance = filterManager.makeInstance(email);
 
 			model.trainOnInstance(instance);
			System.out
					.println("Classifier has retrained using the user feedback!");
 			updatedModel = new Model();
 			updatedModel.setId(pk);
 			ByteArrayOutputStream bos = new ByteArrayOutputStream();
 			byte[] serializedModel = null;
 			try {
 				ObjectOutputStream oos = new ObjectOutputStream(bos);
 				oos.writeObject(model);
 				oos.flush();
 				oos.close();
 				bos.close();
 				serializedModel = bos.toByteArray();
 			} catch (IOException ex) {
 				ex.printStackTrace();
 			}
 			updatedModel.setModel(serializedModel);
 		} 
 
 		// update user statistics
 		account.setTotalIncorrect(account.getTotalIncorrect()+1);
 		float accuracy = (account.getTotalClassified() - account
 				.getTotalIncorrect()) / (float) account.getTotalClassified();
 		account.setAccuracy(accuracy);
 		
 		EntityTransaction entr = entityManager.getTransaction();
 		entr.begin();
 		if (updatedModel != null) {
 			entityManager.merge(updatedModel);
 		}
 		entityManager.merge(account);
 		entr.commit();
 	}
 }
