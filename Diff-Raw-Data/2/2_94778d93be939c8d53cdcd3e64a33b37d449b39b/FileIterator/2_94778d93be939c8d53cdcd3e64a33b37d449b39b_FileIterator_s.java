 package edu.berkeley.nlp.chess.util;
 import java.io.File;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 import java.util.Stack;
 
 public class FileIterator implements Iterable<File> {
 	Stack<String> files = new Stack<String>();
 
 	public FileIterator(String f) {
 		files.push(f);
 	}
 
 	@Override
 	public Iterator<File> iterator() {
 		return new Iterator<File>() {
 			public boolean hasNext() {
 				return !files.isEmpty();
 			}
 
 			public void remove() {
 			}
 
 			public File next() {
				while (hasNext()) {
 					File f = new File(files.pop());
 
 					if (f.isDirectory()) {
 						if (f.isDirectory()) {
 							for (String file : f.list()) {
 								files.add(f.getPath() + "/" + file);
 							}
 						}
 					} else {
 						return f;
 					}
 				}
 
 				throw new NoSuchElementException("No file");
 			}
 		};
 	}
 	public static void main(String[] args) {
 		for (File f : new FileIterator("/home/aa/ugrad/jinghao/chess/Chess-Commentator/src")) {
 			System.out.println(f.getPath());
 		}
 	}
 }
