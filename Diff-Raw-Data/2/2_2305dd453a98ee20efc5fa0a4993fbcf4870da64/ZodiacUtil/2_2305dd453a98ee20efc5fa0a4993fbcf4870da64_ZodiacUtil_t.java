 package utils;
 
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import model.Zodiac;
 
 import org.json.JSONException;
 
 public class ZodiacUtil {
 
 	private static final String zodiac= "{\r\n" + 
 			"\"leo\":\"Generous and warmhearted, Creative and enthusiastic,Broad-minded and expansive,Faithful and loving,Pompous and patronizing,Bossy and interfering,Dogmatic and intolerant\",\r\n" + 
 			"\"taurus\":\"Patient and reliable,Warmhearted and loving,Persistant and determined,Placid and security loving,Jealous and possessive,Resentful and inflexible,Self-indulgent and greedy\",\r\n" + 
 			"\"gemini\":\"Adaptive and versatile,Communicative and witty,Intellectual and eloquent,Youthful and lively,Nervous and tense,Superficial and inconsistent,Cunning and inquisitive\",\r\n" + 
 			"\"cancer\":\"Emotional and loving,Intuitive and imaginative,Shrewd and cautious,Protective and sympathetic,Changeable and moody,Overemotional an touchy,Clinging and unable to let go\",\r\n" + 
 			"\"virgo\":\"Modest and shy,Meticulous and reliable,Practical and diligent,Intelligent and analytical,Fussy and a worrier,Overcritical and harsh,Perfectionalist and conservative\",\r\n" + 
 			"\"libra\":\"Diplomatic and urbane,Romantic and charming,Easygoing and sociable,Idealistic and peaceable,Indecisive and changeable,Gullible and easily influenced,Flirtatious and elf-indulgent\",\r\n" + 
 			"\"scorpio\":\"Determined and forceful,Emotional and intuitive,Powerful and passionate,Exciting and magnetic,Jealous and resentful,Compulsive and obsessive,Secretive and obstinate\",\r\n" + 
 			"\"sagittarius\":\"Optimistic and freedom-loving,Jovial and good-humored,Honest and straightforward,Intellectual and philosophical,Blindly optimistic and careless,Irresponsible and superficial,Tactless and restless\",\r\n" + 
 			"\"capricorn\":\"Practical and prudent,Ambitious and disciplined,Patient and careful,Humorous and reserved,Pessimistic and fanalistic,Miserly and grudging\",\r\n" + 
 			"\"aquarius\":\"Friendly and humanitarian,Honest and loyal,Original and inventive,Independent and intelluctual,Intractale and contrary,Preverse and unpredictable,Unemotional and detached\",\r\n" + 
 			"\"pisces\":\"Imaginative and sensitive,Compassionate and kind,Selfless and unworldly,Intuitive and sympathetic,Escapist and idealistic,Secretive and vague,Weak-willed and easily lead\",\r\n" + 
			"\"aries\":\"Adventurous and energetic,Pioneering and courageous,Enthusiastic and confident,Dynamic and quick-witted,Selfish and quick-tempered,Impulsive and impatient,Foolhardy and daredevil\"\r\n" + 
 			"}";
 	
 	public static void main(String[] a) throws JSONException {
 		Calendar cal = new GregorianCalendar(1981, 7, 28);
 		Zodiac zod = ZodiacUtil.getZodiac(cal.getTime());
 		System.out.println(zod.getSunSign());
 		System.out.println(zod.getCharactor());
 	}
 	
 	public static Zodiac getZodiac(Date date) throws JSONException {
 		
 		if(date == null)
 			return new Zodiac();
 		
 		String sign="";
 		String element="";
 		String chineseSign="";
 		int month, day,year;
 
 		Calendar cal = new GregorianCalendar();
 		cal.setTime(date);
 		
 		day = cal.get(Calendar.DAY_OF_MONTH);
 		month = cal.get(Calendar.MONTH) + 1;
 		year = cal.get(Calendar.YEAR);
 
 		if((month == 1) && (day <= 20) || (month == 12) && (day >= 22)) {
 			sign = "Capricorn";
 		} else if((month == 1) || (month == 2) && (day <= 19)) {
 			sign = "Aquarius";
 		} else if((month == 2) || (month == 3) && (day <= 20)) {
 			sign = "Pisces";
 		} else if((month == 3) || (month == 4) && (day <= 19)) {
 			sign = "Aries";
 		} else if((month == 4) || (month == 5) && (day <= 21)) {
 			sign = "Taurus";
 		} else if((month == 5) || (month == 6) && (day <= 21)) {
 			sign = "Gemini";
 		} else if((month == 6) || (month == 7) && (day <= 23)) {
 			sign = "Cancer";
 		} else if((month == 7) || (month == 8) && (day <= 23)) {
 			sign = "Leo";
 		} else if((month == 8) || (month == 9) && (day <= 23)) {
 			sign = "Virgo";
 		} else if((month == 9) || (month == 10) && (day <= 23)) {
 			sign = "Libra";
 		} else if((month == 10) || (month == 11) && (day <= 22)) {
 			sign = "Scorpio";
 		} else if(month == 12) {
 			sign = "Sagittarius";
 		}
 		if((sign.equals("Aries"))||(sign.equals("Leo"))||(sign.equals("Sagittarius"))){
 			element="Fire";
 		}
 		else if((sign.equals("Taurus"))||(sign.equals("Virgo"))||(sign.equals("Capricorn"))){
 			element="Earth";
 		}
 		else if((sign.equals("Gemini"))||(sign.equals("Libra"))||(sign.equals("Aquarius"))){
 			element="Air";
 		}
 		else if((sign.equals("Cancer"))||(sign.equals("Scorpio"))||(sign.equals("Pisces"))){
 			element="Water";
 		}
 
 		int x = (1997 - year) % 12;
 		if ((x == 1) || (x == -11)){
 			chineseSign="Rat";
 		}
 		else{
 			if (x == 0){
 				chineseSign="Ox";
 			}
 			else{
 				if ((x == 11) || (x == -1)){
 					chineseSign="Tiger";
 				}
 				else{
 					if ((x == 10) || (x == -2)){
 						chineseSign="Rabbit";
 					}
 					else{
 						if ((x == 9) || (x == -3)){
 							chineseSign="Dragon";
 						}
 						else{
 							if ((x == 8) || (x == -4)){ 
 								chineseSign="Snake";
 							}
 							else{
 								if ((x == 7) || (x == -5)){ 
 									chineseSign="Horse";
 								}
 								else{
 									if ((x == 6) || (x == -6)){ 
 										chineseSign="Sheep";
 									}
 									else{
 										if ((x == 5) || (x == -7)){ 
 											chineseSign="Monkey";
 										}
 										else{
 											if ((x == 4) || (x == -8)){
 												chineseSign="Chicken";
 											}
 											else{
 												if ((x == 3) || (x == -9)){
 													chineseSign="Dog";
 												}
 												else{
 													if ((x == 2) || (x == -10)){
 														chineseSign="Pig";
 													} 
 												}
 											}
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		Zodiac zod = new Zodiac();
 		zod.setSunSign(sign);
 		zod.setElement(element);
 		zod.setChineseSign(chineseSign);
 		
 		JSONUtils jUtils = new JSONUtils();
 		jUtils.initialize(zodiac);
 		String charactors = jUtils.getValue(sign.toLowerCase()).getStringValue();
 		zod.setCharactor(charactors);
 
 		return zod;
 	}
 }
