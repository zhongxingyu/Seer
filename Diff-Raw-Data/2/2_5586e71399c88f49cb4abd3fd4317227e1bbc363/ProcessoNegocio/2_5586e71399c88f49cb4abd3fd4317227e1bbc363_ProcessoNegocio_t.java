 package br.ipt.servico.relevancia.bpel;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.StringReader;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.stream.StreamSource;
 
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import br.ipt.servico.relevancia.excecao.BPAlreadyExistsException;
 import br.ipt.servico.relevancia.modelo.TDefinitions;
 import br.ipt.servico.relevancia.modelo.TDocumented;
 import br.ipt.servico.relevancia.modelo.TOperation;
 import br.ipt.servico.relevancia.modelo.TPartnerLink;
 import br.ipt.servico.relevancia.modelo.TPort;
 import br.ipt.servico.relevancia.modelo.TPortType;
 import br.ipt.servico.relevancia.modelo.TProcess;
 import br.ipt.servico.relevancia.modelo.TService;
 import br.ipt.servico.relevancia.util.Arquivo;
 
 /**
  * Definicao de um processo de negocio, tipicamente proveniente de um arquivo
  * XML com extensao <code>.bpel</code>.
  * 
  * @author Rodrigo Mendes Leme
  */
 public class ProcessoNegocio implements Comparable<ProcessoNegocio> {
 
     private static final String ARQUIVO_DESCRITOR_ORACLE_BPEL = "bpel.xml";
 
     private static Logger log = Logger.getLogger(ProcessoNegocio.class);
 
     protected int id;
 
     protected String nome;
 
     protected DocumentoBPEL bpel;
 
     protected String url;
 
     protected List<PartnerLink> partnerLinks;
 
     ProcessoNegocio(DocumentoBPEL bpel) {
 	this.bpel = bpel;
 	this.partnerLinks = new ArrayList<PartnerLink>();
     }
 
     public ProcessoNegocio(int id, String nome, DocumentoBPEL bpel, String url) {
 	this.id = id;
 	this.nome = nome;
 	this.bpel = bpel;
 	this.url = url;
 	this.partnerLinks = new ArrayList<PartnerLink>();
     }
 
     public int getId() {
 	return this.id;
     }
 
     void setId(int id) {
 	this.id = id;
     }
 
     public String getNome() {
 	return this.nome;
     }
 
     public DocumentoBPEL getBpel() {
 	return this.bpel;
     }
 
     public String getURL() {
 	return this.url;
     }
 
     public List<PartnerLink> getPartnerLinks() {
 	return this.partnerLinks;
     }
 
     public String toString() {
 	return this.getNome();
     }
 
     public boolean equals(Object obj) {
 	if (obj == null || !(obj instanceof ProcessoNegocio)) {
 	    return false;
 	}
 
 	return this.id == ((ProcessoNegocio) obj).getId();
     }
 
     public int hashCode() {
 	return this.id;
     }
 
     public int compareTo(ProcessoNegocio processoNegocio) {
 	return new Integer(this.id).compareTo(processoNegocio.getId());
     }
 
    public String obterURLPartnerLink(String nomePartnerLink) {
 	for (PartnerLink partnerLink : this.partnerLinks) {
 	    if (partnerLink.getNome().equals(nomePartnerLink)) {
 		return partnerLink.getURLServico();
 	    }
 	}
 	return null;
     }
 
     public static ProcessoNegocio importar(File arquivoBPEL,
 	    String urlProcessoNegocio) throws FileNotFoundException,
 	    IOException, SQLException, IllegalArgumentException,
 	    BPAlreadyExistsException {
 	log.info("Iniciando importacao de processo de negocio (arquivo: "
 		+ arquivoBPEL.getName() + ").");
 
 	if (urlProcessoNegocio == null || urlProcessoNegocio.trim().equals("")) {
 	    throw new IllegalArgumentException(
 		    "urlProcessoNegocio nao pode ser nulo.");
 	}
 
 	ProcessoNegocio pn = new ProcessoNegocio(new DocumentoBPEL(Arquivo
 		.lerArquivo(arquivoBPEL)));
 	pn.url = urlProcessoNegocio;
 	pn.getPartnerLinks().addAll(pn.importarConteudoBPEL(arquivoBPEL));
 
 	BpelDAO dao = new BpelDAO();
 	if (dao.existeProcessoNegocio(pn.getNome())) {
 	    throw new BPAlreadyExistsException("Processo de negocio \""
 		    + pn.getNome() + "\" ja existente, nao pode ser importado.");
 	} else {
 	    Map<String, String> partnerLinkBindings = pn
 		    .importarDescritorBPEL(arquivoBPEL.getParent());
 
 	    for (PartnerLink partnerLink : pn.getPartnerLinks()) {
 		pn.lerArquivoWSDLRef(arquivoBPEL.getParent(), partnerLink,
 			partnerLinkBindings);
 	    }
 
 	    dao.criarProcessoNegocio(pn);
 
 	    log.info("Processo de negocio \"" + pn.getNome()
 		    + "\" importado com sucesso.");
 	}
 
 	return pn;
     }
 
     private List<PartnerLink> importarConteudoBPEL(File arquivoBPEL)
 	    throws IOException {
 	List<PartnerLink> partnerLinks = new ArrayList<PartnerLink>();
 
 	try {
 	    TProcess process = JAXBContext.newInstance(TProcess.class)
 		    .createUnmarshaller().unmarshal(
 			    new StreamSource(new StringReader(this.bpel
 				    .getConteudo())), TProcess.class)
 		    .getValue();
 	    this.nome = process.getName();
 	    log.debug("Carregou arquivo BPEL em memoria (processo de negocio: "
 		    + this.nome + ").");
 
 	    for (TPartnerLink partnerLink : process.getPartnerLinks()
 		    .getPartnerLink()) {
 		if (!partnerLink.getName().equals("client")) {
 		    log.debug("Encontrou partner link: "
 			    + partnerLink.getName());
 		    partnerLinks.add(new PartnerLink(this, partnerLink
 			    .getName(), partnerLink.getPartnerRole(),
 			    partnerLink.getPartnerLinkType().getLocalPart()));
 		}
 	    }
 	} catch (JAXBException e) {
 	    throw new IOException(
 		    "Erro: nao conseguiu fazer o parsing do arquivo "
 			    + arquivoBPEL.getName() + ".", e);
 	}
 
 	return partnerLinks;
     }
 
     private Map<String, String> importarDescritorBPEL(String caminho)
 	    throws IOException {
 	Map<String, String> partnerLinkBindings = new HashMap<String, String>();
 
 	File arquivoDescritor = new File(caminho + File.separator
 		+ ARQUIVO_DESCRITOR_ORACLE_BPEL);
 	try {
 	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 	    DocumentBuilder db = dbf.newDocumentBuilder();
 	    Document doc = db.parse(arquivoDescritor);
 	    log.debug("Lendo arquivo " + arquivoDescritor.getName() + ".");
 
 	    NodeList partnerLinkBindingsTag = doc
 		    .getElementsByTagName("partnerLinkBinding");
 	    for (int i = 0; i < partnerLinkBindingsTag.getLength(); i++) {
 		Element partnerLinkBindingTag = (Element) partnerLinkBindingsTag
 			.item(i);
 		if (!partnerLinkBindingTag.getAttribute("name")
 			.equals("client")) {
 		    NodeList properties = partnerLinkBindingTag
 			    .getElementsByTagName("property");
 		    for (int j = 0; j < properties.getLength(); j++) {
 			Element propertyTag = (Element) properties.item(j);
 			if (propertyTag.getAttribute("name").equals(
 				"wsdlLocation")) {
 			    partnerLinkBindings.put(partnerLinkBindingTag
 				    .getAttribute("name"), propertyTag
 				    .getTextContent());
 			}
 		    }
 		}
 	    }
 	} catch (ParserConfigurationException e) {
 	    throw new IOException(
 		    "Erro: nao conseguiu carregar a configuracao do parser DOM da JVM.",
 		    e);
 	} catch (SAXException e) {
 	    throw new IOException(
 		    "Erro: nao conseguiu fazer o parsing do arquivo "
 			    + arquivoDescritor + ".", e);
 	}
 
 	return partnerLinkBindings;
     }
 
     private void lerArquivoWSDLRef(String caminho, PartnerLink partnerLink,
 	    Map<String, String> partnerLinkBindings) throws IOException {
 	String definitionsName = null;
 	String wsdlPath = null;
 	String partnerLinkTypeName = null;
 	String roleName = null;
 	String portTypeName = null;
 
 	File arquivoWSDLRef = new File(caminho + File.separator
 		+ partnerLinkBindings.get(partnerLink.getNome()));
 	try {
 	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 	    DocumentBuilder db = dbf.newDocumentBuilder();
 	    Document doc = db.parse(arquivoWSDLRef);
 
 	    NodeList definitions = doc.getElementsByTagName("definitions");
 	    for (int i = 0; i < definitions.getLength(); i++) {
 		Element definitionsTag = (Element) definitions.item(i);
 
 		definitionsName = definitionsTag.getAttribute("name");
 
 		Element importTag = (Element) definitionsTag
 			.getElementsByTagName("import").item(0);
 		wsdlPath = importTag.getAttribute("location");
 
 		Element partnerLinkTypeTag = (Element) definitionsTag
 			.getElementsByTagName("plnk:partnerLinkType").item(0);
 		partnerLinkTypeName = partnerLinkTypeTag.getAttribute("name");
 
 		Element roleTag = (Element) partnerLinkTypeTag
 			.getElementsByTagName("plnk:role").item(0);
 		roleName = roleTag.getAttribute("name");
 
 		Element portTypeTag = (Element) roleTag.getElementsByTagName(
 			"plnk:portType").item(0);
 		portTypeName = portTypeTag.getAttribute("name").split(":")[1];
 	    }
 	} catch (ParserConfigurationException e) {
 	    throw new IOException(
 		    "Erro: nao conseguiu carregar a configuracao do parser DOM da JVM.",
 		    e);
 	} catch (SAXException e) {
 	    throw new IOException(
 		    "Erro: nao conseguiu fazer o parsing do arquivo "
 			    + arquivoWSDLRef + ".", e);
 	}
 
 	if (partnerLink.equals(definitionsName, roleName, partnerLinkTypeName)) {
 	    File arquivoWsdl = new File(caminho + File.separator + wsdlPath);
 	    partnerLink.setWsdl(new DocumentoWSDL(Arquivo
 		    .lerArquivo(arquivoWsdl)));
 	    log.debug("Partner link \"" + partnerLink.getNome()
 		    + "\": carregou arquivo WSDL (" + arquivoWsdl.getName()
 		    + ").");
 
 	    try {
 		partnerLink.getOperacoes().addAll(
 			this.importarOperacoesWSDL(partnerLink.getWsdl(),
 				portTypeName));
 	    } catch (JAXBException e) {
 		throw new IOException(
 			"Erro: nao conseguiu importar as operacoes do arquivo WSDL "
 				+ arquivoWsdl.getName() + ".", e);
 	    }
 	    log.debug("Partner link \"" + partnerLink.getNome()
 		    + "\": carregou operacoes (" + partnerLink.getOperacoes()
 		    + ").");
 
 	    try {
 		partnerLink.setURLServico(this.obterURLServico(partnerLink
 			.getWsdl()));
 	    } catch (JAXBException e) {
 		throw new IOException(
 			"Erro: nao conseguir importar a URL do servico do arquivo WSDL "
 				+ arquivoWsdl.getName() + ".", e);
 	    }
 	    log.debug("Partner link \"" + partnerLink.getNome()
 		    + "\": carregou URL do servico ("
 		    + partnerLink.getURLServico() + ").");
 	}
     }
 
     private List<String> importarOperacoesWSDL(DocumentoWSDL wsdl,
 	    String nomePortType) throws JAXBException {
 	List<String> operacoes = new ArrayList<String>();
 
 	TDefinitions w = JAXBContext.newInstance(TDefinitions.class)
 		.createUnmarshaller().unmarshal(
 			new StreamSource(new StringReader(wsdl.getConteudo())),
 			TDefinitions.class).getValue();
 
 	for (TDocumented d : w.getAnyTopLevelOptionalElement()) {
 	    if (d instanceof TPortType) {
 		TPortType portType = (TPortType) d;
 		if (portType.getName().equals(nomePortType)) {
 		    for (TOperation operation : portType.getOperation()) {
 			operacoes.add(operation.getName());
 		    }
 		}
 	    }
 	}
 
 	return operacoes;
     }
 
     private String obterURLServico(DocumentoWSDL wsdl) throws JAXBException {
 	String urlServico = null;
 
 	TDefinitions w = JAXBContext.newInstance(TDefinitions.class)
 		.createUnmarshaller().unmarshal(
 			new StreamSource(new StringReader(wsdl.getConteudo())),
 			TDefinitions.class).getValue();
 
 	for (TDocumented d : w.getAnyTopLevelOptionalElement()) {
 	    if (d instanceof TService) {
 		TPort port = ((TService) d).getPort().get(0);
 		Element addressTag = (Element) port.getAny().get(0);
 		urlServico = addressTag.getAttribute("location");
 	    }
 	}
 
 	return urlServico;
     }
 }
