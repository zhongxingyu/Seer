 /*
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an 
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language 
  * governing permissions and limitations under the License.
  */
 package fuzzy.mf;
 
 import java.io.Serializable;
 
 import org.apache.commons.math.util.FastMath;
 
 /**
  * Gaussian Membership Function. Equivalent to Matlab 
  * <a href="http://www.mathworks.com/help/toolbox/fuzzy/gaussmf.html">gaussmf</a> 
  * function. 
  * 
  * @author Bruno P. Kinoshita - http://www.kinoshita.eti.br
  * @since 0.1
  */
 public class GaussianMembershipFunction implements MembershipFunction, Serializable {
 
 	// TODO Revisit this class later, and see if we can use math Gaussian
 	//protected final Gaussian gaussian;
 	
 	/**
      * serialVersionUID declaration.
      */
 	private static final long serialVersionUID = 5531867246630240033L;
 	
 	private final double sigma;
 	private final double c;
 	
 	public GaussianMembershipFunction(double sigma, double c) {
 		this.sigma = sigma;
 		this.c = c;
 	}
 	
 	public Double evaluate(Double x) {
		return FastMath.exp(FastMath.pow((x-c), 2) / (2*FastMath.pow(sigma, 2))); 
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if(obj == null) {
 			return false;
 		}
 		if(obj == this) {
 			return true;
 		}
 		if(!(obj instanceof GaussianMembershipFunction)) {
 			return false;
 		}
 		final GaussianMembershipFunction that = (GaussianMembershipFunction)obj;
 		return this.sigma == that.sigma && this.c == that.c;
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		int hash = "GaussianMembershipFunction".hashCode();
 		hash <<= 2;
 		hash ^= (int)this.sigma;
 		hash <<= 2;
 		hash ^= (int)this.c;
 		return hash;
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "Gaussian Membership Function ["+sigma+" "+c+"]";
 	}
 
 }
