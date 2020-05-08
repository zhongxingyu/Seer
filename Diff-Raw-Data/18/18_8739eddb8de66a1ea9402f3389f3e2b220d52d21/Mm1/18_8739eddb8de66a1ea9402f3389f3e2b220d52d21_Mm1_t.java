 package methods;
 
 public class Mm1 extends AbstractMethod {
 
	public void setValues(float lambda, float mu, float n){
 		setLambda(lambda);
 		setMu(mu);
 		setN(n);
 	}
 
 	@Override
 	public float getW() {
 		return 1/(getMu()-getLambda());
 	}
 
 	@Override
 	public float getWq() {
 		return getLambda()/(getMu()*(getMu()-getLambda()));
 	}
 
 	@Override
 	public float getL() {
 		return getLambda()/(getMu()-getLambda());
 	}
 
 	@Override
 	public float getLq() {
 		return getLambda()*getWq();
 	}
 
 	@Override
 	public float getPo() {
 		if(getMu()!=0&&getLambda()!=0){
 			return (getMu()-getLambda())/getMu();
 		}
 		return 1-getRho();
 	}
 
 	@Override
 	public float getPn() {
 		if(getMu()!=0&&getLambda()!=0){
 			float part1,part2;
 			part1=(float) Math.pow((getLambda()/getMu()),getN());
 			part2=(getMu()-getLambda())/getMu();
 			return part1*part2;
 		}
 		return ((float) Math.pow(getRho(), getN()))*(1-getRho());
 	}
 
 	@Override
 	public float getRho() {
 		if(getMu()!=0&&getLambda()!=0){
 			return getLambda()/getMu();
 		}
 		return this.rho;
 	}
 
 }
