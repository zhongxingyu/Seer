 package es.udc.cartolab.gvsig.fonsagua.prediseno.ui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.InputStream;
 import java.sql.SQLException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFormattedTextField;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 
 import net.miginfocom.swing.MigLayout;
 
 import com.iver.andami.PluginServices;
 import com.iver.andami.ui.mdiManager.SingletonWindow;
 import com.iver.andami.ui.mdiManager.WindowInfo;
 import com.jeta.forms.components.panel.FormPanel;
 import com.jeta.forms.gui.common.FormException;
 
 import es.icarto.gvsig.navtableforms.ormlite.domainvalidator.ValidatorComponent;
 import es.icarto.gvsig.navtableforms.ormlite.domainvalidator.ValidatorDomain;
 import es.icarto.gvsig.navtableforms.ormlite.domainvalidator.ValidatorForm;
 import es.icarto.gvsig.navtableforms.ormlite.domainvalidator.rules.DoublePositiveRule;
 import es.icarto.gvsig.navtableforms.ormlite.domainvalidator.rules.IntegerPositiveRule;
 import es.icarto.gvsig.navtableforms.ormlite.domainvalidator.rules.PercentageRule;
 import es.icarto.gvsig.navtableforms.ormlite.domainvalidator.rules.ValidationRule;
 import es.icarto.gvsig.navtableforms.utils.AbeilleParser;
 import es.udc.cartolab.gvsig.fonsagua.OpenAlternativeExtension;
 import es.udc.cartolab.gvsig.fonsagua.forms.alternativas.AlternativasForm;
 import es.udc.cartolab.gvsig.fonsagua.utils.AlternativesPreferences;
 import es.udc.cartolab.gvsig.fonsagua.utils.AlternativesPreferences.Bomba;
 import es.udc.cartolab.gvsig.fonsagua.utils.AlternativesPreferences.Tuberia;
 import es.udc.cartolab.gvsig.fonsagua.utils.FonsaguaConstants;
 import es.udc.cartolab.gvsig.fonsagua.utils.ImageUtils;
 import es.udc.cartolab.gvsig.users.utils.DBSession;
 
 @SuppressWarnings("serial")
 public class PredesignDialog extends JPanel implements ActionListener,
 	SingletonWindow, KeyListener, FocusListener, ItemListener {
 
     public static String NAME = "predisenho";
 
     /*
      * The next properties are the ones related to the interface itself, so they
      * don't need a real explanation
      */
 
     private WindowInfo windowInfo = null;
     private ValidatorForm validator;
     private Map<String, JComponent> widgets;
     private JButton closeButton;
     private AlternativesPreferences pref;
     private List<Bomba> bombas;
     private static boolean combosLoaded = false;
 
     /*
      * There is no need to read and parse the xml each time we press the button,
      * so we can keep an already built instance and show it as a singleton
      * window.
      */
     private static PredesignDialog instance = new PredesignDialog();
 
     public static void showDialog() {
 	if (!combosLoaded) {
 	    instance.loadCombos();
 	    combosLoaded = true;
 	}
 	PluginServices.getMDIManager().addWindow(instance);
     }
 
     private PredesignDialog() {
 	FormPanel formBody;
 	InputStream stream = getClass().getClassLoader().getResourceAsStream(
 		"ui/" + NAME + ".xml");
 	try {
 	    formBody = new FormPanel(stream);
 	    widgets = AbeilleParser.getWidgetsFromContainer(formBody);
 	    JScrollPane scrollPane = new JScrollPane(formBody);
 	    MigLayout thisLayout = new MigLayout("inset 0, align center",
 		    "[grow]", "[][grow][]");
 	    this.setLayout(thisLayout);
 	    this.add(scrollPane, "shrink, growx, growy, wrap");
 	    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
 	    closeButton = new JButton(PluginServices.getText(this, "close"));
 	    closeButton.setPreferredSize(new Dimension(80, 30));
 	    buttons.add(closeButton);
 	    this.add(buttons, "shrink, growx, h 40!, wrap");
 	    createValidator();
 	    setListeners();
 	    super.repaint();
 	    super.setVisible(true);
 	} catch (FormException e) {
 	    e.printStackTrace();
 	}
 
     }
 
     private void loadCombos() {
 	List<Tuberia> tuberias = AlternativesPreferences.getTuberias();
 	DefaultComboBoxModel model1 = new DefaultComboBoxModel();
 	model1.addElement(" ");
 	DefaultComboBoxModel model2 = new DefaultComboBoxModel();
 	model2.addElement(" ");
 	for (Tuberia tuberia : tuberias) {
 	    model1.addElement(new Item(tuberia.getId(), tuberia.getDiametro()));
 	    model2.addElement(new Item(tuberia.getId(), tuberia.getDiametro()));
 	}
 	((JComboBox) widgets.get("diametro_impulsion")).setModel(model1);
 	((JComboBox) widgets.get("diametro_impulsion_2")).setModel(model2);
     }
 
     private void createValidator() {
 	validator = new ValidatorForm();
 	Set<ValidationRule> vr = new HashSet<ValidationRule>();
 	vr.add(new PercentageRule());
 	ValidatorDomain vd = new ValidatorDomain(vr);
 	validator.addComponentValidator(new ValidatorComponent(widgets
 		.get("q_entra_gravedad"), vd));
 	validator.addComponentValidator(new ValidatorComponent(widgets
 		.get("q_entra_bombeo"), vd));
 	vr = new HashSet<ValidationRule>();
 	vr.add(new DoublePositiveRule());
 	vd = new ValidatorDomain(vr);
 	validator.addComponentValidator(new ValidatorComponent(widgets
 		.get("altura_bombeo"), vd));
 	validator.addComponentValidator(new ValidatorComponent(widgets
 		.get("longitud"), vd));
 	validator.addComponentValidator(new ValidatorComponent(widgets
 		.get("caudal"), vd));
 	validator.addComponentValidator(new ValidatorComponent(widgets
 		.get("tiempo_bombeo"), vd));
 	validator.addComponentValidator(new ValidatorComponent(widgets
 		.get("tiempo_bombeo_2"), vd));
 	vr = new HashSet<ValidationRule>();
 	vr.add(new IntegerPositiveRule());
 	vd = new ValidatorDomain(vr);
 	validator.addComponentValidator(new ValidatorComponent(widgets
 		.get("poblacion_disenho"), vd));
 	validator.addComponentValidator(new ValidatorComponent(widgets
 		.get("poblacion_sistema"), vd));
     }
 
     private void setListeners() {
 	closeButton.addActionListener(this);
 	for (String key : widgets.keySet()) {
 	    JComponent widget = widgets.get(key);
 	    if ((widget instanceof JTextField)
 		    || (widget instanceof JFormattedTextField)) {
 		widget.addKeyListener(this);
 	    }
 	    if (widget instanceof JComboBox) {
 		((JComboBox) widget).addItemListener(this);
 	    }
 	}
     }
 
     @Override
     public WindowInfo getWindowInfo() {
 	if (windowInfo == null) {
 	    windowInfo = new WindowInfo(WindowInfo.PALETTE
 		    | WindowInfo.RESIZABLE);
 	    windowInfo.setTitle(PluginServices.getText(this, "predesign"));
 	    windowInfo.setWidth(655);
 	    windowInfo.setHeight(645);
 	}
 	return windowInfo;
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
 	if (e.getSource().equals(closeButton)) {
 	    PluginServices.getMDIManager().closeWindow(this);
 	}
 
     }
 
     private void validateAndCompute() {
 	validator.validate();
 	if (!validator.hasValidationErrors()) {
 	    compute();
 	}
     }
 
     private void compute() {
 	pref = AlternativesPreferences.getInstance();
 	String altCode = OpenAlternativeExtension.getCode();
 
 	String pobl_disenho = ((JTextField) widgets.get("poblacion_disenho"))
 		.getText();
 
 	String[] fields = {AlternativasForm.TIPODISTRIBUCIONFIELD};
 	try {
 	    String[][] alts = DBSession.getCurrentSession().getTable(
 		    AlternativasForm.NAME,
 		    FonsaguaConstants.dataSchema,
 		    fields,
 		    "WHERE " + AlternativasForm.PKFIELD + " = '" + altCode
 			    + "'", new String[0], false);
 	    if ((alts.length > 0) && (alts[0].length > 0)) {
 		int dotacion;
 		if (alts[0][0].toLowerCase().contains("cantareras")) {
 		    dotacion = pref.getDotCantareras();
 		} else {
 		    dotacion = pref.getDotDomiciliar();
 		}
 		computeDemandaAbastece(pobl_disenho, dotacion,
 			"demanda_abastece");
 	    }
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 
 
 	String demanda_abastece = ((JTextField) widgets.get("demanda_abastece"))
 		.getText();
 	String q_ent_gravedad = ((JTextField) widgets.get("q_entra_gravedad"))
 		.getText();
 	String q_ent_bombeo = ((JTextField) widgets.get("q_entra_bombeo"))
 		.getText();
 
 	computeVolDeposito(demanda_abastece, q_ent_gravedad, q_ent_bombeo,
 		"vol_deposito");
 
 
 	String caudal = ((JTextField) widgets.get("caudal")).getText();
 	String t_bombeo1 = ((JTextField) widgets.get("tiempo_bombeo"))
 		.getText();
 	String t_bombeo2 = ((JTextField) widgets.get("tiempo_bombeo_2"))
 		.getText();
 
 	computeCaudalBombeo(caudal, t_bombeo1, "caudal_bombeo");
 	computeCaudalBombeo(caudal, t_bombeo2, "caudal_bombeo_2");
 
 
 	String caudal_bombeo1 = ((JTextField) widgets.get("caudal_bombeo"))
 		.getText();
 	String caudal_bombeo2 = ((JTextField) widgets.get("caudal_bombeo_2"))
 		.getText();
 	Object tuberia1 = ((JComboBox) widgets.get("diametro_impulsion"))
 		.getSelectedItem();
 	Object tuberia2 = ((JComboBox) widgets.get("diametro_impulsion_2"))
 		.getSelectedItem();
 
 	computeVelocidad(caudal_bombeo1, tuberia1, "velocidad");
 	computeVelocidad(caudal_bombeo2, tuberia2, "velocidad_2");
 
 
 	String longitud = ((JTextField) widgets.get("longitud")).getText();
 	String velocidad1 = ((JTextField) widgets.get("velocidad")).getText();
 	String velocidad2 = ((JTextField) widgets.get("velocidad_2")).getText();
 
 	computePerdidas(longitud, velocidad1, tuberia1, "perdidas");
 	computePerdidas(longitud, velocidad2, tuberia2, "perdidas_2");
 
 
 	String altura_bombeo = ((JTextField) widgets.get("altura_bombeo"))
 		.getText();
 	String perdidas1 = ((JTextField) widgets.get("perdidas")).getText();
 	String perdidas2 = ((JTextField) widgets.get("perdidas_2")).getText();
 
 	computePotencia(altura_bombeo, perdidas1, caudal_bombeo1,
 		"potencia_calculo");
 	computePotencia(altura_bombeo, perdidas2, caudal_bombeo2,
 		"potencia_calculo_2");
 
 
 	String potencia_calculo1 = ((JTextField) widgets
 		.get("potencia_calculo")).getText();
 	String potencia_calculo2 = ((JTextField) widgets
 		.get("potencia_calculo_2")).getText();
 	bombas = AlternativesPreferences.getBombas();
 	Bomba bomba1 = chooseBomba(potencia_calculo1, "potencia"), bomba2 = chooseBomba(
 		potencia_calculo2, "potencia_2");
 
 
 	Double potencia1 = null, potencia2 = null;
 	try {
 	    potencia1 = Double.parseDouble(((JTextField) widgets
 		    .get("potencia")).getText());
 	} catch (NumberFormatException e) {
 	}
 
 	try {
 	    potencia2 = Double.parseDouble(((JTextField) widgets
 		    .get("potencia_2")).getText());
 	} catch (NumberFormatException e) {
 	}
 
 	computeConsumo(potencia1, t_bombeo1, "consumo");
 	computeConsumo(potencia2, t_bombeo2, "consumo_2");
 
 
 	String poblacion_sistema = ((JTextField) widgets
 		.get("poblacion_sistema")).getText();
 	String consumo1 = ((JTextField) widgets.get("consumo")).getText();
 	String consumo2 = ((JTextField) widgets.get("consumo_2")).getText();
 	computeCuotaFutura(bomba1, consumo1, poblacion_sistema, "cuota_futura");
 	computeCuotaFutura(bomba2, consumo2, poblacion_sistema,
 		"cuota_futura_2");
 
 
 	computeVolDepBombeo(caudal, t_bombeo1, "vol_dep_bombeo");
 	computeVolDepBombeo(caudal, t_bombeo2, "vol_dep_bombeo_2");
     }
 
     private void computeDemandaAbastece(String pobl_disenho, int dotacion,
 	    String widgetName) {
 	if (pobl_disenho.length() > 0) {
 	    Double aux = pref.getfVarEstacional()
 		    * dotacion
 		    * Double.parseDouble(pobl_disenho)
 		    * (1 + (pref.getTasaCrecimiento()
 			    * pref.getAnhoHorizSistema() / 100)) / 86400;
 	    ((JTextField) widgets.get(widgetName)).setText(aux
 		    .toString());
 	} else {
 	    ((JTextField) widgets.get(widgetName)).setText("");
 	}
     }
 
     private void computeVolDeposito(String demanda_abastece,
 	    String q_ent_gravedad, String q_ent_bombeo, String widgetName) {
 	if ((demanda_abastece.length() > 0) && (q_ent_gravedad.length() > 0)
 		&& (q_ent_bombeo.length() > 0)) {
 	    Double aux = Double.parseDouble(demanda_abastece)
 		    * ((0.4 * Double.parseDouble(q_ent_gravedad)) + (0.8 * Double
 			    .parseDouble(q_ent_bombeo))) * 86400 / 1000;
 	    ((JTextField) widgets.get(widgetName)).setText(aux.toString());
 	} else {
 	    ((JTextField) widgets.get(widgetName)).setText("");
 	}
     }
 
     private void computeCaudalBombeo(String caudal, String t_bombeo,
 	    String widgetName) {
 	if ((caudal.length() > 0) && (t_bombeo.length() > 0)) {
 	    Double aux = Double.parseDouble(caudal) * 24
 		    / Double.parseDouble(t_bombeo);
 	    ((JTextField) widgets.get(widgetName)).setText(aux.toString());
 	} else {
 	    ((JTextField) widgets.get(widgetName)).setText("");
 	}
     }
 
     private void computeVelocidad(String caudal_bombeo, Object tuberia,
 	    String widgetName) {
 	if ((caudal_bombeo.length() > 0) && (tuberia instanceof Item)) {
 	    Double aux = Double.parseDouble(caudal_bombeo)
 		    / 1000
 		    / (Math.pow(
 			    (Math.PI * (((Item) tuberia)).getValue() / 2 / 1000),
 			    2));
 	    ((JTextField) widgets.get(widgetName)).setText(aux.toString());
 	} else {
 	    ((JTextField) widgets.get(widgetName)).setText("");
 	}
     }
 
     private void computePerdidas(String longitud, String velocidad,
 	    Object tuberia, String widgetName) {
 	if ((longitud.length() > 0) && (velocidad.length() > 0)
 		&& (tuberia instanceof Item)) {
 	    Double aux = 0.0025 * Double.parseDouble(longitud)
 		    * Math.pow(Double.parseDouble(velocidad), 2)
 		    / ((((Item) tuberia)).getValue() / 1000) / 2 / 9.80665;
 	    ((JTextField) widgets.get(widgetName)).setText(aux.toString());
 	} else {
 	    ((JTextField) widgets.get(widgetName)).setText("");
 	}
     }
 
     private void computePotencia(String altura_bombeo, String perdidas,
 	    String caudal_bombeo, String widgetName) {
 	if ((altura_bombeo.length() > 0) && (perdidas.length() > 0)
 		&& (caudal_bombeo.length() > 0)) {
 	    Double aux = 1000
 		    * (Double.parseDouble(caudal_bombeo) / 1000)
 		    * (Double.parseDouble(altura_bombeo) + Double
 			    .parseDouble(perdidas)) * 0.00136
 		    / pref.getRendimientoBomba();
 	    ((JTextField) widgets.get(widgetName)).setText(aux.toString());
 	} else {
 	    ((JTextField) widgets.get(widgetName)).setText("");
 	}
     }
 
     private Bomba chooseBomba(String potencia_calculo, String widgetName) {
 	Bomba bombaElegida = null;
 	if (potencia_calculo.length() > 0) {
 	    double aux = Double.parseDouble(potencia_calculo);
 	    for (Bomba bomba : bombas) {
 		double pot_bomba = bomba.getPotencia();
 		if ((pot_bomba > aux)
 			&& ((bombaElegida == null) || (pot_bomba < bombaElegida
 				.getPotencia()))) {
 		    bombaElegida = bomba;
 		}
 	    }
 	    if (bombaElegida != null) {
 		((JTextField) widgets.get(widgetName)).setText(new Double(
 			bombaElegida.getPotencia()).toString());
 		((JTextField) widgets.get(widgetName))
 			.setBackground(ImageUtils.NOT_ENABLED_COLOR);
 	    } else {
 		((JTextField) widgets.get(widgetName)).setText(PluginServices
 			.getText(this, "no_valid_bombs"));
 		((JTextField) widgets.get(widgetName))
 			.setBackground(Color.PINK);
 	    }
 	} else {
 	    ((JTextField) widgets.get(widgetName)).setText("");
 	    ((JTextField) widgets.get(widgetName))
 		    .setBackground(ImageUtils.NOT_ENABLED_COLOR);
 	}
 	return bombaElegida;
     }
 
     private void computeConsumo(Double potencia, String t_bombeo,
 	    String widgetName) {
 	if ((t_bombeo.length() > 0) && (potencia != null)) {
 	    Double aux = potencia * 0.736 * 30
 		    * Double.parseDouble(t_bombeo);
 	    ((JTextField) widgets.get(widgetName)).setText(aux.toString());
 	} else {
 	    ((JTextField) widgets.get(widgetName)).setText("");
 	}
     }
 
     private void computeCuotaFutura(Bomba bomba, String consumo,
 	    String poblacion_sistema, String widgetName) {
 	if ((bomba != null) && (consumo.length() > 0)
 		&& (poblacion_sistema.length() > 0)) {
 	    Double aux = (bomba.getPrecio() / pref.getAnhoHorizBomba() / 12 / Double
 		    .parseDouble(poblacion_sistema))
 		    + (pref.getPvpKwh() * Double.parseDouble(consumo) / Double
 			    .parseDouble(poblacion_sistema));
 	    ((JTextField) widgets.get(widgetName)).setText(aux.toString());
 	} else {
 	    ((JTextField) widgets.get(widgetName)).setText("");
 	}
     }
 
     private void computeVolDepBombeo(String caudal, String t_bombeo,
 	    String widgetName) {
 	if ((caudal.length() > 0) && (t_bombeo.length() > 0)) {
 	    double caudal_s_double = Double.parseDouble(caudal);
 	    Double aux = ((pref.getfVarEstacional() - 1) * caudal_s_double
 		    * 86400 / 1000)
 		    + (caudal_s_double * 86400 / 1000)
 		    - (Double.parseDouble(t_bombeo) * caudal_s_double * 3600 / 1000);
 	    ((JTextField) widgets.get(widgetName)).setText(aux.toString());
 	} else {
 	    ((JTextField) widgets.get(widgetName)).setText("");
 	}
     }
 
     @Override
     public Object getWindowProfile() {
 	return null;
     }
 
     @Override
     public Object getWindowModel() {
 	return NAME;
     }
 
     @Override
     public void keyTyped(KeyEvent e) {
     }
 
     @Override
     public void keyPressed(KeyEvent e) {
     }
 
     @Override
     public void keyReleased(KeyEvent e) {
 	validateAndCompute();
     }
 
     @Override
     public void focusGained(FocusEvent e) {
     }
 
     @Override
     public void focusLost(FocusEvent e) {
 	validateAndCompute();
     }
 
     @Override
     public void itemStateChanged(ItemEvent e) {
 	if (e.getStateChange() == ItemEvent.SELECTED) {
 	    validateAndCompute();
 	}
     }
 
     private class Item {
 	private double value;
 	private String description;
 
 	public Item(String description, double value) {
 	    this.value = value;
 	    this.description = description;
 	}
 
 	@Override
 	public String toString() {
 	    return description;
 	}
 
 	public double getValue() {
 	    return value;
 	}
     }
 
 }
