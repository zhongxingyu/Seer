 package fiuba.mda.ui.launchers;
 
 import com.google.common.base.Optional;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import fiuba.mda.model.BehaviorDiagram;
 import fiuba.mda.model.GraficInterfaceDiagram;
 import fiuba.mda.ui.actions.validators.NameValidatorFactory;
 
 @Singleton
 public class GraficInterfaceDiagramPropsLauncher extends BaseLauncher<GraficInterfaceDiagram> {
 	private final SimpleDialogLauncher dialogs;
 	private final NameValidatorFactory validatorFactory;
 
 	@Inject
 	public GraficInterfaceDiagramPropsLauncher(final SimpleDialogLauncher dialogs,
                                                final NameValidatorFactory validatorFactory) {
 		this.dialogs = dialogs;
 		this.validatorFactory = validatorFactory;
 	}
 
 	@Override
 	protected void doLaunch(final GraficInterfaceDiagram component) {
 		Optional<String> name = askForName(component);
 		if (name.isPresent()) {
 			component.setName(name.get());
 		}
 	}
 
 	private Optional<String> askForName(final GraficInterfaceDiagram component) {
 		return dialogs.showInput(
 				dialogTitle(component),
 				"Nombre",
 				component.getName(),
 				validatorFactory.validatorForRenameInParent(
 						component.getParent(), component));
 	}
 
 	private String dialogTitle(final GraficInterfaceDiagram component) {
		return "Interfaz Gráfica " + component.getQualifiedName();
 	}
 }
