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
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sonar.api.profiles.RulesProfile;
 import org.sonar.api.resources.Project;
 import org.sonar.api.rules.ActiveRule;
 import org.sonar.plugins.erlang.language.Erlang;
 import org.sonar.plugins.erlang.testmetrics.utils.GenericFileNameRegexFilter;
 import org.sonar.plugins.erlang.violations.ActiveRuleFilter;
 import org.sonar.plugins.erlang.violations.ErlangRuleManager;
 import org.sonar.plugins.erlang.violations.ViolationReport;
 import org.sonar.plugins.erlang.violations.ViolationReportUnit;
 
 /**
  * Read and parse generated dialyzer report
  * 
  * @author tkende
  * 
  */
 public class ErlangRefactorErl {
 	private final static Logger LOG = LoggerFactory.getLogger(ErlangRefactorErl.class);
 
 	/**
 	 * Matches to something like: (ATOM:ATOM/NUMBER)(WHITESPACES)(SOMETHING NOT
 	 * WHITESPACES)(:WHITESPACES)(NUMBER,NUMBER-NUMBER,NUMBER) like: game:start/0
 	 * /home/dev/project/erlang/game.erl: 4,1-5,12 means: 'application
 	 * name':'function name'/'number of parameters' 'URL to the file': 'starting
 	 * row','starting col'-'ending row','ending col'
 	 */
 
 	public ViolationReport refactorErl(Project project, ErlangRuleManager erlangRuleManager, RulesProfile profile) {
 		ViolationReport report = new ViolationReport();
 		List<ActiveRule> activeRules = profile.getActiveRulesByRepository("Erlang");
 
 		/**
 		 * Read refactorErl results
 		 */
 		File basedir = new File(project.getFileSystem().getBasedir() + File.separator
 				+ ((Erlang) project.getLanguage()).getEunitFolder());
 		LOG.debug("Parsing refactorErl reports from folder {}", basedir.getAbsolutePath());
 
 		String refactorErlPattern = ((Erlang) project.getLanguage()).getRefactorErlFilenamePattern();
 
 		String[] list = getFileNamesByPattern(basedir, refactorErlPattern);
 
 		if (list.length == 0) {
 			LOG.warn("no file matches to : ", refactorErlPattern);
			return null;
 		}
 
 		for (String file : list) {
 			try {
 				List<ViolationReportUnit> units = readRefactorErlReportUnits(basedir, file);
 				for (ViolationReportUnit refactorErlReportUnit : units) {
 					ActiveRule activeRule = ActiveRuleFilter.getActiveRuleByRuleName(activeRules,
 							refactorErlReportUnit.getMetricKey());
 					if (activeRule != null && checkIsValid(activeRule, refactorErlReportUnit.getMetricValue())) {
 						refactorErlReportUnit.setDescription(getMessageForMetric(activeRule,
 								refactorErlReportUnit.getMetricValue()));
 						/**
 						 * Replace key coming from activeProfile because it contains
 						 * the name originaly 
 						 */
 						refactorErlReportUnit.setMetricKey(activeRule.getRuleKey());
 						report.addUnit(refactorErlReportUnit);
 					}
 				}
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return report;
 	}
 
 	public static String[] getFileNamesByPattern(File basedir, String refactorErlPattern) {
 		GenericFileNameRegexFilter filter = new GenericFileNameRegexFilter(refactorErlPattern);
 
 		if (basedir.isDirectory() == false) {
 			LOG.warn("Folder does not exist {}", basedir);
			return null;
 		}
 
 		String[] list = basedir.list(filter);
 		return list;
 	}
 
 	public static List<ViolationReportUnit> readRefactorErlReportUnits(File basedir, String file) throws FileNotFoundException,
 			IOException {
 		File oneReportFile = new File(basedir, file);
 		FileInputStream fstream = new FileInputStream(oneReportFile);
 		DataInputStream in = new DataInputStream(fstream);
 		BufferedReader RefactorErlOutput = new BufferedReader(new InputStreamReader(in));
 		BufferedReader breader = new BufferedReader(RefactorErlOutput);
 		List<ViolationReportUnit> units = RefactorErlReportParser.parse(breader);
 		return units;
 	}
 
 	private String getMessageForMetric(ActiveRule activeRule, Object value) {
 		String param = activeRule.getParameter("maximum");
 		if (param != null) {
 			return activeRule.getRule().getName() + " is " + String.valueOf(value) + " (max allowed is " + param + ")";
 		}
 		return activeRule.getRule().getDescription();
 	}
 
 	private boolean checkIsValid(ActiveRule activeRule, String value) {
 		String param = activeRule.getParameter("maximum");
 		if (param != null) {
 			return (Integer.valueOf(value) > Integer.valueOf(param));
 		}
 		return true;
 	}
 
 }
