 package org.lightmare.jpa.jta;
 
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.ejb.EJBException;
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 import javax.ejb.TransactionManagementType;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityTransaction;
 import javax.transaction.HeuristicMixedException;
 import javax.transaction.HeuristicRollbackException;
 import javax.transaction.RollbackException;
 import javax.transaction.SystemException;
 import javax.transaction.UserTransaction;
 
 import org.hibernate.cfg.NotYetImplementedException;
 import org.lightmare.ejb.handlers.BeanHandler;
 import org.lightmare.ejb.meta.MetaContainer;
 import org.lightmare.ejb.meta.MetaData;
 import org.lightmare.utils.ObjectUtils;
 
 /**
  * Class to manage {@link javax.transaction.UserTransaction} for
  * {@link javax.ejb.Stateless} bean {@link java.lang.reflect.Proxy} calls
  * 
  * @author levan
  * 
  */
 public class BeanTransactions {
 
     private static final String MANDATORY_ERROR = "TransactionAttributeType.MANDATORY must always be called within transaction";
 
     private static final String NEVER_ERROR = "TransactionAttributeType.NEVER is called within transaction";
 
     private static final String SUPPORTS_ERROR = "TransactionAttributeType.SUPPORTS is not yet implemented";
 
     /**
      * Inner class to cache {@link EntityTransaction}s and {@link EntityManager}
      * s in one {@link Collection} for {@link UserTransaction} implementation
      * 
      * @author levan
      * 
      */
     private static class TransactionData {
 
 	EntityManager em;
 
 	EntityTransaction entityTransaction;
     }
 
     private static TransactionData createTransactionData(
 	    EntityTransaction entityTransaction, EntityManager em) {
 
 	TransactionData transactionData = new TransactionData();
 	transactionData.em = em;
 	transactionData.entityTransaction = entityTransaction;
 
 	return transactionData;
     }
 
     /**
      * Gets existing transaction from cache
      * 
      * @param entityTransactions
      * @return {@link UserTransaction}
      */
     public static UserTransaction getTransaction(
 	    EntityTransaction... entityTransactions) {
 
 	UserTransaction transaction = MetaContainer.getTransaction();
 	if (transaction == null) {
 	    transaction = new UserTransactionImpl(entityTransactions);
 	    MetaContainer.setTransaction(transaction);
 	}
 
 	// If entityTransactions array is available then adds it to
 	// UserTransaction object
 	if (ObjectUtils.available(entityTransactions)) {
 
 	    ((UserTransactionImpl) transaction)
 		    .addTransactions(entityTransactions);
 	}
 
 	return transaction;
     }
 
     /**
      * Gets existing transaction from cache
      * 
      * @param entityTransactions
      * @return {@link UserTransaction}
      */
     public static UserTransaction getTransaction(Collection<EntityManager> ems) {
 
 	UserTransaction transaction = MetaContainer.getTransaction();
 	if (transaction == null) {
 	    transaction = new UserTransactionImpl();
 	    MetaContainer.setTransaction(transaction);
 	}
 
 	Collection<TransactionData> entityTransactions = getEntityTransactions(ems);
 	addEntityTransactions((UserTransactionImpl) transaction,
 		entityTransactions);
 
 	return transaction;
     }
 
     /**
      * Gets appropriated {@link TransactionAttributeType} for instant
      * {@link Method} of {@link javax.ejb.Stateless} bean
      * 
      * @param metaData
      * @param method
      * @return {@link TransactionAttributeType}
      */
     public static TransactionAttributeType getTransactionType(
 	    MetaData metaData, Method method) {
 
 	TransactionAttributeType attrType = metaData.getTransactionAttrType();
 	TransactionManagementType manType = metaData.getTransactionManType();
 
 	TransactionAttribute attr = method
 		.getAnnotation(TransactionAttribute.class);
 
 	TransactionAttributeType type;
 	if (manType.equals(TransactionManagementType.CONTAINER)) {
 
 	    if (attr == null) {
 		type = attrType;
 	    } else {
 		type = attr.value();
 	    }
 	} else {
 	    type = null;
 	}
 
 	return type;
     }
 
     /**
      * Gets status of passed transaction by {@link UserTransaction#getStatus()}
      * method call
      * 
      * @param transaction
      * @return <code>int</code>
      * @throws IOException
      */
     private static int getStatus(UserTransaction transaction)
 	    throws IOException {
 
 	int status;
 	try {
 	    status = transaction.getStatus();
 	} catch (SystemException ex) {
 	    throw new IOException(ex);
 	}
 
 	return status;
     }
 
     /**
      * Checks if transaction is active and if it is not vegins transaction
      * 
      * @param entityTransaction
      */
     private static void beginEntityTransaction(
 	    EntityTransaction entityTransaction) {
 
 	if (!entityTransaction.isActive()) {
 	    entityTransaction.begin();
 	}
     }
 
     private static EntityTransaction getEntityTransaction(EntityManager em) {
 
 	EntityTransaction entityTransaction;
 	if (em == null) {
 	    entityTransaction = null;
 	} else {
 	    entityTransaction = em.getTransaction();
 	    beginEntityTransaction(entityTransaction);
 	}
 
 	return entityTransaction;
     }
 
     private static Collection<TransactionData> getEntityTransactions(
 	    Collection<EntityManager> ems) {
 
 	Collection<TransactionData> entityTransactions = null;
 	if (ObjectUtils.available(ems)) {
 	    entityTransactions = new ArrayList<TransactionData>();
 	    for (EntityManager em : ems) {
 		EntityTransaction entityTransaction = getEntityTransaction(em);
 		TransactionData transactionData = createTransactionData(
 			entityTransaction, em);
 		entityTransactions.add(transactionData);
 	    }
 	}
 	return entityTransactions;
     }
 
     private static void addEntityTransaction(UserTransactionImpl transaction,
 	    EntityTransaction entityTransaction, EntityManager em) {
 
 	if (ObjectUtils.notNull(entityTransaction)) {
 	    transaction.addTransaction(entityTransaction);
 	}
 	if (ObjectUtils.notNull(em)) {
 	    transaction.addEntityManager(em);
 	}
     }
 
     private static void addEntityTransactions(UserTransactionImpl transaction,
 	    Collection<TransactionData> entityTransactions) {
 
 	if (ObjectUtils.available(entityTransactions)) {
 	    for (TransactionData transactionData : entityTransactions) {
 		addEntityTransaction(transaction,
 			transactionData.entityTransaction, transactionData.em);
 	    }
 	}
     }
 
     private static void addEntityManager(UserTransactionImpl transaction,
 	    EntityManager em) {
 
 	if (ObjectUtils.notNull(em)) {
 	    transaction.addEntityManager(em);
 	}
     }
 
     private static void addEntityManagers(UserTransactionImpl transaction,
 	    Collection<EntityManager> ems) {
 	if (ObjectUtils.available(ems)) {
 	    for (EntityManager em : ems) {
 		addEntityManager(transaction, em);
 	    }
 	}
     }
 
     private static void addReqNewTransaction(UserTransactionImpl transaction,
 	    EntityTransaction entityTransaction, EntityManager em) {
 
 	if (ObjectUtils.notNull(entityTransaction)) {
 	    transaction.pushReqNew(entityTransaction);
 	}
 	if (ObjectUtils.notNull(em)) {
 	    transaction.pushReqNewEm(em);
 	}
     }
 
     private static void addReqNewTransactions(UserTransactionImpl transaction,
 	    Collection<TransactionData> entityTransactions) {
 
 	if (ObjectUtils.available(entityTransactions)) {
 	    for (TransactionData transactionData : entityTransactions) {
 		addReqNewTransaction(transaction,
 			transactionData.entityTransaction, transactionData.em);
 	    }
 	}
     }
 
     /**
      * Decides whether create or join {@link UserTransaction} by
      * {@link TransactionAttribute} annotation
      * 
      * @param handler
      * @param type
      * @param transaction
      * @param em
      * @throws IOException
      */
     private static void addTransaction(BeanHandler handler,
 	    TransactionAttributeType type, UserTransactionImpl transaction,
 	    Collection<EntityManager> ems) throws IOException {
 
 	Collection<TransactionData> entityTransactions;
 	if (type.equals(TransactionAttributeType.NOT_SUPPORTED)) {
 
 	    addEntityManagers(transaction, ems);
 	} else if (type.equals(TransactionAttributeType.REQUIRED)) {
 
 	    Object caller = transaction.getCaller();
 	    if (caller == null) {
 		transaction.setCaller(handler);
 	    }
 
 	    entityTransactions = getEntityTransactions(ems);
 	    addEntityTransactions(transaction, entityTransactions);
 
 	} else if (type.equals(TransactionAttributeType.REQUIRES_NEW)) {
 
 	    entityTransactions = getEntityTransactions(ems);
 	    addReqNewTransactions(transaction, entityTransactions);
 
 	} else if (type.equals(TransactionAttributeType.MANDATORY)) {
 
 	    int status = getStatus(transaction);
 	    if (status == 0) {
 		throw new EJBException(MANDATORY_ERROR);
 	    } else {
 		entityTransactions = getEntityTransactions(ems);
 		addEntityTransactions(transaction, entityTransactions);
 	    }
 	} else if (type.equals(TransactionAttributeType.NEVER)) {
 
 	    int status = getStatus(transaction);
 	    if (status > 0) {
 		throw new EJBException(NEVER_ERROR);
 	    } else {
 		addEntityManagers(transaction, ems);
 	    }
 
 	} else if (type.equals(TransactionAttributeType.SUPPORTS)) {
 
 	    throw new NotYetImplementedException(SUPPORTS_ERROR);
 	}
     }
 
     /**
      * Defines which {@link TransactionAttribute} is used on bean {@link Class}
      * and decides whether create or join {@link UserTransaction} by this
      * annotation
      * 
      * @param handler
      * @param method
      * @param entityTransaction
      * @throws IOException
      */
     public static TransactionAttributeType addTransaction(BeanHandler handler,
 	    Method method, Collection<EntityManager> ems) throws IOException {
 
 	MetaData metaData = handler.getMetaData();
 	TransactionAttributeType type = getTransactionType(metaData, method);
 	UserTransactionImpl transaction = (UserTransactionImpl) getTransaction();
 	if (ObjectUtils.notNull(type)) {
 	    addTransaction(handler, type, transaction, ems);
 	}
 
 	return type;
     }
 
     /**
      * Commits passed {@link UserTransaction} with {@link IOException} throw
      * 
      * @param transaction
      * @throws IOException
      */
     private static void commit(UserTransaction transaction) throws IOException {
 
 	try {
 	    transaction.commit();
 	} catch (SecurityException ex) {
 	    throw new IOException(ex);
 	} catch (IllegalStateException ex) {
 	    throw new IOException(ex);
 	} catch (RollbackException ex) {
 	    throw new IOException(ex);
 	} catch (HeuristicMixedException ex) {
 	    throw new IOException(ex);
 	} catch (HeuristicRollbackException ex) {
 	    throw new IOException(ex);
 	} catch (SystemException ex) {
 	    throw new IOException(ex);
 	}
     }
 
     /**
     * Commits all {@link TransactionAttributeType.REQUIRES_NEW} transactions
     * for passed {@link UserTransactionImpl} with {@link IOException} throw
     * 
     * @param transaction
     * @throws IOException
     */
    private static void commitReqNew(UserTransactionImpl transaction)
	    throws IOException {

	try {
	    transaction.commitReqNew();
	} catch (SecurityException ex) {
	    throw new IOException(ex);
	} catch (IllegalStateException ex) {
	    throw new IOException(ex);
	} catch (RollbackException ex) {
	    throw new IOException(ex);
	} catch (HeuristicMixedException ex) {
	    throw new IOException(ex);
	} catch (HeuristicRollbackException ex) {
	    throw new IOException(ex);
	} catch (SystemException ex) {
	    throw new IOException(ex);
	}
    }

    /**
      * Calls {@link UserTransaction#rollback()} method of passed
      * {@link UserTransaction} with {@link IOException} throw
      * 
      * @param transaction
      * @throws IOException
      */
     private static void rollback(UserTransaction transaction)
 	    throws IOException {
 	try {
 	    transaction.rollback();
 	} catch (IllegalStateException ex) {
 	    throw new IOException(ex);
 	} catch (SecurityException ex) {
 	    throw new IOException(ex);
 	} catch (SystemException ex) {
 	    throw new IOException(ex);
 	}
     }
 
     /**
      * Decides whether rollback or not {@link UserTransaction} by
      * {@link TransactionAttribute} annotation
      * 
      * @param type
      * @param handler
      * @throws IOException
      */
     private static void rollbackTransaction(TransactionAttributeType type,
 	    BeanHandler handler) throws IOException {
 
 	if (type.equals(TransactionAttributeType.REQUIRED)
 		|| type.equals(TransactionAttributeType.MANDATORY)) {
 	    UserTransactionImpl transaction = (UserTransactionImpl) getTransaction();
 	    rollback(transaction);
 	}
     }
 
     /**
      * Decides whether rollback or not {@link UserTransaction} by
      * {@link TransactionAttribute} annotation
      * 
      * @param handler
      * @param method
      * @throws IOException
      */
     public static void rollbackTransaction(BeanHandler handler, Method method)
 	    throws IOException {
 
 	TransactionAttributeType type = getTransactionType(
 		handler.getMetaData(), method);
 	if (ObjectUtils.notNull(type)) {
 	    rollbackTransaction(type, handler);
 	}
     }
 
     /**
      * Decides whether commit or not {@link UserTransaction} by
      * {@link TransactionAttribute} annotation
      * 
      * @param type
      * @param handler
      * @throws IOException
      */
     public static void commitTransaction(TransactionAttributeType type,
 	    BeanHandler handler) throws IOException {
 
 	UserTransactionImpl transaction = (UserTransactionImpl) getTransaction();
 
 	if (type.equals(TransactionAttributeType.REQUIRED)) {
 
 	    boolean check = transaction.checkCaller(handler);
 	    if (check) {
 		commit(transaction);
 	    }
 	} else if (type.equals(TransactionAttributeType.REQUIRES_NEW)) {
 
	    commitReqNew(transaction);
 	} else {
 
 	    transaction.closeEntityManagers();
 	}
     }
 
     /**
      * Decides whether commit or not {@link UserTransaction} by
      * {@link TransactionAttribute} annotation
      * 
      * @param handler
      * @param method
      * @throws IOException
      */
     public static void commitTransaction(BeanHandler handler, Method method)
 	    throws IOException {
 
 	TransactionAttributeType type = getTransactionType(
 		handler.getMetaData(), method);
 	if (ObjectUtils.notNull(type)) {
 	    commitTransaction(type, handler);
 	}
     }
 
     /**
      * Closes cached {@link EntityManager}s after method calll
      */
     public static void closeEntityManagers() {
 
 	UserTransactionImpl transaction = (UserTransactionImpl) getTransaction();
 	transaction.closeEntityManagers();
     }
 }
