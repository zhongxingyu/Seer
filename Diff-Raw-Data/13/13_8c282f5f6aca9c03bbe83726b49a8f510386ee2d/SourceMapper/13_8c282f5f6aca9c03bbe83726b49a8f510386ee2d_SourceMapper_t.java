 package org.eclipse.dltk.core.model.binary;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.WeakHashMap;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.dltk.compiler.IElementRequestor.FieldInfo;
 import org.eclipse.dltk.compiler.IElementRequestor.MethodInfo;
 import org.eclipse.dltk.compiler.IElementRequestor.TypeInfo;
 import org.eclipse.dltk.compiler.util.Util;
 import org.eclipse.dltk.core.IField;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ISourceRange;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.internal.core.SourceRange;
 
 /**
  * @since 2.0
  */
 public class SourceMapper {
	protected static final SourceRange UNKNOWN_RANGE = new SourceRange(-1, 0);

 	private static class Range {
 		int start;
 		int end;
 
 		public Range(int start, int end) {
 			super();
 			this.start = start;
 			this.end = end;
 		}
 	}
 
 	private Map<IModelElement, Range> sourceRanges = new HashMap<IModelElement, Range>();
 
 	private Map<IModelElement, Range> nameRanges = new HashMap<IModelElement, Range>();
 
 	private IPath sourcePath;
 
 	// private IPath sourceRoot;
 
 	private Set<IBinaryModule> sourcesNotPressent = new HashSet<IBinaryModule>();
 	private Map<IBinaryModule, String> sourcesMap = new WeakHashMap<IBinaryModule, String>();
 
 	public SourceMapper(IPath sourcePath, IPath sourceAttachmentRootPath) {
 		this.sourcePath = sourcePath;
 		// this.sourceRoot = sourceAttachmentRootPath;
 	}
 
 	private ISourceRange getRange(Range range) {
 		if (range == null) {
			return UNKNOWN_RANGE;
 		}
 		return new SourceRange(range.start, range.end - range.start + 1);
 	}
 
 	public ISourceRange getSourceRange(IModelElement element) {
 		fetchRangesForElement(element);
 		return getRange(sourceRanges.get(element));
 	}
 
 	protected void fetchRangesForElement(IModelElement element) {
 	}
 
 	public ISourceRange getNameRange(IModelElement element) {
 		fetchRangesForElement(element);
 		return getRange(nameRanges.get(element));
 	}
 
 	void reportType(TypeInfo info, IType type) {
 		nameRanges.put(type,
 				new Range(info.nameSourceStart, info.nameSourceEnd));
 		sourceRanges.put(type, new Range(info.declarationStart, 0));
 	}
 
 	void reportField(FieldInfo info, IField field) {
 		nameRanges.put(field, new Range(info.nameSourceStart,
 				info.nameSourceEnd));
 		sourceRanges.put(field, new Range(info.declarationStart, 0));
 	}
 
 	void reportMethod(MethodInfo info, IMethod method) {
 		nameRanges.put(method, new Range(info.nameSourceStart,
 				info.nameSourceEnd));
 		sourceRanges.put(method, new Range(info.declarationStart, 0));
 	}
 
 	public void setRangeEnd(IModelElement element, int declarationEnd) {
 		Range range = sourceRanges.get(element);
 		if (range != null) {
 			range.end = declarationEnd;
 		}
 	}
 
 	public String getSource(IBinaryModule binaryModule, String fileName) {
 		if (sourcesNotPressent.contains(binaryModule)) {
 			return null;
 		}
 		String content = sourcesMap.get(binaryModule);
 		if (content != null) {
 			return content;
 		}
 		String source = findSource(binaryModule, fileName);
 		if (source != null) {
 			sourcesMap.put(binaryModule, source);
 		} else {
 			sourcesNotPressent.add(binaryModule);
 		}
 		return source;
 	}
 
 	protected String findSource(IBinaryModule binaryModule, String filename) {
 		File file = sourcePath.toFile();
 		if (file.isDirectory()) {
 			// Look into directory
 			File result = traversePath(file, filename);
 			if (result != null) {
 				try {
 					return new String(Util.getFileByteContent(result));
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		} else if (isArchive(file)) {
 			return findSourceInArchive(file, filename);
 		}
 
 		return null;
 	}
 
 	protected String findSourceInArchive(File file, String filename) {
 		return null;
 	}
 
 	public boolean isArchive(File file) {
 		return false;
 	}
 
 	private File traversePath(File file, String filename) {
 		File[] listFiles = file.listFiles();
 		for (File child : listFiles) {
 			if (child.isDirectory()) {
 				File result = traversePath(child, filename);
 				if (result != null) {
 					return result;
 				}
 			} else {
 				if (isFileMatch(child, filename)) {
 					return child;
 				}
 			}
 		}
 		return null;
 	}
 
 	protected boolean isFileMatch(File child, String filename) {
 		if (child.getName().equals(filename)) {
 			return true;
 		}
 		return false;
 	}
 }
