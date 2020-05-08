 package nl.nikhef.jgridstart.gui;
 
 import java.awt.Component;
 import java.awt.Dimension;
 
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JList;
 
 import org.bouncycastle.asn1.x509.X509Name;
 
 import nl.nikhef.jgridstart.CertificatePair;
 import nl.nikhef.jgridstart.CertificateSelection;
 import nl.nikhef.jgridstart.CertificateStore;
 
 /**
  * Java Swing component that contains a list of certificates. It is a view for
  * the CertificateStore class and optionally interfaces with a
  * CertificateSelection.
  * 
  * @author wvengen
  */
 public class ComponentCertificateList extends JList {
 
     protected CertificateSelection selection = null;
     
     public ComponentCertificateList() {
 	super();
 	initialize();
     }
     public ComponentCertificateList(CertificateStore store, CertificateSelection selection) {
 	super();
 	initialize();
 	setModel(store);
 	setSelectionModel(selection);
 	setCellRenderer(new CertificateCellRenderer());
     }
 
     /**
      * Initialize this component, create its gui. Should be called only once.
      */
     protected void initialize() {
	setPreferredSize(new Dimension(150, 200));
     }
     
     class CertificateCellRenderer extends DefaultListCellRenderer {
 	public Component getListCellRendererComponent(JList list, Object value,
 		int index, boolean isSelected, boolean cellHasFocus) {
 	    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
 	    
 	    if (value==null) return this;
 	    
 	    // we want a more rich markup from the object
 	    CertificatePair cert = (CertificatePair)value;
 	    String name = cert.getSubjectPrincipalValue(X509Name.CN);
 	    String star = "", org = "";
 	    if (index==0) {
		star = "<b color='#ffcc00'>✰</b>";
 	    }	
 	    if (cert.getSubjectPrincipalValue(X509Name.CN)!=null) {
 		org = "<br>\n&nbsp;<i style='font-size:80%;'>"+
 			cert.getSubjectPrincipalValue(X509Name.O)+"</i>";
 	    }
 	    setText("<html><body width='100%'>"+name+star+org+"</html></body>");
 
 	    return this;
 	}
     }
 }
