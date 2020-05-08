 package dridco.seleniumhtmltojava;
 
 import static dridco.seleniumhtmltojava.TestVariables.LOGGER;
 import static dridco.seleniumhtmltojava.TestVariables.SELENIUM;
 import static dridco.seleniumhtmltojava.TestVariables.STORAGE;
 import static java.lang.String.format;
 import static org.apache.commons.lang.StringUtils.EMPTY;
 import static org.apache.commons.lang.StringUtils.isNotEmpty;
 import static org.apache.commons.logging.LogFactory.getLog;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 
 enum Commands {
 
 	fireEvent {
 		@Override
 		public String doBuild(final String locator, final String eventName) {
 			return format("%s.fireEvent(\"%s\", \"%s\");", SELENIUM, locator,
 					eventName);
 		}
 	},
 	waitForVisible {
 		@Override
 		public String doBuild(String target, String value) {
 			warnIfUnusedValueIsNotEmpty(value);
 			return format("waitForVisible(\"%s\", \"%s\");", target, resolveTimeout(EMPTY));
 		}
 	},
 	open {
 		@Override
 		public String doBuild(final String target, final String value) {
 			warnIfUnusedValueIsNotEmpty(value);
 			return format("open(\"%s\");", target);
 		}
 	},
 	verifyNotChecked {
 		@Override
 		public String doBuild(String locator, String unused) {
 			warnIfUnusedValueIsNotEmpty(unused);
 			return format("assertFalse(\"%s\", %s.isChecked(\"%s\"));",
 					message(locator, unused), SELENIUM, locator);
 		}
 
 	},
 	waitForPageToLoad {
 		@Override
 		public String doBuild(final String target, final String value) {
 			warnIfUnusedValueIsNotEmpty(value);
 			return format("waitForPageToLoad(\"%s\");", target);
 		}
 	},
 	uncheck {
 		@Override
 		public String doBuild(String locator, String unused) {
 			warnIfUnusedValueIsNotEmpty(unused);
 			return format("%s.uncheck(\"%s\");", SELENIUM, locator);
 		}
 	},
 	verifyEditable {
 		@Override
 		public String doBuild(String locator, String unused) {
 			warnIfUnusedValueIsNotEmpty(unused);
 			return format("assertTrue(\"%s\", %s.isEditable(\"%s\"));", message(locator, unused), SELENIUM, locator);
 		}
 	},
 	verifyVisible {
 		@Override
 		public String doBuild(String locator, String unused) {
 			warnIfUnusedValueIsNotEmpty(unused);
 			return format("assertTrue(\"%s\", %s.isVisible(\"%s\"));", message(locator, unused), SELENIUM, locator);
 		}
 	},
 	verifyNotEditable {
 		@Override
 		public String doBuild(String locator, String unused) {
 			warnIfUnusedValueIsNotEmpty(unused);
 			return format("assertFalse(\"%s\", %s.isEditable(\"%s\"));", message(locator, unused), SELENIUM, locator);
 		}
 	},
 	verifyNotVisible {
 		@Override
 		public String doBuild(String locator, String unused) {
 			warnIfUnusedValueIsNotEmpty(unused);
 			return format("assertFalse(\"%s\", %s.isVisible(\"%s\"));", message(locator, unused), SELENIUM, locator);
 		}
 	},
 	verifyChecked {
 		@Override
 		public String doBuild(String locator, String unused) {
 			warnIfUnusedValueIsNotEmpty(unused);
 			return format("assertTrue(\"%s\", %s.isChecked(\"%s\"));",
 					message(locator, unused), SELENIUM, locator);
 		}
 	},
 	clickAndWait {
 		@Override
 		public String doBuild(final String target, final String value) {
 			return click.doBuild(target, value) + waitForPageToLoad.doBuild(String.valueOf(Globals.timeout()), value);
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
 						escape(value.substring(exactMatchPrefix.length())),
 						SELENIUM, target);
 			} else if (value.startsWith(regularExpressionPrefix)) {
 				built = format(
 						"assertThat(\"%s\", %s.getText(\"%s\"), containsString(\"%s\"));",
 						message(target, value), SELENIUM, target,
 						value.substring(regularExpressionPrefix.length()));
 			} else {
 				built = format(
						"assertThat(\"%s\", %s.getText(\"%s\").toLowerCase(), containsString((\"%s\").toLowerCase()));",
 						message(target, value), SELENIUM, target, escape(value));
 			}
 			return built;
 		}
 
 		private String escape(final String s) {
 			return s.replaceAll("<br */?>", "\\\\n");
 		}
 	},
 	verifyValue {
 		@Override
 		public String doBuild(final String target, final String value) {
 			return format(
 					"assertThat(\"%s\", %s.getValue(\"%s\"), containsString(\"%s\"));",
 					message(target, value), SELENIUM, target, value);
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
 		private static final String GLOB_PREFIX = "glob:";
 
 		@Override
 		public String doBuild(final String script, final String pattern) {
 			Transformations transformations;
 			if (pattern.startsWith(GLOB_PREFIX)) {
 				transformations = new GlobTransformations(pattern);
 			} else {
 				transformations = new QuoteTransformation(pattern);
 			}
 			return format(
 					"assertTrue(\"%s\", %s.getEval(\"%s\").matches(%s));",
 					message(script, pattern), SELENIUM, script,
 					transformations.javaCalls());
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
 	verifySelectedLabel {
 
 		@Override
 		public String doBuild(String locator, String pattern) {
 			LOG.warn("VerifySelectedLabel supports only exact matching");
 			return format("assertEquals(\"%s\", %s.getSelectedLabel(\"%s\"));", pattern, SELENIUM, locator);
 		}
 	},
 	storeValue {
 		@Override
 		public String doBuild(final String target, final String value) {
 			return format(
 					"%s.put(\"%s\", escapeJavaScript(%s.getValue(\"%s\")));",
 					STORAGE, value, SELENIUM, target);
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
 		public String doBuild(final String target, final String value) {
 			return format("%s.removeSelection(\"%s\", \"%s\");", SELENIUM,
 					target, value);
 		}
 	},
 	assertTitle {
 		@Override
 		public String doBuild(final String target, final String value) {
 			return verifyTitle.doBuild(target, value);
 		}
 	},
 	assertTextPresent {
 		@Override
 		public String doBuild(final String target, final String value) {
 			return verifyTextPresent.doBuild(target, value);
 		}
 	},
 	assertText {
 		@Override
 		public String doBuild(final String target, final String value) {
 			return verifyText.doBuild(target, value);
 		}
 	},
 	assertElementPresent {
 		@Override
 		public String doBuild(final String target, final String value) {
 			return verifyElementPresent.doBuild(target, value);
 		}
 	},
 	assertConfirmation {
 		@Override
 		public String doBuild(final String target, final String value) {
 			warnIfUnusedValueIsNotEmpty(value);
 			return format(
 					"assertEquals(\"%s\", unescapeJava(\"%s\"), %s.getConfirmation());",
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
 			return format("%s.info(\"%s\");", LOGGER, target);
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
 		public String doBuild(final String target, final String value) {
 			warnIfUnusedValueIsNotEmpty(value);
 			return format(
 					"%s.put(\"%s\", escapeJavaScript(%s.getHtmlSource()));",
 					STORAGE, target, SELENIUM);
 		}
 
 	},
 	clickAt {
 		@Override
 		public String doBuild(final String target, final String value) {
 			return format("%s.clickAt(\"%s\", \"%s\");", SELENIUM, target,
 					value);
 		}
 
 	},
 	waitForElementPresent {
 		@Override
 		public String doBuild(final String target, final String timeout) {
 			return format("waitForElementPresent(\"%s\", \"%s\");", //
 					target, resolveTimeout(timeout));
 		}
 
 	},
 	refresh {
 		@Override
 		public String doBuild(String target, String value) {
 			warnIfUnusedTargetIsNotEmpty(target);
 			warnIfUnusedValueIsNotEmpty(value);
 			return format("%s.refresh();", SELENIUM);
 		}
 	},
 	refreshAndWait {
 		@Override
 		public String doBuild(final String target, final String value) {
 			return refresh.doBuild(target, value) + waitForPageToLoad.doBuild(String.valueOf(Globals.timeout()), value);
 		}
 
 	},
 	waitForEditable() {
 		@Override
 		public String doBuild(final String target, final String timeout) {
 			warnIfUnusedValueIsNotEmpty(timeout);
 			return format("waitForEditable(\"%s\", \"%s\");", //
 					target, resolveTimeout(timeout));
 		}
 
 	},
 	waitForTextPresent() {
 		@Override
 		public String doBuild(final String target, final String timeout) {
 			warnIfUnusedValueIsNotEmpty(timeout);
 			return format("waitForTextPresent(\"%s\", \"%s\");", //
 					target, resolveTimeout(timeout));
 		}
 
 	},
 	waitForNotValue() {
 		@Override
 		public String doBuild(final String target, final String value) {
 			return format("waitForNotValue(\"%s\", \"%s\");", target, value);
 		}
 
 	},
 	check() {
 		@Override
 		public String doBuild(String locator, String unused) {
 			warnIfUnusedValueIsNotEmpty(unused);
 			return format("%s.check(\"%s\");", SELENIUM, locator);
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
 	 * the existence of all variables defined in
 	 * {@link dridco.seleniumhtmltojava.TestVariables}, and every method in the 
 	 * {@link org.junit.Assert}, {@link org.apache.commons.lang.StringEscapeUtils} 
 	 * and {@link org.junit.matchers.JUnitMatchers} classes. There's also every
 	 * function defined in the {@link dridco.seleniumhtmltojava.Functions}
 	 * enumeration
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
 
 	protected final String message(final String target, final String value) {
 		return format("%s(\\\"%s\\\", \\\"%s\\\")", name(), target, value);
 	}
 
 	protected String resolveTimeout(final String defined) {
 		String actualTimeout;
 		if (StringUtils.isEmpty(defined)) {
 			actualTimeout = Globals.timeout().toString();
 		} else {
 			actualTimeout = defined;
 		}
 		return actualTimeout;
 
 	}
 
 }
