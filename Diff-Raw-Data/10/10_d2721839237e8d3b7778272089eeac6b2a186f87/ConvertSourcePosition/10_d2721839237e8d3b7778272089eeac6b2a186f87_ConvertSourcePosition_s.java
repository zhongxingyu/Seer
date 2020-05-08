 /*
  * aptIn16 - Apt implementation with Java 6 annotation processors.
  * Copyright (C) 2012 Travis Burtrum (moparisthebest)
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published y
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.moparisthebest.mirror.util;
 
 import com.moparisthebest.mirror.apt.ConvertAnnotationProcessorEnvironment;
 import com.moparisthebest.mirror.log.Debug;
 import com.sun.mirror.util.SourcePosition;
 import com.sun.source.tree.CompilationUnitTree;
 import com.sun.source.tree.ExpressionTree;
 import com.sun.source.tree.LineMap;
 import com.sun.source.util.SourcePositions;
 import com.sun.source.util.TreePath;
 import com.sun.source.util.Trees;
 
 import javax.annotation.processing.ProcessingEnvironment;
 import javax.lang.model.element.AnnotationMirror;
 import javax.lang.model.element.AnnotationValue;
 import javax.lang.model.element.Element;
 import javax.lang.model.type.TypeMirror;
 import javax.lang.model.util.Types;
 import javax.tools.JavaFileObject;
 import java.io.File;
 
 public class ConvertSourcePosition implements SourcePosition {
 
 	// declared as object so no runtime error is thrown if we aren't running under a sun compiler
 	private static Trees trees = null;
 	private static SourcePositions sourcePositions = null;
 	private static ConvertAnnotationProcessorEnvironment callback = null;
 
 	private static Types types = null;
 
 	//private static final ConvertSourcePosition emptySourcePosition = new ConvertSourcePosition(null, 0, 0);
 	private static final ConvertSourcePosition emptySourcePosition = null;
 
 	private final File file;
 	private final int line;
 	private final int column;
 
 	public ConvertSourcePosition(File file, int line, int column) {
 		this.file = file;
 		this.line = line;
 		this.column = column;
 		if (Debug.debug)
 			System.err.println(this);
 	}
 
 	public static void setProcessingEnvironment(ProcessingEnvironment pe, ConvertAnnotationProcessorEnvironment callback) {
 		ConvertSourcePosition.callback = callback;  //todo: keep a list of these if we start having multiple instances of ConvertAnnotationProcessorEnvironment
 		try {
 			ConvertSourcePosition.trees = Trees.instance(pe);
 			ConvertSourcePosition.sourcePositions = trees.getSourcePositions();
 		} catch (Throwable e) {
 			//e.printStackTrace(); // we aren't ALWAYS running under a sun compiler
 			System.out.println(e.getMessage());
 			System.out.println("Must not be running with a sun compiler, so SourcePosition will not be available to the apt classes...");
 		}
 		ConvertSourcePosition.types = pe.getTypeUtils();
 	}
 
 	public static boolean supported() {
 		return trees != null;
 	}
 
 	public static ConvertSourcePosition convert(Object o) {
 		if (Debug.debug)
 			System.out.println("in ConvertSourcePosition.convert: " + o.toString());
 		if (trees == null || sourcePositions == null)
 			return emptySourcePosition;
 		// todo: is this possible for AnnotationMirror or AnnotationValue? not sure if right at all
 		if (o instanceof AnnotationValue) {
 			if (Debug.debug)
 				System.err.println("handling AnnotationValue!");
 			AnnotationValue annotationValue = (AnnotationValue) o;
 			Object value = annotationValue.getValue();
 			if (value instanceof AnnotationMirror || value instanceof Element)
 				o = value; // will be handled by the later if statements
 			else if (value instanceof TypeMirror)
 				o = types.asElement((TypeMirror) value);  // will be handled later
 		}
 		if (o instanceof AnnotationMirror) {
 			if (Debug.debug)
 				System.err.println("handling AnnotationMirror!");
 			AnnotationMirror annotationMirror = (AnnotationMirror) o;
 			o = annotationMirror.getAnnotationType().asElement();  // will be handled later
 		}
 		if (o instanceof Element) {
 			if (Debug.debug)
 				System.err.println("handling Element!");
 			Element element = (Element) o;
 			TreePath tp = trees.getPath(element);
 			if (tp == null)
 				return emptySourcePosition;
 			CompilationUnitTree compUnit = tp.getCompilationUnit();
 			if (compUnit == null)
 				return emptySourcePosition;
 			JavaFileObject sourceFile = compUnit.getSourceFile();
 			if (sourceFile == null)
 				return emptySourcePosition;
			File file = new File(sourceFile.toUri());
 			ExpressionTree pkgName = compUnit.getPackageName();
 			if (callback != null && pkgName != null)
 				callback.deriveSourcePath(file, pkgName.toString());
 			//System.out.println("file: "+file);
 			//System.out.println("package: "+compUnit.getPackageName());
 			//System.exit(1);
 			int line = 0, column = 0;
 			long startPos = sourcePositions.getStartPosition(compUnit, tp.getLeaf());
 			LineMap lineMap = compUnit.getLineMap();
 			if (lineMap != null) {
 				line = (int) lineMap.getLineNumber(startPos);
 				column = (int) lineMap.getColumnNumber(startPos);
 			}
 
 			if (Debug.debug) {
 				System.err.println("element: " + element);
 				System.err.println("element.kind: " + element.getKind());
 				System.err.println("element.getEnclosingElement: " + element.getEnclosingElement());
 				System.err.println("java file: " + file);
 				System.err.println("startPos: " + startPos);
 			}
 
 			return new ConvertSourcePosition(file, line, column);
 		}
 		return emptySourcePosition;
 	}
 
 	@Override
 	public File file() {
 		Debug.implemented("File");
 		return file;
 	}
 
 	@Override
 	public int line() {
 		Debug.implemented("int");
 		return line;
 	}
 
 	@Override
 	public int column() {
 		Debug.implemented("int");
 		return column;
 	}
 
 	@Override
 	public String toString() {
 		return "ConvertSourcePosition{" +
 				"file=" + file +
 				", line=" + line +
 				", column=" + column +
 				'}';
 	}
 }
