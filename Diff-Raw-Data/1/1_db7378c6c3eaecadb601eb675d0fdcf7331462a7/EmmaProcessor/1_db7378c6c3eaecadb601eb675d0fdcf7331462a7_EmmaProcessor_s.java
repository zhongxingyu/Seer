 /*
  * Sonar, open source software quality management tool.
  * Copyright (C) 2009 SonarSource
  * mailto:contact AT sonarsource DOT com
  *
  * Sonar is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 
 package org.sonar.plugins.emma;
 
 import com.vladium.emma.data.DataFactory;
 import com.vladium.emma.data.ICoverageData;
 import com.vladium.emma.data.IMergeable;
 import com.vladium.emma.data.IMetaData;
 import com.vladium.emma.report.*;
 import com.vladium.util.IntObjectMap;
 import org.apache.commons.lang.StringUtils;
 import org.sonar.api.batch.SensorContext;
 import org.sonar.api.measures.CoreMetrics;
 import org.sonar.api.measures.PersistenceMode;
 import org.sonar.api.measures.PropertiesBuilder;
 import org.sonar.api.resources.JavaFile;
import org.sonar.api.utils.ParsingUtils;
 import org.sonar.api.utils.SonarException;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 
 /**
  * @author Evgeny Mandrikov
  */
 public class EmmaProcessor {
 
   private final PropertiesBuilder<Integer, Integer> lineHitsBuilder = new PropertiesBuilder<Integer, Integer>(CoreMetrics.COVERAGE_LINE_HITS_DATA);
 
   private final SensorContext context;
   private final IReportDataModel model;
 
   public EmmaProcessor(File buildDir, SensorContext context) {
     try {
       File coverageFile = new File(buildDir, EmmaPlugin.COVERAGE_DATA);
       final ICoverageData coverageData;
       if (coverageFile.exists()) {
         IMergeable[] mergeableCoverageData = DataFactory.load(coverageFile);
         coverageData = (ICoverageData) mergeableCoverageData[DataFactory.TYPE_COVERAGEDATA];
       } else {
         coverageData = DataFactory.newCoverageData();
       }
       IMergeable[] mergeableMetadata = DataFactory.load(new File(buildDir, EmmaPlugin.META_DATA));
       IMetaData metaData = (IMetaData) mergeableMetadata[DataFactory.TYPE_METADATA];
       this.model = IReportDataModel.Factory.create(metaData, coverageData);
       this.context = context;
     } catch (IOException e) {
       throw new SonarException(e);
     }
   }
 
   public void process() {
     model.getView(IReportDataView.HIER_SRC_VIEW).getRoot().accept(new MyVisitor(), null);
   }
 
   class MyVisitor extends AbstractItemVisitor {
 
     public Object visit(AllItem item, Object o) {
       work(item, o);
       return o;
     }
 
     public Object visit(PackageItem item, Object o) {
       work(item, o);
       return o;
     }
 
     public Object visit(SrcFileItem item, Object o) {
       lineHitsBuilder.clear();
       int lines = 0;
       int coveredLines = 0;
 
       IntObjectMap map = item.getLineCoverage();
       for (int lineId : map.keys()) {
         SrcFileItem.LineCoverageData lineCoverageData = (SrcFileItem.LineCoverageData) map.get(lineId);
 
         lines++;
         final int fakeHits;
         if (lineCoverageData.m_coverageStatus == SrcFileItem.LineCoverageData.LINE_COVERAGE_COMPLETE) {
           coveredLines++;
           fakeHits = 1;
         } else {
           fakeHits = 0;
         }
         lineHitsBuilder.add(lineId, fakeHits);
       }
 
       String packageName = item.getParent().getName();
       String fileName = item.getName();
       JavaFile resource = new JavaFile(packageName, StringUtils.substringBeforeLast(fileName, "."));
 
       context.saveMeasure(resource, CoreMetrics.LINES_TO_COVER, (double) lines);
       context.saveMeasure(resource, CoreMetrics.UNCOVERED_LINES, (double) lines - coveredLines);
       context.saveMeasure(resource, lineHitsBuilder.build().setPersistenceMode(PersistenceMode.DATABASE));
 
       return o;
     }
 
     private void work(IItem item, Object ctx) {
       Iterator iter = item.getChildren();
       while (iter.hasNext()) {
         IItem child = (IItem) iter.next();
         child.accept(this, ctx);
       }
     }
   }
 }
