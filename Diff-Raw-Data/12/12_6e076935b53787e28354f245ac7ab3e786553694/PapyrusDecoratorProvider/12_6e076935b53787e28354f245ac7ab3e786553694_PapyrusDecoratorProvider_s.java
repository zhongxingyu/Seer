 package org.eclipse.emf.refactor.smells.papyrus.ui;
 
 import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
 import org.eclipse.gmf.runtime.common.core.service.IOperation;
 import org.eclipse.gmf.runtime.diagram.ui.services.decorator.CreateDecoratorsOperation;
 import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorProvider;
 import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorTarget;
 
 public class PapyrusDecoratorProvider extends AbstractProvider implements IDecoratorProvider {
 	
	public static final String PAPYRUS_DECORATOR_KEY = "papyrus_decorator";
 
 	@Override
 	public boolean provides(IOperation operation) {
 		return (operation instanceof CreateDecoratorsOperation);
 	}
 
 	@Override
 	public void createDecorators(IDecoratorTarget decoratorTarget) {
 		decoratorTarget.installDecorator(PAPYRUS_DECORATOR_KEY, new PapyrusDecorator(decoratorTarget));
 	}
 
 }
