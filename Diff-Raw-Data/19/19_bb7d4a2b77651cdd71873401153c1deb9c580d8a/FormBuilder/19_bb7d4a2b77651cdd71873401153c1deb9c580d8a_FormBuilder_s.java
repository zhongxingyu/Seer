 package pimp.form;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.lang.reflect.Field;
 import java.lang.reflect.Type;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 public class FormBuilder {
 
 	private JPanel jp;
 	private Class<?> c;
 	private Map<String, JComponent> fieldToComponentMapping;
 	private Map<Type, FormElement> typeToFormElementMapping;
 
 	/**
 	 * Constructor takes a Class for which it can build forms for
 	 * 
 	 * @param c
 	 */
 	public FormBuilder(Class<?> c) {
 		this.c = c;
 		fieldToComponentMapping = new HashMap<String, JComponent>();
 		typeToFormElementMapping = new HashMap<Type, FormElement>();
 
 		addFormElement(new StringFormElement()); // Default to string if form
 													// builder type added
 		// jp = createForm();
 	}
 
 	public void addFormElement(FormElement fe) {
 		typeToFormElementMapping.put(fe.getInputType(), fe);
 	}
 
 	/**
 	 * Creates the form based on the Class c
 	 * 
 	 * @return a JPanel form that represents the class
 	 */
 	public void createForm() {
 		
 		// Get all public fields for the class.
 		Field[] fields = c.getFields();
 		
 		JPanel grid = new JPanel(new GridBagLayout());
 		
 		// Input constraints.
 		GridBagConstraints cInput = new GridBagConstraints();
 		cInput.weightx = 1.0;
 		cInput.insets = new Insets(5, 5, 5, 5); // T, L, B, R.
 		cInput.fill = GridBagConstraints.HORIZONTAL;
 		
 		// Label constraints.
 		GridBagConstraints cLabel = new GridBagConstraints();
 		cLabel.insets = new Insets(5, 5, 5, 5); // T, L, B, R.
 		cLabel.anchor = GridBagConstraints.WEST;
 		
 		int row = 0;
 		for (Field f : fields) {
 			// Only add fields with the Form Field Annotation.
 			if (f.isAnnotationPresent(FormField.class)) {
 				// Update constraints to next row.
 				cLabel.gridy = row;
 				cInput.gridy = row;
 				row++;
 				
 				// Create label.
 				grid.add(createFieldFormNameComponent(f), cLabel);
 				
 				// Create input.
 				FormElement fe = typeToFormElementMapping.get(f.getType());
 				if (fe == null) {	// Default to String Input.
 					fe = typeToFormElementMapping.get(String.class);
 				}
 				JComponent input = fe.createComponent();
 				grid.add(input, cInput);
 				
 				// Set mapping to input field.
 				fieldToComponentMapping.put(f.getName(), input);
 			}
 		}
 		
 		/* Create a wrapper panel that expands horizontally and vertically, 
 		 * but only expands it's contents horizontally. Anchor contents to 
 		 * the top leaving the area at the bottom empty. */
 		JPanel wrapper = new JPanel(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();	
 		c.weighty = 1.0;
 		c.weightx = 1.0;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.anchor = GridBagConstraints.NORTH; 
 		wrapper.add(grid, c);
 		
 		jp = wrapper;
 	}
 
 	/**
 	 * Clears the current form
 	 * 
 	 * @return a cleared form
 	 */
 	public JComponent getBlankForm() {
 		createForm();
 		return jp;
 	}
 
 	/**
 	 * Will fill the formBuilder form based on the object given and return the
 	 * form
 	 * 
 	 * @param o
 	 *            the object to fill the form with
 	 * @return the form filled with the objects field values
 	 * 
 	 * @throws IllegalArgumentException
 	 *             if the given object is not the same type as the class the
 	 *             form builder was created with
 	 * @throws IllegalAccessException
 	 */
	public JComponent fillForm(Object o) throws IllegalArgumentException,
			IllegalAccessException {

 		if (!o.getClass().equals(c)) {
 			throw new IllegalArgumentException("Incompatiable object");
 		}

 		for (Field f : o.getClass().getFields()) {
 			if (f.isAnnotationPresent(FormField.class)) {
 				JComponent input = fieldToComponentMapping.get(f.getName());
 				FormElement fe = typeToFormElementMapping.get(f.getType());
				fe.setValue(input, f.get(o));
 			}
 		}
 		return jp;
 	}
 
 	/**
 	 * Will update the given object o so that its values correspond to the
 	 * values in the form
 	 * 
 	 * @param o
 	 *            the object to update
 	 * @throws IllegalArgumentException
 	 *             if the given object is not the same type as the class the
 	 *             form builder was created with
 	 * @throws IllegalAccessException
 	 */
 	public void updateObjectBasedFromForm(Object o)
 			throws IllegalArgumentException, IllegalAccessException {
 
 		if (!o.getClass().equals(c)) {
 			throw new IllegalArgumentException("Incompatiable object");
 		}
 
 		for (Field f : o.getClass().getFields()) {
 			if (f.isAnnotationPresent(FormField.class)) {
 				JComponent input = fieldToComponentMapping.get(f.getName());
 				FormElement fe = typeToFormElementMapping.get(f.getType());
 				f.set(o, fe.getValue(input));
 			}
 		}
 	}
 
 	/**
 	 * Will create and return an object that corresponds to the values in the
 	 * form
 	 * 
 	 * @return an object that will represent the current state of the form
 	 * @throws IllegalArgumentException
 	 * @throws IllegalAccessException
 	 * @throws InstantiationException
 	 */
 	public Object getObjectFromForm() throws IllegalArgumentException,
 			IllegalAccessException, InstantiationException {
 
 		Object o;
 		o = c.newInstance();
 
 		for (Field f : o.getClass().getFields()) {
 			if (f.isAnnotationPresent(FormField.class)) {
 				JComponent input = fieldToComponentMapping.get(f.getName());
 				FormElement fe = typeToFormElementMapping.get(f.getType());
 				f.set(o, fe.getValue(input));
 			}
 		}
 
 		return o;
 	}
 
 	/**
 	 * Given a field will return the input component that will represent that
 	 * field
 	 * 
 	 * @param f
 	 * @return
 	 */
 	private JComponent createFieldFormNameComponent(Field f) {
 
 		JLabel name = new JLabel();
 		name.setText(f.getName());
 		return name;
 	}
 
 }
