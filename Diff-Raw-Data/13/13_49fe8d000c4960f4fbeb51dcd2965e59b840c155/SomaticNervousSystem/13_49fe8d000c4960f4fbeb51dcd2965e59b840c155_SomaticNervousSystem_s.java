 package model;
 
 /**
  * Nerves connecting to voluntary skeletal muscles and sensory receptors using
  * two types of directed Segments(Axons).
  *
  * 1) Afferent nerve fibers(segments) =
  * incoming axons that carry activation activity away from the periphery(muscles
  * and senses) to the CNS.
  *
  * 2) Efferent nerve fibers(segments) = outgoing axons that carry activation
  * activity from the CNS outward to the periphery(muscles and senses)
  *
  * @author Quinn Liu (quinnliu@vt.edu)
  * @version June 5, 2013
  */
 public class SomaticNervousSystem {
     private Retine retine;
 
    public SomaticNervousSystem(Retine retina) {
	this.retine = retina;
     }
 
     public Retine getRetine() {
 	return this.retine;
     }
 }
