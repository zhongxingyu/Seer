 package rcp.example;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.DocumentBuilder;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Node;
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 
 import java.io.File;
 import java.io.IOException;
 import java.math.BigDecimal;
import java.math.RoundingMode;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.dma.utils.eclipse.UIHelper;
 import org.dma.utils.eclipse.swt.DialogHandler;
 import org.dma.utils.java.Debug;
 import org.dma.utils.java.apache.mail.EmailAddress;
 import org.dma.utils.java.apache.mail.EmailParameters;
 import org.dma.utils.java.array.ErrorList;
 import org.eclipse.swt.printing.PrintDialog;
 import org.eclipse.swt.printing.PrinterData;
 
 import java.lang.Number;
 
 import rcp.Application;
 import rcp.colibri.core.BIRT.report.BIRTReport;
 import rcp.colibri.core.mappers.filters.FilterMap;
 import rcp.colibri.core.mappers.filters.FilterMap.OPERANDS.TYPES;
 import rcp.colibri.core.mappers.filters.FilterMap.OPERATORS;
 import rcp.colibri.core.mappers.filters.FilterOperandMap;
 import rcp.colibri.core.vars.database.DatabaseVARS;
 import rcp.colibri.core.vars.database.PopulateVARS;
 import rcp.colibri.core.vars.database.DatabaseVARS.FIELDS;
 import rcp.colibri.core.vars.database.PopulateVARS.ENTIDADESTIPOS;
 import rcp.colibri.dao.business.process.EntidadesdocumentosProcess;
 import rcp.colibri.dao.business.process.EntidadesdocumentoslinhasProcess;
 import rcp.colibri.dao.business.rules.EntidadesdocumentosRules;
 import rcp.colibri.dao.business.rules.EntidadesdocumentoslinhasRules;
 import rcp.colibri.dao.database.ColibriDatabase;
 import rcp.colibri.dao.database.model.Entidades;
 import rcp.colibri.dao.database.model.Artigos;
 import rcp.colibri.dao.database.model.Entidadesdocumentos;
 import rcp.colibri.dao.database.model.Entidadesdocumentoslinhas;
 import rcp.colibri.dao.database.query.QueryDefinition;
 import rcp.colibri.dao.database.query.QueryDefinition.QUERY_ORDER;
 import rcp.colibri.workbench.processamento.entidades.facturacao.FacturacaoPreference.TIPO;
 import rcp.colibri.xml.licencas.ValidadeDocument;
 
 public class DOMOrderParser {
 	public void ParseOrders(String ordersFile, Document output,String printerName) {
 		System.out.println("Begin parse orders");
 		try {
 
 			Element orders = output.createElement("Orders");
 			Element rootElement = output.getDocumentElement();
 			rootElement.appendChild(orders);
 
 			File fXmlFile = new File(ordersFile);
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
 					.newInstance();
 			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 			Document doc = dBuilder.parse(fXmlFile);
 			doc.getDocumentElement().normalize();
 
 			
 			NodeList nList = doc.getElementsByTagName("order");
 			System.out.println("Parsing " + nList.getLength()+" orders");
 
 			for (int temp = 0; temp < nList.getLength(); temp++) {
 				Node nNode = nList.item(temp);
 				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 
 					Element eElement = (Element) nNode;
 					try {
 						Element order = output.createElement("Order");
 						orders.appendChild(order);
 						Element ide = output.createElement("presta_id");
 						ide.appendChild(output.createTextNode(getTagValue("id",
 								eElement)));
 						order.appendChild(ide);
 
 						// cria objecto documento
 						Entidadesdocumentos documento = new Entidadesdocumentos(
 								ColibriDatabase
 										.loadDocumentostipos(PopulateVARS.DOCUMENTOSTIPOS.clientes_factura.codigo));
 
 						// carrega uma entidade do tipo CLIENTE
 						Entidades entidade = getEntidadeById(getTagValue(
 								"id_address_invoice", eElement));
 						String envio = getTagValue("total_shipping", eElement);
						System.out.println("SHIPPING:"+envio);
 						String total_paid = getTagValue("total_paid_real",
 								eElement);
 						// inicializa a entidade do documento
 						EntidadesdocumentosRules.init(documento, entidade);
 						documento.setEntidade(entidade);
 
 						Element idec = output.createElement("colibri_id");
 						idec.appendChild(output.createTextNode(documento
 								.getNumerodocumentoToText()));
 						order.appendChild(idec);
 
 						// cria as linhas do documento
 						NodeList nOrder_row = eElement
 								.getElementsByTagName("order_row");
 						System.out.println("LENGHT" + nOrder_row.getLength());
 						for (int temp2 = 0; temp2 < nOrder_row.getLength(); temp2++) {
 							Node nNode2 = nOrder_row.item(temp2);
 							if (nNode2.getNodeType() == Node.ELEMENT_NODE) {
 								Element orderElement = (Element) nNode2;
 								System.out.println("ARTIGO"
 										+ getTagValue("product_id",
 												orderElement));
 								System.out.println("NOVO ARTIGO"
 										+ getArtigoById(getTagValue(
 												"product_id", orderElement)));
 								createLinhasdocumento(
 										documento,
 										entidade,
 										getArtigoById(getTagValue("product_id",
 												orderElement)),
 										Long.valueOf(getTagValue(
 												"product_quantity",
 												orderElement)));
 							}
 						}
 						createEnviodocumento(documento, entidade, envio);
 
 						// processa regras
 						EntidadesdocumentosProcess.rules(documento,
 								documento.getLinhasdocumento());
 						if (documento.getTotal()
 								.subtract(new BigDecimal(total_paid)).abs()
 								.compareTo(new BigDecimal("0.1")) == 1) {
 							System.out.println("Order ID:"
 									+ getTagValue("id", eElement));
 							System.out.println("TOTAL DONT MATCH NOT SAVING!!");
 							System.out.println("PRESTA TOTAL:" + total_paid
 									+ " COLIBRI TOTAL:" + documento.getTotal());
 
 							Element priceComp = output
 									.createElement("price_compare");
 							priceComp.appendChild(output.createTextNode("NOK"));
 							order.appendChild(priceComp);
 						} else {
 							System.out.println("Order ID:"
 									+ getTagValue("id", eElement));
 							System.out
 									.println("TOTAL MATCH, SAVING AND PRINTING!!");
 							// finaliza processamento
 							EntidadesdocumentosProcess.finish(documento,
 									documento.getLinhasdocumento(), false);
 							// grava o documento na base de dados
 							ErrorList error = ColibriDatabase
 									.storeEntidadesdocumentos(documento, false);
 
 							// apresenta possiveis erros
 							DialogHandler.error(error.getErrors());
 							printInvoice(documento.getNumerodocumento(),printerName);
 							Element priceComp = output
 									.createElement("price_compare");
 							priceComp.appendChild(output.createTextNode("OK"));
 							order.appendChild(priceComp);
 						}
 
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void createLinhasdocumento(Entidadesdocumentos documento,
 			Entidades entidade, Artigos artigo, long quantidade) {
 		try {
 			// cria objecto linha
 			Entidadesdocumentoslinhas linha = documento.createLinhasdocumento();
 
 			// inicializa a linha
 			EntidadesdocumentoslinhasRules.init(linha, entidade, artigo);
 
 			// actualiza a quantidade
 			linha.setQuantidade(BigDecimal.valueOf(quantidade));
 
 			// processa a linha
 			EntidadesdocumentoslinhasProcess.rules(documento, linha);
 
 			// adiciona a linha ao documento
 			documento.addLinhasdocumento(linha);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public void createEnviodocumento(Entidadesdocumentos documento,
 			Entidades entidade, String valor) {
		System.out.println("SHIPPING function "+valor);
 		try {
 			// cria objecto linha
 			Entidadesdocumentoslinhas linha = documento.createLinhasdocumento();
 			Artigos artigo;
 			// inicializa a linha
			//EntidadesdocumentoslinhasRules.init(linha, entidade, artigo);
 			linha.setDescricao("Shipping and handling");
 			// actualiza a quantidade
 			linha.setQuantidade(new BigDecimal(1));
 			if (linha.getCodigoivaToText().contains("EXP"))
 				linha.setPreco(new BigDecimal(valor));
 			else {
 				BigDecimal v = new BigDecimal(valor);
				v = v.divide(new BigDecimal("1.23"),2, RoundingMode.HALF_UP);
 				linha.setPreco(v);
 			}
 
 			// processa a linha
 			EntidadesdocumentoslinhasProcess.rules(documento, linha);
 
 			// adiciona a linha ao documento
 			documento.addLinhasdocumento(linha);
			System.out.println("ADDED SHIPPING TO INVOICE");
 		} catch (Exception e) {
 			e.printStackTrace();
			System.out.println(e.toString());
 		}
 
 	}
 
 	private Entidades getEntidadeById(String id) {
 		id = "presta:" + id;
 		Collection<Entidades> collection = ColibriDatabase
 				.getCollectionEntidades(new QueryDefinition(
 						new FilterMap(DatabaseVARS.entidades).addRule(
 								DatabaseVARS.FIELDS.entidades_obs.name,
 								FilterMap.OPERATORS.MATH.EQUAL,
 								new FilterOperandMap(id, TYPES.VALUE)),
 						new String[] { DatabaseVARS.FIELDS.entidades_key.name },
 						QueryDefinition.QUERY_ORDER.ASCENDING));
 		if (collection.size() < 1) {
 			System.out
 					.println("getEntidadeById couldn't find customer "+id+" returning null");
 			return null;
 		} else {
 			Iterator<Entidades> iterator = collection.iterator();
 			Entidades entidade = ColibriDatabase.loadEntidades(iterator.next()
 					.getKey());
 			return entidade;
 		}
 	}
 
 	private Artigos getArtigoById(String id) {
 		id = "presta:" + id;
 		Collection<Artigos> collection = ColibriDatabase
 				.getCollectionArtigos(new QueryDefinition(
 						new FilterMap(DatabaseVARS.artigos).addRule(
 								DatabaseVARS.FIELDS.artigos_obs.name,
 								FilterMap.OPERATORS.MATH.EQUAL,
 								new FilterOperandMap(id, TYPES.VALUE)),
 						new String[] { DatabaseVARS.FIELDS.artigos_codigo.name },
 						QueryDefinition.QUERY_ORDER.ASCENDING));
 		if (collection.size() < 1) {
 			System.out
 					.println("getArtigoById couldn't find customer returning null");
 			return null;
 		} else {
 			Iterator<Artigos> iterator = collection.iterator();
 			Artigos artigo = ColibriDatabase.loadArtigos(iterator.next()
 					.getCodigo());
 			return artigo;
 		}
 	}
 
 	private static String getTagValue(String sTag, Element eElement) {
 		NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
 				.getChildNodes();
 		if (nlList.getLength() == 0)
 			return "";
 		Node nValue = (Node) nlList.item(0);
 		return nValue.getNodeValue();
 	}
 
 	private void printInvoice(int numeroDoc,String printerName) {
 		String tipodocumento = PopulateVARS.DOCUMENTOSTIPOS.clientes_factura.codigo;
 		// primeira serie do documento
 		String serie = ColibriDatabase.loadDocumentosseries(tipodocumento, 0)
 				.getSerie();
 		process(tipodocumento, serie, numeroDoc, BIRTReport.ACTION_PRINT,printerName);
 	}
 
 	public void process(String tipodocumento, String serie,
 			int numerodocumento, int action,String printerName) {
 
 		BIRTReport report = initReport(tipodocumento, serie, numerodocumento,
 				action);
 		String file = report.process();
 		System.out.println("Output file: " + file);
 		System.out.println("PRINTER ISEMPTY="+printerName.isEmpty());
 		if(!printerName.isEmpty())
 		{
 			report.output(printerName);
 			System.out.println("PRINTING TO PRINTER:"+printerName);
 		}
 		else
 		{
 			System.out.println("PRINTING WITH DIALOG");
 			 PrintDialog dialog = new PrintDialog(UIHelper.getWorkbenchShell());
 			 PrinterData printerData = dialog.open();
 			 printerName = (printerData == null) ? null : printerData.name;
 			report.output(printerName);
 		}
 	}
 
 	/**
 	 * Inicializa o report
 	 */
 	public BIRTReport initReport(String tipodocumento, String serie,
 			int numerodocumento, int action) {
 
 		BIRTReport report = new BIRTReport(action,
 				PopulateVARS.TEMPLATES.entidadesdocumentos_factura.codigo,
 				new FilterMap(DatabaseVARS.entidadesdocumentos).addRule(
 						FIELDS.entidadesdocumentos_key.name,
 						OPERATORS.MATH.EQUAL,
 						new FilterOperandMap(Entidadesdocumentos.generateKey(
 								tipodocumento, serie, numerodocumento))),
 				new EmailParameters(new EmailAddress[] { new EmailAddress("",
 						"") }, "Envio de documento " + numerodocumento));
 
 		return report;
 
 	}
 
 }
