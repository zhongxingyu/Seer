 package br.com.unifacs.dao;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import br.com.unifacs.model.Categoria;
 import br.com.unifacs.utils.JpaUtil;
 
 public class GenericDaoJpa<T, PK extends Serializable> implements GenericDao<T, PK> {
 	
 	private Class  tipoModel;
 	
 	public GenericDaoJpa(Class tipoModel){
 		this.tipoModel = tipoModel;
 	}
 	
 	public void inserir(T model)throws DaoException{
 		
 		try {
 			EntityManager entitymanager = JpaUtil.getEntityManager();
 			entitymanager.getTransaction().begin();
 			entitymanager.persist(model);
 			entitymanager.getTransaction().commit();
 			entitymanager.close();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new DaoException(e);
 		}
 		
 	}
 
 	public void atualizar(T model) throws DaoException{
 		try {
 			EntityManager entitymanager = JpaUtil.getEntityManager();
 			entitymanager.getTransaction().begin();
 			entitymanager.merge(model);
 			entitymanager.getTransaction().commit();
 			entitymanager.close();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new DaoException(e);
 		}
 		
 	}
 
 	public void excluir(T model) throws DaoException{
 		try {
 			EntityManager entitymanager = JpaUtil.getEntityManager();
 			entitymanager.getTransaction().begin();
 			T entityToBeRemoved = entitymanager.merge(model);
 			entitymanager.remove(entityToBeRemoved);	
 			entitymanager.getTransaction().commit();
 			entitymanager.close();			
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new DaoException(e);
 		}
 		
 	}
 	public T obter(PK id) {
 		EntityManager entitymanager = JpaUtil.getEntityManager();
 		return (T) entitymanager.find(tipoModel, id);
 	}
 
 	public List<T> obterTodos() {
 			EntityManager entitymanager = JpaUtil.getEntityManager();
 			entitymanager.getTransaction().begin();
 			final String jpql = "select this from " + getTipoModel().getSimpleName() + " this";
 			Query query = entitymanager.createQuery(jpql); 	
 			entitymanager.close();
 			return query.getResultList();
 	}
 
 	public Class getTipoModel() {
 		return tipoModel;
 	}
 
	public List<T> query(String q, String... param) throws DaoException {
 		try {
 			EntityManager entitymanager = JpaUtil.getEntityManager();
 			Query query = entitymanager.createQuery(q);
 			for(int i=0; i < param.length; i++)
 				query.setParameter((i + 1), param[i]);
 			entitymanager.close();
			return query.getResultList();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new DaoException(e);
 		}
 	}
 }
