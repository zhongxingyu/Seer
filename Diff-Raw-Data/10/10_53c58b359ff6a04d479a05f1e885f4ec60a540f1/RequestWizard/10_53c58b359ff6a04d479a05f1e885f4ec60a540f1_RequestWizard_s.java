 package nl.nikhef.jgridstart.gui;
 
 import java.awt.Dialog;
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.swing.AbstractAction;
 import javax.swing.JOptionPane;
 
 import org.jdesktop.swingworker.SwingWorker;
 import org.xhtmlrenderer.swing.BasicPanel;
 import org.xhtmlrenderer.swing.LinkListener;
 
 import nl.nikhef.jgridstart.CertificatePair;
 import nl.nikhef.jgridstart.CertificateRequest;
 import nl.nikhef.jgridstart.CertificateSelection;
 import nl.nikhef.jgridstart.CertificateStore;
 import nl.nikhef.jgridstart.Organisation;
 import nl.nikhef.jgridstart.gui.util.ErrorMessage;
 import nl.nikhef.jgridstart.gui.util.TemplateWizard;
 import nl.nikhef.jgridstart.gui.util.URLLauncherCertificate;
 import nl.nikhef.jgridstart.install.BrowserFactory;
 import nl.nikhef.jgridstart.install.exception.BrowserNotAvailableException;
 import nl.nikhef.jgridstart.util.PasswordCache;
 import nl.nikhef.jgridstart.util.PasswordCache.PasswordCancelledException;
 
 /** Wizard that asks the user for information and generates the certificate */
 public class RequestWizard extends TemplateWizard implements TemplateWizard.PageListener {
 
     /** CertificateStore to operate on */
     protected CertificateStore store = null;
     /** CertificateSelection to select newly requested certificate*/
     protected CertificateSelection selection = null;
     /** the resulting CertificatePair, or null if not yet set */
     protected CertificatePair cert = null;
     /** the parent CertificatePair in case of a renewal */
     protected CertificatePair certParent = null;
     /** working thread */
     protected GenerateWorker worker = null;
 
     /** New certificate request */
     public RequestWizard(Frame parent, CertificateStore store, CertificateSelection sel) {
 	super(parent);
 	this.store = store;
 	this.selection = sel;
 	setData(new Properties());
     }
     /** New certificate request */
     public RequestWizard(Dialog parent, CertificateStore store, CertificateSelection sel) {
 	super(parent);
 	this.store = store;
 	this.selection = sel;
 	setData(new Properties());
     }
     /** View form of existing CertificatePair */
     public RequestWizard(Frame parent, CertificatePair cert, CertificateSelection sel) {
 	super(parent);
 	this.cert = cert;
 	this.selection = sel;
 	setData(cert);
     }
     /** View form of existing CertificatePair */
     public RequestWizard(Dialog parent, CertificatePair cert, CertificateSelection sel) {
 	super(parent);
 	this.cert = cert;
 	this.selection = sel;
 	setData(cert);
     }
     /** Certificate renewal */
     public RequestWizard(Frame parent, CertificateStore store, CertificatePair certParent, CertificateSelection sel) {
 	super(parent);
 	this.store = store;
 	this.selection = sel;
 	setRenewal(certParent);
     }
     /** Certificate renewal */
     public RequestWizard(Dialog parent, CertificateStore store, CertificatePair certParent, CertificateSelection sel) {
 	super(parent);
 	this.store = store;
 	this.selection = sel;
 	setRenewal(certParent);
     }
     /** Make this request wizard a renewal request */
     private void setRenewal(CertificatePair certParent) {
 	this.certParent = certParent;
 	setData(new Properties());
     }
     /** Return whether we have a renewal or not */
     protected boolean isRenewal() {
 	return certParent != null;
     }
     
     /** Set the current step to the one that is relevant for the process. */
     public void setStepDetect() {
 	int step = 2; // fallback to form page
 	
 	if (cert==null)
 	    step = 0;
 	else if (!Boolean.valueOf(cert.getProperty("cert"))) {
 	    if (!Boolean.valueOf(cert.getProperty("request.submitted")))
 		step = 1;
 	    else if (!Boolean.valueOf(cert.getProperty("request.processed")))
 		step = 2;
 	} else {
 	    if (!Boolean.valueOf(cert.getProperty("install.done")))
 		step = 3;
 	    else if (pages.size()>=5)
 		step = 4; // post-install step, if present
 	}
 	
 	setStep(step);
     }
     
     @Override
     protected void initialize() {
 	super.initialize();
 	setPreferredSize(new Dimension(800, 550));
 	// add the html pages
 	pages.add(getClass().getResource("requestwizard-01.html"));
 	pages.add(getClass().getResource("requestwizard-02.html"));
 	pages.add(getClass().getResource("requestwizard-03.html"));
 	pages.add(getClass().getResource("requestwizard-04.html"));
 	// add optional user-supplied html page at the end
 	if (getClass().getResource("requestwizard-05.html")!=null)
 	    pages.add(getClass().getResource("requestwizard-05.html"));
 	setHandler(this);
 	// extra special handling of "action:" links
 	replaceLinkListener(new LinkListener() {
 	    @Override
             public void linkClicked(BasicPanel panel, String uri) {
 		// handle help toggle buttons, not a regular action
 		if (uri.startsWith("action:toggle(")) {
 		    String var = uri.substring(14,uri.length()-1);
 		    boolean val = Boolean.valueOf(data().getProperty(var));
 		    data().setProperty(var, Boolean.toString(!val));
 		    data().setProperty(var+".volatile", "true");
 		    refresh();
 		    return;
 		}
 		// select certificate so that any "action:" links are executed
 		// on the correct certificate.
 		if (uri.startsWith("action:"))
 		    selection.setSelection(cert);
 		// hook for install step to set private key password from form before action
 		if (data().getProperty("wizard.privkeypass")!=null) {
 		    assert(cert!=null);
 		    try {
 			PasswordCache.getInstance().set(cert.getKeyFile().getCanonicalPath(),
 			    data().getProperty("wizard.privkeypass").toCharArray());
 		    } catch (IOException e) {
 			ErrorMessage.internal(getParent(), e);
 		    }
 		}
 		// go!
 		URLLauncherCertificate.openURL(uri, panel);
 		// refresh document after action because properties may be updated
 		if (uri.startsWith("action:"))
 		    refresh();
 	    }	    
 	});
     }
     @Override
     public void setData(Properties p) {
 	super.setData(p);
 	// also set static properties for the forms
 	// initialize properties when new request / renewal
 	if (cert==null) {
 	    // help the user by prefilling some elements
 	    if (!isRenewal()) {
 		CertificateRequest.preFillData(p, certParent);
 	    	data().setProperty("wizard.title", "Request a new certificate");
 	    } else {
 		// parse fields from dn if needed
 		CertificateRequest.completeData(certParent);
 		CertificateRequest.preFillData(data(), certParent);
 		// cannot edit fields for renewal; except email!!!
 		CertificateRequest.postFillDataLock(data());
 		data().setProperty("email.lock", Boolean.toString(false));
     	    	data().setProperty("wizard.title", "Renew a certificate");
 	    }
 	} else {
 	    CertificateRequest.completeData(cert);
 	    if (!data().containsKey("wizard.title"))
 		data().setProperty("wizard.title", "Certificate Request");
 	}
 	data().setProperty("wizard.title.volatile", "true");
 	data().setProperty("wizard.title.html", data().getProperty("wizard.title"));
 	data().setProperty("wizard.title.html.volatile", "true");
 	data().setProperty("organisations.html.options", Organisation.getAllOptionsHTML(cert));
 	data().setProperty("organisations.html.options.volatile", "true");
 	// workaround for checkboxes without a name; even with checked="checked" they
 	// would sometimes not be shown as checked (irregular behaviour though)
 	data().setProperty("true", "true");
 	data().setProperty("true.volatile", "true");
 	// make sure to keep the password safe
 	data().setProperty("password1.volatile", "true");
 	data().setProperty("password2.volatile", "true");
 	data().setProperty("wizard.privkeypass.volatile", "true");
     }
     
     /** called before a page in wizard is changed */
     public boolean pageLeave(TemplateWizard w, int curPage, int newPage) {
 	if (newPage==0) {
 	    // lock fields that generate DN
 	    if (cert!=null)
 		CertificateRequest.postFillDataLock(data());
 	}
 	
 	// make sure input fields for certificate are ok
 	if (curPage==0 && cert==null) {
 	    // make sure passwords are equal
 	    if (data().getProperty("password1")!=null &&
 		    !data().getProperty("password1").equals(data().getProperty("password2"))) {
 		JOptionPane.showMessageDialog(this,
 			"Passwords don't match, please make sure they are equal.",
 			"Passwords don't match", JOptionPane.ERROR_MESSAGE);
 		return false;
 	    }
 	    // make sure we have a valid email address
 	    try {
 		new InternetAddress(data().getProperty("email")).validate();
 	    } catch (AddressException e) {	    
 		JOptionPane.showMessageDialog(this,
 			"Please enter a valid email address.\n(" + e.getLocalizedMessage() + ")",
 			"Bad email address", JOptionPane.ERROR_MESSAGE);
 		return false;
 	    }
 	    
 	    // Not needed for renewal, since it may be that the level wasn't
 	    //   specified because the parent hadn't set the level explictely
 	    //   after an import, for example.
 	    if (!isRenewal()) {
 		// and a level was chosen
 		if (data().getProperty("level")==null) {
 		    JOptionPane.showMessageDialog(this,
 			    "Please select a certification level",
 			    "Missing data", JOptionPane.ERROR_MESSAGE);
 		    return false;
 		}
 		if (data().getProperty("givenname").length()==0 ||
 			data().getProperty("surname").length()==0 ) {
 		    JOptionPane.showMessageDialog(this,
 			    "Please enter your full name",
 			    "Missing data", JOptionPane.ERROR_MESSAGE);
 		    return false;
 		}
 	    }
 	}
 	
 	// set organisation info if not present
 	if (data().getProperty("org.ras")==null) {
 	    Organisation org = Organisation.get(data().getProperty("org"));
 	    if (org!=null)
 		org.copyTo(data(), "org.");
 	}
 	
 	// clear error message and passwords, if any
 	if (curPage != newPage) {
 	    data().remove("wizard.error");
 	    data().remove("wizard.error.volatile");
 	    data().remove("wizard.privkeypass");
 	    data().remove("wizard.privkeypass.volatile");
 	}
 	
 	// ok!
 	return true;
     }
 
     /** called when page in wizard is changed */
     public void pageEnter(TemplateWizard w, int oldPage, int curPage) {
 	// custom window title
 	setTitle(data().getProperty("wizard.title") + " - " + getDocumentTitle(curPage));
 	
 	// stop worker on page change when needed
 	if (worker!=null) {
 	    worker.cancel(true);
 	    worker = null;
 	}
 	
 	// lock password fields on first page when key is present
 	if (curPage==0 && cert!=null && cert.getKeyFile().exists()) {
 	    data().setProperty("password1.lock", "true");
 	    data().setProperty("password1.lock.volatile", "true");
 	    data().setProperty("password2.lock", "true");
 	    data().setProperty("password2.lock.volatile", "true");
 	}
 	if (curPage==0 && cert!=null && Boolean.valueOf(cert.getProperty("request.submitted"))) {
 	    data().setProperty("agreecps.lock", "true");
 	    data().setProperty("email.lock", "true");
 	}
 	
 	// say "Close" when a certificate is present because everything is done by then
 	// TODO this won't work for renewals
 	try {
 	    if (cert!=null && cert.getCertificate()!=null)
 		cancelAction.putValue(AbstractAction.NAME, "Close");
 	} catch (IOException e) { }
 
 	if ((curPage==1 || curPage==3) && curPage!=oldPage) {
 	    // on page two we need to execute the things
 	    //  curPage!=oldPage is really needed, since worker.execute()
 	    //  refreshes the page several times to update the status, and
 	    //  that triggers this again. We don't want to get stuck in
 	    //  an update loop, do we.
 	    worker = new GenerateWorker(curPage);
 	    worker.useErrorDialog(curPage!=1);
 	    worker.execute();
 	}
 	// stop wizard when no certificate yet before install step
 	if (curPage==2) {
 	    if (cert==null) {
 		nextAction.setEnabled(false);
 	    } else if (!Boolean.valueOf(cert.getProperty("cert")) &&
 		    !Boolean.valueOf(cert.getProperty("request.processed"))) {
 		nextAction.setEnabled(false);
 		cancelAction.putValue(AbstractAction.NAME, "Close");
 	    }
 	}
 	
 	// update default browser
 	if (curPage==3) {
 	    try {
 		// set browser properties
 		System.setProperty("install.browser", BrowserFactory.getInstance().getDefaultBrowser());
 		String browserid = data().getProperty("install.browser");
 		if (browserid==null) browserid = System.getProperty("install.browser");
 		// remove the old browser Properties
 		for (Enumeration<?> en = data().propertyNames(); en.hasMoreElements(); ) {
 		    String key = (String)en.nextElement();
 		    if (!key.startsWith("install.browser.")) continue;
 		    data().remove(key);
 		}
 		// copy the browser's Properties
 		Properties p = BrowserFactory.getInstance().getBrowserProperties(browserid);
 		for (Enumeration<?> en = p.propertyNames(); en.hasMoreElements(); ) {
 		    String key = (String)en.nextElement();
 		    data().setProperty("install.browser."+key, p.getProperty(key));
 		    data().setProperty("install.browser."+key+".volatile", "true");
 		}
 	    } catch (IOException e) {
 		// no browser info, handled in html by !${install.browser*}
 	    } catch (BrowserNotAvailableException e) {
		// should not happen as we query default browser
		ErrorMessage.internal(this, e);
 	    } catch (Exception e) {
 		ErrorMessage.error(this, "Certificate installation failed", e);
 	    }
 	}
     }
 
     /** {@inheritDoc}
      * <p>
      * This method adds a css class done when a step is finished
      * for the state of the certificate.
      */
     @Override
     protected String getWizardContentsLine(int step, int current) {
 	String classes = "";
 	// standard classes from parent
 	if (step==current) classes += " wizard-current";
 	if (step>current)  classes += " wizard-future";
 	// step 0: done if CertificatePair present
 	if (step==0 && cert!=null) classes += " wizard-done";
 	// step 1: done if request submitted or certificate present
 	if (step==1 && cert!=null &&
 		(Boolean.valueOf(cert.getProperty("request.submitted")) ||
 		 Boolean.valueOf(cert.getProperty("cert"))) )
 	    classes += " wizard-done";
 	// step 2: done if certificate present or request processed
 	if (step==2 && cert!=null &&
 		(Boolean.valueOf(cert.getProperty("cert")) ||
 		 Boolean.valueOf(cert.getProperty("request.processed")) ))
 	    classes += " wizard-done";
 	// step 3: done if certificate installed previously
 	if (step==3 && cert!=null && cert.getProperty("install.done")!=null)
 	    classes += " wizard-done";
 	return (classes=="" ? "<li>" : "<li class='"+classes+"'>") + getDocumentTitle(step) + "</li>\n";
     }
 
     /** worker thread for generation of a certificate */
     protected class GenerateWorker extends SwingWorker<Void, String> {
 	protected Properties p;
 	/** exception from background thread */
 	protected Exception e = null;
 	/** current step */
 	protected int step = -1;
 	
 	protected boolean useErrordlg = true;
 
 	public GenerateWorker(int step) {
 	    super();
 	    this.step = step;
 	    this.p = data();
 	}
 	
 	/** Set error handler behaviour: dialog, or set property
 	 *  for showing in template. */
 	public void useErrorDialog(boolean use) {
 	    this.useErrordlg = use;
 	}
 
 	/** worker thread that generates the certificate, etc. */
 	@Override
 	protected Void doInBackground() throws Exception {
 	    // Generate a keypair and certificate signing request
 	    // TODO verify concurrency 
 	    // TODO make this configurable
 	    try {
 		// generate request when no key or certificate
 		if (cert==null) {
 		    // generate request
 		    // TODO check thread safety ... :/
 		    CertificatePair newCert = null;
 		    if (!isRenewal()) {
 			CertificateRequest.postFillData(p);
 			 newCert = store.generateRequest(p, p.getProperty("password1").toCharArray());
 		    } else {
 			p.setProperty("subject", certParent.getProperty("subject"));
 			newCert = store.generateRenewal(certParent, p.getProperty("password1").toCharArray());
 		    }
 		    // clear password
 		    p.remove("password1");
 		    p.remove("password1.volatile");
 		    p.remove("password2");
 		    p.remove("password2.volatile");
 		    // copy properties to certificate pair
 		    for (Enumeration<?> en = p.propertyNames(); en.hasMoreElements(); ) {
 			String key = (String)en.nextElement();
 			if (key!=null && p.getProperty(key)!=null)
 			    newCert.setProperty(key, p.getProperty(key));
 		    }
 		    // and make sure data is saved
 		    newCert.store();
 		    cert = newCert;
 		    publish("state.certificate_created");
 		}
 		// upload request only if no certificate present yet
 		if (cert.getCertificate()==null && !Boolean.valueOf(cert.getProperty("request.processed"))) {
 		    // upload request if it hasn't been done
 		    if (!Boolean.valueOf(cert.getProperty("request.submitted"))) {
 			cert.uploadRequest();
 		    }
 		}
 		// make sure gui is updated and user can continue
 		if (step==1) publish("state.cancontinue");
 		// update downloadable status
 		if (!Boolean.valueOf(cert.getProperty("request.processed"))) {
 		    cert.isCertificationRequestProcessed();
 		    publish((String)null);
 		}
 		// and download when needed and possible
 		if (cert.getCertificate()==null && 
 			Boolean.valueOf(cert.getProperty("request.processed"))) {
 		    cert.downloadCertificate();
 		    publish((String)null);
 		}
 	    } catch (PasswordCancelledException e) {
 		// special state to go to the previous page
 		publish("state.cancelled");
 	    } catch (Exception e) {
 		// store exception so it can be shown to the user
 		this.e = e;
 		publish("state.cancelled");
 	    }
 	    return null;
 	}
 
 	/** process publish() event from worker thread. This updates the
 	 * gui in the sense that a property is added to the TemplateWizard
 	 * and it is refreshed. This gives a template the opportunity to
 	 * change the display based on a property (e.g. a checkbox) */
 	@Override
 	protected void process(List<String> keys) {
 	    // process messages
 	    for (Iterator<String> it = keys.iterator(); it.hasNext(); ) {
 		String key = it.next();
 		if (key==null) continue;
 		// process cancel
 		if (key.equals("state.cancelled")) {
 		    if (e!=null) {
 			if (useErrordlg) {
 			    ErrorMessage.error(RequestWizard.this, "Error during request", e);
 			    setStepRelative(-1);
 			} else {
 			    data().setProperty("wizard.error", e.getLocalizedMessage());
 			    data().setProperty("wizard.error.volatile", "true");
 			    refresh();
 			}
 		    }
 		    return;
 		}
 		// select certificate in main view on creation
 		if (key.equals("state.certificate_created")) {
 		    assert(cert!=null);
 		    setData(cert);
 		    if (selection!=null) {
 			int index = store.indexOf(cert);
 			selection.setSelection(index);
 		    }
 		}
 		// update next button
 		if (key.equals("state.cancontinue") && step<pages.size()-1)
 		    nextAction.setEnabled(true);
 	    }
 	    // update content pane
 	    refresh();
 	}
     }
 }
