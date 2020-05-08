 package jp.ac.osaka_u.ist.sdl.mpanalyzer;
 
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Modification;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Revision;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.db.ReadOnlyDAO;
 
 public class MExtractor {
 
 	public static void main(String[] args) {
 
 		try {
 
 			final int THREADS = Config.getThreadsValue();
 
 			final long startTime = System.nanoTime();
 
 			final Revision[] revisions = ReadOnlyDAO.getInstance()
 					.getRevisions().toArray(new Revision[0]);
 
 			if (0 == revisions.length) {
 				System.out.println("no revision.");
 				System.exit(0);
 			}
 
			final AtomicInteger index = new AtomicInteger(1);
 			final BlockingQueue<Modification> queue = new ArrayBlockingQueue<Modification>(
 					100000, true);
 			final ModificationWritingThread writingThread = new ModificationWritingThread(
 					queue);
 			writingThread.start();
 			final ModificationExtractionThread[] extractingThreads = new ModificationExtractionThread[THREADS];
 			for (int i = 0; i < extractingThreads.length; i++) {
 				extractingThreads[i] = new ModificationExtractionThread(i,
 						revisions, index, queue);
 				extractingThreads[i].start();
 			}
 
 			for (final ModificationExtractionThread thread : extractingThreads) {
 				thread.join();
 			}
 
 			writingThread.finish();
 			writingThread.join();
 
 			// 実行時間を表示
 			final long endTime = System.nanoTime();
 			System.out.print("execution time: ");
 			System.out.println(TimingUtility.getExecutionTime(startTime,
 					endTime));
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
