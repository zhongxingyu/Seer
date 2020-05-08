 package lorian.graph.function;
 
 public class Calculate {
 
 	
 	private static double returnRounded(double d)
 	{
 		double r = Util.round(d, 0);
 		if(Math.abs(r - d) < 0.0001)
 			return r;
 		else 
 			return d;
 	}
 	
 	public static PointXY Zero(Function f, double LowX, double UpX)
 	{
 		if(UpX <= LowX) return new PointXY(-999999, -99999);
 		Function zeroline = new Function("0");
 		PointXY intersect = Intersect(f, zeroline, LowX, UpX);
		intersect.setX(returnRounded(intersect.getX()));
		intersect.setY(0);
 		return intersect;
 	}
 	public static PointXY Minimum(Function f, double LowX, double UpX)
 	{
 		double dx = 0.000001;
 		double lowestX = LowX - 1;
 		double lowest = 9999999;
 		double val;
 		for(double x = LowX + dx; x < UpX; x += dx)
 		{
 			val = f.Calc(x);
 			if(val < lowest)
 			{
 				lowest = val;
 				lowestX = x;
 			}
 		}
 		return new PointXY(returnRounded(Util.round(lowestX, 6)), returnRounded(f.Calc(lowestX)));
 	}
 	public static PointXY Maximum(Function f, double LowX, double UpX)
 	{
 		double dx = 0.000001;
 		double highestX = LowX - 1;
 		double highest = -9999999;
 		double val;
 		for(double x = LowX; x < UpX; x += dx)
 		{
 			val = f.Calc(x);
 			if(val > highest)
 			{
 				highest = val;
 				highestX = x;
 			}
 		}
 		return new PointXY(returnRounded(Util.round(highestX, 6)), returnRounded(f.Calc(highestX)));
 	}
 	public static PointXY Intersect(Function f, Function g, double LowX, double UpX)
 	{
 		double smallestDelta = 1000000000;
 		double currentDelta;
 		double step = 10 / 10000000.0;
 		double x = 0, x2 = 0;
 		 
 	
 		for(x = LowX + 0.001; x < UpX;x+=0.1)
 		{
 			currentDelta = Math.abs(g.Calc(x) - f.Calc(x));
 			if(currentDelta < 1)
 			{
 				x2 = x;
 				break;
 			}
 		}
 		
 		for(x = x2; x < x2 + 1; x+= 0.001)
 		{
 			currentDelta = Math.abs(g.Calc(x) - f.Calc(x));
 			if(currentDelta < 0.01)
 			{
 				x2 = x;
 				break;
 			}
 		}
 		
 		for(x = x2; x < x2 + 1; x+= step)
 		{
 			currentDelta = Math.abs(g.Calc(x) - f.Calc(x));
 			if(smallestDelta > currentDelta)
 			{
 				smallestDelta = currentDelta;
 				x2 = x;
 			}
 			
 		}
		return new PointXY(Util.round(x2, 6), Util.round(f.Calc(x2), 6));
 	}	
 
 	
 	public static double DyDx(Function f, double x)
 	{
 		if(Double.isNaN(x)) return Double.NaN;
 		//double dx = 0.000000001;
 		double dx   = 0.000001;
 		
 		double dydx;
 		double dy = (f.Calc(x+dx) - f.Calc(x));
 		
 		dydx = dy / dx;
 		
 		return returnRounded(Util.round(dydx, 6));
 		//return Util.round(dydx, 6);
 	}	
 	public static double DyDx(Function f, double x, double dx)
 	{
 		if(Double.isNaN(x) || Double.isNaN(dx)) return Double.NaN;
 		double dydx;
 		double dy = (f.Calc(x+dx) - f.Calc(x));
 		
 		dydx = dy / dx;
 		return dydx;
 	
 	}
 	
 	public static double Integral(Function f, double LowX, double UpX)
 	{
 		double dx = 0.00001;
 		//double dx =   0.000001;
 		double lowersum=0, uppersum=0;
 		double val;
 		for(double x = LowX; x < UpX; x += dx)
 		{
 			val = f.Calc(x);
 			if(Double.isNaN(val)) return Double.NaN;
 			if(x != LowX)
 			{
 				uppersum += val * dx;
 			}
 			if(x != (UpX - dx))
 			{
 				lowersum += val * dx;
 			}
 		}
 		double average = (lowersum + uppersum) / 2;
 		average = Util.round(average, 6);
 		return returnRounded(average);
 	}
 	public static double FindLastXBeforeNaN(Function f, double Xstart)
 	{
 		double dx = 0.0001;
 		if(Double.isNaN(f.Calc(Xstart))) return Double.NaN;
 		
 		double Xtmp = 0;
 		for(double x = Xstart; x < Xstart + 50 ; x += 0.001)
 		{
 			if(Double.isNaN(f.Calc(x))) {
 				Xtmp = x - 0.001;
 				break;
 			}
 		}
 		//for(double x = Xstart; x < Xstart + 50 ; x += dx)
 		for(double x = Xtmp; x < Xtmp + 1; x += dx)
 		{
 			if(Double.isNaN(f.Calc(x)))
 			{
 				return (x - dx);
 			}
 		}
 		
 		return Double.NaN;
 	}
 }
