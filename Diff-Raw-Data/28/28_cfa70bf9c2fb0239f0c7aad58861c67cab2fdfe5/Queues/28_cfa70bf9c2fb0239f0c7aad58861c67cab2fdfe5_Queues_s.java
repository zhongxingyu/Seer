 // Copyright 2009 Warsaw University, Faculty of Physics
 //
 // This program is free software: you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 package multiplexer.jmx.util;
 
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 
 /**
  * @author Piotr Findeisen
  */
 public class Queues {
 	private Queues() {
 	}
 
	public static <E> E pollUninterruptibly(BlockingQueue<E> queue, TimeoutCounter timer) {
		while (true) {
			long remainingMillis = timer.getRemainingMillis();
			if (remainingMillis <= 0)
				break;
			try {
				return queue.poll(remainingMillis, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// no-op
 			}
 		}
		return null;
 	}
 
 	public static <E> E pollUninterruptibly(BlockingQueue<E> queue, long timeoutMillis) {
 		return pollUninterruptibly(queue, new TimeoutCounter(timeoutMillis));
 	}
 }
