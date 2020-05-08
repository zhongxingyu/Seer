 /**
  * Copyright (C) 2009 Universidade Federal de Campina Grande
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package ddg.model.loginAlgorithm;
 
 import java.util.List;
 import java.util.Random;
 
 import ddg.model.Aggregator;
 import ddg.model.DDGClient;
 
 
 /**
  * TODO make doc
  *
  * @author thiago - thiago@lsd.ufcg.edu.br
  */
 public class SweetHomeLoginAlgorithm extends LoginAlgorithm {
 
 	private final Random random;
 	private final DDGClient sweetHomeClient;
 	private final List<DDGClient> othersClients;
 	
 	private final double migrationProb;
 	
 	/**
 	 * @param swapMachineProb
 	 * @param mSecondsBetweenLogins
 	 * @param sweetHomeClient
 	 * @param othersClients
 	 */
 	public SweetHomeLoginAlgorithm(double swapMachineProb, long mSecondsBetweenLogins, DDGClient sweetHomeClient, List<DDGClient> othersClients) {
 		
		super(mSecondsBetweenLogins, sweetHomeClient);
 		
 		if (swapMachineProb < 0 || swapMachineProb >= 1) {
 			throw new IllegalArgumentException();
 		}
 		
 		if (othersClients.contains(sweetHomeClient)) {
 			throw new IllegalArgumentException();
 		}
 		
 		this.migrationProb = swapMachineProb;
 		this.sweetHomeClient = sweetHomeClient;
 		this.othersClients = othersClients;
 		this.random = new Random();
 	}
 
 	protected DDGClient pickAClient(long now) {
 		double sample = random.nextDouble();
 		DDGClient client = (sample <= migrationProb) ? othersClients.get(random.nextInt(othersClients.size())) : sweetHomeClient;
 		Aggregator.getInstance().reportlogin(client, now);
 		return client;
 	}
 
 }
