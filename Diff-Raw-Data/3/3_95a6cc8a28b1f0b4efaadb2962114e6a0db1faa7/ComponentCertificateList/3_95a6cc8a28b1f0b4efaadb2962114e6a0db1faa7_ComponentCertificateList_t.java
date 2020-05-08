 package nl.nikhef.jgridstart.gui;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.io.IOException;
 
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JList;
 
 import org.bouncycastle.asn1.x509.X509Name;
 
 import nl.nikhef.jgridstart.CertificatePair;
 import nl.nikhef.jgridstart.CertificateSelection;
 import nl.nikhef.jgridstart.CertificateStoreWithDefault;
 import nl.nikhef.jgridstart.Organisation;
 
 /** List of certificates.
  * <p>
  * Java Swing component that contains a list of certificates. It is a view for
  * {@link CertificateStoreWithDefault} and optionally interfaces with a
  * {@link CertificateSelection}.
  * 
  * @author wvengen
  */
 public class ComponentCertificateList extends JList {
     
     /** maximum length of name to display; any longer gets an ellipsis */
     final static int maxNameLen = 22;
 
     /** currently selected item */
     protected CertificateSelection selection = null;
     
     /** Create a new certificate list */
     public ComponentCertificateList(CertificateStoreWithDefault store, CertificateSelection selection) {
 	super();
 	initialize();
 	setModel(store);
 	setSelectionModel(selection);
 	setCellRenderer(new CertificateCellRenderer());
     }
 
     /** Initialize this component, create its gui.
      * <p>
      * Should be called only once, from the constructor.
      */
     protected void initialize() {
 	setMaximumSize(new Dimension(200, Integer.MAX_VALUE));
     }
     
     /** Rendering of cells in this list */
     class CertificateCellRenderer extends DefaultListCellRenderer {
 	@Override
 	public Component getListCellRendererComponent(JList list, Object value,
 		int index, boolean isSelected, boolean cellHasFocus) {
 	    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
 	    if (value==null) return this;
 	    // we want a more rich markup from the object
 	    CertificatePair cert = (CertificatePair)value;
 	    String line1 = "", line2 = "";
 	    String dfl = "";
 	    // name of person
 	    String name = cert.getSubjectPrincipalValue(X509Name.CN);
	    if (name!=null && name.length() > maxNameLen) name = name.substring(0, maxNameLen-2)+"&#x2026;";
 	    if (name==null) name="<i>(Unknown)</i>";
 	    line2 += name;
 	    // add star to default certificate
 	    CertificatePair dflCert = null;
 	    try {
 		dflCert = ((CertificateStoreWithDefault)getModel()).getDefault();
 	    } catch (IOException e) { }
 	    if ( cert.equals(dflCert) )
 		dfl += "&nbsp;<b color='#ffcc00'>&#x2730</b>";
 	    // organisation
 	    Organisation org = Organisation.getFromCertificate(cert);
 	    if (org!=null) line1 += org.getProperty("name.full"); // TODO full name, incl. O if OU
 	    // add serial number to 3rd line, if any
 	    String serial = cert.getProperty("cert.serial");
 	    if (serial!=null) line2 += " <span color='#888888'>(#"+serial+ ")</span>";
 	    // set html contents
 	    String s =
 		"<html><body width='100%'>" +
 	    	"<table border='0' cellpadding='2' cellspacing='0'>" +
 	    	"  <tr>" +
 	    	"    <td width='19' align='center'>" + cert.getProperty("state.icon.html") + "</td>" +
 	    	"    <td>" +
 	    	"      "+ line1 + dfl + "<br>" +
 	    	"      <small>" + line2 + "</small>" + "<br>" +
 	    	"    </td>" +
 	    	"  </tr>" +
 	    	"</html></body>";
 	    setText(s);
 	    return this;
 	}
     }
 }
