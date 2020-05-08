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
 package fr.liglab.adele.cilia.workbench.designer.view.specrepositoryview;
 
 import java.util.List;
 
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IEditorReference;
 
 import fr.liglab.adele.cilia.workbench.common.view.ViewUtil;
 import fr.liglab.adele.cilia.workbench.designer.parser.spec.MediatorSpec;
 import fr.liglab.adele.cilia.workbench.designer.parser.spec.SpecFile;
 import fr.liglab.adele.cilia.workbench.designer.parser.spec.SpecModel;
 import fr.liglab.adele.cilia.workbench.designer.service.abstractreposervice.Changeset;
 import fr.liglab.adele.cilia.workbench.designer.service.specreposervice.SpecRepoService;
 import fr.liglab.adele.cilia.workbench.designer.view.repositoryview.RepositoryView;
 
 /**
  * SpecRepositoryView.
  * 
  * @author Etienne Gandrille
  */
 public class SpecRepositoryView extends RepositoryView<SpecFile, SpecModel> {
 
 	public final static String VIEW_ID = "fr.liglab.adele.cilia.workbench.designer.view.specrepositoryview";
 
 	/**
 	 * Instantiates a new spec repository view.
 	 */
 	public SpecRepositoryView() {
 		super(SpecRepoService.getInstance());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * fr.liglab.adele.cilia.workbench.designer.view.repositoryview.RepositoryView
 	 * #createPartControl(org.eclipse.swt .widgets.Composite)
 	 */
 	@Override
 	public void createPartControl(Composite parent) {
 		super.createPartControl(parent);
 
 		// TreeViewer listener
 		viewer.addDoubleClickListener(new IDoubleClickListener() {
 			@Override
 			public void doubleClick(DoubleClickEvent event) {
 				Object element = getFirstSelectedElement();
 
 				if (element != null) {
 					if (element instanceof SpecFile)
 						openFileEditor(event);
 					else if (element instanceof MediatorSpec)
 						editMediatorSpec(event, (MediatorSpec) element);
 				}
 			}
 		});
 
 		// Linking already opened editors for getting save notifications
 		for (IEditorReference editor : getRelevantFileStoreEditors(".xml"))
 			addEditorSavedListener(editor.getPart(true));
 
 		viewer.setLabelProvider(new SpecLabelProvider());
 		viewer.setAutoExpandLevel(3);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * fr.liglab.adele.cilia.workbench.designer.view.repositoryview.RepositoryView
 	 * #repositoryContentUpdated(fr.liglab
 	 * .adele.cilia.workbench.designer.service.abstractreposervice.Changeset[])
 	 */
 	@Override
 	public void repositoryContentUpdated(List<Changeset> changes) {
 		if (changes != null && changes.size() != 0) {
 			refresh();
 			return;
 		}
 	}
 
 	private void editMediatorSpec(DoubleClickEvent event, MediatorSpec element) {
 
 		UpdateMediatorSpecDialog dialog = new UpdateMediatorSpecDialog(ViewUtil.getShell(event), element);
 
 		if (dialog.open() == Window.OK) {
 			SpecRepoService.getInstance().updateMediatorSpec(element, dialog.getInPorts(), dialog.getOutPorts(),
 					dialog.getMediatorProperties(), dialog.getSchedulerParam(), dialog.getProcessorParam(),
 					dialog.getDispatcherParam());
 		}
 	}
 }
