 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering, Technische Universitaet Muenchen. All rights
  * reserved. This program and the accompanying materials are made available under the terms of the Eclipse Public
  * License v1.0 which accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.test.conflictDetection.merging;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.emfstore.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.client.model.changeTracking.merging.DecisionManager;
 import org.eclipse.emf.emfstore.client.model.changeTracking.merging.conflict.Conflict;
 import org.eclipse.emf.emfstore.client.model.util.EMFStoreCommand;
 import org.eclipse.emf.emfstore.client.test.conflictDetection.ConflictDetectionTest;
 import org.eclipse.emf.emfstore.common.model.ModelElementId;
 import org.eclipse.emf.emfstore.common.model.Project;
 import org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.VersioningFactory;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.AttributeOperation;
 
 /**
  * Helper super class for merge tests.
  * 
  * @author wesendon
  */
 public class MergeTest extends ConflictDetectionTest {
 
 	private MergeCase mergeCase;
 
 	/**
 	 * Default Constructor.
 	 * 
 	 * @return case helper
 	 */
 	public MergeCase newMergeCase() {
 		return newMergeCase(new EObject[0]);
 	}
 
 	public MergeCase newMergeCase(EObject... objs) {
 		mergeCase = new MergeCase();
 		mergeCase.add(objs);
 		mergeCase.ensureCopy();
 		return mergeCase;
 	}
 
 	public List<ModelElementId> getIds(EObject... objs) {
 		ArrayList<ModelElementId> result = new ArrayList<ModelElementId>();
 		for (EObject obj : objs) {
 			result.add(mergeCase.getMyId(obj));
 		}
 		return result;
 	}
 
 	public ModelElementId getId(EObject obj) {
 		return mergeCase.getMyId(obj);
 	}
 
 	public ModelElementId getMyId(EObject obj) {
 		return mergeCase.getMyId(obj);
 	}
 
 	public ModelElementId getTheirId(EObject obj) {
 		return mergeCase.getTheirId(obj);
 	}
 
 	/**
 	 * Helper class for merge tests. It manages the two projectspaces and offers covenience methods.
 	 * 
 	 * @author wesendon
 	 */
 	public class MergeCase {
 
 		private ProjectSpace theirProjectSpace;
 
 		private void add(final EObject... objs) {
 			new EMFStoreCommand() {
 				@Override
 				protected void doRun() {
 					for (EObject obj : objs) {
 						getProject().addModelElement(obj);
 					}
 				}
 			}.run(false);
 		}
 
 		public void addTheirs(final EObject... objs) {
 			new EMFStoreCommand() {
 				@Override
 				protected void doRun() {
 					for (EObject obj : objs) {
 						getTheirProject().addModelElement(obj);
 					}
 				}
 			}.run(false);
 		}
 
 		@SuppressWarnings("unchecked")
 		public <T extends EObject> T getMyItem(T id) {
 			ensureCopy();
 			return (T) getProject().getModelElement(byId(id));
 		}
 
 		public ModelElementId getMyId(EObject obj) {
 			return getProject().getModelElementId(obj);
 		}
 
 		public ModelElementId getTheirId(EObject obj) {
 			return getTheirProject().getModelElementId(getTheirItem(obj));
 		}
 
 		@SuppressWarnings("unchecked")
 		public <T extends EObject> T getTheirItem(T id) {
 			ensureCopy();
 			return (T) getTheirProject().getModelElement(byId(id));
 		}
 
 		private ModelElementId byId(EObject id) {
 			return getProject().getModelElementId(id);
 		}
 
 		public void ensureCopy() {
 			if (theirProjectSpace == null) {
 				new EMFStoreCommand() {
 					@Override
 					protected void doRun() {
 						clearOperations();
 						theirProjectSpace = cloneProjectSpace(getProjectSpace());
 					}
 				}.run(false);
 			}
 		}
 
 		public Project getTheirProject() {
 			ensureCopy();
 			return this.theirProjectSpace.getProject();
 		}
 
 		public ProjectSpace getTheirProjectSpace() {
 			ensureCopy();
 			return this.theirProjectSpace;
 		}
 
 		public DecisionManager execute() {
 			ensureCopy();
 
 			PrimaryVersionSpec spec = VersioningFactory.eINSTANCE.createPrimaryVersionSpec();
 			spec.setIdentifier(23);
 
 			DecisionManager manager = new DecisionManager(getProject(), Arrays.asList(getProjectSpace()
 				.getLocalChangePackage(true)), Arrays.asList(getTheirProjectSpace().getLocalChangePackage(true)), spec,
 				spec);
 
 			return manager;
 		}
 
 		public <T extends Conflict> MergeTestQuery hasConflict(Class<T> clazz, int expectedConflicts) {
 			MergeTestQuery query = new MergeTestQuery(execute());
 			return query.hasConflict(clazz, expectedConflicts);
 		}
 
 		public <T extends Conflict> MergeTestQuery hasConflict(Class<T> clazz) {
 			if (clazz == null) {
 				ArrayList<Conflict> conflicts = execute().getConflicts();
 				assertEquals(0, conflicts.size());
 				return null;
 			}
 			return hasConflict(clazz, 1);
 		}
 
 		public ProjectSpace getMyProjectSpace() {
 			return getProjectSpace();
 		}
 	}
 
 	public class MergeTestQuery {
 
 		private final DecisionManager manager;
 		private ArrayList<Conflict> conflicts;
 		private Object lastObject;
 		private HashSet<AbstractOperation> mySeen;
 		private HashSet<AbstractOperation> theirSeen;
 
 		public MergeTestQuery(DecisionManager manager) {
 			this.manager = manager;
 			mySeen = new HashSet<AbstractOperation>();
 			theirSeen = new HashSet<AbstractOperation>();
 		}
 
 		public <T extends Conflict> MergeTestQuery hasConflict(Class<T> clazz, int i) {
 			conflicts = manager.getConflicts();
 			assertEquals("Number of conflicts", i, conflicts.size());
 			Conflict currentConflict = currentConflict();
 			if (!clazz.isInstance(currentConflict)) {
 				throw new AssertionError("Expected: " + clazz.getName() + " but found: "
 					+ ((currentConflict == null) ? "null" : currentConflict.getClass().getName()));
 			}
 			return this;
 		}
 
 		private Conflict currentConflict() {
 			return conflicts.get(0);
 		}
 
 		@SuppressWarnings("unchecked")
 		public <T extends AbstractOperation> T getMy(Class<T> class1, int i) {
 			List<AbstractOperation> ops = currentConflict().getMyOperations();
 			assertTrue(ops.size() > i);
 			if (!class1.isInstance(ops.get(i))) {
 				throw new AssertionError("Expected: " + class1.getName() + " but found: "
 					+ ((ops.get(i) == null) ? "null" : ops.get(i).getClass().getName()));
 			}
 			return (T) ops.get(i);
 		}
 
 		@SuppressWarnings("unchecked")
 		public <T extends AbstractOperation> T getTheirs(Class<T> class1, int i) {
 			List<AbstractOperation> ops = currentConflict().getTheirOperations();
 			assertTrue(ops.size() > i);
 			assertTrue(class1.isInstance(ops.get(i)));
 			return (T) ops.get(i);
 		}
 
 		public <T extends AbstractOperation> MergeTestQuery myIs(Class<T> class1) {
 			return myIs(class1, 0);
 		}
 
 		public <T extends AbstractOperation> MergeTestQuery myIs(Class<T> class1, int index) {
 			last(true, getMy(class1, index));
 			return this;
 		}
 
 		public <T extends AbstractOperation> MergeTestQuery theirsIs(Class<T> class1) {
 			return theirsIs(class1, 0);
 		}
 
 		public <T extends AbstractOperation> MergeTestQuery theirsIs(Class<T> class1, int index) {
 			last(false, getTheirs(class1, index));
 			return this;
 		}
 
 		private void last(boolean my, AbstractOperation op) {
 			lastObject = op;
 			if (my) {
 				mySeen.add(op);
 			} else {
 				theirSeen.add(op);
 			}
 		}
 
 		public MergeTestQuery andNoOtherMyOps() {
 			HashSet<AbstractOperation> my = new HashSet<AbstractOperation>(currentConflict().getMyOperations());
 			my.removeAll(mySeen);
 			assertEquals(0, my.size());
 			return this;
 		}
 
 		public MergeTestQuery andNoOtherTheirOps() {
 			HashSet<AbstractOperation> theirs = new HashSet<AbstractOperation>(currentConflict().getTheirOperations());
 			theirs.removeAll(theirSeen);
 			assertEquals(theirs.size(), 0);
 			return this;
 		}
 
 		public MergeTestQuery andReturns(String methodName, Object b) {
 			assertTrue(lastObject != null);
 			try {
 				for (Method method : lastObject.getClass().getMethods()) {
 					if (method.getName().equals(methodName)) {
						Object invoke = method.invoke(lastObject, null);
 						assertEquals(b, invoke);
 						return this;
 					}
 				}
 			} catch (IllegalArgumentException e) {
 			} catch (IllegalAccessException e) {
 			} catch (InvocationTargetException e) {
 			}
 			throw new AssertionError("No such method");
 		}
 
 		private int myCounter = 0;
 
 		public MergeTestQuery andMyIs(Class<AttributeOperation> class1) {
 			myIs(class1, ++myCounter);
 			return this;
 		}
 	}
 }
