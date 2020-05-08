 package xmldumptools;
 
 import info.bliki.api.*;
 import info.bliki.api.creator.*;
 
 import info.bliki.wiki.dump.IArticleFilter;
 import info.bliki.wiki.dump.Siteinfo;
 import info.bliki.wiki.dump.WikiArticle;
 import info.bliki.wiki.dump.WikiXMLParser;
 import info.bliki.wiki.filter.Encoder;
 import info.bliki.wiki.filter.PlainTextConverter;
 import info.bliki.wiki.impl.DumpWikiModel;
 
 import java.io.IOException;
 
 import org.xml.sax.SAXException;
 
 /**
  * Extract plain text from a given Mediawiki dump and count the words
  * and nr of articles a word occurs in.
  */
 public class DumpWordStatisticsCreator {
 
 	public DumpWordStatisticsCreator() {
 		super();
 	}
 
 	static class DemoArticleFilter implements IArticleFilter {
 		WikiDB wikiDB;
 		int counter;
 		private final String htmlDirectory;
 		private final String imageDirectory;
 
 		public DemoArticleFilter(WikiDB db, String htmlDirectory, String imageDirectory) {
 			this.counter = 0;
 			this.wikiDB = db;
 			if (htmlDirectory.charAt(htmlDirectory.length() - 1) != '/') {
 				htmlDirectory = htmlDirectory + "/";
 			}
 			this.htmlDirectory = htmlDirectory;
 			this.imageDirectory = imageDirectory;
 		}
 
 		public void process(WikiArticle page, Siteinfo siteinfo) throws SAXException {
 			if (page.isMain() || page.isCategory() || page.isProject()) {
 				String title = page.getTitle();
 				String titleURL = Encoder.encodeTitleLocalUrl(title);
 				String generatedTXTFilename = htmlDirectory + titleURL + ".txt";
 				DumpWikiModel wikiModel = new DumpWikiModel(wikiDB, siteinfo, "${image}", "${title}", imageDirectory);
 
                                 // We use our own implementation of of the creator
                                 // to initiate the count process
                                 DocumentStatisticsCreator creator = new DocumentStatisticsCreator(wikiModel, page);
 
                                 // creator.setHeader(HTMLConstants.HTML_HEADER1 + HTMLConstants.CSS_SCREEN_STYLE + HTMLConstants.HTML_HEADER2);
 				// creator.setFooter(HTMLConstants.HTML_FOOTER);
 
                                 wikiModel.setUp();
 
                                 try {
 					creator.renderToFile( null, generatedTXTFilename);
 					System.out.println("(" + counter + ")\t" + generatedTXTFilename );
                                         counter++;
 
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (Exception e1) {
 					e1.printStackTrace();
 				}
 			}
 		}
 	}
 
 	static class DemoTemplateArticleFilter implements IArticleFilter {
 
                 WikiDB wikiDB;
 		int counter;
 
 		public DemoTemplateArticleFilter(WikiDB wikiDB) {
 			this.wikiDB = wikiDB;
 			this.counter = 0;
 		}
 
 		public void process(WikiArticle page, Siteinfo siteinfo) throws SAXException {
 			if (page.isTemplate()) {
 				// System.out.println(page.getTitle());
 				TopicData topicData = new TopicData(page.getTitle(), page.getText());
 				try {
 					wikiDB.insertTopic(topicData);
 					System.out.print('.');
 					if (++counter >= 80) {
 						System.out.println(' ');
 						counter = 0;
 					}
 				} catch (Exception e) {
 					String mess = e.getMessage();
 					if (mess == null) {
 						throw new SAXException(e.getClass().getName());
 					}
 					throw new SAXException(mess);
 				}
 			}
 		}
 	}
 
 	public static WikiDB prepareDB(String mainDirectory) {
 		// the following subdirectory should not exist if you would like to create a
 		// new database
 		if (mainDirectory.charAt(mainDirectory.length() - 1) != '/') {
 			mainDirectory = mainDirectory + "/";
 		}
 		String databaseSubdirectory = "WikiDumpDB";
 
 		WikiDB db = null;
 
 		try {
 			db = new WikiDB(mainDirectory, databaseSubdirectory);
 			return db;
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		} finally {
 			// if (db != null) {
 			// try {
 			// db.tearDown();
 			// } catch (Exception e) {
 			// e.printStackTrace();
 			// }
 			// }
 		}
 		return null;
 	}
 
 	public static void main(String[] args) throws IOException {
 
                 Results.init();
 
 		boolean skipFirstPass = false;
 		if (args.length < 2) {
 			System.err.println("Usage: DumpWordStatisticsCreator <XML-FILE> <WORK-folder> [<SKIP-FIRST_PASS>=true|yes]");
 			System.exit(-1);
 		}
 		if (args.length > 2) {
 			String arg2 = args[2].toLowerCase();
 			if (arg2.equals("true") || arg2.equals("yes")) {
 				skipFirstPass = true;
 				System.out.println("Option <skip first pass> is set to true");
 			}
 		}
 		// String bz2Filename =
 		// "c:\\temp\\dewikiversity-20100401-pages-articles.xml.bz2";
 		String bz2Filename = args[0];
 
 		WikiDB db = null;
 
 		try {
 			String mainDirectory = args[1];
 			String htmlDirectory = args[1] + "/dump/";
 
 			// the following directory must exist for image references
 			String imageDirectory = args[1] + "/dump/WikiDumpImages";
 			System.out.println("Prepare wiki database");
 			db = prepareDB(mainDirectory);
 			IArticleFilter handler;
 			WikiXMLParser wxp;
 			if (!skipFirstPass) {
 				System.out.println("First pass - write templates to database:");
 				handler = new DemoTemplateArticleFilter(db);
 				wxp = new WikiXMLParser(bz2Filename, handler);
 				wxp.parse();
 				System.out.println(' ');
 			}

                         Results.run = 1;
                         System.out.println("Second pass - count words in plain text files\n" +
                                 "and write statistics file to folder:");
 			handler = new DemoArticleFilter(db, htmlDirectory, imageDirectory);
 			wxp = new WikiXMLParser(bz2Filename, handler);
 			wxp.parse();
 			System.out.println(' ');
 
                         Results.nextRun();
       			System.out.println("Third pass - to count all words ever in articles ... ");
 			handler = new DemoArticleFilter(db, htmlDirectory, imageDirectory);
 			wxp = new WikiXMLParser(bz2Filename, handler);
 			wxp.parse();
 			System.out.println(' ');
 
 			System.out.println("Done!");
 
                 }
                 catch (Exception e) {
 			e.printStackTrace();
 		}
                 finally {
 			if (db != null) {
 				try {
                                     // Write all cached data to disc ...
                                     Results.close();
                         		db.tearDown();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 }
