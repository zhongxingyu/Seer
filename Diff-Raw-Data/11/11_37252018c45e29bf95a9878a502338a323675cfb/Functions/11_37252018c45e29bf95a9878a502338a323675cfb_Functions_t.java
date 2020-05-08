 package dridco.seleniumhtmltojava;
 
 import static dridco.seleniumhtmltojava.TestVariables.SELENIUM;
 import static java.lang.String.format;
 import static java.util.Arrays.asList;
 import static org.apache.commons.collections15.CollectionUtils.collect;
 import static org.apache.commons.lang.StringUtils.join;
 
 import org.apache.commons.collections15.Transformer;
 
 /**
  * An enumeration of functions available for commands to invoke
  */
 public enum Functions {
 
 	pause {
 		@Override
 		public String render() {
 			return functionDeclaration(new Parameter[] { new Parameter(
 					"millis", Integer.class) }, new FunctionBody() {
 
 				public String render() {
 					return "try { Thread.sleep(millis); }"
 							+ "catch (InterruptedException e) { fail(e.getMessage()); }";
 				}
 			});
 		}
 	},
 	waitForPageToLoad {
 		@Override
 		public String render() {
 			return functionDeclaration(new Parameter[] { new Parameter(
 					"timeout", String.class) }, new FunctionBody() {
 
 				public String render() {
 					return "int millis = Integer.valueOf(timeout);"
 							+ "int actualTimeout;"
 							+ "if("
 							+ Globals.forcedTimeout
 							+ " > 0) { actualTimeout = "
 							+ Globals.forcedTimeout
 							+ "; }"
 							+ "else { actualTimeout = millis; }"
 							+ "long start = System.currentTimeMillis();"
 							+ "selenium.waitForPageToLoad(\"\" + actualTimeout);"
 							+ "long duration = System.currentTimeMillis() - start;"
 							+ "if(duration > millis) { logger.warning(java.text.MessageFormat.format(\"Defined timeout insufficient. Declared: {0}, Forced: {1}, Actual: {2}\", millis, "
 							+ Globals.forcedTimeout + ", duration)); }";
 				}
 			});
 		}
 	},
 	waitForElementPresent {
 		@Override
 		public String render() {
 			return waitForSomething(new WaitCallback() {
 
 				public String waitCondition(final String targetArgumentName,
 						final String valueArgumentName) {
 					return SELENIUM + ".isElementPresent(" + targetArgumentName
 							+ ")";
 				}
 
 			});
 		}
 	},
 	waitForTextPresent {
 		@Override
 		public String render() {
 			return waitForSomething(new WaitCallback() {
 
 				public String waitCondition(final String targetArgumentName,
 						final String valueArgumentName) {
 					return SELENIUM + ".isTextPresent(" + targetArgumentName
 							+ ")";
 				}
 
 			});
 		}
 	},
 	waitForEditable {
 		@Override
 		public String render() {
 			return waitForSomething(new WaitCallback() {
 
 				public String waitCondition(final String targetArgumentName,
 						final String valueArgumentName) {
 					return SELENIUM + ".isEditable(" + targetArgumentName + ")";
 				}
 
 			});
 		}
 	},
 	waitForNotValue {
 
 		@Override
 		public String render() {
 			return functionDeclaration(new Parameter[] {
 					new Parameter("target", String.class),
 					new Parameter("value", String.class) }, new FunctionBody() {
 
 				public String render() {
 					return "final int millisBetweenAttempts = 500;"
 							+ "int remainingAttempts = "
 							+ Globals.timeout
 							+ " / millisBetweenAttempts;"
 							+ "while (remainingAttempts > 0) {"
 							+ "if(! value.equals("
 							+ SELENIUM
 							+ ".getValue(target))) { break; }"
 							+ "else { remainingAttempts--; try { Thread.sleep(millisBetweenAttempts); } catch (InterruptedException e) { fail(e.getMessage()); } }"
 							+ "}";
 				}
 			});
 		}
 	};
 
 	public abstract String render();
 
 	private abstract class WaitCallback {
 
 		abstract String waitCondition( //
 				String targetArgumentName, String valueArgumentName);
 
 	}
 
 	protected String waitForSomething(final WaitCallback callback) {
 		final String targetArgumentName = "element";
 		final String valueArgumentName = "timeout";
 		return functionDeclaration(new Parameter[] {
 				new Parameter(targetArgumentName, String.class),
 				new Parameter(valueArgumentName, String.class) },
 				new FunctionBody() {
 
 					public String render() {
 						return format(
 								"int millis = Integer.valueOf(timeout);"
 										+ "final int millisBetweenAttempts = 500;"
 										+ "int remainingAttempts = millis / millisBetweenAttempts;"
 										+ "while (remainingAttempts > 0) {"
 										+ "if(%s) { break; }"
 										+ "else { remainingAttempts--; try { Thread.sleep(millisBetweenAttempts); } catch (InterruptedException e) { fail(e.getMessage()); } }"
 										+ "}", //
 								callback.waitCondition(targetArgumentName,
 										valueArgumentName));
 					}
 				});
 	}
 
 	protected String functionDeclaration(Parameter[] parameters,
 			FunctionBody body) {
 		Transformer<Parameter, String> parametersForDeclaration = new Transformer<Parameter, String>() {
 
 			public String transform(Parameter input) {
 				return input.renderForDeclaration();
 			}
 		};
 		String parameterSeparator = ",";
 		String parametersDeclaration = //
 		join(collect(asList(parameters), parametersForDeclaration),
 				parameterSeparator);
 		String functionBody = body.render();
 		return format("private void %s(%s) {%s}", //
 				name(), parametersDeclaration, functionBody);
 	}
 
 	private static class Parameter {
 
 		public Parameter(String name, Class<?> type) {
 			this.name = name;
 			this.type = type;
 		}
 
 		private String name;
 		private Class<?> type;
 
 		String renderForDeclaration() {
 			return format("%s %s", type.getName(), name);
 		}
 
 	}
 
	private static interface FunctionBody {
 
 		String render();
 
 	}
 
 }
