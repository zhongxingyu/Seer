 package org.eclipse.e4.tools.orion.css.editor.views;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.e4.tools.orion.css.editor.Activator;
 import org.eclipse.e4.ui.css.swt.internal.theme.ThemeEngine;
 import org.eclipse.e4.ui.css.swt.theme.ITheme;
 import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.ViewPart;
 import org.osgi.framework.Bundle;
 
 /**
  * This sample class demonstrates how to plug-in a new workbench view. The view
  * shows data obtained from the model. The sample creates a dummy model on the
  * fly, but a real implementation would connect to the model available either in
  * this or another plug-in (e.g. the workspace). The view is connected to the
  * model using a content provider.
  * <p>
  * The view uses a label provider to define how model objects should be
  * presented in the view. Each view can present the same model objects using
  * different labels and icons, if needed. Alternatively, a single label provider
  * can be shared between views in order to ensure that objects of the same type
  * are presented in the same way everywhere.
  * <p>
  */
 
 public class SampleCSSView extends ViewPart {
 
 	/**
 	 * The ID of the view as specified by the extension.
 	 */
 	public static final String ID = "z.ex.css.editor.views.SampleCSSView";
 	private Browser browser;
 	private String editorHtml;
 	private String editorContent;
 
 	/**
 	 * This is a callback that will allow us to create the viewer and initialize
 	 * it.
 	 * 
 	 * @throws PartInitException
 	 */
 	public void createPartControl(Composite parent) {
 		browser = new Browser(parent, SWT.NONE);
 		browser.setText(editorHtml, true);
 	}
 
 	@Override
 	public void init(IViewSite site) throws PartInitException {
 		super.init(site);
 		try {
 			initContent();
 			final Bundle bundle = Activator.getDefault().getBundle();
 			final InputStream in = bundle
 					.getEntry("/web/css.html").openStream();
 			String cssTemplate = loadFile(in, 1024);
 			final String editorCssUrl = FileLocator.toFileURL(bundle.getEntry("/web/built-editor.css")).toExternalForm();
			final String editorJsUrl = FileLocator.toFileURL(bundle.getEntry("/web/built-editor.js")).toExternalForm();
 			
 			editorHtml = String.format(cssTemplate, editorCssUrl, editorJsUrl, editorContent);
 			System.out.println(editorHtml);
 		} catch (IOException e) {
 			throw new PartInitException("Failed to load CSS editor", e);
 		}
 	}
 	
 	private void initContent() throws MalformedURLException, IOException {
 		IThemeEngine engine = (IThemeEngine) getSite().getService(IThemeEngine.class);
 		final ITheme theme = engine.getActiveTheme();
 		final List<String> sheets = ((ThemeEngine)engine).getStylesheets(theme);
 		if (sheets.size()>0) {
 			String path = sheets.get(0);
 			final InputStream in = FileLocator.toFileURL(new URL(sheets.get(0))).openStream();
 			editorContent = loadFile(in, 1024);
 		} else {
 			editorContent = "/*\n * This is an Orion editor sample.\n */\nfunction() {\n    var a = 'hi there!';\n    window.console.log(a);\n}";
 		}
 	}
 
 	public String loadFile(final InputStream in, final int bufferSize) throws IOException {
 		final char[] buffer = new char[bufferSize];
 		final StringBuilder out = new StringBuilder();
 		final Reader reader = new InputStreamReader(in, "UTF-8");
 		try {
 			int size = reader.read(buffer, 0, buffer.length);
 			while (size > 0) {
 				out.append(buffer, 0, size);
 				size = reader.read(buffer, 0, buffer.length);
 			}
 		} finally {
 			reader.close();
 		}
 		return out.toString();
 	}
 
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	public void setFocus() {
 		browser.setFocus();
 	}
 
 	/**
 	 * @return the browser
 	 */
 	public Browser getBrowser() {
 		return browser;
 	}
 
 }
