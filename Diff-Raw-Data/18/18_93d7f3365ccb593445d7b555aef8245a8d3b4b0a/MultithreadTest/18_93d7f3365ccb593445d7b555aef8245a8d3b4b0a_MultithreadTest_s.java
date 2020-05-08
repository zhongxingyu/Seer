 package dfh.grammar;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class MultithreadTest {
 
 	private static final int MULTIPLIER = 100;
 	private static Grammar g;
 	private static String[] testPhrases = {
 			//
 			"asdfadsfa foo    bar asdidlasd",//
 			"aicavmcax quux\nbazdsaiidk",//
 			"cvcfoo  \tbar asdi,ci,ci,kci",//
 			"asdfadikci,kcquux    baz" };
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		String[] rules = {
 				//
 				"<ROOT> = <a> | <b>", //
 				"<a> = <foo> <s> <bar>",//
 				"<b> = <quux> <s> <baz>",//
 				"<s> = /\\s++/",//
 				"<foo> = /foo/",//
 				"<bar> = /bar/",//
 				"<quux> = /quux/",//
 				"<baz> = /baz/",//
 		};
 		g = new Grammar(rules);
 	}
 
 	@Test
 	public void aTest() {
 		final LinkedList<String> phraseList = new LinkedList<String>();
 		for (int i = 0; i < MULTIPLIER; i++) {
 			for (String phrase : testPhrases)
 				phraseList.add(phrase);
 		}
 		final int lim = MULTIPLIER * testPhrases.length;
 		Collections.shuffle(phraseList);
 		final AtomicInteger count = new AtomicInteger(0), matchCount = new AtomicInteger(
 				0);
 		for (int i = 0; i < MULTIPLIER; i++) {
 			new Thread(new Runnable() {
 
 				@Override
 				public void run() {
 					while (count.get() < lim) {
 						String phrase;
 						synchronized (phraseList) {
 							while (phraseList.isEmpty()) {
 								try {
 									phraseList.wait();
 								} catch (InterruptedException e) {
 									e.printStackTrace();
 								}
 							}
 							phrase = phraseList.removeFirst();
 						}
 						Matcher m = g.find(phrase);
 						while (m.match() != null)
 							matchCount.incrementAndGet();
 						synchronized (phraseList) {
 							phraseList.addLast(phrase);
 							phraseList.notify();
 						}
 						count.incrementAndGet();
 						synchronized (count) {
 							count.notify();
 						}
 					}
 				}
 			}).start();
 		}
 		synchronized (count) {
 			while (count.get() < lim) {
 				try {
 					count.wait();
 				} catch (InterruptedException e) {
 				}
 			}
 		}
 		assertTrue("got all matches", matchCount.get() == count.get());
 	}
 }
