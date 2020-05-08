 package cz.cvut.fit.mirun.lemavm.core.memory;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import cz.cvut.fit.mirun.lemavm.core.VMInterpreter;
 import cz.cvut.fit.mirun.lemavm.core.VMSettings;
 import cz.cvut.fit.mirun.lemavm.core.memory.VMMemoryManager.WhichSpace;
 import cz.cvut.fit.mirun.lemavm.structures.ObjectType;
 import cz.cvut.fit.mirun.lemavm.structures.VMArray;
 import cz.cvut.fit.mirun.lemavm.structures.VMObject;
 import cz.cvut.fit.mirun.lemavm.structures.builtin.VMNull;
 import cz.cvut.fit.mirun.lemavm.structures.classes.VMClassInstance;
 import cz.cvut.fit.mirun.lemavm.structures.classes.VMEnvironment;
 import cz.cvut.fit.mirun.lemavm.utils.VMUtils;
 
 public final class GenerationalGarbageCollector extends VMGarbageCollector {
 
 	private VMObject[] fromSpace;
 	private VMObject[] toSpace;
 	private VMObject[] oldSpace;
 	private int freeInd;
 
 	private int oldFreeInd;
 
 	private Set<Entry<String, VMObject>> rememberedSet;
 
 	private byte ageThreshold;
 
 	private int oldSpaceGCThreshold;
 	private MarkAndSweepCollector oldSpaceCollector;
 
 	private Method getEntryMethod;
 
 	public GenerationalGarbageCollector(VMMemoryManager manager) {
 		super(manager);
 		rememberedSet = new HashSet<>();
 		final Byte ageT = (Byte) VMSettings.get(VMSettings.TENURE_AGE);
 		this.ageThreshold = ageT.byteValue();
 		// Old space threshold is 2/3 of its size
 		this.oldSpaceGCThreshold = manager.oldSpace.length / 3 * 2;
 		this.oldSpaceCollector = new MarkAndSweepCollector();
 		try {
 			this.getEntryMethod = HashMap.class.getDeclaredMethod("getEntry",
 					Object.class);
 			getEntryMethod.setAccessible(true);
 		} catch (NoSuchMethodException | SecurityException e) {
 			e.printStackTrace();
 			this.getEntryMethod = null;
 		}
 	}
 
 	@Override
 	protected void runGC() {
 		if (LOG.isDebugEnabled()) {
 			LOG.debug("Running garbage collection.");
 		}
 		WhichSpace which = manager.which;
 		if (which.equals(WhichSpace.FIRST)) {
 			this.fromSpace = manager.heapOne;
 			this.toSpace = manager.heapTwo;
 		} else {
 			this.fromSpace = manager.heapTwo;
 			this.toSpace = manager.heapOne;
 		}
 		this.oldSpace = manager.oldSpace;
 		final Set<Entry<String, VMObject>> rootSet = resolveRootSet();
 		moveRootsToNewSpace(rootSet);
 		int scanInd = 0;
 		while (scanInd < freeInd) {
 			scanObject(toSpace[scanInd]);
 			scanInd++;
 		}
 		scanInd = 0;
 		while (scanInd < oldFreeInd) {
 			scanObject(oldSpace[scanInd]);
 			scanInd++;
 		}
 		// Flip the spaces
 		manager.flipSpaces();
 		if (LOG.isDebugEnabled()) {
 			LOG.debug("Garbage collection finished. Garbage collector reclaimed "
 					+ (manager.heapPtr - freeInd) + " memory cells.");
 		}
 		manager.heapPtr = freeInd;
 		manager.oldSpacePtr = oldFreeInd;
 		if (oldFreeInd > oldSpaceGCThreshold) {
 			oldSpaceCollector.runGC(rootSet);
 		}
 		cleanUp();
 	}
 
 	protected void addEntryToRememberedSet(VMEnvironment env, String name,
 			VMObject value) {
 		if (env.getOwner().getHeader().getAge() > ageThreshold
 				&& value.getHeader().getAge() <= ageThreshold) {
 			// OldSpace->newSpace reference, add to remembered set
 			HashMap<String, VMObject> bindings = (HashMap<String, VMObject>) env
 					.getBindings();
 			try {
 				@SuppressWarnings("unchecked")
 				Entry<String, VMObject> e = (Entry<String, VMObject>) getEntryMethod
 						.invoke(bindings, name);
 				rememberedSet.add(e);
 			} catch (IllegalAccessException | IllegalArgumentException
 					| InvocationTargetException e) {
 				LOG.error("Unable to get entry from environment.", e);
 			}
 		}
 		// Otherwise don't add
 	}
 
 	private void cleanUp() {
 		freeInd = 0;
 		this.fromSpace = null;
 		this.toSpace = null;
 	}
 
 	private Set<Entry<String, VMObject>> resolveRootSet() {
 		final List<VMEnvironment> envs = VMInterpreter.getInstance()
 				.getEnvironments();
 		final Set<Entry<String, VMObject>> roots = new HashSet<>();
 		for (VMEnvironment e : envs) {
 			roots.addAll(e.getBindings().entrySet());
 		}
 		roots.addAll(rememberedSet);
 		return roots;
 	}
 
 	private void moveRootsToNewSpace(Set<Entry<String, VMObject>> rootSet) {
 		for (Entry<String, VMObject> o : rootSet) {
 			VMObject ob = o.getValue();
 			if (ob == VMNull.getInstance() && rememberedSet.contains(o)) {
 				// Remove this entry from the remembered set, it is no longer
 				// valid
 				// This will be used by arrays
 				rememberedSet.remove(o);
 				continue;
 			}
 			VMObject moved = moveObject(ob);
 			if (moved != null) {
 				if (moved.getHeader().getAge() > ageThreshold
 						&& rememberedSet.contains(o)) {
 					// This means that an oldspace->newspace relationship has
 					// changed to oldspace->oldspace
 					rememberedSet.remove(o);
 				}
 				o.setValue(moved);
 			}
 		}
 	}
 
 	private void scanObject(VMObject object) {
 		if (LOG.isTraceEnabled()) {
 			LOG.trace("Scanning object " + object.getHeader().getId());
 		}
 		if (object.getType().equals(ObjectType.STRING)
 				|| object.getType().equals(ObjectType.FILE)) {
 			// Strings and files don't contain any references
 			return;
 		}
 		if (object.getType().equals(ObjectType.ARRAY)) {
 			// The object is an array, check for element types
 			final VMArray arr = (VMArray) object;
 			if (!VMUtils.isTypePrimitive(arr.getElementTypeName())) {
 				final VMObject[] elems = (VMObject[]) arr.getAll();
 				for (int i = 0; i < elems.length; i++) {
 					VMObject moved = moveObject(elems[i]);
 					if (moved != null) {
 						elems[i] = moved;
 						if (moved.getHeader().getAge() < ageThreshold
 								&& object.getHeader().getAge() > ageThreshold) {
 							rememberedSet
 									.add(new GCArrayEntry(elems, i, moved));
 						}
 					}
 				}
 			}
 			// Otherwise do nothing, primitives are stored in the array and
 			// those are not allocated on our heap
 		} else {
 			// Look for reference type field values
 			VMClassInstance inst = (VMClassInstance) object;
 			final Set<Entry<String, VMObject>> refs = inst.getEnvironment()
 					.getBindings().entrySet();
 			for (Entry<String, VMObject> o : refs) {
 				VMObject moved = moveObject(o.getValue());
 				if (moved != null) {
 					o.setValue(moved);
 					if (moved.getHeader().getAge() < ageThreshold
 							&& object.getHeader().getAge() > ageThreshold) {
 						// OldSpace->newSpace reference, add to remembered set
 						rememberedSet.add(o);
 					}
 				}
 			}
 		}
 	}
 
 	private VMObject moveObject(VMObject object) {
 		if (object.getType().equals(ObjectType.NULL)
 				|| object.getHeader().getAge() > ageThreshold) {
 			// Null does not have to be moved, it cannot be collected
 			// If object's age is bigger than the threshold, it has already been
 			// moved to oldspace
 			return null;
 		}
 		int oldPtr = object.getHeader().getHeapPtr();
 		if (fromSpace[oldPtr] == null
 				|| fromSpace[oldPtr].getType().equals(
 						ObjectType.FORWARD_POINTER)) {
 			// The object has already been moved
 			final ForwardPointer ptr = (ForwardPointer) fromSpace[oldPtr];
 			return ptr.isTenured() ? oldSpace[ptr.getPointer()] : toSpace[ptr
 					.getPointer()];
 		}
 		// Move the reference
 		final VMObject clone = object.clone();
 		clone.getHeader().incrementAge();
 		boolean tenured = clone.getHeader().getAge() > ageThreshold;
 		if (tenured) {
 			oldSpace[oldFreeInd] = clone;
 			// set forward pointer
 			fromSpace[object.getHeader().getHeapPtr()] = new ForwardPointer(
 					oldFreeInd, tenured);
 			object.getHeader().setHeapPtr(oldFreeInd);
 			oldFreeInd++;
 			if (LOG.isTraceEnabled()) {
 				LOG.trace("Object tenured. Old space heap size = " + oldFreeInd);
 			}
 		} else {
 			toSpace[freeInd] = clone;
 			// set forward pointer
 			fromSpace[object.getHeader().getHeapPtr()] = new ForwardPointer(
 					freeInd, tenured);
 			object.getHeader().setHeapPtr(freeInd);
 			freeInd++;
 		}
 		return clone;
 	}
 
 	/**
 	 * Entry for the remembered set. Keeps track of its owner so that when the
 	 * element is moved, reference to it is updated in the array.
 	 * 
 	 * @author kidney
 	 * 
 	 */
 	private static final class GCArrayEntry implements
 			Map.Entry<String, VMObject> {
 
 		private final VMObject[] arr;
 		private final int index;
 		private VMObject orig;
 
 		GCArrayEntry(VMObject[] elems, int index, VMObject orig) {
 			this.arr = elems;
 			this.index = index;
 			this.orig = orig;
 		}
 
 		@Override
 		public String getKey() {
 			return null; // Not supported
 		}
 
 		@Override
 		public VMObject getValue() {
 			if (orig != arr[index]) {
 				return VMNull.getInstance();
 			}
 			return arr[index];
 		}
 
 		@Override
 		public VMObject setValue(VMObject value) {
 			this.orig = arr[index];
 			arr[index] = value;
 			return null;
 		}
 	}
 
 	/**
 	 * Entry for mark and compact old space collector.
 	 * 
 	 * @author kidney
 	 * 
 	 */
 	private static final class GCEntry implements Map.Entry<String, VMObject> {
 
 		private VMObject o;
 
 		public GCEntry(VMObject o) {
 			this.o = o;
 		}
 
 		@Override
 		public String getKey() {
 			return null; // Not supported
 		}
 
 		@Override
 		public VMObject getValue() {
 			return o;
 		}
 
 		@Override
 		public VMObject setValue(VMObject value) {
 			final VMObject toRet = o;
 			this.o = value;
 			return toRet;
 		}
 	}
 
 	/**
 	 * Mark and compact collector for the old space.
 	 * 
 	 * @author kidney
 	 * 
 	 */
 	private final class MarkAndSweepCollector {
 
 		private static final byte age = 100;
 
 		private VMObject[] heap;
 		private int heapPtr;
 		private Set<Entry<String, VMObject>> rootSet;
 
 		void runGC(Set<Entry<String, VMObject>> initialRootSet) {
 			init();
 			mark();
 			sweep();
 		}
 
 		private void init() {
 			this.heap = GenerationalGarbageCollector.this.oldSpace;
 			this.heapPtr = GenerationalGarbageCollector.this.oldFreeInd;
 			// Add objects from toSpace to the root set
 			for (VMObject o : GenerationalGarbageCollector.this.toSpace) {
 				rootSet.add(new GCEntry(o));
 			}
 		}
 
 		private void mark() {
 			for (Entry<String, VMObject> e : rootSet) {
 				VMObject obj = e.getValue();
 				if (obj.getHeader().getAge() != age) {
 					if (obj.getHeader().getAge() > ageThreshold) {
 						heap[obj.getHeader().getHeapPtr()].getHeader().setAge(
 								age);
 					}
 					scanObject(obj);
 				}
 			}
 		}
 
 		private void scanObject(VMObject object) {
 			if (object.getType().equals(ObjectType.STRING)
 					|| object.getType().equals(ObjectType.FILE)) {
 				// Strings and files don't contain any references
 				return;
 			}
 			if (object.getType().equals(ObjectType.ARRAY)) {
 				// The object is an array, check for element types
 				final VMArray arr = (VMArray) object;
 				if (!VMUtils.isTypePrimitive(arr.getElementTypeName())) {
 					final VMObject[] elems = (VMObject[]) arr.getAll();
 					for (int i = 0; i < elems.length; i++) {
 						if (elems[i].getHeader().getAge() != age) {
 							if (elems[i].getHeader().getAge() > ageThreshold) {
 								heap[elems[i].getHeader().getHeapPtr()]
 										.getHeader().setAge(age);
 							}
 							scanObject(elems[i]);
 						}
 					}
 				}
 				// Otherwise do nothing, primitives are stored in the array and
 				// those are not allocated on our heap
 			} else {
 				// Look for reference type field values
 				VMClassInstance inst = (VMClassInstance) object;
 				final Set<Entry<String, VMObject>> refs = inst.getEnvironment()
 						.getBindings().entrySet();
 				for (Entry<String, VMObject> e : refs) {
 					final VMObject obj = e.getValue();
 					if (obj.getHeader().getAge() != age) {
 						if (obj.getHeader().getAge() > ageThreshold) {
 							heap[obj.getHeader().getHeapPtr()].getHeader()
 									.setAge(age);
 						}
 						scanObject(obj);
 					}
 				}
 			}
 		}
 
 		private void sweep() {
 			for (int i = 0; i < heapPtr; i++) {
 				if (heap[i].getHeader().getAge() != age) {
 					// Reclaim the memory
 					heap[i] = null;
 				} else {
 					heap[i].getHeader().setAge((byte) 3);
 				}
 			}
 			compact();
 		}
 
 		private void compact() {
 			int freePtr = -1;
 			for (int i = 0; i < heapPtr; i++) {
 				if (heap[i] == null) {
 					freePtr = i;
 				} else if (freePtr != -1) {
 					heap[freePtr] = heap[i];
 					heap[freePtr].getHeader().setHeapPtr(freePtr);
 					heap[i] = null;
 					freePtr = -1;
 				}
 			}
 		}
 	}
 }
