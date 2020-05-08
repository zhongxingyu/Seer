 package cat.atridas.antagonista;
 
 import javax.vecmath.AxisAngle4f;
 import javax.vecmath.Matrix3f;
 import javax.vecmath.Matrix4f;
 import javax.vecmath.Quat4f;
 import javax.vecmath.Tuple3f;
 import javax.vecmath.Vector3f;
 
 /**
  * <p>
  * This class encapsulates information about the front, up, left and right vectors in the engine.
  * Use the vectors here created and never change them. Also, whenever you need to compute the Euler
  * angles, use the functions in this class so everything works consistently.
  * </p>
  * <p>
  * We will do a Yaw -> Pitch -> Roll rotation (in that order) and Yaw will be arround the
  * Z vector (a positive Yaw means turning to the left), Pitch arround the -X vector (a 
  * positive angle means to turn your head up) and Roll arround the -Y angle (a positive 
  * Roll means to do a barrell roll to your right).
  * </p>
  * <p>
  * In the Blender exporter we should take the ZXY rotation and negate both Pitch and Roll.
  * </p>
  * 
  * @author Isaac 'Atridas' Serrano Guasch
  * @since 0.1
  *
  */
 public class Conventions {
 
   /**
    * Front vector, taken from Blender (0,-1,0).
    * @since 0.1
    */
   public static final Vector3f FRONT_VECTOR = new Vector3f(0, -1, 0);
   /**
    * Back vector, taken from Blender (0,1,0).
    * @since 0.2
    */
   public static final Vector3f BACK_VECTOR = new Vector3f(0, 1, 0);
   /**
    * Up vector, taken from Blender (0,0,1).
    * @since 0.1
    */
   public static final Vector3f UP_VECTOR = new Vector3f(0, 0, 1);
   /**
    * Down vector, taken from Blender (0,0,1).
    * @since 0.2
    */
   public static final Vector3f DOWN_VECTOR = new Vector3f(0, 0, -1);
   /**
    * Right vector, taken from Blender (-1,0,0).
    * @since 0.1
    */
   public static final Vector3f RIGHT_VECTOR = new Vector3f(-1, 0, 0); 
   /**
    * Left vector, taken from Blender (1,0,0).
    * @since 0.1
    */
   public static final Vector3f LEFT_VECTOR = new Vector3f(1, 0, 0); 
   
 
   private static final ThreadLocal<Quat4f> quaternion = new ThreadLocal<>();
 
   
   /**
    * Transforms a Quaternion into Euler Angles.
    * 
    * @since 0.1
    * @param _quaternion Rotation entered as a unit quaternion.
    * @param euler_ returns the angles in Yaw (x) Pitch (y) Roll (z) convention.
    */
   public static void quaternionToEulerAngles(Quat4f _quaternion, Tuple3f euler_) {
        
     double q0 = _quaternion.w;
     double q1 = _quaternion.x;
     double q2 = _quaternion.y;
     double q3 = _quaternion.z;
 
     double q03 = q0 * q3;
     double q12 = q1 * q2;
     double q33 = q3 * q3;
     double q22 = q2 * q2;
     double q23 = q2 * q3;
     double q01 = q0 * q1;
     double q02 = q0 * q2;
     double q13 = q1 * q3;
     double q11 = q1 * q1;
     
     double q00 = q0 * q0;
     
    double abs = Math.abs(1 - q00 - q11 - q22 - q33); // que estigui normalitzat per favor 
    assert abs  < 0.0001;
 
     float yaw   = (float) Math.atan2( 2 * (q03 + q12), 1 - 2 * (q33 + q11));
     float pitch = (float) Math.asin ( 2 * (q23 - q01));
     float roll  = (float) Math.atan2(-2 * (q02 + q13), 1 - 2 * (q11 + q22));
 
     euler_.x = yaw;
     euler_.y = pitch;
     euler_.z = roll;
   }
   
   
   /**
    * Transform a rotation matrix into Euler Angles.
    * 
    * @since 0.1
    * @param _matrix Rotation Matrix.
    * @param euler_ returns the angles in Yaw (x) Pitch (y) Roll (z) convention.
    */
   public static void matrixToEulerAngles(Matrix3f _matrix, Tuple3f euler_) {
     Quat4f q = quaternion.get();
     q.set(_matrix);
     quaternionToEulerAngles(q, euler_);
   }
   
 
   /**
    * Transform a rotation matrix into Euler Angles.
    * 
    * @since 0.1
    * @param _matrix Rotation Matrix.
    * @param euler_ returns the angles in Yaw (x) Pitch (y) Roll (z) convention.
    */
   public static void matrixToEulerAngles(Matrix4f _matrix, Tuple3f euler_) {
     Quat4f q = quaternion.get();
     q.set(_matrix);
     quaternionToEulerAngles(q, euler_);
   }
   
 
   /**
    * Transform an axis angle rotation into Euler Angles.
    * 
    * @since 0.1
    * @param _aa Axis angle rotation.
    * @param euler_ returns the angles in Yaw (x) Pitch (y) Roll (z) convention.
    */
   public static void axisAngleToEulerAngles(AxisAngle4f _aa, Tuple3f euler_) {
     Quat4f q = quaternion.get();
     q.set(_aa);
     quaternionToEulerAngles(q, euler_);
   }
   
 
   /**
    * Transforms an Euler rotation to Quaternion anotation.
    * 
    * @since 0.1
    * @param _euler Euler rotation to translate, in Yaw (x) Pitch (y) Roll (z) convention.
    * @param quaternion_ output.
    */
   public static void eulerAnglesToQuaternion(Tuple3f _euler, Quat4f quaternion_) {
     
     float yaw   = _euler.x;
     float pitch = _euler.y;
     float roll  = _euler.z;
     
     float cosY = (float) Math.cos(yaw   / 2);
     float cosP = (float) Math.cos(pitch / 2);
     float cosR = (float) Math.cos(roll  / 2);
 
     float sinY = (float) Math.sin(yaw   / 2);
     float sinP = (float) Math.sin(pitch / 2);
     float sinR = (float) Math.sin(roll  / 2);
 
     quaternion_.w = + (cosY * cosP * cosR) + (sinY * sinP * sinR);
     quaternion_.x = - (cosY * sinP * cosR) - (sinY * cosP * sinR);
     quaternion_.y = - (cosY * cosP * sinR) + (sinY * sinP * cosR);
     quaternion_.z = + (sinY * cosP * cosR) - (cosY * sinP * sinR);
   }
   
 
   /**
    * Transforms an Euler rotation to Matrix anotation.
    * 
    * @since 0.1
    * @param _euler Euler rotation to translate, in Yaw (x) Pitch (y) Roll (z) convention.
    * @param matrix_ output.
    */
   public static void eulerAnglesToMatrix(Tuple3f _euler, Matrix3f matrix_) {
     Quat4f q = quaternion.get();
     quaternionToEulerAngles(q, _euler);
     matrix_.set(q);
   }
   
 
   /**
    * Transforms an Euler rotation to Matrix anotation.
    * 
    * @since 0.1
    * @param _euler Euler rotation to translate, in Yaw (x) Pitch (y) Roll (z) convention.
    * @param matrix_ output.
    */
   public static void eulerAnglesToMatrix(Tuple3f _euler, Matrix4f matrix_) {
     Quat4f q = quaternion.get();
     quaternionToEulerAngles(q, _euler);
     matrix_.setRotation(q);
   }
   
 
   /**
    * Transforms an Euler rotation to Axis angle anotation.
    * 
    * @since 0.1
    * @param _euler Euler rotation to translate, in Yaw (x) Pitch (y) Roll (z) convention.
    * @param aa_ output.
    */
   public static void eulerAnglesToAxisAngle(Tuple3f _euler, AxisAngle4f aa_) {
     Quat4f q = quaternion.get();
     quaternionToEulerAngles(q, _euler);
     aa_.set(q);
   }
 }
