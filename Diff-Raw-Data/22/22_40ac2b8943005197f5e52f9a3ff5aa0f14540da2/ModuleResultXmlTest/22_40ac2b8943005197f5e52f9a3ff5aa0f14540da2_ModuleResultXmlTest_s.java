 package org.kalibro.service.entities;
 
 import static org.kalibro.core.model.ModuleFixtures.*;
 import static org.kalibro.core.model.ModuleResultFixtures.*;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Set;
 
 import org.junit.Test;
 import org.kalibro.DtoTestCase;
 import org.kalibro.core.concurrent.Task;
 import org.kalibro.core.model.CompoundMetric;
 import org.kalibro.core.model.ModuleResult;
 import org.powermock.reflect.internal.WhiteboxImpl;
 
 public class ModuleResultXmlTest extends DtoTestCase<ModuleResult, ModuleResultXml> {
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void defaultConstructorOfCompoundMetricWithErrorShouldDoNothing() {
 		checkException(new Task() {
 
 			@Override
 			public void perform() {
 				new CompoundMetricWithErrorXml().getError();
 			}
 		}, NullPointerException.class);
 	}
 
 	@Override
 	protected ModuleResultXml newDtoUsingDefaultConstructor() {
		new CompoundMetricWithErrorXml();
 		return new ModuleResultXml();
 	}
 
 	@Override
 	protected Collection<ModuleResult> entitiesForTestingConversion() {
 		ModuleResult resultWithError = new ModuleResult(helloWorldClass(), new Date());
 		resultWithError.addCompoundMetricWithError(new CompoundMetric(), new Exception());
 		return Arrays.asList(helloWorldApplicationResult(), helloWorldClassResult(), resultWithError);
 	}
 
 	@Override
 	protected ModuleResultXml createDto(ModuleResult moduleResult) {
 		return new ModuleResultXml(moduleResult);
 	}
 
 	@Override
 	protected void assertCorrectConversion(ModuleResult original, ModuleResult converted) {
 		assertCorrectCompoundMetricsWithErrorConversion(original, converted);
 		removeCompoundMetricsWithError(original);
 		removeCompoundMetricsWithError(converted);
 		assertDeepEquals(original, converted);
 	}
 
 	private void assertCorrectCompoundMetricsWithErrorConversion(ModuleResult original, ModuleResult converted) {
 		Set<CompoundMetric> compoundMetricsWithError = original.getCompoundMetricsWithError();
 		assertDeepEquals(compoundMetricsWithError, converted.getCompoundMetricsWithError());
 		for (CompoundMetric metric : compoundMetricsWithError)
 			new ErrorXmlTest().assertCorrectConversion(original.getErrorFor(metric), converted.getErrorFor(metric));
 	}
 
 	private void removeCompoundMetricsWithError(ModuleResult moduleResult) {
 		WhiteboxImpl.setInternalState(moduleResult, "compoundMetricsWithError", (Object) null);
 	}
 }
