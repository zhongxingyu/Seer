 /*
 * Copyright (C) 2013 Mohatu.net
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
 
 package net.mohatu.bloocoin.miner;
 
 public class KhsClass implements Runnable {
 
 	private int hour=0, minute=0, second=0;
 	@Override
 	public void run() {
 		boolean running = true;
 		while (running) {
 			long oldAmount, newAmount;
 			boolean sawException = false;
 			try {
 				oldAmount = MainView.getCounter();
 				Thread.sleep(1000);
 				newAmount = MainView.getCounter();
 				MainView.updateKhs((double)(newAmount - oldAmount) / 1000);
 				convertTime(System.nanoTime() - MainView.getStartTime());
 				MainView.setTime(hour, minute, second);
 				if(!MainView.getStatus()){
 					running = false;
 				}
 			} catch (InterruptedException e) {
 				sawException = true;
 			}
 			if (sawException)
 				Thread.currentThread().interrupt();
 		}
 		MainView.updateKhs(0);
 	}
 	
 	private void convertTime(long nanos){
 		int seconds = (int) (nanos / 1000000000);
 		hour = (seconds/3600);
		minute = (seconds/60)-(hour*3600);
 		second = seconds - (minute*60) - (hour*3600);
 	}
 }
