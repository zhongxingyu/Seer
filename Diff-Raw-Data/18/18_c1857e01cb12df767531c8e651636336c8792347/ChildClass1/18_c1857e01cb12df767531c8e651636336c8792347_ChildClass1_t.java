 package tests.inheritance;
 
 import ecologylab.serialization.annotations.Hint;
 import ecologylab.serialization.annotations.simpl_hints;
import ecologylab.serialization.annotations.simpl_inherit;
 import ecologylab.serialization.annotations.simpl_scalar;
 
@simpl_inherit
 public class ChildClass1 extends BaseClass
 {
 	@simpl_scalar @simpl_hints(Hint.XML_LEAF)
 	int ccvar1 = 1;
 	
 	public ChildClass1()
 	{
 	}
 }
