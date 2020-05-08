 /**
  *  Gerry AI - Open framework for automated planning algorithms
  *  Copyright (C) 2012  David Edwards
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as
  *  published by the Free Software Foundation, either version 3 of the
  *  License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of the GNU Affero General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.gerryai.htn.simple.planner.impl;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.gerryai.htn.plan.TaskNotActionable;
 import org.gerryai.htn.planner.PlanNotFound;
 import org.gerryai.htn.problem.State;
 import org.gerryai.htn.simple.constraint.ImmutableConstraint;
 import org.gerryai.htn.simple.decomposition.DecompositionService;
 import org.gerryai.htn.simple.decomposition.ImmutableSubstitution;
 import org.gerryai.htn.simple.decomposition.UnificationService;
 import org.gerryai.htn.simple.decomposition.UnifierNotFound;
 import org.gerryai.htn.simple.domain.ImmutableMethod;
 import org.gerryai.htn.simple.logic.ImmutableCondition;
 import org.gerryai.htn.simple.logic.ImmutableTerm;
 import org.gerryai.htn.simple.logic.impl.SimpleUnifier;
 import org.gerryai.htn.simple.plan.ImmutableAction;
 import org.gerryai.htn.simple.plan.ImmutableActionFactory;
 import org.gerryai.htn.simple.plan.ImmutablePlan;
 import org.gerryai.htn.simple.plan.ImmutablePlanBuilder;
 import org.gerryai.htn.simple.plan.ImmutablePlanBuilderFactory;
 import org.gerryai.htn.simple.planner.DecompositionNotFound;
 import org.gerryai.htn.simple.planner.sort.SortService;
 import org.gerryai.htn.simple.tasknetwork.ImmutableTask;
 import org.gerryai.htn.simple.tasknetwork.ImmutableTaskNetwork;
 import org.gerryai.htn.simple.tasknetwork.InvalidConstraint;
 import org.junit.Test;
 
 /**
  * @author David Edwards <david@more.fool.me.uk>
  *
  */
 public class SimplePlannerHelperTest {
 
 	@Test(expected=NonPrimitiveTaskNotFound.class)
 	public void testGetNonPrimitiveTask() throws NonPrimitiveTaskNotFound {
 		
 		ImmutableActionFactory mockActionFactory = mock(ImmutableActionFactory.class);
 		ImmutablePlanBuilderFactory mockPlanBuilderFactory = mock(ImmutablePlanBuilderFactory.class);
 		@SuppressWarnings("unchecked")
 		DecompositionService<ImmutableMethod, ImmutableTerm<?>, ImmutableTask, ImmutableTaskNetwork,
 		        ImmutableConstraint<?>, ImmutableSubstitution> mockDecompositionService
 				= mock(DecompositionService.class);
 		@SuppressWarnings("unchecked")
 		UnificationService<ImmutableMethod, ImmutableTerm<?>, ImmutableTask, ImmutableTaskNetwork,
 		        ImmutableConstraint<?>, ImmutableCondition<?>> mockUnificationService = mock(UnificationService.class);
 		@SuppressWarnings("unchecked")
 		SortService<ImmutableTerm<?>, ImmutableTask, ImmutableConstraint<?>> mockSortService
 		        = mock(SortService.class);
 
 		SimplePlannerHelper plannerHelper = new SimplePlannerHelper(mockActionFactory,
 		        mockPlanBuilderFactory, mockDecompositionService, mockUnificationService, mockSortService);
 		
 		ImmutableTaskNetwork mockTaskNetwork = mock(ImmutableTaskNetwork.class);
 		Set<ImmutableTask> tasks = new HashSet<ImmutableTask>();
 		when(mockTaskNetwork.getTasks()).thenReturn(tasks);
 		
 		// Trying to find a non-primitive task should throw an exception since there isn't one
 		plannerHelper.getNonPrimitiveTask(mockTaskNetwork);
 		
 	}
 	
 	@Test
 	public void testFindPlanForPrimitiveEmptyTaskNetwork() throws PlanNotFound {
 
 		List<ImmutableAction> mockActions = new ArrayList<ImmutableAction>();
 		ImmutablePlan mockPlan = mock(ImmutablePlan.class);
 		when(mockPlan.getActions()).thenReturn(mockActions);
 		ImmutablePlanBuilder mockPlanBuilder = mock(ImmutablePlanBuilder.class);
 		ImmutablePlanBuilderFactory mockPlanBuilderFactory = mock(ImmutablePlanBuilderFactory.class);
 		when(mockPlanBuilderFactory.createBuilder()).thenReturn(mockPlanBuilder);
 		when(mockPlanBuilder.build()).thenReturn(mockPlan);
 		ImmutableActionFactory mockActionFactory = mock(ImmutableActionFactory.class);
 		@SuppressWarnings("unchecked")
 		DecompositionService<ImmutableMethod, ImmutableTerm<?>, ImmutableTask, ImmutableTaskNetwork,
 		        ImmutableConstraint<?>, ImmutableSubstitution>
 				mockDecompositionService = mock(DecompositionService.class);
 		@SuppressWarnings("unchecked")
 		UnificationService<ImmutableMethod, ImmutableTerm<?>, ImmutableTask, ImmutableTaskNetwork,
 		        ImmutableConstraint<?>, ImmutableCondition<?>> mockUnificationService = mock(UnificationService.class);
         @SuppressWarnings("unchecked")
         SortService<ImmutableTerm<?>, ImmutableTask, ImmutableConstraint<?>> mockSortService
                 = mock(SortService.class);
         
 		SimplePlannerHelper plannerHelper = new SimplePlannerHelper(mockActionFactory,
 		        mockPlanBuilderFactory, mockDecompositionService, mockUnificationService, mockSortService);
 		
 		ImmutableTaskNetwork mockTaskNetwork = mock(ImmutableTaskNetwork.class);
 		Set<ImmutableTask> tasks = new HashSet<ImmutableTask>();
 		when(mockTaskNetwork.getTasks()).thenReturn(tasks);
 		State mockState = mock(State.class);
 		
 		// Try to find a plan
 		ImmutablePlan plan = plannerHelper.findPlanForPrimitive(mockState, mockTaskNetwork);		
 		
 		assertTrue(plan.getActions().isEmpty());
 	}
 	
 	@Test
 	public void testFindPlanForPrimitiveOneTask() throws PlanNotFound, TaskNotActionable {
 
 		List<ImmutableAction> mockActions = new ArrayList<ImmutableAction>();
 		ImmutablePlan mockPlan = mock(ImmutablePlan.class);
 		when(mockPlan.getActions()).thenReturn(mockActions);
 		ImmutablePlanBuilder mockPlanBuilderA = mock(ImmutablePlanBuilder.class);
 		ImmutablePlanBuilder mockPlanBuilderB = mock(ImmutablePlanBuilder.class);
         ImmutablePlanBuilderFactory mockPlanBuilderFactory = mock(ImmutablePlanBuilderFactory.class);
         when(mockPlanBuilderFactory.createBuilder()).thenReturn(mockPlanBuilderA);
         
 		ImmutableActionFactory mockActionFactory = mock(ImmutableActionFactory.class);
 		@SuppressWarnings("unchecked")
 		DecompositionService<ImmutableMethod, ImmutableTerm<?>, ImmutableTask, ImmutableTaskNetwork,
 		        ImmutableConstraint<?>, ImmutableSubstitution>
 				mockDecompositionService = mock(DecompositionService.class);
 		@SuppressWarnings("unchecked")
 		UnificationService<ImmutableMethod, ImmutableTerm<?>, ImmutableTask, ImmutableTaskNetwork,
 		        ImmutableConstraint<?>, ImmutableCondition<?>> mockUnificationService = mock(UnificationService.class);
         @SuppressWarnings("unchecked")
         SortService<ImmutableTerm<?>, ImmutableTask, ImmutableConstraint<?>> mockSortService
                 = mock(SortService.class);
         
 		SimplePlannerHelper plannerHelper = new SimplePlannerHelper(mockActionFactory,
 		        mockPlanBuilderFactory, mockDecompositionService, mockUnificationService, mockSortService);
 		
 		ImmutableTask mockTaskA = mock(ImmutableTask.class);
 		ImmutableTaskNetwork mockTaskNetwork = mock(ImmutableTaskNetwork.class);
 		Set<ImmutableTask> mockTasks = new HashSet<ImmutableTask>();
 		mockTasks.add(mockTaskA);
 		Set<ImmutableConstraint<?>> mockConstraints = new HashSet<ImmutableConstraint<?>>();
 		List<ImmutableTask> sortedTasks = new ArrayList<ImmutableTask>(mockTasks);
 		when(mockTaskNetwork.getTasks()).thenReturn(mockTasks);
 		when(mockTaskNetwork.getConstraints()).thenReturn(mockConstraints);
 		State mockState = mock(State.class);
 		when(mockSortService.sortByConstaints(mockTasks, mockConstraints)).thenReturn(sortedTasks);
 		
 		ImmutableAction  mockActionA = mock(ImmutableAction.class);
 		mockActions.add(mockActionA);
 		when(mockActionFactory.create(mockTaskA)).thenReturn(mockActionA);
 		when(mockPlanBuilderA.addAction(mockActionA)).thenReturn(mockPlanBuilderB);
         when(mockPlanBuilderB.build()).thenReturn(mockPlan);
         
 		// Try to find a plan
 		ImmutablePlan plan = plannerHelper.findPlanForPrimitive(mockState, mockTaskNetwork);		
 		
 		assertEquals(1, plan.getActions().size());
 		assertTrue(plan.getActions().contains(mockActionA));
 	}
 	
 	@Test
 	public void testFindPlanForPrimitiveTwoTasks() throws PlanNotFound, TaskNotActionable {
 
         List<ImmutableAction> mockActions = new ArrayList<ImmutableAction>();
         ImmutablePlan mockPlan = mock(ImmutablePlan.class);
         when(mockPlan.getActions()).thenReturn(mockActions);
         ImmutablePlanBuilder mockPlanBuilderA = mock(ImmutablePlanBuilder.class);
         ImmutablePlanBuilder mockPlanBuilderB = mock(ImmutablePlanBuilder.class);
         ImmutablePlanBuilder mockPlanBuilderC = mock(ImmutablePlanBuilder.class);
         ImmutablePlanBuilderFactory mockPlanBuilderFactory = mock(ImmutablePlanBuilderFactory.class);
         when(mockPlanBuilderFactory.createBuilder()).thenReturn(mockPlanBuilderA);
         when(mockPlanBuilderC.build()).thenReturn(mockPlan);
		
 		ImmutableActionFactory mockActionFactory = mock(ImmutableActionFactory.class);
 		@SuppressWarnings("unchecked")
 		DecompositionService<ImmutableMethod, ImmutableTerm<?>, ImmutableTask, ImmutableTaskNetwork,
 		        ImmutableConstraint<?>, ImmutableSubstitution>
 				mockDecompositionService = mock(DecompositionService.class);
 		@SuppressWarnings("unchecked")
 		UnificationService<ImmutableMethod, ImmutableTerm<?>, ImmutableTask, ImmutableTaskNetwork,
 		        ImmutableConstraint<?>, ImmutableCondition<?>> mockUnificationService = mock(UnificationService.class);
         @SuppressWarnings("unchecked")
         SortService<ImmutableTerm<?>, ImmutableTask, ImmutableConstraint<?>> mockSortService
                 = mock(SortService.class);
         
 		SimplePlannerHelper plannerHelper = new SimplePlannerHelper(mockActionFactory,
 		        mockPlanBuilderFactory, mockDecompositionService, mockUnificationService, mockSortService);
 		
 		ImmutableTask mockTaskA = mock(ImmutableTask.class);
 		ImmutableTask mockTaskB = mock(ImmutableTask.class);
 		ImmutableTaskNetwork mockTaskNetwork = mock(ImmutableTaskNetwork.class);
 		Set<ImmutableTask> tasks = new HashSet<ImmutableTask>();
 		tasks.add(mockTaskA);
 		tasks.add(mockTaskB);
 		List<ImmutableTask> sortedTasks = new ArrayList<ImmutableTask>(tasks);
         Set<ImmutableConstraint<?>> mockConstraints = new HashSet<ImmutableConstraint<?>>();
         when(mockTaskNetwork.getTasks()).thenReturn(tasks);
         when(mockTaskNetwork.getConstraints()).thenReturn(mockConstraints);
         State mockState = mock(State.class);
         when(mockSortService.sortByConstaints(tasks, mockConstraints)).thenReturn(sortedTasks);
         
 		ImmutableAction mockActionA = mock(ImmutableAction.class);
 		ImmutableAction mockActionB = mock(ImmutableAction.class);
 		mockActions.add(mockActionA);
 		mockActions.add(mockActionB);
 		when(mockActionFactory.create(mockTaskA)).thenReturn(mockActionA);
 		when(mockActionFactory.create(mockTaskB)).thenReturn(mockActionB);
 		when(mockPlanBuilderA.addAction(mockActionA)).thenReturn(mockPlanBuilderB);
 		when(mockPlanBuilderB.addAction(mockActionB)).thenReturn(mockPlanBuilderC);
        
 		// Try to find a plan
 		ImmutablePlan plan = plannerHelper.findPlanForPrimitive(mockState, mockTaskNetwork);		
 		
 		assertEquals(2, plan.getActions().size());
 		assertTrue(plan.getActions().contains(mockActionA));
 		assertTrue(plan.getActions().contains(mockActionB));
 	}
 	
 	@Test(expected=PlanNotFound.class)
 	public void testFindPlanForPrimitiveTaskNotActionable() throws PlanNotFound, TaskNotActionable {
 
 	    List<ImmutableAction> actions = new ArrayList<ImmutableAction>();
         ImmutablePlan mockPlan = mock(ImmutablePlan.class);
         when(mockPlan.getActions()).thenReturn(actions);
         ImmutablePlanBuilder mockPlanBuilder = mock(ImmutablePlanBuilder.class);
         ImmutablePlanBuilderFactory mockPlanBuilderFactory = mock(ImmutablePlanBuilderFactory.class);
         when(mockPlanBuilderFactory.createBuilder()).thenReturn(mockPlanBuilder);
         when(mockPlanBuilder.build()).thenReturn(mockPlan);
         
 		ImmutableActionFactory mockActionFactory = mock(ImmutableActionFactory.class);
 		@SuppressWarnings("unchecked")
 		DecompositionService<ImmutableMethod, ImmutableTerm<?>, ImmutableTask, ImmutableTaskNetwork,
 		        ImmutableConstraint<?>, ImmutableSubstitution>
 				mockDecompositionService = mock(DecompositionService.class);
 		@SuppressWarnings("unchecked")
 		UnificationService<ImmutableMethod, ImmutableTerm<?>, ImmutableTask, ImmutableTaskNetwork,
 		        ImmutableConstraint<?>, ImmutableCondition<?>> mockUnificationService = mock(UnificationService.class);
         @SuppressWarnings("unchecked")
         SortService<ImmutableTerm<?>, ImmutableTask, ImmutableConstraint<?>> mockSortService
                 = mock(SortService.class);
         
 		SimplePlannerHelper plannerHelper = new SimplePlannerHelper(mockActionFactory,
 		        mockPlanBuilderFactory, mockDecompositionService, mockUnificationService, mockSortService);
 		
 		ImmutableTask mockTaskA = mock(ImmutableTask.class);
 		ImmutableTaskNetwork mockTaskNetwork = mock(ImmutableTaskNetwork.class);
 		Set<ImmutableTask> tasks = new HashSet<ImmutableTask>();
 		tasks.add(mockTaskA);
 		List<ImmutableTask> sortedTasks = new ArrayList<ImmutableTask>(tasks);
 		Set<ImmutableConstraint<?>> mockConstraints = new HashSet<ImmutableConstraint<?>>();
         when(mockTaskNetwork.getTasks()).thenReturn(tasks);
         when(mockTaskNetwork.getConstraints()).thenReturn(mockConstraints);
         State mockState = mock(State.class);
         when(mockSortService.sortByConstaints(tasks, mockConstraints)).thenReturn(sortedTasks);
         
 		when(mockActionFactory.create(mockTaskA)).thenThrow(new TaskNotActionable());
 		
 		// Try to find a plan which should throw an exception because the task account be converted
 		plannerHelper.findPlanForPrimitive(mockState, mockTaskNetwork);		
 	}
 	
 	/**
 	 * Test that a task and method that cannot be unified cannot be decomposed
 	 * @throws DecompositionNotFound
 	 * @throws UnifierNotFound
 	 */
 	@Test(expected=DecompositionNotFound.class)
 	public void testDecomposeNoUnifier() throws DecompositionNotFound, UnifierNotFound, InvalidConstraint {
 		
 		ImmutableTaskNetwork mockTaskNetwork = mock(ImmutableTaskNetwork.class);
 		ImmutableTask mockTask = mock(ImmutableTask.class);
 		ImmutableMethod mockMethod = mock(ImmutableMethod.class);
 		
 		ImmutablePlanBuilderFactory mockPlanBuilderFactory = mock(ImmutablePlanBuilderFactory.class);
         ImmutableActionFactory mockActionFactory = mock(ImmutableActionFactory.class);
 		@SuppressWarnings("unchecked")
 		DecompositionService<ImmutableMethod, ImmutableTerm<?>, ImmutableTask, ImmutableTaskNetwork,
 		        ImmutableConstraint<?>, ImmutableSubstitution>
 				mockDecompositionService = mock(DecompositionService.class);
 		@SuppressWarnings("unchecked")
 		UnificationService<ImmutableMethod, ImmutableTerm<?>, ImmutableTask, ImmutableTaskNetwork,
 		        ImmutableConstraint<?>, ImmutableCondition<?>> mockUnificationService = mock(UnificationService.class);
 		when(mockUnificationService.findUnifier(mockTask, mockMethod)).thenThrow(new UnifierNotFound());
         @SuppressWarnings("unchecked")
         SortService<ImmutableTerm<?>, ImmutableTask, ImmutableConstraint<?>> mockSortService
                 = mock(SortService.class);
         
 		SimplePlannerHelper plannerHelper = new SimplePlannerHelper(mockActionFactory,
 		        mockPlanBuilderFactory, mockDecompositionService, mockUnificationService, mockSortService);
 		
 		plannerHelper.decompose(mockTaskNetwork, mockTask, mockMethod);
 	}
 	
 	/**
 	 * Test that a unifiable task and method can be decomposed.
 	 * @throws DecompositionNotFound
 	 * @throws UnifierNotFound
 	 */
 	@Test
 	public void testDecompose() throws DecompositionNotFound, UnifierNotFound, InvalidConstraint {
 		
 		ImmutableTaskNetwork mockTaskNetwork = mock(ImmutableTaskNetwork.class);
 		ImmutableTaskNetwork mockDecomposedTaskNetwork = mock(ImmutableTaskNetwork.class);
 		ImmutableTask mockTask = mock(ImmutableTask.class);
 		ImmutableMethod mockMethod = mock(ImmutableMethod.class);
 		SimpleUnifier mockUnifier = mock(SimpleUnifier.class);
 		
 		ImmutablePlanBuilderFactory mockPlanBuilderFactory = mock(ImmutablePlanBuilderFactory.class);
         ImmutableActionFactory mockActionFactory = mock(ImmutableActionFactory.class);
 		@SuppressWarnings("unchecked")
 		DecompositionService<ImmutableMethod, ImmutableTerm<?>, ImmutableTask, ImmutableTaskNetwork,
 		        ImmutableConstraint<?>, ImmutableSubstitution>
 				mockDecompositionService = mock(DecompositionService.class);
 		@SuppressWarnings("unchecked")
 		UnificationService<ImmutableMethod, ImmutableTerm<?>, ImmutableTask, ImmutableTaskNetwork,
 		        ImmutableConstraint<?>, ImmutableCondition<?>> mockUnificationService = mock(UnificationService.class);
 		when(mockUnificationService.findUnifier(mockTask, mockMethod)).thenReturn(mockUnifier);
 		when(mockDecompositionService.decompose(mockUnifier, mockTaskNetwork, mockTask, mockMethod)).thenReturn(mockDecomposedTaskNetwork);
         @SuppressWarnings("unchecked")
         SortService<ImmutableTerm<?>, ImmutableTask, ImmutableConstraint<?>> mockSortService
                 = mock(SortService.class);
         
 		SimplePlannerHelper plannerHelper = new SimplePlannerHelper(mockActionFactory,
 		        mockPlanBuilderFactory, mockDecompositionService, mockUnificationService, mockSortService);
 		
 		assertEquals(mockDecomposedTaskNetwork,plannerHelper.decompose(mockTaskNetwork, mockTask, mockMethod));
 	}
 }
