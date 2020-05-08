 package com.digt.jpa.spi;
 
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.Future;
 import java.util.logging.Logger;
 
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMessage.RecipientType;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.Query;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.codec.binary.Hex;
 import org.apache.shindig.auth.SecurityToken;
 import org.apache.shindig.common.util.ImmediateFuture;
 import org.apache.shindig.protocol.ProtocolException;
 import org.apache.shindig.protocol.RestfulCollection;
 import org.apache.shindig.social.opensocial.jpa.api.DbObject;
 import org.apache.shindig.social.opensocial.jpa.spi.JPQLUtils;
 import org.apache.shindig.social.opensocial.jpa.spi.SPIUtils;
 import org.apache.shindig.social.opensocial.spi.CollectionOptions;
 import org.apache.shindig.social.opensocial.spi.GroupId;
 import org.apache.shindig.social.opensocial.spi.UserId;
 import org.bouncycastle.asn1.ASN1OctetString;
 import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
 import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
 
 import com.digt.common.utils.LogUtils;
 import com.digt.common.utils.X509CertificateGenerator;
 import com.digt.crypto.PKCSCtlData;
 import com.digt.jpa.CertPubRequestDb;
 import com.digt.jpa.CertificateContainerDb;
 import com.digt.jpa.CertificateRequestDb;
 import com.digt.jpa.CertificateTrustedListDb;
 import com.digt.jpa.PersonCertificateDb;
 import com.digt.model.CertificateContainer;
 import com.digt.model.CertificateRequest;
 import com.digt.model.CertificateTrustedList;
 import com.digt.model.PersonCertificate;
 import com.digt.model.PersonCertificate.Type;
 import com.digt.model.PubRequest;
 import com.digt.web.WebUtil;
 import com.google.caja.util.Lists;
 import com.google.common.collect.ImmutableSet;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.google.inject.name.Named;
 
 public class CertificateServiceDb extends EntityServiceDb implements CertificateService {
 
 	/** The entity manager. */
 
 	@Inject
 	private Provider<Session> mailSession;
 
 	private static final Logger LOG = Logger.getLogger(
 			CertificateServiceDb.class.getName());
 	
 	private String BASE_URL;
 
 	@Inject
 	public CertificateServiceDb(@Named("com.digt.web.baseurl") String serverName)
 	{
 		BASE_URL = serverName;
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public Future<RestfulCollection<PersonCertificate>> getCertificates(UserId userId, Set<Long> certIds, SecurityToken token)
 	{
 		List<PersonCertificate> cList;
 		EntityManager em = getEntityManager();
 		try {
 			Query q = em.createQuery(PersonCertificateDb.FIND + " and c.objectId in ?2");
 			q.setParameter(1, Lists.newArrayList(userId.getUserId(token)));
 			q.setParameter(2, certIds);
 			
 			cList = q.getResultList();
 		} catch (Exception e) {
 			LOG.severe(LogUtils.getTrace(e));
 			throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
 		} finally {
 			em.close();
 		}
 		return ImmediateFuture.newInstance(new RestfulCollection<PersonCertificate>(cList));
 	}
 
 	@Override
 	public Future<RestfulCollection<PersonCertificate>> getCertificates(
 			Set<UserId> uids, GroupId groupId, CollectionOptions options, SecurityToken token) 
 	{
 		List<PersonCertificate> plist = null;
 
 		StringBuilder sb = new StringBuilder();
 		// sanitize the list to get the uid's and remove duplicates
 		List<Object> params = new ArrayList<Object>();
 		params.add(SPIUtils.getUserList(uids, token));
 		
 		// select the group Id as this will drive the query
 		switch (groupId.getType()) {
 		case all:
 			// Select self shared for all
 			sb.append(PersonCertificateDb.FIND_ALL);
 			break;
 		case friends:
 			// select all friends (subset of contacts)
 			sb.append(PersonCertificateDb.FIND_BY_FRIENDS);
 			// TODO Group by doesn't work in HSQLDB or Derby - causes a "Not in aggregate function or group by clause" jdbc exception
 			// sb.append(" group by p ");
 			break;
 		case groupId:
 			// select those in the group
 			// TODO Needs implementing and then have a unit test created to test it.
 			//sb.append("");
 			params.add(groupId.getGroupId());
 			sb.append(PersonCertificateDb.FIND_BY_GROUP);
 			break;
 		case deleted:
 			// ???
 			break;
 		case self:
 			// select self
 			sb.append(PersonCertificateDb.FIND);
 			break;
 		default:
 			throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, "Group ID not recognized");
 
 		}
 		
 		JPQLUtils.addFilterClause(sb, PersonCertificateDb.FILTER_CAPABILITY, options, params);
 			
 		EntityManager entityManager = getEntityManager();
 
 		// Get total results, that is count the total number of rows for this query
 		Long totalResults = JPQLUtils.getTotalResults(entityManager, sb.toString(), params);
 
 		// Execute paginated query
 		if (totalResults > 0) {
 			plist = JPQLUtils.getListQuery(entityManager, sb.toString(), params, options);
 		}
 		
 		entityManager.close();
 		
 		if (plist == null) {
 			plist = new ArrayList<PersonCertificate>();
 		}
 
 		// all of the above could equally have been placed into a thread to overlay the
 		// db wait times.
 		RestfulCollection<PersonCertificate> restCollection;
 		if (options != null)
 		{
 			restCollection = new RestfulCollection<PersonCertificate>(
 					plist, options.getFirst(), totalResults.intValue(), options.getMax());
 		} else {
 			restCollection = new RestfulCollection<PersonCertificate>(plist);
 		}
 		return ImmediateFuture.newInstance(restCollection);
 	}
 
 	@Override
 	public Future<Long> addCertificate(UserId userId, byte[] certData, String label, Type type, SecurityToken token) {
 		try {
 			return addCertificate(userId, X509CertificateGenerator.parseCert(certData), null, type, label, token);
 		} catch (Exception e) {
 			LOG.severe(LogUtils.getTrace(e));
 			throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
 					"Certificate adding error." + e.getMessage());
 		}
 	}
 
 	@Override
 	public Future<Long> addCertificate(UserId userId, X509Certificate cert, Long containerId, Type type, String label, SecurityToken token) {
 
 		EntityManager entityManager = getEntityManager();
 
 		try
 		{
 			PersonCertificateDb certDb = new PersonCertificateDb();
 			certDb.setDn(cert.getSubjectDN().toString());
 			certDb.setIssuer(cert.getIssuerDN().toString());
 			certDb.setValidFrom(cert.getNotBefore());
 			certDb.setValidTo(cert.getNotAfter());
 			certDb.setSerialNumber(cert.getSerialNumber().toString());
 			certDb.setCertificate(cert.getEncoded());
 			certDb.setType(type);
 			// get subjectKeyIdendifier
 			ASN1OctetString oct = ASN1OctetString.getInstance(cert.getExtensionValue("2.5.29.14"));
 			SubjectKeyIdentifier ski = SubjectKeyIdentifier.getInstance(oct.getOctets());
 			certDb.setSubjectKeyId(Hex.encodeHexString(ski.getKeyIdentifier())); 
 			try {
 				// get authorityKeyIdendifier
 				ASN1OctetString oct2 = ASN1OctetString.getInstance(cert.getExtensionValue("2.5.29.35"));
 				AuthorityKeyIdentifier aki = AuthorityKeyIdentifier.getInstance(oct2.getOctets());
 				certDb.setAuthorityKeyId(Hex.encodeHexString(aki.getKeyIdentifier()));
 			} catch (Exception e) {
 				LOG.fine(LogUtils.getTrace(e));
 			}
 			certDb.setKeyUsage(cert.getKeyUsage());
 			certDb.setExtKeyUsage(cert.getExtendedKeyUsage());
 			certDb.setLabel(label);
 			certDb.setUserId(userId.getUserId(token));
 
 			if (!entityManager.getTransaction().isActive()) {
 				entityManager.getTransaction().begin();
 			}
 			entityManager.persist(certDb);
 			entityManager.getTransaction().commit();
 
 			bindContainer(userId, certDb.getObjectId(), containerId, token);
 			return ImmediateFuture.newInstance(certDb.getObjectId());
 		} catch (Exception e) {
 			LOG.severe(LogUtils.getTrace(e));
 			throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
 					"Certificate creating error." + e.getMessage());
 		} finally {
 			entityManager.close();
 		}
 	}
 
 	@Override
 	public Future<Void> deleteCertificate(UserId userId, List<String> certIds, SecurityToken token) 
 	{
 		String uid = userId.getUserId(token);
 		EntityManager entityManager = getEntityManager();
 		try {
 			if (!entityManager.getTransaction().isActive()) {
 				entityManager.getTransaction().begin();
 			}
 			Query q = entityManager.createNamedQuery(PersonCertificateDb.DEL);
 			q.setParameter(1, uid);
 			q.setParameter(2, certIds);
 			q.executeUpdate();
 	
 			entityManager.getTransaction().commit();
 		} catch (Exception e) {
 			LOG.severe(LogUtils.getTrace(e));
 			throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
 		} finally {
 			entityManager.close();
 		}
 		return null;
 	}
 
 	@Override
 	public Future<PersonCertificate> getDefaultCertificate(UserId userId, SecurityToken token) {
 
 		PersonCertificate cert = null;
 		try {
 			RestfulCollection<PersonCertificate> res = getCertificates(ImmutableSet.of(userId), SELF_GROUP, null, token).get();
 			cert = res.getEntry().get(0);
 		} catch (Exception e) {
 			LOG.severe(LogUtils.getTrace(e));
 			throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
 		}
 		return ImmediateFuture.newInstance(cert);
 	}
 
 
 	@Override
 	public Future<Long> addCertificateTrustedList(UserId userId, byte[] data, SecurityToken token)
 	{
 		
 		EntityManager em = getEntityManager();
 		try {
 			PKCSCtlData ctlData = X509CertificateGenerator.parseCtl(data);
 			CertificateTrustedListDb ctl = new CertificateTrustedListDb();
 			ctl.setData(data);
			ctl.setId(ctlData.getId());
 			if (ctlData.getSigners() != null && !ctlData.getSigners().isEmpty())
 					ctl.setSignerDn(ctlData.getSigners().get(0).getSubjectDN().getName());
 			
 			ctl.setValidFrom(ctlData.getValidFrom());
 			ctl.setValidTo(ctlData.getValidTo());
 			ctl.setUserId(userId.getUserId(token));
 			if (!em.getTransaction().isActive()) {
 				em.getTransaction().begin();
 			}
 			em.persist(ctl);
 			em.getTransaction().commit();
 			return ImmediateFuture.newInstance(((DbObject)ctl).getObjectId());
 		} catch (Exception e) {
 			LOG.severe(LogUtils.getTrace(e));
 			throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
 					"CTL adding error." + e.getMessage());
 		} finally {
 			em.close();
 		}
 	}
 	
 	@Override
 	public Future<PubRequest> getCertPublishRequest(String secret)
 	{
 		PubRequest res = null;
 		EntityManager entityManager = getEntityManager();
 		Query q = entityManager.createNamedQuery(CertPubRequestDb.FINDBY_SECRET);
 		q.setParameter(1, secret);
 		try
 		{
 			res = (PubRequest) q.getSingleResult();
 		} catch (NoResultException e) {}
 		finally
 		{
 			entityManager.close();
 		}
 		return ImmediateFuture.newInstance(res);
 	}
 
 	@Override
 	public Future<Void> publishCertificate(PersonCertificate certDb, String emailAddress)
 	{
 		EntityManager em = getEntityManager();
 		try
 		{
 			CertPubRequestDb certReqDb = new CertPubRequestDb();
 			certReqDb.setCertificate(certDb);
 			certReqDb.setUserId(certDb.getUserId());
 			
 			if (!em.getTransaction().isActive()) {
 				em.getTransaction().begin();
 			}
 			
 			em.persist(certReqDb);
 			em.getTransaction().commit();
 
 			MimeMessage msg = new MimeMessage(mailSession.get());
 			msg.setRecipient(RecipientType.TO, new InternetAddress(emailAddress));
 			msg.setSubject("[TrustedNET] публикация сертификата", WebUtil.MAIL_CHARSET);
 			msg.setContent("Пожалуйста, подтвердите публикацию сертификата, перейдя по ссылке " 
 					+ BASE_URL + "/web/cert/publish/confirm?id=" + certReqDb.getSecret(), 
 					"text/plain;charset=" + WebUtil.MAIL_CHARSET);
 			Transport.send(msg);
 
 
 		} catch (AddressException e) {
 			throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, 
 					"Certificate contains invalid email address " + emailAddress);
 		} catch (MessagingException e) {
 			LOG.severe(LogUtils.getTrace(e));
 			throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
 					"Error sending email to " + emailAddress);
 		} finally {
 			if (em.getTransaction().isActive()) {
 				em.getTransaction().rollback();
 			}
 			em.close();
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public Future<RestfulCollection<CertificateContainer>> getContainers(
 			Set<UserId> uids, CollectionOptions options, SecurityToken token) {
 		List<CertificateContainer> plist = null;
 
 		StringBuilder sb = new StringBuilder();
 		// sanitize the list to get the uid's and remove duplicates
 		List<Object> params = new ArrayList<Object>();
 		params.add(SPIUtils.getUserList(uids, token));
 		sb.append(CertificateContainerDb.FIND);
 		
 		// Apply filters
 		JPQLUtils.addFilterClause(sb, CertificateContainerDb.FILTER_CAPABILITY, options, params);
 		
 		EntityManager entityManager = getEntityManager();
 
 		// Get total results, that is count the total number of rows for this query
 		Long totalResults = JPQLUtils.getTotalResults(entityManager, sb.toString(), params);
 
 		// Execute paginated query
 		if (totalResults > 0) {
 			plist = JPQLUtils.getListQuery(entityManager, sb.toString(), params, options);
 		}
 		entityManager.close();
 		if (plist == null) {
 			plist = new ArrayList<CertificateContainer>();
 		}
 
 		// all of the above could equally have been placed into a thread to overlay the
 		// db wait times.
 		RestfulCollection<CertificateContainer> restCollection;
 		if (options != null)
 		{
 			restCollection = new RestfulCollection<CertificateContainer>(
 					plist, options.getFirst(), totalResults.intValue(), options.getMax());
 		} else {
 			restCollection = new RestfulCollection<CertificateContainer>(plist);
 		}
 		return ImmediateFuture.newInstance(restCollection);
 	}
 
 	@Override
 	public Future<Long> addContainer(UserId userId, byte[] data, String pin, SecurityToken token)
 	{
 		EntityManager entityManager = getEntityManager();
 		try {
 			X509CertificateGenerator.parseContainer(data, pin);
 			CertificateContainerDb contDb = new CertificateContainerDb();
 			contDb.setUserId(userId.getUserId(token));
 			contDb.setData(data);
 			
 			if (!entityManager.getTransaction().isActive()) {
 				entityManager.getTransaction().begin();
 			}
 	
 			entityManager.persist(contDb);
 			entityManager.getTransaction().commit();
 			return ImmediateFuture.newInstance(contDb.getObjectId());
 		} catch (Exception e) {
 			LOG.severe(LogUtils.getTrace(e));
 			throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
 		} finally {
 			entityManager.close();
 		}
 
 	}
 
 	@Override
 	public Future<Void> deleteContainer(UserId userId, List<String> contIds,
 			SecurityToken token) {
 
 		EntityManager entityManager = getEntityManager();
 		try {
 			if (!entityManager.getTransaction().isActive()) {
 				entityManager.getTransaction().begin();
 			}
 			// delete container
 			Query q = entityManager.createNamedQuery(CertificateContainerDb.DEL);
 			q.setParameter(1, userId.getUserId(token));
 			q.setParameter(2, contIds);
 			q.executeUpdate();
 	
 			entityManager.getTransaction().commit();
 		} catch (Exception e) {
 			LOG.severe(LogUtils.getTrace(e));
 			throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
 		} finally {
 			entityManager.close();
 		}
 
 		return null;
 	}
 	
 	@Override
 	public Future<Void> bindContainer(UserId userId, Long certId, Long contId, SecurityToken token) {
 		EntityManager entityManager = getEntityManager();
 		try {
 			Query qCert = entityManager.createQuery(PersonCertificateDb.FIND + " and c.objectId = ?2");
 			List<String> uid = Arrays.asList(userId.getUserId(token));
 			qCert.setParameter(1, uid);
 			qCert.setParameter(2, certId);
 	
 			Query qCont = entityManager.createQuery(CertificateContainerDb.FIND + " and c.objectId = ?2");
 			qCont.setParameter(1, uid);
 			qCont.setParameter(2, contId);
 	
 			PersonCertificate cert = (PersonCertificate)qCert.getSingleResult();
 			CertificateContainer container = (CertificateContainer)qCont.getSingleResult();
 			
 			if (!entityManager.getTransaction().isActive()) {
 				entityManager.getTransaction().begin();
 			}
 	
 			cert.setContainer(container);
 			entityManager.getTransaction().commit();
 		} catch (NoResultException e) {
 		} catch (Exception e) {
 			LOG.severe(LogUtils.getTrace(e));
 			throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
 		} finally {
 			entityManager.close();
 		}
 		return null;
 	}
 
 	@Override
 	public Future<RestfulCollection<CertificateRequest>> getCertRequests(
 			Set<UserId> uids, CollectionOptions options, SecurityToken token) {
 		EntityManager em = getEntityManager();
 		RestfulCollection<CertificateRequest> res = 
 				new RestfulCollection<CertificateRequest>(new ArrayList<CertificateRequest>());
 		try {
 				res = JPQLUtils.getList(em, uids, WebUtil.GROUP_SELF, options, res, 
 						CertificateRequestDb.FIND, CertificateRequestDb.FILTER_CAPABILITY, token);
 		} catch (Exception e) {
 			LOG.severe(LogUtils.getTrace(e));
 			throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
 		} finally {
 			em.close();
 		}
 		
 		return ImmediateFuture.newInstance(res);
 	}
 
 	@Override
 	public Future<Long> addCertRequest(UserId userId, CertificateRequest req, SecurityToken token)
 	{
 		req.setUserId(userId.getUserId(token));
 		EntityManager entityManager = getEntityManager();
 		try {
 			if (!entityManager.getTransaction().isActive()) {
 				entityManager.getTransaction().begin();
 			}
 	
 			entityManager.persist(req);
 			entityManager.getTransaction().commit();
 		} catch (Exception e) {
 			LOG.severe(LogUtils.getTrace(e));
 			throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
 		} finally {
 			entityManager.close();
 		}
 		return ImmediateFuture.newInstance(((DbObject)req).getObjectId());
 	}
 
 	@Override
 	public Future<Void> deleteCertRequest(UserId userId, List<String> reqIds,
 			SecurityToken token) {
 		EntityManager entityManager = getEntityManager();
 		try {
 			Query q = entityManager.createNamedQuery(CertificateRequestDb.DEL);
 			q.setParameter(1, userId.getUserId(token));
 			q.setParameter(2, reqIds);
 	
 			if (!entityManager.getTransaction().isActive()) {
 				entityManager.getTransaction().begin();
 			}
 			q.executeUpdate();
 			entityManager.getTransaction().commit();
 		} catch (Exception e) {
 			LOG.severe(LogUtils.getTrace(e));
 			throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
 		} finally {
 			entityManager.close();
 		}
 		return null;
 	}
 
 	@Override
 	public Future<RestfulCollection<CertificateTrustedList>> getCertificateTrustedLists(Set<UserId> uids, GroupId groupId, 
 			 CollectionOptions options, SecurityToken token) {
 		List<CertificateTrustedList> plist = null;
 
 		StringBuilder sb = new StringBuilder();
 		// sanitize the list to get the uid's and remove duplicates
 		List<Object> params = new ArrayList<Object>();
 		params.add(SPIUtils.getUserList(uids, token));
 		// select the group Id as this will drive the query
 		switch (groupId.getType()) {
 		case all:
 			// Select self shared for all
 			sb.append(CertificateTrustedListDb.FIND);
 			//sb.append(" and c.sharedFor = -1");
 			break;
 		case friends:
 			// select all friends (subset of contacts)
 			//sb.append(CertificateTrustedListDb.FIND_BY_FRIENDS);
 			// TODO Group by doesn't work in HSQLDB or Derby - causes a "Not in aggregate function or group by clause" jdbc exception
 			// sb.append(" group by p ");
 			break;
 		case groupId:
 			// select those in the group
 			// TODO Needs implementing and then have a unit test created to test it.
 			//sb.append("");
 			//params.add(groupId.getGroupId());
 			//sb.append(CertificateTrustedListDb.FIND_BY_GROUP);
 			break;
 		case deleted:
 			// ???
 			break;
 		case self:
 			// select self
 			sb.append(CertificateTrustedListDb.FIND);
 			break;
 		default:
 			throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, "Group ID not recognized");
 
 		}
 		// Apply filters
 		JPQLUtils.addFilterClause(sb, CertificateTrustedListDb.FILTER_CAPABILITY, options, params);
 
 		EntityManager entityManager = getEntityManager();
 
 		// Get total results, that is count the total number of rows for this query
 		Long totalResults = JPQLUtils.getTotalResults(entityManager, sb.toString(), params);
 
 		// Execute paginated query
 		if (totalResults > 0) {
 			plist = JPQLUtils.getListQuery(entityManager, sb.toString(), params, options);
 		}
 		entityManager.close();
 
 		if (plist == null) {
 			plist = new ArrayList<CertificateTrustedList>();
 		}
 
 		// all of the above could equally have been placed into a thread to overlay the
 		// db wait times.
 		RestfulCollection<CertificateTrustedList> restCollection;
 		if (options != null)
 		{
 			restCollection = new RestfulCollection<CertificateTrustedList>(
 					plist, options.getFirst(), totalResults.intValue(), options.getMax());
 		} else {
 			restCollection = new RestfulCollection<CertificateTrustedList>(plist);
 		}
 		return ImmediateFuture.newInstance(restCollection);
 	}
 
 	@Override
 	public Future<Void> deleteCertificateTrustedList(UserId userId, List<String> ctlIds,
 			SecurityToken token) {
 		EntityManager entityManager = getEntityManager();
 
 		Query q = entityManager.createNamedQuery(CertificateTrustedListDb.DEL);
 		q.setParameter(1, userId.getUserId(token));
 		q.setParameter(2, ctlIds);
 
 		if (!entityManager.getTransaction().isActive()) {
 			entityManager.getTransaction().begin();
 		}
 		q.executeUpdate();
 		entityManager.getTransaction().commit();
 		entityManager.close();
 
 		return null;
 	}
 
 }
