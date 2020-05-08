 package curveGeneration;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import model.Excel;
 
 public class ParametricCurve {
 	
 	public ParametricCurve(Excel Ex4, Excel Ex3, Excel Ex2, Excel Ex1)
 	{
 		ex1 = Ex1;
 		ex2 = Ex2;
 		ex3 = Ex3;
 		ex4 = Ex4;
 	}
 	
 	public ArrayList<Excel> Calculation()
 	{	
 		NumberFormat formatter = new DecimalFormat("0.0##########");
 		ArrayList<Excel> result = new ArrayList<Excel>();
 		if(ex1==null || ex2==null || ex3==null || ex4==null) return null;
 			
 			ex1.setColor(Color.red);
 			ex2.setColor(Color.red);
 			ex3.setColor(Color.red);
 			ex4.setColor(Color.red);
 			
 			Matrix Gnx = new Matrix(4,2);
 			Gnx.fillingMatrix(masGnx);
 			
 			Matrix Mn = new Matrix(4,4);
 			
 			Mn.fillingMatrix(masMn);
 			
 			Matrix Cx;
 			if(matrixMultiplication(Mn,Gnx) != null)
 			{
 				Cx = matrixMultiplication(Mn,Gnx);
 				
 				double step = getStep(ex1,ex2,ex3,ex4);
 				
 				for(double i=0; i<=1; i+=step)
 				{
 					Matrix T = new Matrix(1,4);
 					double masT[] = {factor*Double.parseDouble(formatter.format(Math.pow(i,3)).replace(',', '.')),
 							factor*Double.parseDouble(formatter.format(Math.pow(i,2)).replace(',', '.')),
 							factor*Double.parseDouble(formatter.format(Math.pow(i,1)).replace(',', '.')),
 							factor*1.0};
 					T.fillingMatrix(masT);
 					
 					if(matrixMultiplication(T,Cx) != null)
 					{
 						Matrix Pt;
 						Pt = matrixMultiplication(T,Cx);
						Pt.showMatrix();
 
 						
 						if(Pt.getColumns() == 1 && Pt.getRows() == 2)
 						{
 							result.add(new Excel((int)Pt.getEl(0, 0),(int)Pt.getEl(0, 1),Color.black));
 						}
 					}
 				}
 				result.add(ex1);
 				result.add(ex2);
 				result.add(ex3);
 				result.add(ex4);
 			}
 		
 		return result;
 	}
 
 	public static Matrix matrixMultiplication(Matrix A, Matrix B)
 	{
 		Matrix C = new Matrix(A.getColumns(),B.getRows());
 		
 		if(A.getRows() == B.getColumns())
 		{
 			double rezultValue = 0;
 	
 			for(int i=0; i<A.getColumns(); i++)
 			{
 				for(int j=0; j<B.getRows(); j++)
 				{
 					for(int p=0; p<A.getRows(); p++)
 					{
 						rezultValue+=A.getEl(i, p)*B.getEl(p, j);
 					}
 					C.updateEl(i, j, new BigDecimal(rezultValue).setScale(3, RoundingMode.UP).doubleValue());
 					rezultValue = 0;
 				}
 			}
 			return C;
 		}
 		else
 		{
 			System.out.println("  1    " +
 					"   2 !!!");
 		}
 		
 		return null;
 	}
 	
 	public static double getStep(Excel e1,Excel e2,Excel e3,Excel e4)
 	{
 		double step = 0;
 		
 		double max_size = 0;
 		
 		List<Excel> list = new LinkedList<Excel>();
 		list.add(e1);
 		list.add(e2);
 		list.add(e3);
 		list.add(e4);
 		
 		for(int i=0;i<list.size();i++)
 		{
 			if(i + 1 != list.size())
 			{
 				Excel point1 = list.get(i);
 				Excel point2 = list.get(i+1);
 				double current_max = Math.max(Math.abs((double)point2.getX()-(double)point1.getX() ), Math.abs( (double)point2.getY()-(double)point1.getY()));
 				
 				if(current_max > max_size)
 				{
 					max_size = current_max;
 				}
 			}
 		}
 		
 		step = (1.0/max_size)/5.0;
 		
 		return step;
 	}
 	
 	public static Point rVector(Excel Ex1,Excel Ex2)
 	{
 		 return new Point(Ex2.getX() - Ex1.getX(), Ex2.getY() - Ex1.getY());	
 	}
 	
 	public static Excel getEx1() {
 		return ex1;
 	}
 
 	public static Excel getEx2() {
 		return ex2;
 	}
 
 	public static Excel getEx3() {
 		return ex3;
 	}
 
 	public static Excel getEx4() {
 		return ex4;
 	}
 
 	public static void setMasGnx(double[] masGnx) {
 		ParametricCurve.masGnx = masGnx;
 	}
 
 	public static void setMasMn(double[] masMn) {
 		ParametricCurve.masMn = masMn;
 	}
 
 	public static void setFactor(double factor) {
 		ParametricCurve.factor = factor;
 	}
 	
 	/*public static Excel[] getMasEx()
 	{
 		Excel masEx[] = {ex1,ex2,ex3,ex4};
 		return masEx;
 	}*/
 
 	private static Excel ex1;
 	private static Excel ex2;
 	private static Excel ex3;
 	private static Excel ex4;
 	private static double masGnx[];
 	private static double masMn[];
 	private static double factor;
 }
