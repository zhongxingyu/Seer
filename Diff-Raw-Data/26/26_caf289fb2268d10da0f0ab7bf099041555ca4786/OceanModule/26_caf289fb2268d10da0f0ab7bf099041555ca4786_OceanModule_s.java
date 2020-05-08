 /**
  * Copyright 2012 Eric P. Vialle
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
 package it.freshminutes.oceanrunner.modules.engine;
 
 import it.freshminutes.oceanrunner.OceanRunner;
 import it.freshminutes.oceanrunner.exceptions.OceanModuleException;
 
 import org.junit.runner.Description;
 import org.junit.runner.notification.Failure;
 
 /**
  * Define an OceanModule. An OceanModule could be compared to a JUnit Runner.
  * But when you may have only one JUnit Runner for your class, you may use
  * multiple OceanModules.
  * 
  * @author Eric Vialle
  * 
  */
 public abstract class OceanModule {
 
 
 	/**
 	 * Called before all the tested method (first method to be called, after the
 	 * instantiation of class to test).
 	 */
 	public void doBeforeAllTestedMethods(final OceanRunner oceanRunner, final Class<?> klass) throws OceanModuleException {
 	}
 
 	/** Called after all the tested method (last method to be called). */
 	public void doAfterAllTestedMethods(final OceanRunner oceanRunner, final Class<?> klass) throws OceanModuleException {
 	}
 	
 	/** Called before each tested method. */
 	public void doBeforeEachTestedMethod(final OceanRunner oceanRunner) throws OceanModuleException {
 	}
 	
 	/** Called after each tested method (successful or not). */
 	public void doAfterEachTestedMethod(final OceanRunner oceanRunner, final Description description) throws OceanModuleException {
 	}
 	
 	/** Called after each failed tested method. */
 	public void doAfterEachFailedMethod(final OceanRunner oceanRunner, final Failure failure) throws OceanModuleException {
 	}
 	
 	/** Called after each ignored method. */
 	public void doAfterEachIgnoredMethod(final OceanRunner oceanRunner, final Description description) throws OceanModuleException {
 	}
 	
 	/** Called after each tested method with a failed assumption. */
 	public void doAfterEachAssumptionFailedMethod(final OceanRunner oceanRunner, final Failure failure) throws OceanModuleException {
 	}
 	
 	/** Called after each tested method with a failed assertion. */
 	public void doAfterEachAssertionFailedMethod(final OceanRunner oceanRunner, final Failure failure) throws OceanModuleException {
 	}
 
 	/**
 	 * Number of times that the method should be run.
 	 */
 	public long totalNumberOfRepeat(final OceanRunner oceanRunner) throws OceanModuleException {
 		return 1;
 	}
 	
 	/**
 	 * Number of times that this method has been run in function of the total
 	 * number of repeat for dedicated to this module.
 	 */
 	public final long nbOfRepeatModulo(final OceanRunner oceanRunner) throws OceanModuleException {
		return oceanRunner.getNbOfIterationOfTheMethod(oceanRunner.getMethodUnderTest()) % totalNumberOfRepeat(oceanRunner);
 	}
 
 }
