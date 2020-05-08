 package nl.nikhef.xhtmlrenderer.swing;
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Method;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.swing.AbstractButton;
 import javax.swing.Action;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JList;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.ListModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.JTextComponent;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xhtmlrenderer.extend.NamespaceHandler;
 import org.xhtmlrenderer.extend.ReplacedElement;
 import org.xhtmlrenderer.extend.UserAgentCallback;
 import org.xhtmlrenderer.layout.LayoutContext;
 import org.xhtmlrenderer.render.BlockBox;
 import org.xhtmlrenderer.resource.XMLResource;
 import org.xhtmlrenderer.swing.FSMouseListener;
 import org.xhtmlrenderer.swing.LinkListener;
 /* For xhtmlrenderer CVS
 import org.xhtmlrenderer.swing.ImageResourceLoader;
 import org.xhtmlrenderer.swing.RepaintListener;
 */
 import org.xhtmlrenderer.swing.SwingReplacedElement;
 import org.xhtmlrenderer.swing.SwingReplacedElementFactory;
 import org.xhtmlrenderer.util.Configuration;
 
 
 /** An enhanced version of XHTMLPanel
  * <p>
  * Additional features:<ul>
  *  <li><em>Template</em> functionality (see below)</li>
  *  <li>Easier handling of <em>form submission</em> in the Java application</li>
  *  <li><em>Printing</em> workaround to properly set margins</li>
  *  <li>Support of the <em><tt>readonly</tt></em> attribute on form elements</li>
  * </ul>
  * <p>
  * <strong>Template functionality</strong>
  * <p>
  * To allow customisation of an HTML page without having to do HTML construction in Java
  * code, templates were introduced. The template language is quite simple, just enough to
  * enable conditionals and variable substitution. To set variables, either use
  * {@link #setData} to bind to your own {@link Properties} object, or use
  * {@link #data()}.{@link Properties#setProperty setProperty} to set variables directly;
  * note that only the former will automatically {@link #refresh} as well.
  * <p>
  * Variables are used in document templates as explained by {@link TemplateDocument}.
  * A basic example is shown below.
  * <pre><code>
  *   String page =
  *     "&lt;html&gt;" +
  *     " &lt;head&gt;&lt;title&gt;Hi&lt;/title&gt;&lt;/head&gt;" +
  *     " &lt;body&gt;" +
  *     "  &lt;h1&gt;Hi&lt;/h1&gt;" +
  *     "  &lt;p&gt;Please &lt;a href='${url}'&gt;visit our webpage&lt;/a&gt;.&lt;/p&gt;" +
  *     "  &lt;p if='${special}'&gt;Please note that we have a special offer for you.&lt;/p&gt;" +
  *     "  &lt;p c='${copyright}'/&gt;" +
  *     " &lt;/body&gt;" +
  *     "&lt;/html&gt;";
  *   DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
  *   Document document = builder.parse(new ByteArrayInputStream(page.getBytes()));
  *   TemplatePanel panel = new TemplatePanel();
  *   panel.data().setProperty("url", "http://www.example.com/");
  *   if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
  *   	panel.data().setProperty("special", "true");
  *   panel.data().setProperty("copyright", "&amp;copy; by &lt;i&gt;Example&lt;/i&gt; Inc.");
  *   panel.setDocument(document);
  * </code></pre>
  * <p>
  * Additionally, HTML form elements are automatically bound to properties. This means
  * that each form element is bound to a property (see {@link #setData} and {@link #data})
  * with the name specified by the element's <tt>name</tt> attribute. So the HTML item
  * <pre><code>
  *   &lt;label for="name"&gt;Your name:&lt;/label&gt; &lt;input type="text" name="name" id="name"/&gt;
  * </code></pre>
  * would enable the user to enter his/her name. When the name is changed, the corresponding
  * property <tt>name</tt> is updated as well, so <code>panel.data().getProperty("name")</code>
  * would return the text entered by the user.
  * <p>
  * Note that a form element's initial value is set from the property when available on
  * document loading. If no such property exists, its value is set from the one specified
  * in HTML (like <tt>value="foo"</tt> or <tt>selected="selected"</tt>).
  * <p>
  * To handle form submission from the HTML form, use {@link #setSubmitAction}.
  * <p>
  * The input button element has a special attribute {@code href} that opens the page
  * it points to using the installed linklistener(s). When one hooks that, it is possible
  * to generate actions by button presses.
  * <p>
  * It may be desirable to keep the user from editing certain variables under specific
  * circumstances. This is done by setting the property <tt><i>propertyname</i>.lock</tt>
  * to <tt>true</tt>, which enables the <tt>readonly</tt> attribute on the form element
  * (this functionality is implemented by {@link TemplateDocument}).
  * 
  * @author wvengen
  */
 public class TemplatePanel extends XHTMLPanel implements ITemplatePanel {
     
     protected Properties data = new Properties();
     protected TemplateDocument template = null;
     protected Action submitAction = null;
     
     protected HashMap<String, Component> formelements = new HashMap<String, Component>();
 
     /** Construct a new, empty TemplatePanel with empty properties. */
     public TemplatePanel() {
 	super();
 	// install custom form handling hooks
 	getSharedContext().setReplacedElementFactory(
 		new TemplateSwingReplacedElementFactory()
 		// for xhtmlrenderer CVS
 		//new TemplateSwingReplacedElementFactory(null, new ImageResourceLoader())
 	);
 	setFormSubmissionListener(this);
     }
     
     /** Construct a new TemplatePanel with empty Properties, and set the source document. */
     public TemplatePanel(String src) throws IOException {
 	this();
 	setDocument(src);
     }
     
     /** Set the properties to use for the template and form elements.
      * <p>
      * Also reparses the document and updates the panel, if a template was set.
      * 
      * @param p New properties to set 
      */
     public void setData(Properties p) {
 	data = p;
 	if (template!=null) {
 	    template.setData(p);
 	    // don't do just refresh cause it reparses the document which has already happened
 	    super.setDocument(template, template.getDocumentURI(), getSharedContext().getNamespaceHandler());
 	}
     }
     
     /** Return the properties.
      *<p>
      * Use this to retrieve data from form elements. The name of each
      * element has a corresponding property key, and the value of the property
      * is updated when the form element changes.
      */
     public Properties data() {
 	// properties from form elements are updated directly using listeners
 	// so it is unnecessary to retrieve them here  
 	return data;
     }
     
     /** Refresh the contents so that all parsing is redone.
      * <p>
      * Note that the template is _not_ retrieved again from its source.
      * <p>
      * This happens automatically in {@link #setData} and {@link #setDocument}.
      * 
      * @return true on success, false if no document URI was specified
      *         (which happens when {@link #setDocument} was called with a {@link Document}
      *         as argument but no base uri was specified).
      */
     public boolean refresh() {
 	if (template==null) return false;
 	template.refresh();
 	super.setDocument(template, template.getDocumentURI(), getSharedContext().getNamespaceHandler());
 	return true;
     }
     
     /** Set the action to perform on form submission.
      * <p>
      * If this is set to null, the standard behaviour is done: posting data
      * to the url supplied by the form. By default this class's
      * {@link #submit} is called. */
     public void setSubmitAction(Action e) {
 	submitAction = e;
     }
     
     /** Event handler for form submission.
      * <p>
      * You can override this in your class to handle form submission. The
      * The default implementation calls the Action defined by
      * {@link #setSubmitAction} with its command string set to the <tt>name</tt>
      * of the originating submit button. In this way you can discriminate
      * between different forms on a single HTML page.
      * 
      * @param url URL to submit to, not used in the default implementation.
      */
     @Override
     public void submit(String url) {
 	if (submitAction!=null) {
 	    String action = null;
 	    // find out which submit button was the source
 	    NodeList nl = getDocument().getElementsByTagName("input");
 	    for (int i=0; i<nl.getLength(); i++) {
 		Node node = nl.item(i);
 		Node type =  node.getAttributes().getNamedItem("type");
 		if (type==null) continue;
 		if (!type.getNodeValue().equals("submit")) continue;
 		Node name = node.getAttributes().getNamedItem("name");
 		if (name==null) continue;
 		if (url.contains(name.getNodeValue()+'=')) {
 		    action = name.getNodeValue();
 		    break;
 		}		
 	    }
 	    // and use that as command string in the action event
 	    ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, action);
 	    submitAction.actionPerformed(e);
 	}
     }
 
     public Component getFormComponent(String name) {
 	return formelements.get(name);
     }
     public Map<String, Component> getFormComponents() {
 	return Collections.unmodifiableMap(formelements);
     }
     
     public void setDocument(TemplateDocument doc) {
 	template = doc;
 	super.setDocument(doc, doc.getDocumentURI());
     }
     public void setDocument(TemplateDocument doc, NamespaceHandler nsh) {
 	template = doc;
 	super.setDocument(doc, doc.getDocumentURI(), nsh);
     }
 
     /** Override of {@link XHTMLPanel#setDocument setDocument} to process the template. */
     /* <p>
      * This could have been done using xhtmlrenderer's configuration key
      * <pre><code>xr.load.xml-reader=some.package.TemplateXMLReader</code></pre>
      * but I found no way to pass data to the class, which is needed for the
      * variables. And so this method is overloaded.
      * <p>
      * Overriding just this method is enough since this one is called by all
      * the others.
      */
     @Override
     public void setDocument(Document doc, String url, NamespaceHandler nsh) {
 	template = new TemplateDocument(doc, data);
 	template.setDocumentURI(url);
 	super.setDocument(template, url, nsh);
     }
     @Override
     public void setDocument(Document doc, String url) {
 	// XHTMLPanel.resetListeners() is private so copy here :(
         if (Configuration.isTrue("xr.use.listeners", true)) {
             resetMouseTracker();
         }
         setDocument(doc, url, getSharedContext().getNamespaceHandler());
     }
     @Override
     public void setDocument(InputStream stream, String url) {
         setDocument(XMLResource.load(stream).getDocument(), url);
     }
     
     /** Class binds HTML form components to their respective properties. 
      * <p>
      * On Swing component creation, the initial value is set from the TemplatePanel
      * property, if any. Also its listeners are set to update the corresponding
      * TemplatePanel property when the component's contents was changed. */
     protected class TemplateSwingReplacedElementFactory extends SwingReplacedElementFactory {
 	
 	public TemplateSwingReplacedElementFactory() {
 	    super();
 	}
 	/* for xhtmlrenderer CVS
 	public TemplateSwingReplacedElementFactory(RepaintListener listener) {
 	    super(listener);
 	}
 	public TemplateSwingReplacedElementFactory(final RepaintListener listener, final ImageResourceLoader irl) {
 	    super(listener, irl);
 	}
 	*/
 
 	@Override
 	public ReplacedElement createReplacedElement(LayoutContext context, BlockBox box,
 		UserAgentCallback uac, int cssWidth, int cssHeight) {
 	    ReplacedElement el = super.createReplacedElement(context, box, uac, cssWidth, cssHeight);
 	    if (el instanceof SwingReplacedElement)
 		bindProperty(box.getElement(), ((SwingReplacedElement)el).getJComponent());
 	    return el;
 	}
 	
 	@Override
 	public void reset() {
 	    formelements = new HashMap<String, Component>();
 	};
 
 	/** Add a listener that updates the properties bound to the
 	 * enclosing TemplatePane when the supplied component is changed. This
 	 * also sets the current value to the property's current value. If no
 	 * name is set on the element, nothing is bound. */
 	protected void bindProperty(Element e, JComponent c) {
 	    String name = e.getAttribute("name");
 	    String ivalue = e.getAttribute("value");
 	    // only for options the name is taken from enclosing select
 	    if (e.getTagName().toLowerCase().equals("option")) {
 		Node node;
 		while ((node = e.getParentNode()) != null) {
 		    if (node.getNodeName().toLowerCase().equals("select")) {
 			name = node.getAttributes().getNamedItem("name").getNodeValue();
 			break;
 		    }
 		}
 	    }
 	    if (name==null) return;
 	    String value = data.getProperty(name);
 	    // we care about content controls, not scrollpanes
 	    if (c instanceof JScrollPane) c = (JComponent)((JScrollPane)c).getViewport().getView();
 	    // store Component reference by name; radio button especially
 	    if (c instanceof JRadioButton)
 		formelements.put(name+"."+ivalue, c);
 	    else
 		formelements.put(name, c);
 	    // how to get&set contents depends on type of control.
 	    if (c instanceof JTextComponent) {
 		// text and password
 		if (value!=null)
 		    ((JTextComponent)c).setText(value);
 		((JTextComponent)c).getDocument().addDocumentListener(new FormComponentListener(e, c));
 	    } else if (c instanceof JComboBox || c instanceof JList) {
 		// combo box or list
 		// find selected items from document; see also listSelectionChanged()
 		// TODO implement multiple selection
 		ListModel model = null;
 		int index = -1;
 		if (c instanceof JComboBox)
 		    model = ((JComboBox)c).getModel();
 		else if (c instanceof JList)
 		    model = ((JList)c).getModel();
 		for (int i=0; i<model.getSize(); i++) {
 		    String thisval = getSelectValue(model.getElementAt(i));
 		    if (thisval!=null && thisval.equals(value)) {
 			index = i;
 			break;
 		    }
 		}
 		
 		if (c instanceof JComboBox) {
 		    if (value!=null)
 			((JComboBox)c).setSelectedIndex(index);
 		    ((JComboBox)c).addItemListener(new FormComponentListener(e, c));
 		} else if (c instanceof JList) {
 		    if (value!=null)
 			((JList)c).setSelectedIndex(index);
 		    ((JList)c).addListSelectionListener(new FormComponentListener(e, c));
 		}
 	    } else if (c instanceof JRadioButton) {
 		// radio button
 		if (value!=null)
 		    ((JRadioButton)c).setSelected(ivalue.equals(value));
 		((AbstractButton)c).addChangeListener(new FormComponentListener(e, c));
 	    } else if (c instanceof JButton && !"submit".equals(e.getAttribute("type"))) {
 		// ordinary button
 		// remove actionlisteners that pops up useless dialog, add new one
 		for (int i=0; i<((JButton)c).getActionListeners().length; i++)
 		    ((JButton)c).removeActionListener(((JButton)c).getActionListeners()[i]);
 		((JButton)c).addActionListener(new FormComponentListener(e, c));
 	    } else if (c instanceof AbstractButton) {
 		// checkbox
 		if (value!=null)
 		    ((AbstractButton)c).setSelected(Boolean.valueOf(value));
 		((AbstractButton)c).addChangeListener(new FormComponentListener(e, c));
 	    }
 	    // TODO other form elements as well
 	    // TODO just copy hidden to properties?
 	}
 
 	/** Class that implements listeners for the form elements which update
 	 * the corresponding TemplatePanel property. */
 	protected class FormComponentListener implements DocumentListener, ChangeListener, ListSelectionListener, ItemListener, ActionListener {
 
 	    protected Element el = null;
 
 	    public FormComponentListener(Element el, Object source) {
 		this.el = el;
 		// make sure data and component are in sync. This is an issue when
 		// the html has specified a default value but the property isn't
 		// defined. This call updates the property from the default value.
 		doUpdate(source);
 	    }
 	    
 	    protected void doUpdate(Object source) {
 		String name = el.getAttribute("name");
 		
 		if (source instanceof JRadioButton) {
 		    if (((JRadioButton)source).isSelected())
 			data.setProperty(name, el.getAttribute("value"));
 		    
 		} else if (source instanceof AbstractButton) {
 		    data.setProperty(name, Boolean.toString(((AbstractButton)source).isSelected()));
 		
 		} else if (source instanceof JComboBox || source instanceof JList) { 
 		    /** Update the properties bound to the enclosing TemplatePanel
 		     * from a list. This is called when a list's state is changed.
 		     * 
 		     * The implementation looks somewhat clumsy. I have no access to the
 		     * underlying model since that is declared private. So I get the
 		     * selected index and look up the corresponding value from the
 		     * document.
 		     * TODO implement lists with multiple selections */
 		    Object sel = null;
 		    if (source instanceof JComboBox)
 			sel = ((JComboBox)source).getSelectedItem();
 		    else if (source instanceof JList)
 			sel = ((JList)source).getSelectedValue();
 		    if (sel!=null) {
 			String value = getSelectValue(sel);
 			if (value!=null)
 			    data.setProperty(name, value);
 		    }
 		
 		} else if (source instanceof JTextComponent) {
 		    // not used since javax.swing.text.Document is bound instead
 		    data.setProperty(name, ((JTextComponent)source).getText());
 		} else if (source instanceof javax.swing.text.Document) {
 		    javax.swing.text.Document doc = (javax.swing.text.Document)source;
 		    try {
 			data.setProperty(name, doc.getText(0, doc.getLength()));
 		    } catch (BadLocationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		    }
 		} else {
 		    // TODO warn
 		    System.out.println("unrecognised update!");
 		}
 	    }
 
 	    // document update (text fields)
 	    public void changedUpdate(DocumentEvent e)  { doUpdate(e.getDocument()); }
 	    public void insertUpdate(DocumentEvent e)   { doUpdate(e.getDocument()); }
 	    public void removeUpdate(DocumentEvent e)   { doUpdate(e.getDocument()); }
 	    // button update
 	    public void stateChanged(ChangeEvent e) {
 		doUpdate(e.getSource());
 	    }
 	    // combobox selection update
 	    public void itemStateChanged(ItemEvent e) {
 		if (e.getStateChange() == ItemEvent.SELECTED)
 		    doUpdate(e.getSource());
 	    }
 	    // list selection update
 	    public void valueChanged(ListSelectionEvent e) {
 		if (!e.getValueIsAdjusting())
 		    doUpdate(e.getSource());
 	    }
 
 	    // button was pressed
 	    @SuppressWarnings("unchecked") // for mousetracklisteners
 	    public void actionPerformed(ActionEvent e) {
 		if (e.getSource() instanceof JButton) {
 		    String href = el.getAttribute("href");
 		    if (href!=null && href!="") {
 			// activate all link listeners
 			List<FSMouseListener> ls = (List<FSMouseListener>)getMouseTrackingListeners();
 			for (Iterator<FSMouseListener> it = ls.iterator(); it.hasNext(); ) {
 			    FSMouseListener l = it.next();
 			    if (l instanceof LinkListener)
 				((LinkListener)l).linkClicked(TemplatePanel.this, href);
 			}
 		    }
 		}
 	    }
 	}
 	/** Helper method to read value from private {@code SelectField.NameValuePair} object */
 	public String getSelectValue(Object obj) {
 	    try {
 		final Class<?> pairClass = Class.forName("org.xhtmlrenderer.simple.extend.form.SelectField$NameValuePair");
 		final Method getValueMethod = pairClass.getMethod("getValue", new Class<?>[] {});
 		getValueMethod.setAccessible(true);
 		return (String)getValueMethod.invoke(obj, new Object[]{});
 	    } catch (Exception e) {
 		return null;
 	    }
 	}
     }
 }
