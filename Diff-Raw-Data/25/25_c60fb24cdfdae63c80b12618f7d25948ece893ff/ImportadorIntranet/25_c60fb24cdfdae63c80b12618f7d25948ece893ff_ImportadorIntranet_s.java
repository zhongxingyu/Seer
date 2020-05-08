 package com.cmt.importador;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.rmi.RemoteException;
 import java.sql.Clob;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.ResourceBundle;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.rpc.ServiceException;
 
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.AuthCache;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.impl.auth.BasicScheme;
 import org.apache.http.impl.client.BasicAuthCache;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.util.EntityUtils;
 import org.apache.log4j.Logger;
 import org.jsoup.Jsoup;
 import org.jsoup.select.Elements;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import com.cmt.importador.model.xml.DynamicElement;
 import com.cmt.importador.model.xml.Root;
 import com.cmt.importador.persistence.Manager;
 import com.cmt.importador.utils.HtmlManipulator;
 import com.cmt.importador.utils.PwdGenerator;
 import com.cmt.importador.utils.XMLReaderWriter;
 import com.liferay.client.soap.portal.kernel.repository.model.FileEntrySoap;
 import com.liferay.client.soap.portal.model.LayoutSoap;
 import com.liferay.client.soap.portal.service.ServiceContext;
 import com.liferay.client.soap.portal.service.http.LayoutServiceSoap;
 import com.liferay.client.soap.portal.service.http.LayoutServiceSoapServiceLocator;
 import com.liferay.client.soap.portal.service.http.Portal_LayoutServiceSoapBindingStub;
 import com.liferay.client.soap.portlet.asset.model.AssetCategorySoap;
 import com.liferay.client.soap.portlet.asset.model.AssetTagSoap;
 import com.liferay.client.soap.portlet.asset.model.AssetVocabularySoap;
 import com.liferay.client.soap.portlet.asset.service.http.AssetCategoryServiceSoap;
 import com.liferay.client.soap.portlet.asset.service.http.AssetCategoryServiceSoapServiceLocator;
 import com.liferay.client.soap.portlet.asset.service.http.AssetTagServiceSoap;
 import com.liferay.client.soap.portlet.asset.service.http.AssetTagServiceSoapServiceLocator;
 import com.liferay.client.soap.portlet.asset.service.http.AssetVocabularyServiceSoap;
 import com.liferay.client.soap.portlet.asset.service.http.AssetVocabularyServiceSoapServiceLocator;
 import com.liferay.client.soap.portlet.asset.service.http.Portlet_Asset_AssetCategoryServiceSoapBindingStub;
 import com.liferay.client.soap.portlet.asset.service.http.Portlet_Asset_AssetTagServiceSoapBindingStub;
 import com.liferay.client.soap.portlet.asset.service.http.Portlet_Asset_AssetVocabularyServiceSoapBindingStub;
 import com.liferay.client.soap.portlet.calendar.service.http.CalEventServiceSoap;
 import com.liferay.client.soap.portlet.calendar.service.http.CalEventServiceSoapServiceLocator;
 import com.liferay.client.soap.portlet.calendar.service.http.Portlet_Cal_CalEventServiceSoapBindingStub;
 import com.liferay.client.soap.portlet.documentlibrary.model.DLFileEntrySoap;
 import com.liferay.client.soap.portlet.documentlibrary.model.DLFolderSoap;
 import com.liferay.client.soap.portlet.documentlibrary.service.http.DLAppServiceSoap;
 import com.liferay.client.soap.portlet.documentlibrary.service.http.DLAppServiceSoapServiceLocator;
 import com.liferay.client.soap.portlet.documentlibrary.service.http.DLFileEntryServiceSoap;
 import com.liferay.client.soap.portlet.documentlibrary.service.http.DLFileEntryServiceSoapServiceLocator;
 import com.liferay.client.soap.portlet.documentlibrary.service.http.DLFolderServiceSoap;
 import com.liferay.client.soap.portlet.documentlibrary.service.http.DLFolderServiceSoapServiceLocator;
 import com.liferay.client.soap.portlet.documentlibrary.service.http.Portlet_DL_DLAppServiceSoapBindingStub;
 import com.liferay.client.soap.portlet.documentlibrary.service.http.Portlet_DL_DLFileEntryServiceSoapBindingStub;
 import com.liferay.client.soap.portlet.documentlibrary.service.http.Portlet_DL_DLFolderServiceSoapBindingStub;
 import com.liferay.client.soap.portlet.dynamicdatalists.service.http.DDLRecordServiceSoap;
 import com.liferay.client.soap.portlet.dynamicdatalists.service.http.DDLRecordServiceSoapServiceLocator;
 import com.liferay.client.soap.portlet.dynamicdatalists.service.http.DDLRecordSetServiceSoap;
 import com.liferay.client.soap.portlet.dynamicdatalists.service.http.DDLRecordSetServiceSoapServiceLocator;
 import com.liferay.client.soap.portlet.dynamicdatalists.service.http.Portlet_DDL_DDLRecordServiceSoapBindingStub;
 import com.liferay.client.soap.portlet.dynamicdatalists.service.http.Portlet_DDL_DDLRecordSetServiceSoapBindingStub;
 import com.liferay.client.soap.portlet.dynamicdatamapping.service.http.DDMStructureServiceSoap;
 import com.liferay.client.soap.portlet.dynamicdatamapping.service.http.DDMStructureServiceSoapServiceLocator;
 import com.liferay.client.soap.portlet.dynamicdatamapping.service.http.DDMTemplateServiceSoap;
 import com.liferay.client.soap.portlet.dynamicdatamapping.service.http.DDMTemplateServiceSoapServiceLocator;
 import com.liferay.client.soap.portlet.dynamicdatamapping.service.http.Portlet_DDM_DDMStructureServiceSoapBindingStub;
 import com.liferay.client.soap.portlet.dynamicdatamapping.service.http.Portlet_DDM_DDMTemplateServiceSoapBindingStub;
 import com.liferay.client.soap.portlet.journal.model.JournalStructureSoap;
 import com.liferay.client.soap.portlet.journal.model.JournalTemplateSoap;
 import com.liferay.client.soap.portlet.journal.service.http.JournalArticleServiceSoap;
 import com.liferay.client.soap.portlet.journal.service.http.JournalArticleServiceSoapServiceLocator;
 import com.liferay.client.soap.portlet.journal.service.http.JournalStructureServiceSoap;
 import com.liferay.client.soap.portlet.journal.service.http.JournalStructureServiceSoapServiceLocator;
 import com.liferay.client.soap.portlet.journal.service.http.JournalTemplateServiceSoap;
 import com.liferay.client.soap.portlet.journal.service.http.JournalTemplateServiceSoapServiceLocator;
 import com.liferay.client.soap.portlet.journal.service.http.Portlet_Journal_JournalArticleServiceSoapBindingStub;
 import com.liferay.client.soap.portlet.journal.service.http.Portlet_Journal_JournalStructureServiceSoapBindingStub;
 import com.liferay.client.soap.portlet.journal.service.http.Portlet_Journal_JournalTemplateServiceSoapBindingStub;
 
 public class ImportadorIntranet {
 
     static JournalArticleServiceSoap articleService;
     static AssetCategoryServiceSoap categoryService;
 
     public static ResourceBundle CONFIGURATION;
 
     static DDLRecordServiceSoap ddlRecordService;;
 
     static DDLRecordSetServiceSoap ddlRecordSetService;
 
     private static DDMStructureServiceSoap ddmStructureService;
 
     private static CalEventServiceSoap calEventServiceSoap;
 
     private static DDMTemplateServiceSoap ddmTemplateService;
 
     static DLFileEntryServiceSoap dlFileEntryService;
     static DLFolderServiceSoap dlFolderService;
     static DLAppServiceSoap dlService;
 
     static Long homeTagId;
 
     private static LayoutServiceSoap layoutService;
     private static Logger log = Logger.getLogger(ImportadorIntranet.class);
     static Long novedadesTagId;
 
     private static Map<Integer, Object[]> PAGE_MAP = new HashMap<Integer, Object[]>();
     private static final Map<String, JournalStructureSoap> STRUCTURES = new HashMap<String, JournalStructureSoap>();
 
     static JournalStructureServiceSoap structureService;
     static AssetTagServiceSoap tagService;
 
     private static final Map<String, JournalTemplateSoap> TEMPLATES = new HashMap<String, JournalTemplateSoap>();
 
     static JournalTemplateServiceSoap templateService;
 
     static AssetVocabularyServiceSoap vocabularyService;
 
     private static Map<String, String> EVENT_CATEGORIES = new HashMap<String, String>();
     private static Map<String, String> LOCATION_CATEGORIES = new HashMap<String, String>();
 
     static {
         ImportadorIntranet.PAGE_MAP.put(1, new Object[] { "La CMT y T",
                 "Excedencias, Vacaciones y Permisos", "Excedencias", 0L });
         ImportadorIntranet.PAGE_MAP.put(2, new Object[] { "La CMT y T",
                 "Excedencias, Vacaciones y Permisos",
                 "Vacaciones y Das de Libre Disposicin", 0L });
         ImportadorIntranet.PAGE_MAP.put(3, new Object[] { "La CMT y T",
                 "Excedencias, Vacaciones y Permisos", "Permisos", 0L });
 
         ImportadorIntranet.PAGE_MAP.put(4, new Object[] { "La CMT y T",
                 "Tus Retribuciones", "Salario Base", 0L });
         ImportadorIntranet.PAGE_MAP.put(5, new Object[] { "La CMT y T",
                 "Tus Retribuciones", "Productividad", 0L });
         ImportadorIntranet.PAGE_MAP.put(6, new Object[] { "La CMT y T",
                 "Tus Retribuciones", "Anticipo de Retribuciones", 0L });
 
         ImportadorIntranet.PAGE_MAP.put(7, new Object[] {
                 "Recursos Grficos e Imagen Corporativa",
                 "Plantillas Oficiales", 0L });
         ImportadorIntranet.PAGE_MAP.put(8, new Object[] {
                 "Recursos Grficos e Imagen Corporativa", "Imagen Corporativa",
                 0L });
 
         ImportadorIntranet.PAGE_MAP.put(10, new Object[] { "La CMT y T",
                 "Tus Retribuciones", "Normativa", 0L });
 
         ImportadorIntranet.PAGE_MAP.put(11, new Object[] {
                 "Soporte, Reparaciones, Incidencias y Peticin de Material",
                 "Manuales de Usuario", 0L });
 
         ImportadorIntranet.PAGE_MAP.put(18, new Object[] { "La CMT y T",
                 "Viajes", "Normativa", 0L });
         ImportadorIntranet.PAGE_MAP
                 .put(19,
                         new Object[] {
                                 "La CMT y T",
                                 "Viajes",
                                 "Tramitacin de Comisiones de Servicio y Abono de Gastos de Viaje",
                                 0L });
         ImportadorIntranet.PAGE_MAP
                 .put(20,
                         new Object[] {
                                 "La CMT y T",
                                 "Viajes",
                                 "Comisiones de Servicio con la Consideracin de Residencia Eventual",
                                 0L });
         ImportadorIntranet.PAGE_MAP.put(21, new Object[] { "La CMT y T",
                 "Viajes", "Modelos", 0L });
         // pageMap.put(21, new Object[] { "La CMT y T", "Modelos de Solicitud",
         // 0L });
 
         ImportadorIntranet.PAGE_MAP.put(22, new Object[] { "La CMT y T",
                 "Beneficios Sociales", "Normativa", 0L });
         ImportadorIntranet.PAGE_MAP.put(23, new Object[] { "La CMT y T",
                 "Beneficios Sociales", "Seguro Mdico", 0L });
         ImportadorIntranet.PAGE_MAP.put(24, new Object[] { "La CMT y T",
                 "Beneficios Sociales",
                 "Seguro de Responsabilidad Civil Profesional", 0L });
         ImportadorIntranet.PAGE_MAP.put(25, new Object[] { "La CMT y T",
                 "Beneficios Sociales", "Ayuda de Comida", 0L });
         ImportadorIntranet.PAGE_MAP.put(26, new Object[] { "La CMT y T",
                 "Beneficios Sociales",
                 "Complemento de Transporte / Plaza de Garaje", 0L });
         ImportadorIntranet.PAGE_MAP
                 .put(27,
                         new Object[] {
                                 "La CMT y T",
                                 "Beneficios Sociales",
                                 "Complemento Salarial en Caso de Accidente, Enfermedad y Maternidad o Paternidad",
                                 0L });
         ImportadorIntranet.PAGE_MAP.put(28, new Object[] { "La CMT y T",
                 "Beneficios Sociales", "Anticipo de Retribuciones", 0L });
         ImportadorIntranet.PAGE_MAP.put(29, new Object[] { "La CMT y T",
                 "Beneficios Sociales", "Jornada Laboral", 0L });
         ImportadorIntranet.PAGE_MAP.put(30, new Object[] { "La CMT y T",
                 "Beneficios Sociales", "Permiso de Lactancia", 0L });
         ImportadorIntranet.PAGE_MAP.put(31, new Object[] { "La CMT y T",
                 "Beneficios Sociales", "Formacin", 0L });
         ImportadorIntranet.PAGE_MAP.put(32, new Object[] { "La CMT y T",
                 "Beneficios Sociales", "Modelos", 0L });
 
         ImportadorIntranet.PAGE_MAP.put(33, new Object[] { "La CMT y T",
                 "Comit de Empresa", "Normativa", 0L });
         ImportadorIntranet.PAGE_MAP.put(34, new Object[] { "La CMT y T",
                 "Comit de Empresa", "Concepto y Composicin", 0L });
         ImportadorIntranet.PAGE_MAP.put(35, new Object[] { "La CMT y T",
                 "Comit de Empresa", "Tabln de Anuncios", 0L });
         ImportadorIntranet.PAGE_MAP.put(36, new Object[] { "La CMT y T",
                 "Comit de Empresa",
                 "Elecciones a representantes de los trabajadores", 0L });
 
         ImportadorIntranet.PAGE_MAP.put(37, new Object[] { "La CMT y T",
                 "Plan de Formacin", "Normativa", 0L });
         ImportadorIntranet.PAGE_MAP.put(38, new Object[] { "La CMT y T",
                 "Plan de Formacin", "Formacin de Carcter General", 0L });
         ImportadorIntranet.PAGE_MAP.put(39, new Object[] { "La CMT y T",
                 "Plan de Formacin", "Formacin Especfica", 0L });
         ImportadorIntranet.PAGE_MAP.put(39, new Object[] { "La CMT y T",
                 "Plan de Formacin", "Formacin Especfica", 0L });
         ImportadorIntranet.PAGE_MAP.put(40, new Object[] { "La CMT y T",
                 "Plan de Formacin", "Mejora de Currculum Profesional", 0L });
 
         ImportadorIntranet.PAGE_MAP.put(41, new Object[] { "La CMT y T",
                 "Calendario Laboral", 0L });
 
         ImportadorIntranet.PAGE_MAP.put(42,
                 new Object[] { "La CMT y T",
                         "Excedencias, Vacaciones y Permisos",
                         "Horarios de Trabajo", 0L });
         ImportadorIntranet.PAGE_MAP.put(43, new Object[] { "La CMT y T",
                 "Excedencias, Vacaciones y Permisos", "Modelos de Solicitud",
                 0L });
         ImportadorIntranet.PAGE_MAP.put(44, new Object[] { "La CMT y T",
                 "Excedencias, Vacaciones y Permisos", "Normativa", 0L });
 
         ImportadorIntranet.PAGE_MAP.put(45, new Object[] { "La CMT y T",
                 "Normativa Aplicable",
                 "Resoluciones - Instrucciones Presidente", 0L });
 
         ImportadorIntranet.PAGE_MAP.put(46, new Object[] {
                 "Biblioteca y Bases de Datos",
                 "Sumarios de journals y revistas", 0L });
         ImportadorIntranet.PAGE_MAP.put(47, new Object[] {
                 "Biblioteca y Bases de Datos", "Base de Datos", 0L });
         ImportadorIntranet.PAGE_MAP.put(48, new Object[] {
                 "Biblioteca y Bases de Datos", "Consulta", 0L });
 
         ImportadorIntranet.PAGE_MAP.put(49, new Object[] { "La CMT y T",
                 "Normativa Aplicable", "Leyes", 0L });
         ImportadorIntranet.PAGE_MAP.put(50, new Object[] { "La CMT y T",
                 "Normativa Aplicable", "Reglamentos", 0L });
         ImportadorIntranet.PAGE_MAP.put(51, new Object[] { "La CMT y T",
                 "Normativa Aplicable", "Resoluciones Consejo", 0L });
         ImportadorIntranet.PAGE_MAP.put(52, new Object[] { "La CMT y T",
                 "Normativa Aplicable", "Resoluciones CECIR", 0L });
 
         ImportadorIntranet.PAGE_MAP.put(53, new Object[] { "La CMT y T",
                 "Prevencin de Riesgos Laborales", "Informacin General", 0L });
         ImportadorIntranet.PAGE_MAP.put(54, new Object[] { "La CMT y T",
                 "Prevencin de Riesgos Laborales",
                 "Comit de Seguridad y Salud", 0L });
         ImportadorIntranet.PAGE_MAP.put(55, new Object[] { "La CMT y T",
                 "Prevencin de Riesgos Laborales", "Delegados de Prevencin",
                 0L });
         ImportadorIntranet.PAGE_MAP.put(56, new Object[] { "La CMT y T",
                 "Prevencin de Riesgos Laborales",
                 "Medidas de Prevencin de Riesgos Laborales", 0L });
 
         ImportadorIntranet.PAGE_MAP.put(61, new Object[] { "La CMT y T",
                 "Prevencin de Riesgos Laborales", "Normativa", 0L });
 
         ImportadorIntranet.PAGE_MAP.put(62, new Object[] {
                 "Biblioteca y Bases de Datos", "Modelos de Solicitud", 0L });
 
         ImportadorIntranet.PAGE_MAP.put(63, new Object[] { "La CMT y T",
                 "Beneficios Sociales", "Plan de pensiones", 0L });
         ImportadorIntranet.PAGE_MAP.put(64, new Object[] { "La CMT y T",
                 "Modelos de Solicitud", 0L });
         ImportadorIntranet.PAGE_MAP.put(65, new Object[] { "La CMT y T",
                 "Tus Retribuciones", "Complemento de carrera", 0L });
         ImportadorIntranet.PAGE_MAP.put(66, new Object[] { "La CMT y T",
                 "Beneficios Sociales", "Escuela de Educacin Infantil", 0L });
         ImportadorIntranet.PAGE_MAP.put(67, new Object[] { "La CMT y T",
                 "Nuevo edificio", 0L });
 
         ImportadorIntranet.PAGE_MAP.put(68, new Object[] {
                 "Soporte, Reparaciones, Incidencias y Peticin de Material",
                 "Modelos de Solicitud", "Listado Roaming Vodafone", 0L });
         ImportadorIntranet.PAGE_MAP.put(69, new Object[] {
                 "Soporte, Reparaciones, Incidencias y Peticin de Material",
                 "Peticin de Material", 0L });
 
         ImportadorIntranet.PAGE_MAP.put(70, new Object[] {
                 "Biblioteca y Bases de Datos", "Boletines", 0L });
 
         ImportadorIntranet.PAGE_MAP.put(95, new Object[] { "La CMT y T",
                 "Prevencin de Riesgos Laborales",
                 "Trmites en caso de accidente de trabajo", 0L });
         ImportadorIntranet.PAGE_MAP.put(96,
                 new Object[] { "La CMT y T",
                         "Prevencin de Riesgos Laborales",
                         "Medidas de emergencia", 0L });
         ImportadorIntranet.PAGE_MAP.put(97, new Object[] { "La CMT y T",
                 "Prevencin de Riesgos Laborales", "Modelos y Documentacin",
                 0L });
 
         ImportadorIntranet.PAGE_MAP.put(89, new Object[] {
                 "Soporte Informtico", "Sabas qu?", 0L });
         ImportadorIntranet.PAGE_MAP.put(90, new Object[] {
                 "Soporte Informtico", "Preguntas frecuentes", 0L });
 
         ImportadorIntranet.EVENT_CATEGORIES.put("1", "Organizado por la CMT");
         ImportadorIntranet.EVENT_CATEGORIES.put("2", "Participa la CMT");
         ImportadorIntranet.EVENT_CATEGORIES.put("3",
                 "CMT Actividad Internacional");
         ImportadorIntranet.EVENT_CATEGORIES.put("other", "Evento del sector");
 
         ImportadorIntranet.LOCATION_CATEGORIES.put("1", "Barcelona");
         ImportadorIntranet.LOCATION_CATEGORIES.put("2", "Madrid");
         ImportadorIntranet.LOCATION_CATEGORIES.put("3", "Bruselas");
     }
 
     static void cargarEventos() throws ParserConfigurationException,
             SAXException, IOException, URISyntaxException, ParseException {
 
         final URL ulr = ImportadorIntranet.class.getClassLoader().getResource(
                 "data/events.xml");
 
         final File fXmlFile = new File(ulr.getPath());
         final DocumentBuilderFactory dbFactory = DocumentBuilderFactory
                 .newInstance();
         final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
         final Document doc = dBuilder.parse(fXmlFile);
 
         // optional, but recommended
         // read this -
         // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
         doc.getDocumentElement().normalize();
 
         System.out.println("Root element :"
                 + doc.getDocumentElement().getNodeName());
 
         final NodeList nList = doc.getElementsByTagName("event");
 
         final ServiceContext context = new ServiceContext();
         final Long groupId = Long.valueOf(ImportadorIntranet.CONFIGURATION
                 .getString("liferayGroupId"));
 
         context.setAddCommunityPermissions(true);
         context.setAddGuestPermissions(true);
         context.setScopeGroupId(groupId);
         context.setCompanyId(Long.valueOf(ImportadorIntranet.CONFIGURATION
                 .getString("liferayCompanyId")));
         context.setUserId(Long.valueOf(ImportadorIntranet.CONFIGURATION
                 .getString("liferayUserId")));
         context.setWorkflowAction(1);
 
         final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
         for (int i = 0; i < nList.getLength(); i++) {
             final Node nNode = nList.item(i);
 
             if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 
                 final Element eElement = (Element) nNode;
 
                 final String id = ImportadorIntranet.getXmlTag(eElement,
                         "event_id");
                 String title = ImportadorIntranet.getXmlTag(eElement, "title");
                 final String date = ImportadorIntranet.getXmlTag(eElement,
                         "date");
                 final String date_end = ImportadorIntranet.getXmlTag(eElement,
                         "date_to");
                 // ImportadorIntranet.getXmlTag(eElement, "time");
                 final String category = ImportadorIntranet.getXmlTag(eElement,
                         "category_id");
                 final String location = ImportadorIntranet.getXmlTag(eElement,
                         "location_id");
                 String description = ImportadorIntranet.getXmlTag(eElement,
                         "description");
                 final String url = ImportadorIntranet
                         .getXmlTag(eElement, "url");
                 final String image = ImportadorIntranet.getXmlTag(eElement,
                         "image");
                 // final String target =
                 // ImportadorIntranet.getXmlTag(eElement,
                 // "target");
 
                 final List<FileEntrySoap> files = ImportadorIntranet
                         .getFilesFromEvent(url, image);
                 if (!files.isEmpty()) {
                     final long[] fileIds = new long[files.size()];
                     for (int j = 0; j < files.size(); j++) {
                         fileIds[j] = files.get(j).getPrimaryKey();
                     }
                     context.setAssetLinkEntryIds(fileIds);
                 } else {
                     context.setAssetLinkEntryIds(null);
                 }
                 final List<String> links = ImportadorIntranet
                         .getLinksFromEvent(url, image);
                 for (final String link : links) {
                     description += "<br/>" + link;
                 }
 
                 if ((ImportadorIntranet.EVENT_CATEGORIES.get(category) != null)
                         && !ImportadorIntranet.EVENT_CATEGORIES.get(category)
                                 .isEmpty()) {
                     context.setAssetTagNames(new String[] { ImportadorIntranet.EVENT_CATEGORIES
                             .get(category) });
                 } else {
                     context.setAssetTagNames(null);
                 }
 
                 if (title.length() > 74) {
                     description = title + "<br/>" + description;
                     title = title.substring(0, 71) + "...";
                 }
 
                 Date date_from = null;
                 if ((date != null) && !date.isEmpty()) {
                     date_from = sdf.parse(date);
                 }
                 Date date_to = null;
                 if ((date_end != null) && !date_end.isEmpty()) {
                     date_to = sdf.parse(date_end);
                 }
 
                 final Calendar cal_from = Calendar.getInstance();
                 cal_from.setTime(date_from);
                 Calendar cal_to = null;
                 if (date_to != null) {
                     cal_to = Calendar.getInstance();
                     cal_to.setTime(date_to);
                 }
 
                 int days = 1;
                 if (cal_to != null) {
                     if (cal_from.get(Calendar.YEAR) != cal_to
                             .get(Calendar.YEAR)) {
                         ImportadorIntranet.log
                                 .error("EVENTO OCURRIDO EN DISTINTOS AOS");
                         throw new RuntimeException();
                     }
                     final int from = cal_from.get(Calendar.DAY_OF_YEAR);
                     final int to = cal_to.get(Calendar.DAY_OF_YEAR);
 
                     days = (to - from) + 1;
                 }
 
                 for (int j = 0; j < days; j++) {
                     ImportadorIntranet.calEventServiceSoap.addEvent(title,
                             description, ImportadorIntranet.LOCATION_CATEGORIES
                                     .get(location), cal_from
                                     .get(Calendar.MONTH), cal_from
                                     .get(Calendar.DAY_OF_MONTH), cal_from
                                     .get(Calendar.YEAR), 0, 0, cal_from
                                     .get(Calendar.MONTH), cal_from
                                     .get(Calendar.DAY_OF_MONTH), cal_from
                                     .get(Calendar.YEAR), 24, 0, true, true,
                             "event", false, null, 0, 0, 0, context);
 
                     cal_from.add(Calendar.DATE, 1);
                 }
 
                 ImportadorIntranet.log.info("IMPORTADO CON XITO EL EVENTO "
                         + id);
             }
         }
     }
 
     static void cargarGlosario() throws NumberFormatException,
             UnsupportedEncodingException, ClientProtocolException,
             SQLException, IOException {
         final String groupId = ImportadorIntranet.CONFIGURATION
                 .getString("liferayGroupId");
         final String recordSetId = ImportadorIntranet.CONFIGURATION
                 .getString("liferayRecordSetId");
 
         final ResultSet result = Manager.executeSelectGetGlosario();
 
         final HttpHost targetHost = new HttpHost("192.168.50.32", 8080, "http");
         final DefaultHttpClient httpclient = new DefaultHttpClient();
         httpclient.getCredentialsProvider().setCredentials(
                 new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                 new UsernamePasswordCredentials("test@liferay.com", "test"));
 
         // Create AuthCache instance
         final AuthCache authCache = new BasicAuthCache();
         // Generate BASIC scheme object and add it to the local
         // auth cache
         final BasicScheme basicAuth = new BasicScheme();
         authCache.put(targetHost, basicAuth);
 
         // Add AuthCache to the execution context
         final BasicHttpContext ctx = new BasicHttpContext();
         ctx.setAttribute(ClientContext.AUTH_CACHE, authCache);
 
         int i = 0;
         while (result.next()) {
             final String termino = result.getString("termino");
             final String definicion = result.getString("definicion")
                     .replace("\n", "").replace("\r", "").replace("\"", "\\\"");
 
             final HttpPost post = new HttpPost(
                     "/api/secure/jsonws/ddlrecord/add-record");
             final List<NameValuePair> params = new ArrayList<NameValuePair>();
             params.add(new BasicNameValuePair("groupId", groupId));
             params.add(new BasicNameValuePair("recordSetId", recordSetId));
             params.add(new BasicNameValuePair("displayIndex", String.valueOf(i)));
             params.add(new BasicNameValuePair("fieldsMap", "{\"text2369\":\""
                     + termino + "\",\"textarea3468\":\"" + definicion + "\"}"));
             final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
                     params, "UTF-8");
             post.setEntity(entity);
             final HttpResponse resp = httpclient.execute(targetHost, post, ctx);
 
             if (resp.getStatusLine().getStatusCode() != 200) {
                 ImportadorIntranet.log
                         .error("Error creating new glossary entry: " + termino
                                 + " -> " + definicion);
             }
             post.releaseConnection();
             i++;
         }
         httpclient.getConnectionManager().shutdown();
     }
 
     static void cargarPropiedades() {
         ImportadorIntranet.CONFIGURATION = ResourceBundle
                 .getBundle("conf.config");
     }
 
     static void crearNovedadesIntranet() throws Exception {
         final Long groupId = Long.valueOf(ImportadorIntranet.CONFIGURATION
                 .getString("liferayGroupId"));
 
         final JournalStructureSoap structureNovedades = ImportadorIntranet.STRUCTURES
                 .get(ImportadorIntranet.CONFIGURATION
                         .getString("structure.novedades"));
 
         final JournalTemplateSoap templateNovedades = ImportadorIntranet.TEMPLATES
                 .get(ImportadorIntranet.CONFIGURATION
                         .getString("template.novedades"));
 
         final Root articleNovedades = (Root) XMLReaderWriter.getReaderWriter()
                 .fromXML(structureNovedades.getXsd());
 
         final ResultSet result = Manager.executeSelectGetNovedades();
 
         final DynamicElement articleTitle = articleNovedades
                 .getDynamicElements().get(0);
 
         final DynamicElement articleName = articleNovedades
                 .getDynamicElements().get(1);
         final DynamicElement articleEmail = articleNovedades
                 .getDynamicElements().get(2);
         final DynamicElement articlePhone = articleNovedades
                 .getDynamicElements().get(3);
 
         final DynamicElement articleDescription = articleNovedades
                 .getDynamicElements().get(4);
         final DynamicElement articlePage = articleNovedades
                 .getDynamicElements().get(5);
         final DynamicElement articleLink = articleNovedades
                 .getDynamicElements().get(6);
 
         final List<Root> xmlArticles = new ArrayList<Root>();
 
         Root root = null;
         DynamicElement title = null;
         DynamicElement phone = null;
         DynamicElement email = null;
         DynamicElement name = null;
         DynamicElement description = null;
         DynamicElement page = null;
         DynamicElement link = null;
 
         final ServiceContext context = new ServiceContext();
 
         context.setAddCommunityPermissions(true);
         context.setAddGuestPermissions(true);
         context.setScopeGroupId(groupId);
         context.setCompanyId(Long.valueOf(ImportadorIntranet.CONFIGURATION
                 .getString("liferayCompanyId")));
         context.setUserId(Long.valueOf(ImportadorIntranet.CONFIGURATION
                 .getString("liferayUserId")));
         context.setWorkflowAction(1);
 
         final Calendar cal = Calendar.getInstance();
 
         while (result.next()) {
             final Date date = result.getDate("fecha_inicio");
             final String correo = result.getString("email");
             final String nombre = result.getString("nombre");
             final String telefono = result.getString("telefono");
             String titulo = result.getString("titulo");
             String descripcion = result.getString("descripcion");
             final Boolean isHome = result.getBoolean("home");
             final String pagePath = result.getString("page");
 
             String link_ = result.getString("link");
             final String adjuntos_list = result.getString("adjuntos");
 
             if (titulo != null) {
                 titulo = HtmlManipulator.replaceHtmlEntities(titulo);
             }
             if (descripcion != null) {
                 descripcion = HtmlManipulator.replaceHtmlEntities(descripcion);
             }
 
             if ((link_ != null) && link_.contains(".php")) {
                 link_ = null;
             }
 
             final List<FileEntrySoap> files = new ArrayList<FileEntrySoap>();
             FileEntrySoap file = ImportadorIntranet
                     .uploadFileToDLAndGetAsset(link_);
             if (file != null) {
                 files.add(file);
             }
             if (adjuntos_list != null) {
                 final String[] adjuntos = adjuntos_list.split("\\|");
                 for (final String adjunto : adjuntos) {
                     file = ImportadorIntranet
                             .uploadFileToDLAndGetAsset("docs/cmt/nac/tab/"
                                     + adjunto);
                     if (file != null) {
                         files.add(file);
                     }
                 }
             }
             if (!files.isEmpty()) {
                 final long[] fileIds = new long[files.size()];
                 for (int i = 0; i < files.size(); i++) {
                     final DLFileEntrySoap dffileentry = ImportadorIntranet.dlFileEntryService
                             .getFileEntry(files.get(i).getFileEntryId());
                     fileIds[i] = dffileentry.getPrimaryKey();
                 }
                 context.setAssetLinkEntryIds(fileIds);
             } else {
                 context.setAssetLinkEntryIds(null);
             }
 
             if ((correo != null) && !correo.isEmpty()) {
                 final HashMap attributes = new HashMap();
                 attributes.put("responsable", correo);
                 context.setExpandoBridgeAttributes(attributes);
             } else {
                 context.setExpandoBridgeAttributes(null);
             }
 
             context.setAssetTagNames(new String[] { ImportadorIntranet.CONFIGURATION
                     .getString("tag.novedades") });
             if (isHome) {
                 context.setAssetTagNames(new String[] {
                         ImportadorIntranet.CONFIGURATION
                                 .getString("tag.novedades"),
                         ImportadorIntranet.CONFIGURATION.getString("tag.home") });
             }
 
             root = new Root("es_ES", "es_ES");
             xmlArticles.add(root);
             title = new DynamicElement(articleTitle,
                     PwdGenerator.getPassword(), titulo);
             root.addDynamicElement(title);
             name = new DynamicElement(articleName, PwdGenerator.getPassword(),
                     nombre);
             root.addDynamicElement(name);
             email = new DynamicElement(articleEmail,
                     PwdGenerator.getPassword(), correo);
             root.addDynamicElement(email);
             phone = new DynamicElement(articlePhone,
                     PwdGenerator.getPassword(), telefono);
             root.addDynamicElement(phone);
             description = new DynamicElement(articleDescription,
                     PwdGenerator.getPassword(),
                     ImportadorIntranet.uploadLinksToDL(descripcion));
             root.addDynamicElement(description);
             if (!pagePath.startsWith("Inicio")) {
                 page = new DynamicElement(articlePage,
                         PwdGenerator.getPassword(), pagePath);
                 root.addDynamicElement(page);
                 link = new DynamicElement(articleLink,
                         PwdGenerator.getPassword(),
                         ImportadorIntranet.getPageLink(pagePath));
                 root.addDynamicElement(link);
             }
 
             cal.setTime(date);
 
             ImportadorIntranet.articleService.addArticle(groupId, 0l, 0l, "",
                     true, new String[] { "es_ES" }, new String[] { titulo },
                     new String[] {}, new String[] {}, XMLReaderWriter
                            .getReaderWriter().toXML(root), "General",
                     structureNovedades.getStructureId(), templateNovedades
                             .getTemplateId(), "", cal.get(Calendar.MONTH), cal
                             .get(Calendar.DATE), cal.get(Calendar.YEAR), cal
                             .get(Calendar.HOUR), cal.get(Calendar.MINUTE), 0,
                     0, 0, 0, 0, true, 0, 0, 0, 0, 0, true, true, null, context);
         }
         Manager.closeStatement();
     }
 
     static void crearPaginasIntranet() throws Exception {
 
         final Long groupId = Long.valueOf(ImportadorIntranet.CONFIGURATION
                 .getString("liferayGroupId"));
 
         final JournalStructureSoap structure2level = ImportadorIntranet.STRUCTURES
                 .get(ImportadorIntranet.CONFIGURATION
                         .getString("structure.2level"));
         final JournalStructureSoap structure3level = ImportadorIntranet.STRUCTURES
                 .get(ImportadorIntranet.CONFIGURATION
                         .getString("structure.3level"));
 
         final JournalTemplateSoap template2level = ImportadorIntranet.TEMPLATES
                 .get(ImportadorIntranet.CONFIGURATION
                         .getString("template.2level"));
         final JournalTemplateSoap template3level = ImportadorIntranet.TEMPLATES
                 .get(ImportadorIntranet.CONFIGURATION
                         .getString("template.3level"));
 
         final Root article2level = (Root) XMLReaderWriter.getReaderWriter()
                 .fromXML(structure2level.getXsd());
         final Root article3level = (Root) XMLReaderWriter.getReaderWriter()
                 .fromXML(structure3level.getXsd());
 
         final ResultSet result = Manager.executeSelectGetArticles();
 
         final DynamicElement article2Category = article2level
                 .getDynamicElements().get(0);
         final DynamicElement article2Data = article2Category
                 .getDynamicElements().get(0);
         final DynamicElement article2Link = article2Data.getDynamicElements()
                 .get(0);
         // DynamicElement article2Date =
         // article2Data.getDynamicElements().get(1);
 
         final DynamicElement article3Category = article3level
                 .getDynamicElements().get(0);
         final DynamicElement article3Subcategory = article3Category
                 .getDynamicElements().get(0);
         final DynamicElement article3Data = article3Subcategory
                 .getDynamicElements().get(0);
         final DynamicElement article3Link = article3Data.getDynamicElements()
                 .get(0);
         // DynamicElement article3Date =
         // article3Data.getDynamicElements().get(1);
 
         final List<Root> xmlArticles = new ArrayList<Root>();
 
         Root root = null;
         DynamicElement category = null;
         DynamicElement subcategory = null;
         DynamicElement data = null;
         DynamicElement link = null;
         final Map<String, Integer> subcategories = new HashMap<String, Integer>();
 
         final ServiceContext context = new ServiceContext();
 
         context.setAddCommunityPermissions(true);
         context.setAddGuestPermissions(true);
         context.setScopeGroupId(groupId);
         context.setCompanyId(Long.valueOf(ImportadorIntranet.CONFIGURATION
                 .getString("liferayCompanyId")));
         context.setUserId(Long.valueOf(ImportadorIntranet.CONFIGURATION
                 .getString("liferayUserId")));
         context.setWorkflowAction(1);
 
         while (result.next()) {
             final Integer idtipo = result.getInt("idtipo");
             final Integer grupo = result.getInt("grupo");
             final String tipo = result.getString("tipo");
             // Integer orden = result.getInt("orden");
             String titulo = HtmlManipulator.replaceHtmlEntities(result
                     .getString("titulo"));
             String enlace = result.getString("enlace");
             Clob contenido = result.getClob("contenido");
             String contenidoString = null;
             if (contenido != null) {
                 contenidoString = contenido.getSubString(1l,
                         Long.valueOf(contenido.length()).intValue());
                 contenidoString = ImportadorIntranet
                         .uploadLinksToDL(contenidoString);
             }
 
             if ((tipo == null) || tipo.isEmpty()) {
                 root = new Root("es_ES", "es_ES");
                 xmlArticles.add(root);
                 category = new DynamicElement(article2Category,
                         PwdGenerator.getPassword(), titulo);
                 root.addDynamicElement(category);
                 data = new DynamicElement(article2Data,
                         PwdGenerator.getPassword(), contenidoString);
                 category.addDynamicElement(data);
                 if ((enlace != null) && !enlace.isEmpty()) {
                     link = new DynamicElement(article2Link,
                             PwdGenerator.getPassword(),
                             ImportadorIntranet.uploadFileToDLAndGetURL(enlace));
                     data.addDynamicElement(link);
                 }
                 while (result.next()) {
                     if (idtipo.equals(result.getInt("idtipo"))
                             && grupo.equals(result.getInt("grupo"))) {
                         titulo = HtmlManipulator.replaceHtmlEntities(result
                                 .getString("titulo"));
                         enlace = result.getString("enlace");
                         contenido = result.getClob("contenido");
                         contenidoString = null;
                         if (contenido != null) {
                             contenidoString = contenido.getSubString(1l, Long
                                     .valueOf(contenido.length()).intValue());
                             contenidoString = ImportadorIntranet
                                     .uploadLinksToDL(contenidoString);
                         }
 
                         data = new DynamicElement(article2Data,
                                 PwdGenerator.getPassword(), contenidoString);
                         category.addDynamicElement(data);
                         if ((enlace != null) && !enlace.isEmpty()) {
                             link = new DynamicElement(article2Link,
                                     PwdGenerator.getPassword(),
                                     ImportadorIntranet
                                             .uploadFileToDLAndGetURL(enlace));
                             data.addDynamicElement(link);
                         }
                     } else {
                         // context.set
                         final Object[] pageCategory = ImportadorIntranet.PAGE_MAP
                                 .get(idtipo);
                         if ((pageCategory != null)
                                 && (pageCategory.length != 0)) {
                             final Long categoryId = (Long) pageCategory[pageCategory.length - 1];
                             context.setAssetCategoryIds(new long[] { categoryId });
                             String articleTitle = "";
                             for (int i = 0; i < (pageCategory.length - 1); i++) {
                                 articleTitle += (String) pageCategory[i]
                                         + " - ";
                             }
                             articleTitle += titulo;
                             ImportadorIntranet.articleService.addArticle(
                                     groupId,
                                     0l,
                                     0l,
                                     "",
                                     true,
                                     new String[] { "es_ES" },
                                     new String[] { articleTitle },
                                     new String[] {},
                                     new String[] {},
                                     XMLReaderWriter.getReaderWriter().toXML(
                                            root), "General",
                                     structure2level.getStructureId(),
                                     template2level.getTemplateId(), "", 1, 1,
                                     2013, 0, 0, 0, 0, 0, 0, 0, true, 0, 0, 0,
                                     0, 0, true, true, null, context);
                         }
                         result.previous();
                         break;
                     }
                 }
                 if (result.isAfterLast()) {
                     final Object[] pageCategory = ImportadorIntranet.PAGE_MAP
                             .get(idtipo);
                     if ((pageCategory != null) && (pageCategory.length != 0)) {
                         final Long categoryId = (Long) pageCategory[pageCategory.length - 1];
                         context.setAssetCategoryIds(new long[] { categoryId });
                         String articleTitle = "";
                         for (int i = 0; i < (pageCategory.length - 1); i++) {
                             articleTitle += (String) pageCategory[i] + " - ";
                         }
                         articleTitle += titulo;
                         ImportadorIntranet.articleService.addArticle(groupId,
                                 0l, 0l, "", true, new String[] { "es_ES" },
                                 new String[] { articleTitle }, new String[] {},
                                 new String[] {}, XMLReaderWriter
                                         .getReaderWriter().toXML(root),
                                "General", structure2level.getStructureId(),
                                 template2level.getTemplateId(), "", 1, 1, 2013,
                                 0, 0, 0, 0, 0, 0, 0, true, 0, 0, 0, 0, 0, true,
                                 true, null, context);
                     }
                 }
             } else {
                 subcategories.clear();
 
                 root = new Root("es_ES", "es_ES");
                 xmlArticles.add(root);
                 category = new DynamicElement(article3Category,
                         PwdGenerator.getPassword(), tipo);
                 root.addDynamicElement(category);
                 subcategory = new DynamicElement(article3Subcategory,
                         PwdGenerator.getPassword(), titulo);
                 category.addDynamicElement(subcategory);
                 data = new DynamicElement(article3Data,
                         PwdGenerator.getPassword(), contenidoString);
                 subcategory.addDynamicElement(data);
                 if ((enlace != null) && !enlace.isEmpty()) {
                     link = new DynamicElement(article2Link,
                             PwdGenerator.getPassword(),
                             ImportadorIntranet.uploadFileToDLAndGetURL(enlace));
                     data.addDynamicElement(link);
                 }
                 subcategories.put(titulo, 0);
 
                 while (result.next()) {
                     if (idtipo.equals(result.getInt("idtipo"))
                             && grupo.equals(result.getInt("grupo"))) {
                         titulo = HtmlManipulator.replaceHtmlEntities(result
                                 .getString("titulo"));
                         enlace = result.getString("enlace");
                         contenido = result.getClob("contenido");
                         contenidoString = null;
                         if (contenido != null) {
                             contenidoString = contenido.getSubString(1l, Long
                                     .valueOf(contenido.length()).intValue());
                             contenidoString = ImportadorIntranet
                                     .uploadLinksToDL(contenidoString);
                         }
 
                         if (subcategories.containsKey(titulo)) {
                             subcategory = category.getDynamicElements().get(
                                     subcategories.get(titulo));
                         } else {
                             subcategory = new DynamicElement(
                                     article3Subcategory,
                                     PwdGenerator.getPassword(), titulo);
                             category.addDynamicElement(subcategory);
 
                             subcategories.put(titulo, category
                                     .getDynamicElements().size() - 1);
                         }
                         data = new DynamicElement(article3Data,
                                 PwdGenerator.getPassword(), contenidoString);
                         subcategory.addDynamicElement(data);
                         if ((enlace != null) && !enlace.isEmpty()) {
                             link = new DynamicElement(article3Link,
                                     PwdGenerator.getPassword(),
                                     ImportadorIntranet
                                             .uploadFileToDLAndGetURL(enlace));
                             data.addDynamicElement(link);
                         }
                     } else {
                         final Object[] pageCategory = ImportadorIntranet.PAGE_MAP
                                 .get(idtipo);
                         if ((pageCategory != null)
                                 && (pageCategory.length != 0)) {
                             final Long categoryId = (Long) pageCategory[pageCategory.length - 1];
                             context.setAssetCategoryIds(new long[] { categoryId });
                             String articleTitle = "";
                             for (int i = 0; i < (pageCategory.length - 1); i++) {
                                 articleTitle += (String) pageCategory[i]
                                         + " - ";
                             }
                             articleTitle += tipo;
                             ImportadorIntranet.articleService.addArticle(
                                     groupId,
                                     0l,
                                     0l,
                                     "",
                                     true,
                                     new String[] { "es_ES" },
                                     new String[] { articleTitle },
                                     new String[] {},
                                     new String[] {},
                                     XMLReaderWriter.getReaderWriter().toXML(
                                            root), "General",
                                     structure3level.getStructureId(),
                                     template3level.getTemplateId(), "", 1, 1,
                                     2013, 0, 0, 0, 0, 0, 0, 0, true, 0, 0, 0,
                                     0, 0, true, true, null, context);
                         }
                         result.previous();
                         break;
                     }
                 }
                 if (result.isAfterLast()) {
                     final Object[] pageCategory = ImportadorIntranet.PAGE_MAP
                             .get(idtipo);
                     if ((pageCategory != null) && (pageCategory.length != 0)) {
                         final Long categoryId = (Long) pageCategory[pageCategory.length - 1];
                         context.setAssetCategoryIds(new long[] { categoryId });
                         String articleTitle = "";
                         for (int i = 0; i < (pageCategory.length - 1); i++) {
                             articleTitle += (String) pageCategory[i] + " - ";
                         }
                         articleTitle += tipo;
                         ImportadorIntranet.articleService.addArticle(groupId,
                                 0l, 0l, "", true, new String[] { "es_ES" },
                                 new String[] { articleTitle }, new String[] {},
                                 new String[] {}, XMLReaderWriter
                                         .getReaderWriter().toXML(root),
                                "General", structure3level.getStructureId(),
                                 template3level.getTemplateId(), "", 1, 1, 2013,
                                 0, 0, 0, 0, 0, 0, 0, true, 0, 0, 0, 0, 0, true,
                                 true, null, context);
                     }
                 }
             }
         }
         Manager.closeStatement();
     }
 
     static void getCategories() throws RemoteException {
         final Long groupId = Long.valueOf(ImportadorIntranet.CONFIGURATION
                 .getString("liferayGroupId"));
         final AssetVocabularySoap vocabulary = ImportadorIntranet.vocabularyService
                 .getGroupVocabularies(groupId, "Pginas", 0, 1, null)[0];
         final Long vocabularyId = vocabulary.getVocabularyId();
 
         AssetCategorySoap category;
         AssetCategorySoap[] subcategories;
         Object[] pages;
         final AssetCategorySoap[] rootCategories = ImportadorIntranet.categoryService
                 .getVocabularyRootCategories(vocabularyId, -1, -1, null);
         for (final Entry<Integer, Object[]> entry : ImportadorIntranet.PAGE_MAP
                 .entrySet()) {
             pages = entry.getValue();
             category = null;
             for (final AssetCategorySoap rootCategory : rootCategories) {
                 if (rootCategory.getName().equals(pages[0])) {
                     category = rootCategory;
                     break;
                 }
             }
             for (int i = 1; i < (pages.length - 1); i++) {
                 subcategories = ImportadorIntranet.categoryService
                         .getChildCategories(category.getCategoryId());
                 for (final AssetCategorySoap subcategory : subcategories) {
                     if (subcategory.getName().equals(pages[i])) {
                         category = subcategory;
                         break;
                     }
                 }
             }
             pages[pages.length - 1] = category.getCategoryId();
         }
     }
 
     static List<FileEntrySoap> getFilesFromEvent(final String... urls)
             throws IOException, URISyntaxException {
         final List<FileEntrySoap> files = new ArrayList<FileEntrySoap>();
 
         for (final String url : urls) {
             if (url != null) {
                 if (url.contains("intranet/") && url.contains(".html")) {
                     final List<String> links = ImportadorIntranet
                             .getLinksFromHtml(url);
                     for (final String link : links) {
                         if (link.contains("intranet/")) {
                             final int index_start = link.indexOf("intranet/") + 9;
                             final int index_end = link.indexOf("\"",
                                     index_start);
                             files.add(ImportadorIntranet
                                     .uploadFileToDLAndGetAsset(link.substring(
                                             index_start, index_end)));
                         }
                     }
                 } else if (url.contains("intranet/")) {
                     files.add(ImportadorIntranet.uploadFileToDLAndGetAsset(url
                             .substring(url.indexOf("intranet/") + 9)));
                 }
             }
         }
         return files;
     }
 
     static List<String> getLinksFromEvent(final String... urls)
             throws ClientProtocolException, URISyntaxException, IOException {
         final List<String> files = new ArrayList<String>();
 
         for (final String url : urls) {
             if (url != null) {
                 if (url.contains("intranet/") && url.contains(".htm")) {
                     final List<String> links = ImportadorIntranet
                             .getLinksFromHtml(url);
                     for (final String link : links) {
                         if (!link.contains("intranet/")) {
                             files.add(link);
                         }
                     }
                 } else if (!url.contains("intranet/")) {
                     files.add("<a href=\"" + url + "\">" + url + "</a>");
                 }
             }
         }
         return files;
     }
 
     static List<String> getLinksFromHtml(final String url)
             throws URISyntaxException, ClientProtocolException, IOException {
         final List<String> links = new ArrayList<String>();
 
         final int index = url.indexOf("/intranet/");
         String enlaceAux = url.substring(index);
         enlaceAux = enlaceAux.trim();
 
         final String host = ImportadorIntranet.CONFIGURATION
                 .getString("intranet.file.server");
 
         final org.jsoup.nodes.Document doc = Jsoup.connect(
                 new URI("http", null, host, 80, enlaceAux, null, null)
                         .toString()).get();
         final Elements as = doc.select("a");
         final Iterator<org.jsoup.nodes.Element> it = as.iterator();
         while (it.hasNext()) {
             final org.jsoup.nodes.Element element = it.next();
             links.add(HtmlManipulator.replaceHtmlEntities(element.toString()));
         }
 
         return links;
     }
 
     static String getPageLink(final String pagePath) throws RemoteException {
         final Long groupId = Long.valueOf(ImportadorIntranet.CONFIGURATION
                 .getString("liferayGroupId"));
 
         LayoutSoap[] layouts = ImportadorIntranet.layoutService.getLayouts(
                 groupId, false, 0l);
         final LayoutSoap inicio = layouts[0];
 
         String link = inicio.getFriendlyURL();
 
         if (pagePath.startsWith("Inicio")) {
             return link;
         }
 
         final String pages[] = pagePath.split(" / ");
 
         Long parentLayoutId = inicio.getLayoutId();
         for (String page : pages) {
             page = page.trim();
             layouts = ImportadorIntranet.layoutService.getLayouts(groupId,
                     false, parentLayoutId);
             for (final LayoutSoap childPage : layouts) {
                 if (childPage.getName().contains(page)) {
                     link = childPage.getFriendlyURL();
                     parentLayoutId = childPage.getLayoutId();
                     break;
                 }
             }
         }
 
         return link;
     }
 
     static void getStructuresAndTemplates() throws RemoteException {
         final Long groupId = Long.valueOf(ImportadorIntranet.CONFIGURATION
                 .getString("liferayGroupId"));
 
         ImportadorIntranet.STRUCTURES.put(
                 ImportadorIntranet.CONFIGURATION.getString("structure.3level"),
                 null);
         ImportadorIntranet.STRUCTURES.put(
                 ImportadorIntranet.CONFIGURATION.getString("structure.2level"),
                 null);
         ImportadorIntranet.STRUCTURES.put(ImportadorIntranet.CONFIGURATION
                 .getString("structure.novedades"), null);
 
         ImportadorIntranet.TEMPLATES.put(
                 ImportadorIntranet.CONFIGURATION.getString("template.3level"),
                 null);
         ImportadorIntranet.TEMPLATES.put(
                 ImportadorIntranet.CONFIGURATION.getString("template.2level"),
                 null);
         ImportadorIntranet.TEMPLATES.put(ImportadorIntranet.CONFIGURATION
                 .getString("template.novedades"), null);
 
         final JournalStructureSoap[] structures = ImportadorIntranet.structureService
                 .getStructures(groupId);
 
         for (final JournalStructureSoap structure : structures) {
             if (ImportadorIntranet.STRUCTURES.containsKey(structure
                     .getStructureId())) {
                 ImportadorIntranet.STRUCTURES.put(structure.getStructureId(),
                         structure);
                 final JournalTemplateSoap[] templates = ImportadorIntranet.templateService
                         .getStructureTemplates(groupId,
                                 structure.getStructureId());
                 for (final JournalTemplateSoap template : templates) {
                     if (ImportadorIntranet.TEMPLATES.containsKey(template
                             .getTemplateId())) {
                         ImportadorIntranet.TEMPLATES.put(
                                 template.getTemplateId(), template);
                     }
                 }
             }
         }
     }
 
     static void getTags() throws RemoteException {
         final Long groupId = Long.valueOf(ImportadorIntranet.CONFIGURATION
                 .getString("liferayGroupId"));
 
         final AssetTagSoap[] tags = ImportadorIntranet.tagService
                 .getGroupTags(groupId);
 
         for (final AssetTagSoap tag : tags) {
             if (tag.getName()
                     .equals(ImportadorIntranet.CONFIGURATION
                             .getString("tag.novedades"))) {
                 ImportadorIntranet.novedadesTagId = tag.getTagId();
                 continue;
             }
             if (tag.getName().equals(
                     ImportadorIntranet.CONFIGURATION.getString("tag.home"))) {
                 ImportadorIntranet.homeTagId = tag.getTagId();
                 continue;
             }
             if ((ImportadorIntranet.novedadesTagId != null)
                     && (ImportadorIntranet.homeTagId != null)) {
                 break;
             }
         }
 
     }
 
     static String getXmlTag(final Element node, final String tagName) {
         if (node.getElementsByTagName(tagName).getLength() > 0) {
             return HtmlManipulator.replaceHtmlEntities(node
                     .getElementsByTagName(tagName).item(0).getTextContent()
                     .trim());
         }
         return null;
     }
 
     public static void main(final String[] args) throws Exception {
         ImportadorIntranet.cargarPropiedades();
         ImportadorIntranet.startWSClients();
        // ImportadorIntranet.getStructuresAndTemplates();
         // ImportadorIntranet.getCategories();
         // ImportadorIntranet.crearPaginasIntranet();
        // getTags();
        // crearNovedadesIntranet();
         // cargarGlosario();
        ImportadorIntranet.cargarEventos();
     }
 
     static void startWSClients() throws MalformedURLException, ServiceException {
         final String url = ImportadorIntranet.CONFIGURATION
                 .getString("urlLiferay");
         final String user = ImportadorIntranet.CONFIGURATION
                 .getString("userLiferay");
         final String pass = ImportadorIntranet.CONFIGURATION
                 .getString("pswLiferay");
 
         final JournalStructureServiceSoapServiceLocator structureServiceLocator = new JournalStructureServiceSoapServiceLocator();
         ImportadorIntranet.structureService = structureServiceLocator
                 .getPortlet_Journal_JournalStructureService(new URL(url
                         + "/Portlet_Journal_JournalStructureService"));
         final JournalTemplateServiceSoapServiceLocator templateServiceLocator = new JournalTemplateServiceSoapServiceLocator();
         ImportadorIntranet.templateService = templateServiceLocator
                 .getPortlet_Journal_JournalTemplateService(new URL(url
                         + "/Portlet_Journal_JournalTemplateService"));
         final JournalArticleServiceSoapServiceLocator articleServiceLocator = new JournalArticleServiceSoapServiceLocator();
         ImportadorIntranet.articleService = articleServiceLocator
                 .getPortlet_Journal_JournalArticleService(new URL(url
                         + "/Portlet_Journal_JournalArticleService"));
 
         final DLFileEntryServiceSoapServiceLocator dlFileEntryServiceLocator = new DLFileEntryServiceSoapServiceLocator();
         ImportadorIntranet.dlFileEntryService = dlFileEntryServiceLocator
                 .getPortlet_DL_DLFileEntryService(new URL(url
                         + "/Portlet_DL_DLFileEntryService"));
         final DLFolderServiceSoapServiceLocator dlFolderServiceLocator = new DLFolderServiceSoapServiceLocator();
         ImportadorIntranet.dlFolderService = dlFolderServiceLocator
                 .getPortlet_DL_DLFolderService(new URL(url
                         + "/Portlet_DL_DLFolderService"));
         final DLAppServiceSoapServiceLocator dlServiceLocator = new DLAppServiceSoapServiceLocator();
         ImportadorIntranet.dlService = dlServiceLocator
                 .getPortlet_DL_DLAppService(new URL(url
                         + "/Portlet_DL_DLAppService"));
 
         final AssetCategoryServiceSoapServiceLocator categoryServiceLocator = new AssetCategoryServiceSoapServiceLocator();
         ImportadorIntranet.categoryService = categoryServiceLocator
                 .getPortlet_Asset_AssetCategoryService(new URL(url
                         + "/Portlet_Asset_AssetCategoryService"));
         final AssetVocabularyServiceSoapServiceLocator vocabularyServiceLocator = new AssetVocabularyServiceSoapServiceLocator();
         ImportadorIntranet.vocabularyService = vocabularyServiceLocator
                 .getPortlet_Asset_AssetVocabularyService(new URL(url
                         + "/Portlet_Asset_AssetVocabularyService"));
 
         final AssetTagServiceSoapServiceLocator tagServiceLocator = new AssetTagServiceSoapServiceLocator();
         ImportadorIntranet.tagService = tagServiceLocator
                 .getPortlet_Asset_AssetTagService(new URL(url
                         + "/Portlet_Asset_AssetTagService"));
 
         final DDLRecordServiceSoapServiceLocator ddlRecordServiceLocator = new DDLRecordServiceSoapServiceLocator();
         ImportadorIntranet.ddlRecordService = ddlRecordServiceLocator
                 .getPortlet_DDL_DDLRecordService(new URL(url
                         + "/Portlet_DDL_DDLRecordService"));
         final DDLRecordSetServiceSoapServiceLocator ddlRecordSetServiceLocator = new DDLRecordSetServiceSoapServiceLocator();
         ImportadorIntranet.ddlRecordSetService = ddlRecordSetServiceLocator
                 .getPortlet_DDL_DDLRecordSetService(new URL(url
                         + "/Portlet_DDL_DDLRecordSetService"));
 
         final DDMStructureServiceSoapServiceLocator ddmStructureServiceLocator = new DDMStructureServiceSoapServiceLocator();
         ImportadorIntranet.ddmStructureService = ddmStructureServiceLocator
                 .getPortlet_DDM_DDMStructureService(new URL(url
                         + "/Portlet_DDM_DDMStructureService"));
         final DDMTemplateServiceSoapServiceLocator ddmTemplateServiceLocator = new DDMTemplateServiceSoapServiceLocator();
         ImportadorIntranet.ddmTemplateService = ddmTemplateServiceLocator
                 .getPortlet_DDM_DDMTemplateService(new URL(url
                         + "/Portlet_DDM_DDMTemplateService"));
 
         final LayoutServiceSoapServiceLocator layoutServiceLocator = new LayoutServiceSoapServiceLocator();
         ImportadorIntranet.layoutService = layoutServiceLocator
                 .getPortal_LayoutService(new URL(url + "/Portal_LayoutService"));
 
         final CalEventServiceSoapServiceLocator calEventServiceLocator = new CalEventServiceSoapServiceLocator();
         ImportadorIntranet.calEventServiceSoap = calEventServiceLocator
                 .getPortlet_Cal_CalEventService(new URL(
                         (url + "/Portlet_Cal_CalEventService")));
 
         ((Portlet_Journal_JournalStructureServiceSoapBindingStub) ImportadorIntranet.structureService)
                 .setUsername(user);
         ((Portlet_Journal_JournalStructureServiceSoapBindingStub) ImportadorIntranet.structureService)
                 .setPassword(pass);
 
         ((Portlet_Journal_JournalArticleServiceSoapBindingStub) ImportadorIntranet.articleService)
                 .setUsername(user);
         ((Portlet_Journal_JournalArticleServiceSoapBindingStub) ImportadorIntranet.articleService)
                 .setPassword(pass);
 
         ((Portlet_Journal_JournalTemplateServiceSoapBindingStub) ImportadorIntranet.templateService)
                 .setUsername(user);
         ((Portlet_Journal_JournalTemplateServiceSoapBindingStub) ImportadorIntranet.templateService)
                 .setPassword(pass);
 
         ((Portlet_DL_DLFolderServiceSoapBindingStub) ImportadorIntranet.dlFolderService)
                 .setUsername(user);
         ((Portlet_DL_DLFolderServiceSoapBindingStub) ImportadorIntranet.dlFolderService)
                 .setPassword(pass);
 
         ((Portlet_DL_DLFileEntryServiceSoapBindingStub) ImportadorIntranet.dlFileEntryService)
                 .setUsername(user);
         ((Portlet_DL_DLFileEntryServiceSoapBindingStub) ImportadorIntranet.dlFileEntryService)
                 .setPassword(pass);
 
         ((Portlet_DL_DLAppServiceSoapBindingStub) ImportadorIntranet.dlService)
                 .setUsername(user);
         ((Portlet_DL_DLAppServiceSoapBindingStub) ImportadorIntranet.dlService)
                 .setPassword(pass);
 
         ((Portlet_Asset_AssetCategoryServiceSoapBindingStub) ImportadorIntranet.categoryService)
                 .setUsername(user);
         ((Portlet_Asset_AssetCategoryServiceSoapBindingStub) ImportadorIntranet.categoryService)
                 .setPassword(pass);
         ((Portlet_Asset_AssetVocabularyServiceSoapBindingStub) ImportadorIntranet.vocabularyService)
                 .setUsername(user);
         ((Portlet_Asset_AssetVocabularyServiceSoapBindingStub) ImportadorIntranet.vocabularyService)
                 .setPassword(pass);
         ((Portlet_Asset_AssetTagServiceSoapBindingStub) ImportadorIntranet.tagService)
                 .setUsername(user);
         ((Portlet_Asset_AssetTagServiceSoapBindingStub) ImportadorIntranet.tagService)
                 .setPassword(pass);
 
         ((Portlet_DDL_DDLRecordServiceSoapBindingStub) ImportadorIntranet.ddlRecordService)
                 .setUsername(user);
         ((Portlet_DDL_DDLRecordServiceSoapBindingStub) ImportadorIntranet.ddlRecordService)
                 .setPassword(pass);
         ((Portlet_DDL_DDLRecordSetServiceSoapBindingStub) ImportadorIntranet.ddlRecordSetService)
                 .setUsername(user);
         ((Portlet_DDL_DDLRecordSetServiceSoapBindingStub) ImportadorIntranet.ddlRecordSetService)
                 .setPassword(pass);
 
         ((Portlet_DDM_DDMStructureServiceSoapBindingStub) ImportadorIntranet.ddmStructureService)
                 .setUsername(user);
         ((Portlet_DDM_DDMTemplateServiceSoapBindingStub) ImportadorIntranet.ddmTemplateService)
                 .setPassword(pass);
 
         ((Portal_LayoutServiceSoapBindingStub) ImportadorIntranet.layoutService)
                 .setUsername(user);
         ((Portal_LayoutServiceSoapBindingStub) ImportadorIntranet.layoutService)
                 .setPassword(pass);
 
         ((Portlet_Cal_CalEventServiceSoapBindingStub) ImportadorIntranet.calEventServiceSoap)
                 .setUsername(user);
         ((Portlet_Cal_CalEventServiceSoapBindingStub) ImportadorIntranet.calEventServiceSoap)
                 .setPassword(pass);
     }
 
     static FileEntrySoap uploadFileToDLAndGetAsset(final String enlace)
             throws IOException, URISyntaxException {
         if ((enlace == null)
                 || (!enlace.startsWith("docs/")
                         && !enlace.startsWith("images/") && !enlace
                             .startsWith("agenda/"))) {
             return null;
         }
 
         String enlaceAux = enlace.trim().replace("%20", " ");
         enlaceAux = enlaceAux.replace("%C3%B1", "");
         enlaceAux = enlaceAux.replace("%C3%B3", "");
         if (enlaceAux.contains("#")) {
             final String[] aux = enlaceAux.split("#");
             enlaceAux = aux[0];
         }
         enlaceAux = HtmlManipulator.replaceHtmlEntities(enlaceAux);
 
         final String host = ImportadorIntranet.CONFIGURATION
                 .getString("intranet.file.server");
 
         final HttpClient httpclient = new DefaultHttpClient();
 
         final HttpGet httpget = new HttpGet(new URI("http", null, host, 80,
                 "/intranet/" + enlaceAux, null, null));
         final HttpResponse response = httpclient.execute(httpget);
         if (response.getStatusLine().getStatusCode() != 200) {
             ImportadorIntranet.log.error("error downloading file: "
                     + new URI("http", null, host, 80, "/intranet/" + enlaceAux,
                             null, null));
             return null;
         }
         final String contentType = response.getEntity().getContentType()
                 .getValue();
         final byte[] fileData = EntityUtils.toByteArray(response.getEntity());
 
         httpclient.getConnectionManager().shutdown();
 
         final String[] path = enlaceAux.trim().split("/");
 
         final Long groupId = Long.valueOf(ImportadorIntranet.CONFIGURATION
                 .getString("liferayGroupId"));
         Long parentFolderId = 0L;
         for (int i = 0; i < (path.length - 1); i++) {
             DLFolderSoap folder = null;
             try {
                 folder = ImportadorIntranet.dlFolderService.getFolder(groupId,
                         parentFolderId, path[i]);
             } catch (final Exception e) {
                 folder = ImportadorIntranet.dlFolderService.addFolder(groupId,
                         groupId, false, parentFolderId, path[i], null,
                         new ServiceContext());
             }
             parentFolderId = folder.getFolderId();
         }
 
         String fileName = path[path.length - 1];
         fileName = fileName.replace('[', '(');
         fileName = fileName.replace(']', ')');
         fileName = fileName.replace(';', '_');
         final String fileTitle = fileName.substring(0, fileName.indexOf('.'));
 
         FileEntrySoap lfrFile = null;
         final ServiceContext context = new ServiceContext();
         context.setWorkflowAction(1);
         try {
             lfrFile = ImportadorIntranet.dlService.getFileEntry(groupId,
                     parentFolderId, fileTitle);
         } catch (final Exception e) {
             try {
                 lfrFile = ImportadorIntranet.dlService.addFileEntry(groupId,
                         parentFolderId, fileName, contentType, fileTitle, null,
                         null, fileData, context);
             } catch (final Exception ex) {
                 ImportadorIntranet.log.error("error uploading file: " + enlace);
                 return null;
             }
         }
         return lfrFile;
     }
 
     static String uploadFileToDLAndGetURL(final String enlace)
             throws IOException, URISyntaxException {
         if ((enlace == null)
                 || (!enlace.startsWith("docs/") && !enlace
                         .startsWith("images/"))) {
             return enlace;
         }
 
         final FileEntrySoap lfrFile = ImportadorIntranet
                 .uploadFileToDLAndGetAsset(enlace);
         if (lfrFile == null) {
             return null;
         }
         final String enlaceAux = enlace.trim().replaceAll("%20", " ");
         String fragment = null;
         if (enlaceAux.contains("#")) {
             final String[] aux = enlaceAux.split("#");
             fragment = aux[1];
         }
 
         final Long groupId = Long.valueOf(ImportadorIntranet.CONFIGURATION
                 .getString("liferayGroupId"));
         String link = new URI("http", null, "fake.host.com", 80, "/documents/"
                 + groupId + "/" + lfrFile.getFolderId() + "/"
                 + lfrFile.getTitle(), null, fragment).toString();
 
         link = link.substring(link.indexOf("/documents/"));
         return link;
     }
 
     static String uploadLinksToDL(String contenidoString) throws IOException,
             URISyntaxException {
         if (contenidoString == null) {
             return null;
         }
         contenidoString = contenidoString.replaceAll("\n", " ");
         String oldLink = null;
         String newLink = null;
         while (contenidoString.contains("href=\"/intranet/")) {
             oldLink = contenidoString.substring(
                     contenidoString.indexOf("href=\"/intranet/") + 16,
                     contenidoString.indexOf("\"",
                             contenidoString.indexOf("href=\"/intranet/") + 16));
             newLink = ImportadorIntranet.uploadFileToDLAndGetURL(oldLink);
             contenidoString = contenidoString.replace("/intranet/" + oldLink,
                     newLink == null ? "" : newLink);
         }
         while (contenidoString.contains("src=\"/intranet/")) {
             oldLink = contenidoString.substring(
                     contenidoString.indexOf("src=\"/intranet/") + 15,
                     contenidoString.indexOf("\"",
                             contenidoString.indexOf("src=\"/intranet/") + 15));
             newLink = ImportadorIntranet.uploadFileToDLAndGetURL(oldLink);
             contenidoString = contenidoString.replace("/intranet/" + oldLink,
                     newLink == null ? "" : newLink);
         }
         while (contenidoString.contains("href=\"docs/")) {
             oldLink = contenidoString.substring(
                     contenidoString.indexOf("href=\"docs/") + 6,
                     contenidoString.indexOf("\"",
                             contenidoString.indexOf("href=\"docs/") + 6));
             newLink = ImportadorIntranet.uploadFileToDLAndGetURL(oldLink);
             contenidoString = contenidoString.replace(oldLink,
                     newLink == null ? "" : newLink);
         }
         while (contenidoString.contains("src=\"images/")) {
             oldLink = contenidoString.substring(
                     contenidoString.indexOf("src=\"images/") + 5,
                     contenidoString.indexOf("\"",
                             contenidoString.indexOf("src=\"images/") + 5));
             newLink = ImportadorIntranet.uploadFileToDLAndGetURL(oldLink);
             contenidoString = contenidoString.replace(oldLink,
                     newLink == null ? "" : newLink);
         }
         while (contenidoString
                 .contains("href=\"http://intranet.cmt.es/intranet/")) {
             oldLink = contenidoString
                     .substring(
                             contenidoString
                                     .indexOf("href=\"http://intranet.cmt.es/intranet/") + 38,
                             contenidoString.indexOf(
                                     "\"",
                                     contenidoString
                                             .indexOf("href=\"http://intranet.cmt.es/intranet/") + 38));
             newLink = ImportadorIntranet.uploadFileToDLAndGetURL(oldLink);
             contenidoString = contenidoString.replace(
                     "http://intranet.cmt.es/intranet/" + oldLink,
                     newLink == null ? "" : newLink);
         }
         while (contenidoString
                 .contains("src=\"http://intranet.cmt.es/intranet/")) {
             oldLink = contenidoString
                     .substring(
                             contenidoString
                                     .indexOf("src=\"http://intranet.cmt.es/intranet/") + 37,
                             contenidoString.indexOf(
                                     "\"",
                                     contenidoString
                                             .indexOf("src=\"http://intranet.cmt.es/intranet/") + 37));
             newLink = ImportadorIntranet.uploadFileToDLAndGetURL(oldLink);
             contenidoString = contenidoString.replace(
                     "http://intranet.cmt.es/intranet/" + oldLink,
                     newLink == null ? "" : newLink);
         }
 
         return contenidoString;
     }
 }
