 /*
  * Created on 08.12.2003
  *
  */
 package org.caesarj.compiler.aspectj;
 import org.aspectj.weaver.patterns.PerSingleton;
 import org.aspectj.weaver.patterns.Pointcut;
 
 /**
  * @author Karl Klose
  * This class is a wrapper for an AspectJ-Pointcut
  */
 public class CaesarPointcut {
 // Attributes	
 	private Pointcut	pointcut;
 // Construction
 	public CaesarPointcut( Pointcut pointcut ) 
 	{	
 		this.pointcut = pointcut;
 	}
 // Accesors
 	public Pointcut	wrappee()
 	{
 		return pointcut;
 	}
 // Functions
	static public CaesarPointcut	makeMatchesNothing()
 	{
 		return new CaesarPointcut(Pointcut.makeMatchesNothing(Pointcut.SYMBOLIC));
 	}
 	
 	public CaesarPointcut resolve(CaesarScope scope) {
 			pointcut = pointcut.resolve(scope);		
 			return this;
 		}	
 /*
  * PerClause factory methods
  * 	Since perClauses are pointcuts, they are created and wrapped here. As a result,
  *  the compiler classes kneedn't know the PerClause-classes.
  */ 
 	public static CaesarPointcut createPerSingleton()
 	{
 		return new CaesarPointcut( new PerSingleton() );
 	}
 }
