 /*******************************************************************************
  * Copyright (c) 2011 Florian Thienel and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * 		Florian Thienel - initial API and implementation
  *******************************************************************************/
 package ft.vex.wikitext.ui.editor;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.mylyn.wikitext.core.WikiText;
 import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
 import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.IStorageEditorInput;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.EditorPart;
 import org.eclipse.vex.core.internal.css.StyleSheet;
 import org.eclipse.vex.core.internal.css.StyleSheetReader;
 import org.eclipse.vex.core.internal.dom.Document;
 import org.eclipse.vex.core.internal.dom.DocumentWriter;
 import org.eclipse.vex.core.internal.dom.Validator;
 import org.eclipse.vex.core.internal.validator.WTPVEXValidator;
 import org.eclipse.vex.ui.internal.swt.VexWidget;
 
 import ft.vex.wikitext.Activator;
 import ft.vex.wikitext.VexDocumentBuilder;
 
@SuppressWarnings("restriction")
 public class WikiTextEditor extends EditorPart {
 
 	public static final String ID = "ft.vex.wikitext.editor"; //$NON-NLS-1$
 
 	private Document document;
 
	private Text sourceText;

 	private VexWidget vexWidget;
 
 	@Override
 	public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
 		if (!(input instanceof IStorageEditorInput))
 			throw new PartInitException(input + " is not supported.");
 		setSite(site);
 		setInput(input);
 		setPartName(input.getName());
 		try {
 			loadDocument((IStorageEditorInput) input);
 		} catch (CoreException e) {
 			throw new PartInitException(e.getStatus());
 		}
 	}
 
 	private void loadDocument(IStorageEditorInput input) throws CoreException {
 		final VexDocumentBuilder documentBuilder = new VexDocumentBuilder();
 		final MarkupParser markupParser = new MarkupParser(getMarkupLanguage(input), documentBuilder);
 		final InputStreamReader reader = new InputStreamReader(input.getStorage().getContents()); // TODO infer encoding
 		try {
 			try {
 				markupParser.parse(reader);
 			} catch (IOException e) {
 				throw new CoreException(new Status(IStatus.ERROR, Activator.ID, e.getMessage(), e));
 			}
 		} finally {
 			try {
 				reader.close();
 			} catch (IOException e) {
 				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.ID, e.getMessage(), e));
 			}
 		}
 		document = documentBuilder.getDocument();
 	}
 
 	private static MarkupLanguage getMarkupLanguage(final IStorageEditorInput input) {
 		final String name;
 		if (input instanceof IFileEditorInput)
 			name = ((IFileEditorInput) input).getFile().getName();
 		else
 			name = input.getName();
 		final MarkupLanguage result = WikiText.getMarkupLanguageForFilename(name);
 		if (result == null)
 			return WikiText.getMarkupLanguage("Textile"); //$NON-NLS-1$
 		return result;
 	}
 
 	@Override
 	public void createPartControl(final Composite parent) {
 		// TODO trace
 		final DocumentWriter documentWriter = new DocumentWriter();
 		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
 		try {
 			documentWriter.write(document, buffer);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		System.out.println(buffer.toString());
 
 		final URL dtdUrl = FileLocator.find(Activator.getDefault().getBundle(), new Path("documenttype/wikitext.dtd"), null);
 		final Validator validator = new WTPVEXValidator(dtdUrl);
 		document.setValidator(validator);
 
 		final URL styleSheetUrl = FileLocator.find(Activator.getDefault().getBundle(), new Path("documenttype/wikitext.css"), null);
 		final StyleSheet styleSheet;
 		try {
 			styleSheet = new StyleSheetReader().read(styleSheetUrl);
 		} catch (IOException e) {
 			throw new AssertionError("Cannot read stylesheet! " + e.getMessage());
 		}
 
 		vexWidget = new VexWidget(parent, SWT.V_SCROLL);
 		vexWidget.setDocument(document, styleSheet);
 	}
 
 	@Override
 	public void setFocus() {
 		vexWidget.setFocus();
 	}
 
 	@Override
 	public boolean isDirty() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean isSaveAsAllowed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void doSave(final IProgressMonitor monitor) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void doSaveAs() {
 		// TODO Auto-generated method stub
 	}
 
 }
