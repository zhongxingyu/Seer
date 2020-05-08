 package nl.nikhef.jgridstart;
 
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.File;
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.security.GeneralSecurityException;
 import java.security.PrivateKey;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import nl.nikhef.jgridstart.util.FileUtils;
 import nl.nikhef.jgridstart.util.PasswordCache;
 import nl.nikhef.jgridstart.util.PasswordCache.PasswordCancelledException;
 import nl.nikhef.jgridstart.CertificateCheck.CertificateCheckException;
 import nl.nikhef.jgridstart.ca.CAException;
 import nl.nikhef.jgridstart.gui.util.ArrayListModel;
 
 /** Mangement of multiple {@code .globus}-type certificates on disk
  * <p>
  * This represents a directory that has {@code .globus}-type subdirectories as
  * its children, each of which is represented by a {@link CertificatePair}.
  * <p>
  * The default location of this store is {@code ~/.globus}. The key and
  * certificate found in this directory itself are ignored; please see
  * {@link CertificateStoreWithDefault} for handling those.
  * <p>
  * All this is implemented to allow multiple certificates to be present, for
  * example when one is a member of multiple organisations, or if a renewal is
  * in progress.
  * <p>
  * <h3>Listeners</h3>
  * A user-interface may want to keep a list of certificates synchronised with
  * this {@linkplain CertificateStore}. To this end, the {@link ItemListener}
  * interface was implemented, and the {@link ArrayListModel} was used as its
  * parent class. The former allows one to catch changes in a
  * {@linkplain CertificatePair}, while the latter notifies its listeners when
  * an item is added or removed.
  * <p>
  * 
  * @author wvengen
  *
  */
 public class CertificateStore extends ArrayListModel<CertificatePair> implements ItemListener {
 
     static protected Logger logger = Logger.getLogger("nl.nikhef.jgridstart");
 
     protected File path = null;
     protected PasswordCache pwcache = null;
     /** Default certificate (the one copied to {@code ~/.globus}) */
     protected CertificatePair defaultCert = null;
     /** Prefix of user certificate subdirs of {@code ~/.globus} to load from */
     protected final String userCertPrefix = "user-cert-";
 
     /** new empty certificate store */
     public CertificateStore() {
 	super();
     }
 
     /** new certificate store and load from path */
     public CertificateStore(String path) {
 	this();
 	load(path);
     }
 
     /** new certificate store and load from path as File */
     public CertificateStore(File path) {
 	this();
 	load(path);
     }
     
     /** load certificates from the default directory
      * <p>
      * This is {@code ~/.globus} by default, but if the hostname starts
      * with "tutorial" we have something different.
      * <p>
      * TODO move this out of jGridStart and put it in a configfile
      */
     public void load() {
 	String hostname;
 	try {
 	    hostname = java.net.InetAddress.getLocalHost().getHostName();
 	} catch (UnknownHostException e) {
 	    hostname = "";
 	}
 	String certhome = System.getProperty("user.home") + File.separator;
 	if (hostname.startsWith("tutorial")) {
 	    certhome += "personal-dutchgrid-certificate";
 	} else {
 	    certhome += ".globus";
 	}
 	certhome += File.separator;
 	load(certhome);
     }
     
     /** Load certificates from store path
      * <p>
      * All subdirectories of the supplied path are loaded as separate
      * certificates.
      * 
      * @param path Path to load certificates from
      * @see #load(File)
      */
     public void load(String path) {
 	load(new File(path));
     }
 
     /** Load certificates from store path
      * <p>
      * All subdirectories of the supplied path that start with
      * {@link #userCertPrefix} are loaded as separate certificates; in
      * addition to this, the directory itself is loaded as well.
      * 
      * @param path Directory to load certificates from
      */
     public void load(File path) {
 	this.path = path;
 	if (!path.isDirectory()) return;
 	File[] files = path.listFiles();
 	
 	// add new items
 	for (int i=0; i<files.length; i++) {
 	    File f = files[i];
 	    // filter out unwanted items
 	    if (!f.getName().startsWith(userCertPrefix)) continue;
 	    if (!f.isDirectory()) continue;
 	    // make sure it doesn't exist already in this store
 	    boolean found = false;
 	    for (int j = 0; j < size(); j++) {
 		File a = get(j).getPath();
 		if (a.equals(f)) {
 		    found = true;
 		    break;
 		}
 	    }
 	    // add when required
 	    if (!found)
 		tryAdd(f);
 	}
     }
 
     /** refresh the certificate list from its source and each certificate as well */
     public void refresh() throws GeneralSecurityException, IOException, CAException {
 	if (path == null) {
 	    logger.warning("Refresh of empty certificate store");
 	    return;
 	}
 	List<File> files = Arrays.asList(path.listFiles());
 	ArrayList<CertificatePair> removals = new ArrayList<CertificatePair>();
 	// refresh each existing item and remove old ones
 	for (int i = 0; i < size(); i++) {
 	    CertificatePair c = get(i);
 	    if (c.getPath() == path || files.contains(c.getPath()))
 		c.refresh();
 	    else
 		removals.add(c);
 	}
 	removeAll(removals);
 	// add new items
 	load(path);
     }
 
     /** Try to add a certificate path to this store but don't fail if an error occurs.
      * 
      * @param f File to add
      * @return true if the certificate was succesfully added
      */
     protected boolean tryAdd(File f) {
 	try {
 	    add(new CertificatePair(f));
 	} catch (IOException e) {
 	    logger.warning("Failed to load certificate from " + f + ": " + e.getMessage());
 	    return false;
 	} catch (CertificateCheckException e) {
 	    logger.warning("Certificate invalid " + f + ": " + e.getMessage());
 	    return false;
 	}
 	return true;
     }
     
     /** Create a new subdirectory for a {@linkplain CertificatePair} in this store.
      *
      * @return Newly created directory name
      * @throws IOException
      */
     protected File newItem() throws IOException {
 	File dst = null;
	for (int i = 0; i < Integer.MAX_VALUE; i++) {
	    dst = new File(path, String.format("user-cert-%04d", i));
 	    if (!dst.exists()) break;
 	}
 	if (dst.exists())
 	    throw new IOException("Maximum number of certificates reached"); // very unlikely
 	dst.mkdirs();
 	if (!dst.exists())
 	    throw new IOException("Could not create new certificate directory: " +
 		    dst + ".\n" +
 		    "Please check permissions, disk space and quota.");
 	return dst;
     }
     
     /** Hook parent to add an {@linkplain ItemListener} when an item is added.
      * <p>
      * {@inheritDoc} */
     @Override
     protected void notifyAdded(int start, int end) {
 	super.notifyAdded(start, end);
 	for (int i=start; i<=end; i++)
 	    get(i).addItemListener(this);
     }
     /** Hook parent to remove an {@linkplain ItemListener} when an item is removed
      * <p>
      * {@inheritDoc} */
     @Override
     protected void notifyRemoved(int start, int end) {
 	for (int i=start; i<=end; i++)
 	    get(i).removeItemListener(this);
 	super.notifyRemoved(start, end);
     }
     /** {@linkplain ItemListener} handler to catch changes in {@linkplain CertificatePair} */
     public void itemStateChanged(ItemEvent e) {
 	// and notify selection change listeners
 	notifyChanged(indexOf(e.getItem()));
     }
     
     /** Deletes a CertificatePair from the store.
      * <p>
      * This removes it permanently from disk, so be careful.
      * In the future it may be put into an archive instead.
      * <p>
      * TODO should this be called 'remove' or is that too dangerous?
      * 
      * @throws IOException 
      */
     public CertificatePair delete(int index) throws IOException {
 	logger.info("Deleting certificate #"+index+": "+get(index));
 	CertificatePair cert = super.remove(index);
 	deletePath(cert.getPath());
 	return cert;
     }
     /** Deletes a path on which a {@linkplain CertificatePair} is based from disk. */
     protected void deletePath(File certPath) throws IOException {
 	// and from disk; subdirs are not deleted
 	File[] items = certPath.listFiles();
 	for (int i=0; i<items.length; i++)
 	    if (!items[i].delete())
 		throw new IOException("Could not remove file: " +
 			items[i] + ".\n" +
 			"Please check permissions, disk space and quota.");
 	if (!certPath.delete())
 	    throw new IOException("Could not remove certificate directory " + 
 		    certPath + ".\n" +
 		    "Please check permissions, disk space and quota."); 
     }
     public CertificatePair delete(CertificatePair cert) throws IOException {
 	return delete(indexOf(cert));
     }
 
     /** Import a PKCS#12 or PEM file as a new entry
      * <p>
      * A new {@linkplain CertificatePair} is created from the imported file, and
      * this is added as a new entry to this store.
      * 
      * @param src File to import from
      * @param dstpw password to use for private key storage, or {@code null} to use the
      *          same password as the import password
      * @return Newly created CertificatePair
      */
     public CertificatePair importFrom(File src, char[] dstpw) throws IOException, GeneralSecurityException, CertificateCheckException {
 	File dst = newItem();
 	// import
 	try {
 	    CertificatePair cert = CertificatePair.importFrom(src, dst, dstpw);
 	    add(cert);
 	    return cert;
 	} catch(IOException e) {
 	    deletePath(dst);
 	    throw e;
 	} catch(GeneralSecurityException e) {
 	    deletePath(dst);
 	    throw e;
 	} catch (CertificateCheckException e) {
 	    deletePath(dst);
 	    throw e;
 	}
     }
 
     /** Import a PKCS#12 or PEM file as a new entry
      * 
      * @param src File to import from
      * @see #importFrom(File, char[])
      */
     public CertificatePair importFrom(File src) throws IOException, GeneralSecurityException, CertificateCheckException {
 	return importFrom(src, null);
     }
 
     
     /** Create a new certificate request
      *
      * @param p Properties to use for generation
      * @see CertificatePair#generateRequest
      */
     public CertificatePair generateRequest(Properties p) throws IOException, GeneralSecurityException, PasswordCancelledException, CAException {
 	File dst = newItem();
 	try {
 	    CertificatePair cert = CertificatePair.generateRequest(dst, p);
 	    add(cert);
 	    return cert;
 	} catch(IOException e) {
 	    deletePath(dst);
 	    throw e;
 	}
     }
     /** Create a new certificate request with preset password
      *
      * @param p Properties to use for generation
      * @param pw password to use for new private key
      * @see CertificatePair#generateRequest
      */
     public CertificatePair generateRequest(Properties p, char[] pw) throws IOException, GeneralSecurityException, PasswordCancelledException, CAException {
 	File dst = newItem();
 	try {
 	    CertificatePair cert = CertificatePair.generateRequest(dst, p, pw);
 	    add(cert);
 	    return cert;
 	} catch(IOException e) {
 	    deletePath(dst);
 	    throw e;
 	} catch(GeneralSecurityException e) {
 	    deletePath(dst);
 	    throw e;
 	} catch(CAException e) {
 	    deletePath(dst);
 	    throw e;
 	}
     }
     /** Renew a certificate with preset password
      * <p>
      * Generates a new certificate request in the store supplying password
      * for new private key as a renewal from an existing certificate.
      * 
      * @param oldCert certificate to renew
      * @param pw password to use for new private key
      */
     public CertificatePair generateRenewal(CertificatePair oldCert, char[] pw) throws IOException, GeneralSecurityException, PasswordCancelledException, CAException {
 	// ask for private key first, cancel would skip everything
 	PrivateKey oldKey = oldCert.getPrivateKey();
 	
 	// generate request
 	CertificatePair newCert = generateRequest(oldCert, pw);
 	try {
 	    newCert.setProperty("renewal", "true");
 	    newCert.setProperty("renewal.parent.path", oldCert.getProperty("path"));
 	    newCert.setProperty("renewal.parent.modulus", oldCert.getProperty("modulus"));
 
 	    // and store it as new CSR
 	    FileUtils.writeFile(newCert.getCSRFile(),
 		    newCert.getCA().signCertificationRequest(newCert.getCSR(), newCert,
 			    oldKey, oldCert.getCertificate()));
 
 	} catch (IOException e) {
 	    remove(newCert);
 	    deletePath(newCert.path);
 	    throw e;
 	}
 	
 	return newCert;
     }
     
     /** Renew a certificate
      * 
      * @see #generateRenewal(CertificatePair, char[])
      */
     public CertificatePair generateRenewal(CertificatePair oldCert) throws PasswordCancelledException, IOException, GeneralSecurityException, CAException {
 	return generateRenewal(oldCert, null);
     }
 }
