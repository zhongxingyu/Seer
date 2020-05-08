 /*
  * Copyright 2011-2011 R.M.Morrien. 
  * Licensed under the Eclipse Public License, Version 1.0
  * Initial Developer: R.M.Morrien
  */
 package org.facttype.ui;
 
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.eclipse.xtext.builder.IXtextBuilderParticipant;
 import org.eclipse.xtext.documentation.IEObjectDocumentationProvider;
 import org.eclipse.xtext.ui.editor.contentassist.XtextContentAssistProcessor;
 import org.eclipse.xtext.ui.editor.hover.IEObjectHoverProvider;
 import org.facttype.ui.hover.DiagramEObjectDocumentationProvider;
 import org.facttype.ui.hover.DiagramEObjectHoverProvider;
 
 import com.google.inject.Binder;
 
 /**
  * Use this class to register components to be used within the IDE.
  */
 public class DiagramUiModule extends org.facttype.ui.AbstractDiagramUiModule {
 	public DiagramUiModule(AbstractUIPlugin plugin) {
 		super(plugin);
 	}
 
 	/**
 	 * Register our hover provider
 	 * https://github.com/escay/Facttype/issues/20
 	 */
 	public Class<? extends IEObjectHoverProvider> bindIEObjectHoverProvider() {
 		return DiagramEObjectHoverProvider.class;
 	}
 
 	/**
 	 * Register our documentation provider
 	 * https://github.com/escay/Facttype/issues/20
 	 */
 	public Class<? extends IEObjectDocumentationProvider> bindIEObjectDocumentationProviderr() {
 		return DiagramEObjectDocumentationProvider.class;
 	}
 	
 	@Override
 	public Class<? extends IXtextBuilderParticipant> bindIXtextBuilderParticipant() {
 		// Use our own builder to enable generating one file based on all models
 		// This generates the index.html file.
 		return JavaProjectBasedBuilderParticipant2.class;
 	}
 	
 	@Override
 	public void configure(Binder binder) {
 		super.configure(binder);
 		
 		// Start auto complete when a space is entered in the model file.
 		// See also http://christiandietrich.wordpress.com/2011/09/19/xtext-content-assist-auto-activation/
 		//
		// The characters "[" and newLine will now trigger code completion automatically
		String eol = System.getProperty("line.separator");
 		binder.bind(String.class).annotatedWith(
 		    com.google.inject.name.Names.named((XtextContentAssistProcessor.COMPLETION_AUTO_ACTIVATION_CHARS)))
				.toInstance("[" + eol);
 	}
 }
