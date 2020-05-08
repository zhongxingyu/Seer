 package curveGeneration;
 
 import java.awt.Point;
 import java.util.ArrayList;
 
 import model.Excel;
 
 public class ErmitCurve extends ParametricCurve
 {
 	public ErmitCurve(Excel ex4, Excel ex3, Excel ex2, Excel ex1) 
 	{
 		super(ex4, ex3, ex2, ex1);
 		// TODO Auto-generated constructor stub
 	}
 	
 	public ArrayList<Excel> Calculation()
 	{
		//System.out.println("ermitForm");
 		double masMn[] = {2,-2,1,1,
 				 -3,3,-2,-1,
 				  0,0,1,0,
 				  1,0,0,0};
 		setMasMn(masMn);
 		
 		Point r1 = rVector(getEx1(), getEx2());
         Point r4 = rVector(getEx3(), getEx4());
 		double masGnx[] = {getEx1().getX(),getEx1().getY(),getEx4().getX(),getEx4().getY(),r1.getX(),r1.getY(),r4.getX(),r4.getY()};
 		setMasGnx(masGnx);
 		
 		double factor = 1.0;
 		setFactor(factor);
 		
 		return super.Calculation();
 	}
 }
