 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.PriorityQueue;
 
 public class ScheduleClientConnection implements Runnable {
 
 	private Socket clientSocket;
 	private String line;
 	BufferedReader in;
 	PrintStream out;
 
 	private HashMap<CurrentDate, Day> calStorage;
 
 	ScheduleClientConnection(Socket server, HashMap<CurrentDate, Day> calStorage) {
 		clientSocket = server;
 		this.calStorage = calStorage;
 	}
 
 	/*
 	 * The server takes 4 ints separated by spaces this is somewhat sanitized,
 	 * but don't test it
 	 * 
 	 * the format for input is month day year time
 	 * 
 	 * year and time must be four digits, the time must be in 24 hr format
 	 * 
 	 * it returns two lines, the first line is the current period given the time
 	 * 
 	 * the second line is the next period
 	 * 
 	 * these lines each give the period number and then the start and end times
 	 * separated by spaces
 	 * 
 	 * ex 10 11 2012 1051 <-- input A 4 1050 1205 <-output 6 1305 1405
 	 * 
 	 * key to output anything >0 is the period number -1 is break -2 assembly -3
 	 * class meeting -4 advisory -5 clubs -7 lunch
 	 */
 
 	@Override
 	public void run() {
 
 		try {
 			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
 			out = new PrintStream(clientSocket.getOutputStream());
 			while ((line = in.readLine()) != null && !line.equals(".")) {
 				String[] temp = line.split(" ");
 				if (temp.length != 4) {
 					out.println("Please enter the time and date in the following format: month day year time");
 					// out.println(".");
 				} else {
 					CurrentDate today = new CurrentDate(Integer.parseInt(temp[0]), Integer.parseInt(temp[1]),
 							Integer.parseInt(temp[2]));
 					Day thing = null;
 					for (Entry<CurrentDate, Day> c : calStorage.entrySet()) {
 						if (c.getKey().equals(today)) {
 							thing = c.getValue();
 						}
 
 					}
 					if (thing != null) {
 						if (thing.getD().size() <= 7) {
 							thing = buildDay(today);
 						}
 						int currentTime = Integer.parseInt(temp[3]);
 						currentTime = adjustTime(currentTime, today, 1);
 						// System.out.println(currentTime);
 						// System.out.println(thing);
 						String stuff = "";
 						if (thing != null) {
 							Period tp;
 							if (currentTime < adjustTime(800, today, 1)) {
 								stuff += thing.getDayType() + "--";
 								stuff += "-9--";
 								tp = thing.getD().peek();
 								stuff += tp.getNumber() + "--" + adjustTime(tp.getStartTime(), today, -1) + "--"
 										+ adjustTime(tp.getEndTime(), today, -1);
 							} else {
 								if ((tp = thing.currentPeriod(currentTime)) != null) {
 									stuff += thing.getDayType() + "--";
 									stuff += tp.getNumber() + "--" + adjustTime(tp.getStartTime(), today, -1) + "--"
 											+ adjustTime(tp.getEndTime(), today, -1) + "--";
 								} else {
 									stuff += "-8--";
 								}
 								if ((tp = thing.nextPeriod(currentTime)) != null) {
 									stuff += tp.getNumber() + "--" + adjustTime(tp.getStartTime(), today, -1) + "--"
 											+ adjustTime(tp.getEndTime(), today, -1);
								} else if ((tp = thing.nextPeriod(currentTime - 10)) != null) {
 									stuff += tp.getNumber() + "--" + adjustTime(tp.getStartTime(), today, -1) + "--"
 											+ adjustTime(tp.getEndTime(), today, -1);
 
 								} else {
 									stuff += "-8";
 								}
 							}
 							out.println(stuff);
 							// out.println(".");
 						}
 					} else {
 						out.println('X');
 					}
 				}
 
 			}
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 
 	}
 
 	public int adjustTime(int time, CurrentDate today, int direction) {
 		int currentTime = time;
 		if (today.isBefore(new CurrentDate(11, 4, 2012)) || today.isAfterOrEqual(new CurrentDate(3, 10, 2013))) {
 			currentTime += 600 * direction;
 		} else {
 			currentTime += 700 * direction;
 		}
 		return currentTime;
 
 	}
 
 	public Day buildDay(CurrentDate today) {
 		Period tp = new Period();
 		Period tp2 = new Period();
 		Period lunch = new Period();
 		Day temp = null;
 		for (Entry<CurrentDate, Day> c : calStorage.entrySet()) {
 			if (c.getKey().equals(today)) {
 				temp = c.getValue();
 			}
 
 		}
 		if (temp != null) {
 			int firstPeriod = temp.getD().peek().getNumber();
 			PriorityQueue<Period> tempQueue = new PriorityQueue<Period>();
 			for (Period p : temp.getD()) {
 				tempQueue.offer(p);
 			}
 			if (temp.getD().peek().getStartTime() == 900) {
 				lunch.setStartTime(adjustTime(1230, today, 1));
 				lunch.setEndTime(adjustTime(1330, today, 1));
 				lunch.setNumber(-7);
 				temp.add(lunch);
 				return temp;
 			}
 			int breakStart;
 			switch (firstPeriod) {
 				case 1:
 					temp.setDayType('A');
 					tempQueue.poll();
 					breakStart = tempQueue.poll().getEndTime();
 					tp.setStartTime(breakStart);
 					tp.setEndTime(breakStart + 20);
 					tp.setNumber(-1);
 					temp.add(tp);
 					tp2.setStartTime(breakStart + 20);
 					tp2.setEndTime(breakStart + 40);
 					tp2.setNumber(-2);
 					temp.add(tp2);
 					lunch.setStartTime(adjustTime(1205, today, 1));
 					lunch.setEndTime(adjustTime(1305, today, 1));
 					lunch.setNumber(-7);
 					temp.add(lunch);
 					break;
 
 				case 2:
 					temp.setDayType('B');
 					tempQueue.poll();
 					breakStart = tempQueue.poll().getEndTime();
 					tp.setStartTime(breakStart);
 					tp.setEndTime(breakStart + 20);
 					tp.setNumber(-1);
 					temp.add(tp);
 					breakStart = tempQueue.poll().getEndTime();
 					tp2.setStartTime(breakStart + 5);
 					tp2.setEndTime(breakStart + 65);
 					tp2.setNumber(-5);
 					temp.add(tp2);
 					lunch.setStartTime(adjustTime(1205, today, 1));
 					lunch.setEndTime(adjustTime(1305, today, 1));
 					lunch.setNumber(-7);
 					temp.add(lunch);
 					break;
 
 				case 3:
 					temp.setDayType('C');
 					tempQueue.poll();
 					breakStart = tempQueue.poll().getEndTime();
 					tp.setStartTime(breakStart);
 					tp.setEndTime(breakStart + 20);
 					tp.setNumber(-1);
 					temp.add(tp);
 					tp2.setStartTime(breakStart + 20);
 					tp2.setEndTime(breakStart + 40);
 					tp2.setNumber(-3);
 					temp.add(tp2);
 					lunch.setStartTime(adjustTime(1205, today, 1));
 					lunch.setEndTime(adjustTime(1305, today, 1));
 					lunch.setNumber(-7);
 					temp.add(lunch);
 					break;
 
 				case 4:
 					temp.setDayType('D');
 					tempQueue.poll();
 					breakStart = tempQueue.poll().getEndTime();
 					tp.setStartTime(breakStart);
 					tp.setEndTime(breakStart + 20);
 					tp.setNumber(-1);
 					temp.add(tp);
 					tp2.setStartTime(breakStart + 20);
 					tp2.setEndTime(breakStart + 40);
 					tp2.setNumber(-4);
 					temp.add(tp2);
 					lunch.setStartTime(adjustTime(1205, today, 1));
 					lunch.setEndTime(adjustTime(1305, today, 1));
 					lunch.setNumber(-7);
 					temp.add(lunch);
 					break;
 				case 5:
 					temp.setDayType('E');
 					tempQueue.poll();
 					breakStart = tempQueue.poll().getEndTime();
 					tp.setStartTime(breakStart);
 					tp.setEndTime(breakStart + 20);
 					tp.setNumber(-1);
 					temp.add(tp);
 					tp2.setStartTime(breakStart + 20);
 					tp2.setEndTime(breakStart + 40);
 					tp2.setNumber(-2);
 					temp.add(tp2);
 					lunch.setStartTime(adjustTime(1205, today, 1));
 					lunch.setEndTime(adjustTime(1305, today, 1));
 					lunch.setNumber(-7);
 					temp.add(lunch);
 					break;
 
 				case 6:
 					temp.setDayType('F');
 					tempQueue.poll();
 					breakStart = tempQueue.poll().getEndTime();
 					tp.setStartTime(breakStart);
 					tp.setEndTime(breakStart + 20);
 					tp.setNumber(-1);
 					temp.add(tp);
 					tp2.setStartTime(breakStart + 20);
 					tp2.setEndTime(breakStart + 40);
 					tp2.setNumber(-4);
 					temp.add(tp2);
 					lunch.setStartTime(adjustTime(1205, today, 1));
 					lunch.setEndTime(adjustTime(1305, today, 1));
 					lunch.setNumber(-7);
 					temp.add(lunch);
 					break;
 
 				case 7:
 					temp.setDayType('G');
 					tempQueue.poll();
 					breakStart = tempQueue.poll().getEndTime();
 					tp.setStartTime(breakStart);
 					tp.setEndTime(breakStart + 20);
 					tp.setNumber(-1);
 					temp.add(tp);
 					breakStart = tempQueue.poll().getEndTime();
 					tp2.setStartTime(breakStart + 5);
 					tp2.setEndTime(breakStart + 65);
 					tp2.setNumber(-5);
 					temp.add(tp2);
 					lunch.setStartTime(adjustTime(1205, today, 1));
 					lunch.setEndTime(adjustTime(1305, today, 1));
 					lunch.setNumber(-7);
 					temp.add(lunch);
 					break;
 				default:
 					break;
 			}
 		}
 		return temp;
 	}
 }
