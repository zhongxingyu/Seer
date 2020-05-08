 package cz.muni.fi.pa165.pujcovnastroju.dao;
 
 import java.util.ArrayList;
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
 import javax.persistence.Query;
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.Predicate;
 
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
 	
 	private Loan mergeParams(Loan loan) {
 		SystemUser user = loan.getCustomer();
		if (user != null && user.getId() != null) {
			user = em.find(SystemUser.class, user.getId());
			loan.setCustomer(user);
		}
 		
 		List<Machine> machines = loan.getMachines();
 		if (machines != null) {
 		    List<Machine> machinesToMerge = new ArrayList<>();
 		    
 		    for (Machine machine : machines) {
 			/*Query availableMachine = em.createQuery(
 			    "SELECT machine FROM Machine machine JOIN machine.loans loan WHERE machine.id = :machineId AND loan.loanTime<:rTime AND loan.returnTime>:lTime AND loan.id!=:loanId")
 			    .setParameter("rTime", loan.getReturnTime()).setParameter("lTime", loan.getLoanTime()).setParameter("loanId", loan.getId()).setParameter("machineId", machine.getId());
 			machine = (Machine) availableMachine.getSingleResult();*/
 			if (!machinesToMerge.contains(machine)) {
 				machine = em.merge(machine);
 				machinesToMerge.add(machine);
 			}
 			
 		    }
 		    loan.setMachines(machinesToMerge);
 		    
 		}
 		return loan;
 	}
 
 	@Override
 	public Loan create(Loan loan) throws IllegalArgumentException {
 		if (loan == null)
 			throw new IllegalArgumentException("loan is null");
 		
 		loan = this.mergeParams(loan);
 		
 		em.persist(loan);
 		return loan;
 	}
 
 	@Override
 	public Loan update(Loan loan) throws IllegalArgumentException {
 		if (loan == null)
 			throw new IllegalArgumentException("loan is null");
 		if (loan.getId() == null)
 			throw new IllegalArgumentException("loan.id is null");
 
 		Loan loanStored = em.find(Loan.class, loan.getId());
 		
 		loan = this.mergeParams(loan);
 		
 		if (loanStored != null) em.merge(loan);
 		else em.persist(loan);
 
 		return loan;
 	}
 
 	@Override
 	public Loan read(Long id) throws IllegalArgumentException {
 		if (id == null)
 			throw new IllegalArgumentException("id is null");
 
 		Loan loan = (Loan) em.find(Loan.class, id);
 		return loan;
 	}
 
 	@Override
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
 
 	@Override
 	public List<Loan> getAllLoans() {
 		CriteriaBuilder cb = em.getCriteriaBuilder();
 		CriteriaQuery<Loan> cq = cb.createQuery(Loan.class);
 
 		Root<Loan> loanRoot = cq.from(Loan.class);
 		cq.select(loanRoot);
 
 		return em.createQuery(cq).getResultList();
 	}
 
 	@Override
 	public List<Loan> getLoansByParams(Date loanedFrom, Date loanedTill,
 			LoanStateEnum loanState, SystemUser loanedBy,
 			Machine includedMachine) {
 
 		CriteriaBuilder cb = em.getCriteriaBuilder();
 		CriteriaQuery<Loan> cq = cb.createQuery(Loan.class);
 
 		Root<Loan> loanRoot = cq.from(Loan.class);
 		cq.select(loanRoot);
 		
 		List<Predicate> predicates = new ArrayList<>();
 
 		if (loanedFrom != null) {
 			Expression<Date> loanedFromExp = loanRoot.get("loanTime");
 			predicates.add(cb.greaterThanOrEqualTo(loanedFromExp, loanedFrom));
 		}
 		if (loanedTill != null) {
 			Expression<Date> loanedTillExp = loanRoot.get("returnTime");
 			predicates.add(cb.greaterThanOrEqualTo(loanedTillExp, loanedTill));
 		}
 		if (loanState != null)
 			predicates.add(cb.equal(loanRoot.get("loanState"), loanState));
 		if (loanedBy != null)
 			predicates.add(cb.equal(loanRoot.get("customer"), loanedBy));
 		if (includedMachine != null) {
 			Expression<Collection> machinesExp = loanRoot.get("machines");
 			predicates.add(cb.isMember(includedMachine, machinesExp));
 		}
 		
 		cq.where(predicates.toArray(new Predicate[]{}));
 
 		return em.createQuery(cq).getResultList();
 	}
 
 }
