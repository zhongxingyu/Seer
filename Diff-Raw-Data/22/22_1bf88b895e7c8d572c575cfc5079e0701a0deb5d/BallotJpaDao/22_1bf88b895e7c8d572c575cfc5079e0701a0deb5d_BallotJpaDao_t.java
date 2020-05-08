 package jacs.database.exceed.jpa;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityTransaction;
 import javax.persistence.Persistence;
 import javax.persistence.Query;
 
 import jacs.database.exceed.dao.BallotDAO;
 import jacs.database.exceed.model.Ballot;
 import jacs.database.exceed.model.User;
 
 public class BallotJpaDao implements BallotDAO{
 	
 	private static final String PERSISTENCE_UNIT = "exceedvote";
 	
 	private static EntityManagerFactory emf;
 	private static EntityManager em;
 	static {
 		emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
 		em = emf.createEntityManager();
 	}
 	
 	@Override
 	public List<Ballot> getVote(User user) {
 		Query q  = em.createQuery("SELECT user from User user WHERE user.id = :id");
 		q.setParameter("id",user.getId());
 		User u = (User) q.getSingleResult();
 		for (Ballot b : u.getBallots()) {
 			System.out.println(b);
 		}
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String saveBallot(Ballot b) {
 		// TODO Auto-generated method stub
 		System.out.printf("Saving Ballot : %s\n",b.getCriteria());
 		EntityTransaction tx = em.getTransaction();
 		try {
 				tx.begin();
 				em.persist(b);
 				tx.commit();
				System.out.printf("Ballot saved. id = %d : Vote %s by %s\n",b.getId(),b.getCriteria(),b.getUser().getUsername());
 				System.out.printf("Save Ballot Complete !!! \n");
 				return "VOTE_SUCCES" + ","+ b.getCriteria() +","+b.getUser().getUsername();
 		} catch (Exception ex) {
 			System.out.println("Exception occurred!");
 			if (tx.isActive()) tx.rollback();
 		}
 		return "VOTE_FAILED"+","+b.getCriteria()+","+b.getUser().getUsername();
 		
 	}
 	
 }
