 package org.eclipse.emf.refactor.smells.papyrus.managers;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.refactor.smells.runtime.core.EObjectGroup;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.DecorationEditPolicy.DecoratorTarget;
 import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecorator;
 import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorTarget;
 import org.eclipse.gmf.runtime.notation.Diagram;
 import org.eclipse.gmf.runtime.notation.View;
 
 
 public class HighlightManager {
 	
 	private static HighlightManager instance = null;	
 	private List<EObject> selected; 
 	private List<IDecorator> decorators = null;
 	private Map<EObject, IDecoratorTarget> decoratorTargets = null;
 	
 	private HighlightManager() {
 		selected = new ArrayList<EObject>();
 		decoratorTargets = new HashMap<EObject, IDecoratorTarget>();
 		decorators = new ArrayList<IDecorator>();
 	}
 	
 	public static HighlightManager getInstance() {
 		if(instance == null) {
 			instance = new HighlightManager();
 		}
 		return instance;
 	}
 	
 	public void highlight(Object selection) {
 		// clear former selected eObjects
 		selected.clear();
 		// set selected eObjects from selection
 		if (selection instanceof EObjectGroup) {			
 			selected.addAll(((EObjectGroup) selection).getEObjects());
 			System.out.println("Highlight ...");
 		}
 		if (selection instanceof EObject) {
 			selected.add((EObject) selection);
 			System.out.println("Highlight ...");
 		}
 		for (EObject eo : selected) {
 			System.out.println("EObject: " + eo.toString());
 		}
 		// refresh each corresponding decorator
 		for (IDecorator decorator : decorators) {
 			System.out.println("Call refresh ...");
 			decorator.refresh();
 		}
 	}
 
 	public void setComponents(Object o) {
 		System.out.println("Do something ...");
 	}
 
 	public void registerDecorator(IDecorator decorator, DecoratorTarget decoratorTarget) {
 		System.out.println("Register decorator; decorator: " + decorator.toString());
 		System.out.println("Register decorator; decoratorTarget: " + decoratorTarget.toString());
 		View view = (View) decoratorTarget.getAdapter(View.class);
 
 		if(decoratorTargets.containsKey(view.getElement())){
 			View parent = view;
 			boolean topMost = false;
 			if(!(parent instanceof Diagram)){
 				while(!((parent = (View) parent.eContainer()) instanceof Diagram)){
 					if(parent == decoratorTargets.get(view.getElement()).getAdapter(View.class)){
 						topMost = true;
 					}
 				}
 				if(!topMost){
 					decoratorTargets.put(view.getElement(), decoratorTarget);
 				}
 			}
 		} else {
 			decoratorTargets.put(view.getElement(), decoratorTarget);
 		}
 
 		decorators.add(decorator);
 	}
 
 	public void unregisterDecorator(IDecorator decorator) {
 		System.out.println("Unregister decorator; decorator: " + decorator.toString());
 		decorators.remove(decorator);
 	}
 
 	public IDecoratorTarget getPrefferedDecoratorTarget(EObject element) {
 		return decoratorTargets.get(element);
 	}
 
 	public List<EObject> getSelected() {
 		return selected;
 	}
 		
 }
