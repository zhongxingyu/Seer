 /*
  * $Id$
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.spec;
 
 import java.awt.*;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 import javax.swing.*;
 import javax.swing.text.JTextComponent;
 
 import org.xins.common.types.EnumItem;
 import org.xins.common.types.EnumType;
 import org.xins.common.types.Type;
 
 /**
  * Graphical user interface that allows to browse the specification of an API
  * and execute the functions of this API.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:anthony.goubard@orange-ftgroup.com">Anthony Goubard</a>
  *
  * @since XINS 2.1
  */
 public class TestFormPanel extends JPanel {
 
    private APISpec apiSpec;
    
    private String functionName;
 
    private java.util.List parameterComponents;
 
    private ActionListener submitListener;
 
    private Color tfBackground;
    
    private Color tfInvalidColor;
 
    /**
     * Constructs a new <code>SpecGUI</code>.
     * 
     * @param apiSpec
     *    the specification of the API.
     * 
     * @param functionName
     *    the specification of the API.
     */
    public TestFormPanel(APISpec apiSpec, String functionName, ActionListener submitListener) {
       this.apiSpec = apiSpec;
       this.functionName = functionName;
       this.submitListener = submitListener;
       try {
          initUI();
       } catch (Exception ex) {
          ex.printStackTrace();
       }
       initData();
    }
 
    /**
     * Creates the user interface.
     */
    protected void initUI() throws Exception {
       FunctionSpec functionSpec = apiSpec.getFunction(functionName);
       setLayout(new BorderLayout(5,5));
       JLabel jlFunctionName = new JLabel(functionName + " function") {
          public void paint(Graphics g) {
             Graphics2D g2 = (Graphics2D) g;
             Color background = getBackground();
             
             Paint oldPaint = g2.getPaint();
             GradientPaint gradient = new GradientPaint(0.0f, 0.0f, background.brighter(), 
                   TestFormPanel.this.getWidth() + 0.1f, getHeight() + 0.1f, background.darker());
             g2.setPaint(gradient);
             g2.fill(new Rectangle(TestFormPanel.this.getWidth(), getHeight()));
             g2.setPaint(oldPaint);
             super.paint(g);
          }
       };
       jlFunctionName.setOpaque(false);
       jlFunctionName.setFont(jlFunctionName.getFont().deriveFont(20.0f));
       jlFunctionName.setToolTipText(functionSpec.getDescription());
       add(jlFunctionName, BorderLayout.NORTH);
       
       Map inputParameters = functionSpec.getInputParameters();
       boolean hasInputDataSection = functionSpec.getInputDataSectionElements().size() > 0;
       parameterComponents = new ArrayList();
       //JPanel paramNamesPanel = new JPanel();
       //JPanel paramValuesPanel = new JPanel();
       tfBackground = UIManager.getColor("TextField.background");
       tfInvalidColor = new Color(
             Math.min(tfBackground.getRed() + 30, 255), 
             Math.max(tfBackground.getGreen() - 15, 0), 
             Math.max(tfBackground.getBlue() - 20, 0));
       JPanel paramsPanel = new JPanel();
       GridBagLayout gridbag = new GridBagLayout();
       GridBagConstraints c = new GridBagConstraints();
       paramsPanel.setLayout(gridbag);
       c.fill = GridBagConstraints.HORIZONTAL;
       c.anchor = GridBagConstraints.WEST;
       c.insets = new Insets(2,5,2,5);
       Iterator itInputParameters = inputParameters.values().iterator();
       while (itInputParameters.hasNext()) {
          ParameterSpec inputSpec = (ParameterSpec) itInputParameters.next();
          JLabel jlInput = new JLabel(inputSpec.getName() + ":");
          jlInput.setToolTipText(inputSpec.getDescription());
          c.weightx = 0.2;
          c.gridwidth = 1;
          gridbag.setConstraints(jlInput, c);
          paramsPanel.add(jlInput);
          JComponent inputField = createInputComponent(inputSpec);
          c.weightx = 1.0;
          c.gridwidth = 2;
          gridbag.setConstraints(inputField, c);
          paramsPanel.add(inputField);
          parameterComponents.add(inputField);
          c.gridwidth = GridBagConstraints.REMAINDER;
          c.weightx = 0.2;
          JLabel jlBlank = new JLabel();
          gridbag.setConstraints(jlBlank, c);
          paramsPanel.add(jlBlank);
       }
       if (hasInputDataSection) {
          JLabel jlInput = new JLabel("Data section:");
          c.weightx = 0.2;
          c.gridwidth = 1;
          gridbag.setConstraints(jlInput, c);
          paramsPanel.add(jlInput);
          JTextArea inputField = new JTextArea(8,40);
          inputField.putClientProperty("PARAM_NAME", "_data");
          c.weightx = 1.0;
          c.gridwidth = 2;
          c.fill = GridBagConstraints.BOTH;
          gridbag.setConstraints(inputField, c);
          paramsPanel.add(new JScrollPane(inputField));
          parameterComponents.add(inputField);
       }
       add(paramsPanel, BorderLayout.CENTER);
       
       JPanel submitPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
       JButton jbSubmit = new JButton("Submit");
       jbSubmit.addActionListener(submitListener);
       submitPanel.add(jbSubmit);
       add(submitPanel, BorderLayout.SOUTH);
    }
 
    protected void initData() {
    }
 
    protected JComponent createInputComponent(final ParameterSpec inputSpec) {
       final JComponent inputField;
       final Type inputType = inputSpec.getType();
      String defaultValue = inputSpec.getDefault();

       if (inputType instanceof EnumType) {
          inputField = new JComboBox();
          Iterator itItems = ((EnumType) inputType).getEnumItems().iterator();
          if (!inputSpec.isRequired()) {
             ((JComboBox) inputField).addItem("");
          }
          while (itItems.hasNext()) {
             EnumItem item = (EnumItem) itItems.next();
            ((JComboBox) inputField).addItem(item.getValue());
          }
       } else if (inputType instanceof org.xins.common.types.standard.Boolean) {
          if (inputSpec.isRequired()) {
             inputField = new JCheckBox();
            if ("true".equals(defaultValue)) {
               ((JCheckBox) inputField).setSelected(true);
            }
          } else {
             inputField = new JComboBox();
             ((JComboBox) inputField).addItem("");
             ((JComboBox) inputField).addItem("true");
             ((JComboBox) inputField).addItem("false");
            if (defaultValue != null) {
               ((JComboBox) inputField).setSelectedItem(defaultValue);
            }
          }
       } else {
          inputField = new JTextField(20);
         if (inputSpec.isRequired() && defaultValue == null) {
             inputField.setBackground(tfInvalidColor);
          }
          inputField.addKeyListener(new KeyAdapter() {
             public void keyTyped(KeyEvent ke) {
                String text = ((JTextField) inputField).getText();
                if (!ke.isActionKey()) {
                   text += ke.getKeyChar();
                }
                if (inputType.isValidValue(text) || (text.equals("") && !inputSpec.isRequired())) {
                   inputField.setBackground(tfBackground);
                } else {
                   inputField.setBackground(tfInvalidColor);
                }
             }
          });
         if (defaultValue != null) {
            ((JTextField) inputField).setText(defaultValue);
          }
       }
       inputField.setToolTipText(inputType.getName() + ": " + inputType.getDescription());
       inputField.putClientProperty("PARAM_NAME", inputSpec.getName());
       return inputField;
    }
 
    /**
     * Gets the list of parameters in a URL form.
     * 
     * @return
     *    the list of the parameters as it should be send to the URL 
     *    (starting with an '&') or an empty String if no parameter is set.
     */
    public String getParameters() {
       String result = "";
       Iterator itParameters = parameterComponents.iterator();
       while (itParameters.hasNext()) {
          JComponent inputComponent = (JComponent) itParameters.next();
          String paramName = (String) inputComponent.getClientProperty("PARAM_NAME");
          String paramValue = "";
          if (inputComponent instanceof JTextComponent) {
             paramValue = ((JTextComponent) inputComponent).getText();
         } else if (inputComponent instanceof JComboBox) {
            paramValue = ((JComboBox) inputComponent).getSelectedItem().toString();
         } else if (inputComponent instanceof JCheckBox) {
            paramValue = ((JCheckBox) inputComponent).isSelected() ? "true" : "false";
          }
          if (!"".equals(paramValue)) {
             result += "&" + paramName + "=" + paramValue;
          }
       }
       return result;
    }
 }
