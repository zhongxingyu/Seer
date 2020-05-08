 package curveGeneration;
 
 import java.util.ArrayList;
 
 import model.Excel;
 
 public class BesieCurve extends ParametricCurve{
 
 	public BesieCurve(Excel ex4, Excel ex3, Excel ex2, Excel ex1) 
 	{
 		super(ex4, ex3, ex2, ex1);
 		// TODO Auto-generated constructor stub
 	}
 	
 	public ArrayList<Excel> Calculation()
 	{
		System.out.println("besieForm");
 		double masMn[] = {-1,3,-3,1,
 				 3,-6,3,0,
 				  -3,3,0,0,
 				  1,0,0,0};
 		setMasMn(masMn);
 
 		double masGnx[] = {getEx1().getX(),getEx1().getY(),getEx2().getX(),getEx2().getY(),
 				getEx3().getX(),getEx3().getY(),getEx4().getX(),getEx4().getY()};
 		setMasGnx(masGnx);
 		
 		double factor = 1.0;
 		setFactor(factor);
 		
 		return super.Calculation();
 	}
 }
