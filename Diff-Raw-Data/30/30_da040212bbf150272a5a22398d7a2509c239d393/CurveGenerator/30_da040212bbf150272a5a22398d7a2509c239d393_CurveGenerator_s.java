 package curveGeneration;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import model.Excel;
 
 public class CurveGenerator {
 	
 	public static ArrayList<Excel> ErmitForm(Excel ex4, Excel ex3, Excel ex2, Excel ex1)
 	{
 		System.out.println("ermitForm");
 		ArrayList<Excel> result = new ArrayList<Excel>();	
 		//NumberFormat formatter = new DecimalFormat("0.0##########");
 		
 		if(ex1==null || ex2==null || ex3==null || ex4==null) return null;
 		
 			int Px1 = ex1.getX();
 			int Py1 = ex1.getY();
 
 			int Px4 = ex4.getX();
 			int Py4 = ex4.getY();
 			
 			Point r1 = rVector(ex1, ex2);
 	        Point r4 = rVector(ex3, ex4);
 			
 			//System.out.println(Px1+" : "+Px4+" : "+Rx1+" : "+Rx4);
 			
 			ex1.setColor(Color.red);
 			ex2.setColor(Color.red);
 			ex3.setColor(Color.red);
 			ex4.setColor(Color.red);
 			
 			result.add(new Excel (ex3.getX(), ex3.getY(), Color.red));
 			result.add(new Excel (ex2.getX(), ex2.getY(), Color.red));
 			result.add(new Excel (ex1.getX(), ex1.getY(), Color.red));
 			result.add(new Excel (ex4.getX(), ex4.getY(), Color.red));
 			
 			Matrix Gnx = new Matrix(4,2);
 			double masGnx[] = {Px1,Py1,Px4,Py4,r1.getX(),r1.getY(),r4.getX(),r4.getY()};
 			Gnx.fillingMatrix(masGnx);
			Gnx.showMatrix();
 			
 			Matrix Mn = new Matrix(4,4);
 			double masMn[] = {2,-2,1,1,
 							 -3,3,-2,-1,
 							  0,0,1,0,
 							  1,0,0,0};
 			Mn.fillingMatrix(masMn);
 			
 			Matrix Cx;
 			if(matrixMultiplication(Mn,Gnx) != null)
 			{
 				Cx = matrixMultiplication(Mn,Gnx);
 				
 				double step = getStep(ex1,ex2,ex3,ex4);
 				
 				for(double i=0; i<=1; i+=step)
 				{
 					Matrix T = new Matrix(1,4);
 					/*double masT[] = {Double.parseDouble(formatter.format(Math.pow(i,3)).replace(',', '.')),
 							Double.parseDouble(formatter.format(Math.pow(i,2)).replace(',', '.')),
 							Double.parseDouble(formatter.format(Math.pow(i,1)).replace(',', '.')),
 							1};*/
 					double masT[] = {Math.pow(i,3),Math.pow(i,2),Math.pow(i,1),1};
 					T.fillingMatrix(masT);
 					
 					if(matrixMultiplication(T,Cx) != null)
 					{
 						Matrix Pt;
 						Pt = matrixMultiplication(T,Cx);
 						
 						if(Pt.getColumns() == 1 && Pt.getRows() == 2)
 						{
 							result.add(new Excel((int)Pt.getEl(0, 0),(int)Pt.getEl(0, 1),Color.black));
 						}
 					}
 				}
 				//result.add(ex1);
 				//result.add(ex4);
 				/*result.get(0).setColor(Color.red);
 				result.get(result.size()-1).setColor(Color.red);*/
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
 					C.updateEl(i, j, rezultValue);
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
 		
 		step = (1.0/max_size)/3.0;
 		
 		return step;
 	}
 	
 	public static Point rVector(Excel Ex1,Excel Ex2)
 	{
 		 return new Point(Ex2.getX() - Ex1.getX(), Ex2.getY() - Ex1.getY());	
 	}
 }
