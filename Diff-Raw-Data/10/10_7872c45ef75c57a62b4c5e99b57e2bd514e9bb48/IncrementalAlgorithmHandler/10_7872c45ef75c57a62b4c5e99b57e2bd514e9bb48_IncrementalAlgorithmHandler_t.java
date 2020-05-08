 /**
  * 
  */
 package org.feature.cluster.model.editor.editors.algorithms;
 
 import java.util.List;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.feature.cluster.model.cluster.GroupModel;
 import org.feature.cluster.model.editor.editors.ClusterMultiPageEditor;
 import org.feature.cluster.model.editor.editors.View;
 import org.feature.cluster.model.editor.editors.ViewCreater;
 import org.featuremapper.models.feature.Feature;
 import org.featuremapper.models.featuremapping.FeatureMappingModel;
 import org.featuremapper.models.featuremapping.Mapping;
 import org.featuremapper.models.featuremapping.SolutionModelRef;
 
 /**
  * Handler of the Incremental Algorithm
  * 
  * @author Tim Winkelmann
  * 
  */
 public class IncrementalAlgorithmHandler extends AbstractHandler {
 
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
       IWorkbench workbench = PlatformUI.getWorkbench();
       if (workbench != null) {
          IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
          if (activeWindow != null) {
             IWorkbenchPage page = activeWindow.getActivePage();
             if (page != null) {
                IEditorPart activeEditor = page.getActiveEditor();
 
                if (activeEditor instanceof ClusterMultiPageEditor) {
                   ClusterMultiPageEditor multiPageEditor = (ClusterMultiPageEditor) activeEditor;
                   Resource mapping = multiPageEditor.getMappingResource();
                   if (mapping != null) {
                      EList<EObject> contents = mapping.getContents();
                      FeatureMappingModel featureMappingModel = null;
                      for (EObject eObject : contents) {
                         if (eObject instanceof FeatureMappingModel) {
                            featureMappingModel = (FeatureMappingModel) eObject;
                            break;
                         }
                      }
                      EList<SolutionModelRef> solutionModels = featureMappingModel.getSolutionModels();
                      GroupModel groupModel = null;
                      for (SolutionModelRef solutionModelRef : solutionModels) {
                         EObject value = solutionModelRef.getValue();
                         if (value instanceof GroupModel) {
                            groupModel = (GroupModel) value;
                            break;
                         }
                      }
                      EList<Feature> allFeatures = featureMappingModel.getFeatureModel().getValue().getAllFeatures();
                      EList<Mapping> mappings = featureMappingModel.getMappings();
 
                      List<View> views = new ViewCreater(allFeatures, mappings, groupModel.getCoreGroup(), featureMappingModel.getFeatureModel().getValue()).getViews();
                     IncrementalAlgorithm algorithm = new IncrementalAlgorithm(views, groupModel, featureMappingModel.getFeatureModel().getValue());
                     algorithm.run();
                   }
                }
             }
          }
       }
       return null; // No return value needed.
    }
 }
