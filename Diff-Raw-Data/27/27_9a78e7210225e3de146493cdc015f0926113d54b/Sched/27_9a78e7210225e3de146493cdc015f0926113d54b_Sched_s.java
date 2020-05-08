 /*
  *  Sched - The brain damaged scheduler
  *  Copyright (C) 2012  Bart Kuivenhoven
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 public class Sched
 {
 	public static void main(String args[])
 	{
 		new Sched();
 	}
 	
 	public Sched()
 	{
		Scheduler s = new RandomScheduler(16);
		s.threadAdd(new Test('a', 0, 256));
		s.threadAdd(new Test('b', 1, 256));
		s.threadAdd(new Test('c', 3, 256));
 		System.err.println("Starting!");
 		s.start();
 		System.exit(0);
 	}
 }
