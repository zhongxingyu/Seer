 package es.udc.cartolab.gvsig.pmf.reports.community;
 
 /*
  * Copyright (c) 2010. Cartolab (Universidade da Corua)
  * 
  * This file is part of gvSIG Fonsagua.
  * 
  * gvSIG Fonsagua is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * gvSIG Fonsagua is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with gvSIG Fonsagua.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.sql.SQLException;
 
 import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
 import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
 import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
 import com.lowagie.text.Chunk;
 import com.lowagie.text.DocumentException;
 import com.lowagie.text.HeaderFooter;
 import com.lowagie.text.List;
 import com.lowagie.text.ListItem;
 import com.lowagie.text.Paragraph;
 import com.lowagie.text.Phrase;
 import com.lowagie.text.Table;
 import com.lowagie.text.rtf.RtfWriter2;
 import com.lowagie.text.rtf.document.RtfDocumentSettings;
 import com.lowagie.text.rtf.style.RtfFont;
 
 import es.udc.cartolab.gvsig.pmf.forms.CentrosEducativosForm;
 import es.udc.cartolab.gvsig.pmf.forms.CentrosReunionesForm;
 import es.udc.cartolab.gvsig.pmf.forms.CentrosSaludForm;
 import es.udc.cartolab.gvsig.pmf.forms.ComunidadesForm;
 import es.udc.cartolab.gvsig.pmf.forms.CultivosForm;
 import es.udc.cartolab.gvsig.pmf.forms.OrganizacionesBaseForm;
 import es.udc.cartolab.gvsig.pmf.forms.PresenciaInstitucionalForm;
 import es.udc.cartolab.gvsig.pmf.forms.InformacionGeneralForm;
 import es.udc.cartolab.gvsig.pmf.reports.RtfBaseReport;
 import es.udc.cartolab.gvsig.pmf.utils.PmfConstants;
 
 public class RtfCommunityReport extends RtfBaseReport {
 
     private String codCom = null;
 
     // Campos y cabeceras de listados
     private final String[] creuFieldNames = { "nom_creu", "direccion",
 	    "responsa" };
     private final String[] creuBoolFieldNames = {};
     private final String[] creuFieldHeaders = {
 	    "Nombre del centro de reuniones: ", "Direccin: ", "Responsable: " };
     private final String[] ceduFieldNames = { "nom_cedu", "tipo_cedu",
 	    "n_ninhos", "n_ninhas", "i_deserc" };
     private final String[] ceduBoolFieldNames = { "mer_escol" };
     private final String[] ceduFieldHeaders = {
 	    "Nombre del centro educativo: ", "Tipo de centro educativo: ",
 	    "Nmero de nios: ", "Nmero de nias: ",
 	    "ndice de desercin (%): ", "Merienda escolar: " };
     private final String[] csaludFieldNames = { "nom_csalud" };
     private final String[] csaludBoolFieldNames = { "inf_resp", "inf_piel",
 	    "inf_inst", "inf_vec" };
     private final String[] csaludFieldHeaders = {
 	    "Nombre del centro de salud: ",
 	    "Atencin a infecciones respiratorias agudas: ",
 	    "Atencin a infecciones de la piel: ",
 	    "Atencin a infecciones intestinales: ",
 	    "Atencin a infecciones vectoriales: " };
     private final String[] orgBaseFieldNames = { "org_exist", "func", "resp" };
     private final String[] orgBaseBoolFieldNames = {};
     private final String[] orgBaseFieldHeaders = { "NOMBRE DE LA ORGANIZACIN",
 	    "FUNCIONES DE LA ORGANIZACIN", "PERSONA RESPONSABLE" };
     private final String[] presInstFieldNames = { "org_exist", "func", "resp" };
     private final String[] presInstBoolFieldNames = {};
     private final String[] presInstFieldHeaders = {
 	    "NOMBRE DE LA ORGANIZACIN", "FUNCIONES DE LA ORGANIZACIN",
 	    "PERSONA RESPONSABLE" };
     private final String[] cultivosFieldNames = { "area", "tipo" };
     private final String[] cultivosBoolFieldNames = {};
     private final String[] cultivosFieldHeaders = { "REA", "TIPO DE CULTIVO" };
 
     // FONT STYLES
     private final RtfFont titleFont = new RtfFont("Century Gothic", 24,
 	    RtfFont.STYLE_BOLD);
     private final RtfFont subtitleFont = new RtfFont("Century Gothic", 24,
 	    RtfFont.STYLE_NONE);
     private final RtfFont normalBoldTextFont = new RtfFont("Century Gothic",
 	    10, RtfFont.STYLE_BOLD);
     private final RtfFont normalTextFont = new RtfFont("Century Gothic", 10,
 	    RtfFont.STYLE_NONE);
     private final RtfFont normalItalicTextFont = new RtfFont("Century Gothic",
 	    10, RtfFont.STYLE_ITALIC);
     private final RtfFont tableTitleTextFont = new RtfFont("Century Gothic", 9,
 	    RtfFont.STYLE_NONE);
     private final RtfFont sectionFont = new RtfFont("Century Gothic", 14,
 	    RtfFont.STYLE_BOLD);
     private final RtfFont subsectionFont = new RtfFont("Century Gothic", 11,
 	    RtfFont.STYLE_BOLD);
 
     public RtfCommunityReport(int nRow, SelectableDataSource source,
 	    String fileName) {
 
 	this.source = source;
 	this.nRow = nRow;
 	this.fileName = fileName;
 	try {
 	    this.codCom = source.getFieldValue(nRow,
 		    source.getFieldIndexByName(ComunidadesForm.PKFIELD))
 		    .toString();
 	} catch (ReadDriverException e1) {
 	    e1.printStackTrace();
 	}
 	try {
 
 	    startDocument();
 	    createSection1();
 	    createSection2();
 	    createSection3();
 	    createSection4();
 
 	    // close document
 	    document.close();
 
 	} catch (FileNotFoundException e) {
 	    e.printStackTrace();
 	} catch (DocumentException e) {
 	    e.printStackTrace();
 	} catch (Exception e) {
 	    e.printStackTrace();
 	}
     }
 
     private void startDocument() throws FileNotFoundException,
 	    DocumentException, ReadDriverException {
 
 	RtfWriter2 writer;
 	// getting document instance and opening to write
 	writer = RtfWriter2.getInstance(document,
 		new FileOutputStream(fileName));
 	document.open();
 	RtfDocumentSettings settings = writer.getDocumentSettings();
 	settings.setOutputTableRowDefinitionAfter(true);
 
 	HeaderFooter footer = new HeaderFooter(new Phrase("", normalTextFont),
 		true);
 	footer.setAlignment(HeaderFooter.ALIGN_RIGHT);
 	document.setFooter(footer);
 
 	// Title
 	Paragraph reportTitle = new Paragraph(
 		"\n\n\n\n\n\nREPORTE DE LA COMUNIDAD", titleFont);
 	reportTitle.setAlignment(Paragraph.ALIGN_CENTER);
 	document.add(reportTitle);
 
 	// Subtitle
 	Paragraph reportSubtitle = new Paragraph("("
 		+ source.getFieldValue(nRow,
 			source.getFieldIndexByName("nombre")).toString() + ")",
 		subtitleFont);
 	reportSubtitle.setAlignment(Paragraph.ALIGN_CENTER);
 	document.add(reportSubtitle);
 
 	Paragraph aldea = new Paragraph();
 	aldea.add(new Chunk("\n\n\n\n\nCdigo de la comunidad:",
 		normalBoldTextFont));
 	aldea.add(new Chunk(" " + codCom, normalTextFont));
 	aldea.setAlignment(Paragraph.ALIGN_RIGHT);
 	document.add(aldea);
 
 	document.newPage();
     }
 
     private void createSection1() throws DocumentException, ReadDriverException {
 
 	// Section 1
 	Paragraph sectionTitle = new Paragraph(
 		"1. DATOS GENERALES DE LA COMUNIDAD\n", sectionFont);
 	document.add(sectionTitle);
 	Paragraph sectionBody = new Paragraph();
 	List list = new List();
 	list.setIndentationLeft(20);
 	list.setSymbolIndent(15);
 	list.setListSymbol(new Chunk("\u2022", normalBoldTextFont));
 	Phrase phrase = new Phrase();
 	phrase.add(new Chunk("Comunidad: ", normalBoldTextFont));
 	phrase.add(new Chunk(source.getFieldValue(nRow,
 		source.getFieldIndexByName("nombre")).toString(),
 		normalTextFont));
 	list.add(new ListItem(phrase));
 	phrase = new Phrase();
 	phrase.add(new Chunk("Cdigo de la comunidad: ", normalBoldTextFont));
 	phrase.add(new Chunk(codCom, normalTextFont));
 	list.add(new ListItem(phrase));
 	sectionBody.add(list);
 	document.add(sectionBody);
 
 	// Section 1.1
 	sectionTitle = new Paragraph("1.1. Ubicacin\n", subsectionFont);
 	document.add(sectionTitle);
 	sectionBody = new Paragraph();
 	list = new List();
 	list.setIndentationLeft(20);
 	list.setSymbolIndent(15);
 	list.setListSymbol(new Chunk("\u2022", normalBoldTextFont));
 	phrase = new Phrase();
 	phrase.add(new Chunk("Departamento: ", normalBoldTextFont));
 	phrase.add(new Chunk(source.getFieldValue(nRow,
 		source.getFieldIndexByName("departa")).toString(),
 		normalTextFont));
 	list.add(new ListItem(phrase));
 	phrase = new Phrase();
 	phrase.add(new Chunk("Municipio: ", normalBoldTextFont));
 	phrase.add(new Chunk(source.getFieldValue(nRow,
 		source.getFieldIndexByName("municip")).toString(),
 		normalTextFont));
 	list.add(new ListItem(phrase));
 	sectionBody.add(list);
 	document.add(sectionBody);
 
 	// Section 1.2
 	sectionTitle = new Paragraph("1.2. Poblacin\n", subsectionFont);
 	document.add(sectionTitle);
 	sectionBody = new Paragraph();
 	list = new List();
 	list.setIndentationLeft(20);
 	list.setSymbolIndent(15);
 	list.setListSymbol(new Chunk("\u2022", normalBoldTextFont));
 	phrase = new Phrase();
 	phrase.add(new Chunk("Nmero de habitantes: ", normalBoldTextFont));
 	phrase.add(new Chunk(source.getFieldValue(nRow,
 		source.getFieldIndexByName("n_habit")).toString(),
 		normalTextFont));
 	list.add(new ListItem(phrase));
 	phrase = new Phrase();
 	phrase.add(new Chunk("Nmero de familias: ", normalBoldTextFont));
 	phrase.add(new Chunk(source.getFieldValue(nRow,
 		source.getFieldIndexByName("n_fam")).toString(), normalTextFont));
 	list.add(new ListItem(phrase));
 	sectionBody.add(list);
 	document.add(sectionBody);
 
     }
 
     private void createSection2() throws DocumentException, ReadDriverException {
 
 	// Section 2
 	Paragraph sectionTitle = new Paragraph(
 		"\n\n2. ASPECTOS ORGANIZATIVOS:\n", sectionFont);
 	document.add(sectionTitle);
 
 	// Section 2.1
 	sectionTitle = new Paragraph("2.1. Centro comunal de reuniones:\n",
 		subsectionFont);
 	document.add(sectionTitle);
 	Paragraph sectionBody = new Paragraph();
 	Phrase phrase;
 	List list = getFieldsList(
 		CentrosReunionesForm.NAME,
		CentrosReunionesForm.PKFIELD,
 		source.getFieldValue(nRow,
 			source.getFieldIndexByName(ComunidadesForm.PKFIELD))
 			.toString(), creuFieldNames, creuBoolFieldNames,
 		creuFieldHeaders);
 	if (list.isEmpty()) {
 	    phrase = new Phrase();
 	    phrase.add(new Chunk("\tNo presenta Centros de Reuniones.",
 		    normalItalicTextFont));
 	    sectionBody.add(phrase);
 	} else {
 	    sectionBody.add(list);
 	}
 	document.add(sectionBody);
 
 	// Section 2.2
 	sectionTitle = new Paragraph("2.2. Organizaciones de base:\n",
 		subsectionFont);
 	document.add(sectionTitle);
 
 	// Base organizations table
 	Table table = getFieldsTable(OrganizacionesBaseForm.NAME,
 		ComunidadesForm.PKFIELD, codCom, orgBaseFieldNames,
 		orgBaseBoolFieldNames, orgBaseFieldHeaders);
 	document.add(table);
 
 	// Section 2.3
 	sectionTitle = new Paragraph("\n\n2.3. Presencia institucional:\n",
 		subsectionFont);
 	document.add(sectionTitle);
 
 	// Inst. presence table
 	table = getFieldsTable(PresenciaInstitucionalForm.NAME,
 		ComunidadesForm.PKFIELD, codCom, presInstFieldNames,
 		presInstBoolFieldNames, presInstFieldHeaders);
 	document.add(table);
     }
 
     private void createSection3() throws DocumentException, ReadDriverException {
 
 	// Section 3
 	Paragraph sectionTitle = new Paragraph("\n\n\n3. SERVICIOS BSICOS:\n",
 		sectionFont);
 	document.add(sectionTitle);
 
 	// Section 3.1
 	sectionTitle = new Paragraph("3.1. Servicios bsicos:\n",
 		subsectionFont);
 	document.add(sectionTitle);
 	Paragraph sectionBody = new Paragraph();
 	List list = new List();
 	list.setIndentationLeft(20);
 	list.setSymbolIndent(15);
 	list.setListSymbol(new Chunk("\u2022", normalBoldTextFont));
 	Phrase phrase = new Phrase();
 	phrase.add(new Chunk("Electricidad: ", normalBoldTextFont));
 	if (source.getFieldValue(nRow, source.getFieldIndexByName("luz_elec"))
 		.toString().equals("true")) {
 	    phrase.add(new Chunk("S", normalItalicTextFont));
 	} else {
 	    phrase.add(new Chunk("No", normalItalicTextFont));
 	}
 	list.add(new ListItem(phrase));
 	phrase = new Phrase();
 	phrase.add(new Chunk("Agua potable: ", normalBoldTextFont));
 	if (source.getFieldValue(nRow, source.getFieldIndexByName("agua_pot"))
 		.toString().equals("true")) {
 	    phrase.add(new Chunk("S", normalItalicTextFont));
 	} else {
 	    phrase.add(new Chunk("No", normalItalicTextFont));
 	}
 	list.add(new ListItem(phrase));
 	phrase = new Phrase();
 	phrase.add(new Chunk("Alcantarillado: ", normalBoldTextFont));
 	if (source.getFieldValue(nRow, source.getFieldIndexByName("alcantar"))
 		.toString().equals("true")) {
 	    phrase.add(new Chunk("S", normalItalicTextFont));
 	} else {
 	    phrase.add(new Chunk("No", normalItalicTextFont));
 	}
 	list.add(new ListItem(phrase));
 	phrase = new Phrase();
 	phrase.add(new Chunk("Telfono fijo: ", normalBoldTextFont));
 	if (source.getFieldValue(nRow, source.getFieldIndexByName("tfn_fijo"))
 		.toString().equals("true")) {
 	    phrase.add(new Chunk("S", normalItalicTextFont));
 	} else {
 	    phrase.add(new Chunk("No", normalItalicTextFont));
 	}
 	list.add(new ListItem(phrase));
 	sectionBody.add(list);
 	document.add(sectionBody);
 
 	// Section 3.2
 	sectionTitle = new Paragraph("3.2. Centros educativos:\n",
 		subsectionFont);
 	document.add(sectionTitle);
 	sectionBody = new Paragraph();
 	list = getFieldsList(
 		CentrosEducativosForm.NAME,
		CentrosEducativosForm.PKFIELD,
 		source.getFieldValue(nRow,
 			source.getFieldIndexByName(ComunidadesForm.PKFIELD))
 			.toString(), ceduFieldNames, ceduBoolFieldNames,
 		ceduFieldHeaders);
 	if (list.isEmpty()) {
 	    phrase = new Phrase();
 	    phrase.add(new Chunk("\tNo presenta Centros Educativos.",
 		    normalItalicTextFont));
 	    sectionBody.add(phrase);
 	} else {
 	    sectionBody.add(list);
 	}
 	document.add(sectionBody);
 
 	// Section 3.3
 	sectionTitle = new Paragraph("3.3. Asistencia sanitaria:\n",
 		subsectionFont);
 	document.add(sectionTitle);
 	sectionBody = new Paragraph();
 	list = getFieldsList(
 		CentrosSaludForm.NAME,
		CentrosSaludForm.PKFIELD,
 		source.getFieldValue(nRow,
 			source.getFieldIndexByName(ComunidadesForm.PKFIELD))
 			.toString(), csaludFieldNames, csaludBoolFieldNames,
 		csaludFieldHeaders);
 	if (list.isEmpty()) {
 	    phrase = new Phrase();
 	    phrase.add(new Chunk("\tNo presenta Centros de Salud.",
 		    normalItalicTextFont));
 	    sectionBody.add(phrase);
 	} else {
 	    sectionBody.add(list);
 	}
 	document.add(sectionBody);
     }
 
     private void createSection4() throws DocumentException,
 	    ExpansionFileReadException, ReadDriverException {
 
 	// Section 4
 	Paragraph sectionTitle = new Paragraph(
 		"\n\n4. RESUMEN DE CULTIVOS EN LA COMUNIDAD:\n", sectionFont);
 	document.add(sectionTitle);
 
 	try {
 	    String[] vivCods = session
 		    .getDistinctValues(
 			    InformacionGeneralForm.NAME,
 			    PmfConstants.DATA_SCHEMA,
 			    InformacionGeneralForm.PKFIELD,
 			    false,
 			    false,
 			    "WHERE "
 				    + ComunidadesForm.PKFIELD
 				    + " = '"
 				    + source.getFieldValue(
 					    nRow,
 					    source.getFieldIndexByName(ComunidadesForm.PKFIELD))
 					    .toString() + "'");
 
 	    // Farming table
 	    Table table = getFieldsTable(CultivosForm.NAME,
		    InformacionGeneralForm.PKFIELD, vivCods, cultivosFieldNames,
		    cultivosBoolFieldNames, cultivosFieldHeaders);
 	    document.add(table);
 	} catch (SQLException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
     }
 
 }// Class
