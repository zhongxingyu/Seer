 package fiuba.mda.ui.actions;
 
 import org.eclipse.jface.action.Action;
 
 import com.google.common.base.Optional;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.google.inject.Singleton;
 
 import fiuba.mda.model.Application;
 import fiuba.mda.model.BehaviorDiagram;
 import fiuba.mda.model.ModelAspect;
 import fiuba.mda.model.ModelPackage;
 import fiuba.mda.ui.actions.validators.NameValidatorFactory;
 import fiuba.mda.ui.launchers.Launcher;
 import fiuba.mda.ui.launchers.SimpleDialogLauncher;
 import fiuba.mda.ui.main.tree.ComponentDefaultActionVisitor;
 import fiuba.mda.ui.utilities.ImageLoader;
 import fiuba.mda.utilities.SimpleEvent.Observer;
 
 /**
  * {@link Action} implementation which represents the command of creating a new
  * behavior diagram on the current active package
  */
 @Singleton
 public class NewBehaviourDiagramAction extends Action {
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
 	 * Creates a new {@link NewBehaviourDiagramAction} instance
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
 	public NewBehaviourDiagramAction(final Application model,
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
 		setText("Nuevo Diagrama de Comportamiento");
 		setToolTipText("Crear un nuevo diagrama de comportamiento en el proyecto");
 		setEnabled(false);
 		setImageDescriptor(imageLoader.descriptorOf("chart_line_add"));
 	}
 
 	@Override
 	public void run() {
 		ModelPackage activePackage = model.getActivePackage();
		ModelAspect aspect = activePackage.ensureAspect("Comportamiento");
 		Optional<String> name = askForName(aspect);
 		if (name.isPresent()) {
 			BehaviorDiagram newDiagram = new BehaviorDiagram(name.get());
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
 		return "Diagrama de comportamiento en "
 				+ model.getActivePackage().getQualifiedName();
 	}
 }
