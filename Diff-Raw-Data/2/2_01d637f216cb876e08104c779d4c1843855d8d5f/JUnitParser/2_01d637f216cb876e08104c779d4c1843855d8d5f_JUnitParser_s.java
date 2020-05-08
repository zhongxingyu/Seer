 package org.ita.testrefactoring.junitparser;
 
 import org.ita.testrefactoring.abstracttestparser.AbstractTestParser;
 import org.ita.testrefactoring.abstracttestparser.TestParserException;
 import org.ita.testrefactoring.codeparser.Environment;
 
 public class JUnitParser extends AbstractTestParser {
 
 	private JUnitSelection selection;
 	private Environment environment;
 	private JUnitTestBattery battery;
 
 	@Override
 	public void setEnvironment(Environment environment) {
 		this.environment = environment;
 	}
 
 	@Override
 	public void parse() throws TestParserException {
 		selection = new JUnitSelection(environment.getSelection());
 		
 		battery = new JUnitTestBattery();
 		
 		doBatteryParse();
 	}
 
 	private void doBatteryParse() {
 		BatteryParser batteryParser = new BatteryParser();
 		
 		batteryParser.setEnvironment(environment);
 		
 		batteryParser.setBattery(battery);
 		
 		batteryParser.parse();
 	}
 
 	@Override
 	public JUnitTestBattery getBattery() {
 		return battery;
 	}
 
 	@Override
 	public JUnitSelection getSelection() {
 		return selection;
 	}
 
 }
