 package es.udc.cartolab.gvsig.fonsagua.forms.abastecimiento;
 
 import javax.swing.JTextField;
 
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 import com.iver.cit.gvsig.project.documents.table.gui.Table;
 
 import es.icarto.gvsig.navtableforms.BasicAbstractForm;
 import es.icarto.gvsig.navtableforms.gui.tables.AlphanumericRelNNTableHandler;
 import es.icarto.gvsig.navtableforms.gui.tables.AlphanumericTableLoader;
 import es.icarto.gvsig.navtableforms.gui.tables.TableHandler;
 import es.icarto.gvsig.navtableforms.gui.tables.VectorialTableHandler;
 import es.icarto.gvsig.navtableforms.utils.TOCLayerManager;
 import es.icarto.gvsig.navtableforms.utils.TOCTableManager;
 import es.udc.cartolab.gvsig.fonsagua.FonsaguaConstants;
 import es.udc.cartolab.gvsig.fonsagua.forms.comunidades.AdescosForm;
 import es.udc.cartolab.gvsig.fonsagua.forms.comunidades.ComunidadesForm;
 import es.udc.cartolab.gvsig.fonsagua.forms.comunidades.DatosConsumoForm;
 import es.udc.cartolab.gvsig.fonsagua.forms.comunidades.ImplicacionComunidadForm;
 import es.udc.cartolab.gvsig.fonsagua.forms.comunidades.ValoracionSistemaForm;
 import es.udc.cartolab.gvsig.fonsagua.forms.factories.FonsaguaTableFormFactory;
 import es.udc.cartolab.gvsig.fonsagua.forms.fuentes.FuentesForm;
 import es.udc.cartolab.gvsig.fonsagua.forms.relationship.TableRelationship;
 import es.udc.cartolab.gvsig.navtable.listeners.PositionEvent;
 
 @SuppressWarnings("serial")
 public class AbastecimientosForm extends BasicAbstractForm {
 
     public static final String NAME = "abastecimientos";
     public static final String PKFIELD = "cod_abastecimiento";
     public static String[] colNames = { "abastecimiento", "cod_abastecimiento" };
     public static String[] colAlias = { "Abastecimiento", "Cdigo" };
 
     private TableHandler juntasAguaHandler;
     private TableHandler coberturaHandler;
     private TableHandler gestionComercialHandler;
     private TableHandler gestionFinancieraHandler;
     private TableHandler evaluacionHandler;
     private TableHandler datosConsumoHandler;
     private VectorialTableHandler captacionesHandler;
     private VectorialTableHandler depIntermediosHandler;
     private VectorialTableHandler depDistribucionHandler;
     private VectorialTableHandler tuberiasHandler;
     private VectorialTableHandler bombeosHandler;
     private AlphanumericRelNNTableHandler adescosHandler;
     private AlphanumericRelNNTableHandler implicacionComunidadHandler;
     private AlphanumericRelNNTableHandler valoracionSistemaHandler;
     private TableRelationship comunidadesRelationship;
     private TableRelationship fuentesRelationship;
 
     public AbastecimientosForm(FLyrVect layer) {
 	super(layer);
 
 	juntasAguaHandler = new TableHandler(FonsaguaConstants.dataSchema,
 		JuntasAguaForm.NAME, getWidgetComponents(), PKFIELD,
 		JuntasAguaForm.colNames, JuntasAguaForm.colAlias);
 	coberturaHandler = new TableHandler(FonsaguaConstants.dataSchema,
 		CoberturaForm.NAME, getWidgetComponents(), PKFIELD,
 		CoberturaForm.colNames, CoberturaForm.colAlias);
 	gestionComercialHandler = new TableHandler(
 		FonsaguaConstants.dataSchema, GestComercialForm.NAME,
 		getWidgetComponents(), PKFIELD, GestComercialForm.colNames,
 		GestComercialForm.colAlias);
 	gestionFinancieraHandler = new TableHandler(
 		FonsaguaConstants.dataSchema, GestFinancieraForm.NAME,
 		getWidgetComponents(), PKFIELD, GestFinancieraForm.colNames,
 		GestFinancieraForm.colAlias);
 	evaluacionHandler = new TableHandler(FonsaguaConstants.dataSchema,
 		EvaluacionForm.NAME, getWidgetComponents(), PKFIELD,
 		EvaluacionForm.colNames, EvaluacionForm.colAlias);
 	datosConsumoHandler = new TableHandler(FonsaguaConstants.dataSchema,
 		DatosConsumoForm.NAME, getWidgetComponents(),
 		"cod_abastecimiento", DatosConsumoForm.colNames,
 		DatosConsumoForm.colAlias);
 	captacionesHandler = new VectorialTableHandler(CaptacionesForm.NAME,
 		getWidgetComponents(), PKFIELD, CaptacionesForm.colNames,
 		CaptacionesForm.colAlias);
 	depIntermediosHandler = new VectorialTableHandler(
 		DepIntermediosForm.NAME, getWidgetComponents(), PKFIELD,
 		DepIntermediosForm.colNames, DepIntermediosForm.colAlias);
 	depDistribucionHandler = new VectorialTableHandler(
 		DepDistribucionForm.NAME, getWidgetComponents(), PKFIELD,
 		DepDistribucionForm.colNames,
 		DepDistribucionForm.colAlias);
 	tuberiasHandler = new VectorialTableHandler(TuberiasForm.NAME,
 		getWidgetComponents(), PKFIELD, TuberiasForm.colNames,
 		TuberiasForm.colAlias);
 	bombeosHandler = new VectorialTableHandler(BombeosForm.NAME,
 		getWidgetComponents(), PKFIELD, BombeosForm.colNames,
 		BombeosForm.colAlias);
 	comunidadesRelationship = new TableRelationship(getWidgetComponents(),
 		NAME, PKFIELD, ComunidadesForm.NAME, ComunidadesForm.PKFIELD,
 		"r_abastecimientos_comunidades", FonsaguaConstants.dataSchema,
 		ComunidadesForm.colNames, ComunidadesForm.colAlias);
 	fuentesRelationship = new TableRelationship(getWidgetComponents(),
 		NAME, PKFIELD, FuentesForm.NAME, FuentesForm.PKNAME,
 		"r_abastecimientos_fuentes", FonsaguaConstants.dataSchema,
 		FuentesForm.colNames, FuentesForm.colAlias);
 	adescosHandler = new AlphanumericRelNNTableHandler(loadTable(
 		AdescosForm.NAME).getModel(), getWidgetComponents(),
 		FonsaguaConstants.dataSchema, "cod_abastecimiento",
 		"r_abastecimientos_comunidades", "cod_comunidad",
 		AdescosForm.colNames, AdescosForm.colAlias);
 	implicacionComunidadHandler = new AlphanumericRelNNTableHandler(
 		loadTable(ImplicacionComunidadForm.NAME).getModel(),
 		getWidgetComponents(), FonsaguaConstants.dataSchema,
 		"cod_abastecimiento", "r_abastecimientos_comunidades",
 		"cod_comunidad", ImplicacionComunidadForm.colNames,
 		ImplicacionComunidadForm.colAlias);
 	valoracionSistemaHandler = new AlphanumericRelNNTableHandler(loadTable(
 		ValoracionSistemaForm.NAME).getModel(), getWidgetComponents(),
 		FonsaguaConstants.dataSchema, "cod_abastecimiento",
 		"r_abastecimientos_comunidades", "cod_comunidad",
 		ValoracionSistemaForm.colNames, ValoracionSistemaForm.colAlias);
     }
 
     private Table loadTable(String name) {
 	TOCTableManager tocTable = new TOCTableManager();
 	Table table = tocTable.getTableByName(name);
 	try {
 	    if (table == null) {
 		AlphanumericTableLoader.loadTable(FonsaguaConstants.dataSchema,
 			name);
 		tocTable = new TOCTableManager();
 		table = tocTable.getTableByName(name);
 	    }
 	} catch (Exception e) {
 	    e.printStackTrace();
 	}
 	return table;
     }
 
     @Override
     protected void fillSpecificValues() {
 	String codAbastecimiento = getFormController().getValue(PKFIELD);
 	juntasAguaHandler.fillValues(codAbastecimiento);
 	coberturaHandler.fillValues(codAbastecimiento);
 	gestionComercialHandler.fillValues(codAbastecimiento);
 	gestionFinancieraHandler.fillValues(codAbastecimiento);
 	evaluacionHandler.fillValues(codAbastecimiento);
 	datosConsumoHandler.fillValues(codAbastecimiento);
 	captacionesHandler.fillValues(codAbastecimiento);
 	depIntermediosHandler.fillValues(codAbastecimiento);
 	depDistribucionHandler.fillValues(codAbastecimiento);
 	tuberiasHandler.fillValues(codAbastecimiento);
 	bombeosHandler.fillValues(codAbastecimiento);
 	final String pkValue = ((JTextField) getWidgetComponents().get(PKFIELD))
 		.getText();
 	comunidadesRelationship.fillValues(pkValue);
 	fuentesRelationship.fillValues(pkValue);
 	adescosHandler.fillValues(codAbastecimiento);
 	implicacionComunidadHandler.fillValues(codAbastecimiento);
 	valoracionSistemaHandler.fillValues(codAbastecimiento);
     }
 
     @Override
     protected void setListeners() {
 	super.setListeners();
 	juntasAguaHandler.reload(new JuntasAguaForm());
 	coberturaHandler.reload(new CoberturaForm());
 	gestionComercialHandler.reload(new GestComercialForm());
 	gestionFinancieraHandler.reload(new GestFinancieraForm());
 	evaluacionHandler.reload(new EvaluacionForm());
 	datosConsumoHandler.reload(new DatosConsumoForm());
 	captacionesHandler.reload(CaptacionesForm.NAME,
 		FonsaguaTableFormFactory.getInstance());
 	depIntermediosHandler.reload(DepIntermediosForm.NAME,
 		FonsaguaTableFormFactory.getInstance());
 	depDistribucionHandler.reload(DepDistribucionForm.NAME,
 		FonsaguaTableFormFactory.getInstance());
 	tuberiasHandler.reload(TuberiasForm.NAME,
 		FonsaguaTableFormFactory.getInstance());
 	bombeosHandler.reload(BombeosForm.NAME,
 		FonsaguaTableFormFactory.getInstance());
 	comunidadesRelationship.reload(FonsaguaTableFormFactory.getInstance()
 		.createSingletonForm(
 			new TOCLayerManager()
 				.getLayerByName(ComunidadesForm.NAME)));
 	fuentesRelationship
 		.reload(FonsaguaTableFormFactory.getInstance()
 			.createSingletonForm(
 			new TOCLayerManager().getLayerByName(FuentesForm.NAME)));
 	adescosHandler.reload(new AdescosForm());
     }
 
     @Override
     protected void removeListeners() {
 	super.removeListeners();
 	juntasAguaHandler.removeListeners();
 	coberturaHandler.removeListeners();
 	gestionComercialHandler.removeListeners();
 	gestionFinancieraHandler.removeListeners();
 	evaluacionHandler.removeListeners();
 	datosConsumoHandler.removeListeners();
 	captacionesHandler.removeListeners();
 	depIntermediosHandler.removeListeners();
 	depDistribucionHandler.removeListeners();
 	tuberiasHandler.removeListeners();
 	bombeosHandler.removeListeners();
 	comunidadesRelationship.removeListeners();
 	fuentesRelationship.removeListeners();
 	adescosHandler.removeListeners();
 	implicacionComunidadHandler.removeListeners();
 	valoracionSistemaHandler.removeListeners();
     }
 
     @Override
     public void onPositionChange(PositionEvent e) {
 	super.onPositionChange(e);
 	fillSpecificValues();
     }
 
     @Override
     protected String getBasicName() {
 	return NAME;
     }
 
 }
