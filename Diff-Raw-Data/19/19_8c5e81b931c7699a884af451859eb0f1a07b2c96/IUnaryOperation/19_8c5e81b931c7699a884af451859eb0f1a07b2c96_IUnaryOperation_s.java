 package net.untoldwind.moredread.model.op;
 
 import net.untoldwind.moredread.model.mesh.IMesh;
 import net.untoldwind.moredread.model.mesh.Mesh;
 
 public interface IUnaryOperation {
	Mesh<?> perform(IMesh mesh);
 }
