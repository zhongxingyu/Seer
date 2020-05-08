 /************************************************************************
  ** 
  ** Copyright (C) 2011 Dave Thomas, PeopleMerge.
  ** All rights reserved.
  ** Contact: opensource@peoplemerge.com.
  **
  ** This file is part of the NGDS language.
  **
  ** Licensed under the Apache License, Version 2.0 (the "License");
  ** you may not use this file except in compliance with the License.
  ** You may obtain a copy of the License at
  **
  **    http://www.apache.org/licenses/LICENSE-2.0
  **
  ** Unless required by applicable law or agreed to in writing, software
  ** distributed under the License is distributed on an "AS IS" BASIS,
  ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ** See the License for the specific language governing permissions and
  ** limitations under the License.
  **  
  ** Other Uses
  ** Alternatively, this file may be used in accordance with the terms and
  ** conditions contained in a signed written agreement between you and the 
  ** copyright owner.
  ************************************************************************/
 package org.deploymentobjects.core.application;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.deploymentobjects.core.domain.model.environment.Environment;
 import org.deploymentobjects.core.domain.model.environment.EnvironmentRepository;
 import org.deploymentobjects.core.domain.model.execution.CreatesJob;
 import org.deploymentobjects.core.domain.model.execution.Dispatchable;
 import org.deploymentobjects.core.domain.model.execution.DispatchableStep;
 import org.deploymentobjects.core.domain.model.execution.Executable;
 import org.deploymentobjects.core.domain.model.execution.ExitCode;
 import org.deploymentobjects.core.domain.model.execution.Job;
 import org.deploymentobjects.core.domain.model.execution.Script;
 import org.deploymentobjects.core.domain.model.execution.SequentialSteps;
 import org.deploymentobjects.core.domain.shared.EventPublisher;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 public class DeployApplicationCommand implements CreatesJob {
 
 	
 	private Dispatchable dispatch;
 	private String appName;
 	private EnvironmentRepository environmentRepository;
 	private Environment environment;
 	
 	//private List<DispatchableStep> steps = new LinkedList<DispatchableStep>();
 
 
 	private Logger logger = LoggerFactory
 			.getLogger(DeployApplicationCommand.class);
 	public EventPublisher publisher;
 	public String environmentName;
	private SequentialSteps steps;
 	public SequentialSteps getSteps(){
 		return steps;
 	}
 
 	public DeployApplicationCommand() {
 	}
 
 	public static class Builder {
 		private static class Tuple {
 			public final String commands;
 			public final String role;
 
 			public Tuple(String x, String y) {
 				this.commands = x;
 				this.role = y;
 			}
 		}
 
 		List<Tuple> commandsToNodes = new LinkedList<Tuple>();
 
 		private DeployApplicationCommand command = new DeployApplicationCommand();
 
 		public Builder(EventPublisher publisher, String appName, String environmentName, EnvironmentRepository repo, Dispatchable dispatch) {
 			command.appName = appName;
 			command.environmentName = environmentName;
 			command.environmentRepository = repo;
 			command.publisher = publisher;
 			command.dispatch = dispatch;
			command.steps = new SequentialSteps(publisher);
 		}
 
 		public DeployApplicationCommand build() {
 			command.environment = command.environmentRepository
 					.lookupByName(command.environmentName);
 			for (Tuple tuple : commandsToNodes) {
 				Script cmd = new Script(tuple.commands);
 				DispatchableStep step =  DispatchableStep.factory(command.publisher, cmd, command.environment
 						.lookupRoleByName(tuple.role), command.dispatch);
 				command.steps.add(step);
 			}
 			return command;
 		}
 
 
 		public Builder addCommandOnNodesByRole(String commands, String role) {
 			commandsToNodes.add(new Tuple(commands, role));
 			return this;
 		}
 
 		public Builder withDispatch(Dispatchable dispatch) {
 			command.dispatch = dispatch;
 			return this;
 		}
 		
 		public Builder withData(/**/){
 			//command.data = not new NoData();
 			return this;
 		}
 
 	}
 
 
 	@Override
 	public Job create() {
 		
 		Job job = new Job(publisher, steps, "DeployApplication-"+appName+"-to-"+environmentName);
 		return job;
 	}
 
 }
