 package zen;
 
 import rjson.Rjson;
 import rjson.utils.NullifyDateTransformer;
 import rjson.utils.RjsonUtil;
 
 public class Then {
 	public Then assertThatObjectUnderTestIsNotModified() {
 		String objectUnderTestJsonAfterTestExecution = RjsonUtil.completeSerializer().toJson(when.given().objectUnderTest());
 		Assert.thatJsonEqualsLiterally(when.given().objectUnderTestJsonBeforeTestExecution(), objectUnderTestJsonAfterTestExecution);
 		return this;
 	}
 
 	public Then assertThatObjectUnderTestIsModifiedAs(Object expectedObject) {
 		return this;
 	}
 
 	public Then assertThatInputParametersAreNotModified() {
		Rjson rjson = Rjson.newInstance().and(new NullifyDateTransformer()).andIgnoreModifiers();
 		for (int i = 0; i < when.inputParams().size(); i++) {
 			Object object = when.inputParams().get(i);
 			String afterExecutionJson = rjson.toJson(object);
 			Assert.thatJsonEqualsLiterally(afterExecutionJson, when.inputParamJsons().get(i));
 		}
 		return this;
 	}
 
 	public Then assertThatThereAreNoSideEffects() {
 		this.assertThatObjectUnderTestIsNotModified();
 		this.assertThatInputParametersAreNotModified();
 		return this;
 	}
 
 	public Then assertThatReturnValueIsSameAs(Object expectedObject) {
 		assertThatThereAreNoSideEffects();
 		Assert.thatEquals(expectedObject, returnObject);
 		return this;
 	}
 
 	public static Then thenAssertChanges(When when) {
 		Then then = new Then();
 		then.when = when;
 		return then;
 	}
 
 	private When when;
 	private Object returnObject;
 
 	public Object getReturnObject() {
 		return returnObject;
 	}
 
 	public void setReturnObject(Object returnObject) {
 		this.returnObject = returnObject;
 	}
 
 	private Then() {
 	}
 }
