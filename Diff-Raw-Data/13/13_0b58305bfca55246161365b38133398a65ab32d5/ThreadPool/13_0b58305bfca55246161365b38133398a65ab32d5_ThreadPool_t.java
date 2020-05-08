 /*
  *        Copyright (c) 2013 Andrew Fontaine, James Finlay, Jesse Tucker, Jacob Viau, and
  *         Evan DeGraff
  *
  *         Permission is hereby granted, free of charge, to any person obtaining a copy of
  *         this software and associated documentation files (the "Software"), to deal in
  *         the Software without restriction, including without limitation the rights to
  *         use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  *         the Software, and to permit persons to whom the Software is furnished to do so,
  *         subject to the following conditions:
  *
  *         The above copyright notice and this permission notice shall be included in all
  *         copies or substantial portions of the Software.
  *
  *         THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  *         IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
  *         FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
  *         COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
  *         IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  *         CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 package ca.cmput301f13t03.adventure_datetime.model;
 
 import android.util.Log;
 
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
/**
 * ThreadPool for asynchronous stuff
 * @author evandegraff
 *
 */
 public class ThreadPool {
 	private static ThreadPoolExecutor threadPool;
 	private final BlockingQueue<Runnable> workQueue;
 
 	public ThreadPool() {
 		int numberCores = Runtime.getRuntime().availableProcessors();
 		Log.v("ThreadPool", "numberThreads: " + numberCores);
 		workQueue = new LinkedBlockingQueue<Runnable>();
 		threadPool = new ThreadPoolExecutor(numberCores, numberCores, 1,
 				TimeUnit.SECONDS, workQueue);
 	}
 	
 	public void execute(Runnable runnable) {
 		threadPool.execute(runnable);
 	}
 }
