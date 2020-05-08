 package sabazios.domains;
 
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import sabazios.A;
 
 import com.ibm.wala.analysis.pointers.HeapGraph;
 import com.ibm.wala.ipa.callgraph.CGNode;
 import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
 import com.ibm.wala.util.collections.SparseVector;
 
 public class PointerForValue {
 	private boolean computed = false;
 
	private Map<CGNode, SparseVector<LocalPointerKey>> pointedObjects;
 //	private HashMap<IClass, ConcreteTypeKey> classToInstanceKey = new HashMap<IClass, ConcreteTypeKey>(); 
 
 	public void compute(HeapGraph heapGraph) {
		pointedObjects = new LinkedHashMap<CGNode, SparseVector<LocalPointerKey>>();
 		Iterator<Object> iterator = heapGraph.iterator();
 		while (iterator.hasNext()) {
 			Object ik = iterator.next();
 			if (ik instanceof LocalPointerKey) {
 				LocalPointerKey pk = (LocalPointerKey) ik;
 				CGNode n = pk.getNode();
 				int v = pk.getValueNumber();
				if (!pointedObjects.containsKey(n))
					pointedObjects.put(n, new SparseVector<LocalPointerKey>());
				pointedObjects.get(n).set(v, pk);
 			}
 //			if(ik instanceof ConcreteTypeKey) {
 //				ConcreteTypeKey ctk = (ConcreteTypeKey) ik;
 //				IClass concreteType = ctk.getConcreteType();
 //				classToInstanceKey.put(concreteType, ctk);
 //			}
 		}
 		this.computed = true;
 	}
 
 	public LocalPointerKey get(CGNode n, int v) {
 		if (!computed)
 			throw new RuntimeException("Not computed yet");
 
		SparseVector<LocalPointerKey> pks = pointedObjects.get(n);
 		if (pks == null)
 			return null;
 		return pks.get(v);
 	}
 	
 //	public ConcreteTypeKey get(IClass c) {
 //		return classToInstanceKey.get(c);
 //	}
 }
