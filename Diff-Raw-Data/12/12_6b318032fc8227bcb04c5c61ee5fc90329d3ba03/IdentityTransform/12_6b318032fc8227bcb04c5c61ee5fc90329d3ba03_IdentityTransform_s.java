 package dungeon.models.messages;
 
import java.io.Serializable;

 /**
  * This transform does nothing and is an implementation of the null object pattern.
  *
  * If you have to create a transform (e.g. because a method has Transform as return type), but there is none
  * applicable, you should return an IdentityTransform instead of null. This way callers of your method can rely on your
  * method returning a valid Transform and never have to check for null.
  */
 public class IdentityTransform implements Transform {
 
 }
