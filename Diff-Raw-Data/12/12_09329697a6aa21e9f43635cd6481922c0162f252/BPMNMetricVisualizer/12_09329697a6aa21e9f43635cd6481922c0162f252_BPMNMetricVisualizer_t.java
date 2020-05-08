 package org.processmining.plugins.bpmn.exporting.metrics;
 
 import java.awt.Color;
 import java.util.LinkedList;
 
 import javax.swing.BorderFactory;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 
 import javax.swing.JScrollPane;
 import javax.swing.ScrollPaneConstants;
 
 import org.processmining.contexts.uitopia.annotations.Visualizer;
 
 import org.processmining.framework.plugin.PluginContext;
 import org.processmining.framework.plugin.annotations.Plugin;
 import org.processmining.framework.plugin.annotations.PluginVariant;
 
 import com.fluxicon.slickerbox.factory.SlickerDecorator;
 
 
 
 @Plugin(name = "Visualize BPMN Metrics", parameterLabels = { "BPMN Metrics" }, returnLabels = { "BPMN Metrics Visualization" }, returnTypes = { JComponent.class })
 @Visualizer
 public class BPMNMetricVisualizer {
 
 	
 	@PluginVariant(requiredParameterLabels = { 0 })
 	public static <E> JComponent visualize(PluginContext context, LinkedList<E> metrics) {
 		
		String visualizze = metrics.toString().replace("/n", "<br/>");
		return visualizestring("<html>"+visualizze+"</html>");
		
 	
 	}
 	
 	public static JComponent visualizestring( String tovisualize) {
 		JScrollPane sp = new JScrollPane();
 		sp.setOpaque(false);
 		sp.getViewport().setOpaque(false);
 		sp.setBorder(BorderFactory.createEmptyBorder());
 		sp.setViewportBorder(BorderFactory.createLineBorder(new Color(10, 10, 10), 2));
 		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
 		SlickerDecorator.instance().decorate(sp.getVerticalScrollBar(), new Color(0, 0, 0, 0),
 				new Color (140, 140, 140), new Color(80, 80, 80));
 		sp.getVerticalScrollBar().setOpaque(false);
 		
 		
 		JLabel l = new JLabel(tovisualize);
 		sp.setViewportView(l);
 
 		return sp;
 	}
 	
 	
 }
