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
 package zhyi.zse.swing;
 
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
 import java.io.InputStream;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.ResourceBundle;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
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
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JTabbedPane;
 import javax.swing.JToolBar;
 import javax.swing.KeyStroke;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.RootPaneContainer;
 import javax.swing.UIManager;
 import javax.swing.border.Border;
 import javax.swing.plaf.basic.BasicHTML;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.html.StyleSheet;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import org.w3c.dom.Document;
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
 import zhyi.zse.lang.ReflectionUtils;
 import zhyi.zse.lang.StringUtils;
 import zhyi.zse.lang.StringUtils.DelimitationStyle;
 
 /**
  * Provides utility methods to parse GUI XML.
  *
  * @author Zhao Yi
  */
 public class GuiParser {
     private static final Pattern MNEMONIC_PATTERN = Pattern.compile("_._");
     private static final DocumentBuilder DOCUMENT_BUILDER = createDocumentBuilder();
     private static final StyleSheet STYLE_SHEET = new StyleSheet();
     private static final Constructor<?> CSS_BORDER_CONSTRUCTOR
             = ReflectionUtils.getDeclaredConstructor(
                     ReflectionUtils.getClass("javax.swing.text.html.CSSBorder"),
                     AttributeSet.class);
     private static final ConverterManager CONVERTER_MANAGER = new ConverterManager();
     static {
         CONVERTER_MANAGER.register(Border.class, new AsObjectOnly<Border>() {
             @Override
             protected Border asObjectInternal(String literalValue) {
                 try {
                     return (Border) CSS_BORDER_CONSTRUCTOR.newInstance(
                             STYLE_SHEET.getDeclaration("border:" + literalValue));
                 } catch (ReflectiveOperationException ex) {
                     throw new RuntimeException(ex);
                 }
             }
         });
         CONVERTER_MANAGER.register(Color.class, new AsObjectOnly<Color>() {
             @Override
             protected Color asObjectInternal(String literalValue) {
                 return STYLE_SHEET.stringToColor(literalValue);
             }
         });
         CONVERTER_MANAGER.register(Cursor.class, new AsObjectOnly<Cursor>() {
             @Override
             protected Cursor asObjectInternal(String literalValue) {
                 try {
                     return Cursor.getPredefinedCursor(Integer.parseInt(literalValue));
                 } catch (IllegalArgumentException ex1) {
                     try {
                         return Cursor.getSystemCustomCursor(literalValue);
                     } catch (AWTException ex2) {
                         throw new IllegalArgumentException(ex2);
                     }
                 }
             }
         });
         CONVERTER_MANAGER.register(Dimension.class, new AsObjectOnly<Dimension>() {
             @Override
             protected Dimension asObjectInternal(String literalValue) {
                 List<String> dimensions = StringUtils.split(literalValue, ",", true, 1);
                 return new Dimension(Integer.parseInt(dimensions.get(0)),
                         Integer.parseInt(dimensions.get(1)));
             }
         });
         CONVERTER_MANAGER.register(Font.class, new AsObjectOnly<Font>() {
             @Override
             protected Font asObjectInternal(String literalValue) {
                 return STYLE_SHEET.getFont(
                         STYLE_SHEET.getDeclaration("font:" + literalValue));
             }
         });
         CONVERTER_MANAGER.register(Insets.class, new AsObjectOnly<Insets>() {
             @Override
             protected Insets asObjectInternal(String literalValue) {
                 List<String> parameters = StringUtils.split(literalValue, ",", true, 3);
                 return new Insets(Integer.parseInt(parameters.get(0)),
                         Integer.parseInt(parameters.get(1)),
                         Integer.parseInt(parameters.get(2)),
                         Integer.parseInt(parameters.get(3)));
             }
         });
         CONVERTER_MANAGER.register(Rectangle.class, new AsObjectOnly<Rectangle>() {
             @Override
             protected Rectangle asObjectInternal(String literalValue) {
                 List<String> parameters = StringUtils.split(literalValue, ",", true, 3);
                 return new Rectangle(Integer.parseInt(parameters.get(0)),
                         Integer.parseInt(parameters.get(1)),
                         Integer.parseInt(parameters.get(2)),
                         Integer.parseInt(parameters.get(3)));
             }
         });
         CONVERTER_MANAGER.register(KeyStroke.class, new AsObjectOnly<KeyStroke>() {
             @Override
             protected KeyStroke asObjectInternal(String literalValue) {
                 return KeyStroke.getKeyStroke(literalValue);
             }
         });
     }
 
     private Document guiXml;
     private ClassLoader controllerLoader;
     private Class<?> controllerClass;
     private Object controller;
     private Map<String, Class<?>> importMap;
     private List<String> starImports;
     private Map<Class<?>, BeanMate> beanMateMap;
     private Map<Class<?>, ListenerMate> listenerMateMap;
     private Map<String, Object> objectMap;
     private String resourceBundleName;
     private boolean autoMnemonic;
 
     private GuiParser(Document guiXml, Object controller,
             Map<String, Object> existingObjectMap) {
         this.guiXml = guiXml;
         this.controller = controller;
         controllerClass = controller.getClass();
         controllerLoader = controllerClass.getClassLoader();
         importMap = new HashMap<>();
         starImports = new ArrayList<>();
         starImports.add("java.lang.");
         starImports.add("java.util.");
         starImports.add("java.awt.");
         starImports.add("java.awt.event.");
         starImports.add("javax.swing.");
         starImports.add("javax.swing.event.");
         beanMateMap = new HashMap<>();
         listenerMateMap = new HashMap<>();
         objectMap = new HashMap<>();
         if (existingObjectMap != null) {
             objectMap.putAll(existingObjectMap);
         }
         CONVERTER_MANAGER.register(Icon.class, new AsObjectOnly<Icon>() {
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
     }
 
     private void parse() {
         for (Node node : iterable(guiXml.getChildNodes())) {
             switch (node.getNodeType()) {
                 case Node.PROCESSING_INSTRUCTION_NODE:
                     ProcessingInstruction pi = (ProcessingInstruction) node;
                     String data = pi.getData();
                     switch (pi.getTarget()) {
                         case "import":
                             if (data.endsWith("*")) {
                                 starImports.add(data.substring(0, data.length() - 1));
                             } else {
                                 Class<?> c = ReflectionUtils.getClass(
                                         data, true, controllerLoader);
                                 importMap.put(c.getSimpleName(), c);
                             }
                             break;
                         case "resource":
                             resourceBundleName = data;
                             break;
                         case "autoMnemonic":
                             autoMnemonic = Boolean.parseBoolean(data);
                     }
                     break;
                 case Node.ELEMENT_NODE:    // The unique root node.
                     for (Element child : getChildren((Element) node, "*")) {
                         createBean(child);
                     }
             }
         }
         for (final Method m : controllerClass.getDeclaredMethods()) {
             if (m.isAnnotationPresent(PostParseGui.class)) {
                 ReflectionUtils.invoke(m, controller);
                 break;
             }
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
                 setMnemonic((JLabel) bean);
             } else if (bean instanceof AbstractButton) {
                 setMnemonic((AbstractButton) bean);
             }
         }
 
         if (bean instanceof JFileChooser || bean instanceof JColorChooser) {
             final JComponent c = (JComponent) bean;
             c.addPropertyChangeListener("locale", new PropertyChangeListener() {
                 @Override
                 public void propertyChange(PropertyChangeEvent evt) {
                     c.updateUI();
                 }
             });
         } else if (bean instanceof JMenuBar) {
             for (Element child : getChildren(e, "component")) {
                 ((JMenuBar) bean).add((Component) createBean(child));
             }
         } else if (bean instanceof JMenu) {
             JMenu m = (JMenu) bean;
             for (Element child : getChildren(e, "*")) {
                 switch (child.getTagName()) {
                     case "component":
                         m.add((Component) objectMap.get(getAttribute(child, "ref")));
                         break;
                     case "separator":
                         m.addSeparator();
                         break;
                 }
             }
         } else if (bean instanceof JPopupMenu) {
             JPopupMenu pm = (JPopupMenu) bean;
             for (Element child : getChildren(e, "*")) {
                 switch (child.getTagName()) {
                     case "component":
                         pm.add((Component) objectMap.get(getAttribute(child, "ref")));
                         break;
                     case "separator":
                         pm.addSeparator();
                         break;
                 }
             }
         } else if (bean instanceof JToolBar) {
             JToolBar tb = (JToolBar) bean;
             for (Element child : getChildren(e, "*")) {
                 switch (child.getTagName()) {
                     case "component":
                         tb.add((Component) objectMap.get(getAttribute(child, "ref")));
                         break;
                     case "separator":
                         tb.addSeparator();
                         break;
                 }
             }
         } else if (bean instanceof JTabbedPane) {
             for (Element child : getChildren(e, "component")) {
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
             }
         } else if (bean instanceof ButtonGroup) {
             for (Element child : getChildren(e, "button")) {
                 ((ButtonGroup) bean).add((AbstractButton)
                         objectMap.get(getAttribute(child, "ref")));
             }
         } else if (bean instanceof SingleValueSelector) {
             SingleValueSelector<Object> svs = (SingleValueSelector<Object>) bean;
             Class<?> valueClass = getClass(getAttribute(e, "valueClass"));
             for (Element child : getChildren(e, "button")) {
                 AbstractButton button = (AbstractButton)
                         objectMap.get(getAttribute(child, "ref"));
                 svs.add(button, evaluate(getAttribute(child, "value"),
                         valueClass, null));
                 if (evaluate(getAttribute(child, "selected"),
                         Boolean.class, Boolean.FALSE)) {
                     button.setSelected(true);
                 }
             }
         } else if (bean instanceof MultiValueSelector) {
             MultiValueSelector<Object> mvs = (MultiValueSelector<Object>) bean;
             Class<?> valueClass = getClass(getAttribute(e, "valueClass"));
             for (Element child : getChildren(e, "button")) {
                 AbstractButton button = (AbstractButton)
                         objectMap.get(getAttribute(child, "ref"));
                 mvs.add(button, evaluate(getAttribute(child, "value"),
                         valueClass, null));
                 if (evaluate(getAttribute(child, "selected"),
                         Boolean.class, Boolean.FALSE)) {
                     button.setSelected(true);
                 }
             }
         }
 
         for (Element child : getChildren(e, "*")) {
             String name = child.getTagName();
             if (name.equals("layout")) {
                 Container host = bean instanceof RootPaneContainer
                         ? ((RootPaneContainer) bean).getContentPane() : (Container) bean;
                 if (host.getLayout() instanceof BorderLayout) {
                     JPanel panel = new JPanel();
                     host.add(panel, BorderLayout.CENTER);
                     host = panel;
                 }
                 layout(host, child);
             } else if (name.endsWith("Listener")) {
                 addListener(bean, child);
             }
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
                         if (value.startsWith("#{res.") && value.endsWith("}")) {
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
             if (borderTitle.startsWith("#{res.") && borderTitle.endsWith("}")) {
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
 
     private void layout(Container host, Element e) {
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
         String propertyName = null;
         for (Node attr : iterable(e.getAttributes())) {
             String name = attr.getNodeName();
             String value = attr.getNodeValue();
             if (name.equals("propertyName")) {
                 propertyName = evaluate(value, String.class, null);
             } else {
                 Method listenerMethod = lm.methodMap.get(name);
                 methodMap.put(listenerMethod, ReflectionUtils.getDeclaredMethod(
                         controllerClass, value, listenerMethod.getParameterTypes()));
             }
         }
 
         Object listener = Proxy.newProxyInstance(controllerLoader,
                 new Class<?>[] {lm.listenerClass}, new InvocationHandler() {
             @Override
             public Object invoke(Object proxy, Method method, Object[] args) {
                 Method controllerMethod = methodMap.get(method);
                 if (controllerMethod != null) {
                     ReflectionUtils.invoke(method, controller, args);
                 }
                 return null;
             }
         });
 
         if (propertyName != null && PropertyChangeListener
                 .class.isAssignableFrom(lm.listenerClass)) {
             ((JComponent) bean).addPropertyChangeListener(
                     propertyName, (PropertyChangeListener) listener);
         } else {
             ReflectionUtils.invoke(
                     getBeanMate(bean.getClass()).addListenerMap.get(lm.listenerClass),
                     bean, listener);
         }
     }
 
     private Class<?> getClass(String name) {
         Class<?> beanClass = importMap.get(name);
         if (beanClass == null) {
             for (String starImport : starImports) {
                String fqcn = starImport.replace('.', '/') + name + ".class";
                 if (controllerLoader.getResource(starImport + name) != null) {
                     beanClass = ReflectionUtils.getClass(fqcn, true, controllerLoader);
                     break;
                 }
             }
             if (beanClass == null) {
                 beanClass = ReflectionUtils.getClass(name, true, controllerLoader);
             }
         }
         return beanClass;
     }
 
     private BeanMate getBeanMate(Class<?> c) {
         BeanMate bm = beanMateMap.get(c);
         if (bm == null) {
             bm = new BeanMate(c);
             beanMateMap.put(c, bm);
         }
         return bm;
     }
 
     private ListenerMate getListenerMate(Class<?> c) {
         ListenerMate lm = listenerMateMap.get(c);
         if (lm == null) {
             lm = new ListenerMate(c);
             listenerMateMap.put(c, lm);
         }
         return lm;
     }
 
     private static DocumentBuilder createDocumentBuilder() {
         try {
             return DocumentBuilderFactory.newInstance().newDocumentBuilder();
         } catch (ParserConfigurationException ex) {
             throw new RuntimeException(ex);
         }
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
 
     @SuppressWarnings("unchecked")
     private <V> V evaluate(String literalValue, Class<V> valueClass) {
         valueClass = ReflectionUtils.wrap(valueClass);
         Converter<V> converter = CONVERTER_MANAGER.getConverter(valueClass);
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
                     value = ResourceBundle.getBundle(resourceBundleName,
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
      * Parses a GUI XML.
      * <p>
      * This method is a convenient variant of {@link #parse(String, Object,
      * Map) parse(String, Object, Map)} where there are no existing objects.
      *
      * @param guiXmlName The resource name of the GUI XML.
      * @param controller The GUI controller instance.
      */
     public static void parse(String guiXmlName, Object controller) {
         parse(controller.getClass().getResourceAsStream(guiXmlName), controller, null);
     }
 
     /**
      * Parses a GUI XML, with a map containing already created objects.
      * <p>
      * This method is a convenient variant of {@link #parse(InputStream, Object,
      * Map) parse(InputStream, Object, Map)} where the GUI XML is a class path
      * resource.
      *
      * @param guiXmlName        The resource name of the GUI XML.
      * @param controller        The GUI controller instance.
      * @param existingObjectMap A map containing existing ID-object pairs.
      */
     public static void parse(String guiXmlName, Object controller,
             Map<String, Object> existingObjectMap) {
         parse(controller.getClass().getResourceAsStream(guiXmlName),
                 controller, existingObjectMap);
     }
 
     /**
      * Parses a GUI XML.
      * <p>
      * This method is a convenient variant of {@link #parse(InputStream, Object,
      * Map) parse(InputStream, Object, Map)} where there are no existing objects.
      *
      * @param guiXmlIn   The input stream from which to read the XML GUI
      *                   declaration. It remains open after this method has
      *                   returned.
      * @param controller The controller instance.
      */
     public static void parse(InputStream guiXmlIn, Object controller) {
         parse(guiXmlIn, controller, null);
     }
 
     /**
      * Parses a GUI XML, with a map containing already created objects.
      * <p>
      * The GUI controller class can optionally declares fields with their types
      * and names matching the elements declared in the GUI XML. If the GUI XML
      * has an element with the tag name as the simple name of the controller's
      * class, the element represents the GUI controller itself. This is typically
      * useful when the controller is a component. Additionally, one controller's
      * method can be annotated with {@link PostParseGui}, so that it is automatically
      * called after the GUI has been parsed. If more than one methods are annotated
      * with {@link PostParseGui}, only the first found one is called.
      * <p>
      * The {@code existingBeanMap} may contain already created objects. While
      * parsing an element, if the element's ID is already associated with a object
      * in this map, that object is directly used instead of reflectively creating
      * a new one with the default constructor.
      *
      * @param guiXmlIn          The input stream from which to read the XML GUI
      *                          declaration. It remains open after this method
      *                          has returned.
      * @param controller        The controller instance.
      * @param existingObjectMap A map containing existing ID-bean pairs.
      */
     public static void parse(InputStream guiXmlIn, Object controller,
             Map<String, Object> existingObjectMap)  {
         try {
             new GuiParser(DOCUMENT_BUILDER.parse(guiXmlIn),
                     controller, existingObjectMap).parse();
         } catch (IOException | SAXException ex) {
             throw new RuntimeException(ex);
         }
     }
 
     private static void setMnemonic(JLabel label) {
         Mnemonic mnemonic = analyzeMnemonic(label.getText());
         if (mnemonic != null) {
             label.setText(mnemonic.text);
             label.setDisplayedMnemonic(mnemonic.mnemonicChar);
             label.setDisplayedMnemonicIndex(mnemonic.mnemonicIndex);
         }
     }
 
     private static void setMnemonic(AbstractButton button) {
         Mnemonic mnemonic = analyzeMnemonic(button.getText());
         if (mnemonic != null) {
             button.setText(mnemonic.text);
             button.setMnemonic(mnemonic.mnemonicChar);
             button.setDisplayedMnemonicIndex(mnemonic.mnemonicIndex);
         }
     }
 
     private static Mnemonic analyzeMnemonic(String text) {
         if (text == null) {
             return null;
         }
 
         boolean isHtml = BasicHTML.isHTMLString(text);
         Matcher matcher = MNEMONIC_PATTERN.matcher(text);
         if (matcher.find()) {
             String mnemonicMark = matcher.group();
             char mnemonicChar = mnemonicMark.charAt(1);
             int start = matcher.start();
             int end = matcher.end();
             StringBuilder sb = new StringBuilder()
                     .append(text.substring(0, start));
             if (isHtml) {
                 // HTML can be complex so it doesn't always work as expected.
                 sb.append("<u>").append(mnemonicChar).append("</u>");
             } else {
                 sb.append(mnemonicChar);
             }
             sb.append(text.substring(end));
             return new Mnemonic(sb.toString(), mnemonicChar, start);
         } else {
             return null;
         }
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
 
     private static class Mnemonic {
         private String text;
         private char mnemonicChar;
         private int mnemonicIndex;
 
         private Mnemonic(String text, char mnemonicChar, int mnemonicIndex) {
             this.text = text;
             this.mnemonicChar = mnemonicChar;
             this.mnemonicIndex = mnemonicIndex;
         }
     }
 }
