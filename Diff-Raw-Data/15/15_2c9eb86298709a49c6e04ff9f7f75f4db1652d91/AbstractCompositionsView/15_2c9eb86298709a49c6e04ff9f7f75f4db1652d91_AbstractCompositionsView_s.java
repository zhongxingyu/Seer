 /**
  * Copyright Universite Joseph Fourier (www.ujf-grenoble.fr)
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package fr.liglab.adele.cilia.workbench.designer.view.abstractcompositionsview;
 
 import java.util.List;
 
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IEditorReference;
 
import fr.liglab.adele.cilia.workbench.designer.parser.abstractcompositions.Chain;
 import fr.liglab.adele.cilia.workbench.designer.parser.abstractcompositions.AbstractCompositionFile;
 import fr.liglab.adele.cilia.workbench.designer.parser.abstractcompositions.AbstractCompositionModel;
 import fr.liglab.adele.cilia.workbench.designer.service.abstractcompositionsservice.AbstractCompositionsRepoService;
 import fr.liglab.adele.cilia.workbench.designer.service.abstractreposervice.Changeset;
 import fr.liglab.adele.cilia.workbench.designer.service.abstractreposervice.Changeset.Operation;
 import fr.liglab.adele.cilia.workbench.designer.view.repositoryview.RepositoryView;
 
 /**
  * DsciliaRepositoryView.
  * 
  * @author Etienne Gandrille
  */
 public class AbstractCompositionsView extends RepositoryView<AbstractCompositionFile, AbstractCompositionModel> {
 
 	public final static String VIEW_ID = "fr.liglab.adele.cilia.workbench.designer.view.abstractcompositionsview";
 
 	public AbstractCompositionsView() {
 		super(AbstractCompositionsRepoService.getInstance());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * fr.liglab.adele.cilia.workbench.designer.repositoryview.RepositoryView
 	 * #createPartControl(org.eclipse.swt.widgets.Composite)
 	 */
 	@Override
 	public void createPartControl(Composite parent) {
 		super.createPartControl(parent);
 
 		viewer.setLabelProvider(new DsciliaLabelProvider());
 
 		// TreeViewer listener
 		viewer.addDoubleClickListener(new IDoubleClickListener() {
 			@Override
 			public void doubleClick(DoubleClickEvent event) {
 				openFileEditor(event);
 			}
 		});
 
 		// Linking already opened editors for getting save notifications
 		for (IEditorReference editor : getRelevantFileStoreEditors(".dscilia"))
 			addEditorSavedListener(editor.getPart(true));
 	}
 
 	/**
 	 * Called when DScilia repository changes. Refresh view if needed.
 	 * 
 	 * @param changes
 	 *            the changes
 	 */
 	@Override
 	public void repositoryContentUpdated(List<Changeset> changes) {
 		for (Changeset change : changes) {
 			Object object = change.getObject();
 			Operation operation = change.getOperation();
 			if (operation != Operation.UPDATE) {
 				if (object instanceof AbstractCompositionFile || object instanceof Chain) {
 					refresh();
 					return;
 				}
 			} else {
 				if (object instanceof AbstractCompositionFile) {
 					refresh();
 					return;
 				}
 			}
 		}
 	}
 }
