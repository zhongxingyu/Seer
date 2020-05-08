 package curveGeneration;
 
 import java.util.ArrayList;
 
 import model.Excel;
 
 public class BSplaynCurve extends ParametricCurve{
 
 	public BSplaynCurve(Excel Ex4, Excel Ex3, Excel Ex2, Excel Ex1) {
 		super(Ex4, Ex3, Ex2, Ex1);
 		// TODO Auto-generated constructor stub
 	}
 	
 	public ArrayList<Excel> Calculation()
 	{
		System.out.println("BSplaynForm");
 		ArrayList<Excel> result = new ArrayList<Excel>();
 		
 		Excel masEx[] = {getEx1(),getEx2(),getEx3(),getEx4()};
 		
 		double masMn[] = {-1,3,-3,1,
 				  3,-6,3,0,
 				  -3,0,3,0,
 				  1,4,1,0};
 		setMasMn(masMn);
 		
 		double factor = 1/6.0;
 		setFactor(factor);
 		
 		for(int i=1;i<masEx.length-2;i++)
 		{
 			double masGnx[] = {masEx[i-1].getX(),masEx[i-1].getY(),
 							   masEx[i].getX(),masEx[i].getY(),
 							   masEx[i+1].getX(),masEx[i+1].getY(),
 							   masEx[i+2].getX(),masEx[i+2].getY()};
 			setMasGnx(masGnx);
 			result.addAll(super.Calculation());
 		}
 		
 		return result;
 	}
 
 }
