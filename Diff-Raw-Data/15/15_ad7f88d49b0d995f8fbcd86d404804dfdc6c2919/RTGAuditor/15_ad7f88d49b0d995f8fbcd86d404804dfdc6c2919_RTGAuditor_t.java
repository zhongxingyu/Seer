 package org.essevo.remotegrity.auditor;
 
 
 import javax.crypto.spec.SecretKeySpec;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.DocumentBuilder; 
 import javax.xml.parsers.ParserConfigurationException;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Calendar;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 
 public class RTGAuditor {
 
 
 	private int numberOfAuthCards;
 	public String electionConstant;
 	private String path;
 	private String commitmentFile;
 	private String openedFile;
 
 	
 
 	/**
 	 * If preAudit is true then only preAudit files are parsed
 	 * @param path - path to the folder containing files
 	 * @param preAudit
 	 */
 	public RTGAuditor(String electionConstant, String path, String commitmentFile, String openedFile )
 	{
 		this.electionConstant = new String(electionConstant);
 		this.path = new String(path);
 		this.commitmentFile = new String(commitmentFile);
 		this.openedFile = new String(openedFile);		
 	}
 
 	/**
 	 * Checks if the commitment is correct
 	 * @param cardOpen - element of the OPENEd authentication card
 	 * @param cardComm - element of the Commitment authenticaiton card
	 * @param field - name of the field to be verified: lockInA/lockInB/systemOTP/OTP
 	 * @return - true if commitment is correct
 	 * @throws Exception
 	 */
	protected boolean verifyCommitment(Element cardOpen, Element cardComm, String field) { 
		for (int i = 0; i < cardOpen.getElementsByTagName(field).getLength(); i++) {
			if (!verifyCommitment(((Element) cardOpen.getElementsByTagName(field).item(i)).getFirstChild().getNodeValue(),
					((Element) cardOpen.getElementsByTagName(field).item(i)).getAttribute("salt"), 
					((Element) cardComm.getElementsByTagName(field).item(i)).getFirstChild().getNodeValue())) 
					return false;
				} 
		return true;
 	}
 	
 	/**
 	 * Checks if given commitment to given message was created using given salt
 	 * @param message -
 	 * @param salt
 	 * @param commitment
 	 * @return
 	 * @throws Exception
 	 */
 	protected boolean verifyCommitment(String message, String salt, String commitment) {// throws Exception {
 		byte[] newCom;
 		try {
 			newCom = SecurityUtil.getCommitment(
 					new SecretKeySpec(Base64.decode(salt),"AES"),
 							this.electionConstant.getBytes(), 
 							message.getBytes());
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 	
 	if (commitment.compareTo(Base64.encode(newCom)) == 0 ) {
 		return true;
 	} else {
 		System.out.println("\nInconsistency found for:" + message);
 		return false;
 	}
 
 	}
 	
 	protected boolean verifyData() //String path, String commitmentFile, String openedFile)
 	{
 		System.out.println("\tStep " + ". Reading data from: "+ this.commitmentFile);
 		
 		DocumentBuilderFactory factoryComm = DocumentBuilderFactory.newInstance();
 		
 		DocumentBuilder docBuilderComm;
 		try {
 			docBuilderComm = factoryComm.newDocumentBuilder();
 		} catch (ParserConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 		
 		Document docEComm;
 		try {
 			docEComm = docBuilderComm.parse(new BufferedInputStream(new FileInputStream(this.path + this.commitmentFile)));
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			//e.printStackTrace();
 			System.out.println("File with commitments not found");
 			return false;
 		} catch (SAXException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 		Element mainElComm = docEComm.getDocumentElement();
 		NodeList nlACComm = mainElComm.getElementsByTagName("ac");
 
 		this.numberOfAuthCards = nlACComm.getLength();
 		System.out.println("\t\tNumber of authentication cards: "+this.numberOfAuthCards);
 
 
 		
 		System.out.println("\tStep " + ". Reading data from: "+this.openedFile);
 		
 		
 		DocumentBuilderFactory factoryOpen = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilderOpen;
 		try {
 			docBuilderOpen = factoryOpen.newDocumentBuilder();
 		} catch (ParserConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			System.out.println("File with open commitments not found.");
 			return false;
 		}
 		
 		Document docEOpen;
 		try {
 			docEOpen = docBuilderOpen.parse(new BufferedInputStream(new FileInputStream(this.path + this.openedFile)));
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			System.out.println("File with open commitments not found.");
 			return false;
 		} catch (SAXException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 		Element mainElOpen = docEOpen.getDocumentElement();
 		NodeList nlACOpen = mainElOpen.getElementsByTagName("ac");
 
 		this.numberOfAuthCards = nlACOpen.getLength();
 		System.out.println("\t\tNumber of authentication cards: "+this.numberOfAuthCards);
 
 			
 			/**
 			 * Goes through both xml files and verifies commitments 
 			 * - in fact it goes  first through a file with opened commitments
 			 * and reads every <ac> element. It learns the id of the entry
 			 * and then finds the corresponding <ac> (with the same id) in
 			 * file with commitments. 
 			 */
 		
 			System.out.print("\n\n\tVerification starts: ");
 			for (int authCardRow = 0; authCardRow < nlACOpen.getLength(); authCardRow++) {
 				if (authCardRow % (nlACOpen.getLength() / 20) == 0) { System.out.print("#"); }
 				
 				Element authCardOpen = (Element) nlACOpen.item(authCardRow);
 				
 				int cardID = Integer.parseInt(authCardOpen.getAttribute("id"));
 				//System.out.println("r: "+authCardRow + " " + cardID);
 				//String salt = authCardOpen.getAttribute("salt");
 				//String serial = authCardOpen.getAttribute("serial");
 				
 				/**
 				 * this was previously - when everything was opened
 				 *
 				 */
 				 //Element authCardComm = (Element) nlACComm.item(authCardRow); 
 				Element authCardComm = (Element) nlACComm.item(cardID);
 				//String commitmentTOSerial = authCardComm.getAttribute("serial");
 				//System.out.println( cardID + "\t" + salt);
 				//System.out.println("\t" + serial);
 				//System.out.println("\tcomm: " + commitmentTOSerial);
 				
 				/**
 				 * verification of commitment to serial
 				 */
 				if (!verifyCommitment(
 						authCardOpen.getAttribute("serial"), 
 						authCardOpen.getAttribute("salt"), 
 						authCardComm.getAttribute("serial"))) {
 					System.out.println("Commitments do not match - serial!");
 					return false;
 				}
 
 
 				if (!verifyCommitment(authCardOpen, authCardComm, "lockInA")) {
 					System.out.println("Commitments do not match - lockInA!");
 					return false;
 				}
 
 				if (!verifyCommitment(authCardOpen, authCardComm, "lockInB")) {
 					System.out.println("Commitments do not match - lockInB!");
 					return false;
 				}
 				
 				if (!verifyCommitment(authCardOpen, authCardComm, "systemOTP")) {
 					System.out.println("Commitments do not match - systemOTP!");
 					return false;
 				}
 
 				/** 
 				 * verification of OTPs
 				 */
 				NodeList nlOTPopen = authCardOpen.getElementsByTagName("otps");
 				NodeList nlOTPcomm = authCardComm.getElementsByTagName("otps");
 				
 				for (int otpNo = 0; otpNo < nlOTPopen.getLength(); otpNo++) {
 					//System.out.println("OTP: " + otpNo);
 					Element OTPopen = (Element) nlOTPopen.item(otpNo);
 					Element OTPcomm = (Element) nlOTPcomm.item(otpNo);
 					if (!verifyCommitment(OTPopen, OTPcomm, "otp")) {
 						System.out.println("Commitments do not match - OTP: cardID=" + cardID + " OTP: " + otpNo);
 						return false;
 					}
 				}
 
 				
 				
 				
 			}
 			
 			
 			System.out.println();
 			return true;
 			
 		}
 	
 	/**
 	 * Method that runs post-audit check of table RS, RM and RF
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		String electionConstant;
 		String path;
 		String commFile;
 		String openFile;
 		
 		long startTime = System.currentTimeMillis();
 		
 		
 		if (args.length > 0) {
 			path = new String(args[0]);
 		} else {
 			path = "remotegrity/";
 		}
 		
 		if (args.length > 1) {
 			commFile = new String(args[1]);
 		} else {
 			commFile = "acards-comm.xml";
 		}
 		
 		if (args.length > 2) {
 			openFile = new String(args[2]);
 		} else {
 			openFile = "acards-open.xml";
 		}
 
 		if (args.length > 3 ) {
 			electionConstant = new String(args[3]);
 		} else {
 			electionConstant = "Stala16znakowa12";
 		}
 		System.out.println("\n\tRemotegrity Auditor");
 		System.out.println("\tVersion 1.0.13. October 13 2011");
 		System.out.println("\twritten by Filip Zagorski\n");
 		System.out.println("\tAudit start: "+ Calendar.getInstance().getTime().toString());
 		
 		System.out.println("\tPath set to: "+path + "");
 		System.out.println("\tElectionConst: "+electionConstant+ "\n");
 		
 		try {
 			RTGAuditor rtg = new RTGAuditor(electionConstant, path, commFile, openFile);
 			
 			
 			if (rtg.verifyData())		
 			{
 				System.out.println("\n\tAudit end: "+ Calendar.getInstance().getTime().toString());
 				System.out.println("\tAudit last: " + ((System.currentTimeMillis() - startTime)/1000 + 1) + " sec.");
 				System.out.println("\n\tRemotegrity Audit result: OK ");
 			} else {
 				System.out.println("\n\tAudit end: "+ Calendar.getInstance().getTime().toString());
 				System.out.println("\n\tRemotegrityi Audit result: FAILED !!!!!!! ");
 			}
 			
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 			System.exit(1);
 		}
 		
 	}
 
 }
