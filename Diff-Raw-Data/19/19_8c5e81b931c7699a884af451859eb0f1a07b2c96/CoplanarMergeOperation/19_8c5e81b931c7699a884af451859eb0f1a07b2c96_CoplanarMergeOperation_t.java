 package net.untoldwind.moredread.model.op.merge.coplanar;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
import net.untoldwind.moredread.model.mesh.FaceId;
 import net.untoldwind.moredread.model.mesh.IFace;
 import net.untoldwind.moredread.model.mesh.IMesh;
 import net.untoldwind.moredread.model.mesh.Mesh;
 import net.untoldwind.moredread.model.op.IUnaryOperation;
 
 public class CoplanarMergeOperation implements IUnaryOperation {
 
 	@Override
	public Mesh<?, ?> perform(final IMesh mesh) {
		final Set<FaceId> visitedFaces = new HashSet<FaceId>();
 
 		for (final IFace face : mesh.getFaces()) {
 			if (visitedFaces.contains(face.getIndex())) {
 				continue;
 			}
 
			final List<FaceId> coplanarFaces = new ArrayList<FaceId>();
 
 			collectCoplanarNeigbours(face, coplanarFaces, visitedFaces);
 		}
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	private void collectCoplanarNeigbours(final IFace face,
			final List<FaceId> coplanarFaces, final Set<FaceId> visitedFaces) {
 		for (final IFace neighbour : face.getNeighbours()) {
 
 		}
 	}
 
 }
