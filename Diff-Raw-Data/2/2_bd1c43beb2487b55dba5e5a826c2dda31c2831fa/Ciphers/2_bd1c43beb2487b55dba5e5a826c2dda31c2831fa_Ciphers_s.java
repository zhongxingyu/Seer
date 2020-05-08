 public class Ciphers {
 
     static char[]          asciitable   = { '\u2718', '\u2718', '\u2718',
             '\u2718', '\u2718', '\u2718', '\u2718', '\u2718', '\u2718',
             '\u2718', '\u2718', '\u2718', '\u2718', '\u2718', '\u2718',
             '\u2718', '\u2718', '\u2718', '\u2718', '\u2718', '\u2718',
             '\u2718', '\u2718', '\u2718', '\u2718', '\u2718', '\u2718',
             '\u2718', '\u2718', '\u2718', '\u2718', '\u2718', '\u2718',
             '\u2718', '\u2718', '\u2718', '\u2718', '\u2718', '\u2718',
             '\u2718', '\u2718', '\u2718', '\u2718', '\u2718', '\u2718',
             '\u2718', '\u2718', '\u2718', '0', '1', '2', '3', '4', '5', '6',
             '7', '8', '9', '\u2718', '\u2718', '\u2718', '\u2718', '\u2718',
             '\u2718', '\u2718', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
             'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
             'W', 'X', 'Y', 'Z', '\u2718', '\u2718', '\u2718', '\u2718',
             '\u2718', '\u2718', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
             'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
             'w', 'x', 'y', 'z'         };
 
     static String[]        morse        = { ".-", // a
             "-...", // b
             "-.-.", // c
             "-..", // d
             ".", // e
             "..-.", // f
             "--.", // g
             "....", // h
             "..", // i
             ".---", // j
             "-.-", // k
             ".-..", // l
             "--", // m
             "-.", // n
             "---", // o
             ".--.", // p
             "--.-", // q
             ".-.", // r
             "...", // s
             "-", // t
             "..-", // u
             "...-", // v
             ".--", // w
             "-..-", // x
             "-.--", // y
             "--..", // z
             "-----",// 0
             ".----",// 1
             "..---",// 2
             "...--",// 3
             "....-",// 4
             ".....",// 5
             "-....",// 6
             "--...",// 7
             "---..",// 8
             "----." // 9
                                         };
 
     static String[]        morseletters = { "A", "B", "C", "D", "E", "F", "G",
             "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
             "U", "V", "W", "X", "Y", "Z", "0", "1", "2", "3", "4", "5", "6",
             "7", "8", "9"              };
 
     public static String[] base64table  = { "A", "B", "C", "D", "E", "F", "G",
             "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
             "U", "V", "W", "X", "Y", "Z", "a", "b", "c", "d", "e", "f", "g",
             "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
             "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6",
             "7", "8", "9", "+", "/", "=" };
 
     static char[]          hex          = { '0', '1', '2', '3', '4', '5', '6',
             '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
 
     public static String asctodec(String code) {
         char[] codeline = removeSpaces(code).toCharArray();
         String[] parts = new String[codeline.length];
         String result = "";
 
         for (int i = 0; i < codeline.length; i++) {
             parts[i] = "";
             if (codeline[i] / 100 < 1) {
                 parts[i] += "0";
             }
             parts[i] += (int) codeline[i];
         }
         for (String part : parts) {
             result += part + " ";
         }
         return result;
     }
 
     public static String atbash(String code) {
         char[] codeline = code.toCharArray();
         String result = "";
         for (int i = 0; i < codeline.length; i++) {
             if (codeline[i] >= 'a' && codeline[i] <= 'z') {
                 codeline[i] = (char) ('z' - (codeline[i] - 'a'));
             } else if (codeline[i] >= 'A' && codeline[i] <= 'Z') {
                 codeline[i] = (char) ('Z' - (codeline[i] - 'A'));
             }
             result += codeline[i];
         }
         return result;
 
     }
 
     public static String base64todec(String code) {
         try {
             String base64code = "";
             for (char c : code.toCharArray())
                 if ((c >= 65 && c <= 90) || (c >= 97 && c <= 122)
                         || (c >= 48 && c <= 57) || c == 43 || c == 47)
                     base64code += c;
             if (base64code.endsWith("A")) if (base64code.endsWith("AA"))
                 base64code = base64code.substring(0, base64code.length() - 2);
             else base64code = base64code.substring(0, base64code.length() - 1);
             String binary6 = "";
             for (int i = 0; i < base64code.length(); i++) {
                 binary6 += Ciphers
                         .dectobin(getPostionInBase64Table(base64code.charAt(i)))
                         .trim().substring(2);
             }
             int correctlength = binary6.length() % 8;
             while (correctlength > 0) {
                 binary6 += "0";
                 correctlength--;
             }
             // split into group of 8
             // fill up with 0 at the end to prevent errors
             int missingnulls = 8 - (binary6.length() % 8);
             for (; missingnulls % 8 > 0; missingnulls--)
                 binary6 = binary6 + "0";
             char[] codeline = binary6.toCharArray();
             String binary8[] = new String[codeline.length / 8];
             binary8[0] = "";
             for (int i = 0, j = 0; i < codeline.length; i++) {
                 if (i % 8 == 0 && i != 0) {
                     j++;
                     binary8[j] = "";
                 }
                 binary8[j] += codeline[i];
             }
             String result = "";
             for (String s : binary8) {
                 result += Ciphers.bintodec(s).trim() + " ";
             }
             result = result.trim();
             if (result.endsWith(" 0"))
                 result = result.substring(0, result.length() - 2);
             return result;
         } catch (Exception e) {
             e.printStackTrace();
             return "ERROR 2: An internal error occured".toUpperCase();
         }
     }
 
     public static String bintodec(String code) {
         try {
             String preparedcode = removeSpaces(code);
 
             // fill up with 0 at the beginning to prevent errors
             int missingnulls = 8 - (preparedcode.length() % 8);
             for (; missingnulls % 8 > 0; missingnulls--)
                 preparedcode = preparedcode + "0";
 
             char[] codeline = preparedcode.toCharArray();
             String[] parts = new String[(codeline.length + 7) / 8];
             String result = "";
 
             parts[0] = "";
             for (int i = 0, j = 0; i < codeline.length; i++) {
                 if (i % 8 == 0 && i != 0) {
                     j++;
                     parts[j] = "";
                 }
                 parts[j] += codeline[i];
             }
 
             for (int i = 0; i < parts.length; i++) {
                 int temp = 0;
                 int add = 128;
                 for (int j = 0; j < 8; j++) {
                     if (parts[i].charAt(j) == '1') {
                         temp += add;
                     }
                     add /= 2;
                 }
                 parts[i] = "" + temp;
             }
             for (String part : parts) {
                 if (part.length() == 3)
                     result += part + " ";
                 else if (part.length() == 2)
                     result += "0" + part + " ";
                 else if (part.length() == 1) result += "00" + part + " ";
             }
 
             return result.trim();
         } catch (Exception e) {
             return "ERROR 2: An internal error occured".toUpperCase();
         }
 
     }
 
     public static String caesarianShift(String code, int shift) {
         char[] codeline = code.toCharArray();
         String result = "";
         for (int i = 0; i < codeline.length; i++) {
             if (codeline[i] >= 'a' && codeline[i] <= 'z') {
                 if (codeline[i] + shift > 'z')
                     codeline[i] = (char) (('a' - 1) + ((codeline[i] + shift) - 'z'));
                 else codeline[i] = (char) (codeline[i] + shift);
             } else if (codeline[i] >= 'A' && codeline[i] <= 'Z') {
                 if (codeline[i] + shift > 'Z')
                     codeline[i] = (char) (('A' - 1) + ((codeline[i] + shift) - 'Z'));
                 else codeline[i] = (char) (codeline[i] + shift);
             }
         }
         for (char element : codeline) {
             result += element;
         }
         return result.trim();
     }
 
     public static String dectoasc(String code) {
         char[] codeline = code.toCharArray();
         boolean spaces = false;
         int space = 0;
         boolean check = true;
         String solution = "ERROR 1: INVALID INPUT";
 
         for (char element : codeline) {
             if (element < '0' || element > '9')
             	 if(element != ' ')
             		 check = false;
             if(element == ' '){
             	spaces = true;
             	space++;
             }
         }
 
         if (check) {
         	if(spaces){
         		String[] parted = new String[space+1];
         		for (int i = 0; i < parted.length; i++) {
 	                parted[i] = "";
 	            }
         		for (int i = 0, j = 0; i < codeline.length; i++) {
 	                if (codeline[i]==' ') {
 	                    j++;
 	                }
 	                else{
 	                    parted[j] += codeline[i];
 	                }
 	            }
         		solution = "";
 	            for (String element : parted) {
 	                if (Integer.parseInt(element) < 123) {
 	                    solution += asciitable[Integer.parseInt(element)];
 	                } else solution += '\u2718';
 	            }
         	}
 	        else{
 	            String[] parted = new String[(codeline.length + 2) / 3];
 	
 	            for (int i = 0; i < parted.length; i++) {
 	                parted[i] = "";
 	            }
 	            for (int i = 0, j = 0; i < codeline.length; i++) {
 	                if (i == 0) {
 	                    parted[j] += codeline[i];
 	                } else if (i % 3 == 0) {
 	                    j++;
 	                    parted[j] += codeline[i];
 	                } else parted[j] += codeline[i];
 	            }
 	            solution = "";
 	            for (String element : parted) {
 	                if (Integer.parseInt(element) < 123) {
 	                    solution += asciitable[Integer.parseInt(element)];
 	                } else solution += '\u2718';
 	            }
 	        }
         }
         return solution.trim();
     }
 
     public static String dectobase64(String code) {
         try {
             String leer = "";
             for (int i = 0; i < code.length(); i++)
                 if (code.charAt(i) == ' ') leer += i + " ";
             leer = leer.trim();
             String bincode = Ciphers.dectobin(code);
             if (bincode.equals("ERROR 1: INVALID INPUT")) return bincode;
             bincode = Ciphers.removeSpaces(bincode);
             // check if correct length
             int correctlength = bincode.length() % 3;
             while (correctlength > 0) {
                 bincode += "00000000";
                 correctlength--;
             }
             // split into group of 6
             // fill up with 0 at the end to prevent errors
             int missingnulls = 6 - (bincode.length() % 6);
             for (; missingnulls % 6 > 0; missingnulls--)
                 bincode = "0" + bincode;
             char[] codeline = bincode.toCharArray();
             String parts[] = new String[codeline.length / 6];
             parts[0] = "";
             for (int i = 0, j = 0; i < codeline.length; i++) {
                 if (i % 6 == 0 && i != 0) {
                     j++;
                     parts[j] = "";
                 }
                 parts[j] += codeline[i];
             }
             String result = "";
             for (String s : parts) {
                 result += base64table[Integer.parseInt(Ciphers.bintodec(
                         "00" + s).trim())];
             }
             if (result.endsWith("A")) if (result.endsWith("AA"))
                 result = result.substring(0, result.length() - 2);
             else result = result.substring(0, result.length() - 1);
             return result.trim();
         } catch (Exception e) {
 
             e.printStackTrace();
             return "ERROR 2: An internal error occured".toUpperCase();
         }
     }
 
     public static String dectobin(String code) {
         if (code != "") {
             boolean check = true;
             char[] codeline = code.toCharArray();
             boolean spaces = false;
             int space = 0;
             String[] parts;
             String result = "";
 
             for (char element : codeline) {
                 if (element == ' ') {
                     spaces = true;
                     space++;
                 }
                 if (element < '0' || element > '9')
                     if (element != ' ') check = false;
             }
             if (check) {
                 if (spaces) {
                     parts = new String[space + 1];
                     parts[0] = "";
                     for (int i = 0, j = 0; i < codeline.length; i++) {
                         if (codeline[i] == ' ') {
                             j++;
                             parts[j] = "";
                         } else {
                             parts[j] += codeline[i];
                         }
                     }
                     for (int i = 0; i < parts.length; i++) {
                         String temp = parts[i];
                         parts[i] = "";
                         for (int j = 0; j < 8 - Integer.toBinaryString(
                                 Integer.parseInt(temp)).length(); j++) {
                             parts[i] += "0";
                         }
                         parts[i] += Integer.toBinaryString(Integer
                                 .parseInt(temp)) + " ";
                         result += parts[i];
                     }
                     return result.trim();
                 }
                 parts = new String[(codeline.length + 2) / 3];
                 parts[0] = "";
                 for (int i = 0, j = 0; i < codeline.length; i++) {
                     if (i % 3 == 0 && j != parts.length - 1 && i != 0) {
                         j++;
                         parts[j] = "";
                     }
                     parts[j] += codeline[i];
                 }
                 for (int i = 0; i < parts.length; i++) {
                     String temp = parts[i];
                     parts[i] = "";
                     for (int j = 0; j < 8 - Integer.toBinaryString(
                             Integer.parseInt(temp)).length(); j++) {
                         parts[i] += "0";
                     }
                     parts[i] += Integer.toBinaryString(Integer.parseInt(temp))
                             + " ";
                     result += parts[i];
                 }
                 return result.trim();
             }
         }
         return "ERROR 1: INVALID INPUT";
 
     }
 
     public static String dectohex(String code) {
         if (code != "") {
             boolean check = true;
             char[] codeline = code.toCharArray();
             boolean spaces = false;
             int space = 0;
             String[] parts;
             String result = "";
 
             for (char element : codeline) {
                 if (element == ' ') {
                     spaces = true;
                     space++;
                 }
                 if (element < '0' || element > '9')
                     if (element != ' ') check = false;
             }
             if (check) {
                 if (spaces) {
                     parts = new String[space + 1];
                     parts[0] = "";
                     for (int i = 0, j = 0; i < codeline.length; i++) {
                         if (codeline[i] == ' ') {
                             j++;
                             parts[j] = "";
                         } else {
                             parts[j] += codeline[i];
                         }
                     }
                     for (int i = 0; i < parts.length; i++) {
                         String temp = parts[i];
                         parts[i] = "";
                         for (int j = 0; j < 2 - Integer.toHexString(
                                 Integer.parseInt(temp)).length(); j++) {
                             parts[i] += "0";
                         }
                         parts[i] += Integer.toHexString(Integer.parseInt(temp))
                                 + " ";
                         result += parts[i];
                     }
                     return result;
                 }
                 parts = new String[(codeline.length + 2) / 3];
                 parts[0] = "";
                 for (int i = 0, j = 0; i < codeline.length; i++) {
                     if (i % 3 == 0 && j != parts.length - 1 && i != 0) {
                         j++;
                         parts[j] = "";
                     }
                     parts[j] += codeline[i];
                 }
                 for (int i = 0; i < parts.length; i++) {
                     String temp = parts[i];
                     parts[i] = "";
                     for (int j = 0; j < 2 - Integer.toHexString(
                             Integer.parseInt(temp)).length(); j++) {
                         parts[i] += "0";
                     }
                     parts[i] += Integer.toHexString(Integer.parseInt(temp))
                             + " ";
                     result += parts[i];
                 }
                 return result.trim();
             }
         }
         return "ERROR 1: INVALID INPUT";
     }
 
     public static String hextodec(String code) {
         try {
             code = removeSpaces(code);
             char[] codeline = code.toLowerCase().toCharArray();
             String[] parts = new String[(codeline.length + 1) / 2];
             String result = "";
 
             parts[0] = "";
             for (int i = 0, j = 0; i < codeline.length; i++) {
                 if (i % 2 == 0 && i != 0) {
                     j++;
                     parts[j] = "";
                 }
                 parts[j] += codeline[i];
             }
 
             for (int i = 0; i < parts.length; i++) {
                 String temp = parts[i];
                 if (parts[i].length() == 2) {
                     for (int j = 0; j < hex.length; j++) {
                         if (temp.charAt(0) == hex[j]) {
                             parts[i] = "" + j * 16;
                         }
                     }
                     for (int j = 0; j < hex.length; j++) {
                         if (temp.charAt(1) == hex[j]) {
                             parts[i] = "" + (Integer.parseInt(parts[i]) + j);
                         }
                     }
                 } else {
                     for (int j = 0; j < hex.length; j++) {
                         if (temp.charAt(0) == hex[j]) {
                             parts[i] = "" + j;
                         }
                     }
                 }
             }
             for (String part : parts) {
                 if (part.length() == 3)
                     result += part + " ";
                 else if (part.length() == 2)
                     result += "0" + part + " ";
                 else if (part.length() == 1) result += "00" + part + " ";
             }
             return result.trim();
         } catch (Exception e) {
             return "ERROR 2: An internal error occured".toUpperCase();
         }
     }
 
     public static String lettertonumber(String code, int start) {
         char[] codeline = code.toCharArray();
         String result = "";
         for (char element : codeline) {
             if (Character.toLowerCase(element) >= 'a'
                     && Character.toLowerCase(element) <= 'z')
                 result += (Character.toLowerCase(element) - 'a') + start;
         }
         return result;
     }
 
     public static String pattmorstoascii(String code, String shorts,
             String longs) {
         if (!shorts.equals("") || !longs.equals("")) {
             char[] dot = shorts.toCharArray();
             char[] line = longs.toCharArray();
             char[] codeline = code.toCharArray();
             int spaces = 0;
             String result = "";
 
             for (int i = 0; i < codeline.length; i++) {
                 for (char element : dot) {
                     if (codeline[i] == element) codeline[i] = '\u00B7';
                 }
                 for (char element : line) {
                     if (codeline[i] == element) codeline[i] = '\u23AF';
                 }
                 if (codeline[i] != '\u00B7' && codeline[i] != '\u23AF')
                     codeline[i] = ' ';
             }
 
             for (int i = 0; i < codeline.length; i++) {
                 if (codeline[i] == '\u00B7')
                     codeline[i] = '.';
                 else if (codeline[i] == '\u23AF')
                     codeline[i] = '-';
                 else spaces++;
             }
 
             String[] parted = new String[spaces + 1];
             parted[0] = "";
             for (int i = 0, j = 0; i < codeline.length; i++) {
                 if (codeline[i] != '.' && codeline[i] != '-') {
                     j++;
                     parted[j] = "";
                 } else parted[j] += codeline[i];
             }
 
             for (String element : parted) {
                 for (int j = 0; j < morse.length; j++) {
                     if (element.equals(morse[j])) {
                         result += morseletters[j];
                     }
                 }
             }
             return result;
         }
         return "";
     }
 
     public static String patttobin(String code, String zero, String one) {
         if (!zero.equals("") || !one.equals("")) {
             char[] zeros = zero.toCharArray();
             char[] ones = one.toCharArray();
             char[] codeline = code.toCharArray();
             String result = "";
             for (int i = 0; i < codeline.length; i++) {
                 boolean done = false;
                 for (char zero2 : zeros) {
                     if (codeline[i] == zero2) {
                         codeline[i] = '0';
                         done = true;
                         break;
                     }
                 }
                 if (!done) {
                     for (char one2 : ones) {
                         if (codeline[i] == one2) {
                             codeline[i] = '1';
                             break;
                         }
                     }
                 }
             }
             for (char element : codeline) {
                 result += element;
             }
             return result;
         }
         return "";
     }
 
     public static String removeSpaces(String input) {
         char[] withSpace = input.toCharArray();
         input = "";
         for (char element : withSpace) {
             if (element != ' ') {
                 input += element;
             }
         }
         return input;
     }
 
     public static String reverse(String code) {
         char[] codeline = code.toCharArray();
         String reversed = "";
         for (int i = codeline.length - 1; i >= 0; i--) {
             reversed += codeline[i];
         }
         return reversed;
     }
 
     public static String vinegere(String code, String pass) {
         char[] passcode;
         if (!pass.equals(""))
             passcode = pass.toCharArray();
         else {
             passcode = new char[1];
             passcode[0] = 'a';
         }
         char[] codeline = code.toCharArray();
         String result = "";
         for (int i = 0, j = 0; i < codeline.length; i++) {
             if (codeline[i] >= 'a' && codeline[i] <= 'z') {
                 if ((codeline[i] - Character.toLowerCase(passcode[j])) + 'a' < 'a') {
                     codeline[i] = (char) (('z' + 1) + (codeline[i] - Character
                             .toLowerCase(passcode[j])));
                 } else codeline[i] = (char) (codeline[i] - (Character
                         .toLowerCase(passcode[j]) - 'a'));
             } else if (codeline[i] >= 'A' && codeline[i] <= 'Z') {
                 if ((codeline[i] - Character.toUpperCase(passcode[j])) + 'A' < 'A') {
                     codeline[i] = (char) (('Z' + 1) + (codeline[i] - Character
                             .toUpperCase(passcode[j])));
                 } else codeline[i] = (char) (codeline[i] - (Character
                         .toUpperCase(passcode[j]) - 'A'));
             } else j--;
             if (j == passcode.length - 1) {
                 j = 0;
             } else {
                 j++;
             }
         }
         for (char element : codeline) {
             result += element;
         }
         return result;
 
     }
     
     public static String vinegereAutokey(String code, String pass) {
         char[] passcode;
         if (!pass.equals(""))
             passcode = pass.toCharArray();
         else {
             passcode = new char[1];
             passcode[0] = 'a';
         }
         char[] codeline = code.toCharArray();
         String result = "";
         if(passcode.length<=codeline.length){
 	        for (int i = 0, j = 0; i < passcode.length; i++, j++) {
 	            if (codeline[i] >= 'a' && codeline[i] <= 'z') {
 	                if ((codeline[i] - Character.toLowerCase(passcode[j])) + 'a' < 'a') {
 	                    codeline[i] = (char) (('z' + 1) + (codeline[i] - Character.toLowerCase(passcode[j])));
 	                } else codeline[i] = (char) (codeline[i] - (Character.toLowerCase(passcode[j]) - 'a'));
 	            } else if (codeline[i] >= 'A' && codeline[i] <= 'Z') {
 	                if ((codeline[i] - Character.toUpperCase(passcode[j])) + 'A' < 'A') {
 	                    codeline[i] = (char) (('Z' + 1) + (codeline[i] - Character.toUpperCase(passcode[j])));
 	                } else codeline[i] = (char) (codeline[i] - (Character.toUpperCase(passcode[j]) - 'A'));
 	            } 
 	            else{
 	            	j--;
 	            }
 	        }
 	        for (int i = passcode.length, j = 0; i < codeline.length; i++,j++) {
 	        	while(Character.toLowerCase(codeline[j]) < 'a' || Character.toLowerCase(codeline[j]) > 'z'){
 	        		j++;
 	        	}
 	            if (codeline[i] >= 'a' && codeline[i] <= 'z') {
 	                if ((codeline[i] - Character.toLowerCase(codeline[j])) + 'a' < 'a') {
 	                    codeline[i] = (char) (('z' + 1) + (codeline[i] - Character.toLowerCase(codeline[j])));
 	                } 
 	                else 
 	                	codeline[i] = (char) (codeline[i] - (Character.toLowerCase(codeline[j]) - 'a'));
 	            } else if (codeline[i] >= 'A' && codeline[i] <= 'Z') {
 	                if ((codeline[i] - Character.toUpperCase(codeline[j])) + 'A' < 'A') {
 	                    codeline[i] = (char) (('Z' + 1) + (codeline[i] - Character.toUpperCase(codeline[j])));
 	                } 
 	                else 
 	                	codeline[i] = (char) (codeline[i] - (Character.toUpperCase(codeline[j]) - 'A'));
 	            } 
 	            else{
 	            	j--;
 	            }
 	        }
 	        for (char element : codeline) {
 	            result += element;
 	        }
         }
         return result;
 
     }
     
     public static String numbertoletter(String code, int start) {
         char[] codeline = code.toCharArray();
         String result = "";
         boolean spaces = false;
         int space=0;
         int letters = 0;
         for(int i = 0; i < codeline.length;i++){
         	if(codeline[i]==' '){
         		spaces = true;
         		space++;
         	}
         	if((codeline[i]<'0' || codeline[i]> '9')&&codeline[i]!= ' '){
         		letters++;
         	}
         }
         if(spaces){
         	String[] parts = new String[space+letters+1];
         	parts[0]="";
         	for(int i=0,j=0;i< codeline.length;i++){
         		if(codeline[i]>='0' && codeline[i]<='9'){
         			parts[j]+=codeline[i];
         		}
         		else if(codeline[i]==' '){
         			if(j<parts.length-1){
 	        			j++;
 	        			parts[j]="";
         			}
         		}
         		else{
         			if(j<parts.length-1){
         				if(codeline[i-1]!=' '){
         					if(!(codeline[i-1]<'0' || codeline[i-1]> '9'))
         						j++;
         				}
 	        			parts[j]=""+codeline[i];
 	        			if(j<parts.length-1){
 		        			j++;
 		        			parts[j]="";
 	        			}
         			}
         		}
         	}
         	for(int i = 0; i< parts.length;i++){
         		if(parts[i].length()>0){
 	        		if(parts[i].charAt(0)>='0' &&parts[i].charAt(0)<='9'){
 	        			result+=""+(char)(Integer.parseInt(parts[i])+('a'-start));
 	        		}
 	        		else{
 	        			result+= parts[i];
 	        		}
         		}
         	}
         }
 	    else{
 	        for (char element : codeline) {
 	            if (element >= '0' && element <= '9')
 	                result += (char)(Integer.parseInt(""+element)+('a'-start));
 	            else
 	            	result+=element;
 	        }
 	    }
         return result;   
     }
 
     private static String getPostionInBase64Table(char c) {
         if ((c >= 65 && c <= 90))// Grobuchstabe
             return c - 65 + "";
         else if ((c >= 97 && c <= 122))// Kleinbuchstabe
             return c - 71 + "";
         else if ((c >= 48 && c <= 57))// Ziffern
             return c + 4 + "";
         else if (c == 43)// Sonderzeichen +
             return c + 19 + "";
         else return c + 16 + "";
     }
 
 }
