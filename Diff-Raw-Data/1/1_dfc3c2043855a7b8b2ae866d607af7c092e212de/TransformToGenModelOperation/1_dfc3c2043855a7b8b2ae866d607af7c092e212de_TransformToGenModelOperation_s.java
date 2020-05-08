 /*
  * Copyright (c) 2006, 2008 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Alexander Fedorov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.internal.bridge.transform;
 
 import java.io.IOException;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
 import org.eclipse.emf.common.util.BasicDiagnostic;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.ContentHandler;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.util.EcoreUtil.ExternalCrossReferencer;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.gmf.codegen.gmfgen.GenEditorGenerator;
 import org.eclipse.gmf.graphdef.codegen.MapModeCodeGenStrategy;
 import org.eclipse.gmf.internal.bridge.VisualIdentifierDispenser;
 import org.eclipse.gmf.internal.bridge.genmodel.BasicDiagramRunTimeModelHelper;
 import org.eclipse.gmf.internal.bridge.genmodel.DiagramGenModelTransformer;
 import org.eclipse.gmf.internal.bridge.genmodel.DiagramRunTimeModelHelper;
 import org.eclipse.gmf.internal.bridge.genmodel.GenModelProducer;
 import org.eclipse.gmf.internal.bridge.genmodel.InnerClassViewmapProducer;
 import org.eclipse.gmf.internal.bridge.genmodel.ViewmapProducer;
 import org.eclipse.gmf.internal.bridge.naming.gen.GenModelNamingMediator;
 import org.eclipse.gmf.internal.bridge.naming.gen.GenNamingMediatorImpl;
 import org.eclipse.gmf.internal.bridge.ui.Plugin;
 import org.eclipse.gmf.internal.codegen.util.GMFGenConfig;
 import org.eclipse.gmf.internal.common.migrate.ModelLoadHelper;
 import org.eclipse.gmf.internal.common.reconcile.Reconciler;
 import org.eclipse.gmf.mappings.Mapping;
 
 public class TransformToGenModelOperation {
 	
 	private URI myGMFGenModelURI;
 	private TransformOptions myOptions;
 	private Mapping myMapping;
 	private GenModelDetector myGMDetector;
 	private GenModel myGenModel;
 	
 	private Diagnostic myMapmodelValidationResult = Diagnostic.CANCEL_INSTANCE;
 	private Diagnostic myGMFGenValidationResult = Diagnostic.CANCEL_INSTANCE;
 
 	private IStatus myStaleGenmodelStatus = Status.CANCEL_STATUS;
 	private final ResourceSet myResourceSet;
 	
 	public TransformToGenModelOperation(ResourceSet rs) {
 		assert rs != null;
 		myResourceSet = rs;
 		this.myOptions = new TransformOptions();
 	}
 
 	public TransformOptions getOptions() {
 		return myOptions;
 	}
 	
 	public URI getGenURI() {
 		return this.myGMFGenModelURI;
 	}
 
 	public void setGenURI(URI gmfGen) {
 		this.myGMFGenModelURI = gmfGen;
 	}
 
 	public GenModel getGenModel() {
 		return this.myGenModel;
 	}
 
 	public final ResourceSet getResourceSet() {
 		return myResourceSet;
 	}
 
 	Mapping getMapping() {
 		return this.myMapping;
 	}
 	
 	private void setMapping(Mapping m, Diagnostic validationResult) {
 		this.myMapping = m;
 		this.myMapmodelValidationResult = validationResult;
 		myGMDetector = (m != null) ? new GenModelDetector(m) : null;
 		myGenModel = null;
 	}
 	
 	private void setGMFGenValidationResult(Diagnostic validationResult) {
 		this.myGMFGenValidationResult = validationResult;
 	}
 
 	public GenModelDetector getGenModelDetector() {
 		return myGMDetector;
 	}
 	
 	public Diagnostic getGMFGenValidationResult() {
 		return this.myGMFGenValidationResult;
 	}
 
 	public Diagnostic getMapmodelValidationResult() {
 		return this.myMapmodelValidationResult;
 	}
 	
 	public IStatus getStaleGenmodelStatus() {
 		return this.myStaleGenmodelStatus;
 	}
 
 	public Mapping loadMappingModel(URI uri, IProgressMonitor pm) throws CoreException {
 		Mapping content = null;
 		IStatus status = Status.CANCEL_STATUS;
 		Diagnostic validation = Diagnostic.CANCEL_INSTANCE;
 		IProgressMonitor monitor = null;
 		try {
 			if (uri == null) {
 				throw new IllegalArgumentException(Messages.TransformToGenModelOperation_e_null_map_uri);
 			}
 			monitor = (pm != null) ? new SubProgressMonitor(pm, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK) : new NullProgressMonitor(); 
 			String cancelMessage = Messages.TransformToGenModelOperation_e_map_load_cancelled;
 			monitor.beginTask("", 100); //$NON-NLS-1$
 			subTask(monitor, 0, Messages.TransformToGenModelOperation_task_load, cancelMessage);
 			ModelLoadHelper loadHelper = new ModelLoadHelper(getResourceSet(), uri);
 			if (!loadHelper.isOK()) {
 				throw new CoreException(loadHelper.getStatus());
 			}
 			subTask(monitor, 20, Messages.TransformToGenModelOperation_task_validate, cancelMessage);
 			EObject root = loadHelper.getContentsRoot();
 			if (!(root instanceof Mapping)) {
 				String msg = MessageFormat.format(Messages.TransformToGenModelOperation_e_wrong_root_element, root.getClass().getName());
 				status = Plugin.createError(msg, null);
 				throw new CoreException(status);
 			}
 			content = (Mapping) loadHelper.getContentsRoot();
 			validation = ValidationHelper.validate(content, true, monitor);
 			monitor.worked(60);
 			if (Diagnostic.CANCEL == validation.getSeverity()) {
 				throw new CoreException(Plugin.createCancel(cancelMessage));
 			}
 			return content;
 		} catch (CoreException e) {
 			throw e;
 		} catch (Exception e) {
 			IStatus error = Plugin.createError(Messages.TransformToGenModelOperation_e_load_mapping_model, e);
 			throw new CoreException(error);
 		} finally {
 			setMapping(content, validation);
 			if (monitor != null) {
 				monitor.done();
 			}
 		}
 	}
 	
 	public GenModel findGenmodel() throws CoreException {
 		try {
 			checkMapping();
 			GenModelDetector gmd = getGenModelDetector();
 			IStatus detect = gmd.detect();
 			if (detect.isOK()) {
 				GenModel genModel = gmd.get(getResourceSet());
 				this.myGenModel = genModel;
 				return genModel;
 			}
 			throw new CoreException(detect);
 		} catch (Exception e) {
 			IStatus error = Plugin.createError(Messages.TransformToGenModelOperation_e_mapping_invalid, e);
 			throw new CoreException(error);
 		}
 	}
 
 	public GenModel loadGenModel(URI uri, IProgressMonitor pm) throws CoreException {
 		IProgressMonitor monitor = null;
 		try {
 			checkMapping();
 			monitor = (pm != null) ? new SubProgressMonitor(pm, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK) : new NullProgressMonitor(); 
 			String cancelMessage = Messages.TransformToGenModelOperation_e_genmodel_load_cancelled;
 			monitor.beginTask("", 100); //$NON-NLS-1$
 			monitor.subTask(Messages.TransformToGenModelOperation_task_detect);
 			GenModelDetector gmd = getGenModelDetector();
 			IStatus status = Status.OK_STATUS;
 			if (uri == null) {
 				status = gmd.detect();
 			} else {
 				status = gmd.advise(uri); 
 			}
 			if (!status.isOK()) {
 				throw new CoreException(status);
 			}
 			subTask(monitor, 30, Messages.TransformToGenModelOperation_task_load, cancelMessage);
 			GenModel genModel = gmd.get(getResourceSet());
 			if (genModel == null) {
 				if (uri == null) {
 					this.myStaleGenmodelStatus = Status.CANCEL_STATUS;
 					this.myGenModel = null;
 					return null;
 				}
 				IStatus notFound = Plugin.createError(Messages.GenModelDetector_e_not_found, null);
 				throw new CoreException(notFound);
 			}
 			subTask(monitor, 40, Messages.TransformToGenModelOperation_task_validate, cancelMessage);
 			StaleGenModelDetector staleDetector = new StaleGenModelDetector(genModel);
 			IStatus stale = staleDetector.detect();
 			this.myGenModel = genModel;
 			this.myStaleGenmodelStatus = stale;
 			return genModel;
 
 		} catch (CoreException e) {
 			throw e;
 		} catch (Exception e) {
 			IStatus error = Plugin.createError(Messages.TransformToGenModelOperation_e_genmodel_load, e);
 			throw new CoreException(error);
 		} finally {
 			if (monitor != null) {
 				monitor.done();
 			}
 		}
 	}
 	
 	public IStatus executeTransformation(IProgressMonitor pm) {
 		IProgressMonitor monitor = null;
 		Diagnostic validation = Diagnostic.CANCEL_INSTANCE;
 		try {
 			if (getGenURI() == null) {
 				throw new IllegalStateException(Messages.TransformToGenModelOperation_e_null_gmfgen_uri);
 			}
 			checkMapping();
 			monitor = (pm != null) ? new SubProgressMonitor(pm, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK) : new NullProgressMonitor(); 
 			monitor.beginTask("", 100); //$NON-NLS-1$
 			if (monitor.isCanceled()) {
 				return Status.CANCEL_STATUS;
 			}
 			final DiagramRunTimeModelHelper drtModelHelper = detectRunTimeModel();
 			final ViewmapProducer viewmapProducer = detectTransformationOptions();
 			final VisualIdentifierDispenserProvider idDispenser = getVisualIdDispenser();
 			idDispenser.acquire();
 
 			GenModelProducer t = createGenModelProducer(getGenModel(), drtModelHelper, viewmapProducer, idDispenser.get());
 
 			monitor.subTask(Messages.TransformToGenModelOperation_task_generate);
 			GenEditorGenerator genEditor = t.process(getMapping(), new SubProgressMonitor(monitor, 20));
 			if (monitor.isCanceled()) {
 				return Status.CANCEL_STATUS;
 			}
 			monitor.subTask(Messages.TransformToGenModelOperation_task_reconcile);
 			if (Plugin.needsReconcile()) {
 				reconcile(genEditor);
 			}
 			GenNamingMediatorImpl namer = new GenNamingMediatorImpl();
 			namer.setMode(GenNamingMediatorImpl.Mode.COLLECT_NAMES);
 			namer.traverse(genEditor); // collect reconciled names
 			namer.setMode(GenNamingMediatorImpl.Mode.DISPENSE_NAMES);
 			namer.traverse(genEditor); // dispense names to new elements
 			monitor.worked(20);
 			if (monitor.isCanceled()) {
 				return Status.CANCEL_STATUS;
 			}
 			monitor.subTask(Messages.TransformToGenModelOperation_task_save);
 			save(genEditor);
 			monitor.worked(20);
 			if (monitor.isCanceled()) {
 				return Status.CANCEL_STATUS;
 			}
 			monitor.subTask(Messages.TransformToGenModelOperation_task_validate);
 			try {
 				validation = ValidationHelper.validate(genEditor, true, monitor);
 			} catch (RuntimeException re) {
 				validation = BasicDiagnostic.toDiagnostic(re);
 			}
 			if (Diagnostic.CANCEL != validation.getSeverity()) {
 				idDispenser.release();
 			}
 			return Status.OK_STATUS;
 			
 		} catch (Exception ex) {
 			String message = ex.getMessage();
 			if (message == null) {
 				message = Messages.TransformToGenModelOperation_e_generator_creation;
 			}
 			return Plugin.createError(message, ex);
 		} finally {
 			setGMFGenValidationResult(validation);
 			if (monitor != null) {
 				monitor.done();
 			}
 		}
 	}
 
 	private void checkMapping() {
 		if (getMapping() == null) {
 			throw new IllegalStateException(Messages.TransformToGenModelOperation_e_null_mapping);
 		}
 	}
 	
 	static IStatus getFirst(Diagnostic d) {
 		if (d == null) {
 			return Status.OK_STATUS;
 		}
 		List<Diagnostic> children = d.getChildren();
 		if (children.isEmpty()) {
 			return BasicDiagnostic.toIStatus(d);
 		} else {
 			return BasicDiagnostic.toIStatus(children.get(0));
 		}
 	}
 	
 	private DiagramRunTimeModelHelper detectRunTimeModel() {
 		return new BasicDiagramRunTimeModelHelper();
 	}
 
 	private ViewmapProducer detectTransformationOptions() {
 		String runtimeToken = getOptions().getUseRuntimeFigures() ? "full" : "lite";
 		MapModeCodeGenStrategy mmStrategy = getOptions().getUseMapMode() ? MapModeCodeGenStrategy.DYNAMIC : MapModeCodeGenStrategy.STATIC;
 		URL dynamicFigureTemplates = getOptions().getFigureTemplatesPath();
 		return new InnerClassViewmapProducer(runtimeToken, mmStrategy, dynamicFigureTemplates == null ? null : new URL[] {dynamicFigureTemplates});
 	}
 
 	private VisualIdentifierDispenserProvider getVisualIdDispenser() {
 		return new VisualIdentifierDispenserProvider(getGenURI());
 	}
 
 	private GenModelProducer createGenModelProducer(GenModel domainGenModel, final DiagramRunTimeModelHelper drtModelHelper, final ViewmapProducer viewmapProducer, final VisualIdentifierDispenser idDespenser) {
 		final DiagramGenModelTransformer t = new DiagramGenModelTransformer(drtModelHelper, new GenModelNamingMediator.Empty(), viewmapProducer, idDespenser, getOptions().getGenerateRCP());
 		if (domainGenModel != null) {
 			t.setEMFGenModel(domainGenModel);
 		}
 		return new GenModelProducer() {
 
 			public GenEditorGenerator process(Mapping mapping, IProgressMonitor progress) {
 				progress.beginTask(null, 1);
 				try {
 					t.transform(mapping);
 					return t.getResult();
 				} finally {
 					progress.done();
 				}
 			}
 		};
 	}
 
 	private void reconcile(GenEditorGenerator genBurdern) {
 		GenEditorGenerator old = null;
 		Resource resource = null;
 		try {
 			resource = getResourceSet().getResource(getGenURI(), true);
 			List<EObject> contents = resource.getContents();
 			if (!contents.isEmpty() && contents.get(0) instanceof GenEditorGenerator) {
 				old = (GenEditorGenerator) contents.get(0);
 			}
 			if (old != null) {
 				new Reconciler(new GMFGenConfig()).reconcileTree(genBurdern, old);
 			}
 		} catch (RuntimeException e) {
 			old = null;
 		} finally {
 			if (resource != null) {
 				resource.unload();
 			}
 		}
 	}
 
 	private void save(GenEditorGenerator genBurdern) throws IOException {
 		try {
 			Resource gmfgenRes = getResourceSet().getResource(getGenURI(), true);
 			updateExistingResource(gmfgenRes, genBurdern);
 			// one might want to ignore dangling href on save when there are more than one
 			// content object - there are chances we don't match them during reconcile and 
 			// failed update all the references.
 			final Map<String, Object> saveOptions = getSaveOptions();
 			if (gmfgenRes.getContents().size() > 1 && Plugin.ignoreDanglingHrefOnSave()) {
 				saveOptions.put(XMLResource.OPTION_PROCESS_DANGLING_HREF, XMLResource.OPTION_PROCESS_DANGLING_HREF_RECORD);
 			}
 			gmfgenRes.save(saveOptions);
 		} catch (RuntimeException ex) {
 			Resource dgmmRes = getResourceSet().createResource(getGenURI(), ContentHandler.UNSPECIFIED_CONTENT_TYPE);
 			dgmmRes.getContents().add(genBurdern);
 			dgmmRes.save(getSaveOptions());
 		}
 	}
 
 	private static void updateExistingResource(Resource gmfgenRes, GenEditorGenerator genBurden) {
 		boolean editorGenFound = false;
 		for (int i = 0; !editorGenFound && i < gmfgenRes.getContents().size(); i++) {
 			if (gmfgenRes.getContents().get(i) instanceof GenEditorGenerator) {
 				if (gmfgenRes.getContents().size() > 1) {
 					// chances there are other content eobjects that reference 
 					// some parts of old GenEditorGenerator, hence need update
 					LinkedList<EObject> rest = new LinkedList<EObject>(gmfgenRes.getContents());
 					GenEditorGenerator oldEditorGenerator = (GenEditorGenerator) rest.remove(i);
 					updateExternalReferences(genBurden, oldEditorGenerator, rest);
 				}
 				gmfgenRes.getContents().set(i, genBurden); // replace with new one
 				editorGenFound = true;
 			}
 		}
 		if (!editorGenFound) {
 			gmfgenRes.getContents().add(genBurden);
 		}
 	}
 
 	private static void updateExternalReferences(GenEditorGenerator newEditorGenerator, final GenEditorGenerator oldEditorGenerator, List<EObject> allContentButOldGenerator) {
 		// find references from rest of the content to old generator
 		final Map<EObject, Collection<EStructuralFeature.Setting>> crossReferences = new ExternalCrossReferencer(allContentButOldGenerator) {
 			@Override
 			protected boolean crossReference(EObject object, EReference reference, EObject crossReferencedEObject) {
 				return super.crossReference(object, reference, crossReferencedEObject) && EcoreUtil.isAncestor(oldEditorGenerator, crossReferencedEObject);
 			}
 
 			Map<EObject, Collection<EStructuralFeature.Setting>> find() {
 				return findExternalCrossReferences();
 			}
 		}.find();
 		// match new and old objects using reconciler without decisions
 		new Reconciler(new GMFGenConfig()) {
 			@Override
 			protected void handleNotMatchedCurrent(EObject current) {/*no-op*/};
 			@Override
 			protected EObject handleNotMatchedOld(EObject currentParent, EObject notMatchedOld) {
 				return null; /*no-op*/
 			};
 			@Override
 			protected void reconcileVertex(EObject current, EObject old) {
 				if (!crossReferences.containsKey(old)) {
 					return;
 				}
 				// and replace old values with new
 				for (EStructuralFeature.Setting s : crossReferences.get(old)) {
 					EcoreUtil.replace(s, old, current);
 				}
 			}
 		}.reconcileTree(newEditorGenerator, oldEditorGenerator);
 	}
 
 	private Map<String,Object> getSaveOptions() {
 		HashMap<String, Object> saveOptions = new HashMap<String, Object>();
 		saveOptions.put(XMLResource.OPTION_ENCODING, "UTF-8"); //$NON-NLS-1$
 		return saveOptions;
 	}
 
 	private static void subTask(IProgressMonitor monitor, int ticks, String name, String cancelMessage) throws CoreException{
 		if (monitor == null) {
 			return;
 		}
 		if (monitor.isCanceled()) {
 			IStatus cancel = Plugin.createCancel(cancelMessage);
 			throw new CoreException(cancel);
 		}
 		if (ticks > 0) {
 			monitor.worked(ticks);
 		}
 		if (name != null) {
 			monitor.subTask(name);
 		}
 	}
 }
