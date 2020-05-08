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
		PackageInterface2Setters,
 		Builder<PublicInterface2>
 	{
		PublicInterface2Builder setName(String name);
 	}
 
 }
 
 /*package*/ interface PackageInterface2Getters extends
 	PackageInterface1Getters
 {
 
 }
 
/*package*/ interface PackageInterface2Setters extends PackageInterface1Setters {
 
 }
