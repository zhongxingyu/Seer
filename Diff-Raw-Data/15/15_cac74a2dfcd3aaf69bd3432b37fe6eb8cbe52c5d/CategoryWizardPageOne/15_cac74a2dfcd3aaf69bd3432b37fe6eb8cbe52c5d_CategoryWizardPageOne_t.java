 package de.hswt.hrm.component.ui.wizard;
 
 import java.net.URL;
 import java.util.Collection;
 
 import javax.inject.Inject;
 
 import org.eclipse.e4.xwt.IConstants;
 import org.eclipse.e4.xwt.XWT;
 import org.eclipse.e4.xwt.forms.XWTForms;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ComboViewer;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.widgets.Section;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Optional;
 
 import de.hswt.hrm.catalog.model.Catalog;
 import de.hswt.hrm.catalog.service.CatalogService;
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.ui.swt.forms.FormUtil;
 import de.hswt.hrm.common.ui.swt.layouts.PageContainerFillLayout;
 import de.hswt.hrm.component.model.Category;
 import de.hswt.hrm.i18n.I18n;
 import de.hswt.hrm.i18n.I18nFactory;
 
 public class CategoryWizardPageOne extends WizardPage {
 
 	private static final Logger LOG = LoggerFactory
 			.getLogger(CategoryWizardPageOne.class);
 	private static final I18n I18N = I18nFactory
 			.getI18n(CategoryWizardPageOne.class);
 	private static int MAX_QUANIFIER = 10;
 
 	private Composite container;
 	private Optional<Category> category;
 
 	@Inject
 	CatalogService catalogService;
 
 	private int[] validGridSizeValue = { 1, 2, 4, 6 };
 
 	private Text nameText;
 
 	private Combo weightCombo;
 
 	private ComboViewer catalogCombo;
 
 	private List widthList;
 
 	private List heightList;
 
 	private Button ratingCheckButton;
 
 	private String name;
 
 	private int weight;
 
 	private int width;
 
 	private int height;
 
 	private boolean rating;
 
 	private Optional<Catalog> catalog;
 
 	private boolean first = true;
 
 	public CategoryWizardPageOne(String title, Optional<Category> category) {
 		super(title);
 		this.category = category;
 		setDescription(createDescription());
 		setTitle(I18N.tr("Category Wizard"));
 	}
 
 	private String createDescription() {
 		if (category.isPresent()) {
 			return I18N.tr("Edit a category");
 		}
 		return I18N.tr("Add a new category");
 	}
 
 	public void createControl(Composite parent) {
 		parent.setLayout(new PageContainerFillLayout());
 		URL url = CategoryWizardPageOne.class.getClassLoader().getResource(
 				"de/hswt/hrm/component/ui/xwt/CategoryWizardPageOne"
 						+ IConstants.XWT_EXTENSION_SUFFIX);
 		try {
 			container = (Composite) XWTForms.load(parent, url);
 		} catch (Exception e) {
 			LOG.error("An error occured: ", e);
 		}
 
 		translate();
 
 		nameText = (Text) XWT.findElementByName(container, "name");
 		weightCombo = (Combo) XWT.findElementByName(container,
 				"defaultQuantifier");
 		widthList = (List) XWT.findElementByName(container, "width");
 		heightList = (List) XWT.findElementByName(container, "height");
 		ratingCheckButton = (Button) XWT.findElementByName(container,
 				"defaultBoolRating");
 		catalogCombo = (ComboViewer) XWT.findElementByName(container,
 				"catalogCombo");
 
 		initItems();
 		initalizeCatalogCombo();
 
 		if (this.category.isPresent()) {
 			updateFields();
 		}
 
 		nameText.addKeyListener(new KeyListener() {
 			public void keyPressed(KeyEvent e) {
 			}
 
 			public void keyReleased(KeyEvent e) {
 				checkPageComplete();
 			}
 		});
 
 		addSelectionListener(widthList);
 		addSelectionListener(weightCombo);
 		addSelectionListener(heightList);
 		addSelectionListener(ratingCheckButton);
		addSelectionListener(catalogCombo.getCombo());
 
 		FormUtil.initSectionColors((Section) XWT.findElementByName(container,
 				"General"));
 
 		setControl(container);
 		checkPageComplete();
 	}
 
 	private void initalizeCatalogCombo() {
 
 		try {
 			Collection<Catalog> catalogs = catalogService.findAllCatalog();
 
 			catalogCombo.setLabelProvider(new LabelProvider() {
 
 				@Override
 				public String getText(Object element) {
 					Catalog c = (Catalog) element;
 					return c.getName();
 				}
 
 			});
 			catalogCombo.setContentProvider(ArrayContentProvider.getInstance());
 			catalogCombo.setInput(catalogs);
 		} catch (DatabaseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	private void initItems() {
 		for (Integer value : validGridSizeValue) {
 			widthList.add(String.valueOf(value));
 			heightList.add(String.valueOf(value));
 		}
 		for (int i = 1; i <= MAX_QUANIFIER; i++) {
 			weightCombo.add(String.valueOf(i));
 		}
 	}
 
 	private void updateFields() {
 		Category cat = category.get();
 		nameText.setText(cat.getName());
 		weightCombo.select(weightCombo.indexOf(String.valueOf(cat
 				.getDefaultQuantifier())));
 		ratingCheckButton.setSelection(cat.getDefaultBoolRating());
 		widthList.select(widthList.indexOf(String.valueOf(cat.getWidth())));
 		heightList.select(heightList.indexOf(String.valueOf(cat.getHeight())));
 
 		String[] items = catalogCombo.getCombo().getItems();
 		int i = 0;
 
 		if (!cat.getCatalog().isPresent()) {
 			return;
 		} else {
 			while (!cat.getCatalog().get().getName().equals(items[i])) {
 				i++;
 			}
 			catalogCombo.getCombo().select(i);
 		}
 
 	}
 
 	private void checkPageComplete() {
 		if (first) {
 			first = false;
 			setPageComplete(false);
 			return;
 		}
 
 		rating = ratingCheckButton.getSelection();
 
 		setErrorMessage(null);
 		// FIXME check if category is not empty or it already exists
 		name = nameText.getText();
 
 		if (weightCombo.getSelectionIndex() > -1) {
 			weight = Integer.parseInt(weightCombo.getItem(weightCombo
 					.getSelectionIndex()));
 		} else {
 			setErrorMessage(I18N.tr("Select a standard weight!"));
 			return;
 		}
 
 		if (widthList.getSelectionIndex() > -1) {
 			width = Integer.parseInt(widthList.getItem(widthList
 					.getSelectionIndex()));
 		} else {
 			setErrorMessage(I18N.tr("Select a grid width!"));
 			return;
 		}
 
 		if (heightList.getSelectionIndex() > -1) {
 			height = Integer.parseInt(heightList.getItem(heightList
 					.getSelectionIndex()));
		} else {
			setErrorMessage(I18N.tr("Select a grid height!"));
			return;
 		}
 
 		if (catalogCombo.getCombo().getSelectionIndex() > -1) {
 			IStructuredSelection selection = (IStructuredSelection) catalogCombo
 					.getSelection();
 			catalog = Optional.of((Catalog) selection.getFirstElement());
		} else {
			setErrorMessage(null);
 		}
 
		
 	}
 
 	@Override
 	public void setErrorMessage(String newMessage) {
 		if (newMessage == null || newMessage.isEmpty()) {
 			setPageComplete(true);
 		} else {
 			setPageComplete(false);
 		}
 		super.setErrorMessage(newMessage);
 	}
 
 	private void addSelectionListener(Control control) {
 		control.addListener(SWT.Selection, new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				checkPageComplete();
 			}
 		});
 	}
 
 	private void translate() {
 		// Section
 		setSectionText("General", I18N.tr("Category definition"));
 		// Labels
 		setLabelText("lblName", I18N.tr("Name") + ":");
 		setLabelText("lblWeight", I18N.tr("Weight") + ":");
 		setLabelText("lblCatalog", I18N.tr("Catalog") + ":");
 		setLabelText("lblWidth", I18N.tr("Width in grid") + ":");
 		setLabelText("lblHeight", I18N.tr("Height in grid") + ":");
 		// CheckButton
 		setCheckButtonText("defaultBoolRating", I18N.tr("with rating"));
 	}
 
 	private void setLabelText(String labelName, String text) {
 		Label l = (Label) XWT.findElementByName(container, labelName);
 		if (l == null) {
 			LOG.error("Label '" + labelName + "' not found.");
 		}
 		l.setText(text);
 	}
 
 	private void setSectionText(String sectionName, String text) {
 		Section s = (Section) XWT.findElementByName(container, sectionName);
 		if (s == null) {
 			LOG.error("Section '" + sectionName + "' not found.");
 		}
 		s.setText(text);
 	}
 
 	private void setCheckButtonText(String buttonName, String text) {
 		Button b = (Button) XWT.findElementByName(container, buttonName);
 		if (b == null) {
 			LOG.error("Button '" + buttonName + "' not found.");
 		}
 		b.setText(text);
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public int getWeight() {
 		return weight;
 	}
 
 	public int getWidth() {
 		return width;
 	}
 
 	public int getHeight() {
 		return height;
 	}
 
 	public boolean isRating() {
 		return rating;
 	}
 
 	public Optional<Catalog> getCatalog() {
 		return catalog;
 	}
 }
