 /*<Dynamic Refactoring Plugin For Eclipse 2.0 - Plugin that allows to perform refactorings 
 on Java code within Eclipse, as well as to dynamically create and manage new refactorings>
 
 Copyright (C) 2009  Laura Fuente De La Fuente
 
 This file is part of Foobar
 
 Foobar is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.*/
 
 package dynamicrefactoring.interfaz.wizard;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.queryParser.ParseException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.forms.events.ExpansionAdapter;
 import org.eclipse.ui.forms.events.ExpansionEvent;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.eclipse.ui.forms.widgets.TableWrapData;
 import org.eclipse.ui.forms.widgets.TableWrapLayout;
 
 import com.google.common.base.Throwables;
 
 import dynamicrefactoring.RefactoringConstants;
 import dynamicrefactoring.RefactoringImages;
 import dynamicrefactoring.RefactoringPlugin;
 import dynamicrefactoring.domain.DynamicRefactoringDefinition;
 import dynamicrefactoring.domain.InputParameter;
 import dynamicrefactoring.domain.xml.XMLRefactoringsCatalog;
 import dynamicrefactoring.interfaz.dynamic.InputProcessor;
 import dynamicrefactoring.interfaz.wizard.listener.ListDownListener;
 import dynamicrefactoring.interfaz.wizard.listener.ListUpListener;
 import dynamicrefactoring.interfaz.wizard.search.internal.QueryResult;
 import dynamicrefactoring.interfaz.wizard.search.internal.SearchingFacade;
 import dynamicrefactoring.interfaz.wizard.search.internal.SearchingFacade.SearchableType;
 import dynamicrefactoring.interfaz.wizard.search.javadoc.EclipseBasedJavadocReader;
 import dynamicrefactoring.util.MOONTypeLister;
 
 /**
  * Segunda p�gina del asistente de creaci�n o edici�n de refactorizaciones.
  * 
  * <p>
  * Permite definir las entradas de la refactorizacion, asociarles un tipo y un
  * nombre, determinar a partir de qu� otra entrada y mediante la llamada a qu�
  * m�todo se puede obtener su valor, as� como establecer cu�l de todas
  * constituir� la entrada principal a la refactorizaci�n.
  * </p>
  * 
  * @author <A HREF="mailto:lfd0002@alu.ubu.es">Laura Fuente de la Fuente</A>
  * @author <A HREF="mailto:sfd0009@alu.ubu.es">Sonia Fuente de la Fuente</A>
  * @author <A HREF="mailto:ehp0001@alu.ubu.es">Enrique Herrero Paredes</A>
  */
 public class RefactoringWizardPage2 extends WizardPage {
 
 	/**
 	 * Elemento de registro de errores y otros eventos de la clase.
 	 */
 	private static final Logger logger = Logger
 			.getLogger(RefactoringWizardPage2.class);
 
 	/**
 	 * Bot�n que permite a�adir una nueva entrada.
 	 */
 	private Button addButton;
 
 	/**
 	 * Bot�n que permite eliminar entradas previamente a�adidas.
 	 */
 	private Button delButton;
 
 	/**
 	 * Bot�n que permite desplazar una entrada de la refactorizaci�n hacia
 	 * abajo.
 	 */
 	private Button downButton;
 
 	/**
 	 * Bot�n que permite desplazar una entrada de la refactorizaci�n hacia
 	 * abajo.
 	 */
 	private Button upButton;
 
 	/**
 	 * Refactorizaci�n configurada a trav�s del asistente y que debe ser creada
 	 * finalmente (si se trata de una nueva refactorizaci�n) o modificada (si se
 	 * est� editando una ya existente).
 	 */
 	private DynamicRefactoringDefinition refactoring = null;
 
 	/**
 	 * Lista desplegable con los nombres de los m�todos del objeto seleccionado
 	 * en {@link #cFrom} que permiten obtener iteradores o colecciones.
 	 */
 	private Combo cMethod;
 
 	/**
 	 * Lista desplegable con los identificadores de par�metros disponibles ya en
 	 * la lista de elegidos.
 	 */
 	private Combo cFrom;
 
 	/**
 	 * Campo de texto en que se muestra el nombre del par�metro de entrada
 	 * seleccionado sobre la lista de entradas.
 	 */
 	private Text tName;
 
 	/**
 	 * Marca de selecci�n que permite indicar si una entrada es la entrada
 	 * principal de la refactorizaci�n o no.
 	 */
 	private Button ch_Root;
 
 	/**
 	 * Lista de entradas elegidas para la refactorizaci�n.
 	 */
 	private List lInputs;
 
 	/**
 	 * Lista de tipos disponibles como tipos de los par�metros de entrada.
 	 */
 	private List lTypes;
 
 	/**
 	 * Lista de posibles tipos del modelo.
 	 * 
 	 * <p>
 	 * Se utiliza como clave el nombre completamente cualificado del tipo, y
 	 * como valor, el n�mero de entradas que tienen el tipo seleccionado, menos
 	 * 1.
 	 * </p>
 	 */
 	private Hashtable<String, Integer> listModelTypes;
 
 	/**
 	 * Tipos disponibles con su descripción asociada.
 	 * 
 	 * <p>
 	 * Se utiliza como clave el nombre completamente cualificado del tipo y
 	 * como valor la descripción correspondiente a este tipo, obtenida a partir
 	 * de la descripción asociada en la documentación del código fuente, javadoc.
 	 * </p>
 	 */
 	private HashMap<String, String> descriptionTypes;
 	
 	/**
 	 * Tabla de par�metros de entrada ya introducidos.
 	 * 
 	 * <p>
 	 * Se utiliza como clave el nombre completamente cualificado del tipo de
 	 * cada entrada concatenado con un espacio en blanco y el n�mero de entrada
 	 * que utiliza ese tipo, y como valor, un objeto de tipo
 	 * <code>InputParameter
 	 * </code> con toda la informaci�n asociada a la entrada.
 	 * </p>
 	 */
 	private Hashtable<String, InputParameter> inputsTable;
 	
 	/**
 	 * Navegador en el que se muestra informaci�n relativa al elemento
 	 * seleccionado dentro de la lista de tipos con el fin de ayudar al usuario
 	 * a la compresi�n de la interfaz.
 	 */
 	private Browser navegador;
 
 	/**
 	 * Caja de texto que permite introducir al usuario el patr�n de la b�squeda.
 	 */
 	private Text tSearch;
 
 	/**
 	 * Bot�n que permite activar un proceso de b�squeda al usuario.
 	 */
 	private Button bSearch;
 
 	/**
 	 * Bot�n por defecto de esta p�gina del wizard.
 	 */
 	private Button bDefault;
 
 	private FormToolkit toolkit;
 	private ScrolledForm form;
 	private Label descriptionFormLabel;
 	private ExpandableComposite refExpandableComp;
 	private ExpandableComposite refMainExpandableComp;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param refactoring
 	 *            refactorizaci�n que se est� editando o <code>null
 	 * </code> si se est� creando una nueva.
 	 */
 	public RefactoringWizardPage2(DynamicRefactoringDefinition refactoring) {
 		super("Wizard page"); //$NON-NLS-1$
 		setPageComplete(false);
 		setDescription(Messages.RefactoringWizardPage2_DescriptionInput);
 
 		this.refactoring = refactoring;
 	}
 
 	/**
 	 * Hace visible o invisible la p�gina del asistente.
 	 * 
 	 * @param visible
 	 *            si la p�gina se debe hacer visible o no.
 	 */
 	@Override
 	public void setVisible(boolean visible) {
 		if (visible) {
 			Object[] messageArgs = { ((RefactoringWizard) getWizard())
 					.getOperationAsString() };
 			MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
 			formatter
 					.applyPattern(Messages.RefactoringWizardPage2_DynamicRefactoring);
 
 			setTitle(formatter.format(messageArgs) + " (" + //$NON-NLS-1$
 					Messages.RefactoringWizardPage2_Step + ")"); //$NON-NLS-1$
 		}
 		super.setVisible(visible);
 	}
 
 	/**
 	 * Crea el contenido de la p�gina del asistente.
 	 * 
 	 * @param parent
 	 *            el elemento padre de esta p�gina del asistente.
 	 */
 	@Override
 	public void createControl(Composite parent) {
 
 		Composite container = new Composite(parent, SWT.NONE);
 		container.setLayout(new FormLayout());
 
 		setControl(container);
 
 		// sash_form
 		SashForm sash_form = new SashForm(container, SWT.VERTICAL | SWT.NULL);
 		sash_form.setLayout(new FormLayout());
 		final FormData sashFormData = new FormData();
 		sashFormData.top = new FormAttachment(0, 5);
 		sashFormData.left = new FormAttachment(0, 5);
 		sashFormData.right = new FormAttachment(100, -5);
 		sashFormData.bottom = new FormAttachment(100, -5);
 		sash_form.setLayoutData(sashFormData);
 		sash_form.setSashWidth(2);
 		sash_form.setBackground(parent.getDisplay().getSystemColor(
 				SWT.COLOR_DARK_GRAY));
 
 		// sash_form Top: composite
 		final Composite composite = new Composite(sash_form, SWT.NONE);
 		composite.setLayout(new FormLayout());
 
 		// sash_form Bottom: compBrowser
 		final Composite compBrowser = new Composite(sash_form, SWT.NONE);
 		compBrowser.setLayout(new FormLayout());
 
 		final Composite composite1 = new Composite(composite, SWT.NONE);
 		final FormData fdComposite1 = new FormData();
 		fdComposite1.right = new FormAttachment(0, 245);
 		fdComposite1.top = new FormAttachment(0, 5);
 		fdComposite1.left = new FormAttachment(0, 5);
 		composite1.setLayoutData(fdComposite1);
 		composite1.setLayout(new FormLayout());
 
 		Label search = new Label(composite1, SWT.NONE);
 		final FormData fdSearch = new FormData();
 		fdSearch.bottom = new FormAttachment(0, 58);
 		fdSearch.top = new FormAttachment(0, 29);
 		fdSearch.left = new FormAttachment(0, 15);
 		search.setLayoutData(fdSearch);
 		search.setText(Messages.RefactoringWizardPage2_Search);
 
 		tSearch = new Text(composite1, SWT.BORDER);
 		final FormData fdTextSearch = new FormData();
 		fdTextSearch.right = new FormAttachment(0, 185);
 		fdTextSearch.top = new FormAttachment(0, 28);
 		fdTextSearch.left = new FormAttachment(search, 0, SWT.RIGHT);
 		tSearch.setLayoutData(fdTextSearch);
 		tSearch.addFocusListener(new FocusListener() {
 			public void focusGained(FocusEvent e) {
 				bDefault = e.display.getShells()[0].getDefaultButton();
 				e.display.getShells()[0].setDefaultButton(bSearch);
 			}
 
 			public void focusLost(FocusEvent e) {
 				e.display.getShells()[0].setDefaultButton(bDefault);
 			}
 		});
 
 		bSearch = new Button(composite1, SWT.PUSH);
 		final FormData fdButtonSearch = new FormData();
 		fdButtonSearch.right = new FormAttachment(0, 225);
 		fdButtonSearch.top = new FormAttachment(0, 26);// 28
 		fdButtonSearch.left = new FormAttachment(tSearch, 3, SWT.RIGHT);
 		bSearch.setLayoutData(fdButtonSearch);
 		bSearch.setImage(RefactoringImages.getSearchIcon());
 		bSearch.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				fillSearchTypesList(tSearch.getText());
 			}
 		});
 
 		lTypes = new List(composite1, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL
 				| SWT.BORDER);
 		lTypes.setData("org.eclipse.swtbot.widget.key", "types");
 		
 		final FormData fdList = new FormData();
 		fdList.bottom = new FormAttachment(0, 247);
 		fdList.right = new FormAttachment(0, 225);
 		fdList.top = new FormAttachment(0, 59);// 28
 		fdList.left = new FormAttachment(0, 10);
 		lTypes.setLayoutData(fdList);
 		lTypes.addSelectionListener(new TypeSelectionListener());
 		lTypes.setToolTipText(Messages.RefactoringWizardPage2_SelectTypes);
 
 		navegador = new Browser(compBrowser, SWT.BORDER);
 		final FormData fd_navegador = new FormData();
 		fd_navegador.top = new FormAttachment(0, 10);
 		fd_navegador.left = new FormAttachment(0, 10);
 		fd_navegador.right = new FormAttachment(100, -10);
 		fd_navegador.bottom = new FormAttachment(100, 0);
 		navegador.setLayoutData(fd_navegador);
 		try {
 			navegador.setUrl(FileLocator.toFileURL(
 					RefactoringPlugin
 							.getDefault()
 							.getBundle()
 							.getEntry(
 									RefactoringConstants.REFACTORING_JAVADOC
 											+ "/moon/overview-summary.html"))
 					.toString());
 		} catch (IOException e1) {
 			throw Throwables.propagate(e1);
 		}
 
 		final Group typesGroup = new Group(composite1, SWT.NONE);
 		typesGroup.setText(Messages.RefactoringWizardPage2_Types);
 		final FormData fd_typwsGroup = new FormData();
 		fd_typwsGroup.bottom = new FormAttachment(100, -5);
 		fd_typwsGroup.top = new FormAttachment(0, 7);
 		fd_typwsGroup.left = new FormAttachment(0, 5);
 		fd_typwsGroup.right = new FormAttachment(100, -5);
 		typesGroup.setLayoutData(fd_typwsGroup);
 		typesGroup.setLayout(new FormLayout());
 
 		Composite composite_2;
 		composite_2 = new Composite(composite, SWT.NONE);
 		fdComposite1.bottom = new FormAttachment(composite_2, 0, SWT.BOTTOM);
 		composite_2.setLayout(new FormLayout());
 		final FormData fdComposite2 = new FormData();
 		fdComposite2.bottom = new FormAttachment(60, -5);
 		fdComposite2.right = new FormAttachment(100, -5);
 		fdComposite2.top = new FormAttachment(0, 1);
 		fdComposite2.left = new FormAttachment(0, 300);
 		composite_2.setLayoutData(fdComposite2);
 
 		lInputs = new List(composite_2, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL
 				| SWT.BORDER);
 		final FormData fdList1 = new FormData();
 		fdList1.right = new FormAttachment(100, -83);
 		fdList1.left = new FormAttachment(0, 15);
 		fdList1.top = new FormAttachment(0, 30);
 		lInputs.setData("org.eclipse.swtbot.widget.key", "inputs");
 		lInputs.setLayoutData(fdList1);
 		lInputs.addSelectionListener(new InputSelectionListener());
 
 		Group parametersGroup;
 		parametersGroup = new Group(composite_2, SWT.NONE);
 		parametersGroup.setLayout(new FormLayout());
 		parametersGroup.setText(Messages.RefactoringWizardPage2_Parameters);
 		final FormData fd_parametersGroup = new FormData();
 		fd_parametersGroup.bottom = new FormAttachment(100, -5);
 		fd_parametersGroup.top = new FormAttachment(lInputs, 5, SWT.BOTTOM);
 		fd_parametersGroup.left = new FormAttachment(0, 10);
 		parametersGroup.setLayoutData(fd_parametersGroup);
 
 		Group inputsGroup;
 		inputsGroup = new Group(composite_2, SWT.NONE);
 		fd_parametersGroup.right = new FormAttachment(inputsGroup, 0, SWT.RIGHT);
 
 		Label nameLabel = new Label(parametersGroup, SWT.NONE);
 		final FormData fdNameLabel = new FormData();
 		fdNameLabel.bottom = new FormAttachment(0, 30);
 		fdNameLabel.top = new FormAttachment(0, 10);
 		nameLabel.setLayoutData(fdNameLabel);
 		nameLabel.setText(Messages.RefactoringWizardPage2_Name);
 
 		tName = new Text(parametersGroup, SWT.BORDER);
 		fdNameLabel.left = new FormAttachment(tName, 0, SWT.LEFT);
 		final FormData fd_tName = new FormData();
 		fd_tName.top = new FormAttachment(0, 30);
 		tName.setLayoutData(fd_tName);
 		tName.addFocusListener(new NameFocusListener());
 		tName.setToolTipText(Messages.RefactoringWizardPage2_FillInName);
 
 		Label fromLabel;
 		fromLabel = new Label(parametersGroup, SWT.NONE);
 		fdNameLabel.right = new FormAttachment(fromLabel, 0, SWT.RIGHT);
 		fd_tName.left = new FormAttachment(fromLabel, 0, SWT.LEFT);
 		fd_tName.bottom = new FormAttachment(fromLabel, -5, SWT.TOP);
 		final FormData fdFromLabel = new FormData();
 		fdFromLabel.right = new FormAttachment(0, 145);
 		fromLabel.setLayoutData(fdFromLabel);
 		fromLabel.setText(Messages.RefactoringWizardPage2_From);
 
 		cFrom = new Combo(parametersGroup, SWT.NONE);
 		fdFromLabel.top = new FormAttachment(cFrom, -18, SWT.TOP);
 		fdFromLabel.bottom = new FormAttachment(cFrom, -5, SWT.TOP);
 		fdFromLabel.left = new FormAttachment(cFrom, 0, SWT.LEFT);
 		final FormData fdCFrom = new FormData();
 		cFrom.setLayoutData(fdCFrom);
 		cFrom.addFocusListener(new FromFocusListener());
 		cFrom.setToolTipText(Messages.RefactoringWizardPage2_SelectFromInput);
 
 		Label methodLabel = new Label(parametersGroup, SWT.NONE);
 		fdCFrom.top = new FormAttachment(methodLabel, -26, SWT.TOP);
 		fdCFrom.bottom = new FormAttachment(methodLabel, -5, SWT.TOP);
 		fdCFrom.right = new FormAttachment(methodLabel, 207, SWT.LEFT);
 		fdCFrom.left = new FormAttachment(methodLabel, 0, SWT.LEFT);
 		final FormData fdMethodLabel = new FormData();
 		fdMethodLabel.right = new FormAttachment(0, 150);
 		fdMethodLabel.bottom = new FormAttachment(0, 113);
 		fdMethodLabel.top = new FormAttachment(0, 100);
 		methodLabel.setLayoutData(fdMethodLabel);
 		methodLabel.setText(Messages.RefactoringWizardPage2_Method);
 
 		cMethod = new Combo(parametersGroup, SWT.NONE);
 		fdMethodLabel.left = new FormAttachment(cMethod, 0, SWT.LEFT);
 		final FormData fdCMethod = new FormData();
 		fdCMethod.bottom = new FormAttachment(0, 136);
 		fdCMethod.top = new FormAttachment(0, 115);
 		fdCMethod.right = new FormAttachment(methodLabel, 207, SWT.LEFT);
 		fdCMethod.left = new FormAttachment(methodLabel, 0, SWT.LEFT);
 		cMethod.setLayoutData(fdCMethod);
 		cMethod.addFocusListener(new MethodFocusListener());
 		cMethod.setToolTipText(Messages.RefactoringWizardPage2_SelectMethod);
 
 		ch_Root = new Button(parametersGroup, SWT.CHECK);
 		fd_tName.right = new FormAttachment(ch_Root, -5, SWT.LEFT);
 		final FormData fdChRoot = new FormData();
 		fdChRoot.left = new FormAttachment(cFrom, -44, SWT.RIGHT);
 		fdChRoot.right = new FormAttachment(cFrom, 0, SWT.RIGHT);
 		fdChRoot.bottom = new FormAttachment(tName, 21, SWT.TOP);
 		fdChRoot.top = new FormAttachment(tName, 0, SWT.TOP);
 		ch_Root.setLayoutData(fdChRoot);
 		ch_Root.setText(Messages.RefactoringWizardPage2_Main);
 		ch_Root.addSelectionListener(new RootSelectionListener());
 		ch_Root.setToolTipText(Messages.RefactoringWizardPage2_SelectMainBox);
 
 		inputsGroup.setText(Messages.RefactoringWizardPage2_Inputs);
 		final FormData fdInputsGroup = new FormData();
 		fdInputsGroup.bottom = new FormAttachment(parametersGroup, 0, SWT.TOP);
 		fdInputsGroup.right = new FormAttachment(100, -75);
 		fdInputsGroup.left = new FormAttachment(0, 10);
 		fdInputsGroup.top = new FormAttachment(0, 10);
 		inputsGroup.setLayoutData(fdInputsGroup);
 		inputsGroup.setLayout(new FormLayout());
 
 		upButton = new Button(composite_2, SWT.NONE);
 		final FormData fdButton2 = new FormData();
 		fdButton2.bottom = new FormAttachment(0, 58);
 		fdButton2.top = new FormAttachment(0, 35);
 		fdButton2.right = new FormAttachment(parametersGroup, 50, SWT.RIGHT);
 		upButton.setLayoutData(fdButton2);
 		upButton.setImage(RefactoringImages.getArrowUpIcon());
 		upButton.addSelectionListener(new ListUpListener(lInputs));
 
 		downButton = new Button(composite_2, SWT.NONE);
 		fdList1.bottom = new FormAttachment(downButton, 0, SWT.BOTTOM);
 		fdButton2.right = new FormAttachment(downButton, 0, SWT.RIGHT);
 		final FormData fd_vButton = new FormData();
 		fd_vButton.bottom = new FormAttachment(0, 100);
 		fd_vButton.top = new FormAttachment(0, 77);
 		fd_vButton.right = new FormAttachment(parametersGroup, 50, SWT.RIGHT);
 		downButton.setLayoutData(fd_vButton);
 		downButton.setImage(RefactoringImages.getArrowDownIcon());
 		downButton.addSelectionListener(new ListDownListener(lInputs));
 
 		delButton = new Button(composite, SWT.NONE);
 		final FormData fdButton = new FormData();
 		fdButton.right = new FormAttachment(0, 290);
 		fdButton.left = new FormAttachment(0, 250);
 		fdButton.bottom = new FormAttachment(0, 102);// 138
 		fdButton.top = new FormAttachment(0, 79);// 115
 		delButton.setLayoutData(fdButton);
 		delButton.setImage(PlatformUI.getWorkbench().getSharedImages()
 				.getImage(ISharedImages.IMG_TOOL_DELETE));
 
 		addButton = new Button(composite, SWT.NONE);
 		addButton.setData("org.eclipse.swtbot.widget.key","addInput");
 		final FormData fdButton1 = new FormData();
 		fdButton1.bottom = new FormAttachment(0, 61);
 		fdButton1.top = new FormAttachment(0, 40);
 		fdButton1.right = new FormAttachment(composite1, 45, SWT.RIGHT);
 		fdButton1.left = new FormAttachment(composite1, 5, SWT.RIGHT);
 		addButton.setLayoutData(fdButton1);
 		addButton.setImage(RefactoringImages.getArrowRightIcon());
 
 		addButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				addElements();
 			}
 		});
 
 		delButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				removeElements();
 			}
 		});
 
 		Composite composite_3;
 		composite_3 = new Composite(composite, SWT.BORDER);
 		composite_3.setLayout(new FormLayout());
 		final FormData fdComposite3 = new FormData();
 		fdComposite3.top = new FormAttachment(composite_2, 10);
 		fdComposite3.left = new FormAttachment(0, 10);
 		fdComposite3.right = new FormAttachment(100, -10);
 		fdComposite3.bottom = new FormAttachment(100, -10);
 		composite_3.setLayoutData(fdComposite3);
 		composite_3.setVisible(false);
 
 		// form
 		toolkit = new FormToolkit(composite_3.getDisplay());
 		form = toolkit.createScrolledForm(composite_3);
 		final FormData scrolledFormData = new FormData();
 		scrolledFormData.top = new FormAttachment(0, 0);
 		scrolledFormData.left = new FormAttachment(0, 0);
 		scrolledFormData.right = new FormAttachment(100, 0);
 		scrolledFormData.bottom = new FormAttachment(100, 0);
 		form.setLayoutData(scrolledFormData);
 		form.getBody().setLayout(new TableWrapLayout());
 
 		// formLabel
 		descriptionFormLabel = toolkit
 				.createLabel(form.getBody(), "", SWT.WRAP);
 
 		// refExpandableComp
 		refExpandableComp = toolkit.createExpandableComposite(form.getBody(),
 				ExpandableComposite.TREE_NODE
 						| ExpandableComposite.CLIENT_INDENT);
 		refExpandableComp
 				.setText(Messages.RefactoringWizardPage2_RefactoringsBelong);
 		TableWrapData td = new TableWrapData();
 		td.colspan = 1;
 		refExpandableComp.setLayoutData(td);
 		refExpandableComp.addExpansionListener(new ExpansionAdapter() {
 			public void expansionStateChanged(ExpansionEvent e) {
 				form.reflow(true);
 			}
 		});
 		Composite refExpandableClient = toolkit.createComposite(
 				refExpandableComp, SWT.WRAP);
 		refExpandableClient.setLayout(new RowLayout());
 		refExpandableComp.setClient(refExpandableClient);
 
 		// refMainExpandableComp
 		refMainExpandableComp = toolkit.createExpandableComposite(
 				form.getBody(), ExpandableComposite.TREE_NODE
 						| ExpandableComposite.CLIENT_INDENT);
 		refMainExpandableComp
 				.setText(Messages.RefactoringWizardPage2_RefactoringsMain);
 		td = new TableWrapData();
 		td.colspan = 1;
 		refMainExpandableComp.setLayoutData(td);
 		refMainExpandableComp.addExpansionListener(new ExpansionAdapter() {
 			public void expansionStateChanged(ExpansionEvent e) {
 				form.reflow(true);
 			}
 		});
 		Composite refMainExpandableClient = toolkit.createComposite(
 				refMainExpandableComp, SWT.WRAP);
 		refMainExpandableClient.setLayout(new RowLayout());
 		refMainExpandableComp.setClient(refMainExpandableClient);
 
 		sash_form.setWeights(new int[] { 5, 1 });
 
 		fillTypesList();
 		if (refactoring != null)
 			fillInRefactoringData();
 		enableFields(false);
 		enableInputButtons(false);
 		addButton.setEnabled(false);
 	}
 
 	/**
 	 * Obtiene la lista de entradas seleccionadas para la refactorizaci�n.
 	 * 
 	 * <p>
 	 * El formato devuelve es el de una lista de <i>arrays</i> de cadenas en la
 	 * que cada <i>array</i> se corresponde con una entrada y se compone de 5
 	 * elementos ordenados:
 	 * <li>
 	 * <ol>
 	 * El nombre completamente cualificado del tipo de la entrada.
 	 * </ol>
 	 * <ol>
 	 * El nombre identificativo dado a la entrada.
 	 * </ol>
 	 * <ol>
 	 * El nombre identificativo de la entrada a partir de la cual se obtiene el
 	 * valor o posibles valores para la entrada actual (si hay alguna).
 	 * </ol>
 	 * <ol>
 	 * El nombre simple del m�tod mediante el cual se obtendr�an dichos valores
 	 * a partir de la entrada apuntada por el valor del atributo "from".
 	 * </ol>
 	 * <ol>
 	 * El valor que indica si la entrada es la entrada principal de la
 	 * refactorizaci�n (en cuyo caso vale <code>"true"</code>) o no (si tiene
 	 * cualquier otro valor, como <code>"false"</code>, una cadena vac�a o
 	 * incluso <code>null</code>).
 	 * </ol>
 	 * </p>
 	 * 
 	 * @return la lista de entradas seleccionadas para la refactorizaci�n.
 	 */
 	public java.util.List<InputParameter> getInputs() {
 		return new ArrayList<InputParameter>(inputsTable.values());
 	}
 
 	/**
 	 * Obtiene la tabla temporal de par�metros.
 	 * 
 	 * @return la tabla temporal de par�metros.
 	 */
 	public InputParameter[] getInputTable() {
 		return inputsTable.values().toArray(
 				new InputParameter[inputsTable.size()]);
 	}
 
 	/**
 	 * Puebla los campos del formulario del asistente con la informaci�n que se
 	 * pueda obtener de la refactorizaci�n existente que se est� editando.
 	 */
 	private void fillInRefactoringData() {
 		if (refactoring.getInputs() != null)
 			for (InputParameter input : refactoring.getInputs()) {
 
 				// Se a�ade a la tabla de par�metros.
 				// Si el tipo no est� en la lista de tipos disponibles, se
 				// a�ade.
 				if (listModelTypes.get(input.getType()) == null) {
 					listModelTypes.put(input.getType(), 1);
 					lTypes.add(input.getType());
 					descriptionTypes.put(input.getType(), 
 							EclipseBasedJavadocReader.INSTANCE.getTypeJavaDocAsPlainText(input.getType()));
 				}
 				if (inputsTable == null)
 					inputsTable = new Hashtable<String, InputParameter>();
 
 				Integer number = listModelTypes.get(input.getType());
 				inputsTable.put(input.getType() + " (" + number + ")", input); //$NON-NLS-1$
 				listModelTypes.put(input.getType(), number + 1);
 
 				// Se a�ade a la lista de par�metros elegidos.
 				lInputs.add(input.getType() + " (" + number + ")"); //$NON-NLS-1$
 			}
 		lInputs.deselectAll();
 		this.enableFields(false);
 
 		checkForCompletion();
 
 		// Se comprueba que ninguna entrada de las cargadas apunte en el campo
 		// "from" a un nombre que no pertenece a ning�n par�metro.
 		for (InputParameter nextInput : inputsTable.values())
 			// Si tiene un campo a partir del cual se obtiene.
 			if (nextInput.getFrom() != null && !nextInput.getFrom().equals("")) { //$NON-NLS-1$
 				boolean found = false;
 				for (InputParameter subInput : inputsTable.values())
 					if (nextInput.getFrom().equals(subInput.getName())) {
 						found = true;
 						break;
 					}
 
 				if (!found) {
 					Object[] messageArgs = { nextInput.getName(),
 							nextInput.getFrom() };
 					MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
 					formatter
 							.applyPattern(Messages.RefactoringWizardPage2_InputFromInput);
 
 					updateStatus(Messages.RefactoringWizardPage2_InputsReferToNone
 							+ ": " + formatter.format(messageArgs)); //$NON-NLS-1$
 				}
 			}
 	}
 
 	/**
 	 * A�ade tantas entradas a la lista de entradas elegidas como tipos se
 	 * encuentren seleccionados en la lista de tipos disponibles en el modelo.
 	 * 
 	 * <p>
 	 * Para cada entrada a�adida, le asocia un n�mero, que ser� el n�mero de
 	 * entrada con el mismo tipo, al tiempo que actualiza las tablas de
 	 * referencia de entradas seleccionadas y de tipos de datos disponibles.
 	 * </p>
 	 */
 	private void addElements() {
 		String[] selected = lTypes.getSelection();
 		for (int i = 0; i < selected.length; i++) {
 			Integer number = listModelTypes.get(selected[i]);
 			lInputs.add(selected[i] + " (" + number + ")"); //$NON-NLS-1$
 
 			if (inputsTable == null)
 				inputsTable = new Hashtable<String, InputParameter>();
 			inputsTable.put(selected[i] + " (" + number + ")", //$NON-NLS-1$
 					new InputParameter.Builder(selected[i]).build());
 			listModelTypes.put(selected[i], number + 1);
 		}
 		checkForCompletion();
 		lInputs.deselectAll();
 		enableFields(false);
 		enableInputButtons(false);
 	}
 
 	/**
 	 * Elimina de la lista de entradas elegidas aqu�llas que se encuentren
 	 * seleccionadas y actualiza las tablas de referencia de entradas y tipos.
 	 */
 	private void removeElements() {
 		String[] selected = lInputs.getSelection();
 		for (int i = 0; i < selected.length; i++) {
 			if (!selected[i].startsWith(RefactoringConstants.MODEL_PATH + " ")) { //$NON-NLS-1$
 				lInputs.remove(selected[i]);
 				inputsTable.remove(selected[i]);
 			} else {
 				MessageDialog.openWarning(getShell(),
 						Messages.RefactoringWizardPage2_Warning,
 						Messages.RefactoringWizardPage2_ModelRequired + "."); //$NON-NLS-1$
 			}
 		}
 
 		checkForCompletion();
 		lInputs.deselectAll();
 		enableFields(false);
 		enableInputButtons(false);
 	}
 
 	/**
 	 * Puebla la lista de tipos disponibles para los par�metros de entrada con
 	 * los tipos disponibles en el modelo MOON.
 	 * 
 	 * <p>
 	 * Solo se tienen en cuenta los tipos de representaci�n de los paquetes
 	 * <code>moon.core.classdef</code>, <code>moon.core.genericity</code> y
 	 * algunos otros.
 	 * </p>
 	 * 
 	 * @param patron
 	 *            Expresion regular de b�squeda.
 	 */
 	private void fillSearchTypesList(String patron) {
 		boolean search = !(patron.trim().equals("") || patron.trim().equals("*"));
 
 		// se vacia la lista lTypes
 		lTypes.removeAll();
 		
 		if(search){
 			try {
 				Set<QueryResult> queryResultTypes=SearchingFacade.INSTANCE.search(SearchableType.INPUT, patron);
 				//se muestra por orden de relevancia
 				for(QueryResult qResult: queryResultTypes)
 					lTypes.add(qResult.getClassName());
 			} catch (ParseException e) {
 				String message = Messages.RefactoringWizardPage2_SearchNotSucceded
 								 + ".\n" + e.getMessage(); //$NON-NLS-1$
 				logger.error(message);
 				e.printStackTrace();
 			}
 		}else{
 			ArrayList<String> itemList=new ArrayList<String>(descriptionTypes.keySet());
 			Collections.sort(itemList);
 			//se muestra en orden alfabético
 			for(String item : itemList)
 				lTypes.add(item);
 		}
 		
 	}
 
 	/**
 	 * Puebla la lista de tipos disponibles para los par�metros de entrada con
 	 * los tipos disponibles en el modelo MOON.
 	 * 
 	 * <p>
 	 * Solo se tienen en cuenta los tipos de representaci�n de los paquetes
 	 * <code>moon.core.classdef</code>, <code>moon.core.genericity</code> y
 	 * algunos otros.
 	 * </p>
 	 */
 	private void fillTypesList() {
 		listModelTypes = new Hashtable<String, Integer>();
 		descriptionTypes = new HashMap<String,String>();
 
 		MOONTypeLister l = MOONTypeLister.getInstance();
 
 		java.util.List<String> itemList = l.getTypeNameList();
 		// Se ordena la lista de candidatos.
 		Collections.sort(itemList);
 
 		// Se obtiene la lista de candidatos.
 		listModelTypes.put(RefactoringConstants.STRING_PATH, 1);
 		for (String typeName : itemList) {
 
 			listModelTypes.put(typeName, 1);
 			lTypes.add(typeName);
 			descriptionTypes.put(typeName, 
 					EclipseBasedJavadocReader.INSTANCE.getTypeJavaDocAsPlainText(typeName));
 
 			// Si se est� creando una nueva refactorizaci�n.
 			if (((RefactoringWizard) getWizard()).getOperation() == RefactoringWizard.CREATE) {
 				// Se busca y a�ade autom�ticamente el modelo MOON.
 				if (typeName.equals( //$NON-NLS-1$ //$NON-NLS-2$
 						RefactoringConstants.MODEL_PATH)) {
 					lInputs.add(typeName + " (" + 1 + ")"); //$NON-NLS-1$
 					if (inputsTable == null)
 						inputsTable = new Hashtable<String, InputParameter>();
 					inputsTable
 							.put(typeName + " (" + 1 + ")", //$NON-NLS-1$
 									new InputParameter.Builder(typeName)
 											.name(Messages.RefactoringWizardPage2_ModelName)
 											.from("").method("").main(false).build()); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
 					listModelTypes.put(typeName, 2);
 
 					checkForCompletion();
 					lInputs.deselectAll();
 					enableFields(false);
 					enableInputButtons(false);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Activa o desactiva todos los campos del formulario para la introducci�n
 	 * de datos de un par�metro seleccionado.
 	 * 
 	 * @param enable
 	 *            <code>true</code> si se deben habilitar todos los campos;
 	 *            <code>false</code> si se deben deshabilitar.
 	 */
 	public void enableFields(boolean enable) {
 		tName.setEnabled(enable);
 		cFrom.setEnabled(enable);
 		cMethod.setEnabled(enable);
 		ch_Root.setEnabled(enable);
 	}
 
 	/**
 	 * Activa o desactiva todos los botones que solo deben permanecer operativos
 	 * cuando una entrada se encuentre seleccionada.
 	 * 
 	 * @param enable
 	 *            <code>true</code> si se desean activar los botones;
 	 *            <code>false</code> si se desean desactivar.
 	 */
 	public void enableInputButtons(boolean enable) {
 		delButton.setEnabled(enable);
 		upButton.setEnabled(enable);
 		downButton.setEnabled(enable);
 	}
 
 	/**
 	 * Completa los campos referentes al par�metro seleccionado en un cierto
 	 * instante con los datos de la entrada representada por #parameter.
 	 * 
 	 * @param parameter
 	 *            la entrada cuyos datos se deben mostrar.
 	 */
 	private void fillInputData(InputParameter parameter) {
 		if (parameter.getName() == null)
 			tName.setText(""); //$NON-NLS-1$
 		else
 			tName.setText(parameter.getName());
 
 		if (parameter.isMain()) //$NON-NLS-1$
 			ch_Root.setSelection(true);
 		else
 			ch_Root.setSelection(false);
 
 		fillFromComboBox();
 		cFrom.select(cFrom.indexOf(parameter.getFrom()));
 
 		fillMethodComboBox(parameter.getFrom(), parameter.getType());
 		cMethod.select(cMethod.indexOf(parameter.getMethod()));
 	}
 
 	/**
 	 * Rellena la lista desplegable de posibles objetos de origen para un valor
 	 * con los identificadores de los par�metros ya disponibles.
 	 */
 	private void fillFromComboBox() {
 		for (InputParameter nextParameter : inputsTable.values())
 			// Solo se toman las entradas con nombre.
 			if (nextParameter.getName() != null
 					&& nextParameter.getName().compareTo("") != 0) //$NON-NLS-1$
 				cFrom.add(nextParameter.getName());
 
 		cFrom.add(String.valueOf(""), 0); //$NON-NLS-1$
 	}
 
 	/**
 	 * Rellena el desplegable con los nombres de los m�todos disponibles para la
 	 * obtenci�n del par�metro de entrada seleccionado a partir de la entrada
 	 * especificada en la secci�n <i>from</i>.
 	 * 
 	 * @param fromName
 	 *            nombre de la entrada a partir de la cual se obtendr�a el valor
 	 *            de esta entrada mediante la aplicaci�n del m�todo
 	 *            seleccionado.
 	 * @param returnType
 	 *            tipo del resultado que deben devolver los m�todos en caso de
 	 *            que devuelvan un valor �nico en lugar de un conjunto.
 	 */
 	private void fillMethodComboBox(String fromName, String returnType) {
 
 		String[] methods = new String[0];
 
 		// Nombre completamente cualificado del tipo de la entrada a partir de
 		// la que se deber�a obtener el valor del par�metro tratado.
 		String type = "";
 
 		// Si no se especifica otra entrada como origen, se intentar� obtener
 		// la nueva entrada directamente desde el modelo MOON.
 		if (fromName == null || fromName.length() == 0)
 			type = RefactoringConstants.MODEL_PATH;
 
 		else
 			for (InputParameter nextInput : inputsTable.values())
 				// Se busca la clase asociada a dicho nombre.
 				if (nextInput.getName().compareTo(fromName) == 0) {
 					type = nextInput.getType();
 					break;
 				}
 
 		if (type.length() > 0)
 			methods = getMethodsFromType(type, returnType);
 
 		// Se rellena el desplegable con todos los m�todos encontrados.
 		for (int i = 0; i < methods.length; i++)
 			cMethod.add(methods[i]);
 	}
 
 	/**
 	 * Obtiene los nombres de los m�todos de una clase que devuelven objetos de
 	 * tipo <code>Iterator</code> ,<code>Collection</code> o <code>List</code>,
 	 * as� como aqu�llos que devuelven un �nico valor del tipo especificado.
 	 * 
 	 * @param className
 	 *            nombre de la clase cuyos m�todos se deben obtener.
 	 * @param type
 	 *            tipo del valor �nico que pueden devolver los m�todos.
 	 * 
 	 * @return un <i>array</i> de cadenas con los nombres de los m�todos.
 	 */
 	private String[] getMethodsFromType(String className, String type) {
 
 		ArrayList<String> temp = new ArrayList<String>();
 
 		try {
 			Class<?> from = Class.forName(className);
 
 			int i = 0;
 
 			Method[] methods = from.getMethods();
 			for (i = 0; i < methods.length; i++)
 				if (InputProcessor.isMethodValid(methods[i], type))
 					temp.add((from.getMethods()[i]).getName());
 
 			temp.add(0, ""); //$NON-NLS-1$
 		} catch (Exception e) {
 		}
 
 		String[] array = new String[temp.size()];
 		return temp.toArray(array);
 	}
 
 	/**
 	 * Actualiza el estado de la pantalla de di�logo del asistente.
 	 * 
 	 * @param message
 	 *            mensaje asociado al estado actual de la pantalla.
 	 */
 	private void updateStatus(String message) {
 		setErrorMessage(message);
 		setPageComplete(message == null);
 	}
 
 	/**
 	 * Comprueba si todos los campos necesarios se han completado y si se puede
 	 * continuar con el siguiente paso del asistente.
 	 */
 	private void checkForCompletion() {
 		int rootInputs = 0;
 
 		// Se comprueban todos los par�metros de entrada.
 		for (InputParameter nextInput : inputsTable.values()) {
 			// Todos los par�metros deben tener nombre.
 			if (!nextInput.getType().equals(RefactoringConstants.MODEL_PATH)
 					&& nextInput.getName().equals("")) { //$NON-NLS-1$
 
 				Object[] messageArgs = { nextInput.getType() };
 				MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
 				formatter
 						.applyPattern(Messages.RefactoringWizardPage2_NameNeeded);
 
 				updateStatus(formatter.format(messageArgs) + "."); //$NON-NLS-1$
 				return;
 			}
 
 			if (nextInput.isMain()) { //$NON-NLS-1$
 				rootInputs++;
 				// El par�metro principal no puede obtenerse a partir de otro.
 				if ((nextInput.getFrom() != null && nextInput.getFrom()
 						.length() != 0)
 						|| (nextInput.getMethod() != null && nextInput
 								.getMethod().length() != 0)) {
 					updateStatus(Messages.RefactoringWizardPage2_MainCannotBeObtained
 							+ "."); //$NON-NLS-1$
 					return;
 				}
 				// El par�metro principal no puede ser de cualquier tipo.
 				MainInputValidator validator = new MainInputValidator();
 				if (!validator.checkMainType(nextInput.getType())) {
 
 					Object[] messageArgs = { MainInputValidator.ATTDEC,
 							MainInputValidator.CLASSDEF,
 							MainInputValidator.FORMALARG,
 							MainInputValidator.FORMALPAR,
 							MainInputValidator.METHDEC };
 					MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
 					formatter
 							.applyPattern(Messages.RefactoringWizardPage2_MainMustConform);
 
 					updateStatus(formatter.format(messageArgs));
 					return;
 				}
 				//El tipo del parámetro principal debe cumplir con el ambito seleccinado
 				//para la refactorización elegido en la pagina 1 del wizard.
 				String scopeName=((RefactoringWizard) this.getWizard()).scope.getName();
 				if( scopeName==null ||
 					validator.convertScopeCategory(nextInput.getType())==null ||
 					!validator.convertScopeCategory(nextInput.getType()).equalsIgnoreCase(scopeName)){
 					updateStatus(Messages.RefactoringWizardPage2_MainMustConformWithScope + "."); //$NON-NLS-1$
 					return ;
 				}
 					
 			}
 			if (nextInput.getFrom() != null
 					&& nextInput.getFrom().length() != 0)
 				if (nextInput.getMethod() == null
 						|| nextInput.getMethod().length() == 0) {
 					Object[] messageArgs = { "(" + nextInput.getType() //$NON-NLS-1$
 							+ ")" }; //$NON-NLS-1$
 					MessageFormat formatter = new MessageFormat(""); //$NON-NLS-1$
 					formatter
 							.applyPattern(Messages.RefactoringWizardPage2_MethodNeeded);
 
 					updateStatus(formatter.format(messageArgs) + "."); //$NON-NLS-1$
 					return;
 				}
 
 		}
 
 		// Tiene que haber una entrada principal.
 		if (rootInputs < 1) {
 			updateStatus(Messages.RefactoringWizardPage2_MainNeeded + "."); //$NON-NLS-1$
 			return;
 		}
 		// No puede haber m�s de una entrada principal.
 		if (rootInputs > 1) {
 			updateStatus(Messages.RefactoringWizardPage2_OnlyOneMain + "."); //$NON-NLS-1$
 			return;
 		}
 
 		updateStatus(null);
 	}
 
 	/**
 	 * Actualiza el panel inferior para mostrar en cada uno de los campos del
 	 * formulario la informaci�n apropiada acerca del par�metro seleccionado en
 	 * la lista del panel de par�metros.
 	 * 
 	 * @author <A HREF="mailto:lfd0002@alu.ubu.es">Laura Fuente de la Fuente</A>
 	 * @author <A HREF="mailto:sfd0009@alu.ubu.es">Sonia Fuente de la Fuente</A>
 	 * @author <A HREF="mailto:ehp0001@alu.ubu.es">Enrique Herrero Paredes</A>
 	 */
 	private class InputSelectionListener implements SelectionListener {
 
 		/**
 		 * Recibe una notificaci�n de que un elemento de la lista de par�metros
 		 * de entrada ha sido seleccionado.
 		 * 
 		 * <p>
 		 * Inicia las acciones que sean necesarias para actualizar la
 		 * informaci�n mostrada en el panel inferior acerca del par�metro
 		 * seleccionado.
 		 * </p>
 		 * 
 		 * @param e
 		 *            el evento de selecci�n disparado en la ventana.
 		 * 
 		 * @see SelectionListener#widgetSelected(SelectionEvent)
 		 */
 		public void widgetSelected(SelectionEvent e) {
 			cFrom.removeAll();
 			cMethod.removeAll();
 
 			// Si hay elementos seleccionados entre los elegidos.
 			if (lInputs.getSelectionCount() > 0
 					&& lInputs.getItem(lInputs.getSelectionIndex()) != null) {
 				enableInputButtons(true);
 				enableFields(false);
 
 				if (lInputs.getSelectionCount() == 1) {
 
 					InputParameter input = inputsTable.get(lInputs
 							.getSelection()[0]);
 
 					if (input != null) {
 						enableFields(true);
 						fillInputData(input);
 					}
 					if (input.getType().equals(RefactoringConstants.MODEL_PATH)) {
 						cFrom.setEnabled(false);
 						cMethod.setEnabled(false);
 					}
 				}
 			}
 			// Si no hay ning�n elemento seleccionado.
 			else {
 				enableFields(false);
 				enableInputButtons(false);
 			}
 		}
 
 		/**
 		 * @see SelectionListener#widgetDefaultSelected(SelectionEvent)
 		 */
 		public void widgetDefaultSelected(SelectionEvent e) {
 			widgetSelected(e);
 		}
 	}
 
 	/**
 	 * Actualiza el valor del atributo de la entrada seleccionada que indica si
 	 * se trata de la entrada principal de la refactorizaci�n o no.
 	 * 
 	 * @author <A HREF="mailto:lfd0002@alu.ubu.es">Laura Fuente de la Fuente</A>
 	 * @author <A HREF="mailto:sfd0009@alu.ubu.es">Sonia Fuente de la Fuente</A>
 	 * @author <A HREF="mailto:ehp0001@alu.ubu.es">Enrique Herrero Paredes</A>
 	 */
 	private class RootSelectionListener implements SelectionListener {
 
 		/**
 		 * Recibe una notificaci�n de que la marca de selecci�n ha sido
 		 * seleccionada o deseleccionada.
 		 * 
 		 * @param e
 		 *            el evento de selecci�n disparado en la ventana.
 		 * 
 		 * @see SelectionListener#widgetSelected(SelectionEvent)
 		 */
 		public void widgetSelected(SelectionEvent e) {
 			// Si hay elementos seleccionados entre los elegidos
 			if (lInputs.getSelectionCount() > 0
 					&& lInputs.getItem(lInputs.getSelectionIndex()) != null) {
 				InputParameter input = inputsTable
 						.get(lInputs.getSelection()[0]);
 
 				Boolean isTrue = ch_Root.getSelection();
 				inputsTable.put(lInputs.getSelection()[0], input.getBuilder()
 						.main(isTrue).build());
 				checkForCompletion();
 			}
 		}
 
 		/**
 		 * @see SelectionListener#widgetDefaultSelected(SelectionEvent)
 		 */
 		public void widgetDefaultSelected(SelectionEvent e) {
 			widgetSelected(e);
 		}
 	}
 
 	/**
 	 * Recibe notificaciones cuando uno de los elementos de la lista de tipos es
 	 * seleccionado.
 	 * 
 	 * @author <A HREF="mailto:lfd0002@alu.ubu.es">Laura Fuente de la Fuente</A>
 	 * @author <A HREF="mailto:sfd0009@alu.ubu.es">Sonia Fuente de la Fuente</A>
 	 * @author <A HREF="mailto:ehp0001@alu.ubu.es">Enrique Herrero Paredes</A>
 	 */
 	private class TypeSelectionListener implements SelectionListener {
 
 		/**
 		 * Recibe una notificaci�n de que un elemento de la lista de tipos
 		 * disponibles ha sido seleccionado.
 		 * 
 		 * <p>
 		 * Activa el bot�n que permite a�adir el o los tipos seleccionados a la
 		 * lista de entradas.
 		 * </p>
 		 * 
 		 * @param e
 		 *            el evento de selecci�n disparado en la ventana.
 		 * 
 		 * @see SelectionListener#widgetSelected(SelectionEvent)
 		 */
 		public void widgetSelected(SelectionEvent e) {
 			String path = lTypes.getItem(lTypes.getSelectionIndex()).toString();
 			path = path.replace('.', '/');
 			if (path.startsWith("moon")) {
 				path = RefactoringConstants.REFACTORING_JAVADOC + "/moon/"
 						+ path + ".html";
 			} else {
 				if (path.startsWith("javamoon"))
 					path = RefactoringConstants.REFACTORING_JAVADOC
 							+ "/javamoon/" + path + ".html";
 			}
 
 			try {
 
 				if (new File(FileLocator.toFileURL(
 						RefactoringPlugin.getDefault().getBundle()
 								.getEntry(path)).getFile()).exists()) {
 					navegador.setUrl(FileLocator.toFileURL(RefactoringPlugin
 							.getDefault().getBundle().getEntry(path))
 							+ "#skip-navbar_top");
 				} else {
 					navegador
 							.setUrl(FileLocator
 									.toFileURL(
 											RefactoringPlugin
 													.getDefault()
 													.getBundle()
 													.getEntry(
 															RefactoringConstants.REFACTORING_JAVADOC
 																	+ "/moon/notFound.html"))
 									.toString());
 				}
 			} catch (IOException excp) {
 				throw Throwables.propagate(excp);
 			}
 
 			// TODO: realizar modificaciones oportunas
 			// form
 			form.getParent().setVisible(false);
 			// previamente eleminamos las etiquetas que contienen los
 			// desplegables
 			Control labels[] = null;
 			labels = ((Composite) refExpandableComp.getClient()).getChildren();
 			for (int i = 0; i < labels.length; i++)
 				labels[i].dispose();
 			labels = ((Composite) refMainExpandableComp.getClient())
 					.getChildren();
 			for (int i = 0; i < labels.length; i++)
 				labels[i].dispose();
 
 			String typeSelected = lTypes.getItem(lTypes.getSelectionIndex())
 					.toString();
 			form.setText(typeSelected);
 			descriptionFormLabel.setText(descriptionTypes.get(typeSelected));
 
 			// refactoringsInputType
 			java.util.List<DynamicRefactoringDefinition> refactoringsInputType = new ArrayList<DynamicRefactoringDefinition>(
 					XMLRefactoringsCatalog.getInstance()
 							.getRefactoringsContainsInputType(typeSelected));
 
 			Collections.sort(refactoringsInputType);
 			Label refLabel = null;
 			String refName = null;
 			for (int i = 0; i < refactoringsInputType.size(); i++) {
 				refName = refactoringsInputType.get(i).getName();
 				if (i < refactoringsInputType.size() - 1)
 					refName += ",";
 				refLabel = toolkit.createLabel(
 						(Composite) refExpandableComp.getClient(), refName);
 				refLabel.setData(refactoringsInputType.get(i));
 				RefactoringTooltip tooltip = new RefactoringTooltip(refLabel);
 				tooltip.setPopupDelay(200);
 			}
 			refExpandableComp.setExpanded(!refactoringsInputType.isEmpty());
 
 			// refactoringsRootInputType
 			ArrayList<DynamicRefactoringDefinition> refactoringsRootInputType = new ArrayList<DynamicRefactoringDefinition>(
 					XMLRefactoringsCatalog.getInstance()
 							.getRefactoringsContainsRootInputType(typeSelected));
 			Collections.sort(refactoringsRootInputType);
 			Label refRootLabel = null;
 			String refRootName = null;
 			for (int i = 0; i < refactoringsRootInputType.size(); i++) {
 				refRootName = refactoringsRootInputType.get(i).getName();
 				if (i < refactoringsRootInputType.size() - 1)
 					refRootName += ",";
 				refRootLabel = toolkit.createLabel(
 						(Composite) refMainExpandableComp.getClient(),
 						refRootName);
 				RefactoringTooltip tooltip = new RefactoringTooltip(
 						refRootLabel);
 				tooltip.setPopupDelay(200);
 			}
 			refMainExpandableComp.setExpanded(!refactoringsRootInputType
 					.isEmpty());
 
 			form.getParent().setVisible(true);
 			form.reflow(true);
 
 			if (lTypes.getSelectionCount() > 0
 					&& lTypes.getItem(lTypes.getSelectionIndex()) != null)
 				addButton.setEnabled(true);
 			else
 				addButton.setEnabled(false);
 		}
 
 		/**
 		 * @see SelectionListener#widgetDefaultSelected(SelectionEvent)
 		 */
 		public void widgetDefaultSelected(SelectionEvent e) {
 			widgetSelected(e);
 		}
 	}
 
 	/**
 	 * Permite observar y controlar los cambios realizados sobre el contenido
 	 * del campo que contiene el nombre del par�metro de entrada seleccionado.
 	 * 
 	 * <p>
 	 * Cuando se modifica el contenido de dicho campo, se actualiza el nombre
 	 * del par�metro, a menos que se trate de un nombre ya asignado a otra
 	 * entrada.
 	 * </p>
 	 * 
 	 * @author <A HREF="mailto:lfd0002@alu.ubu.es">Laura Fuente de la Fuente</A>
 	 * @author <A HREF="mailto:sfd0009@alu.ubu.es">Sonia Fuente de la Fuente</A>
 	 * @author <A HREF="mailto:ehp0001@alu.ubu.es">Enrique Herrero Paredes</A>
 	 */
 	private class NameFocusListener implements FocusListener {
 
 		/**
 		 * Recibe una notificaci�n indicando que el texto observado ha recibido
 		 * el foco (en este caso, el texto que contiene el nombre del par�metro
 		 * seleccionado).
 		 * 
 		 * @param e
 		 *            evento con la informaci�n referente a la recepci�n del
 		 *            foco.
 		 * 
 		 * @see FocusListener#focusGained(FocusEvent)
 		 */
 		@Override
 		public void focusGained(FocusEvent e) {
 		}
 
 		/**
 		 * Recibe una notificaci�n indicando que el texto observado ha perdido
 		 * el foco.
 		 * 
 		 * @param e
 		 *            evento con la informaci�n referente a la p�rdida del foco.
 		 * 
 		 * @see FocusListener#focusLost(FocusEvent)
 		 */
 		@Override
 		public void focusLost(FocusEvent e) {
 
 			// Si hay elementos seleccionados en la lista de tipos elegidos.
 			String[] selection = lInputs.getSelection();
 			if (selection != null && selection.length > 0) {
 				InputParameter current = inputsTable
 						.get(lInputs.getSelection()[0]);
 
 				if (!tName.getText().equals(current.getName())) {
 					if (checkName(tName.getText(), current)) {
 						inputsTable.remove(lInputs.getSelection()[0]);
 						inputsTable.put(lInputs.getSelection()[0], current
 								.getBuilder().name(tName.getText()).build());
 					} else {
 						MessageDialog.openWarning(getShell(),
 								Messages.RefactoringWizardPage2_Warning,
 								Messages.RefactoringWizardPage2_NameRepeated
 										+ ".\n"); //$NON-NLS-1$
 						tName.setText(current.getName());
 					}
 				}
 			}
 
 			checkForCompletion();
 		}
 
 		/**
 		 * Comprueba que un nombre dado no pertenezca a alg�n otro elemento
 		 * distinto del indicado.
 		 * 
 		 * @param name
 		 *            el nombre cuya unicidad se debe verificar.
 		 * @param current
 		 *            el �nico par�metro de entrada para el que se admite el
 		 *            nombre como v�lido.
 		 * 
 		 * @return <code>false</code> si el nombre ya existe; <code>true
 		 * </code> en caso contrario.
 		 */
 		private boolean checkName(String name, InputParameter current) {
 			for (InputParameter nextInput : inputsTable.values()) {
 				// Se busca una entrada con el nombre.
 				if (nextInput.getName().equals(name)
 						&& nextInput.getType().equals(current)) {
 					return false;
 				}
 			}
 			return true;
 
 		}
 	}
 
 	/**
 	 * Permite observar y controlar los cambios realizados sobre el contenido
 	 * del campo desplegable que contiene la entrada a partir de la que se puede
 	 * obtener a su vez la entrada actual.
 	 * 
 	 * <p>
 	 * Cuando se modifica el contenido de dicho campo, se actualiza el apuntador
 	 * del par�metro al objeto a partir del que se puede obtener su valor.
 	 * </p>
 	 * 
 	 * @author <A HREF="mailto:sfd0009@alu.ubu.es">Sonia Fuente de la Fuente</A>
 	 * @author <A HREF="mailto:ehp0001@alu.ubu.es">Enrique Herrero Paredes</A>
 	 */
 	private class FromFocusListener implements FocusListener {
 
 		/**
 		 * Recibe una notificaci�n indicando que el elemento observado ha
 		 * recibido el foco (en este caso, el desplegable que contiene los
 		 * nombres de las entradas).
 		 * 
 		 * @param e
 		 *            evento con la informaci�n referente a la recepci�n del
 		 *            foco.
 		 * 
 		 * @see FocusListener#focusGained(FocusEvent)
 		 */
 		@Override
 		public void focusGained(FocusEvent e) {
 		}
 
 		/**
 		 * Recibe una notificaci�n indicando que el elemento observado ha
 		 * perdido el foco.
 		 * 
 		 * @param e
 		 *            evento con la informaci�n referente a la p�rdida del foco.
 		 * 
 		 * @see FocusListener#focusLost(FocusEvent)
 		 */
 		@Override
 		public void focusLost(FocusEvent e) {
 			cMethod.removeAll();
 
 			// Si hay elementos seleccionados en la lista de tipos elegidos.
 			String[] selection = lInputs.getSelection();
 			if (selection != null && selection.length > 0) {
 
 				if (cFrom.getSelectionIndex() != -1) {
 					InputParameter current = inputsTable.get(lInputs
 							.getSelection()[0]);
 					inputsTable.put(
 							lInputs.getSelection()[0],
 							current.getBuilder()
 									.from(cFrom.getItem(cFrom
 											.getSelectionIndex())).build());
 					fillMethodComboBox(
 							cFrom.getItem(cFrom.getSelectionIndex()),
 							current.getType());
 					cMethod.select(cMethod.indexOf(current.getMethod()));
 				}
 				checkForCompletion();
 			}
 		}
 	}
 
 	/**
 	 * Permite observar y controlar los cambios realizados sobre el contenido
 	 * del campo desplegable que contiene el m�todo mediante el que se puede
 	 * obtener a su vez el valor de la entrada actual.
 	 * 
 	 * <p>
 	 * Cuando se modifica el contenido de dicho campo, se actualiza el apuntador
 	 * del par�metro al m�todo mediante el que se puede obtener su valor.
 	 * </p>
 	 * 
 	 * @author <A HREF="mailto:sfd0009@alu.ubu.es">Sonia Fuente de la Fuente</A>
 	 * @author <A HREF="mailto:ehp0001@alu.ubu.es">Enrique Herrero Paredes</A>
 	 */
 	private class MethodFocusListener implements FocusListener {
 
 		/**
 		 * Recibe una notificaci�n indicando que el elemento observado ha
 		 * recibido el foco (en este caso, el desplegable que contiene los
 		 * m�todos de la entrada seleccionada como origen).
 		 * 
 		 * @param e
 		 *            evento con la informaci�n referente a la recepci�n del
 		 *            foco.
 		 * 
 		 * @see FocusListener#focusGained(FocusEvent)
 		 */
 		@Override
 		public void focusGained(FocusEvent e) {
 		}
 
 		/**
 		 * Recibe una notificaci�n indicando que el elemento observado ha
 		 * perdido el foco.
 		 * 
 		 * @param e
 		 *            evento con la informaci�n referente a la p�rdida del foco.
 		 * 
 		 * @see FocusListener#focusLost(FocusEvent)
 		 */
 		@Override
 		public void focusLost(FocusEvent e) {
 			// Si hay elementos seleccionados entre los elegidos.
 			String[] selection = lInputs.getSelection();
 			if (selection != null && selection.length > 0) {
 
 				if (cMethod.getSelectionIndex() != -1) {
 					InputParameter current = inputsTable.get(lInputs
 							.getSelection()[0]);
 					inputsTable.put(
 							lInputs.getSelection()[0],
 							current.getBuilder()
 									.method(cMethod.getItem(cMethod
 											.getSelectionIndex())).build());
 				}
 
 				checkForCompletion();
 			}
 		}
 	}
 }
