 /*-
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 package uk.ac.diamond.scisoft.analysis.rcp.pixelinfoutils;
 
 /*
  * creating a dependency to vecmath package from the plotting tools plugin was throwing a linkage error
  * 
  * This class has been created here as a workaround since the current plugin is dependant
  * on gda.libs that holds the vecmath package
  * */
 
 import javax.vecmath.Vector3d;
 import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;
 
 public class Vector3dutil {
 
 	Vector3d q = null;
 
 	public Vector3dutil(QSpace qSpace, double x, double y){
 		q = qSpace.qFromPixelPosition(x, y);
 	}
 
 	public Vector3d getQ(QSpace qSpace, double x, double y ){
 
 		return qSpace.qFromPixelPosition(x, y);
 	}
 	
 	public Object getQMask(QSpace qSpace, double x, double y ){
 		
 		return (qSpace.qFromPixelPosition(x, y) == null) ? null : new Object();
 	}
 	
 	public double getQx(){
 
 		return q.x;
 	}
 
 	public double getQy(){
 
 		return q.y;
 	}
 	
 	public double getQz(){
 
 		return q.z;
 	}
 	
 	public double getQlength(){
 
 		return q.length();
 	}
 	
	public double getQScaterringAngle(QSpace qSpace){
 		return qSpace.scatteringAngle(q);
 	}
 }
