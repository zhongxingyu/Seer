 /*******************************************************************************
  * Copyright (c) 2010 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.javascript.internal.core.codeassist;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.concurrent.Callable;
 
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.utils.ASTUtil;
 import org.eclipse.dltk.codeassist.ScriptSelectionEngine;
 import org.eclipse.dltk.compiler.env.IModuleSource;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IMember;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelElementVisitor;
 import org.eclipse.dltk.core.IParent;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ISourceRange;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.ScriptModelUtil;
 import org.eclipse.dltk.core.model.LocalVariable;
 import org.eclipse.dltk.core.model.UnresolvedElement;
 import org.eclipse.dltk.internal.javascript.ti.IReferenceAttributes;
 import org.eclipse.dltk.internal.javascript.ti.JSDocSupport;
 import org.eclipse.dltk.internal.javascript.ti.JSDocSupport.ParameterNode;
 import org.eclipse.dltk.internal.javascript.ti.PositionReachedException;
 import org.eclipse.dltk.internal.javascript.ti.TypeInferencer2;
 import org.eclipse.dltk.internal.javascript.validation.JavaScriptValidations;
 import org.eclipse.dltk.javascript.ast.Argument;
 import org.eclipse.dltk.javascript.ast.FunctionStatement;
 import org.eclipse.dltk.javascript.ast.Identifier;
 import org.eclipse.dltk.javascript.ast.MultiLineComment;
 import org.eclipse.dltk.javascript.ast.PropertyInitializer;
 import org.eclipse.dltk.javascript.ast.Script;
 import org.eclipse.dltk.javascript.ast.StringLiteral;
 import org.eclipse.dltk.javascript.core.JavaScriptPlugin;
 import org.eclipse.dltk.javascript.parser.JavaScriptParserUtil;
 import org.eclipse.dltk.javascript.parser.jsdoc.JSDocTag;
 import org.eclipse.dltk.javascript.typeinference.IValueReference;
 import org.eclipse.dltk.javascript.typeinference.ReferenceKind;
 import org.eclipse.dltk.javascript.typeinference.ReferenceLocation;
 import org.eclipse.dltk.javascript.typeinfo.IElementConverter;
 import org.eclipse.dltk.javascript.typeinfo.IRType;
 import org.eclipse.dltk.javascript.typeinfo.JSTypeSet;
 import org.eclipse.dltk.javascript.typeinfo.TypeInfoManager;
 import org.eclipse.dltk.javascript.typeinfo.model.Element;
 import org.eclipse.dltk.javascript.typeinfo.model.Member;
 import org.eclipse.dltk.javascript.typeinfo.model.Method;
 import org.eclipse.dltk.javascript.typeinfo.model.Property;
 import org.eclipse.dltk.javascript.typeinfo.model.Type;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeKind;
 
 public class JavaScriptSelectionEngine2 extends ScriptSelectionEngine {
 
 	private static final boolean DEBUG = false;
 
 	@SuppressWarnings("serial")
 	private static class ModelElementFound extends RuntimeException {
 		final IModelElement element;
 
 		public ModelElementFound(IModelElement element) {
 			this.element = element;
 		}
 
 	}
 
 	private static class Visitor implements IModelElementVisitor {
 
 		private final int nameStart;
 		private final int nameEnd;
 
 		public Visitor(int nameStart, int nameEnd) {
 			this.nameStart = nameStart;
 			this.nameEnd = nameEnd;
 		}
 
 		public boolean visit(IModelElement element) {
 			if (element instanceof IMember) {
 				IMember member = (IMember) element;
 				try {
 					ISourceRange range = member.getNameRange();
 					if (range.getOffset() == nameStart
 							&& range.getLength() == nameEnd - nameStart) {
 						throw new ModelElementFound(element);
 					}
 				} catch (ModelException e) {
 					//
 				}
 			}
 			return true;
 		}
 
 	}
 
 	private static boolean isStringLiteralInObjectLiteral(ASTNode node) {
 		if (node instanceof StringLiteral) {
 			final StringLiteral literal = (StringLiteral) node;
 			if (literal.getParent() instanceof PropertyInitializer) {
 				final PropertyInitializer initializer = (PropertyInitializer) literal
 						.getParent();
 				return initializer.getName() == literal;
 			}
 		}
 		return false;
 	}
 
 	public IModelElement[] select(final IModuleSource module, int position,
 			int i) {
 		if (!(module.getModelElement() instanceof ISourceModule) || i == -1) {
 			return null;
 		}
 		String content = module.getSourceContents();
 		if (position < 0 || position > content.length()) {
 			return null;
 		}
 		if (DEBUG) {
 			System.out
 					.println("select in " + module.getFileName() + " at " + position); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		final Script script = JavaScriptParserUtil.parse(module, null);
 
 		NodeFinder finder = new NodeFinder(position, i + 1);
 		finder.locate(script);
 		ASTNode node = finder.getNode();
 		if (node != null) {
 			if (DEBUG) {
 				System.out.println(node.getClass().getName() + "=" + node); //$NON-NLS-1$
 			}
 			if (node instanceof Identifier
 					|| isStringLiteralInObjectLiteral(node)) {
 				final TypeInferencer2 inferencer2 = new TypeInferencer2();
 				final SelectionVisitor visitor = new SelectionVisitor(
 						inferencer2, node);
 				inferencer2.setVisitor(visitor);
 				inferencer2.setModelElement(module.getModelElement());
 				try {
 					inferencer2.doInferencing(script);
 				} catch (PositionReachedException e) {
 					//
 				}
 				final IValueReference value = visitor.getValue();
 				if (value == null) {
 					if (DEBUG) {
 						System.out.println("value is null or not found"); //$NON-NLS-1$
 					}
 					return null;
 				}
 				try {
 					return TypeInferencer2.withTypeSystem(inferencer2,
 							new Callable<IModelElement[]>() {
 								public IModelElement[] call() throws Exception {
 									return toModelElements(inferencer2,
 											visitor, module, value);
 								}
 							});
 				} catch (Exception e) {
 					// Shouldn't happen, but declared for Callable.call()
 					JavaScriptPlugin.error(e);
 				}
 			} else if (node instanceof MultiLineComment) {
 				final MultiLineComment comment = (MultiLineComment) node;
 				if (comment.isDocumentation()) {
 					final JSDocTag tag = JSDocSupport.parse(comment).getTagAt(
 							position);
 					if (tag != null && JSDocTag.PARAM.equals(tag.name())) {
 						final ParameterNode paramNode = JSDocSupport
 								.parseParameter(tag);
 						if (paramNode != null
 								&& paramNode.offset <= position
 								&& paramNode.offset + paramNode.name.length() >= position) {
 							for (FunctionStatement function : ASTUtil.select(
									script, FunctionStatement.class)) {
 								if (function.getDocumentation() == comment
 										|| JSDocSupport.getComment(function) == comment) {
 									final Argument argument = function
 											.getArgument(paramNode.name);
 									if (argument != null) {
 										return new IModelElement[] { new LocalVariable(
 												module.getModelElement(),
 												paramNode.name,
 												argument.start(),
 												argument.end(),
 												argument.start(),
 												argument.end() - 1, null) };
 									}
 									break;
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	private IModelElement[] toModelElements(TypeInferencer2 inferencer2,
 			SelectionVisitor visitor, IModuleSource module,
 			IValueReference value) {
 		final ReferenceKind kind = value.getKind();
 		if (DEBUG) {
 			System.out.println(value + "," + kind); //$NON-NLS-1$
 		}
 		ISourceModule m = (ISourceModule) module.getModelElement();
 		if (kind == ReferenceKind.ARGUMENT || kind == ReferenceKind.LOCAL) {
 			final ReferenceLocation location = value.getLocation();
 			if (DEBUG) {
 				System.out.println(location);
 			}
 			if (location == ReferenceLocation.UNKNOWN) {
 				return null;
 			}
 			final IModelElement result = locateModelElement(location);
 			if (result != null
 					&& (result.getElementType() == IModelElement.FIELD || result
 							.getElementType() == IModelElement.METHOD)) {
 				return new IModelElement[] { result };
 			}
 			final IRType type = JavaScriptValidations.typeOf(value);
 			return new IModelElement[] { new LocalVariable(m, value.getName(),
 					location.getDeclarationStart(),
 					location.getDeclarationEnd(), location.getNameStart(),
 					location.getNameEnd() - 1, type == null ? null
 							: type.getName()) };
 		} else if (kind == ReferenceKind.FUNCTION
 				|| kind == ReferenceKind.GLOBAL || kind == ReferenceKind.FIELD) {
 			final ReferenceLocation location = value.getLocation();
 			if (DEBUG) {
 				System.out.println(location);
 			}
 			if (location == ReferenceLocation.UNKNOWN) {
 				return null;
 			}
 			final IModelElement result = locateModelElement(location);
 			if (result != null) {
 				return new IModelElement[] { result };
 			}
 		} else if (kind == ReferenceKind.PROPERTY) {
 			final Collection<Property> properties = JavaScriptValidations
 					.extractElements(value, Property.class);
 			if (properties != null) {
 				return convert(m, properties);
 			}
 		} else if (kind == ReferenceKind.METHOD) {
 			final List<Method> methods = JavaScriptValidations.extractElements(
 					value, Method.class);
 			if (methods != null) {
 				IValueReference[] arguments = visitor.getArguments();
 				if (arguments == null) {
 					arguments = new IValueReference[0];
 				}
 				final Method method = JavaScriptValidations.selectMethod(
 						inferencer2, methods, arguments, true);
 				return convert(m, Collections.singletonList(method));
 			}
 		} else if (kind == ReferenceKind.TYPE) {
 			final LinkedHashSet<Type> t = new LinkedHashSet<Type>();
 			JSTypeSet types = value.getDeclaredTypes();
 			if (types != null) {
 				Collections.addAll(t, types.toArray());
 			}
 			types = value.getTypes();
 			if (types != null) {
 				Collections.addAll(t, types.toArray());
 			}
 			if (!t.isEmpty()) {
 				return convert(m, t);
 			}
 		}
 		final ReferenceLocation location = value.getLocation();
 		if (location != ReferenceLocation.UNKNOWN
 				&& location.getSourceModule() != null) {
 			return new IModelElement[] { new UnresolvedElement(
 					location.getSourceModule(), value.getName(),
 					location.getNameStart(), location.getNameEnd() - 1) };
 		}
 		return null;
 	}
 
 	/**
 	 * @param module
 	 * @param elements
 	 * @return
 	 */
 	private IModelElement[] convert(ISourceModule module,
 			Collection<? extends Element> elements) {
 		List<IModelElement> result = new ArrayList<IModelElement>();
 		for (Element element : elements) {
 			try {
 				IModelElement me = convert(module, element);
 				if (me != null) {
 					result.add(me);
 				} else {
 					reportForeignElement(element);
 				}
 			} catch (ModelException e) {
 				//
 			}
 		}
 		return result.toArray(new IModelElement[result.size()]);
 	}
 
 	/**
 	 * @param module
 	 * @param element
 	 * @return
 	 * @throws ModelException
 	 */
 	private IModelElement convert(ISourceModule module, Element element)
 			throws ModelException {
 		Type type;
 		if (element instanceof Type) {
 			type = (Type) element;
 		} else {
 			type = ((Member) element).getDeclaringType();
 		}
 		if (type != null && type.getKind() == TypeKind.PREDEFINED) {
 			final List<String> path = new ArrayList<String>();
 			path.add(type.getName());
 			if (element != type) {
 				path.add(element.getName());
 			}
 			return resolveBuiltin(module.getScriptProject(), path);
 		}
 		if (type != null && type.getKind() == TypeKind.JAVASCRIPT) {
 			ReferenceLocation location = (ReferenceLocation) element
 					.getAttribute(IReferenceAttributes.LOCATION);
 			if (location != null && location != ReferenceLocation.UNKNOWN) {
 				if (element instanceof Property) {
 					return new LocalVariable(module, element.getName(),
 							location.getDeclarationStart(),
 							location.getDeclarationEnd(),
 							location.getNameStart(), location.getNameEnd() - 1,
 							null);
 				}
 				final IModelElement result = locateModelElement(location);
 				if (result != null) {
 					return result;
 				}
 			} else {
 				// TODO this only goes 1 deep, need support for nested types..
 				IModelElement[] children = module.getChildren();
 				for (IModelElement modelElement : children) {
 					if (modelElement.getElementType() == IModelElement.METHOD
 							&& modelElement.getElementName().equals(
 									type.getName())) {
 						IModelElement[] children2 = ((IParent) modelElement)
 								.getChildren();
 						for (IModelElement child : children2) {
 							if (child.getElementName()
 									.equals(element.getName()))
 								return child;
 						}
 						return modelElement;
 					}
 				}
 			}
 		}
 		for (IElementConverter converter : TypeInfoManager
 				.getElementConverters()) {
 			IModelElement result = converter.convert(module, element);
 			if (result != null) {
 				return result;
 			}
 		}
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	private IModelElement locateModelElement(final ReferenceLocation location) {
 		ISourceModule module = location.getSourceModule();
 		if (module != null) {
 			try {
 				ScriptModelUtil.reconcile(module);
 				module.accept(new Visitor(location.getNameStart(), location
 						.getNameEnd()));
 			} catch (ModelException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 			} catch (ModelElementFound e) {
 				return e.element;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @param project
 	 * @param segments
 	 * @return
 	 * @throws ModelException
 	 */
 	private IModelElement resolveBuiltin(IScriptProject project,
 			List<String> segments) throws ModelException {
 		for (IProjectFragment fragment : project.getProjectFragments()) {
 			if (fragment.isBuiltin()) {
 				ISourceModule m = fragment.getScriptFolder(Path.EMPTY)
 						.getSourceModule("builtins.js"); //$NON-NLS-1$
 				if (!m.exists()) {
 					return null;
 				}
 				IModelElement me = m;
 				SEGMENT_LOOP: for (String segment : segments) {
 					if (me instanceof IParent) {
 						final IModelElement[] children = ((IParent) me)
 								.getChildren();
 						for (IModelElement child : children) {
 							if (segment.equals(child.getElementName())) {
 								me = child;
 								continue SEGMENT_LOOP;
 							}
 						}
 					}
 					return null;
 				}
 				return me;
 			}
 		}
 		return null;
 	}
 }
