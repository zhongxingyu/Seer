 package cz.muni.fi.pa165.pujcovnastroju.dao;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TransactionRequiredException;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Expression;
 import javax.persistence.criteria.Root;
 
 import org.springframework.stereotype.Repository;
 
 import cz.muni.fi.pa165.pujcovnastroju.entity.Loan;
 import cz.muni.fi.pa165.pujcovnastroju.entity.LoanStateEnum;
 import cz.muni.fi.pa165.pujcovnastroju.entity.Machine;
 import cz.muni.fi.pa165.pujcovnastroju.entity.SystemUser;
 
 /**
  * 
  * @author Ondřej Güttner
  */
 @Repository
 public class LoanDAOImpl implements LoanDAO {
 
 	@PersistenceContext
 	private EntityManager em;
 
 	public LoanDAOImpl() {
 	};
 
 	public LoanDAOImpl(EntityManager em) throws IllegalArgumentException {
 		if (em == null)
 			throw new IllegalArgumentException("em is null");
 		else
 			this.em = em;
 	}
 
 	public EntityManager getEm() {
 		return em;
 	}
 
 	public void setEm(EntityManager em) {
 		this.em = em;
 	}
 
 	public Loan create(Loan loan) throws IllegalArgumentException {
 		if (loan == null)
 			throw new IllegalArgumentException("loan is null");
 		em.persist(loan);
 		return loan;
 	}
 
 	public Loan update(Loan loan) throws IllegalArgumentException {
 		if (loan == null)
 			throw new IllegalArgumentException("loan is null");
 		if (loan.getId() == null)
 			throw new IllegalArgumentException("loan.id is null");
 
 		Loan loanStored = em.find(Loan.class, loan.getId());
 		if (loanStored != null)
 			em.merge(loan);
 		else
 			em.persist(loan);
 
 		return loan;
 	}
 
 	public Loan read(Long id) throws IllegalArgumentException {
 		if (id == null)
 			throw new IllegalArgumentException("id is null");
 
 		Loan loan = (Loan) em.find(Loan.class, id);
 		return loan;
 	}
 
 	public Loan delete(Long id) throws IllegalArgumentException {
 		if (id == null)
 			throw new IllegalArgumentException("id is null");
 
 		Loan loan = null;
 		loan = em.find(Loan.class, id);
 		try {
 			em.remove(loan);
 		} catch (TransactionRequiredException e) {
 			throw new IllegalArgumentException("transaction required", e);
 		}
 
 		return loan;
 	}
 
 	public List<Loan> getAllLoans() {
 		CriteriaBuilder cb = em.getCriteriaBuilder();
 		CriteriaQuery<Loan> cq = cb.createQuery(Loan.class);
 
 		Root<Loan> loanRoot = cq.from(Loan.class);
 		cq.select(loanRoot);
 
 		return em.createQuery(cq).getResultList();
 	}
 
 	public List<Loan> getLoansByParams(Date loanedFrom, Date loanedTill,
 			LoanStateEnum loanState, SystemUser loanedBy,
 			Machine includedMachine) {
 
 		CriteriaBuilder cb = em.getCriteriaBuilder();
 		CriteriaQuery<Loan> cq = cb.createQuery(Loan.class);
 
 		Root<Loan> loanRoot = cq.from(Loan.class);
 		cq.select(loanRoot);
 
 		if (loanedFrom != null) {
 			Expression<Date> loanedFromExp = loanRoot.get("loanTime");
 			cq.where(cb.greaterThanOrEqualTo(loanedFromExp, loanedFrom));
 		}
 		if (loanedTill != null) {
 			Expression<Date> loanedTillExp = loanRoot.get("returnTime");
 			cq.where(cb.greaterThanOrEqualTo(loanedTillExp, loanedTill));
 		}
 		if (loanState != null)
 			cq.where(cb.equal(loanRoot.get("loanState"), loanState));
 		if (loanedBy != null)
 			cq.where(cb.equal(loanRoot.get("customer"), loanedBy));
 		if (includedMachine != null) {
 			Expression<Collection> machinesExp = loanRoot.get("machines");
 			cq.where(cb.isMember(includedMachine, machinesExp));
 		}
 
 		return em.createQuery(cq).getResultList();
 	}
 
 }
