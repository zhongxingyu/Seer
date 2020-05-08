 package name.kazennikov.fb2;
 
 import gnu.trove.map.hash.TObjectIntHashMap;
 import gnu.trove.procedure.TObjectIntProcedure;
import name.kazennikov.annotations.BaseTokenType;
import name.kazennikov.annotations.*;
 
import java.io.*;
 import java.util.Enumeration;
 import java.util.HashSet;
import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 
 public class FBReader {
 
     public static class SimpleJsoupProcessor extends AbstractJSoupProcessor {
         int totCount = 0;
         int rusCount = 0;
         long chars = 0;
         int tokens = 0;
         long tokenChars = 0;
         int sents = 0;
         TObjectIntHashMap<String> words = new TObjectIntHashMap<String>();
 
         PrintWriter pwSent;
 
         public SimpleJsoupProcessor() throws IOException {
             pwSent = new PrintWriter("sents.txt");
         }
 
         public void close() throws IOException {
             pwSent.close();
         }
 
        public void add(String word) {
             for(int i = 0; i != word.length(); i++) {
                 if(Character.isDigit(word.charAt(i)))
                     return;
             }
 
             words.adjustOrPutValue(new String(word.toLowerCase()), 1, 1);
         }
 
         SentenceSplitter ss = new SentenceSplitter(new HashSet<String>(), false);
         UnicodeTokenizer tokenizer = new UnicodeTokenizer();
 
         @Override
         public void process(String text) {
             chars += text.length();
             rusCount++;
             Document doc = new Document(text);
             tokenizer.annotate(doc);
             ss.annotate(doc);
             //List<AbstractToken> tokens = SimpleTokenizer.tokenize(text);
 
             //List<AbstractToken> sents = ss.split(new TokenStream(tokens));
             this.sents += doc.get(Annotation.SENT).size();
 
             for(Annotation sent : doc.get(Annotation.SENT)) {
                 pwSent.println(sent.getText());
             }
 
             for(Annotation t : doc.get(Annotation.TOKEN)) {
             	BaseTokenType type = t.getFeature(Annotation.TYPE);
                 if(type.is(BaseTokenType.SPACE) || type.is(BaseTokenType.NEWLINE) || type.is(BaseTokenType.PUNC))
                     continue;
 
                 this.tokens++;
                 tokenChars += t.getText().length();
                 add(t.getText());
 
 
             }
         }
 
 
     }
 
     /**
      * Process FB2 file for zip archive with
      */
 	public static interface Processor {
         /**
          * Process single fb2 file in binary stream
          * @param id file id
          * @param is file stream
          * @throws IOException
          */
 		public void process(String id, InputStream is) throws IOException;
 	}
 
     public void process(File f, Processor p) throws IOException {
 		if(f.getName().endsWith(".zip")) {
 
 			ZipFile zip = new ZipFile(f);
 			try {
 				for(Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
 					ZipEntry zipEntry = e.nextElement();
 					System.out.printf("processing %s%n", zipEntry.getName());
 					p.process(zipEntry.getName(), zip.getInputStream(zipEntry));
 				}
 				zip.close();
 			} finally {
 				zip.close();
 			}
 		} else {
 			FileInputStream fis = new FileInputStream(f);
 			try {
 				p.process(f.getName(), fis);
 			} finally {
 				fis.close();
 			}
 			
 		}
 		
 	}
 	
 	
 	public static void main(String[] args) throws IOException {
 		
 		FBReader fbr = new FBReader();
 		SimpleJsoupProcessor p = new SimpleJsoupProcessor();
 		
 		long st = System.currentTimeMillis();
 		
 		//File fb2Path = new File("/media/f978cba3-c1ac-42e8-9daa-f34ee4b3ea55/torrents/_LIB.RUS.EC/lib.rus.ec");
 		File fb2Path = new File(".");
 		int count = 0;
 		
 		for(File f : fb2Path.listFiles()) {
 			if(!f.getName().endsWith(".zip"))
 				continue;
 			System.err.printf("Processing %s%n", f);
 			fbr.process(f, p);
 			
 			count++;
 			
 			if(count == 50)
 				break;
 		}
 		
 //		fbr.process(new File("test.fb2"), p);
 		
 		System.out.printf("Elapsed: %d ms%n", System.currentTimeMillis() - st);
 		
 		System.out.printf("Russian: %d/%d%n", p.rusCount, p.totCount);
 		System.out.printf("Characters: %d%n", p.chars);
 		System.out.printf("Tokens: %d%n", p.tokens);
 		System.out.printf("Tokens chars: %d%n", p.tokenChars);
 		System.out.printf("Sents: %d%n", p.sents);
 		System.out.printf("Distinct tokens: %d%n", p.words.size());
 		
 		final PrintWriter pw = new PrintWriter("words.tsv");
 		
 		p.words.forEachEntry(new TObjectIntProcedure<String>() {
 
 			@Override
 			public boolean execute(String a, int b) {
 				pw.printf("%s\t%d%n", a, b);
 				return true;
 			}
 		});
 		
 		pw.close();
 		p.close();
 		
 	}
 
 }
