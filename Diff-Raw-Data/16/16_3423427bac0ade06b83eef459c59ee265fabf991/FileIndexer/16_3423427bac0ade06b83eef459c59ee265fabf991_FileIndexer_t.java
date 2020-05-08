 package com.isu.module.luceneindexer;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.index.IndexWriter;
 
 import com.google.common.collect.ImmutableSet;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.isu.module.directoryspider.FileHandler;
 
 @Singleton
 public class FileIndexer implements FileHandler 
 {
 	private final long MAX_FILE_SIZE_TO_INDEX = (long) (0.5 * 1024 * 1024);
 	private final ImmutableSet<String> EXCLUDE_DIRECTORIES = ImmutableSet.of(".svn", ".git");
 	private final ImmutableSet<String> EXCLUDE_FILES = ImmutableSet.of("contacts.txt");
 	
 	private IndexWriter indexWriter;
 	
 	@Inject
 	FileIndexer(IndexWriterProvider indexWriterProvider) {
 		this.indexWriter = indexWriterProvider.get();
 	}
 	
 	@Override
 	public Direction process(File file) 
 	{
 		if (file.isDirectory()) {
 			// Exclude .svn and .git folders
 			if (EXCLUDE_DIRECTORIES.contains(file.getName())) 
 			{
 				System.out.println("Returning do not traverse");
 				return Direction.DO_NOT_TRAVERSE;
 			}
 			else
 			{
 				return Direction.NORMAL;
 			}
 		}

		// If file is zero length then ignore
		if (file.length() == 0) {
			System.out.println("File [" + file.getName() + "] is zero size.");
			return Direction.NORMAL;
		}
 		
 		// Files to exclude
 		if (EXCLUDE_FILES.contains(file.getName())) {
 			return Direction.NORMAL;
 		}
 		
 		// File should be readable
 		if (!file.canRead()) {
 			System.out.println("File [" + file.getName() + "]is not readable, ignoring.");
 			return Direction.NORMAL;
 		}
 		
 		// File should should be less than MAX_FILE_SIZE_TO_INDEX in bytes
 		if (file.length() > MAX_FILE_SIZE_TO_INDEX) {
 			System.out.println("File [" + file.getName() + "] exceed maximum size, ignoring.");
 			return Direction.NORMAL;
 		}

 			
 		// Now read the text inside it
 		indexFile(file);
 		
 		return Direction.NORMAL;
 	}
 	
 	private void indexFile(File file) {
 		System.out.println("Indexing document: [" + file.getAbsolutePath() + "]");
 		Document doc = new Document();
 		try 
 		{
 			doc.add(new Field("contents", new FileReader(file)));
 			doc.add(new Field("filename", file.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
 			doc.add(new Field("fullpath", file.getCanonicalPath(), Field.Store.YES, Field.Index.NOT_ANALYZED));
 			indexWriter.addDocument(doc);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
