 /**
  * Copyright (c) Members of the EGEE Collaboration. 2006-2009.
  * See http://www.eu-egee.org/partners/ for details on the copyright holders.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * Authors:
  * 	Andrea Ceccanti (INFN)
  */
 package org.glite.security.voms.admin.persistence.dao;
 
 import java.security.cert.X509Certificate;
 import java.util.Date;
 import java.util.List;
 
 import org.glite.security.voms.admin.error.VOMSException;
 import org.glite.security.voms.admin.persistence.HibernateFactory;
 import org.glite.security.voms.admin.persistence.error.NoSuchCAException;
 import org.glite.security.voms.admin.persistence.model.Certificate;
 import org.glite.security.voms.admin.persistence.model.VOMSCA;
 import org.glite.security.voms.admin.persistence.model.VOMSUser;
 import org.glite.security.voms.admin.util.DNUtil;
 
 public class CertificateDAO {
 
 	private CertificateDAO() {
 		HibernateFactory.beginTransaction();
 	}
 
 	public Certificate findByDN(String dn){
 		
 		assert dn != null : "Null DN passed as argument!";
 		
 		String query = "From Certificate where subjectString = :subjectString";
 		
 		List<Certificate> dbCerts = (List<Certificate>) HibernateFactory.getSession()
 		.createQuery(query).setString("subjectString", dn).list();
 		
 		if (dbCerts.size() > 1)
 			throw new VOMSException("Multiple certificates found for the following dn '"+dn+"'. Please specify the CA dn!");
 		
 		if (dbCerts.size() == 0)
 			return null;
 
 		return dbCerts.get(0);
 		
 	}
 	public Certificate findByDNCA(String dn, String ca) {
 
 		assert dn != null : "Null DN passed as argument!";
 		assert ca != null : "Null ca passed as argument!";
 
 		String query = "From Certificate where subjectString = :subjectString and ca.subjectString = :ca";
 
 		Certificate dbCert = (Certificate) HibernateFactory.getSession()
 				.createQuery(query).setString("subjectString", dn).setString(
 						"ca", ca).uniqueResult();
 
 		return dbCert;
 	}
 
 	public Certificate findById(long id) {
 
 		return (Certificate) HibernateFactory.getSession().get(
 				Certificate.class, new Long(id));
 	}
 
 	public Certificate find(X509Certificate cert) {
 
 		assert cert != null : "Null certificate passed as argument!";
 
		String subjectString = DNUtil.normalizeDN(
			DNUtil.getOpenSSLSubject(cert.getSubjectX500Principal()));
 
		String issuerString = DNUtil.normalizeDN(
			DNUtil.getOpenSSLSubject(cert.getIssuerX500Principal()));
		
		return findByDNCA(subjectString, issuerString);
 
 	}
 
 	public boolean isAlreadyAssigned(X509Certificate cert) {
 
 		Certificate dbCert = find(cert);
 		return (dbCert != null);
 	}
 
 	public Certificate create(String dn, String ca) {
 
 		String normalizedCA = DNUtil.normalizeDN(ca);
 
 		VOMSCA dbCA = VOMSCADAO.instance().getByName(normalizedCA);
 
 		if (dbCA == null)
 			throw new NoSuchCAException("CA '" + ca
 					+ "' not found in database!");
 		
 		Certificate cert = new Certificate();
 
 		cert.setSubjectString(DNUtil.normalizeDN(dn));
 		cert.setCa(dbCA);
 		cert.setCreationTime(new Date());
 
 		return cert;
 	}
 	
 	public Certificate create(VOMSUser u, String ca) {
 
 		Certificate cert = create(u.getDn(), ca);
 		cert.setUser(u);
 		return cert;
 	}
 
 	public List<Certificate> getForCA(VOMSCA ca) {
 		assert ca != null : "null ca passed as argument!";
 
 		String query = "from Certificate where ca = :ca";
 		return HibernateFactory.getSession().createQuery(query).setEntity("ca",
 				ca).list();
 	}
 
 	public List<Certificate> getAll() {
 
 		String query = "from Certificate order by subjectString";
 		return HibernateFactory.getSession().createQuery(query).list();
 
 	}
 
 	public static CertificateDAO instance() {
 		return new CertificateDAO();
 	}
 }
