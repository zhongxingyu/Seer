 package org.processmining.plugins.connectionfactories.logpetrinet;
 
 import info.clearthought.layout.TableLayout;
 import info.clearthought.layout.TableLayoutConstants;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
import java.util.HashSet;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.processmining.plugins.connectionfactories.logpetrinet.*;
 
 import org.deckfour.xes.classification.XEventClass;
 import org.deckfour.xes.classification.XEventClasses;
 import org.deckfour.xes.classification.XEventClassifier;
 import org.deckfour.xes.info.XLogInfo;
 import org.deckfour.xes.info.XLogInfoFactory;
 import org.deckfour.xes.model.XLog;
 import org.processmining.framework.util.ArrayUtils;
import org.processmining.framework.util.Pair;
 import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
 import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
 
 import com.fluxicon.slickerbox.factory.SlickerFactory;
 
 import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
 import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
 
 public class LogPetrinetAssUI extends LogPetrinetConnectionFactoryUI {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -7885986532303653221L;
 	
 	private boolean similarity=false;
 	private final XLog log;
 	private Map<Transition, JComboBox> mapTrans2ComboBox = new HashMap<Transition, JComboBox>();
 	private JComboBox classifierSelectionCbBox;
 	
 	public LogPetrinetAssUI(final XLog log, final PetrinetGraph net, Object[] availableClassifier) {
 
 		super(log, net, availableClassifier);
 
 		// index for row
 		int rowCounter = 0;
 
 		// import variable
 		this.log = log;
 
 		// swing factory
 		SlickerFactory factory = SlickerFactory.instance();
 
 		// set layout
 		double size[][] = { { TableLayoutConstants.FILL,TableLayoutConstants.FILL, TableLayoutConstants.FILL }, { 80,20, 70 } };
 		TableLayout layout = new TableLayout(size);
 		setLayout(layout);
 
 		// label
 		add(factory
 				.createLabel("<html><h1>Map Transitions to Event Class</h1><p>First, select a classifier. Unmapped transitions will be mapped to a dummy event class.</p></html>"),
 				"0, " + rowCounter + ", 1, " + rowCounter);
 		rowCounter++;
 
 		final String invisibleTransitionRegEx = "t[0-9]+|(tr[0-9]+)|(silent)|(tau)|(skip)|(invi)";
 		final Pattern pattern = Pattern.compile(invisibleTransitionRegEx);
 		//add 1:1
 		ChangeListener changeListener = new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				similarity= !similarity;
 				Object[] boxOptions = extractEventClasses(log,
 						(XEventClassifier) classifierSelectionCbBox.getSelectedItem());
 				for (Transition transition : mapTrans2ComboBox.keySet()) {
 					JComboBox cbBox = mapTrans2ComboBox.get(transition);
 					cbBox.removeAllItems(); // remove all items
 
 					for (Object item : boxOptions) {
 						cbBox.addItem(item);
 					}
 					if (!transition.isInvisible()) {					
 						cbBox.setSelectedIndex(preSelectOption(transition.getLabel().toLowerCase(), boxOptions, pattern));
 					} else {
 						cbBox.setSelectedItem(DUMMY);
 					}
 				}
 			}
 		};
 		JCheckBox checkbox = factory.createCheckBox("Log to Transition 1:1", similarity);
 		checkbox.addChangeListener(changeListener);
 		add(checkbox, "1, " + rowCounter + ", l, c");
 		rowCounter++;
 
 		// add classifier selection
 		classifierSelectionCbBox = factory.createComboBox(availableClassifier);
 		classifierSelectionCbBox.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Object[] boxOptions = extractEventClasses(log,
 						(XEventClassifier) classifierSelectionCbBox.getSelectedItem());
 
 				for (Transition transition : mapTrans2ComboBox.keySet()) {
 					JComboBox cbBox = mapTrans2ComboBox.get(transition);
 					cbBox.removeAllItems(); // remove all items
 
 					for (Object item : boxOptions) {
 						cbBox.addItem(item);
 					}
 					if (!transition.isInvisible()) {
 						cbBox.setSelectedIndex(preSelectOption(transition.getLabel().toLowerCase(), boxOptions, pattern));
 					} else {
 						cbBox.setSelectedItem(DUMMY);
 					}
 				}
 			}
 		});
 		classifierSelectionCbBox.setSelectedIndex(0);
 		classifierSelectionCbBox.setPreferredSize(new Dimension(350, 30));
 		classifierSelectionCbBox.setMinimumSize(new Dimension(350, 30));
 
 		add(factory.createLabel("Choose classifier"), "0, " + rowCounter + ", l, c");
 		add(classifierSelectionCbBox, "1, " + rowCounter + ", l, c");
 		rowCounter++;
 
 
 		// add mapping between transitions and selected event class 
 		Object[] boxOptions = extractEventClasses(log, (XEventClassifier) classifierSelectionCbBox.getSelectedItem());
 		for (Transition transition : net.getTransitions()) {
 			layout.insertRow(rowCounter, 30);
 			JComboBox cbBox = factory.createComboBox(boxOptions);
 			cbBox.setPreferredSize(new Dimension(350, 30));
 			cbBox.setMinimumSize(new Dimension(350, 30));
 			mapTrans2ComboBox.put(transition, cbBox);
 			if (transition.isInvisible()) {
 				cbBox.setSelectedItem(DUMMY);
 			} else {
 				cbBox.setSelectedIndex(preSelectOption(transition.getLabel().toLowerCase(), boxOptions, pattern));
 			}
 
 			add(factory.createLabel(transition.getLabel()), "0, " + rowCounter + ", l, c");
 			add(cbBox, "1, " + rowCounter + ", l, c");
 			rowCounter++;
 		}
 
 	}
 	/**
 	 * Returns the Event Option Box index of the most similar event for the
 	 * transition.
 	 * 
 	 * @param transition
 	 *            Name of the transitions
 	 * @param events
 	 *            Array with the options for this transition
 	 * @return Index of option more similar to the transition
 	 */
 	private int preSelectOption(String transition, Object[] events, Pattern pattern) {
 		Matcher matcher = pattern.matcher(transition);
 		if(matcher.find() && matcher.start()==0){
 			return 0;
 		}
 		if(!similarity){
 			//The metric to get the similarity between strings
 			AbstractStringMetric metric = new Levenshtein();
 			if( transition.contains("GW_"))
 				return 0;
 			if( transition.contains("_"))
 				transition=	transition.replace("_", "#");
 
 			int index = 0;
 			float simOld = Float.MIN_VALUE;
 			for (int i = 1; i < events.length; i++) {
 				String event = ((XEventClass) events[i]).toString();
 				float sim = metric.getSimilarity(transition, event);
 
 				if (simOld < sim) {
 					simOld = sim;
 					index = i;
 				}
 			}
 
 			return index;
 		}else{
 			for (int i = 1; i < events.length; i++) {
 				String event = ((XEventClass) events[i]).toString();
 				//int h = event.indexOf("+");
 				//if(h>0)
 					//event=event.substring(0, h);
 				if (transition.toLowerCase().equals(event.toLowerCase())) {
 					return i;
 				}
 			}
 
 			return 0;
 		}
 	}
 	
 	/**
 	 * get all available event classes using the selected classifier, add with
 	 * NONE
 	 * 
 	 * @param log
 	 * @param selectedItem
 	 * @return
 	 */
 	private Object[] extractEventClasses(XLog log, XEventClassifier selectedItem) {
 		XLogInfo summary = XLogInfoFactory.createLogInfo(log,
 				(XEventClassifier) classifierSelectionCbBox.getSelectedItem());
 		XEventClasses eventClasses = summary.getEventClasses();
 
 		// sort event class
 		Collection<XEventClass> classes = eventClasses.getClasses();
 
 		// create possible event classes
 		Object[] arrEvClass = classes.toArray();
 		Arrays.sort(arrEvClass);
 		Object[] notMappedAct = { "NONE" };
 		Object[] boxOptions = ArrayUtils.concatAll(notMappedAct, arrEvClass);
 
 		return boxOptions;
 	}
	
	public XEventClassifier getSelectedClassifier() {
		return (XEventClassifier) classifierSelectionCbBox.getSelectedItem();
	}

	public XEventClasses getClasses() {
		XEventClassifier classifier = ((XEventClassifier) classifierSelectionCbBox.getSelectedItem());
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, classifier);
		XEventClasses classes = summary.getEventClasses(classifier);
		return classes;	
	}

	public Collection<Pair<Transition, XEventClass>> getMap() {
		Collection<Pair<Transition, XEventClass>> res = new HashSet<Pair<Transition,XEventClass>>();
		for (Transition trans : mapTrans2ComboBox.keySet()) {
			Object selectedValue = mapTrans2ComboBox.get(trans).getSelectedItem();
			if (selectedValue instanceof XEventClass) {
				// a real event class
				res.add(new Pair<Transition, XEventClass>(trans, (XEventClass) selectedValue));
			} else {
				// this is "NONE"
				res.add(new Pair<Transition, XEventClass>(trans, DUMMY));
			}
		}
		return res;
	}
 
 }
