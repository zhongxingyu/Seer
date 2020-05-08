 /*
  * Copyright (C) 2013 Zhao Yi
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package zhyi.zse.swing.parser;
 
 import java.awt.AWTException;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Insets;
 import java.awt.Rectangle;
 import java.awt.font.TextAttribute;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.ResourceBundle;
 import javax.swing.AbstractButton;
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.GroupLayout.Group;
 import javax.swing.GroupLayout.ParallelGroup;
 import javax.swing.GroupLayout.SequentialGroup;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JColorChooser;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JSlider;
 import javax.swing.JSpinner;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JToolBar;
 import javax.swing.JTree;
 import javax.swing.KeyStroke;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.RootPaneContainer;
 import javax.swing.UIManager;
 import javax.swing.border.Border;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.html.StyleSheet;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.ProcessingInstruction;
 import org.xml.sax.SAXException;
 import zhyi.zse.conversion.AbstractConverter;
 import zhyi.zse.conversion.Converter;
 import zhyi.zse.conversion.ConverterManager;
 import zhyi.zse.i18n.FallbackLocaleControl;
 import zhyi.zse.lang.AbstractInvocationHandler;
 import zhyi.zse.lang.ReflectionUtils;
 import zhyi.zse.lang.StringUtils;
 import zhyi.zse.lang.StringUtils.DelimitationStyle;
 import zhyi.zse.swing.MultiValueSelector;
 import zhyi.zse.swing.SingleValueSelector;
 import zhyi.zse.swing.SwingUtils;
 
 /**
  * This class eases the way to set up GUI components and layouts with XML files,
  * and provides additional benefits like automatic mnemonic analysis and dynamic
  * locales.
  * <p>
  * For convenience, two static {@code parseGui} methods are provided for mostly
  * common usages with default parsing configurations.
  *
  * @author Zhao Yi
  */
 public class GuiParser {
     private static final GuiParser SHARED_PARSER = new GuiParser();
     private static final DocumentBuilder documentBuilder = createDocumentBuilder();
     private static final StyleSheet styleSheet = new StyleSheet();
     private static final Constructor<?> cssBorderConstructor
             = ReflectionUtils.getDeclaredConstructor(
                     ReflectionUtils.getClass("javax.swing.text.html.CSSBorder"),
                     AttributeSet.class);
     private static final Map<Class<?>, BeanMate> beanMateMap = new HashMap<>();
     private static final Map<Class<?>, ListenerMate> listenerMateMap = new HashMap<>();
 
     private Object controller;
     private Class<?> controllerClass;
     private ClassLoader controllerLoader;
     private Map<String, Object> objectMap;
     private Map<String, Class<?>> importMap;
     private List<String> starImports;
     private boolean autoMnemonic;
     private boolean dynamicLocale;
     private String bundle;
     private ConverterManager converterManager;
     private List<BeanProcessor> beanProcessors;
 
     /**
      * Constructs a new instance.
      */
     public GuiParser() {
         objectMap = new HashMap<>();
         importMap = new HashMap<>();
         starImports = new ArrayList<>();
         beanProcessors = new ArrayList<>();
         converterManager = new ConverterManager();
         converterManager.register(Border.class, new AsObjectOnly<Border>() {
             @Override
             protected Border asObjectInternal(String literalValue) {
                 try {
                     return (Border) cssBorderConstructor.newInstance(
                             styleSheet.getDeclaration("border:" + literalValue));
                 } catch (ReflectiveOperationException ex) {
                     throw new RuntimeException(ex);
                 }
             }
         });
         converterManager.register(Color.class, new AsObjectOnly<Color>() {
             @Override
             protected Color asObjectInternal(String literalValue) {
                 return styleSheet.stringToColor(literalValue);
             }
         });
         converterManager.register(Cursor.class, new AsObjectOnly<Cursor>() {
             @Override
             protected Cursor asObjectInternal(String literalValue) {
                 try {
                     return Cursor.getPredefinedCursor(Integer.parseInt(literalValue));
                 } catch (IllegalArgumentException iae) {
                     try {
                         return Cursor.getSystemCustomCursor(literalValue);
                     } catch (AWTException awte) {
                         IllegalArgumentException ex = new IllegalArgumentException(awte);
                         ex.addSuppressed(iae);
                         throw ex;
                     }
                 }
             }
         });
         converterManager.register(Dimension.class, new AsObjectOnly<Dimension>() {
             @Override
             protected Dimension asObjectInternal(String literalValue) {
                 List<String> dimensions = StringUtils.split(literalValue, ",", true, 1);
                 return new Dimension(Integer.parseInt(dimensions.get(0)),
                         Integer.parseInt(dimensions.get(1)));
             }
         });
         converterManager.register(Font.class, new AsObjectOnly<Font>() {
             @Override
             protected Font asObjectInternal(String literalValue) {
                 return styleSheet.getFont(
                         styleSheet.getDeclaration("font:" + literalValue));
             }
         });
         converterManager.register(Icon.class, new AsObjectOnly<Icon>() {
             @Override
             protected Icon asObjectInternal(String literalValue) {
                 URL url = controllerClass.getResource(literalValue);
                 if (url == null) {
                     url = controllerLoader.getResource(literalValue);
                     if (url == null) {
                         url = ClassLoader.getSystemResource(literalValue);
                     }
                 }
                 return url == null ? new ImageIcon(literalValue) : new ImageIcon(url);
             }
         });
         converterManager.register(Insets.class, new AsObjectOnly<Insets>() {
             @Override
             protected Insets asObjectInternal(String literalValue) {
                 List<String> parameters = StringUtils.split(literalValue, ",", true, 3);
                 return new Insets(Integer.parseInt(parameters.get(0)),
                         Integer.parseInt(parameters.get(1)),
                         Integer.parseInt(parameters.get(2)),
                         Integer.parseInt(parameters.get(3)));
             }
         });
         converterManager.register(Rectangle.class, new AsObjectOnly<Rectangle>() {
             @Override
             protected Rectangle asObjectInternal(String literalValue) {
                 List<String> parameters = StringUtils.split(literalValue, ",", true, 3);
                 return new Rectangle(Integer.parseInt(parameters.get(0)),
                         Integer.parseInt(parameters.get(1)),
                         Integer.parseInt(parameters.get(2)),
                         Integer.parseInt(parameters.get(3)));
             }
         });
         converterManager.register(KeyStroke.class, new AsObjectOnly<KeyStroke>() {
             @Override
             protected KeyStroke asObjectInternal(String literalValue) {
                 return KeyStroke.getKeyStroke(literalValue);
             }
         });
     }
 
     /**
      * Registers a custom converter for string-to-object conversion during
      * parsing the GUI XML file.
      * <p>
      * The converter does not need to implement {@link Converter#asString}.
      * If a converter has already been registered for the type, it will be
      * replaced.
      * <p>
      * By default, {@link GuiParser} supports the following types in addition
      * to the standard converters provided by {@link ConverterManager} itself:
      * <dl>
      * <dt><b>{@link Border}</b>
      * <dd>The literal value is a CSS border declaration, e.g. "{@code 1px solid
      * black}".
      * <dt><b>{@link Color}</b>
      * <dd>The literal value is a CSS color declaration, e.g. "{@code red}".
      * <dt><b>{@link Cursor}</b>
      * <dd>The literal value is an integer for a {@link Cursor#getPredefinedCursor
      * predefined cursor}, or a string for a {@link Cursor#getSystemCustomCursor
      * system-specific custom cursor}, e.g. "{@code #{Cursor.HAND_CURSOR}}".
      * <dt><b>{@link Dimension}</b>
      * <dd>The literal value is a CSV string, e.g. "{@code 5, 5, 5, 5}".
      * <dt><b>{@link Font}</b>
      * <dd>The literal value is a CSS font declaration, e.g. "{@code italic bold}".
      * <dt><b>{@link Icon}</b>
      * <dd>The literal value is the path to the class path resource or file,
      * e.g. "{@code com/abc/Icon.png}".
      * <dt><b>{@link Rectangle}</b>
      * <dd>The literal value is a CSV string, e.g. "{@code 5, 5, 5, 5}".
      * <dt><b>{@link KeyStroke}</b>
      * <dd>The literal value is a string used by {@link KeyStroke#getKeyStroke(String)},
      * e.g. "{@code ctrl C}".
      * </dl>
      *
      * @param <T> The type supported by the converter.
      *
      * @param type      The class of the supported type.
      * @param converter The converter to be registered.
      */
     public <T> void registerConverter(Class<T> type, Converter<T> converter) {
         converterManager.register(type, converter);
     }
 
     /**
      * Registers a custom bean processor to this parser.
      * <p>
      * All registered bean processors will be called after a bean has been
      * parsed by the standard parser.
      *
      * @param beanProcessor The bean processor to be registered.
      */
     public void registerBeanProcessor(BeanProcessor beanProcessor) {
         beanProcessors.add(beanProcessor);
     }
 
     /**
      * Parses the specified controller's associating GUI XML file to initialize
      * the GUI components defined in the controller.
      * <p>
      * This method is a convenient variant of {@link #parse(Object, Map)
      * parse(Object, Map)}
      *
      * @param controller The controller instance.
      */
     public void parse(Object controller) {
         parse(controller, null);
     }
 
     /**
      * Parses the specified controller's associating GUI XML file to initialize
      * the GUI components defined in the controller.
      * <p>
      * The controller's associated GUI XML file must be located in the same
      * package as the controller's class, and named as "{@code
      * <controller_class_simple_name>.xml}". For example, if the controller's
      * class is "{@code com.abc.Controller}", its associating GUI XML file must
      * be located in package "{@code com.abc}", and named as "{@code Controller.xml}".
      * <p>
      * The controller's class can optionally declares fields with their types
      * and names matching the elements declared in the GUI XML file. The GUI XML
      * file can have an element to represent the controller itself, and that
      * element's tag must be named as the simple name of the controller's class.
      * This is typically useful when the controller is a GUI component.
      * <p>
      * The {@code existingObjectMap} may contain already created objects. While
      * parsing an element, if the element's ID is already associated with a object
      * in this map, that object is directly used instead of reflectively creating
      * a new one with the default constructor.
      *
      * @param controller        The controller.
      * @param existingObjectMap A map containing existing ID-object pairs;
      *                          may be {@code null}.
      */
     public void parse(Object controller,
             Map<String, Object> existingObjectMap) {
         this.controller = controller;
         controllerClass = controller.getClass();
         controllerLoader = controllerClass.getClassLoader();
         objectMap.clear();
         if (existingObjectMap != null) {
             objectMap.putAll(existingObjectMap);
         }
         importMap.clear();
         starImports.clear();
         starImports.add("java.lang.");
         starImports.add("java.util.");
         starImports.add("java.awt.");
         starImports.add("java.awt.event.");
         starImports.add("javax.swing.");
         starImports.add("javax.swing.event.");
         starImports.add("zhyi.zse.swing.");
         starImports.add("zhyi.zse.swing.event.");
         autoMnemonic = true;
         dynamicLocale = true;
 
         try {
             String guiFile = controllerClass.getSimpleName() + ".xml";
             for (Node node : iterable(documentBuilder.parse(
                    controllerClass.getResourceAsStream(guiFile)).getChildNodes())) {
                 switch (node.getNodeType()) {
                     case Node.PROCESSING_INSTRUCTION_NODE:
                         ProcessingInstruction pi = (ProcessingInstruction) node;
                         String data = pi.getData();
                         switch (pi.getTarget()) {
                             case "import":
                                 if (data.endsWith("*")) {
                                     starImports.add(data.substring(
                                             0, data.length() - 1));
                                 } else {
                                     Class<?> c = ReflectionUtils.getClass(
                                             data, true, controllerLoader);
                                     importMap.put(c.getSimpleName(), c);
                                 }
                                 break;
                             case "resource":
                                 bundle = data;
                                 break;
                             case "autoMnemonic":
                                 autoMnemonic = Boolean.parseBoolean(data);
                                 break;
                             case "dynamicLocale":
                                 dynamicLocale = Boolean.parseBoolean(data);
                         }
                         break;
                     case Node.ELEMENT_NODE:    // The unique root node.
                         for (Element child : getChildren((Element) node, "*")) {
                             createBean(child);
                         }
                 }
             }
         } catch (SAXException | IOException ex) {
             throw new RuntimeException(ex);
         }
     }
 
     @SuppressWarnings("unchecked")
     private Object createBean(Element e) {
         Object bean;
         if (e.getTagName().equals(controllerClass.getSimpleName())) {
             bean = controller;
         } else {
             bean = ReflectionUtils.newInstance(getClass(e.getTagName()));
         }
 
         String id = getAttribute(e, "id");
         if (id != null) {
             objectMap.put(id, bean);
             if (bean != controller) {
                 Field field = ReflectionUtils.getDeclaredField(controllerClass, id);
                 if (field != null) {
                     ReflectionUtils.setValue(field, controller, bean);
                 }
             }
         }
 
         setProperties(bean, e.getAttributes());
         if (autoMnemonic) {
             if (bean instanceof JLabel) {
                 JLabel label = (JLabel) bean;
                 SwingUtils.setTextWithMnemonic(label, label.getText());
             } else if (bean instanceof AbstractButton) {
                 AbstractButton button = (AbstractButton) bean;
                 SwingUtils.setTextWithMnemonic(button, button.getText());
             }
         }
 
         if (dynamicLocale) {
             if (bean instanceof JFileChooser || bean instanceof JColorChooser) {
                 final JComponent c = (JComponent) bean;
                 c.addPropertyChangeListener("locale", new PropertyChangeListener() {
                     @Override
                     public void propertyChange(PropertyChangeEvent evt) {
                         c.updateUI();
                     }
                 });
             } else if (bean instanceof JComboBox || bean instanceof JList
                     || bean instanceof JSpinner || bean instanceof JTable
                     || bean instanceof JTree || bean instanceof JSlider) {
                 final JComponent c = (JComponent) bean;
                 c.addPropertyChangeListener("locale", new PropertyChangeListener() {
                     @Override
                     public void propertyChange(PropertyChangeEvent evt) {
                         c.revalidate();
                         c.repaint();
                     }
                 });
             }
         }
 
         for (Element child : getChildren(e, "*")) {
             String name = child.getTagName();
             switch (name) {
                 case "layout":
                     Container host = bean instanceof RootPaneContainer
                             ? ((RootPaneContainer) bean).getContentPane()
                             : (Container) bean;
                     if (host.getLayout() instanceof BorderLayout) {
                         JPanel panel = new JPanel();
                         host.add(panel, BorderLayout.CENTER);
                         host = panel;
                     }
                     setLayout(host, child);
                     break;
                 case "component":
                     if (bean instanceof Container) {
                         ((Container) bean).add((Component)
                                 objectMap.get(getAttribute(child, "ref")));
                     }
                     break;
                 case "separator":
                     if (bean instanceof JMenu) {
                         ((JMenu) bean).addSeparator();
                     } else if (bean instanceof JPopupMenu) {
                         ((JPopupMenu) bean).addSeparator();
                     } else if (bean instanceof JToolBar) {
                         ((JToolBar) bean).addSeparator(
                                 evaluate(getAttribute(child, "size"),
                                         Dimension.class, null));
                     }
                     break;
                 case "button":
                     if (bean instanceof ButtonGroup) {
                         ((ButtonGroup) bean).add((AbstractButton)
                                 objectMap.get(getAttribute(child, "ref")));
                     } else if (bean instanceof SingleValueSelector) {
                         SingleValueSelector<Object> svs = (SingleValueSelector<Object>) bean;
                         Class<?> valueClass = getClass(getAttribute(e, "valueClass"));
                         AbstractButton button = (AbstractButton)
                                 objectMap.get(getAttribute(child, "ref"));
                         svs.add(button, evaluate(getAttribute(child, "value"),
                                         valueClass, null));
                         if (evaluate(getAttribute(child, "selected"),
                                 Boolean.class, Boolean.FALSE)) {
                             button.setSelected(true);
                         }
                     } else if (bean instanceof MultiValueSelector) {
                         MultiValueSelector<Object> mvs = (MultiValueSelector<Object>) bean;
                         Class<?> valueClass = getClass(getAttribute(e, "valueClass"));
                         AbstractButton button = (AbstractButton)
                                 objectMap.get(getAttribute(child, "ref"));
                         mvs.add(button, evaluate(getAttribute(child, "value"),
                                         valueClass, null));
                         if (evaluate(getAttribute(child, "selected"),
                                 Boolean.class, Boolean.FALSE)) {
                             button.setSelected(true);
                         }
                     }
                     break;
                 case "tab":
                     JTabbedPane tp = (JTabbedPane) bean;
                     Component c = (Component) objectMap.get(getAttribute(child, "ref"));
                     tp.addTab(evaluate(getAttribute(child, "title"), String.class, c.getName()),
                             evaluate(getAttribute(child, "icon"), Icon.class, null),
                             c, evaluate(getAttribute(child, "tip"), String.class, null));
                     String tab = getAttribute(child, "tab");
                     if (tab != null) {
                         tp.setTabComponentAt(tp.getTabCount() - 1,
                                 (Component) objectMap.get(tab));
                     }
                     break;
                 case "label":
                     JSlider slider = (JSlider) bean;
                     Dictionary<Integer, Component> labelTable = slider.getLabelTable();
                     if (labelTable == null) {
                         labelTable = new Hashtable<>();
                         slider.setLabelTable(labelTable);
                     }
                     Integer value = evaluate(getAttribute(child, "value"), Integer.class);
                     final String labelExp = getAttribute(child, "label");
                     Component label = (Component) objectMap.get(labelExp);
                     if (label == null) {
                         label = new JLabel(evaluate(labelExp, String.class));
                         if (dynamicLocale && isLocalizable(labelExp)) {
                             label.addPropertyChangeListener("locale",
                                     new PropertyChangeListener() {
                                 @Override
                                 public void propertyChange(PropertyChangeEvent evt) {
                                     ((JLabel) evt.getSource()).setText(
                                             evaluate(labelExp, String.class));
                                 }
                             });
                         }
                     }
                     labelTable.put(value, label);
                     break;
                 default:
                     if (name.endsWith("Listener")) {
                         addListener(bean, child);
                     }
             }
         }
 
         for (BeanProcessor beanProcessor : beanProcessors) {
             beanProcessor.process(bean, e);
         }
 
         return bean;
     }
 
     private void setProperties(final Object bean, NamedNodeMap attrs) {
         BeanMate bm = getBeanMate(bean.getClass());
         String borderTitle = null;
         for (Node attr : iterable(attrs)) {
             String name = attr.getNodeName();
             final String value = attr.getNodeValue();
             switch (name) {
                 case "id":
                     break;
                 case "borderTitle":
                     borderTitle = value;
                     break;
                 case "toolBar":
                     Container host = (Container) bean;
                     host.setLayout(new BorderLayout());
                     host.add(evaluate(value, JToolBar.class, null),
                             BorderLayout.PAGE_START);
                     break;
                 default:
                     final Method setter = bm.setterMap.get(name);
                     if (setter == null) {
                         continue;
                     }
                     final Class<?> propClass = setter.getParameterTypes()[0];
                     Object prop = evaluate(value, propClass);
                     if (prop instanceof Font) {
                         Font font = (Font) prop;
                         JComponent c = (JComponent) bean;
                         if (value.contains(font.getName())) {
                             c.setFont(font);
                         } else {
                             Map<TextAttribute, ?> map = font.getAttributes();
                             map.remove(TextAttribute.FAMILY);
                             c.setFont(c.getFont().deriveFont(map));
                         }
                     } else {
                         ReflectionUtils.invoke(setter, bean, prop);
                         if (dynamicLocale && isLocalizable(value)) {
                             ((Component) bean).addPropertyChangeListener("locale",
                                     new PropertyChangeListener() {
                                 @Override
                                 public void propertyChange(PropertyChangeEvent evt) {
                                     try {
                                         setter.invoke(bean, evaluate(value, propClass));
                                     } catch (ReflectiveOperationException ex) {
                                         throw new RuntimeException(ex);
                                     }
                                 }
                             });
                         }
                     }
             }
         }
 
         if (borderTitle != null) {
             final JComponent c = (JComponent) bean;
             final Border originalBorder = c.getBorder();
             c.setBorder(BorderFactory.createTitledBorder(originalBorder,
                     evaluate(borderTitle, String.class)));
             if (isLocalizable(borderTitle)) {
                 final String borderTitleKey = borderTitle;
                 c.addPropertyChangeListener("locale", new PropertyChangeListener() {
                     @Override
                     public void propertyChange(PropertyChangeEvent evt) {
                         c.setBorder(BorderFactory.createTitledBorder(
                                 originalBorder, evaluate(borderTitleKey, String.class)));
                     }
                 });
             }
         }
     }
 
     private void setLayout(Container host, Element e) {
         GroupLayout gl = new GroupLayout(host);
         setProperties(gl, e.getAttributes());
         host.setLayout(gl);
         List<Element> groups = getChildren(e, "group");
         gl.setHorizontalGroup(createGroup(gl, groups.get(0)));
         gl.setVerticalGroup(createGroup(gl, groups.get(1)));
         for (Element link : getChildren(e, "link")) {
             Integer axis = evaluate(getAttribute(link, "axis"), Integer.class, null);
             List<JComponent> components = new ArrayList<>();
             for (String ref : StringUtils.split(
                     getAttribute(link, "components"), ",", true)) {
                 components.add((JComponent) objectMap.get(ref));
             }
             if (axis == null) {
                 gl.linkSize(components.toArray(new JComponent[components.size()]));
             } else {
                 gl.linkSize(axis, components.toArray(new JComponent[components.size()]));
             }
         }
     }
 
     @SuppressWarnings("null")
     private Group createGroup(GroupLayout gl, Element e) {
         Group group = null;
         switch (getAttribute(e, "type")) {
             case "baseline":
                 group = gl.createBaselineGroup(
                         evaluate(getAttribute(e, "resizable"),
                                 Boolean.class, Boolean.FALSE),
                         evaluate(getAttribute(e, "anchorBaselineToTop"),
                                 Boolean.class, Boolean.FALSE));
                 break;
             case "sequential":
                 group = gl.createSequentialGroup();
                 break;
             case "parallel":
                 group = gl.createParallelGroup(
                         evaluate(getAttribute(e, "alignment"),
                                 Alignment.class, Alignment.LEADING),
                         evaluate(getAttribute(e, "resizable"),
                                 Boolean.class, Boolean.TRUE));
                 break;
         }
 
         int min;
         int pref;
         int max;
         for (Element child : getChildren(e, "*")) {
             switch (child.getTagName()) {
                 case "component":
                     JComponent c = (JComponent) objectMap.get(
                             getAttribute(child, "ref"));
                     pref = evaluate(getAttribute(child, "pref"),
                             Integer.class, GroupLayout.DEFAULT_SIZE);
                     min = evaluate(getAttribute(child, "min"), Integer.class, pref);
                     max = evaluate(getAttribute(child, "max"), Integer.class, pref);
                     if (group instanceof SequentialGroup) {
                         ((SequentialGroup) group).addComponent(
                                 evaluate(getAttribute(child, "useAsBaseline"),
                                         Boolean.class, Boolean.FALSE),
                                 c, min, pref, max);
                     } else {
                         Alignment alignment = evaluate(
                                 getAttribute(child, "alignment"), Alignment.class, null);
                         if (alignment == null) {
                             ((ParallelGroup) group).addComponent(c, min, pref, max);
                         } else {
                             ((ParallelGroup) group).addComponent(
                                     c, alignment, min, pref, max);
                         }
                     }
                     break;
                 case "containerGap":
                     pref = evaluate(getAttribute(child, "pref"),
                             Integer.class, GroupLayout.DEFAULT_SIZE);
                     ((SequentialGroup) group).addContainerGap(pref,
                             evaluate(getAttribute(child, "max"), Integer.class, pref));
                     break;
                 case "gap":
                     pref = evaluate(getAttribute(child, "pref"),
                             Integer.class, GroupLayout.DEFAULT_SIZE);
                     group.addGap(evaluate(getAttribute(child, "min"), Integer.class, pref),
                             pref, evaluate(getAttribute(child, "max"), Integer.class, pref));
                     break;
                 case "preferredGap":
                     JComponent c1 = (JComponent) objectMap.get(getAttribute(child, "component1"));
                     JComponent c2 = (JComponent) objectMap.get(getAttribute(child, "component2"));
                     ComponentPlacement type = evaluate(getAttribute(child, "type"),
                             ComponentPlacement.class, ComponentPlacement.RELATED);
                     pref = evaluate(getAttribute(child, "pref"),
                             Integer.class, GroupLayout.DEFAULT_SIZE);
                     max = evaluate(getAttribute(child, "max"), Integer.class, pref);
                     if (c1 != null && c2 != null) {
                         ((SequentialGroup) group).addPreferredGap(c1, c2, type, pref, max);
                     } else {
                         ((SequentialGroup) group).addPreferredGap(type, pref, max);
                     }
                     break;
                 case "group":
                     if (group instanceof SequentialGroup) {
                         ((SequentialGroup) group).addGroup(
                                 evaluate(getAttribute(child, "useAsBaseline"),
                                         Boolean.class, Boolean.FALSE),
                                 createGroup(gl, child));
                     } else {
                         ((ParallelGroup) group).addGroup(
                                 evaluate(getAttribute(child, "alignment"),
                                         Alignment.class, Alignment.LEADING),
                                 createGroup(gl, child));
                     }
             }
         }
         return group;
     }
 
     private void addListener(Object bean, Element e) {
         ListenerMate lm = getListenerMate(getClass(e.getTagName()));
         final Map<Method, Method> methodMap = new HashMap<>();
         String forProp = null;
         String propName = null;
         for (Node attr : iterable(e.getAttributes())) {
             String name = attr.getNodeName();
             String value = attr.getNodeValue();
             switch (name) {
                 case "for":
                     forProp = value;
                     break;
                 case "propertyName":
                     propName = evaluate(value, String.class, null);
                     break;
                 default:
                     Method listenerMethod = lm.methodMap.get(name);
                     methodMap.put(listenerMethod, ReflectionUtils.getDeclaredMethod(
                             controllerClass, value, listenerMethod.getParameterTypes()));
             }
         }
 
         Object listener = Proxy.newProxyInstance(controllerLoader,
                 new Class<?>[] {lm.listenerClass}, new AbstractInvocationHandler() {
             @Override
             public Object invokeOthers(Object proxy, Method method, Object[] args) {
                 Method controllerMethod = methodMap.get(method);
                 if (controllerMethod != null) {
                     ReflectionUtils.invoke(controllerMethod, controller, args);
                 }
                 return null;
             }
         });
 
         Object target = bean;
         if (forProp != null) {
             String getter = "get" + Character.toUpperCase(forProp.charAt(0))
                     + forProp.substring(1);
             target = ReflectionUtils.invoke(
                     ReflectionUtils.getMethod(bean.getClass(), getter), bean);
         }
 
         if (propName != null && PropertyChangeListener
                 .class.isAssignableFrom(lm.listenerClass)) {
             ReflectionUtils.invoke(
                     ReflectionUtils.getMethod(target.getClass(),
                             "addPropertyChangeListener",
                             String.class, PropertyChangeListener.class),
                     target, listener);
         } else {
             ReflectionUtils.invoke(
                     getBeanMate(target.getClass()).addListenerMap.get(lm.listenerClass),
                     target, listener);
         }
     }
 
     private Class<?> getClass(String name) {
         Class<?> beanClass = importMap.get(name);
         if (beanClass == null) {
             for (String starImport : starImports) {
                 String path = starImport.replace('.', '/') + name + ".class";
                 if (controllerLoader.getResource(path) != null) {
                     beanClass = ReflectionUtils.getClass(
                             starImport + name, true, controllerLoader);
                     importMap.put(name, beanClass);
                     break;
                 }
             }
             if (beanClass == null) {
                 // Treat as FQCN and don't cache it.
                 beanClass = ReflectionUtils.getClass(name, true, controllerLoader);
             }
         }
         return beanClass;
     }
 
     @SuppressWarnings("unchecked")
     private <V> V evaluate(String literalValue, Class<V> valueClass) {
         valueClass = ReflectionUtils.wrap(valueClass);
         Converter<V> converter = converterManager.getConverter(valueClass);
         Object value = literalValue;
         if (literalValue.startsWith("#{") && literalValue.endsWith("}")) {
             String expression = literalValue.substring(2, literalValue.length() - 1);
             List<String> operators = StringUtils.split(expression, ".",
                     DelimitationStyle.IGNORE_DELIMITER, false, 1);
             String type = operators.get(0);
             String target = operators.get(1);
             switch (type) {
                 case "env":
                     value = System.getenv(target);
                     break;
                 case "new":
                     value = ReflectionUtils.newInstance(getClass(target));
                     break;
                 case "ref":
                     value = objectMap.get(target);
                     break;
                 case "res":
                     value = ResourceBundle.getBundle(bundle,
                             Locale.getDefault(), controllerLoader,
                             FallbackLocaleControl.EN_US_CONTROL).getObject(target);
                     break;
                 case "sys":
                     value = System.getProperty(target);
                     break;
                 case "uid":
                     value = UIManager.get(target, Locale.getDefault());
                     break;
                 default:
                     value = ReflectionUtils.getValue(
                             ReflectionUtils.getField(getClass(type), target), null);
             }
         }
         if (valueClass.isInstance(value)) {
             return valueClass.cast(value);
         } else {
             return converter.asObject(value.toString());
         }
     }
 
     @SuppressWarnings("UseSpecificCatch")
     private <V> V evaluate(String literalValue,
             Class<V> valueClass, V nullDefault) {
         return literalValue == null ? nullDefault : evaluate(literalValue, valueClass);
     }
 
     /**
      * A short cut to parse GUI with default configurations.
      *
      * @param controller The controller.
      */
     public static void parseGui(Object controller) {
         SHARED_PARSER.parse(controller);
     }
 
     /**
      * A short cut to parse GUI with default configurations.
      *
      * @param controller        The controller.
      * @param existingObjectMap A map containing existing ID-object pairs;
      *                          may be {@code null}.
      */
     public static void parseGui(Object controller,
             Map<String, Object> existingObjectMap) {
         SHARED_PARSER.parse(controller, existingObjectMap);
     }
 
     private static DocumentBuilder createDocumentBuilder() {
         try {
             return DocumentBuilderFactory.newInstance().newDocumentBuilder();
         } catch (ParserConfigurationException ex) {
             throw new RuntimeException(ex);
         }
     }
 
     private static BeanMate getBeanMate(Class<?> c) {
         BeanMate bm = beanMateMap.get(c);
         if (bm == null) {
             bm = new BeanMate(c);
             beanMateMap.put(c, bm);
         }
         return bm;
     }
 
     private static ListenerMate getListenerMate(Class<?> c) {
         ListenerMate lm = listenerMateMap.get(c);
         if (lm == null) {
             lm = new ListenerMate(c);
             listenerMateMap.put(c, lm);
         }
         return lm;
     }
 
     private static Iterable<Node> iterable(final NodeList nodeList) {
         final int length = nodeList.getLength();
         return new Iterable<Node>() {
             @Override
             public Iterator<Node> iterator() {
                 return new Iterator<Node>() {
                     private int index;
 
                     @Override
                     public boolean hasNext() {
                         return index < length;
                     }
 
                     @Override
                     public Node next() {
                         if (index < length) {
                             Node node = nodeList.item(index);
                             index++;
                             return node;
                         }
                         throw new NoSuchElementException("No more nodes.");
                     }
 
                     @Override
                     public void remove() {
                         throw new UnsupportedOperationException("Not supported.");
                     }
                 };
             }
         };
     }
 
     private static Iterable<Node> iterable(final NamedNodeMap namedNodeMap) {
         final int length = namedNodeMap.getLength();
         return new Iterable<Node>() {
             @Override
             public Iterator<Node> iterator() {
                 return new Iterator<Node>() {
                     private int index;
 
                     @Override
                     public boolean hasNext() {
                         return index < length;
                     }
 
                     @Override
                     public Node next() {
                         if (index < length) {
                             Node node = namedNodeMap.item(index);
                             index++;
                             return node;
                         }
                         throw new NoSuchElementException("No more nodes.");
                     }
 
                     @Override
                     public void remove() {
                         throw new UnsupportedOperationException("Not supported.");
                     }
                 };
             }
         };
     }
 
     private static String getAttribute(Element e, String name) {
         Node attr = e.getAttributeNode(name);
         return attr == null ? null : attr.getNodeValue();
     }
 
     private static List<Element> getChildren(Element e, String name) {
         List<Element> children = new ArrayList<>();
         for (Node child : iterable(e.getChildNodes())) {
             if (child.getNodeType() == Node.ELEMENT_NODE
                     && (name.equals("*") || name.equals(child.getNodeName()))) {
                 children.add((Element) child);
             }
         }
         return children;
     }
 
     private static boolean isLocalizable(String exp) {
         return (exp.startsWith("#{res.") || exp.startsWith("#{uid."))
                 && exp.endsWith("}");
     }
 
     private static class BeanMate {
         private Map<String, Method> setterMap;
         private Map<Class<?>, Method> addListenerMap;
 
         private BeanMate(Class<?> beanClass) {
             setterMap = new HashMap<>();
             addListenerMap = new HashMap<>();
             for (Method method : beanClass.getMethods()) {
                 if (method.getParameterTypes().length == 1) {
                     String name = method.getName();
                     if (name.startsWith("set")) {
                         setterMap.put(Character.toLowerCase(
                                 name.charAt(3)) + name.substring(4), method);
                     }
                     if (name.startsWith("add") && name.endsWith("Listener")) {
                         addListenerMap.put(method.getParameterTypes()[0], method);
                     }
                 }
             }
         }
     }
 
     private static class ListenerMate {
         private Class<?> listenerClass;
         private Map<String, Method> methodMap;
 
         private ListenerMate(Class<?> listenerClass) {
             this.listenerClass = listenerClass;
             methodMap = new HashMap<>();
             for (Method method : listenerClass.getMethods()) {
                 methodMap.put(method.getName(), method);
             }
         }
     }
 
     private static abstract class AsObjectOnly<T> extends AbstractConverter<T> {
         @Override
         protected String asStringInternal(T object) {
             throw new UnsupportedOperationException("Not supported.");
         }
     }
 }
