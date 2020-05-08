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
 
 import ddg.model.DDGClient;
 
 
 /**
  * TODO make doc
  *
  * @author thiago - thiago@lsd.ufcg.edu.br
  */
 public abstract class LoginAlgorithm {
 	
 	private final long mSecondsBetweenLogins;
 	
 	private long lastStamp = -1;
 	private DDGClient lastSampledClient;
 	
	public LoginAlgorithm(long mSecondsBetweenLogins) {
 		this.mSecondsBetweenLogins = mSecondsBetweenLogins;
 	}
 
 	/**
 	 * 
 	 * @param now
 	 * @return
 	 */
 	public DDGClient sampleClient(long now) {
 		
 		if (lastStamp == -1) {
 			lastStamp = now;
 		}
 		
 		lastSampledClient = ( ( (now - lastStamp) < mSecondsBetweenLogins)) ? lastSampledClient : pickAClient(now);
 		lastStamp = now;
 		
 		return lastSampledClient;
 	}
 	
 	/**
 	 * 
 	 * @param now
 	 * @return
 	 */
 	protected abstract DDGClient pickAClient(long now);
 }
