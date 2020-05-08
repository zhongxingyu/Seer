 package edu.kit.iti.algo2.textindexing.main;
 
 import java.io.File;
 import java.io.IOException;
 
 import edu.kit.iti.algo2.textindexing.alexdomge.indexer.DocumentRepository;
 import edu.kit.iti.algo2.textindexing.gui.MainWindow;
 
 public class MainWindowStart {
 
     public static void main(String[] args) throws IOException {
	File index = new File("test-index.dump.gzip");
	File repo = new File("test-repo");
//	File index = new File("Indexer/built_indexes/audio-index.xml.gzip");
//	File repo = new File("Indexer/audio-repo");
 //    File index = new File("Indexer/documents/omegatau-6.trscrpt.xml");
 	DocumentRepository.init(repo);
 	new MainWindow(index).setVisible(true);
     }
 }
