 package dridco.seleniumhtmltojava;
 
 import static dridco.seleniumhtmltojava.TestVariables.SELENIUM;
 import static dridco.seleniumhtmltojava.TestVariables.STORAGE;
 import static java.lang.String.format;
 import static org.apache.commons.lang.StringUtils.isNotEmpty;
 import static org.apache.commons.logging.LogFactory.getLog;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 
 enum Commands {
 
 	open {
 		@Override
 		public String doBuild(final String target, final String value) {
 			warnIfUnusedValueIsNotEmpty(value);
 			return format("%s.open(\"%s\");", SELENIUM, target);
 		}
 	},
 	waitForPageToLoad {
 		@Override
 		public String doBuild(final String target, final String value) {
 			warnIfUnusedValueIsNotEmpty(value);
			return format("waitForPageToLoad(\"%s\");", target);
 		}
 	},
 	clickAndWait {
 		@Override
 		public String doBuild(final String target, final String value) {
 			warnIfUnusedValueIsNotEmpty(value);
 			return click.doBuild(target, value)
 					+ waitForPageToLoad.doBuild(
 							String.valueOf(Globals.timeout), value);
 		}
 	},
 	verifyText {
 		@Override
 		public String doBuild(final String target, final String value) {
 			final String exactMatchPrefix = "exact:";
 			final String regularExpressionPrefix = "regexp:";
 			String built;
 			if (value.startsWith(exactMatchPrefix)) {
 				built = format(
 						"assertEquals(\"%s\", \"%s\", %s.getText(\"%s\"));",
 						message(target, value),
 						value.substring(exactMatchPrefix.length()), SELENIUM,
 						target);
 			} else if (value.startsWith(regularExpressionPrefix)) {
 				built = format(
 						"assertThat(\"%s\", %s.getText(\"%s\"), containsString(\"%s\"));",
 						message(target, value), SELENIUM, target,
 						value.substring(regularExpressionPrefix.length()));
 			} else {
 				built = format(
 						"assertThat(\"%s\", %s.getText(\"%s\"), containsString(\"%s\"));",
 						message(target, value), SELENIUM, target, value);
 			}
 			return built;
 		}
 	},
 	verifyValue {
 		@Override
 		public String doBuild(final String target, final String value) {
 			return format(
 					"assertThat(\"%s\", %s.getValue(\"%s\"), containsString(\"%s\"));",
 					message(target, value), SELENIUM, target, value, target);
 		}
 	},
 	verifyTitle {
 		@Override
 		public String doBuild(final String target, final String value) {
 			warnIfUnusedValueIsNotEmpty(value);
 			return format(
 					"assertThat(\"%s\", %s.getTitle(), containsString(\"%s\"));",
 					message(target, value), SELENIUM, target);
 		}
 	},
 	verifyExpression {
 		@Override
 		public String doBuild(final String target, final String value) {
 			return format(
 					"assertThat(\"%s\", %s.getExpression(\"%s\"), containsString(\"%s\"));",
 					message(target, value), SELENIUM, target, value);
 		}
 	},
 	verifyEval {
 		@Override
 		public String doBuild(final String target, final String value) {
 			return "assertEquals(\"" + message(target, value) + "\", "
 					+ SELENIUM + ".getEval(\"" + target + "\"), \"" + value
 					+ "\");";
 		}
 	},
 	verifyElementNotPresent {
 		@Override
 		public String doBuild(final String target, final String value) {
 			warnIfUnusedValueIsNotEmpty(value);
 			return format("assertFalse(\"%s\", %s.isElementPresent(\"%s\"));",
 					message(target, value), SELENIUM, target);
 		}
 	},
 	storeValue {
 		@Override
 		public String doBuild(final String target, final String value) {
 			return format("%s.put(\"%s\", %s.getValue(\"%s\"));", STORAGE,
 					value, SELENIUM, target);
 		}
 	},
 	typeKeys {
 		@Override
 		public String doBuild(final String target, final String value) {
 			return format("%s.typeKeys(\"%s\", \"%s\");", SELENIUM, target,
 					value);
 		}
 	},
 	storeText {
 		@Override
 		public String doBuild(final String target, final String value) {
 			return format("%s.put(\"%s\", %s.getText(\"%s\"));", STORAGE,
 					value, SELENIUM, target);
 		}
 	},
 	storeLocation {
 		@Override
 		public String doBuild(final String target, final String value) {
 			warnIfUnusedValueIsNotEmpty(value);
 			return format("%s.put(\"%s\", %s.getLocation());", STORAGE, target,
 					SELENIUM);
 		}
 	},
 	store {
 		@Override
 		public String doBuild(final String target, final String value) {
 			return format("%s.put(\"%s\", \"%s\");", STORAGE, value, target);
 		}
 	},
 	setTimeout {
 		@Override
 		public String doBuild(final String target, final String value) {
 			warnIfUnusedValueIsNotEmpty(value);
 			return format("%s.setTimeout(\"%s\");", SELENIUM, target);
 		}
 	},
 	setSpeed {
 		@Override
 		public String doBuild(final String target, final String value) {
 			return format("%s.setSpeed(\"%s\");", SELENIUM, target);
 		}
 	},
 	selectWindow {
 		@Override
 		public String doBuild(final String target, final String value) {
 			return format("%s.selectWindow(\"%s\");", SELENIUM, target);
 		}
 	},
 	selectFrame {
 		@Override
 		public String doBuild(final String target, final String value) {
 			return format("%s.selectFrame(\"%s\");", SELENIUM, target);
 		}
 	},
 	removeSelection {
 		@Override
 		public String doBuild(String target, String value) {
 			return format("%s.removeSelection(\"%s\", \"%s\");", SELENIUM,
 					target, value);
 		}
 	},
 	assertTitle {
 		@Override
 		public String doBuild(String target, String value) {
 			return verifyTitle.doBuild(target, value);
 		}
 	},
 	assertTextPresent {
 		@Override
 		public String doBuild(String target, String value) {
 			return verifyTextPresent.doBuild(target, value);
 		}
 	},
 	assertText {
 		@Override
 		public String doBuild(String target, String value) {
 			return verifyText.doBuild(target, value);
 		}
 	},
 	assertElementPresent {
 		@Override
 		public String doBuild(String target, String value) {
 			return verifyElementPresent.doBuild(target, value);
 		}
 	},
 	assertConfirmation {
 		@Override
 		public String doBuild(String target, String value) {
 			warnIfUnusedValueIsNotEmpty(value);
 			return format(
 					"assertEquals(\"%s\", \"%s\", %s.getConfirmation());",
 					message(target, value), target, SELENIUM);
 		}
 	},
 	addSelection {
 		@Override
 		public String doBuild(final String target, final String value) {
 			return format("%s.addSelection(\"%s\", \"%s\");", SELENIUM, target,
 					value);
 		}
 	},
 	verifyTextNotPresent {
 		@Override
 		public String doBuild(final String target, final String value) {
 			warnIfUnusedValueIsNotEmpty(value);
 			return format("assertFalse(\"%s\", %s.isTextPresent(\"%s\"));",
 					message(target, value), SELENIUM, target);
 		}
 	},
 	verifyTextPresent {
 		@Override
 		public String doBuild(final String target, final String value) {
 			warnIfUnusedValueIsNotEmpty(value);
 			return format("assertTrue(\"%s\", %s.isTextPresent(\"%s\"));",
 					message(target, value), SELENIUM, target);
 		}
 	},
 	verifyElementPresent {
 		@Override
 		public String doBuild(final String target, final String value) {
 			warnIfUnusedValueIsNotEmpty(value);
 			return format("assertTrue(\"%s\", %s.isElementPresent(\"%s\"));",
 					message(target, value), SELENIUM, target);
 		}
 	},
 	deleteAllVisibleCookies {
 
 		@Override
 		public String doBuild(final String target, final String value) {
 			warnIfUnusedTargetIsNotEmpty(target);
 			warnIfUnusedValueIsNotEmpty(value);
 			return format("%s.deleteAllVisibleCookies();", SELENIUM);
 		}
 	},
 	type {
 		@Override
 		public String doBuild(final String target, final String value) {
 			return format("%s.type(\"%s\", \"%s\");", SELENIUM, target, value);
 		}
 	},
 	select {
 		@Override
 		public String doBuild(final String target, final String value) {
 			return format("%s.select(\"%s\", \"%s\");", SELENIUM, target, value);
 		}
 	},
 	click {
 		@Override
 		public String doBuild(final String target, final String value) {
 			warnIfUnusedValueIsNotEmpty(value);
 			return SELENIUM + ".click(\"" + target + "\");";
 		}
 	},
 	echo {
 		@Override
 		public String doBuild(final String target, final String value) {
 			warnIfUnusedValueIsNotEmpty(value);
 			return format("/* echo %s */", target);
 		}
 	},
 	storeAttribute {
 		@Override
 		public String doBuild(final String target, final String value) {
 			return format("%s.put(\"%s\", selenium.getValue(\"%s\"));",
 					STORAGE, value, target);
 		}
 	},
 	storeEval {
 		@Override
 		public String doBuild(final String target, final String value) {
 			return format("%s.put(\"%s\", selenium.getEval(\"%s\"));", STORAGE,
 					value, target);
 		}
 	},
 	pause {
 		@Override
 		public String doBuild(final String target, final String value) {
 			warnIfUnusedValueIsNotEmpty(value);
 			LOG.warn("The use of pause is discouraged.");
 			return format("pause(%d);", Integer.valueOf(target));
 		}
 	},
 	storeHtmlSource {
 
 		@Override
 		public String doBuild(String target, String value) {
 			warnIfUnusedValueIsNotEmpty(value);
 			return format("%s.put(\"%s\", %s.getHtmlSource());", STORAGE,
 					target, SELENIUM);
 		}
 
 	},
 	clickAt {
 
 		@Override
 		public String doBuild(String target, String value) {
 			return format("%s.clickAt(\"%s\", \"%s\");", SELENIUM, target,
 					value);
 		}
 
 	},
 	waitForElementPresent {
 
 		@Override
 		public String doBuild(String target, String timeout) {
			String actualTimeout;
 			if (StringUtils.isEmpty(timeout)) {
				actualTimeout = Globals.timeout.toString();
 			} else {
				actualTimeout = timeout;
 			}
			return format("waitForElementPresent(\"%s\", \"%s\");", //
 					target, actualTimeout);
 		}
 
 	},
 	refreshAndWait {
 
 		@Override
 		public String doBuild(String target, String value) {
 			warnIfUnusedTargetIsNotEmpty(target);
 			warnIfUnusedValueIsNotEmpty(value);
 			return format("%s.refresh();", SELENIUM)
 					+ waitForPageToLoad.doBuild(
 							String.valueOf(Globals.timeout), value);
 		}
 
 	},
 	__unknown__ {
 		@Override
 		public String doBuild(final String target, final String value) {
 			return format("fail(\"unknown command %s\");",
 					message(target, value));
 		}
 	};
 
 	private static final Log LOG = getLog(Commands.class);
 
 	public final String build(final String target, final String value) {
 		if (LOG.isDebugEnabled()) {
 			LOG.debug(format("Invoking %s", message(target, value)));
 		}
 		return doBuild( //
 				new Argument(target).parse(), //
 				new Argument(value).parse());
 	}
 
 	/**
 	 * Each implementation should be printing Java lines of code, considering
 	 * the existence of the
 	 * {@link dridco.seleniumhtmltojava.TestVariables#SELENIUM} and
 	 * {@link dridco.seleniumhtmltojava.TestVariables#STORAGE} variable, and
 	 * every method in the {@link org.junit.Assert} and
 	 * {@link org.junit.matchers.JUnitMatchers} classes. There's also every
 	 * method on the test template's source. See
 	 * {@link dridco.seleniumhtmltojava.JavaTestCompiler}
 	 */
 	public abstract String doBuild(String target, String value);
 
 	protected final void warnIfUnusedTargetIsNotEmpty(final String value) {
 		warnIfUnusedIsNotEmpty(value, "target");
 	}
 
 	protected final void warnIfUnusedValueIsNotEmpty(final String value) {
 		warnIfUnusedIsNotEmpty(value, "value");
 	}
 
 	private void warnIfUnusedIsNotEmpty(final String value,
 			final String description) {
 		if (isNotEmpty(value)) {
 			LOG.warn(format(
 					"Ignoring declared value %s for unused field %s in %s command",
 					value, description, name()));
 		}
 	}
 
 	protected final String message(String target, String value) {
 		return format("%s(\\\"%s\\\", \\\"%s\\\")", name(), target, value);
 	}
 
 }
