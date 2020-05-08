 package fearlesscode.util;
 
 import java.util.*;
 import java.io.*;
 
 /**
  * Az események naplózásáért felelős osztály.
  */
 public class Logger
 {
 	/**
 	 * Eseményeket ír ki a képernyőre.
 	 * pl.: [3:Player] has moved to position (10,15).
 	 * @param info Az esemény forrása
 	 * @param string Az esemény leírása
	 * @return A specifikált kimeneti nyelv szerinti formátumban az esemény szöveges reprezentációja.
 	 */
 	public static void log(Info info, String string)
 	{
 		System.out.println(info.getName()+" has "+ string);
 	}
 }
