 package com.jff.engine3d.model.camera;
 
 import com.jff.engine3d.model.entities.Point3D;
 import com.jff.engine3d.model.entities.SpherePoint3D;
 import com.jff.engine3d.model.scene.SceneObject;
 
 public abstract class AbstractCameraProjection {
 
 
     protected ProjectionType type;
     protected CameraSettings cameraSettings;
     private double minTheta;
     private double maxTheta;
 
 
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
         SpherePoint3D spherePoint3D = SpherePoint3D.fromCartesian(point3D1);
 
 
         double theta = spherePoint3D.theta;
         double phi = spherePoint3D.phi;
         double ro = spherePoint3D.r;
 
 
         double xe;
         double ye;
         double ze;
 
 
         xe = -xw * Math.sin(theta) + yw * Math.cos(theta);
         ye = -xw * Math.cos(phi) * Math.cos(theta) - yw * Math.cos(phi) * Math.sin(theta) + zw * Math.sin(phi);
         ze = -xw * Math.sin(phi) * Math.cos(theta) - yw * Math.sin(phi) * Math.sin(theta) - zw * Math.cos(phi);
 
         ze *= -1;
 
 //
 //        if (Double.compare(theta, Math.PI) == 0) {
 //
 //            xe *= -1;
 //            ye *= -1;
 //
 //
 //        }
 //        if (theta <= 0) {
 //
 //            xe *= -1;
 //            ye *= -1;
 //
 //
 //        }
 
 
         Point3D result = new Point3D(xe, ye, ze);
 //
 //        if(Double.compare(ty, 0) == 0) {
 //            result = result.rotateZ(90);
 //        }
 //
 //
 //
 //        if(theta >= Math.PI /2 || theta < -Math.PI /2) {
 //
 //            result = result.rotateZ(180);
 //        }
 //
 //
 //
 //        if(tx > 0 || ty == 0) {
 //            result.x *= -1;
 //            result.y *= -1;
 //        }
 //
 //        if(ty > 0 || tx == 0) {
 //            result.x *= -1;
 //            result.y *= -1;
 //        }
 
 //        System.out.println("result " + result);
 //        System.out.println("eye " + eye);
 //        System.out.println("look  " + lookAt);
 //        System.out.println("point  " + point3D);
 //        System.out.println("translated " + point3D1);
         System.out.println(spherePoint3D);
 //        System.out.println();
 
 
         int zAngle = 0;
 //        if (tx < 0) {
 //            if (ty < 0) {
 //                if (tz > 0) {
 //
 //                    zAngle = 90;
 //                } else {
 //                    zAngle = 90;
 //                }
 //            } else if (ty > 0) {
 //
 //                if (tz > 0) {
 //
 //                      zAngle = 180;
 //                } else {
 //
 //                      zAngle =180;
 //                }
 //
 //            } else {
 //
 //                if (tz > 0) {
 //
 //                    zAngle = 270;
 //                } else {
 //                    zAngle = 270;
 //                }
 //            }
 //        } else if (tx > 0) {
 //            if (ty < 0) {
 //                if (tz > 0) {
 //                    zAngle = 90;
 //                } else {
 //                    zAngle = 90;
 //
 //                }
 //            } else if (ty > 0) {
 //
 //                if (tz > 0) {
 //
 //                    zAngle = 180;
 //                } else {
 //                    zAngle = 180;
 //                }
 //
 //            } else {
 //
 //                if (tz > 0) {
 //                    zAngle = 90;
 //
 //                } else {
 //                    zAngle = 90;
 //
 //                }
 //            }
 //        } else {
 //            if (ty < 0) {
 //                if (tz > 0) {
 //
 //                } else {
 //
 //                }
 //            } else if (ty > 0) {
 //
 //                if (tz > 0) {
 //                    zAngle = 180;
 //                } else {
 //                    zAngle = 180;
 //                }
 //
 //            } else {
 //
 //                if (tz > 0) {
 //
 //                    zAngle = 90;
 //                } else {
 //                    zAngle = 90;
 //                }
 //            }
 //        }
 
 //        double pi = Math.PI;
 //        if (theta > -pi && theta < -pi / 2) {
 //
//                zAngle = 180;
 //
 //        } else if (theta > -pi / 2 && theta < 0) {
 //
 //            zAngle = 270;
 //
 //        } else if (theta > 0 && theta < pi / 2) {
 //
//
 //        } else if (theta > pi / 2 && theta < pi) {
 //
//            zAngle = 90;
 //
 //        } else if (theta > pi) {
 //
 //        } else if (theta == -pi) {
 //
 //        } else if (theta == -pi / 2) {
 //            zAngle = 270;
 //        } else if (theta == 0) {
 //
 //        } else if (theta == pi / 2) {
 //            zAngle = 90;
 //        } else if (theta == pi) {
 //            zAngle = 180;
 //        }

        result = result.rotateZ(zAngle);
 
 
 //        minTheta = Math.min(minTheta, theta);
 //        maxTheta = Math.max(maxTheta, theta);
 //        System.out.println(minTheta);
 //        System.out.println(maxTheta);
 //        System.out.println();
         return result;
     }
 
 }
