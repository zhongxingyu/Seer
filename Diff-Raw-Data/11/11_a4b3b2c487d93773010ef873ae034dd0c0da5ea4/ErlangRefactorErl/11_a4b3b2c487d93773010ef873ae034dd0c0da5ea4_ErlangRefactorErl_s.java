 /*
  * Sonar Erlang Plugin
  * Copyright (C) 2012 Tamás Kende
  * kende.tamas@gmail.com
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 package org.sonar.plugins.erlang.violations.refactorerl;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.List;
 
 import org.sonar.api.profiles.RulesProfile;
 import org.sonar.api.resources.Project;
 import org.sonar.api.rules.ActiveRule;
 import org.sonar.api.rules.Rule;
 import org.sonar.api.rules.RuleParam;
 import org.sonar.plugins.erlang.language.Erlang;
 import org.sonar.plugins.erlang.violations.ActiveRuleFilter;
 import org.sonar.plugins.erlang.violations.ErlangRuleManager;
 import org.sonar.plugins.erlang.violations.ErlangViolationResults;
 import org.sonar.plugins.erlang.violations.Issue;
 
 /**
  * Read and parse generated dialyzer report 
  * @author tkende
  *
  */
 public class ErlangRefactorErl {
 	/**
 	 * Matches to something like:
 	 * (ATOM:ATOM/NUMBER)(WHITESPACES)(SOMETHING NOT WHITESPACES)(:WHITESPACES)(NUMBER,NUMBER-NUMBER,NUMBER)
 	 * like:
 	 * game:start/0   /home/dev/project/erlang/game.erl: 4,1-5,12
 	 * means:
 	 * 'application name':'function name'/'number of parameters' 'URL to the file': 'starting row','starting col'-'ending row','ending col'
 	 */
 	
 	public ErlangViolationResults refactorErl(Project project, String systemId, Reader reader, ErlangRuleManager erlangRuleManager, RulesProfile profile) {
 		ErlangViolationResults result = new ErlangViolationResults();
 		/**
 		 * Read dialyzer results
 		 */
 		try {
 			String refactorerlUri = ((Erlang) project.getLanguage()).getRefactorErlUri();
 			File file = new File(project.getFileSystem().getBasedir(), refactorerlUri);
 			FileInputStream fstream = new FileInputStream(file);
 			DataInputStream in = new DataInputStream(fstream);
 			BufferedReader RefactorErlOutput = new BufferedReader(new InputStreamReader(in));
 			BufferedReader breader = new BufferedReader(RefactorErlOutput);
 
 			RefactorErlReport report = RefactorErlReportParser.parse(breader);
 			String actModuleName = systemId.replaceAll("(.*[\\\\/])(.*?)(\\.erl.*)", "$2");
 			List<RefactorErlReportUnit> matchingUnits = report.getUnitsByModuleName(actModuleName);
 			for (RefactorErlReportUnit refactorErlReportUnit : matchingUnits) {
 				for (RefactorErlMetric metric : refactorErlReportUnit.getMetrics()) {
					List<ActiveRule> activeRules = profile.getActiveRulesByRepository("Erlang");
					
 					Rule rule = ActiveRuleFilter.getActiveRuleByRuleName(activeRules, metric.getName());
 					if(checkIsValid(rule, metric)){
 						Issue issue = new Issue(refactorErlReportUnit.getModuleName()+".erl",refactorErlReportUnit.getStartRow(), rule.getKey(), rule.getDescription());
 						result.getIssues().add(issue);	
 					}
 						
 				}
 				
 			}
 		/*	while ((strLine = breader.readLine()) != null) {
 				Matcher matcher = REFACTORERL_VIOLATION_ROW_REGEX.matcher(strLine);
 				if (matcher.matches()) {
 					String moduleName = matcher.group(1).split(":")[0];
 					if (systemId.contains(moduleName)) {
 						Integer rowNum = Integer.valueOf(matcher.group(1).split("-")[0].split(",")[0]);
 						strLine = breader.readLine().trim();
 						String name = strLine.split(" = ")[0];
 						Rule rule = erlangRuleManager.getRuleByName(name);
 						Issue i = new Issue(moduleName+".erl",rowNum, rule.getKey(), rule.getDescription());
 						result.getIssues().add(i);
 					}
 				}
 			}*/
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return result;
 	}
 
 	private boolean checkIsValid(Rule rule, RefactorErlMetric metric) {
 		RuleParam param = rule.getParam("maximum");
 		if(param!=null){
 			return Integer.valueOf(metric.getValue())>param.getDefaultValueAsInteger();
 		}
 		return true;
 	}
 
 }
