 package ceri.common.io;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.util.ArrayDeque;
 import java.util.Arrays;
 import java.util.Deque;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 /**
  * Iterates over files under the given root directory.
  */
 public class FileIterator implements Iterator<File> {
 	public final File rootDir;
 	private final FileFilter filter;
 	private final Deque<Iterator<File>> iterators = new ArrayDeque<>(); // Iterator for each dir
 	private File next = null;
 	
 	public FileIterator(File rootDir) {
 		this(rootDir, null);
 	}
 	
 	public FileIterator(File rootDir, FileFilter filter) {
 		this.filter = filter == null ? FileFilters.ALL : filter;
 		this.rootDir = rootDir;
 		File[] files = rootDir.listFiles();
 		if (files == null) files = new File[0];
 		iterators.add(Arrays.asList(files).iterator());
 		next = findNext();
 	}
 
 	@Override
 	public boolean hasNext() {
 		return next != null;
 	}
 
 	@Override
 	public File next() {
 		if (next == null) throw new NoSuchElementException();
 		File file = next;
 		next = findNext();
 		return file;
 	}
 
 	@Override
 	public void remove() {
 		throw new UnsupportedOperationException("File iteration is immutable.");
 	}
 	
 	private File findNext() {
 		// iterator for each level of dir hierarchy
 		while (!iterators.isEmpty()) {
 			Iterator<File> iterator = iterators.getLast();
 			// If no files left in iterator, go to level above
 			if (!iterator.hasNext()) {
 				iterators.removeLast();
 				continue;
 			}
 			File next = iterator.next();
 			// If next is a non-empty directory add iterator down one level
 			Iterator<File> nextIterator = createIterator(next);
 			if (nextIterator != null) iterators.add(nextIterator);
 			// If the file matches the filter, return it
 			if (filter.accept(next)) return next;
 		}
 		return null;
 	}
 
 	private Iterator<File> createIterator(File dir) {
 		if (!dir.isDirectory()) return null;
 		File[] files = dir.listFiles();
		if (files == null || files.length == 0) return null;
 		return Arrays.asList(files).iterator();
 	}
 	
 }
