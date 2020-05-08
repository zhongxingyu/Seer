 /**
  * Copyright 2013 Semen A Martynov <semen.martynov@gmail.com>
  * 
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 package ru.spbau.martynov.task4;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Random;
 
 /**
  * The implementation of the Observer pattern.
  */
 public class Main {
 
 	/**
 	 * Entry point.
 	 * 
 	 * @param args
 	 *            - Doesn't play a role.
 	 */
 	public static void main(String[] args) {
 		Random random = new Random();
 		TimeEvent timeEvent = new TimeEvent();
 		RandomEvent randomEvent = new RandomEvent();
 
 		for (int i = 1; i != 6; ++i) {
 			// Adding subscribers who report their numbers.
 			if (random.nextInt() % 2 != 0) {
 				timeEvent.addListener(getTimeEventActionListener(i));
 			} else {
 				randomEvent.addListener(getRandomEventActionListener(i));
 			}
 			// Adding Subscribers who report their creation time.
 			if (random.nextInt() % 2 != 0) {
 				timeEvent.addListener(getTimeBasedActionListener());
 			} else {
 				randomEvent.addListener(getTimeBasedActionListener());
 			}
 			// Pause that creation time would differ.
 			try {
 				Thread.sleep(1000L);
 			} catch (InterruptedException e) {
				e.printStackTrace(System.err);
 			}
 		}
 		// Infinite loop.
 		while (true) {
 			timeEvent.fireEvent();
 			randomEvent.fireEvent();
 		}
 
 	}
 
 	/**
 	 * Function returns unnamed object with number, for a subscription to
 	 * TimeEvent.
 	 * 
 	 * @param i
 	 *            Number of the subscriber.
 	 * @return Unnamed TimeEvent subscriber with number.
 	 */
 	private static ActionListener getTimeEventActionListener(final int i) {
 		return new ActionListener() {
 			{
 				id = i;
 			}
 
 			public void performAction() {
 				System.out.println("TimeEvent - подписчик № " + id);
 			}
 
 			private int id;
 		};
 	}
 
 	/**
 	 * Function returns unnamed object with number, for a subscription to
 	 * RandomEvent.
 	 * 
 	 * @param i
 	 *            Number of the subscriber.
 	 * @return Unnamed RandomEvent subscriber with number.
 	 */
 	private static ActionListener getRandomEventActionListener(final int i) {
 		return new ActionListener() {
 			{
 				id = i;
 			}
 
 			public void performAction() {
 				System.out.println("RandomEvent - подписчик № " + id);
 			}
 
 			private int id;
 		};
 	}
 
 	/**
 	 * Function returns unnamed object with can report their creation time.
 	 * 
 	 * @return Unnamed ActionListener object.
 	 */
 	private static ActionListener getTimeBasedActionListener() {
 		return new ActionListener() {
 			{
 				time = Calendar.getInstance().getTime();
 			}
 
 			public void performAction() {
 				System.out.println("TimeBased - подписчик создан: " + time);
 			}
 
 			private Date time;
 		};
 	}
 }
