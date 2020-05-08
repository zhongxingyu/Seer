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
 package org.gerryai.htn.planner;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.util.HashSet;
 import java.util.List;
 
 import org.gerryai.htn.tasknetwork.Plan;
 import org.gerryai.htn.tasknetwork.Problem;
 import org.gerryai.htn.tasknetwork.Task;
 import org.gerryai.htn.tasknetwork.TaskNetwork;
 import org.junit.Test;
 
 /**
  * @author David Edwards <david@more.fool.me.uk>
  *
  */
 public class PlannerImplTest {
 
 	@Test
 	public void testFind() {
 		
 		TaskNetwork mockTaskNetwork = mock(TaskNetwork.class);
 		when(mockTaskNetwork.getTasks()).thenReturn(new HashSet<Task>());
 		Problem mockProblem = mock(Problem.class);
 		when(mockProblem.getTaskNetwork()).thenReturn(mockTaskNetwork);
 		
 		PlannerImpl planner = new PlannerImpl();
 		
 		List<Plan> plans = planner.solve(mockProblem);
 		
		assertEquals(1,plans.size());
		Plan plan = plans.get(0);
		assertEquals(0, plan.getOperators().size());
 	}
 
 }
