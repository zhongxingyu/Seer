 package fi.hbp.angr;
 
 
 /**
  * A PID controller class.
  */
 public class PID {
    private float integral = 0;
    private float previous_error = 0;
     private float output;
 
     private final float kp;
     private final float ki;
     private final float kd;
 
     /**
      * Constructor for a new PID controller object.
      * @param kp proportional gain.
      * @param ki integral gain.
      * @param kd derivative gain.
      */
     public PID(float kp, float ki, float kd) {
         this.kp = kp;
         this.ki = ki;
         this.kd = kd;
     }
 
     /**
      * Reset output of the PID controller to a given output value.
      * @param output
      */
     public void reset(float output) {
         this.output = output;
        integral = 0;
        previous_error = 0;
     }
 
     /**
      * Update output value of the PID controller.
      * @param setpoint new setpoint.
      * @param dt delta time.
      */
     public void update(float setpoint, float dt) {
         float error = (setpoint - output);
         integral += error * dt;
         float derivative = (error - previous_error) / dt;
         output = kp * error + ki * integral + kd * derivative;
         previous_error = error;
     }
 
     /**
      * Get current output value.
      * @return output value of this PID controller.
      */
     public float getOutput() {
         return output;
     }
 }
