 package com.jff.engine3d.model.camera;
 
 import com.jff.engine3d.model.entities.Point3D;
 import com.jff.engine3d.model.entities.SpherePoint3D;
 import com.jff.engine3d.model.scene.SceneObject;
 
 public abstract class AbstractCameraProjection {
 
 
     protected ProjectionType type;
     protected CameraSettings cameraSettings;
 
 
     public ProjectionType getType() {
         return type;
     }
 
     protected AbstractCameraProjection(ProjectionType type, CameraSettings cameraSettings) {
         this.type = type;
         this.cameraSettings = cameraSettings;
     }
 
     public static AbstractCameraProjection createProjection(ProjectionType projectionType, CameraSettings cameraSettings) {
 
         AbstractCameraProjection projection = null;
         switch (projectionType) {
 
             case PARALLEL:
                 projection = new ParallelCameraProjection(cameraSettings);
                 break;
             case CENTRAL:
                 projection = new CentralCameraProjection(cameraSettings);
                 break;
         }
 
         return projection;
     }
 
     public Point3D convertToCameraCoordinateSystem(Point3D point3D) {
 
         Point3D newPoint3D = new Point3D(point3D.x, point3D.y, point3D.z);
 
 
         Point3D to = cameraSettings.getRealToCoordinates();
         Point3D from = cameraSettings.getRealFromCoordinates();
 
         newPoint3D = convertWorldCoordinatesToCameraCoordinates(from, to, point3D);
 
 
         return newPoint3D;
     }
 
 
     public abstract float distanceFromCameraToPoint(Point3D point3D);
 
     public abstract boolean isObjectInCamera(SceneObject object);
 
 
     public Point3D convertWorldCoordinatesToCameraCoordinates(Point3D eye, Point3D lookAt, Point3D point3D) {
 
         double xe;
         double ye;
         double ze;
 
         double xw = point3D.x;
         double yw = point3D.y;
         double zw = point3D.z;
 
 
         xw -= eye.x;
         yw -= eye.y;
         zw -= eye.z;
 
         double tx = lookAt.x - eye.x;
         double ty = lookAt.y - eye.y;
         double tz = lookAt.z - eye.z;
 
         Point3D point3D1 = new Point3D(tx, ty, tz);
         System.out.println("translated " + point3D1);
         SpherePoint3D spherePoint3D = SpherePoint3D.fromCartesian(point3D1);
         System.out.println(spherePoint3D);
 
         double theta = spherePoint3D.theta;
         double phi = spherePoint3D.phi;
         double ro = spherePoint3D.r;
 
         xe = -xw * Math.sin(theta) + yw * Math.cos(theta);
         ye = -xw * Math.cos(phi) * Math.cos(theta) - yw * Math.cos(phi) * Math.sin(theta) + zw * Math.sin(phi);
         ze = -xw * Math.sin(phi) * Math.cos(theta) - yw * Math.sin(phi) * Math.sin(theta) - zw * Math.cos(phi);
 
         ze *= -1;
//
//        if(Double.compare(theta, Math.PI) >= 0 || theta <0) {
//
//            xe *=-1;
//           ye *=-1;
//        }
 
 
         return new Point3D(xe, ye, ze);
     }
 
 }
