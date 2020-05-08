 package pt.feup.stockportfolio;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import android.graphics.Color;
 
 public class ColorFactory {
 
 	static boolean served = false;
 	static ArrayList<Integer> availableColor =  new ArrayList<Integer>();
 	
 	
 	ColorFactory()
 	{
 		refresh();
 	}
 	
 	static int getDefault()
 	{
 		return Color.parseColor("#ED1C24");
 	}
 	
 	static void refresh()
 	{
 		availableColor.add(Color.parseColor("#D6DF23"));
 		availableColor.add(Color.parseColor("#F7931D"));
 		availableColor.add(Color.parseColor("#27A9E1"));
 
 		availableColor.add(Color.parseColor("#652C90"));
 		availableColor.add(Color.parseColor("#FBAF3F"));
 		availableColor.add(Color.parseColor("#8A5D3B"));
 		
 
 		availableColor.add(Color.parseColor("#27A9E1"));
 		availableColor.add(Color.parseColor("#FBAF3F"));
 		availableColor.add(Color.parseColor("#006738"));
 		availableColor.add(Color.parseColor("#720F72"));
 		availableColor.add(Color.parseColor("#8A5D3B"));
 		availableColor.add(Color.parseColor("#F05A28"));
 		Collections.shuffle(availableColor);
 	}
 	static synchronized Integer getColor()
 	{
 		if (!served)
 			return serve();
 		if (availableColor.size() == 0)
 			refresh();
 		Integer ret ;
 		int i = 0;
 		do
 		{
 			ret = availableColor.get(i);
 			i++;
 		}
 		while(ret == Utils.myQuotes.get(Utils.myQuotes.size()-1).color);
 		availableColor.remove(i);
 		return ret;
 	}
 
 	
 	static int serve = Color.parseColor("#262261");
 	private static Integer serve() {
 		
 		served = true;
 		return serve;
 		
 	}
 	
 }
