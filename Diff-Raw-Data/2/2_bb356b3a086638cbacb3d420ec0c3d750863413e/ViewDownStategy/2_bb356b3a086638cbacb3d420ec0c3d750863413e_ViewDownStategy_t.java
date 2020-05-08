 package org.robotlegs.plugins.upDown.strategy;
 
 import com.intellij.lang.javascript.psi.ecmal4.JSClass;
 import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
 import com.intellij.openapi.actionSystem.AnActionEvent;
 import com.intellij.openapi.actionSystem.DataKeys;
 import com.intellij.openapi.actionSystem.Presentation;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiReference;
 import com.intellij.psi.search.GlobalSearchScope;
 import com.intellij.psi.search.searches.ReferencesSearch;
 import org.robotlegs.plugins.util.ActionEventUtil;
 import org.robotlegs.plugins.util.JSFileUtil;
 import org.robotlegs.plugins.util.JSMemberUtil;
 
 import java.util.Collection;
 import java.util.ResourceBundle;
 
 /**
  * Created by IntelliJ IDEA. User: rasheed Date: 31/03/2010 Time: 8:15:19 AM To change this template use File | Settings
  * | File Templates.
  */
 public class ViewDownStategy implements IUpDownStrategy {
 
 	protected ResourceBundle resourceBundle = ResourceBundle.getBundle("org.robotlegs.plugins.UpDownPluginBundle");
 	
 	public void update(AnActionEvent event) {
 		Presentation presentation = event.getPresentation();
 
 		presentation.setEnabled(true);
 		presentation.setText(resourceBundle.getString("view.down"));
 	}
 
 	public void perform(AnActionEvent event) {
 		JSClass clazz = JSFileUtil.getJSClass(event);
 
 		if (clazz == null)
 			return;
 
 		GlobalSearchScope scope = JSResolveUtil.getSearchScopeWithPredefined(event.getData(DataKeys.PROJECT));
 
 		Collection<PsiReference> references = ReferencesSearch.search(clazz,scope,false).findAll();
 
 		// for now, all we hunt for is mediators, and of those mediators, only those that specify this view.
 		// these are most likely what we're looking for.
 
 		JSClass candidateClass;
 
 		for (PsiReference reference : references) {
 //			System.out.println("*** Ref Found ***");
 //			System.out.println("Text Range: " + reference.getRangeInElement().toString());
 //			System.out.println("Element File: " + reference.getElement().getContainingFile().getName());
 
 			candidateClass = JSFileUtil.getJSClass(reference.getElement().getContainingFile());
 
 			if ((candidateClass != null) && directlyMediatedBy(clazz, candidateClass)) {
 				ActionEventUtil.getIdeView(event).selectElement(candidateClass);
 				break;
 			}
 
 		}
 
 	}
 
 	private static boolean directlyMediatedBy(JSClass viewClass, JSClass candidateClass) {
 		PsiElement viewMember = JSMemberUtil.getTypeByAccessorName("view", candidateClass);
 		
 		if (viewMember == null)
 			return false;
 
		return viewMember.isEquivalentTo(viewClass);
 
 	}
 }
