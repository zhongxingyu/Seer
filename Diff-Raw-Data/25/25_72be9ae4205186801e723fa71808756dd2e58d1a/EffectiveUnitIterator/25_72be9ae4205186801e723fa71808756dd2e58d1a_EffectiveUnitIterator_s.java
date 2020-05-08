 package org.eclipse.b3.build.core;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.eclipse.b3.backend.core.ParentContextIterator;
 import org.eclipse.b3.backend.evaluator.b3backend.BContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BExecutionContext;
 import org.eclipse.b3.build.BuildUnit;
 
 /**
  * Iterates over the effective "horizon" of build units (advised units have been supplanted).
  * 
  */
 public class EffectiveUnitIterator implements Iterator<BuildUnit> {
 
 	private Map<Class<? extends BuildUnit>, BuildUnit> unitStore = new HashMap<Class<? extends BuildUnit>, BuildUnit>();
 
 	private Iterator<BuildUnit> itor;
 
 	public EffectiveUnitIterator(BExecutionContext ctx) {
 		ParentContextIterator pitor = new ParentContextIterator(ctx, BContext.class);
 		if(pitor.hasNext())
 			collectUnits(pitor.next(), pitor);
 		itor = unitStore.values().iterator();
 	}
 
 	@SuppressWarnings("unchecked")
 	private void collectUnits(BExecutionContext ctx, Iterator<BExecutionContext> pitor) {
 		if(pitor.hasNext())
 			collectUnits(pitor.next(), pitor);
 		Map<Object, Object> m = ctx.getMapOfThings(BuildUnit.class);
 		// unitStore.putAll(((BuildContextImpl) ctx).getBuildUnitStore());
 		for(Entry<Object, Object> entry : m.entrySet())
 			unitStore.put((Class<? extends BuildUnit>) entry.getKey(), (BuildUnit) entry.getValue());
 	}
 
 	public boolean hasNext() {
 		return itor.hasNext();
 	}
 
 	public Iterator<BuildUnit> iterator() {
 		return itor;
 	}
 
 	public BuildUnit next() {
 		return itor.next();
 	}
 
 	public void remove() {
 		throw new UnsupportedOperationException();
 	}

 }
