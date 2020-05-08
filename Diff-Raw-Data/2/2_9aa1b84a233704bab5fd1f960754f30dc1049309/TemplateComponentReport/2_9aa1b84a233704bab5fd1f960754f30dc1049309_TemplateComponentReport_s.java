 package com.thingtrack.konekti.view.addon.ui;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import net.sf.jasperreports.engine.JRException;
 import net.sf.jasperreports.engine.JRExporter;
 import net.sf.jasperreports.engine.JRExporterParameter;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.export.JRCsvExporter;
 import net.sf.jasperreports.engine.export.JRHtmlExporter;
 import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
 import net.sf.jasperreports.engine.export.JRPdfExporter;
 import net.sf.jasperreports.engine.export.JRRtfExporter;
 import net.sf.jasperreports.engine.export.JRXhtmlExporter;
 import net.sf.jasperreports.engine.export.JRXmlExporter;
 import net.sf.jasperreports.engine.export.oasis.JROdsExporter;
 import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
 import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
 import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter;
 import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
 import net.sf.jasperreports.j2ee.servlets.ImageServlet;
 
 import org.dellroad.stuff.vaadin.VaadinConfigurable;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.FrameworkUtil;
 import org.osgi.framework.ServiceReference;
 import org.vaadin.hene.splitbutton.SplitButton;
 import org.vaadin.hene.splitbutton.SplitButton.SplitButtonClickEvent;
 import org.vaadin.hene.splitbutton.SplitButton.SplitButtonClickListener;
 
 import ar.com.fdvs.dj.domain.Style;
 import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
 import ar.com.fdvs.dj.domain.builders.DynamicReportBuilder;
 import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;
 import ar.com.fdvs.dj.domain.entities.conditionalStyle.ConditionalStyle;
 
 import com.thingtrack.konekti.report.ReportManagerService;
 import com.vaadin.Application;
 import com.vaadin.terminal.StreamResource;
 import com.vaadin.terminal.UserError;
 import com.vaadin.terminal.gwt.server.WebApplicationContext;
 import com.vaadin.ui.Accordion;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.CheckBox;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.CustomComponent;
 import com.vaadin.ui.GridLayout;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.HorizontalSplitPanel;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.themes.BaseTheme;
 import com.vaadin.ui.themes.Reindeer;
 
 /**
  * Extend this template class to create custom reports.
  * @author Alejandro Duarte
  *
  */
 @VaadinConfigurable
 public class TemplateComponentReport extends CustomComponent implements ClickListener, SplitButtonClickListener {
 
 	private static final long serialVersionUID = 1L;
 	
 	protected HorizontalSplitPanel layout;
 	protected VerticalLayout htmlLayout = new VerticalLayout();
 	protected Label htmlLabel;
 	protected HorizontalLayout displayLayout;
 	protected Button refreshButton;
 	protected Button pdfButton;
 	protected Button excelButton;
 	protected Button wordButton;
 	protected Button powerPointButton;
 	protected Button odsButton;
 	protected Button odtButton;
 	protected Button rtfButton;
 	protected Button htmlButton;
 	protected Button csvButton;
 	protected Button xmlButton;
 	protected Accordion accordion;
 	protected CheckBox printBackgroundOnOddRowsCheckBox;
 	protected CheckBox printColumnNamesCheckBox;
 	protected CheckBox stretchWithOverflowCheckBox;
 	protected TextField columnsPerPageTextField;
 	protected TextField pageWidthTextField;
 	protected TextField pageHeightTextField;
 	protected TextField marginTopTextField;
 	protected TextField marginBottomTextField;
 	protected TextField marginLeftTextField;
 	protected TextField marginRightTextField;
 	protected CheckBox[] columnsCheckBoxes;
 	protected CheckBox[] groupingCheckBoxes;
 	protected VerticalLayout observationsLayout;
 	protected Label observationsLabel;
 	
 	private Application application;
 
 	private String[] columnProperties;
 	private Class<?>[] columnClasses;
 	private String[] columnTitles;
 	private Collection<?> data;
 	private Component parametersComponent;
 
 	private ReportManagerService reportManagerService;
     
 	private JasperPrint jasperPrint;
 	
 	public TemplateComponentReport() {
 		getServices();
 		
 	}
 	
 	@Override
 	public void attach() {
 		super.attach();
 		
 		application = getApplication();
 		
 		initLayout();
 		build();
 		
 	}
 	
 	/**
 	 * @return property names that correspond to the objects returned by getData(). Each instance returned by
 	 * getData() must have a getter for each property returned by this method.
 	 */	
 	public String[] getColumnProperties() {
 		return columnProperties;
 	}
 	
 	public void setColumnProperties(String[] columnProperties) {
 		this.columnProperties = columnProperties;
 		
 	}
 	
 	/**
 	 * @return property classes that correspond to the objects returned by getData() according to getColumnProperties().
 	 */	
 	public Class<?>[] getColumnClasses() {
 		return columnClasses;
 	}
 	
 	public void setColumnClasses(Class<?>[] columnClasses) {
 		this.columnClasses = columnClasses;
 		
 	}
 	
 	/**
 	 * @return titles to use on the table.
 	 */	
 	public String[] getColumnTitles() {
 		return columnTitles;
 	}
 	
 	public void setColumnTitles(String[] columnTitles) {
 		this.columnTitles = columnTitles;
 		
 	}
 	
 	/**
 	 * Collection of rows to show on the table. Each object in the collection must define a getter for each String returned
 	 * in getColumnProperties().
 	 */	
 	public Collection<?> getData() {
 		return data;
 	}
 	
 	public void setData(Collection<?> data) {
 		this.data = data;
 		
 	}
 	
 	/**
 	 * @return A custom component to add to the accordion component. You can use it to add custom filtering or configuration
 	 * to the report. Return null if no component is needed.
 	 */	
 	public Component getParametersComponent() {
 		return parametersComponent;
 	}
 	
 	public void setParametersComponent(Component parametersComponent) {
 		this.parametersComponent = parametersComponent;
 		
 	}
 	
 	public Integer[] getColumnWidths() { return null; };
 	
 	public String getFileName() { return "report"; };
 	
 	public void configureColumn(String property, AbstractColumn column, DynamicReportBuilder reportBuilder) {};
 	
 	public void configureColumnBuilder(String property, ColumnBuilder columnBuilder, DynamicReportBuilder reportBuilder) {};
 	
 	public boolean getDefalutColumnCheckBox(String property) { return true; };
 	
 	public String getColumnPattern(String property) { return null; };
 	
 	public Style getColumnStyle(String property) { return null; };
 	
 	public List<ConditionalStyle> getColumnConditionalStyle(String property) { return null; };
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private void getServices() {
 		try {
 			BundleContext bundleContext = FrameworkUtil.getBundle(TemplateComponentReport.class).getBundleContext();
 			
 			ServiceReference reportManagerServiceReference = bundleContext.getServiceReference(ReportManagerService.class.getName());
 			reportManagerService = ReportManagerService.class.cast(bundleContext.getService(reportManagerServiceReference));
 		}
 		catch (Exception e) {
 			e.getMessage();
 			
 		}
 		
 	}
 	
 	public static int mmToPoints(float f) {
 		return Math.round(f / 25.4f * 72); // 1in = 25.4mm = 72pt
 	}
 	
 	public void executeReport(String templateCode, Map<String,Object> parameters) throws Exception {
		this.jasperPrint = reportManagerService.executeReport(user templateCode, parameters);		
 		
 	}
 	
 	protected JRHtmlExporter getHtmlExporter() {
 		JRHtmlExporter exporter = new JRHtmlExporter();
 		String random = "" + Math.random() * 1000.0;
 		random = random.replace('.', '0').replace(',', '0');
 		
 		exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, getWebContextPath() + "/image?r=" + random + "&image=");
 		return exporter;
 	}
 	
 	/**
 	 * Web context path (e.g. "cis/") for the specified Application instance.
 	 * @param application
 	 * @return Web context path.
 	 */
 	public String getWebContextPath() {
 		WebApplicationContext context = (WebApplicationContext) application.getContext();
 		return context.getHttpSession().getServletContext().getContextPath();
 	}
 	
 	protected void build() {
 		try {
 			refreshButton.setComponentError(null);
 			setObservations("");
 			layout.setFirstComponent(htmlLayout);
 			htmlLabel.setValue(getOutputStream(getHtmlExporter()).toString("UTF-8"));
 			
 			if(!getObservations().isEmpty()) {
 				refreshButton.setComponentError(new UserError("Error showing report!"));
 			}
 			
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	protected ByteArrayOutputStream getOutputStream(JRExporter exporter) {
 		ByteArrayOutputStream outputStream = null;
 		
 		try {			
 			outputStream = new ByteArrayOutputStream();
 			
 			WebApplicationContext context = (WebApplicationContext) getApplication().getContext();
 			context.getHttpSession().setAttribute(ImageServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE, jasperPrint);
 			
 			exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
 			exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
 			
 			exporter.exportReport();
 			
 			outputStream.flush();
 			outputStream.close();
 			
 		} catch (JRException e) {
 			throw new RuntimeException(e);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 		
 		return outputStream;
 	}
 
 	public void initLayout() {
 		htmlLabel = new Label("", Label.CONTENT_XHTML);
 		htmlLabel.setStyleName(Reindeer.LAYOUT_WHITE);
 		htmlLabel.setSizeUndefined();
 		
 		htmlLayout.setMargin(true);
 		htmlLayout.setStyleName(Reindeer.LAYOUT_BLACK);
 		htmlLayout.addStyleName("report-background");
 		htmlLayout.addComponent(htmlLabel);
 		htmlLayout.setComponentAlignment(htmlLabel, Alignment.TOP_CENTER);
 		
 		refreshButton = new Button("Refresh");
 		pdfButton = new Button("Pdf");
 		excelButton = new Button("Excel");
 		wordButton = new Button("Word");
 		powerPointButton = new Button("PowerPoint");
 		odsButton = new Button("Odd rows hightlight");
 		odtButton = new Button("Show ");
 		rtfButton = new Button("RTF");
 		htmlButton = new Button("HTML");
 		csvButton = new Button("CVS");
 		xmlButton = new Button("Xml");
 		
 		pdfButton.setStyleName(BaseTheme.BUTTON_LINK);
 		excelButton.setStyleName(BaseTheme.BUTTON_LINK);
 		wordButton.setStyleName(BaseTheme.BUTTON_LINK);
 		powerPointButton.setStyleName(BaseTheme.BUTTON_LINK);
 		odsButton.setStyleName(BaseTheme.BUTTON_LINK);
 		odtButton.setStyleName(BaseTheme.BUTTON_LINK);
 		rtfButton.setStyleName(BaseTheme.BUTTON_LINK);
 		htmlButton.setStyleName(BaseTheme.BUTTON_LINK);
 		csvButton.setStyleName(BaseTheme.BUTTON_LINK);
 		xmlButton.setStyleName(BaseTheme.BUTTON_LINK);
 		
 		refreshButton.addListener(this);
 		pdfButton.addListener(this);
 		excelButton.addListener(this);
 		wordButton.addListener(this);
 		powerPointButton.addListener(this);
 		odsButton.addListener(this);
 		odtButton.addListener(this);
 		rtfButton.addListener(this);
 		htmlButton.addListener(this);
 		csvButton.addListener(this);
 		xmlButton.addListener(this);
 		
 		VerticalLayout exportOptionsLayout = new VerticalLayout();
 		exportOptionsLayout.setSizeUndefined();
 		exportOptionsLayout.setSpacing(true);
 		
 		exportOptionsLayout.addComponent(pdfButton);
 		exportOptionsLayout.addComponent(excelButton);
 		exportOptionsLayout.addComponent(wordButton);
 		exportOptionsLayout.addComponent(powerPointButton);
 		exportOptionsLayout.addComponent(odsButton);
 		exportOptionsLayout.addComponent(odtButton);
 		exportOptionsLayout.addComponent(rtfButton);
 		exportOptionsLayout.addComponent(htmlButton);
 		exportOptionsLayout.addComponent(csvButton);
 		exportOptionsLayout.addComponent(xmlButton);
 		
 		SplitButton exportButton = new SplitButton("PDF");
 		exportButton.setComponent(exportOptionsLayout);
 		exportButton.addClickListener(this);
 		
 		//String[] columnTitles = getColumnTitles();
 		//columnsCheckBoxes = new CheckBox[columnTitles.length];
 		//groupingCheckBoxes = new CheckBox[columnTitles.length];
 		
 		//VerticalLayout columnsLayout = new VerticalLayout();
 		//columnsLayout.setSizeUndefined();
 		//columnsLayout.setSpacing(true);
 		
 		//VerticalLayout groupingLayout = new VerticalLayout();
 		//groupingLayout.setSizeUndefined();
 		//groupingLayout.setSpacing(true);
 		
 		/*for(int i = 0; i < columnTitles.length; i++) {
 			CheckBox columnCheckBox = new CheckBox(columnTitles[i], true);
 			columnCheckBox.setValue(getDefalutColumnCheckBox(getColumnProperties()[i]));
 			columnsLayout.addComponent(columnCheckBox);
 			columnsCheckBoxes[i] = columnCheckBox;
 			
 			CheckBox groupingCheckBox = new CheckBox(columnTitles[i], false);
 			groupingLayout.addComponent(groupingCheckBox);
 			groupingCheckBoxes[i] = groupingCheckBox;
 		}*/
 		
 		//PopupButton columnsButton = new PopupButton("Columns");
 		//columnsButton.setComponent(columnsLayout);
 		
 		//PopupButton groupingButton = new PopupButton("Grouping");
 		//groupingButton.setComponent(groupingLayout);
 		
 		displayLayout = new HorizontalLayout();
 		displayLayout.setSpacing(true);
 		displayLayout.addComponent(refreshButton);
 		//displayLayout.addComponent(columnsButton);
 		//displayLayout.addComponent(groupingButton);
 		displayLayout.addComponent(exportButton);
 		
 		Panel exportPanel = new Panel();
 		exportPanel.addComponent(displayLayout);
 		
 		printBackgroundOnOddRowsCheckBox = new CheckBox("Odd rows hightlight", true);
 		printColumnNamesCheckBox = new CheckBox("Show columns tittles");
 		stretchWithOverflowCheckBox = new CheckBox("Column wight adjust");
 		printColumnNamesCheckBox.setValue(true);
 		columnsPerPageTextField = new TextField("Columns per page");
 		columnsPerPageTextField.setValue("1");
 		pageWidthTextField = new TextField("Page width (mm)");
 		pageWidthTextField.setValue(215.9f);
 		pageHeightTextField = new TextField("Page height (mm)");
 		pageHeightTextField.setValue(279.4f);
 		marginTopTextField = new TextField("Top margin");
 		marginTopTextField.setValue(15);
 		marginTopTextField.setWidth("115px");
 		marginBottomTextField = new TextField("Botton margin");
 		marginBottomTextField.setValue(15);
 		marginBottomTextField.setWidth("115px");
 		marginLeftTextField = new TextField("Left margin");
 		marginLeftTextField.setValue(15);
 		marginLeftTextField.setWidth("115px");
 		marginRightTextField = new TextField("Right margin");
 		marginRightTextField.setValue(15);
 		marginRightTextField.setWidth("115px");
 		
 		Button reverseButton = new Button("Reverse");
 		
 		reverseButton.addListener(new Button.ClickListener() {
 			private static final long serialVersionUID = 1L;
 			@Override
 			public void buttonClick(ClickEvent event) {
 				Object height = pageHeightTextField.getValue();
 				pageHeightTextField.setValue(pageWidthTextField.getValue());
 				pageWidthTextField.setValue(height);
 			}
 		});
 		
 		HorizontalLayout pageLayout = new HorizontalLayout();
 		pageLayout.setSpacing(true);
 		pageLayout.addComponent(pageWidthTextField);
 		pageLayout.addComponent(pageHeightTextField);
 		pageLayout.addComponent(reverseButton);
 		pageLayout.setComponentAlignment(reverseButton, Alignment.BOTTOM_LEFT);
 		
 		GridLayout marginLayout = new GridLayout(3, 3);
 		marginLayout.addComponent(marginTopTextField, 1, 0);
 		marginLayout.addComponent(marginBottomTextField, 1, 2);
 		marginLayout.addComponent(marginLeftTextField, 0, 1);
 		marginLayout.addComponent(marginRightTextField, 2, 1);
 		
 		Panel marginPanel = new Panel();
 		marginPanel.setSizeUndefined();
 		marginPanel.addComponent(marginLayout);
 		
 		VerticalLayout reportConfigurationLayout = new VerticalLayout();
 		reportConfigurationLayout.setWidth("100%");
 		reportConfigurationLayout.setMargin(true);
 		reportConfigurationLayout.setSpacing(true);
 		reportConfigurationLayout.addComponent(printBackgroundOnOddRowsCheckBox);
 		reportConfigurationLayout.addComponent(printColumnNamesCheckBox);
 		reportConfigurationLayout.addComponent(stretchWithOverflowCheckBox);
 		reportConfigurationLayout.addComponent(new Label());
 		reportConfigurationLayout.addComponent(columnsPerPageTextField);
 		reportConfigurationLayout.addComponent(new Label());
 		reportConfigurationLayout.addComponent(pageLayout);
 		reportConfigurationLayout.addComponent(new Label());
 		reportConfigurationLayout.addComponent(marginPanel);
 		
 		Component parametersComponent = getParametersComponent();
 		
 		VerticalLayout parametersLayout = new VerticalLayout();
 		parametersLayout.setMargin(true);
 		
 		if(parametersComponent != null) {
 			parametersLayout.addComponent(parametersComponent);
 		}
 		
 		observationsLabel = new Label("", Label.CONTENT_XHTML);
 		
 		observationsLayout = new VerticalLayout();
 		observationsLayout.setMargin(true);
 		observationsLayout.addComponent(observationsLabel);
 		
 		accordion = new Accordion();
 		accordion.setSizeFull();
 		accordion.addTab(parametersLayout, "Parameters", null);
 		accordion.addTab(reportConfigurationLayout, "Configuration", null);
 		accordion.addTab(observationsLayout, "Comment", null);
 		
 		VerticalLayout rightLayout = new VerticalLayout();
 		rightLayout.setSizeFull();
 		rightLayout.setMargin(true);
 		rightLayout.addComponent(exportPanel);
 		rightLayout.addComponent(accordion);
 		rightLayout.setExpandRatio(accordion, 1);
 		
 		layout = new HorizontalSplitPanel();
 		layout.setSplitPosition(65);
 		layout.setSizeFull();
 		layout.setSecondComponent(rightLayout);
 		
 		setCompositionRoot(layout);
 	}
 	
 	public void addObservation(String text) {
 		String currentText = observationsLabel.getValue().toString();
 		
 		if(currentText.isEmpty()) {
 			setObservations("- " + text);
 		} else {
 			setObservations(currentText + "<br/><br/>- " + text);
 		}
 	}
 	
 	public void setObservations(String text) {
 		observationsLabel.setValue(text);
 	}
 	
 	public String getObservations() {
 		return (String) observationsLabel.getValue();
 	}
 	
 	@Override
 	public void splitButtonClick(SplitButtonClickEvent event) {
 		exportToPdf();
 	}
 	
 	@Override
 	public void buttonClick(ClickEvent event) {
 		if(event.getButton().equals(refreshButton)) {
 			build();
 		} else if(event.getButton().equals(pdfButton)) {
 			exportToPdf();
 		} else if(event.getButton().equals(excelButton)) {
 			exportToExcel();
 		} else if(event.getButton().equals(wordButton)) {
 			exportToWord();
 		} else if(event.getButton().equals(powerPointButton)) {
 			exportToPowerPoint();
 		} else if(event.getButton().equals(odsButton)) {
 			exportToOds();
 		} else if(event.getButton().equals(odtButton)) {
 			exportToOdt();
 		} else if(event.getButton().equals(rtfButton)) {
 			exportToRtf();
 		} else if(event.getButton().equals(htmlButton)) {
 			exportToHtml();
 		} else if(event.getButton().equals(csvButton)) {
 			exportToCsv();
 		} else if(event.getButton().equals(xmlButton)) {
 			exportToXml();
 		}
 	}
 
 	public void exportToPdf() {
 		download(getFileName() + ".pdf", new JRPdfExporter());
 	}
 
 	public void exportToExcel() {
 		download(getFileName() + ".xlsx", new JRXlsxExporter());
 	}
 
 	public void exportToWord() {
 		download(getFileName() + ".docx", new JRDocxExporter());
 	}
 
 	public void exportToPowerPoint() {
 		download(getFileName() + ".pptx", new JRPptxExporter());
 	}
 
 	public void exportToOds() {
 		download(getFileName() + ".ods", new JROdsExporter());
 	}
 
 	public void exportToOdt() {
 		download(getFileName() + ".odf", new JROdtExporter());
 	}
 
 	public void exportToRtf() {
 		download(getFileName() + ".rtf", new JRRtfExporter());
 	}
 
 	public void exportToHtml() {
 		download(getFileName() + ".html", new JRXhtmlExporter());
 	}
 	
 	public void exportToCsv() {
 		download(getFileName() + ".csv", new JRCsvExporter());
 	}
 	
 	public void exportToXml() {
 		download(getFileName() + ".xml", new JRXmlExporter());
 	}
 	
 	protected void download(String filename, final JRExporter exporter) {
 		StreamResource resource = new StreamResource(new StreamResource.StreamSource() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public InputStream getStream() {
 				return new ByteArrayInputStream(getOutputStream(exporter).toByteArray());
 			}
 			
 		}, filename, getApplication());
 		
 		getApplication().getMainWindow().open(resource, "_blank");
 	}
 	
 }
