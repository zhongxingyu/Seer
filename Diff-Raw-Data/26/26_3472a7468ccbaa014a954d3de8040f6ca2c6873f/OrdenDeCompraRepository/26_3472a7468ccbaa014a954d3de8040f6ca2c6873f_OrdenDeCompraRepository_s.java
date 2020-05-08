 /*Esta clase contiene las relaciones a la base de datos que maneja el UserAdminController.java*/
 package web.ecommerce.tpfinal.ecommerce_web.repository;
 
 //imports
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 
 import org.hibernate.Hibernate;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 import web.ecommerce.tpfinal.ecommerce_web.clasesDominio.Compra;
 
 /*Clase Repository*/
 @Repository
 @Transactional(readOnly = true)
 public class OrdenDeCompraRepository {
 	
 
 	/*Crea el entityManager que nos permitira hacer las consultas a la base de datos*/
 	@PersistenceContext
 	private EntityManager entityManager;
 	
 	
 	/***************************************************************************************************************
 	 * Esta consulta carga un objeto Compra a la tabla Compra (Controller - crearChanguito)
 	 **************************************************************************************************************/
 	//Carga los changuitos(objeto compra) a la base de datos a la tabla compra.
	public void cargarChanguitoDB(Compra compra){
 		entityManager.persist(compra);
		System.out.println("compra account: " + compra.getAccount().getEmail());
 	}
 
 	
 	/***************************************************************************************************************
 	 * Consultar para: -admin.jsp -adminconfirmado.jsp
 	 **************************************************************************************************************/
 	
 	/*Extrae los datos de la base de datos de la tabla Compra en una lista. -admin.jsp*/
 	public List<Compra> extraerCompras(){
 		TypedQuery<Compra> q = entityManager.createQuery("select c from Compra c "
 				+ "where c.estadoCompra = 'aconfirmar'", Compra.class);
 		List<Compra> compras = q.getResultList();
 		for(Compra compra : compras){
 			Hibernate.initialize(compra.getProductos());
 		}
 		return compras;
 	}
 
 
 	/*Extrae los datos de la base de datos de la tabla compra en una lista. -adminconfirmado.jsp*/
 	public List<Compra> extraerComprasConfirmadas(){
 		TypedQuery<Compra> q = entityManager.createQuery("select c from Compra c "
 				+ "where c.estadoCompra != 'aconfirmar'", Compra.class);
 		List<Compra> compras = q.getResultList();
 		for(Compra compra : compras){
 			Hibernate.initialize(compra.getProductos());
 		}
 		return compras;
 	}
 
 	/*Hace un update del estado de la compra echa por el administrador en el -admin.jsp*/
 	public void cargarCompras(long id,String estadoCompra){
 		Compra compra = entityManager.find(Compra.class, id);
 		compra.setEstadoCompra(estadoCompra);
 		entityManager.merge(compra);
 		entityManager.flush();
 	}
 
 	
 	
 	
 	/***************************************************************************************************************
 	 * Consultar para: -user.jsp 
 	 **************************************************************************************************************/
 	
 	/*Extrae los datos de la tabla Compras.*/
 	public List<Compra> extraerUsuarios(){
 		TypedQuery<Compra> q = entityManager.createQuery("select a from Compra a", Compra.class);
 		List<Compra> usuarios = q.getResultList();
 		return usuarios;
 	}
 	
 	/*extrae las compras realizadas por el usuario a traves del mail*/
 	public List<Compra> extraerComprasDeUsuario(String email){
 		TypedQuery<Compra> q = entityManager.createQuery("select c from Compra c"
 				+ " where c.usuario.email = :email",Compra.class).setParameter("email", email);
 		System.out.println(email);
 		List<Compra> compra = q.getResultList();
 		for(Compra compras : compra){
 			Hibernate.initialize(compras.getAccount());
 		}
 		for(Compra compras : compra){
 			Hibernate.initialize(compras.getProductos());
 		}
 		return compra;
 	}
 	
 	
 }
