 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.bh.gui;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.beans.PropertyChangeListener;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.EventListenerList;
 
 import org.apache.log4j.Logger;
 import org.bh.data.IDTO;
 import org.bh.gui.chart.IBHAddValue;
 import org.bh.gui.swing.BHButton;
 import org.bh.gui.swing.BHLabel;
 import org.bh.gui.swing.BHStatusBar;
 import org.bh.gui.swing.BHTextField;
 import org.bh.gui.swing.IBHComponent;
 import org.bh.gui.swing.IBHModelComponent;
import org.bh.platform.Services;
 
 /**
  * 
  * @author Marco Hammel
  * @author Robert
  */
 public abstract class View implements MouseListener, FocusListener,
 		ICompValueChangeListener {
 
 	private static Logger log = Logger.getLogger(View.class);
 
 	/**
 	 * refernce to the instance of the UI validator
 	 * 
 	 * @see BHValidityEngine
 	 */
 	private BHValidityEngine validator = null;
 	/**
 	 * refernce to a subclass of JPanel keeping UI Components
 	 */
 	private JPanel viewPanel;
 	/**
 	 * representign chart BH components
 	 * 
 	 * @see IBHAddValue
 	 */
 	private Map<String, IBHAddValue> bhChartComponents;
 	/**
 	 * representing all components
 	 * 
 	 * @see IDTO
 	 */
 	private Map<String, IBHComponent> bhComponents;
 	/**
 	 * representing all components related to the model
 	 * 
 	 * @see IDTO
 	 */
 	private Map<String, IBHModelComponent> bhModelComponents;
 	/**
 	 * representing all BH labels and Buttons
 	 * 
 	 * @see BHButton
 	 * @see BHLabel
 	 */
 	private Map<String, IBHComponent> bhTextComponents;
 
 	/**
 	 * Listeners for this view.
 	 */
 	private final EventListenerList viewListeners = new EventListenerList();
 
 	/**
 	 * Should be used in case of a UI which have to be validated
 	 * 
 	 * @param viewPanel
 	 *            instances of a subclass of JPanel
 	 * @param validator
 	 *            instances of a subclass of IBHValidator
 	 * @throws ViewException
 	 *             in case of null or mapping issues
 	 * @see JPanel
 	 * @see BHValidityEngine
 	 */
 	public View(JPanel viewPanel, BHValidityEngine validator)
 			throws ViewException {
 		this.setViewPanel(viewPanel);
 		this.setValidator(validator);
 	}
 
 	/**
 	 * Only should be uesed for UI´s without validation
 	 * 
 	 * @param viewPanel
 	 *            instances of a subclass of JPanel
 	 * @throws ViewException
 	 *             in case of null or mapping issues
 	 * @see JPanel
 	 */
 	public View(JPanel viewPanel) throws ViewException {
 		this(viewPanel, null);
 	}
 
 	/**
 	 * add View class as Key and PropertyChange to all BHTextFields an
 	 * MouseListener to all IBHComponents
 	 * 
 	 * @see IBHComponent
 	 * @see BHTextField
 	 * @see KeyListener
 	 * @see PropertyChangeListener
 	 * @see MouseListener
 	 * @param comp
 	 */
 	private void addListeners(IBHComponent comp) {
 		if (comp instanceof Component) {
 			((Component) comp).addMouseListener(this);
 			((Component) comp).addFocusListener(this);
 		}
 		if (comp instanceof IBHModelComponent) {
 			((IBHModelComponent) comp).getValueChangeManager()
 					.addCompValueChangeListener(this);
 		}
 	}
 
 	/**
 	 * Removes all listeners added by {@link #addListeners(IBHComponent)}.
 	 * 
 	 * @param comp
 	 */
 	private void removeViewListeners(IBHComponent comp) {
 		if (comp instanceof Component) {
 			((Component) comp).removeMouseListener(this);
 			((Component) comp).removeFocusListener(this);
 		}
 		if (comp instanceof IBHModelComponent) {
 			((IBHModelComponent) comp).getValueChangeManager()
 					.removeCompValueChangeListener(this);
 		}
 	}
 
 	/**
 	 * Map all components of IBHComponent in the model, text or chart map and
 	 * set KeyListener and PropertyChangeListener <code>addViewListener</code>
 	 * 
 	 * @see KeyListener
 	 * @see PropertyChangeListener
 	 * @param components
 	 * @return Map of model related components
 	 */
 	private void mapBHcomponents(Component[] components) throws ViewException {
 		for (Component comp : components) {
 			if (comp instanceof IBHComponent) {
 				IBHComponent bhcomp = (IBHComponent) comp;
 				this.bhComponents.put(bhcomp.getKey(), bhcomp);
 				if (comp instanceof BHLabel || comp instanceof BHButton) {
 					this.bhTextComponents.put(bhcomp.getKey(), bhcomp);
 				} else if (comp instanceof IBHAddValue) {
 					this.bhChartComponents.put(bhcomp.getKey(),
 							(IBHAddValue) comp);
 				} else if (comp instanceof IBHModelComponent) {
 					this.bhModelComponents.put(bhcomp.getKey(),
 							(IBHModelComponent) comp);
 				}
 			}
 			// map all subcomponents
 			if (comp instanceof Container) {
 				Container container = (Container) comp;
 				if (container.getComponentCount() > 0)
 					mapBHcomponents(container.getComponents());
 			}
 		}
 	}
 
 	private void mapBHcomponents() throws ViewException {
 		log
 				.debug("UI Components are getting organized and registered in a View instance");
 		this.bhChartComponents = Collections
 				.synchronizedMap(new HashMap<String, IBHAddValue>());
 		this.bhTextComponents = Collections
 				.synchronizedMap(new HashMap<String, IBHComponent>());
 		this.bhComponents = Collections
 				.synchronizedMap(new HashMap<String, IBHComponent>());
 		this.bhModelComponents = Collections
 				.synchronizedMap(new HashMap<String, IBHModelComponent>());
 		mapBHcomponents(this.viewPanel.getComponents());
 	}
 
 	/**
 	 * deliver the Labels an Buttones with translation key
 	 * 
 	 * @return Map of text related components
 	 */
 	public Map<String, IBHComponent> getBHtextComponents() {
 		return this.bhTextComponents;
 	}
 
 	/**
 	 * deliver the components which can bind their values to matching dto
 	 * 
 	 * @return Map of model related componentes
 	 */
 	public Map<String, IBHModelComponent> getBHModelComponents() {
 		return this.bhModelComponents;
 	}
 
 	/**
 	 * Returns all <code>IBHComponent</code>s on the view.
 	 * 
 	 * @return all <code>IBHComponent</code>s on the view.
 	 */
 	public Map<String, IBHComponent> getBHComponents() {
 		return this.bhComponents;
 	}
 
 	public IBHComponent getBHComponent(Object key) {
 		return this.bhComponents.get(key.toString());
 	}
 
 	/**
 	 * deliver the BH charts
 	 * 
 	 * @return Map of BHcharts
 	 */
 	protected Map<String, IBHAddValue> getBHchartComponents() {
 		return bhChartComponents;
 	}
 
 	/**
 	 * deliver the instance of the Plugin Panel for the Platform
 	 * 
 	 * @return instance of a subclass of JPanel
 	 * @see JPanel
 	 */
 	public JPanel getViewPanel() {
 		return this.viewPanel;
 	}
 
 	/**
 	 * Controller can set a new view. All maps will automatically be refactored.
 	 * At this point, all components must have been added to the panel already.
 	 * 
 	 * @param panel
 	 *            instance of a JPanel subclass
 	 * @throws ViewException
 	 *             in case of null reference
 	 * @see JPanel
 	 */
 	public void setViewPanel(JPanel panel) throws ViewException {
 		log.debug("a new panel is set");
 		if (panel == null) {
 			log.error("null reference for view is set");
 			throw new ViewException("null refernce panel is set");
 		}
 		this.viewPanel = panel;
 
 		// remove old listeners
 		if (bhComponents != null) {
 			for (IBHComponent comp : bhComponents.values()) {
 				removeViewListeners(comp);
 			}
 		}
 
 		mapBHcomponents();
 
 		// add listeners
 		for (IBHComponent comp : bhComponents.values()) {
 			addListeners(comp);
 		}
 	}
 
 	/**
 	 * deliver the <code>BHValidityEngine</code> instance
 	 * 
 	 * @return instance of a subclass of BHValidityEngine
 	 * @see BHValidityEngine
 	 */
 	public BHValidityEngine getValidator() {
 		return validator;
 	}
 
 	/**
 	 * set a new validator instance to the view <code>registerComponents</code>
 	 * will be called
 	 * 
 	 * @param validator
 	 * @throws ViewException
 	 *             in case of null refernce
 	 * @see View
 	 */
 	public void setValidator(BHValidityEngine validator) throws ViewException {
 		log.debug("a new validator has been set");
 		this.validator = validator;
 		if (validator != null) {
 			this.validator.registerComponents(bhModelComponents.values());
 			revalidate();
 		}
 	}
 
 	public void revalidate() {
 		if (validator != null)
 			this.validator.publishValidationAll(bhModelComponents);
 	}
 
 	public void revalidate(IBHModelComponent comp) {
 		if (validator != null)
 			this.validator.publishValidationComp(comp);
 	}
 
 	@Override
 	public void compValueChanged(IBHModelComponent comp) {
 		if (validator == null
 				|| !validator.publishValidationComp(comp).hasErrors())
 			fireViewEvent(new ViewEvent(comp, ViewEvent.Type.VALUE_CHANGED));
 	}
 
 	/**
 	 * set the InputHint on the Platform in case of a IBHComponent
 	 * 
 	 * @param e
 	 *            representes the source
 	 * @see BHStatusBar
 	 */
 	private void handleInputInfoEvent(Object e) {
 		if (e instanceof IBHComponent) {
 			BHValidityEngine.setInputHintLabel((IBHComponent) e);
 		}
 	}
 
 	public void mouseClicked(MouseEvent e) {
 		// this.handleInputInfoEvent(e.getSource());
 	}
 
 	public void mouseEntered(MouseEvent e) {
 		// this.handleInputInfoEvent(e.getSource());
 	}
 
 	public void mouseExited(MouseEvent e) {
 		// this.handleInputInfoEvent(e.getSource());
 	}
 
 	public void mousePressed(MouseEvent e) {
 		this.handleInputInfoEvent(e.getSource());
 	}
 
 	public void mouseReleased(MouseEvent e) {
 		// this.handleInputInfoEvent(e.getSource());
 	}
 
 	public void addViewListener(IViewListener l) {
 		viewListeners.add(IViewListener.class, l);
 	}
 
 	public void removeViewListener(IViewListener l) {
 		viewListeners.remove(IViewListener.class, l);
 	}
 
 	protected void fireViewEvent(ViewEvent event) {
 		for (IViewListener l : viewListeners.getListeners(IViewListener.class))
 			l.viewEvent(event);
 	}
 
 	@Override
 	public void focusGained(final FocusEvent e) {
 		if (e.getSource() instanceof IBHModelComponent)
 			SwingUtilities.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					revalidate((IBHModelComponent) e.getSource());
 				}
 			});
 
 	}
 
 	@Override
 	public void focusLost(FocusEvent e) {
		Services.getBHstatusBar().removeErrorHint();
 	}
 
 }
