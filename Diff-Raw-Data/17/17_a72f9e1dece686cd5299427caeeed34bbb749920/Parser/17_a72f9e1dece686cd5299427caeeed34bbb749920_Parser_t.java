 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class Parser {
 	
 	private String[] exportable;
 	
 	public Parser(String file) throws IOException{
 		if (file == null) throw new IllegalArgumentException();
 		
 		ArrayList<String> data = new ArrayList<String>();
 		BufferedReader in = new BufferedReader(new FileReader(file));
 		String sentence = in.readLine();
 		
 		/* The first two converter variables and both temp variables are for
 		 * converting latitude and longitude. The third converter variable is
 		 * for converting the timestamp.
 		 */
 		
 		double converter1, converter2, temp1, temp2;
 		int converter3;
 		
		final double speedFactor = 0.006544931;	//The multiplier for converting the hex speed readout to mph. Currently result of linear fit.
 		
 		int code = 0;
		int tempVelocity = 0;
 		double velocity = 0;
 		int wheelAngle = 0;
 		int brakePedal = 0;
 		int gasPedal = 0;
 		int turnSignal = 0;
 		int hazardLights = 0;
 		int lights = 0;
 		
 		int count = 0;
 		int angcnt = 0;
 		int brkcnt = 0;
 		int gascnt = 0;
 		int litcnt = 0;
 		int hazcnt = 0;
 		int velcnt = 0;
 		
 		while(sentence != null){
 			//System.out.println(sentence);
 			if (sentence.contains("$GPGLL")){
 				String[] chunks = sentence.split(",");
 				chunks[5] = chunks[5].replaceAll(".00", "");
 				
 				/*Since the latitude and longitude data comes in degrees and 
 				 * minutes rather than a simple decimal representation of the
 				 * degree value, we have to do some numerical gymnastics below
 				 * to convert the GPS coordinates from the degree/minute form,
 				 * as they're received, to the decimal representation form that
 				 * the Google Maps API requires.
 				 */
 				
 				temp1 = new Double(chunks[1]);
 				temp2 = new Double(chunks[3]);
 				//temp1 and temp2 are the latitude and longitude values,
 				//respectively, in whole degrees
 				temp1 = (int) Math.floor(temp1/100);
 				temp2 = (int) Math.floor(temp2/100);
 				
 				converter1 = new Double(chunks[1]);
 				converter2 = new Double(chunks[3]);
 				
 				//Trimming away the degree values, leaving just the minutes
 				converter1 %= 100;
 				converter2 %= 100;
 				
 				//100 decimals/60 minutes -> multiplication by 5/3
 				//Also shifts values back two decimal places to match desired 
 				//output format.
 				converter1 *= 5;
 				converter2 *= 5;
 				converter1 /= 300;
 				converter2 /= 300;
 				//Recombining degree values
 				converter1 += temp1;
 				converter2 += temp2;
 				converter3 = new Integer(chunks[5]);
 				
 				//Conversions to proper sign by hemisphere
 				if (chunks[2].equals("S")) converter1 *= -1; 
 				if (chunks[4].equals("W")) converter2 *= -1;
 				
 				converter3 += 200000;	//Changes to Eastern Time Zone (4 hours earlier than UTC-1)
 				converter3 %= 240000;	//Returns to military time format
 
 				chunks[1] = Double.toString(converter1);
 				chunks[3] = Double.toString(converter2);
 				chunks[5] = Integer.toString(converter3);				
 				
 				data.add(chunks[1]);
 				data.add(chunks[3]);
 				data.add(chunks[5]);
 			} else if (sentence.contains("m s ")){
 				//Note that all lines come in the form:
 				//m s 0x00000XXX X  0xXX 0xXX ...
 				//     NOTE THE    ^ DOUBLE-SPACE
 				String[] chunks = sentence.split(" ");
 				chunks[2] = chunks[2].replaceAll("x", "");
 				code = Integer.parseInt(chunks[2], 16);
 				switch(code){
 				case 37:	//Steering wheel angle
 					/**The below algorithm steps through several steps to distill a desired integer value from
 					 * its original form of two hex bytes displayed as, for example, "0x01 0x1F".
 					 * 
 					 * First, the leading "0x" is cleaved from the both bytes. Since, though in two's complement,
 					 * each of the steering wheel angle data consists only of twelve bits, an "F" is added to the
 					 * beginning of the first byte if an 'F' or 'E' is detected. This allows the shortValue method
 					 * to treat it as a two-byte number in two's complement, which sets the number in a predictable
 					 * range, specifically -373 to 337. For the purposes of graphical display, this number is then 
 					 * transformed to fall within the range -100 to 100. 
 					 */
 					
					//System.out.print("Transformed steering wheel input from " + chunks[5] + " " + chunks[6] + " to ");
 					chunks[5] = chunks[5].substring(2);
 					chunks[6] = chunks[6].substring(2);
 					if(chunks[5].contains("e") || chunks[5].contains("E")
 							|| chunks[5].contains("f") || chunks[5].contains("F"))
 						chunks[5] = chunks[5].replaceFirst("0", "F");
 					chunks[5] = chunks[5] + chunks[6];
 					wheelAngle = Integer.valueOf(chunks[5],16).shortValue();
 					wheelAngle += 373;
 					wheelAngle *=200;
 					wheelAngle /= 710;
 					wheelAngle -= 100;
 					angcnt++;
 					data.add(Integer.toString(wheelAngle));
 					break;
 				case 1492:	//Brake pedal
 					chunks[6] = chunks[6].replaceAll("x", "");
 					brakePedal = Integer.parseInt(chunks[6]); //Brake pedal byte is conveniently either 0 or 1
 					brkcnt++;
 					data.add(Integer.toString(brakePedal));
 					break;
 				case 710: //Gas pedal
 					chunks[8] = chunks[8].substring(2);
 					if(chunks[8].equals("18")) gasPedal = 0;
 					else if(chunks[8].equals("00")) gasPedal = 1;
 					data.add(Integer.toString(gasPedal));
 					gascnt++;
 					break;
 				case 1407:	//Hazard lights
 					chunks[11] = chunks[11].substring(2);
 					if(chunks[11].equals("80")) hazardLights = 0;
 					else if(chunks[11].equals("90")) hazardLights = 4;
 					hazcnt++;
 					data.add(Integer.toString(hazardLights));
 				case 1421:	//Turn signals and lights
 					chunks[6] = chunks[6].substring(2);
 					chunks[7] = chunks[7].substring(2);
 					/**Turn signals can be in one of five modes: off, depressed right,
 					 * depressed left, locked right, or locked left. The numerical scheme is
 					 * as follows:
 					 * -2 = Locked left
 					 * -1 = Depressed left
 					 * 0 = Off
 					 * 1 = Depressed right
 					 * 2 = Locked right 
 					 */
 					if(chunks[7].equals("00")) turnSignal = 0;
 					else if(chunks[7].equals("40")) turnSignal = -1;
 					else if(chunks[7].equals("48")) turnSignal = -2;
 					else if(chunks[7].equals("80")) turnSignal = 1;
 					else if(chunks[7].equals("90")) turnSignal = 2;
 					data.add(Integer.toString(turnSignal));
 					
 					/**Lights can be on in one of four modes: off, parking lights,
 					 * on, and auto. Additionally, the brights may or may not be on
 					 * during any of these modes. As the lights all operate through
 					 * the same mechanism, the two separate inputs are combined into
 					 * one single datum below with the following assignment scheme:
 					 * 5: Brights on
 					 * 4: Lights on mode
 					 * 3: Auto mode
 					 * 2: Parking lights mode
 					 * 1: Lights off mode
 					 */
 					if(chunks[6].contains("A")) lights = 4;
 					else if(chunks[6].equals("08")) lights = 0;
 					else if(chunks[6].equals("48")) lights = 1;
 					else if(chunks[6].equals("28")) lights = 3;
 					else if(chunks[6].equals("88")) lights = 2;
 					litcnt++;
 					data.add(Integer.toString(lights));
 					break;
 				case 1477:	//Velocity
					//System.out.print("Converted " + chunks[6] + " " + chunks[7] + " to ");
 					chunks[6] = chunks[6].substring(2);
 					chunks[7] = chunks[7].substring(2);
 					chunks[6] = chunks[6] + chunks[7];
					tempVelocity = Integer.valueOf(chunks[6],16);
					velocity = (double) tempVelocity*speedFactor;
 					velcnt++;
					//System.out.println(velocity + " mph");
 					data.add(Double.toString(velocity));
 					break;
 				}
 				count++;
 			}
 			sentence = in.readLine();			
 		}
 		
 		in.close();
 		exportable = data.toArray(new String[0]);
 	
 		double angfreq = (double) angcnt/count;
 		double brkfreq = (double) brkcnt/count;
 		double gasfreq = (double) gascnt/count;
 		double litfreq = (double) litcnt/count;
 		double hazfreq = (double) hazcnt/count;
 		double velfreq = (double) velcnt/count;
 		
 		System.out.println("Steering wheel angle count: " + angcnt);
 		System.out.println("Brake pedal count: " + brkcnt);
 		System.out.println("Gas pedal count: " + gascnt);
 		System.out.println("Head light count: " + litcnt);
 		System.out.println("Hazard light count: " + hazcnt);
 		System.out.println("Velocity count: " + velcnt);
 		System.out.println("Total count: " + count + "\n");
 		
 		System.out.println("Steering wheel angle relative frequency: " + angfreq);
 		System.out.println("Brake pedal relative frequency: " + brkfreq);
 		System.out.println("Gas pedal relative frequency: " + gasfreq);
 		System.out.println("Head light relative frequency: " + litfreq);
 		System.out.println("Hazard light relative frequency: " + hazfreq);
 		System.out.println("Velocity relative frequency: " + velfreq);
 	}
 	
 	public String[] getData(){
 		return exportable;
 	}
 
 }
