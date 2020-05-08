 package methods;
 
 public abstract class AbstractMethod {
 	float lambda;
 	float mu;
 	float rho;
 	float n;
 	float k;
 	
 	public abstract float getW();
 	public abstract float getWq();
 	public abstract float getL();
 	public abstract float getLq();
 	public abstract float getPo();
 	public abstract float getPn();
 	public abstract float getRho();
 	
 	public AbstractMethod(){}
 	public float getLambda(){return this.lambda;}
 	public float getMu(){return this.mu;}
 	public float getN(){return this.n;}
 	public float getK(){return this.k;}
 	public void setLambda(float l){this.lambda=l;}
 	public void setRho(float ro){this.rho=ro;}
 	public void setMu(float m){this.mu=m;}
 	public void setN(float n){this.n=n;}
 	public void setK(float n){this.k=n;}
 	
 	public float factorial(float num){
	      if(num==0) return 1;   
 	      else return num*factorial(num-1);  
 	} 
 }
