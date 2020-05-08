 package RayTracing;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 public class ColorComputation {
 	
 	public Scene scene;
 	
 	public ColorComputation(Scene scene){
 		this.scene=scene;
 	}
 	
 	public Color getColorOfPixel(int w, int h){
 		Ray ray=getRayFromCamToPixel(w,h);
 		
 		ObjectPrimitive intersected=findClosestIntersection(ray);
 		
 		Color c=getColorByIntersectedRay(intersected, ray, scene.setts.maximumNumberOfRecursions);
 		
 		return c;
 	}
 	
 	private Ray getRayFromCamToPixel(int w, int h){
 		Vector startPoint=scene.cam.position;
 		Vector toPoint=scene.scr.getPixelPosition(w, h);
 		return Ray.getRay(startPoint, toPoint);
 	}
 	
 	private ObjectPrimitive findClosestIntersection(Ray ray){
 		return findClosestIntersection(ray, null);
 	}
 	
 	private ObjectPrimitive findClosestIntersection(Ray ray, ObjectPrimitive ignored){
 		double t, t_min=Double.MAX_VALUE;
 		ObjectPrimitive obj_min_intr=null;
 		for(ObjectPrimitive obj: scene){
 			if(obj==ignored)
 				continue;
 			t=obj.getIntersection(ray);
 			if(t<0)
 				continue;
 			if(t<t_min){
 				t_min=t;
 				obj_min_intr=obj;
 			}
 		}
 		return obj_min_intr;
 	}
 	
 	private Color getColorByIntersectedRay(ObjectPrimitive obj, Ray ray, int recursionsToGo){
 		if(obj==null || recursionsToGo==0)
 			return scene.setts.backgroundColor;
 		Color outputColor, illuminatedColor, reflectionColor, transColor;
 		Material material=obj.getMaterial();
 		double transparency=material.transparencyValue;
 		
 		Vector point=obj.getIntersectionPoint(ray);
 		Vector normal=obj.getNormalForPoint(point);
 		Map<Light, Color> diffuseColorsFromLights=new HashMap<Light, Color>();
 		Map<Light, Color> specularColorsFromLights=new HashMap<Light, Color>();
 		Map<Light, Ray> LRaysForLights=new HashMap<Light, Ray>();
 		for(Light light: scene.lights){
 			Ray L=Ray.getRay(point, light.position);
 			LRaysForLights.put(light, L);
 		}
 		
 		getDiffuseColors(obj, ray, normal, point, diffuseColorsFromLights, LRaysForLights);
 		getSpecularColors(obj, ray, normal, point, specularColorsFromLights, LRaysForLights);
 		reflectionColor=getReflectedColor(obj, ray, normal, point, recursionsToGo);
 		transColor=getTransColor(obj, ray, normal, point, recursionsToGo);
 		
 		illuminatedColor=getIlluminatedFromDiffsAndSpecs_plusShadowing(diffuseColorsFromLights, specularColorsFromLights, point, obj);
 		
 		double IV=-1 * ray.direction.dot(normal);
 		double MI=material.incidence;
 		if(material.isTransparent())
 				transColor=Color.color(transColor.mul((1-MI) + MI*IV));
 		if(material.isReflective())
 				reflectionColor=Color.color(reflectionColor.mul((1-MI)+MI*(1-IV)));
 		
 		//illuminatedColor=diffuseColor.add(specularColor);
 		outputColor=Color.color(transColor.mul(transparency).add(illuminatedColor.mul(1-transparency)).add(reflectionColor));
 		return outputColor;
 	}
 
 	private void getDiffuseColors(ObjectPrimitive obj, Ray ray, Vector N, Vector point,
 			Map<Light, Color> diffuseForLights, Map<Light, Ray> LRays){
 		Material material=obj.getMaterial();
 		
 		Color surfaceDiffuse=material.diffuseColor;
 		Color lightDiffuse;
 		Color colorFromLight;
 		for(Light light: scene.lights){
 			lightDiffuse=light.color;
 			Ray L=LRays.get(light);
 			double cos=N.dot(L.direction);
 			colorFromLight=cos<=0 ? Color.black() : Color.color(surfaceDiffuse.mul(lightDiffuse).mul(cos));
 			diffuseForLights.put(light, colorFromLight);
 		}
 	}
 	
 	private void getSpecularColors(ObjectPrimitive obj, Ray ray, Vector N, Vector point,
 			Map<Light, Color> specularForLights, Map<Light, Ray> LRays){
 		Material material=obj.getMaterial();
 		
 		Color surfaceSpecular=material.specularColor;
 		Color lightSpecular;
 		Color colorFromLight;
 		for(Light light: scene.lights){
 			lightSpecular=Color.color(light.color.mul(light.specularI));
 			Ray L=LRays.get(light);
 			Ray R=Ray.getReflectedRay(N, L.direction, point);
 			double cos=R.direction.dot(ray.direction.mul(-1));
 			colorFromLight=cos<=0 ? Color.black() : Color.color(surfaceSpecular.mul(lightSpecular).mul(Math.pow(cos, material.phongSpecularityCoefficient)));
 			specularForLights.put(light, colorFromLight);
 		}
 	}
 	
 	private Color getTransColor(ObjectPrimitive obj, Ray ray, Vector N, Vector point, int recs) {
 		if(obj.getMaterial().isTransparent())
 		{
 			Ray transRay=new Ray();
 			transRay.setOrigin(point);
 			transRay.setDirection(ray.direction);
 			ObjectPrimitive transObj=findClosestIntersection(transRay, obj);
 			return Color.color(getColorByIntersectedRay(transObj, transRay, recs-1));
 		}
 		else
 			return Color.zeroColor();
 	}
 
 	private Color getReflectedColor(ObjectPrimitive obj, Ray ray, Vector N, Vector point, int recs) {
 		if(obj.getMaterial().isReflective()){
 			Ray R=Ray.getReflectedRay(N, ray.direction.mul(-1), point);
 			ObjectPrimitive refObj=findClosestIntersection(R, obj);
 			return Color.color(getColorByIntersectedRay(refObj, R, recs-1).mul(obj.material.reflectionColor));
 		}
 		else
 			return Color.zeroColor();
 	}
 	
 	private Color getIlluminatedFromDiffsAndSpecs_plusShadowing(Map<Light, Color> diffs, Map<Light, Color> specs, Vector point, ObjectPrimitive obj) {
 		Color illum=Color.zeroColor();
 		for(Light light: scene.lights){
 			Color light_illum=Color.color(diffs.get(light).add(specs.get(light)));
 			double shadowCoeff=1-light.shadowsI;
 			Ray lightToObject=Ray.getRay(light.position, point);
 			double raysPrecent=precentageOfReturnedRays(light, lightToObject, point, obj);
 			Color fromThisLight;
			if(raysPrecent==1)
 				fromThisLight=light_illum;//TODO is this the real formula
 			else
 				fromThisLight=Color.color(light_illum.mul(shadowCoeff*raysPrecent));//TODO is this the real formula*/
 			fromThisLight=Color.color(light_illum.mul(raysPrecent));
 			illum=Color.color(illum.add(fromThisLight));
 		}
 		return illum;
 	}
 
 	private double precentageOfReturnedRays(Light light, Ray lightToObject, Vector objPoint, ObjectPrimitive obj) {
 		double widthOfPlane=light.radius;
 		double iterationWidth=scene.setts.rootNumberOfShadowRays;
 		double stepSize=widthOfPlane/iterationWidth;
 		Vector center=light.position;
 		Vector v=new Vector();
 		Vector u=new Vector();
 		//LinearAlgebra.getPerpendicularPlane(v, u, center, lightToObject.direction);
         if (lightToObject.direction.x == 0)
             u = new Vector(1, 0, 0);
         else
             u = new Vector(-lightToObject.direction.y / lightToObject.direction.x, 1, 0).normalize();
         
         v = lightToObject.direction.cross(u).normalize();
 		Vector corner=center.sub(v.mul(widthOfPlane/2)).sub(u.mul(widthOfPlane/2));
 		double horizontalDistance=0, verticalDistance=0;
 		boolean doesItHit=false;
 		double hittingRays=0;
 		for(int i=0; i<iterationWidth; i++){
 			for(int j=0; j<iterationWidth; j++){
 				Vector lowerLeftCornerOfCell=corner.add(u.mul(horizontalDistance)).add(v.mul(verticalDistance));
 				double randomHstep=random()*stepSize;
 				double randomVstep=random()*stepSize;
 				Vector randomPointInCell=lowerLeftCornerOfCell.add(u.mul(randomHstep)).add(v.mul(randomVstep));
 				Ray toObj=Ray.getRay(randomPointInCell, objPoint);
 				ObjectPrimitive potentiallyShadowingObj=findClosestIntersection(toObj);
 				if(potentiallyShadowingObj!=null){
 					Vector hittingPoint=potentiallyShadowingObj.getIntersectionPoint(toObj);
 					if(hittingPoint.isEqual(objPoint))
 						doesItHit=true;
 				}
				hittingRays+=doesItHit?1:0;//1-light.shadowsI;
 				horizontalDistance+=stepSize;
 				doesItHit=false;
 			}
 			verticalDistance+=stepSize;
 			horizontalDistance=0;
 		}
 		return hittingRays/(iterationWidth*iterationWidth);
 	}
 	
 	private double random(){
 		final Random r=new Random();
 		return r.nextDouble();
 	}
 }
