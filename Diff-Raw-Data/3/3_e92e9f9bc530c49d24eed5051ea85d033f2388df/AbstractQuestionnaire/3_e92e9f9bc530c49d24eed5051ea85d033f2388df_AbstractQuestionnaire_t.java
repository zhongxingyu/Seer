 package org.whole.crossexamples.lwc13.ui.swt;
 
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Stack;
 
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.whole.lang.bindings.BindingManagerFactory;
 import org.whole.lang.bindings.IBindingManager;
 import org.whole.lang.codebase.StreamPersistenceProvider;
 import org.whole.lang.factories.GenericEntityFactory;
 import org.whole.lang.factories.IEntityFactory;
 import org.whole.lang.factories.RegistryConfigurations;
 import org.whole.lang.model.IEntity;
 import org.whole.lang.operations.InterpreterOperation;
 import org.whole.lang.reflect.FeatureDescriptor;
 import org.whole.lang.reflect.ILanguageKit;
 import org.whole.lang.reflect.ReflectionFactory;
 import org.whole.lang.xml.codebase.XmlBuilderPersistenceKit;
 
 public abstract class AbstractQuestionnaire {
 	private Display display;
 	private Shell shell;
 	private Runnable notifier;
 	private Stack<Composite> compositeStack;
 	private Stack<Style> styleStack;
 	private Map<String, Style> styleMap;
 
 	public AbstractQuestionnaire(String title) {
 		this.compositeStack = new Stack<Composite>();
 		this.compositeStack.push(createShell(title));
 		this.styleMap = new HashMap<String, Style>();
 		this.styleStack = new Stack<Style>();
 		this.styleStack.push(defineStyle(null, FontStyle.NORMAL, 10, ColorConstants.BLACK));
 	}
 
 	public void show(Runnable notifier) {
 		this.notifier = notifier;
 		createControls();
 		shellEventLoop();
 	}
 
 	protected Shell createShell(String title) {
 		display = new Display();
 		shell = new Shell(display);
 		GridLayout layout = new GridLayout(2, false);
 		layout.horizontalSpacing = layout.verticalSpacing = 10;
 		layout.marginWidth = layout.marginHeight = 10;
 		shell.setLayout(layout);
 		shell.setText(title);
 		shell.setBounds(200, 200, 600, 800);
 		shell.pack();
		shell.forceActive();
 		return shell;
 	}
 
 	protected void shellEventLoop() {
 		shell.open();
 		try {
 			load();
 		} catch (Exception e) {
 		}
 		getNotifier().run();
 		while (!shell.isDisposed())
 			if (!display.readAndDispatch())
 				display.sleep();
 		try {
 			save();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		display.dispose();
 	}
 	
 	protected Composite peekComposite() {
 		return compositeStack.peek();
 	}
 	protected Composite pushComposite(Composite composite) {
 		return compositeStack.push(composite);
 	}
 	protected Composite popComposite() {
 		return compositeStack.pop();
 	}
 
 	protected Style peekStyle() {
 		return styleStack.peek();
 	}
 	protected Style pushStyle(Style style) {
 		return styleStack.push(peekStyle().merge(style));
 	}
 	protected Style popStyle() {
 		return styleStack.pop();
 	}
 
 	public Runnable getNotifier() {
 		return notifier;
 	}
 	public void setNotifier(Runnable notifier) {
 		this.notifier = notifier;
 		notifier.run();
 	}
 
 	public Style defineStyle(String name, FontStyle fontStyle, Integer fontSize, Color color) {
 		Style style = new Style(fontStyle, fontSize, color);
 		styleMap.put(name, style);
 		return style;
 	}
 	public Style getStyle(String name) {
 		return styleMap.get(name);
 	}
 
 	protected ILanguageKit languageKit;
 	protected void deployMetaModel(String metaModelName) {
 		ReflectionFactory.deployWholePlatform();
 		try {
 			InputStream is = getClass().getResourceAsStream(metaModelName);
 			IEntity metaModel = XmlBuilderPersistenceKit.instance().readModel(new StreamPersistenceProvider(is));
 			IBindingManager bm = BindingManagerFactory.instance.createArguments();
 			InterpreterOperation.interpret(metaModel, bm);
 			languageKit = (ILanguageKit) bm.wGetValue("languageKit");
 		} catch (Exception e) {
 			throw new IllegalStateException("cannot deploy model", e);
 		}
 	}
     protected ILanguageKit getLanguageKit() {
     	return languageKit;
     }
 	protected void unmarshallPersistedValue(IEntity model, String fdName, AbstractValued<?> valued) {
 		valued.setValue(model.wGet(getLanguageKit().getFeatureDescriptorEnum().valueOf(fdName)));
 	}
 	protected void marshallPersistedValue(IEntity model, String fdName, AbstractValued<?> valued) {
 		marshallPersistedValue(model, fdName, valued.getValue());
 	}
 	protected void marshallPersistedValue(IEntity model, String fdName, Object value) {
 		IEntityFactory gef = GenericEntityFactory.instance(RegistryConfigurations.RESOLVER);
         FeatureDescriptor fd = getLanguageKit().getFeatureDescriptorEnum().valueOf(fdName);
         model.wSet(fd, gef.create(model.wGetEntityDescriptor(fd), value));
 	}
 
 	protected abstract void createControls();
 	protected abstract void load() throws Exception;
 	protected abstract void save() throws Exception;
 }
