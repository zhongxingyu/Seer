 package fiuba.mda.ui.actions;
 
 import com.google.common.base.Optional;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.google.inject.Singleton;
 import fiuba.mda.model.*;
 import fiuba.mda.ui.actions.validators.NameValidatorFactory;
 import fiuba.mda.ui.launchers.Launcher;
 import fiuba.mda.ui.launchers.SimpleDialogLauncher;
 import fiuba.mda.ui.main.tree.ComponentDefaultActionVisitor;
 import fiuba.mda.ui.utilities.ImageLoader;
 import fiuba.mda.utilities.SimpleEvent.Observer;
 import org.eclipse.jface.action.Action;
 
 /**
  * {@link org.eclipse.jface.action.Action} implementation which represents the command of creating a new
  * behavior diagram on the current active package
  */
 @Singleton
 public class NewGraficInterfaceDiagramAction extends Action {
 	private Observer<Application> onProjectOpen = new Observer<Application>() {
 		@Override
 		public void notify(Application observable) {
 			setEnabled(model.hasCurrentProject());
 		}
 	};
 
 	private final Application model;
 
 	private final SimpleDialogLauncher dialog;
 
 	private final NameValidatorFactory dialogNameValidator;
 
 	private final Provider<ComponentDefaultActionVisitor> editorProvider;
 
 	/**
 	 * Creates a new {@link fiuba.mda.ui.actions.NewGraficInterfaceDiagramAction} instance
 	 *
 	 * @param model
 	 *            the model on which this action will create a new package
 	 * @param dialog
 	 *            the dialog controller used to create the associated dialogs
 	 * @param imageLoader
 	 *            the image loader used to provide the image of this action
 	 * @param dialogNameValidator
 	 *            the validator used to validate the package name on the input
 	 *            dialogs
 	 */
 	@Inject
 	public NewGraficInterfaceDiagramAction(final Application model,
                                            final SimpleDialogLauncher dialog, final ImageLoader imageLoader,
                                            final NameValidatorFactory dialogNameValidator,
                                            Provider<ComponentDefaultActionVisitor> editorProvider) {
 		this.model = model;
 		this.dialog = dialog;
 		this.dialogNameValidator = dialogNameValidator;
 		this.editorProvider = editorProvider;
 
 		setupPresentation(imageLoader);
 		setupEventObservation(model);
 	}
 
 	private void setupEventObservation(final Application model) {
 		model.projectOpenEvent().observe(this.onProjectOpen);
 	}
 
 	private void setupPresentation(final ImageLoader imageLoader) {
 		setText("Nuevo Diagrama de Interfáz Gráfica");
 		setToolTipText("Crear un nuevo diagrama de Interfáz Gráfica en el proyecto");
 		setEnabled(false);
 		setImageDescriptor(imageLoader.descriptorOf("image_add"));
 	}
 
 	@Override
 	public void run() {
 		ModelPackage activePackage = model.getActivePackage();
		ModelAspect aspect = activePackage.ensureAspect("Interfaces");
 		Optional<String> name = askForName(aspect);
 		if (name.isPresent()) {
 			GraficInterfaceDiagram newDiagram = new GraficInterfaceDiagram(name.get());
 			aspect.addChild(newDiagram);
 			Optional<Launcher> controller = editorProvider.get()
 					.controllerFor(newDiagram);
 			if (controller.isPresent()) {
 				controller.get().launch(newDiagram);
 			}
 		}
 		aspect.removeIfUnnecessary();
 	}
 
 	private Optional<String> askForName(ModelAspect aspect) {
 		return dialog.showInput(dialogTitle(), "Nombre", null,
 				dialogNameValidator.validatorForNewNameInParent(aspect));
 	}
 
 	private String dialogTitle() {
 		return "Diagrama de Interfáz Gráfica en "
 				+ model.getActivePackage().getQualifiedName();
 	}
 }
