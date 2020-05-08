 package api;
 
 import api.PublicInterface1.PublicInterface1Builder;
 import api.builder.Builder;
 import api.builder.Copyable;
 
 public interface PublicInterface1 extends
 	PackageInterface1Getters,
 	Copyable<PublicInterface1, PublicInterface1Builder>
 {
 
 	public interface PublicInterface1Builder extends
 		PackageInterface1Getters,
		PackageInterface1Setters<PublicInterface1Builder>,
 		Builder<PublicInterface1>
 	{

 	}
 
 }
 
 /*package*/ interface PackageInterface1Getters {
 	String getName();
 }
 
/*package*/ interface PackageInterface1Setters<
	T extends PackageInterface1Setters<T> & Builder<?>
> {
	T setName(String name);
 }
