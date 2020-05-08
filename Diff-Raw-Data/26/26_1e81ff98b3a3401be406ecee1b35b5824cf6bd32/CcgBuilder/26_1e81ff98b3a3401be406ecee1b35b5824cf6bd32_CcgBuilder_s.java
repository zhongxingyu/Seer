 /*
 * $Id: CcgBuilder.java,v 1.10 2006-12-08 16:28:46 concentus Exp $
  * 
  * Copyright 2005 Sebastian Hasait
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package de.hasait.eclipse.ccg.builder;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 
 import de.hasait.eclipse.ccg.generator.CcgGeneratorLookupEp;
 import de.hasait.eclipse.ccg.generator.ICcgBlockGenerator;
 import de.hasait.eclipse.ccg.generator.ICcgGenerator;
 import de.hasait.eclipse.ccg.generator.ICcgGeneratorLookup;
 import de.hasait.eclipse.ccg.generator.ICcgResourceGenerator;
 import de.hasait.eclipse.ccg.parser.CcgParserLookupEp;
 import de.hasait.eclipse.ccg.parser.ICcgComment;
 import de.hasait.eclipse.ccg.parser.ICcgNonComment;
 import de.hasait.eclipse.ccg.parser.ICcgParser;
 import de.hasait.eclipse.ccg.parser.ICcgParserLookup;
 import de.hasait.eclipse.ccg.parser.ICcgRoot;
 import de.hasait.eclipse.ccg.parser.ICcgTreeChild;
 import de.hasait.eclipse.ccg.properties.CcgProjectConfiguration;
 import de.hasait.eclipse.common.OidGenerator;
 import de.hasait.eclipse.common.Util;
 import de.hasait.eclipse.common.resource.XFile;
 import de.hasait.eclipse.common.resource.XFolder;
 import de.hasait.eclipse.common.resource.XProject;
 import de.hasait.eclipse.common.xml.XElement;
 
 /**
  * @author Sebastian Hasait (hasait at web.de)
 * @version $Revision: 1.10 $
  */
 public class CcgBuilder extends IncrementalProjectBuilder {
 	/**
 	 * ID of this builder.
 	 */
 	public static final String BUILDER_ID = "de.hasait.eclipse.ccg.builder";
 
 	private static final String MARKER_TYPE = "de.hasait.eclipse.ccg.problem";
 
 	/**
 	 * ID of {@link ICcgParser} extension point.
 	 */
 	public static final String PARSERS_EXTENSION_POINT_ID = "de.hasait.eclipse.ccg.parsers";
 
 	private final ICcgParserLookup _parserLookup = new CcgParserLookupEp(PARSERS_EXTENSION_POINT_ID);
 
 	/**
 	 * ID of {@link ICcgGenerator} extension point.
 	 */
 	public static final String GENERATORS_EXTENSION_POINT_ID = "de.hasait.eclipse.ccg.generators";
 
 	private final ICcgGeneratorLookup _generatorLookup = new CcgGeneratorLookupEp(GENERATORS_EXTENSION_POINT_ID);
 
 	/**
 	 * File extension used for resource generator scripts.
 	 */
	public static final String RESOURCE_GENERATOR_FILE_EXTENSION = "ccg.xml";
 
 	/**
 	 * File extension used for resource generator scripts.
 	 */
 	public static final String RESOURCE_GENERATOR_ROOT_TAG_NAME = "ccg";
 
 	protected final IProject[] build(final int kind, final Map args, final IProgressMonitor monitor)
 	      throws CoreException {
 		CcgProjectConfiguration configuration = CcgProjectConfiguration.getProjectConfiguration(getProject());
 		if (kind == FULL_BUILD) {
 			fullBuild(configuration, monitor);
 		} else {
 			IResourceDelta delta = getDelta(getProject());
 			if (delta == null) {
 				fullBuild(configuration, monitor);
 			} else {
 				fullBuild(configuration, monitor);
 				// incrementalBuild(delta, monitor);
 			}
 		}
 		return null;
 	}
 
 	protected final void fullBuild(final CcgProjectConfiguration configuration, final IProgressMonitor monitor)
 	      throws CoreException {
 		getProject().accept(new IResourceVisitor() {
 			public boolean visit(final IResource resource) {
 				executeGenerators(resource, configuration, monitor);
 				return true;
 			}
 		});
 	}
 
 	protected final void incrementalBuild(final IResourceDelta buildDelta, final CcgProjectConfiguration configuration,
 	      final IProgressMonitor monitor) throws CoreException {
 		buildDelta.accept(new IResourceDeltaVisitor() {
 			public boolean visit(final IResourceDelta delta) throws CoreException {
 				IResource resource = delta.getResource();
 				switch (delta.getKind()) {
 				case IResourceDelta.ADDED:
 					executeGenerators(resource, configuration, monitor);
 					break;
 				case IResourceDelta.REMOVED:
 					executeGenerators(resource, configuration, monitor);
 					break;
 				case IResourceDelta.CHANGED:
 					executeGenerators(resource, configuration, monitor);
 					break;
 				}
 				return true;
 			}
 		});
 	}
 
 	protected final void executeGenerators(final IResource resource, final CcgProjectConfiguration configuration,
 	      final IProgressMonitor monitor) {
 		if (resource instanceof IFile) {
 			XFile file = new XFile((IFile) resource, getProject());
			String fileExtension = file.getFileExtension();
			if (RESOURCE_GENERATOR_FILE_EXTENSION.equals(fileExtension) || _parserLookup.containsParser(fileExtension)) {
				executeGenerators(file, fileExtension, configuration, monitor);
 			}
 		}
 	}
 
	private void executeGenerators(final XFile sourceFile, final String sourceFileExtension,
	      final CcgProjectConfiguration configuration, final IProgressMonitor monitor) {
 		try {
 			deleteMarkers(sourceFile.getRawFile());
 			// create a context to allow data-exchange between generators...
 			Map sourceFileContext = new HashMap();
			if (RESOURCE_GENERATOR_FILE_EXTENSION.equals(sourceFileExtension)) {
 				// check for valid sourceFolder
 				if (isPrefixOf(getProject(), configuration.getSourceFolderPaths(), sourceFile.getRawResource())) {
 					// our own file-format - execute generators...
 					executeResourceGenerators(sourceFile, sourceFileContext, configuration, monitor);
 				}
 			} else {
 				// foreign file - lookup comment-parser...
				ICcgParser parser = _parserLookup.findParser(sourceFileExtension);
 				if (parser != null) {
 					// ok - found a parser - try to parse...
 					ICcgRoot root = parser.parse(sourceFile.read());
 					if (root != null) {
 						// parsed - execute generators...
 						if (executeBlockGenerators(sourceFile, root, sourceFileContext, monitor)) {
 							sourceFile.write(root.getSource(), null, monitor);
 						}
 					}
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			addMarker(sourceFile.getRawFile(), e, -1, IMarker.SEVERITY_ERROR);
 		}
 	}
 
 	private boolean isPrefixOf(IProject base, String[] paths, IResource resource) {
 		for (int i = 0; i < paths.length; i++) {
 			IFolder folder = base.getFolder(paths[i]);
 			if (folder.getFullPath().isPrefixOf(resource.getFullPath())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private void executeResourceGenerators(final XFile sourceFile, final Map sourceFileContext,
 	      final CcgProjectConfiguration configuration, final IProgressMonitor monitor) throws Exception {
 		XElement sourceElement = sourceFile.parseXml();
 		if (RESOURCE_GENERATOR_ROOT_TAG_NAME.equals(sourceElement.getTagName())) {
 			// contains our tag - continue...
 			XFolder targetBaseFolder = new XProject(getProject(), getProject()).getFolder(configuration
 			      .getOutputFolderPath());
 			// each childElement of root represents a generator...
 			XElement[] configElements = sourceElement.getChildElements();
 			for (int configElementsI = 0; configElementsI < configElements.length; configElementsI++) {
 				XElement configElement = configElements[configElementsI];
 				String configElementTagName = configElement.getTagName();
 				ICcgResourceGenerator generator = _generatorLookup.findResourceGenerator(configElementTagName);
 				if (generator != null) {
 					// found a generator for tagName - execute...
 					generator.generateResources(configElement, sourceFile, targetBaseFolder, sourceFileContext,
 					      _generatorLookup, monitor);
 				} else {
 					throw new IllegalArgumentException("Unknown generator: " + configElementTagName);
 				}
 			}
 		}
 	}
 
 	private boolean executeBlockGenerators(final XFile sourceFile, final ICcgRoot root, final Map sourceFileContext,
 	      final IProgressMonitor monitor) throws Exception {
 		int index = 0;
 		while (index < root.childNodesSize()) {
 			ICcgTreeChild child = root.getChildNode(index);
 			if (child instanceof ICcgComment) {
 				ICcgComment blockStartComment = (ICcgComment) child;
 				String command = blockStartComment.getCommand();
 				if (command != null) {
 					String block;
 					XElement configElement = XElement.parse(command);
 					ICcgBlockGenerator generator = _generatorLookup.findBlockGenerator(configElement.getTagName());
 					if (generator == null) {
 						throw new IllegalArgumentException("Unknown generator: " + configElement.getTagName());
 					}
 					block = generator.generateBlock(configElement, blockStartComment, sourceFile, sourceFileContext,
 					      _generatorLookup, monitor);
 					String blockId = blockStartComment.getBlockStart();
 					ICcgComment blockEndComment = null;
 					if (blockId != null) {
 						int a = 1;
 						while (index + a < root.childNodesSize() && blockEndComment == null) {
 							child = root.getChildNode(index + a);
 							if (child instanceof ICcgComment && Util.equals(((ICcgComment) child).getBlockEnd(), blockId)) {
 								blockEndComment = (ICcgComment) child;
 							} else {
 								root.removeChildNode(index + a);
 							}
 						}
 					}
 					if (blockId == null) {
 						blockId = OidGenerator.getInstance().getOid();
 					}
 					ICcgNonComment blockNonComment = root.createNonComment();
 					root.insertChildNode(index + 1, blockNonComment);
 					if (blockEndComment == null) {
 						blockEndComment = root.createComment();
 						root.insertChildNode(index + 2, blockEndComment);
 					}
 					blockStartComment.setBlockStart(blockId);
 					blockNonComment.setSource(block);
 					blockEndComment.setBlockEnd(blockId);
 				}
 			}
 			index++;
 		}
 		return true;
 	}
 
 	private void addMarker(IFile file, Exception exception, int lineNumber, int severity) {
 		StringWriter messageWriter = new StringWriter();
 		PrintWriter printWriter = new PrintWriter(messageWriter);
 		if (exception != null) {
 			printWriter.println(exception.getMessage());
 			exception.printStackTrace(printWriter);
 		} else {
 			printWriter.println("Exception is null");
 		}
 		addMarker(file, messageWriter.getBuffer().toString(), lineNumber, severity);
 	}
 
 	private void addMarker(IFile file, String message, int lineNumber, int severity) {
 		try {
 			IMarker marker = file.createMarker(MARKER_TYPE);
 			marker.setAttribute(IMarker.MESSAGE, message);
 			marker.setAttribute(IMarker.SEVERITY, severity);
 			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber < 1 ? 1 : lineNumber);
 		} catch (CoreException e) {
 			// ignore
 		}
 	}
 
 	private void deleteMarkers(IFile file) {
 		try {
 			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
 		} catch (CoreException e) {
 			// ignore
 		}
 	}
 }
