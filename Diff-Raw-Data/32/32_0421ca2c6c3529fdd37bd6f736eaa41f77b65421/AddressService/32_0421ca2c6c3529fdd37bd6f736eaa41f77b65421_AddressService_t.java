 package org.tamarillo.addressws.service.impl;
 
 import java.util.List;
 
 import javax.ejb.LocalBean;
 import javax.ejb.Stateful;
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 import javax.enterprise.inject.Model;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 
 import org.tamarillo.addressws.entity.Arteria;
 import org.tamarillo.addressws.entity.CodigoPostal;
 import org.tamarillo.addressws.entity.Concelho;
 import org.tamarillo.addressws.entity.ConcelhoPK;
 import org.tamarillo.addressws.entity.Distrito;
 import org.tamarillo.addressws.entity.Localidade;
 import org.tamarillo.addressws.entity.LocalidadePK;
 import org.tamarillo.addressws.jpa.exception.PreexistingEntityException;
 import org.tamarillo.addressws.service.IAddressService;
 
 /**
  * The Class AddressService.
  */
 @Stateful(name = "addressService")
@LocalBean
 public class AddressService implements IAddressService {
 
 	/** The em. */
 	@Inject
 	private EntityManager em;
 
 	/**
 	 * Creates the distrito.
 	 * 
 	 * @param d
 	 *            the d
 	 * @throws PreexistingEntityException
 	 *             , Exception
 	 */
 	@TransactionAttribute(TransactionAttributeType.REQUIRED)
 	public void createDistrito(Distrito d) throws PreexistingEntityException,
 			Exception {
 		try {
 			em.persist(d);
 		} catch (Exception ex) {
 			if (lookupDistritoById(d.getId()) != null) {
 				throw new PreexistingEntityException("Distrito " + d.toString()
 						+ " already exists.", ex);
 			}
 			throw ex;
		} 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.tamarillo.addressws.service.IAddressService#listAllDistrito()
 	 */
 	@Override
 	public List<Distrito> listAllDistrito() {
 		return em.createNamedQuery("Distrito.findAll", Distrito.class)
 				.getResultList();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.tamarillo.addressws.service.IAddressService#lookupDistritoById(java
 	 * .lang.String)
 	 */
 	@Override
 	public Distrito lookupDistritoById(String id) {
 		return (Distrito) em.find(Distrito.class, id);
 	}
 
 	/**
 	 * Creates the concelho.
 	 * 
 	 * @param c
 	 *            the c
 	 */
 	@TransactionAttribute(TransactionAttributeType.REQUIRED)
 	public void createConcelho(Concelho c) throws PreexistingEntityException,
 			Exception {
 		try {
 			em.persist(c);
 		} catch (Exception ex) {
 			if (c.getId() != null && lookupConcelhoById(c.getId().getIdDistrito(), c.getId()
 					.getIdConcelho()) != null) {
 				throw new PreexistingEntityException("Concelho " + c.toString()
 						+ " already exists.", ex);
 			}
 			throw ex;
		} 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.tamarillo.addressws.service.IAddressService#listAllConcelho()
 	 */
 	@Override
 	public List<Concelho> listAllConcelho() {
 		return em.createNamedQuery("Concelho.findAll", Concelho.class)
 				.getResultList();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.tamarillo.addressws.service.IAddressService#lookupConcelhoById(java
 	 * .lang.String, java.lang.String)
 	 */
 	@Override
 	public Concelho lookupConcelhoById(String idDistrito, String idConcelho) {
 		ConcelhoPK id = new ConcelhoPK(idDistrito, idConcelho);
 		return (Concelho) em.find(Concelho.class, id);
 	}
 	
 	/**
 	 * Creates the localidade.
 	 * 
 	 * @param l
 	 *            the l
 	 */
 	@TransactionAttribute(TransactionAttributeType.REQUIRED)
 	public void createLocalidade(Localidade l)
 			throws PreexistingEntityException, Exception {
 		try {
 			em.persist(l);
 		} catch (Exception ex) {
 			if (l.getId() != null && lookupLocalidadeById(l.getId().getIdDistrito(), l.getId()
 					.getIdConcelho(),l.getId().getIdLocalidade()) != null) {
 				throw new PreexistingEntityException("Localidade " + l.toString()
 						+ " already exists.", ex);
 			}
 			throw ex;
		} 
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.tamarillo.addressws.service.IAddressService#listAllLocalidade()
 	 */
 	@Override
 	public List<Localidade> listAllLocalidade() {
 		return em.createNamedQuery("Localidade.findAll", Localidade.class)
 				.getResultList();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.tamarillo.addressws.service.IAddressService#lookupConcelhoById(java
 	 * .lang.String, java.lang.String)
 	 */
 	@Override
 	public Localidade lookupLocalidadeById(String idDistrito, String idConcelho, String idLocalidade) {
 		LocalidadePK id = new LocalidadePK(idDistrito, idConcelho, idLocalidade);
 		return (Localidade) em.find(Localidade.class, id);
 	}
 
 	/**
 	 * Creates the arteria.
 	 * 
 	 * @param a
 	 *            the a
 	 */
 	@TransactionAttribute(TransactionAttributeType.REQUIRED)
 	public void createArteria(Arteria a) {
 		em.persist(a);
 	}
 
 	/**
 	 * Creates the codigo postal.
 	 * 
 	 * @param cp
 	 *            the cp
 	 */
 	@TransactionAttribute(TransactionAttributeType.REQUIRED)
 	public void createCodigoPostal(CodigoPostal cp) {
 		em.persist(cp);
 	}
 
 }
