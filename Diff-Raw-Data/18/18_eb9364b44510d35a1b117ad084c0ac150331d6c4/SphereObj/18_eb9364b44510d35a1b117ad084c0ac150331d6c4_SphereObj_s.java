 package com.github.hiuprocon.pve.obj;
 
 import com.github.hiuprocon.pve.core.*;
 
 public class SphereObj extends PVEObject {
 	Type type;
 	String a3url;
 	double mass;
	double radius;
 	Sphere sphere;
 	public SphereObj() {
 		this(Type.DYNAMIC,1.0,1,"x-res:///res/Sphere.wrl");
 	}
	public SphereObj(Type type,double mass,double radius,String a3url) {
 		this.type = type;
 		this.mass = mass;
 		this.a3url = a3url;
		this.radius = radius;
 		init();
 	}
 
 	@Override
 	protected PVEPart[] createParts() {
		sphere = new Sphere(type,mass,radius/2,a3url);
 		//sphere.setInitLocRot(0,0,0, 0,0,0);
 		return new PVEPart[]{sphere};
 	}
 
 	@Override
 	protected Constraint[] createConstraints() {
 		return new Constraint[0];
 	}
 
 	@Override
 	protected PVEPart getMainPart() {
 		return sphere;
 	}
 }
