 
 /**
  * This class represents a Triangle.
  */
 public class Triangle implements Shape
 {
     public Double3D p0;
     public Double3D p1;
     public Double3D p2;
     public PMesh theObj;
 
 	public Triangle()
 	{
 		p0 = null;
 		p1 = null;
 		p2 = null;
 		theObj = null;
 	} // end constructor
 	
 	public Triangle(Double3D v0, Double3D v1, Double3D v2, PMesh mesh)
 	{
 		p0 = v0;
 		p1 = v1;
 		p2 = v2;
 		theObj = mesh;
 	}
 	
 	public Triangle(Triangle from)
 	{
 		p0 = from.p0;
 		p1 = from.p1;
 		p2 = from.p2;
 		theObj = from.theObj;
 	} // end constructor
 
 	public String toString(){
 		String rtn = new String("Triangle - Vertex 1: " + p0.toString() +
 								" Vertex 2: " + p1.toString() +
 								" Vertex 3: " + p2.toString());
 		return rtn;
 	} // end method toString
 
 	@Override
 	public boolean hit(Ray r, double tMin, double tMax, double time,
 			HitRecord record) 
 	{
 		double tval;
 	    double A = p0.x - p1.x;
 	    double B = p0.y - p1.y;
 	    double C = p0.z - p1.z;
 	    
 	    double D = p0.x - p2.x;
 	    double E = p0.y - p2.y;
 	    double F = p0.z - p2.z;
 	    
 	    double G = r.dir.x;
 	    double H = r.dir.y;
 	    double I = r.dir.z;
 	    
 	    double J = p0.x - r.origin.x;
 	    double K = p0.y - r.origin.y;
 	    double L = p0.z - r.origin.z;
 	    
 	    double EIHF = E * I - H * F;
 	    double GFDI = G * F - D * I;
 	    double DHEG = D * H - E * G;
 	    
 	    double denom = (A * EIHF + B * GFDI + C * DHEG);
 	    
 	    double beta = (J * EIHF + K * GFDI + L * DHEG) / denom;
 	    
 	    if(beta <= 0.0f || beta >= 1.0f) 
 	        return false;
 	    
 	    double AKJB = A * K - J * B;
 	    double JCAL = J * C - A * L;
 	    double BLKC = B * L - K * C;
 
 	    double gamma = (I * AKJB + H * JCAL + G * BLKC) / denom;
 	    
 	    if(gamma <= 0.0f || beta + gamma >= 1.0f) 
 	        return false;
 	    
 	    tval = -(F * AKJB + E * JCAL + D * BLKC) / denom;
 	    
 	    if(tval >= tMin && tval <= tMax){
 	        record.t = tval;
 	        record.hitP = r.pointAtParameter(tval);
 	        //Normals are pre-calculated
	        Double3D v1 = p1.minus(p0);
	        Double3D v2 = p2.minus(p0);
 	        
	        record.normal = v1.cross(v2).getUnit();
 	        
 	        return true;
 	    }
 	    return false;
 	}
 
 	@Override
 	public boolean shadowHit(Ray r, double tMin, double tMax, double time) {
 		double tval;
 	    double A = p0.x - p1.x;
 	    double B = p0.y - p1.y;
 	    double C = p0.z - p1.z;
 	    
 	    double D = p0.x - p2.x;
 	    double E = p0.y - p2.y;
 	    double F = p0.z - p2.z;
 	    
 	    double G = r.dir.x;
 	    double H = r.dir.y;
 	    double I = r.dir.z;
 	    
 	    double J = p0.x - r.origin.x;
 	    double K = p0.y - r.origin.y;
 	    double L = p0.z - r.origin.z;
 	    
 	    double EIHF = E * I - H * F;
 	    double GFDI = G * F - D * I;
 	    double DHEG = D * H - E * G;
 	    
 	    double denom = (A * EIHF + B * GFDI + C * DHEG);
 	    
 	    double beta = (J * EIHF + K * GFDI + L * DHEG) / denom;
 	    
 	    if(beta <= 0.0f || beta >= 1.0f) 
 	        return false;
 	    
 	    double AKJB = A * K - J * B;
 	    double JCAL = J * C - A * L;
 	    double BLKC = B * L - K * C;
 
 	    double gamma = (I * AKJB + H * JCAL + G * BLKC) / denom;
 	    
 	    if(gamma <= 0.0f || beta + gamma >= 1.0f) 
 	        return false;
 	    
 	    tval = -(F * AKJB + E * JCAL + D * BLKC) / denom;
 	    
 	    return (tval >= tMin && tval <= tMax);
 	}
 
 	public static boolean hit(Double3D p0, Double3D p1, Double3D p2, Ray r, double tMin, double tMax, double time,
 			HitRecord record) 
 	{
 		double tval;
 	    double A = p0.x - p1.x;
 	    double B = p0.y - p1.y;
 	    double C = p0.z - p1.z;
 	    
 	    double D = p0.x - p2.x;
 	    double E = p0.y - p2.y;
 	    double F = p0.z - p2.z;
 	    
 	    double G = r.dir.x;
 	    double H = r.dir.y;
 	    double I = r.dir.z;
 	    
 	    double J = p0.x - r.origin.x;
 	    double K = p0.y - r.origin.y;
 	    double L = p0.z - r.origin.z;
 	    
 	    double EIHF = E * I - H * F;
 	    double GFDI = G * F - D * I;
 	    double DHEG = D * H - E * G;
 	    
 	    double denom = (A * EIHF + B * GFDI + C * DHEG);
 	    
 	    double beta = (J * EIHF + K * GFDI + L * DHEG) / denom;
 	    
 	    if(beta <= 0.0f || beta >= 1.0f) 
 	        return false;
 	    
 	    double AKJB = A * K - J * B;
 	    double JCAL = J * C - A * L;
 	    double BLKC = B * L - K * C;
 
 	    double gamma = (I * AKJB + H * JCAL + G * BLKC) / denom;
 	    
 	    if(gamma <= 0.0f || beta + gamma >= 1.0f) 
 	        return false;
 	    
 	    tval = -(F * AKJB + E * JCAL + D * BLKC) / denom;
 	    
 	    if(tval >= tMin && tval <= tMax){
 	        record.t = tval;
 	        record.hitP = r.pointAtParameter(tval);
 	        //record.normal = p1.minus(p0).cross(p2.minus(p0)).getUnit();
 	        
 	        return true;
 	    }
 	    return false;
 	}
 	
 	public static boolean shadowHit(Double3D p0, Double3D p1, Double3D p2, Ray r, double tMin, double tMax, double time) {
 		double tval;
 	    double A = p0.x - p1.x;
 	    double B = p0.y - p1.y;
 	    double C = p0.z - p1.z;
 	    
 	    double D = p0.x - p2.x;
 	    double E = p0.y - p2.y;
 	    double F = p0.z - p2.z;
 	    
 	    double G = r.dir.x;
 	    double H = r.dir.y;
 	    double I = r.dir.z;
 	    
 	    double J = p0.x - r.origin.x;
 	    double K = p0.y - r.origin.y;
 	    double L = p0.z - r.origin.z;
 	    
 	    double EIHF = E * I - H * F;
 	    double GFDI = G * F - D * I;
 	    double DHEG = D * H - E * G;
 	    
 	    double denom = (A * EIHF + B * GFDI + C * DHEG);
 	    
 	    double beta = (J * EIHF + K * GFDI + L * DHEG) / denom;
 	    
 	    if(beta <= 0.0f || beta >= 1.0f) 
 	        return false;
 	    
 	    double AKJB = A * K - J * B;
 	    double JCAL = J * C - A * L;
 	    double BLKC = B * L - K * C;
 
 	    double gamma = (I * AKJB + H * JCAL + G * BLKC) / denom;
 	    
 	    if(gamma <= 0.0f || beta + gamma >= 1.0f) 
 	        return false;
 	    
 	    tval = -(F * AKJB + E * JCAL + D * BLKC) / denom;
 	    
 	    return (tval >= tMin && tval <= tMax);
 	}
 
 } // end class Triangle
 
