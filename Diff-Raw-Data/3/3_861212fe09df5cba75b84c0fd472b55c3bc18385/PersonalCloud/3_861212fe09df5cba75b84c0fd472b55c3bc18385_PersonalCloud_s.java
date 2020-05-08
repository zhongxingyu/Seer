 package clouds.client.basic;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.UUID;
 
 import com.ibm.icu.util.StringTokenizer;
 
 import xdi2.client.XDIClient;
 import xdi2.client.exceptions.Xdi2ClientException;
 import xdi2.client.http.XDIHttpClient;
 import xdi2.core.ContextNode;
 import xdi2.core.Graph;
 import xdi2.core.Literal;
 import xdi2.core.Relation;
 import xdi2.core.constants.XDIAuthenticationConstants;
 import xdi2.core.exceptions.Xdi2ParseException;
 import xdi2.core.features.nodetypes.XdiAttributeSingleton;
 import xdi2.core.features.nodetypes.XdiPeerRoot;
 import xdi2.core.features.nodetypes.XdiValue;
 import xdi2.core.impl.json.memory.MemoryJSONGraphFactory;
 import xdi2.core.impl.memory.MemoryGraph;
 import xdi2.core.io.XDIWriterRegistry;
 import xdi2.core.util.iterators.ReadOnlyIterator;
 import xdi2.core.xri3.XDI3Segment;
 import xdi2.core.xri3.XDI3Statement;
 import xdi2.core.xri3.XDI3SubSegment;
 import xdi2.discovery.XDIDiscoveryClient;
 import xdi2.discovery.XDIDiscoveryResult;
 import xdi2.messaging.GetOperation;
 import xdi2.messaging.Message;
 import xdi2.messaging.MessageEnvelope;
 import xdi2.messaging.MessageResult;
 
 public class PersonalCloud {
 
 	public static XDI3Segment XRI_S_DEFAULT_LINKCONTRACT = XDI3Segment
 			.create("$do");
 
 	public static String DEFAULT_REGISTRY_URI = "http://mycloud.neustar.biz:12220/";
 	public static String WEBAPP_BASE_URI = "http://mycloud.neustar.biz:8080/myapp/personalclouds/";
 	//public static String WEBAPP_BASE_URI = "http://ACHOWDHU-LTW7.cis.neustar.com:8080/myapp/personalclouds/";
 	private String secretToken = null;
 	private XDI3Segment linkContractAddress = null;
 
 	private XDI3Segment cloudNumber = null;
 	private XDI3Segment cloudName = null;
 	private XDI3Segment senderCloudNumber = null;
 	private String registryURI = null;
 	private String cloudEndpointURI = null;
 	private ProfileInfo profileInfo = null;
 	private Hashtable<String, ContactInfo> addressBook = new Hashtable<String, ContactInfo>();
 
 	private String sessionId = null;
 
 	/*
 	 * factory methods for opening personal clouds
 	 */
 
 	/**
 	 * 
 	 * @param cloudNameOrCloudNumber
 	 * @param secretToken
 	 * @param linkContractAddress
 	 * @param regURI
 	 * @return
 	 */
 	public static PersonalCloud open(XDI3Segment cloudNameOrCloudNumber,
 			String secretToken, XDI3Segment linkContractAddress, String regURI,
 			String session) {
 
 		// like My Cloud Sign-in in clouds.projectdanbe.org
 		// 1. discover the endpoint
 		// 2. Load profile if available
 		PersonalCloud pc = new PersonalCloud();
 		XDIHttpClient httpClient = null;
 		if (regURI != null && regURI.length() > 0) {
 			httpClient = new XDIHttpClient(regURI);
 			pc.registryURI = regURI;
 		} else {
 			httpClient = new XDIHttpClient(DEFAULT_REGISTRY_URI);
 			pc.registryURI = DEFAULT_REGISTRY_URI;
 		}
 		XDIDiscoveryClient discovery = new XDIDiscoveryClient();
 		discovery.setRegistryXdiClient(httpClient);
 		try {
 			ArrayList<XDI3Segment> endpointUriTypes = new ArrayList<XDI3Segment>();
 			endpointUriTypes.add(XDI3Segment.create("$xdi"));
 			XDIDiscoveryResult discoveryResult = discovery
 					.discoverFromRegistry(cloudNameOrCloudNumber,
 							endpointUriTypes
 									.toArray(new XDI3Segment[endpointUriTypes
 											.size()]));
 			// if the cloudName or cloudNumber is not registered in the
 			// Registry, then return null
 			if (discoveryResult.getCloudNumber() == null) {
 				return null;
 			}
 
 			pc.cloudNumber = discoveryResult.getCloudNumber();
 			pc.cloudEndpointURI = discoveryResult.getXdiEndpointUri();
 
 		} catch (Xdi2ClientException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		} finally {
 			httpClient.close();
 		}
 
 		pc.linkContractAddress = linkContractAddress;
 		pc.secretToken = secretToken;
 		pc.senderCloudNumber = pc.cloudNumber;
 		pc.sessionId = session;
 		// System.out.println(pc.toString());
 		// pc.getProfileInfo();
 		// pc.createDefaultLinkContracts();
 		// if (session == null || session.isEmpty()) {
 		// String sessionId = pc.cloudNumber + "[+session]" + "!:uuid:"
 		// + UUID.randomUUID().toString() ;
 		// long sessionValidityPeriod = Calendar.getInstance().getTimeInMillis()
 		// + 86400*1000;
 		// String expirationDate = sessionId + "$not$valid$after<$t>&/&/"
 		// + sessionValidityPeriod + "";
 		// ArrayList<XDI3Statement> setSessionId = new
 		// ArrayList<XDI3Statement>();
 		// setSessionId.add(XDI3Statement.create(expirationDate));
 		// pc.setXDIStmts(setSessionId);
 		// } else {
 		// pc.sessionId = session;
 		// }
 		return pc;
 	}
 /*
  * Under construction
  */
 	public String createNewSession(int durationInSecs) {
 		String sessionId = this.cloudNumber + "[+session]" + "!:uuid:"
 				+ UUID.randomUUID().toString();
 		long sessionValidityPeriod = Calendar.getInstance().getTimeInMillis() + durationInSecs * 1000;
 		String expirationDate = sessionId + "$not$valid$after<$t>&/&/"
 				+ sessionValidityPeriod + "";
 		ArrayList<XDI3Statement> setSessionId = new ArrayList<XDI3Statement>();
 		setSessionId.add(XDI3Statement.create(expirationDate));
 		this.setXDIStmts(setSessionId);
 		this.sessionId = sessionId;
 		return sessionId;
 
 	}
 
 	public static PersonalCloud open(XDI3Segment cloudNameOrCloudNumber,
 			String secretToken, XDI3Segment linkContractAddress, String regURI) {
 		return PersonalCloud.open(cloudNameOrCloudNumber, secretToken,
 				linkContractAddress, regURI, null);
 	}
 
 	@Override
 	public String toString() {
 
 		StringBuffer str = new StringBuffer();
 		str.append("\n");
 		str.append("CloudNumber\t:\t" + cloudNumber);
 		str.append("\n");
 		str.append("registryURI\t:\t" + registryURI);
 		str.append("\n");
 		try {
 			str.append("Cloud endpoint URI\t:\t"
 					+ URLDecoder.decode(cloudEndpointURI, "UTF-8"));
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			str.append("Cloud endpoint URI\t:\tnull");
 			e.printStackTrace();
 		}
 		str.append("\n");
 		str.append("Link Contract Address\t:\t" + linkContractAddress);
 		str.append("\n");
 
 		return str.toString();
 
 	}
 
 	/**
 	 * Open a peer cloud
 	 * 
 	 * @param cloudNameOrCloudNumber
 	 *            : The cloudName/Number for the peer cloud
 	 * @param senderCN
 	 *            : Messages will have this cloudNumber as source
 	 * @param linkContractAddress
 	 * @param regURI
 	 * @return
 	 */
 	public static PersonalCloud open(XDI3Segment cloudNameOrCloudNumber,
 			XDI3Segment senderCN, XDI3Segment linkContractAddress, String regURI) {
 
 		// like My Cloud Sign-in in clouds.projectdanbe.org
 		// 1. discover the endpoint
 		// 2. test if the secret token is correct by sending a test message
 		PersonalCloud pc = new PersonalCloud();
 		XDIHttpClient httpClient = null;
 		if (regURI != null && regURI.length() > 0) {
 			httpClient = new XDIHttpClient(regURI);
 			pc.registryURI = regURI;
 		} else {
 			httpClient = new XDIHttpClient(DEFAULT_REGISTRY_URI);
 			pc.registryURI = DEFAULT_REGISTRY_URI;
 		}
 		XDIDiscoveryClient discovery = new XDIDiscoveryClient();
 
 		discovery.setRegistryXdiClient(httpClient);
 		try {
 			ArrayList<XDI3Segment> endpointUriTypes = new ArrayList<XDI3Segment>();
 			endpointUriTypes.add(XDI3Segment.create("$xdi"));
 			XDIDiscoveryResult discoveryResult = discovery
 					.discoverFromRegistry(cloudNameOrCloudNumber,
 							endpointUriTypes
 									.toArray(new XDI3Segment[endpointUriTypes
 											.size()]));
 			// if the cloudName or cloudNumber is not registered in the
 			// Registry, then return null
 			if (discoveryResult.getCloudNumber() == null) {
 				return null;
 			}
 
 			pc.cloudNumber = discoveryResult.getCloudNumber();
 			pc.cloudEndpointURI = discoveryResult.getXdiEndpointUri();
 		} catch (Xdi2ClientException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		} finally {
 			httpClient.close();
 		}
 
 		pc.linkContractAddress = linkContractAddress;
 		pc.senderCloudNumber = senderCN;
 		System.out.println(pc.toString());
 		// pc.getProfileInfo();
 		return pc;
 	}
 
 	public static String findCloudNumber(String cloudName, String regURI) {
 		XDIDiscoveryResult discoveryResult = null;
 		XDIHttpClient httpClient = null;
 		if (regURI != null && regURI.length() > 0) {
 			httpClient = new XDIHttpClient(regURI);
 
 		} else {
 			httpClient = new XDIHttpClient(DEFAULT_REGISTRY_URI);
 
 		}
 		XDIDiscoveryClient discovery = new XDIDiscoveryClient();
 		discovery.setRegistryXdiClient(httpClient);
 		try {
 			ArrayList<XDI3Segment> endpointUriTypes = new ArrayList<XDI3Segment>();
 			endpointUriTypes.add(XDI3Segment.create("$xdi"));
 			discoveryResult = discovery.discoverFromRegistry(XDI3Segment
 					.create(cloudName), endpointUriTypes
 					.toArray(new XDI3Segment[endpointUriTypes.size()]));
 
 		} catch (Xdi2ClientException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		} finally {
 			httpClient.close();
 		}
 
 		return discoveryResult != null ? discoveryResult.getCloudNumber()
 				.toString() : "";
 	}
 
 	public Graph getWholeGraph() {
 
 		XDIClient xdiClient = new XDIHttpClient(cloudEndpointURI);
 
 		// prepare message envelope for getting email
 
 		MessageEnvelope messageEnvelope = new MessageEnvelope();
 		Message message = messageEnvelope.getMessage(cloudNumber, true);
 		message.setLinkContractXri(linkContractAddress);
 		message.setSecretToken(secretToken);
 		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot
 				.createPeerRootArcXri(cloudNumber)));
 
 		message.createGetOperation(XDI3Segment.create("()"));
 
 		// System.out.println("Message :\n" + messageEnvelope + "\n");
 		try {
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(
 					messageEnvelope.getGraph(), System.out);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// send the message
 
 		MessageResult messageResult;
 
 		try {
 
 			messageResult = xdiClient.send(messageEnvelope, null);
 			// System.out.println(messageResult);
 			MemoryGraph response = (MemoryGraph) messageResult.getGraph();
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(response,
 					System.out);
 			return response;
 
 		} catch (Xdi2ClientException ex) {
 
 			ex.printStackTrace();
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 		} finally {
 			xdiClient.close();
 		}
 
 		return null;
 
 	}
 
 	public ArrayList<PCAttributeCollection> geAllCollections() {
 
 		Graph g = this.getWholeGraph();
 		ContextNode root = g.getRootContextNode();
 		ReadOnlyIterator<Literal> allLiterals = root.getAllLiterals();
 		while (allLiterals.hasNext()) {
 			Literal lit = allLiterals.next();
 			String value = lit.getLiteralData().toString();
 			String name = lit.getContextNode().toString();
 		}
 		return null;
 
 	}
 
 	/**
 	 * 
 	 * @param profileInfo
 	 */
 
 	public void saveProfileInfo(ProfileInfo profileInfo) {
 
 		// construct the statements for Profiles's fields
 
 		ArrayList<XDI3Statement> profileXDIStmts = new ArrayList<XDI3Statement>();
 
 		if (profileInfo.getEmail() != null) {
 			profileXDIStmts.add(XDI3Statement.create(cloudNumber.toString()
 					+ "<+email>&/&/\"" + profileInfo.getEmail() + "\""));
 		}
 		if (profileInfo.getPhone() != null) {
 			profileXDIStmts.add(XDI3Statement.create(cloudNumber.toString()
 					+ "+home<+phone>&/&/\"" + profileInfo.getPhone() + "\""));
 		}
 		// send the message
 
 		// prepare XDI client
 
 		XDIClient xdiClient = new XDIHttpClient(cloudEndpointURI);
 
 		// prepare message envelope
 
 		MessageEnvelope messageEnvelope = new MessageEnvelope();
 		Message message = messageEnvelope.getMessage(cloudNumber, true);
 		message.setLinkContractXri(linkContractAddress);
 
 		message.setSecretToken(secretToken);
 
 		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot
 				.createPeerRootArcXri(cloudNumber)));
 		message.createSetOperation(profileXDIStmts.iterator());
 
 		System.out.println("Message :\n" + messageEnvelope + "\n");
 
 		// send the message
 
 		MessageResult messageResult;
 
 		try {
 
 			messageResult = xdiClient.send(messageEnvelope, null);
 			System.out.println(messageResult);
 
 		} catch (Xdi2ClientException ex) {
 
 			ex.printStackTrace();
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 		} finally {
 			xdiClient.close();
 		}
 
 		this.profileInfo = profileInfo;
 
 	}
 
 	public String getDataBucket(String bucketName) {
 		String values = new String();
 
 		XDI3Segment query = XDI3Segment.create(cloudNumber + "[<+" + bucketName
 				+ ">]");
 		MessageResult result = getXDIStmts(query, true);
 
 		MemoryGraph response = (MemoryGraph) result.getGraph();
 		ContextNode root = response.getRootContextNode();
 		ReadOnlyIterator<Literal> literals = root.getAllLiterals();
 		while (literals.hasNext()) {
 			Literal literal = literals.next();
 
 			values += literal.getLiteralDataString();
 			values += ";";
 		}
 
 		return values;
 	}
 
 	public ProfileInfo getProfileInfo() {
 
 		ProfileInfo profileInfo = new ProfileInfo();
 
 		// prepare XDI client to get profile info
 
 		XDIClient xdiClient = new XDIHttpClient(cloudEndpointURI);
 
 		// prepare message envelope for getting email
 
 		MessageEnvelope messageEnvelope = new MessageEnvelope();
 		Message message = messageEnvelope.getMessage(senderCloudNumber, true);
 		message.setLinkContractXri(linkContractAddress);
 		if (secretToken != null) {
 			message.setSecretToken(secretToken);
 		}
 		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot
 				.createPeerRootArcXri(cloudNumber)));
 
 		message.createGetOperation(XDI3Segment.create(cloudNumber.toString()
 				+ "<+email>&"));
 
 		// System.out.println("Message :\n" + messageEnvelope + "\n");
 		try {
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(
 					messageEnvelope.getGraph(), System.out);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// send the message
 
 		MessageResult messageResult;
 
 		try {
 
 			messageResult = xdiClient.send(messageEnvelope, null);
 			// System.out.println(messageResult);
 			MemoryGraph response = (MemoryGraph) messageResult.getGraph();
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(response,
 					System.out);
 			Literal emailLiteral = response.getDeepLiteral(XDI3Segment
 					.create(cloudNumber.toString() + "<+email>&"));
 			String email = (emailLiteral == null) ? "" : emailLiteral
 					.getLiteralData().toString();
 			profileInfo.setEmail(email);
 
 		} catch (Xdi2ClientException ex) {
 
 			ex.printStackTrace();
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 		} finally {
 			xdiClient.close();
 		}
 
 		// prepare message envelope for getting phone
 
 		MessageEnvelope messageEnvelope2 = new MessageEnvelope();
 		Message message2 = messageEnvelope2.getMessage(senderCloudNumber, true);
 		message2.setLinkContractXri(linkContractAddress);
 		if (secretToken != null) {
 			message2.setSecretToken(secretToken);
 		}
 		message2.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot
 				.createPeerRootArcXri(cloudNumber)));
 
 		message2.createGetOperation(XDI3Segment.create(cloudNumber.toString()
 				+ "+home<+phone>&"));
 
 		// System.out.println("Message :\n" + messageEnvelope2 + "\n");
 		try {
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(
 					messageEnvelope2.getGraph(), System.out);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		xdiClient.close();
 		xdiClient = new XDIHttpClient(cloudEndpointURI);
 		// send the message
 
 		MessageResult messageResult2;
 
 		try {
 
 			messageResult2 = xdiClient.send(messageEnvelope2, null);
 			// System.out.println(messageResult2);
 			MemoryGraph response = (MemoryGraph) messageResult2.getGraph();
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(response,
 					System.out);
 			Literal phoneLiteral = response.getDeepLiteral(XDI3Segment
 					.create(cloudNumber.toString() + "+home<+phone>&"));
 			String phone = (phoneLiteral == null) ? "" : phoneLiteral
 					.getLiteralData().toString();
 			profileInfo.setPhone(phone);
 
 		} catch (Xdi2ClientException ex) {
 
 			ex.printStackTrace();
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 		} finally {
 			xdiClient.close();
 		}
 
 		this.profileInfo = profileInfo;
 		return profileInfo;
 	}
 
 	public MessageResult setXDIStmts(ArrayList<XDI3Statement> XDIStmts) {
 
 		// prepare XDI client
 System.out.println("setXDIStmts 1");
 		XDIClient xdiClient = new XDIHttpClient(cloudEndpointURI);
 
 		// prepare message envelope
 
 		MessageEnvelope messageEnvelope = new MessageEnvelope();
 		Message message = messageEnvelope.getMessage(cloudNumber, true);
 		message.setLinkContractXri(linkContractAddress);
 		System.out.println("setXDIStmts 2");
 		if(secretToken != null){
 			message.setSecretToken(secretToken);
 		}
 //		if(this.sessionId != null && !this.sessionId.isEmpty()){
 //			
 //			XdiAttributeSingleton xdiAttribute = XdiAttributeSingleton.fromContextNode(message.getContextNode().setDeepContextNode(XDI3Segment.create("$public$sessionid<$token>")));
 //			XdiValue xdiValue = xdiAttribute.getXdiValue(true);
 //			xdiValue.getContextNode().setLiteral(sessionId);
 //		}
 		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot
 				.createPeerRootArcXri(cloudNumber)));
 		System.out.println("setXDIStmts 2.5");
 		if(XDIStmts != null && XDIStmts.size() > 0){
 		message.createSetOperation(XDIStmts.iterator());
 		}
 		System.out.println("setXDIStmts 3");
 
 		// System.out.println("Message :\n" + messageEnvelope + "\n");
 		try {
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(
 					messageEnvelope.getGraph(), System.out);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// send the message
 
 		MessageResult messageResult = null;
 
 		try {
 
 			messageResult = xdiClient.send(messageEnvelope, null);
 			System.out.println("setXDIStmts 4");
 			// System.out.println(messageResult);
 			try {
 				XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(
 						messageResult.getGraph(), System.out);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		} catch (Xdi2ClientException ex) {
 
 			ex.printStackTrace();
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 		} finally {
 			xdiClient.close();
 		}
 		return messageResult;
 	}
 
 	public MessageResult setXDISegment(XDI3Segment targetSegment) {
 
 		// prepare XDI client
 
 		XDIClient xdiClient = new XDIHttpClient(cloudEndpointURI);
 
 		// prepare message envelope
 
 		MessageEnvelope messageEnvelope = new MessageEnvelope();
 		Message message = messageEnvelope.getMessage(cloudNumber, true);
 		message.setLinkContractXri(linkContractAddress);
 
 		message.setSecretToken(secretToken);
 
 		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot
 				.createPeerRootArcXri(cloudNumber)));
 		message.createSetOperation(targetSegment);
 
 		// System.out.println("Message :\n" + messageEnvelope + "\n");
 		try {
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(
 					messageEnvelope.getGraph(), System.out);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// send the message
 
 		MessageResult messageResult = null;
 
 		try {
 
 			messageResult = xdiClient.send(messageEnvelope, null);
 			// System.out.println(messageResult);
 			try {
 				XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(
 						messageResult.getGraph(), System.out);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		} catch (Xdi2ClientException ex) {
 
 			ex.printStackTrace();
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 		} finally {
 			xdiClient.close();
 		}
 		return messageResult;
 	}
 
 	public MessageResult delXDIStmts(ArrayList<XDI3Statement> XDIStmts , XDI3Segment target) {
 
 		// prepare XDI client
 
 		XDIClient xdiClient = new XDIHttpClient(cloudEndpointURI);
 
 		// prepare message envelope
 
 		MessageEnvelope messageEnvelope = new MessageEnvelope();
 		Message message = messageEnvelope.getMessage(cloudNumber, true);
 		message.setLinkContractXri(linkContractAddress);
 
 		message.setSecretToken(secretToken);
 
 		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot
 				.createPeerRootArcXri(cloudNumber)));
 		if(XDIStmts != null && XDIStmts.size() > 0){
 			message.createDelOperation(XDIStmts.iterator());
 		}
 		if(target != null && !target.toString().isEmpty()){
 			message.createDelOperation(target);
 		}
 		
 
 		// System.out.println("Message :\n" + messageEnvelope + "\n");
 		try {
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(
 					messageEnvelope.getGraph(), System.out);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// send the message
 
 		MessageResult messageResult = null;
 
 		try {
 
 			messageResult = xdiClient.send(messageEnvelope, null);
 			// System.out.println(messageResult);
 			try {
 				XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(
 						messageResult.getGraph(), System.out);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		} catch (Xdi2ClientException ex) {
 
 			ex.printStackTrace();
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 		} finally {
 			xdiClient.close();
 		}
 		return messageResult;
 	}
 
 	public MessageResult sendQueries(ArrayList<XDI3Segment> queries,
 			ArrayList<XDI3Statement> queryStmts, boolean isDeref) {
 
 		XDIClient xdiClient = new XDIHttpClient(cloudEndpointURI);
 
 		// prepare message envelope
 
 		MessageEnvelope messageEnvelope = new MessageEnvelope();
 		Message message = messageEnvelope.getMessage(senderCloudNumber, true);
 		message.setLinkContractXri(linkContractAddress);
 		if (secretToken != null) {
 			message.setSecretToken(secretToken);
 		}
 		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot
 				.createPeerRootArcXri(cloudNumber)));
 
 		Iterator<XDI3Segment> queryIter = queries.iterator();
 		while (queryIter.hasNext()) {
 			XDI3Segment query = queryIter.next();
 			GetOperation getOp = message.createGetOperation(query);
 			if (isDeref) {
 				getOp.setParameter(XDI3SubSegment.create("$deref"), "true");
 			}
 		}
 		if (queryStmts!= null && queryStmts.size() > 0) {
 			message.createGetOperation(queryStmts.iterator());
 		}
 
 		// System.out.println("Message :\n" + messageEnvelope + "\n");
 		try {
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(
 					messageEnvelope.getGraph(), System.out);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// send the message
 
 		MessageResult messageResult = null;
 
 		try {
 
 			messageResult = xdiClient.send(messageEnvelope, null);
 			// System.out.println(messageResult);
 			MemoryGraph response = (MemoryGraph) messageResult.getGraph();
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(response,
 					System.out);
 
 		} catch (Xdi2ClientException ex) {
 
 			ex.printStackTrace();
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 		} finally {
 			xdiClient.close();
 		}
 		return messageResult;
 	}
 
 	public MessageResult getXDIStmts(XDI3Segment query, boolean isDeref) {
 
 		XDIClient xdiClient = new XDIHttpClient(cloudEndpointURI);
 
 		// prepare message envelope
 
 		MessageEnvelope messageEnvelope = new MessageEnvelope();
 		Message message = messageEnvelope.getMessage(senderCloudNumber, true);
 		message.setLinkContractXri(linkContractAddress);
 		if (secretToken != null) {
 			message.setSecretToken(secretToken);
 		}
 		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot
 				.createPeerRootArcXri(cloudNumber)));
 
 		GetOperation getOp = message.createGetOperation(query);
 		if (isDeref) {
 			getOp.setParameter(XDI3SubSegment.create("$deref"), "true");
 		}
 		// System.out.println("Message :\n" + messageEnvelope + "\n");
 		try {
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(
 					messageEnvelope.getGraph(), System.out);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// send the message
 
 		MessageResult messageResult = null;
 
 		try {
 
 			messageResult = xdiClient.send(messageEnvelope, null);
 			// System.out.println(messageResult);
 			MemoryGraph response = (MemoryGraph) messageResult.getGraph();
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(response,
 					System.out);
 
 		} catch (Xdi2ClientException ex) {
 
 			ex.printStackTrace();
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 		} finally {
 			xdiClient.close();
 		}
 		return messageResult;
 	}
 
 	/*
 	 * contact info
 	 */
 
 	public void saveContactInfo(XDI3Segment cloudNameOrCloudNumber,
 			ContactInfo contactInfo) {
 		// construct the statements for Contact's fields
 
 		PersonalCloud contactPC = PersonalCloud.open(cloudNameOrCloudNumber,
 				cloudNumber, XDI3Segment.create("$public$do"), "");
 		XDI3Segment contactCN = contactPC.cloudNumber;
 
 		ArrayList<XDI3Statement> contactXDIStmts = new ArrayList<XDI3Statement>();
 
 		if (contactInfo.getEmail() != null) {
 			contactXDIStmts.add(XDI3Statement.create(contactCN.toString()
 					+ "<+email>&/&/\"" + contactInfo.getEmail() + "\""));
 		}
 		if (contactInfo.getPhone() != null) {
 			contactXDIStmts.add(XDI3Statement.create(contactCN.toString()
 					+ "+home<+phone>&/&/\"" + contactInfo.getPhone() + "\""));
 		}
 		// send the message
 
 		// prepare XDI client
 
 		XDIClient xdiClient = new XDIHttpClient(cloudEndpointURI);
 
 		// prepare message envelope
 
 		MessageEnvelope messageEnvelope = new MessageEnvelope();
 		Message message = messageEnvelope.getMessage(cloudNumber, true);
 		message.setLinkContractXri(linkContractAddress);
 
 		message.setSecretToken(secretToken);
 
 		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot
 				.createPeerRootArcXri(cloudNumber)));
 		message.createSetOperation(contactXDIStmts.iterator());
 
 		System.out.println("Message :\n" + messageEnvelope + "\n");
 
 		// send the message
 
 		MessageResult messageResult;
 
 		try {
 
 			messageResult = xdiClient.send(messageEnvelope, null);
 			System.out.println(messageResult);
 
 		} catch (Xdi2ClientException ex) {
 
 			ex.printStackTrace();
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 		}
 
 		addressBook.put(contactCN.toString(), contactInfo);
 
 	}
 
 	public PCAttribute readAttr(PCAttributeCollection coll, String attrName) {
 
 		// prepare XDI client to get profile info
 
 		XDIClient xdiClient = new XDIHttpClient(cloudEndpointURI);
 
 		// prepare message envelope
 
 		MessageEnvelope messageEnvelope = new MessageEnvelope();
 		Message message = messageEnvelope.getMessage(senderCloudNumber, true);
 		message.setLinkContractXri(linkContractAddress);
 		if (secretToken != null) {
 			message.setSecretToken(secretToken);
 		}
 		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot
 				.createPeerRootArcXri(cloudNumber)));
 
 		message.createGetOperation(XDI3Segment.create(cloudNumber.toString()
 				+ "+" + coll.getName() + "<+" + attrName + ">&"));
 
 		// System.out.println("Message :\n" + messageEnvelope + "\n");
 		try {
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(
 					messageEnvelope.getGraph(), System.out);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// send the message
 
 		MessageResult messageResult;
 
 		try {
 
 			messageResult = xdiClient.send(messageEnvelope, null);
 			// System.out.println(messageResult);
 			MemoryGraph response = (MemoryGraph) messageResult.getGraph();
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(response,
 					System.out);
 			Literal literalValue = response.getDeepLiteral(XDI3Segment
 					.create(cloudNumber.toString() + "+" + coll.getName()
 							+ "<+" + attrName + ">&"));
 			String strVal = (literalValue == null) ? "" : literalValue
 					.getLiteralData().toString();
 			PCAttribute attr = new PCAttribute(attrName, strVal, coll);
 			return attr;
 
 		} catch (Xdi2ClientException ex) {
 
 			ex.printStackTrace();
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 		}
 
 		return null;
 	}
 
 	public void deleteAttr(PCAttributeCollection coll, String attrName) {
 
 		// prepare XDI client to get profile info
 
 		XDIClient xdiClient = new XDIHttpClient(cloudEndpointURI);
 
 		// prepare message envelope
 
 		MessageEnvelope messageEnvelope = new MessageEnvelope();
 		Message message = messageEnvelope.getMessage(senderCloudNumber, true);
 		message.setLinkContractXri(linkContractAddress);
 		if (secretToken != null) {
 			message.setSecretToken(secretToken);
 		}
 		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot
 				.createPeerRootArcXri(cloudNumber)));
 
 		message.createDelOperation(XDI3Segment.create(cloudNumber.toString()
 				+ "+" + coll.getName() + "<+" + attrName + ">&"));
 
 		// System.out.println("Message :\n" + messageEnvelope + "\n");
 		try {
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(
 					messageEnvelope.getGraph(), System.out);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// send the message
 
 		MessageResult messageResult;
 
 		try {
 
 			messageResult = xdiClient.send(messageEnvelope, null);
 			// System.out.println(messageResult);
 			MemoryGraph response = (MemoryGraph) messageResult.getGraph();
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(response,
 					System.out);
 
 		} catch (Xdi2ClientException ex) {
 
 			ex.printStackTrace();
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 		}
 		coll.deleteAttribute(attrName);
 
 	}
 
 	public void save(PCAttributeCollection coll) {
 		// construct the statements for Profiles's fields
 
 		ArrayList<XDI3Statement> profileXDIStmts = new ArrayList<XDI3Statement>();
 
 		// for all attributes in the collection, create XDI statements
 		Hashtable<String, PCAttribute> attrMap = coll.getAttributeMap();
 		Iterator<PCAttribute> iter = attrMap.values().iterator();
 		while (iter.hasNext()) {
 			PCAttribute attr = iter.next();
 			if (attr.getValue() != null) {
 				XDI3Statement stmt = XDI3Statement.create(attr.getAddress(this)
 						.toString() + "/&/\"" + attr.getValue() + "\"");
 				profileXDIStmts.add(stmt);
 			} else {
 				XDI3Statement stmt = XDI3Statement.create(attr.getAddress(this)
 						.toString() + "/&/\"" + "\"");
 				profileXDIStmts.add(stmt);
 
 			}
 		}
 
 		// send the message
 
 		// prepare XDI client
 
 		XDIClient xdiClient = new XDIHttpClient(cloudEndpointURI);
 
 		// prepare message envelope
 
 		MessageEnvelope messageEnvelope = new MessageEnvelope();
 		Message message = messageEnvelope.getMessage(cloudNumber, true);
 		message.setLinkContractXri(linkContractAddress);
 
 		message.setSecretToken(secretToken);
 
 		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot
 				.createPeerRootArcXri(cloudNumber)));
 
 		message.createSetOperation(profileXDIStmts.iterator());
 
 		try {
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(
 					messageEnvelope.getGraph(), System.out);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// System.out.println("Message :\n" + messageEnvelope + "\n");
 
 		// send the message
 
 		MessageResult messageResult;
 
 		try {
 
 			messageResult = xdiClient.send(messageEnvelope, null);
 			System.out.println(messageResult);
 
 		} catch (Xdi2ClientException ex) {
 
 			ex.printStackTrace();
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 		}
 
 	}
 
 	public ContactInfo getContactInfo(XDI3Segment cloudNameOrCloudNumber) {
 
 		return null;
 	}
 
 	public ContactInfo findContactInfoById(String id) {
 
 		return null;
 	}
 
 	public ContactInfo findContactInfoByEmail(String email) {
 
 		return null;
 	}
 
 	public void setLinkContractAddress(XDI3Segment linkContractAddress) {
 		this.linkContractAddress = linkContractAddress;
 	}
 
 	/*
 	 * access control
 	 */
 
 	/**
 	 * 
 	 * @param entity
 	 *            The entity (e.g. ProfileInfo, ContactInfo, etc.) to allow
 	 *            access to
 	 * @param permissionXri
 	 *            The allowed XDI operation, e.g. $get, $set, $del. If null, no
 	 *            access is allowed.
 	 * @param assignee
 	 *            The Cloud Name or Cloud Number of the assigned
 	 *            people/organization. If null, allow public access.
 	 */
 	public void allowAccess(PersonalCloudEntity entity,
 			XDI3Segment permissionXri, XDI3Segment assignee) {
 
 		PersonalCloud assigneePC = PersonalCloud.open(assignee, cloudNumber,
 				XDI3Segment.create("$public$do"), "");
 		XDI3Segment assigneeCN = assigneePC.cloudNumber;
 
 		XDIClient xdiClient = new XDIHttpClient(cloudEndpointURI);
 
 		// prepare message envelope for getting email
 
 		MessageEnvelope messageEnvelope = new MessageEnvelope();
 		Message message = messageEnvelope.getMessage(cloudNumber, true);
 		message.setLinkContractXri(linkContractAddress);
 		message.setSecretToken(secretToken);
 		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot
 				.createPeerRootArcXri(cloudNumber)));
 
 		message.createSetOperation(XDI3Statement.create(assigneeCN.toString()
 				+ "$do$if$and/$true/({$from}/$is/" + assigneeCN.toString()
 				+ ")"));
 		message.createSetOperation(XDI3Statement.create(assigneeCN.toString()
 				+ "$do/" + permissionXri.toString() + "/"
 				+ entity.getAddress(this)));
 
 		// System.out.println("Message :\n" + messageEnvelope + "\n");
 		try {
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(
 					messageEnvelope.getGraph(), System.out);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			xdiClient.close();
 		}
 
 		xdiClient = new XDIHttpClient(cloudEndpointURI);
 		// send the message
 
 		MessageResult messageResult;
 
 		try {
 
 			messageResult = xdiClient.send(messageEnvelope, null);
 			// System.out.println(messageResult);
 			MemoryGraph response = (MemoryGraph) messageResult.getGraph();
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(response,
 					System.out);
 
 		} catch (Xdi2ClientException ex) {
 
 			ex.printStackTrace();
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 		} finally {
 			xdiClient.close();
 		}
 
 	}
 
 	public void deleteNodeTree(XDI3Segment target) {
 		XDIClient xdiClient = new XDIHttpClient(cloudEndpointURI);
 
 		// prepare message envelope for deleting the link contract for the
 		// assignee
 
 		MessageEnvelope messageEnvelope = new MessageEnvelope();
 		Message message = messageEnvelope.getMessage(cloudNumber, true);
 		message.setLinkContractXri(linkContractAddress);
 		message.setSecretToken(secretToken);
 		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot
 				.createPeerRootArcXri(cloudNumber)));
 
 		// message.createDelOperation(XDI3Statement.create(assigneeCN.toString()
 		// + "$do$if$and/$true/({$from}/$is/" + assigneeCN.toString()
 		// + ")"));
 		message.createDelOperation(target);
 
 		// System.out.println("Message :\n" + messageEnvelope + "\n");
 		try {
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(
 					messageEnvelope.getGraph(), System.out);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// send the message
 
 		MessageResult messageResult;
 
 		try {
 
 			messageResult = xdiClient.send(messageEnvelope, null);
 			// System.out.println(messageResult);
 			MemoryGraph response = (MemoryGraph) messageResult.getGraph();
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(response,
 					System.out);
 
 		} catch (Xdi2ClientException ex) {
 
 			ex.printStackTrace();
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 		} finally {
 			xdiClient.close();
 		}
 
 	}
 
 	public void removeAccess(PersonalCloudEntity entity, XDI3Segment assignee) {
 		PersonalCloud assigneePC = PersonalCloud.open(assignee, cloudNumber,
 				XDI3Segment.create("$public$do"), "");
 		XDI3Segment assigneeCN = assigneePC.cloudNumber;
 
 		XDIClient xdiClient = new XDIHttpClient(cloudEndpointURI);
 
 		// prepare message envelope for deleting the link contract for the
 		// assignee
 
 		MessageEnvelope messageEnvelope = new MessageEnvelope();
 		Message message = messageEnvelope.getMessage(cloudNumber, true);
 		message.setLinkContractXri(linkContractAddress);
 		message.setSecretToken(secretToken);
 		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot
 				.createPeerRootArcXri(cloudNumber)));
 
 		// message.createDelOperation(XDI3Statement.create(assigneeCN.toString()
 		// + "$do$if$and/$true/({$from}/$is/" + assigneeCN.toString()
 		// + ")"));
 		message.createDelOperation(XDI3Segment.create(assigneeCN.toString()
 				+ "$do"));
 
 		// System.out.println("Message :\n" + messageEnvelope + "\n");
 		try {
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(
 					messageEnvelope.getGraph(), System.out);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// send the message
 
 		MessageResult messageResult;
 
 		try {
 
 			messageResult = xdiClient.send(messageEnvelope, null);
 			// System.out.println(messageResult);
 			MemoryGraph response = (MemoryGraph) messageResult.getGraph();
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(response,
 					System.out);
 
 		} catch (Xdi2ClientException ex) {
 
 			ex.printStackTrace();
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 		} finally {
 			xdiClient.close();
 		}
 
 	}
 
 	public String requestForAccess(XDI3Segment requestedObjectXri,
 			XDI3Segment operation, XDI3Segment fromRelationshipXri,
 			XDI3Segment toRelationshipXri, PersonalCloud peerCloud) {
 
 		XDIClient xdiClient = new XDIHttpClient(peerCloud.getCloudEndpointURI());
 		MessageEnvelope messageEnvelope = new MessageEnvelope();
 		Message message = messageEnvelope.getMessage(cloudNumber, true);
 		message.setLinkContractXri(XDI3Segment
 				.create("$public[+pendingrequest]$do"));
 
 		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot
 				.createPeerRootArcXri(peerCloud.getCloudNumber())));
 
 		String reqUUID = "!:uuid:" + UUID.randomUUID().toString();
 
 		message.createSetOperation(XDI3Statement
 				.create("$public[+pendingrequest]" + reqUUID
 						+ "<+from_cn>&/&/\"" + cloudNumber.toString() + "\""));
 		message.createSetOperation(XDI3Statement
 				.create("$public[+pendingrequest]" + reqUUID + "<+to_cn>&/&/\""
 						+ peerCloud.getCloudNumber() + "\""));
 
 		message.createSetOperation(XDI3Statement
 				.create("$public[+pendingrequest]" + reqUUID
 						+ "<+from_rel>&/&/\"" + fromRelationshipXri.toString()
 						+ "\""));
 
 		message.createSetOperation(XDI3Statement
 				.create("$public[+pendingrequest]" + reqUUID
 						+ "<+to_rel>&/&/\"" + toRelationshipXri.toString()
 						+ "\""));
 		message.createSetOperation(XDI3Statement
 				.create("$public[+pendingrequest]" + reqUUID
 						+ "<+requested_object>&/&/\""
 						+ requestedObjectXri.toString() + "\""));
 		message.createSetOperation(XDI3Statement
 				.create("$public[+pendingrequest]" + reqUUID
 						+ "<+requested_op>&/&/\"" + operation.toString() + "\""));
 		message.createSetOperation(XDI3Statement
 				.create("$public[+pendingrequest]" + reqUUID
 						+ "<+requestor_link_contract>&/&/\"" + cloudNumber
 						+ toRelationshipXri.toString() + "$do" + "\""));
 		try {
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(
 					messageEnvelope.getGraph(), System.out);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return "";
 		}
 
 		// send the message
 
 		MessageResult messageResult;
 
 		try {
 
 			messageResult = xdiClient.send(messageEnvelope, null);
 			// System.out.println(messageResult);
 			MemoryGraph response = (MemoryGraph) messageResult.getGraph();
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(response,
 					System.out);
 
 		} catch (Xdi2ClientException ex) {
 
 			ex.printStackTrace();
 			return "";
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 			return "";
 		} finally {
 			xdiClient.close();
 		}
 
 		// create a pending request entry in sender's graph
 		xdiClient = new XDIHttpClient(getCloudEndpointURI());
 		messageEnvelope = new MessageEnvelope();
 		message = messageEnvelope.getMessage(cloudNumber, true);
 		message.setLinkContractXri(linkContractAddress);
 		message.setSecretToken(secretToken);
 		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot
 				.createPeerRootArcXri(cloudNumber)));
 
 		message.createSetOperation(XDI3Statement
 				.create("$public[+pendingrequest]" + reqUUID
 						+ "<+from_cn>&/&/\"" + cloudNumber.toString() + "\""));
 		message.createSetOperation(XDI3Statement
 				.create("$public[+pendingrequest]" + reqUUID
 						+ "<+from_rel>&/&/\"" + fromRelationshipXri.toString()
 						+ "\""));
 		message.createSetOperation(XDI3Statement
 				.create("$public[+pendingrequest]" + reqUUID + "<+to_cn>&/&/\""
 						+ peerCloud.getCloudNumber() + "\""));
 		message.createSetOperation(XDI3Statement
 				.create("$public[+pendingrequest]" + reqUUID
 						+ "<+to_rel>&/&/\"" + toRelationshipXri.toString()
 						+ "\""));
 		message.createSetOperation(XDI3Statement
 				.create("$public[+pendingrequest]" + reqUUID
 						+ "<+requested_object>&/&/\""
 						+ requestedObjectXri.toString() + "\""));
 		message.createSetOperation(XDI3Statement
 				.create("$public[+pendingrequest]" + reqUUID
 						+ "<+requested_op>&/&/\"" + operation.toString() + "\""));
 		message.createSetOperation(XDI3Statement
 				.create("$public[+pendingrequest]" + reqUUID
 						+ "<+requestor_link_contract>&/&/\"" + cloudNumber
 						+ toRelationshipXri.toString() + "$do" + "\""));
 		try {
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(
 					messageEnvelope.getGraph(), System.out);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return "";
 		}
 
 		try {
 
 			messageResult = xdiClient.send(messageEnvelope, null);
 			// System.out.println(messageResult);
 			MemoryGraph response = (MemoryGraph) messageResult.getGraph();
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(response,
 					System.out);
 
 		} catch (Xdi2ClientException ex) {
 
 			ex.printStackTrace();
 			return "";
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 			return "";
 		} finally {
 			xdiClient.close();
 		}
 		// create the reciprocal relationship in requester graph
 		createRelationship(peerCloud.getCloudNumber(), toRelationshipXri,
 				fromRelationshipXri);
 
 		// create requested object XRI under the peerCloud id in requester graph
 
 		xdiClient = new XDIHttpClient(getCloudEndpointURI());
 		messageEnvelope = new MessageEnvelope();
 		message = messageEnvelope.getMessage(cloudNumber, true);
 		message.setLinkContractXri(linkContractAddress);
 		message.setSecretToken(secretToken);
 		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot
 				.createPeerRootArcXri(cloudNumber)));
 
 		message.createSetOperation(XDI3Statement.create(peerCloud
 				.getCloudNumber()
 				+ "[<+shared_data>]"
 				+ "<"
 				+ reqUUID
 				+ ">"
 				+ "&/&/\"" + requestedObjectXri.toString() + "\""));
 
 		try {
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(
 					messageEnvelope.getGraph(), System.out);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return "";
 		}
 
 		try {
 
 			messageResult = xdiClient.send(messageEnvelope, null);
 			// System.out.println(messageResult);
 			MemoryGraph response = (MemoryGraph) messageResult.getGraph();
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(response,
 					System.out);
 
 		} catch (Xdi2ClientException ex) {
 
 			ex.printStackTrace();
 			return "";
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 			return "";
 		} finally {
 			xdiClient.close();
 		}
 
 		return new String("$public[+pendingrequest]" + reqUUID);
 	}
 
 	public boolean approveAccess(XDI3Segment requestIdXri,
 			XDI3Segment mappedTarget) {
 
 		String from = "", to = "", from_rel = "", to_rel = "", operation = "", requested_object = "";
 		XDIClient xdiClient = new XDIHttpClient(cloudEndpointURI);
 		MessageEnvelope messageEnvelope = new MessageEnvelope();
 		Message message = messageEnvelope.getMessage(cloudNumber, true);
 		message.setLinkContractXri(linkContractAddress);
 		message.setSecretToken(secretToken);
 		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot
 				.createPeerRootArcXri(cloudNumber)));
 
 		message.createGetOperation(requestIdXri);
 
 		try {
 			XDIWriterRegistry.forFormat("XDI/JSON", null).write(
 					messageEnvelope.getGraph(), System.out);
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 
 		// send the message
 
 		MessageResult messageResult;
 
 		try {
 
 			messageResult = xdiClient.send(messageEnvelope, null);
 			// System.out.println(messageResult);
 			MemoryGraph response = (MemoryGraph) messageResult.getGraph();
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(response,
 					System.out);
 			// parse the response and get the five components of a request
 			from = response.getDeepLiteral(XDI3Segment.create(requestIdXri
 					+ "<+from_cn>&")) != null ? response.getDeepLiteral(
 					XDI3Segment.create(requestIdXri + "<+from_cn>&"))
 					.getLiteralDataString() : "";
 			operation = response.getDeepLiteral(XDI3Segment.create(requestIdXri
 					+ "<+requested_op>&")) != null ? response.getDeepLiteral(
 					XDI3Segment.create(requestIdXri + "<+requested_op>&"))
 					.getLiteralDataString() : "";
 			from_rel = response.getDeepLiteral(XDI3Segment.create(requestIdXri
 					+ "<+from_rel>&")) != null ? response.getDeepLiteral(
 					XDI3Segment.create(requestIdXri + "<+from_rel>&"))
 					.getLiteralDataString() : "";
 			to_rel = response.getDeepLiteral(XDI3Segment.create(requestIdXri
 					+ "<+to_rel>&")) != null ? response.getDeepLiteral(
 					XDI3Segment.create(requestIdXri + "<+to_rel>&"))
 					.getLiteralDataString() : "";
 			requested_object = response.getDeepLiteral(XDI3Segment
 					.create(requestIdXri + "<+requested_object>&")) != null ? response
 					.getDeepLiteral(
 							XDI3Segment.create(requestIdXri
 									+ "<+requested_object>&"))
 					.getLiteralDataString() : "";
 			to = response.getDeepLiteral(XDI3Segment.create(requestIdXri
 					+ "<+to_cn>&")) != null ? response.getDeepLiteral(
 					XDI3Segment.create(requestIdXri + "<+to_cn>&"))
 					.getLiteralDataString() : "";
 
 			String jsonStr = response.toString("XDI/JSON", null);
 			System.out.println(jsonStr);
 
 		} catch (Xdi2ClientException ex) {
 
 			ex.printStackTrace();
 			return false;
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 			return false;
 		} finally {
 			xdiClient.close();
 		}
 
 		// approve the request
 		this.allowAccessToRelationship(XDI3Segment.create(requested_object),
 				mappedTarget, XDI3Segment.create(operation),
 				XDI3Segment.create(from_rel), XDI3Segment.create(to_rel),
 				XDI3Segment.create(from));
 
 		// delete the request
 		xdiClient = new XDIHttpClient(cloudEndpointURI);
 		MessageEnvelope delMessageEnvelope = new MessageEnvelope();
 		Message delMessage = delMessageEnvelope.getMessage(cloudNumber, true);
 		delMessage.setLinkContractXri(linkContractAddress);
 		delMessage.setSecretToken(secretToken);
 		delMessage.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot
 				.createPeerRootArcXri(cloudNumber)));
 
 		delMessage.createDelOperation(requestIdXri);
 
 		MessageResult delMessageResult;
 
 		try {
 
 			delMessageResult = xdiClient.send(delMessageEnvelope, null);
 			// System.out.println(messageResult);
 			MemoryGraph response = (MemoryGraph) delMessageResult.getGraph();
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(response,
 					System.out);
 
 		} catch (Xdi2ClientException ex) {
 
 			ex.printStackTrace();
 			return false;
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 			return false;
 		} finally {
 			xdiClient.close();
 		}
 		// send a message to peer cloud that the request has been approved
 		PersonalCloud peerCloud = PersonalCloud.open(XDI3Segment.create(from),
 				cloudNumber, XDI3Segment.create("$public$do"), null);
 
 		xdiClient = new XDIHttpClient(peerCloud.getCloudEndpointURI());
 		messageEnvelope = new MessageEnvelope();
 		message = messageEnvelope.getMessage(cloudNumber, true);
 		message.setLinkContractXri(XDI3Segment
 				.create("$public[+approvedrequest]$do"));
 
 		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot
 				.createPeerRootArcXri(peerCloud.getCloudNumber())));
 
 		String reqUUID = requestIdXri.getLastSubSegment().toString();
 
 		message.createSetOperation(XDI3Statement
 				.create("$public[+approvedrequest]" + reqUUID
 						+ "<+from_cn>&/&/\"" + from + "\""));
 		message.createSetOperation(XDI3Statement
 				.create("$public[+approvedrequest]" + reqUUID
 						+ "<+from_rel>&/&/\"" + from_rel + "\""));
 
 		message.createSetOperation(XDI3Statement
 				.create("$public[+approvedrequest]" + reqUUID
 						+ "<+to_rel>&/&/\"" + to_rel + "\""));
 		message.createSetOperation(XDI3Statement
 				.create("$public[+approvedrequest]" + reqUUID
 						+ "<+requested_object>&/&/\"" + requested_object + "\""));
 		message.createSetOperation(XDI3Statement
 				.create("$public[+approvedrequest]" + reqUUID
 						+ "<+requested_op>&/&/\"" + operation + "\""));
 		message.createSetOperation(XDI3Statement
 				.create("$public[+approvedrequest]" + reqUUID
 						+ "<+to_cn>&/&/\"" + to + "\""));
 		message.createSetOperation(XDI3Statement
 				.create("$public[+approvedrequest]" + reqUUID
 						+ "<+acceptor_link_contract>&/&/\"" + cloudNumber
 						+ from_rel + "$do" + "\""));
 
 		try {
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(
 					messageEnvelope.getGraph(), System.out);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 
 		// send the message
 
 		try {
 
 			messageResult = xdiClient.send(messageEnvelope, null);
 			// System.out.println(messageResult);
 			MemoryGraph response = (MemoryGraph) messageResult.getGraph();
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(response,
 					System.out);
 
 		} catch (Xdi2ClientException ex) {
 
 			ex.printStackTrace();
 			return false;
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 			return false;
 		} finally {
 
 			xdiClient.close();
 		}
 
 		// send another message to peer cloud to delete the pending request
 		xdiClient = new XDIHttpClient(peerCloud.getCloudEndpointURI());
 
 		messageEnvelope = new MessageEnvelope();
 		message = messageEnvelope.getMessage(cloudNumber, true);
 		message.setLinkContractXri(XDI3Segment
 				.create("$public[+pendingrequest]$do"));
 
 		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot
 				.createPeerRootArcXri(peerCloud.getCloudNumber())));
 
 		message.createDelOperation(XDI3Segment
 				.create("$public[+pendingrequest]" + reqUUID));
 		try {
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(
 					messageEnvelope.getGraph(), System.out);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 
 		// send the message
 
 		try {
 
 			messageResult = xdiClient.send(messageEnvelope, null);
 			// System.out.println(messageResult);
 			MemoryGraph response = (MemoryGraph) messageResult.getGraph();
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(response,
 					System.out);
 
 		} catch (Xdi2ClientException ex) {
 
 			ex.printStackTrace();
 			return false;
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 			return false;
 		} finally {
 			xdiClient.close();
 		}
 
 		return true;
 	}
 
 	public boolean denyAccess(XDI3Segment requestIdXri) {
 		// delete the request
 		XDIClient xdiClient = new XDIHttpClient(cloudEndpointURI);
 		MessageEnvelope delMessageEnvelope = new MessageEnvelope();
 		Message delMessage = delMessageEnvelope.getMessage(cloudNumber, true);
 		delMessage.setLinkContractXri(linkContractAddress);
 		delMessage.setSecretToken(secretToken);
 		delMessage.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot
 				.createPeerRootArcXri(cloudNumber)));
 
 		delMessage.createDelOperation(requestIdXri);
 
 		MessageResult delMessageResult;
 
 		try {
 
 			delMessageResult = xdiClient.send(delMessageEnvelope, null);
 			// System.out.println(messageResult);
 			MemoryGraph response = (MemoryGraph) delMessageResult.getGraph();
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(response,
 					System.out);
 
 		} catch (Xdi2ClientException ex) {
 
 			ex.printStackTrace();
 			return false;
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 			return false;
 		} finally {
 			xdiClient.close();
 		}
 
 		return true;
 
 	}
 
 	public XDI3Segment getLinkContractAddress() {
 		return linkContractAddress;
 	}
 
 	public XDI3Segment getCloudNumber() {
 		return cloudNumber;
 	}
 
 	public String getRegistryURI() {
 		return registryURI;
 	}
 
 	public String getCloudEndpointURI() {
 		return cloudEndpointURI;
 	}
 
 	public void createDefaultLinkContracts() {
 		XDIClient xdiClient = new XDIHttpClient(cloudEndpointURI);
 		MessageEnvelope messageEnvelope = new MessageEnvelope();
 		Message message = messageEnvelope.getMessage(cloudNumber, true);
 		message.setLinkContractXri(linkContractAddress);
 		message.setSecretToken(secretToken);
 		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot
 				.createPeerRootArcXri(cloudNumber)));
 
 		message.createSetOperation(XDI3Statement.create("$public"
 				+ "[+pendingrequest]" + "$do/" + "$add" + "/" + "$public"
 				+ "[+pendingrequest]"));
 		message.createSetOperation(XDI3Statement.create("$public"
 				+ "[+pendingrequest]" + "$do/" + "$del" + "/" + "$public"
 				+ "[+pendingrequest]"));
 		message.createSetOperation(XDI3Statement.create("$public"
 				+ "[+pendingrequest]" + "$do/" + "$set" + "/" + "$public"
 				+ "[+pendingrequest]"));
 		message.createSetOperation(XDI3Statement.create("$public"
 				+ "[+pendingrequest]"
 				+ "$do$if$and/$true/({$from}/$is/{$from})"));
 
 		message.createSetOperation(XDI3Statement.create("$public"
 				+ "[+approvedrequest]" + "$do/" + "$add" + "/" + "$public"
 				+ "[+approvedrequest]"));
 		// message.createSetOperation(XDI3Statement.create("$public"
 		// + "[+approvedrequest]" + "$do/" + "$del" + "/" + "$public"
 		// + "[+approvedrequest]"));
 		message.createSetOperation(XDI3Statement.create("$public"
 				+ "[+approvedrequest]" + "$do/" + "$set" + "/" + "$public"
 				+ "[+approvedrequest]"));
 		message.createSetOperation(XDI3Statement.create("$public"
 				+ "[+approvedrequest]"
 				+ "$do$if$and/$true/({$from}/$is/{$from})"));
 
 		message.createSetOperation(XDI3Statement.create(cloudNumber + "+friend"
 				+ "$do/" + "$all" + "/" + cloudNumber + "+friend"));
 
 		message.createSetOperation(XDI3Statement.create(cloudNumber + "+friend"
 				+ "$do$if$or/$true/(" + cloudNumber + "/" + "+friend"
 				+ "/{$from}" + ")"));
 		message.createSetOperation(XDI3Statement.create(cloudNumber + "+friend"
 				+ "$do$if$or/$true/({$from}/$is/" + cloudNumber + ")"));
 		message.createSetOperation(XDI3Statement.create(cloudNumber + "+family"
 				+ "$do/" + "$all" + "/" + cloudNumber + "+family"));
 
 		message.createSetOperation(XDI3Statement.create(cloudNumber + "+family"
 				+ "$do$if$or/$true/(" + cloudNumber + "/" + "+family"
 				+ "/{$from}" + ")"));
 		message.createSetOperation(XDI3Statement.create(cloudNumber + "+family"
 				+ "$do$if$or/$true/({$from}/$is/" + cloudNumber + ")"));
 		message.createSetOperation(XDI3Statement.create(cloudNumber
 				+ "+coworker" + "$do/" + "$all" + "/" + cloudNumber
 				+ "+coworker"));
 
 		message.createSetOperation(XDI3Statement.create(cloudNumber
 				+ "+coworker" + "$do$if$or/$true/(" + cloudNumber + "/"
 				+ "+coworker" + "/{$from}" + ")"));
 		message.createSetOperation(XDI3Statement.create(cloudNumber
 				+ "+coworker" + "$do$if$or/$true/({$from}/$is/" + cloudNumber
 				+ ")"));
 
 		// System.out.println("Message :\n" + messageEnvelope + "\n");
 		try {
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(
 					messageEnvelope.getGraph(), System.out);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// send the message
 
 		MessageResult messageResult;
 
 		try {
 
 			messageResult = xdiClient.send(messageEnvelope, null);
 			// System.out.println(messageResult);
 			MemoryGraph response = (MemoryGraph) messageResult.getGraph();
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(response,
 					System.out);
 
 		} catch (Xdi2ClientException ex) {
 
 			ex.printStackTrace();
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 		} finally {
 			xdiClient.close();
 		}
 
 	}
 
 	public void createRelationship(XDI3Segment peerCloudCN,
 			XDI3Segment relationship, XDI3Segment reverseRelationship) {
 		XDIClient xdiClient = new XDIHttpClient(cloudEndpointURI);
 
 		// prepare message envelope for getting email
 
 		MessageEnvelope messageEnvelope = new MessageEnvelope();
 		Message message = messageEnvelope.getMessage(cloudNumber, true);
 		message.setLinkContractXri(linkContractAddress);
 		message.setSecretToken(secretToken);
 		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot
 				.createPeerRootArcXri(cloudNumber)));
 
 		message.createSetOperation(XDI3Statement.create(cloudNumber + "/"
 				+ relationship + "/" + peerCloudCN));
 
 		message.createSetOperation(XDI3Statement.create(peerCloudCN + "/"
 				+ reverseRelationship + "/" + cloudNumber));
 
 		// System.out.println("Message :\n" + messageEnvelope + "\n");
 		try {
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(
 					messageEnvelope.getGraph(), System.out);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// send the message
 
 		MessageResult messageResult;
 
 		try {
 
 			messageResult = xdiClient.send(messageEnvelope, null);
 			// System.out.println(messageResult);
 			MemoryGraph response = (MemoryGraph) messageResult.getGraph();
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(response,
 					System.out);
 
 		} catch (Xdi2ClientException ex) {
 
 			ex.printStackTrace();
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 		} finally {
 			xdiClient.close();
 		}
 	}
 
 	public void allowAccessToRelationship(XDI3Segment target,
 			XDI3Segment mapTarget, XDI3Segment permissionXri,
 			XDI3Segment relationship, XDI3Segment reverseRelationship,
 			XDI3Segment assignee) {
 
 		PersonalCloud assigneePC = PersonalCloud.open(assignee, cloudNumber,
 				XDI3Segment.create("$public$do"), "");
 		XDI3Segment assigneeCN = assigneePC.cloudNumber;
 
 		XDIClient xdiClient = new XDIHttpClient(cloudEndpointURI);
 
 		// prepare message envelope for creating the link contract for accessing
 		// the target
 
 		MessageEnvelope messageEnvelope = new MessageEnvelope();
 		Message message = messageEnvelope.getMessage(cloudNumber, true);
 		message.setLinkContractXri(linkContractAddress);
 		message.setSecretToken(secretToken);
 		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot
 				.createPeerRootArcXri(cloudNumber)));
 
 		if (mapTarget != null) {
 			message.createSetOperation(XDI3Statement.create(target + "/$rep/"
 					+ mapTarget));
 		}
 		message.createSetOperation(XDI3Statement.create(cloudNumber.toString()
 				+ relationship.toString() + "$do/" + permissionXri.toString()
 				+ "/" + target));
 
 		message.createSetOperation(XDI3Statement.create(cloudNumber.toString()
 				+ "/" + relationship + "/" + assigneeCN));
 
 		message.createSetOperation(XDI3Statement.create(assigneeCN + "/"
 				+ reverseRelationship + "/" + cloudNumber.toString()));
 
 		// System.out.println("Message :\n" + messageEnvelope + "\n");
 		try {
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(
 					messageEnvelope.getGraph(), System.out);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// send the message
 
 		MessageResult messageResult;
 
 		try {
 
 			messageResult = xdiClient.send(messageEnvelope, null);
 			// System.out.println(messageResult);
 			MemoryGraph response = (MemoryGraph) messageResult.getGraph();
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(response,
 					System.out);
 
 		} catch (Xdi2ClientException ex) {
 
 			ex.printStackTrace();
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 		} finally {
 			xdiClient.close();
 		}
 
 	}
 
 	public void getPCEntity(XDI3Segment targetAddress,
 			XDI3Segment linkContract, PersonalCloud peerCloud) {
 
 		XDIClient xdiClient = new XDIHttpClient(peerCloud.getCloudEndpointURI());
 
 		MessageEnvelope messageEnvelope = new MessageEnvelope();
 		Message message = messageEnvelope.getMessage(cloudNumber, true);
 		message.setLinkContractXri(linkContract);
 
 		message.setToAddress(XDI3Segment.fromComponent(XdiPeerRoot
 				.createPeerRootArcXri(peerCloud.getCloudNumber())));
 
 		message.createGetOperation(targetAddress);
 
 		// System.out.println("Message :\n" + messageEnvelope + "\n");
 		try {
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(
 					messageEnvelope.getGraph(), System.out);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// send the message
 
 		MessageResult messageResult;
 
 		try {
 
 			messageResult = xdiClient.send(messageEnvelope, null);
 			// System.out.println(messageResult);
 			MemoryGraph response = (MemoryGraph) messageResult.getGraph();
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(response,
 					System.out);
 
 		} catch (Xdi2ClientException ex) {
 
 			ex.printStackTrace();
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 		} finally {
 			xdiClient.close();
 		}
 	}
 
 	/**
 	 * 
 	 * @param cloudName
 	 *            : Desired cloudName for the cloud
 	 * @param secretToken
 	 *            : Alphanumeric string which will be used as the password to
 	 *            login to the cloud
 	 * @param CSPName
 	 *            : Name of the CSP under which this cloud should be created.
 	 *            Valid values are "Neustar", "OwnYourInfo"
 	 * @return
 	 */
 	public static PersonalCloud create(String cloudName, String secretToken,
 			String CSPName) {
 		CSP csp = null;
 		if (CSPName.equalsIgnoreCase("Neustar")) {
 			csp = new CSPNeustar();
 		} else if (CSPName.equalsIgnoreCase("OwnYourInfo")) {
 			csp = new CSPOwnYourInfo();
 		}
 
 		if (csp == null) {
 			System.out.println("No valid CSP found for the given CSP name.");
 			return null;
 		}
 
 		PersonalCloud pc = new PersonalCloud();
 		try {
 			// step 1: Check if Cloud Name available
 
 			XDI3Segment cloudNumber = CSPClient.checkCloudNameAvailable(csp,
 					cloudName);
 
 			// step 2: Register Cloud Name
 			if (cloudNumber == null || cloudNumber.toString().length() == 0) {
 
 				XDI3Segment cloudNumberPeerRootXri = CSPClient
 						.registerCloudName(csp, cloudName);
 
 				if (cloudNumberPeerRootXri != null
 						&& cloudNumberPeerRootXri.toString().length() > 0) {
 					// step 3: Register Cloud with Cloud Number and Shared
 					// Secret
 
 					String xdiEndpoint = CSPClient.registerCloud(csp,
 							XDI3Segment.create(cloudName), cloudNumber,
 							cloudNumberPeerRootXri, secretToken);
 
 					if (xdiEndpoint.length() > 0) {
 						// step 4: Register Cloud XDI URL with Cloud Number
 
 						CSPClient.registerCloudXdiUrl(csp,
 								cloudNumberPeerRootXri, xdiEndpoint);
 						pc.cloudNumber = cloudNumber;
 						pc.cloudEndpointURI = xdiEndpoint;
 					}
 				}
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			return null;
 		}
 
 		pc.linkContractAddress = PersonalCloud.XRI_S_DEFAULT_LINKCONTRACT;
 		pc.secretToken = secretToken;
 		pc.senderCloudNumber = pc.cloudNumber;
 		pc.createDefaultLinkContracts();
 		return pc;
 
 	}
 
 	public String getSessionId() {
 		return sessionId;
 	}
 
 	/*
 	 * {
 	 * "[@]!:uuid:e0178407-b7b6-43f9-e017-8407b7b643f9{$from}[$do]!:uuid:11c43ab0-6ce5-4084-b1bc-9f4a07a27328/$get"
 	 * : [ "{$to}<+email>", "{$to}<+name>" ],
 	 * "[@]!:uuid:e0178407-b7b6-43f9-e017-8407b7b643f9{$from}[$do]!:uuid:11c43ab0-6ce5-4084-b1bc-9f4a07a27328$if$and/$true"
 	 * : [ { "{$from}/$is": [ "[@]!:uuid:e0178407-b7b6-43f9-e017-8407b7b643f9"
 	 * ], "{$msg}<$sig><$valid>&/&": true } ],
 	 * "[@]!:uuid:e0178407-b7b6-43f9-e017-8407b7b643f9{$from}[$do]!:uuid:11c43ab0-6ce5-4084-b1bc-9f4a07a27328<$sig>/$is+"
 	 * : [ "$sha$256$rsa$2048" ],
 	 * "[@]!:uuid:e0178407-b7b6-43f9-e017-8407b7b643f9{$from}[$do]!:uuid:11c43ab0-6ce5-4084-b1bc-9f4a07a27328<$sig>&/&"
 	 * : "..." }
 	 */
 
 	public String showAuthenticationForm(String respectConnectRequest,
 			String respondingPartyCloudName, String respondingPartyCloudNumber,
 			String successurl, String failureurl, String relayState) {
 
 		String result = null;
 		System.out.println("Connect Request :\n" + respectConnectRequest);
 		System.out.println("respondingPartyCloudName :\n"
 				+ respondingPartyCloudName);
 		System.out.println("respondingPartyCloudNumber : \n"
 				+ respondingPartyCloudNumber);
 		MemoryJSONGraphFactory graphFactory = new MemoryJSONGraphFactory();
 		String templateOwnerInumber = null;
 		try {
 			Graph g = graphFactory.parseGraph(respectConnectRequest);
 			// get remote cloud number
 
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(g,
 					System.out);
 			ContextNode c = g.getRootContextNode();
 			ReadOnlyIterator<ContextNode> allCNodes = c.getAllContextNodes();
 			for (ContextNode ci : allCNodes) {
 				if (ci.containsContextNode(XDI3SubSegment.create("[$msg]"))) {
 					templateOwnerInumber = ci.toString(); // .substring(0,
 															// ci.toString().length()-new
 															// String("{$from}").length());
 					System.out.println(templateOwnerInumber);
 					break;
 				}
 			}
 			if (templateOwnerInumber == null) {
 				System.out
 						.println("No cloudnumber for requestor/template owner");
 				return result;
 			}
 			// get the address of the link contract template
 			// $set{$do}
 
 			String lcTemplateAddress = null;
 
 			ReadOnlyIterator<Relation> allRelations = c.getAllRelations(); // g.getDeepRelations(XDI3Segment.create(templateOwnerInumber),XDI3Segment.create("$get"));
 			for (Relation r : allRelations) {
 				if (r.getArcXri().toString().equals("$set{$do}")) {
 					lcTemplateAddress = r.getTargetContextNodeXri().toString();
 					System.out.println(r.getTargetContextNodeXri());
 				}
 
 			}
 			if (lcTemplateAddress == null) {
 				System.out.println("No LC template address provided");
 				return result;
 			}
 			// address of meta link contract in ACME's cloud :
 			// {$to}[@]!:uuid:e0178407-b7b6-43f9-e017-8407b7b643f9$from[@]!:uuid:e0178407-b7b6-43f9-e017-8407b7b643f9+registration$do
 			/*
 			 * String meta_link_contract = "{$to}" + templateOwnerInumber +
 			 * "{$from}" + templateOwnerInumber + "+registration$do";
 			 * 
 			 * PersonalCloud remoteCloud =
 			 * PersonalCloud.open(XDI3Segment.create(templateOwnerInumber),
 			 * this.cloudNumber, XDI3Segment.create("$public$do"), "");
 			 * ArrayList<XDI3Segment> querySegments = new
 			 * ArrayList<XDI3Segment>();
 			 * querySegments.add(XDI3Segment.create(templateOwnerInumber +
 			 * "<+name>"));
 			 * 
 			 * ArrayList<XDI3Statement> queryStmts = new
 			 * ArrayList<XDI3Statement>();
 			 * queryStmts.add(XDI3Statement.create(templateOwnerInumber +
 			 * "/$is$ref/{}")); MessageResult responseFromRemoteCloud =
 			 * remoteCloud.sendQueries(querySegments, queryStmts,false);
 			 * 
 			 * Graph responseGraph = responseFromRemoteCloud.getGraph();
 			 * ContextNode responseRootContext =
 			 * responseGraph.getRootContextNode();
 			 * 
 			 * querySegments = new ArrayList<XDI3Segment>();
 			 * querySegments.add(XDI3Segment.create(lcTemplateAddress));
 			 * remoteCloud
 			 * .setLinkContractAddress(XDI3Segment.create(meta_link_contract));
 			 * MessageResult responseFromRemoteCloud2 =
 			 * remoteCloud.sendQueries(querySegments, queryStmts,false); Graph
 			 * responseGraph2 = responseFromRemoteCloud2.getGraph(); ContextNode
 			 * responseRootContext2 = responseGraph2.getRootContextNode();
 			 * 
 			 * //get requested data fields
 			 * 
 			 * ArrayList<XDI3Segment> getDataFields = new
 			 * ArrayList<XDI3Segment>();
 			 * 
 			 * ReadOnlyIterator<Relation> getRelations =
 			 * responseRootContext2.getAllRelations() ;
 			 * //g.getDeepRelations(XDI3Segment
 			 * .create(templateOwnerInumber),XDI3Segment.create("$get"));
 			 * for(Relation r : getRelations){
 			 * if(r.getArcXri().toString().equals("$get")){
 			 * getDataFields.add(r.getTargetContextNodeXri());
 			 * System.out.println(r.getTargetContextNodeXri()); }
 			 * 
 			 * }
 			 * 
 			 * Literal requestingPartyNameLit =
 			 * responseRootContext.getDeepLiteral
 			 * (XDI3Segment.create(templateOwnerInumber + "<+name>&")); Relation
 			 * requestingPartyCloudnameRel =
 			 * responseRootContext.getDeepRelation(
 			 * XDI3Segment.create(templateOwnerInumber
 			 * ),XDI3Segment.create("$is$ref")); String
 			 * requestingPartyCloudNumberCtx =
 			 * requestingPartyCloudnameRel.getTargetContextNodeXri().toString();
 			 */
 			// prepare secret token input HTML
 
 			StringBuffer buf = new StringBuffer();
 
 			buf.append("<html><head></head><div id=\"authn_form\" style=\"position: relative; top: 61px; left: 64px; z-index: 1000;display: block;\">");
 			buf.append("<body>");
 			buf.append("<p>Hello : ");
 			buf.append(respondingPartyCloudName);
 			buf.append(", welcome to your Connect Service.</p>");
 			buf.append("<p>Please authenticate:</p>");
 			// buf.append("<form action=\"http://mycloud.neustar.biz:8080/myapp/personalclouds/");
 			buf.append("<form action=\"" + WEBAPP_BASE_URI);
 			buf.append(URLEncoder.encode(respondingPartyCloudNumber, "UTF-8"));
 			buf.append("/connect/authorize/\" method=\"post\">");
 			// buf.append("<input type=\"hidden\" name=\"lcTemplateAddress\" value=\"");
 			// buf.append(lcTemplateAddress);
 			// buf.append("\">");
 			// buf.append("</input>");
 			// buf.append("<input type=\"hidden\" name=\"relyingParty\" value=\"");
 			// buf.append(templateOwnerInumber);
 			// buf.append("\">");
 			// buf.append("</input>");
 			buf.append("<input type=\"hidden\" name=\"successurl\" value=\""
 					+ successurl + "\">");
 			buf.append("</input>");
 			buf.append("<input type=\"hidden\" name=\"failureurl\" value=\""
 					+ failureurl + "\">");
 			buf.append("</input>");
 			buf.append("<input type=\"hidden\" name=\"cloudname\" value=\""
 					+ respondingPartyCloudName + "\">");
 			buf.append("</input>");
 			buf.append("<input type=\"hidden\" name=\"relayState\" value=\""
 					+ relayState + "\">");
 			buf.append("</input>");
 			// buf.append("<input type=\"hidden\" name=\"connectRequest\" value=\"");
 			// buf.append(URLEncoder.encode(respectConnectRequest,"UTF-8"));
 			// buf.append("\">");
 			buf.append("<input type=\"hidden\" name=\"connectRequest\" value=\'");
 			buf.append(respectConnectRequest);
 			buf.append("\'>");
 
 			buf.append("</input>");
 			buf.append("Your Secret Token: <input type=\"password\" name=\"secrettoken\"/><br>");
 			buf.append("<input type=\"submit\" value=\"Authenticate!\"/>");
 			buf.append("</form>");
 			buf.append("</div>");
 			buf.append("</body>");
 			buf.append("</html>");
 
 			result = buf.toString();
 			System.out.println("Result HTML:\n" + result);
 
 		} catch (Xdi2ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return result;
 
 	}
 
 	public String showApprovalForm(String connectRequestEncoded,
 			String respondingPartyCloudNumberEncoded, String authToken,
 			String successurl, String failureurl, String cloudname,
 			String relayState) {
 		String result = null;
 
 		String connectRequest = null, respondingPartyCloudNumber = null;
 		try {
 			connectRequest = connectRequestEncoded; // URLDecoder.decode(connectRequestEncoded,
 													// "UTF-8");
 			respondingPartyCloudNumber = URLDecoder.decode(
 					respondingPartyCloudNumberEncoded, "UTF-8");
 		} catch (UnsupportedEncodingException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		System.out.println("Connect Request :\n" + connectRequest);
 
 		System.out.println("respondingPartyCloudNumber : \n"
 				+ respondingPartyCloudNumber);
 
 		System.out.println("Auth Token : \n" + authToken);
 		this.secretToken = authToken;
 		this.linkContractAddress = PersonalCloud.XRI_S_DEFAULT_LINKCONTRACT;
 		this.cloudNumber = XDI3Segment.create(respondingPartyCloudNumber);
 		this.senderCloudNumber = XDI3Segment.create(respondingPartyCloudNumber);
 
 		MemoryJSONGraphFactory graphFactory = new MemoryJSONGraphFactory();
 		String templateOwnerInumber = null;
 		try {
 			Graph g = graphFactory.parseGraph(connectRequest);
 			// get remote cloud number
 
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(g,
 					System.out);
 			ContextNode c = g.getRootContextNode();
 			ReadOnlyIterator<ContextNode> allCNodes = c.getAllContextNodes();
 			for (ContextNode ci : allCNodes) {
 				if (ci.containsContextNode(XDI3SubSegment.create("[$msg]"))) {
 					templateOwnerInumber = ci.toString();
 					System.out.println(templateOwnerInumber);
 					break;
 				}
 			}
 			if (templateOwnerInumber == null) {
 				System.out
 						.println("No cloudnumber for requestor/template owner");
 				return result;
 			}
 			// get the address of the link contract template
 			// $set{$do}
 
 			String lcTemplateAddress = null;
 
 			ReadOnlyIterator<Relation> allRelations = c.getAllRelations(); // g.getDeepRelations(XDI3Segment.create(templateOwnerInumber),XDI3Segment.create("$get"));
 			for (Relation r : allRelations) {
 				if (r.getArcXri().toString().equals("$set{$do}")) {
 					lcTemplateAddress = r.getTargetContextNodeXri().toString();
 					System.out.println(r.getTargetContextNodeXri());
 				}
 
 			}
 			if (lcTemplateAddress == null) {
 				System.out.println("No LC template address provided");
 				return result;
 			}/*
 			 * PersonalCloud remoteCloud =
 			 * PersonalCloud.open(XDI3Segment.create(templateOwnerInumber),
 			 * this.cloudNumber, XDI3Segment.create("$public$do"), "");
 			 * ArrayList<XDI3Segment> querySegments = new
 			 * ArrayList<XDI3Segment>();
 			 * querySegments.add(XDI3Segment.create(templateOwnerInumber +
 			 * "<+name>"));
 			 * querySegments.add(XDI3Segment.create(lcTemplateAddress));
 			 * ArrayList<XDI3Statement> queryStmts = new
 			 * ArrayList<XDI3Statement>();
 			 * queryStmts.add(XDI3Statement.create(templateOwnerInumber +
 			 * "/$is$ref/{}")); MessageResult responseFromRemoteCloud =
 			 * remoteCloud.sendQueries(querySegments, queryStmts,false);
 			 * 
 			 * Graph responseGraph = responseFromRemoteCloud.getGraph();
 			 * ContextNode responseRootContext =
 			 * responseGraph.getRootContextNode(); //get requested data fields
 			 * 
 			 * ArrayList<XDI3Segment> getDataFields = new
 			 * ArrayList<XDI3Segment>();
 			 * 
 			 * ReadOnlyIterator<Relation> getRelations =
 			 * responseRootContext.getAllRelations() ;
 			 * //g.getDeepRelations(XDI3Segment
 			 * .create(templateOwnerInumber),XDI3Segment.create("$get"));
 			 * for(Relation r : getRelations){
 			 * if(r.getArcXri().toString().equals("$get")){
 			 * getDataFields.add(r.getTargetContextNodeXri());
 			 * System.out.println(r.getTargetContextNodeXri()); }
 			 * 
 			 * } Literal requestingPartyNameLit =
 			 * responseRootContext.getDeepLiteral
 			 * (XDI3Segment.create(templateOwnerInumber + "<+name>&")); Relation
 			 * requestingPartyCloudnameRel =
 			 * responseRootContext.getDeepRelation(
 			 * XDI3Segment.create(templateOwnerInumber
 			 * ),XDI3Segment.create("$is$ref")); String
 			 * requestingPartyCloudNumberCtx =
 			 * requestingPartyCloudnameRel.getTargetContextNodeXri().toString();
 			 */
 			String meta_link_contract = "{$to}" + templateOwnerInumber
 					+ "{$from}" + templateOwnerInumber + "+registration$do";
 
 			PersonalCloud remoteCloud = PersonalCloud.open(
 					XDI3Segment.create(templateOwnerInumber), this.cloudNumber,
 					XDI3Segment.create("$public$do"), "");
 			ArrayList<XDI3Segment> querySegments = new ArrayList<XDI3Segment>();
 			querySegments.add(XDI3Segment.create(templateOwnerInumber
 					+ "<+name>"));
 
 			querySegments.add(XDI3Segment.create(lcTemplateAddress));
 			
 			ArrayList<XDI3Statement> queryStmts = new ArrayList<XDI3Statement>();
 			queryStmts.add(XDI3Statement.create(templateOwnerInumber
 					+ "/$is$ref/{}"));
 			MessageResult responseFromRemoteCloud = remoteCloud.sendQueries(
 					querySegments, queryStmts, false);
 
 			Graph responseGraph = responseFromRemoteCloud.getGraph();
 			ContextNode responseRootContext = responseGraph
 					.getRootContextNode();
 
 
 			// get requested data fields
 
 			ArrayList<XDI3Segment> getDataFields = new ArrayList<XDI3Segment>();
 
 			ReadOnlyIterator<Relation> getRelations = responseRootContext
 					.getAllRelations(); 
 			for (Relation r : getRelations) {
 				if (r.getArcXri().toString().equals("$get")) {
 					getDataFields.add(r.getTargetContextNodeXri());
 					System.out.println(r.getTargetContextNodeXri());
 				}
 
 			}
 
 			Literal requestingPartyNameLit = responseRootContext
 					.getDeepLiteral(XDI3Segment.create(templateOwnerInumber
 							+ "<+name>&"));
 			Relation requestingPartyCloudnameRel = responseRootContext
 					.getDeepRelation(XDI3Segment.create(templateOwnerInumber),
 							XDI3Segment.create("$is$ref"));
 			String requestingPartyCloudNumberCtx = requestingPartyCloudnameRel
 					.getTargetContextNodeXri().toString();
 
 			querySegments = new ArrayList<XDI3Segment>();
 			queryStmts = new ArrayList<XDI3Statement>();
 			for (XDI3Segment dataField : getDataFields) {
 				String dataFieldStr = dataField.toString();
 				dataFieldStr = dataFieldStr.replace("{$to}",
 						respondingPartyCloudNumber);
 
 				querySegments.add(XDI3Segment.create(dataFieldStr));
 			}
 			MessageResult responseFromThisCloud = this.sendQueries(
 					querySegments, queryStmts, false);
 
 			Graph responseGraph3 = responseFromThisCloud.getGraph();
 			ContextNode responseRootContext3 = responseGraph3
 					.getRootContextNode();
 			ReadOnlyIterator<Literal> allLiteralsFromResponse = responseRootContext3
 					.getAllLiterals();
 
 			// prepare authorization input HTML
 
 			StringBuffer buf = new StringBuffer();
 			buf.append("<html><head>");
 			
 			buf.append("<SCRIPT LANGUAGE=\"JavaScript\">");
 			buf.append("function buttonClick(val){ document.getElementById('buttonClicked').value = val; return true; }");
 			buf.append("</SCRIPT></head>");
 			buf.append("<body>");
 			buf.append("<div id=\"authz_form\" style=\"position: relative; top: 61px; left: 64px; z-index: 1000;display: block;\"><p>Link Contract Authorization Form</p>");
 			buf.append("<p>");
 			buf.append(requestingPartyNameLit.getLiteralDataString()
 					+ "(Cloud Name: "
 					+ requestingPartyCloudNumberCtx
 					+ ") is offering to connect to your Cloud via Respect Connect.</p>");
 			buf.append("<p>Please approve the link contract:</p>");
 			buf.append("<form action=\"" + WEBAPP_BASE_URI);
 			buf.append(URLEncoder.encode(respondingPartyCloudNumber, "UTF-8"));
 			buf.append("/connect/approve/\" method=\"post\">");
 
 			for (Literal lit : allLiteralsFromResponse) {
 				String str = new String("<input type=\"checkbox\" name=\"")
 						+ "fieldchoices" + "\"";
 				str += " value=\'";
 				// str += URLEncoder.encode(lit.getLiteralDataString(),"UTF-8");
 				str += lit.getContextNode().toString() + "|"
 						+ lit.getLiteralDataString();
 				str += "\'>";
 				str += lit.getLiteralDataString();
 				str += "</input>";
 				buf.append(str);
 			}
 			buf.append("<input type=\"hidden\" name=\"buttonClicked\" id=\"buttonClicked\"  /> <br>");
 			buf.append("<input type=\"hidden\" name=\"authToken\" value=\"");
 			buf.append(authToken);
 			buf.append("\">");
 			buf.append("</input>");
 			buf.append("<input type=\"hidden\" name=\"linkContractTemplateAddress\" value=\'");
 			buf.append(lcTemplateAddress);
 			buf.append("\'>");
 			buf.append("<input type=\"hidden\" name=\"relyingPartyCloudNumber\" value=\'");
 			buf.append(templateOwnerInumber);
 			buf.append("\'>");
 			buf.append("<input type=\"hidden\" name=\"successurl\" value=\""
 					+ successurl + "\">");
 			buf.append("</input>");
 			buf.append("<input type=\"hidden\" name=\"failureurl\" value=\""
 					+ failureurl + "\">");
 			buf.append("</input>");
 			buf.append("<input type=\"hidden\" name=\"cloudname\" value=\""
 					+ cloudname + "\">");
 			buf.append("</input>");
 			buf.append("<input type=\"hidden\" name=\"relayState\" value=\""
 					+ relayState + "\">");
 			buf.append("</input>");
 			buf.append("<input type=\"hidden\" name=\"connectRequest\" value=\'");
 			buf.append(connectRequest);
 			buf.append("\'>");
 			buf.append("<input type=\"submit\" value=\"Approve!\" onclick=\"return buttonClick('Approve')\" />");
 			buf.append("<input type=\"submit\" value=\"Reject!\" onclick=\"return buttonClick('Reject')\" />");
 			buf.append("</form>");
 			buf.append("</div>");
 			buf.append("</body>");
 			buf.append("</html>");
 
 			result = buf.toString();
 
 		} catch (Xdi2ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println("Result HTML:\n" + result);
 		return result;
 	}
 
 	public String processApprovalForm(String linkContractTemplateAddress,
 			String relyingPartyCloudNumber, String respondingPartyCloudNumber,
 			String secrettoken, String[] selectedValues, String successurl,
 			String failureurl, String cloudname, String relayState) {
 
 		String result = new String();
 		// create a link contract instance
 		/*
 		 * [=]!:uuid:2$to[@]!:uuid:1$from[@]!:uuid:1+registration$do/$get/[=]!:uuid
 		 * :2<+tel>
 		 * <<respondingPartyCloudNumber>>$to<<templateOwnerInumber>>$from
 		 * <<templateOwnerInumber>>+registration$do/$get/<<data element
 		 * address>>
 		 */
 		ArrayList<XDI3Statement> setStatements = new ArrayList<XDI3Statement>();
 		String isPlusstmt = new String();
 		isPlusstmt += respondingPartyCloudNumber;
 		isPlusstmt += "$to";
 		isPlusstmt += relyingPartyCloudNumber;
 		isPlusstmt += "$from";
 		isPlusstmt += relyingPartyCloudNumber;
 		isPlusstmt += "+registration$do/$is+/";
 		isPlusstmt += linkContractTemplateAddress;
 
 		setStatements.add(XDI3Statement.create(isPlusstmt));
 
 		String policyStmt = new String();
 		policyStmt += respondingPartyCloudNumber;
 		policyStmt += "$to";
 		policyStmt += relyingPartyCloudNumber;
 		policyStmt += "$from";
 		policyStmt += relyingPartyCloudNumber;
 		policyStmt += "+registration$do$if$and/$true/({$from}/$is/"
 				+ relyingPartyCloudNumber + ")";
 		setStatements.add(XDI3Statement.create(policyStmt));
 
 		policyStmt = new String();
 		policyStmt += respondingPartyCloudNumber;
 		policyStmt += "$to";
 		policyStmt += relyingPartyCloudNumber;
 		policyStmt += "$from";
 		policyStmt += relyingPartyCloudNumber;
 		policyStmt += "+registration$do$if$and/$true/({$msg}<$sig><$valid>&/&/true)";
 		setStatements.add(XDI3Statement.create(policyStmt));
 
 		String xdiResponseValues = new String();
 		
 		for (int i = 0; i < selectedValues.length; i++) {
 			String value = selectedValues[i];
 			StringTokenizer st = new StringTokenizer(value, "|");
 			String addressPart = st.nextToken();
 			String valuePart = st.nextToken();
 			xdiResponseValues += addressPart + "/&/" + "\"" + valuePart + "\"" + "\n";
 			// strip the last & off
 			addressPart = addressPart.substring(0, addressPart.length() - 1);
 			String stmt = new String();
 			stmt += respondingPartyCloudNumber;
 			stmt += "$to";
 			stmt += relyingPartyCloudNumber;
 			stmt += "$from";
 			stmt += relyingPartyCloudNumber;
 			stmt += "+registration$do/$get/";
 			stmt += addressPart;
 
 			System.out.println("Set statements :" + stmt);
 			setStatements.add(XDI3Statement.create(stmt));
 		}
 		System.out.println("All Set statements :" + setStatements);
 		MessageResult setResponse = this.setXDIStmts(setStatements);
 		System.out.println("Set response : " + setResponse);
 
 		String targetSegment = new String();
 		targetSegment += respondingPartyCloudNumber;
 		targetSegment += "$to";
 		targetSegment += relyingPartyCloudNumber;
 		targetSegment += "$from";
 		targetSegment += relyingPartyCloudNumber;
 		targetSegment += "+registration$do";
 
 		xdiResponseValues += targetSegment + "/$is+/" + linkContractTemplateAddress + "\n";
 		// send link contract to the relying party
 		// {$from}[@]!:uuid:1+registration$do
 //		String lcAddress = "{$to}" + relyingPartyCloudNumber + "{$from}"
 //				+ relyingPartyCloudNumber + "+registration$do";
 //		PersonalCloud relyingPartyPC = PersonalCloud.open(
 //				XDI3Segment.create(relyingPartyCloudNumber),
 //				this.senderCloudNumber, XDI3Segment.create(lcAddress), "");
 //		MessageResult setResponse2 = relyingPartyPC.setXDISegment(XDI3Segment
 //				.create(targetSegment));
 //		System.out.println("Set response 2 : " + setResponse2);
 
 		StringBuffer buf = new StringBuffer();
 		buf.append("<html><head></head><div id=\"approval_form\" style=\"position: relative; top: 61px; left: 64px; z-index: 1000;display: block;\">");
 		buf.append("<SCRIPT LANGUAGE=\"JavaScript\">");
 		buf.append("function submitForm() { document.forms['connectResponse'].submit(); }");
 		buf.append("</SCRIPT>");
 		buf.append("<body onload=\"submitForm()\">");
 //		buf.append("<p>Hello <b>" + cloudname + "</b>. Welcome back!");
 //		buf.append("<p>New Link Contracts have been established successfully!");
 //		buf.append("<p>");
 		// for(int i = 0 ; i < selectedValues.length ; i++){
 		// buf.append(selectedValues[i] + "<br>");
 		// }
 		buf.append("<form action=\"" + successurl
 				+ "\" method=\"post\" name=\"connectResponse\">");
 		buf.append("<input type=\"hidden\" name=\"cloudname\" value=\""
 				+ cloudname + "\">");
 		buf.append("</input>");
 		buf.append("<input type=\"hidden\" name=\"relayState\" value=\""
 				+ relayState + "\">");
 		buf.append("</input>");
 		buf.append("<input type=\"hidden\" name=\"cloudnumber\" value=\""
 				+ respondingPartyCloudNumber + "\">");
 		buf.append("</input>");
 		buf.append("<input type=\"hidden\" name=\"endpointuri\" value=\""
 				+ this.cloudEndpointURI + "\">");
 		buf.append("</input>");
 		buf.append("<input type=\"hidden\" name=\"xdiresponse\" value=\'"
 				+ xdiResponseValues
 				+ "\'>");
 		buf.append("</input>");
 		buf.append("<input type=\"hidden\" name=\"statuscode\" value=\""
 				+ "@respect.network*connection.manager$success" + "\">");
 		buf.append("</input>");
 		buf.append("<input type=\"submit\" value=\"submit\" style=\"display: none;\" border=\"0\"/>");
 		buf.append("</form>");
 		buf.append("</div>");
 		buf.append("</body>");
 		buf.append("</html>");
 
 		result = buf.toString();
 		System.out.println("Result HTML : \n" + result);
 		return result;
 	}
 	public boolean linkContractExists(String connectRequest){
 		System.out.println("\nChecking if a link contract exists\n");
 		boolean result = false;
 		MemoryJSONGraphFactory graphFactory = new MemoryJSONGraphFactory();
 		String templateOwnerInumber = null;
 		try {
 			Graph g = graphFactory.parseGraph(connectRequest);
 			// get remote cloud number
 
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(g,
 					System.out);
 			ContextNode c = g.getRootContextNode();
 			ReadOnlyIterator<ContextNode> allCNodes = c.getAllContextNodes();
 			for (ContextNode ci : allCNodes) {
 				if (ci.containsContextNode(XDI3SubSegment.create("[$msg]"))) {
 					templateOwnerInumber = ci.toString();
 					System.out.println(templateOwnerInumber);
 					break;
 				}
 			}
 			if (templateOwnerInumber == null) {
 				System.out
 						.println("No cloudnumber for requestor/template owner");
 				return result;
 			}
 			// get the address of the link contract template
 			// $set{$do}
 
 			String lcTemplateAddress = null;
 
 			ReadOnlyIterator<Relation> allRelations = c.getAllRelations(); // g.getDeepRelations(XDI3Segment.create(templateOwnerInumber),XDI3Segment.create("$get"));
 			for (Relation r : allRelations) {
 				if (r.getArcXri().toString().equals("$set{$do}")) {
 					lcTemplateAddress = r.getTargetContextNodeXri().toString();
 					System.out.println(r.getTargetContextNodeXri());
 				}
 
 			}
 			if (lcTemplateAddress == null) {
 				System.out.println("No LC template address provided");
 				return result;
 			}
 		}catch(Exception io){
 			io.printStackTrace();
 			return result;
 		}
 		
 		String isPlusstmt = new String();
 		isPlusstmt += this.cloudNumber;
 		isPlusstmt += "$to";
 		isPlusstmt += templateOwnerInumber;
 		isPlusstmt += "$from";
 		isPlusstmt += templateOwnerInumber;
 		isPlusstmt += "+registration$do";
 		
 		ArrayList<XDI3Segment> querySegments = new ArrayList<XDI3Segment>();
 		
 
 		querySegments.add(XDI3Segment.create(isPlusstmt));
 		
 		MessageResult responseFromRemoteCloud = this.sendQueries(
 				querySegments, null, false);
 
 		Graph responseGraph = responseFromRemoteCloud.getGraph();
 		ContextNode responseRootContext = responseGraph
 				.getRootContextNode();
 		System.out.println("\n\nLink Contract exists check\n\n" + responseGraph.toString() );
 		if(responseRootContext.getContextNodeCount() > 1){
 			result = true;
 		}
 		
 		return result;
 	}
 	public String autoSubmitForm(String cloudname , String connectRequest, String successurl , String relayState){
 		String result = new String("");
 		
 		
 		MemoryJSONGraphFactory graphFactory = new MemoryJSONGraphFactory();
 		String templateOwnerInumber = null;
 		String lcTemplateAddress = null;
 		String xdiResponse = new String();
 		try {
 			Graph g = graphFactory.parseGraph(connectRequest);
 			// get remote cloud number
 
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(g,
 					System.out);
 			ContextNode c = g.getRootContextNode();
 			ReadOnlyIterator<ContextNode> allCNodes = c.getAllContextNodes();
 			for (ContextNode ci : allCNodes) {
 				if (ci.containsContextNode(XDI3SubSegment.create("[$msg]"))) {
 					templateOwnerInumber = ci.toString();
 					System.out.println(templateOwnerInumber);
 					break;
 				}
 			}
 			if (templateOwnerInumber == null) {
 				System.out
 						.println("No cloudnumber for requestor/template owner");
 				return result;
 			}
 			// get the address of the link contract template
 			// $set{$do}
 
 			
 
 			ReadOnlyIterator<Relation> allRelations = c.getAllRelations(); // g.getDeepRelations(XDI3Segment.create(templateOwnerInumber),XDI3Segment.create("$get"));
 			for (Relation r : allRelations) {
 				if (r.getArcXri().toString().equals("$set{$do}")) {
 					lcTemplateAddress = r.getTargetContextNodeXri().toString();
 					System.out.println(r.getTargetContextNodeXri());
 				}
 
 			}
 			if (lcTemplateAddress == null) {
 				System.out.println("No LC template address provided");
 				return result;
 			}
 			// get requested data fields
 
 			ArrayList<XDI3Segment> getDataFields = new ArrayList<XDI3Segment>();
 
 			ReadOnlyIterator<Relation> getRelations = c
 					.getAllRelations(); 
 			for (Relation r : getRelations) {
 				if (r.getArcXri().toString().equals("$get")) {
 					getDataFields.add(r.getTargetContextNodeXri());
 					System.out.println(r.getTargetContextNodeXri());
 				}
 
 			}
 
 			ArrayList<XDI3Segment> querySegments = new ArrayList<XDI3Segment>();
 			for (XDI3Segment dataField : getDataFields) {
 				String dataFieldStr = dataField.toString();
 				dataFieldStr = dataFieldStr.replace("{$to}",
 						this.cloudNumber.toString());
 
 				querySegments.add(XDI3Segment.create(dataFieldStr));
 			}
 			
 			MessageResult responseFromThisCloud = this.sendQueries(
 					querySegments, null, false);
 			Graph responseGraph3 = responseFromThisCloud.getGraph();
 			ContextNode responseRootContext3 = responseGraph3
 					.getRootContextNode();
 			ReadOnlyIterator<Literal> allLiteralsFromResponse = responseRootContext3
 					.getAllLiterals();
 
 			
 			for (Literal lit : allLiteralsFromResponse) {
 				xdiResponse += lit.getContextNode().toString() + "/&/"
 						+ "\"" + lit.getLiteralDataString() + "\"\n";
 			}
 			String targetSegment = new String();
 			targetSegment += this.cloudNumber;
 			targetSegment += "$to";
 			targetSegment += templateOwnerInumber;
 			targetSegment += "$from";
 			targetSegment += templateOwnerInumber;
 			targetSegment += "+registration$do";
 			
 			xdiResponse += targetSegment + "/$is+/" + lcTemplateAddress;
 		}catch(Exception io){
 			io.printStackTrace();
 			return result;
 		}
 
 
 		
 		
 		
 //		String linkContractTemplateAddress = new String("{$from}");
 //		linkContractTemplateAddress += templateOwnerInumber;
 //		linkContractTemplateAddress += "+registration$do";
 				
 		StringBuffer buf = new StringBuffer();
 		buf.append("<html><head></head><div id=\"approval_form\" style=\"position: relative; top: 61px; left: 64px; z-index: 1000;display: block;\">");
 		buf.append("<SCRIPT LANGUAGE=\"JavaScript\">");
 		buf.append("function submitForm() { document.forms['connectResponse'].submit(); }");
 		buf.append("</SCRIPT>");
 		buf.append("<body onload=\"submitForm()\">");
 //		buf.append("<p>Hello <b>" + cloudname + "</b>. Welcome back!");
 //		buf.append("<p>New Link Contracts have been established successfully!");
 //		buf.append("<p>");
 		// for(int i = 0 ; i < selectedValues.length ; i++){
 		// buf.append(selectedValues[i] + "<br>");
 		// }
 		buf.append("<form action=\"" + successurl
 				+ "\" method=\"post\" name=\"connectResponse\">");
 		buf.append("<input type=\"hidden\" name=\"cloudname\" value=\""
 				+ cloudname + "\">");
 		buf.append("</input>");
 		buf.append("<input type=\"hidden\" name=\"relayState\" value=\""
 				+ relayState + "\">");
 		buf.append("</input>");
 		buf.append("<input type=\"hidden\" name=\"cloudnumber\" value=\""
 				+ this.cloudNumber + "\">");
 		buf.append("</input>");
 		buf.append("<input type=\"hidden\" name=\"endpointuri\" value=\""
 				+ this.cloudEndpointURI + "\">");
 		buf.append("</input>");
 		buf.append("<input type=\"hidden\" name=\"xdiresponse\" value=\'"
 				+ xdiResponse
 				+ "\'>");
 		buf.append("</input>");
 		buf.append("<input type=\"hidden\" name=\"statuscode\" value=\""
 				+ "@respect.network*connection.manager$success" + "\">");
 		buf.append("</input>");
 		buf.append("<input type=\"submit\" value=\"submit\" style=\"display: none;\" border=\"0\"/>");
 		buf.append("</form>");
 		buf.append("</div>");
 		buf.append("</body>");
 		buf.append("</html>");
 
 		result = buf.toString();
 		System.out.println("Result HTML : \n" + result);
 		return result;
 
 	}
 	public String autoSubmitRejectForm(String cloudname , String connectRequest, String failureurl , String relayState){
 		String result = new String("");
 		
 		
 		MemoryJSONGraphFactory graphFactory = new MemoryJSONGraphFactory();
 		String templateOwnerInumber = null;
 		String lcTemplateAddress = null;
 		try {
 			Graph g = graphFactory.parseGraph(connectRequest);
 			// get remote cloud number
 
 			XDIWriterRegistry.forFormat("XDI DISPLAY", null).write(g,
 					System.out);
 			ContextNode c = g.getRootContextNode();
 			ReadOnlyIterator<ContextNode> allCNodes = c.getAllContextNodes();
 			for (ContextNode ci : allCNodes) {
 				if (ci.containsContextNode(XDI3SubSegment.create("[$msg]"))) {
 					templateOwnerInumber = ci.toString();
 					System.out.println(templateOwnerInumber);
 					break;
 				}
 			}
 			if (templateOwnerInumber == null) {
 				System.out
 						.println("No cloudnumber for requestor/template owner");
 				return result;
 			}
 			// get the address of the link contract template
 			// $set{$do}
 
 			
 
 			ReadOnlyIterator<Relation> allRelations = c.getAllRelations(); // g.getDeepRelations(XDI3Segment.create(templateOwnerInumber),XDI3Segment.create("$get"));
 			for (Relation r : allRelations) {
 				if (r.getArcXri().toString().equals("$set{$do}")) {
 					lcTemplateAddress = r.getTargetContextNodeXri().toString();
 					System.out.println(r.getTargetContextNodeXri());
 				}
 
 			}
 			if (lcTemplateAddress == null) {
 				System.out.println("No LC template address provided");
 				return result;
 			}
 		}catch(Exception io){
 			io.printStackTrace();
 			return result;
 		}
 
 		
 		
 //		String linkContractTemplateAddress = new String("{$from}");
 //		linkContractTemplateAddress += templateOwnerInumber;
 //		linkContractTemplateAddress += "+registration$do";
 				
 		StringBuffer buf = new StringBuffer();
 		buf.append("<html><head></head><div id=\"rejection_form\" style=\"position: relative; top: 61px; left: 64px; z-index: 1000;display: block;\">");
 		buf.append("<SCRIPT LANGUAGE=\"JavaScript\">");
 		buf.append("function submitForm() { document.forms['connectResponseReject'].submit(); }");
 		buf.append("</SCRIPT>");
 		buf.append("<body onload=\"submitForm()\">");
 //		buf.append("<p>Hello <b>" + cloudname + "</b>. Welcome back!");
 //		buf.append("<p>Link Contract request has been cancelled by the user!");
 //		buf.append("<p>");
 		// for(int i = 0 ; i < selectedValues.length ; i++){
 		// buf.append(selectedValues[i] + "<br>");
 		// }
 		buf.append("<form action=\"" + failureurl
 				+ "\" method=\"post\" name=\"connectResponseReject\">");
 		buf.append("<input type=\"hidden\" name=\"cloudname\" value=\""
 				+ cloudname + "\">");
 		buf.append("</input>");
 		buf.append("<input type=\"hidden\" name=\"relayState\" value=\""
 				+ relayState + "\">");
 		buf.append("</input>");
 		buf.append("<input type=\"hidden\" name=\"cloudnumber\" value=\""
 				+ this.cloudNumber + "\">");
 		buf.append("</input>");
 		buf.append("<input type=\"hidden\" name=\"statuscode\" value=\""
 				+ "@respect.network*connection.manager+user+canceled+request$error" + "\">");
 		buf.append("</input>");
 		buf.append("<input type=\"submit\" value=\"submit\" style=\"display: none;\" border=\"0\"/>");
 		buf.append("</form>");
 		buf.append("</div>");
 		buf.append("</body>");
 		buf.append("</html>");
 
 		result = buf.toString();
 		System.out.println("Result HTML : \n" + result);
 		return result;
 
 	}
 
 	public String processDisconnectRequest(String requestingParty, String respondingParty){
 		
 		String targetSegment = new String();
 		targetSegment += this.cloudNumber;
 		targetSegment += "$to";
 		targetSegment += requestingParty;
 		targetSegment += "$from";
 		targetSegment += requestingParty;
 		targetSegment += "+registration$do";
 		MessageResult result = this.delXDIStmts(null, XDI3Segment.create(targetSegment));
 		System.out.println("Result of delete lc :\n" + result.toString());
 		
 		return "<html><body>Deletion of LC was successful!</body></html>";
 	}
 
 }
