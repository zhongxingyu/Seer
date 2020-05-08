 /* Copyright 2006 VPAC
  * 
  * This file is part of Grisu.
  * Grisu is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * any later version.
 
  * Girus is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with Grisu; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  */
 
 package grisu.backend.model;
 
 import grisu.backend.model.job.Job;
 import grisu.backend.utils.CertHelpers;
 import grith.jgrith.CredentialHelpers;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.UUID;
 
 import org.apache.log4j.Logger;
 import org.ietf.jgss.GSSCredential;
 import org.ietf.jgss.GSSException;
 
 /**
  * This extends the {@link Credential} abstract class. It uses a normal GSSCredential as credential. Most likely you
  * won't need a different implementation of the {@link Credential} abstract class.
  * 
  * @author Markus Binsteiner
  *
  */
 /**
  * @author Markus Binsteiner
  * 
  */
 public class ProxyCredential {
 
 	static Logger myLogger = Logger.getLogger(ProxyCredential.class.getName());
 
 	// the internal "non-raw" credential data
 	private GSSCredential gsscredential = null;
 
 	// all jobs that use this credential
 	private Set<Job> jobs = new HashSet<Job>();
 
 	// the "raw" credential data
 	private byte[] credentialData = null;
 
 	// for hibernate
 	private Long id = null;
 
 	// the dn of the credential - easier for db queries that way
 	private String dn = null;
 
 	// when will this credential expire - easier for db queries that way
 	private Date expiryDate = null;
 
 	// the (primary) fqan of this certificate (can be null)
 	private String fqan = null;
 
 	// whether this certificate is renewable - not used yet but maybe usefull
 	// for MyProxy certificates
 	private final Boolean renewable = false;
 
 	private final UUID uuid = UUID.randomUUID();
 
 	// For hibernate only
 	protected ProxyCredential() {
 	}
 
 	/**
 	 * This constructor creates a ProxyCredential using a standard GSSCredential
 	 * as parameter and wraps all the grisu-credential specific things around
 	 * it.
 	 * 
 	 * @param proxy
 	 *            the GSSCredential
 	 * @throws Exception
 	 *             if something goes wrong
 	 */
 	public ProxyCredential(final GSSCredential proxy) throws Exception {
 
 		setCredentialData(convertFromGSSCredential(proxy));
 	}
 
 	/**
 	 * This constructor wraps a voms enabled proxy into this ProxyCredential
 	 * class.
 	 * 
 	 * @param proxy
 	 *            a voms proxy
 	 * @param fqan
 	 *            the fqan for the above voms proxy
 	 * @throws Exception
 	 *             if something is wrong with the proxy
 	 */
 	public ProxyCredential(final GSSCredential proxy, final String fqan)
 			throws Exception {
 		setCredentialData(convertFromGSSCredential(proxy));
 		setFqan(fqan);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * grisu.credential.model.GenericCredential#addJob(grisu
 	 * .js.model.Job)
 	 */
 	public final void addJob(final Job job) {
 		job.setCredential(this);
 
 		if (!jobs.contains(job)) {
 			jobs.add(job);
 		}
 	}
 
 	/**
 	 * Just a helper method.
 	 * 
 	 * @param proxy
 	 *            the proxy in GSS format
 	 * @return a byte array holding the credential
 	 */
 	private byte[] convertFromGSSCredential(final GSSCredential proxy) {
 
 		byte[] data = null;
 		try {
 			data = CredentialHelpers.convertGSSCredentialToByteArray(proxy);
 		} catch (final GSSException e) {
 			myLogger.error(e);
 			return null;
 		}
 		return data;
 
 	}
 
 	/**
 	 * Just a helper method.
 	 * 
 	 * @param data
 	 *            a byte array hodlding the credential
 	 * @return the credential in GSS format
 	 */
 	private GSSCredential convertToGSSCredential(final byte[] data) {
 
 		GSSCredential cred = null;
 		try {
 			cred = CredentialHelpers.convertByteArrayToGSSCredential(data);
 		} catch (final GSSException e) {
 			// TODO Auto-generated catch block
 			myLogger.error(e);
 			return null;
 		}
 		return cred;
 	}
 
 	public final void destroy() {
 
 		try {
 			getGssCredential().dispose();
 		} catch (final GSSException e) {
 			// bad luck
 			assert true;
 		}
 
 	}
 
 	@Override
 	public boolean equals(Object o) {
 
 		if ( o instanceof ProxyCredential ) {
 			ProxyCredential pc = (ProxyCredential) o;
 
 			return pc.getGssCredential().equals(getGssCredential());
 
 		} else {
			return true;
 		}
 
 	}
 
 	/**
 	 * Returns the credential data as byte[]. The format is dependant on the
 	 * type of the credential. (But we are using X509 proxies only - so don't
 	 * worry about that.)
 	 * 
 	 * @return the "raw" credential data
 	 */
 	public final byte[] getCredentialData() {
 		return credentialData;
 	}
 
 	/**
 	 * The credential type specific way of getting the dn.
 	 * 
 	 * @return the dn of the credential
 	 */
 	public final String getDn() {
 		if (dn == null) {
 			dn = CertHelpers.getDnInProperFormat(getGssCredential());
 		}
 
 		return dn;
 	}
 
 	/**
 	 * The credential type specific way of getting the expiry date.
 	 * 
 	 * @return the expiry date of the credential
 	 */
 	public final Date getExpiryDate() {
 		if (expiryDate == null) {
 			long fromNow = 0;
 			try {
 				fromNow = getGssCredential().getRemainingLifetime();
 			} catch (final GSSException e) {
 				// return null in that case
 				myLogger.error(e);
 				return null;
 			}
 
 			expiryDate = new Date(new Date().getTime() + (fromNow * 1000));
 		}
 		return expiryDate;
 
 	}
 
 	/**
 	 * The credential type specific way of getting the FQAN (VOMS information)
 	 * out of the credential.
 	 * 
 	 * @return the FQAN of this credential or null if there is none
 	 */
 	public final String getFqan() {
 		return fqan;
 	}
 
 	/**
 	 * Returns the credential in GSS format. This is just a convenience method
 	 * for credentials that are of type ProxyCredential. It does not exist for
 	 * the superclass.
 	 * 
 	 * @return the credential data in GSS format
 	 */
 	public final GSSCredential getGssCredential() {
 		if (credentialData == null) {
 			return null;
 		}
 		if (this.gsscredential == null) {
 			this.gsscredential = convertToGSSCredential(this.credentialData);
 		}
 		return this.gsscredential;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.credential.model.GenericCredential#getId()
 	 */
 	public final Long getId() {
 		return id;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.credential.model.GenericCredential#getJobs()
 	 */
 	public final Set<Job> getJobs() {
 		return jobs;
 	}
 
 	@Override
 	public int hashCode() {
 		if ( getGssCredential() != null ) {
 			return (getGssCredential().hashCode() *41)  ;
 		} else {
 			return uuid.hashCode();
 		}
 	}
 
 	/**
 	 * Not used yet.
 	 * 
 	 * @return
 	 */
 	public final boolean isRenewable() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/**
 	 * Checks whether this credential is valid.
 	 * 
 	 * @return true - if valid; false - if not
 	 */
 	public final boolean isValid() {
 		try {
 			if (getGssCredential() == null) {
 				return false;
 			}
 			if (getGssCredential().getRemainingLifetime() <= 0) {
 				return false;
 			} else {
 				return true;
 			}
 		} catch (final GSSException e) {
 			myLogger.error(e);
 			return false;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * grisu.credential.model.GenericCredential#remove(grisu
 	 * .js.model.Job)
 	 */
 	public final void remove(final Job job) {
 		jobs.remove(job);
 	}
 
 	/**
 	 * Not used yet.
 	 * 
 	 * @return
 	 */
 	public final boolean renew() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/**
 	 * Sets the credential using a byte[]. This is used by hibernate to load the
 	 * data back into a credential object form a database entry.
 	 * 
 	 * @param data
 	 *            the credential data
 	 */
 	protected final void setCredentialData(final byte[] data) {
 		this.credentialData = data;
 		this.gsscredential = convertToGSSCredential(data);
 		// forcing these two fields to refresh
 		this.dn = null;
 		this.expiryDate = null;
 	}
 
 	/**
 	 * This should be used only by hibernate. This only sets the dn from the
 	 * database. We trust that this is the right dn. Once the CredentialData is
 	 * loaded from the database, the dn gets updated.
 	 * 
 	 * @param dn
 	 *            the dn
 	 */
 	protected final void setDn(final String dn) {
 		this.dn = dn;
 	}
 
 	/**
 	 * This should be used only by hibernate. This only sets the expiry date
 	 * from the database. We trust that this is the right expiry date. Once the
 	 * CredentialData is loaded from the database, the expiry date gets updated.
 	 * 
 	 * @param expiryDate
 	 *            the expiry date
 	 */
 	protected final void setExpiryDate(final Date expiryDate) {
 		this.expiryDate = expiryDate;
 	}
 
 	/**
 	 * This should be used only by hibernate. This only sets the fqan from the
 	 * database. We trust that this is the right fqan. Once the CredentialData
 	 * is loaded from the database, the fqan gets updated.
 	 * 
 	 * @param fqan
 	 *            the fqan
 	 */
 	protected final void setFqan(final String fqan) {
 		this.fqan = fqan;
 	}
 
 	// public static void main(String[] args) throws GlobusCredentialException,
 	// Exception {
 	//
 	// ProxyCredentialDAO creddao = new ProxyCredentialDAO();
 	// ProxyCredential cred = new
 	// ProxyCredential(CredentialHelpers.wrapGlobusCredential(CredentialHelpers.loadGlobusCredential(new
 	// File("/tmp/x509up_u1000"))));
 	//
 	// creddao.save(cred);
 	//
 	// ProxyCredential cred2 = creddao.findCredentialByID(new Long(2));
 	//
 	// System.out.println(cred2.getExpiryDate());
 	//
 	// }
 
 	/**
 	 * For hibernate. Sets the id of the job.
 	 * 
 	 * @param id
 	 *            the new id of the job.
 	 */
 	private void setId(final Long id) {
 		this.id = id;
 	}
 
 	/**
 	 * Used by hibernate to initiate the credential object when loading it from
 	 * the database.
 	 * 
 	 * @param jobs
 	 *            all jobs that are using this credential
 	 */
 	private void setJobs(final Set<Job> jobs) {
 		this.jobs = jobs;
 	}
 
 }
