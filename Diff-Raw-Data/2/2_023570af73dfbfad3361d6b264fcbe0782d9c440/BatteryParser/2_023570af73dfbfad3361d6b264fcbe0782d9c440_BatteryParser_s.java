 package org.ita.neutrino.junit3parser;
 
 import org.ita.neutrino.codeparser.Method;
 
 /**
  * Responsável por localizar as Suites de testes e seus respectivos métodos.
  * 
  * @author Rafael Monico
  * 
  */
 class BatteryParser extends org.ita.neutrino.junitgenericparser.BatteryParser {
 
 	protected TestMethodKind getTestMethodKind(Method method) {
 		if (method.getName().equals("setup")) {
 			return TestMethodKind.BEFORE_METHOD;
		} else if (method.getName().equals("tearDown"))  {
 			return TestMethodKind.AFTER_METHOD;
 		} else if (method.getName().startsWith("test")) {
 			return TestMethodKind.TEST_METHOD;
 		} else {
 			return TestMethodKind.NOT_TEST_METHOD;
 		}
 	}
 
 }
