 /*
  * AbstractVisualisationView.java (JMetricVis)
  * Copyright 2011 Luke Plaster. All rights reserved.
  */
 package org.lukep.javavis.visualisation.views;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import org.lukep.javavis.metrics.MetricAttribute;
 import org.lukep.javavis.program.generic.models.ClassModel;
 import org.lukep.javavis.program.generic.models.IGenericModelNode;
 import org.lukep.javavis.program.generic.models.ProjectModel;
 import org.lukep.javavis.ui.swing.WorkspaceContext;
 import org.lukep.javavis.visualisation.visualisers.Openchart2Visualiser;
 import org.lukep.javavis.visualisation.visualisers.PrefuseVisualiser;
 import org.lukep.javavis.visualisation.visualisers.mxGraphVisualiser;
 
 import prefuse.Display;
 
 import com.approximatrix.charting.model.DataSet;
 import com.approximatrix.charting.model.DefaultDataSet;
 import com.approximatrix.charting.model.ObjectChartDataModel;
 import com.approximatrix.charting.swing.ExtendedChartPanel;
 import com.mxgraph.swing.mxGraphComponent;
 
 public class AbstractVisualisationView implements IVisualiserVisitor {
 
 	private static final String UNSUPPORTED_VISUALISATION_EXCEPTION = 
 		"This Visualisation is unsupported by the current Workspace.";
 	
 	@Override
 	public void visit(mxGraphVisualiser visualiser, WorkspaceContext wspContext, 
 			mxGraphComponent graphComponent) {
 		
 		// overridden in sub-class
 		throw new UnsupportedOperationException(UNSUPPORTED_VISUALISATION_EXCEPTION);
 	}
 
 	@Override
 	public void visit(PrefuseVisualiser visualiser, WorkspaceContext wspContext, 
 			Display display) {
 		
 		// overridden in sub-class
 		throw new UnsupportedOperationException(UNSUPPORTED_VISUALISATION_EXCEPTION);
 	}
 
 	@Override
 	public ExtendedChartPanel visit(Openchart2Visualiser visualiser, WorkspaceContext wspContext) {
 		
 		// overridden in sub-class
 		throw new UnsupportedOperationException(UNSUPPORTED_VISUALISATION_EXCEPTION);
 	}
 	
 	protected static Map<IGenericModelNode, ClassModel[]> getFilteredClasses(ProjectModel project, 
 			IGenericModelNode[] subjects) {
 		
 		Map<IGenericModelNode, ClassModel[]> nodeChildClasses = 
 			new LinkedHashMap<IGenericModelNode, ClassModel[]>(subjects.length);
 		
 		// create filtered collection of classes
 		for (IGenericModelNode subject : subjects) {
 			Collection<ClassModel> filteredClasses;
 			if (subject instanceof ProjectModel) {
 				// we're dealing with a project node - this is easy
 				filteredClasses = project.getClassMap().values();
 			} else {
 				// gather a list of the classes with the subject's qualifiedName as its containerName
 				filteredClasses = new Vector<ClassModel>();
 				String containerFilter = subject.getQualifiedName();
 				for (ClassModel model : project.getClassMap().values()) {
 					if (model.getContainerName().equals(containerFilter))
 						filteredClasses.add(model);
 				}
 			}
 			nodeChildClasses.put(subject, filteredClasses.toArray(new ClassModel[filteredClasses.size()]));
 		}
 		
 		return nodeChildClasses;
 	}
 	
 	protected static List<ClassModel> getAllClassesInFilter(Map<IGenericModelNode, ClassModel[]> nodeChildClasses) {
 		// compile a collection of all of the classes in the map's arrays
 		List<ClassModel> allClasses = new Vector<ClassModel>();
 		for (ClassModel[] classes : nodeChildClasses.values()) {
 			allClasses.addAll(Arrays.asList(classes));
 		}
 		System.out.println(allClasses);
 		return allClasses;
 	}
 	
 	protected static ObjectChartDataModel getSingleSeriesObjectChartDataModel(
 			Collection<ClassModel> classes, MetricAttribute metric) {
 		
 		// create the chart's data model - single series
 		int classCount = classes.size();
 		Object[] columns = new Object[classCount];
 		double[][] data = new double[1][classCount];
 		
 		int i = 0;
 		for (ClassModel clazz : classes) {
 			// add the ClassModel object as a column
 			columns[i] = clazz;
 			
 			// add the metric measurement as a data value in series 0
 			data[0][i] = metric.measureTargetCached(clazz).getResult();
 			
 			i++;
 		}
 		
 		return new ObjectChartDataModel(data, columns, new String[] { metric.getInternalName() });
 	}
 	
 	protected static ObjectChartDataModel getContainerSeriesObjectChartDataModel(
 			Map<IGenericModelNode, ClassModel[]> nodeChildClasses, MetricAttribute metric) {
 		
 		// one series for each parent node
 		String[] series = new String[nodeChildClasses.size()];
 		
 		// find the largest class array and total class counts
 		int	largestClassArray = 0,
 			totalClassCount = 0,
 			i = 0;
 		for (Map.Entry<IGenericModelNode, ClassModel[]> seriesEntry 
 				: nodeChildClasses.entrySet()) {
 			
 			int classCount = seriesEntry.getValue().length;
 			totalClassCount += classCount;
 			
 			if (classCount > largestClassArray)
 				largestClassArray = classCount;
 			
 			series[i++] = seriesEntry.getKey().getQualifiedName();
 		}
 		
 		// create our columns and data 2D arrays
 		Vector<Object> columnsVec = new Vector<Object>(totalClassCount);
 		DataSet[] dataSets = new DataSet[series.length];
 		
 		// construct a DataSet for each series (container) with its constituent classes mapped to metric measurements
 		i = 0; int j;
 		for (Map.Entry<IGenericModelNode, ClassModel[]> seriesEntry 
 				: nodeChildClasses.entrySet()) {
 			j = 0;
 			Double[] values = new Double[seriesEntry.getValue().length];
 			for (ClassModel clazz : seriesEntry.getValue()) {
 				columnsVec.add(clazz);
				values[j++] = metric.measureTargetCached(clazz).getResult();
 			}
 			dataSets[i++] = new DefaultDataSet(values, seriesEntry.getValue(), 0, 
 					seriesEntry.getKey().getQualifiedName());
 		}
 		
 		return new ObjectChartDataModel(dataSets, columnsVec.toArray());
 	}
 	
 	protected static ObjectChartDataModel getClassSeriesObjectChartDataModel(
 			Map<IGenericModelNode, ClassModel[]> nodeChildClasses, MetricAttribute metric) {
 		
 		// compile a collection of all of the classes in the map's arrays
 		List<ClassModel> allClasses = getAllClassesInFilter(nodeChildClasses);
 		
 		// create the chart's data model - multiple series
 		int classCount = allClasses.size();
 		Object[] columns = new Object[classCount];
 		double[][] data = new double[classCount][1];
 		String[] series = new String[classCount];
 		
 		int i = 0;
 		for (ClassModel clazz : allClasses) {
 			// add the ClassModel object as a column
 			columns[i] = clazz;
 			series[i] = clazz.getSimpleName();
 			
 			// add the metric measurement as a data value in series 0
 			data[i][0] = metric.measureTargetCached(clazz).getResult();
 			
 			i++;
 		}
 		
 		return new ObjectChartDataModel(data, columns, series);
 	}
 
 }
