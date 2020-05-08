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
 public class RoundRobbin extends Scheduler {
 	
 	int no_threads = 0;
 	GreenThread threads = null;
 	
 	public RoundRobbin(int prios)
 	{
 		// We're just gonna leave priorities for what they are
 	}
 
 	private void exec()
 	{
 		GreenThread next = null;
		GreenThread prev = null;
		prev = threads;
 		for (GreenThread t = threads; t != null; t = next)
 		{
 			next = t.next();
 			if (t.killed())
 			{
				if (prev == t)
					threads = t.next();
				else
					prev.next(t.next());

 				no_threads--;
 				continue;
 			
			}
 			t.run();
			prev = t;
 		}
 	}
 	
 	@Override
 	public void start() {
 		while (no_threads != 0)
 			exec();
 	}
 
 	@Override
 	public void threadAdd(GreenThread t) {
 		t.next(threads);
 		threads = t;
 		no_threads++;
 		
 	}
 
 }
