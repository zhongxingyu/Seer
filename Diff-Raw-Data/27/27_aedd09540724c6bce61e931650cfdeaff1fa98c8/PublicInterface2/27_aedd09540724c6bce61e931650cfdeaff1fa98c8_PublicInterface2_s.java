 package api;
 
 import api.PublicInterface2.PublicInterface2Builder;
 import api.builder.Builder;
 import api.builder.Copyable;
 
 public interface PublicInterface2 extends
 	PackageInterface2Getters,
 	Copyable<PublicInterface2, PublicInterface2Builder>
 {
 
 	public interface PublicInterface2Builder extends
 		PackageInterface2Getters,
		PackageInterface2Setters<PublicInterface2Builder>,
 		Builder<PublicInterface2>
 	{

 	}
 
 }
 
 /*package*/ interface PackageInterface2Getters extends
 	PackageInterface1Getters
 {
 
 }
 
/*package*/ interface PackageInterface2Setters<
	T extends PackageInterface1Setters<T> & Builder<?>
> extends PackageInterface1Setters<T> {
 
 }
