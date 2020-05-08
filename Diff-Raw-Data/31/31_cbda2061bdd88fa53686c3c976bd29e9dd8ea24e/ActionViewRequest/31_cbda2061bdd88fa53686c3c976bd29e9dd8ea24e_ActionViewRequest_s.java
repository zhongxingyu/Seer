 package nl.nikhef.jgridstart.gui;
 
 import java.awt.event.ActionEvent;
 import javax.swing.JFrame;
 import javax.swing.event.ListSelectionEvent;
 
 import nl.nikhef.jgridstart.CertificatePair;
 import nl.nikhef.jgridstart.CertificateSelection;
 import nl.nikhef.jgridstart.gui.util.BareBonesActionLaunch;
 import nl.nikhef.jgridstart.gui.util.TemplateWizard;
 
 /** Shows the "request new" wizard from ActionRequest, but this action
  * just views the form of the currently selected certificate and doesn't
  * create a new one.
  * 
  * @author wvengen
  */
 public class ActionViewRequest extends CertificateAction {
     
     /** Page of request wizard to show by default */
     protected int defaultPage = 0;
     
     public ActionViewRequest(JFrame parent, CertificateSelection s) {
 	super(parent, s);
 	putValue(NAME, "View request...");
 	putValue(MNEMONIC_KEY, new Integer('R'));
 	BareBonesActionLaunch.addAction("viewrequest", this);
     }
     public ActionViewRequest(JFrame parent, CertificateSelection s, int defaultPage) {
 	this(parent, s);
 	this.defaultPage = defaultPage;
     }
     
     /** Name depends on state of selected certificate */
     @Override
     public void valueChanged(ListSelectionEvent e) {
	CertificatePair cert = selection.getCertificatePair();
	if (cert!=null && cert.getCertificate()!=null)
	    putValue(NAME, "View request...");
	else
	    putValue(NAME, "Continue request...");
 	super.valueChanged(e);
     }
     
     public void actionPerformed(ActionEvent e) {
 	logger.finer("Action: "+getValue(NAME));
 	TemplateWizard dlg = new RequestWizard(parent, selection.getCertificatePair());
 	dlg.setStep(defaultPage);
 	dlg.setVisible(true);
     }
 }
