 /*
  * Copyright 2010 Softgress - http://www.softgress.com/
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package sim.data;
 
 /**
  * Implementation for {@link MethodMetrics}.
  * 
  * @author mcq
  * 
  */
 public final class MethodImpl implements Method {
 	private static final long serialVersionUID = 1L;
 
 	private ApplicationId applicationId;
 
 	private String methodName;
 	private String className;
 
 	public MethodImpl(ApplicationId applicationId, String className, String methodName) {
 		this.applicationId = applicationId;
 		this.methodName = methodName;
 		this.className = className;
 	}
 
 
 	@Override
 	public ApplicationId getApplicationId() {
 		return applicationId;
 	}
 
 	@Override
 	public String getMethodName() {
 		return methodName;
 	}
 
 	@Override
 	public String getClassName() {
 		return className;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("MethodImpl [application=");
 		builder.append(applicationId);
 		builder.append(", methodName=");
 		builder.append(methodName);
 		builder.append(", className=");
 		builder.append(className);
 		builder.append("]");
 		return builder.toString();
 	}
 
 }
