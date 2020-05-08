 package nebraska;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.InputStream;
 import java.math.BigInteger;
 import java.security.KeyPair;
 import java.security.KeyStore;
 import java.security.PublicKey;
 import java.security.cert.CertPath;
 import java.security.cert.CertificateFactory;
 import java.security.cert.X509Certificate;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.SwingWorker;
 import javax.swing.table.DefaultTableModel;
 
 import org.bouncycastle.asn1.ASN1InputStream;
 import org.bouncycastle.asn1.ASN1Object;
 import org.bouncycastle.asn1.ASN1Sequence;
 import org.bouncycastle.asn1.ASN1Set;
 import org.bouncycastle.asn1.DERObject;
 import org.bouncycastle.asn1.pkcs.CertificationRequestInfo;
 import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
 import org.bouncycastle.jce.PKCS10CertificationRequest;
 import org.bouncycastle.openssl.PEMWriter;
 import org.jdesktop.swingx.JXTable;
 
 import pdfDrucker.PDFDrucker;
 
 import utils.DatFunk;
 import utils.INIFile;
 import utils.JCompTools;
 import utils.OOorgTools;
 
 import com.jgoodies.forms.builder.PanelBuilder;
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 import com.lowagie.text.pdf.AcroFields;
 import com.lowagie.text.pdf.PdfReader;
 import com.lowagie.text.pdf.PdfStamper;
 
 public class NebraskaTestPanel  extends JPanel implements ActionListener{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 3831067622472056656L;
 	JTextField[] tn1 = {null,null,null,null,null};
 	JTextField[] tn2 = {null,null,null,null,null};
 	JTextField[] tn3 = {null,null,null,null,null};
 	JButton[] but1 = {null,null,null,null,null,null,null};
 	JButton[] but2 = {null,null,null,null,null};
 
 	Vector<String>vecprax=new Vector<String>();
 	Vector<String>vecca=new Vector<String>();
 	public static String keystoreDir = Constants.KEYSTORE_DIR;
 	public static String praxisKeystore;
 	public static String caKeystore;
 	public static String praxisPassw;
 	public static String caPassw;
 	public static String annahmeKeyFile;
 	
 	public JXTable tabprax;
 	public JXTable tabca;
 	public MyCertTableModel tabmodprax;
 	public MyCertTableModel tabmodca;
 	
 	public KeyPair kpprax = null; 
 	public KeyPair kpca = null;
 	
 	public NebraskaTestPanel(){
 		super();                   //     1      2                3         4          5            6
 		FormLayout lay = new FormLayout("2dlu,fill:0:grow(0.5),2dlu,fill:0:grow(0.5),2dlu",
 	//    1     2          3          4     5
 		"5dlu,20dlu,fill:0:grow(0.5),5dlu,50dlu,5dlu");
 		CellConstraints cc = new CellConstraints();
 		setLayout(lay);
 		add(getTN1(),cc.xy(2, 3,CellConstraints.FILL,CellConstraints.FILL));
 		add(getTN2(),cc.xy(4, 3,CellConstraints.FILL,CellConstraints.FILL));
 		add(getButs1(),cc.xy(2, 5,CellConstraints.FILL,CellConstraints.FILL));
 		add(getButs2(),cc.xy(4, 5,CellConstraints.FILL,CellConstraints.FILL));
 		validate();
 	}
 	
 	private JPanel getTN1(){
 		FormLayout lay = new FormLayout("5dlu,right:max(50dlu;p),5dlu,p:g,5dlu",
 				"5dlu,p,5dlu,p,5dlu,p,5dlu,p,5dlu:g,75dlu");
 		CellConstraints cc = new CellConstraints();
 		PanelBuilder pb = new PanelBuilder(lay);
 		pb.addLabel("OU=IK-Alias ( z.B.: IK540840108)",cc.xy(2,2));
 		pb.add((tn1[0] = new JTextField(Constants.PRAXIS_OU_ALIAS)),cc.xy(4,2));
 		pb.addLabel("OU=Praxisname",cc.xy(2,4));
 		pb.add((tn1[1] = new JTextField(Constants.PRAXIS_OU_FIRMA)),cc.xy(4,4));
 		pb.addLabel("CN=Ansprechpartner",cc.xy(2,6));
 		pb.add((tn1[2] = new JTextField(Constants.PRAXIS_CN)),cc.xy(4,6));
 		pb.addLabel("Passwort (max. 6 Zeichen)",cc.xy(2,8));
 		pb.add((tn1[3] = new JTextField(Constants.PRAXIS_KS_PW)),cc.xy(4,8));
 		tabmodprax = new MyCertTableModel();
 		tabmodprax.setColumnIdentifiers(new String[] {"Alias","Zert oder Key?","CA-Root","Gültig bis","zurückgezogen"});
 		tabprax = new JXTable(tabmodprax);
 		JScrollPane jscr = JCompTools.getTransparentScrollPane(tabprax);
 		jscr.validate();
 		pb.add(jscr,cc.xyw(2,10,3,CellConstraints.FILL,CellConstraints.BOTTOM));
 		new SwingWorker<Void,Void>(){
 			@Override
 			protected Void doInBackground() throws Exception {
 				doFuelleTabelle(false);
 				return null;
 			}
 		}.execute();
 		pb.getPanel().validate();
 		return pb.getPanel();
 	}
 
 	private JPanel getTN2(){
 		FormLayout lay = new FormLayout("5dlu,right:max(50dlu;p),5dlu,p:g,5dlu",
 		//		 1    2  3   4  5   6  7   8  9   10  11    12
 				"5dlu,p,5dlu,p,5dlu,p,5dlu,p,5dlu,p,5dlu:g,75dlu");
 		CellConstraints cc = new CellConstraints();
 		PanelBuilder pb = new PanelBuilder(lay);
 		pb.getPanel().setBackground(Color.WHITE);
 		pb.addLabel("OU=IK-Alias ( z.B.: IK999999999)",cc.xy(2,2));
 		pb.add((tn2[0] = new JTextField(Constants.TEST_CA_OU_ALIAS)),cc.xy(4,2));
 		pb.addLabel("OU=Firma",cc.xy(2,4));
 		pb.add((tn2[1] = new JTextField(Constants.TEST_CA_OU_FIRMA)),cc.xy(4,4));
 		pb.addLabel("CN=Ansprechpartner",cc.xy(2,6));
 		pb.add((tn2[2] = new JTextField(Constants.TEST_CA_CN)),cc.xy(4,6));
 		pb.addLabel("O=CA-Organisation",cc.xy(2,8));
 		pb.add((tn2[3] = new JTextField(Constants.TEST_CA_O)),cc.xy(4,8));
 		pb.addLabel("Passwort (max. 6 Zeichen)",cc.xy(2,10));
 		pb.add((tn2[4] = new JTextField(Constants.TEST_CA_KS_PW)),cc.xy(4,10));
 		tabmodca = new MyCertTableModel();
 		tabmodca.setColumnIdentifiers(new String[] {"Alias","Zert oder Key?","CA-Root","Gültig bis","zurückgezogen"});
 		tabca = new JXTable(tabmodca);
 		JScrollPane jscr = JCompTools.getTransparentScrollPane(tabca);
 		jscr.validate();
 		pb.add(jscr,cc.xyw(2,12,3,CellConstraints.FILL,CellConstraints.BOTTOM));
 		new SwingWorker<Void,Void>(){
 			@Override
 			protected Void doInBackground() throws Exception {
 				doFuelleTabelle(true);
 				return null;
 			}
 		}.execute();
 		pb.getPanel().validate();
 		return pb.getPanel();
 	}
 	
 	private JPanel getButs1(){
 		FormLayout lay = new FormLayout("5dlu,p,2dlu,p,2dlu,p,2dlu,p,5dlu",
 		"5dlu,p,2dlu,p,5dlu");
 		CellConstraints cc = new CellConstraints();
 		PanelBuilder pb = new PanelBuilder(lay);
 		pb.add((but1[0] = macheBut("KStore gen.","kgen1")),cc.xy(2,2));
 		pb.add((but1[1] = macheBut("Reques. gen.","requgen1")),cc.xy(4,2));
 		pb.add((but1[2] = macheBut("ReqRepl. read","replread1")),cc.xy(6,2));
 		pb.add((but1[3] = macheBut("AnnKey read","annread1")),cc.xy(8,2));
 		pb.add((but1[4] = macheBut("Encode","enc1")),cc.xy(2,4));
 		pb.add((but1[4] = macheBut("Decode","dec1")),cc.xy(4,4));
 		pb.add((but1[5] = macheBut("Cert create","create1")),cc.xy(6,4));
 		pb.add((but1[6] = macheBut("SHA1-Test","sha1test1")),cc.xy(8,4));
 		pb.getPanel().validate();
 		return pb.getPanel();
 	}
 	private JPanel getButs2(){
 		FormLayout lay = new FormLayout("5dlu,p,2dlu,p,2dlu,p,2dlu,p,5dlu",
 		"5dlu,p,2dlu,p,5dlu");
 		CellConstraints cc = new CellConstraints();
 		PanelBuilder pb = new PanelBuilder(lay);
 		pb.add((but2[0] = macheBut("KStore gen.","kgen2")),cc.xy(2,2));
 		pb.add((but2[1] = macheBut("Request read / Reply generate","requread")),cc.xy(4,2));
 		pb.add((but2[2] = macheBut("save TestCase-settings","savetc")),cc.xy(4,4));
 		pb.add((but2[3] = macheBut("Zert.Antrag","certantrag")),cc.xy(2,4));
 		pb.getPanel().validate();
 		return pb.getPanel();
 
 	}	
 
 	public JButton macheBut(String titel,String cmd){
 		JButton but = new JButton(titel);
 		but.setActionCommand(cmd);
 		but.addActionListener(this);
 		return but;
 	}
 	public void doFuelleTabelle(boolean isRoot) throws Exception{
 		// Hier werden die beiden Tabellen mit den Angaben zu enthaltenen Zertifikaten gefüllt
 		String keystore = (isRoot ? "" : "");
 		String pw = (isRoot ? "" : "");
 		MyCertTableModel mod = (isRoot ? tabmodca : tabmodprax);
 		mod.setRowCount(0);
 		KeyStore store = BCStatics2.loadStore(Constants.KEYSTORE_DIR+File.separator+keystore, pw);
 		//Hier die Enumeration durch die Aliases und dann die Tabellen füllen
 	}
 	@Override
 	public void actionPerformed(ActionEvent arg0){
 		String cmd = arg0.getActionCommand();
 		setVecs();
 		try{
 			/***********Keystore anlegen**************/
 			if(cmd.equals("kgen1")){
 				Nebraska.jf.setCursor(new Cursor(Cursor.WAIT_CURSOR));
 				boolean geklappt = doKeystore(praxisPassw,false);
 				if(geklappt){
 					Nebraska.jf.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
 					JOptionPane.showMessageDialog(null, "Keystore wurde erzeugt");					
 				}
 			}
 			/*****Zertifikatsrequest* erzeugen*********/
 			if(cmd.equals("requgen1")){
 				doGenerateRequest();
 				JOptionPane.showMessageDialog(null, "ZertifikatsRequest wurde erzeugt");
 			}
 			/****CertifikatsRequestReply einlesen ****/
 			if(cmd.equals("replread1")){
 				doReadAndManageReply();
 				JOptionPane.showMessageDialog(null, "ZertifikatsReply wurde eingelesen");
 			}
 			/****Vergleicht den Fingerprint des Zerts der Datenbank mit dem der PEM-Datei ****/			
 			if(cmd.equals("sha1test1")){
 				doSHA1Test();
 			}
 			/*************/
 			if(cmd.equals("dec1")){
 				doVergleichen();
 			}
 
 			if(cmd.equals("enc1")){
 				doEncode();
 			}
 			if(cmd.equals("create1")){
 				doCertCreate();
 			}
 			if(cmd.equals("annread1")){
 				doAnnahmeReadAndStore();
 			}
 
 			
 			/***********Keystore f�r CA anlegen********/
 			if(cmd.equals("kgen2")){
 				doKeystore(caPassw,true);
 				JOptionPane.showMessageDialog(null, "Keystore wurde erzeugt");
 			}
 			/*****Zertifikatsrequest einlesen (CA)*****/
 			if(cmd.equals("requread")){
 				doRequestEinlesen();
 				JOptionPane.showMessageDialog(null, "ZertifikatsRequest wurde eingelesen und Reply wurde erzeugt");
 			}
 			if(cmd.equals("savetc")){
 				saveTestCase();
 			}
 			if(cmd.equals("certantrag")){
 				doCertAntrag();
 			}
 
 		}catch(Exception ex){
 			ex.printStackTrace();
 		}
 	}
 	private void doCertAntrag() throws Exception{
 		// Umbau zum test der MD5-Fingerprints
 		//ASN1InputStream ain = new ASN1InputStream(new FileInputStream(Constants.KEYSTORE_DIR+File.separator+"40091472.p10"));
 		/*
 		ASN1InputStream ain = new ASN1InputStream(new FileInputStream(Constants.KEYSTORE_DIR+File.separator+"54084010.p10"));
 		DERObject derob = ain.readObject();
 		PKCS10CertificationRequest csr = new PKCS10CertificationRequest(
             	  derob.getDEREncoded() );
 		System.out.println("Fingerprint von Certifikation-Request:"+BCStatics2.getMD5fromByte(derob.getDEREncoded()));
 		PublicKey key = csr.getPublicKey("BC");
 
 		System.out.println(key);
 		System.out.println("Fingerprint vom Public-Key: "+BCStatics2.getMD5fromByte(key.getEncoded()));
 		
 		
 		CertificationRequestInfo csrInfo = csr.getCertificationRequestInfo();
 		System.out.println("SubjectDN: "+csrInfo.getSubject());
 		System.out.println("SubjectDN: "+csrInfo.getSubjectPublicKeyInfo().getPublicKey());
 		System.out.println("SubjectDN: "+BCStatics2.getMD5fromByte(csrInfo.getSubjectPublicKeyInfo().getPublicKeyData().getBytes()));
 		*/
 		String outFile = null;
 		PdfReader reader = new PdfReader(Constants.KEYSTORE_DIR+File.separator+"vorlagen"+File.separator+"Zertifizierungsantrag.pdf");
 		outFile = Constants.KEYSTORE_DIR+File.separator+"vorlagen"+File.separator+"Zertifizierungsantrag"+DatFunk.sHeute()+".pdf"; 
 		FileOutputStream out = new FileOutputStream(outFile);
 		PdfStamper stamper = new PdfStamper(reader, out);
 		AcroFields form = stamper.getAcroFields();
 		Map fieldMap = form.getFields();
         Set keys = fieldMap.keySet();
         for (Iterator it = keys.iterator(); it.hasNext();){
             String fieldName = (String) it.next();
             AcroFields.Item field = (AcroFields.Item) fieldMap.get(fieldName);
         	System.out.println(fieldName);
             if(fieldName.equals("IK")){
             	//fieldMap.put("IK", "540840108");
             	//System.out.println("Feld IK ersetzt");
             	form.setField(fieldName, "540840108");
             }
         }
         stamper.setFormFlattening(true);
         stamper.close();
         reader.close();
         //PDFDrucker.setup(outFile);
         
 	}
 	private void saveTestCase(){
 		INIFile inif = new INIFile(Constants.INI_FILE);
 		// In der Entwicklungsversion werden verschiedene Items nicht gespeichert z.b. CE oder PRAXIS_O
 		inif.setStringProperty("Praxis", "PRAXIS_OU_ALIAS", tn1[0].getText().trim(),null);
 		inif.setStringProperty("Praxis", "PRAXIS_OU_FIRMA", tn1[1].getText().trim(),null);
 		inif.setStringProperty("Praxis", "PRAXIS_CN", tn1[2].getText().trim(),null);
 		inif.setStringProperty("Praxis", "PRAXIS_KS_PW", tn1[3].getText().trim(),null);
 		inif.setStringProperty("TestCA", "TEST_CA_OU_ALIAS", tn2[0].getText().trim(),null);
 		inif.setStringProperty("TestCA", "TEST_CA_OU_FIRMA", tn2[1].getText().trim(),null);
 		inif.setStringProperty("TestCA", "TEST_CA_O", tn2[3].getText().trim(),null);
 		inif.setStringProperty("TestCA", "TEST_CA_CN", tn2[2].getText().trim(),null);
 		inif.setStringProperty("TestCA", "TEST_CA_KS_PW", tn2[4].getText().trim(),null);
 		inif.save();
 		JOptionPane.showMessageDialog(null, "Die aktuellen Angaben zu Praxis und CA wurden gesichert in Datei:\n"+Constants.INI_FILE);
 		
 	}
 	public void doVergleichen()throws Exception{
 		BCStatics2.providerTest();
 		KeyStore store = BCStatics2.loadStore(keystoreDir + File.separator +"540840108",praxisPassw);
 		X509Certificate storedcert = (X509Certificate) store.getCertificate("IK540840108");
 		X509Certificate cert = BCStatics2.readSingleCert(keystoreDir);
 		System.out.println(storedcert);
 		System.out.println(cert);
 	}
 	public void doSHA1Test() throws Exception{
 		PublicKey pairDb = null;
 		KeyPair pairPem = null;
 		PublicKey pairSt = null;
 		
 		X509Certificate cert = null;
 		X509Certificate storedcert = null;
 		
 		cert = BCStatics2.readSingleCert(keystoreDir);
 		pairDb = cert.getPublicKey();
 		
 		KeyStore store = BCStatics2.loadStore(keystoreDir + File.separator +"540840108",praxisPassw);
 		storedcert = (X509Certificate) store.getCertificate("IK540840108");
 		pairSt = storedcert.getPublicKey();
 		
 		pairPem = BCStatics2.getBothFromPem(keystoreDir + File.separator + "540840108");
 		
 		System.out.println("Public-Key aus .p7b-File    = "+pairDb);
 		System.out.println("Public-Key im PEM-File      = "+pairPem.getPublic());
 		System.out.println("Public-Key KeyStore-File    = "+pairSt);
 		System.out.println("**************** SHA1 Fingerprints ******************");
 		System.out.println("SHA1-.p7b-File      "+BCStatics2.getSHA1fromByte(pairDb.getEncoded()));
 		System.out.println("SHA1-PEM-File       "+BCStatics2.getSHA1fromByte(pairPem.getPublic().getEncoded()));
 		System.out.println("SHA1-Keystore-File  "+BCStatics2.getSHA1fromByte(pairSt.getEncoded()));
 		System.out.println("**************** SHA1 Fingerprint Zertifikat im KeyStore ******************");
 		System.out.println("SHA1-Zertifikat p7b File  "+BCStatics2.getSHA1(cert));
 		System.out.println("SHA1-KeyStore-Zertifikat  "+BCStatics2.getSHA1(storedcert));
 	}
 	
 	public void doAnnahmeReadAndStore() throws Exception{
 		annahmeKeyFile = "";
 		int anzahl = BCStatics2.readMultipleAnnahme(keystoreDir,praxisPassw,vecprax.get(0).replace("IK", ""),true,null);
 		System.out.println("Es befinden sich "+anzahl+" Zertifikate in der Datei");
 		if(anzahl <= 0){
 			return;
 		}
 		X509Certificate[] chain = new X509Certificate[anzahl];
 		anzahl = BCStatics2.readMultipleAnnahme(keystoreDir,praxisPassw,vecprax.get(0).replace("IK", ""),false,chain);
 		for(int i = 0; i < anzahl;i++){
 			//System.out.println("***************************");
 			//System.out.println("Zertifikat Nr. "+i+" = "+chain[i].getBasicConstraints());
 			//System.out.println(chain[i]);
 		}
 		CertificateFactory fact = CertificateFactory.getInstance("X.509","BC");
 		CertPath certPath = fact.generateCertPath(Arrays.asList(chain));
 
 		byte[] encoded = certPath.getEncoded("PEM");
 		//System.out.println(new String(encoded));
 		CertPath newCertPath = fact.generateCertPath(new ByteArrayInputStream(encoded), "PEM");
 		byte[] newEncoded = newCertPath.getEncoded("PEM");
 		List<X509Certificate> lcert = (List<X509Certificate>) newCertPath.getCertificates();
 
 		for(int i = 0; i < anzahl;i++){
 			System.out.println("Zertifikat Nr. "+i+" = "+lcert.get(i).getBasicConstraints());
 			System.out.println(lcert.get(i));
 		}
 
 		//System.out.println(new String(newEncoded));
 		if(newCertPath.equals(certPath)){
 			System.out.println("Bestehender Cert-Pfad war bereits korrekt");
 		}else{
 			System.out.println("Cert-Pfad wurde generiert");
 		}
 	}
 	public void doCertCreate() throws Exception{
 		KeyPair kpprax = null;
 		String datei = keystoreDir + File.separator +vecprax.get(0).replace("IK", ""); 
 		File f = new File(datei+".prv");
 		if(f.exists()){
 			KeyPair kp = BCStatics2.getBothFromPem(datei);
 			kpprax = kp;
 			System.out.println("SHA1 von PrivateKey (aus Pem-File) = "+BCStatics2.getSHA1fromByte(kpprax.getPrivate().getEncoded()));
 			System.out.println("SHA1 von PublicKey (aus Pem-File) "+BCStatics2.getSHA1fromByte(kpprax.getPublic().getEncoded()));
 		}else if(kpprax==null){
 			kpprax = BCStatics2.generateRSAKeyPair();
 		}
 		X509Certificate[] chain = {BCStatics2.generateV3Certificate(kpprax,vecprax,vecca)};
 		KeyStore store = BCStatics2.loadStore(datei, praxisPassw);
 		store.setKeyEntry(vecprax.get(0), kpprax.getPrivate(),praxisPassw.toCharArray(), chain);
 		BCStatics2.saveStore(store, praxisPassw, vecprax.get(0).replace("IK", ""));
 		//BCStatics2.certToFile(cert, cert.getEncoded(), keystoreDir+ File.separator +"test");
 		
 
 		//System.out.println(cert);
 		//FileStatics.BytesToFile(cert.getEncoded(), new File(keystoreDir+ File.separator +"test.p7b"));
 		
 	}
 	public void doEncode() throws Exception{
 		BCStatics2.verschluesseln(vecprax.get(0),keystoreDir + File.separator +vecprax.get(0).replace("IK", ""),praxisPassw);
 	}
 	public void doReadAndManageReply() throws Exception{
 		BCStatics2.readCertReply(keystoreDir,vecprax.get(0).replace("IK", ""),praxisPassw);
 	}
 	public void doRequestEinlesen()throws Exception{
 		BCStatics2.providerTest();
 		String request = FileStatics.fileChooser(keystoreDir,"Request (.p10) öffnen");
 		if(request.trim().equals("")){return;}
 		if(request.indexOf(".p10") < 0){return;}
 		setVecs();
 	}
 	/******************************************************************************/
 	
 	public void doGenerateRequest() throws Exception{
 		Nebraska.jf.setCursor(new Cursor(Cursor.WAIT_CURSOR));
 		Nebraska.hmZertifikat.clear();
 		String datei = keystoreDir + File.separator +vecprax.get(0).replace("IK", ""); 
 		File f = new File(datei+".prv");
 		if(f.exists()){
 			KeyPair kp = BCStatics2.getBothFromPem(datei);
 			kpprax = kp;
 		}else if(kpprax==null){
 			kpprax = BCStatics2.generateRSAKeyPair();
 		}
 		PKCS10CertificationRequest request = BCStatics2.generateRequest(kpprax,vecprax,vecca);
 		request.verify(kpprax.getPublic(),Constants.SECURITY_PROVIDER);
 		f = new File(datei+".p10");
 		FileOutputStream fos = new FileOutputStream(f);
 		fos.write(request.getEncoded());
 		fos.flush();
 		fos.close();
 		FileWriter file = new FileWriter(new File(datei+".pem"));
         PEMWriter pemWriter = new PEMWriter(file);
         pemWriter.writeObject(request);
         pemWriter.close();
         file.close();
         /************/
         CertificationRequestInfo info = request.getCertificationRequestInfo();
         System.out.println(info.getSubject());
         System.out.println(info.getDEREncoded());
         System.out.println(info.getDERObject());
         System.out.println(info.getVersion());
         System.out.println(info.getDERObject().toASN1Object());
         System.out.println(info.getAttributes());
         ASN1Set attrib = info.getAttributes();
         System.out.println("Attribute = "+attrib);
         ASN1Object asno =  (ASN1Object) info.getDERObject().toASN1Object();
         ASN1Sequence aseq = ASN1Sequence.getInstance(asno);
         System.out.println(aseq);
         System.out.println(aseq.size());
         SubjectPublicKeyInfo spub = null;
         for(int i = 0; i < aseq.size();i++){
         	System.out.println("Objec Nr."+i+" aus der ASN1-Struktur = "+aseq.getObjectAt(i));
         	if(aseq.getObjectAt(i) instanceof SubjectPublicKeyInfo){
         		spub = (SubjectPublicKeyInfo) aseq.getObjectAt(i);
         		System.out.println("Public Key des Requests = "+spub.getPublicKeyData());
         		System.out.println("SHA1-Hash aus dem PubKey des Requests = "+BCStatics2.getSHA1fromByte(spub.getPublicKeyData().getEncoded()));
         	}
         }
         System.out.println("Origianl Public Key des PEM-Files = "+kpprax.getPublic());
         System.out.println("SHA-1 Fingerprint des Zerifikates = "+BCStatics2.getSHA1fromByte(FileStatics.BytesFromFile(new File(keystoreDir + File.separator +vecprax.get(0).replace("IK", "")+".p7b"))));
         System.out.println("**********Request Start************\n");
         System.out.println(request);
         System.out.println("**********Request Ende*************\n");
         System.out.println("**********SHA-1 des PublicKey*************");
         System.out.println("SHA-1-Fingerprint PublicKey  ="+BCStatics2.getSHA1fromByte(kpprax.getPublic().getEncoded()));
         System.out.println("**********SHA-1 des Requests*************");
         System.out.println("SHA-1-Fingerprint CertRequest="+BCStatics2.getSHA1fromByte(request.getEncoded()));
         /*********Test eines neuen Schl�ssels******///
         //BCStatics.machePublicKey(kpprax.getPublic(), cert1);
         
         
         Nebraska.hmZertifikat.put("<Ikpraxis>",vecprax.get(0));
         Nebraska.hmZertifikat.put("<Issuerc>","C=DE");
         Nebraska.hmZertifikat.put("<Issuero>","O="+vecca.get(3));
         Nebraska.hmZertifikat.put("<Subjectc>","C=DE");
         Nebraska.hmZertifikat.put("<Subjecto>","O="+vecca.get(3));
         Nebraska.hmZertifikat.put("<Subjectou1>","OU="+vecprax.get(1));
         Nebraska.hmZertifikat.put("<Subjectou2>","OU="+vecprax.get(0));
         Nebraska.hmZertifikat.put("<Subjectcn>","CN="+vecprax.get(2));
         Nebraska.hmZertifikat.put("<Algorithm>",kpprax.getPublic().getAlgorithm());
         
         String sha1 = BCStatics2.getSHA1fromByte(spub.getPublicKeyData().getBytes());
         //String sha1 = BCStatics2.getSHA1fromByte(kpprax.getPublic().getEncoded());
         Nebraska.hmZertifikat.put("<Sha1publickey>",BCStatics2.macheHexDump(sha1, 20," "));
         
         String md5 = BCStatics2.getMD5fromByte(spub.getPublicKeyData().getBytes());
         //String md5 = BCStatics2.getMD5fromByte(kpprax.getPublic().getEncoded());
         Nebraska.hmZertifikat.put("<Md5publickey>",BCStatics2.macheHexDump(md5, 20," "));
         
         sha1 = BCStatics2.getSHA1fromByte(request.getEncoded());
         Nebraska.hmZertifikat.put("<Sha1certificate>",BCStatics2.macheHexDump(sha1, 20," "));
 
         md5 = BCStatics2.getMD5fromByte(request.getEncoded());
         Nebraska.hmZertifikat.put("<Md5certificate>",BCStatics2.macheHexDump(md5, 20," "));
 
         
         java.security.interfaces.RSAPublicKey pub =
 			(java.security.interfaces.RSAPublicKey)kpprax.getPublic();
         String hexstring = new BigInteger(pub.getModulus().toByteArray()).toString(16);
         System.out.println("Hexstring = "+hexstring);
         String modulus = BCStatics2.macheHexDump(hexstring, 20," ");
         Nebraska.hmZertifikat.put("<Modulus>",modulus);
         
         hexstring = new BigInteger(pub.getPublicExponent().toByteArray()).toString(16);
         Nebraska.hmZertifikat.put("<Exponent>",(hexstring.length()==5 ? "0"+hexstring : hexstring  ));
 		OOorgTools.starteStandardFormular(keystoreDir + File.separator +"vorlagen"+File.separator+"ZertBegleitzettel.ott", null);
 		Nebraska.jf.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
 	}            
 
 	
 	/**
 	 * @throws Exception ************************/
 	private boolean doKeystore(String passw,boolean isRoot) throws Exception{
 		String datei = "";
 		String keystore = "";
 		if(isRoot){
 			String testdatei = keystoreDir+ File.separator +vecca.get(0).replace("IK", ""); 
 			File f = new File(testdatei+".prv");
 			if(f.exists()){
 				KeyPair kp = BCStatics2.getBothFromPem(datei);
 				kpca = kp;
 			}else if(kpca==null){
 					kpca = BCStatics2.generateRSAKeyPair();
 			}
 			datei =tn2[0].getText().trim().replace("IK", "");
 			keystore =  keystoreDir+ File.separator +datei+".p12";
 			if(datei.trim().equals("")){
 				Nebraska.jf.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
 				JOptionPane.showMessageDialog(null,"Kein Alias angegeben");
 				return false;
 			}
 		}else{
 			String testdatei = keystoreDir + File.separator +vecprax.get(0).replace("IK", ""); 
 			File f = new File(testdatei+".prv");
 			if(f.exists()){
				KeyPair kp = BCStatics2.getBothFromPem(testdatei);
 				kpprax = kp;
 			}else if(kpprax==null){
 				kpprax = BCStatics2.generateRSAKeyPair();
 			}
 			datei =tn1[0].getText().trim().replace("IK", "");
 			keystore =  keystoreDir + File.separator +tn1[0].getText().trim().replace("IK", "")+".p12";
 			if(tn1[0].getText().trim().equals("")){
 				Nebraska.jf.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
 				JOptionPane.showMessageDialog(null,"Kein Alias angegeben");
 				return false;
 			}
 		}
 		if(passw.equals("")){
 			Nebraska.jf.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
 			JOptionPane.showMessageDialog(null,"Kein Passwort angegeben");
 			return false;
 		}
 		File f = new File(keystoreDir);
 		if(!f.isDirectory()){f.mkdir();}
 		f = new File(keystore);
 		if(!f.exists()){
 			System.out.println("Passwort f�r Keystore = "+passw);
 			BCStatics2.createKeyStore(datei,passw,isRoot,vecprax,vecca,(isRoot ? kpca : kpprax));return true;}
 		else{
 			Nebraska.jf.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
 			Message("KeystoreFile "+keystore+" existiert bereits");
 			return false;
 		}
 	}
 
 	private void Message(String msg){
 		JOptionPane.showMessageDialog(null, msg);
 	}
 	
 	private void setVecs(){
 		vecprax.clear();vecca.clear();
 		for(int i = 0;i<5;i++){
 			vecca.add(tn2[i].getText());
 			if(i<4){
 				vecprax.add(tn1[i].getText());
 			}
 		}
 		praxisPassw = vecprax.get(3);
 		caPassw = vecca.get(4);
 	}
 
 	class MyCertTableModel extends DefaultTableModel{
 		   /**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		public Class getColumnClass(int columnIndex) {
 			   if(columnIndex==1 ){
 				   return JLabel.class;}
 			   else{
 				   return String.class;
 			   }
 		}
 
 		public boolean isCellEditable(int row, int col) {
 		          return false;
         }
 	}
 
 }
