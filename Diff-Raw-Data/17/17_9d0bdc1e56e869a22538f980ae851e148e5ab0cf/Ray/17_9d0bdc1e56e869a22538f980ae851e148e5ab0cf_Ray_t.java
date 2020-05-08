 package cabbagegl;
 import java.awt.Color;
 import java.util.*;
 import java.util.concurrent.atomic.AtomicInteger;
 
 public class Ray {
    public static AtomicInteger nrays = new AtomicInteger();
 
     private Vector3 base;
     private Vector3 dir;
 
     // Holds data about all of the media we've passed through
     // If we travel through the front face of something, we're going into
     // whatever it is made of, and we can pop it from the stack when we go
     // through  a bacck face
     private Stack<HitPoint> media;
 
     public Ray(Vector3 ibase, Vector3 idir) {
         nrays.incrementAndGet();
         base = ibase;
         dir = idir;
 
         media = new Stack<HitPoint>();
     }
 
     private Ray(Vector3 ibase, Vector3 idir, Stack<HitPoint> imedia) {
        nrays.incrementAndGet();
        base = ibase;
        dir = idir;
        media = imedia;
     }
 
     public Vector3 getBase() {
         return base;
     }
 
     public Vector3 getDir() {
         return dir;
     }
 
     public double getCurrMediaIOR() {
        double currIOR = 1.000293;
 
        // Fetches the index of refraction of the current media
        if (!media.isEmpty()) {
           // We're in air, return the IOR of air
           HitPoint hp = media.peek();
           Material mat = hp.getHitShape().materialPropsAt(hp.getHitpoint(), 
                 hp.getSideHit());
           currIOR = mat.getIndexOfRefraction();
        }
        return currIOR;
     }
 
     public Vector3 trace(Scene s, double mindist, double maxdist, int mxdpth) {
        return trace(s, mindist, maxdist, 0, 0, mxdpth, null, null);
     }
 
     // Compute color of ray fired through scene. ignore objects that are hit
     // that are closer to the viewer than mindist or further than maxdist
     private Vector3 trace(Scene s, double mindist, double maxdist, double sofar,
          int depth, int maxdepth, Shape ignore, FaceSide fIgnore) {
        Vector3 retColor = Vector3.ZERO;
 
        // Find the closest hitpoint
        HitData allHitpoints = getAllHitpoints(s, mindist, maxdist);
        HitPoint closest = null;
        if (!allHitpoints.isEmpty()) {
          if (fIgnore == null) {
             closest = findClosestHPointsExclude(allHitpoints, ignore);
          } else {
             closest = findClosestHPointsExcludeFace(allHitpoints, ignore,
                fIgnore);
          }
        }
        if (closest != null) {
          sofar += closest.getDistTo();
 
          Vector3 hitPos = closest.getHitpoint();
          Shape hitShape = closest.getHitShape();
 
          Material mat = hitShape.getMaterial();
 
          Vector3 matC = mat.getColor();
          Vector3 ambient = s.ambient;
 
          // Compute ambient contribution
          retColor = matC.cmul(ambient);
 
          // Compute diffuse contribution
          Vector3 illum = computeIllumination(s, closest);
          retColor = retColor.sum(illum);
 
          // Scale the resulting color using the dist travelled so far
          retColor = retColor.scale(1.0/sofar);
 
          
          Vector3 matSpec = mat.getSpecular();
          if (!(matSpec.equals(Vector3.ZERO)) && depth < maxdepth) {
             depth++;
 
             // Calculate reflection vector
             Vector3 u = dir.normalize();
             Vector3 n = closest.getNormal().normalize();
             Vector3 refdir = u.reflect(n);
 
             // Get the color of reflected objects
             Ray refRay = new Ray(hitPos, refdir);
             Vector3 refColor = refRay.trace(s, 0, maxdist, sofar, depth,
                maxdepth, hitShape, null);
             
             // TODO compute strength of reflection with Fresnell equations
             Vector3 specular = matSpec.cmul(refColor);
             retColor = retColor.sum(specular);
          }
 
          double alpha = mat.getAlpha();
          if(alpha > 0 && depth < maxdepth) {
             // Compute color from transmission
             Vector3 u = dir.normalize();
             Vector3 n = closest.getNormal().normalize();
 
             // Assume we're entering into a new material
             double n_ior = mat.getIndexOfRefraction();
             double c_ior = getCurrMediaIOR();
 
             FaceSide sideHit = closest.getSideHit();
             // Check to see if that isn't the case
             if (sideHit == FaceSide.BACK) {
                // now we're actually going the other way
                if (!media.isEmpty())
                   media.pop();
                n_ior = getCurrMediaIOR();
             }
 
             // Compute the transmission direction
             Vector3 refrdir = u.refract(n, c_ior, n_ior).normalize();
             boolean reflected = u.totalInternalReflection(n, c_ior, n_ior);
 
             // Make sure we don't hit the same point on recursive tracing
             FaceSide toIgnore = sideHit;
             if (reflected) {
                toIgnore = toIgnore.not();
             }
             
             // We need to find out what medium we are in now!
             if (!reflected) {
                // If no total internal reflection took place, we're in a new
                // medium
                media.push(closest);
             }
 
             Stack<HitPoint> nstack = (Stack<HitPoint>) (media.clone());
             Ray refrRay = new Ray(hitPos, refrdir, nstack);
 
             Vector3 refrColor = refrRay.trace(s, 0, maxdepth, sofar, depth,
                maxdepth, hitShape, toIgnore);
 
             // Compute strength with fresnell equations
             // Right now we'll just use "alpha" and set it to 0
             alpha = 1.0;
             refrColor = matSpec.cmul(refrColor.scale(alpha));
             retColor = retColor.sum(refrColor.scale(alpha));
          }
        }
 
        retColor = retColor.clamp(0.0, 1.0);
        return retColor;
     }
 
 
     public HitData getAllHitpoints(Scene s, double mindist, double maxdist) {
        HitData allhit = new HitData();
        // go through the object list and see if we hit anything
        for (Shape i : s.renderables) {
           HitData data = i.hitBy(this);
           if (data != null)
              for (HitPoint j : data.getHitpoints()) {
                 double dist = j.getDistTo();
                 // Ignore hitpoints that aren't in the view volume
                 if (dist > mindist && dist < maxdist)
                    allhit.addHitpoint(j);
              }
        }
        return allhit;
     }
 
     public HitPoint findClosestHitpoint(HitData hitpoints) {
        return findClosestHPointsExclude(hitpoints, null);
     }
 
     public HitPoint findClosestHPointsExclude(HitData hitpoints, Shape ex) {
        HitPoint ret = null;
        if (!hitpoints.isEmpty()) {
           hitpoints.sort();
           for (HitPoint i : hitpoints.getHitpoints()) {
              if (i.getHitShape() == ex)
                 continue;
              else {
                 ret = i;
                 break;
              }
           }
        }
        return ret;
     }
 
 
     public HitPoint findClosestHPointsExcludeFace(HitData hitpoints, Shape ex,
          FaceSide face) {
        HitPoint ret = null;
        if (!hitpoints.isEmpty()) {
           hitpoints.sort();
           for (HitPoint i : hitpoints.getHitpoints()) {
              if (i.getHitShape() == ex && i.getSideHit() == face)
                 continue;
              else {
                 ret = i;
                 break;
              }
           }
        }
        return ret;
     }
 
     private Vector3 computeIllumination(Scene s, HitPoint hp) {
        Vector3 illumAdded = Vector3.ZERO;
        for (Light l : s.lights)
           illumAdded = illumAdded.sum(l.computeIllumination(s, hp));
        return illumAdded;
     }
 
 
     public Vector3 cellShadedTrace(Scene s, double mindist, double maxdist) {
        return ctrace(s, mindist, maxdist, 0);
     }
 
     // Compute color of ray fired through scene. ignore objects that are hit
     // that are closer to the viewer than mindist or further than maxdist
     private Vector3 ctrace(Scene s, double mindist, double maxdist, double sofar) {
        Vector3 retColor = Vector3.ZERO;
 
        // Find the closest hitpoint
        HitData allHitpoints = getAllHitpoints(s, mindist, maxdist);
        HitPoint closest = null;
        if (!allHitpoints.isEmpty()) {
          closest = findClosestHPointsExclude(allHitpoints, null);
        }
        if (closest != null) {
          sofar += closest.getDistTo();
 
          Vector3 hitPos = closest.getHitpoint();
          Shape hitShape = closest.getHitShape();
 
          Material mat = hitShape.getMaterial();
 
          Vector3 matC = mat.getColor();
          Vector3 ambient = s.ambient;
 
          // Compute ambient contribution
          retColor = matC.cmul(ambient);
 
          // Compute diffuse contribution
          // Lighting is weird in this case
          Vector3 totalDiff = Vector3.ZERO;
          double diffFracs = 0;
          Vector3 totalSpec = Vector3.ZERO;
          Double distToLight;
          double attenuation;
          for (Light i : s.lights) {
             if ((distToLight = i.distFromLight(s, closest)) != null) {
                attenuation = i.computeAttenuation(s, closest);
                diffFracs += i.diffScaleAtPoint(closest) * attenuation;
                Vector3 ls = i.computeSpecular(s, closest).scale(attenuation);
                totalSpec = totalSpec.sum(ls);
             }
          }
 
          if (diffFracs < 0) diffFracs = 0;
          if (diffFracs > 1) diffFracs = 1;
 
          double grayVals[] = {0, .5, 1.0};
 
          int g_ndx = (int) (grayVals.length * diffFracs);
          if (g_ndx == grayVals.length) g_ndx--;
          double g_val = grayVals[g_ndx];
          Vector3 gray = new Vector3(g_val, g_val, g_val);
 
          Vector3 diffIllum = mat.getDiffuse().cmul(gray);
          retColor = retColor.sum(diffIllum);
 
          // Scale the resulting color using the dist travelled so far
          retColor = retColor.scale(1.0/sofar);
        }
 
        retColor = retColor.clamp(0.0, 1.0);
        return retColor;
     }
 }
