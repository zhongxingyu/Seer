 /*******************************************************************************
  * Copyright Technophobia Ltd 2012
  * 
  * This file is part of the Substeps Eclipse Plugin.
  * 
  * The Substeps Eclipse Plugin is free software: you can redistribute it and/or modify
  * it under the terms of the Eclipse Public License v1.0.
  * 
  * The Substeps Eclipse Plugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * Eclipse Public License for more details.
  * 
  * You should have received a copy of the Eclipse Public License
  * along with the Substeps Eclipse Plugin.  If not, see <http://www.eclipse.org/legal/epl-v10.html>.
  ******************************************************************************/
 package com.technophobia.substeps.editor;
 
 import java.util.ResourceBundle;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.Position;
 import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
 import org.eclipse.jface.text.contentassist.IContentAssistant;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.jface.text.source.SourceViewer;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IPartListener2;
 import org.eclipse.ui.IPartService;
 import org.eclipse.ui.IWorkbenchPartReference;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.eclipse.ui.contexts.IContextActivation;
 import org.eclipse.ui.contexts.IContextService;
 import org.eclipse.ui.editors.text.TextEditor;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.texteditor.TextOperationAction;
 import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
 
 import com.technophobia.eclipse.editor.FormattableEditorPart;
 import com.technophobia.substeps.FeatureEditorPlugin;
 import com.technophobia.substeps.colour.ColourManager;
 import com.technophobia.substeps.document.content.ContentTypeDefinitionFactory;
 import com.technophobia.substeps.document.content.assist.AutoActivatingContentAssistantDecorator;
 import com.technophobia.substeps.document.content.assist.ContentAssistantFactory;
 import com.technophobia.substeps.document.content.assist.ProcessedContentAssistantFactory;
 import com.technophobia.substeps.document.content.assist.feature.StepImplementationProcessorSupplier;
 import com.technophobia.substeps.document.content.feature.FeatureContentTypeDefinitionFactory;
 import com.technophobia.substeps.document.content.partition.ContentTypeRuleBasedPartitionScannerFactory;
 import com.technophobia.substeps.document.content.view.ContentTypeViewConfiguration;
 import com.technophobia.substeps.document.formatting.FormattingContextFactory;
 import com.technophobia.substeps.document.formatting.partition.PartitionedFormattingContextFactory;
 import com.technophobia.substeps.document.partition.EditorInputPartitionContext;
 import com.technophobia.substeps.document.partition.PartitionContext;
 import com.technophobia.substeps.document.partition.PartitionScannedDocumentProvider;
 import com.technophobia.substeps.editor.outline.OutlineLabelProvider;
 import com.technophobia.substeps.editor.outline.SubstepsContentOutlinePage;
 import com.technophobia.substeps.editor.outline.feature.FileToFeatureElementTransformer;
 import com.technophobia.substeps.editor.outline.model.AbstractModelElement;
 import com.technophobia.substeps.editor.outline.substeps.ProjectFile;
 import com.technophobia.substeps.supplier.Callback1;
 import com.technophobia.substeps.supplier.Supplier;
 import com.technophobia.substeps.supplier.Transformer;
 
 /**
  * TextEditor whose text is configured to view as a feature file
  * 
  * @author sforbes
  * 
  */
 public class FeatureEditor extends TextEditor implements FormattableEditorPart, IPartListener2 {
 
     private static final String SUBSTEPS_CONTEXT = "com.technophobia.substeps.editor.SubstepsContext";
 
     private final ColourManager colourManager;
     private IContextActivation currentActivateContext;
     private SubstepsContentOutlinePage outlinePage;
 
     private IEditorInput editorInput;
 
 
     @SuppressWarnings("unchecked")
     public FeatureEditor() {
 
         final ContentTypeDefinitionFactory contentTypeDefinitionFactory = contentTypeDefinitionFactory();
         final FormattingContextFactory formattingContextFactory = new PartitionedFormattingContextFactory(
                 contentTypeDefinitionFactory);
         final ContentAssistantFactory contentAssistantFactory = new ProcessedContentAssistantFactory(
                 processorSupplier(), (Callback1<IContentAssistant>) new AutoActivatingContentAssistantDecorator());
         colourManager = new ColourManager();
 
         final Supplier<PartitionContext> partitionContextSupplier = partitionContextSupplier();
         setSourceViewerConfiguration(new ContentTypeViewConfiguration(colourManager, contentTypeDefinitionFactory,
                 formattingContextFactory, contentAssistantFactory, partitionContextSupplier));
         setDocumentProvider(new PartitionScannedDocumentProvider(new ContentTypeRuleBasedPartitionScannerFactory(
                 contentTypeDefinitionFactory), partitionContextSupplier));
     }
 
 
     @Override
     public void doFormat() {
         ((SourceViewer) getSourceViewer()).doOperation(ISourceViewer.FORMAT);
     }
 
 
     @Override
     public void doSave(final IProgressMonitor progressMonitor) {
         super.doSave(progressMonitor);
 
         FeatureEditorPlugin.instance().info("Saving file " + ((FileEditorInput) editorInput).getFile().getLocation());
 
         if (outlinePage != null) {
             outlinePage.update();
         }
     }
 
 
     @SuppressWarnings("rawtypes")
     @Override
     public Object getAdapter(final Class required) {
         if (IContentOutlinePage.class.equals(required)) {
             if (outlinePage == null) {
                 outlinePage = new SubstepsContentOutlinePage(this, new OutlineLabelProvider(),
                         fileToModelTransformer(), documentOffsetToLineNumber());
                 if (getEditorInput() != null)
                     outlinePage.setInput(getEditorInput());
             }
             return outlinePage;
         }
         return super.getAdapter(required);
     }
 
 
     public IDocument getCurrentDocument() {
         final PartitionScannedDocumentProvider docProvider = (PartitionScannedDocumentProvider) getDocumentProvider();
         return docProvider.getDocuemnt();
     }
 
 
     @Override
     public void partActivated(final IWorkbenchPartReference partRef) {
 
         if (getEditorSite().getId().equals(partRef.getId())) {
             activateContext();
         }
     }
 
 
     @Override
     public void partBroughtToTop(final IWorkbenchPartReference partRef) {
 
         if (getEditorSite().getId().equals(partRef.getId())) {
             activateContext();
         }
     }
 
 
     @Override
     public void partClosed(final IWorkbenchPartReference partRef) {
 
         if (getEditorSite().getId().equals(partRef.getId())) {
             deactivateContext();
         }
     }
 
 
     @Override
     public void partDeactivated(final IWorkbenchPartReference partRef) {
 
         if (getEditorSite().getId().equals(partRef.getId())) {
             deactivateContext();
         }
     }
 
 
     @Override
     public void partOpened(final IWorkbenchPartReference partRef) {
 
         if (getEditorSite().getId().equals(partRef.getId())) {
             activateContext();
         }
     }
 
 
     @Override
     public void partHidden(final IWorkbenchPartReference partRef) {
 
         if (getEditorSite().getId().equals(partRef.getId())) {
             deactivateContext();
         }
     }
 
 
     @Override
     public void partVisible(final IWorkbenchPartReference partRef) {
         // beware you get LOADS of these, to the point where it doesn't really
         // stop when running eclipse via eclipse (during plugin dev)
 
     }
 
 
     @Override
     public void partInputChanged(final IWorkbenchPartReference partRef) {
         // not used yet... ?
     }
 
 
     @Override
     public void dispose() {
         colourManager.dispose();
         if (outlinePage != null) {
             outlinePage.setInput(null);
         }
 
         final IPartService partService = (IPartService) this.getSite().getService(IPartService.class);
 
         partService.removePartListener(this);
 
         super.dispose();
     }
 
 
     @Override
     protected void createActions() {
         super.createActions();
 
         final ResourceBundle resourceBundle = FeatureEditorPlugin.instance().getResourceBundle();
         final TextOperationAction action = new TextOperationAction(resourceBundle, "ContentFormatProposal.", this,
                 ISourceViewer.FORMAT);
         setAction("ContentFormatProposal", action);
         getEditorSite().getActionBars().setGlobalActionHandler("ContentFormatProposal", action);
     }
 
 
     @Override
     protected void doSetInput(final IEditorInput input) throws CoreException {
         super.doSetInput(input);
 
         this.editorInput = input;
     }
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void setSite(final IWorkbenchPartSite site) {
         super.setSite(site);
 
         // add an activation listener
 
         final IPartService partService = (IPartService) site.getService(IPartService.class);
 
         partService.addPartListener(this);
 
     }
 
 
     /**
      * Return a new {@link ContentTypeDefinitionFactory}
      * 
      * @return
      */
     protected ContentTypeDefinitionFactory contentTypeDefinitionFactory() {
         return new FeatureContentTypeDefinitionFactory();
     }
 
 
     protected Transformer<ProjectFile, AbstractModelElement> fileToModelTransformer() {
         return new FileToFeatureElementTransformer(lineNumberToDocumentOffset());
     }
 
 
     protected Transformer<Integer, Position> lineNumberToDocumentOffset() {
         return new Transformer<Integer, Position>() {
             @Override
             public Position from(final Integer lineNumber) {
                 if (editorInput != null) {
                     try {
                         final IDocument document = getDocumentProvider().getDocument(editorInput);
                         return new Position(document.getLineOffset(lineNumber.intValue()));
                     } catch (final BadLocationException e) {
                         FeatureEditorPlugin.instance().info(
                                 "Couldn't get offset for line " + lineNumber + " in document");
                     }
                 }
                 return new Position(0);
             }
         };
     }
 
 
     protected Transformer<Position, Integer> documentOffsetToLineNumber() {
         return new Transformer<Position, Integer>() {
             @Override
             public Integer from(final Position offset) {
                 if (editorInput != null) {
                     try {
                         final IDocument document = getDocumentProvider().getDocument(editorInput);
                         return Integer.valueOf(document.getLineOfOffset(offset.getOffset()));
                     } catch (final BadLocationException e) {
                         FeatureEditorPlugin.instance().info(
                                 "Couldn't get line number for offset " + offset + " in document");
                     }
                 }
                 return Integer.valueOf(-1);
             }
         };
     }
 
 
     /**
      * Return a new {@link Supplier} of type {@link IContentAssistProcessor}
      * 
      * @return
      */
     private Supplier<IContentAssistProcessor> processorSupplier() {
         return new Supplier<IContentAssistProcessor>() {
             @Override
             public IContentAssistProcessor get() {
                 return new StepImplementationProcessorSupplier(getSite(), FeatureEditorPlugin.instance()
                         .getSuggestionManager()).get();
             }
 
         };
     }
 
 
     private Supplier<PartitionContext> partitionContextSupplier() {
         return new Supplier<PartitionContext>() {
 
             @Override
             public PartitionContext get() {
                 return new EditorInputPartitionContext(getEditorInput(), FeatureEditorPlugin.instance()
                         .getSuggestionManager());
 
             }
         };
     }
 
 
     private void activateContext() {
         if (currentActivateContext == null) {
             final IContextService contextService = (IContextService) this.getSite().getWorkbenchWindow()
                     .getService(IContextService.class);
             currentActivateContext = contextService.activateContext(SUBSTEPS_CONTEXT);
         }
         // else we're already active
     }
 
 
     private void deactivateContext() {
         if (currentActivateContext != null) {
             final IContextService contextService = (IContextService) this.getSite().getWorkbenchWindow()
                     .getService(IContextService.class);
 
             contextService.deactivateContext(currentActivateContext);
             currentActivateContext = null;
         }
     }
 }
