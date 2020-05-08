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
 package org.sonar.plugins.erlang.testmetrics.cover;
 
 import java.io.File;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sonar.api.batch.SensorContext;
 import org.sonar.api.measures.CoreMetrics;
 import org.sonar.api.measures.Measure;
 import org.sonar.api.measures.PropertiesBuilder;
 import org.sonar.api.resources.InputFile;
 import org.sonar.api.resources.Project;
 import org.sonar.plugins.erlang.language.Erlang;
 import org.sonar.plugins.erlang.language.ErlangFile;
 import org.sonar.plugins.erlang.sensor.AbstractErlangSensor;
 import org.sonar.plugins.erlang.testmetrics.utils.GenericExtFilter;
 import org.sonar.plugins.erlang.testmetrics.utils.TestSensorUtils;
 
 public final class ErlangCoverageSensor extends AbstractErlangSensor {
 
 	public ErlangCoverageSensor(Erlang erlang) {
 		super(erlang);
 	}
 
 	private final static Logger LOG = LoggerFactory.getLogger(ErlangCoverageSensor.class);
 
 	public void analyse(Project project, SensorContext sensorContext) {
 
 		File reportsDir = new File(project.getFileSystem().getBasedir(), erlang.getEunitFolder());
 		GenericExtFilter filter = new GenericExtFilter(".html");
 
 		if (reportsDir.isDirectory() == false) {
 			LOG.warn("Folder does not exist {}", reportsDir);
 			return;
 		}
 
 		String[] list = reportsDir.list(filter);
 
 		if (list.length == 0) {
 			LOG.warn("no files end with : ", reportsDir);
 			return;
 		}
 
 		for (String file : list) {
 			if (!file.matches(".*\\.COVER.html") || file.contains("_eunit")) {
 				continue;
 			}
 			String sourceName = file.replaceAll("(.*?)(\\.COVER\\.html)", "$1");
 			CoverCoverageParser parser = new CoverCoverageParser();
 			CoverFileCoverage fileCoverage = parser.parseFile(new File(reportsDir, file), project.getFileSystem()
 					.getSourceDirs().get(0).getName(), sourceName);
 			LOG.debug("Analysing coverage file: " + file + " for coverage result of " + sourceName);
 			analyseCoveredFile(project, sensorContext, fileCoverage, sourceName);
 		}
 
 	}
 
 	protected void analyseCoveredFile(Project project, SensorContext sensorContext, CoverFileCoverage coveredFile,
 			String sourceName) {
 		InputFile sourceFile = TestSensorUtils.findFileForReport(project.getFileSystem().mainFiles(erlang.getKey()),
 				sourceName.concat(Erlang.EXTENSION));
 		ErlangFile sourceResource = ErlangFile.fromInputFile(sourceFile, true);
 
 		PropertiesBuilder<Integer, Integer> lineHitsData = new PropertiesBuilder<Integer, Integer>(
 				CoreMetrics.COVERAGE_LINE_HITS_DATA);
 
 		if (coveredFile.getCoveredLines() > 0) {
 			Map<Integer, Integer> hits = coveredFile.getLineCoverageData();
 			for (Map.Entry<Integer, Integer> entry : hits.entrySet()) {
 				lineHitsData.add(entry.getKey(), entry.getValue());
 			}
 			try {
 				sensorContext.saveMeasure(sourceResource, lineHitsData.build());
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			sensorContext.saveMeasure(sourceResource, CoreMetrics.LINES_TO_COVER, (double) coveredFile.getLinesToCover());
 			sensorContext.saveMeasure(sourceResource, CoreMetrics.UNCOVERED_LINES,
 					(double) coveredFile.getUncoveredLines());
		} 

 	}
 
 	protected CoverFileCoverage getFileCoverage(InputFile input, List<CoverFileCoverage> coverages) {
 		for (CoverFileCoverage file : coverages) {
 			if (file.getFullFileName().equals(input.getFile().getAbsolutePath())) {
 				return file;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public String toString() {
 		return getClass().getSimpleName();
 	}
 
 }
