 package net.untoldwind.moredread.ui.tools.utilities;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import net.untoldwind.moredread.model.mesh.Mesh;
 import net.untoldwind.moredread.model.op.IUnaryOperation;
 import net.untoldwind.moredread.model.op.UnaryOperationFactory;
 import net.untoldwind.moredread.model.op.UnaryOperationFactory.Implementation;
 import net.untoldwind.moredread.model.scene.INode;
 import net.untoldwind.moredread.model.scene.MeshNode;
 import net.untoldwind.moredread.model.scene.Scene;
 import net.untoldwind.moredread.model.scene.SceneSelection;
 import net.untoldwind.moredread.ui.controls.IModelControl;
 import net.untoldwind.moredread.ui.tools.IDisplaySystem;
 import net.untoldwind.moredread.ui.tools.spi.IToolHandler;
 
 public class MergeCoplanarToolHandler implements IToolHandler {
 
 	@Override
 	public boolean activate(final Scene scene) {
 		final SceneSelection sceneSelection = scene.getSceneSelection();
 
 		final List<MeshNode> meshNodes = new ArrayList<MeshNode>();
 
 		for (final INode node : sceneSelection.getSelectedNodes()) {
 			if (node instanceof MeshNode) {
 				meshNodes.add((MeshNode) node);
 			}
 		}
 
 		scene.getSceneChangeHandler().begin(true);
 
 		try {
 			final IUnaryOperation mergeOperation = UnaryOperationFactory
 					.createOperation(Implementation.COPLANAR_MERGE);
 
 			for (final MeshNode meshNode : meshNodes) {
				final Mesh<?> mesh = meshNode.getEditableGeometry();
 
 				meshNode.setMesh(mergeOperation.perform(mesh));
 			}
 		} finally {
 			scene.getSceneChangeHandler().commit();
 		}
 
 		return false;
 	}
 
 	@Override
 	public List<? extends IModelControl> getModelControls(final Scene scene,
 			final IDisplaySystem displaySystem) {
 		return Collections.emptyList();
 	}
 
 }
